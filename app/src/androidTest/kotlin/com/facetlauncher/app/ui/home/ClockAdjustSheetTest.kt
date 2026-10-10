package com.facetlauncher.app.ui.home

import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertTextContains
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.hasAnyDescendant
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import com.facetlauncher.app.ui.theme.FacetLauncherTheme
import org.junit.Rule
import org.junit.Test

class ClockAdjustSheetTest {

    @get:Rule
    val composeRule = createComposeRule()

    private fun setContent(
        overrideFacetName: String?,
        onEditStylesClick: () -> Unit = {},
        hasCustomClockWidget: Boolean = false,
        onUseCustomWidgetClick: () -> Unit = {},
        onSwitchToLauncherClockClick: () -> Unit = {},
        isPro: Boolean = true,
    ) {
        composeRule.setContent {
            FacetLauncherTheme {
                ClockAdjustSheet(
                    onAdjustClick = {},
                    onEditStylesClick = onEditStylesClick,
                    onFacetSettingsClick = {},
                    onLauncherSettingsClick = {},
                    onUseCustomWidgetClick = onUseCustomWidgetClick,
                    onSwitchToLauncherClockClick = onSwitchToLauncherClockClick,
                    overrideFacetName = overrideFacetName,
                    hasCustomClockWidget = hasCustomClockWidget,
                    isPro = isPro,
                )
            }
        }
    }

    @Test
    fun editStylesRowShowsTheGlobalBadgeWhenNoFacetOverridesItsOwn() {
        // Given no facet owns the clock's own style right now
        setContent(overrideFacetName = null)

        // Then the row's label stays generic and its badge reads "Global"
        composeRule.onNode(
            hasTestTag("clock_adjust_edit_styles") and hasAnyDescendant(hasText("Edit clock styles")),
            useUnmergedTree = true,
        ).assertExists()
        composeRule.onNode(
            hasTestTag("clock_adjust_edit_styles") and hasAnyDescendant(hasText("Global")),
            useUnmergedTree = true,
        ).assertExists()
    }

    @Test
    fun editStylesRowShowsTheFacetNameBadgeWhenTheActiveFacetOverridesItsOwn() {
        // Given the "Work" facet owns the clock's own style right now
        setContent(overrideFacetName = "Work")

        // Then the row's label is unchanged, and its badge names that facet instead of "Global"
        composeRule.onNode(
            hasTestTag("clock_adjust_edit_styles") and hasAnyDescendant(hasText("Edit clock styles")),
            useUnmergedTree = true,
        ).assertExists()
        composeRule.onNode(
            hasTestTag("clock_adjust_edit_styles") and hasAnyDescendant(hasText("Work")),
            useUnmergedTree = true,
        ).assertExists()
    }

    @Test
    fun tappingEditStylesRowFiresOnEditStylesClick() {
        var clicked = false
        setContent(overrideFacetName = null, onEditStylesClick = { clicked = true })

        composeRule.onNodeWithTag("clock_adjust_edit_styles").performClick()

        assert(clicked)
    }

    @Test
    fun nativeClockShowsUseCustomWidgetRowButNotSwitchBackRow() {
        // Given the active facet has no hosted clock widget
        setContent(overrideFacetName = null, hasCustomClockWidget = false)

        // Then "Use custom widget" is offered, "Switch to launcher clock widget" is not, and styles stay editable
        composeRule.onNodeWithTag("clock_adjust_use_custom_widget").assertExists()
        composeRule.onNodeWithTag("clock_adjust_switch_to_launcher_clock").assertDoesNotExist()
        composeRule.onNodeWithTag("clock_adjust_edit_styles").assertExists()
    }

    @Test
    fun customWidgetActiveHidesEditStylesAndShowsSwitchBackRow() {
        // Given the active facet already has a hosted clock widget
        setContent(overrideFacetName = null, hasCustomClockWidget = true)

        // Then "Edit ... styles" is hidden (nothing to style), and both widget-choice rows are offered
        composeRule.onNodeWithTag("clock_adjust_edit_styles").assertDoesNotExist()
        composeRule.onNodeWithTag("clock_adjust_use_custom_widget").assertExists()
        composeRule.onNodeWithTag("clock_adjust_switch_to_launcher_clock").assertExists()
    }

    @Test
    fun tappingUseCustomWidgetRowFiresOnUseCustomWidgetClick() {
        var clicked = false
        setContent(overrideFacetName = null, onUseCustomWidgetClick = { clicked = true })

        composeRule.onNodeWithTag("clock_adjust_use_custom_widget").performClick()

        assert(clicked)
    }

    @Test
    fun useCustomWidgetRowShowsAProPillOnlyOnTheFreePlan() {
        // Given a free user
        setContent(overrideFacetName = null, isPro = false)

        // Then the row is marked Pro, and still reports its click so the caller can open Facet Pro
        composeRule.onNodeWithTag("clock_adjust_use_custom_widget").assertTextContains("Pro")
    }

    @Test
    fun useCustomWidgetRowHasNoProPillForAProUser() {
        setContent(overrideFacetName = null, isPro = true)

        composeRule.onAllNodesWithText("Pro").assertCountEquals(0)
    }

    @Test
    fun aFreeUserCanStillSwitchBackToTheLauncherClock() {
        var clicked = false
        setContent(overrideFacetName = null, hasCustomClockWidget = true, isPro = false, onSwitchToLauncherClockClick = { clicked = true })

        composeRule.onNodeWithTag("clock_adjust_switch_to_launcher_clock").performClick()

        assert(clicked)
    }

    @Test
    fun tappingSwitchToLauncherClockRowFiresOnSwitchToLauncherClockClick() {
        var clicked = false
        setContent(overrideFacetName = null, hasCustomClockWidget = true, onSwitchToLauncherClockClick = { clicked = true })

        composeRule.onNodeWithTag("clock_adjust_switch_to_launcher_clock").performClick()

        assert(clicked)
    }
}
