package com.lumenlauncher.app.domain

import com.lumenlauncher.app.data.local.WidgetPlacementEntity
import javax.inject.Inject

/**
 * Resolves a widget resize whose new rectangle overlaps other widgets by pushing each of them
 * straight down out of the way, rather than rejecting the resize outright. Unlike
 * [ResolveWidgetDropUseCase]'s move-and-displace (which prefers right, then down), a resize is
 * growing one widget's own footprint in place, so anything in its way has nowhere to go but
 * below it. Pushing one widget down can land it on top of another, which gets pushed further
 * down in turn — that cascade repeats per widget until nothing overlaps. Returns the full
 * resolved placement list (the resized widget plus any displaced ones, everyone else
 * unchanged), or `null` if the cascade would run a widget off the grid's row cap, in which case
 * the caller leaves everything as-is.
 */
class ResolveWidgetResizeUseCase @Inject constructor() {
    operator fun invoke(
        existing: List<WidgetPlacementEntity>,
        resizingAppWidgetId: Int,
        row: Int,
        col: Int,
        colSpan: Int,
        rowSpan: Int,
    ): List<WidgetPlacementEntity>? {
        if (row < 0 || col < 0) return null
        if (col + colSpan > HUB_COLUMNS) return null
        if (row + rowSpan > HUB_MAX_ROWS) return null
        val resizing = existing.find { it.appWidgetId == resizingAppWidgetId } ?: return null
        val resized = resizing.copy(row = row, col = col, colSpan = colSpan, rowSpan = rowSpan)

        // Settled widgets are fixed at their final position; pending ones haven't been touched
        // yet. Repeatedly pick any pending widget that overlaps something already settled (its
        // own original spot might now sit under a widget that was just pushed down) and settle
        // it too — this is what lets a push cascade through several widgets, not just the one
        // directly under the resize.
        val settled = mutableListOf(resized)
        val pending = existing.filter { it.appWidgetId != resizingAppWidgetId }.toMutableList()
        while (true) {
            val next = pending.firstOrNull { candidate -> settled.any { rectanglesOverlap(candidate, it) } } ?: break
            pending.remove(next)
            val newRow = pushedDownRow(next, settled) ?: return null
            settled += next.copy(row = newRow)
        }
        settled += pending

        return settled
    }

    /**
     * Drops [widget] straight down from its own current row until it clears every [settled]
     * rectangle in its column range — jumping to just below whichever it lands on and rechecking,
     * since that new spot can itself land on a different settled widget further down. `null` if
     * this would push it past the grid's row cap.
     */
    private fun pushedDownRow(widget: WidgetPlacementEntity, settled: List<WidgetPlacementEntity>): Int? {
        var candidateRow = widget.row
        while (true) {
            val blockerBottom = settled
                .filter { rectanglesOverlap(candidateRow, widget.col, widget.colSpan, widget.rowSpan, it.row, it.col, it.colSpan, it.rowSpan) }
                .maxOfOrNull { it.row + it.rowSpan }
                ?: return candidateRow
            if (blockerBottom + widget.rowSpan > HUB_MAX_ROWS) return null
            candidateRow = blockerBottom
        }
    }

    private fun rectanglesOverlap(a: WidgetPlacementEntity, b: WidgetPlacementEntity): Boolean =
        rectanglesOverlap(a.row, a.col, a.colSpan, a.rowSpan, b.row, b.col, b.colSpan, b.rowSpan)
}
