package com.facetlauncher.app.ui.settings

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.facetlauncher.app.data.DefaultAppRepository
import com.facetlauncher.app.data.DefaultFavoriteAppRepository
import com.facetlauncher.app.data.FavoriteAppRepository
import com.facetlauncher.app.data.ProfileRepository
import com.facetlauncher.app.data.SettingsRepository
import com.facetlauncher.app.data.WallpaperRepository
import com.facetlauncher.app.data.model.AppInfo
import com.facetlauncher.app.data.model.AppListLimits
import com.facetlauncher.app.data.model.AppListVerticalAlignment
import com.facetlauncher.app.data.model.AppRowPosition
import com.facetlauncher.app.data.model.AppRowPresentation
import com.facetlauncher.app.data.model.ClockColorOption
import com.facetlauncher.app.data.model.FontWeightOption
import com.facetlauncher.app.data.model.HomeWallpaper
import com.facetlauncher.app.data.model.ListContentMode
import com.facetlauncher.app.data.model.NO_ACTIVE_PROFILE_ID
import com.facetlauncher.app.domain.GetInstalledAppsUseCase
import com.facetlauncher.app.domain.SelectPreviewAppsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/** Appearance preview card home-row + dock-icon slots — how many recognizable apps to surface. */
private const val PREVIEW_APP_COUNT = 3

data class HomeAppsListUiState(
    /** `false` when editing the launcher-wide default list (reached from Settings) rather than one profile's own. */
    val isProfileScoped: Boolean = false,
    val appRowPosition: AppRowPosition = AppRowPosition.LEFT,
    val appRowPresentation: AppRowPresentation = AppRowPresentation.ICON_AND_TEXT,
    val listContentMode: ListContentMode = ListContentMode.FAVORITES,
    val appsToShowCount: Int = AppListLimits.DEFAULT_APPS_TO_SHOW,
    val appListVerticalAlignment: AppListVerticalAlignment = AppListVerticalAlignment.BOTTOM,
    /** This scope's favorites — the profile's own when scoped, the launcher-wide default list otherwise. */
    val favorites: List<AppInfo> = emptyList(),
    /** The real apps the preview renders — [favorites] in Favorites mode, a recognizable sample otherwise. */
    val previewApps: List<AppInfo> = emptyList(),
    val homeWallpaper: HomeWallpaper = HomeWallpaper.Unavailable,
    /** Home's app label color/weight — global only, no per-profile override; drives the preview's label rendering. */
    val appLabelColorOption: ClockColorOption = ClockColorOption.THEME,
    val homeAppsFontWeight: FontWeightOption = FontWeightOption.REGULAR,
) {
    val favoritesLabel: String get() = "${favorites.size} of ${DefaultFavoriteAppRepository.MAX_FAVORITES}"
}

/**
 * Settings → Home & Apps → Home Apps List, and (with a `profileId`) a profile's own Apps-list
 * screen — reused the same way [com.facetlauncher.app.ui.home.clock.ClockStyleGalleryViewModel]
 * is reused for the global and per-profile clock gallery. No `profileId` (or
 * [NO_ACTIVE_PROFILE_ID]) edits the launcher-wide default; a real id edits that profile's own
 * row and favorites, always — the Inherit/Override switch that decides whether those actually
 * apply lives on [com.facetlauncher.app.ui.profiles.ProfileSettingsScreen].
 */
@HiltViewModel
class HomeAppsListSettingsViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val settingsRepository: SettingsRepository,
    private val profileRepository: ProfileRepository,
    private val favoriteAppRepository: FavoriteAppRepository,
    private val defaultFavoriteAppRepository: DefaultFavoriteAppRepository,
    private val wallpaperRepository: WallpaperRepository,
    private val defaultAppRepository: DefaultAppRepository,
    getInstalledApps: GetInstalledAppsUseCase,
    private val selectPreviewApps: SelectPreviewAppsUseCase,
) : ViewModel() {

    private val profileId: Long? = savedStateHandle.get<Long>("profileId")?.takeIf { it != NO_ACTIVE_PROFILE_ID }

    private val homeWallpaper = MutableStateFlow<HomeWallpaper>(HomeWallpaper.Unavailable)
    private val preferredPreviewPackages = MutableStateFlow<List<String>>(emptyList())

    private val favoritesFlow =
        profileId?.let { favoriteAppRepository.observeFavoritesForProfile(it) } ?: defaultFavoriteAppRepository.observeDefaultFavorites()

    val uiState: StateFlow<HomeAppsListUiState> = combine(
        settingsRepository.settings,
        profileRepository.observeProfiles(),
        favoritesFlow,
        combine(homeWallpaper, getInstalledApps.observe(), preferredPreviewPackages, ::Triple),
    ) { settings, profiles, favorites, previewInputs ->
        val (wallpaper, installed, preferred) = previewInputs
        val profile = profiles.find { it.id == profileId }
        val position = profile?.appRowPosition ?: settings.appRowPosition
        val presentation = profile?.appRowPresentation ?: settings.appRowPresentation
        val mode = profile?.listContentMode ?: settings.listContentMode
        val count = profile?.appsToShowCount ?: settings.appsToShowCount
        val alignment = profile?.appListVerticalAlignment ?: settings.appListVerticalAlignment
        HomeAppsListUiState(
            isProfileScoped = profileId != null,
            appRowPosition = position,
            appRowPresentation = presentation,
            listContentMode = mode,
            appsToShowCount = count,
            appListVerticalAlignment = alignment,
            favorites = favorites,
            previewApps = if (mode == ListContentMode.FAVORITES) {
                favorites
            } else {
                selectPreviewApps(installed, preferred, PREVIEW_APP_COUNT)
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
            profileId?.let { pid -> profileRepository.getById(pid)?.let { profileRepository.setAppRowPosition(it, position) } }
                ?: settingsRepository.setAppRowPosition(position)
        }
    }

    fun setAppRowPresentation(presentation: AppRowPresentation) {
        viewModelScope.launch {
            profileId?.let { pid -> profileRepository.getById(pid)?.let { profileRepository.setAppRowPresentation(it, presentation) } }
                ?: settingsRepository.setAppRowPresentation(presentation)
        }
    }

    fun setListContentMode(mode: ListContentMode) {
        viewModelScope.launch {
            profileId?.let { pid -> profileRepository.getById(pid)?.let { profileRepository.setListContentMode(it, mode) } }
                ?: settingsRepository.setListContentMode(mode)
        }
    }

    fun setAppsToShowCount(count: Int) {
        viewModelScope.launch {
            profileId?.let { pid -> profileRepository.getById(pid)?.let { profileRepository.setAppsToShowCount(it, count) } }
                ?: settingsRepository.setAppsToShowCount(count)
        }
    }

    fun setAppListVerticalAlignment(alignment: AppListVerticalAlignment) {
        viewModelScope.launch {
            profileId?.let { pid -> profileRepository.getById(pid)?.let { profileRepository.setAppListVerticalAlignment(it, alignment) } }
                ?: settingsRepository.setAppListVerticalAlignment(alignment)
        }
    }

    fun reorderFavorites(orderedApps: List<AppInfo>) {
        viewModelScope.launch {
            profileId?.let { favoriteAppRepository.reorderFavorites(it, orderedApps) }
                ?: defaultFavoriteAppRepository.reorderFavorites(orderedApps)
        }
    }
}
