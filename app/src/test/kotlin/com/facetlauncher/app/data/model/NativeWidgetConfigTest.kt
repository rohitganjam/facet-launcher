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
    fun `a damaged city line follows the launcher`() {
        // Given a city line missing its coordinates
        val decoded = decodeNativeWidgetConfig("wx\tcity\tOslo")

        // Then no location is read
        assertNull(decoded.weatherLocation)
    }
}
