package com.facetlauncher.app.domain

import com.facetlauncher.app.data.FacetRepository
import com.facetlauncher.app.data.SettingsRepository
import com.facetlauncher.app.data.model.NO_ACTIVE_FACET_ID
import javax.inject.Inject
import kotlinx.coroutines.flow.first

/**
 * Bootstraps facet state on app start (F4: the launcher starts with exactly one facet).
 * Spans [FacetRepository] and [SettingsRepository], so it's a UseCase rather than living in
 * either repository or a ViewModel.
 */
class EnsureActiveFacetUseCase @Inject constructor(
    private val facetRepository: FacetRepository,
    private val settingsRepository: SettingsRepository,
) {
    suspend operator fun invoke() {
        val facets = facetRepository.observeFacets().first()
        val activeId = settingsRepository.settings.first().activeFacetId

        val validActiveFacet = if (activeId != NO_ACTIVE_FACET_ID) {
            facets.find { it.id == activeId }
        } else {
            null
        }
        if (validActiveFacet != null) return

        val resolvedFacet = facets.firstOrNull() ?: facetRepository.addFacet()
        settingsRepository.setActiveFacetId(resolvedFacet.id)
    }
}
