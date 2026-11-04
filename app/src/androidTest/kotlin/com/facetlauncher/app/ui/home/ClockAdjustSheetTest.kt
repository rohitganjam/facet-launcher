package com.facetlauncher.app.ui.home

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasAnyDescendant
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import com.facetlauncher.app.ui.theme.FacetLauncherTheme
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

class ClockAdjustSheetTest {

    @get:Rule
    val composeRule = createComposeRule()

    private fun setSheet(
        hasPage: Boolean = true,
        calls: MutableList<String> = mutableListOf(),
        stackSummary: String = "Clock",
    ) {
        composeRule.setContent {
            FacetLauncherTheme {
                ClockAdjustSheet(
                    onAdjustClick = { calls += "adjust" },
                    onFacetSettingsClick = { calls += "facet-settings" },
                    onLauncherSettingsClick = { calls += "launcher-settings" },
                    onReplaceWidgetClick = { calls += "replace" },
                    onRemoveWidgetClick = { calls += "remove" },
                    onAddWidgetClick = { calls += "add" },
                    onWidgetStackClick = { calls += "stack" },
                    stackSummary = stackSummary,
                    hasPage = hasPage,
                )
            }
        }
    }

    @Test
    fun withAPageTheSheetOffersAdjustReplaceRemoveAndTheStack() {
        // Given a stack with a page showing
        setSheet(hasPage = true)

        // Then the same rows are there whatever the page is, and nothing to add
        composeRule.onNodeWithTag("clock_adjust_open").assertIsDisplayed()
        composeRule.onNodeWithTag("clock_adjust_replace_widget").assertIsDisplayed()
        composeRule.onNodeWithTag("clock_adjust_remove_widget").assertIsDisplayed()
        composeRule.onNodeWithTag("clock_adjust_widget_stack").assertIsDisplayed()
        composeRule.onNodeWithTag("clock_adjust_add_widget").assertDoesNotExist()
    }

    @Test
    fun withNoPageTheSheetOffersAddInsteadOfReplaceAndRemove() {
        // Given an empty stack
        setSheet(hasPage = false, stackSummary = "Empty")

        // Then adding is offered, and there is nothing to replace or remove
        composeRule.onNodeWithTag("clock_adjust_add_widget").assertIsDisplayed()
        composeRule.onNodeWithTag("clock_adjust_replace_widget").assertDoesNotExist()
        composeRule.onNodeWithTag("clock_adjust_remove_widget").assertDoesNotExist()
        composeRule.onNodeWithTag("clock_adjust_open").assertIsDisplayed()
    }

    @Test
    fun thereIsNoClockSettingsOrReplaceWithClockRowAnyMore() {
        // Given the sheet over a page
        setSheet()

        // Then the clock's own rows live on the clock's sheet, not here
        composeRule.onNodeWithTag("clock_adjust_clock_settings").assertDoesNotExist()
        composeRule.onNodeWithTag("clock_adjust_replace_with_clock").assertDoesNotExist()
        composeRule.onNodeWithTag("clock_adjust_replace_with_widget").assertDoesNotExist()
    }

    @Test
    fun everyRowFiresItsOwnAction() {
        // Given a sheet over a page, with a log of calls
        val calls = mutableListOf<String>()
        setSheet(calls = calls)

        // When each row is tapped
        listOf("open", "replace_widget", "remove_widget", "widget_stack", "facet_settings", "launcher_settings").forEach { name ->
            composeRule.onNodeWithTag("clock_adjust_$name").performScrollTo().performClick()
        }

        // Then each reached its own callback
        assertEquals(listOf("adjust", "replace", "remove", "stack", "facet-settings", "launcher-settings"), calls)
    }

    @Test
    fun theAddRowFiresAddWhenTheStackIsEmpty() {
        val calls = mutableListOf<String>()
        setSheet(hasPage = false, calls = calls)

        composeRule.onNodeWithTag("clock_adjust_add_widget").performClick()

        assertEquals(listOf("add"), calls)
    }

    @Test
    fun theWidgetStackRowShowsWhatIsInTheStack() {
        setSheet(stackSummary = "3 pages")

        composeRule.onNodeWithTag("clock_adjust_widget_stack").assertIsDisplayed()
        composeRule.onNode(hasTestTag("clock_adjust_widget_stack") and hasAnyDescendant(hasText("3 pages")), useUnmergedTree = true).assertExists()
    }

    @Test
    fun theClockSheetOffersStylesAndCalendarSettings() {
        // Given the clock's own sheet
        val calls = mutableListOf<String>()
        composeRule.setContent {
            FacetLauncherTheme { ClockStyleSheetContent(onClockStylesClick = { calls += "styles" }, onCalendarSettingsClick = { calls += "calendar" }) }
        }

        // When both rows are tapped
        composeRule.onNodeWithTag("clock_style_sheet_styles").performClick()
        composeRule.onNodeWithTag("clock_style_sheet_calendar").performClick()

        // Then each fires its own action, and that is all the sheet holds
        assertEquals(listOf("styles", "calendar"), calls)
        composeRule.onNodeWithTag("clock_adjust_open").assertDoesNotExist()
    }
}
