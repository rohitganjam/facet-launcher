package com.lumenlauncher.app.domain

import com.lumenlauncher.app.data.local.WidgetPlacementEntity
import javax.inject.Inject

/**
 * "Gravity" compaction — pulls every widget up as far as it can go, so there's never an empty row
 * gap between it and whatever's above it (or the top of the grid). Run after every placement
 * change (a move/displacement, a resize, a delete) so gaps never accumulate, per the user's own
 * explicit "vertical spaces should be squashed" requirement (see chat history). Column position
 * never changes, only row — this is purely a vertical settle, not a full repack.
 */
class CompactWidgetsUseCase @Inject constructor() {
    operator fun invoke(placements: List<WidgetPlacementEntity>): List<WidgetPlacementEntity> {
        val settled = mutableListOf<WidgetPlacementEntity>()
        // Row order first: a higher widget must settle before a lower one can rest against it.
        for (widget in placements.sortedBy { it.row }) {
            var newRow = widget.row
            while (newRow > 0 && settled.none { rectanglesOverlap(newRow - 1, widget.col, widget.colSpan, widget.rowSpan, it.row, it.col, it.colSpan, it.rowSpan) }) {
                newRow--
            }
            settled += widget.copy(row = newRow)
        }
        return settled
    }
}
