package com.facetlauncher.app.domain

import com.facetlauncher.app.data.local.WidgetPlacementEntity
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
 *
 * [preferredRow]/[preferredCol] (F14 Backup & Restore's guided widget re-add — see
 * `ui/settings/backup/BackupRestoreViewModel.kt`) are tried first, honoring a backup's own
 * recorded position when that spot happens to still be free; any other caller (the normal
 * add-widget flow) leaves them `null` and gets the unchanged first-fit-only behavior.
 */
class PlaceWidgetUseCase @Inject constructor() {
    operator fun invoke(
        existing: List<WidgetPlacementEntity>,
        colSpan: Int,
        rowSpan: Int,
        preferredRow: Int? = null,
        preferredCol: Int? = null,
    ): PlaceWidgetResult {
        if (existing.size >= HUB_MAX_WIDGETS) return PlaceWidgetResult.HubFull
        if (preferredRow != null && preferredCol != null &&
            preferredRow in 0..(HUB_MAX_ROWS - rowSpan) && preferredCol in 0..(HUB_COLUMNS - colSpan) &&
            existing.none { placed -> rectanglesOverlap(preferredRow, preferredCol, colSpan, rowSpan, placed.row, placed.col, placed.colSpan, placed.rowSpan) }
        ) {
            return PlaceWidgetResult.Placed(preferredRow, preferredCol)
        }
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
