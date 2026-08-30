package com.lumenlauncher.app.ui.drawer

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lumenlauncher.app.data.AppShortcutRepository
import com.lumenlauncher.app.data.ContactPermissionRepository
import com.lumenlauncher.app.data.ContactRepository
import com.lumenlauncher.app.data.NotificationAccessRepository
import com.lumenlauncher.app.data.NotificationBadgeRepository
import com.lumenlauncher.app.data.SettingsRepository
import com.lumenlauncher.app.data.model.AppInfo
import com.lumenlauncher.app.data.model.AppShortcut
import com.lumenlauncher.app.data.model.ContactInfo
import com.lumenlauncher.app.data.model.LauncherSettings
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn

/** F6's per-query contact matches, capped at 5 (see README `1j`/the approved search-results spec). */
private const val MAX_CONTACT_RESULTS = 5

/** Read-only settings needed to render the Drawer (icon visibility, opacity, left-edge rail toggle), plus F6's contacts search. */
@HiltViewModel
class DrawerViewModel @Inject constructor(
    settingsRepository: SettingsRepository,
    private val contactPermissionRepository: ContactPermissionRepository,
    private val contactRepository: ContactRepository,
    private val appShortcutRepository: AppShortcutRepository,
    private val notificationBadgeRepository: NotificationBadgeRepository,
    private val notificationAccessRepository: NotificationAccessRepository,
) : ViewModel() {

    val settings: StateFlow<LauncherSettings> = settingsRepository.settings
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), LauncherSettings())

    /** F13 — empty unless both the "Notification badges" setting is on and access is granted; see `ObserveHomeScreenStateUseCase`'s identical gating for Home. */
    val badgeCounts: StateFlow<Map<String, Int>> = combine(
        settingsRepository.settings,
        notificationBadgeRepository.badgeCounts,
    ) { settings, counts ->
        if (settings.notificationDotsEnabled && notificationAccessRepository.isGranted()) counts else emptyMap()
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyMap())

    private val searchQuery = MutableStateFlow("")

    /** Called alongside the Drawer's own (locally-owned) query state — see `HomeDrawerRoute.kt`. */
    fun onQueryChanged(query: String) {
        searchQuery.value = query
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    val contactResults: StateFlow<List<ContactInfo>> = combine(searchQuery, settingsRepository.settings) { query, settings ->
        query to settings.searchContactsEnabled
    }.flatMapLatest { (query, enabled) ->
        if (!enabled || !contactPermissionRepository.isGranted() || query.isBlank()) {
            flowOf(emptyList())
        } else {
            flow { emit(contactRepository.searchContacts(query, MAX_CONTACT_RESULTS)) }
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    /** F12's long-press context menu — fetched fresh per app, only when its menu actually opens. */
    suspend fun getShortcuts(app: AppInfo): List<AppShortcut> = appShortcutRepository.getShortcuts(app.packageName)

    fun launchShortcut(shortcut: AppShortcut) = appShortcutRepository.launchShortcut(shortcut)
}
