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
    fun `it falls back to the built-in wallpaper when none is set`() = runTest {
        // Given no explicitly-set wallpaper but a built-in default
        `when`(wallpaperManager.peekDrawable()).thenReturn(null)
        `when`(wallpaperManager.builtInDrawable).thenReturn(drawableOf(100, 100))

        // When resolving
        val result = repository.currentHomeWallpaper()

        // Then the built-in default is used
        assertTrue(result is HomeWallpaper.Image)
    }

    @Test
    fun `it falls back to wallpaper colors when no drawable is available`() = runTest {
        // Given no drawable (e.g. a live wallpaper) but readable Material You tones
        `when`(wallpaperManager.peekDrawable()).thenReturn(null)
        `when`(wallpaperManager.builtInDrawable).thenReturn(null)
        `when`(wallpaperManager.getWallpaperColors(WallpaperManager.FLAG_SYSTEM)).thenReturn(
            WallpaperColors(Color.valueOf(Color.RED), Color.valueOf(Color.GREEN), null),
        )

        // When resolving
        val result = repository.currentHomeWallpaper()

        // Then the tones are returned in primary/secondary/tertiary order
        assertEquals(HomeWallpaper.Tones(listOf(Color.RED, Color.GREEN)), result)
    }

    @Test
    fun `it returns Unavailable when nothing can be read`() = runTest {
        // Given every source empty (no permission, no colors)
        `when`(wallpaperManager.peekDrawable()).thenReturn(null)
        `when`(wallpaperManager.builtInDrawable).thenReturn(null)
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
        `when`(wallpaperManager.builtInDrawable).thenReturn(null)
        `when`(wallpaperManager.getWallpaperColors(WallpaperManager.FLAG_SYSTEM)).thenReturn(
            WallpaperColors(Color.valueOf(Color.BLUE), null, null),
        )

        // When resolving
        val result = repository.currentHomeWallpaper()

        // Then it degrades to tones rather than propagating the exception
        assertEquals(HomeWallpaper.Tones(listOf(Color.BLUE)), result)
    }
}
