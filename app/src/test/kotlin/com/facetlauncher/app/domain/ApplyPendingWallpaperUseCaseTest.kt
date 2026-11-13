package com.facetlauncher.app.domain

import android.graphics.Bitmap
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import com.facetlauncher.app.data.FakeEntitlementRepository
import com.facetlauncher.app.data.SettingsRepository
import com.facetlauncher.app.data.model.DailyWallpaperPhoto
import com.facetlauncher.app.data.model.DailyWallpaperProblem
import java.io.File
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class ApplyPendingWallpaperUseCaseTest {

    @get:Rule
    val tempFolder = TemporaryFolder()

    private val copies = FakeDailyCopies()
    private val photo = DailyWallpaperPhoto("https://img.peapix.com/a.jpg", "Arches", "© someone")

    private fun daily() = FakeDailyWallpaperRepository(tempFolder.root)
    private val entitlement = FakeEntitlementRepository()

    private fun useCase(daily: FakeDailyWallpaperRepository): ApplyPendingWallpaperUseCase {
        val settings = SettingsRepository(PreferenceDataStoreFactory.create(produceFile = { File(tempFolder.root, "s-${System.nanoTime()}.preferences_pb") }))
        return ApplyPendingWallpaperUseCase(daily, copies, SetWallpaperFromPhotoUseCase(copies, settings, UnconfinedTestDispatcher()), dailyTestClock, entitlement)
    }

    private suspend fun FakeDailyWallpaperRepository.givenPending(includeLock: Boolean = false) {
        pendingBitmap = Bitmap.createBitmap(100, 200, Bitmap.Config.ARGB_8888)
        update { it.copy(enabled = true, includeLock = includeLock, pending = photo) }
    }

    @Test
    fun `nothing is set without pro`() = runTest {
        // Given a prepared photo, and a free user
        val daily = daily().apply { givenPending() }
        entitlement.proFlow.value = false

        // When the screen turns off
        val set = useCase(daily)()

        // Then the wallpaper wasn't touched and the photo still waits
        assertFalse(set)
        assertEquals(0, copies.setCount)
        assertEquals(photo, daily.get().pending)
    }

    @Test
    fun `the prepared photo is set, stored for glass and recorded`() = runTest {
        // Given a prepared photo
        val daily = daily().apply { givenPending() }

        // When the screen turns off
        val set = useCase(daily)()

        // Then it was set for Home only and saved as the glass copy, and the state moved on
        assertTrue(set)
        assertEquals(1, copies.setCount)
        assertEquals(false, copies.setIncludeLock)
        assertNotNull(copies.saved)
        val state = daily.get()
        assertNull(state.pending)
        assertEquals(photo, state.current)
        assertEquals(copies.systemId, state.systemId)
        assertEquals(listOf(photo.imageUrl), state.history)
        assertEquals(dailyTestClock.millis(), state.currentSetAtMillis)
        assertFalse(state.applying)
        assertNull(daily.pendingBitmap)
    }

    @Test
    fun `the lock screen is set too when chosen`() = runTest {
        // Given Home and lock
        val daily = daily().apply { givenPending(includeLock = true) }

        // When applied
        useCase(daily)()

        // Then both were set and the lock id recorded
        assertEquals(true, copies.setIncludeLock)
        assertEquals(copies.lockId, daily.get().lockId)
    }

    @Test
    fun `nothing happens without a prepared photo`() = runTest {
        // Given the daily change on with nothing prepared
        val daily = daily()
        daily.update { it.copy(enabled = true) }

        // When the screen turns off
        val set = useCase(daily)()

        // Then nothing was set
        assertFalse(set)
        assertEquals(0, copies.setCount)
    }

    @Test
    fun `a refusal turns the daily change off and says why`() = runTest {
        // Given a phone that refuses the wallpaper
        val daily = daily().apply { givenPending() }
        copies.setSucceeds = false

        // When applied
        val set = useCase(daily)()

        // Then it is off with the reason, and the photo is dropped
        assertFalse(set)
        val state = daily.get()
        assertFalse(state.enabled)
        assertEquals(DailyWallpaperProblem.REFUSED, state.problem)
        assertNull(state.pending)
    }

    @Test
    fun `a wallpaper changed elsewhere since the photo was prepared pauses instead of overwriting it`() = runTest {
        // Given the daily change last set wallpaper 10 and the user has since set 42
        val daily = daily().apply { givenPending() }
        daily.update { it.copy(systemId = 10) }
        copies.systemId = 42

        // When the screen turns off
        val set = useCase(daily)()

        // Then nothing was set, and it is paused
        assertFalse(set)
        assertEquals(0, copies.setCount)
        assertTrue(daily.get().paused)
    }

    @Test
    fun `an interrupted change is finished, not taken for the user's`() = runTest {
        // Given the process died mid-change: the system id moved but the change was not recorded
        val daily = daily().apply { givenPending() }
        daily.update { it.copy(systemId = 10, applying = true) }
        copies.systemId = 11

        // When the screen turns off
        val set = useCase(daily)()

        // Then the photo is set and recorded
        assertTrue(set)
        assertFalse(daily.get().paused)
        assertEquals(photo, daily.get().current)
    }

    @Test
    fun `a pending photo lost from the cache is prepared again on the next resume`() = runTest {
        // Given a recorded pending photo whose file is gone
        val daily = daily()
        daily.update { it.copy(enabled = true, pending = photo, checkedDay = java.time.LocalDate.of(2026, 11, 13)) }

        // When the screen turns off
        val set = useCase(daily)()

        // Then nothing was set, and today is open again
        assertFalse(set)
        assertNull(daily.get().pending)
        assertNull(daily.get().checkedDay)
    }
}
