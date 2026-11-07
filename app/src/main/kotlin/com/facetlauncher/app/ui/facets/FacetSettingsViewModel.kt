package com.facetlauncher.app.ui.facets

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.facetlauncher.app.data.FacetRepository
import com.facetlauncher.app.domain.ActivateFacetByIdUseCase
import com.facetlauncher.app.domain.ObservePlacedItemsUseCase
import com.facetlauncher.app.data.SettingsRepository
import com.facetlauncher.app.data.model.Facet
import com.facetlauncher.app.domain.ResolvedFacetStyle
import com.facetlauncher.app.domain.resolveFacetStyle
import com.facetlauncher.app.data.model.AppListColumnAlignment
import com.facetlauncher.app.data.model.AppListGridColumns
import com.facetlauncher.app.data.model.AppListGridDisplayMode
import com.facetlauncher.app.data.model.AppListLayout
import com.facetlauncher.app.data.model.AppRowPosition
import com.facetlauncher.app.data.model.AppRowPresentation
import com.facetlauncher.app.data.model.ClockAlignment
import com.facetlauncher.app.data.model.ClockColorOption
import com.facetlauncher.app.data.model.ClockDateStyle
import com.facetlauncher.app.data.model.ClockFontOption
import com.facetlauncher.app.data.model.ClockTemplateId
import com.facetlauncher.app.data.model.DockDisplayMode
import com.facetlauncher.app.data.model.LauncherSettings
import com.facetlauncher.app.data.model.ListContentMode
import com.facetlauncher.app.data.model.PlacedItem
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import com.facetlauncher.app.data.model.AppListLimits
import com.facetlauncher.app.data.model.PlacementTarget
import com.facetlauncher.app.data.model.PlacementList

data class FacetSettingsUiState(
    val facet: Facet? = null,
    val activeFacetId: Long = 0L,
    /** This facet's own favorites. */
    val favorites: List<PlacedItem> = emptyList(),
    /** This facet's own dock. */
    val dockApps: List<PlacedItem> = emptyList(),
    val settings: LauncherSettings = LauncherSettings(),
) {
    /** Gates the header's "Apply facet" button — see [FacetSettingsHeader]'s own doc. */
    val isActive: Boolean get() = facet?.id == activeFacetId

    private val style: ResolvedFacetStyle by lazy(LazyThreadSafetyMode.NONE) { resolveFacetStyle(facet, settings) }
    val clockTemplateId: ClockTemplateId get() = style.clock.templateId
    val clockFontOption: ClockFontOption get() = style.clock.fontOption
    val clockColorOption: ClockColorOption get() = style.clock.colorOption
    val clockAccentColorOption: ClockColorOption get() = style.clock.accentColorOption
    val effectiveUse24HourTime: Boolean get() = style.clock.use24HourTime
    val clockShowMeridiem: Boolean get() = style.clock.showMeridiem
    val clockDateStyle: ClockDateStyle get() = style.clock.dateStyle
    val clockAlignment: ClockAlignment get() = style.clockPlacement.portrait.alignment
    val clockZoneHeightDp: Float? get() = style.clockPlacement.portrait.zoneHeightDp

    val appRowPosition: AppRowPosition get() = style.appList.rowPosition
    val appRowPresentation: AppRowPresentation get() = style.appList.rowPresentation
    val appListLayout: AppListLayout get() = style.appList.layout
    val appListColumnAlignment: AppListColumnAlignment get() = style.appList.columnAlignment
    val appListGridColumns: AppListGridColumns get() = style.appList.gridColumns
    val appListGridDisplayMode: AppListGridDisplayMode get() = style.appList.gridDisplayMode
    val listContentMode: ListContentMode get() = style.appList.contentMode
    val appsToShowCount: Int get() = facet?.appsToShowCount ?: settings.appsToShowCount
    val favoritesLabel: String get() = "${favorites.size} of ${AppListLimits.MAX_FAVORITES}"

    val dockDisplayMode: DockDisplayMode get() = style.dockDisplayMode
    val dockLabel: String get() = "${dockApps.size} of ${AppListLimits.MAX_DOCK_APPS}"
}

@HiltViewModel
class FacetSettingsViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val facetRepository: FacetRepository,
    private val settingsRepository: SettingsRepository,
    private val observePlacedItems: ObservePlacedItemsUseCase,
    private val activateFacetById: ActivateFacetByIdUseCase,
) : ViewModel() {

    val facetId: Long = checkNotNull(savedStateHandle["facetId"])

    val uiState: StateFlow<FacetSettingsUiState> = combine(
        facetRepository.observeFacets(),
        settingsRepository.settings,
        observePlacedItems.items(PlacementTarget(PlacementList.FAVORITES, facetId)),
        observePlacedItems.items(PlacementTarget(PlacementList.DOCK, facetId)),
    ) { facets, settings, favorites, dockApps ->
        FacetSettingsUiState(
            facet = facets.find { it.id == facetId },
            activeFacetId = settings.activeFacetId,
            favorites = favorites,
            dockApps = dockApps,
            settings = settings,
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), FacetSettingsUiState())

    fun renameFacet(newName: String) {
        val facet = uiState.value.facet ?: return
        viewModelScope.launch { facetRepository.renameFacet(facet.id, newName) }
    }

    /** The header's "Apply facet" button — mirrors `ManageFacetsViewModel.applyFacet`. A no-op if already active (button is disabled in that state, but guard here too since it's cheap). */
    fun applyFacet() {
        if (uiState.value.isActive) return
        viewModelScope.launch { activateFacetById(facetId) }
    }
}
