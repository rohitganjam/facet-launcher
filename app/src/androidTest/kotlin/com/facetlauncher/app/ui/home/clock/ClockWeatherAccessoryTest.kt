package com.facetlauncher.app.ui.home.clock

import androidx.compose.material3.Text
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.test.assertHasClickAction
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.performClick
import androidx.compose.ui.text.font.FontFamily
import com.facetlauncher.app.ui.home.weather.WeatherIconKind
import com.facetlauncher.app.ui.theme.FacetLauncherTheme
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

class ClockWeatherAccessoryTest {

    @get:Rule
    val composeRule = createComposeRule()

    private val description = "27 degrees, Light rain. Opens the weather details"

    private fun setContent(weather: ClockWeatherAccessory?) {
        composeRule.setContent {
            FacetLauncherTheme {
                CompositionLocalProvider(LocalClockWeather provides weather) {
                    WeatherAccessoryContent(color = Color.Black, family = FontFamily.Default)
                    Text("sentinel")
                }
            }
        }
        composeRule.waitForIdle()
    }

    @Test
    fun accessoryIsOneSpokenButton() {
        // Given a weather accessory
        setContent(ClockWeatherAccessory(WeatherIconKind.RAIN, "27°", description) {})

        // Then the accessory is one spoken button, not a loose icon and number
        composeRule.onNodeWithContentDescription(description).assertHasClickAction()
    }

    @Test
    fun tappingTheAccessoryOpensTheSheet() {
        // Given a weather accessory
        var clicks = 0
        setContent(ClockWeatherAccessory(WeatherIconKind.RAIN, "27°", description) { clicks++ })

        // When tapping it
        composeRule.onNodeWithContentDescription(description).performClick()

        // Then its click is called once
        assertEquals(1, clicks)
    }

    @Test
    fun noAccessoryRendersWithoutWeather() {
        // Given no weather
        setContent(null)

        // Then nothing is drawn for it
        composeRule.onNodeWithContentDescription(description).assertDoesNotExist()
    }
}
