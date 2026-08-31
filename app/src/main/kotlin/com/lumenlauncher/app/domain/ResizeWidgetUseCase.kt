package com.lumenlauncher.app.domain

import com.lumenlauncher.app.data.local.WidgetPlacementEntity
import javax.inject.Inject

/**
 * Pure bounds + minimum-size + no-overlap check for resizing a widget in place (its top-left cell
 * never moves — only [newColSpan]/[newRowSpan] change). [minColSpan]/[minRowSpan] are the
 * provider's declared `minResizeWidth`/`minResizeHeight` already converted to grid cells by the
 * caller (cell size is a runtime/UI concern — this stays framework-agnostic per `CLAUDE.md`).
 */
class ResizeWidgetUseCase @Inject constructor() {
    operator fun invoke(
        existing: List<WidgetPlacementEntity>,
        resizingAppWidgetId: Int,
        row: Int,
        col: Int,
        newColSpan: Int,
        newRowSpan: Int,
        minColSpan: Int,
        minRowSpan: Int,
    ): Boolean {
        if (newColSpan < minColSpan || newRowSpan < minRowSpan) return false
        if (col + newColSpan > HUB_COLUMNS) return false
        if (row + newRowSpan > HUB_MAX_ROWS) return false
        return existing.none { other ->
            other.appWidgetId != resizingAppWidgetId &&
                rectanglesOverlap(row, col, newColSpan, newRowSpan, other.row, other.col, other.colSpan, other.rowSpan)
        }
    }
}
