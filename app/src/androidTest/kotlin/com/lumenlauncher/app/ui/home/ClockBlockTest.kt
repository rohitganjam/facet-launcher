package com.lumenlauncher.app.ui.home

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.lumenlauncher.app.data.model.CalendarEvent
import com.lumenlauncher.app.data.model.ClockAlignment
import com.lumenlauncher.app.data.model.ClockTemplateId
import com.lumenlauncher.app.ui.theme.LumenLauncherTheme
import java.time.Clock
import java.time.Instant
import java.time.ZoneOffset
import java.util.Locale
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
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

    @Test
    fun rendersAdvancedTemplatesWithoutCrashing() {
        val fixedClock = Clock.fixed(Instant.parse("2026-08-27T09:05:00Z"), ZoneOffset.UTC)
        composeRule.setContent {
            LumenLauncherTheme {
                ClockBlock(
                    clock = fixedClock,
                    locale = Locale.US,
                    templateId = ClockTemplateId.ROBOTO_FLEX_WIDE
                )
            }
        }
        composeRule.onNodeWithText("9:05").assertExists()
    }

    @Test
    fun clockAndCalendarAlignmentsMoveIndependentlyOfEachOther() {
        // Given a clock aligned right but a calendar explicitly kept left
        val fixedClock = Clock.fixed(Instant.parse("2026-08-27T09:05:00Z"), ZoneOffset.UTC)
        val event = CalendarEvent(
            id = 1,
            calendarId = "1",
            title = "Standup",
            startTimeMillis = Instant.parse("2026-08-27T10:00:00Z").toEpochMilli(),
            endTimeMillis = Instant.parse("2026-08-27T10:30:00Z").toEpochMilli(),
            isAllDay = false,
        )

        // When the ClockBlock is composed with opposite alignments for each
        composeRule.setContent {
            LumenLauncherTheme {
                ClockBlock(
                    clock = fixedClock,
                    locale = Locale.US,
                    events = listOf(event),
                    clockAlignment = ClockAlignment.RIGHT,
                    calendarAlignment = ClockAlignment.LEFT,
                )
            }
        }

        // Then the clock's time text sits further right than the calendar row — each obeys its
        // own setting, unaffected by the other's
        val clockLeft = composeRule.onNodeWithText("9:05").fetchSemanticsNode().boundsInRoot.left
        val calendarRowLeft = composeRule.onNodeWithTag("clock_event_row_1").fetchSemanticsNode().boundsInRoot.left
        assertTrue(clockLeft > calendarRowLeft)
    }

    @Test
    fun calendarRightAlignmentReversesEventRowItemOrder() {
        // Given a single event and the calendar aligned right
        val fixedClock = Clock.fixed(Instant.parse("2026-08-27T09:05:00Z"), ZoneOffset.UTC)
        val event = CalendarEvent(
            id = 1,
            calendarId = "1",
            title = "Standup",
            startTimeMillis = Instant.parse("2026-08-27T10:00:00Z").toEpochMilli(),
            endTimeMillis = Instant.parse("2026-08-27T10:30:00Z").toEpochMilli(),
            isAllDay = false,
        )

        // When the ClockBlock is composed with calendarAlignment = RIGHT
        composeRule.setContent {
            LumenLauncherTheme {
                ClockBlock(
                    clock = fixedClock,
                    locale = Locale.US,
                    events = listOf(event),
                    calendarAlignment = ClockAlignment.RIGHT,
                )
            }
        }

        // Then the row reads title, then time, then indicator — the mirror of the normal order.
        // useUnmergedTree = true everywhere: the row's own clickable merges all three
        // descendants' semantics into itself, so a merged-tree text lookup would otherwise
        // resolve to the row's own (identical) bounds for every child.
        val titleLeft = composeRule.onNodeWithText("Standup", useUnmergedTree = true).fetchSemanticsNode().boundsInRoot.left
        val timeLeft = composeRule.onNodeWithText("10:00", useUnmergedTree = true).fetchSemanticsNode().boundsInRoot.left
        val indicatorLeft = composeRule.onNodeWithTag("clock_event_indicator_1", useUnmergedTree = true).fetchSemanticsNode().boundsInRoot.left
        assertTrue(titleLeft < timeLeft)
        assertTrue(timeLeft < indicatorLeft)
    }

    @Test
    fun calendarLeftAlignmentKeepsTheNormalEventRowItemOrder() {
        // Given a single event and the default (LEFT) calendar alignment
        val fixedClock = Clock.fixed(Instant.parse("2026-08-27T09:05:00Z"), ZoneOffset.UTC)
        val event = CalendarEvent(
            id = 1,
            calendarId = "1",
            title = "Standup",
            startTimeMillis = Instant.parse("2026-08-27T10:00:00Z").toEpochMilli(),
            endTimeMillis = Instant.parse("2026-08-27T10:30:00Z").toEpochMilli(),
            isAllDay = false,
        )

        // When the ClockBlock is composed with the default calendar alignment
        composeRule.setContent {
            LumenLauncherTheme {
                ClockBlock(clock = fixedClock, locale = Locale.US, events = listOf(event))
            }
        }

        // Then the row reads indicator, then time, then title — today's unchanged order
        val indicatorLeft = composeRule.onNodeWithTag("clock_event_indicator_1", useUnmergedTree = true).fetchSemanticsNode().boundsInRoot.left
        val timeLeft = composeRule.onNodeWithText("10:00", useUnmergedTree = true).fetchSemanticsNode().boundsInRoot.left
        val titleLeft = composeRule.onNodeWithText("Standup", useUnmergedTree = true).fetchSemanticsNode().boundsInRoot.left
        assertTrue(indicatorLeft < timeLeft)
        assertTrue(timeLeft < titleLeft)
    }

    @Test
    fun onlyTheFirstThreeUpcomingEventsRender() {
        // Given five still-relevant events, chronologically ordered (as CalendarRepository hands them)
        val fixedClock = Clock.fixed(Instant.parse("2026-08-27T09:05:00Z"), ZoneOffset.UTC)
        val events = (1..5).map { i ->
            CalendarEvent(
                id = i.toLong(),
                calendarId = "1",
                title = "Event $i",
                startTimeMillis = Instant.parse("2026-08-27T1${i}:00:00Z").toEpochMilli(),
                endTimeMillis = Instant.parse("2026-08-27T1${i}:30:00Z").toEpochMilli(),
                isAllDay = false,
            )
        }

        // When the ClockBlock is composed with all five
        composeRule.setContent {
            LumenLauncherTheme { ClockBlock(clock = fixedClock, locale = Locale.US, events = events) }
        }

        // Then only the first three (soonest) render, not the whole day's agenda
        composeRule.onNodeWithText("Event 1").assertExists()
        composeRule.onNodeWithText("Event 2").assertExists()
        composeRule.onNodeWithText("Event 3").assertExists()
        composeRule.onNodeWithText("Event 4").assertDoesNotExist()
        composeRule.onNodeWithText("Event 5").assertDoesNotExist()
    }
}
