package com.facetlauncher.app.ui.facets

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.facetlauncher.app.data.FacetRepository
import com.facetlauncher.app.data.SettingsRepository
import com.facetlauncher.app.data.local.FacetEntity
import com.facetlauncher.app.domain.DeleteFacetUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class ManageFacetsUiState(
    val facets: List<FacetEntity> = emptyList(),
    val activeFacetId: Long = 0L,
) {
    val canAddFacet: Boolean get() = facets.size < FacetRepository.MAX_FACETS
    val canDeleteFacet: Boolean get() = facets.size > FacetRepository.MIN_FACETS
}

/**
 * Settings → Facets ("Manage Facets"): the drag-to-reorder / add / delete list. A plain
 * settings screen — unlike [FacetCarouselViewModel], it only needs the facet list, so it
 * skips that class's expensive per-facet preview flow ([com.facetlauncher.app.domain.ObserveFacetPreviewsUseCase]).
 */
@HiltViewModel
class ManageFacetsViewModel @Inject constructor(
    private val facetRepository: FacetRepository,
    private val settingsRepository: SettingsRepository,
    private val deleteFacetUseCase: DeleteFacetUseCase,
) : ViewModel() {

    val uiState: StateFlow<ManageFacetsUiState> = combine(
        facetRepository.observeFacets(),
        settingsRepository.settings,
    ) { facets, settings ->
        ManageFacetsUiState(facets = facets, activeFacetId = settings.activeFacetId)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), ManageFacetsUiState())

    fun reorderFacets(orderedFacets: List<FacetEntity>) {
        viewModelScope.launch { facetRepository.reorderFacets(orderedFacets) }
    }

    fun applyFacet(facetId: Long) {
        viewModelScope.launch { settingsRepository.setActiveFacetId(facetId) }
    }

    fun addFacet() {
        if (!uiState.value.canAddFacet) return
        viewModelScope.launch { facetRepository.addFacet() }
    }

    fun deleteFacet(facet: FacetEntity) {
        if (!uiState.value.canDeleteFacet) return
        viewModelScope.launch {
            deleteFacetUseCase(facet)
            if (uiState.value.activeFacetId == facet.id) {
                uiState.value.facets.firstOrNull { it.id != facet.id }
                    ?.let { settingsRepository.setActiveFacetId(it.id) }
            }
        }
    }
}
