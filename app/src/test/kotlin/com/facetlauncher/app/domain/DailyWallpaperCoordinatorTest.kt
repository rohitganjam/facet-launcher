package com.facetlauncher.app.domain

import android.graphics.Bitmap
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import com.facetlauncher.app.data.FakeEntitlementRepository
import com.facetlauncher.app.data.SettingsRepository
import com.facetlauncher.app.data.dailyphotos.DailyPhotoFeed
import com.facetlauncher.app.data.model.DailyPhotoSource
import com.facetlauncher.app.data.model.DailyWallpaperPhoto
import java.io.File
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
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

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(RobolectricTestRunner::class)
class DailyWallpaperCoordinatorTest {

    @get:Rule
    val tempFolder = TemporaryFolder()

    private val copies = FakeDailyCopies()
    private val conditions = FakeConditions()
    private lateinit var daily: FakeDailyWallpaperRepository
    private lateinit var photos: FakeDailyPhotos

    private fun TestScope.coordinator(): DailyWallpaperCoordinator {
        daily = FakeDailyWallpaperRepository(tempFolder.root)
        photos = FakeDailyPhotos(tempFolder.newFolder(), copies)
        photos.feeds[DailyPhotoSource.BING] = DailyPhotoFeed.Photos(listOf(dailyPhoto("a", "2026-11-13")), savedEarlier = false)
        photos.originals["https://img.peapix.com/a.jpg"] = byteArrayOf(1)
        copies.sizes[1.toByte()] = 400 to 200
        val settings = SettingsRepository(PreferenceDataStoreFactory.create(produceFile = { File(tempFolder.root, "s-${System.nanoTime()}.preferences_pb") }))
        val dispatcher = StandardTestDispatcher(testScheduler)
        return DailyWallpaperCoordinator(
            runDailyWallpaper = RunDailyWallpaperUseCase(daily, photos, copies, conditions, dailyTestClock, FakeEntitlementRepository()),
            applyPendingWallpaper = ApplyPendingWallpaperUseCase(daily, copies, SetWallpaperFromPhotoUseCase(copies, settings, UnconfinedTestDispatcher(testScheduler)), dailyTestClock, FakeEntitlementRepository()),
            dailyWallpaperRepository = daily,
            wallpaperCopyRepository = copies,
            deviceConditions = conditions,
            scope = backgroundScope,
            ioDispatcher = dispatcher,
        )
    }

    @Test
    fun `a resume prepares the photo but sets it only when the screen turns off`() = runTest {
        // Given the daily change on and the screen on
        val coordinator = coordinator()
        daily.update { it.copy(enabled = true) }
        coordinator.start()

        // When the launcher resumes
        coordinator.onLauncherResumed()
        testScheduler.advanceUntilIdle()

        // Then the photo is prepared but not set
        assertEquals("https://img.peapix.com/a.jpg", daily.get().pending?.imageUrl)
        assertEquals(0, copies.setCount)

        // When the screen turns off
        conditions.screenOn = false
        conditions.screenOff.emit(Unit)
        testScheduler.advanceUntilIdle()

        // Then it is set
        assertEquals(1, copies.setCount)
        assertNull(daily.get().pending)
    }

    @Test
    fun `a photo ready after the screen went off is set straight away`() = runTest {
        // Given the screen is already off when the download finishes
        val coordinator = coordinator()
        daily.update { it.copy(enabled = true) }
        conditions.screenOn = false

        // When a run completes
        coordinator.onLauncherResumed()
        testScheduler.advanceUntilIdle()

        // Then it was set without waiting for another screen-off
        assertEquals(1, copies.setCount)
    }

    @Test
    fun `resumes in quick succession start a single run`() = runTest {
        // Given the daily change on
        val coordinator = coordinator()
        daily.update { it.copy(enabled = true) }

        // When the launcher resumes three times before the first run finishes
        repeat(3) { coordinator.onLauncherResumed() }
        testScheduler.advanceUntilIdle()

        // Then the feed was asked for once
        assertEquals(1, photos.feedRequests)
    }

    @Test
    fun `turning off drops the prepared photo`() = runTest {
        // Given a prepared photo
        val coordinator = coordinator()
        daily.pendingBitmap = Bitmap.createBitmap(1, 1, Bitmap.Config.ARGB_8888)
        daily.update { it.copy(enabled = true, pending = DailyWallpaperPhoto("https://img.peapix.com/a.jpg", "a", "")) }

        // When turned off
        coordinator.turnOff()
        testScheduler.advanceUntilIdle()

        // Then nothing is left to set
        assertFalse(daily.get().enabled)
        assertNull(daily.pendingBitmap)
    }

    @Test
    fun `turning on runs straight away, with mobile data when allowed`() = runTest {
        // Given mobile data with Wi-Fi only on
        val coordinator = coordinator()
        conditions.network = com.facetlauncher.app.data.model.NetworkConditions(connected = true, unmetered = false, roaming = false)

        // When turned on with the one-time mobile data choice
        coordinator.turnOn(DailyPhotoSource.BING, includeLock = false, useMobileData = true)
        testScheduler.advanceUntilIdle()

        // Then today's photo was prepared
        assertTrue(daily.get().enabled)
        assertEquals("https://img.peapix.com/a.jpg", daily.get().pending?.imageUrl)
    }

    @Test
    fun `resume takes the wallpaper set now as the baseline`() = runTest {
        // Given a pause after the user changed the wallpaper to 42
        val coordinator = coordinator()
        daily.update { it.copy(enabled = true, paused = true, systemId = 10) }
        copies.systemId = 42

        // When resumed
        coordinator.resume()
        testScheduler.advanceUntilIdle()

        // Then it runs again against the new wallpaper
        assertFalse(daily.get().paused)
        assertEquals(42, daily.get().systemId)
        assertEquals(1, photos.feedRequests)
    }
}
