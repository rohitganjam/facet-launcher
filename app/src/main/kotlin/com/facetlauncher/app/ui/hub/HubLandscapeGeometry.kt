package com.facetlauncher.app.ui.hub

import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlin.math.floor
import kotlin.math.roundToInt

/** Landscape Hub's cell is a fixed size, so a widget keeps the same size on any window, as the app grid's cells do. */
internal val HUB_LANDSCAPE_CELL_SIZE = 64.dp

/** Never fewer rows than this, so even a very short window has something to put widgets in. */
internal const val HUB_LANDSCAPE_MIN_ROWS = 2

/** How many whole rows of [cell] with [gap] between them fit in [availableHeight]. */
internal fun hubLandscapeRows(availableHeight: Dp, cell: Dp = HUB_LANDSCAPE_CELL_SIZE, gap: Dp): Int {
    val rows = floor((availableHeight + gap).value / (cell + gap).value).toInt()
    return rows.coerceAtLeast(HUB_LANDSCAPE_MIN_ROWS)
}

/** The size of [span] cells including the gaps between them. */
internal fun hubLandscapeSpanSize(span: Int, cell: Dp = HUB_LANDSCAPE_CELL_SIZE, gap: Dp): Dp = cell * span + gap * (span - 1)

/** Where the cell at [index] starts: cells plus gaps before it. */
internal fun hubLandscapeCellOrigin(index: Int, cell: Dp = HUB_LANDSCAPE_CELL_SIZE, gap: Dp): Dp = (cell + gap) * index

/** A grid cell, by row and column. */
internal data class HubCell(val row: Int, val col: Int)

/**
 * The cell a dragged widget's top-left corner is over: its own cell moved by the drag, rounded to
 * whole cells, kept inside the grid's [rows] (the widget never hangs off the bottom) and never left of column 0.
 */
internal fun hubLandscapeDropCell(
    widgetRow: Int,
    widgetCol: Int,
    widgetRowSpan: Int,
    dragX: Float,
    dragY: Float,
    pitchX: Float,
    pitchY: Float,
    rows: Int,
): HubCell {
    val col = (widgetCol + (dragX / pitchX).roundToInt()).coerceAtLeast(0)
    val maxRow = (rows - widgetRowSpan).coerceAtLeast(0)
    val row = (widgetRow + (dragY / pitchY).roundToInt()).coerceIn(0, maxRow)
    return HubCell(row, col)
}
