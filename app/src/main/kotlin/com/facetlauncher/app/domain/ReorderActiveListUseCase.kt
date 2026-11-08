package com.facetlauncher.app.domain

import com.facetlauncher.app.data.PlacementRepository
import com.facetlauncher.app.data.model.PlacedItem
import com.facetlauncher.app.data.model.PlacementList
import javax.inject.Inject

/** A drag-to-reorder on Home: rewrites the active facet's [PlacementList] list to [orderedItems]' order. */
class ReorderActiveListUseCase @Inject constructor(
    private val resolveTarget: ResolveActivePlacementTargetUseCase,
    private val placementRepository: PlacementRepository,
) {
    suspend operator fun invoke(list: PlacementList, orderedItems: List<PlacedItem>) {
        placementRepository.reorderItems(resolveTarget(list), orderedItems)
    }

    /** A drag on Home's grid: saves the Favorites' new cells for [landscape] or portrait, and their order to match. */
    suspend fun saveGridCells(landscape: Boolean, orderedItems: List<PlacedItem>, cells: Map<String, GridCell>) {
        placementRepository.saveGridCells(resolveTarget(PlacementList.FAVORITES), landscape, orderedItems, cells)
    }
}
