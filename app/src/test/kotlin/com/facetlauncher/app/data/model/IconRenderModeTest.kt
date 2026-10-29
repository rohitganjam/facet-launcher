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
    fun `a colour the style lacks falls back to its first`() {
        // Given monochrome has no inverted colour
        // When asking for it anyway
        val mode = IconRenderMode.of(IconStyle.MONOCHROME, IconColour.THEME_INVERTED)

        // Then it is the accent one
        assertEquals(IconRenderMode.MONOCHROME_ACCENT, mode)
    }
}
