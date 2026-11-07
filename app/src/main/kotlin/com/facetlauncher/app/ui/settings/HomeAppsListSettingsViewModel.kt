package com.facetlauncher.app.ui.settings

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.facetlauncher.app.data.DefaultAppRepository
import com.facetlauncher.app.data.FacetRepository
import com.facetlauncher.app.data.SettingsRepository
import com.facetlauncher.app.data.WallpaperRepository
import com.facetlauncher.app.data.model.Facet
import com.facetlauncher.app.domain.ApplyFacetDefaultsUseCase
import com.facetlauncher.app.domain.FacetDefaultsScope
import com.facetlauncher.app.domain.ResolvedFacetStyle
import com.facetlauncher.app.domain.ObservePlacedItemsUseCase
import com.facetlauncher.app.domain.resolveFacetStyle
import com.facetlauncher.app.data.model.AppListColumnAlignment
import com.facetlauncher.app.data.model.AppListGridColumns
import com.facetlauncher.app.data.model.AppListGridDisplayMode
import com.facetlauncher.app.data.model.AppListLayout
import com.facetlauncher.app.data.model.AppListLimits
import com.facetlauncher.app.data.model.AppListVerticalAlignment
import com.facetlauncher.app.data.model.AppRowPosition
import com.facetlauncher.app.data.model.AppRowPresentation
import com.facetlauncher.app.data.model.ClockColorOption
import com.facetlauncher.app.data.model.FontWeightOption
import com.facetlauncher.app.data.model.HomeWallpaper
import com.facetlauncher.app.data.model.ListContentMode
import com.facetlauncher.app.data.model.LauncherSettings
import com.facetlauncher.app.data.model.NO_ACTIVE_FACET_ID
import com.facetlauncher.app.data.model.PlacedItem
import com.facetlauncher.app.domain.GetInstalledAppsUseCase
import com.facetlauncher.app.domain.SelectPreviewAppsUseCase
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

/** Appearance preview card home-row + dock-icon slots — how many recognizable apps to surface. */
private const val PREVIEW_APP_COUNT = 3

/**
 * `global*`/`facet*` fields are the raw stored values; the screen-bound properties below are this scope's own:
 * the facet's, or the launcher-wide defaults a new facet is seeded from.
 */
data class HomeAppsListUiState(
    val facet: Facet? = null,
    /** Every facet, for the default screen's "Copy to facets". */
    val facets: ImmutableList<Facet> = persistentListOf(),
    val settings: LauncherSettings = LauncherSettings(),
    /** The launcher-wide default Favorites list, which seeds new facets. */
    val globalFavorites: ImmutableList<PlacedItem> = persistentListOf(),
    val facetFavorites: ImmutableList<PlacedItem> = persistentListOf(),
    /** What the preview renders in a non-Favorites mode — a recognizable app sample. */
    val previewSourceApps: ImmutableList<PlacedItem> = persistentListOf(),
    val homeWallpaper: HomeWallpaper = HomeWallpaper.Unavailable,
    /** Home's app label color/weight — global only, no per-facet override; drives the preview's label rendering. */
    val appLabelColorOption: ClockColorOption = ClockColorOption.THEME,
    val homeAppsFontWeight: FontWeightOption = FontWeightOption.REGULAR,
) {
    val isFacetScoped: Boolean get() = facet != null
    private val style: ResolvedFacetStyle by lazy(LazyThreadSafetyMode.NONE) { resolveFacetStyle(facet, settings) }
    /** The preview needs the look here. */
    val appRowPosition: AppRowPosition get() = style.appList.rowPosition
    val appRowPresentation: AppRowPresentation get() = style.appList.rowPresentation
    val listContentMode: ListContentMode get() = style.appList.contentMode
    val appsToShowCount: Int get() = facet?.appsToShowCount ?: settings.appsToShowCount
    val appListVerticalAlignment: AppListVerticalAlignment get() = style.appList.verticalAlignment
    val appListLayout: AppListLayout get() = style.appList.layout
    val appListColumnAlignment: AppListColumnAlignment get() = style.appList.columnAlignment
    val appListGridColumns: AppListGridColumns get() = style.appList.gridColumns
    val appListGridDisplayMode: AppListGridDisplayMode get() = style.appList.gridDisplayMode
    /** This scope's favorites, apps and folders interleaved, same as Home's own list. */
    val favorites: ImmutableList<PlacedItem> get() = if (isFacetScoped) facetFavorites else globalFavorites
    val favoritesLabel: String get() = "${favorites.size} of ${AppListLimits.MAX_FAVORITES}"
    /** What the preview renders — [favorites] in Favorites mode, a recognizable app sample otherwise. */
    val previewApps: ImmutableList<PlacedItem> get() = if (listContentMode == ListContentMode.FAVORITES) favorites else previewSourceApps
}

/**
 * Settings → Home & Apps → Home Apps List, and (with a `facetId`) a facet's own Apps-list
 * screen — reused the same way [com.facetlauncher.app.ui.home.clock.ClockStyleGalleryViewModel]
 * is reused for the global and per-facet clock gallery. No `facetId` (or
 * [NO_ACTIVE_FACET_ID]) edits the launcher-wide default, which only seeds new facets; a real id edits that
 * facet's own content mode, count and favorites.
 */
@HiltViewModel
class HomeAppsListSettingsViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val settingsRepository: SettingsRepository,
    private val facetRepository: FacetRepository,
    private val placementRepository: PlacementRepository,
    private val observePlacedItems: ObservePlacedItemsUseCase,
    private val wallpaperRepository: WallpaperRepository,
    private val defaultAppRepository: DefaultAppRepository,
    getInstalledApps: GetInstalledAppsUseCase,
    private val selectPreviewApps: SelectPreviewAppsUseCase,
    private val applyFacetDefaults: ApplyFacetDefaultsUseCase,
) : ViewModel() {

    private val facetId: Long? = savedStateHandle.get<Long>("facetId")?.takeIf { it != NO_ACTIVE_FACET_ID }

    private val homeWallpaper = MutableStateFlow<HomeWallpaper>(HomeWallpaper.Unavailable)
    private val preferredPreviewPackages = MutableStateFlow<List<String>>(emptyList())

    private val facetFavoritesFlow: Flow<List<PlacedItem>> = facetId?.let { observePlacedItems.items(PlacementTarget(PlacementList.FAVORITES, it)) } ?: flowOf(emptyList())
    private val globalFavoritesFlow: Flow<List<PlacedItem>> = observePlacedItems.items(PlacementTarget.GlobalFavorites)

    val uiState: StateFlow<HomeAppsListUiState> = combine(
        settingsRepository.settings,
        facetRepository.observeFacets(),
        combine(facetFavoritesFlow, globalFavoritesFlow, ::Pair),
        combine(homeWallpaper, getInstalledApps.observe(), preferredPreviewPackages, ::Triple),
    ) { settings, facets, favs, previewInputs ->
        val (facetFavorites, globalFavorites) = favs
        val (wallpaper, installed, preferred) = previewInputs
        HomeAppsListUiState(
            facet = facets.find { it.id == facetId },
            facets = facets.toImmutableList(),
            settings = settings,
            globalFavorites = globalFavorites.toImmutableList(),
            facetFavorites = facetFavorites.toImmutableList(),
            previewSourceApps = selectPreviewApps(installed, preferred, PREVIEW_APP_COUNT).map { PlacedItem.SingleApp(it) }.toImmutableList(),
            homeWallpaper = wallpaper,
            appLabelColorOption = settings.appLabelColorOption,
            homeAppsFontWeight = settings.homeAppsFontWeight,
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), HomeAppsListUiState())

    init {
        viewModelScope.launch { homeWallpaper.value = wallpaperRepository.currentHomeWallpaper() }
        viewModelScope.launch { preferredPreviewPackages.value = defaultAppRepository.getDefaultAppPackages() }
    }

    fun setListContentMode(mode: ListContentMode) {
        viewModelScope.launch {
            facetId?.let { pid -> facetRepository.getById(pid)?.let { facetRepository.setListContentMode(it.id, mode) } }
                ?: settingsRepository.setListContentMode(mode)
        }
    }

    fun setAppsToShowCount(count: Int) {
        viewModelScope.launch {
            facetId?.let { pid -> facetRepository.getById(pid)?.let { facetRepository.setAppsToShowCount(it.id, count) } }
                ?: settingsRepository.setAppsToShowCount(count)
        }
    }

    fun reorderFavorites(orderedItems: List<PlacedItem>) {
        viewModelScope.launch {
            placementRepository.reorderItems(PlacementTarget(PlacementList.FAVORITES, facetId), orderedItems)
        }
    }

    /** "Copy to facets": replaces the Favorites, content mode and count of each chosen facet with the launcher-wide ones. */
    fun copyToFacets(facetIds: Set<Long>) {
        viewModelScope.launch { facetIds.forEach { applyFacetDefaults(it, FacetDefaultsScope.APPS_LIST) } }
    }

    /** "Reset to default": replaces this facet's Favorites, content mode and count with the launcher-wide ones. */
    fun resetToDefault() {
        val id = facetId ?: return
        viewModelScope.launch { applyFacetDefaults(id, FacetDefaultsScope.APPS_LIST) }
    }
}
