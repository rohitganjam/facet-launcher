package com.facetlauncher.app.domain

import javax.inject.Inject

/**
 * The landscape Hub in one place: lays the widgets out in [rows] rows and works out the new order
 * after a drag. It composes [OrderWidgetsForLandscapeUseCase], [FlowPackWidgetsUseCase] and
 * [ReorderLandscapeWidgetUseCase], so `HubViewModel` has a single collaborator for landscape.
 */
class LandscapeHubUseCase @Inject constructor(
    private val orderWidgets: OrderWidgetsForLandscapeUseCase,
    private val flowPack: FlowPackWidgetsUseCase,
    private val reorder: ReorderLandscapeWidgetUseCase,
) {
    /** The widgets in landscape order, packed into [rows] rows. A widget taller than [rows] is clamped to it. */
    fun layout(widgets: List<HubWidgetState>, rows: Int): List<PackedWidget> =
        flowPack(orderWidgets(widgets).map { WidgetSize(it.appWidgetId, it.colSpan, it.rowSpan) }, rows)

    /**
     * Every widget's new landscape order after [movedId] is dropped with its top-left corner on
     * ([dropRow], [dropCol]) of the layout from [layout]. Empty if [movedId] is unknown.
     */
    fun ordersAfterDrop(widgets: List<HubWidgetState>, rows: Int, movedId: Int, dropRow: Int, dropCol: Int): Map<Int, Int> {
        val packed = layout(widgets, rows)
        val before = landscapeBeforeCount(packed, movedId, dropRow, dropCol, rows)
        return reorder(orderWidgets(widgets), movedId, before)
    }
}
