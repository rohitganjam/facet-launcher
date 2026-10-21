package com.facetlauncher.app.ui.home

import android.content.res.Configuration
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onFirst
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.unit.dp
import com.facetlauncher.app.data.model.AppInfo
import com.facetlauncher.app.data.model.AppListVerticalAlignment
import com.facetlauncher.app.data.model.AppRowPosition
import com.facetlauncher.app.data.model.AppRowPresentation
import com.facetlauncher.app.data.model.ClockAlignment
import com.facetlauncher.app.data.model.HomeWallpaper
import com.facetlauncher.app.data.model.LauncherSettings
import com.facetlauncher.app.data.model.PlacedItem
import com.facetlauncher.app.domain.resolveFacetStyle
import com.facetlauncher.app.ui.theme.FacetLauncherTheme
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class ScaledHomeScreenTest {

    @get:Rule
    val composeRule = createComposeRule()

    private val app = PlacedItem.SingleApp(AppInfo(packageName = "preview.test.one", activityName = ".Main", label = "Camera", icon = null))

    private fun settings(
        clockAlignment: ClockAlignment = ClockAlignment.LEFT,
        vertical: AppListVerticalAlignment = AppListVerticalAlignment.BOTTOM,
        clockZoneHeightDp: Float? = null,
    ) = LauncherSettings(
        clockAlignment = clockAlignment,
        appListVerticalAlignment = vertical,
        clockZoneHeightDp = clockZoneHeightDp,
        appRowPosition = AppRowPosition.LEFT,
        appRowPresentation = AppRowPresentation.TEXT_ONLY,
    )

    /** A preview at half the window's size, in the window's own configuration unless [configuration] replaces it. */
    private fun setContent(configuration: Configuration? = null, onClick: (() -> Unit)? = null, settings: () -> LauncherSettings) {
        composeRule.setContent {
            FacetLauncherTheme {
                val config = configuration ?: LocalConfiguration.current
                CompositionLocalProvider(LocalConfiguration provides config, LocalAllowLandscape provides (configuration != null)) {
                    val current = settings()
                    ScaledHomeScreen(
                        style = resolveFacetStyle(null, current),
                        settings = current,
                        content = HomeScreenContent(listItems = listOf(app), dockItems = emptyList(), calendarEvents = emptyList()),
                        homeWallpaper = HomeWallpaper.Unavailable,
                        tagPrefix = "test_preview_",
                        onClick = onClick,
                        modifier = Modifier.size((config.screenWidthDp / 2).dp, (config.screenHeightDp / 2).dp).testTag("preview_card"),
                    )
                }
            }
        }
        composeRule.waitForIdle()
    }

    private fun landscapeConfiguration() = Configuration().apply {
        screenWidthDp = 900
        screenHeightDp = 420
    }

    private val clock get() = composeRule.onNodeWithTag("test_preview_home_clock_block", useUnmergedTree = true).fetchSemanticsNode().boundsInRoot
    private val card get() = composeRule.onNodeWithTag("preview_card").fetchSemanticsNode().boundsInRoot
    private val listTop
        get() = composeRule
            .onAllNodes(hasTestTag("test_preview_home_app_label_preview.test.one"), useUnmergedTree = true)
            .onFirst().fetchSemanticsNode().boundsInRoot.top

    @Test
    fun `top alignment puts the app list in the upper part of the preview`() {
        setContent { settings(vertical = AppListVerticalAlignment.TOP) }

        assertTrue("list at $listTop should be above the middle of $card", listTop < card.top + card.height * 0.5f)
    }

    @Test
    fun `bottom alignment puts the app list in the lower part of the preview`() {
        setContent { settings(vertical = AppListVerticalAlignment.BOTTOM) }

        assertTrue("list at $listTop should be below the middle of $card", listTop > card.top + card.height * 0.5f)
    }

    @Test
    fun `right clock alignment moves the clock to the right edge`() {
        setContent { settings(clockAlignment = ClockAlignment.RIGHT) }

        assertTrue("clock ${clock.right} should reach the right side of $card", clock.right > card.left + card.width * 0.8f)
    }

    @Test
    fun `left clock alignment keeps the clock at the left edge`() {
        setContent { settings(clockAlignment = ClockAlignment.LEFT) }

        assertTrue("clock ${clock.left} should stay on the left of $card", clock.left < card.left + card.width * 0.2f)
    }

    @Test
    fun `a larger clock zone height pushes the clock down`() {
        var zoneHeight by mutableStateOf<Float?>(null)
        setContent { settings(clockZoneHeightDp = zoneHeight) }
        val defaultTop = clock.top

        zoneHeight = 400f
        composeRule.waitForIdle()

        assertTrue("clock top ${clock.top} should be below default $defaultTop", clock.top > defaultTop)
    }

    @Test
    fun `a wide window previews the landscape layout`() {
        setContent(configuration = landscapeConfiguration()) { settings() }

        composeRule.onNodeWithTag("test_preview_home_landscape_layout", useUnmergedTree = true).assertExists()
        composeRule.onNodeWithTag("test_preview_home_clock_pane", useUnmergedTree = true).assertExists()
        composeRule.onNodeWithTag("test_preview_home_apps_pane", useUnmergedTree = true).assertExists()
    }

    @Test
    fun `a tall window previews the portrait layout`() {
        setContent { settings() }

        composeRule.onAllNodes(hasTestTag("test_preview_home_landscape_layout"), useUnmergedTree = true).assertCountEquals(0)
    }

    @Test
    fun `tapping the preview reports the click`() {
        var clicks = 0
        setContent(onClick = { clicks++ }) { settings() }

        composeRule.onNodeWithTag("preview_card").performClick()

        assertEquals(1, clicks)
    }
}
