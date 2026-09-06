package com.lumenlauncher.app.domain

import com.lumenlauncher.app.data.local.WidgetPlacementEntity
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class ResolveWidgetResizeUseCaseTest {

    private val useCase = ResolveWidgetResizeUseCase()

    private fun placement(id: Int, row: Int, col: Int, colSpan: Int = 1, rowSpan: Int = 1) = WidgetPlacementEntity(
        appWidgetId = id,
        providerPackageName = "com.example.widgets",
        providerClassName = ".Provider",
        row = row,
        col = col,
        colSpan = colSpan,
        rowSpan = rowSpan,
    )

    private fun List<WidgetPlacementEntity>.byId(id: Int) = single { it.appWidgetId == id }

    @Test
    fun `growing without overlap just resizes in place`() {
        val existing = listOf(placement(id = 1, row = 0, col = 0, colSpan = 1, rowSpan = 1))

        val result = useCase(existing, resizingAppWidgetId = 1, row = 0, col = 0, colSpan = 2, rowSpan = 2)

        assertEquals(2, result?.byId(1)?.colSpan)
        assertEquals(2, result?.byId(1)?.rowSpan)
    }

    @Test
    fun `shrinking below the provider's own declared minimum is still accepted — widgets adapt to whatever size the user picks`() {
        val existing = listOf(placement(id = 1, row = 0, col = 0, colSpan = 2, rowSpan = 2))

        val result = useCase(existing, resizingAppWidgetId = 1, row = 0, col = 0, colSpan = 1, rowSpan = 1)

        assertEquals(1, result?.byId(1)?.colSpan)
        assertEquals(1, result?.byId(1)?.rowSpan)
    }

    @Test
    fun `growing into a neighbor pushes it straight down below the new footprint`() {
        // Neighbor sits immediately to the right, same row.
        val existing = listOf(
            placement(id = 1, row = 0, col = 0, colSpan = 1, rowSpan = 1),
            placement(id = 2, row = 0, col = 1, colSpan = 1, rowSpan = 1),
        )

        val result = useCase(existing, resizingAppWidgetId = 1, row = 0, col = 0, colSpan = 2, rowSpan = 1)

        requireNotNull(result)
        assertEquals(0, result.byId(1).row)
        assertEquals(2, result.byId(1).colSpan)
        // Pushed down out of the grown widget's way — not sideways, per the resize-specific rule.
        assertEquals(1, result.byId(2).row)
        assertEquals(1, result.byId(2).col)
    }

    @Test
    fun `pushing a widget down onto another cascades the push further down`() {
        // Widget 2 sits directly below where widget 3 already occupies — pushing 2 down (out of
        // widget 1's growing way) should in turn push 3 down too, not land on top of it.
        val existing = listOf(
            placement(id = 1, row = 0, col = 0, colSpan = 1, rowSpan = 1),
            placement(id = 2, row = 0, col = 1, colSpan = 1, rowSpan = 1),
            placement(id = 3, row = 1, col = 1, colSpan = 1, rowSpan = 1),
        )

        val result = useCase(existing, resizingAppWidgetId = 1, row = 0, col = 0, colSpan = 2, rowSpan = 1)

        requireNotNull(result)
        assertEquals(1, result.byId(2).row)
        assertEquals(2, result.byId(3).row)
    }

    @Test
    fun `growing past the column bound is rejected`() {
        val existing = listOf(placement(id = 1, row = 0, col = HUB_COLUMNS - 1, colSpan = 1, rowSpan = 1))

        val result = useCase(existing, resizingAppWidgetId = 1, row = 0, col = HUB_COLUMNS - 1, colSpan = 2, rowSpan = 1)

        assertNull(result)
    }

    @Test
    fun `a cascade that would run off the grid's row cap is rejected entirely`() {
        val existing = listOf(
            placement(id = 1, row = 0, col = 0, colSpan = 1, rowSpan = 1),
            placement(id = 2, row = 0, col = 1, colSpan = 1, rowSpan = HUB_MAX_ROWS),
        )

        val result = useCase(existing, resizingAppWidgetId = 1, row = 0, col = 0, colSpan = 2, rowSpan = 1)

        assertNull(result)
    }
}
