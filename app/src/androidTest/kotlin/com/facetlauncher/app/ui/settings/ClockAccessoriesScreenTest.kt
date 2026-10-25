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
        navigation: ClockAccessoriesNavigation = ClockAccessoriesNavigation(onBack = {}, onOpenClockStyle = {}, onOpenLocation = {}, onOpenPermissions = {}),
        actions: ClockAccessoriesActions = ClockAccessoriesActions(),
    ) {
        composeRule.setContent {
            FacetLauncherTheme { ClockAccessoriesContent(uiState, navigation, actions) }
        }
        composeRule.waitForIdle()
    }

    @Test
    fun unitsAndLocationCardsAreHiddenWhileWeatherIsOffEverywhere() {
        // Given weather off for the default and every facet
        setContent(ClockAccessoriesUiState(weatherEnabledAnywhere = false))

        // Then the switches show but the Units and Location cards do not
        composeRule.onNodeWithTag("accessory_weather_toggle").assertIsEnabled()
        composeRule.onNodeWithTag("unit_temperature_row").assertDoesNotExist()
        composeRule.onNodeWithTag("icon_style_row").assertDoesNotExist()
        composeRule.onNodeWithTag("accessory_location_row").assertDoesNotExist()
    }

    @Test
    fun unitsAndLocationCardsShowOnceWeatherIsOnSomewhere() {
        // Given weather on for the launcher default
        setContent(ClockAccessoriesUiState(settings = LauncherSettings(showWeather = true), weatherEnabledAnywhere = true))

        // Then both global cards are there
        composeRule.onNodeWithTag("unit_temperature_row").assertExists()
        composeRule.onNodeWithTag("icon_style_row").assertExists()
        composeRule.onNodeWithTag("accessory_location_row").assertExists()
    }

    @Test
    fun theIconsCardPreviewFollowsTheSelectedIconStyle() {
        // Given weather on with the default monochrome icons
        setContent(ClockAccessoriesUiState(settings = LauncherSettings(showWeather = true), weatherEnabledAnywhere = true))
        composeRule.onNodeWithTag("weather_icons_preview_MONOCHROME").assertExists()
        composeRule.onNodeWithTag("weather_icons_preview_COLORED").assertDoesNotExist()
    }

    @Test
    fun theIconsCardPreviewShowsColouredIconsWhenColouredIsSelected() {
        // Given weather on with coloured icons chosen
        setContent(
            ClockAccessoriesUiState(
                settings = LauncherSettings(showWeather = true, weatherIconStyle = WeatherIconStyle.COLORED),
                weatherEnabledAnywhere = true,
            ),
        )

        // Then the preview row shows the coloured set, not the monochrome one
        composeRule.onNodeWithTag("weather_icons_preview_COLORED").assertExists()
        composeRule.onNodeWithTag("weather_icons_preview_MONOCHROME").assertDoesNotExist()
    }

    @Test
    fun thePreviewShowsTheWeatherSampleOnlyWhileWeatherIsOn() {
        // Given weather on
        setContent(ClockAccessoriesUiState(settings = LauncherSettings(showWeather = true), weatherEnabledAnywhere = true))

        // Then the preview clock carries the weather sample
        composeRule.onNodeWithTag("clock_accessories_preview").assertExists()
        composeRule.onNodeWithContentDescription("21°, Partly cloudy").assertExists()
    }

    @Test
    fun thePreviewDropsTheWeatherSampleWhenWeatherIsOff() {
        // Given weather off
        setContent(ClockAccessoriesUiState())

        // Then the preview clock has none
        composeRule.onNodeWithTag("clock_accessories_preview").assertExists()
        composeRule.onNodeWithContentDescription("21°, Partly cloudy").assertDoesNotExist()
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
    fun locationRowOpensTheLocationScreen() {
        // Given weather on
        var opened = false
        setContent(
            ClockAccessoriesUiState(settings = LauncherSettings(showWeather = true), weatherEnabledAnywhere = true),
            navigation = ClockAccessoriesNavigation(onBack = {}, onOpenClockStyle = {}, onOpenLocation = { opened = true }, onOpenPermissions = {}),
        )

        // When tapping Location
        composeRule.onNodeWithTag("accessory_location_row").performScrollTo().performClick()

        // Then it navigates there
        assertEquals(true, opened)
    }

    @Test
    fun aFacetThatInheritsCannotEditItsSwitches() {
        // Given a facet that does not override the clock
        setContent(ClockAccessoriesUiState(facet = Facet(id = 1L, name = "Work", position = 1), weatherEnabledAnywhere = true))

        // Then its switches are disabled and the global cards stay off this screen
        composeRule.onNodeWithTag("accessory_weather_toggle").assertIsNotEnabled()
        composeRule.onNodeWithTag("unit_temperature_row").assertDoesNotExist()
    }

    @Test
    fun theLocationPermissionRowOpensThePermissionsScreen() {
        // Given weather following the phone, without the permission
        var opened = false
        setContent(
            ClockAccessoriesUiState(settings = LauncherSettings(showWeather = true), weatherEnabledAnywhere = true, locationPermissionGranted = false),
            navigation = ClockAccessoriesNavigation(onBack = {}, onOpenClockStyle = {}, onOpenLocation = {}, onOpenPermissions = { opened = true }),
        )

        // When tapping the Location row
        composeRule.onNodeWithTag("permission_row_LOCATION").performScrollTo().performClick()

        // Then it sends the user to Permissions
        assertEquals(true, opened)
    }

    @Test
    fun aGrantedLocationRowShowsItIsOnAndIsNotTappable() {
        // Given the permission granted
        setContent(ClockAccessoriesUiState(settings = LauncherSettings(showWeather = true), weatherEnabledAnywhere = true, locationPermissionGranted = true))

        // Then it reads as on, with no Turn on
        composeRule.onNodeWithTag("permission_status_LOCATION").performScrollTo().assertExists()
        composeRule.onNodeWithTag("permission_turn_on_LOCATION").assertDoesNotExist()
    }
}
