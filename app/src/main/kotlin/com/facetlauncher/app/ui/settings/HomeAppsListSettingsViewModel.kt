package com.facetlauncher.app.ui.settings

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.facetlauncher.app.data.DefaultAppRepository
import com.facetlauncher.app.data.DefaultFavoriteAppRepository
import com.facetlauncher.app.data.FavoriteAppRepository
import com.facetlauncher.app.data.FacetRepository
import com.facetlauncher.app.data.SettingsRepository
import com.facetlauncher.app.data.WallpaperRepository
import com.facetlauncher.app.data.local.FacetEntity
import com.facetlauncher.app.data.local.resolveSentinel
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
import com.facetlauncher.app.data.model.NO_ACTIVE_FACET_ID
import com.facetlauncher.app.data.model.PlacedItem
import com.facetlauncher.app.domain.GetInstalledAppsUseCase
import com.facetlauncher.app.domain.SelectPreviewAppsUseCase
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

/** Appearance preview card home-row + dock-icon slots — how many recognizable apps to surface. */
private const val PREVIEW_APP_COUNT = 3

/**
 * `global*`/`facet*` fields are the raw stored values; the effective, screen-bound properties
 * below resolve facet-or-global the same way `HomeUiState`/`FacetCarouselViewModel` do at
 * runtime — this facet's own value while [isOverriding], the global default otherwise — matching
 * [com.facetlauncher.app.ui.settings.CalendarSettingsUiState]'s shape.
 */
data class HomeAppsListUiState(
    val facet: FacetEntity? = null,
    val globalAppRowPosition: AppRowPosition = AppRowPosition.LEFT,
    val globalAppRowPresentation: AppRowPresentation = AppRowPresentation.ICON_AND_TEXT,
    val globalListContentMode: ListContentMode = ListContentMode.FAVORITES,
    val globalAppsToShowCount: Int = AppListLimits.DEFAULT_APPS_TO_SHOW,
    val globalAppListVerticalAlignment: AppListVerticalAlignment = AppListVerticalAlignment.BOTTOM,
    val globalAppListLayout: AppListLayout = AppListLayout.SINGLE_COLUMN,
    val globalAppListColumnAlignment: AppListColumnAlignment = AppListColumnAlignment.BOTH_LEFT,
    val globalAppListGridColumns: AppListGridColumns = AppListGridColumns.FOUR,
    val globalAppListGridDisplayMode: AppListGridDisplayMode = AppListGridDisplayMode.ICONS,
    /** The launcher-wide default Favorites list — shown (read-only) while inheriting, and copied into the facet's own list the moment it starts overriding with an empty list. */
    val globalFavorites: List<PlacedItem> = emptyList(),
    /** This facet's own favorites — only meaningful/edited while [isOverriding]. */
    val facetFavorites: List<PlacedItem> = emptyList(),
    /** What the preview renders in a non-Favorites mode — a recognizable app sample, independent of facet/override. */
    val previewSourceApps: List<PlacedItem> = emptyList(),
    val homeWallpaper: HomeWallpaper = HomeWallpaper.Unavailable,
    /** Home's app label color/weight — global only, no per-facet override; drives the preview's label rendering. */
    val appLabelColorOption: ClockColorOption = ClockColorOption.THEME,
    val homeAppsFontWeight: FontWeightOption = FontWeightOption.REGULAR,
) {
    val isFacetScoped: Boolean get() = facet != null
    val isOverriding: Boolean get() = facet?.overrideApps ?: false
    /** Look, not content — edited from Settings → Appearance now, resolved independently of [isOverriding] via its own `LAUNCHER_DEFAULT` sentinel (see [com.facetlauncher.app.data.local.resolveSentinel]). Still read here since this screen's own preview needs it. */
    val appRowPosition: AppRowPosition get() = facet.resolveSentinel({ it.appRowPosition }, AppRowPosition.LAUNCHER_DEFAULT, globalAppRowPosition)
    val appRowPresentation: AppRowPresentation get() = facet.resolveSentinel({ it.appRowPresentation }, AppRowPresentation.LAUNCHER_DEFAULT, globalAppRowPresentation)
    val listContentMode: ListContentMode get() = if (isOverriding) facet?.listContentMode ?: globalListContentMode else globalListContentMode
    val appsToShowCount: Int get() = if (isOverriding) facet?.appsToShowCount ?: globalAppsToShowCount else globalAppsToShowCount
    val appListVerticalAlignment: AppListVerticalAlignment get() = facet.resolveSentinel({ it.appListVerticalAlignment }, AppListVerticalAlignment.LAUNCHER_DEFAULT, globalAppListVerticalAlignment)
    val appListLayout: AppListLayout get() = facet.resolveSentinel({ it.appListLayout }, AppListLayout.LAUNCHER_DEFAULT, globalAppListLayout)
    val appListColumnAlignment: AppListColumnAlignment get() = facet.resolveSentinel({ it.appListColumnAlignment }, AppListColumnAlignment.LAUNCHER_DEFAULT, globalAppListColumnAlignment)
    val appListGridColumns: AppListGridColumns get() = facet.resolveSentinel({ it.appListGridColumns }, AppListGridColumns.LAUNCHER_DEFAULT, globalAppListGridColumns)
    val appListGridDisplayMode: AppListGridDisplayMode get() = facet.resolveSentinel({ it.appListGridDisplayMode }, AppListGridDisplayMode.LAUNCHER_DEFAULT, globalAppListGridDisplayMode)
    /** This scope's effective favorites — the facet's own while overriding, the launcher-wide default otherwise. Apps and folders interleaved, same as Home's own list. */
    val favorites: List<PlacedItem> get() = if (isOverriding) facetFavorites else globalFavorites
    val favoritesLabel: String get() = "${favorites.size} of ${DefaultFavoriteAppRepository.MAX_FAVORITES}"
    /** What the preview renders — [favorites] in Favorites mode, a recognizable app sample otherwise. */
    val previewApps: List<PlacedItem> get() = if (listContentMode == ListContentMode.FAVORITES) favorites else previewSourceApps
}

/**
 * Settings → Home & Apps → Home Apps List, and (with a `facetId`) a facet's own Apps-list
 * screen — reused the same way [com.facetlauncher.app.ui.home.clock.ClockStyleGalleryViewModel]
 * is reused for the global and per-facet clock gallery. No `facetId` (or
 * [NO_ACTIVE_FACET_ID]) edits the launcher-wide default; a real id edits that facet's own
 * row and favorites, always. The Inherit/Override switch (moved in from
 * `FacetSettingsScreen` — see chat history) lives on this screen itself now.
 */
@HiltViewModel
class HomeAppsListSettingsViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val settingsRepository: SettingsRepository,
    private val facetRepository: FacetRepository,
    private val favoriteAppRepository: FavoriteAppRepository,
    private val defaultFavoriteAppRepository: DefaultFavoriteAppRepository,
    private val wallpaperRepository: WallpaperRepository,
    private val defaultAppRepository: DefaultAppRepository,
    getInstalledApps: GetInstalledAppsUseCase,
    private val selectPreviewApps: SelectPreviewAppsUseCase,
) : ViewModel() {

    private val facetId: Long? = savedStateHandle.get<Long>("facetId")?.takeIf { it != NO_ACTIVE_FACET_ID }

    private val homeWallpaper = MutableStateFlow<HomeWallpaper>(HomeWallpaper.Unavailable)
    private val preferredPreviewPackages = MutableStateFlow<List<String>>(emptyList())

    private val facetFavoritesFlow: Flow<List<PlacedItem>> = facetId?.let { favoriteAppRepository.observeFavoriteItems(it) } ?: flowOf(emptyList())
    private val globalFavoritesFlow: Flow<List<PlacedItem>> = defaultFavoriteAppRepository.observeDefaultItems()

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
            globalAppRowPosition = settings.appRowPosition,
            globalAppRowPresentation = settings.appRowPresentation,
            globalListContentMode = settings.listContentMode,
            globalAppsToShowCount = settings.appsToShowCount,
            globalAppListVerticalAlignment = settings.appListVerticalAlignment,
            globalAppListLayout = settings.appListLayout,
            globalAppListColumnAlignment = settings.appListColumnAlignment,
            globalAppListGridColumns = settings.appListGridColumns,
            globalAppListGridDisplayMode = settings.appListGridDisplayMode,
            globalFavorites = globalFavorites,
            facetFavorites = facetFavorites,
            previewSourceApps = selectPreviewApps(installed, preferred, PREVIEW_APP_COUNT).map { PlacedItem.SingleApp(it) },
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
            facetId?.let { pid -> facetRepository.getById(pid)?.let { facetRepository.setListContentMode(it, mode) } }
                ?: settingsRepository.setListContentMode(mode)
        }
    }

    fun setAppsToShowCount(count: Int) {
        viewModelScope.launch {
            facetId?.let { pid -> facetRepository.getById(pid)?.let { facetRepository.setAppsToShowCount(it, count) } }
                ?: settingsRepository.setAppsToShowCount(count)
        }
    }

    fun reorderFavorites(orderedItems: List<PlacedItem>) {
        viewModelScope.launch {
            facetId?.let { favoriteAppRepository.reorderFavoriteItems(it, orderedItems) }
                ?: defaultFavoriteAppRepository.reorderItems(orderedItems)
        }
    }

    /**
     * The Apps card's single Inherit/Override switch — governs list content mode, apps-to-show,
     * and favorites as one unit now (position/presentation/vertical alignment moved to Settings →
     * Appearance, resolved independently via their own `LAUNCHER_DEFAULT` sentinel — see chat
     * history). Moved in from `FacetSettingsViewModel` so it sits on the same screen as the
     * controls it gates. Switching to Override seeds mode/count with the current *effective*
     * values (`uiState.value`'s own properties, which still resolve to the global default at the
     * moment this runs) — including copying the default favorites list in if the facet's own is
     * empty, so the card doesn't suddenly go blank. Switching back to Inherit just clears the flag.
     */
    fun setOverriding(overriding: Boolean) {
        val pid = facetId ?: return
        val state = uiState.value
        viewModelScope.launch {
            val facet = facetRepository.getById(pid) ?: return@launch
            if (overriding && state.facetFavorites.isEmpty()) {
                favoriteAppRepository.replaceItems(pid, state.globalFavorites)
            }
            facetRepository.updateOverridingApps(
                facet = facet,
                overriding = overriding,
                mode = state.listContentMode,
                count = state.appsToShowCount,
                overridingFavorites = overriding,
            )
        }
    }
}
