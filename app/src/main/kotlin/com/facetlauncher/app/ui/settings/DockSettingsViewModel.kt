package com.facetlauncher.app.ui.settings

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.facetlauncher.app.data.DockAppRepository
import com.facetlauncher.app.data.ProfileDockAppRepository
import com.facetlauncher.app.data.ProfileRepository
import com.facetlauncher.app.data.SettingsRepository
import com.facetlauncher.app.data.WallpaperRepository
import com.facetlauncher.app.data.model.AppInfo
import com.facetlauncher.app.data.model.ClockColorOption
import com.facetlauncher.app.data.model.DockDisplayMode
import com.facetlauncher.app.data.model.FontWeightOption
import com.facetlauncher.app.data.model.HomeWallpaper
import com.facetlauncher.app.data.model.NO_ACTIVE_PROFILE_ID
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class DockSettingsUiState(
    /** `false` when editing the launcher-wide default dock (reached from Settings) rather than one profile's own. */
    val isProfileScoped: Boolean = false,
    val dockDisplayMode: DockDisplayMode = DockDisplayMode.ICONS,
    val dockApps: List<AppInfo> = emptyList(),
    /** Home's app label color/weight — global only, no per-profile override; used only to render the preview exactly as Home would. */
    val appLabelColorOption: ClockColorOption = ClockColorOption.THEME,
    val homeAppsFontWeight: FontWeightOption = FontWeightOption.REGULAR,
    val homeWallpaper: HomeWallpaper = HomeWallpaper.Unavailable,
)

/**
 * Settings → Dock, and (with a `profileId`) a profile's own Dock screen — reused the same way
 * [com.facetlauncher.app.ui.home.clock.ClockStyleGalleryViewModel] is reused for the global and
 * per-profile clock gallery. No `profileId` (or [NO_ACTIVE_PROFILE_ID]) edits the launcher-wide
 * default dock; a real id edits that profile's own [ProfileDockAppRepository] list and
 * `dockDisplayMode`, always — the Inherit/Override switch that decides whether those actually
 * apply lives on [com.facetlauncher.app.ui.profiles.ProfileSettingsScreen].
 */
@HiltViewModel
class DockSettingsViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val settingsRepository: SettingsRepository,
    private val profileRepository: ProfileRepository,
    private val dockAppRepository: DockAppRepository,
    private val profileDockAppRepository: ProfileDockAppRepository,
    private val wallpaperRepository: WallpaperRepository,
) : ViewModel() {

    private val profileId: Long? = savedStateHandle.get<Long>("profileId")?.takeIf { it != NO_ACTIVE_PROFILE_ID }

    private val homeWallpaper = MutableStateFlow<HomeWallpaper>(HomeWallpaper.Unavailable)

    private val dockAppsFlow =
        profileId?.let { profileDockAppRepository.observeDockAppsForProfile(it) } ?: dockAppRepository.observeDockApps()

    val uiState: StateFlow<DockSettingsUiState> = combine(
        settingsRepository.settings,
        profileRepository.observeProfiles(),
        dockAppsFlow,
        homeWallpaper,
    ) { settings, profiles, dockApps, wallpaper ->
        val profile = profiles.find { it.id == profileId }
        DockSettingsUiState(
            isProfileScoped = profileId != null,
            dockDisplayMode = profile?.dockDisplayMode ?: settings.dockDisplayMode,
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
            profileId?.let { pid ->
                profileRepository.getById(pid)?.let { profileRepository.setDockDisplayMode(it, mode) }
            } ?: settingsRepository.setDockDisplayMode(mode)
        }
    }

    fun reorderDockApps(orderedApps: List<AppInfo>) {
        viewModelScope.launch {
            profileId?.let { profileDockAppRepository.reorderDockApps(it, orderedApps) }
                ?: dockAppRepository.reorderDockApps(orderedApps)
        }
    }
}
