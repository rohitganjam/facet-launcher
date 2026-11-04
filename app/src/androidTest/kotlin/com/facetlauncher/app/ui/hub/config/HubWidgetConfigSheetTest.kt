package com.facetlauncher.app.ui.hub.config

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.assertIsOff
import androidx.compose.ui.test.assertIsOn
import androidx.compose.ui.test.assertIsSelected
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import com.facetlauncher.app.data.model.CalendarInfo
import com.facetlauncher.app.data.model.NativeWidgetConfig
import com.facetlauncher.app.data.model.NativeWidgetType
import com.facetlauncher.app.data.model.WeatherCity
import com.facetlauncher.app.data.model.WidgetWeatherLocation
import com.facetlauncher.app.ui.theme.FacetLauncherTheme
import kotlinx.collections.immutable.persistentListOf
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

class HubWidgetConfigSheetTest {

    @get:Rule
    val composeRule = createComposeRule()

    private val oslo = WeatherCity("Oslo", 59.91, 10.75, "Europe/Oslo")
    private val state = HubWidgetConfigUiState(
        calendars = persistentListOf(CalendarInfo("1", "Personal", "me@example.com"), CalendarInfo("2", "Work", "me@work.com")),
        savedCities = persistentListOf(oslo),
    )

    private fun show(type: NativeWidgetType, initial: NativeWidgetConfig, onChange: (NativeWidgetConfig) -> Unit) {
        composeRule.setContent {
            FacetLauncherTheme {
                var config by mutableStateOf(initial)
                HubWidgetConfigContent(type = type, config = config, state = state, onDismiss = {}, onConfigChange = { config = it; onChange(it) })
            }
        }
    }

    @Test
    fun agendaFollowsTheLauncherUntilTheSwitchIsTurnedOff() {
        // Given an agenda widget that follows the launcher
        var saved: NativeWidgetConfig? = null
        show(NativeWidgetType.AGENDA, NativeWidgetConfig(), onChange = { saved = it })

        // When turning the follow switch off
        composeRule.onNodeWithTag("hub_widget_config_follow").performClick()

        // Then it is set to its own calendars, starting with all of them
        composeRule.waitForIdle()
        assertEquals(setOf("1", "2"), saved?.calendarIds)
    }

    @Test
    fun agendaCalendarCanBeUnticked() {
        // Given an agenda widget with both calendars chosen
        var saved: NativeWidgetConfig? = null
        show(NativeWidgetType.AGENDA, NativeWidgetConfig(calendarIds = setOf("1", "2")), onChange = { saved = it })

        // When unticking the work calendar
        composeRule.onNodeWithTag("hub_widget_config_calendar_2").performClick()

        // Then only the personal one is kept
        composeRule.waitForIdle()
        assertEquals(setOf("1"), saved?.calendarIds)
    }

    @Test
    fun weatherCanBeSetToASavedCityAndBackToTheLauncher() {
        // Given a weather widget that follows the launcher
        var saved: NativeWidgetConfig? = null
        show(NativeWidgetType.WEATHER, NativeWidgetConfig(), onChange = { saved = it })

        // When choosing a saved city
        composeRule.onNodeWithTag("hub_widget_config_city_Oslo").performClick()
        composeRule.waitForIdle()

        // Then the widget is set to it
        assertEquals(WidgetWeatherLocation.City(oslo), saved?.weatherLocation)

        // And choosing the launcher's setting again clears it
        composeRule.onNodeWithTag("hub_widget_config_follow").performClick()
        composeRule.waitForIdle()
        assertEquals(null, saved?.weatherLocation)
    }

    @Test
    fun weatherCanBeSetToCurrentLocation() {
        // Given a weather widget
        var saved: NativeWidgetConfig? = null
        show(NativeWidgetType.WEATHER, NativeWidgetConfig(), onChange = { saved = it })

        // When choosing current location
        composeRule.onNodeWithTag("hub_widget_config_current").performClick()

        // Then the widget is set to it
        composeRule.waitForIdle()
        assertEquals(WidgetWeatherLocation.Current, saved?.weatherLocation)
    }
}
