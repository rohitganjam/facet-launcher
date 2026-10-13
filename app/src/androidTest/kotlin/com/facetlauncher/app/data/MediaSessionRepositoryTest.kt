package com.facetlauncher.app.data

import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.Bitmap
import android.media.MediaMetadata
import android.media.session.MediaSession
import android.media.session.PlaybackState
import android.net.Uri
import android.os.ParcelFileDescriptor
import androidx.test.platform.app.InstrumentationRegistry
import com.facetlauncher.app.data.model.MediaPlayerState
import com.facetlauncher.app.data.model.NotificationInfo
import java.io.File
import java.io.FileOutputStream
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

/**
 * Drives a real [MediaSession] through the real framework into [MediaSessionRepository]. Reading sessions needs the
 * notification-listener grant, which a test can only give through the shell — and must always take away again, since
 * other tests (e.g. `PermissionsScreenTest`) assume this device has never granted it.
 */
class MediaSessionRepositoryTest {

    private val instrumentation = InstrumentationRegistry.getInstrumentation()
    private val context: Context = instrumentation.targetContext
    private val listenerComponent = "${context.packageName}/${FacetNotificationListenerService::class.java.name}"
    private val notificationBadgeRepository = NotificationBadgeRepository()
    private val repository = MediaSessionRepository(context, notificationBadgeRepository)

    private val sessions = mutableListOf<MediaSession>()
    private val files = mutableListOf<File>()
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

    /** `null` until the repository has emitted at all, then the latest emission (itself possibly `null`). */
    private class Emission(val state: MediaPlayerState?)

    private val latest = MutableStateFlow<Emission?>(null)

    @Before
    fun setUp() {
        shell("cmd notification disallow_listener $listenerComponent")
    }

    @After
    fun tearDown() {
        scope.cancel()
        instrumentation.runOnMainSync { sessions.forEach { it.release() } }
        files.forEach { it.delete() }
        shell("cmd notification disallow_listener $listenerComponent")
    }

    private fun shell(command: String) {
        instrumentation.uiAutomation.executeShellCommand(command).use { fd ->
            ParcelFileDescriptor.AutoCloseInputStream(fd).readBytes()
        }
    }

    /** Stands in for the listener service: marks this app as having a media notification posted (or not). */
    private fun setMediaNotification(posted: Boolean) {
        notificationBadgeRepository.setActiveNotifications(
            if (posted) listOf(NotificationInfo(packageName = context.packageName, silent = false, isMedia = true)) else emptyList(),
        )
    }

    private fun grantAccessAndCollect() {
        setMediaNotification(posted = true)
        shell("cmd notification allow_listener $listenerComponent")
        scope.launch { repository.observe().collect { latest.value = Emission(it) } }
    }

    private fun await(timeoutMs: Long = 8_000, predicate: (MediaPlayerState?) -> Boolean): MediaPlayerState? = runBlocking {
        withTimeout(timeoutMs) { latest.first { it != null && predicate(it.state) } }!!.state
    }

    private fun session(tag: String = "repo-test"): MediaSession {
        lateinit var created: MediaSession
        instrumentation.runOnMainSync {
            created = MediaSession(context, tag)
            sessions += created
        }
        return created
    }

    private fun MediaSession.publish(
        title: String?,
        artist: String? = "Artist",
        playbackState: Int = PlaybackState.STATE_PLAYING,
        actions: Long = ALL_ACTIONS,
        configure: MediaMetadata.Builder.() -> Unit = {},
    ) {
        instrumentation.runOnMainSync {
            setMetadata(
                MediaMetadata.Builder().apply {
                    title?.let { putString(MediaMetadata.METADATA_KEY_TITLE, it) }
                    artist?.let { putString(MediaMetadata.METADATA_KEY_ARTIST, it) }
                    putLong(MediaMetadata.METADATA_KEY_DURATION, 200_000L)
                    configure()
                }.build(),
            )
            setPlaybackState(PlaybackState.Builder().setState(playbackState, 50_000L, 1f).setActions(actions).build())
            isActive = true
        }
    }

    private fun pngFile(name: String, width: Int = 24, height: Int = 16): File {
        val file = File(context.cacheDir, name).also { files += it }
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888).apply { eraseColor(0xFFCC3333.toInt()) }
        FileOutputStream(file).use { bitmap.compress(Bitmap.CompressFormat.PNG, 100, it) }
        return file
    }

    @Test
    fun emitsNullWhenNotificationAccessIsNotGranted() {
        // Given a session exists but access was never granted
        session().publish("No access song")
        scope.launch { repository.observe().collect { latest.value = Emission(it) } }

        // Then the repository emits null instead of throwing
        assertNull(await { true })
    }

    @Test
    fun emitsTheActiveSessionsTitleArtistAndPlayingState() {
        // Given access and a playing session
        grantAccessAndCollect()
        session().publish("Playing song", artist = "The Artist")

        // Then its details come through
        val state = await { it?.title == "Playing song" }!!
        assertEquals("The Artist", state.artist)
        assertEquals(context.packageName, state.packageName)
        assertTrue(state.isPlaying)
        assertEquals(200_000L, state.durationMs)
        assertTrue(state.canSkipPrevious)
        assertTrue(state.canSkipNext)
    }

    @Test
    fun aPausedSessionIsKeptButNotPlaying() {
        grantAccessAndCollect()
        session().publish("Paused song", playbackState = PlaybackState.STATE_PAUSED)

        assertFalse(await { it?.title == "Paused song" }!!.isPlaying)
    }

    @Test
    fun changingTheMetadataOrPlaybackStateUpdatesTheEmission() {
        // Given a playing session
        grantAccessAndCollect()
        val session = session()
        session.publish("First title")
        await { it?.title == "First title" }

        // When the title changes and playback pauses
        session.publish("Second title", playbackState = PlaybackState.STATE_PAUSED)

        // Then the latest state reflects both
        val state = await { it?.title == "Second title" }!!
        assertFalse(state.isPlaying)
    }

    @Test
    fun aStoppedSessionEmitsNullAndComesBackWhenItPlaysAgain() {
        // Given a playing session
        grantAccessAndCollect()
        val session = session()
        session.publish("Stoppable song")
        await { it?.title == "Stoppable song" }

        // When it stops
        session.publish("Stoppable song", playbackState = PlaybackState.STATE_STOPPED)

        // Then nothing is shown, until it plays again
        assertNull(await { it == null })
        session.publish("Stoppable song")
        assertTrue(await { it?.title == "Stoppable song" }!!.isPlaying)
    }

    @Test
    fun aPlayingSessionWinsOverAPausedOne() {
        // Given a paused session and a playing one
        grantAccessAndCollect()
        session("paused-one").publish("Paused one", playbackState = PlaybackState.STATE_PAUSED)
        session("playing-one").publish("Playing one")

        // Then the playing one is shown
        assertEquals("Playing one", await { it?.title == "Playing one" }!!.title)
    }

    @Test
    fun skipActionsFollowWhatTheSessionAdvertises() {
        grantAccessAndCollect()
        session().publish("No previous", actions = PlaybackState.ACTION_PLAY_PAUSE or PlaybackState.ACTION_SKIP_TO_NEXT)

        val state = await { it?.title == "No previous" }!!
        assertFalse(state.canSkipPrevious)
        assertTrue(state.canSkipNext)
    }

    @Test
    fun transportCallsReachTheSession() {
        // Given a playing session that records what it is told
        val played = CountDownLatch(1)
        val paused = CountDownLatch(1)
        val next = CountDownLatch(1)
        val previous = CountDownLatch(1)
        grantAccessAndCollect()
        val session = session()
        instrumentation.runOnMainSync {
            session.setCallback(
                object : MediaSession.Callback() {
                    override fun onPlay() = played.countDown()
                    override fun onPause() = paused.countDown()
                    override fun onSkipToNext() = next.countDown()
                    override fun onSkipToPrevious() = previous.countDown()
                },
            )
        }
        session.publish("Controlled song")
        await { it?.title == "Controlled song" }

        // When toggling while playing, then skipping
        repository.togglePlayPause()
        repository.skipToNext()
        repository.skipToPrevious()

        // Then the session received pause, next and previous
        assertTrue("pause", paused.await(5, TimeUnit.SECONDS))
        assertTrue("next", next.await(5, TimeUnit.SECONDS))
        assertTrue("previous", previous.await(5, TimeUnit.SECONDS))

        // And toggling while paused plays
        session.publish("Controlled song", playbackState = PlaybackState.STATE_PAUSED)
        await { it?.isPlaying == false }
        repository.togglePlayPause()
        assertTrue("play", played.await(5, TimeUnit.SECONDS))
    }

    @Test
    fun openPlayerSendsTheSessionsContentIntent() {
        // Given a session whose content intent is a broadcast we can observe
        val received = CountDownLatch(1)
        val action = "com.facetlauncher.app.test.OPEN_PLAYER"
        val receiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context, intent: Intent) = received.countDown()
        }
        context.registerReceiver(receiver, IntentFilter(action), Context.RECEIVER_NOT_EXPORTED)
        try {
            grantAccessAndCollect()
            val session = session()
            instrumentation.runOnMainSync {
                session.setSessionActivity(
                    PendingIntent.getBroadcast(context, 0, Intent(action).setPackage(context.packageName), PendingIntent.FLAG_IMMUTABLE),
                )
            }
            session.publish("Openable song")
            await { it?.title == "Openable song" }

            // When opening the player
            repository.openPlayer()

            // Then the intent fires
            assertTrue(received.await(5, TimeUnit.SECONDS))
        } finally {
            context.unregisterReceiver(receiver)
        }
    }

    @Test
    fun usesTheArtBitmapInTheMetadataDirectly() {
        // Given metadata carrying an album art bitmap
        grantAccessAndCollect()
        val bitmap = Bitmap.createBitmap(30, 20, Bitmap.Config.ARGB_8888)
        session().publish("Bitmap art song") { putBitmap(MediaMetadata.METADATA_KEY_ALBUM_ART, bitmap) }

        // Then the state has art of that size
        val art = await { it?.title == "Bitmap art song" }!!.art
        assertNotNull(art)
        assertEquals(30, art!!.width)
        assertEquals(20, art.height)
    }

    @Test
    fun decodesArtFromAFileUriWhenThereIsNoBitmap() {
        // Given metadata with only an art URI pointing at an on-device image
        grantAccessAndCollect()
        val file = pngFile("repo-art.png", width = 24, height = 16)
        session().publish("Uri art song") { putString(MediaMetadata.METADATA_KEY_ART_URI, Uri.fromFile(file).toString()) }

        // Then the row first has no art, and the decoded image arrives shortly after
        val art = await { it?.title == "Uri art song" && it.art != null }!!.art
        assertEquals(24, art!!.width)
        assertEquals(16, art.height)
    }

    @Test
    fun ignoresAnArtUriThatWouldNeedTheNetwork() {
        // Given an http art URI (the app has no network permission and never fetches)
        grantAccessAndCollect()
        session().publish("Web art song") { putString(MediaMetadata.METADATA_KEY_ART_URI, "https://example.com/cover.png") }

        // Then the song still shows, without art
        assertNull(await { it?.title == "Web art song" }!!.art)
    }

    @Test
    fun anUndecodableArtFileLeavesTheSongShowingWithoutArt() {
        // Given an art URI whose file isn't an image
        grantAccessAndCollect()
        val file = File(context.cacheDir, "repo-not-an-image.png").also { files += it }
        file.writeText("this is not a png")
        session().publish("Broken art song") { putString(MediaMetadata.METADATA_KEY_ART_URI, Uri.fromFile(file).toString()) }

        // Then the song shows without art and keeps its other details
        val state = await { it?.title == "Broken art song" }!!
        assertNull(state.art)
        assertEquals("Artist", state.artist)
    }

    @Test
    fun aPausedSessionStaysWhileItsNotificationExists() {
        // Given access, a paused session and its media notification
        grantAccessAndCollect()
        session().publish("Paused song", playbackState = PlaybackState.STATE_PAUSED)

        // Then the paused song is shown
        assertEquals(false, await { it?.title == "Paused song" }!!.isPlaying)
    }

    @Test
    fun dismissingTheNotificationRemovesAPausedSessionAndPostingAgainBringsItBack() {
        // Given a paused session that is showing
        grantAccessAndCollect()
        session().publish("Dismiss song", playbackState = PlaybackState.STATE_PAUSED)
        await { it?.title == "Dismiss song" }

        // When its notification is swiped away
        setMediaNotification(posted = false)

        // Then nothing is shown, though the session itself is still alive
        assertNull(await { it == null })

        // And when the app posts the notification again
        setMediaNotification(posted = true)

        // Then the song returns
        await { it?.title == "Dismiss song" }
    }

    @Test
    fun aSessionWithoutAnyNotificationIsNeverShown() {
        // Given access and a playing session, but no media notification
        grantAccessAndCollect()
        setMediaNotification(posted = false)
        session().publish("Silent song")

        // Then nothing is shown
        assertNull(await { true })
    }

    private companion object {
        const val ALL_ACTIONS = PlaybackState.ACTION_PLAY or PlaybackState.ACTION_PAUSE or PlaybackState.ACTION_PLAY_PAUSE or
            PlaybackState.ACTION_SKIP_TO_NEXT or PlaybackState.ACTION_SKIP_TO_PREVIOUS
    }
}
