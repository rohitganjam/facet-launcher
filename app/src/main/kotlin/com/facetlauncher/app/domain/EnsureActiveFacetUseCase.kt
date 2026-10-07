package com.facetlauncher.app.domain

import com.facetlauncher.app.data.FacetRepository
import com.facetlauncher.app.data.SettingsRepository
import com.facetlauncher.app.data.model.NO_ACTIVE_FACET_ID
import javax.inject.Inject
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

/**
 * Bootstraps facet state (F4: the launcher starts with exactly one facet) and keeps the active facet one the
 * user may use: if it is missing, or disabled on the free plan, the first selectable facet takes over.
 * Spans [FacetRepository] and [SettingsRepository], so it's a UseCase rather than living in
 * either repository or a ViewModel.
 */
class EnsureActiveFacetUseCase @Inject constructor(
    private val facetRepository: FacetRepository,
    private val settingsRepository: SettingsRepository,
    private val selectableFacets: SelectableFacetsUseCase,
) {
    /**
     * Runs [invoke] now and again whenever the set of selectable facets changes (an add, a delete, a
     * reorder, or a change of entitlement), for as long as it is collected: launched once, for the
     * app's lifetime, from [com.facetlauncher.app.ui.launcher.LauncherViewModel].
     */
    suspend fun keepUsable() {
        selectableFacets.observe().map { facets -> facets.map { it.id } }.distinctUntilChanged().collect { invoke() }
    }

    suspend operator fun invoke() {
        val facets = facetRepository.observeFacets().first()
        val activeId = settingsRepository.settings.first().activeFacetId

        val selectableIds = selectableFacets()
        val activeIsUsable = activeId != NO_ACTIVE_FACET_ID && activeId in selectableIds
        if (!activeIsUsable) {
            val resolvedFacet = facets.firstOrNull { it.id in selectableIds } ?: facetRepository.addFacet()
            settingsRepository.setActiveFacetId(resolvedFacet.id)
        }
    }
}
