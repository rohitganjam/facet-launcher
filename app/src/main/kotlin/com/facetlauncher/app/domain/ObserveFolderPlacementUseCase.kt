package com.facetlauncher.app.domain

import com.facetlauncher.app.data.FacetRepository
import com.facetlauncher.app.data.SettingsRepository
import com.facetlauncher.app.data.model.Folder
import com.facetlauncher.app.data.model.PlacementList
import com.facetlauncher.app.data.model.placementTarget
import javax.inject.Inject
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map

/**
 * What the Settings folder screen's menu offers: the Favorites and Dock actions for one folder in the active
 * facet's lists, and the facet's name so the menu can say which facet it changes (only worth showing with
 * more than one facet).
 */
data class FolderPlacementState(
    val quickAdd: QuickAddState = QuickAddState(),
    /** The active facet's name; null with no active facet. */
    val facetName: String? = null,
    val showFacetBadge: Boolean = false,
)

class ObserveFolderPlacementUseCase @Inject constructor(
    private val settingsRepository: SettingsRepository,
    private val facetRepository: FacetRepository,
    private val observePlacedItems: ObservePlacedItemsUseCase,
    private val quickAddState: ObserveQuickAddStateUseCase,
) {
    @OptIn(ExperimentalCoroutinesApi::class)
    operator fun invoke(folderId: Long): Flow<FolderPlacementState> =
        combine(settingsRepository.settings.map { it.activeFacetId }.distinctUntilChanged(), facetRepository.observeFacets()) { activeId, facets ->
            facets.find { it.id == activeId } to facets.size
        }.flatMapLatest { (facet, facetCount) ->
            combine(
                facet?.let { observePlacedItems.items(it.placementTarget(PlacementList.DOCK)) } ?: flowOf(emptyList()),
                facet?.let { observePlacedItems.items(it.placementTarget(PlacementList.FAVORITES)) } ?: flowOf(emptyList()),
            ) { dock, favorites ->
                FolderPlacementState(
                    quickAdd = quickAddState.forFolder(Folder(folderId, "", emptyList()), dock, favorites, facet?.name, facet?.name),
                    facetName = facet?.name,
                    showFacetBadge = facet != null && facetCount > 1,
                )
            }
        }
}
