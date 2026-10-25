package com.facetlauncher.app.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.facetlauncher.app.data.SettingsRepository
import com.facetlauncher.app.data.WallpaperRepository
import com.facetlauncher.app.data.model.DrawerFolderDisplayMode
import com.facetlauncher.app.data.model.DrawerGridDensity
import com.facetlauncher.app.data.model.DrawerListItemSize
import com.facetlauncher.app.data.model.DrawerPresentation
import com.facetlauncher.app.data.model.HomeWallpaper
import com.facetlauncher.app.data.model.LauncherSettings
import com.facetlauncher.app.data.model.RecentlyInstalledPosition
import com.facetlauncher.app.data.model.SearchBarPosition
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/** Settings → Appearance → App Drawer. Every field here is global only, no per-facet override. */
@HiltViewModel
class AppDrawerSettingsViewModel @Inject constructor(
    private val settingsRepository: SettingsRepository,
    private val wallpaperRepository: WallpaperRepository,
) : ViewModel() {

    val settings: StateFlow<LauncherSettings> = settingsRepository.settings
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), LauncherSettings())

    private val _homeWallpaper = MutableStateFlow<HomeWallpaper>(HomeWallpaper.Unavailable)

    /** The system wallpaper behind the drawer preview, so its opacity setting is visible. Loaded once. */
    val homeWallpaper: StateFlow<HomeWallpaper> = _homeWallpaper

    init {
        viewModelScope.launch { _homeWallpaper.value = wallpaperRepository.currentHomeWallpaper() }
    }

    fun setDrawerPresentation(presentation: DrawerPresentation) {
        viewModelScope.launch { settingsRepository.setDrawerPresentation(presentation) }
    }

    fun setDrawerGridDensity(density: DrawerGridDensity) {
        viewModelScope.launch { settingsRepository.setDrawerGridDensity(density) }
    }

    fun setDrawerListItemSize(itemSize: DrawerListItemSize) {
        viewModelScope.launch { settingsRepository.setDrawerListItemSize(itemSize) }
    }

    fun setShowDrawerIcons(enabled: Boolean) {
        viewModelScope.launch { settingsRepository.setShowDrawerIcons(enabled) }
    }

    fun setShowDrawerLabels(enabled: Boolean) {
        viewModelScope.launch { settingsRepository.setShowDrawerLabels(enabled) }
    }

    /** The toggle turning on triggers the real `READ_CONTACTS` request from the screen; denial reverts this back to `false`. */
    fun setSearchContactsEnabled(enabled: Boolean) {
        viewModelScope.launch { settingsRepository.setSearchContactsEnabled(enabled) }
    }

    fun setSearchSettingsEnabled(enabled: Boolean) {
        viewModelScope.launch { settingsRepository.setSearchSettingsEnabled(enabled) }
    }

    fun setSearchBarPosition(position: SearchBarPosition) {
        viewModelScope.launch { settingsRepository.setSearchBarPosition(position) }
    }

    fun setDrawerOpacity(opacity: Float) {
        viewModelScope.launch { settingsRepository.setDrawerOpacity(opacity) }
    }

    fun setDrawerFolderDisplayMode(mode: DrawerFolderDisplayMode) {
        viewModelScope.launch { settingsRepository.setDrawerFolderDisplayMode(mode) }
    }

    fun setRecentlyInstalledPosition(position: RecentlyInstalledPosition) {
        viewModelScope.launch { settingsRepository.setRecentlyInstalledPosition(position) }
    }
}
