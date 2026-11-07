package com.facetlauncher.app.domain

import com.facetlauncher.app.data.clockItemWith
import com.facetlauncher.app.data.model.ClockWidgetConfig
import com.facetlauncher.app.data.model.Facet
import com.facetlauncher.app.data.model.LauncherSettings
import com.facetlauncher.app.data.model.WeatherIconStyle
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ClockAccessorySwitchesTest {

    private val launcher = LauncherSettings()

    private fun facetWith(config: ClockWidgetConfig) = Facet(id = 1, name = "Work", position = 0, clockStack = listOf(clockItemWith(config)))

    @Test
    fun `with no facet the clock shows alarm and battery and not weather`() {
        val clock = resolveFacetStyle(null, launcher).clock

        assertTrue(clock.showAlarm)
        assertTrue(clock.showBattery)
        assertFalse(clock.showWeather)
    }

    @Test
    fun `a clock uses its own switches`() {
        val clock = resolveFacetStyle(facetWith(ClockWidgetConfig(showAlarm = false, showBattery = false, showWeather = true)), launcher).clock

        assertFalse(clock.showAlarm)
        assertFalse(clock.showBattery)
        assertTrue(clock.showWeather)
    }

    @Test
    fun `a clock uses its own weather icon style`() {
        val monochrome = facetWith(ClockWidgetConfig(weatherIconStyle = WeatherIconStyle.MONOCHROME))

        assertEquals(WeatherIconStyle.MONOCHROME, resolveFacetStyle(monochrome, launcher).clock.weatherIconStyle)
        assertEquals(WeatherIconStyle.COLORED, resolveFacetStyle(null, launcher).clock.weatherIconStyle)
    }

    @Test
    fun `weather icons are coloured by default for a new clock`() {
        assertEquals(WeatherIconStyle.COLORED, ClockWidgetConfig().weatherIconStyle)
    }
}
