package com.facetlauncher.app.ui.hub

import androidx.compose.ui.unit.dp
import org.junit.Assert.assertEquals
import org.junit.Test

class HubLandscapeGeometryTest {

    @Test
    fun `rows are as many whole cells as fit`() {
        // Given 64dp cells with a 4dp gap: five rows need 5 * 64 + 4 * 4 = 336dp
        assertEquals(5, hubLandscapeRows(availableHeight = 336.dp, gap = 4.dp))
        assertEquals(4, hubLandscapeRows(availableHeight = 335.dp, gap = 4.dp))
    }

    @Test
    fun `a very short window still gets the minimum rows`() {
        assertEquals(HUB_LANDSCAPE_MIN_ROWS, hubLandscapeRows(availableHeight = 50.dp, gap = 4.dp))
    }

    @Test
    fun `a span is its cells plus the gaps between them`() {
        assertEquals(64.dp, hubLandscapeSpanSize(1, gap = 4.dp))
        assertEquals(64.dp * 3 + 4.dp * 2, hubLandscapeSpanSize(3, gap = 4.dp))
    }

    @Test
    fun `a cell starts after the cells and gaps before it`() {
        assertEquals(0.dp, hubLandscapeCellOrigin(0, gap = 4.dp))
        assertEquals((64.dp + 4.dp) * 2, hubLandscapeCellOrigin(2, gap = 4.dp))
    }

    @Test
    fun `a drag moves the drop cell by whole cells`() {
        // Given a widget at row 1, column 2 and cells 68px apart, dragged 70px right and 140px down
        val cell = hubLandscapeDropCell(widgetRow = 1, widgetCol = 2, widgetRowSpan = 1, dragX = 70f, dragY = 140f, pitchX = 68f, pitchY = 68f, rows = 5)

        assertEquals(HubCell(row = 3, col = 3), cell)
    }

    @Test
    fun `a drag left past the first column stops at column zero`() {
        val cell = hubLandscapeDropCell(widgetRow = 0, widgetCol = 1, widgetRowSpan = 1, dragX = -900f, dragY = 0f, pitchX = 68f, pitchY = 68f, rows = 5)

        assertEquals(0, cell.col)
    }

    @Test
    fun `a tall widget cannot be dropped so low that it hangs off the grid`() {
        // Given a two row widget in a four row grid, dragged far down
        val cell = hubLandscapeDropCell(widgetRow = 0, widgetCol = 0, widgetRowSpan = 2, dragX = 0f, dragY = 900f, pitchX = 68f, pitchY = 68f, rows = 4)

        assertEquals(2, cell.row)
    }

    @Test
    fun `a drag above the grid stops at row zero`() {
        val cell = hubLandscapeDropCell(widgetRow = 2, widgetCol = 0, widgetRowSpan = 1, dragX = 0f, dragY = -900f, pitchX = 68f, pitchY = 68f, rows = 5)

        assertEquals(0, cell.row)
    }
}
