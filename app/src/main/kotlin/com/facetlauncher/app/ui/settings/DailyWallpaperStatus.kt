package com.facetlauncher.app.ui.settings

import androidx.compose.runtime.Immutable
import com.facetlauncher.app.data.model.DailyWallpaperProblem
import com.facetlauncher.app.data.model.DailyWallpaperState
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId

/** What the status line under the daily wallpaper settings says. */
@Immutable
sealed interface DailyWallpaperStatus {
    data object Off : DailyWallpaperStatus

    /** On, and nothing set yet. */
    data object Starting : DailyWallpaperStatus

    data object Paused : DailyWallpaperStatus

    /** A photo is prepared and will be set the next time the screen turns off. */
    data class Ready(val title: String) : DailyWallpaperStatus

    data class Problem(val problem: DailyWallpaperProblem) : DailyWallpaperStatus

    /** The photo the daily change last set, with when; [today] when that was today. */
    data class Current(val title: String, val credit: String, val setAtMillis: Long?, val today: Boolean) : DailyWallpaperStatus
}

/** The status line for [state] on [today]. A refusal is shown even though it turned the change off, so the user knows why. */
fun dailyWallpaperStatus(state: DailyWallpaperState, today: LocalDate, zone: ZoneId): DailyWallpaperStatus {
    val current = state.current
    return when {
        !state.enabled -> if (state.problem == DailyWallpaperProblem.REFUSED) DailyWallpaperStatus.Problem(DailyWallpaperProblem.REFUSED) else DailyWallpaperStatus.Off
        state.paused -> DailyWallpaperStatus.Paused
        state.pending != null -> DailyWallpaperStatus.Ready(state.pending.title)
        state.problem != null -> DailyWallpaperStatus.Problem(state.problem)
        current != null -> DailyWallpaperStatus.Current(
            title = current.title,
            credit = current.credit,
            setAtMillis = state.currentSetAtMillis,
            today = state.currentSetAtMillis?.let { Instant.ofEpochMilli(it).atZone(zone).toLocalDate() == today } ?: false,
        )
        else -> DailyWallpaperStatus.Starting
    }
}
