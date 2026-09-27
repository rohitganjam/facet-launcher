package com.facetlauncher.app.ui.home

import android.appwidget.AppWidgetHostView
import androidx.compose.ui.test.center
import androidx.compose.ui.test.down
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.up
import androidx.compose.ui.unit.dp
import com.facetlauncher.app.data.model.CalendarEvent
import com.facetlauncher.app.data.model.ClockAlignment
import com.facetlauncher.app.data.model.ClockColorOption
import com.facetlauncher.app.data.model.ClockDateStyle
import com.facetlauncher.app.data.model.ClockTemplateId
import com.facetlauncher.app.ui.theme.FacetLauncherTheme
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
            FacetLauncherTheme {
                ClockBlock(clock = fixedClock, locale = Locale.US)
            }
        }

        // Then it renders the matching time and date strings
        composeRule.onNodeWithText("9:05").assertExists()
        composeRule.onNodeWithText("Thursday, 27 August").assertExists()
    }

    @Test
    fun rendersHostedWidgetInsteadOfTheNativeClockWhenClockWidgetAppWidgetIdIsSet() {
        // Given a fixed clock, but the active facet has bound a hosted clock widget (PRD F15)
        val fixedClock = Clock.fixed(Instant.parse("2026-08-27T09:05:00Z"), ZoneOffset.UTC)

        // When the ClockBlock is composed with a non-null clockWidgetAppWidgetId
        composeRule.setContent {
            FacetLauncherTheme {
                ClockBlock(
                    clock = fixedClock,
                    locale = Locale.US,
                    clockWidgetAppWidgetId = 42,
                    clockWidgetWidthDp = 180.dp,
                    clockWidgetHeightDp = 90.dp,
                )
            }
        }

        // Then the hosted widget's host view renders in place of the native clock
        composeRule.onNodeWithTag("clock_widget_host_42").assertExists()
        composeRule.onNodeWithText("9:05").assertDoesNotExist()
    }

    @Test
    fun longPressingTheHostedWidgetFiresOnClockWidgetLongPressAndSwallowsItsOwnTap() {
        // Given a hosted widget whose own content has a real click listener (its tap action) —
        // real bug found on-device: a long-press on a hosted widget was firing this instead of
        // opening the clock-adjust sheet, because a plain View's OnClickListener only cares about
        // touch-slop, not hold duration.
        var longPressed = false
        var widgetOwnTapFired = false
        composeRule.setContent {
            FacetLauncherTheme {
                ClockBlock(
                    clockWidgetAppWidgetId = 42,
                    clockWidgetWidthDp = 180.dp,
                    clockWidgetHeightDp = 90.dp,
                    createClockWidgetHostView = { context, _ ->
                        AppWidgetHostView(context).apply { setOnClickListener { widgetOwnTapFired = true } }
                    },
                    onClockWidgetLongPress = { longPressed = true },
                )
            }
        }

        // When long-pressing the widget's own rendered surface — split into a real down/wait/up
        // rather than performTouchInput { longClick() }: that helper only fabricates MotionEvent
        // timestamps spanning the long-press duration, it doesn't make the test thread actually
        // wait in real time. This gesture needs a genuine native Handler.postDelayed callback on
        // the real main Looper — so the test has to let real time actually pass for it to fire.
        composeRule.onNodeWithTag("clock_widget_host_42").performTouchInput { down(center) }
        composeRule.waitUntil(timeoutMillis = 2_000) { longPressed }
        composeRule.onNodeWithTag("clock_widget_host_42").performTouchInput { up() }

        // Then Facet's own callback fires, and the widget's own tap action never does
        assertTrue(longPressed)
        assertTrue(!widgetOwnTapFired)
    }

    @Test
    fun aPlainTapOnTheHostedWidgetReachesItsOwnClickHandlerUnintercepted() {
        // Given the same setup, but this time a plain (short) tap
        var longPressed = false
        var widgetOwnTapFired = false
        composeRule.setContent {
            FacetLauncherTheme {
                ClockBlock(
                    clockWidgetAppWidgetId = 42,
                    clockWidgetWidthDp = 180.dp,
                    clockWidgetHeightDp = 90.dp,
                    createClockWidgetHostView = { context, _ ->
                        AppWidgetHostView(context).apply { setOnClickListener { widgetOwnTapFired = true } }
                    },
                    onClockWidgetLongPress = { longPressed = true },
                )
            }
        }

        composeRule.onNodeWithTag("clock_widget_host_42").performTouchInput {
            down(center)
            up()
        }
        composeRule.waitForIdle()

        // Then the widget's own tap action fires normally, same as it would without this wrapper
        assertTrue(widgetOwnTapFired)
        assertTrue(!longPressed)
    }

    @Test
    fun rendersTheNativeClockWhenNoClockWidgetIsBound() {
        // Given the default (no hosted clock widget for the active facet)
        val fixedClock = Clock.fixed(Instant.parse("2026-08-27T09:05:00Z"), ZoneOffset.UTC)

        composeRule.setContent {
            FacetLauncherTheme {
                ClockBlock(clock = fixedClock, locale = Locale.US)
            }
        }

        // Then the native clock renders, and there's no hosted widget host view at all
        composeRule.onNodeWithText("9:05").assertExists()
        composeRule.onNodeWithTag("clock_widget_host_42").assertDoesNotExist()
    }

    @Test
    fun rendersTwentyFourHourTimeWhenEnabled() {
        // Given the same fixed instant, but with 24-hour time on
        val fixedClock = Clock.fixed(Instant.parse("2026-08-27T09:05:00Z"), ZoneOffset.UTC)

        // When the ClockBlock is composed with use24HourTime = true
        composeRule.setContent {
            FacetLauncherTheme {
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
            FacetLauncherTheme {
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
            FacetLauncherTheme { ClockBlock(clock = fixedClock, locale = Locale.US) }
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
            FacetLauncherTheme {
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
            FacetLauncherTheme {
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
    fun fluidStackInvertedTemplateRendersTheHourAndMinuteSeparately() {
        // Given the inverted variant, which swaps FluidStackTemplate's thin/wide vs bold/narrow
        // treatment between hour and minute (see chat history)
        val fixedClock = Clock.fixed(Instant.parse("2026-08-27T09:05:00Z"), ZoneOffset.UTC)
        composeRule.setContent {
            FacetLauncherTheme {
                ClockBlock(clock = fixedClock, locale = Locale.US, templateId = ClockTemplateId.FLUID_STACK_INVERTED)
            }
        }
        composeRule.onNodeWithText("9").assertExists()
        composeRule.onNodeWithText("05").assertExists()
    }

    @Test
    fun accentedFluidStackTemplateRendersTheHourAndMinuteSeparately() {
        val fixedClock = Clock.fixed(Instant.parse("2026-08-27T09:05:00Z"), ZoneOffset.UTC)
        composeRule.setContent {
            FacetLauncherTheme {
                ClockBlock(clock = fixedClock, locale = Locale.US, templateId = ClockTemplateId.ACCENTED_FLUID_STACK)
            }
        }
        composeRule.onNodeWithText("9").assertExists()
        composeRule.onNodeWithText("05").assertExists()
        composeRule.onNodeWithText("Thursday, 27 August").assertExists()
    }

    @Test
    fun accentedFluidStackInvertedTemplateRendersTheHourAndMinuteSeparately() {
        val fixedClock = Clock.fixed(Instant.parse("2026-08-27T09:05:00Z"), ZoneOffset.UTC)
        composeRule.setContent {
            FacetLauncherTheme {
                ClockBlock(clock = fixedClock, locale = Locale.US, templateId = ClockTemplateId.ACCENTED_FLUID_STACK_INVERTED)
            }
        }
        composeRule.onNodeWithText("9").assertExists()
        composeRule.onNodeWithText("05").assertExists()
    }

    @Test
    fun accentContrastTemplateRendersTheHourAndMinuteSeparately() {
        val fixedClock = Clock.fixed(Instant.parse("2026-08-27T09:05:00Z"), ZoneOffset.UTC)
        composeRule.setContent {
            FacetLauncherTheme {
                ClockBlock(clock = fixedClock, locale = Locale.US, templateId = ClockTemplateId.ACCENT_CONTRAST)
            }
        }
        composeRule.onNodeWithText("9").assertExists()
        composeRule.onNodeWithText(":05").assertExists()
    }

    @Test
    fun verticalStackBoldHourStillRendersCorrectly() {
        // Given the bold-hour variant, whose hour weight went from ExtraBold to Black (see chat history)
        val fixedClock = Clock.fixed(Instant.parse("2026-08-27T09:05:00Z"), ZoneOffset.UTC)
        composeRule.setContent {
            FacetLauncherTheme {
                ClockBlock(clock = fixedClock, locale = Locale.US, templateId = ClockTemplateId.VERTICAL_STACK_BOLD_HOUR)
            }
        }
        composeRule.onNodeWithText("9").assertExists()
        composeRule.onNodeWithText("05").assertExists()
    }

    @Test
    fun flexNarrowLeftAlignmentRendersTimeBeforeDate() {
        // Given the default (Left) clock alignment
        val fixedClock = Clock.fixed(Instant.parse("2026-08-27T09:05:00Z"), ZoneOffset.UTC)
        composeRule.setContent {
            FacetLauncherTheme {
                ClockBlock(clock = fixedClock, locale = Locale.US, templateId = ClockTemplateId.ROBOTO_FLEX_NARROW)
            }
        }

        // Then the time digits sit to the left of the date column — today's unchanged reading order
        val hourLeft = composeRule.onNodeWithText("9").fetchSemanticsNode().boundsInRoot.left
        val dateLeft = composeRule.onNodeWithText("AUG", substring = true).fetchSemanticsNode().boundsInRoot.left
        assertTrue(hourLeft < dateLeft)
    }

    @Test
    fun flexNarrowRightAlignmentSwapsTimeAndDatePositions() {
        // Given Right clock alignment — a straight slide-right of the same order would put the
        // date, not the time, against the actual right edge
        val fixedClock = Clock.fixed(Instant.parse("2026-08-27T09:05:00Z"), ZoneOffset.UTC)
        composeRule.setContent {
            FacetLauncherTheme {
                ClockBlock(
                    clock = fixedClock,
                    locale = Locale.US,
                    templateId = ClockTemplateId.ROBOTO_FLEX_NARROW,
                    clockAlignment = ClockAlignment.RIGHT,
                )
            }
        }

        // Then the columns mirror instead: the date sits to the left, the time digits hug the right edge
        val hourLeft = composeRule.onNodeWithText("9").fetchSemanticsNode().boundsInRoot.left
        val dateLeft = composeRule.onNodeWithText("AUG", substring = true).fetchSemanticsNode().boundsInRoot.left
        assertTrue(dateLeft < hourLeft)
    }

    @Test
    fun clockAndCalendarShareTheSameAlignment() {
        // Given a clock aligned right — the calendar strip has no alignment of its own any more,
        // it follows the clock's (see chat history: calendar/appearance styling consolidation)
        val fixedClock = Clock.fixed(Instant.parse("2026-08-27T09:05:00Z"), ZoneOffset.UTC)
        val event = CalendarEvent(
            id = 1,
            calendarId = "1",
            title = "Standup",
            startTimeMillis = Instant.parse("2026-08-27T10:00:00Z").toEpochMilli(),
            endTimeMillis = Instant.parse("2026-08-27T10:30:00Z").toEpochMilli(),
            isAllDay = false,
        )

        // When the ClockBlock is composed with clockAlignment = RIGHT
        composeRule.setContent {
            FacetLauncherTheme {
                ClockBlock(
                    clock = fixedClock,
                    locale = Locale.US,
                    events = listOf(event),
                    clockAlignment = ClockAlignment.RIGHT,
                )
            }
        }

        // Then the calendar row's own alignment moved with the clock's — both are now end-aligned
        // within the same fillMaxWidth column, so their right edges (not left, since the two
        // items differ in width) land close together, near the container's right edge.
        val clockRight = composeRule.onNodeWithText("9:05").fetchSemanticsNode().boundsInRoot.right
        val calendarRowRight = composeRule.onNodeWithTag("clock_event_row_1").fetchSemanticsNode().boundsInRoot.right
        assertTrue(kotlin.math.abs(clockRight - calendarRowRight) < 50f)
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

        // When the ClockBlock is composed with clockAlignment = RIGHT (also governs the calendar
        // strip now — see chat history: calendar/appearance styling consolidation)
        composeRule.setContent {
            FacetLauncherTheme {
                ClockBlock(
                    clock = fixedClock,
                    locale = Locale.US,
                    events = listOf(event),
                    clockAlignment = ClockAlignment.RIGHT,
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
            FacetLauncherTheme {
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
    fun calendarCenterAlignmentKeepsRowsLeftAlignedWithinTheCenteredBlock() {
        // Given two events with very different title lengths, and CENTER alignment
        val fixedClock = Clock.fixed(Instant.parse("2026-08-27T09:05:00Z"), ZoneOffset.UTC)
        val events = listOf(
            CalendarEvent(id = 1, calendarId = "1", title = "A", startTimeMillis = Instant.parse("2026-08-27T10:00:00Z").toEpochMilli(), endTimeMillis = Instant.parse("2026-08-27T10:30:00Z").toEpochMilli(), isAllDay = false),
            CalendarEvent(id = 2, calendarId = "1", title = "A much longer event title", startTimeMillis = Instant.parse("2026-08-27T11:00:00Z").toEpochMilli(), endTimeMillis = Instant.parse("2026-08-27T11:30:00Z").toEpochMilli(), isAllDay = false),
        )

        // When the ClockBlock is composed with CENTER alignment
        composeRule.setContent {
            FacetLauncherTheme {
                ClockBlock(clock = fixedClock, locale = Locale.US, events = events, clockAlignment = ClockAlignment.CENTER)
            }
        }

        // Then both rows' own left edges line up — a naive per-row CenterHorizontally would
        // instead center each row independently by its own width, leaving their left edges
        // ragged; the block as a whole should be centered, with its rows left-aligned inside it
        // (see chat history — direct request).
        val row1Left = composeRule.onNodeWithTag("clock_event_row_1").fetchSemanticsNode().boundsInRoot.left
        val row2Left = composeRule.onNodeWithTag("clock_event_row_2").fetchSemanticsNode().boundsInRoot.left
        assertTrue(kotlin.math.abs(row1Left - row2Left) < 1f)
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
            FacetLauncherTheme { ClockBlock(clock = fixedClock, locale = Locale.US, events = events) }
        }

        // Then only the first three (soonest) render, not the whole day's agenda
        composeRule.onNodeWithText("Event 1").assertExists()
        composeRule.onNodeWithText("Event 2").assertExists()
        composeRule.onNodeWithText("Event 3").assertExists()
        composeRule.onNodeWithText("Event 4").assertDoesNotExist()
        composeRule.onNodeWithText("Event 5").assertDoesNotExist()
    }

    // The 16 shape-based templates (ClockTemplates.kt's own "Accent Field" through "Half
    // Immersed") — one smoke test each, matching this file's existing rigor level: assert the
    // hour/minute render somewhere in the tree without crashing, not pixel-perfect shape checks.

    @Test
    fun accentFieldTemplateRendersTheHourAndMinuteSeparately() {
        val fixedClock = Clock.fixed(Instant.parse("2026-08-27T09:05:00Z"), ZoneOffset.UTC)
        composeRule.setContent {
            FacetLauncherTheme { ClockBlock(clock = fixedClock, locale = Locale.US, templateId = ClockTemplateId.ACCENT_FIELD) }
        }
        composeRule.onNodeWithText("9").assertExists()
        composeRule.onNodeWithText("05").assertExists()
    }

    @Test
    fun hourTileTemplateRendersTheHourAndMinuteSeparately() {
        val fixedClock = Clock.fixed(Instant.parse("2026-08-27T09:05:00Z"), ZoneOffset.UTC)
        composeRule.setContent {
            FacetLauncherTheme { ClockBlock(clock = fixedClock, locale = Locale.US, templateId = ClockTemplateId.HOUR_TILE) }
        }
        composeRule.onNodeWithText("9").assertExists()
        composeRule.onNodeWithText("05").assertExists()
    }

    @Test
    fun chipTemplateRendersTheCombinedTime() {
        val fixedClock = Clock.fixed(Instant.parse("2026-08-27T09:05:00Z"), ZoneOffset.UTC)
        composeRule.setContent {
            FacetLauncherTheme { ClockBlock(clock = fixedClock, locale = Locale.US, templateId = ClockTemplateId.CHIP) }
        }
        composeRule.onNodeWithText("9:05").assertExists()
    }

    @Test
    fun duotoneOverlapTemplateRendersTheHourAndMinuteSeparately() {
        val fixedClock = Clock.fixed(Instant.parse("2026-08-27T09:05:00Z"), ZoneOffset.UTC)
        composeRule.setContent {
            FacetLauncherTheme { ClockBlock(clock = fixedClock, locale = Locale.US, templateId = ClockTemplateId.DUOTONE_OVERLAP) }
        }
        composeRule.onNodeWithText("9").assertExists()
        composeRule.onNodeWithText("05").assertExists()
    }

    @Test
    fun cornerFrameTemplateRendersTheCombinedTime() {
        val fixedClock = Clock.fixed(Instant.parse("2026-08-27T09:05:00Z"), ZoneOffset.UTC)
        composeRule.setContent {
            FacetLauncherTheme { ClockBlock(clock = fixedClock, locale = Locale.US, templateId = ClockTemplateId.CORNER_FRAME) }
        }
        composeRule.onNodeWithText("9:05").assertExists()
    }

    @Test
    fun stubTemplateRendersTheCombinedTime() {
        val fixedClock = Clock.fixed(Instant.parse("2026-08-27T09:05:00Z"), ZoneOffset.UTC)
        composeRule.setContent {
            FacetLauncherTheme { ClockBlock(clock = fixedClock, locale = Locale.US, templateId = ClockTemplateId.STUB) }
        }
        composeRule.onNodeWithText("9:05").assertExists()
    }

    @Test
    fun haloTemplateRendersTheCombinedTime() {
        val fixedClock = Clock.fixed(Instant.parse("2026-08-27T09:05:00Z"), ZoneOffset.UTC)
        composeRule.setContent {
            FacetLauncherTheme { ClockBlock(clock = fixedClock, locale = Locale.US, templateId = ClockTemplateId.HALO) }
        }
        composeRule.onNodeWithText("9:05").assertExists()
    }

    @Test
    fun digitCellsTemplateRendersEachDigitSeparately() {
        val fixedClock = Clock.fixed(Instant.parse("2026-08-27T09:05:00Z"), ZoneOffset.UTC)
        composeRule.setContent {
            FacetLauncherTheme { ClockBlock(clock = fixedClock, locale = Locale.US, templateId = ClockTemplateId.DIGIT_CELLS) }
        }
        composeRule.onNodeWithText("9").assertExists()
        composeRule.onNodeWithText("0").assertExists()
        composeRule.onNodeWithText("5").assertExists()
    }

    @Test
    fun negativePanelTemplateRendersTheCombinedTime() {
        val fixedClock = Clock.fixed(Instant.parse("2026-08-27T09:05:00Z"), ZoneOffset.UTC)
        composeRule.setContent {
            FacetLauncherTheme { ClockBlock(clock = fixedClock, locale = Locale.US, templateId = ClockTemplateId.NEGATIVE_PANEL) }
        }
        composeRule.onNodeWithText("9:05").assertExists()
    }

    @Test
    fun hollowHourTemplateRendersTheHourAndMinuteSeparately() {
        val fixedClock = Clock.fixed(Instant.parse("2026-08-27T09:05:00Z"), ZoneOffset.UTC)
        composeRule.setContent {
            FacetLauncherTheme { ClockBlock(clock = fixedClock, locale = Locale.US, templateId = ClockTemplateId.HOLLOW_HOUR) }
        }
        composeRule.onNodeWithText("9").assertExists()
        composeRule.onNodeWithText("05").assertExists()
    }

    @Test
    fun highlighterTemplateRendersTheCombinedTime() {
        val fixedClock = Clock.fixed(Instant.parse("2026-08-27T09:05:00Z"), ZoneOffset.UTC)
        composeRule.setContent {
            FacetLauncherTheme { ClockBlock(clock = fixedClock, locale = Locale.US, templateId = ClockTemplateId.HIGHLIGHTER) }
        }
        composeRule.onNodeWithText("9:05").assertExists()
    }

    @Test
    fun columnRuleTemplateRendersTheHourAndMinuteSeparately() {
        val fixedClock = Clock.fixed(Instant.parse("2026-08-27T09:05:00Z"), ZoneOffset.UTC)
        composeRule.setContent {
            FacetLauncherTheme { ClockBlock(clock = fixedClock, locale = Locale.US, templateId = ClockTemplateId.COLUMN_RULE) }
        }
        composeRule.onNodeWithText("9").assertExists()
        composeRule.onNodeWithText("05").assertExists()
    }

    @Test
    fun colonMarkTemplateRendersTheHourAndMinuteSeparately() {
        val fixedClock = Clock.fixed(Instant.parse("2026-08-27T09:05:00Z"), ZoneOffset.UTC)
        composeRule.setContent {
            FacetLauncherTheme { ClockBlock(clock = fixedClock, locale = Locale.US, templateId = ClockTemplateId.COLON_MARK) }
        }
        composeRule.onNodeWithText("9").assertExists()
        composeRule.onNodeWithText("05").assertExists()
    }

    @Test
    fun pillPairTemplateRendersTheHourAndMinuteSeparately() {
        val fixedClock = Clock.fixed(Instant.parse("2026-08-27T09:05:00Z"), ZoneOffset.UTC)
        composeRule.setContent {
            FacetLauncherTheme { ClockBlock(clock = fixedClock, locale = Locale.US, templateId = ClockTemplateId.PILL_PAIR) }
        }
        composeRule.onNodeWithText("9").assertExists()
        composeRule.onNodeWithText("05").assertExists()
    }

    @Test
    fun shelfTemplateRendersTheCombinedTime() {
        val fixedClock = Clock.fixed(Instant.parse("2026-08-27T09:05:00Z"), ZoneOffset.UTC)
        composeRule.setContent {
            FacetLauncherTheme { ClockBlock(clock = fixedClock, locale = Locale.US, templateId = ClockTemplateId.SHELF) }
        }
        composeRule.onNodeWithText("9:05").assertExists()
    }

    @Test
    fun halfImmersedTemplateRendersTheCombinedTime() {
        val fixedClock = Clock.fixed(Instant.parse("2026-08-27T09:05:00Z"), ZoneOffset.UTC)
        composeRule.setContent {
            FacetLauncherTheme { ClockBlock(clock = fixedClock, locale = Locale.US, templateId = ClockTemplateId.HALF_IMMERSED) }
        }
        composeRule.onNodeWithText("9:05").assertExists()
    }

    @Test
    fun condensedDateStyleAbbreviatesTheDayAndMonth() {
        // Given the condensed date style, on a template that renders its date through the shared
        // dateFormatter() helper
        val fixedClock = Clock.fixed(Instant.parse("2026-08-27T09:05:00Z"), ZoneOffset.UTC)
        composeRule.setContent {
            FacetLauncherTheme {
                ClockBlock(clock = fixedClock, locale = Locale.US, dateStyle = ClockDateStyle.CONDENSED)
            }
        }

        // Then the date reads "Thu, 27 Aug", not "Thursday, 27 August"
        composeRule.onNodeWithText("Thu, 27 Aug").assertExists()
        composeRule.onNodeWithText("Thursday, 27 August").assertDoesNotExist()
    }

    @Test
    fun fullDateStyleIsTheUnchangedDefault() {
        val fixedClock = Clock.fixed(Instant.parse("2026-08-27T09:05:00Z"), ZoneOffset.UTC)
        composeRule.setContent {
            FacetLauncherTheme { ClockBlock(clock = fixedClock, locale = Locale.US) }
        }
        composeRule.onNodeWithText("Thursday, 27 August").assertExists()
    }

    @Test
    fun accentUsingTemplateStillRendersWithACustomAccentColorOption() {
        // Given ACCENT_FIELD (an accent-using template) with a non-default accent color option
        val fixedClock = Clock.fixed(Instant.parse("2026-08-27T09:05:00Z"), ZoneOffset.UTC)
        composeRule.setContent {
            FacetLauncherTheme {
                ClockBlock(
                    clock = fixedClock,
                    locale = Locale.US,
                    templateId = ClockTemplateId.ACCENT_FIELD,
                    accentColorOption = ClockColorOption.ACCENT_SECONDARY,
                )
            }
        }
        composeRule.onNodeWithText("9").assertExists()
        composeRule.onNodeWithText("05").assertExists()
    }
}
