package com.facetlauncher.app.ui.home

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.down
import androidx.compose.ui.test.getUnclippedBoundsInRoot
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.moveBy
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.swipe
import androidx.compose.ui.test.swipeDown
import androidx.compose.ui.test.swipeUp
import androidx.compose.ui.test.up
import androidx.compose.ui.unit.dp
import com.facetlauncher.app.data.model.AppInfo
import com.facetlauncher.app.data.model.AppListColumnAlignment
import com.facetlauncher.app.data.model.AppListGridDisplayMode
import com.facetlauncher.app.data.model.AppRowPresentation
import com.facetlauncher.app.data.model.PlacedItem
import com.facetlauncher.app.domain.SavedGridCells
import com.facetlauncher.app.ui.theme.FacetLauncherTheme
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

/** Home's grid and two-column list scroll when they overflow, and hand the scroll they cannot use to the screen. */
class HomeAppScrollTest {

    @get:Rule
    val composeRule = createComposeRule()

    private val apps = (0 until 40).map { AppInfo(packageName = "com.example.app%02d".format(it), activityName = ".Main", label = "App $it", icon = null) }
    private val items = apps.map { PlacedItem.SingleApp(it) }
    private val hostHeight = 400.dp
    private var leftover = 0f

    private val screen = object : NestedScrollConnection {
        override fun onPostScroll(consumed: Offset, available: Offset, source: NestedScrollSource): Offset {
            if (source == NestedScrollSource.UserInput) leftover += available.y
            return available
        }
    }

    private fun tag(index: Int) = "home_app_icon_${apps[index].packageName}"

    private fun shown(index: Int): Boolean {
        val nodes = composeRule.onAllNodesWithTagUnmerged(tag(index))
        if (nodes.isEmpty()) return false
        val bounds = composeRule.onNodeWithTag(tag(index), useUnmergedTree = true).getUnclippedBoundsInRoot()
        val host = composeRule.onNodeWithTag(HOST, useUnmergedTree = true).getUnclippedBoundsInRoot()
        return bounds.top >= host.top && bounds.bottom <= host.bottom
    }

    private fun androidx.compose.ui.test.junit4.ComposeContentTestRule.onAllNodesWithTagUnmerged(tag: String) =
        onAllNodes(androidx.compose.ui.test.hasTestTag(tag), useUnmergedTree = true).fetchSemanticsNodes()

    private fun setGrid(anchoredAtBottom: Boolean = false) = setHost {
        HomeAppGrid(
            content = HomeAppListContent(items, HomeItemStyle(labelColor = Color.White)),
            actions = HomeItemActions(onAppClick = {}),
            columns = 4,
            displayMode = AppListGridDisplayMode.ICONS,
            savedCells = SavedGridCells(),
            landscape = false,
            anchoredAtBottom = anchoredAtBottom,
            availableHeight = hostHeight,
            onMove = {},
        )
    }

    /** The two-column list sits in a scrolling Column like Home's list region does. */
    private fun setTwoColumnList() = setHost(scrolls = true) {
        HomeAppTwoColumnList(
            content = HomeAppListContent(items, HomeItemStyle(labelColor = Color.White)),
            actions = HomeItemActions(onAppClick = {}),
            columnAlignment = AppListColumnAlignment.BOTH_LEFT,
            appRowPresentation = AppRowPresentation.ICON_AND_TEXT,
            verticalPadding = 8.dp,
            iconSize = 48.dp,
            onReorder = {},
        )
    }

    private fun setHost(scrolls: Boolean = false, body: @Composable () -> Unit) {
        composeRule.setContent {
            FacetLauncherTheme {
                Box(Modifier.height(hostHeight).nestedScroll(screen).testTag(HOST)) {
                    if (scrolls) Column(Modifier.verticalScroll(rememberScrollState())) { body() } else body()
                }
            }
        }
    }

    private fun swipeUpRepeatedly(times: Int = 8) = repeat(times) {
        composeRule.onNodeWithTag(HOST, useUnmergedTree = true).performTouchInput { swipeUp() }
        composeRule.waitForIdle()
    }

    private fun swipeDownRepeatedly(times: Int = 8) = repeat(times) {
        composeRule.onNodeWithTag(HOST, useUnmergedTree = true).performTouchInput { swipeDown() }
        composeRule.waitForIdle()
    }

    @Test
    fun aGridTallerThanItsAreaScrollsToItsLastRow() {
        // Given ten rows of four in an area that shows only some of them
        setGrid()
        assertTrue(shown(0))
        assertFalse(shown(39))

        // When it is swiped up
        swipeUpRepeatedly()

        // Then the last row is reachable and the first has scrolled away
        assertTrue(shown(39))
        assertFalse(shown(0))
    }

    @Test
    fun aGridScrollsBackToItsFirstRow() {
        // Given a grid scrolled to the bottom
        setGrid()
        swipeUpRepeatedly()

        // When it is swiped down
        swipeDownRepeatedly()

        // Then the first row is back
        assertTrue(shown(0))
    }

    @Test
    fun aBottomAnchoredGridStartsAtItsBottomAndScrollsUpToTheLaterRows() {
        // Given a grid anchored at the bottom, whose first items sit on the bottom row
        setGrid(anchoredAtBottom = true)
        assertTrue(shown(0))
        assertFalse(shown(39))

        // When it is swiped down, the way it grows
        swipeDownRepeatedly()

        // Then the last row is reachable
        assertTrue(shown(39))
    }

    @Test
    fun aGridHandsTheScrollItCannotUseToTheScreenAtTheBottom() {
        // Given a grid already scrolled to the bottom
        setGrid()
        swipeUpRepeatedly()
        leftover = 0f

        // When it is swiped up again
        composeRule.onNodeWithTag(HOST, useUnmergedTree = true).performTouchInput { swipeUp() }
        composeRule.waitForIdle()

        // Then the screen receives the upward movement, and the grid stays at the bottom
        assertTrue("leftover $leftover", leftover < 0f)
        assertTrue(shown(39))
    }

    @Test
    fun aGridHandsTheScrollItCannotUseToTheScreenAtTheTop() {
        // Given a grid at its first row
        setGrid()

        // When it is swiped down
        composeRule.onNodeWithTag(HOST, useUnmergedTree = true).performTouchInput { swipeDown() }
        composeRule.waitForIdle()

        // Then the screen receives the downward movement, and the grid stays at the top
        assertTrue("leftover $leftover", leftover > 0f)
        assertTrue(shown(0))
    }

    @Test
    fun aGridKeepsTheScrollItUsesFromTheScreen() {
        // Given a grid with rows below
        setGrid()

        // When it is dragged up a little, well inside its range
        composeRule.onNodeWithTag(HOST, useUnmergedTree = true).performTouchInput {
            swipe(Offset(centerX, centerY), Offset(centerX, centerY - 80.dp.toPx()), durationMillis = 600)
        }
        composeRule.waitForIdle()

        // Then the grid used the movement and the screen got nothing
        assertEquals(0f, leftover, 0.5f)
    }

    @Test
    fun aTwoColumnListScrollsToItsLastRowAndBack() {
        // Given twenty rows of two in an area that shows only some of them
        setTwoColumnList()
        assertTrue(shown(0))
        assertFalse(shown(39))

        // When it is swiped up, then down
        swipeUpRepeatedly()
        assertTrue(shown(39))
        swipeDownRepeatedly()

        // Then it is back at the first row
        assertTrue(shown(0))
    }

    @Test
    fun aTwoColumnListHandsTheScrollItCannotUseToTheScreenAtBothEnds() {
        // Given a list at its first row
        setTwoColumnList()

        // When it is swiped down, the screen receives it
        composeRule.onNodeWithTag(HOST, useUnmergedTree = true).performTouchInput { swipeDown() }
        composeRule.waitForIdle()
        assertTrue("top leftover $leftover", leftover > 0f)

        // And when it is scrolled to the bottom and swiped up, the screen receives that too
        swipeUpRepeatedly()
        leftover = 0f
        composeRule.onNodeWithTag(HOST, useUnmergedTree = true).performTouchInput { swipeUp() }
        composeRule.waitForIdle()
        assertTrue("bottom leftover $leftover", leftover < 0f)
    }

    @Test
    fun holdingAnItemNearTheBottomEdgeScrollsTheGridUnderIt() {
        // Given a grid with rows below the area
        setGrid()
        assertFalse(shown(39))

        // When the first item is held and dragged to the bottom edge, and kept there
        composeRule.onNodeWithTag(tag(0), useUnmergedTree = true).performTouchInput {
            down(center)
            advanceEventTime(700)
            val toBottom = composeRule.onNodeWithTag(HOST, useUnmergedTree = true).getUnclippedBoundsInRoot().bottom
            val distance = with(composeRule.density) { toBottom.toPx() } - center.y - EDGE_INSET_PX
            moveBy(Offset(0f, distance / 2))
            moveBy(Offset(0f, distance / 2))
            repeat(40) {
                composeRule.mainClock.advanceTimeBy(100)
                moveBy(Offset(0f, 0f))
            }
            up()
        }
        composeRule.waitForIdle()

        // Then the grid has scrolled further down than the visible rows
        assertTrue("later rows came into view", shown(8) || shown(12) || shown(16))
    }

    private companion object {
        const val HOST = "scroll_host"
        const val EDGE_INSET_PX = 4f
    }
}
