package com.facetlauncher.app.ui.settings

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.facetlauncher.app.data.DockAppRepository
import com.facetlauncher.app.data.FacetDockAppRepository
import com.facetlauncher.app.data.FacetRepository
import com.facetlauncher.app.data.SettingsRepository
import com.facetlauncher.app.data.WallpaperRepository
import com.facetlauncher.app.data.model.AppInfo
import com.facetlauncher.app.data.model.ClockColorOption
import com.facetlauncher.app.data.model.DockDisplayMode
import com.facetlauncher.app.data.model.FontWeightOption
import com.facetlauncher.app.data.model.HomeWallpaper
import com.facetlauncher.app.data.model.NO_ACTIVE_FACET_ID
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class DockSettingsUiState(
    /** `false` when editing the launcher-wide default dock (reached from Settings) rather than one facet's own. */
    val isFacetScoped: Boolean = false,
    val dockDisplayMode: DockDisplayMode = DockDisplayMode.ICONS,
    val dockApps: List<AppInfo> = emptyList(),
    /** Home's app label color/weight — global only, no per-facet override; used only to render the preview exactly as Home would. */
    val appLabelColorOption: ClockColorOption = ClockColorOption.THEME,
    val homeAppsFontWeight: FontWeightOption = FontWeightOption.REGULAR,
    val homeWallpaper: HomeWallpaper = HomeWallpaper.Unavailable,
)

/**
 * Settings → Dock, and (with a `facetId`) a facet's own Dock screen — reused the same way
 * [com.facetlauncher.app.ui.home.clock.ClockStyleGalleryViewModel] is reused for the global and
 * per-facet clock gallery. No `facetId` (or [NO_ACTIVE_FACET_ID]) edits the launcher-wide
 * default dock; a real id edits that facet's own [FacetDockAppRepository] list and
 * `dockDisplayMode`, always — the Inherit/Override switch that decides whether those actually
 * apply lives on [com.facetlauncher.app.ui.facets.FacetSettingsScreen].
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

    private val dockAppsFlow =
        facetId?.let { facetDockAppRepository.observeDockAppsForFacet(it) } ?: dockAppRepository.observeDockApps()

    val uiState: StateFlow<DockSettingsUiState> = combine(
        settingsRepository.settings,
        facetRepository.observeFacets(),
        dockAppsFlow,
        homeWallpaper,
    ) { settings, facets, dockApps, wallpaper ->
        val facet = facets.find { it.id == facetId }
        DockSettingsUiState(
            isFacetScoped = facetId != null,
            dockDisplayMode = facet?.dockDisplayMode ?: settings.dockDisplayMode,
            dockApps = dockApps,
            appLabelColorOption = settings.appLabelColorOption,
            homeAppsFontWeight = settings.homeAppsFontWeight,
            homeWallpaper = wallpaper,
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), DockSettingsUiState())

    init {
        viewModelScope.launch { homeWallpaper.value = wallpaperRepository.currentHomeWallpaper() }
    }

    fun setDockDisplayMode(mode: DockDisplayMode) {
        viewModelScope.launch {
            facetId?.let { pid ->
                facetRepository.getById(pid)?.let { facetRepository.setDockDisplayMode(it, mode) }
            } ?: settingsRepository.setDockDisplayMode(mode)
        }
    }

    fun reorderDockApps(orderedApps: List<AppInfo>) {
        viewModelScope.launch {
            facetId?.let { facetDockAppRepository.reorderDockApps(it, orderedApps) }
                ?: dockAppRepository.reorderDockApps(orderedApps)
        }
    }
}
