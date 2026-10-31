package com.facetlauncher.app.ui.home

import androidx.compose.foundation.layout.height
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.hasAnyDescendant
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.unit.dp
import com.facetlauncher.app.ui.theme.FacetLauncherTheme
import org.junit.Rule
import org.junit.Test

class ClockAdjustSheetTest {

    @get:Rule
    val composeRule = createComposeRule()

    private fun setContent(
        overrideFacetName: String?,
        onClockSettingsClick: () -> Unit = {},
        hasCustomClockWidget: Boolean = false,
        canReplaceWithClock: Boolean = true,
        onReplaceWithWidgetClick: () -> Unit = {},
        onReplaceWithClockClick: () -> Unit = {},
    ) {
        composeRule.setContent {
            FacetLauncherTheme {
                ClockAdjustSheet(
                    onAdjustClick = {},
                    onClockSettingsClick = onClockSettingsClick,
                    onFacetSettingsClick = {},
                    onLauncherSettingsClick = {},
                    onReplaceWithWidgetClick = onReplaceWithWidgetClick,
                    onReplaceWithClockClick = onReplaceWithClockClick,
                    overrideFacetName = overrideFacetName,
                    hasCustomClockWidget = hasCustomClockWidget,
                    canReplaceWithClock = canReplaceWithClock,
                )
            }
        }
    }

    @Test
    fun clockSettingsRowShowsTheGlobalBadgeWhenNoFacetOverridesItsOwn() {
        // Given no facet owns the clock's own style right now
        setContent(overrideFacetName = null)

        // Then the row's label stays generic and its badge reads "Global"
        composeRule.onNode(
            hasTestTag("clock_adjust_clock_settings") and hasAnyDescendant(hasText("Clock & calendar settings")),
            useUnmergedTree = true,
        ).assertExists()
        composeRule.onNode(
            hasTestTag("clock_adjust_clock_settings") and hasAnyDescendant(hasText("Global")),
            useUnmergedTree = true,
        ).assertExists()
    }

    @Test
    fun clockSettingsRowShowsTheFacetNameBadgeWhenTheActiveFacetOverridesItsOwn() {
        // Given the "Work" facet owns the clock's own style right now
        setContent(overrideFacetName = "Work")

        // Then the row's label is unchanged, and its badge names that facet instead of "Global"
        composeRule.onNode(
            hasTestTag("clock_adjust_clock_settings") and hasAnyDescendant(hasText("Clock & calendar settings")),
            useUnmergedTree = true,
        ).assertExists()
        composeRule.onNode(
            hasTestTag("clock_adjust_clock_settings") and hasAnyDescendant(hasText("Work")),
            useUnmergedTree = true,
        ).assertExists()
    }

    @Test
    fun tappingClockSettingsRowFiresOnClockSettingsClick() {
        var clicked = false
        setContent(overrideFacetName = null, onClockSettingsClick = { clicked = true })

        composeRule.onNodeWithTag("clock_adjust_clock_settings").performClick()

        assert(clicked)
    }

    @Test
    fun theClockPageOffersItsSettingsAndReplacingItWithAWidgetOnly() {
        // Given the page showing is the clock
        setContent(overrideFacetName = null, hasCustomClockWidget = false)

        // Then its settings and "Replace with custom widget" are offered, and nothing that only a widget page has
        composeRule.onNodeWithTag("clock_adjust_clock_settings").assertExists()
        composeRule.onNodeWithTag("clock_adjust_replace_with_widget").assertExists()
        composeRule.onNodeWithTag("clock_adjust_replace_with_clock").assertDoesNotExist()
        composeRule.onNodeWithTag("clock_adjust_replace_widget").assertDoesNotExist()
    }

    @Test
    fun aWidgetPageHidesClockSettingsAndOffersBothReplaceRows() {
        // Given the page showing is a hosted widget and the clock is not on another page
        setContent(overrideFacetName = null, hasCustomClockWidget = true, canReplaceWithClock = true)

        // Then there are no clock settings (nothing to style), and the page can become the clock or another widget
        composeRule.onNodeWithTag("clock_adjust_clock_settings").assertDoesNotExist()
        composeRule.onNodeWithTag("clock_adjust_replace_with_clock").assertExists()
        composeRule.onNodeWithTag("clock_adjust_replace_widget").assertExists()
        composeRule.onNodeWithTag("clock_adjust_replace_with_widget").assertDoesNotExist()
    }

    @Test
    fun aWidgetPageCannotBeReplacedWithTheClockWhileTheClockIsOnAnotherPage() {
        // Given a widget page in a stack that already has the clock
        setContent(overrideFacetName = null, hasCustomClockWidget = true, canReplaceWithClock = false)

        // Then only replacing the widget with another widget is offered
        composeRule.onNodeWithTag("clock_adjust_replace_with_clock").assertDoesNotExist()
        composeRule.onNodeWithTag("clock_adjust_replace_widget").assertExists()
    }

    @Test
    fun tappingReplaceWithCustomWidgetFiresItsCallback() {
        var clicked = false
        setContent(overrideFacetName = null, onReplaceWithWidgetClick = { clicked = true })

        composeRule.onNodeWithTag("clock_adjust_replace_with_widget").performClick()

        assert(clicked)
    }

    @Test
    fun tappingReplaceCustomWidgetOnAWidgetPageFiresTheSameCallback() {
        var clicked = false
        setContent(overrideFacetName = null, hasCustomClockWidget = true, onReplaceWithWidgetClick = { clicked = true })

        composeRule.onNodeWithTag("clock_adjust_replace_widget").performClick()

        assert(clicked)
    }

    @Test
    fun tappingReplaceWithClockFiresItsCallback() {
        var clicked = false
        setContent(overrideFacetName = null, hasCustomClockWidget = true, onReplaceWithClockClick = { clicked = true })

        composeRule.onNodeWithTag("clock_adjust_replace_with_clock").performClick()

        assert(clicked)
    }

    @Test
    fun noRowIsMarkedPro() {
        // Given the sheet on either kind of page
        setContent(overrideFacetName = null)

        // Then replacing is free: no row carries a Pro pill
        composeRule.onAllNodesWithText("Pro").assertCountEquals(0)
    }

    @Test
    fun theLastRowIsReachableInAWindowShorterThanTheSheet() {
        // Given the sheet in a window far shorter than its content, as a landscape phone gives it
        var clicked = false
        composeRule.setContent {
            FacetLauncherTheme {
                androidx.compose.foundation.layout.Box(androidx.compose.ui.Modifier.height(220.dp)) {
                    ClockAdjustSheet(
                        onAdjustClick = {},
                        onClockSettingsClick = {},
                        onFacetSettingsClick = {},
                        onLauncherSettingsClick = { clicked = true },
                        onReplaceWithWidgetClick = {},
                        onReplaceWithClockClick = {},
                        overrideFacetName = null,
                    )
                }
            }
        }

        // When scrolling to the last row and tapping it
        composeRule.onNodeWithTag("clock_adjust_launcher_settings").performScrollTo().assertIsDisplayed().performClick()

        // Then it was reachable and fired
        assert(clicked)
    }
}
