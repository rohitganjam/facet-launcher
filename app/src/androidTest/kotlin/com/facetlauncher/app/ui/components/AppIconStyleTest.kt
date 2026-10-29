package com.facetlauncher.app.ui.components

import android.graphics.Bitmap
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.colorspace.ColorSpaces
import androidx.compose.ui.graphics.toPixelMap
import androidx.compose.ui.test.captureToImage
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.unit.dp
import com.facetlauncher.app.data.model.IconRenderMode
import com.facetlauncher.app.data.model.ThemeMode
import com.facetlauncher.app.ui.theme.AccentSwatch
import com.facetlauncher.app.ui.theme.FacetLauncherTheme
import kotlin.math.abs
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class AppIconStyleTest {

    @get:Rule
    val composeRule = createComposeRule()

    private val accent = AccentSwatch.BLUE.light

    /** What shows through wherever the icon draws nothing. */
    private val backdrop = Color.Magenta

    /** A white square on a blue plate, the shape of a plain logo. */
    private fun logo(): ImageBitmap {
        val side = 64
        val pixels = IntArray(side * side) { 0xFF3366CC.toInt() }
        for (y in 20 until 44) for (x in 20 until 44) pixels[y * side + x] = 0xFFFFFFFF.toInt()
        return Bitmap.createBitmap(pixels, side, side, Bitmap.Config.ARGB_8888).asImageBitmap()
    }

    private fun show(mode: IconRenderMode) {
        composeRule.setContent {
            FacetLauncherTheme(themeMode = ThemeMode.LIGHT, accentFromSystem = false, customAccentSwatch = AccentSwatch.BLUE, iconRenderMode = mode) {
                Box(modifier = Modifier.background(backdrop).testTag("frame")) {
                    AppIcon(icon = logo(), size = 64.dp, contentDescription = "logo")
                }
            }
        }
    }

    private fun pixels() = composeRule.onNodeWithTag("frame").captureToImage().toPixelMap()

    private fun waitForPixels(condition: (androidx.compose.ui.graphics.PixelMap) -> Boolean) {
        composeRule.waitUntil(timeoutMillis = 10_000) { runCatching { condition(pixels()) }.getOrDefault(false) }
    }

    private fun Color.near(other: Color) =
        abs(red - other.red) < 0.06f && abs(green - other.green) < 0.06f && abs(blue - other.blue) < 0.06f

    private fun androidx.compose.ui.graphics.PixelMap.centre() = this[width / 2, height / 2].convert(ColorSpaces.Srgb)

    /** On the plate, inside the icon shape's own clip. */
    private fun androidx.compose.ui.graphics.PixelMap.corner() = this[width / 5, height / 5].convert(ColorSpaces.Srgb)

    @Test
    fun `glyph style paints the glyph in the accent and drops the plate`() {
        // Given a logo shown in the glyph style
        show(IconRenderMode.GLYPH_ACCENT)

        // When the styled picture has replaced the placeholder tint
        waitForPixels { it.corner().near(backdrop) }

        // Then the glyph is the accent and the plate is gone
        assertTrue("centre ${pixels().centre()} vs accent $accent", pixels().centre().near(accent))
        assertTrue(pixels().corner().near(backdrop))
    }

    @Test
    fun `glyph theme style paints the glyph in the text colour not the accent`() {
        // Given a logo shown in the glyph theme style under the light theme
        show(IconRenderMode.GLYPH_INK)

        // When the styled picture has replaced the placeholder tint
        waitForPixels { it.corner().near(backdrop) }

        // Then the glyph is dark ink, not the blue accent
        val centre = pixels().centre()
        assertTrue("centre $centre", !centre.near(accent) && centre.red + centre.green + centre.blue < 1.2f)
    }

    @Test
    fun `glyph inverted style paints the glyph in the opposite text colour and drops the plate`() {
        // Given a logo shown in the glyph inverted style under the light theme
        show(IconRenderMode.GLYPH_INVERTED)

        // When the styled picture has replaced the placeholder tint
        waitForPixels { it.corner().near(backdrop) }

        // Then the glyph is the light opposite of the dark text colour, and there is no tile
        assertTrue("centre ${pixels().centre()}", pixels().centre().red > 0.8f)
        assertTrue(pixels().corner().near(backdrop))
    }

    @Test
    fun `glyph tile style paints a text coloured tile with the glyph in the opposite colour`() {
        // Given a logo shown in the glyph tile theme style under the light theme
        show(IconRenderMode.GLYPH_TILE_INK)

        // When the styled picture has replaced the placeholder
        waitForPixels { it.corner().red < 0.2f && it.centre().red > 0.8f }

        // Then the tile is the dark text colour and the glyph on it the light opposite
        assertTrue("corner ${pixels().corner()}", pixels().corner().red < 0.2f)
        assertTrue("centre ${pixels().centre()}", pixels().centre().red > 0.8f)
    }

    @Test
    fun `glyph reversed tile style swaps the tile and the glyph colours`() {
        // Given a logo shown in the glyph reversed tile theme style under the light theme
        show(IconRenderMode.GLYPH_REVERSED_INK)

        // When the styled picture has replaced the placeholder
        waitForPixels { it.corner().red > 0.8f && it.centre().red < 0.2f }

        // Then the tile is the light text tone and the glyph on it the dark text colour
        assertTrue("corner ${pixels().corner()}", pixels().corner().red > 0.8f)
        assertTrue("centre ${pixels().centre()}", pixels().centre().red < 0.2f)
    }

    @Test
    fun `outline style leaves the inside of the shape empty`() {
        // Given a logo shown in the outline style
        show(IconRenderMode.OUTLINE_ACCENT)

        // When the styled picture has replaced the placeholder tint
        waitForPixels { it.centre().near(backdrop) }

        // Then some line art is drawn in the accent
        val map = pixels()
        val accentPixels = (0 until map.height).sumOf { y -> (0 until map.width).count { x -> map[x, y].convert(ColorSpaces.Srgb).near(accent) } }
        assertTrue(accentPixels > 20)
    }

    @Test
    fun `monochrome accent style still tints the whole icon`() {
        // Given a logo shown in the monochrome accent style
        show(IconRenderMode.MONOCHROME_ACCENT)

        // Then the plate is still there
        composeRule.waitForIdle()
        assertTrue(!pixels().corner().near(backdrop))
    }
}
