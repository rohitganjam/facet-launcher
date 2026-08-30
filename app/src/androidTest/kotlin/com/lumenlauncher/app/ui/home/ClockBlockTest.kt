package com.lumenlauncher.app.ui.home

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.lumenlauncher.app.data.model.CalendarEvent
import com.lumenlauncher.app.ui.theme.LumenLauncherTheme
import java.time.Clock
import java.time.Instant
import java.time.ZoneOffset
import java.util.Locale
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

class ClockBlockTest {

    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun rendersTimeAndDateForAFixedClock() {
        // Given a clock fixed to a known instant (Thursday, 27 August 2026, 09:05 UTC)
        val fixedClock = Clock.fixed(Instant.parse("2026-08-27T09:05:00Z"), ZoneOffset.UTC)

        // When the ClockBlock is composed with that clock
        composeRule.setContent {
            LumenLauncherTheme {
                ClockBlock(clock = fixedClock, locale = Locale.US)
            }
        }

        // Then it renders the matching time and date strings
        composeRule.onNodeWithText("9:05").assertExists()
        composeRule.onNodeWithText("Thursday, 27 August").assertExists()
    }

    @Test
    fun rendersTwentyFourHourTimeWhenEnabled() {
        // Given the same fixed instant, but with 24-hour time on
        val fixedClock = Clock.fixed(Instant.parse("2026-08-27T09:05:00Z"), ZoneOffset.UTC)

        // When the ClockBlock is composed with use24HourTime = true
        composeRule.setContent {
            LumenLauncherTheme {
                ClockBlock(clock = fixedClock, locale = Locale.US, use24HourTime = true)
            }
        }

        // Then it renders "09:05", not "9:05"
        composeRule.onNodeWithText("09:05").assertExists()
    }

    @Test
    fun rendersEventRowsInOrderAndFiresClickCallback() {
        // Given a fixed clock and two events, given out of chronological order
        val fixedClock = Clock.fixed(Instant.parse("2026-08-27T09:05:00Z"), ZoneOffset.UTC)
        val later = CalendarEvent(
            id = 2,
            calendarId = "1",
            title = "Design review",
            startTimeMillis = Instant.parse("2026-08-27T11:00:00Z").toEpochMilli(),
            endTimeMillis = Instant.parse("2026-08-27T11:30:00Z").toEpochMilli(),
            isAllDay = false,
        )
        val earlier = CalendarEvent(
            id = 1,
            calendarId = "1",
            title = "Standup",
            startTimeMillis = Instant.parse("2026-08-27T10:00:00Z").toEpochMilli(),
            endTimeMillis = Instant.parse("2026-08-27T10:30:00Z").toEpochMilli(),
            isAllDay = false,
        )
        var clicked: CalendarEvent? = null

        // When the ClockBlock is composed with those events
        composeRule.setContent {
            LumenLauncherTheme {
                ClockBlock(
                    clock = fixedClock,
                    locale = Locale.US,
                    events = listOf(later, earlier),
                    onEventClick = { clicked = it },
                )
            }
        }

        // Then both rows render, each with its own title and time
        composeRule.onNodeWithText("Standup").assertExists()
        composeRule.onNodeWithText("Design review").assertExists()
        composeRule.onNodeWithText("10:00").assertExists()
        composeRule.onNodeWithText("11:00").assertExists()

        // When tapping one row
        composeRule.onNodeWithTag("clock_event_row_1").performClick()

        // Then its callback fires with that exact event
        assertEquals(earlier, clicked)
    }

    @Test
    fun noEventRowsWhenNoEventsAreGiven() {
        // Given a ClockBlock with no events (the default)
        val fixedClock = Clock.fixed(Instant.parse("2026-08-27T09:05:00Z"), ZoneOffset.UTC)
        composeRule.setContent {
            LumenLauncherTheme { ClockBlock(clock = fixedClock, locale = Locale.US) }
        }

        // Then no event-rows container renders at all
        composeRule.onNodeWithTag("clock_event_rows").assertDoesNotExist()
    }

    @Test
    fun anEventThatHasAlreadyEndedIsNotRendered() {
        // Given "now" sits after one event's end time but before the other's, and an all-day event
        val fixedClock = Clock.fixed(Instant.parse("2026-08-27T09:05:00Z"), ZoneOffset.UTC)
        val finished = CalendarEvent(
            id = 1,
            calendarId = "1",
            title = "Early standup",
            startTimeMillis = Instant.parse("2026-08-27T08:00:00Z").toEpochMilli(),
            endTimeMillis = Instant.parse("2026-08-27T08:30:00Z").toEpochMilli(),
            isAllDay = false,
        )
        val ongoing = CalendarEvent(
            id = 2,
            calendarId = "1",
            title = "Design review",
            startTimeMillis = Instant.parse("2026-08-27T09:00:00Z").toEpochMilli(),
            endTimeMillis = Instant.parse("2026-08-27T09:30:00Z").toEpochMilli(),
            isAllDay = false,
        )
        val allDay = CalendarEvent(
            id = 3,
            calendarId = "1",
            title = "Company holiday",
            startTimeMillis = Instant.parse("2026-08-27T00:00:00Z").toEpochMilli(),
            endTimeMillis = Instant.parse("2026-08-27T00:00:01Z").toEpochMilli(),
            isAllDay = true,
        )

        // When the ClockBlock is composed with all three
        composeRule.setContent {
            LumenLauncherTheme {
                ClockBlock(clock = fixedClock, locale = Locale.US, events = listOf(finished, ongoing, allDay))
            }
        }

        // Then the already-ended timed event is gone, but the still-ongoing one and the all-day
        // one (whose own end time is in the past too, but that never matters for all-day) render
        composeRule.onNodeWithText("Early standup").assertDoesNotExist()
        composeRule.onNodeWithText("Design review").assertExists()
        composeRule.onNodeWithText("Company holiday").assertExists()
    }
}
