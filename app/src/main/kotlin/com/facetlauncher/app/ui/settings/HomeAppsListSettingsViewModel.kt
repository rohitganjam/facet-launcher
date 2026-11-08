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
import com.facetlauncher.app.data.model.AppListShrinkToFit
import com.facetlauncher.app.data.model.LandscapeOverrides
import com.facetlauncher.app.data.model.landscape
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
    /** The facet's own Favorites; empty at launcher scope, which has no list. */
    val favorites: ImmutableList<PlacedItem> = persistentListOf(),
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
    val appListShrinkToFit: AppListShrinkToFit get() = style.appList.shrinkToFit
    /** Launcher-wide: off keeps Home in portrait, and the landscape settings are hidden. */
    val allowLandscape: Boolean get() = settings.allowLandscape
    /** What the Landscape rows show: this scope's own landscape values, `null` meaning "same as portrait". */
    val landscape: LandscapeOverrides get() = facet?.landscape ?: settings.landscape
    val favoritesLabel: String get() = "${favorites.size} of ${AppListLimits.MAX_FAVORITES}"
    /** What the preview renders — a facet's [favorites] in Favorites mode, a recognizable app sample otherwise (and at launcher scope). */
    val previewApps: ImmutableList<PlacedItem> get() = if (isFacetScoped && listContentMode == ListContentMode.FAVORITES) favorites else previewSourceApps
}

/**
 * Settings → Home & Apps → Default Apps list, and (with a `facetId`) a facet's own Apps-list
 * screen. No `facetId` (or [NO_ACTIVE_FACET_ID]) edits the launcher-wide list content and look a
 * new facet starts with (there is no launcher-wide Favorites list); a real id edits that
 * facet's own content mode, count, look and favorites.
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

    private val favoritesFlow: Flow<List<PlacedItem>> = facetId?.let { observePlacedItems.items(PlacementTarget(PlacementList.FAVORITES, it)) } ?: flowOf(emptyList())

    val uiState: StateFlow<HomeAppsListUiState> = combine(
        settingsRepository.settings,
        facetRepository.observeFacets(),
        favoritesFlow,
        combine(homeWallpaper, getInstalledApps.observe(), preferredPreviewPackages, ::Triple),
    ) { settings, facets, favorites, previewInputs ->
        val (wallpaper, installed, preferred) = previewInputs
        HomeAppsListUiState(
            facet = facets.find { it.id == facetId },
            facets = facets.toImmutableList(),
            settings = settings,
            favorites = favorites.toImmutableList(),
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
        val id = facetId ?: return
        viewModelScope.launch { placementRepository.reorderItems(PlacementTarget(PlacementList.FAVORITES, id), orderedItems) }
    }

    /** "Copy to facets": replaces the content mode, count and list look of each chosen facet with the launcher-wide ones. Favorites are not touched. */
    fun copyToFacets(facetIds: Set<Long>) {
        viewModelScope.launch {
            facetIds.forEach {
                applyFacetDefaults(it, FacetDefaultsScope.APPS_LIST)
                applyFacetDefaults(it, FacetDefaultsScope.LOOK)
            }
        }
    }

    /** "Reset to default": replaces this facet's content mode, count and list look with the launcher-wide ones. Its Favorites are not touched. */
    fun resetToDefault() {
        val id = facetId ?: return
        viewModelScope.launch {
            applyFacetDefaults(id, FacetDefaultsScope.APPS_LIST)
            applyFacetDefaults(id, FacetDefaultsScope.LOOK)
        }
    }

    /** Facet-or-global write: a facet's own value, or the launcher-wide default a new facet is seeded from. */
    private fun writeLook(onFacet: suspend (Long) -> Unit, onGlobal: suspend () -> Unit) {
        viewModelScope.launch {
            facetId?.let { pid -> facetRepository.getById(pid)?.let { onFacet(it.id) } } ?: onGlobal()
        }
    }

    fun setAppRowPosition(position: AppRowPosition) = writeLook({ facetRepository.setAppRowPosition(it, position) }, { settingsRepository.setAppRowPosition(position) })

    fun setAppRowPresentation(presentation: AppRowPresentation) =
        writeLook({ facetRepository.setAppRowPresentation(it, presentation) }, { settingsRepository.setAppRowPresentation(presentation) })

    fun setAppListVerticalAlignment(alignment: AppListVerticalAlignment) =
        writeLook({ facetRepository.setAppListVerticalAlignment(it, alignment) }, { settingsRepository.setAppListVerticalAlignment(alignment) })

    fun setAppListLayout(layout: AppListLayout) = writeLook({ facetRepository.setAppListLayout(it, layout) }, { settingsRepository.setAppListLayout(layout) })

    fun setAppListColumnAlignment(alignment: AppListColumnAlignment) =
        writeLook({ facetRepository.setAppListColumnAlignment(it, alignment) }, { settingsRepository.setAppListColumnAlignment(alignment) })

    fun setAppListGridColumns(columns: AppListGridColumns) =
        writeLook({ facetRepository.setAppListGridColumns(it, columns) }, { settingsRepository.setAppListGridColumns(columns) })

    fun setAppListGridDisplayMode(mode: AppListGridDisplayMode) =
        writeLook({ facetRepository.setAppListGridDisplayMode(it, mode) }, { settingsRepository.setAppListGridDisplayMode(mode) })

    fun setAppListShrinkToFit(shrink: AppListShrinkToFit) =
        writeLook({ facetRepository.setAppListShrinkToFit(it, shrink) }, { settingsRepository.setAppListShrinkToFit(shrink) })

    fun setAllowLandscape(allow: Boolean) {
        viewModelScope.launch { settingsRepository.setAllowLandscape(allow) }
    }

    fun setLandscapeAppRowPosition(position: AppRowPosition?) = updateLandscape { it.copy(appRowPosition = position) }

    fun setLandscapeAppListVerticalAlignment(alignment: AppListVerticalAlignment?) = updateLandscape { it.copy(appListVerticalAlignment = alignment) }

    fun setLandscapeAppListColumnAlignment(alignment: AppListColumnAlignment?) = updateLandscape { it.copy(appListColumnAlignment = alignment) }

    /** This scope's landscape values: the facet's own when facet-scoped, the launcher-wide ones otherwise. */
    private fun updateLandscape(transform: (LandscapeOverrides) -> LandscapeOverrides) =
        writeLook({ facetRepository.updateLandscape(it, transform) }, { settingsRepository.updateLandscape(transform) })
}
