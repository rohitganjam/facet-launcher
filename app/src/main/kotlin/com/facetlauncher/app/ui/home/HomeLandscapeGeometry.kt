package com.facetlauncher.app.ui.home

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import kotlin.math.abs

/** The clock pane never takes more than this share of the window width. */
internal const val HOME_LANDSCAPE_CLOCK_PANE_MAX_FRACTION = 0.5f

/**
 * Width of the clock pane in px: the saved width (or a default share of [contentWidthPx]), kept
 * between [floorPx] (the clock plus its margins, so the pane never squeezes it) and half the
 * window, so the apps pane always keeps room.
 */
internal fun landscapeClockPaneWidthPx(persistedPx: Float?, contentWidthPx: Float, floorPx: Float): Float {
    val wanted = persistedPx ?: (HOME_LANDSCAPE_CLOCK_PANE_FRACTION * contentWidthPx)
    val ceilingPx = maxOf(floorPx, HOME_LANDSCAPE_CLOCK_PANE_MAX_FRACTION * contentWidthPx)
    return wanted.coerceIn(floorPx, ceilingPx)
}

/**
 * The pane width after one divider drag step. It stops at [floorPx] (the smallest the clock may be
 * scaled to, plus its margins) and at half the window. Clamping the live value, not just what is
 * drawn, keeps the handle under the finger when the drag reverses.
 */
internal fun landscapeDividerDragWidthPx(currentPx: Float, deltaPx: Float, floorPx: Float, contentWidthPx: Float): Float {
    val ceilingPx = maxOf(floorPx, HOME_LANDSCAPE_CLOCK_PANE_MAX_FRACTION * contentWidthPx)
    return (currentPx + deltaPx).coerceIn(floorPx, ceilingPx)
}

/**
 * The largest clock scale that still fits its pane: the clock is scaled in place from its left edge,
 * so it may grow to the pane's inner width ([paneWidthPx] less the margins) and to the pane's height.
 */
internal fun landscapeMaxClockScale(
    naturalSize: Size,
    paneWidthPx: Float,
    paneHeightPx: Float,
    horizontalMarginsPx: Float,
    minScale: Float,
    maxScale: Float,
): Float {
    // Nothing measured yet: don't clamp to a pane of zero size.
    if (naturalSize.width <= 0f || naturalSize.height <= 0f || paneHeightPx <= 0f) return maxScale
    val byWidth = (paneWidthPx - horizontalMarginsPx) / naturalSize.width
    val byHeight = paneHeightPx / naturalSize.height
    return minOf(maxScale, byWidth, byHeight).coerceIn(minScale, maxScale)
}

/**
 * The most a hosted clock widget may be in landscape: the pane's inner width ([paneWidthPx] less the
 * margins) by the pane's height. Unlike the native clock it is resized directly, not scaled.
 */
internal fun landscapeMaxWidgetSize(paneWidthPx: Float, paneHeightPx: Float, horizontalMarginsPx: Float): Size = Size(
    (paneWidthPx - horizontalMarginsPx).coerceAtLeast(0f),
    // Not measured yet: no limit, rather than shrinking the widget to nothing for a frame.
    if (paneHeightPx > 0f) paneHeightPx else Float.POSITIVE_INFINITY,
)

/**
 * The size a hosted widget takes if a touch at [touch] were its dragged corner. The widget keeps its
 * left edge and the vertical edge given by [originY] (0 top, 0.5 middle, 1 bottom) of its current
 * box ([boxTopLeft], [boxSize]), so a top-anchored widget grows downward, a bottom-anchored one
 * upward and a middle-anchored one both ways around its centre.
 */
internal fun landscapeWidgetSizeAtTouch(touch: Offset, boxTopLeft: Offset, boxSize: Size, originY: Float): Size {
    val width = touch.x - boxTopLeft.x
    val anchorY = boxTopLeft.y + boxSize.height * originY
    val height = when {
        originY <= 0f -> touch.y - anchorY
        originY >= 1f -> anchorY - touch.y
        else -> abs(anchorY - touch.y) / originY
    }
    return Size(width, height)
}

/**
 * Where the scaled clock sits. The clock keeps its natural footprint and is scaled around
 * (left edge, [originY]) of it, where [originY] is 0 for a top-aligned clock, 0.5 for middle and 1 for bottom.
 */
internal fun landscapeVisualClockRect(naturalTopLeft: Offset, naturalSize: Size, originY: Float, scale: Float): Rect {
    val height = naturalSize.height * scale
    val top = naturalTopLeft.y + originY * naturalSize.height * (1f - scale)
    return Rect(naturalTopLeft.x, top, naturalTopLeft.x + naturalSize.width * scale, top + height)
}

/**
 * The scale a touch at [touch] implies if it were the dragged corner. Horizontally the clock grows
 * from its left edge. Vertically the dragged corner is the one away from the anchor: the bottom
 * corners for a top-aligned clock, the top corners otherwise.
 */
internal fun landscapeScaleAtTouch(touch: Offset, naturalTopLeft: Offset, naturalSize: Size, originY: Float): Float {
    val scaleX = (touch.x - naturalTopLeft.x) / naturalSize.width
    val scaleY = when {
        originY <= 0f -> (touch.y - naturalTopLeft.y) / naturalSize.height
        originY >= 1f -> (naturalTopLeft.y + naturalSize.height - touch.y) / naturalSize.height
        else -> abs(naturalTopLeft.y + naturalSize.height * originY - touch.y) / (naturalSize.height * originY)
    }
    return (scaleX + scaleY) / 2f
}
