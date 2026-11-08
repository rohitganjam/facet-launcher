package com.facetlauncher.app.ui.components

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class GridDragTest {

    // A 2x2 grid of 100px cells, indexed row by row
    private fun state(onDrop: (Int, Int) -> Unit = { _, _ -> }) = GridDragState().apply {
        indexOfKey = { key -> listOf("a", "b", "c", "d").indexOf(key).takeIf { it >= 0 } }
        this.onDrop = onDrop
        for (index in 0..3) recordBounds(index, Rect(Offset((index % 2) * 100f, (index / 2) * 100f), androidx.compose.ui.geometry.Size(100f, 100f)))
    }

    @Test
    fun `the cell under a point is the one whose bounds hold it`() {
        val bounds = mapOf(0 to Rect(0f, 0f, 100f, 100f), 1 to Rect(100f, 0f, 200f, 100f))

        assertEquals(1, cellIndexAt(bounds, Offset(150f, 50f)))
        assertNull(cellIndexAt(bounds, Offset(250f, 50f)))
    }

    @Test
    fun `a held item targets the cell its centre was moved over`() {
        val state = state()

        // Given the first item lifted and dragged one cell right and one down
        state.pickUp("a")
        state.startDrag()
        state.dragTo(Offset(100f, 100f))

        // Then the target is the last cell
        assertEquals(3, state.targetIndex)
    }

    @Test
    fun `releasing over another cell reports the move`() {
        var dropped: Pair<Int, Int>? = null
        val state = state { from, to -> dropped = from to to }

        state.pickUp("b")
        state.startDrag()
        state.dragTo(Offset(-100f, 0f))
        state.release()

        assertEquals(1 to 0, dropped)
        assertNull(state.liftedKey)
    }

    @Test
    fun `releasing in place or over its own cell reports nothing`() {
        var dropped = false
        val state = state { _, _ -> dropped = true }

        state.pickUp("a")
        state.release()
        state.pickUp("a")
        state.startDrag()
        state.dragTo(Offset(20f, 20f))
        state.release()

        assertEquals(false, dropped)
    }

    @Test
    fun `scrolling under the finger moves the drawn item with it`() {
        val state = state()

        state.pickUp("a")
        state.startDrag()
        state.dragTo(Offset(0f, 30f))
        state.addScroll(40f)

        // The item is drawn the finger's travel plus the scroll, while the finger itself did not move
        assertEquals(Offset(0f, 70f), state.offset)
        assertEquals(Offset(50f, 80f), state.pointer)
    }

    @Test
    fun `in a list read row by row the cells between the held one and its target slide along`() {
        // Given the first item of a 2x2 list held and dragged over the last cell
        val state = state().apply { insertMode = true }
        state.pickUp("a")
        state.startDrag()
        state.dragTo(Offset(100f, 100f))

        // Then b, c and d each slide back one place, into the cell before their own
        assertEquals(Offset(-100f, 0f), state.shiftFor("b"))
        assertEquals(Offset(100f, -100f), state.shiftFor("c"))
        assertEquals(Offset(-100f, 0f), state.shiftFor("d"))
        assertEquals(Offset.Zero, state.shiftFor("a"))
    }

    @Test
    fun `in a grid nothing slides, since a drop swaps`() {
        val state = state()
        state.pickUp("a")
        state.startDrag()
        state.dragTo(Offset(100f, 100f))

        assertEquals(Offset.Zero, state.shiftFor("d"))
    }
}
