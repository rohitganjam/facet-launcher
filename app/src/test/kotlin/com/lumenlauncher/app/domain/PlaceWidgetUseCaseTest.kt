package com.lumenlauncher.app.domain

import com.lumenlauncher.app.data.local.WidgetPlacementEntity
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class PlaceWidgetUseCaseTest {

    private val useCase = PlaceWidgetUseCase()

    private fun placement(id: Int, row: Int, col: Int, colSpan: Int = 1, rowSpan: Int = 1) = WidgetPlacementEntity(
        appWidgetId = id,
        providerPackageName = "com.example.widgets",
        providerClassName = ".Provider",
        row = row,
        col = col,
        colSpan = colSpan,
        rowSpan = rowSpan,
    )

    @Test
    fun `empty grid places at the top-left cell`() {
        val result = useCase(existing = emptyList(), colSpan = 2, rowSpan = 1)

        assertEquals(PlaceWidgetResult.Placed(row = 0, col = 0), result)
    }

    @Test
    fun `a full top row skips to the next row`() {
        // Given the entire first row occupied by one 5-wide widget
        val existing = listOf(placement(id = 1, row = 0, col = 0, colSpan = HUB_COLUMNS, rowSpan = 1))

        // When placing a 1x1 widget
        val result = useCase(existing, colSpan = 1, rowSpan = 1)

        // Then it lands on row 1, not overlapping the full row
        assertEquals(PlaceWidgetResult.Placed(row = 1, col = 0), result)
    }

    @Test
    fun `a 2x2 widget does not overlap an existing 1x1 at its target cell`() {
        // Given a 1x1 widget sitting at (0, 0)
        val existing = listOf(placement(id = 1, row = 0, col = 0))

        // When placing a 2x2 widget
        val result = useCase(existing, colSpan = 2, rowSpan = 2) as PlaceWidgetResult.Placed

        // Then its placement doesn't overlap (0,0)'s single cell
        val overlapsOrigin = result.row == 0 && result.col == 0
        assertTrue("expected the 2x2 widget to avoid (0,0), got (${result.row}, ${result.col})", !overlapsOrigin)
    }

    @Test
    fun `twenty existing widgets returns HubFull regardless of free space`() {
        // Given the 20-widget cap already reached, with plenty of empty grid space left
        val existing = (0 until HUB_MAX_WIDGETS).map { placement(id = it, row = it, col = 0) }

        // When placing one more
        val result = useCase(existing, colSpan = 1, rowSpan = 1)

        // Then it's rejected on the widget cap, not a space search
        assertEquals(PlaceWidgetResult.HubFull, result)
    }

    @Test
    fun `every row occupied but under twenty widgets also returns HubFull`() {
        // Given fewer than 20 widgets, but each a full-width, 3-row-tall widget covering every
        // one of the 50 rows between them (ceil(50/3) = 17 widgets)
        val existing = buildList {
            var row = 0
            var id = 0
            while (row < HUB_MAX_ROWS) {
                val rowSpan = minOf(3, HUB_MAX_ROWS - row)
                add(placement(id = id++, row = row, col = 0, colSpan = HUB_COLUMNS, rowSpan = rowSpan))
                row += rowSpan
            }
        }
        assertTrue(existing.size < HUB_MAX_WIDGETS)

        // When placing one more
        val result = useCase(existing, colSpan = 1, rowSpan = 1)

        // Then the grid-full edge case is treated the same as the widget cap (see plan decisions)
        assertEquals(PlaceWidgetResult.HubFull, result)
    }

    // F14 Backup & Restore's guided widget re-add — see PlaceWidgetUseCase's own doc comment.

    @Test
    fun `a free preferred cell is used instead of the first-fit scan`() {
        // Given an empty grid, but a widget already sitting at the very first first-fit cell
        val existing = listOf(placement(id = 1, row = 0, col = 0))

        // When placing with a preferred position elsewhere that's free
        val result = useCase(existing, colSpan = 1, rowSpan = 1, preferredRow = 3, preferredCol = 2)

        // Then it lands exactly at the preferred cell, not wherever first-fit would have chosen
        assertEquals(PlaceWidgetResult.Placed(row = 3, col = 2), result)
    }

    @Test
    fun `an occupied preferred cell falls back to first-fit`() {
        // Given something already occupying the preferred cell
        val existing = listOf(placement(id = 1, row = 0, col = 0))

        // When placing with that same cell as preferred
        val result = useCase(existing, colSpan = 1, rowSpan = 1, preferredRow = 0, preferredCol = 0)

        // Then it falls back to the ordinary first-fit scan instead of overlapping
        assertEquals(PlaceWidgetResult.Placed(row = 0, col = 1), result)
    }

    @Test
    fun `a preferred cell outside the grid bounds falls back to first-fit`() {
        val result = useCase(existing = emptyList(), colSpan = 1, rowSpan = 1, preferredRow = HUB_MAX_ROWS, preferredCol = 0)

        assertEquals(PlaceWidgetResult.Placed(row = 0, col = 0), result)
    }

    @Test
    fun `no preferred position keeps the unchanged first-fit behavior`() {
        val result = useCase(existing = emptyList(), colSpan = 1, rowSpan = 1)

        assertEquals(PlaceWidgetResult.Placed(row = 0, col = 0), result)
    }
}
