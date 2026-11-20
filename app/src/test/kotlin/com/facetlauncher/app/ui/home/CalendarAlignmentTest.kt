package com.facetlauncher.app.ui.home

import com.facetlauncher.app.data.model.ClockAlignment
import org.junit.Assert.assertEquals
import org.junit.Test

class CalendarAlignmentTest {

    @Test
    fun `a centred clock with a background keeps its events on the left`() {
        // Given a centred clock inside a panel
        // Then the events are left-aligned
        assertEquals(ClockAlignment.LEFT, calendarAlignmentFor(ClockAlignment.CENTER, panelOn = true))
    }

    @Test
    fun `a left or right clock with a background keeps its own alignment for the events`() {
        assertEquals(ClockAlignment.LEFT, calendarAlignmentFor(ClockAlignment.LEFT, panelOn = true))
        assertEquals(ClockAlignment.RIGHT, calendarAlignmentFor(ClockAlignment.RIGHT, panelOn = true))
    }

    @Test
    fun `without a background the events follow the clock including centre`() {
        assertEquals(ClockAlignment.CENTER, calendarAlignmentFor(ClockAlignment.CENTER, panelOn = false))
    }
}
