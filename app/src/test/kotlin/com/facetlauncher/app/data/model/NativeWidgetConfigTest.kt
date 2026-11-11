package com.facetlauncher.app.data.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class NativeWidgetConfigTest {

    private val oslo = WeatherCity("Oslo", 59.91, 10.75, "Europe/Oslo")

    @Test
    fun `a widget that follows the launcher stores nothing`() {
        // Given the default config
        val config = NativeWidgetConfig()

        // Then it encodes to null and reads back as itself
        assertNull(config.encode())
        assertTrue(decodeNativeWidgetConfig(null).followsLauncher)
    }

    @Test
    fun `chosen calendars round trip`() {
        // Given two chosen calendars
        val config = NativeWidgetConfig(calendarIds = setOf("7", "3"))

        // When encoded and decoded
        val decoded = decodeNativeWidgetConfig(config.encode())

        // Then the same set comes back
        assertEquals(setOf("3", "7"), decoded.calendarIds)
        assertNull(decoded.weatherLocation)
    }

    @Test
    fun `a clock's calendar glass round trips and defaults off`() {
        // Given a clock whose calendar has a glass style
        val clock = ClockWidgetConfig(calendar = ClockCalendarConfig(glass = GlassStyle.ACCENT))

        // When encoded and decoded
        val decoded = decodeNativeWidgetConfig(NativeWidgetConfig(clock = clock).encode())

        // Then glass is kept, and a stored clock without the field reads as off
        assertEquals(GlassStyle.ACCENT, decoded.clock?.calendar?.glass)
        assertEquals(GlassStyle.OFF, decodeNativeWidgetConfig(NativeWidgetConfig(clock = ClockWidgetConfig()).encode()).clock?.calendar?.glass)
    }

    @Test
    fun `no calendars chosen is kept apart from following the launcher`() {
        // Given an explicitly empty selection
        val decoded = decodeNativeWidgetConfig(NativeWidgetConfig(calendarIds = emptySet()).encode())

        // Then it is an empty set, not null
        assertEquals(emptySet<String>(), decoded.calendarIds)
    }

    @Test
    fun `a chosen city round trips`() {
        // Given a widget set to a city
        val decoded = decodeNativeWidgetConfig(NativeWidgetConfig(weatherLocation = WidgetWeatherLocation.City(oslo)).encode())

        // Then the whole city comes back
        assertEquals(WidgetWeatherLocation.City(oslo), decoded.weatherLocation)
    }

    @Test
    fun `current location round trips`() {
        val decoded = decodeNativeWidgetConfig(NativeWidgetConfig(weatherLocation = WidgetWeatherLocation.Current).encode())

        assertEquals(WidgetWeatherLocation.Current, decoded.weatherLocation)
    }

    @Test
    fun `a city stored without its coordinates follows the launcher`() {
        // Given stored JSON naming a city but missing its coordinates
        val decoded = decodeNativeWidgetConfig("""{"weather":{"city":"Oslo"}}""")

        // Then no location is read
        assertNull(decoded.weatherLocation)
    }

    @Test
    fun `a value that is not JSON follows the launcher`() {
        // Given text that does not parse, including the old tab-separated form
        // Then nothing is read, and the widget follows the launcher
        assertTrue(decodeNativeWidgetConfig("not json").followsLauncher)
        assertTrue(decodeNativeWidgetConfig("wx\tcurrent").followsLauncher)
        assertTrue(decodeNativeWidgetConfig("").followsLauncher)
    }

    @Test
    fun `fields a newer version added are ignored`() {
        // Given stored JSON with a key this version does not know
        val decoded = decodeNativeWidgetConfig("""{"showAllDayEvents":true,"somethingNew":1}""")

        // Then the fields it knows are still read
        assertEquals(true, decoded.showAllDayEvents)
    }

    @Test
    fun `the stored form is JSON with the chosen calendars sorted and fields that follow the launcher left out`() {
        // Given a widget with two calendars chosen and nothing else
        val encoded = NativeWidgetConfig(calendarIds = setOf("7", "3")).encode()

        // Then only that field is stored, in a stable order
        assertEquals("""{"calendarIds":["3","7"]}""", encoded)
    }

    @Test
    fun `the all-day choice round trips and is kept apart from following the launcher`() {
        // Given a widget set to hide all-day events and nothing else
        val config = NativeWidgetConfig(showAllDayEvents = false)

        // When encoded and decoded
        val decoded = decodeNativeWidgetConfig(config.encode())

        // Then the choice is kept, with no calendars of its own, and the widget is no longer following the launcher
        assertEquals(false, decoded.showAllDayEvents)
        assertNull(decoded.calendarIds)
        assertTrue(!decoded.followsLauncher)
    }

    @Test
    fun `a weather location on its own round trips through the shared encoding`() {
        // Given each kind of location, and none
        val current = WidgetWeatherLocation.Current
        val city = WidgetWeatherLocation.City(oslo)

        // Then each survives, and none stores nothing
        assertEquals(current, decodeWeatherLocation(encodeWeatherLocation(current)))
        assertEquals(city, decodeWeatherLocation(encodeWeatherLocation(city)))
        assertNull(encodeWeatherLocation(null))
        assertNull(decodeWeatherLocation(null))
    }

    @Test
    fun `a damaged stored weather location follows Weather settings`() {
        // Given a stored value that does not hold a whole location
        // Then it decodes to nothing rather than a half-built place
        assertNull(decodeWeatherLocation("""{"weather":{"city":"Oslo"}}"""))
        assertNull(decodeWeatherLocation("not a location"))
    }
}
