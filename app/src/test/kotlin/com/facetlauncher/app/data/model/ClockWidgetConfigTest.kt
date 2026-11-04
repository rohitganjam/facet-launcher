package com.facetlauncher.app.data.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test

class ClockWidgetConfigTest {

    private val oslo = WeatherCity("Oslo", 59.91, 10.75, "Europe/Oslo")

    @Test
    fun `a clock's built-in defaults match the launcher's own defaults`() {
        // Given the config a new clock starts from and the launcher's defaults for the same things
        val config = ClockWidgetConfig()
        val settings = LauncherSettings()

        // Then they agree, so a new clock looks like a fresh install's clock
        assertEquals(settings.clockTemplateId, config.templateId)
        assertEquals(settings.clockFontOption, config.fontOption)
        assertEquals(settings.clockColorOption, config.colorOption)
        assertEquals(settings.clockAccentColorOption, config.accentColorOption)
        assertEquals(settings.use24HourTime, config.use24HourTime)
        assertEquals(settings.clockShowMeridiem, config.showMeridiem)
        assertEquals(settings.clockDateStyle, config.dateStyle)
        assertEquals(settings.showNextAlarm, config.showAlarm)
        assertEquals(settings.showBatteryPercentage, config.showBattery)
        assertEquals(settings.showWeather, config.showWeather)
        assertEquals(settings.weatherIconStyle, config.weatherIconStyle)
        assertEquals(settings.showCalendarIndicator, config.calendar.showIndicator)
        assertEquals(settings.calendarItemsToShow, config.calendar.itemsToShow)
        assertEquals(settings.showAllDayEvents, config.calendar.showAllDay)
        assertEquals(settings.selectedCalendarIds, config.calendar.calendarIds)
    }

    @Test
    fun `a clock with every field changed round trips`() {
        // Given a clock set away from every default
        val config = NativeWidgetConfig(
            clock = ClockWidgetConfig(
                templateId = ClockTemplateId.entries.last(), fontOption = ClockFontOption.entries.last(),
                colorOption = ClockColorOption.entries.last(), accentColorOption = ClockColorOption.entries.first(),
                use24HourTime = true, showMeridiem = true, dateStyle = ClockDateStyle.entries.last(),
                showAlarm = false, showBattery = false, showWeather = true, weatherIconStyle = WeatherIconStyle.entries.last(),
                weatherLocation = WidgetWeatherLocation.City(oslo),
                calendar = ClockCalendarConfig(showIndicator = false, itemsToShow = 1, showAllDay = false, calendarIds = setOf("work:2", "5")),
            ),
        )

        // When stored and read back
        val decoded = decodeNativeWidgetConfig(config.encode())

        // Then nothing changed
        assertEquals(config, decoded)
    }

    @Test
    fun `a clock with only defaults is still stored, since a clock has nothing to follow`() {
        // Given a default clock
        val config = NativeWidgetConfig(clock = ClockWidgetConfig())

        // Then it is not "follows launcher", it has a stored form, and it reads back as a clock
        assertNotNull(config.encode())
        assertEquals(ClockWidgetConfig(), decodeNativeWidgetConfig(config.encode()).clock)
    }

    @Test
    fun `all calendars stays apart from an explicit empty selection`() {
        // Given a clock on all calendars and one on none
        val all = decodeNativeWidgetConfig(NativeWidgetConfig(clock = ClockWidgetConfig()).encode()).clock
        val none = decodeNativeWidgetConfig(NativeWidgetConfig(clock = ClockWidgetConfig(calendar = ClockCalendarConfig(calendarIds = emptySet()))).encode()).clock

        // Then they differ
        assertNull(all?.calendar?.calendarIds)
        assertEquals(emptySet<String>(), none?.calendar?.calendarIds)
    }

    @Test
    fun `an option name this version does not know reads as that option's default`() {
        // Given a stored clock naming a template and a font from a newer version
        val decoded = decodeNativeWidgetConfig("""{"clock":{"template":"FROM_THE_FUTURE","font":"ALSO_NEW","use24Hour":true}}""").clock

        // Then those two fall back and the rest is read
        assertEquals(ClockWidgetConfig().templateId, decoded?.templateId)
        assertEquals(ClockWidgetConfig().fontOption, decoded?.fontOption)
        assertEquals(true, decoded?.use24HourTime)
    }
}
