package com.facetlauncher.app.data.model

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class CalendarSelectionTest {

    @Test
    fun `no selection means every personal calendar but no work calendar`() {
        // Given nothing chosen yet
        val none: Set<String>? = null

        // Then personal calendars are in and work calendars are not
        assertTrue(none.selectsCalendar("1"))
        assertFalse(none.selectsCalendar("work:1"))
    }

    @Test
    fun `a chosen set selects exactly what it lists`() {
        // Given a personal and a work calendar chosen
        val chosen = setOf("2", "work:1")

        // Then only those are selected
        assertTrue(chosen.selectsCalendar("2"))
        assertTrue(chosen.selectsCalendar("work:1"))
        assertFalse(chosen.selectsCalendar("1"))
        assertFalse(chosen.selectsCalendar("work:2"))
    }

    @Test
    fun `a work calendar id never meets a personal calendar's`() {
        // Given personal calendar 1 chosen
        val chosen = setOf("1")

        // Then work calendar 1 is a different calendar and is not selected
        assertFalse(chosen.selectsCalendar("work:1"))
    }
}
