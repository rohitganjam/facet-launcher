package com.facetlauncher.app.ui.home

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.test.getUnclippedBoundsInRoot
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.facetlauncher.app.data.model.AppInfo
import com.facetlauncher.app.data.model.AppListGridDisplayMode
import com.facetlauncher.app.data.model.PlacedItem
import com.facetlauncher.app.data.model.gridKey
import com.facetlauncher.app.domain.GridCell
import com.facetlauncher.app.domain.SavedGridCells
import com.facetlauncher.app.ui.theme.FacetLauncherTheme
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class HomeAppGridTest {

    @get:Rule
    val composeRule = createComposeRule()

    private val apps = ('a'..'c').map { AppInfo(packageName = "com.example.$it", activityName = ".Main", label = "App $it", icon = null) }
    private val items = apps.map { PlacedItem.SingleApp(it) }
    private val height = 400.dp

    private fun setGrid(anchoredAtBottom: Boolean, saved: Map<String, GridCell> = emptyMap()) {
        composeRule.setContent {
            FacetLauncherTheme {
                HomeAppGrid(
                    content = HomeAppListContent(items, HomeItemStyle(labelColor = Color.White)),
                    actions = HomeItemActions(onAppClick = {}),
                    columns = 4,
                    displayMode = AppListGridDisplayMode.ICONS,
                    savedCells = SavedGridCells(portrait = saved),
                    landscape = false,
                    anchoredAtBottom = anchoredAtBottom,
                    availableHeight = height,
                )
            }
        }
    }

    private fun top(app: AppInfo): Dp = composeRule.onNodeWithTag("home_app_icon_${app.packageName}", useUnmergedTree = true).getUnclippedBoundsInRoot().top
    private fun left(app: AppInfo): Dp = composeRule.onNodeWithTag("home_app_icon_${app.packageName}", useUnmergedTree = true).getUnclippedBoundsInRoot().left

    @Test
    fun aTopAnchorStartsTheFirstItemsAtTheTopLeftToRight() {
        // Given three unplaced items with a top anchor
        setGrid(anchoredAtBottom = false)

        // Then they share the top row, left to right
        assertEquals(top(apps[0]), top(apps[1]))
        assertTrue(left(apps[0]) < left(apps[1]) && left(apps[1]) < left(apps[2]))
        assertTrue("starts at the top", top(apps[0]) < height / 4)
    }

    @Test
    fun aBottomAnchorStartsTheFirstItemsAtTheBottomInTheSameOrder() {
        // Given the same items with a bottom anchor
        setGrid(anchoredAtBottom = true)

        // Then they sit on the bottom row of the grid, still left to right
        assertTrue("starts at the bottom", top(apps[0]) > height / 2)
        assertEquals(top(apps[0]), top(apps[2]))
        assertTrue(left(apps[0]) < left(apps[2]))
    }

    @Test
    fun aSavedCellGrowsAwayFromTheAnchor() {
        // Given the third item saved one row away from the anchor
        val saved = mapOf(items[2].gridKey() to GridCell(1, 0))

        // When anchored at the top it is below the first row, and at the bottom above it
        setGrid(anchoredAtBottom = false, saved = saved)
        assertTrue(top(apps[2]) > top(apps[0]))
    }

    @Test
    fun aSavedCellAboveTheAnchorRowAtTheBottomSitsHigher() {
        val saved = mapOf(items[2].gridKey() to GridCell(1, 0))

        setGrid(anchoredAtBottom = true, saved = saved)

        assertTrue(top(apps[2]) < top(apps[0]))
    }
}
