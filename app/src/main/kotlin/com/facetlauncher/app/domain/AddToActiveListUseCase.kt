package com.facetlauncher.app.domain

import com.facetlauncher.app.data.PlacementRepository
import com.facetlauncher.app.data.model.PlacedItem
import com.facetlauncher.app.data.model.PlacementList
import javax.inject.Inject
import kotlinx.coroutines.flow.first

/**
 * F12's long-press "Add to Dock" / "Add to Favorites" (and the facet-scoped variants): appends an
 * app or a folder to the end of [PlacementList]'s list for the active facet — its own list when it
 * overrides that one, otherwise the launcher-wide default ([ResolveActivePlacementTargetUseCase]).
 */
class AddToActiveListUseCase @Inject constructor(
    private val resolveTarget: ResolveActivePlacementTargetUseCase,
    private val placementRepository: PlacementRepository,
    private val observePlacedItems: ObservePlacedItemsUseCase,
) {
    suspend operator fun invoke(list: PlacementList, item: PlacedItem) {
        val target = resolveTarget(list)
        // Item-counted, not app-counted — a folder occupies a slot too, and an app-only count
        // would let a new app's computed position collide with an existing folder's.
        val position = observePlacedItems.items(target).first().size
        when (item) {
            is PlacedItem.SingleApp -> placementRepository.addApp(target, item.app, position)
            is PlacedItem.FolderItem -> placementRepository.placeFolder(target, item.folder.id, position)
        }
    }
}
