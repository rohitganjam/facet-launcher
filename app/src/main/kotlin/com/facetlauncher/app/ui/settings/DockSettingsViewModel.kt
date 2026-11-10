package com.facetlauncher.app.ui.settings

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.facetlauncher.app.data.FacetRepository
import com.facetlauncher.app.data.SettingsRepository
import com.facetlauncher.app.data.WallpaperRepository
import com.facetlauncher.app.data.model.Facet
import com.facetlauncher.app.data.model.LauncherSettings
import com.facetlauncher.app.domain.ApplyFacetDefaultsUseCase
import com.facetlauncher.app.domain.CopyFacetSettingsUseCase
import com.facetlauncher.app.domain.FacetDefaultsScope
import com.facetlauncher.app.domain.resolveFacetStyle
import com.facetlauncher.app.domain.ObservePlacedItemsUseCase
import com.facetlauncher.app.data.model.ClockColorOption
import com.facetlauncher.app.data.model.DockDisplayMode
import com.facetlauncher.app.data.model.FontWeightOption
import com.facetlauncher.app.data.model.HomeWallpaper
import com.facetlauncher.app.data.model.NO_ACTIVE_FACET_ID
import com.facetlauncher.app.data.model.PlacedItem
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import com.facetlauncher.app.data.PlacementRepository
import com.facetlauncher.app.data.model.PlacementTarget
import com.facetlauncher.app.data.model.PlacementList

data class DockSettingsUiState(
    val facet: Facet? = null,
    val settings: LauncherSettings = LauncherSettings(),
    val dockItems: ImmutableList<PlacedItem> = persistentListOf(),
    /** Home's app label color/weight — global only, no per-facet override; used only to render the preview exactly as Home would. */
    val appLabelColorOption: ClockColorOption = ClockColorOption.THEME,
    val homeAppsFontWeight: FontWeightOption = FontWeightOption.REGULAR,
    val homeWallpaper: HomeWallpaper = HomeWallpaper.Unavailable,
    /** The other facets, for Copy from facet. */
    val otherFacets: ImmutableList<Facet> = persistentListOf(),
) {
    /** The preview needs the dock's display mode here. */
    val dockDisplayMode: DockDisplayMode get() = resolveFacetStyle(facet, settings).dockDisplayMode
}

/** A facet's own Dock screen (`facetId` is required): its apps and display style, and a Reset to default that re-seeds it from the device's default apps. */
@HiltViewModel
class DockSettingsViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val settingsRepository: SettingsRepository,
    private val facetRepository: FacetRepository,
    private val placementRepository: PlacementRepository,
    private val observePlacedItems: ObservePlacedItemsUseCase,
    private val wallpaperRepository: WallpaperRepository,
    private val applyFacetDefaults: ApplyFacetDefaultsUseCase,
    private val copyFacetSettings: CopyFacetSettingsUseCase,
) : ViewModel() {

    private val facetId: Long = savedStateHandle.get<Long>("facetId") ?: NO_ACTIVE_FACET_ID

    private val homeWallpaper = MutableStateFlow<HomeWallpaper>(HomeWallpaper.Unavailable)

    val uiState: StateFlow<DockSettingsUiState> = combine(
        settingsRepository.settings,
        facetRepository.observeFacets(),
        observePlacedItems.items(PlacementTarget(PlacementList.DOCK, facetId)),
        homeWallpaper,
    ) { settings, facets, dockItems, wallpaper ->
        DockSettingsUiState(
            facet = facets.find { it.id == facetId },
            settings = settings,
            dockItems = dockItems.toImmutableList(),
            appLabelColorOption = settings.appLabelColorOption,
            homeAppsFontWeight = settings.homeAppsFontWeight,
            homeWallpaper = wallpaper,
            otherFacets = facets.filter { it.id != facetId }.toImmutableList(),
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), DockSettingsUiState())

    init {
        viewModelScope.launch { homeWallpaper.value = wallpaperRepository.currentHomeWallpaper() }
    }

    fun reorderDockItems(orderedItems: List<PlacedItem>) {
        viewModelScope.launch { placementRepository.reorderItems(PlacementTarget(PlacementList.DOCK, facetId), orderedItems) }
    }

    fun setDockDisplayMode(mode: DockDisplayMode) {
        viewModelScope.launch { facetRepository.setDockDisplayMode(facetId, mode) }
    }

    /** "Copy from facet": this Dock (apps, folders, cells and display style) becomes a copy of [sourceFacetId]'s. */
    fun copyFromFacet(sourceFacetId: Long) {
        viewModelScope.launch { copyFacetSettings.copyDock(sourceFacetId, facetId) }
    }

    /** "Reset to default": re-seeds this facet's dock from the device's default apps and restores the default display style. */
    fun resetToDefault() {
        viewModelScope.launch { applyFacetDefaults(facetId, FacetDefaultsScope.DOCK) }
    }
}
