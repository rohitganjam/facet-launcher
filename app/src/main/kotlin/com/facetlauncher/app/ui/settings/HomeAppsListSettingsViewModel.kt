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
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/** Appearance preview card home-row + dock-icon slots — how many recognizable apps to surface. */
private const val PREVIEW_APP_COUNT = 3

data class HomeAppsListUiState(
    /** `false` when editing the launcher-wide default list (reached from Settings) rather than one facet's own. */
    val isFacetScoped: Boolean = false,
    val appRowPosition: AppRowPosition = AppRowPosition.LEFT,
    val appRowPresentation: AppRowPresentation = AppRowPresentation.ICON_AND_TEXT,
    val listContentMode: ListContentMode = ListContentMode.FAVORITES,
    val appsToShowCount: Int = AppListLimits.DEFAULT_APPS_TO_SHOW,
    val appListVerticalAlignment: AppListVerticalAlignment = AppListVerticalAlignment.BOTTOM,
    /** This scope's favorites — the facet's own when scoped, the launcher-wide default list otherwise. Apps and folders interleaved, same as Home's own list. */
    val favorites: List<PlacedItem> = emptyList(),
    /** What the preview renders — [favorites] in Favorites mode, a recognizable app sample otherwise. */
    val previewApps: List<PlacedItem> = emptyList(),
    val homeWallpaper: HomeWallpaper = HomeWallpaper.Unavailable,
    /** Home's app label color/weight — global only, no per-facet override; drives the preview's label rendering. */
    val appLabelColorOption: ClockColorOption = ClockColorOption.THEME,
    val homeAppsFontWeight: FontWeightOption = FontWeightOption.REGULAR,
) {
    val favoritesLabel: String get() = "${favorites.size} of ${DefaultFavoriteAppRepository.MAX_FAVORITES}"
}

/**
 * Settings → Home & Apps → Home Apps List, and (with a `facetId`) a facet's own Apps-list
 * screen — reused the same way [com.facetlauncher.app.ui.home.clock.ClockStyleGalleryViewModel]
 * is reused for the global and per-facet clock gallery. No `facetId` (or
 * [NO_ACTIVE_FACET_ID]) edits the launcher-wide default; a real id edits that facet's own
 * row and favorites, always — the Inherit/Override switch that decides whether those actually
 * apply lives on [com.facetlauncher.app.ui.facets.FacetSettingsScreen].
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

    private val favoritesFlow: Flow<List<PlacedItem>> =
        facetId?.let { favoriteAppRepository.observeFavoriteItems(it) } ?: defaultFavoriteAppRepository.observeDefaultItems()

    val uiState: StateFlow<HomeAppsListUiState> = combine(
        settingsRepository.settings,
        facetRepository.observeFacets(),
        favoritesFlow,
        combine(homeWallpaper, getInstalledApps.observe(), preferredPreviewPackages, ::Triple),
    ) { settings, facets, favorites, previewInputs ->
        val (wallpaper, installed, preferred) = previewInputs
        val facet = facets.find { it.id == facetId }
        val position = facet?.appRowPosition ?: settings.appRowPosition
        val presentation = facet?.appRowPresentation ?: settings.appRowPresentation
        val mode = facet?.listContentMode ?: settings.listContentMode
        val count = facet?.appsToShowCount ?: settings.appsToShowCount
        val alignment = facet?.appListVerticalAlignment ?: settings.appListVerticalAlignment
        HomeAppsListUiState(
            isFacetScoped = facetId != null,
            appRowPosition = position,
            appRowPresentation = presentation,
            listContentMode = mode,
            appsToShowCount = count,
            appListVerticalAlignment = alignment,
            favorites = favorites,
            previewApps = if (mode == ListContentMode.FAVORITES) {
                favorites
            } else {
                selectPreviewApps(installed, preferred, PREVIEW_APP_COUNT).map { PlacedItem.SingleApp(it) }
            },
            homeWallpaper = wallpaper,
            appLabelColorOption = settings.appLabelColorOption,
            homeAppsFontWeight = settings.homeAppsFontWeight,
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), HomeAppsListUiState())

    init {
        viewModelScope.launch { homeWallpaper.value = wallpaperRepository.currentHomeWallpaper() }
        viewModelScope.launch { preferredPreviewPackages.value = defaultAppRepository.getDefaultAppPackages() }
    }

    fun setAppRowPosition(position: AppRowPosition) {
        viewModelScope.launch {
            facetId?.let { pid -> facetRepository.getById(pid)?.let { facetRepository.setAppRowPosition(it, position) } }
                ?: settingsRepository.setAppRowPosition(position)
        }
    }

    fun setAppRowPresentation(presentation: AppRowPresentation) {
        viewModelScope.launch {
            facetId?.let { pid -> facetRepository.getById(pid)?.let { facetRepository.setAppRowPresentation(it, presentation) } }
                ?: settingsRepository.setAppRowPresentation(presentation)
        }
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

    fun setAppListVerticalAlignment(alignment: AppListVerticalAlignment) {
        viewModelScope.launch {
            facetId?.let { pid -> facetRepository.getById(pid)?.let { facetRepository.setAppListVerticalAlignment(it, alignment) } }
                ?: settingsRepository.setAppListVerticalAlignment(alignment)
        }
    }

    fun reorderFavorites(orderedItems: List<PlacedItem>) {
        viewModelScope.launch {
            facetId?.let { favoriteAppRepository.reorderFavoriteItems(it, orderedItems) }
                ?: defaultFavoriteAppRepository.reorderItems(orderedItems)
        }
    }
}
