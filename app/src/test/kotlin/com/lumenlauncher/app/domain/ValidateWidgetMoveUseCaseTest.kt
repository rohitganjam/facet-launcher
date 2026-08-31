package com.lumenlauncher.app.domain

import com.lumenlauncher.app.data.local.WidgetPlacementEntity
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ValidateWidgetMoveUseCaseTest {

    private val useCase = ValidateWidgetMoveUseCase()

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
    fun `a non-overlapping move onto empty cells is accepted`() {
        val existing = listOf(placement(id = 1, row = 0, col = 0))

        val result = useCase(existing, movingAppWidgetId = 1, targetRow = 3, targetCol = 3, colSpan = 1, rowSpan = 1)

        assertTrue(result)
    }

    @Test
    fun `moving onto another widget's occupied cells is rejected`() {
        // Given widget 2 occupying (0,0)
        val existing = listOf(placement(id = 1, row = 5, col = 0), placement(id = 2, row = 0, col = 0))

        // When widget 1 tries to move onto widget 2's cell
        val result = useCase(existing, movingAppWidgetId = 1, targetRow = 0, targetCol = 0, colSpan = 1, rowSpan = 1)

        assertFalse(result)
    }

    @Test
    fun `a move partially off the column bound is rejected`() {
        val existing = listOf(placement(id = 1, row = 0, col = 0))

        // colSpan 2 starting at the last column overruns HUB_COLUMNS
        val result = useCase(existing, movingAppWidgetId = 1, targetRow = 0, targetCol = HUB_COLUMNS - 1, colSpan = 2, rowSpan = 1)

        assertFalse(result)
    }

    @Test
    fun `a move partially off the row bound is rejected`() {
        val existing = listOf(placement(id = 1, row = 0, col = 0))

        val result = useCase(existing, movingAppWidgetId = 1, targetRow = HUB_MAX_ROWS - 1, targetCol = 0, colSpan = 1, rowSpan = 2)

        assertFalse(result)
    }

    @Test
    fun `moving a widget onto its own current cells is accepted, not a false-positive self-overlap`() {
        // Given widget 1 already at (2, 2)
        val existing = listOf(placement(id = 1, row = 2, col = 2))

        // When "moving" it to the same cell it already occupies
        val result = useCase(existing, movingAppWidgetId = 1, targetRow = 2, targetCol = 2, colSpan = 1, rowSpan = 1)

        assertTrue(result)
    }

    @Test
    fun `a negative target position is rejected`() {
        val result = useCase(existing = emptyList(), movingAppWidgetId = 1, targetRow = -1, targetCol = 0, colSpan = 1, rowSpan = 1)

        assertFalse(result)
    }
}
