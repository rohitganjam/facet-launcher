package com.facetlauncher.app.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.facetlauncher.app.data.NotificationAccessRepository
import com.facetlauncher.app.data.SettingsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * Notification listener access has no grant-change callback — [refresh] is called on `onResume`,
 * since the only way to grant it is the system Settings redirect this screen sends the user to.
 * Once granted, this also persists "Notification badges" on — the toggle that sent the user here
 * in the first place — matching the intent of turning it on rather than leaving it stuck off
 * just because the grant itself doesn't flow back through a callback.
 */
@HiltViewModel
class NotificationAccessExplanationViewModel @Inject constructor(
    private val notificationAccessRepository: NotificationAccessRepository,
    private val settingsRepository: SettingsRepository,
) : ViewModel() {

    private val _isGranted = MutableStateFlow(notificationAccessRepository.isGranted())
    val isGranted: StateFlow<Boolean> = _isGranted.asStateFlow()

    fun refresh() {
        viewModelScope.launch {
            val granted = notificationAccessRepository.isGranted()
            _isGranted.value = granted
            if (granted) settingsRepository.setNotificationDotsEnabled(true)
        }
    }
}
