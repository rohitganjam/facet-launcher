package com.facetlauncher.app.ui.home

/**
 * The highest portrait's height handle may go, in px from the top of the content area.
 *
 * The clock block keeps its full natural height ([blockHeightPx]) above the handle even when the clock
 * is scaled down, so limiting the handle to that footprint left a small clock's headroom unusable.
 * When the native clock can shrink ([canShrink]) the handle may instead go up until the clock at its
 * smallest scale ([minScale]) plus [headroomPx] (status bar and resize handles) fits above it: the
 * clock shrinks live to make room. Whatever sits below the clock box (blockHeightPx - [clockBoxHeightPx])
 * keeps its height. A hosted widget can't shrink that way, so it keeps its whole block.
 */
internal fun clockHandleFloorPx(
    blockHeightPx: Float,
    clockBoxHeightPx: Float,
    headroomPx: Float,
    minGapPx: Float,
    minScale: Float,
    canShrink: Boolean,
): Float {
    val wholeFootprintPx = blockHeightPx + minGapPx
    if (!canShrink) return wholeFootprintPx
    val shrunkPx = (blockHeightPx - clockBoxHeightPx) + clockBoxHeightPx * minScale + headroomPx + minGapPx
    return minOf(wholeFootprintPx, shrunkPx)
}
