package com.facetlauncher.app.ui.launcher

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.facetlauncher.app.data.SettingsRepository
import com.facetlauncher.app.data.WorkProfileInfo
import com.facetlauncher.app.data.WorkProfileRepository
import com.facetlauncher.app.data.model.AppInfo
import com.facetlauncher.app.data.model.FontScaleOption
import com.facetlauncher.app.data.model.FontWeightOption
import com.facetlauncher.app.data.model.IconRenderMode
import com.facetlauncher.app.data.model.IconShape
import com.facetlauncher.app.data.model.LauncherFontOption
import com.facetlauncher.app.data.model.SystemBarIconStyle
import com.facetlauncher.app.data.model.ThemeMode
import com.facetlauncher.app.data.model.WallpaperAccentRole
import com.facetlauncher.app.data.model.parseFacetIdFromDeepLink
import com.facetlauncher.app.domain.ActivateFacetByIdUseCase
import com.facetlauncher.app.domain.CleanUpUninstalledAppsUseCase
import com.facetlauncher.app.domain.EnsureActiveFacetUseCase
import com.facetlauncher.app.domain.GetInstalledAppsUseCase
import com.facetlauncher.app.domain.RepairOrphanedProfileRowsUseCase
import com.facetlauncher.app.domain.SeedDefaultDockUseCase
import com.facetlauncher.app.domain.SyncFacetShortcutsUseCase
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
 * emission — [com.facetlauncher.app.LauncherActivity] renders nothing at all while it's `true`
 * rather than a frame styled with these defaults (see chat history: previously visible as a flash
 * of default theme/font/icon on cold start, since these defaults feed [com.facetlauncher.app.ui.theme.FacetLauncherTheme]
 * directly). The `init` block's timeout coroutine is a safety net, not the expected path — the
 * underlying flows normally emit within milliseconds — so a bug in one of them can never leave the
 * launcher permanently blank, since unlike an ordinary app there is no "force quit and reopen" for
 * the actual home screen.
 */
data class LauncherUiState(
    val apps: List<AppInfo> = emptyList(),
    val isLoading: Boolean = true,
    /** F11 — all threaded into [com.facetlauncher.app.ui.theme.FacetLauncherTheme] so it resolves correctly app-wide. */
    val themeMode: ThemeMode = ThemeMode.SYSTEM,
    val systemBarIconStyle: SystemBarIconStyle = SystemBarIconStyle.MATCH_THEME,
    val accentFromSystem: Boolean = true,
    /** An `AccentSwatch` enum name — see [com.facetlauncher.app.data.model.LauncherSettings.customAccentSwatch]. */
    val customAccentSwatch: String? = null,
    val wallpaperAccentRole: WallpaperAccentRole = WallpaperAccentRole.PRIMARY,
    val iconRenderMode: IconRenderMode = IconRenderMode.SYSTEM_DEFAULT,
    val iconShape: IconShape = IconShape.SQUIRCLE,
    val launcherFontOption: LauncherFontOption = LauncherFontOption.SYSTEM,
    val homeAppsFontWeight: FontWeightOption = FontWeightOption.REGULAR,
    val fontScaleOption: FontScaleOption = FontScaleOption.DEFAULT,
    /** Gates [com.facetlauncher.app.LauncherActivity]'s onboarding branch — see [LauncherSettings.onboardingCompleted][com.facetlauncher.app.data.model.LauncherSettings.onboardingCompleted]. */
    val onboardingCompleted: Boolean = false,
    /**
     * Every genuine Work Profile currently on this device — sourced from [WorkProfileRepository]
     * rather than inferred from `apps.any { it.profile == WORK }`, so the App Drawer's Work tab(s)
     * stay visible (with an empty/paused state) while a profile is paused, instead of disappearing
     * just because its apps momentarily aren't enumerable. List-shaped (not a boolean) so it stays
     * correct even in the (today, rare) case of more than one — see [WorkProfileInfo]'s own doc.
     */
    val workProfiles: List<WorkProfileInfo> = emptyList(),
)

@HiltViewModel
class LauncherViewModel @Inject constructor(
    getInstalledApps: GetInstalledAppsUseCase,
    private val ensureActiveFacet: EnsureActiveFacetUseCase,
    private val cleanUpUninstalledApps: CleanUpUninstalledAppsUseCase,
    private val repairOrphanedProfileRows: RepairOrphanedProfileRowsUseCase,
    private val seedDefaultDock: SeedDefaultDockUseCase,
    private val syncFacetShortcuts: SyncFacetShortcutsUseCase,
    private val activateFacetById: ActivateFacetByIdUseCase,
    private val settingsRepository: SettingsRepository,
    private val workProfileRepository: WorkProfileRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(LauncherUiState())
    val uiState: StateFlow<LauncherUiState> = _uiState.asStateFlow()

    private val _homePressedEvent = MutableSharedFlow<Unit>(extraBufferCapacity = 1)
    val homePressedEvent: SharedFlow<Unit> = _homePressedEvent.asSharedFlow()

    fun onHomePressed() {
        _homePressedEvent.tryEmit(Unit)
    }

    /**
     * Ingests an incoming `facetlauncher://facet/{id}` deep link — the shared entry point for both
     * an external deep link and a dynamic shortcut's launch intent (see
     * [com.facetlauncher.app.LauncherActivity]'s cold-start/`onNewIntent` callers). A `Uri` that
     * isn't this scheme, or whose id doesn't resolve to a real facet, is a no-op (see
     * [ActivateFacetByIdUseCase]'s own doc).
     */
    fun activateFacetFromDeepLink(uri: Uri) {
        val facetId = parseFacetIdFromDeepLink(uri) ?: return
        viewModelScope.launch { activateFacetById(facetId) }
    }

    /** Called once, from the final onboarding step ("Set as default" or "Later") — see [com.facetlauncher.app.LauncherActivity]. */
    fun completeOnboarding() {
        viewModelScope.launch { settingsRepository.setOnboardingCompleted(true) }
    }

    init {
        // Live, not one-shot — an install/uninstall/update while Facet is in the foreground
        // (including via the app long-press menu's Uninstall action) must be reflected without
        // requiring a process restart; see AppRepository.observeInstalledApps.
        combine(getInstalledApps.observe(), settingsRepository.settings, workProfileRepository.observeWorkProfiles()) { apps, settings, workProfiles ->
            LauncherUiState(
                apps = apps,
                isLoading = false,
                themeMode = settings.themeMode,
                systemBarIconStyle = settings.systemBarIconStyle,
                accentFromSystem = settings.accentFromSystem,
                customAccentSwatch = settings.customAccentSwatch,
                wallpaperAccentRole = settings.wallpaperAccentRole,
                iconRenderMode = settings.iconRenderMode,
                iconShape = settings.iconShape,
                launcherFontOption = settings.launcherFontOption,
                homeAppsFontWeight = settings.homeAppsFontWeight,
                fontScaleOption = settings.fontScaleOption,
                onboardingCompleted = settings.onboardingCompleted,
                workProfiles = workProfiles,
            )
        }.onEach { _uiState.value = it }.launchIn(viewModelScope)
        viewModelScope.launch { ensureActiveFacet() }
        // Runs for the app's whole lifetime, deleting Favorites/Dock rows on a genuine uninstall
        // rather than just filtering them from view — see CleanUpUninstalledAppsUseCase.
        viewModelScope.launch { cleanUpUninstalledApps() }
        // A single one-time pass, not a live collector (unlike the launches above/below it) — see
        // RepairOrphanedProfileRowsUseCase's own doc.
        viewModelScope.launch { repairOrphanedProfileRows() }
        // Not gated on the onboarding UI being shown or completed — a fresh install's dock is
        // seeded even if onboarding is killed/skipped partway through, see SeedDefaultDockUseCase.
        viewModelScope.launch { seedDefaultDock() }
        // Runs for the app's whole lifetime, republishing the full dynamic-shortcut set on every
        // facet add/rename/reorder/delete — see SyncFacetShortcutsUseCase.
        viewModelScope.launch { syncFacetShortcuts() }

        viewModelScope.launch {
            delay(LOADING_TIMEOUT_MS)
            _uiState.update { if (it.isLoading) it.copy(isLoading = false) else it }
        }
    }

    private companion object {
        const val LOADING_TIMEOUT_MS = 3_000L
    }
}
