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
    fun `a clock's background round trips and defaults off`() {
        // Given a clock with a background shared by its time and events
        val clock = ClockWidgetConfig(glass = GlassStyle.ACCENT)

        // When encoded and decoded
        val decoded = decodeNativeWidgetConfig(NativeWidgetConfig(clock = clock).encode())

        // Then it is kept, and a clock that chose none reads as off
        assertEquals(GlassStyle.ACCENT, decoded.clock?.glass)
        assertEquals(GlassStyle.OFF, decodeNativeWidgetConfig(NativeWidgetConfig(clock = ClockWidgetConfig()).encode()).clock?.glass)
    }

    @Test
    fun `a background stored on the calendar becomes the clock's, unless the clock has its own`() {
        // Given a clock stored the old way, with a background on its events only
        val old = decodeNativeWidgetConfig("{\"clock\":{\"calendarGlass\":\"DARK_FROST\"}}")

        // Then it is the clock's background now
        assertEquals(GlassStyle.DARK_FROST, old.clock?.glass)

        // And a clock that already has its own keeps it
        val both = decodeNativeWidgetConfig("{\"clock\":{\"glass\":\"BRIGHT\",\"calendarGlass\":\"DARK_FROST\"}}")
        assertEquals(GlassStyle.BRIGHT, both.clock?.glass)
    }

    @Test
    fun `a widget's background round trips and is kept apart from following the launcher`() {
        // Given a widget with only a background chosen
        val config = NativeWidgetConfig(glass = GlassStyle.DARK_FROST)

        // When encoded and decoded
        val decoded = decodeNativeWidgetConfig(config.encode())

        // Then it is kept and the widget no longer counts as following the launcher
        assertEquals(GlassStyle.DARK_FROST, decoded.glass)
        assertTrue(!decoded.followsLauncher)
        assertNull(decodeNativeWidgetConfig("{\"glass\":\"NOT_A_STYLE\"}").glass)
    }

    @Test
    fun `a widget's text colours round trip and are kept apart from following the launcher`() {
        // Given a widget with only light text chosen
        val config = NativeWidgetConfig(textTheme = WidgetTextTheme.LIGHT_TEXT)

        // When encoded and decoded
        val decoded = decodeNativeWidgetConfig(config.encode())

        // Then it is kept, the widget no longer follows the launcher, and a name this version does not know reads as unset
        assertEquals(WidgetTextTheme.LIGHT_TEXT, decoded.textTheme)
        assertTrue(!decoded.followsLauncher)
        assertNull(decodeNativeWidgetConfig("{\"textTheme\":\"NOT_A_THEME\"}").textTheme)
    }

    @Test
    fun `the solid theme backgrounds round trip and only widgets are meant to offer them`() {
        // When each solid background is encoded and decoded
        val solid = listOf(GlassStyle.THEME, GlassStyle.THEME_INVERTED)

        // Then it is kept, flagged solid, and left out of the glass-only list the other pickers use
        solid.forEach { style ->
            assertEquals(style, decodeNativeWidgetConfig(NativeWidgetConfig(glass = style).encode()).glass)
            assertTrue(style.isSolid)
            assertTrue(style !in GlassStyle.glassOnly)
        }
        assertTrue(GlassStyle.entries.filterNot { it.isSolid }.all { it in GlassStyle.glassOnly })
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
