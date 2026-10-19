package com.facetlauncher.app.ui.home

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.longClick
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTouchInput
import com.facetlauncher.app.data.model.AppInfo
import com.facetlauncher.app.data.model.AppListLayout
import com.facetlauncher.app.data.model.ClockAlignment
import com.facetlauncher.app.data.model.DockDisplayMode
import com.facetlauncher.app.data.model.PlacedItem
import com.facetlauncher.app.ui.theme.FacetLauncherTheme
import org.junit.Rule
import org.junit.Test

/** Landscape Home (panes, divider room, dock column), forced regardless of the device's own orientation. */
class HomeScreenLandscapeTest {

    @get:Rule
    val composeRule = createComposeRule()

    private fun apps(count: Int, prefix: String = "App") = (1..count).map {
        AppInfo(packageName = "com.example.${prefix.lowercase()}$it", activityName = ".Main", label = "$prefix $it", icon = null)
    }

    @Composable
    private fun LandscapeHome(
        appListItems: List<AppInfo> = apps(3),
        dockApps: List<AppInfo> = apps(3, prefix = "Dock"),
        clockAlignment: ClockAlignment = ClockAlignment.LEFT,
        appListLayout: AppListLayout = AppListLayout.SINGLE_COLUMN,
        dockDisplayMode: DockDisplayMode = DockDisplayMode.ICONS,
    ) {
        var adjustMode by remember { mutableStateOf(ClockAdjustMode.NONE) }
        FacetLauncherTheme {
            HomeScreen(
                appListItems = appListItems.map { PlacedItem.SingleApp(it) },
                dockApps = dockApps.map { PlacedItem.SingleApp(it) },
                onAppClick = {},
                clockAlignment = clockAlignment,
                appListLayout = appListLayout,
                dockDisplayMode = dockDisplayMode,
                clockAdjustMode = adjustMode,
                onAdjustModeChange = { adjustMode = it },
                layoutMode = HomeLayoutMode.LANDSCAPE,
            )
        }
    }

    private fun boundsOf(tag: String) = composeRule.onNodeWithTag(tag).fetchSemanticsNode().boundsInRoot

    @Test
    fun showsClockAppsAndDockPanesAndNoPortraitDockRow() {
        // Given the landscape Home
        composeRule.setContent { LandscapeHome() }

        // Then each pane is present and the portrait dock row is not
        composeRule.onNodeWithTag("home_landscape_layout").assertIsDisplayed()
        composeRule.onNodeWithTag("home_clock_pane").assertIsDisplayed()
        composeRule.onNodeWithTag("home_apps_pane").assertIsDisplayed()
        composeRule.onNodeWithTag("home_dock_column").assertIsDisplayed()
        composeRule.onNodeWithTag("home_dock_row").assertDoesNotExist()
    }

    @Test
    fun panesSitLeftToRightWithTheDockAtTheRightEdge() {
        composeRule.setContent { LandscapeHome() }
        composeRule.waitForIdle()

        val clock = boundsOf("home_clock_pane")
        val apps = boundsOf("home_apps_pane")
        val dock = boundsOf("home_dock_column")

        // Then clock, apps and dock follow each other, with room between the clock and apps panes
        assert(clock.right < apps.left) { "clock $clock should end before apps $apps begin" }
        assert(apps.right <= dock.left) { "apps $apps should end before the dock $dock begins" }
        assert(dock.right > apps.right) { "dock $dock should be right of the apps pane $apps" }
    }

    @Test
    fun appListRendersInsideTheAppsPane() {
        composeRule.setContent { LandscapeHome(appListItems = apps(2)) }
        composeRule.waitForIdle()

        val pane = boundsOf("home_apps_pane")
        val row = boundsOf("home_app_label_com.example.app1")

        assert(row.left >= pane.left && row.right <= pane.right) { "row $row should be inside the apps pane $pane" }
    }

    @Test
    fun dockShowsIconsOnlyEvenWhenTheDockIsSetToText() {
        // Given a dock set to text-only
        composeRule.setContent { LandscapeHome(dockDisplayMode = DockDisplayMode.TEXT) }
        composeRule.waitForIdle()

        // Then landscape still shows icons, not labels
        composeRule.onNodeWithContentDescription("Dock 1").assertExists()
        composeRule.onNodeWithText("Dock 1").assertDoesNotExist()
    }

    @Test
    fun noDockColumnWhenTheDockIsEmpty() {
        composeRule.setContent { LandscapeHome(dockApps = emptyList()) }

        composeRule.onNodeWithTag("home_dock_column").assertDoesNotExist()
        composeRule.onNodeWithTag("home_apps_pane").assertIsDisplayed()
    }

    @Test
    fun clockAlignmentMapsToTopMiddleAndBottomOfItsPane() {
        // Given the clock alignment driven by state, so one composition can step through all three
        var alignment by mutableStateOf(ClockAlignment.LEFT)
        composeRule.setContent { LandscapeHome(clockAlignment = alignment) }
        composeRule.waitForIdle()
        val top = boundsOf("home_clock_block").center.y

        alignment = ClockAlignment.CENTER
        composeRule.waitForIdle()
        val middle = boundsOf("home_clock_block").center.y

        alignment = ClockAlignment.RIGHT
        composeRule.waitForIdle()
        val bottom = boundsOf("home_clock_block").center.y

        // Then left, center and right become top, middle and bottom
        assert(top < middle && middle < bottom) { "expected top < middle < bottom, got $top, $middle, $bottom" }
    }

    @Test
    fun twoColumnListIsShownAsASingleColumn() {
        composeRule.setContent { LandscapeHome(appListItems = apps(4), appListLayout = AppListLayout.TWO_COLUMN) }
        composeRule.waitForIdle()

        val first = boundsOf("home_app_label_com.example.app1")
        val second = boundsOf("home_app_label_com.example.app2")

        // Then the second app is below the first, not beside it
        assert(second.top > first.top) { "second $second should be below first $first" }
        assert(kotlin.math.abs(second.left - first.left) < 1f) { "second $second should share the first's left edge $first" }
    }

    @Test
    fun clockMenuHasNoAdjustRowInLandscape() {
        composeRule.setContent { LandscapeHome() }

        // When the clock is long-pressed
        composeRule.onNodeWithTag("home_clock_block").performTouchInput { longClick() }
        composeRule.waitForIdle()

        // Then the sheet opens without "Adjust size and position", which has no landscape version yet
        composeRule.onNodeWithTag("clock_adjust_sheet").assertExists()
        composeRule.onNodeWithTag("clock_adjust_open").assertDoesNotExist()
        composeRule.onNodeWithTag("clock_adjust_launcher_settings").performClick()
    }
}
