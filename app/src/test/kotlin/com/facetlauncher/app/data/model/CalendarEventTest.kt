package com.facetlauncher.app.data.model

import org.junit.Assert.assertEquals
import org.junit.Test

class CalendarEventTest {

    private fun event(location: String) = CalendarEvent(1, "1", "Scrum", 0L, 1L, isAllDay = false, location = location)

    @Test
    fun `a Workspace room path is shortened to the room name`() {
        // Given a booked room as Google writes it
        val event = event("BLR-Innovate-2-Marie Curie (4)")

        // Then only the room and capacity remain
        assertEquals("Marie Curie (4)", event.roomLabel)
    }

    @Test
    fun `a room path with a spaced dash is shortened too`() {
        assertEquals("Meeting Room SINGAPORE (16)", event("BLR- TOWERS-G-Meeting Room SINGAPORE (16)").roomLabel)
    }

    @Test
    fun `a plain location is left as typed`() {
        assertEquals("Room 4B", event("Room 4B").roomLabel)
        assertEquals("Conf-Room-A", event("Conf-Room-A").roomLabel)
    }

    @Test
    fun `no location gives an empty label`() {
        assertEquals("", event("").roomLabel)
    }
}
