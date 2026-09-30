package com.facetlauncher.app.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.facetlauncher.app.data.SettingsRepository
import com.facetlauncher.app.data.model.DrawerFolderDisplayMode
import com.facetlauncher.app.data.model.DrawerGridSize
import com.facetlauncher.app.data.model.DrawerListItemSize
import com.facetlauncher.app.data.model.DrawerPresentation
import com.facetlauncher.app.data.model.LauncherSettings
import com.facetlauncher.app.data.model.RecentlyInstalledPosition
import com.facetlauncher.app.data.model.SearchBarPosition
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/** Settings → Home & Apps → App Drawer — split out of the main Settings list into its own screen (see chat history: the main list was getting too long to scan). Every field here is global only, no per-facet override. */
@HiltViewModel
class AppDrawerSettingsViewModel @Inject constructor(
    private val settingsRepository: SettingsRepository,
) : ViewModel() {

    val settings: StateFlow<LauncherSettings> = settingsRepository.settings
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), LauncherSettings())

    fun setDrawerPresentation(presentation: DrawerPresentation) {
        viewModelScope.launch { settingsRepository.setDrawerPresentation(presentation) }
    }

    fun setDrawerGridSize(gridSize: DrawerGridSize) {
        viewModelScope.launch { settingsRepository.setDrawerGridSize(gridSize) }
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
