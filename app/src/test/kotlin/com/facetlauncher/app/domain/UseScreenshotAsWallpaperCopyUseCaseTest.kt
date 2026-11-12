package com.facetlauncher.app.domain

import android.app.WallpaperManager
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.RectF
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import com.facetlauncher.app.data.SettingsRepository
import com.facetlauncher.app.data.WallpaperCopyRepository
import kotlinx.coroutines.flow.first
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
class UseScreenshotAsWallpaperCopyUseCaseTest {

    @get:Rule
    val tempFolder = TemporaryFolder()

    private fun settings(): SettingsRepository {
        val dataStore: DataStore<Preferences> = PreferenceDataStoreFactory.create(produceFile = { tempFolder.newFile("t-${System.nanoTime()}.preferences_pb") })
        return SettingsRepository(dataStore)
    }

    private class FakeCopies(var saves: Boolean = true) : WallpaperCopyRepository(RuntimeEnvironment.getApplication(), mock(WallpaperManager::class.java)) {
        var saved: Bitmap? = null
        var setCalls = 0
        override fun currentSystemWallpaperId() = 5
        override fun portraitScreenSize() = 50 to 100
        override suspend fun setSystemWallpaper(bitmap: Bitmap, includeLock: Boolean): Int { setCalls++; return 99 }
        override suspend fun save(bitmap: Bitmap): Boolean { if (saves) saved = bitmap; return saves }
    }

    private fun screenshot() = Bitmap.createBitmap(200, 100, Bitmap.Config.ARGB_8888).also { it.eraseColor(Color.BLUE) }

    @Test
    fun `the framed part is stored at the screen size for the current wallpaper and the wallpaper is left alone`() = runTest {
        // Given a screenshot and a system wallpaper at id 5
        val copies = FakeCopies()
        val settings = settings()

        // When the framed part is used
        val done = UseScreenshotAsWallpaperCopyUseCase(copies, settings, UnconfinedTestDispatcher())(screenshot(), RectF(0f, 0f, 50f, 100f))

        // Then it is stored at the screen's size, tied to id 5, and no wallpaper was set
        assertTrue(done)
        assertEquals(50, copies.saved?.width)
        assertEquals(100, copies.saved?.height)
        assertEquals(0, copies.setCalls)
        assertEquals(5, settings.settings.first { it.wallpaperCopySystemId == 5 }.wallpaperCopySystemId)
    }

    @Test
    fun `when the copy cannot be stored nothing is recorded`() = runTest {
        // Given storing fails
        val copies = FakeCopies(saves = false)
        val settings = settings()

        // When used
        val done = UseScreenshotAsWallpaperCopyUseCase(copies, settings, UnconfinedTestDispatcher())(screenshot(), RectF(0f, 0f, 50f, 100f))

        // Then it reports failure and records no id
        assertFalse(done)
        assertNull(copies.saved)
        assertEquals(-1, settings.settings.first().wallpaperCopySystemId)
    }
}
