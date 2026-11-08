package com.facetlauncher.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.down
import androidx.compose.ui.test.moveBy
import androidx.compose.ui.test.up
import androidx.compose.ui.unit.dp
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class PickUpReorderGestureTest {

    @get:Rule
    val composeRule = createComposeRule()

    private val taps = mutableListOf<String>()
    private val menus = mutableListOf<String>()
    private val reorders = mutableListOf<List<String>>()

    private fun setColumn(initial: List<String> = listOf("a", "b", "c"), enabled: Boolean = true) {
        composeRule.setContent {
            var items by remember { mutableStateOf(initial) }
            Column(Modifier.fillMaxWidth()) {
                PickUpReorderHost(
                    items = items,
                    key = { it },
                    axis = Orientation.Vertical,
                    enabled = enabled,
                    onReorder = { reorders += it; items = it },
                ) { shown ->
                    shown.forEach { id ->
                        Box(Modifier.reorderItemLayer(id)) {
                            Text(
                                text = id,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(100.dp)
                                    .longPressReorderClickable(id, onClick = { taps += id }, onLongPress = { menus += id })
                                    .testTag("row_$id"),
                            )
                        }
                    }
                }
            }
        }
    }

    /** Holds [tag] past the long-press, drags it [rows] rows (a row is 100dp) and lets go. */
    private fun holdAndDrag(tag: String, rows: Float, hold: Long = 700) {
        composeRule.onNodeWithTag(tag).performTouchInput {
            val dy = rows * 100.dp.toPx()
            down(center)
            advanceEventTime(hold)
            if (dy != 0f) {
                moveBy(androidx.compose.ui.geometry.Offset(0f, dy / 2))
                moveBy(androidx.compose.ui.geometry.Offset(0f, dy / 2))
            }
            up()
        }
        composeRule.waitForIdle()
    }

    @Test
    fun holdingAndDraggingARowDownMovesItAndSavesTheNewOrderOnce() {
        setColumn()

        // When the first row is held past the long-press and dragged two rows down
        holdAndDrag("row_a", rows = 2f)

        // Then the order is saved once, with the row moved, and no menu opened
        assertEquals(listOf(listOf("b", "c", "a")), reorders)
        assertTrue(menus.isEmpty())
        assertTrue(taps.isEmpty())
    }

    @Test
    fun holdingAndDraggingARowUpMovesItUp() {
        setColumn()

        holdAndDrag("row_c", rows = -1f)

        assertEquals(listOf(listOf("a", "c", "b")), reorders)
    }

    @Test
    fun holdingAndReleasingInPlaceOpensTheMenuAndReordersNothing() {
        setColumn()

        // When a row is held past the long-press and released without moving
        holdAndDrag("row_b", rows = 0f)

        // Then the menu callback fires on release, and nothing is reordered or tapped
        assertEquals(listOf("b"), menus)
        assertTrue(reorders.isEmpty())
        assertTrue(taps.isEmpty())
    }

    @Test
    fun aQuickTapStillClicks() {
        setColumn()

        composeRule.onNodeWithTag("row_b").performClick()

        assertEquals(listOf("b"), taps)
        assertTrue(menus.isEmpty())
    }

    @Test
    fun aDragBelowHalfARowLeavesTheOrderAlone() {
        setColumn()

        holdAndDrag("row_b", rows = 0.3f)

        assertTrue(reorders.isEmpty())
    }

    @Test
    fun withReorderingOffTheMenuOpensAtTheHoldWithNoDrag() {
        setColumn(enabled = false)

        composeRule.onNodeWithTag("row_b").performTouchInput {
            down(center)
            advanceEventTime(700)
            moveBy(androidx.compose.ui.geometry.Offset(0f, 150.dp.toPx()))
            up()
        }
        composeRule.waitForIdle()

        assertEquals(listOf("b"), menus)
        assertTrue(reorders.isEmpty())
    }

    @Test
    fun aHorizontalRunReordersAlongTheXAxis() {
        composeRule.setContent {
            var items by remember { mutableStateOf(listOf("a", "b", "c")) }
            Row {
                PickUpReorderHost(
                    items = items,
                    key = { it },
                    axis = Orientation.Horizontal,
                    enabled = true,
                    onReorder = { reorders += it; items = it },
                ) { shown ->
                    shown.forEach { id ->
                        Box(Modifier.reorderItemLayer(id)) {
                            Box(
                                Modifier
                                    .size(100.dp)
                                    .background(Color.Gray)
                                    .longPressReorderClickable(id, onClick = {}, onLongPress = { menus += id })
                                    .testTag("tile_$id"),
                            )
                        }
                    }
                }
            }
        }

        composeRule.onNodeWithTag("tile_a").performTouchInput {
            down(center)
            advanceEventTime(700)
            moveBy(androidx.compose.ui.geometry.Offset(100.dp.toPx(), 0f))
            moveBy(androidx.compose.ui.geometry.Offset(100.dp.toPx(), 0f))
            up()
        }
        composeRule.waitForIdle()

        assertEquals(listOf(listOf("b", "c", "a")), reorders)
    }
}
