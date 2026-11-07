package com.facetlauncher.app.ui.home

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasAnyDescendant
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import com.facetlauncher.app.data.model.NativeWidgetType
import com.facetlauncher.app.ui.theme.FacetLauncherTheme
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

class HomeSheetsTest {

    @get:Rule
    val composeRule = createComposeRule()

    private fun setHomeSheet(stackEmpty: Boolean = false, calls: MutableList<String> = mutableListOf(), stackSummary: String = "Clock") {
        composeRule.setContent {
            FacetLauncherTheme {
                HomeMenuSheetContent(
                    onFacetSettingsClick = { calls += "facet-settings" },
                    onLauncherSettingsClick = { calls += "launcher-settings" },
                    onAddWidgetClick = { calls += "add" },
                    onWidgetStackClick = { calls += "stack" },
                    stackSummary = stackSummary,
                    stackEmpty = stackEmpty,
                )
            }
        }
    }

    private fun setWidgetSheet(target: WidgetSheetTarget, calls: MutableList<String> = mutableListOf(), stackSummary: String = "Clock") {
        composeRule.setContent {
            FacetLauncherTheme {
                WidgetSheetContent(
                    target = target,
                    onClockStylesClick = { calls += "styles" },
                    onCalendarSettingsClick = { calls += "calendar" },
                    onWidgetSettingsClick = { calls += "widget-settings" },
                    onAdjustClick = { calls += "adjust" },
                    onReplaceWidgetClick = { calls += "replace" },
                    onRemoveWidgetClick = { calls += "remove" },
                    onWidgetStackClick = { calls += "stack" },
                    stackSummary = stackSummary,
                )
            }
        }
    }

    @Test
    fun theHomeSheetHoldsOnlyTheFacetAndLauncherSettingsWhileAWidgetIsShowing() {
        // Given the Home sheet over a stack with a widget
        val calls = mutableListOf<String>()
        setHomeSheet(calls = calls)

        // Then it has just the two settings rows, none of the widget's
        composeRule.onNodeWithTag("home_sheet_facet_settings").assertIsDisplayed()
        composeRule.onNodeWithTag("home_sheet_launcher_settings").assertIsDisplayed()
        listOf("widget_sheet_adjust", "widget_sheet_replace", "widget_sheet_remove", "widget_sheet_stack", "home_sheet_add_widget").forEach {
            composeRule.onNodeWithTag(it).assertDoesNotExist()
        }

        // And each row fires its own action
        composeRule.onNodeWithTag("home_sheet_facet_settings").performClick()
        composeRule.onNodeWithTag("home_sheet_launcher_settings").performClick()
        assertEquals(listOf("facet-settings", "launcher-settings"), calls)
    }

    @Test
    fun withAnEmptyStackTheHomeSheetAlsoOffersAddAndTheWidgetStack() {
        // Given the Home sheet over an empty stack, where there is no widget to long-press
        val calls = mutableListOf<String>()
        setHomeSheet(stackEmpty = true, calls = calls, stackSummary = "Empty")

        // Then Add widget and the Widget stack are there, and work
        composeRule.onNodeWithTag("home_sheet_add_widget").performClick()
        composeRule.onNodeWithTag("widget_sheet_stack").performClick()
        assertEquals(listOf("add", "stack"), calls)
        composeRule.onNodeWithTag("home_sheet_facet_settings").assertIsDisplayed()
    }

    @Test
    fun theClockSheetOffersItsStylesAndCalendarThenAdjustReplaceRemoveAndTheStack() {
        // Given the widget sheet over the clock
        val calls = mutableListOf<String>()
        setWidgetSheet(WidgetSheetTarget.Clock, calls)

        // When every row is tapped
        listOf("clock_style_sheet_styles", "clock_style_sheet_calendar", "widget_sheet_adjust", "widget_sheet_replace", "widget_sheet_remove", "widget_sheet_stack").forEach {
            composeRule.onNodeWithTag(it).performScrollTo().performClick()
        }

        // Then each reached its own callback, and the facet and launcher settings are not here
        assertEquals(listOf("styles", "calendar", "adjust", "replace", "remove", "stack"), calls)
        composeRule.onNodeWithTag("home_sheet_facet_settings").assertDoesNotExist()
        composeRule.onNodeWithTag("widget_sheet_settings").assertDoesNotExist()
    }

    @Test
    fun anAgendaAndAWeatherSheetHaveASettingsRowNamedForTheWidgetAndNowPlayingHasNone() {
        // Given the widget sheet over each native widget
        val calls = mutableListOf<String>()
        setWidgetSheet(WidgetSheetTarget.Native(NativeWidgetType.AGENDA), calls)

        // Then the Agenda has "Agenda settings", which fires the widget settings action, and no clock rows
        composeRule.onNode(hasTestTag("widget_sheet_settings") and hasAnyDescendant(hasText("Agenda settings")), useUnmergedTree = true).assertExists()
        composeRule.onNodeWithTag("widget_sheet_settings").performClick()
        assertEquals(listOf("widget-settings"), calls)
        composeRule.onNodeWithTag("clock_style_sheet_styles").assertDoesNotExist()
        composeRule.onNodeWithTag("widget_sheet_adjust").assertExists()
    }

    @Test
    fun theWeatherSheetNamesItsSettingsAfterWeather() {
        setWidgetSheet(WidgetSheetTarget.Native(NativeWidgetType.WEATHER))

        composeRule.onNode(hasTestTag("widget_sheet_settings") and hasAnyDescendant(hasText("Weather settings")), useUnmergedTree = true).assertExists()
    }

    @Test
    fun nowPlayingAndHostedWidgetsHaveNoSettingsRowButStillAdjustAndReplace() {
        setWidgetSheet(WidgetSheetTarget.Native(NativeWidgetType.NOW_PLAYING))
        composeRule.onNodeWithTag("widget_sheet_settings").assertDoesNotExist()
        composeRule.onNodeWithTag("widget_sheet_adjust").assertExists()
        composeRule.onNodeWithTag("widget_sheet_replace").assertExists()
    }

    @Test
    fun aHostedWidgetSheetHasNoSettingsRowEither() {
        setWidgetSheet(WidgetSheetTarget.Hosted)

        composeRule.onNodeWithTag("widget_sheet_settings").assertDoesNotExist()
        composeRule.onNodeWithTag("clock_style_sheet_styles").assertDoesNotExist()
        composeRule.onNodeWithTag("widget_sheet_remove").assertExists()
    }

    @Test
    fun theWidgetStackRowShowsWhatIsInTheStack() {
        setWidgetSheet(WidgetSheetTarget.Clock, stackSummary = "3 pages")

        composeRule.onNodeWithTag("widget_sheet_stack").assertExists()
        composeRule.onNode(hasTestTag("widget_sheet_stack") and hasAnyDescendant(hasText("3 pages")), useUnmergedTree = true).assertExists()
    }
}
