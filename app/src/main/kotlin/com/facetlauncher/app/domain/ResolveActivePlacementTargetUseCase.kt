package com.facetlauncher.app.domain

import com.facetlauncher.app.data.FacetRepository
import com.facetlauncher.app.data.SettingsRepository
import com.facetlauncher.app.data.model.placementTarget
import com.facetlauncher.app.data.model.PlacementList
import com.facetlauncher.app.data.model.PlacementTarget
import javax.inject.Inject
import kotlinx.coroutines.flow.first

/** The active facet's own [list], or `null` when there is no active facet yet. */
class ResolveActivePlacementTargetUseCase @Inject constructor(
    private val settingsRepository: SettingsRepository,
    private val facetRepository: FacetRepository,
) {
    suspend operator fun invoke(list: PlacementList): PlacementTarget? {
        val activeFacetId = settingsRepository.settings.first().activeFacetId
        return facetRepository.getById(activeFacetId)?.placementTarget(list)
    }
}
