package com.facetlauncher.app.ui.nativewidget.config

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

class NativeWidgetConfigSheetTest {

    @get:Rule
    val composeRule = createComposeRule()

    private val oslo = WeatherCity("Oslo", 59.91, 10.75, "Europe/Oslo")
    private val state = NativeWidgetConfigUiState(
        calendars = persistentListOf(CalendarInfo("1", "Personal", "me@example.com"), CalendarInfo("2", "Work", "me@work.com"), CalendarInfo("work:1", "", "", isWork = true)),
        launcherShowAllDay = true,
        savedCities = persistentListOf(oslo),
    )

    private fun show(type: NativeWidgetType, initial: NativeWidgetConfig, onChange: (NativeWidgetConfig) -> Unit) {
        composeRule.setContent {
            FacetLauncherTheme {
                var config by mutableStateOf(initial)
                NativeWidgetConfigContent(type = type, config = config, state = state, onDismiss = {}, onConfigChange = { config = it; onChange(it) })
            }
        }
    }

    @Test
    fun agendaStartsFromTheLaunchersCalendarSettingsAndAllDayCanBeTurnedOff() {
        // Given an agenda that follows the launcher, which shows all-day events
        var saved: NativeWidgetConfig? = null
        show(NativeWidgetType.AGENDA, NativeWidgetConfig(), onChange = { saved = it })

        // When turning all-day events off
        composeRule.onNodeWithTag("hub_widget_config_all_day").performClick()

        // Then the widget has its own all-day choice, and the calendars it was already showing
        composeRule.waitForIdle()
        assertEquals(false, saved?.showAllDayEvents)
        assertEquals(setOf("1", "2"), saved?.calendarIds)
    }

    @Test
    fun aWorkProfileCalendarIsOptedIntoAlongsideThePersonalOnes() {
        // Given an agenda that follows the launcher, which shows the personal calendars
        var saved: NativeWidgetConfig? = null
        show(NativeWidgetType.AGENDA, NativeWidgetConfig(), onChange = { saved = it })

        // When ticking the work profile's calendar, which is listed as a Work calendar
        composeRule.onNodeWithTag("hub_widget_config_calendar_work:1").performClick()

        // Then it joins the personal calendars the agenda already showed
        composeRule.waitForIdle()
        assertEquals(setOf("1", "2", "work:1"), saved?.calendarIds)
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

    @Test
    fun everyCalendarRowShowsItsColourDot() {
        // Given an agenda settings sheet listing a personal and a work calendar
        show(NativeWidgetType.AGENDA, NativeWidgetConfig(), onChange = {})

        // Then each row carries the dot that matches its event bars in the widget
        composeRule.onNodeWithTag("hub_widget_config_calendar_color_1", useUnmergedTree = true).assertExists()
        composeRule.onNodeWithTag("hub_widget_config_calendar_color_work:1", useUnmergedTree = true).assertExists()
    }
}
