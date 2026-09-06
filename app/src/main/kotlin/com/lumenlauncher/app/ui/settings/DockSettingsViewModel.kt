package com.lumenlauncher.app.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lumenlauncher.app.data.DockAppRepository
import com.lumenlauncher.app.data.SettingsRepository
import com.lumenlauncher.app.data.model.AppInfo
import com.lumenlauncher.app.data.model.DockDisplayMode
import com.lumenlauncher.app.data.model.LauncherSettings
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class DockSettingsUiState(
    val settings: LauncherSettings = LauncherSettings(),
    val dockApps: List<AppInfo> = emptyList(),
)

/** Settings → Home & Apps → Dock — split out of the combined "Dock & Apps List" screen (see chat history: Dock and the Home apps list are different decisions — device chrome vs. content — even though they used to share one screen). */
@HiltViewModel
class DockSettingsViewModel @Inject constructor(
    private val settingsRepository: SettingsRepository,
    private val dockAppRepository: DockAppRepository,
) : ViewModel() {

    val uiState: StateFlow<DockSettingsUiState> = combine(
        settingsRepository.settings,
        dockAppRepository.observeDockApps(),
    ) { settings, dockApps ->
        DockSettingsUiState(settings = settings, dockApps = dockApps)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), DockSettingsUiState())

    fun setDockDisplayMode(mode: DockDisplayMode) {
        viewModelScope.launch { settingsRepository.setDockDisplayMode(mode) }
    }

    fun reorderDockApps(orderedApps: List<AppInfo>) {
        viewModelScope.launch { dockAppRepository.reorderDockApps(orderedApps) }
    }
}
