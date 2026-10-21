package com.facetlauncher.app.domain

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class FlowPackWidgetsUseCaseTest {

    private val useCase = FlowPackWidgetsUseCase()

    private fun size(id: Int, colSpan: Int = 1, rowSpan: Int = 1) = WidgetSize(id, colSpan, rowSpan)

    private fun List<PackedWidget>.find(id: Int) = single { it.id == id }

    @Test
    fun `no widgets pack to nothing`() {
        assertEquals(emptyList<PackedWidget>(), useCase(emptyList(), rows = 5))
    }

    @Test
    fun `the first widget sits in the top left cell`() {
        val result = useCase(listOf(size(1)), rows = 5)

        assertEquals(PackedWidget(id = 1, row = 0, col = 0, colSpan = 1, rowSpan = 1), result.single())
    }

    @Test
    fun `widgets fill a column top to bottom then wrap to the next column`() {
        // Given three rows and four single-cell widgets
        val result = useCase((1..4).map { size(it) }, rows = 3)

        // Then the first three stack in column 0 and the fourth starts column 1
        assertEquals(listOf(0, 1, 2), (1..3).map { result.find(it).row })
        assertEquals(listOf(0, 0, 0), (1..3).map { result.find(it).col })
        assertEquals(0, result.find(4).row)
        assertEquals(1, result.find(4).col)
    }

    @Test
    fun `a wide widget leaves the cells under it for the next widget`() {
        // Given a two-column, one-row widget followed by a single cell, in a two-row grid
        val result = useCase(listOf(size(1, colSpan = 2), size(2)), rows = 2)

        // Then the second widget sits right under the first
        assertEquals(1, result.find(2).row)
        assertEquals(0, result.find(2).col)
    }

    @Test
    fun `a later small widget does not back-fill a gap before an earlier one`() {
        // Given three rows: a two-tall widget, another two-tall widget (which has to start column 1), then a single cell
        val result = useCase(listOf(size(1, rowSpan = 2), size(2, rowSpan = 2), size(3)), rows = 3)

        // Then widget 3 follows widget 2 in column 1, not up in the free cell under widget 1
        assertEquals(1, result.find(3).col)
        assertEquals(2, result.find(3).row)
    }

    @Test
    fun `a widget taller than the grid is clamped to the grid height`() {
        val result = useCase(listOf(size(1, rowSpan = 6)), rows = 4)

        assertEquals(4, result.single().rowSpan)
    }

    @Test
    fun `a tiny row count still gives a usable grid`() {
        val result = useCase(listOf(size(1), size(2)), rows = 0)

        assertEquals(listOf(0 to 0, 0 to 1), result.map { it.row to it.col })
    }

    @Test
    fun `output keeps the input order and ids`() {
        val result = useCase(listOf(size(7), size(3), size(9)), rows = 2)

        assertEquals(listOf(7, 3, 9), result.map { it.id })
    }

    @Test
    fun `a mixed set never overlaps and stays inside the rows`() {
        // Given a varied set of sizes
        val widgets = listOf(
            size(1, colSpan = 2, rowSpan = 2), size(2, colSpan = 1, rowSpan = 3), size(3, colSpan = 3, rowSpan = 1),
            size(4, colSpan = 2, rowSpan = 2), size(5), size(6, colSpan = 4, rowSpan = 2), size(7, colSpan = 1, rowSpan = 4),
        )

        val result = useCase(widgets, rows = 4)

        // Then every widget is inside the grid and no two overlap
        assertTrue(result.all { it.row >= 0 && it.col >= 0 && it.row + it.rowSpan <= 4 })
        for (a in result) for (b in result) {
            if (a.id < b.id) {
                assertTrue("${a.id} overlaps ${b.id}", !rectanglesOverlap(a.row, a.col, a.colSpan, a.rowSpan, b.row, b.col, b.colSpan, b.rowSpan))
            }
        }
    }
}
