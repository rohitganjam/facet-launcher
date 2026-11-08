package com.facetlauncher.app.ui.components

import androidx.compose.foundation.gestures.Orientation
import androidx.compose.ui.geometry.Offset
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class PickUpReorderTest {

    // Four cells 100px apart on the axis, as a list of rows would rest
    private val centers = listOf(50f, 150f, 250f, 350f)

    @Test
    fun `the target is the slot nearest the dragged cell`() {
        // Given the second cell dragged forward by various amounts
        // Then the target is the slot whose centre is nearest the dragged centre
        assertEquals(2, ReorderGeometry.targetIndex(centers, from = 1, offset = 130f))
        assertEquals(3, ReorderGeometry.targetIndex(centers, from = 1, offset = 210f))
        assertEquals(0, ReorderGeometry.targetIndex(centers, from = 1, offset = -120f))
    }

    @Test
    fun `a drag shorter than half a slot stays in place`() {
        assertEquals(1, ReorderGeometry.targetIndex(centers, from = 1, offset = 40f))
        assertEquals(1, ReorderGeometry.targetIndex(centers, from = 1, offset = -40f))
    }

    @Test
    fun `cells between the start and the target slide into the vacated slots`() {
        // Given the first cell dragged over the third slot
        // Then the second and third each slide back one slot, the rest stay
        assertEquals(0f, ReorderGeometry.neighbourShift(centers, index = 0, from = 0, to = 2))
        assertEquals(-100f, ReorderGeometry.neighbourShift(centers, index = 1, from = 0, to = 2))
        assertEquals(-100f, ReorderGeometry.neighbourShift(centers, index = 2, from = 0, to = 2))
        assertEquals(0f, ReorderGeometry.neighbourShift(centers, index = 3, from = 0, to = 2))
    }

    @Test
    fun `cells slide forward when the dragged cell moves back`() {
        assertEquals(100f, ReorderGeometry.neighbourShift(centers, index = 1, from = 3, to = 1))
        assertEquals(100f, ReorderGeometry.neighbourShift(centers, index = 2, from = 3, to = 1))
        assertEquals(0f, ReorderGeometry.neighbourShift(centers, index = 0, from = 3, to = 1))
    }

    @Test
    fun `uneven slots shift each neighbour by its own gap`() {
        // Given a Dock whose icons rest unevenly
        val uneven = listOf(40f, 100f, 220f)
        // Then a neighbour takes the exact centre of the slot it moves into
        assertEquals(-60f, ReorderGeometry.neighbourShift(uneven, index = 1, from = 0, to = 1))
    }

    @Test
    fun `move reorders a list and leaves it alone for a no-op`() {
        assertEquals(listOf("b", "c", "a"), ReorderGeometry.move(listOf("a", "b", "c"), 0, 2))
        assertEquals(listOf("c", "a", "b"), ReorderGeometry.move(listOf("a", "b", "c"), 2, 0))
        assertEquals(listOf("a", "b", "c"), ReorderGeometry.move(listOf("a", "b", "c"), 1, 1))
        assertEquals(listOf("a", "b", "c"), ReorderGeometry.move(listOf("a", "b", "c"), 1, 5))
    }

    @Test
    fun `the tracker becomes a drag once the finger passes the slop`() {
        val tracker = PickUpDragTracker(slopPx = 10f)

        // When the finger moves a little, it is still a hold
        assertFalse(tracker.update(3f, 4f))
        assertFalse(tracker.dragging)

        // When it passes the slop, the drag starts exactly once
        assertTrue(tracker.update(0f, 12f))
        assertTrue(tracker.dragging)
        assertFalse(tracker.update(0f, 40f))
        assertTrue(tracker.dragging)
    }

    @Test
    fun `the tracker counts travel in both axes`() {
        val tracker = PickUpDragTracker(slopPx = 10f)
        assertTrue(tracker.update(8f, 8f))
    }

    private fun state(vararg keys: String, onMove: (Int, Int) -> Unit = { _, _ -> }) =
        PickUpReorderState(Orientation.Vertical).apply {
            this.keys = keys.toList()
            this.onMove = onMove
            keys.forEachIndexed { index, key -> recordCenter(key, 50f + 100f * index) }
        }

    @Test
    fun `releasing in place reports no move`() {
        var moved: Pair<Int, Int>? = null
        val state = state("a", "b", "c") { from, to -> moved = from to to }

        // Given a cell picked up but not dragged
        state.pickUp("b")
        assertEquals("b", state.liftedKey)
        assertFalse(state.dragging)

        // When it is released
        state.release()

        // Then nothing moved and the state is clear
        assertNull(moved)
        assertNull(state.liftedKey)
    }

    @Test
    fun `dragging a cell to another slot reports the move on release`() {
        var moved: Pair<Int, Int>? = null
        val state = state("a", "b", "c") { from, to -> moved = from to to }

        // Given the first cell picked up and dragged 190px forward
        state.pickUp("a")
        state.startDrag()
        state.dragTo(Offset(0f, 190f))

        // Then the third slot is the target and the second cell slides back
        assertEquals(2, state.targetIndex)
        assertEquals(-100f, state.shiftFor("b"), 0f)

        // When it is released
        state.release()

        // Then the move is reported and the state is clear
        assertEquals(0 to 2, moved)
        assertNull(state.liftedKey)
        assertFalse(state.dragging)
        assertEquals(0f, state.offset, 0f)
    }

    @Test
    fun `a drag that ends over its own slot reports no move`() {
        var moved: Pair<Int, Int>? = null
        val state = state("a", "b", "c") { from, to -> moved = from to to }

        state.pickUp("b")
        state.startDrag()
        state.dragTo(Offset(0f, 30f))
        state.release()

        assertNull(moved)
    }

    @Test
    fun `a horizontal run follows the x axis`() {
        var moved: Pair<Int, Int>? = null
        val state = PickUpReorderState(Orientation.Horizontal).apply {
            keys = listOf("a", "b")
            onMove = { from, to -> moved = from to to }
            recordCenter("a", 50f)
            recordCenter("b", 150f)
        }

        state.pickUp("a")
        state.startDrag()
        state.dragTo(Offset(120f, 5f))
        state.release()

        assertEquals(0 to 1, moved)
    }
}
