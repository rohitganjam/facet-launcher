package com.lumenlauncher.app.domain

import com.lumenlauncher.app.data.local.WidgetPlacementEntity
import javax.inject.Inject

/**
 * Pure bounds + no-overlap check for moving a widget to a target cell — called synchronously
 * against in-memory state for live drag-preview feedback, then again at commit time against the
 * latest state as the authoritative check before persisting.
 */
class ValidateWidgetMoveUseCase @Inject constructor() {
    operator fun invoke(
        existing: List<WidgetPlacementEntity>,
        movingAppWidgetId: Int,
        targetRow: Int,
        targetCol: Int,
        colSpan: Int,
        rowSpan: Int,
    ): Boolean {
        if (targetRow < 0 || targetCol < 0) return false
        if (targetCol + colSpan > HUB_COLUMNS) return false
        if (targetRow + rowSpan > HUB_MAX_ROWS) return false
        return existing.none { other ->
            other.appWidgetId != movingAppWidgetId &&
                rectanglesOverlap(targetRow, targetCol, colSpan, rowSpan, other.row, other.col, other.colSpan, other.rowSpan)
        }
    }
}
