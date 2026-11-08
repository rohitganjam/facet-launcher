package com.facetlauncher.app.domain

import com.facetlauncher.app.data.model.AppListVerticalAlignment

/** A Home grid cell, counted from the anchor edge: row 0 is the row next to the anchor (the top, or the bottom when anchored there). */
data class GridCell(val row: Int, val column: Int)

/** An item to place: [key] identifies it, [saved] is the cell it was last moved to (null until then), and the list order breaks ties. */
data class GridEntry<K>(val key: K, val saved: GridCell?)

/**
 * Where each item sits on Home's grid. Pure, so the rules are tested without a screen: a saved cell is kept while it is
 * inside the columns and free, and every other item takes the first free cell from the anchor, in list order.
 */
object HomeGridPlacement {
    /** Cells for [entries] (in list order) on a grid [columns] wide. */
    fun resolve(entries: List<GridEntry<out Any>>, columns: Int): Map<Any, GridCell> {
        val width = columns.coerceAtLeast(1)
        val placed = LinkedHashMap<Any, GridCell>()
        val taken = HashSet<GridCell>()
        for (entry in entries) {
            val cell = entry.saved ?: continue
            if (cell.column in 0 until width && cell.row >= 0 && taken.add(cell)) placed[entry.key] = cell
        }
        for (entry in entries) {
            if (entry.key in placed) continue
            val cell = firstFree(taken, width)
            taken += cell
            placed[entry.key] = cell
        }
        // Back in list order, so callers can iterate the result as the list.
        return entries.associate { it.key to placed.getValue(it.key) }
    }

    /** The first cell, scanning row by row from the anchor, that is not in [taken]. */
    fun firstFree(taken: Set<GridCell>, columns: Int): GridCell {
        val width = columns.coerceAtLeast(1)
        var index = 0
        while (true) {
            val cell = GridCell(index / width, index % width)
            if (cell !in taken) return cell
            index++
        }
    }

    /** Rows to draw: at least [minimumRows] (what fits on screen), and every occupied row. */
    fun rowCount(cells: Collection<GridCell>, minimumRows: Int): Int =
        maxOf(minimumRows, (cells.maxOfOrNull { it.row } ?: -1) + 1)

    /** Rows to draw while dragging: one empty row below the last occupied one, so an item can go lower. */
    fun rowCountWhileDragging(cells: Collection<GridCell>, minimumRows: Int): Int =
        maxOf(minimumRows, (cells.maxOfOrNull { it.row } ?: -1) + 2)

    /** The screen row (0 at the top) for a cell [row] counted from the anchor, on a grid [rowCount] rows tall. */
    fun screenRow(row: Int, rowCount: Int, anchoredAtBottom: Boolean): Int = if (anchoredAtBottom) rowCount - 1 - row else row

    /** Where a cell lands on screen for the facet's own top or bottom alignment: row 0 is next to the edge the list is anchored to. */
    fun screenRow(row: Int, rowCount: Int, alignment: AppListVerticalAlignment): Int =
        screenRow(row, rowCount, alignment == AppListVerticalAlignment.BOTTOM)

    /** The inverse of [screenRow]. */
    fun anchorRow(screenRow: Int, rowCount: Int, anchoredAtBottom: Boolean): Int = screenRow(screenRow, rowCount, anchoredAtBottom)

    /** Dropping [moved] on [target]: an empty cell takes it, an occupied one swaps places. Returns the new cells, or the same map if nothing changes. */
    fun drop(cells: Map<Any, GridCell>, moved: Any, target: GridCell): Map<Any, GridCell> {
        val from = cells[moved] ?: return cells
        if (from == target || target.row < 0 || target.column < 0) return cells
        val occupant = cells.entries.firstOrNull { it.value == target }?.key
        return cells.toMutableMap().apply {
            this[moved] = target
            if (occupant != null) this[occupant] = from
        }
    }

    /**
     * Drops the cells that no longer fit when the grid narrows to [columns]: each item outside moves to the first free cell,
     * in its old reading order, so nothing is lost and nothing overlaps.
     */
    fun compact(cells: Map<Any, GridCell>, columns: Int): Map<Any, GridCell> {
        val width = columns.coerceAtLeast(1)
        val ordered = cells.entries.sortedWith(compareBy({ it.value.row }, { it.value.column }))
        val taken = ordered.map { it.value }.filter { it.column < width }.toMutableSet()
        val result = LinkedHashMap<Any, GridCell>()
        for ((key, cell) in ordered) {
            result[key] = if (cell.column < width) cell else firstFree(taken, width).also { taken += it }
        }
        return cells.keys.associateWith { result.getValue(it) }
    }

    /** The list order a set of cells reads as, row by row from the anchor: what `position` is rewritten to. */
    fun readingOrder(cells: Map<Any, GridCell>): List<Any> =
        cells.entries.sortedWith(compareBy({ it.value.row }, { it.value.column })).map { it.key }
}
