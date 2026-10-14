package com.facetlauncher.app.domain

import com.facetlauncher.app.data.PlacementRepository
import com.facetlauncher.app.data.model.PlacedItem
import com.facetlauncher.app.data.model.PlacementList
import javax.inject.Inject

/** Counterpart of [AddToActiveListUseCase]: removes an app or a folder from [PlacementList]'s list for the active facet, with the same override resolution. */
class RemoveFromActiveListUseCase @Inject constructor(
    private val resolveTarget: ResolveActivePlacementTargetUseCase,
    private val placementRepository: PlacementRepository,
) {
    suspend operator fun invoke(list: PlacementList, item: PlacedItem) {
        val target = resolveTarget(list)
        when (item) {
            is PlacedItem.SingleApp -> placementRepository.removeApp(target, item.app)
            is PlacedItem.FolderItem -> placementRepository.removeFolderPlacement(target, item.folder.id)
        }
    }
}
