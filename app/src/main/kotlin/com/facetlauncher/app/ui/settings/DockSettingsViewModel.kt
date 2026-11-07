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
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import com.facetlauncher.app.data.PlacementRepository
import com.facetlauncher.app.data.model.PlacementTarget
import com.facetlauncher.app.data.model.PlacementList

/**
 * `global*`/`facet*` fields are the raw stored values; [dockItems] is this scope's own: the facet's, or the
 * launcher-wide default a new facet is seeded from.
 */
data class DockSettingsUiState(
    val facet: Facet? = null,
    val settings: LauncherSettings = LauncherSettings(),
    val globalDockItems: ImmutableList<PlacedItem> = persistentListOf(),
    val facetDockItems: ImmutableList<PlacedItem> = persistentListOf(),
    /** Home's app label color/weight — global only, no per-facet override; used only to render the preview exactly as Home would. */
    val appLabelColorOption: ClockColorOption = ClockColorOption.THEME,
    val homeAppsFontWeight: FontWeightOption = FontWeightOption.REGULAR,
    val homeWallpaper: HomeWallpaper = HomeWallpaper.Unavailable,
) {
    val isFacetScoped: Boolean get() = facet != null
    /** The preview needs the dock's display mode here. */
    val dockDisplayMode: DockDisplayMode get() = resolveFacetStyle(facet, settings).dockDisplayMode
    val dockItems: ImmutableList<PlacedItem> get() = if (isFacetScoped) facetDockItems else globalDockItems
}

/**
 * Settings → Dock, and (with a `facetId`) a facet's own Dock screen — reused the same way
 * [com.facetlauncher.app.ui.home.clock.ClockStyleGalleryViewModel] is reused for the global and
 * per-facet clock gallery. No `facetId` (or [NO_ACTIVE_FACET_ID]) edits the launcher-wide
 * default dock, which only seeds new facets; a real id edits that facet's own list in [PlacementRepository].
 */
@HiltViewModel
class DockSettingsViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val settingsRepository: SettingsRepository,
    private val facetRepository: FacetRepository,
    private val placementRepository: PlacementRepository,
    private val observePlacedItems: ObservePlacedItemsUseCase,
    private val wallpaperRepository: WallpaperRepository,
    private val applyFacetDefaults: ApplyFacetDefaultsUseCase,
) : ViewModel() {

    private val facetId: Long? = savedStateHandle.get<Long>("facetId")?.takeIf { it != NO_ACTIVE_FACET_ID }

    private val homeWallpaper = MutableStateFlow<HomeWallpaper>(HomeWallpaper.Unavailable)

    private val facetDockItemsFlow: Flow<List<PlacedItem>> = facetId?.let { observePlacedItems.items(PlacementTarget(PlacementList.DOCK, it)) } ?: flowOf(emptyList())
    private val globalDockItemsFlow: Flow<List<PlacedItem>> = observePlacedItems.items(PlacementTarget.GlobalDock)

    val uiState: StateFlow<DockSettingsUiState> = combine(
        settingsRepository.settings,
        facetRepository.observeFacets(),
        combine(facetDockItemsFlow, globalDockItemsFlow, ::Pair),
        homeWallpaper,
    ) { settings, facets, dockItems, wallpaper ->
        val (facetDockItems, globalDockItems) = dockItems
        DockSettingsUiState(
            facet = facets.find { it.id == facetId },
            settings = settings,
            globalDockItems = globalDockItems.toImmutableList(),
            facetDockItems = facetDockItems.toImmutableList(),
            appLabelColorOption = settings.appLabelColorOption,
            homeAppsFontWeight = settings.homeAppsFontWeight,
            homeWallpaper = wallpaper,
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), DockSettingsUiState())

    init {
        viewModelScope.launch { homeWallpaper.value = wallpaperRepository.currentHomeWallpaper() }
    }

    fun reorderDockItems(orderedItems: List<PlacedItem>) {
        viewModelScope.launch {
            placementRepository.reorderItems(PlacementTarget(PlacementList.DOCK, facetId), orderedItems)
        }
    }

    /** "Reset to default": replaces this facet's dock with a copy of the launcher-wide one. */
    fun resetToDefault() {
        val id = facetId ?: return
        viewModelScope.launch { applyFacetDefaults(id, FacetDefaultsScope.DOCK) }
    }
}
