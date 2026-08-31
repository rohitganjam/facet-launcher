package com.lumenlauncher.app.domain

import com.lumenlauncher.app.data.local.WidgetPlacementEntity
import javax.inject.Inject

sealed interface PlaceWidgetResult {
    data class Placed(val row: Int, val col: Int) : PlaceWidgetResult
    data object HubFull : PlaceWidgetResult
}

/**
 * First-fit row-major scan for an unoccupied [colSpan]×[rowSpan] rectangle within the Hub's fixed
 * [HUB_COLUMNS]-column grid, respecting the 20-widget cap ([HUB_MAX_WIDGETS]) and the 50-row
 * scroll cap ([HUB_MAX_ROWS]) — either exhausted returns [PlaceWidgetResult.HubFull], since a
 * grid-full-but-under-20-widgets edge case (a few large widgets consuming every row) is treated
 * identically to the widget cap for messaging purposes (see plan decisions).
 */
class PlaceWidgetUseCase @Inject constructor() {
    operator fun invoke(existing: List<WidgetPlacementEntity>, colSpan: Int, rowSpan: Int): PlaceWidgetResult {
        if (existing.size >= HUB_MAX_WIDGETS) return PlaceWidgetResult.HubFull
        for (row in 0..(HUB_MAX_ROWS - rowSpan)) {
            for (col in 0..(HUB_COLUMNS - colSpan)) {
                val fits = existing.none { placed ->
                    rectanglesOverlap(row, col, colSpan, rowSpan, placed.row, placed.col, placed.colSpan, placed.rowSpan)
                }
                if (fits) return PlaceWidgetResult.Placed(row, col)
            }
        }
        return PlaceWidgetResult.HubFull
    }
}
