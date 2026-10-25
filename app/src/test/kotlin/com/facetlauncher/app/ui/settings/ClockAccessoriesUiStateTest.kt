package com.facetlauncher.app.ui.settings

import com.facetlauncher.app.data.model.Facet
import com.facetlauncher.app.data.model.LauncherSettings
import com.facetlauncher.app.data.model.WeatherIconStyle
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
    fun `the default screen shows the icons card and the global cards while weather is on somewhere`() {
        val state = ClockAccessoriesUiState(settings = settings, weatherEnabledAnywhere = true)

        assertTrue(state.showIconStyleCard)
        assertTrue(state.showGlobalCards)
        assertFalse(state.showDefaultsLink)
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
    fun `a facet links to the defaults instead of showing units and location`() {
        val state = ClockAccessoriesUiState(facet = facet(overrideClock = true), settings = settings, weatherEnabledAnywhere = true)

        assertTrue(state.showDefaultsLink)
        assertFalse(state.showGlobalCards)
    }

    @Test
    fun `a facet that does not show weather has no icons card and no link when weather is off everywhere`() {
        val state = ClockAccessoriesUiState(
            facet = facet(overrideClock = true, showWeather = false),
            settings = settings.copy(showWeather = false),
            weatherEnabledAnywhere = false,
        )

        assertFalse(state.showIconStyleCard)
        assertFalse(state.showDefaultsLink)
    }
}
