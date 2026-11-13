package com.facetlauncher.app.ui.settings

import com.facetlauncher.app.data.model.DailyWallpaperPhoto
import com.facetlauncher.app.data.model.DailyWallpaperProblem
import com.facetlauncher.app.data.model.DailyWallpaperState
import java.time.LocalDate
import java.time.ZoneOffset
import java.time.ZonedDateTime
import org.junit.Assert.assertEquals
import org.junit.Test

class DailyWallpaperStatusTest {

    private val today = LocalDate.of(2026, 11, 13)
    private val photo = DailyWallpaperPhoto("https://img.peapix.com/a.jpg", "Arches", "© someone")
    private fun status(state: DailyWallpaperState) = dailyWallpaperStatus(state, today, ZoneOffset.UTC)

    @Test
    fun `off says nothing, unless android refused the wallpaper`() {
        assertEquals(DailyWallpaperStatus.Off, status(DailyWallpaperState()))
        assertEquals(DailyWallpaperStatus.Problem(DailyWallpaperProblem.REFUSED), status(DailyWallpaperState(problem = DailyWallpaperProblem.REFUSED)))
    }

    @Test
    fun `paused wins over everything else while on`() {
        assertEquals(DailyWallpaperStatus.Paused, status(DailyWallpaperState(enabled = true, paused = true, pending = photo, problem = DailyWallpaperProblem.WAITING_FOR_WIFI)))
    }

    @Test
    fun `a prepared photo says it is set at the next screen-off`() {
        assertEquals(DailyWallpaperStatus.Ready("Arches"), status(DailyWallpaperState(enabled = true, pending = photo)))
    }

    @Test
    fun `a problem is shown over the current photo`() {
        val state = DailyWallpaperState(enabled = true, current = photo, problem = DailyWallpaperProblem.WAITING_FOR_WIFI)
        assertEquals(DailyWallpaperStatus.Problem(DailyWallpaperProblem.WAITING_FOR_WIFI), status(state))
    }

    @Test
    fun `the current photo says whether it was set today`() {
        // Given a photo set this morning, and one set yesterday
        val thisMorning = ZonedDateTime.of(2026, 11, 13, 6, 40, 0, 0, ZoneOffset.UTC).toInstant().toEpochMilli()
        val yesterday = ZonedDateTime.of(2026, 11, 12, 6, 40, 0, 0, ZoneOffset.UTC).toInstant().toEpochMilli()

        // Then each says so
        assertEquals(DailyWallpaperStatus.Current("Arches", "© someone", thisMorning, today = true), status(DailyWallpaperState(enabled = true, current = photo, currentSetAtMillis = thisMorning)))
        assertEquals(DailyWallpaperStatus.Current("Arches", "© someone", yesterday, today = false), status(DailyWallpaperState(enabled = true, current = photo, currentSetAtMillis = yesterday)))
    }

    @Test
    fun `on with nothing set yet is starting`() {
        assertEquals(DailyWallpaperStatus.Starting, status(DailyWallpaperState(enabled = true)))
    }
}
