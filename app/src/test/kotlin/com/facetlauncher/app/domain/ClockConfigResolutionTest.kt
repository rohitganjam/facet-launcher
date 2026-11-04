package com.facetlauncher.app.domain

import com.facetlauncher.app.data.model.ClockColorOption
import com.facetlauncher.app.data.model.ClockTemplateId
import com.facetlauncher.app.data.model.Facet
import com.facetlauncher.app.data.model.encode
import com.facetlauncher.app.data.model.NativeWidgetType
import com.facetlauncher.app.data.model.NativeWidgetConfig
import com.facetlauncher.app.data.model.ClockWidgetConfig
import com.facetlauncher.app.data.model.ClockStackItemKind
import com.facetlauncher.app.data.model.ClockStackItem
import com.facetlauncher.app.data.model.LauncherSettings
import com.facetlauncher.app.data.model.WeatherCity
import com.facetlauncher.app.data.model.WidgetWeatherLocation
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class ClockConfigResolutionTest {

    private val settings = LauncherSettings(
        clockTemplateId = ClockTemplateId.entries.last(), use24HourTime = true, showWeather = true,
        showCalendarIndicator = false, calendarItemsToShow = 2, showAllDayEvents = false, selectedCalendarIds = setOf("4"),
    )

    @Test
    fun `a facet that inherits gets the launcher's clock and calendar`() {
        // Given a facet that overrides neither the clock nor the calendar
        val facet = Facet(name = "Home", position = 0)

        // When its effective config is worked out
        val config = legacyClockConfig(facet, settings)

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
        val config = legacyClockConfig(facet, settings)
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
        val config = legacyClockConfig(facet, settings)
        assertEquals(true, config.calendar.showAllDay)
        assertEquals(true, config.calendar.showIndicator)
        assertEquals(3, config.calendar.itemsToShow)
        assertEquals(setOf("9", "work:1"), config.calendar.calendarIds)
        assertEquals(ClockTemplateId.entries.last(), config.templateId)
    }

    @Test
    fun `a facet that follows every calendar keeps following all of them`() {
        // Given the launcher on all calendars (null) and an inheriting facet
        val config = legacyClockConfig(Facet(name = "Home", position = 0), LauncherSettings())

        // Then the clock is on all calendars too, so one added later still shows
        assertNull(config.calendar.calendarIds)
    }

    @Test
    fun `no facet gives the launcher's clock`() {
        assertEquals(ClockTemplateId.entries.last(), legacyClockConfig(null, settings).templateId)
    }

    private fun facetWithStored(config: ClockWidgetConfig, legacy: Facet = Facet(name = "Home", position = 0)) = legacy.copy(
        clockStack = listOf(ClockStackItem(id = 3, facetId = 1, position = 0, kind = ClockStackItemKind.CLOCK, nativeWidgetConfig = NativeWidgetConfig(clock = config).encode())),
    )

    @Test
    fun `the clock's own settings win over the launcher and the facet's old override switches`() {
        // Given a clock with its own settings on a facet whose old fields say otherwise
        val own = ClockWidgetConfig(templateId = ClockTemplateId.entries.first(), use24HourTime = false, showWeather = false)
        val facet = facetWithStored(own, Facet(name = "Work", position = 0, overrideClock = true, clockTemplateId = ClockTemplateId.entries.last(), use24HourTime = true, clockShowWeather = true))

        // Then the clock's own config is what is in force
        assertEquals(own, effectiveClockConfig(facet, settings))
    }

    @Test
    fun `a clock with nothing stored yet shows what its facet showed before`() {
        // Given a facet whose clock page has no settings yet
        val facet = Facet(name = "Home", position = 0)

        // Then the legacy values are in force
        assertEquals(legacyClockConfig(facet, settings), effectiveClockConfig(facet, settings))
    }

    @Test
    fun `a facet with no clock page falls back to the legacy values`() {
        // Given a facet whose stack holds only a widget
        val facet = Facet(name = "Home", position = 0, clockStack = listOf(ClockStackItem(id = 3, facetId = 1, position = 0, kind = ClockStackItemKind.NATIVE_WIDGET, nativeWidgetType = NativeWidgetType.WEATHER)))

        // Then the legacy values are in force
        assertEquals(legacyClockConfig(facet, settings), effectiveClockConfig(facet, settings))
    }
}
