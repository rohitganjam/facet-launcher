package com.facetlauncher.app.data.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class IconRenderModeTest {

    @Test
    fun `every mode round trips through its style and colour`() {
        // Given every mode
        IconRenderMode.entries.forEach { mode ->
            // When rebuilt from its own style and colour
            val rebuilt = IconRenderMode.of(mode.style, mode.colour ?: IconColour.ACCENT, mode.background)

            // Then it is the same mode
            assertEquals(mode, rebuilt)
        }
    }

    @Test
    fun `the default style has no colour and the others offer one`() {
        assertNull(IconRenderMode.SYSTEM_DEFAULT.colour)
        assertTrue(IconRenderMode.coloursFor(IconStyle.DEFAULT).isEmpty())
        assertEquals(listOf(IconColour.ACCENT, IconColour.THEME), IconRenderMode.coloursFor(IconStyle.MONOCHROME))
        assertEquals(IconColour.entries, IconRenderMode.coloursFor(IconStyle.GLYPH))
        assertEquals(IconColour.entries, IconRenderMode.coloursFor(IconStyle.OUTLINE))
    }

    @Test
    fun `only glyph and outline offer a tile background`() {
        assertEquals(IconBackground.entries, IconRenderMode.backgroundsFor(IconStyle.GLYPH))
        assertEquals(IconBackground.entries, IconRenderMode.backgroundsFor(IconStyle.OUTLINE))
        assertEquals(listOf(IconBackground.NONE), IconRenderMode.backgroundsFor(IconStyle.MONOCHROME))
        assertEquals(IconRenderMode.OUTLINE_REVERSED_INK, IconRenderMode.of(IconStyle.OUTLINE, IconColour.THEME, IconBackground.REVERSED_TILE))
        assertEquals(IconRenderMode.GLYPH_TILE_INVERTED, IconRenderMode.of(IconStyle.GLYPH, IconColour.THEME_INVERTED, IconBackground.TILE))
        assertEquals(IconRenderMode.MONOCHROME_ACCENT, IconRenderMode.of(IconStyle.MONOCHROME, IconColour.ACCENT, IconBackground.TILE))
    }

    @Test
    fun `each glass style is its own background on the glyph and outline styles`() {
        // Given the glass backgrounds
        val glass = IconBackground.entries.filter { it.glass != null }

        // Then they cover every glass style except Off, once each (the solid widget fills are not icon backgrounds)
        assertEquals(GlassStyle.glassOnly - GlassStyle.OFF, glass.map { it.glass })

        // And each is reachable from a style and colour, keeping both
        assertEquals(IconRenderMode.GLYPH_GLASS_DARK_FROST_ACCENT, IconRenderMode.of(IconStyle.GLYPH, IconColour.ACCENT, IconBackground.GLASS_DARK_FROST))
        assertEquals(IconRenderMode.OUTLINE_GLASS_DARK_GLASS_INVERTED, IconRenderMode.of(IconStyle.OUTLINE, IconColour.THEME_INVERTED, IconBackground.GLASS_DARK_GLASS))
        assertEquals(IconBackground.GLASS_FROST, IconRenderMode.GLYPH_GLASS_FROST_INK.background)
    }

    @Test
    fun `monochrome has no glass background and falls back to none`() {
        assertEquals(IconRenderMode.MONOCHROME_ACCENT, IconRenderMode.of(IconStyle.MONOCHROME, IconColour.ACCENT, IconBackground.GLASS_FROST))
    }

    @Test
    fun `a glass mode keeps the style and colour of the plain mode`() {
        // Given a glass mode and the plain mode with the same style and colour
        val plain = IconRenderMode.GLYPH_INK
        val glass = IconRenderMode.GLYPH_GLASS_BRIGHT_INK

        // Then only the background differs
        assertEquals(plain.style, glass.style)
        assertEquals(plain.colour, glass.colour)
        assertEquals(IconBackground.NONE, plain.background)
    }

    @Test
    fun `a colour the style lacks falls back to its first`() {
        // Given monochrome has no inverted colour
        // When asking for it anyway
        val mode = IconRenderMode.of(IconStyle.MONOCHROME, IconColour.THEME_INVERTED)

        // Then it is the accent one
        assertEquals(IconRenderMode.MONOCHROME_ACCENT, mode)
    }
}
