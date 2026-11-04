package com.facetlauncher.app.domain

import com.facetlauncher.app.data.model.ClockColorOption
import com.facetlauncher.app.data.model.ClockTemplateId
import com.facetlauncher.app.data.model.Facet
import com.facetlauncher.app.data.model.LauncherSettings
import com.facetlauncher.app.data.model.WeatherCity
import com.facetlauncher.app.data.model.WidgetWeatherLocation
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class EffectiveClockConfigTest {

    private val settings = LauncherSettings(
        clockTemplateId = ClockTemplateId.entries.last(), use24HourTime = true, showWeather = true,
        showCalendarIndicator = false, calendarItemsToShow = 2, showAllDayEvents = false, selectedCalendarIds = setOf("4"),
    )

    @Test
    fun `a facet that inherits gets the launcher's clock and calendar`() {
        // Given a facet that overrides neither the clock nor the calendar
        val facet = Facet(name = "Home", position = 0)

        // When its effective config is worked out
        val config = effectiveClockConfig(facet, settings)

        // Then it is the launcher's, so nothing changes on screen
        assertEquals(ClockTemplateId.entries.last(), config.templateId)
        assertEquals(true, config.use24HourTime)
        assertEquals(true, config.showWeather)
        assertEquals(false, config.calendar.showIndicator)
        assertEquals(2, config.calendar.itemsToShow)
        assertEquals(false, config.calendar.showAllDay)
        assertEquals(setOf("4"), config.calendar.calendarIds)
    }

    @Test
    fun `a facet that overrides the clock keeps its own look and accessories`() {
        // Given a facet with its own clock look, accessories and weather place
        val oslo = WidgetWeatherLocation.City(WeatherCity("Oslo", 59.91, 10.75, "Europe/Oslo"))
        val facet = Facet(
            name = "Work", position = 0, overrideClock = true, clockTemplateId = ClockTemplateId.entries.first(),
            clockColorOption = ClockColorOption.entries.last(), use24HourTime = false, clockShowWeather = false, clockShowAlarm = false,
            clockWeatherLocation = oslo,
        )

        // Then its own values win over the launcher's, including the facet's own weather place
        val config = effectiveClockConfig(facet, settings)
        assertEquals(ClockTemplateId.entries.first(), config.templateId)
        assertEquals(ClockColorOption.entries.last(), config.colorOption)
        assertEquals(false, config.use24HourTime)
        assertEquals(false, config.showWeather)
        assertEquals(false, config.showAlarm)
        assertEquals(oslo, config.weatherLocation)
    }

    @Test
    fun `a facet that overrides the calendar keeps its own calendars and display`() {
        // Given a facet with its own calendar choices, and the launcher's clock
        val facet = Facet(
            name = "Work", position = 0, overrideCalendar = true, showAllDayEvents = true, showCalendarIndicator = true,
            calendarItemsToShow = 3, selectedCalendarIds = setOf("9", "work:1"),
        )

        // Then the calendar part is the facet's and the clock part is still the launcher's
        val config = effectiveClockConfig(facet, settings)
        assertEquals(true, config.calendar.showAllDay)
        assertEquals(true, config.calendar.showIndicator)
        assertEquals(3, config.calendar.itemsToShow)
        assertEquals(setOf("9", "work:1"), config.calendar.calendarIds)
        assertEquals(ClockTemplateId.entries.last(), config.templateId)
    }

    @Test
    fun `a facet that follows every calendar keeps following all of them`() {
        // Given the launcher on all calendars (null) and an inheriting facet
        val config = effectiveClockConfig(Facet(name = "Home", position = 0), LauncherSettings())

        // Then the clock is on all calendars too, so one added later still shows
        assertNull(config.calendar.calendarIds)
    }

    @Test
    fun `no facet gives the launcher's clock`() {
        assertEquals(ClockTemplateId.entries.last(), effectiveClockConfig(null, settings).templateId)
    }
}
