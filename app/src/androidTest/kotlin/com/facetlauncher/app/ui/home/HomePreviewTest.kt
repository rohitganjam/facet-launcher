package com.facetlauncher.app.ui.home

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onFirst
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.unit.dp
import com.facetlauncher.app.data.model.AppInfo
import com.facetlauncher.app.data.model.AppListColumnAlignment
import com.facetlauncher.app.data.model.AppListGridColumns
import com.facetlauncher.app.data.model.AppListGridDisplayMode
import com.facetlauncher.app.data.model.AppListLayout
import com.facetlauncher.app.data.model.AppListVerticalAlignment
import com.facetlauncher.app.data.model.AppRowPosition
import com.facetlauncher.app.data.model.AppRowPresentation
import com.facetlauncher.app.data.model.CalendarDisplayOptions
import com.facetlauncher.app.data.model.ClockAlignment
import com.facetlauncher.app.data.model.ClockColorOption
import com.facetlauncher.app.data.model.ClockDateStyle
import com.facetlauncher.app.data.model.ClockFontOption
import com.facetlauncher.app.data.model.ClockTemplateId
import com.facetlauncher.app.data.model.DockDisplayMode
import com.facetlauncher.app.data.model.FontWeightOption
import com.facetlauncher.app.data.model.HomeWallpaper
import com.facetlauncher.app.data.model.LauncherFontOption
import com.facetlauncher.app.data.model.PlacedItem
import com.facetlauncher.app.ui.theme.FacetLauncherTheme
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class HomePreviewTest {

    @get:Rule
    val composeRule = createComposeRule()

    private val app = PlacedItem.SingleApp(AppInfo(packageName = "preview.test.one", activityName = ".Main", label = "Camera", icon = null))

    private fun state(
        clockAlignment: ClockAlignment = ClockAlignment.LEFT,
        vertical: AppListVerticalAlignment = AppListVerticalAlignment.BOTTOM,
        rowPosition: AppRowPosition = AppRowPosition.LEFT,
        clockZoneHeightDp: Float? = null,
    ) = HomePreviewState(
        favorites = listOf(app),
        dockItems = emptyList(),
        use24HourTime = true,
        clockTemplateId = ClockTemplateId.entries.first(),
        clockFontOption = ClockFontOption.entries.first(),
        clockColorOption = ClockColorOption.entries.first(),
        clockAccentColorOption = ClockColorOption.entries.first(),
        clockShowMeridiem = false,
        clockDateStyle = ClockDateStyle.entries.first(),
        clockAlignment = clockAlignment,
        clockScale = 0.8f,
        clockZoneHeightDp = clockZoneHeightDp,
        calendarEvents = emptyList(),
        calendarColors = emptyMap(),
        calendarDisplay = CalendarDisplayOptions(),
        launcherFontOption = LauncherFontOption.entries.first(),
        appRowPosition = rowPosition,
        appRowPresentation = AppRowPresentation.TEXT_ONLY,
        appListLayout = AppListLayout.SINGLE_COLUMN,
        appListVerticalAlignment = vertical,
        appListColumnAlignment = AppListColumnAlignment.BOTH_LEFT,
        appListGridColumns = AppListGridColumns.entries.first { it != AppListGridColumns.LAUNCHER_DEFAULT },
        appListGridDisplayMode = AppListGridDisplayMode.entries.first { it != AppListGridDisplayMode.LAUNCHER_DEFAULT },
        appLabelColorOption = ClockColorOption.entries.first(),
        homeAppsFontWeight = FontWeightOption.entries.first(),
        dockDisplayMode = DockDisplayMode.entries.first { it != DockDisplayMode.LAUNCHER_DEFAULT },
    )

    private fun setContent(state: () -> HomePreviewState) {
        composeRule.setContent {
            FacetLauncherTheme {
                HomePreview(
                    state = state(),
                    homeWallpaper = HomeWallpaper.Unavailable,
                    testTagPrefix = "test_preview_",
                    clockTestTag = "test_preview_clock",
                    modifier = Modifier.fillMaxWidth().height(600.dp),
                )
            }
        }
    }

    private fun listTop() = composeRule
        .onAllNodes(hasTestTag("test_preview_app_label_preview.test.one"), useUnmergedTree = true)
        .onFirst().fetchSemanticsNode().boundsInRoot.top

    private fun top(tag: String) = composeRule.onNodeWithTag(tag).fetchSemanticsNode().boundsInRoot.top

    @Test
    fun `top alignment places the app list right below the clock`() {
        // Given / When
        setContent { state(vertical = AppListVerticalAlignment.TOP) }
        composeRule.waitForIdle()

        // Then
        val clockBottom = composeRule.onNodeWithTag("test_preview_clock").fetchSemanticsNode().boundsInRoot.bottom
        val listTop = listTop()
        assertTrue("list at $listTop should sit near clock bottom $clockBottom", listTop - clockBottom < 150f)
    }

    @Test
    fun `bottom alignment pushes the app list well below the clock`() {
        // Given / When
        setContent { state(vertical = AppListVerticalAlignment.BOTTOM) }
        composeRule.waitForIdle()

        // Then
        val clockBottom = composeRule.onNodeWithTag("test_preview_clock").fetchSemanticsNode().boundsInRoot.bottom
        val listTop = listTop()
        assertTrue("list at $listTop should be far from clock bottom $clockBottom", listTop - clockBottom > 150f)
    }

    @Test
    fun `right clock alignment moves the clock to the right edge`() {
        // Given / When
        setContent { state(clockAlignment = ClockAlignment.RIGHT) }
        composeRule.waitForIdle()

        // Then
        val clock = composeRule.onNodeWithTag("test_preview_clock").fetchSemanticsNode().boundsInRoot
        val rootWidth = composeRule.onRoot().fetchSemanticsNode().boundsInRoot.width
        assertTrue("clock right edge ${clock.right} should reach the right side of $rootWidth", clock.right > rootWidth * 0.8f)
    }

    @Test
    fun `left clock alignment keeps the clock at the left edge`() {
        // Given / When
        setContent { state(clockAlignment = ClockAlignment.LEFT) }
        composeRule.waitForIdle()

        // Then
        val clock = composeRule.onNodeWithTag("test_preview_clock").fetchSemanticsNode().boundsInRoot
        val rootWidth = composeRule.onRoot().fetchSemanticsNode().boundsInRoot.width
        assertTrue("clock left edge ${clock.left} should stay on the left of $rootWidth", clock.left < rootWidth * 0.2f)
    }

    @Test
    fun `larger clock zone height pushes the clock down`() {
        // Given
        var zoneHeight by mutableStateOf<Float?>(null)
        setContent { state(clockZoneHeightDp = zoneHeight) }
        composeRule.waitForIdle()
        val defaultTop = top("test_preview_clock")

        // When
        zoneHeight = 400f
        composeRule.waitForIdle()

        // Then
        val movedTop = top("test_preview_clock")
        assertTrue("clock top $movedTop should be below default $defaultTop", movedTop > defaultTop)
    }
}
