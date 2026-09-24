package com.facetlauncher.app.ui.settings

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.facetlauncher.app.data.DockAppRepository
import com.facetlauncher.app.data.FacetDockAppRepository
import com.facetlauncher.app.data.FacetRepository
import com.facetlauncher.app.data.SettingsRepository
import com.facetlauncher.app.data.WallpaperRepository
import com.facetlauncher.app.data.local.FacetEntity
import com.facetlauncher.app.data.local.resolveSentinel
import com.facetlauncher.app.data.model.ClockColorOption
import com.facetlauncher.app.data.model.DockDisplayMode
import com.facetlauncher.app.data.model.FontWeightOption
import com.facetlauncher.app.data.model.HomeWallpaper
import com.facetlauncher.app.data.model.NO_ACTIVE_FACET_ID
import com.facetlauncher.app.data.model.PlacedItem
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/**
 * `global*`/`facet*` fields are the raw stored values; the effective, screen-bound properties
 * below resolve facet-or-global the same way `HomeUiState`/`FacetCarouselViewModel` do at
 * runtime — this facet's own value while [isOverriding], the global default otherwise — matching
 * [com.facetlauncher.app.ui.settings.CalendarSettingsUiState]'s shape.
 */
data class DockSettingsUiState(
    val facet: FacetEntity? = null,
    val globalDockDisplayMode: DockDisplayMode = DockDisplayMode.ICONS,
    val globalDockItems: List<PlacedItem> = emptyList(),
    /** This facet's own dock — only meaningful/edited while [isOverriding]. */
    val facetDockItems: List<PlacedItem> = emptyList(),
    /** Home's app label color/weight — global only, no per-facet override; used only to render the preview exactly as Home would. */
    val appLabelColorOption: ClockColorOption = ClockColorOption.THEME,
    val homeAppsFontWeight: FontWeightOption = FontWeightOption.REGULAR,
    val homeWallpaper: HomeWallpaper = HomeWallpaper.Unavailable,
) {
    val isFacetScoped: Boolean get() = facet != null
    val isOverriding: Boolean get() = facet?.overrideDock ?: false
    /** Look, not content — edited from Settings → Appearance now, resolved independently of [isOverriding] via its own `LAUNCHER_DEFAULT` sentinel (see [com.facetlauncher.app.data.local.resolveSentinel]). Still read here since this screen's own preview needs it. */
    val dockDisplayMode: DockDisplayMode get() = facet.resolveSentinel({ it.dockDisplayMode }, DockDisplayMode.LAUNCHER_DEFAULT, globalDockDisplayMode)
    /** This scope's effective dock — the facet's own while overriding, the launcher-wide default otherwise. */
    val dockItems: List<PlacedItem> get() = if (isOverriding) facetDockItems else globalDockItems
}

/**
 * Settings → Dock, and (with a `facetId`) a facet's own Dock screen — reused the same way
 * [com.facetlauncher.app.ui.home.clock.ClockStyleGalleryViewModel] is reused for the global and
 * per-facet clock gallery. No `facetId` (or [NO_ACTIVE_FACET_ID]) edits the launcher-wide
 * default dock; a real id edits that facet's own [FacetDockAppRepository] list and
 * `dockDisplayMode`, always. The Inherit/Override switch (moved in from
 * `FacetSettingsScreen` — see chat history) lives on this screen itself now.
 */
@HiltViewModel
class DockSettingsViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val settingsRepository: SettingsRepository,
    private val facetRepository: FacetRepository,
    private val dockAppRepository: DockAppRepository,
    private val facetDockAppRepository: FacetDockAppRepository,
    private val wallpaperRepository: WallpaperRepository,
) : ViewModel() {

    private val facetId: Long? = savedStateHandle.get<Long>("facetId")?.takeIf { it != NO_ACTIVE_FACET_ID }

    private val homeWallpaper = MutableStateFlow<HomeWallpaper>(HomeWallpaper.Unavailable)

    private val facetDockItemsFlow: Flow<List<PlacedItem>> = facetId?.let { facetDockAppRepository.observeDockItems(it) } ?: flowOf(emptyList())
    private val globalDockItemsFlow: Flow<List<PlacedItem>> = dockAppRepository.observeDockItems()

    val uiState: StateFlow<DockSettingsUiState> = combine(
        settingsRepository.settings,
        facetRepository.observeFacets(),
        combine(facetDockItemsFlow, globalDockItemsFlow, ::Pair),
        homeWallpaper,
    ) { settings, facets, dockItems, wallpaper ->
        val (facetDockItems, globalDockItems) = dockItems
        DockSettingsUiState(
            facet = facets.find { it.id == facetId },
            globalDockDisplayMode = settings.dockDisplayMode,
            globalDockItems = globalDockItems,
            facetDockItems = facetDockItems,
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
            facetId?.let { facetId ->
                facetDockAppRepository.reorderDockItems(facetId, orderedItems)
            } ?: dockAppRepository.reorderDockItems(orderedItems)
        }
    }

    /**
     * The Dock card's single Inherit/Override switch — governs only the dock's own app list now
     * (Icons/Text display style moved to Settings → Appearance, resolved independently via its own
     * `LAUNCHER_DEFAULT` sentinel — see chat history). Moved in from `FacetSettingsViewModel` so it
     * sits on the same screen as the controls it gates. Switching to Override copies the default
     * dock list in when the facet's own is empty, so the card doesn't suddenly go blank — mirrors
     * [com.facetlauncher.app.ui.settings.HomeAppsListSettingsViewModel.setOverriding].
     */
    fun setOverriding(overriding: Boolean) {
        val pid = facetId ?: return
        val state = uiState.value
        viewModelScope.launch {
            val facet = facetRepository.getById(pid) ?: return@launch
            if (overriding && state.facetDockItems.isEmpty()) {
                facetDockAppRepository.replaceItems(pid, state.globalDockItems)
            }
            facetRepository.updateOverridingDock(facet, overriding)
        }
    }
}
