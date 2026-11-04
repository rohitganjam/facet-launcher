package com.facetlauncher.app.ui.settings

import com.facetlauncher.app.data.model.Facet
import com.facetlauncher.app.data.model.LauncherSettings
import com.facetlauncher.app.data.model.WeatherIconStyle
import com.facetlauncher.app.data.model.WidgetWeatherLocation
import com.facetlauncher.app.data.model.WeatherCity
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ClockAccessoriesUiStateTest {

    private val settings = LauncherSettings(showWeather = true, weatherIconStyle = WeatherIconStyle.COLORED)
    private fun facet(overrideClock: Boolean, showWeather: Boolean = true) = Facet(
        id = 1, name = "Work", position = 0, overrideClock = overrideClock,
        clockShowWeather = showWeather, clockWeatherIconStyle = WeatherIconStyle.MONOCHROME,
    )

    @Test
    fun `the default screen shows the icons card while weather is on somewhere`() {
        val state = ClockAccessoriesUiState(settings = settings, weatherEnabledAnywhere = true)

        assertTrue(state.showIconStyleCard)
    }

    @Test
    fun `the default screen hides the icons card while weather is off everywhere`() {
        val state = ClockAccessoriesUiState(weatherEnabledAnywhere = false)

        assertFalse(state.showIconStyleCard)
    }

    @Test
    fun `a facet shows its own icon style while it overrides the clock`() {
        val state = ClockAccessoriesUiState(facet = facet(overrideClock = true), settings = settings, weatherEnabledAnywhere = true)

        assertEquals(WeatherIconStyle.MONOCHROME, state.weatherIconStyle)
        assertTrue(state.showIconStyleCard)
    }

    @Test
    fun `a facet that inherits shows the launcher's icon style`() {
        val state = ClockAccessoriesUiState(facet = facet(overrideClock = false), settings = settings, weatherEnabledAnywhere = true)

        assertEquals(WeatherIconStyle.COLORED, state.weatherIconStyle)
        assertTrue(state.isInheriting)
    }

    @Test
    fun `a facet has no icons card when weather is off everywhere`() {
        val state = ClockAccessoriesUiState(
            facet = facet(overrideClock = true, showWeather = false),
            settings = settings.copy(showWeather = false),
            weatherEnabledAnywhere = false,
        )

        assertFalse(state.showIconStyleCard)
    }

    private val oslo = WidgetWeatherLocation.City(WeatherCity("Oslo", 59.91, 10.75, "Europe/Oslo"))

    @Test
    fun `the location card is only on a facet's screen, while that facet shows weather`() {
        // Given the default screen, a facet showing weather, and a facet with weather off
        val default = ClockAccessoriesUiState(settings = settings, weatherEnabledAnywhere = true)
        val showing = ClockAccessoriesUiState(facet = facet(overrideClock = true), settings = settings, weatherEnabledAnywhere = true)
        val off = ClockAccessoriesUiState(facet = facet(overrideClock = true, showWeather = false), settings = settings)

        // Then only the facet that shows weather gets it: the launcher-wide location lives in Weather settings
        assertFalse(default.showLocationCard)
        assertTrue(showing.showLocationCard)
        assertFalse(off.showLocationCard)
    }

    @Test
    fun `a facet shows its own place while it overrides the clock and none while it inherits`() {
        // Given a facet holding Oslo
        val own = facet(overrideClock = true).copy(clockWeatherLocation = oslo)
        val inheriting = facet(overrideClock = false).copy(clockWeatherLocation = oslo)

        // Then Oslo shows while overriding, and "same as Weather settings" while inheriting
        assertEquals(oslo, ClockAccessoriesUiState(facet = own, settings = settings).weatherLocation)
        assertEquals(null, ClockAccessoriesUiState(facet = inheriting, settings = settings).weatherLocation)
    }

    @Test
    fun `the saved cities offered are the ones from Weather settings`() {
        // Given cities saved in Weather settings
        val state = ClockAccessoriesUiState(settings = settings.copy(savedWeatherCities = listOf(oslo.city)))

        // Then they are the picker's cities
        assertEquals(listOf(oslo.city), state.savedCities)
    }
}
