package com.facetlauncher.app.ui.drawer

import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.facetlauncher.app.data.model.DrawerGridDensity
import kotlin.math.roundToInt

private const val MIN_COLUMNS = 3
private const val MIN_ROWS = 2

/** Ideal cell width per density; the real width stretches to fill the available space. */
private fun DrawerGridDensity.targetCellWidth(): Dp = when (this) {
    DrawerGridDensity.COMPACT -> 50.dp
    DrawerGridDensity.REGULAR -> 60.dp
    DrawerGridDensity.SPACIOUS -> 80.dp
}

/** The widest a tile's one-line label runs: the cell less a 4dp gap to its neighbour. */
internal fun DrawerGridDensity.labelWidth(): Dp = targetCellWidth() - 4.dp

/** Ideal row pitch (tile plus vertical gap) per density. */
private fun DrawerGridDensity.targetRowPitch(): Dp = when (this) {
    DrawerGridDensity.COMPACT -> 92.dp
    DrawerGridDensity.REGULAR -> 108.dp
    DrawerGridDensity.SPACIOUS -> 120.dp
}

/** Columns that fit [availableWidth] at [density]'s ideal cell width, never fewer than [MIN_COLUMNS]. */
internal fun drawerGridColumns(availableWidth: Dp, density: DrawerGridDensity): Int =
    (availableWidth / density.targetCellWidth()).roundToInt().coerceAtLeast(MIN_COLUMNS)

/** Rows that fit [availableHeight] at [density]'s ideal row pitch, never fewer than [MIN_ROWS]. */
internal fun drawerGridRows(availableHeight: Dp, density: DrawerGridDensity): Int =
    (availableHeight / density.targetRowPitch()).roundToInt().coerceAtLeast(MIN_ROWS)
