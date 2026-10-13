package com.facetlauncher.app.data

import android.app.Notification
import android.media.MediaMetadata
import android.media.session.PlaybackState
import android.os.Bundle
import com.facetlauncher.app.data.model.MediaPlayerState

private val InactiveStates = setOf(PlaybackState.STATE_NONE, PlaybackState.STATE_STOPPED, PlaybackState.STATE_ERROR)

/** Maps one session's framework metadata/playback state to [MediaPlayerState]; `null` when it has no title or is stopped/errored (Home shows nothing for it). */
internal fun buildMediaPlayerState(
    packageName: String,
    metadata: MediaMetadata?,
    playback: PlaybackState?,
): MediaPlayerState? {
    val title = metadata?.firstText(MediaMetadata.METADATA_KEY_TITLE, MediaMetadata.METADATA_KEY_DISPLAY_TITLE)
    if (metadata == null || playback == null || title == null) return null
    return if (playback.state in InactiveStates) null else mediaPlayerState(packageName, title, metadata, playback)
}

private fun mediaPlayerState(packageName: String, title: String, metadata: MediaMetadata, playback: PlaybackState): MediaPlayerState {
    val isPlaying = playback.state == PlaybackState.STATE_PLAYING || playback.state == PlaybackState.STATE_BUFFERING
    return MediaPlayerState(
        packageName = packageName,
        title = title,
        artist = metadata.firstText(
            MediaMetadata.METADATA_KEY_ARTIST,
            MediaMetadata.METADATA_KEY_ALBUM_ARTIST,
            MediaMetadata.METADATA_KEY_DISPLAY_SUBTITLE,
        ),
        art = metadata.getBitmap(MediaMetadata.METADATA_KEY_ALBUM_ART)
            ?: metadata.getBitmap(MediaMetadata.METADATA_KEY_ART)
            ?: metadata.getBitmap(MediaMetadata.METADATA_KEY_DISPLAY_ICON),
        isPlaying = isPlaying,
        positionMs = playback.position.coerceAtLeast(0L),
        durationMs = metadata.getLong(MediaMetadata.METADATA_KEY_DURATION).coerceAtLeast(0L),
        positionUpdatedAtMs = playback.lastPositionUpdateTime,
        playbackSpeed = if (isPlaying && playback.playbackSpeed == 0f) 1f else playback.playbackSpeed,
        canSkipPrevious = playback.actions and PlaybackState.ACTION_SKIP_TO_PREVIOUS != 0L,
        canSkipNext = playback.actions and PlaybackState.ACTION_SKIP_TO_NEXT != 0L,
    )
}

private fun MediaMetadata.firstText(vararg keys: String): String? =
    keys.firstNotNullOfOrNull { key -> getString(key)?.takeIf { it.isNotBlank() } }

/**
 * A playing session wins; if none is playing, the one that played most recently ([lastPlayedAt], by package).
 * Ties — including sessions never seen playing — keep [android.media.session.MediaSessionManager.getActiveSessions]'s
 * priority order.
 */
internal fun pickActiveSession(states: List<MediaPlayerState>, lastPlayedAt: Map<String, Long> = emptyMap()): MediaPlayerState? {
    val mostRecentFirst = states.sortedByDescending { lastPlayedAt[it.packageName] ?: 0L }
    return mostRecentFirst.firstOrNull { it.isPlaying } ?: mostRecentFirst.firstOrNull()
}

/** Whether a notification's extras mark it as a `MediaStyle` one (platform or androidx/Media3 — both use the platform template). */
internal fun isMediaNotification(extras: Bundle?): Boolean =
    extras != null && (extras.getString(Notification.EXTRA_TEMPLATE)?.endsWith("MediaStyle") == true || extras.containsKey(Notification.EXTRA_MEDIA_SESSION))

/** Remembers when each package last played, so a later pick among paused sessions can choose the most recent. */
internal class ActiveSessionPicker {
    private val lastPlayedAt = HashMap<String, Long>()
    private var playedCounter = 0L

    /** Only sessions whose package is in [packagesWithNotification] are eligible. */
    fun <T> pick(sessions: List<Pair<T, MediaPlayerState>>, packagesWithNotification: Set<String>): Pair<T, MediaPlayerState>? {
        sessions.filter { it.second.isPlaying }.forEach { lastPlayedAt[it.second.packageName] = ++playedCounter }
        val candidates = sessions.filter { it.second.packageName in packagesWithNotification }
        val picked = pickActiveSession(candidates.map { it.second }, lastPlayedAt)
        return candidates.firstOrNull { it.second === picked }
    }
}
