package com.lumenlauncher.app.ui.onboarding

import android.content.Intent
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lumenlauncher.app.data.DefaultFavoriteAppRepository
import com.lumenlauncher.app.data.DefaultLauncherRepository
import com.lumenlauncher.app.data.DockAppRepository
import com.lumenlauncher.app.data.SettingsRepository
import com.lumenlauncher.app.data.WallpaperRepository
import com.lumenlauncher.app.data.model.AppInfo
import com.lumenlauncher.app.data.model.HomeWallpaper
import com.lumenlauncher.app.data.model.ListContentMode
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/**
 * Backs the whole onboarding flow (`ui/onboarding/OnboardingScreen.kt`) — one instance shared
 * across all four steps (obtained once at the host level, same as [com.lumenlauncher.app.ui.launcher.HomeDrawerRoute]
 * obtains its child ViewModels once and passes state down as stateless parameters). Only the
 * home-setup step (step 2) mutates anything, and only the dock/favorites reordering and content
 * mode — adding/removing apps happens on the full-screen pickers reused from Settings, which own
 * their own ViewModels and write directly to [dockAppRepository]/[defaultFavoriteAppRepository].
 */
@HiltViewModel
class OnboardingViewModel @Inject constructor(
    private val defaultFavoriteAppRepository: DefaultFavoriteAppRepository,
    private val dockAppRepository: DockAppRepository,
    private val settingsRepository: SettingsRepository,
    private val wallpaperRepository: WallpaperRepository,
    private val defaultLauncherRepository: DefaultLauncherRepository,
) : ViewModel() {

    private val homeWallpaper = MutableStateFlow<HomeWallpaper>(HomeWallpaper.Unavailable)
    private val isDefaultLauncher = MutableStateFlow(false)

    val uiState: StateFlow<OnboardingUiState> = combine(
        defaultFavoriteAppRepository.observeDefaultFavorites(),
        dockAppRepository.observeDockApps(),
        homeWallpaper,
        settingsRepository.settings,
        isDefaultLauncher,
    ) { favorites, dockApps, wallpaper, settings, isDefault ->
        OnboardingUiState(
            dockApps = dockApps,
            favoriteApps = favorites,
            listContentMode = settings.listContentMode,
            appsToShowCount = settings.appsToShowCount,
            homeWallpaper = wallpaper,
            dockDisplayMode = settings.dockDisplayMode,
            appLabelColorOption = settings.appLabelColorOption,
            homeAppsFontWeight = settings.homeAppsFontWeight,
            isDefaultLauncher = isDefault,
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), OnboardingUiState())

    init {
        viewModelScope.launch { homeWallpaper.value = wallpaperRepository.currentHomeWallpaper() }
        viewModelScope.launch { isDefaultLauncher.value = defaultLauncherRepository.isDefaultLauncher() }
    }

    /** Set-as-default step's primary action target — see [DefaultLauncherRepository.requestDefaultLauncherIntent]. */
    fun requestDefaultLauncherIntent(): Intent = defaultLauncherRepository.requestDefaultLauncherIntent()

    fun setListContentMode(mode: ListContentMode) {
        viewModelScope.launch { settingsRepository.setListContentMode(mode) }
    }

    fun setAppsToShowCount(count: Int) {
        viewModelScope.launch { settingsRepository.setAppsToShowCount(count) }
    }

    fun reorderDockApps(orderedApps: List<AppInfo>) {
        viewModelScope.launch { dockAppRepository.reorderDockApps(orderedApps) }
    }

    fun reorderFavorites(orderedApps: List<AppInfo>) {
        viewModelScope.launch { defaultFavoriteAppRepository.reorderFavorites(orderedApps) }
    }
}
