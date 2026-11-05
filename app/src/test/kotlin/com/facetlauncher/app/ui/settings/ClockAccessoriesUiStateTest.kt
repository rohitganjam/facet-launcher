package com.facetlauncher.app.ui.settings

import com.facetlauncher.app.data.model.ClockStackItem
import com.facetlauncher.app.data.model.ClockStackItemKind
import com.facetlauncher.app.data.model.ClockWidgetConfig
import com.facetlauncher.app.data.model.Facet
import com.facetlauncher.app.data.model.LauncherSettings
import com.facetlauncher.app.data.model.NativeWidgetConfig
import com.facetlauncher.app.data.model.WeatherCity
import com.facetlauncher.app.data.model.WeatherIconStyle
import com.facetlauncher.app.data.model.WeatherLocationMode
import com.facetlauncher.app.data.model.WidgetWeatherLocation
import com.facetlauncher.app.data.model.encode
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ClockAccessoriesUiStateTest {

    private val oslo = WidgetWeatherLocation.City(WeatherCity("Oslo", 59.91, 10.75, "Europe/Oslo"))

    private fun facetWith(config: ClockWidgetConfig) = Facet(
        id = 1, name = "Work", position = 0,
        clockStack = listOf(ClockStackItem(id = 3, facetId = 1, position = 0, kind = ClockStackItemKind.CLOCK, nativeWidgetConfig = NativeWidgetConfig(clock = config).encode())),
    )

    @Test
    fun `the icons and location cards are shown while the clock shows weather`() {
        val state = ClockAccessoriesUiState(facet = facetWith(ClockWidgetConfig(showWeather = true)))

        assertTrue(state.showIconStyleCard)
        assertTrue(state.showLocationCard)
    }

    @Test
    fun `the icons and location cards are hidden while the clock shows no weather`() {
        val state = ClockAccessoriesUiState(facet = facetWith(ClockWidgetConfig(showWeather = false)))

        assertFalse(state.showIconStyleCard)
        assertFalse(state.showLocationCard)
    }

    @Test
    fun `the switches and icon style are the clock's own`() {
        val state = ClockAccessoriesUiState(
            facet = facetWith(ClockWidgetConfig(showAlarm = false, showBattery = false, showWeather = true, weatherIconStyle = WeatherIconStyle.MONOCHROME)),
            settings = LauncherSettings(weatherIconStyle = WeatherIconStyle.COLORED),
        )

        assertFalse(state.showAlarm)
        assertFalse(state.showBattery)
        assertTrue(state.showWeather)
        assertEquals(WeatherIconStyle.MONOCHROME, state.weatherIconStyle)
    }

    @Test
    fun `a clock shows its own place, and none while it follows Weather settings`() {
        val own = ClockAccessoriesUiState(facet = facetWith(ClockWidgetConfig(weatherLocation = oslo)))
        val follows = ClockAccessoriesUiState(facet = facetWith(ClockWidgetConfig(weatherLocation = null)))

        assertEquals(oslo, own.weatherLocation)
        assertEquals(null, follows.weatherLocation)
    }

    @Test
    fun `the saved cities offered are the ones from Weather settings`() {
        val state = ClockAccessoriesUiState(settings = LauncherSettings(savedWeatherCities = listOf(oslo.city)))

        assertEquals(listOf(oslo.city), state.savedCities)
    }

    @Test
    fun `the weather needs the position when the clock or Weather settings point at it`() {
        // Given a clock on the current location, a clock on a city, and a clock following Weather settings in each mode
        val current = ClockAccessoriesUiState(facet = facetWith(ClockWidgetConfig(weatherLocation = WidgetWeatherLocation.Current)))
        val city = ClockAccessoriesUiState(facet = facetWith(ClockWidgetConfig(weatherLocation = oslo)), settings = LauncherSettings(weatherLocationMode = WeatherLocationMode.CURRENT))
        val followsCurrent = ClockAccessoriesUiState(facet = facetWith(ClockWidgetConfig()), settings = LauncherSettings(weatherLocationMode = WeatherLocationMode.CURRENT))
        val followsCity = ClockAccessoriesUiState(facet = facetWith(ClockWidgetConfig()), settings = LauncherSettings(weatherLocationMode = WeatherLocationMode.CITY))

        // Then only the ones that end up at the phone's position need the permission
        assertTrue(current.weatherNeedsPosition)
        assertFalse(city.weatherNeedsPosition)
        assertTrue(followsCurrent.weatherNeedsPosition)
        assertFalse(followsCity.weatherNeedsPosition)
    }
}
