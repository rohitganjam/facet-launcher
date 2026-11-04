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
}
