package com.facetlauncher.app.ui.settings.automation

import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.time.DayOfWeek
import java.util.Locale

class AutomationRuleFormattingTest {

    private var originalLocale: Locale = Locale.getDefault()

    @Before fun setUp() {
        originalLocale = Locale.getDefault()
        Locale.setDefault(Locale.US)
    }

    @After fun tearDown() = Locale.setDefault(originalLocale)

    private fun days(days: Set<DayOfWeek>) = formatDays(days, weekdays = "Weekdays", weekends = "Weekends", everyDay = "Every day")

    @Test
    fun `monday to friday is called weekdays`() {
        assertEquals("Weekdays", days(setOf(DayOfWeek.MONDAY, DayOfWeek.TUESDAY, DayOfWeek.WEDNESDAY, DayOfWeek.THURSDAY, DayOfWeek.FRIDAY)))
    }

    @Test
    fun `saturday and sunday are called weekends`() {
        assertEquals("Weekends", days(setOf(DayOfWeek.SUNDAY, DayOfWeek.SATURDAY)))
    }

    @Test
    fun `all seven days are called every day`() {
        assertEquals("Every day", days(DayOfWeek.entries.toSet()))
    }

    @Test
    fun `any other set lists the short day names in week order`() {
        assertEquals("Mon, Wed, Sun", days(setOf(DayOfWeek.SUNDAY, DayOfWeek.WEDNESDAY, DayOfWeek.MONDAY)))
    }

    @Test
    fun `a single day is listed on its own`() {
        assertEquals("Fri", days(setOf(DayOfWeek.FRIDAY)))
    }

    @Test
    fun `minutes are shown in the phone's time format`() {
        val nineAm = minuteText(9 * 60)
        val sixPm = minuteText(18 * 60 + 5)

        assertTrue(nineAm, nineAm.startsWith("9:00") && nineAm.contains("AM"))
        assertTrue(sixPm, sixPm.startsWith("6:05") && sixPm.contains("PM"))
    }

    @Test
    fun `the last minute of the day formats too`() {
        assertTrue(minuteText(23 * 60 + 59).startsWith("11:59"))
    }
}
