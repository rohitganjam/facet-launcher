package com.facetlauncher.app.ui.facets

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.facetlauncher.app.data.AutomationRuleRepository
import com.facetlauncher.app.data.FacetRepository
import com.facetlauncher.app.data.SettingsRepository
import com.facetlauncher.app.data.local.FacetEntity
import com.facetlauncher.app.data.EntitlementRepository
import com.facetlauncher.app.data.model.FacetLimits
import com.facetlauncher.app.domain.ActivateFacetByIdUseCase
import com.facetlauncher.app.domain.AddFacetUseCase
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
    /** False for a free user: facets past the third are disabled and the add row opens the upgrade sheet. */
    val isPro: Boolean = true,
    /** How many automation rules exist, for the Facet automation card's subtitle. */
    val automationRuleCount: Int = 0,
) {
    val canAddFacet: Boolean get() = FacetLimits.canAdd(facets.size, isPro)

    /** The add row shows at the limit for a free user too, with a Pro pill, so the way up is visible. */
    val showAddFacet: Boolean get() = canAddFacet || !isPro

    /** The facets the user may switch to or edit; the rest are disabled on the free plan. */
    val selectableFacetIds: Set<Long> get() = FacetLimits.selectableIds(facets.map { it.id }, isPro)
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
    private val activateFacetById: ActivateFacetByIdUseCase,
    private val addFacetUseCase: AddFacetUseCase,
    entitlementRepository: EntitlementRepository,
    automationRuleRepository: AutomationRuleRepository,
) : ViewModel() {

    val uiState: StateFlow<ManageFacetsUiState> = combine(
        facetRepository.observeFacets(),
        settingsRepository.settings,
        entitlementRepository.isPro,
        automationRuleRepository.observeRules(),
    ) { facets, settings, isPro, rules ->
        ManageFacetsUiState(facets = facets, activeFacetId = settings.activeFacetId, isPro = isPro, automationRuleCount = rules.size)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), ManageFacetsUiState())

    fun reorderFacets(orderedFacets: List<FacetEntity>) {
        viewModelScope.launch { facetRepository.reorderFacets(orderedFacets) }
    }

    fun applyFacet(facetId: Long) {
        viewModelScope.launch { activateFacetById(facetId) }
    }

    fun addFacet() {
        viewModelScope.launch { addFacetUseCase() }
    }

    fun deleteFacet(facet: FacetEntity) {
        if (!uiState.value.canDeleteFacet) return
        viewModelScope.launch {
            deleteFacetUseCase(facet)
            if (uiState.value.activeFacetId == facet.id) {
                // The next facet the user may actually use, judged on the list as it is after this delete.
                val remaining = uiState.value.facets.filter { it.id != facet.id }
                val usable = FacetLimits.selectableIds(remaining.map { it.id }, uiState.value.isPro)
                remaining.firstOrNull { it.id in usable }?.let { settingsRepository.setActiveFacetId(it.id) }
            }
        }
    }
}
