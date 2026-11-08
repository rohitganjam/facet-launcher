package com.facetlauncher.app.domain

import com.facetlauncher.app.data.SettingsRepository
import com.facetlauncher.app.data.model.NO_ACTIVE_FACET_ID
import com.facetlauncher.app.data.model.PlacedItem
import com.facetlauncher.app.data.model.PlacementList
import com.facetlauncher.app.data.model.PlacementTarget
import javax.inject.Inject
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map

/** The active facet's own [PlacementList], live: what a screen not tied to one facet shows as the real Home (a preview, sample apps). */
class ObserveActiveFacetItemsUseCase @Inject constructor(
    private val settingsRepository: SettingsRepository,
    private val observePlacedItems: ObservePlacedItemsUseCase,
) {
    @OptIn(ExperimentalCoroutinesApi::class)
    operator fun invoke(list: PlacementList): Flow<List<PlacedItem>> =
        settingsRepository.settings
            .map { it.activeFacetId.takeIf { id -> id != NO_ACTIVE_FACET_ID } }
            .distinctUntilChanged()
            .flatMapLatest { id -> if (id == null) flowOf(emptyList()) else observePlacedItems.items(PlacementTarget(list, id)) }
}
