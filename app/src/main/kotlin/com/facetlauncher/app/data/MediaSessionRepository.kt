package com.facetlauncher.app.data

import android.app.ActivityOptions
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.media.MediaMetadata
import android.media.session.MediaController
import android.media.session.MediaSession
import android.media.session.MediaSessionManager
import android.media.session.PlaybackState
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import com.facetlauncher.app.data.model.MediaPlayerState
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * The media session Home's player row shows. Sessions are read through the notification-listener
 * grant ([FacetNotificationListenerService]'s component is what `getActiveSessions` checks), so
 * with no grant [observe] emits `null`. A session only counts while its app has a media notification posted,
 * so swiping that notification away removes the row even though many apps keep a paused session alive.
 * Cold: it only touches the framework while collected.
 */
@Singleton
open class MediaSessionRepository @Inject constructor(
    @ApplicationContext private val context: Context,
    private val notificationBadgeRepository: NotificationBadgeRepository,
) {
    @Volatile private var activeController: MediaController? = null

    // A grant made in system Settings has no callback — the caller re-collects this after a resume.
    open fun observe(): Flow<MediaPlayerState?> = sessions()

    open fun skipToPrevious() {
        activeController?.transportControls?.skipToPrevious()
    }

    open fun skipToNext() {
        activeController?.transportControls?.skipToNext()
    }

    open fun togglePlayPause() {
        val controller = activeController ?: return
        if (controller.playbackState?.state == PlaybackState.STATE_PLAYING) {
            controller.transportControls.pause()
        } else {
            controller.transportControls.play()
        }
    }

    /**
     * Opens the player app via the session's own content intent, or its launcher intent when it publishes none.
     * The send opts in to this app's foreground privileges: from Android 14 the system otherwise blocks the
     * creator-privileged start (silently — no exception), which is how a tap on the row did nothing.
     */
    open fun openPlayer() {
        val controller = activeController ?: return
        val sent = controller.sessionActivity?.let { intent ->
            runCatching { intent.send(context, 0, null, null, null, null, foregroundSenderOptions()) }.isSuccess
        } == true
        if (!sent) launchPlayerApp(controller.packageName)
    }

    private fun foregroundSenderOptions(): Bundle? =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            ActivityOptions.makeBasic()
                .setPendingIntentBackgroundActivityStartMode(ActivityOptions.MODE_BACKGROUND_ACTIVITY_START_ALLOWED)
                .toBundle()
        } else {
            null
        }

    private fun launchPlayerApp(packageName: String) {
        context.packageManager.getLaunchIntentForPackage(packageName)
            ?.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            ?.let { runCatching { context.startActivity(it) } }
    }

    private fun sessions(): Flow<MediaPlayerState?> = callbackFlow {
        val manager = context.getSystemService(MediaSessionManager::class.java)
        val listenerComponent = ComponentName(context, FacetNotificationListenerService::class.java)
        val handler = Handler(Looper.getMainLooper())
        val tracked = HashMap<MediaSession.Token, Pair<MediaController, MediaController.Callback>>()
        // The last art-URI decode, kept so a metadata change that repeats the URI doesn't re-decode.
        var artCache: Pair<Uri, Bitmap?>? = null
        val picker = ActiveSessionPicker()

        fun publish() {
            val sessions = tracked.values.mapNotNull { (controller, _) ->
                buildMediaPlayerState(controller.packageName, controller.metadata, controller.playbackState)
                    ?.let { controller to it }
            }
            val choice = picker.pick(sessions, notificationBadgeRepository.mediaNotificationPackages.value)
            val picked = choice?.second
            val controller = choice?.first
            activeController = controller
            if (picked == null || controller == null) {
                trySend(null)
                return
            }
            if (picked.art != null) {
                trySend(picked)
                return
            }
            val artUri = controller.metadata?.artUri()
            val cached = artCache?.takeIf { it.first == artUri }
            trySend(picked.copy(art = cached?.second))
            if (artUri != null && cached == null) {
                launch {
                    val bitmap = withContext(Dispatchers.IO) { decodeArt(artUri) }
                    artCache = artUri to bitmap
                    publish()
                }
            }
        }

        fun sync(controllers: List<MediaController>?) {
            val current = controllers.orEmpty().associateBy { it.sessionToken }
            tracked.keys.filter { it !in current }.toList().forEach { token ->
                tracked.remove(token)?.let { (controller, callback) -> controller.unregisterCallback(callback) }
            }
            current.filterKeys { it !in tracked }.forEach { (token, controller) ->
                val callback = object : MediaController.Callback() {
                    override fun onMetadataChanged(metadata: MediaMetadata?) = publish()
                    override fun onPlaybackStateChanged(state: PlaybackState?) = publish()
                    override fun onSessionDestroyed() = publish()
                }
                controller.registerCallback(callback, handler)
                tracked[token] = controller to callback
            }
            publish()
        }

        // Posting or dismissing a media notification changes which sessions count; publish() runs on the main looper.
        launch { notificationBadgeRepository.mediaNotificationPackages.collect { handler.post { publish() } } }

        val sessionsListener = MediaSessionManager.OnActiveSessionsChangedListener { sync(it) }
        try {
            manager.addOnActiveSessionsChangedListener(sessionsListener, listenerComponent, handler)
            sync(manager.getActiveSessions(listenerComponent))
        } catch (_: SecurityException) {
            trySend(null)
        }

        awaitClose {
            runCatching { manager.removeOnActiveSessionsChangedListener(sessionsListener) }
            tracked.values.forEach { (controller, callback) -> controller.unregisterCallback(callback) }
            tracked.clear()
            activeController = null
        }
    }

    private fun MediaMetadata.artUri(): Uri? =
        (getString(MediaMetadata.METADATA_KEY_ART_URI)
            ?: getString(MediaMetadata.METADATA_KEY_ALBUM_ART_URI)
            ?: getString(MediaMetadata.METADATA_KEY_DISPLAY_ICON_URI))
            ?.let { Uri.parse(it) }
            // No network layer here — only on-device content/file art.
            ?.takeIf { it.scheme == "content" || it.scheme == "file" }

    private fun decodeArt(uri: Uri): Bitmap? = runCatching {
        context.contentResolver.openInputStream(uri)?.use { BitmapFactory.decodeStream(it) }
    }.getOrNull()
}
