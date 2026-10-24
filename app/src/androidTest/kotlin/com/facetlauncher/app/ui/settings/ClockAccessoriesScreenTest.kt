package com.facetlauncher.app.ui.settings

import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import com.facetlauncher.app.data.model.Facet
import com.facetlauncher.app.data.model.LauncherSettings
import com.facetlauncher.app.ui.theme.FacetLauncherTheme
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

class ClockAccessoriesScreenTest {

    @get:Rule
    val composeRule = createComposeRule()

    private fun setContent(
        uiState: ClockAccessoriesUiState,
        navigation: ClockAccessoriesNavigation = ClockAccessoriesNavigation(onBack = {}, onOpenClockStyle = {}, onOpenLocation = {}),
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
        composeRule.onNodeWithTag("accessory_location_row").assertDoesNotExist()
    }

    @Test
    fun unitsAndLocationCardsShowOnceWeatherIsOnSomewhere() {
        // Given weather on for the launcher default
        setContent(ClockAccessoriesUiState(settings = LauncherSettings(showWeather = true), weatherEnabledAnywhere = true))

        // Then both global cards are there
        composeRule.onNodeWithTag("unit_temperature_row").assertExists()
        composeRule.onNodeWithTag("accessory_location_row").assertExists()
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
            navigation = ClockAccessoriesNavigation(onBack = {}, onOpenClockStyle = {}, onOpenLocation = { opened = true }),
        )

        // When tapping Location
        composeRule.onNodeWithTag("accessory_location_row").performClick()

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
}
