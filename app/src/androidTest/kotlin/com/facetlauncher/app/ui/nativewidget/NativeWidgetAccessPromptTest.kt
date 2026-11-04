package com.facetlauncher.app.ui.nativewidget

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import com.facetlauncher.app.data.model.NativeWidgetType
import com.facetlauncher.app.ui.theme.FacetLauncherTheme
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

class NativeWidgetAccessPromptTest {

    @get:Rule
    val composeRule = createComposeRule()

    private fun show(type: NativeWidgetType, access: NativeWidgetAccess, actions: NativeWidgetActions) {
        composeRule.setContent {
            FacetLauncherTheme {
                NativeWidget(type = type, size = DpSize(300.dp, 160.dp), state = NativeWidgetState(access = access), actions = actions)
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
}
