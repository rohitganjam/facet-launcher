package com.lumenlauncher.app.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lumenlauncher.app.data.DefaultFavoriteAppRepository
import com.lumenlauncher.app.data.DefaultLauncherRepository
import com.lumenlauncher.app.data.DockAppRepository
import com.lumenlauncher.app.data.SettingsRepository
import com.lumenlauncher.app.data.model.AppInfo
import com.lumenlauncher.app.data.model.DockDisplayMode
import com.lumenlauncher.app.data.model.DrawerGridSize
import com.lumenlauncher.app.data.model.DrawerListItemSize
import com.lumenlauncher.app.data.model.DrawerPresentation
import com.lumenlauncher.app.data.model.IconRenderMode
import com.lumenlauncher.app.data.model.ListContentMode
import com.lumenlauncher.app.data.model.SearchBarPosition
import com.lumenlauncher.app.data.model.ThemeMode
import com.lumenlauncher.app.domain.ObserveSettingsScreenStateUseCase
import com.lumenlauncher.app.ui.theme.AccentSwatch
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@HiltViewModel
class SettingsViewModel @Inject constructor(
    observeSettingsScreenState: ObserveSettingsScreenStateUseCase,
    private val settingsRepository: SettingsRepository,
    private val dockAppRepository: DockAppRepository,
    private val defaultFavoriteAppRepository: DefaultFavoriteAppRepository,
    private val defaultLauncherRepository: DefaultLauncherRepository,
) : ViewModel() {

    private val isDefaultLauncher = MutableStateFlow(false)

    val uiState: StateFlow<SettingsUiState> = combine(
        observeSettingsScreenState(),
        isDefaultLauncher,
    ) { screenState, isDefaultLauncher ->
        SettingsUiState(
            settings = screenState.settings,
            dockApps = screenState.dockApps,
            defaultFavorites = screenState.defaultFavorites,
            isDefaultLauncher = isDefaultLauncher,
            isLoading = false,
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), SettingsUiState())

    init {
        viewModelScope.launch {
            isDefaultLauncher.value = defaultLauncherRepository.isDefaultLauncher()
        }
    }

    fun setDockDisplayMode(mode: DockDisplayMode) {
        viewModelScope.launch { settingsRepository.setDockDisplayMode(mode) }
    }

    fun setShowDrawerIcons(enabled: Boolean) {
        viewModelScope.launch { settingsRepository.setShowDrawerIcons(enabled) }
    }

    fun setShowDrawerLabels(enabled: Boolean) {
        viewModelScope.launch { settingsRepository.setShowDrawerLabels(enabled) }
    }

    fun setDrawerPresentation(presentation: DrawerPresentation) {
        viewModelScope.launch { settingsRepository.setDrawerPresentation(presentation) }
    }

    fun setDrawerGridSize(gridSize: DrawerGridSize) {
        viewModelScope.launch { settingsRepository.setDrawerGridSize(gridSize) }
    }

    fun setDrawerListItemSize(itemSize: DrawerListItemSize) {
        viewModelScope.launch { settingsRepository.setDrawerListItemSize(itemSize) }
    }

    fun setDrawerOpacity(opacity: Float) {
        viewModelScope.launch { settingsRepository.setDrawerOpacity(opacity) }
    }

    fun setSearchBarPosition(position: SearchBarPosition) {
        viewModelScope.launch { settingsRepository.setSearchBarPosition(position) }
    }

    /** The toggle turning on triggers the real `READ_CONTACTS` request from the screen; denial reverts this back to `false`. */
    fun setSearchContactsEnabled(enabled: Boolean) {
        viewModelScope.launch { settingsRepository.setSearchContactsEnabled(enabled) }
    }

    fun setAccentFromSystem(enabled: Boolean) {
        viewModelScope.launch { settingsRepository.setAccentFromSystem(enabled) }
    }

    fun setCustomAccentSwatch(swatch: AccentSwatch) {
        viewModelScope.launch { settingsRepository.setCustomAccentSwatch(swatch.name) }
    }

    fun setIconRenderMode(mode: IconRenderMode) {
        viewModelScope.launch { settingsRepository.setIconRenderMode(mode) }
    }

    fun setThemeMode(mode: ThemeMode) {
        viewModelScope.launch { settingsRepository.setThemeMode(mode) }
    }

    fun reorderDockApps(orderedApps: List<AppInfo>) {
        viewModelScope.launch { dockAppRepository.reorderDockApps(orderedApps) }
    }

    /** The default every profile inherits unless it sets its own override. */
    fun setListContentMode(mode: ListContentMode) {
        viewModelScope.launch { settingsRepository.setListContentMode(mode) }
    }

    fun setAppsToShowCount(count: Int) {
        viewModelScope.launch { settingsRepository.setAppsToShowCount(count) }
    }

    fun canAddMoreDockApps(): Boolean = uiState.value.dockApps.size < DockAppRepository.MAX_APPS

    fun reorderDefaultFavorites(orderedApps: List<AppInfo>) {
        viewModelScope.launch { defaultFavoriteAppRepository.reorderFavorites(orderedApps) }
    }
}
