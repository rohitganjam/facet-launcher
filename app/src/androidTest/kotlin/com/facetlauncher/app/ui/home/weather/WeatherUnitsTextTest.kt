package com.facetlauncher.app.ui.home.weather

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import com.facetlauncher.app.data.model.RainUnit
import com.facetlauncher.app.data.model.TemperatureUnit
import com.facetlauncher.app.data.model.WeatherHour
import com.facetlauncher.app.data.model.WindSpeedUnit
import com.facetlauncher.app.ui.theme.FacetLauncherTheme
import java.time.Instant
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

class WeatherUnitsTextTest {

    @get:Rule
    val composeRule = createComposeRule()

    private fun hour() = WeatherHour(
        time = Instant.parse("2026-08-27T09:00:00Z"), temperatureC = 21.0, humidityPercent = 50.0, pressureHpa = 1013.0,
        cloudPercent = 20.0, windSpeedMps = 3.0, windFromDegrees = 90.0, precipitationMm = 0.4, symbolCode = "rain",
    )

    @Test
    fun temperatureCarriesItsUnitLetter() {
        // Given a 21 degree reading, written in each unit
        var celsius = ""
        var fahrenheit = ""
        composeRule.setContent {
            celsius = temperatureText(21.0, TemperatureUnit.CELSIUS)
            fahrenheit = temperatureText(21.0, TemperatureUnit.FAHRENHEIT)
        }
        composeRule.waitForIdle()

        // Then each ends with its letter
        assertEquals("21°C", celsius)
        assertEquals("70°F", fahrenheit)
    }

    @Test
    fun theHourlyColumnShowsTemperatureRainAndWindWithTheirUnits() {
        // Given one hour of the forecast in Celsius, mm and km/h
        val uiState = WeatherUiState(units = ResolvedWeatherUnits(TemperatureUnit.CELSIUS, WindSpeedUnit.KMH, RainUnit.MM))
        composeRule.setContent {
            FacetLauncherTheme { HourColumn(hour(), isNow = false, timeFormat = DateTimeFormatter.ofPattern("HH").withZone(ZoneOffset.UTC), uiState = uiState) }
        }

        // Then every number carries its unit
        composeRule.onNodeWithText("21°C").assertExists()
        composeRule.onNodeWithText("0.4 mm").assertExists()
        composeRule.onNodeWithText("11 km/h").assertExists()
    }
}
