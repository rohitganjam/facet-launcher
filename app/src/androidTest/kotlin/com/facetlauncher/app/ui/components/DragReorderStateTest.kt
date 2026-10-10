package com.facetlauncher.app.ui.components

import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.down
import androidx.compose.ui.test.moveBy
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.up
import androidx.compose.ui.unit.dp
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

class DragReorderStateTest {

    @get:Rule
    val composeRule = createComposeRule()

    private var order by mutableStateOf(listOf("A", "B", "C"))
    private var committed: List<String>? = null

    private fun setVerticalList() {
        composeRule.setContent {
            val slotPx = with(LocalDensity.current) { SLOT_DP.dp.toPx() }
            val state = rememberDragReorderState(
                items = order,
                key = { it },
                axis = Orientation.Vertical,
                slotSizePx = slotPx,
                onOrderChange = { order = it },
                onDragCommit = { committed = it },
            )
            Column(Modifier.padding(top = 100.dp)) {
                order.forEach { item ->
                    key(item) {
                        Box(Modifier.testTag(item).height(SLOT_DP.dp).width(100.dp).then(state.dragModifier(item)).graphicsLayer { translationY = if (state.isDragging(item)) state.dragOffset else 0f })
                    }
                }
            }
        }
    }

    private fun setHorizontalList() {
        composeRule.setContent {
            val slotPx = with(LocalDensity.current) { SLOT_DP.dp.toPx() }
            val state = rememberDragReorderState(
                items = order,
                key = { it },
                axis = Orientation.Horizontal,
                slotSizePx = slotPx,
                onOrderChange = { order = it },
                onDragCommit = { committed = it },
            )
            Row(Modifier.padding(top = 100.dp, start = 40.dp)) {
                order.forEach { item ->
                    key(item) {
                        Box(Modifier.testTag(item).size(SLOT_DP.dp).then(state.dragModifier(item)).graphicsLayer { translationX = if (state.isDragging(item)) state.dragOffset else 0f })
                    }
                }
            }
        }
    }


    private val slotPx get() = with(composeRule.density) { SLOT_DP.dp.toPx() }

    // Moves in steps, letting a frame render between them (as a real finger would), before lifting.
    private fun dragBy(tag: String, dx: Float = 0f, dy: Float = 0f) {
        val node = composeRule.onNodeWithTag(tag)
        node.performTouchInput { down(Offset(20f, 20f)) }
        repeat(STEPS) {
            node.performTouchInput { moveBy(Offset(dx / STEPS, dy / STEPS)) }
            composeRule.waitForIdle()
        }
        node.performTouchInput { up() }
        composeRule.waitForIdle()
    }

    @Test
    fun draggingDownOneSlotMovesTheItemDownAndCommitsOnRelease() {
        setVerticalList()

        dragBy("A", dy = slotPx + 40f)

        assertEquals(listOf("B", "A", "C"), order)
        assertEquals(listOf("B", "A", "C"), committed)
    }

    @Test
    fun draggingAcrossTwoSlotsMovesTheItemToTheEnd() {
        setVerticalList()

        dragBy("A", dy = 2 * slotPx + 40f)

        assertEquals(listOf("B", "C", "A"), order)
    }

    @Test
    fun draggingPastTheLastSlotKeepsTheItemInBounds() {
        setVerticalList()

        dragBy("C", dy = 3 * slotPx)

        assertEquals(listOf("A", "B", "C"), order)
    }

    @Test
    fun aDragShorterThanHalfASlotLeavesTheOrderAlreadyCommitted() {
        setVerticalList()

        dragBy("A", dy = slotPx / 4)

        assertEquals(listOf("A", "B", "C"), order)
        assertEquals(listOf("A", "B", "C"), committed)
    }

    @Test
    fun horizontalAxisReordersFromHorizontalDrags() {
        setHorizontalList()

        dragBy("A", dx = slotPx + 40f)

        assertEquals(listOf("B", "A", "C"), order)
    }

    private companion object {
        const val SLOT_DP = 100
        const val STEPS = 20
    }
}
