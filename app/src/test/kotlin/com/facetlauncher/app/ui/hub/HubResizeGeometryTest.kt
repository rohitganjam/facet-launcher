package com.facetlauncher.app.ui.hub

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import org.junit.Assert.assertEquals
import org.junit.Test

class HubResizeGeometryTest {

    // 100px cells, 10px gaps: a cell starts every 110px.
    private val grid = HubResizeGrid(cellWidthPx = 100f, cellHeightPx = 100f, gapPx = 10f, columns = 5, maxRows = 50)
    private val oneCell = HubSpan(row = 2, col = 1, colSpan = 1, rowSpan = 1)

    @Test
    fun `a span is drawn over its cells and the gaps between them`() {
        assertEquals(Rect(110f, 220f, 320f, 320f), grid.rectOf(HubSpan(row = 2, col = 1, colSpan = 2, rowSpan = 1)))
    }

    @Test
    fun `the dragged edge follows the finger between cells`() {
        // When the right edge is dragged 40px, less than half a cell
        val free = grid.freeRect(oneCell, ResizeEdge.END, null, Offset(40f, 0f), 1, 1)

        // Then the box is 40px wider, not snapped back to one cell
        assertEquals(Rect(110f, 220f, 250f, 320f), free)
    }

    @Test
    fun `the cells snap to the nearest boundary of the dragged edge`() {
        // Under half a cell past the edge keeps one column; over half takes the next
        assertEquals(1, grid.snappedSpan(oneCell, ResizeEdge.END, null, Offset(54f, 0f), 1, 1).colSpan)
        assertEquals(2, grid.snappedSpan(oneCell, ResizeEdge.END, null, Offset(56f, 0f), 1, 1).colSpan)
    }

    @Test
    fun `a left edge drag moves the column and keeps the right edge`() {
        // When the left edge of a one-cell widget at column 1 is dragged a whole cell left
        val span = grid.snappedSpan(oneCell, ResizeEdge.START, null, Offset(-110f, 0f), 1, 1)

        // Then it starts at column 0 and spans two columns, ending where it did
        assertEquals(HubSpan(row = 2, col = 0, colSpan = 2, rowSpan = 1), span)
    }

    @Test
    fun `a top edge drag moves the row and keeps the bottom edge`() {
        val span = grid.snappedSpan(oneCell, null, ResizeEdge.START, Offset(0f, -230f), 1, 1)

        assertEquals(HubSpan(row = 0, col = 1, colSpan = 1, rowSpan = 3), span)
    }

    @Test
    fun `a box never shrinks below the smallest size`() {
        val wide = HubSpan(row = 0, col = 0, colSpan = 3, rowSpan = 2)

        // When the right edge is dragged far past the left one
        val free = grid.freeRect(wide, ResizeEdge.END, null, Offset(-1_000f, 0f), 2, 1)
        val span = grid.snappedSpan(wide, ResizeEdge.END, null, Offset(-1_000f, 0f), 2, 1)

        // Then the box and the cells stop at two columns
        assertEquals(210f, free.width)
        assertEquals(2, span.colSpan)
    }

    @Test
    fun `a box never leaves the grid`() {
        // When the right edge is dragged past the last column, and the left edge past the first
        val right = grid.freeRect(oneCell, ResizeEdge.END, null, Offset(1_000f, 0f), 1, 1)
        val left = grid.freeRect(oneCell, ResizeEdge.START, null, Offset(-1_000f, 0f), 1, 1)

        // Then they stop at the grid's edges, and so do the cells
        assertEquals(540f, right.right)
        assertEquals(0f, left.left)
        assertEquals(4, grid.snappedSpan(oneCell, ResizeEdge.END, null, Offset(1_000f, 0f), 1, 1).colSpan)
        assertEquals(0, grid.snappedSpan(oneCell, ResizeEdge.START, null, Offset(-1_000f, 0f), 1, 1).col)
    }

    @Test
    fun `a box stops at the last row`() {
        val nearBottom = HubSpan(row = 48, col = 0, colSpan = 1, rowSpan = 1)

        val span = grid.snappedSpan(nearBottom, null, ResizeEdge.END, Offset(0f, 1_000f), 1, 1)

        assertEquals(2, span.rowSpan)
    }

    @Test
    fun `with no edge dragged the box is the widget's cells`() {
        assertEquals(grid.rectOf(oneCell), grid.freeRect(oneCell, null, null, Offset(30f, 30f), 1, 1))
        assertEquals(oneCell, grid.snappedSpan(oneCell, null, null, Offset(30f, 30f), 1, 1))
    }
}
