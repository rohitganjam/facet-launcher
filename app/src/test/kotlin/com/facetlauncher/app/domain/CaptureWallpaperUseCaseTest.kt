package com.facetlauncher.app.domain

import android.app.WallpaperManager
import android.content.Intent
import android.graphics.Bitmap
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import com.facetlauncher.app.data.CapturedFrames
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
        var turned = false
        var onSave: () -> Unit = {}
        override fun currentSystemWallpaperId() = 5
        override fun screenSize() = 50 to 100
        override fun windowSize() = if (turned) 100 to 50 else 50 to 100
        val savedByView = mutableMapOf<com.facetlauncher.app.data.model.ScreenSize, Bitmap>()
        override suspend fun save(bitmap: Bitmap): Boolean { saved = bitmap; return true }
        override suspend fun save(bitmap: Bitmap, size: com.facetlauncher.app.data.model.ScreenSize): Boolean { onSave(); saved = bitmap; savedByView[size] = bitmap; return true }
    }

    private class FakeCapture(private val frame: Bitmap?, private val turned: Bitmap? = null) : ScreenCaptureRepository(RuntimeEnvironment.getApplication()) {
        var calls = 0
        var barsHiddenCheck: (() -> Boolean)? = null
        var rotateAsked: (suspend (Boolean) -> Boolean)? = null
        override suspend fun capture(resultCode: Int, consent: Intent, barsHidden: () -> Boolean, rotateTo: (suspend (landscape: Boolean) -> Boolean)?): CapturedFrames? {
            calls++
            barsHiddenCheck = barsHidden
            rotateAsked = rotateTo
            return frame?.let { CapturedFrames(it, turned) }
        }
    }

    private fun useCase(frame: Bitmap?, copies: FakeCopies, turned: Bitmap? = null): Pair<CaptureWallpaperUseCase, FakeCapture> {
        val capture = FakeCapture(frame, turned)
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
    fun `the capture counts as running until the frames are stored`() = runTest {
        // Given a capture that was not started yet
        val copies = FakeCopies()
        val (capture, fake) = useCase(Bitmap.createBitmap(100, 200, Bitmap.Config.ARGB_8888), copies)
        assertFalse(fake.capturing.value)
        var runningWhenSaved = false
        copies.onSave = { runningWhenSaved = fake.capturing.value }

        // When it is done
        capture(-1, Intent())

        // Then it is not running any more, and it was while the copy was stored
        assertFalse(fake.capturing.value)
        assertTrue(runningWhenSaved)
    }

    @Test
    fun `the bars check goes to the capture`() = runTest {
        // Given a capture and a check that says the bars are hidden
        val copies = FakeCopies()
        val (capture, fake) = useCase(Bitmap.createBitmap(100, 200, Bitmap.Config.ARGB_8888), copies)

        // When the wallpaper is captured with it
        capture(-1, Intent(), barsHidden = { true })

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
    fun `a landscape frame is refused while the screen is portrait`() = runTest {
        // Given a landscape frame
        val copies = FakeCopies()
        val (capture, _) = useCase(Bitmap.createBitmap(200, 100, Bitmap.Config.ARGB_8888), copies)

        // When the wallpaper is captured
        val done = capture(-1, Intent())

        // Then it reports failure and stores nothing
        assertFalse(done)
        assertNull(copies.saved)
    }

    @Test
    fun `a landscape frame is stored whole when the phone is turned`() = runTest {
        // Given a phone held sideways and a landscape frame of it
        val copies = FakeCopies().apply { turned = true }
        val (capture, _) = useCase(Bitmap.createBitmap(200, 100, Bitmap.Config.ARGB_8888), copies)

        // When the wallpaper is captured
        val done = capture(-1, Intent())

        // Then the copy is stored at the landscape size
        assertTrue(done)
        assertEquals(100, copies.saved?.width)
        assertEquals(50, copies.saved?.height)
    }

    @Test
    fun `a portrait frame is refused when the phone is turned`() = runTest {
        // Given a phone held sideways and a portrait frame
        val copies = FakeCopies().apply { turned = true }
        val (capture, _) = useCase(Bitmap.createBitmap(100, 200, Bitmap.Config.ARGB_8888), copies)

        // When the wallpaper is captured
        val done = capture(-1, Intent())

        // Then it reports failure and stores nothing
        assertFalse(done)
        assertNull(copies.saved)
    }

    @Test
    fun `a second frame in the other orientation is stored as that view's copy`() = runTest {
        // Given a portrait screen and a capture that turned it and took a landscape frame too
        val copies = FakeCopies()
        val (capture, fake) = useCase(Bitmap.createBitmap(100, 200, Bitmap.Config.ARGB_8888), copies, turned = Bitmap.createBitmap(200, 100, Bitmap.Config.ARGB_8888))

        // When the wallpaper is captured able to turn the screen
        val done = capture(-1, Intent(), rotateTo = { true })

        // Then the capture was asked to turn the screen, and both views have a copy at their own size
        assertTrue(done)
        assertTrue(fake.rotateAsked != null)
        assertEquals(setOf(com.facetlauncher.app.data.model.ScreenSize(50, 100), com.facetlauncher.app.data.model.ScreenSize(100, 50)), copies.savedByView.keys)
        assertEquals(100, copies.savedByView[com.facetlauncher.app.data.model.ScreenSize(100, 50)]?.width)
    }

    @Test
    fun `a second frame in the same orientation is not stored`() = runTest {
        // Given a capture whose second frame came out portrait too
        val copies = FakeCopies()
        val (capture, _) = useCase(Bitmap.createBitmap(100, 200, Bitmap.Config.ARGB_8888), copies, turned = Bitmap.createBitmap(100, 200, Bitmap.Config.ARGB_8888))

        // When the wallpaper is captured
        val done = capture(-1, Intent(), rotateTo = { true })

        // Then the first copy stands and no copy exists for the other orientation
        assertTrue(done)
        assertEquals(setOf(com.facetlauncher.app.data.model.ScreenSize(50, 100)), copies.savedByView.keys)
    }
}
