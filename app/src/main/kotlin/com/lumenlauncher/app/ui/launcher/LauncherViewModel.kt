package com.lumenlauncher.app.ui.launcher

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lumenlauncher.app.data.SettingsRepository
import com.lumenlauncher.app.data.model.AppInfo
import com.lumenlauncher.app.data.model.IconRenderMode
import com.lumenlauncher.app.data.model.LauncherFontOption
import com.lumenlauncher.app.data.model.ThemeMode
import com.lumenlauncher.app.data.model.WallpaperAccentRole
import com.lumenlauncher.app.domain.CleanUpUninstalledAppsUseCase
import com.lumenlauncher.app.domain.EnsureActiveProfileUseCase
import com.lumenlauncher.app.domain.GetInstalledAppsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * [LauncherUiState.isLoading] starts `true` and flips `false` on this ViewModel's first real
 * emission — [com.lumenlauncher.app.LauncherActivity] renders nothing at all while it's `true`
 * rather than a frame styled with these defaults (see chat history: previously visible as a flash
 * of default theme/font/icon on cold start, since these defaults feed [com.lumenlauncher.app.ui.theme.LumenLauncherTheme]
 * directly). The `init` block's timeout coroutine is a safety net, not the expected path — the
 * underlying flows normally emit within milliseconds — so a bug in one of them can never leave the
 * launcher permanently blank, since unlike an ordinary app there is no "force quit and reopen" for
 * the actual home screen.
 */
data class LauncherUiState(
    val apps: List<AppInfo> = emptyList(),
    val isLoading: Boolean = true,
    /** F11 — all threaded into [com.lumenlauncher.app.ui.theme.LumenLauncherTheme] so it resolves correctly app-wide. */
    val themeMode: ThemeMode = ThemeMode.SYSTEM,
    val accentFromSystem: Boolean = true,
    /** An `AccentSwatch` enum name — see [com.lumenlauncher.app.data.model.LauncherSettings.customAccentSwatch]. */
    val customAccentSwatch: String? = null,
    val wallpaperAccentRole: WallpaperAccentRole = WallpaperAccentRole.PRIMARY,
    val iconRenderMode: IconRenderMode = IconRenderMode.SYSTEM_DEFAULT,
    val launcherFontOption: LauncherFontOption = LauncherFontOption.SYSTEM,
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

    private val _homePressedEvent = MutableSharedFlow<Unit>(extraBufferCapacity = 1)
    val homePressedEvent: SharedFlow<Unit> = _homePressedEvent.asSharedFlow()

    fun onHomePressed() {
        _homePressedEvent.tryEmit(Unit)
    }

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
                wallpaperAccentRole = settings.wallpaperAccentRole,
                iconRenderMode = settings.iconRenderMode,
                launcherFontOption = settings.launcherFontOption,
            )
        }.onEach { _uiState.value = it }.launchIn(viewModelScope)
        viewModelScope.launch { ensureActiveProfile() }
        // Runs for the app's whole lifetime, deleting Favorites/Dock rows on a genuine uninstall
        // rather than just filtering them from view — see CleanUpUninstalledAppsUseCase.
        viewModelScope.launch { cleanUpUninstalledApps() }

        viewModelScope.launch {
            delay(LOADING_TIMEOUT_MS)
            _uiState.update { if (it.isLoading) it.copy(isLoading = false) else it }
        }
    }

    private companion object {
        const val LOADING_TIMEOUT_MS = 3_000L
    }
}
