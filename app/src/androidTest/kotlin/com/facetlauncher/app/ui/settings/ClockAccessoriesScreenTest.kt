package com.facetlauncher.app.ui.settings

import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import com.facetlauncher.app.data.model.Facet
import com.facetlauncher.app.data.model.LauncherSettings
import com.facetlauncher.app.data.model.TemperatureUnit
import com.facetlauncher.app.data.model.WeatherIconStyle
import com.facetlauncher.app.ui.theme.FacetLauncherTheme
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

class ClockAccessoriesScreenTest {

    @get:Rule
    val composeRule = createComposeRule()

    private fun setContent(
        uiState: ClockAccessoriesUiState,
        navigation: ClockAccessoriesNavigation = ClockAccessoriesNavigation(onBack = {}, onOpenClockStyle = {}),
        actions: ClockAccessoriesActions = ClockAccessoriesActions(),
    ) {
        composeRule.setContent {
            FacetLauncherTheme { ClockAccessoriesContent(uiState, navigation, actions) }
        }
        composeRule.waitForIdle()
    }

    @Test
    fun iconsAndUnitsAndLocationLinkAreHiddenWhileWeatherIsOffEverywhere() {
        // Given weather off for the default and every facet
        setContent(ClockAccessoriesUiState(weatherEnabledAnywhere = false))

        // Then the switches show but the Icons card and the link do not
        composeRule.onNodeWithTag("accessory_weather_toggle").assertIsEnabled()
        composeRule.onNodeWithTag("icon_style_row").assertDoesNotExist()
        composeRule.onNodeWithTag("accessory_units_and_location_row").assertDoesNotExist()
    }

    @Test
    fun iconsAndUnitsAndLocationLinkShowOnceWeatherIsOnSomewhere() {
        // Given weather on for the launcher default
        setContent(ClockAccessoriesUiState(settings = LauncherSettings(showWeather = true), weatherEnabledAnywhere = true))

        // Then the Icons card and the link are there, and units and location themselves are not on this screen
        composeRule.onNodeWithTag("icon_style_row").assertExists()
        composeRule.onNodeWithTag("accessory_units_and_location_row").assertExists()
        composeRule.onNodeWithTag("unit_temperature_row").assertDoesNotExist()
    }

    @Test
    fun theUnitsAndLocationRowOpensThatScreen() {
        // Given weather on
        var opened = false
        setContent(
            ClockAccessoriesUiState(settings = LauncherSettings(showWeather = true), weatherEnabledAnywhere = true),
            navigation = ClockAccessoriesNavigation(onBack = {}, onOpenClockStyle = {}, onOpenUnitsAndLocation = { opened = true }),
        )

        // When tapping it
        composeRule.onNodeWithTag("accessory_units_and_location_row").performScrollTo().performClick()

        // Then it navigates there
        assertEquals(true, opened)
    }

    @Test
    fun theIconsCardPreviewShowsColouredIconsByDefault() {
        // Given weather on with the default icon style
        setContent(ClockAccessoriesUiState(settings = LauncherSettings(showWeather = true), weatherEnabledAnywhere = true))

        // Then the preview row shows the coloured set
        composeRule.onNodeWithTag("weather_icons_preview_COLORED").assertExists()
        composeRule.onNodeWithTag("weather_icons_preview_MONOCHROME").assertDoesNotExist()
    }

    @Test
    fun theIconsCardPreviewShowsMonochromeIconsWhenMonochromeIsSelected() {
        // Given weather on with monochrome icons chosen
        setContent(
            ClockAccessoriesUiState(
                settings = LauncherSettings(showWeather = true, weatherIconStyle = WeatherIconStyle.MONOCHROME),
                weatherEnabledAnywhere = true,
            ),
        )

        // Then the preview row shows the monochrome set, not the coloured one
        composeRule.onNodeWithTag("weather_icons_preview_MONOCHROME").assertExists()
        composeRule.onNodeWithTag("weather_icons_preview_COLORED").assertDoesNotExist()
    }

    @Test
    fun aFacetThatOverridesTheClockEditsItsOwnIconStyleAndLinksToUnitsAndLocation() {
        // Given a facet that overrides the clock with weather on and its own monochrome icons
        var opened = false
        val facet = Facet(
            id = 1L, name = "Work", position = 1, overrideClock = true, clockShowWeather = true,
            clockWeatherIconStyle = WeatherIconStyle.MONOCHROME,
        )
        setContent(
            ClockAccessoriesUiState(facet = facet, settings = LauncherSettings(showWeather = true), weatherEnabledAnywhere = true),
            navigation = ClockAccessoriesNavigation(onBack = {}, onOpenClockStyle = {}, onOpenUnitsAndLocation = { opened = true }),
        )

        // Then its own icon style shows and can be edited
        composeRule.onNodeWithTag("icon_style_row").assertIsEnabled()
        composeRule.onNodeWithTag("weather_icons_preview_MONOCHROME").assertExists()

        // And the same Units and location row as the default screen opens that screen
        composeRule.onNodeWithTag("accessory_units_and_location_row").performScrollTo().performClick()
        assertEquals(true, opened)
    }

    @Test
    fun aFacetThatInheritsShowsTheLaunchersIconStyle() {
        // Given a facet that inherits the clock, with the launcher on monochrome icons
        val facet = Facet(id = 1L, name = "Work", position = 1, overrideClock = false, clockWeatherIconStyle = WeatherIconStyle.COLORED)
        setContent(
            ClockAccessoriesUiState(
                facet = facet,
                settings = LauncherSettings(showWeather = true, weatherIconStyle = WeatherIconStyle.MONOCHROME),
                weatherEnabledAnywhere = true,
            ),
        )

        // Then the launcher's style shows, not the facet's own
        composeRule.onNodeWithTag("weather_icons_preview_MONOCHROME").assertExists()
        composeRule.onNodeWithTag("weather_icons_preview_COLORED").assertDoesNotExist()
    }

    @Test
    fun thePreviewShowsTheWeatherSampleOnlyWhileWeatherIsOn() {
        // Given weather on, in Celsius
        setContent(ClockAccessoriesUiState(settings = LauncherSettings(showWeather = true, temperatureUnit = TemperatureUnit.CELSIUS), weatherEnabledAnywhere = true))

        // Then the preview clock carries the weather sample
        composeRule.onNodeWithTag("clock_accessories_preview").assertExists()
        composeRule.onNodeWithContentDescription("21°C, Partly cloudy").assertExists()
    }

    @Test
    fun thePreviewSampleUsesTheChosenTemperatureUnit() {
        // Given weather on, in Fahrenheit
        setContent(ClockAccessoriesUiState(settings = LauncherSettings(showWeather = true, temperatureUnit = TemperatureUnit.FAHRENHEIT), weatherEnabledAnywhere = true))

        // Then the sample reads in Fahrenheit, with its letter
        composeRule.onNodeWithContentDescription("70°F, Partly cloudy").assertExists()
    }

    @Test
    fun thePreviewDropsTheWeatherSampleWhenWeatherIsOff() {
        // Given weather off
        setContent(ClockAccessoriesUiState())

        // Then the preview clock has none
        composeRule.onNodeWithTag("clock_accessories_preview").assertExists()
        composeRule.onNodeWithContentDescription("21°C, Partly cloudy").assertDoesNotExist()
    }

    @Test
    fun weatherSwitchReportsTheNewValue() {
        // Given weather off
        var shown: Boolean? = null
        setContent(ClockAccessoriesUiState(), actions = ClockAccessoriesActions(onShowWeather = { shown = it }))

        // When flipping the switch
        composeRule.onNodeWithTag("accessory_weather_toggle").performClick()

        // Then it asks to turn weather on
        assertEquals(true, shown)
    }

    @Test
    fun aFacetThatInheritsCannotEditItsSwitches() {
        // Given a facet that does not override the clock
        setContent(ClockAccessoriesUiState(facet = Facet(id = 1L, name = "Work", position = 1), weatherEnabledAnywhere = true))

        // Then its switches are disabled
        composeRule.onNodeWithTag("accessory_weather_toggle").assertIsNotEnabled()
    }
}
