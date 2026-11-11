package com.facetlauncher.app.ui.components

import android.graphics.Color
import androidx.core.graphics.ColorUtils
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class WallpaperToneTest {

    private fun hsl(argb: Int) = FloatArray(3).also { ColorUtils.colorToHSL(argb, it) }

    @Test
    fun `a light theme lightens the tone and a dark theme darkens it`() {
        // Given one wallpaper colour
        val source = Color.rgb(20, 40, 90)

        // When toned for each theme
        val light = hsl(wallpaperTone(source, dark = false))
        val dark = hsl(wallpaperTone(source, dark = true))

        // Then they sit either side of a 0.5 base
        assertEquals(0.6f, light[2], 0.02f)
        assertEquals(0.4f, dark[2], 0.02f)
    }

    @Test
    fun `the tone keeps the hue whether the source is dark or light`() {
        // Given a dark and a light colour of the same blue hue
        val dark = Color.rgb(20, 40, 90)
        val light = Color.rgb(190, 210, 255)

        // When toned for the same theme
        val darkTone = hsl(wallpaperTone(dark, dark = false))
        val lightTone = hsl(wallpaperTone(light, dark = false))

        // Then both land at the same lightness with a blue hue
        assertEquals(darkTone[2], lightTone[2], 0.02f)
        assertTrue(darkTone[0] in 200f..240f && lightTone[0] in 200f..240f)
    }

    @Test
    fun `saturation is capped so a vivid wallpaper colour does not shout`() {
        // Given a fully saturated colour
        // When toned
        val tone = hsl(wallpaperTone(Color.RED, dark = true))

        // Then its saturation is held to the cap
        assertTrue(tone[1] <= 0.61f)
    }
}
