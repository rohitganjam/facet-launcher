package com.facetlauncher.app.ui.drawer

import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.facetlauncher.app.data.model.DrawerGridDensity
import kotlin.math.roundToInt

private const val MIN_COLUMNS = 3
private const val MIN_ROWS = 2

/** Ideal cell width per density; the real width stretches to fill the available space. */
private fun DrawerGridDensity.targetCellWidth(): Dp = when (this) {
    DrawerGridDensity.COMPACT -> 56.dp
    DrawerGridDensity.REGULAR -> 64.dp
    DrawerGridDensity.SPACIOUS -> 80.dp
}

/** Ideal row pitch (tile plus vertical gap) per density. */
internal fun DrawerGridDensity.targetRowPitch(): Dp = when (this) {
    DrawerGridDensity.COMPACT -> 92.dp
    DrawerGridDensity.REGULAR -> 108.dp
    DrawerGridDensity.SPACIOUS -> 120.dp
}

/** Whole columns that fit [availableWidth] at [density]'s cell width, so a cell is never narrower than that; at least [MIN_COLUMNS]. */
internal fun drawerGridColumns(availableWidth: Dp, density: DrawerGridDensity): Int =
    (availableWidth / density.targetCellWidth()).toInt().coerceAtLeast(MIN_COLUMNS)

/** Rows that fit [availableHeight] at [density]'s ideal row pitch, never fewer than [MIN_ROWS]. */
internal fun drawerGridRows(availableHeight: Dp, density: DrawerGridDensity): Int =
    (availableHeight / density.targetRowPitch()).roundToInt().coerceAtLeast(MIN_ROWS)
