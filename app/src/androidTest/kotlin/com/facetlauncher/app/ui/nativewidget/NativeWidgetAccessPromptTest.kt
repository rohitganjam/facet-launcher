package com.facetlauncher.app.ui.nativewidget

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import com.facetlauncher.app.data.model.NativeWidgetType
import com.facetlauncher.app.ui.home.weather.WeatherUiState
import com.facetlauncher.app.ui.theme.FacetLauncherTheme
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

class NativeWidgetAccessPromptTest {

    @get:Rule
    val composeRule = createComposeRule()

    private fun show(type: NativeWidgetType, access: NativeWidgetAccess, actions: NativeWidgetActions, weather: WeatherUiState = WeatherUiState()) {
        composeRule.setContent {
            FacetLauncherTheme {
                NativeWidget(type = type, size = DpSize(300.dp, 160.dp), state = NativeWidgetState(weather = weather, access = access), actions = actions)
            }
        }
    }

    @Test
    fun agendaWithoutCalendarAccessOffersToAllowIt() {
        // Given an agenda widget with no calendar permission
        var asked = 0
        show(NativeWidgetType.AGENDA, NativeWidgetAccess(calendarMissing = true), NativeWidgetActions(onRequestCalendarAccess = { asked++ }))

        // When tapping Allow
        composeRule.onNodeWithTag("agenda_calendar_prompt_allow").performClick()

        // Then the calendar permission is requested
        assertEquals(1, asked)
    }

    @Test
    fun weatherWithoutLocationAccessOffersToAllowIt() {
        // Given a weather widget for the current position with no location permission
        var asked = 0
        show(NativeWidgetType.WEATHER, NativeWidgetAccess(locationMissing = true), NativeWidgetActions(onRequestLocationAccess = { asked++ }))

        // When tapping Allow
        composeRule.onNodeWithTag("weather_location_prompt_allow").performClick()

        // Then the location permission is requested
        assertEquals(1, asked)
    }

    @Test
    fun withAccessNoPromptShows() {
        // Given an agenda widget with calendar access
        show(NativeWidgetType.AGENDA, NativeWidgetAccess(), NativeWidgetActions())

        // Then there is no Allow button
        composeRule.onNodeWithTag("agenda_calendar_prompt").assertDoesNotExist()
    }

    @Test
    fun weatherRefreshingFromNothingShowsALoaderNotThePermissionText() {
        // Given a weather widget with no forecast yet and a refresh running, while the location permission still reads as off
        show(NativeWidgetType.WEATHER, NativeWidgetAccess(locationMissing = true), NativeWidgetActions(), WeatherUiState(isRefreshing = true))

        // Then a loader shows in place of the prompt
        composeRule.onNodeWithTag("weather_loading").assertIsDisplayed()
        composeRule.onNodeWithTag("weather_location_prompt_allow").assertDoesNotExist()
    }

    @Test
    fun weatherRefreshingFromNothingShowsALoaderNotTheEmptyText() {
        // Given a weather widget with no forecast yet, location allowed, and a refresh running
        show(NativeWidgetType.WEATHER, NativeWidgetAccess(), NativeWidgetActions(), WeatherUiState(isRefreshing = true))

        // Then a loader shows, not "No weather yet"
        composeRule.onNodeWithTag("weather_loading").assertIsDisplayed()
        composeRule.onNodeWithText("No weather yet").assertDoesNotExist()
    }

    @Test
    fun weatherNotRefreshingStillShowsThePermissionPrompt() {
        // Given a weather widget with no forecast, no permission and no refresh running
        show(NativeWidgetType.WEATHER, NativeWidgetAccess(locationMissing = true), NativeWidgetActions(), WeatherUiState(isRefreshing = false))

        // Then the prompt shows, with no loader
        composeRule.onNodeWithTag("weather_location_prompt_allow").assertIsDisplayed()
        composeRule.onNodeWithTag("weather_loading").assertDoesNotExist()
    }
}
