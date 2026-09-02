package com.lumenlauncher.app.domain

import android.content.Context

/** Hub's grid is fixed at 5 columns (matching `DrawerGridSize.FIVE_BY_SIX`'s column count). */
const val HUB_COLUMNS = 5

/** Rows grow as widgets are added, up to this scroll cap (F5). */
const val HUB_MAX_ROWS = 50

/** F5's widget cap. */
const val HUB_MAX_WIDGETS = 20

/**
 * The standard horizontal padding around the Hub grid (from `HubGrid.kt`).
 * Total deduction is 24dp * 2 = 48dp.
 */
private const val HUB_HORIZONTAL_PADDING_TOTAL_DP = 48

/**
 * The gap between cells in the Hub grid (from `HubGrid.kt`).
 * For 5 columns, there are 4 gaps. 8dp * 4 = 32dp.
 */
private const val HUB_TOTAL_GAP_DP = 32

/**
 * Calculates the width of a single Hub cell in dp based on the screen width.
 * The Hub grid is designed to be square, so this value also serves as the cell height.
 */
fun calculateHubCellWidth(context: Context): Int {
    val screenWidthDp = context.resources.configuration.screenWidthDp
    return (screenWidthDp - HUB_HORIZONTAL_PADDING_TOTAL_DP - HUB_TOTAL_GAP_DP) / HUB_COLUMNS
}

/** Shared rectangle-overlap check for the Hub's grid — two axis-aligned spans overlap iff both their row and column ranges do. */
internal fun rectanglesOverlap(
    row1: Int,
    col1: Int,
    colSpan1: Int,
    rowSpan1: Int,
    row2: Int,
    col2: Int,
    colSpan2: Int,
    rowSpan2: Int,
): Boolean {
    val rowsOverlap = row1 < row2 + rowSpan2 && row2 < row1 + rowSpan1
    val colsOverlap = col1 < col2 + colSpan2 && col2 < col1 + colSpan1
    return rowsOverlap && colsOverlap
}
