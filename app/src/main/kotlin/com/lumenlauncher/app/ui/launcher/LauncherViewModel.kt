package com.lumenlauncher.app.ui.launcher

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lumenlauncher.app.data.SettingsRepository
import com.lumenlauncher.app.data.model.AppInfo
import com.lumenlauncher.app.data.model.IconRenderMode
import com.lumenlauncher.app.data.model.ThemeMode
import com.lumenlauncher.app.domain.CleanUpUninstalledAppsUseCase
import com.lumenlauncher.app.domain.EnsureActiveProfileUseCase
import com.lumenlauncher.app.domain.GetInstalledAppsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

data class LauncherUiState(
    val apps: List<AppInfo> = emptyList(),
    val isLoading: Boolean = true,
    /** F11 — all threaded into [com.lumenlauncher.app.ui.theme.LumenLauncherTheme] so it resolves correctly app-wide. */
    val themeMode: ThemeMode = ThemeMode.SYSTEM,
    val accentFromSystem: Boolean = true,
    /** An `AccentSwatch` enum name — see [com.lumenlauncher.app.data.model.LauncherSettings.customAccentSwatch]. */
    val customAccentSwatch: String? = null,
    val iconRenderMode: IconRenderMode = IconRenderMode.SYSTEM_DEFAULT,
)

@HiltViewModel
class LauncherViewModel @Inject constructor(
    getInstalledApps: GetInstalledAppsUseCase,
    private val ensureActiveProfile: EnsureActiveProfileUseCase,
    private val cleanUpUninstalledApps: CleanUpUninstalledAppsUseCase,
    settingsRepository: SettingsRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(LauncherUiState())
    val uiState: StateFlow<LauncherUiState> = _uiState.asStateFlow()

    init {
        // Live, not one-shot — an install/uninstall/update while Lumen is in the foreground
        // (including via the app long-press menu's Uninstall action) must be reflected without
        // requiring a process restart; see AppRepository.observeInstalledApps.
        combine(getInstalledApps.observe(), settingsRepository.settings) { apps, settings ->
            LauncherUiState(
                apps = apps,
                isLoading = false,
                themeMode = settings.themeMode,
                accentFromSystem = settings.accentFromSystem,
                customAccentSwatch = settings.customAccentSwatch,
                iconRenderMode = settings.iconRenderMode,
            )
        }.onEach { _uiState.value = it }.launchIn(viewModelScope)
        viewModelScope.launch { ensureActiveProfile() }
        // Runs for the app's whole lifetime, deleting Favorites/Dock rows on a genuine uninstall
        // rather than just filtering them from view — see CleanUpUninstalledAppsUseCase.
        viewModelScope.launch { cleanUpUninstalledApps() }
    }
}
