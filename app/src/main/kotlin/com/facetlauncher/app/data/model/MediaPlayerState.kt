package com.facetlauncher.app.data.model

import android.graphics.Bitmap

/** The active media session as Home's player row shows it. [positionMs] is valid at [positionUpdatedAtMs] (`SystemClock.elapsedRealtime`) and advances at [playbackSpeed] while [isPlaying]. */
data class MediaPlayerState(
    val packageName: String,
    val title: String,
    val artist: String?,
    val art: Bitmap?,
    val isPlaying: Boolean,
    val positionMs: Long,
    val durationMs: Long,
    val positionUpdatedAtMs: Long,
    val playbackSpeed: Float,
    val canSkipPrevious: Boolean,
    val canSkipNext: Boolean,
) {
    /** 0..1, or `null` when the duration is unknown (live streams) — the row then shows no bar. */
    fun progressAt(nowElapsedMs: Long): Float? {
        if (durationMs <= 0L) return null
        val elapsed = if (isPlaying) ((nowElapsedMs - positionUpdatedAtMs) * playbackSpeed).toLong() else 0L
        return ((positionMs + elapsed).toFloat() / durationMs).coerceIn(0f, 1f)
    }
}
