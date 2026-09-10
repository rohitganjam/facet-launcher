package com.lumenlauncher.app.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lumenlauncher.app.data.DefaultAppRepository
import com.lumenlauncher.app.data.SettingsRepository
import com.lumenlauncher.app.data.WallpaperRepository
import com.lumenlauncher.app.data.model.AppInfo
import com.lumenlauncher.app.data.model.HomeWallpaper
import com.lumenlauncher.app.data.model.ClockColorOption
import com.lumenlauncher.app.data.model.FontWeightOption
import com.lumenlauncher.app.data.model.IconRenderMode
import com.lumenlauncher.app.data.model.LauncherFontOption
import com.lumenlauncher.app.data.model.LauncherSettings
import com.lumenlauncher.app.data.model.ThemeMode
import com.lumenlauncher.app.data.model.WallpaperAccentRole
import com.lumenlauncher.app.domain.GetInstalledAppsUseCase
import com.lumenlauncher.app.domain.SelectPreviewAppsUseCase
import com.lumenlauncher.app.ui.theme.AccentSwatch
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/** Appearance screen's preview card home-row (2) + dock-icon (3) slots — see [AppearanceSettingsViewModel.previewApps]. */
private const val PREVIEW_APP_COUNT = 5

/**
 * Settings → Appearance (`3c`'s theme/accent/icons/font block, split into its own screen — see
 * chat history: the main Settings list was getting too long to scan). Every field here is global
 * only — no per-profile override exists for these, unlike Clock/Calendar/Apps.
 */
@HiltViewModel
class AppearanceSettingsViewModel @Inject constructor(
    private val settingsRepository: SettingsRepository,
    getInstalledApps: GetInstalledAppsUseCase,
    private val defaultAppRepository: DefaultAppRepository,
    private val wallpaperRepository: WallpaperRepository,
    selectPreviewApps: SelectPreviewAppsUseCase,
) : ViewModel() {

    val settings: StateFlow<LauncherSettings> = settingsRepository.settings
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), LauncherSettings())

    private val _homeWallpaper = MutableStateFlow<HomeWallpaper>(HomeWallpaper.Unavailable)

    /** The live system wallpaper, painted behind the preview card so it reads as real Home,
     *  not a flat slab. Loaded once when the screen opens. */
    val homeWallpaper: StateFlow<HomeWallpaper> = _homeWallpaper

    /** Seeded once from [DefaultAppRepository.getDefaultAppPackages] — this device's default-app
     * picks don't change mid-session, so a one-shot fetch (same pattern as
     * [com.lumenlauncher.app.ui.dock.DockAppPickerViewModel]'s own installed-apps seed) is enough. */
    private val preferredPreviewPackages = MutableStateFlow<List<String>>(emptyList())

    /** Backs the Appearance preview card's sample rows/dock icons with the device's own real
     * installed apps instead of placeholder objects — live via [GetInstalledAppsUseCase.observe]
     * (same source the App Drawer itself reads from), biased toward recognizable apps first via
     * [SelectPreviewAppsUseCase]. */
    val previewApps: StateFlow<List<AppInfo>> = combine(getInstalledApps.observe(), preferredPreviewPackages) { installed, preferred ->
        selectPreviewApps(installed, preferred, PREVIEW_APP_COUNT)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    init {
        viewModelScope.launch { preferredPreviewPackages.value = defaultAppRepository.getDefaultAppPackages() }
        viewModelScope.launch { _homeWallpaper.value = wallpaperRepository.currentHomeWallpaper() }
    }

    fun setThemeMode(mode: ThemeMode) {
        viewModelScope.launch { settingsRepository.setThemeMode(mode) }
    }

    fun setAccentFromSystem(enabled: Boolean) {
        viewModelScope.launch { settingsRepository.setAccentFromSystem(enabled) }
    }

    fun setCustomAccentSwatch(swatch: AccentSwatch) {
        viewModelScope.launch { settingsRepository.setCustomAccentSwatch(swatch.name) }
    }

    fun setWallpaperAccentRole(role: WallpaperAccentRole) {
        viewModelScope.launch { settingsRepository.setWallpaperAccentRole(role) }
    }

    fun setIconRenderMode(mode: IconRenderMode) {
        viewModelScope.launch { settingsRepository.setIconRenderMode(mode) }
    }

    fun setLauncherFontOption(option: LauncherFontOption) {
        viewModelScope.launch { settingsRepository.setLauncherFontOption(option) }
    }

    fun setAppLabelColorOption(option: ClockColorOption) {
        viewModelScope.launch { settingsRepository.setAppLabelColorOption(option) }
    }

    fun setHomeAppsFontWeight(weight: FontWeightOption) {
        viewModelScope.launch { settingsRepository.setHomeAppsFontWeight(weight) }
    }
}
