package com.facetlauncher.app.domain

import com.facetlauncher.app.data.FacetRepository
import com.facetlauncher.app.data.SettingsRepository
import com.facetlauncher.app.data.local.placementTarget
import com.facetlauncher.app.data.model.PlacementList
import com.facetlauncher.app.data.model.PlacementTarget
import javax.inject.Inject
import kotlinx.coroutines.flow.first

/**
 * Which concrete list the active facet reads [list] from: its own when it overrides that list
 * (`overrideDock` / `overridingFavorites`), otherwise the launcher-wide default. Mirrors
 * [ObserveQuickAddStateUseCase]'s own resolution, so a long-press "Add to…" row always writes where
 * the row said it would.
 */
class ResolveActivePlacementTargetUseCase @Inject constructor(
    private val settingsRepository: SettingsRepository,
    private val facetRepository: FacetRepository,
) {
    suspend operator fun invoke(list: PlacementList): PlacementTarget {
        val activeFacetId = settingsRepository.settings.first().activeFacetId
        return facetRepository.getById(activeFacetId).placementTarget(list)
    }
}
