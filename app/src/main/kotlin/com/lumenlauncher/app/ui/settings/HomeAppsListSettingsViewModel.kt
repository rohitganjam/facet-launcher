package com.lumenlauncher.app.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lumenlauncher.app.data.DefaultFavoriteAppRepository
import com.lumenlauncher.app.data.SettingsRepository
import com.lumenlauncher.app.data.model.AppInfo
import com.lumenlauncher.app.data.model.AppRowPosition
import com.lumenlauncher.app.data.model.AppRowPresentation
import com.lumenlauncher.app.data.model.LauncherSettings
import com.lumenlauncher.app.data.model.ListContentMode
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class HomeAppsListUiState(
    val settings: LauncherSettings = LauncherSettings(),
    val defaultFavorites: List<AppInfo> = emptyList(),
)

/** Settings → Home & Apps → Home Apps List — split out of the combined "Dock & Apps List" screen (see chat history: Dock and the Home apps list are different decisions — device chrome vs. content — even though they used to share one screen). Every field here is global only — the default every profile inherits unless it sets its own override. */
@HiltViewModel
class HomeAppsListSettingsViewModel @Inject constructor(
    private val settingsRepository: SettingsRepository,
    private val defaultFavoriteAppRepository: DefaultFavoriteAppRepository,
) : ViewModel() {

    val uiState: StateFlow<HomeAppsListUiState> = combine(
        settingsRepository.settings,
        defaultFavoriteAppRepository.observeDefaultFavorites(),
    ) { settings, defaultFavorites ->
        HomeAppsListUiState(settings = settings, defaultFavorites = defaultFavorites)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), HomeAppsListUiState())

    /** The default every profile inherits unless it sets its own override. */
    fun setAppRowPosition(position: AppRowPosition) {
        viewModelScope.launch { settingsRepository.setAppRowPosition(position) }
    }

    /** The default every profile inherits unless it sets its own override. */
    fun setAppRowPresentation(presentation: AppRowPresentation) {
        viewModelScope.launch { settingsRepository.setAppRowPresentation(presentation) }
    }

    /** The default every profile inherits unless it sets its own override. */
    fun setListContentMode(mode: ListContentMode) {
        viewModelScope.launch { settingsRepository.setListContentMode(mode) }
    }

    fun setAppsToShowCount(count: Int) {
        viewModelScope.launch { settingsRepository.setAppsToShowCount(count) }
    }

    fun reorderDefaultFavorites(orderedApps: List<AppInfo>) {
        viewModelScope.launch { defaultFavoriteAppRepository.reorderFavorites(orderedApps) }
    }
}
