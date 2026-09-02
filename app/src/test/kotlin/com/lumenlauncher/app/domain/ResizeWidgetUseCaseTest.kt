package com.lumenlauncher.app.domain

import com.lumenlauncher.app.data.local.WidgetPlacementEntity
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ResizeWidgetUseCaseTest {

    private val useCase = ResizeWidgetUseCase()

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
    fun `growing without overlap is accepted`() {
        val existing = listOf(placement(id = 1, row = 0, col = 0, colSpan = 1, rowSpan = 1))

        val result = useCase(existing, resizingAppWidgetId = 1, row = 0, col = 0, newColSpan = 2, newRowSpan = 2)

        assertTrue(result)
    }

    @Test
    fun `shrinking below the provider's own declared minimum is still accepted — widgets adapt to whatever size the user picks`() {
        val existing = listOf(placement(id = 1, row = 0, col = 0, colSpan = 2, rowSpan = 2))

        val result = useCase(existing, resizingAppWidgetId = 1, row = 0, col = 0, newColSpan = 1, newRowSpan = 1)

        assertTrue(result)
    }

    @Test
    fun `growing into a neighboring widget's cells is rejected`() {
        // Given a neighbor immediately to the right
        val existing = listOf(
            placement(id = 1, row = 0, col = 0, colSpan = 1, rowSpan = 1),
            placement(id = 2, row = 0, col = 1, colSpan = 1, rowSpan = 1),
        )

        // When widget 1 tries to grow into widget 2's cell
        val result = useCase(existing, resizingAppWidgetId = 1, row = 0, col = 0, newColSpan = 2, newRowSpan = 1)

        assertFalse(result)
    }

    @Test
    fun `growing past the column bound is rejected`() {
        val existing = listOf(placement(id = 1, row = 0, col = HUB_COLUMNS - 1, colSpan = 1, rowSpan = 1))

        val result = useCase(
            existing, resizingAppWidgetId = 1, row = 0, col = HUB_COLUMNS - 1,
            newColSpan = 2, newRowSpan = 1,
        )

        assertFalse(result)
    }
}
