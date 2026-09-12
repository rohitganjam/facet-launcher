package com.facetlauncher.app.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.facetlauncher.app.data.NotificationAccessRepository
import com.facetlauncher.app.data.SettingsRepository
import com.facetlauncher.app.data.model.LauncherSettings
import com.facetlauncher.app.data.model.NotificationBadgeStyle
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class NotificationSettingsUiState(
    val settings: LauncherSettings = LauncherSettings(),
    val isAccessGranted: Boolean = false,
)

/**
 * F13's own settings page, reached from Settings' NOTIFICATIONS card — a real screen rather than
 * an inline toggle, per direct feedback, since it now governs both whether badges show at all
 * and which style they render in. Turning the toggle on when notification listener access isn't
 * granted yet routes through [NotificationAccessExplanationScreen] instead of flipping the
 * setting directly — see [NotificationSettingsScreen]'s own `onEnabledChanged` wiring.
 */
@HiltViewModel
class NotificationSettingsViewModel @Inject constructor(
    private val settingsRepository: SettingsRepository,
    private val notificationAccessRepository: NotificationAccessRepository,
) : ViewModel() {

    private val isAccessGranted = MutableStateFlow(notificationAccessRepository.isGranted())

    val uiState: StateFlow<NotificationSettingsUiState> = combine(
        settingsRepository.settings,
        isAccessGranted,
    ) { settings, granted ->
        NotificationSettingsUiState(settings = settings, isAccessGranted = granted)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), NotificationSettingsUiState())

    /** No grant-change callback exists — call on `onResume` so returning from the explanation screen's Settings redirect is picked up. */
    fun refreshAccessGranted() {
        isAccessGranted.value = notificationAccessRepository.isGranted()
    }

    fun setEnabled(enabled: Boolean) {
        viewModelScope.launch { settingsRepository.setNotificationDotsEnabled(enabled) }
    }

    fun setBadgeStyle(style: NotificationBadgeStyle) {
        viewModelScope.launch { settingsRepository.setNotificationBadgeStyle(style) }
    }
}
