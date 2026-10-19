package com.facetlauncher.app.domain

import javax.inject.Inject

/** A widget's id and size in cells, in the order it should appear in the landscape Hub. */
data class WidgetSize(val id: Int, val colSpan: Int, val rowSpan: Int)

data class PackedWidget(val id: Int, val row: Int, val col: Int, val colSpan: Int, val rowSpan: Int)

/**
 * Lays widgets out for the landscape Hub: a grid with a fixed number of [rows] and as many columns
 * as it takes, filled column by column. Each widget goes at the first free spot at or after the
 * previous widget's spot, so the visual order always matches the list order — a later, smaller
 * widget never back-fills a gap left before an earlier one. A widget taller than [rows] is clamped
 * to [rows]. The result never overlaps and is derived, so it needs no storage beyond the order.
 */
class FlowPackWidgetsUseCase @Inject constructor() {
    operator fun invoke(widgets: List<WidgetSize>, rows: Int): List<PackedWidget> {
        val gridRows = rows.coerceAtLeast(1)
        val packed = mutableListOf<PackedWidget>()
        var cursorCol = 0
        var cursorRow = 0
        for (widget in widgets) {
            val colSpan = widget.colSpan.coerceAtLeast(1)
            val rowSpan = widget.rowSpan.coerceIn(1, gridRows)
            var col = cursorCol
            var row = cursorRow
            while (true) {
                if (row + rowSpan > gridRows) {
                    col++
                    row = 0
                } else if (packed.none { rectanglesOverlap(row, col, colSpan, rowSpan, it.row, it.col, it.colSpan, it.rowSpan) }) {
                    break
                } else {
                    row++
                }
            }
            packed += PackedWidget(widget.id, row, col, colSpan, rowSpan)
            cursorCol = col
            cursorRow = row
        }
        return packed
    }
}
