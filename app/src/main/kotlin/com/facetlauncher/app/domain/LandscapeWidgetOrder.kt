package com.facetlauncher.app.domain

import javax.inject.Inject

/**
 * The widgets in the order the landscape Hub lays them out: those with a saved
 * [HubWidgetState.landscapeOrder] first, lowest first, then the rest in portrait's reading order
 * (row, then column). An install that never reordered in landscape therefore reads like portrait,
 * and a widget added later (no order) follows the ordered ones.
 */
class OrderWidgetsForLandscapeUseCase @Inject constructor() {
    operator fun invoke(widgets: List<HubWidgetState>): List<HubWidgetState> = widgets.sortedWith(
        compareBy<HubWidgetState> { it.landscapeOrder == null }
            .thenBy { it.landscapeOrder ?: 0 }
            .thenBy { it.row }
            .thenBy { it.col }
            .thenBy { it.appWidgetId },
    )
}

/**
 * Moves [movedId] in the landscape order so that exactly [beforeCount] of the other widgets come
 * before it, and returns every widget's new order (0 until n) — renumbering everyone, so each
 * widget has an order from then on. [ordered] is the current order, from
 * [OrderWidgetsForLandscapeUseCase]. An unknown id changes nothing.
 */
class ReorderLandscapeWidgetUseCase @Inject constructor() {
    operator fun invoke(ordered: List<HubWidgetState>, movedId: Int, beforeCount: Int): Map<Int, Int> {
        val moved = ordered.firstOrNull { it.appWidgetId == movedId } ?: return emptyMap()
        val others = ordered.filter { it.appWidgetId != movedId }
        val result = others.toMutableList().apply { add(beforeCount.coerceIn(0, others.size), moved) }
        return result.mapIndexed { index, widget -> widget.appWidgetId to index }.toMap()
    }
}

/**
 * How many of the other widgets come before a widget dropped with its top-left corner on
 * ([dropRow], [dropCol]) in the landscape layout — which is where the dropped widget goes in the
 * flow. The flow runs down each column, then across, so a cell's place is `col * rows + row`;
 * landing on another widget's cell puts the dropped widget just before it.
 */
internal fun landscapeBeforeCount(packed: List<PackedWidget>, movedId: Int, dropRow: Int, dropCol: Int, rows: Int): Int {
    val dropKey = dropCol * rows + dropRow
    return packed.count { it.id != movedId && it.col * rows + it.row < dropKey }
}
