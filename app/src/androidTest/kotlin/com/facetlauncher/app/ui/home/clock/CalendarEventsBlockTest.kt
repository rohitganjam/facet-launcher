package com.facetlauncher.app.ui.home.clock

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import com.facetlauncher.app.data.model.CalendarDisplayOptions
import com.facetlauncher.app.data.model.CalendarEvent
import com.facetlauncher.app.ui.theme.FacetLauncherTheme
import java.time.Clock
import java.time.Instant
import java.time.ZoneOffset
import kotlinx.collections.immutable.toImmutableList
import org.junit.Rule
import org.junit.Test

class CalendarEventsBlockTest {

    @get:Rule
    val composeRule = createComposeRule()

    private val clock = Clock.fixed(Instant.parse("2026-08-27T09:00:00Z"), ZoneOffset.UTC)

    private val events = (1L..3L).map { id ->
        CalendarEvent(
            id = id,
            calendarId = "1",
            title = "Event $id",
            startTimeMillis = clock.millis() + id * 3_600_000,
            endTimeMillis = clock.millis() + (id + 1) * 3_600_000,
            isAllDay = false,
        )
    }

    private fun setBlock(display: CalendarDisplayOptions) {
        composeRule.setContent {
            FacetLauncherTheme { CalendarEventsBlock(events = events.toImmutableList(), clock = clock, display = display) }
        }
    }

    @Test
    fun `indicator bars show by default`() {
        // Given the default display options, When composed
        setBlock(CalendarDisplayOptions())

        // Then every row has its calendar indicator
        events.forEach { composeRule.onNodeWithTag("clock_event_indicator_${it.id}", useUnmergedTree = true).assertExists() }
    }

    @Test
    fun `indicator bars are removed when turned off`() {
        // Given the indicator is off, When composed
        setBlock(CalendarDisplayOptions(showIndicator = false))

        // Then rows render but none has an indicator
        events.forEach {
            composeRule.onNodeWithTag("clock_event_row_${it.id}").assertExists()
            composeRule.onNodeWithTag("clock_event_indicator_${it.id}", useUnmergedTree = true).assertDoesNotExist()
        }
    }

    @Test
    fun `item count caps the number of rows`() {
        // Given a limit of two items, When composed with three events
        setBlock(CalendarDisplayOptions(itemsToShow = 2))

        // Then only the first two rows render
        composeRule.onNodeWithTag("clock_event_row_1").assertExists()
        composeRule.onNodeWithTag("clock_event_row_2").assertExists()
        composeRule.onNodeWithTag("clock_event_row_3").assertDoesNotExist()
    }

    @Test
    fun `rows for tomorrow carry a weekday prefix and rows for today do not`() {
        // Given one event later today and one tomorrow (fixed clock is Thursday 27 Aug 2026)
        val today = events.first()
        val tomorrow = today.copy(id = 9, title = "Tomorrow thing", startTimeMillis = clock.millis() + 24 * 3_600_000, endTimeMillis = clock.millis() + 25 * 3_600_000)
        composeRule.setContent {
            FacetLauncherTheme { CalendarEventsBlock(events = listOf(today, tomorrow).toImmutableList(), clock = clock, locale = java.util.Locale.US, use24HourTime = true) }
        }

        // Then tomorrow's row is prefixed with Fri and today's row is not
        composeRule.onNodeWithText("Fri 09:00").assertExists()
        composeRule.onNodeWithText("10:00").assertExists()
    }
}
