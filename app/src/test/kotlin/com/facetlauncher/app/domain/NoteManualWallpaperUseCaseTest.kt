package com.facetlauncher.app.domain

import android.graphics.Bitmap
import com.facetlauncher.app.data.model.DailyWallpaperPhoto
import java.time.LocalDate
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class NoteManualWallpaperUseCaseTest {

    @get:Rule
    val tempFolder = TemporaryFolder()

    private val copies = FakeDailyCopies().apply { systemId = 77 }

    private suspend fun dailyOnWithPending() = FakeDailyWallpaperRepository(tempFolder.root).apply {
        pendingBitmap = Bitmap.createBitmap(1, 1, Bitmap.Config.ARGB_8888)
        update { it.copy(enabled = true, systemId = 10, pending = DailyWallpaperPhoto("https://img.peapix.com/p.jpg", "p", "")) }
    }

    @Test
    fun `picking a bing or spotlight photo keeps the daily change on and counts as today's`() = runTest {
        // Given the daily change on with a photo prepared
        val daily = dailyOnWithPending()

        // When the user sets a daily photo themselves
        NoteManualWallpaperUseCase(daily, copies, dailyTestClock)(fromDailyPhoto = true)

        // Then it stays on, today is done, the new wallpaper is the baseline, and the prepared photo is dropped
        val state = daily.get()
        assertTrue(state.enabled)
        assertEquals(LocalDate.of(2026, 11, 13), state.checkedDay)
        assertEquals(77, state.systemId)
        assertNull(state.pending)
        assertNull(daily.pendingBitmap)
    }

    @Test
    fun `setting an image of their own turns the daily change off`() = runTest {
        // Given the daily change on
        val daily = dailyOnWithPending()

        // When the user sets their own image
        NoteManualWallpaperUseCase(daily, copies, dailyTestClock)(fromDailyPhoto = false)

        // Then it is off and nothing is pending
        assertFalse(daily.get().enabled)
        assertNull(daily.get().pending)
    }

    @Test
    fun `nothing changes while the daily change is off`() = runTest {
        // Given it is off
        val daily = FakeDailyWallpaperRepository(tempFolder.root)

        // When the user sets a wallpaper
        NoteManualWallpaperUseCase(daily, copies, dailyTestClock)(fromDailyPhoto = true)

        // Then nothing was recorded
        assertNull(daily.get().checkedDay)
        assertEquals(-1, daily.get().systemId)
    }
}
