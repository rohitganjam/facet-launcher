package com.facetlauncher.app.ui.settings

import android.content.Intent
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.facetlauncher.app.data.DefaultLauncherRepository
import com.facetlauncher.app.domain.ObserveSettingsScreenStateUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/**
 * Backs the main Settings list — purely observational now that every mutable section (Appearance,
 * Dock, Home Apps List, App Drawer, ...) has its own dedicated screen/ViewModel; this one only
 * supplies the summary subtitles shown on each navigation row and tracks whether Facet is the
 * current default launcher.
 */
@HiltViewModel
class SettingsViewModel @Inject constructor(
    observeSettingsScreenState: ObserveSettingsScreenStateUseCase,
    private val defaultLauncherRepository: DefaultLauncherRepository,
) : ViewModel() {

    private val isDefaultLauncher = MutableStateFlow(false)

    val uiState: StateFlow<SettingsUiState> = combine(
        observeSettingsScreenState(),
        isDefaultLauncher,
    ) { screenState, isDefaultLauncher ->
        SettingsUiState(
            settings = screenState.settings,
            dockItems = screenState.dockItems,
            defaultFavorites = screenState.defaultFavorites,
            folderCount = screenState.folderCount,
            isDefaultLauncher = isDefaultLauncher,
            isLoading = false,
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), SettingsUiState())

    init {
        viewModelScope.launch {
            isDefaultLauncher.value = defaultLauncherRepository.isDefaultLauncher()
        }
    }

    /** The "Set as default launcher" row's target — an in-place role request when available, a Settings screen otherwise. See [DefaultLauncherRepository.requestDefaultLauncherIntent]. */
    fun requestDefaultLauncherIntent(): Intent = defaultLauncherRepository.requestDefaultLauncherIntent()
}
