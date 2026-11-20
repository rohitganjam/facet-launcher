package com.facetlauncher.app.ui.home

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.unit.dp

/**
 * Where the native clock's sized box is on screen, in the root's own coordinates, or `null` when the clock has never been sized, is not
 * the page showing, or is not measured yet. The box keeps the clock's aligned edge and its bottom (portrait) or its left edge and its
 * top, middle or bottom anchor (landscape), the same as the adjust frame.
 */
internal fun HomeFrame.clockBoxRect(): Rect? {
    val clock = state.clock
    val widthDp = clock.boxWidthDp ?: return null
    val heightDp = clock.boxHeightDp ?: return null
    if (state.widget != null || state.clockStack.isBlank) return null
    val natural = layout.measureClockBox() ?: return null
    val size = with(density) { Size(widthDp.dp.toPx(), heightDp.dp.toPx()) }
    val naturalHeight = natural.size.height.toFloat()
    return if (isLandscape) {
        val originY = geometry.landscape.originY
        val anchorY = natural.topLeft.y + naturalHeight * originY
        Rect(Offset(natural.topLeft.x, anchorY - originY * size.height), size)
    } else {
        val fraction = clock.placement.alignment.originFraction()
        val anchorX = natural.topLeft.x + natural.size.width * fraction
        Rect(Offset(anchorX - size.width * fraction, natural.topLeft.y + naturalHeight - size.height), size)
    }
}

/** Whether a touch at [position] (root coordinates) is inside the clock's sized box, even where nothing is drawn. */
internal fun HomeFrame.clockBoxContains(position: Offset): Boolean = clockBoxRect()?.contains(position) == true
