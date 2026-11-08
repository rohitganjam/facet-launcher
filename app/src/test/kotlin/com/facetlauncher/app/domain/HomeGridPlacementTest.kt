package com.facetlauncher.app.domain

import com.facetlauncher.app.data.model.AppListVerticalAlignment
import org.junit.Assert.assertEquals
import org.junit.Assert.assertSame
import org.junit.Test

class HomeGridPlacementTest {

    private fun entry(key: String, saved: GridCell? = null) = GridEntry(key, saved)

    @Test
    fun `items without a saved cell fill the grid row by row from the anchor`() {
        // Given five unplaced items on a four-column grid
        val cells = HomeGridPlacement.resolve(listOf("a", "b", "c", "d", "e").map(::entry), columns = 4)

        // Then they read left to right, then on to the next row
        assertEquals(GridCell(0, 0), cells["a"])
        assertEquals(GridCell(0, 3), cells["d"])
        assertEquals(GridCell(1, 0), cells["e"])
    }

    @Test
    fun `a saved cell is kept and the others fill the gaps around it`() {
        // Given the first item saved on cell 0,2 and two more unplaced
        val cells = HomeGridPlacement.resolve(listOf(entry("a", GridCell(0, 2)), entry("b"), entry("c")), columns = 4)

        // Then the unplaced ones take the first free cells
        assertEquals(GridCell(0, 2), cells["a"])
        assertEquals(GridCell(0, 0), cells["b"])
        assertEquals(GridCell(0, 1), cells["c"])
    }

    @Test
    fun `two items saved on one cell keep the first and move the second`() {
        val cells = HomeGridPlacement.resolve(listOf(entry("a", GridCell(0, 0)), entry("b", GridCell(0, 0))), columns = 3)

        assertEquals(GridCell(0, 0), cells["a"])
        assertEquals(GridCell(0, 1), cells["b"])
    }

    @Test
    fun `a saved cell outside the columns is placed again`() {
        // Given a cell in column 5 on a grid that is now only four wide
        val cells = HomeGridPlacement.resolve(listOf(entry("a", GridCell(1, 5))), columns = 4)

        assertEquals(GridCell(0, 0), cells["a"])
    }

    @Test
    fun `rows cover every occupied row and at least the minimum`() {
        val cells = listOf(GridCell(0, 0), GridCell(3, 1))

        assertEquals(4, HomeGridPlacement.rowCount(cells, minimumRows = 2))
        assertEquals(6, HomeGridPlacement.rowCount(cells, minimumRows = 6))
        assertEquals(3, HomeGridPlacement.rowCount(emptyList(), minimumRows = 3))
    }

    @Test
    fun `while dragging there is one empty row below the last occupied one`() {
        val cells = listOf(GridCell(0, 0), GridCell(3, 1))

        assertEquals(5, HomeGridPlacement.rowCountWhileDragging(cells, minimumRows = 2))
        assertEquals(6, HomeGridPlacement.rowCountWhileDragging(cells, minimumRows = 6))
    }

    @Test
    fun `anchoring at the bottom mirrors the screen rows`() {
        assertEquals(0, HomeGridPlacement.screenRow(0, rowCount = 5, anchoredAtBottom = false))
        assertEquals(4, HomeGridPlacement.screenRow(0, rowCount = 5, anchoredAtBottom = true))
        assertEquals(2, HomeGridPlacement.anchorRow(2, rowCount = 5, anchoredAtBottom = true))
        assertEquals(1, HomeGridPlacement.anchorRow(3, rowCount = 5, anchoredAtBottom = true))
    }

    @Test
    fun `dropping on an empty cell moves the item`() {
        val cells = mapOf<Any, GridCell>("a" to GridCell(0, 0), "b" to GridCell(0, 1))

        val moved = HomeGridPlacement.drop(cells, "a", GridCell(2, 3))

        assertEquals(GridCell(2, 3), moved["a"])
        assertEquals(GridCell(0, 1), moved["b"])
    }

    @Test
    fun `dropping on an occupied cell swaps the two items`() {
        val cells = mapOf<Any, GridCell>("a" to GridCell(0, 0), "b" to GridCell(0, 1))

        val moved = HomeGridPlacement.drop(cells, "a", GridCell(0, 1))

        assertEquals(GridCell(0, 1), moved["a"])
        assertEquals(GridCell(0, 0), moved["b"])
    }

    @Test
    fun `dropping back on its own cell changes nothing`() {
        val cells = mapOf<Any, GridCell>("a" to GridCell(0, 0))

        assertSame(cells, HomeGridPlacement.drop(cells, "a", GridCell(0, 0)))
    }

    @Test
    fun `narrowing the grid moves outside items to free cells without overlap`() {
        // Given items on a five-column grid, two of them in columns beyond a three-wide grid
        val cells = mapOf<Any, GridCell>("a" to GridCell(0, 0), "b" to GridCell(0, 4), "c" to GridCell(0, 2), "d" to GridCell(0, 3))

        // When it narrows to three columns
        val compact = HomeGridPlacement.compact(cells, columns = 3)

        // Then the outside ones take the next free cells, in their reading order
        assertEquals(GridCell(0, 0), compact["a"])
        assertEquals(GridCell(0, 2), compact["c"])
        assertEquals(GridCell(0, 1), compact["d"])
        assertEquals(GridCell(1, 0), compact["b"])
        assertEquals(4, compact.values.toSet().size)
    }

    @Test
    fun `the reading order is row by row from the anchor`() {
        val cells = mapOf<Any, GridCell>("c" to GridCell(1, 0), "a" to GridCell(0, 2), "b" to GridCell(0, 3))

        assertEquals(listOf<Any>("a", "b", "c"), HomeGridPlacement.readingOrder(cells))
    }

    @Test
    fun `the first items start at the edge the alignment names`() {
        // Given three items on a grid five rows tall, none placed yet
        val cells = HomeGridPlacement.resolve(listOf("a", "b", "c").map(::entry), columns = 4)
        val rows = 5

        // Then a top alignment starts them on the top screen row, left to right
        assertEquals(0, HomeGridPlacement.screenRow(cells.getValue("a").row, rows, AppListVerticalAlignment.TOP))
        // And a bottom alignment starts them on the bottom screen row, in the same left to right order
        assertEquals(4, HomeGridPlacement.screenRow(cells.getValue("a").row, rows, AppListVerticalAlignment.BOTTOM))
        assertEquals(listOf(0, 1, 2), listOf("a", "b", "c").map { cells.getValue(it).column })
    }

    @Test
    fun `a later row grows away from the anchor`() {
        // Given a fifth item that spills to the second row
        val cells = HomeGridPlacement.resolve(listOf("a", "b", "c", "d", "e").map(::entry), columns = 4)
        val rows = HomeGridPlacement.rowCount(cells.values, minimumRows = 5)

        // Then it sits one row below the first from a top anchor, and one row above it from a bottom anchor
        assertEquals(1, HomeGridPlacement.screenRow(cells.getValue("e").row, rows, AppListVerticalAlignment.TOP))
        assertEquals(3, HomeGridPlacement.screenRow(cells.getValue("e").row, rows, AppListVerticalAlignment.BOTTOM))
    }
}
