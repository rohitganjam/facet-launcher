package com.facetlauncher.app.data

import android.media.MediaMetadata
import android.media.session.PlaybackState
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

/** A playing session wins; otherwise the first one — [android.media.session.MediaSessionManager.getActiveSessions] is already ordered by priority. */
internal fun pickActiveSession(states: List<MediaPlayerState>): MediaPlayerState? =
    states.firstOrNull { it.isPlaying } ?: states.firstOrNull()
