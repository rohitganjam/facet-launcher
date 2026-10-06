package com.facetlauncher.app.ui.home

import android.content.Intent
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.facetlauncher.app.data.DefaultLauncherRepository
import com.facetlauncher.app.data.NotificationShadeRepository
import com.facetlauncher.app.data.FacetRepository
import com.facetlauncher.app.data.SettingsRepository
import com.facetlauncher.app.data.model.AppInfo
import com.facetlauncher.app.data.model.ClockAlignment
import com.facetlauncher.app.data.model.Folder
import com.facetlauncher.app.domain.ObserveHomeScreenStateUseCase
import com.facetlauncher.app.domain.ObserveQuickAddStateUseCase
import com.facetlauncher.app.domain.QuickAddState
import com.facetlauncher.app.ui.components.HOME_GESTURES_COACH_MARK_ID
import com.facetlauncher.app.ui.components.HOME_SET_DEFAULT_PROMPT_ID
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * Read-only view of settings/dock/app-list state — Home never writes to it.
 *
 * [HomeUiState.isLoading] starts `true` (Home's default-styled defaults, not the user's real
 * theme/font/dock/app-list choices) and flips `false` on this ViewModel's first real emission —
 * [com.facetlauncher.app.ui.launcher.HomeDrawerRoute] renders nothing at all while it's `true`
 * rather than a frame that looks wrong (see chat history: this was previously visible as a flash
 * of default appearance on cold start). [LOADING_TIMEOUT_MS] is a safety net, not the expected
 * path — the underlying flows normally emit within milliseconds — so that a bug in one of them
 * can never leave Home permanently blank, since unlike an ordinary app there is no "force quit and
 * reopen" for the actual home screen.
 */
@HiltViewModel
class HomeViewModel @Inject constructor(
    private val observeHomeScreenState: ObserveHomeScreenStateUseCase,
    private val notificationShadeRepository: NotificationShadeRepository,
    private val settingsRepository: SettingsRepository,
    private val facetRepository: FacetRepository,
    private val defaultLauncherRepository: DefaultLauncherRepository,
    private val quickAddState: ObserveQuickAddStateUseCase,
    /** PRD F15 — the Composable layer calls straight through these (e.g. `homeViewModel.clockWidgetHost.createHostView(...)`, `homeViewModel.clockWidgetFacet.commitSize(homeUiState.activeFacet, ...)`); see their own docs for why these hooks live outside this class. */
    val clockWidgetHost: ClockWidgetHostController,
    val clockWidgetFacet: ClockWidgetFacetController,
) : ViewModel() {

    private val usageAccessPromptDismissed = MutableStateFlow(false)
    private val isDefaultLauncher = MutableStateFlow<Boolean?>(null)

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        combine(observeHomeScreenState(), usageAccessPromptDismissed, isDefaultLauncher) { screenState, dismissed, isDefault ->
            HomeUiState(
                settings = screenState.settings,
                isLoading = false,
                dockApps = screenState.dockApps,
                appListItems = screenState.appListItems,
                favoriteItems = screenState.favoriteItems,
                facets = screenState.facets,
                usageAccessGranted = screenState.usageAccessGranted,
                calendarEvents = screenState.calendarEvents,
                badgeCounts = screenState.badgeCounts,
                clockAccessories = screenState.clockAccessories,
                usageAccessPromptDismissed = dismissed,
                isDefaultLauncher = isDefault,
            )
        }.onEach { _uiState.value = it }.launchIn(viewModelScope)

        viewModelScope.launch {
            delay(LOADING_TIMEOUT_MS)
            _uiState.update { if (it.isLoading) it.copy(isLoading = false) else it }
        }

        viewModelScope.launch {
            val isDefault = defaultLauncherRepository.isDefaultLauncher()
            isDefaultLauncher.value = isDefault
            // Already the default: there's nothing to ask, so retire the prompt for good — otherwise it would first
            // appear later if the user ever switched to another launcher and opened Facet from its drawer.
            if (isDefault) settingsRepository.markCoachMarkSeen(HOME_SET_DEFAULT_PROMPT_ID)
        }
    }

    /** [com.facetlauncher.app.ui.onboarding.SetDefaultLauncherSheet]'s primary action target — see [DefaultLauncherRepository.requestDefaultLauncherIntent]. */
    fun requestDefaultLauncherIntent(): Intent = defaultLauncherRepository.requestDefaultLauncherIntent()

    /** Dismisses [HomeUiState.showSetDefaultPrompt] for good — "Set as default"/"Later", or tapping its scrim. */
    fun dismissSetDefaultPrompt() {
        viewModelScope.launch { settingsRepository.markCoachMarkSeen(HOME_SET_DEFAULT_PROMPT_ID) }
    }

    /** `PACKAGE_USAGE_STATS` has no grant-change callback — call this from `onResume` so a grant made via Settings takes effect without waiting for an unrelated facet change. */
    fun refresh() {
        observeHomeScreenState.refresh()
    }

    /** Called the moment the usage-access prompt's own button is tapped — hides it for the rest of this ViewModel's lifetime regardless of whether the permission actually ends up granted (see chat history). */
    fun dismissUsageAccessPrompt() {
        usageAccessPromptDismissed.value = true
    }

    /** First gesture on Home after onboarding, or a "Got it" tap — see [HomeUiState.showGestureHint]. */
    fun dismissGestureHint() {
        viewModelScope.launch { settingsRepository.markCoachMarkSeen(HOME_GESTURES_COACH_MARK_ID) }
    }

    /** Swipe-down-to-open-shade gesture, from [com.facetlauncher.app.ui.launcher.HomeDrawerRoute]. */
    fun expandNotificationShade() {
        viewModelScope.launch { notificationShadeRepository.expand() }
    }

    /**
     * Fired once, on release, by the clock's grab handle ([com.facetlauncher.app.ui.home.ClockZoneHandle])
     * — see [com.facetlauncher.app.data.model.LauncherSettings.clockZoneHeightDp]. Writes to the
     * active facet's own override when one is actually governing the widget's position right
     * now ([HomeUiState.clockPositionOwningFacet]); otherwise falls back to the global setting,
     * same as every other facet-overridable value here.
     */
    fun onClockZoneHeightCommit(heightDp: Float) {
        val owningFacet = _uiState.value.clockPositionOwningFacet
        viewModelScope.launch {
            owningFacet?.let { facetRepository.setClockZoneHeight(it, heightDp) }
                ?: settingsRepository.setClockZoneHeight(heightDp)
        }
    }

    /** Fired once, on release, by the clock's resize handles — see [com.facetlauncher.app.data.model.LauncherSettings.clockScale]. */
    fun onClockScaleCommit(scale: Float) {
        val owningFacet = _uiState.value.clockPositionOwningFacet
        viewModelScope.launch {
            owningFacet?.let { facetRepository.setClockScale(it, scale) }
                ?: settingsRepository.setClockScale(scale)
        }
    }


    /** Fired by the adjust-mode alignment toolbar — same facet-or-global ownership as [onClockZoneHeightCommit]/[onClockScaleCommit] (all three sit behind `overrideClock`). */
    fun onClockAlignmentCommit(alignment: ClockAlignment) {
        val owningFacet = _uiState.value.clockPositionOwningFacet
        viewModelScope.launch {
            owningFacet?.let { facetRepository.setClockAlignment(it, alignment) }
                ?: settingsRepository.setClockAlignment(alignment)
        }
    }

    /**
     * Resolves the long-press menu's Add/Remove Favorites/Dock rows synchronously against this
     * ViewModel's own already-live [uiState] — no repository read, so a long-press context menu
     * (Home's own Dock/Favorites, the App Drawer, Search — every one of them, threaded through
     * [com.facetlauncher.app.ui.launcher.HomeDrawerRoute]) never has to resolve those rows after
     * first appearing (see chat history).
     */
    fun quickAddStateForApp(app: AppInfo): QuickAddState {
        val state = _uiState.value
        return quickAddState.forApp(app, state.dockApps, state.favoriteItems, state.dockOverrideFacetName, state.favoritesOverrideFacetName)
    }

    /** Folder-tile counterpart of [quickAddStateForApp]. */
    fun quickAddStateForFolder(folder: Folder): QuickAddState {
        val state = _uiState.value
        return quickAddState.forFolder(folder, state.dockApps, state.favoriteItems, state.dockOverrideFacetName, state.favoritesOverrideFacetName)
    }

    private companion object {
        const val LOADING_TIMEOUT_MS = 3_000L
    }
}
