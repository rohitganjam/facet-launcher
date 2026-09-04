package com.lumenlauncher.app.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lumenlauncher.app.data.NotificationShadeRepository
import com.lumenlauncher.app.domain.ObserveHomeScreenStateUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/** Read-only view of settings/dock/app-list state — Home never writes to it. */
@HiltViewModel
class HomeViewModel @Inject constructor(
    private val observeHomeScreenState: ObserveHomeScreenStateUseCase,
    private val notificationShadeRepository: NotificationShadeRepository,
) : ViewModel() {

    private val usageAccessPromptDismissed = MutableStateFlow(false)

    val uiState: StateFlow<HomeUiState> = combine(observeHomeScreenState(), usageAccessPromptDismissed) { screenState, dismissed ->
            HomeUiState(
                settings = screenState.settings,
                dockApps = screenState.dockApps,
                appListItems = screenState.appListItems,
                profiles = screenState.profiles,
                usageAccessGranted = screenState.usageAccessGranted,
                calendarEvents = screenState.calendarEvents,
                badgeCounts = screenState.badgeCounts,
                usageAccessPromptDismissed = dismissed,
            )
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), HomeUiState())

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
}
