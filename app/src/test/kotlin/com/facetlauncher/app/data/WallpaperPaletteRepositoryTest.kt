package com.facetlauncher.app.data

import android.app.WallpaperColors
import android.app.WallpaperManager
import android.graphics.Bitmap
import com.materialkolor.hct.Hct
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito.mock
import org.mockito.Mockito.`when`
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import kotlin.math.abs
import kotlin.math.min

@RunWith(RobolectricTestRunner::class)
class WallpaperPaletteRepositoryTest {

    private val wallpaperManager = mock(WallpaperManager::class.java)
    private val repository = WallpaperPaletteRepository(wallpaperManager, Dispatchers.Unconfined)

    private fun hueDistance(a: Int, b: Int): Double {
        val d = abs(Hct.fromInt(a).hue - Hct.fromInt(b).hue)
        return min(d, 360 - d)
    }

    private fun solid(color: Int, width: Int = 400, height: Int = 800): Bitmap =
        Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888).apply { eraseColor(color) }

    @Test
    fun `the seed of a mostly blue picture is blue`() {
        // Given a picture that is three quarters blue and a quarter orange
        val blue = 0xFF1E50D2.toInt()
        val pixels = IntArray(400) { if (it < 300) blue else 0xFFE8892A.toInt() }

        // When picking the seed
        val seed = seedFromPixels(pixels)

        // Then it is the blue
        assertTrue(hueDistance(seed, blue) < 10)
    }

    @Test
    fun `the palette keeps the seed's hue and differs between light and dark`() {
        // Given a green seed
        val seed = 0xFF2E7D32.toInt()

        // When building its palette
        val palette = wallpaperPaletteFrom(seed)

        // Then the primary role keeps its hue, is lighter in the dark theme, and the three roles differ
        assertTrue(hueDistance(palette.light.primary, seed) < 15)
        assertTrue(Hct.fromInt(palette.dark.primary).tone > Hct.fromInt(palette.light.primary).tone)
        assertNotEquals(palette.light.primary, palette.light.tertiary)
    }

    @Test
    fun `a wallpaper copy gives a palette from its own pixels`() = runTest {
        // Given Facet's copy of a red wallpaper
        val red = 0xFFC62828.toInt()

        // When computing the palette
        val palette = repository.paletteFor(solid(red))

        // Then it is red
        assertNotNull(palette)
        assertTrue(hueDistance(palette!!.light.primary, red) < 20)
    }

    @Test
    @Config(sdk = [31])
    fun `with no copy on Android 12 the system's own Material You is used`() = runTest {
        // Given no copy, and Android reporting colours
        `when`(wallpaperManager.getWallpaperColors(WallpaperManager.FLAG_SYSTEM))
            .thenReturn(WallpaperColors(android.graphics.Color.valueOf(0xFFC62828.toInt()), null, null))

        // Then Facet computes nothing, so the theme reads the system scheme (and any style picked in Wallpaper & style)
        assertNull(repository.paletteFor(null))
    }

    @Test
    @Config(sdk = [30])
    fun `with no copy on Android 11 the palette comes from the colours Android reports`() = runTest {
        // Given no copy, and Android reporting a blue wallpaper
        val blue = 0xFF1E50D2.toInt()
        `when`(wallpaperManager.getWallpaperColors(WallpaperManager.FLAG_SYSTEM))
            .thenReturn(WallpaperColors(android.graphics.Color.valueOf(blue), null, null))

        // When computing the palette
        val palette = repository.paletteFor(null)

        // Then there is one, seeded from that blue
        assertNotNull(palette)
        assertTrue(hueDistance(palette!!.light.primary, blue) < 20)
    }

    @Test
    @Config(sdk = [30])
    fun `with nothing to read on Android 11 there is no palette`() = runTest {
        // Given no copy and no colours from Android
        `when`(wallpaperManager.getWallpaperColors(WallpaperManager.FLAG_SYSTEM)).thenReturn(null)

        // Then there is nothing to compute from
        assertEquals(null, repository.paletteFor(null))
    }
}
