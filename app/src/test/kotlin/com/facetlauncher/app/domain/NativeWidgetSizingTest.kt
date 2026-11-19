package com.facetlauncher.app.domain

import com.facetlauncher.app.data.model.NativeWidgetType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class NativeWidgetSizingTest {

    private fun span(count: Int) = 72 * count + 4 * (count - 1)

    @Test
    fun `a minimum size always draws the small layout`() {
        // Given every type at its minimum Hub span
        NativeWidgetType.entries.forEach { type ->
            val (w, h) = type.minSizeDp()

            // Then it is the small tier
            assertEquals(type.name, WidgetTier.SMALL, type.tierFor(w, h))
        }
    }

    @Test
    fun `a default size is never below the minimum or the small layout's room`() {
        NativeWidgetType.entries.forEach { type ->
            val (minW, minH) = type.minSizeDp()
            val (w, h) = type.defaultSizeDp()
            assertTrue(type.name, w >= minW && h >= minH)
        }
    }

    @Test
    fun `weather grows from icon to details to hourly strip as the box grows`() {
        assertEquals(WidgetTier.SMALL, NativeWidgetType.WEATHER.tierFor(span(1), span(1)))
        assertEquals(WidgetTier.MEDIUM, NativeWidgetType.WEATHER.tierFor(span(2), span(1)))
        assertEquals(WidgetTier.MEDIUM, NativeWidgetType.WEATHER.tierFor(span(2), span(2)))
        assertEquals(WidgetTier.LARGE, NativeWidgetType.WEATHER.tierFor(span(3), span(2)))
        assertEquals(WidgetTier.LARGE, NativeWidgetType.WEATHER.tierFor(span(5), span(3)))
    }

    @Test
    fun `a wide short weather box stays medium`() {
        assertEquals(WidgetTier.MEDIUM, NativeWidgetType.WEATHER.tierFor(span(5), span(1)))
    }

    @Test
    fun `agenda shows more as it gets taller`() {
        assertEquals(WidgetTier.SMALL, NativeWidgetType.AGENDA.tierFor(span(2), span(1)))
        assertEquals(WidgetTier.MEDIUM, NativeWidgetType.AGENDA.tierFor(span(3), span(2)))
        assertEquals(WidgetTier.LARGE, NativeWidgetType.AGENDA.tierFor(span(4), span(3)))
    }

    @Test
    fun `a narrow tall agenda box stays medium`() {
        assertEquals(WidgetTier.MEDIUM, NativeWidgetType.AGENDA.tierFor(span(2), span(4)))
    }

    @Test
    fun `now playing gains controls and then a large cover`() {
        assertEquals(WidgetTier.SMALL, NativeWidgetType.NOW_PLAYING.tierFor(span(1), span(1)))
        assertEquals(WidgetTier.SMALL, NativeWidgetType.NOW_PLAYING.tierFor(span(2), span(1)))
        assertEquals(WidgetTier.MEDIUM, NativeWidgetType.NOW_PLAYING.tierFor(span(4), span(1)))
        assertEquals(WidgetTier.MEDIUM, NativeWidgetType.NOW_PLAYING.tierFor(span(2), span(2)))
        assertEquals(WidgetTier.LARGE, NativeWidgetType.NOW_PLAYING.tierFor(span(5), span(2)))
    }

    @Test
    fun `tier thresholds are the same wherever the size came from`() {
        // Given a Home size that is not a Hub span, just below and at the weather large threshold
        assertEquals(WidgetTier.MEDIUM, NativeWidgetType.WEATHER.tierFor(219, 300))
        assertEquals(WidgetTier.LARGE, NativeWidgetType.WEATHER.tierFor(220, 140))
    }

    @Test
    fun `now playing picks a layout from the box inside the widget inset`() {
        // Given the Hub spans less the 8dp inset on each side
        fun layout(cols: Int, rows: Int) = nowPlayingLayoutFor(span(cols) - 16, span(rows) - 16)

        // Then each span gets its own layout
        assertEquals(NowPlayingLayout.TILE, layout(1, 1))
        assertEquals(NowPlayingLayout.TILE, layout(1, 3))
        assertEquals(NowPlayingLayout.COMPACT, layout(2, 1))
        assertEquals(NowPlayingLayout.STRIP, layout(4, 1))
        assertEquals(NowPlayingLayout.COVER_CARD, layout(2, 2))
        assertEquals(NowPlayingLayout.CARD, layout(4, 2))
        assertEquals(NowPlayingLayout.FULL, layout(4, 3))
        assertEquals(NowPlayingLayout.FULL, layout(2, 3))
    }

    @Test
    fun `now playing is a tile at 1x1 and a cover card at 2x2 whatever the cell size`() {
        // Given the widget's outer size, less the 8dp inset on each side
        fun layout(cell: Int, cols: Int, rows: Int) = nowPlayingLayoutFor(cell * cols + 4 * (cols - 1) - 16, cell * rows + 4 * (rows - 1) - 16)

        // Then, from a 320dp phone's cells to a roomy one's, 1x1 is a tile and 2x2 a cover card
        listOf(56, 62, 64, 72, 80).forEach { cell ->
            assertEquals("1x1 at $cell", NowPlayingLayout.TILE, layout(cell, 1, 1))
            assertEquals("2x2 at $cell", NowPlayingLayout.COVER_CARD, layout(cell, 2, 2))
        }
    }

    @Test
    fun `now playing leaves the cover card just under 116dp outside`() {
        fun layout(outerW: Int, outerH: Int) = nowPlayingLayoutFor(outerW - 16, outerH - 16)

        assertEquals(NowPlayingLayout.COVER_CARD, layout(116, 116))
        assertEquals(NowPlayingLayout.TILE, layout(115, 116))
        assertEquals(NowPlayingLayout.COMPACT, layout(148, 115))
        assertEquals(NowPlayingLayout.STRIP, layout(292, 115))
    }
}
