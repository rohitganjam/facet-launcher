package com.lumenlauncher.app.domain

import com.lumenlauncher.app.data.local.WidgetPlacementEntity
import javax.inject.Inject

/**
 * Pure bounds + no-overlap check for resizing a widget in place (its top-left cell never moves —
 * only [newColSpan]/[newRowSpan] change). Deliberately does not enforce a provider's declared
 * `minResizeWidth`/`minResizeHeight` — widgets are free to shrink to whatever size the user picks
 * and adapt their own layout, down to the 1-cell-per-dimension floor the caller already coerces to.
 */
class ResizeWidgetUseCase @Inject constructor() {
    operator fun invoke(
        existing: List<WidgetPlacementEntity>,
        resizingAppWidgetId: Int,
        row: Int,
        col: Int,
        newColSpan: Int,
        newRowSpan: Int,
    ): Boolean {
        if (col + newColSpan > HUB_COLUMNS) return false
        if (row + newRowSpan > HUB_MAX_ROWS) return false
        return existing.none { other ->
            other.appWidgetId != resizingAppWidgetId &&
                rectanglesOverlap(row, col, newColSpan, newRowSpan, other.row, other.col, other.colSpan, other.rowSpan)
        }
    }
}
