package com.facetlauncher.app.data.model

import androidx.compose.runtime.Immutable
import java.time.LocalDate

/** Why the last daily change didn't happen; `null` in [DailyWallpaperState.problem] when nothing is wrong. */
enum class DailyWallpaperProblem {
    WAITING_FOR_WIFI,
    NO_CONNECTION,
    BATTERY_SAVER,
    SOURCE_UNAVAILABLE,
    DOWNLOAD_FAILED,
    TOO_SMALL,

    /** Android refused the wallpaper; the daily change is turned off. */
    REFUSED,
}

/** A photo the daily change prepared or set: enough to show its title and credit and to tell it apart from the next one. */
@Immutable
data class DailyWallpaperPhoto(val imageUrl: String, val title: String, val credit: String)

/**
 * The daily wallpaper's settings and progress, in their own DataStore (not in backups). A run starts on the first launcher resume of a
 * day ([checkedDay] is not today), prepares [pending], and the pending photo is set at the next screen-off.
 */
@Immutable
data class DailyWallpaperState(
    val enabled: Boolean = false,
    val source: DailyPhotoSource = DailyPhotoSource.BING,
    val wifiOnly: Boolean = true,
    val includeLock: Boolean = false,
    /** The wallpaper was changed outside the daily change; nothing runs until the user resumes. */
    val paused: Boolean = false,
    /** The day a run last finished its work: a photo prepared, or nothing new to set. No run starts again that day. */
    val checkedDay: LocalDate? = null,
    /** The day [attempts] counts network attempts for. */
    val attemptsDay: LocalDate? = null,
    val attempts: Int = 0,
    val pending: DailyWallpaperPhoto? = null,
    /** Set just before the system wallpaper is replaced and cleared once the change is recorded, so an interrupted change is not taken for the user's. */
    val applying: Boolean = false,
    val current: DailyWallpaperPhoto? = null,
    val currentSetAtMillis: Long? = null,
    /** The system (Home) and lock wallpaper ids right after the daily change last set one; -1 when there is none to compare. */
    val systemId: Int = NO_ID,
    val lockId: Int = NO_ID,
    /** The addresses of recently set photos, newest first, so Spotlight (which has no dates) doesn't repeat one. */
    val history: List<String> = emptyList(),
    val problem: DailyWallpaperProblem? = null,
) {
    fun attemptsOn(day: LocalDate): Int = if (attemptsDay == day) attempts else 0

    companion object {
        const val NO_ID = -1
        const val HISTORY_SIZE = 30
        const val MAX_ATTEMPTS_PER_DAY = 3
    }
}

/** The network at this moment, as far as the daily change cares. */
data class NetworkConditions(val connected: Boolean, val unmetered: Boolean, val roaming: Boolean)

/** A rectangle of an image in its own pixels, right and bottom exclusive. */
data class ImageRegion(val left: Int, val top: Int, val right: Int, val bottom: Int) {
    val width: Int get() = right - left
    val height: Int get() = bottom - top
}
