package com.facetlauncher.app.domain

import android.app.WallpaperManager
import android.content.Intent
import android.graphics.Bitmap
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import com.facetlauncher.app.data.ScreenCaptureRepository
import com.facetlauncher.app.data.SettingsRepository
import com.facetlauncher.app.data.WallpaperCopyRepository
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
import org.mockito.Mockito.mock
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment

@RunWith(RobolectricTestRunner::class)
class CaptureWallpaperUseCaseTest {

    @get:Rule
    val tempFolder = TemporaryFolder()

    private fun settings(): SettingsRepository {
        val dataStore: DataStore<Preferences> = PreferenceDataStoreFactory.create(produceFile = { tempFolder.newFile("t-${System.nanoTime()}.preferences_pb") })
        return SettingsRepository(dataStore)
    }

    private class FakeCopies : WallpaperCopyRepository(RuntimeEnvironment.getApplication(), mock(WallpaperManager::class.java)) {
        var saved: Bitmap? = null
        override fun currentSystemWallpaperId() = 5
        override fun screenSize() = 50 to 100
        override suspend fun save(bitmap: Bitmap): Boolean { saved = bitmap; return true }
    }

    private class FakeCapture(private val frame: Bitmap?) : ScreenCaptureRepository(RuntimeEnvironment.getApplication()) {
        var calls = 0
        var barsHiddenCheck: (() -> Boolean)? = null
        override suspend fun capture(resultCode: Int, consent: Intent, barsHidden: () -> Boolean): Bitmap? {
            calls++
            barsHiddenCheck = barsHidden
            return frame
        }
    }

    private fun useCase(frame: Bitmap?, copies: FakeCopies): Pair<CaptureWallpaperUseCase, FakeCapture> {
        val capture = FakeCapture(frame)
        val store = UseScreenshotAsWallpaperCopyUseCase(copies, settings(), UnconfinedTestDispatcher())
        return CaptureWallpaperUseCase(capture, store, copies) to capture
    }

    @Test
    fun `a portrait frame is stored whole at the screen size`() = runTest {
        // Given a portrait frame of the screen
        val copies = FakeCopies()
        val (capture, _) = useCase(Bitmap.createBitmap(100, 200, Bitmap.Config.ARGB_8888), copies)

        // When the wallpaper is captured
        val done = capture(-1, Intent())

        // Then the copy is stored at the screen's size
        assertTrue(done)
        assertEquals(50, copies.saved?.width)
        assertEquals(100, copies.saved?.height)
    }

    @Test
    fun `the bars check goes to the capture`() = runTest {
        // Given a capture and a check that says the bars are hidden
        val copies = FakeCopies()
        val (capture, fake) = useCase(Bitmap.createBitmap(100, 200, Bitmap.Config.ARGB_8888), copies)

        // When the wallpaper is captured with it
        capture(-1, Intent()) { true }

        // Then the capture got the same check
        assertTrue(fake.barsHiddenCheck?.invoke() == true)
    }

    @Test
    fun `no frame means nothing is stored`() = runTest {
        // Given the capture returns nothing
        val copies = FakeCopies()
        val (capture, _) = useCase(null, copies)

        // When the wallpaper is captured
        val done = capture(-1, Intent())

        // Then it reports failure and stores nothing
        assertFalse(done)
        assertNull(copies.saved)
    }

    @Test
    fun `a landscape frame is refused because the copy is portrait`() = runTest {
        // Given a landscape frame
        val copies = FakeCopies()
        val (capture, _) = useCase(Bitmap.createBitmap(200, 100, Bitmap.Config.ARGB_8888), copies)

        // When the wallpaper is captured
        val done = capture(-1, Intent())

        // Then it reports failure and stores nothing
        assertFalse(done)
        assertNull(copies.saved)
    }
}
