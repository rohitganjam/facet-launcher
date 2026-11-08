package com.facetlauncher.app.ui.home

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.ui.test.up
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.moveBy
import androidx.compose.ui.test.down
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.test.getUnclippedBoundsInRoot
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.facetlauncher.app.data.model.AppInfo
import com.facetlauncher.app.data.model.AppListGridDisplayMode
import com.facetlauncher.app.data.model.AppRowPresentation
import com.facetlauncher.app.data.model.AppListColumnAlignment
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

    private fun setGrid(
        anchoredAtBottom: Boolean,
        saved: Map<String, GridCell> = emptyMap(),
        onMove: ((Map<String, GridCell>) -> Unit)? = null,
    ) {
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
                    onMove = onMove,
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

    private fun drag(app: AppInfo, dx: Float, dy: Float = 0f) {
        composeRule.onNodeWithTag("home_app_icon_${app.packageName}", useUnmergedTree = true).performTouchInput {
            down(center)
            advanceEventTime(700)
            moveBy(Offset(dx / 2, dy / 2))
            moveBy(Offset(dx / 2, dy / 2))
            up()
        }
        composeRule.waitForIdle()
    }

    private fun columnSpacing(): Float = with(composeRule.density) { (left(apps[1]) - left(apps[0])).toPx() }

    @Test
    fun droppingAnItemOnAnEmptyCellMovesIt() {
        // Given the three items on the first row of a draggable grid
        var movedCells: Map<String, GridCell>? = null
        setGrid(anchoredAtBottom = false, onMove = { cells -> movedCells = cells })

        // When the first is held and dragged to the empty fourth column
        drag(apps[0], dx = columnSpacing() * 3)

        // Then it takes that cell and the others stay
        composeRule.waitUntil(timeoutMillis = 3_000) { movedCells != null }
        assertEquals(GridCell(0, 3), movedCells!![items[0].gridKey()])
        assertEquals(GridCell(0, 1), movedCells!![items[1].gridKey()])
    }

    @Test
    fun droppingAnItemOnAnotherSwapsThem() {
        var movedCells: Map<String, GridCell>? = null
        setGrid(anchoredAtBottom = false, onMove = { cells -> movedCells = cells })

        // When the first is dropped on the second
        drag(apps[0], dx = columnSpacing())

        // Then they swap
        composeRule.waitUntil(timeoutMillis = 3_000) { movedCells != null }
        assertEquals(GridCell(0, 1), movedCells!![items[0].gridKey()])
        assertEquals(GridCell(0, 0), movedCells!![items[1].gridKey()])
    }

    @Test
    fun aBottomAnchoredGridDragsUpToTheRowAboveIt() {
        // Given a bottom-anchored grid, whose first row is the bottom one
        var movedCells: Map<String, GridCell>? = null
        setGrid(anchoredAtBottom = true, onMove = { cells -> movedCells = cells })
        val rowHeight = with(composeRule.density) { 64.dp.toPx() }

        // When the first item is dragged one row up
        drag(apps[0], dx = 0f, dy = -rowHeight)

        // Then it lands on the second row from the anchor
        composeRule.waitUntil(timeoutMillis = 3_000) { movedCells != null }
        assertEquals(1, movedCells!![items[0].gridKey()]!!.row)
    }

    @Test
    fun threeColumnsLineUpWithADockOfThreeIcons() = checkLinesUpWithDock(3)

    @Test
    fun fourColumnsLineUpWithADockOfFourIcons() = checkLinesUpWithDock(4)

    @Test
    fun fiveColumnsLineUpWithADockOfFiveIcons() = checkLinesUpWithDock(5)

    private fun checkLinesUpWithDock(columns: Int) {
        run {
            // Given a grid of N icons over a row laid out like the Dock (space evenly) with N icons of the same size
            val shown = items.take(3) + (3 until columns).map { PlacedItem.SingleApp(AppInfo(packageName = "com.example.extra$it", activityName = ".Main", label = "Extra $it", icon = null)) }
            val gridApps = shown.take(columns)
            composeRule.setContent {
                FacetLauncherTheme {
                    androidx.compose.foundation.layout.Column(androidx.compose.ui.Modifier.fillMaxWidth()) {
                        HomeAppGrid(
                            content = HomeAppListContent(gridApps, HomeItemStyle(labelColor = Color.White)),
                            actions = HomeItemActions(onAppClick = {}),
                            columns = columns,
                            displayMode = AppListGridDisplayMode.ICONS,
                            savedCells = SavedGridCells(),
                            landscape = false,
                            anchoredAtBottom = false,
                            availableHeight = 120.dp,
                        )
                        androidx.compose.foundation.layout.Row(androidx.compose.ui.Modifier.fillMaxWidth(), horizontalArrangement = androidx.compose.foundation.layout.Arrangement.SpaceEvenly) {
                            repeat(columns) { androidx.compose.foundation.layout.Box(androidx.compose.ui.Modifier.size(com.facetlauncher.app.ui.components.AppIconSize.scaled()).testTag("ref_$it")) }
                        }
                    }
                }
            }

            // Then each grid icon's centre is where the matching dock icon's centre is
            gridApps.forEachIndexed { index, item ->
                val gridCentre = composeRule.onNodeWithTag("home_app_icon_${(item as PlacedItem.SingleApp).app.packageName}", useUnmergedTree = true).getUnclippedBoundsInRoot().let { (it.left + it.right) / 2 }
                val refCentre = composeRule.onNodeWithTag("ref_$index", useUnmergedTree = true).getUnclippedBoundsInRoot().let { (it.left + it.right) / 2 }
                assertEquals("$columns columns, icon $index", refCentre.value, gridCentre.value, 1f)
            }
        }
    }

    @Test
    fun draggingARowInTheTwoColumnListToAnotherCellReordersTheList() {
        // Given a two-column list of four apps: a b on the first row, c d on the second
        val four = ('a'..'d').map { PlacedItem.SingleApp(AppInfo(packageName = "com.example.$it", activityName = ".Main", label = "App $it", icon = null)) }
        var reordered: List<PlacedItem>? = null
        composeRule.setContent {
            FacetLauncherTheme {
                HomeAppTwoColumnList(
                    content = HomeAppListContent(four, HomeItemStyle(labelColor = Color.White)),
                    actions = HomeItemActions(onAppClick = {}),
                    columnAlignment = AppListColumnAlignment.BOTH_LEFT,
                    appRowPresentation = AppRowPresentation.ICON_AND_TEXT,
                    verticalPadding = 8.dp,
                    iconSize = 48.dp,
                    onReorder = { reordered = it },
                )
            }
        }
        fun bounds(letter: Char) = composeRule.onNodeWithTag("home_app_icon_com.example.$letter", useUnmergedTree = true).getUnclippedBoundsInRoot()
        val dx = with(composeRule.density) { (bounds('d').left - bounds('a').left).toPx() }
        val dy = with(composeRule.density) { (bounds('d').top - bounds('a').top).toPx() }

        // When the first row's app is held and dragged onto the last cell
        composeRule.onNodeWithTag("home_app_icon_com.example.a", useUnmergedTree = true).performTouchInput {
            down(center)
            advanceEventTime(700)
            moveBy(Offset(dx / 2, dy / 2))
            moveBy(Offset(dx / 2, dy / 2))
            up()
        }
        composeRule.waitForIdle()

        // Then it takes that place in the order and the rest move up
        composeRule.waitUntil(timeoutMillis = 3_000) { reordered != null }
        assertEquals(listOf("b", "c", "d", "a").map { "com.example.$it" }, reordered!!.map { (it as PlacedItem.SingleApp).app.packageName })
    }
}
