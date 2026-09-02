package com.lumenlauncher.app.domain

import com.lumenlauncher.app.data.local.WidgetPlacementEntity
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ResolveWidgetDropUseCaseTest {

    private val useCase = ResolveWidgetDropUseCase()

    private fun placement(id: Int, row: Int, col: Int, colSpan: Int = 1, rowSpan: Int = 1) = WidgetPlacementEntity(
        appWidgetId = id,
        providerPackageName = "com.example.widgets",
        providerClassName = ".Provider",
        row = row,
        col = col,
        colSpan = colSpan,
        rowSpan = rowSpan,
    )

    private fun List<WidgetPlacementEntity>.find(id: Int) = single { it.appWidgetId == id }

    @Test
    fun `dropping onto an empty target is a plain move`() {
        val existing = listOf(placement(id = 1, row = 0, col = 0))

        val result = useCase(existing, movingAppWidgetId = 1, targetRow = 3, targetCol = 3, colSpan = 1, rowSpan = 1)

        assertEquals(3, result?.find(1)?.row)
        assertEquals(3, result?.find(1)?.col)
    }

    @Test
    fun `dropping onto an occupied cell pushes the occupant to its own right`() {
        // Given widget 2 sitting where widget 1 is about to be dropped, with room to its right
        val existing = listOf(placement(id = 1, row = 5, col = 0), placement(id = 2, row = 0, col = 0))

        val result = useCase(existing, movingAppWidgetId = 1, targetRow = 0, targetCol = 0, colSpan = 1, rowSpan = 1)

        assertEquals(0, result?.find(1)?.row)
        assertEquals(0, result?.find(1)?.col)
        // Pushed right, same row
        assertEquals(0, result?.find(2)?.row)
        assertEquals(1, result?.find(2)?.col)
    }

    @Test
    fun `when the right is blocked the occupant is pushed straight down instead`() {
        // Given widget 2 at the last column (nowhere to its right) and widget 1 dropping on it
        val existing = listOf(
            placement(id = 1, row = 5, col = 0),
            placement(id = 2, row = 0, col = HUB_COLUMNS - 1),
        )

        val result = useCase(existing, movingAppWidgetId = 1, targetRow = 0, targetCol = HUB_COLUMNS - 1, colSpan = 1, rowSpan = 1)

        assertEquals(HUB_COLUMNS - 1, result?.find(1)?.col)
        // Pushed down, same column
        assertEquals(1, result?.find(2)?.row)
        assertEquals(HUB_COLUMNS - 1, result?.find(2)?.col)
    }

    @Test
    fun `when neither the right nor down fits the whole drop is rejected`() {
        // Given widget 2 at the bottom-right corner — no room to its right or below
        val existing = listOf(
            placement(id = 1, row = 5, col = 0),
            placement(id = 2, row = HUB_MAX_ROWS - 1, col = HUB_COLUMNS - 1),
        )

        val result = useCase(
            existing, movingAppWidgetId = 1,
            targetRow = HUB_MAX_ROWS - 1, targetCol = HUB_COLUMNS - 1, colSpan = 1, rowSpan = 1,
        )

        assertNull(result)
    }

    @Test
    fun `displacing one widget that would then collide with another pushes it down instead`() {
        // Given widget 2 directly under the target, and widget 3 already sitting where a
        // rightward push of widget 2 would land — the right push must be rejected in favor of down
        val existing = listOf(
            placement(id = 1, row = 5, col = 0),
            placement(id = 2, row = 0, col = 0),
            placement(id = 3, row = 0, col = 1),
        )

        val result = useCase(existing, movingAppWidgetId = 1, targetRow = 0, targetCol = 0, colSpan = 1, rowSpan = 1)

        assertEquals(1, result?.find(2)?.row)
        assertEquals(0, result?.find(2)?.col)
        // Widget 3 never had to move
        assertEquals(0, result?.find(3)?.row)
        assertEquals(1, result?.find(3)?.col)
    }

    @Test
    fun `a move partially off the column bound is rejected`() {
        val existing = listOf(placement(id = 1, row = 0, col = 0))

        val result = useCase(existing, movingAppWidgetId = 1, targetRow = 0, targetCol = HUB_COLUMNS - 1, colSpan = 2, rowSpan = 1)

        assertNull(result)
    }

    @Test
    fun `moving a widget onto its own current cells is a no-op move, not a false-positive self-overlap`() {
        val existing = listOf(placement(id = 1, row = 2, col = 2))

        val result = useCase(existing, movingAppWidgetId = 1, targetRow = 2, targetCol = 2, colSpan = 1, rowSpan = 1)

        assertTrue(result != null && result.size == 1)
    }

    @Test
    fun `a negative target position is rejected`() {
        val result = useCase(existing = emptyList(), movingAppWidgetId = 1, targetRow = -1, targetCol = 0, colSpan = 1, rowSpan = 1)

        assertNull(result)
    }
}
