package com.facetlauncher.app.data

import android.app.WallpaperColors
import android.app.WallpaperManager
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.drawable.BitmapDrawable
import com.facetlauncher.app.data.model.HomeWallpaper
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito.mock
import org.mockito.Mockito.`when`
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class WallpaperRepositoryTest {

    private val wallpaperManager = mock(WallpaperManager::class.java)
    private val repository = WallpaperRepository(wallpaperManager)

    @Suppress("DEPRECATION")
    private fun drawableOf(width: Int, height: Int) =
        BitmapDrawable(Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888))

    @Test
    fun `the peeked wallpaper drawable is returned as an Image`() = runTest {
        // Given a set home wallpaper
        val drawable = drawableOf(200, 400)
        `when`(wallpaperManager.peekDrawable()).thenReturn(drawable)

        // When resolving the current wallpaper
        val result = repository.currentHomeWallpaper()

        // Then it comes back as that bitmap, untouched (already within bounds)
        assertTrue(result is HomeWallpaper.Image)
        assertEquals(drawable.bitmap, (result as HomeWallpaper.Image).bitmap)
    }

    @Test
    fun `an oversized wallpaper bitmap is downscaled`() = runTest {
        // Given a wallpaper larger than MAX_WALLPAPER_DIMENSION on its long side
        `when`(wallpaperManager.peekDrawable()).thenReturn(drawableOf(3000, 2000))

        // When resolving
        val result = repository.currentHomeWallpaper() as HomeWallpaper.Image

        // Then the long side is clamped
        assertTrue(maxOf(result.bitmap.width, result.bitmap.height) <= 1080)
    }

    @Test
    fun `the built-in wallpaper is never used in place of the user's`() = runTest {
        // Given no readable wallpaper but a built-in default and readable colors
        `when`(wallpaperManager.peekDrawable()).thenReturn(null)
        `when`(wallpaperManager.builtInDrawable).thenReturn(drawableOf(100, 100))
        `when`(wallpaperManager.getWallpaperColors(WallpaperManager.FLAG_SYSTEM)).thenReturn(
            WallpaperColors(Color.valueOf(Color.RED), null, null),
        )

        // When resolving
        val result = repository.currentHomeWallpaper()

        // Then it is the colour, not the factory image
        assertTrue(result is HomeWallpaper.Tones)
    }

    @Test
    fun `it falls back to the primary color washing toward a blend of the secondary`() = runTest {
        // Given no drawable (e.g. a live wallpaper) but readable colors
        `when`(wallpaperManager.peekDrawable()).thenReturn(null)
        `when`(wallpaperManager.getWallpaperColors(WallpaperManager.FLAG_SYSTEM)).thenReturn(
            WallpaperColors(Color.valueOf(Color.RED), Color.valueOf(Color.GREEN), Color.valueOf(Color.BLUE)),
        )

        // When resolving
        val result = repository.currentHomeWallpaper() as HomeWallpaper.Tones

        // Then the wash runs from the primary to a 40% blend of the secondary, the tertiary left out
        val bottom = androidx.core.graphics.ColorUtils.blendARGB(Color.RED, Color.GREEN, 0.4f)
        assertEquals(listOf(Color.RED, bottom), result.argb)
    }

    @Test
    fun `a wallpaper with no secondary color gives just the primary tone`() = runTest {
        // Given only a primary colour
        `when`(wallpaperManager.peekDrawable()).thenReturn(null)
        `when`(wallpaperManager.getWallpaperColors(WallpaperManager.FLAG_SYSTEM)).thenReturn(
            WallpaperColors(Color.valueOf(Color.RED), null, null),
        )

        // When resolving
        val result = repository.currentHomeWallpaper()

        // Then the wash is flat: the primary at both ends
        assertEquals(HomeWallpaper.Tones(listOf(Color.RED, Color.RED)), result)
    }

    @Test
    fun `it returns Unavailable when nothing can be read`() = runTest {
        // Given every source empty (no permission, no colors)
        `when`(wallpaperManager.peekDrawable()).thenReturn(null)
        `when`(wallpaperManager.getWallpaperColors(WallpaperManager.FLAG_SYSTEM)).thenReturn(null)

        // When resolving
        val result = repository.currentHomeWallpaper()

        // Then the caller is told to fall back to the theme token
        assertEquals(HomeWallpaper.Unavailable, result)
    }

    @Test
    fun `a thrown SecurityException on peekDrawable is swallowed and the chain continues`() = runTest {
        // Given peekDrawable denied (Facet not the active launcher) but colors readable
        `when`(wallpaperManager.peekDrawable()).thenThrow(SecurityException("denied"))
        `when`(wallpaperManager.getWallpaperColors(WallpaperManager.FLAG_SYSTEM)).thenReturn(
            WallpaperColors(Color.valueOf(Color.BLUE), null, null),
        )

        // When resolving
        val result = repository.currentHomeWallpaper()

        // Then it degrades to the tone rather than propagating the exception
        assertEquals(HomeWallpaper.Tones(listOf(Color.BLUE, Color.BLUE)), result)
    }

    private class FakeCopies(private val stored: Bitmap?, var systemId: Int = 1) : WallpaperCopyRepository(org.robolectric.RuntimeEnvironment.getApplication(), mock(WallpaperManager::class.java)) {
        override val copy = kotlinx.coroutines.flow.flowOf(stored)
        override fun currentSystemWallpaperId() = systemId
    }

    private fun settings(storedId: Int): SettingsRepository {
        val file = java.io.File.createTempFile("wallpaper", ".preferences_pb").also { it.delete() }
        return SettingsRepository(androidx.datastore.preferences.core.PreferenceDataStoreFactory.create(produceFile = { file })).also {
            kotlinx.coroutines.runBlocking { it.setWallpaperCopySystemId(storedId) }
        }
    }

    @Test
    fun `a saved copy that matches the system wallpaper is used for previews`() = runTest {
        // Given a saved copy recorded for the current wallpaper, and a system wallpaper that could also be read
        val copy = Bitmap.createBitmap(30, 60, Bitmap.Config.ARGB_8888)
        `when`(wallpaperManager.peekDrawable()).thenReturn(drawableOf(10, 10))
        val repository = WallpaperRepository(wallpaperManager, wallpaperCopies = FakeCopies(copy), settingsRepository = settings(1))

        // When the home wallpaper is read
        val result = repository.currentHomeWallpaper()

        // Then it is the saved copy
        assertTrue(result is HomeWallpaper.Image && result.bitmap === copy)
    }

    @Test
    fun `a saved copy that no longer matches the system wallpaper is ignored`() = runTest {
        // Given a copy recorded for wallpaper 1 while the system is now at 2, and nothing else readable
        val copy = Bitmap.createBitmap(30, 60, Bitmap.Config.ARGB_8888)
        val repository = WallpaperRepository(wallpaperManager, wallpaperCopies = FakeCopies(copy, systemId = 2), settingsRepository = settings(1))

        // When the home wallpaper is read
        val result = repository.currentHomeWallpaper()

        // Then the stale copy is not used
        assertEquals(HomeWallpaper.Unavailable, result)
    }
}
