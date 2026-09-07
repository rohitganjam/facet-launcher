package com.lumenlauncher.app.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lumenlauncher.app.data.NotificationShadeRepository
import com.lumenlauncher.app.data.ProfileRepository
import com.lumenlauncher.app.data.SettingsRepository
import com.lumenlauncher.app.domain.ObserveHomeScreenStateUseCase
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
 * [com.lumenlauncher.app.ui.launcher.HomeDrawerRoute] renders nothing at all while it's `true`
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
    private val profileRepository: ProfileRepository,
) : ViewModel() {

    private val usageAccessPromptDismissed = MutableStateFlow(false)

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        combine(observeHomeScreenState(), usageAccessPromptDismissed) { screenState, dismissed ->
            HomeUiState(
                settings = screenState.settings,
                isLoading = false,
                dockApps = screenState.dockApps,
                appListItems = screenState.appListItems,
                profiles = screenState.profiles,
                usageAccessGranted = screenState.usageAccessGranted,
                calendarEvents = screenState.calendarEvents,
                badgeCounts = screenState.badgeCounts,
                usageAccessPromptDismissed = dismissed,
            )
        }.onEach { _uiState.value = it }.launchIn(viewModelScope)

        viewModelScope.launch {
            delay(LOADING_TIMEOUT_MS)
            _uiState.update { if (it.isLoading) it.copy(isLoading = false) else it }
        }
    }

    /** `PACKAGE_USAGE_STATS` has no grant-change callback — call this from `onResume` so a grant made via Settings takes effect without waiting for an unrelated profile change. */
    fun refresh() {
        observeHomeScreenState.refresh()
    }

    /** Called the moment the usage-access prompt's own button is tapped — hides it for the rest of this ViewModel's lifetime regardless of whether the permission actually ends up granted (see chat history). */
    fun dismissUsageAccessPrompt() {
        usageAccessPromptDismissed.value = true
    }

    /** Swipe-down-to-open-shade gesture, from [com.lumenlauncher.app.ui.launcher.HomeDrawerRoute]. */
    fun expandNotificationShade() {
        viewModelScope.launch { notificationShadeRepository.expand() }
    }

    /**
     * Fired once, on release, by the clock's grab handle ([com.lumenlauncher.app.ui.home.ClockZoneHandle])
     * — see [com.lumenlauncher.app.data.model.LauncherSettings.clockZoneHeightDp]. Writes to the
     * active profile's own override when one is actually governing the widget's position right
     * now ([HomeUiState.clockPositionOwningProfile]); otherwise falls back to the global setting,
     * same as every other profile-overridable value here.
     */
    fun onClockZoneHeightCommit(heightDp: Float) {
        val owningProfile = _uiState.value.clockPositionOwningProfile
        viewModelScope.launch {
            owningProfile?.let { profileRepository.setClockZoneHeight(it, heightDp) }
                ?: settingsRepository.setClockZoneHeight(heightDp)
        }
    }

    private companion object {
        const val LOADING_TIMEOUT_MS = 3_000L
    }
}
