package com.facetlauncher.app.ui.home.clock

import com.facetlauncher.app.data.model.CalendarEvent
import java.time.Clock
import java.time.Instant
import java.time.ZoneId
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.util.Locale
import org.junit.Assert.assertEquals
import org.junit.Test

class EventTimeLabelTest {

    // Wed 2026-08-26 09:00 in Kolkata (+5:30)
    private val zone = ZoneId.of("Asia/Kolkata")
    private val clock = Clock.fixed(Instant.parse("2026-08-26T03:30:00Z"), zone)
    private val formatter = DateTimeFormatter.ofPattern("HH:mm", Locale.US)

    private fun timed(start: String) = CalendarEvent(1, "1", "E", Instant.parse(start).toEpochMilli(), Instant.parse(start).toEpochMilli() + 3_600_000, isAllDay = false)
    private fun allDay(utcDate: String) = CalendarEvent(1, "1", "E", Instant.parse("${utcDate}T00:00:00Z").toEpochMilli(), Instant.parse("${utcDate}T00:00:00Z").toEpochMilli() + 86_400_000, isAllDay = true)

    @Test
    fun `an event today shows only its time`() {
        // Given a timed event later today
        val label = eventTimeLabel(timed("2026-08-26T08:30:00Z"), formatter, clock, Locale.US)

        // Then there is no weekday prefix
        assertEquals("14:00", label)
    }

    @Test
    fun `an event tomorrow gets a weekday prefix`() {
        // Given a timed event tomorrow morning (device zone)
        val label = eventTimeLabel(timed("2026-08-27T03:30:00Z"), formatter, clock, Locale.US)

        // Then it reads Thu 09:00
        assertEquals("Thu 09:00", label)
    }

    @Test
    fun `the weekday follows the device zone not UTC`() {
        // Given an event at 23:00 UTC today, which is already 04:30 tomorrow in Kolkata
        val label = eventTimeLabel(timed("2026-08-26T23:00:00Z"), formatter, clock, Locale.US)

        // Then it is labelled with tomorrow's weekday
        assertEquals("Thu 04:30", label)
    }

    @Test
    fun `the weekday is localized`() {
        // Given a tomorrow event and a German locale
        val label = eventTimeLabel(timed("2026-08-27T03:30:00Z"), formatter, clock, Locale.GERMANY)

        // Then the abbreviation is German
        assertEquals("Do. 09:00", label)
    }

    @Test
    fun `an all-day event today reads All day`() {
        assertEquals("All day", eventTimeLabel(allDay("2026-08-26"), formatter, clock, Locale.US))
    }

    @Test
    fun `an all-day event tomorrow gets a weekday prefix using its UTC date`() {
        // Given an all-day event dated tomorrow (UTC-anchored midnight, which is 05:30 local)
        assertEquals("Thu All day", eventTimeLabel(allDay("2026-08-27"), formatter, clock, Locale.US))
    }
}
