package com.lumenlauncher.app.domain

import com.lumenlauncher.app.data.local.WidgetPlacementEntity
import javax.inject.Inject

/**
 * Resolves where a dragged widget's drop lands. An empty target rectangle is a plain move; one
 * that overlaps other widgets displaces each of them out of the way instead of just rejecting the
 * drop — first to its own right, then straight down if the right doesn't fit — matching how
 * reordering a grid of icons is expected to behave (see chat history). Returns the full resolved
 * placement list (the moved widget plus any displaced ones, everyone else unchanged), or `null`
 * if even displacement can't make room — the caller leaves everything as-is in that case.
 */
class ResolveWidgetDropUseCase @Inject constructor() {
    operator fun invoke(
        existing: List<WidgetPlacementEntity>,
        movingAppWidgetId: Int,
        targetRow: Int,
        targetCol: Int,
        colSpan: Int,
        rowSpan: Int,
    ): List<WidgetPlacementEntity>? {
        if (targetRow < 0 || targetCol < 0) return null
        if (targetCol + colSpan > HUB_COLUMNS) return null
        if (targetRow + rowSpan > HUB_MAX_ROWS) return null
        val moving = existing.find { it.appWidgetId == movingAppWidgetId } ?: return null
        val movedWidget = moving.copy(row = targetRow, col = targetCol, colSpan = colSpan, rowSpan = rowSpan)

        val others = existing.filter { it.appWidgetId != movingAppWidgetId }
        val resolved = others.associateByTo(LinkedHashMap()) { it.appWidgetId }
        val overlapping = others.filter {
            rectanglesOverlap(targetRow, targetCol, colSpan, rowSpan, it.row, it.col, it.colSpan, it.rowSpan)
        }

        for (widget in overlapping) {
            val othersToAvoid = resolved.values.filter { it.appWidgetId != widget.appWidgetId }
            val displaced = displace(widget, movedWidget, othersToAvoid) ?: return null
            resolved[widget.appWidgetId] = displaced
        }

        return resolved.values.toList() + movedWidget
    }

    /** Tries pushing the widget to its own right (out from under [blocker]) first, then straight down; `null` if neither fits. */
    private fun displace(widget: WidgetPlacementEntity, blocker: WidgetPlacementEntity, othersToAvoid: List<WidgetPlacementEntity>): WidgetPlacementEntity? {
        val pushedRight = widget.copy(col = blocker.col + blocker.colSpan)
        if (fits(pushedRight, othersToAvoid)) return pushedRight

        val pushedDown = widget.copy(row = blocker.row + blocker.rowSpan)
        if (fits(pushedDown, othersToAvoid)) return pushedDown

        return null
    }

    private fun fits(candidate: WidgetPlacementEntity, others: List<WidgetPlacementEntity>): Boolean {
        if (candidate.row < 0 || candidate.col < 0) return false
        if (candidate.col + candidate.colSpan > HUB_COLUMNS) return false
        if (candidate.row + candidate.rowSpan > HUB_MAX_ROWS) return false
        return others.none {
            rectanglesOverlap(candidate.row, candidate.col, candidate.colSpan, candidate.rowSpan, it.row, it.col, it.colSpan, it.rowSpan)
        }
    }
}
