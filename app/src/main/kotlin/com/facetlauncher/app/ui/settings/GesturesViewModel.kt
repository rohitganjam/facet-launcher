package com.facetlauncher.app.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.facetlauncher.app.data.AccessibilityAccessRepository
import com.facetlauncher.app.data.SettingsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class GesturesUiState(
    val homeOpensSearch: Boolean = false,
    /** The stored preference and the accessibility service both on: the switch never reads on without the real grant behind it. */
    val doubleTapToLock: Boolean = false,
    val isAccessibilityGranted: Boolean = false,
)

/**
 * Settings → Gestures. Accessibility access has no grant-change callback, so [refreshAccess] is called on `onResume`.
 * Turning "Double-tap to lock" on persists first even without access; the switch stays off until the grant lands.
 */
@HiltViewModel
class GesturesViewModel @Inject constructor(
    private val settingsRepository: SettingsRepository,
    private val accessibilityAccessRepository: AccessibilityAccessRepository,
) : ViewModel() {

    private val isAccessibilityGranted = MutableStateFlow(accessibilityAccessRepository.isGranted())

    val uiState: StateFlow<GesturesUiState> = combine(settingsRepository.settings, isAccessibilityGranted) { settings, granted ->
        GesturesUiState(
            homeOpensSearch = settings.homeOpensSearch,
            doubleTapToLock = settings.doubleTapToLock && granted,
            isAccessibilityGranted = granted,
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), GesturesUiState())

    fun refreshAccess() {
        isAccessibilityGranted.value = accessibilityAccessRepository.isGranted()
    }

    fun setHomeOpensSearch(enabled: Boolean) {
        viewModelScope.launch { settingsRepository.setHomeOpensSearch(enabled) }
    }

    fun setDoubleTapToLock(enabled: Boolean) {
        viewModelScope.launch { settingsRepository.setDoubleTapToLock(enabled) }
    }
}
