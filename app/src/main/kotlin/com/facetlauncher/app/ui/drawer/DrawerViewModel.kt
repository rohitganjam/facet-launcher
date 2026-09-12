package com.facetlauncher.app.ui.drawer

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.facetlauncher.app.data.AppShortcutRepository
import com.facetlauncher.app.data.ContactPermissionRepository
import com.facetlauncher.app.data.ContactRepository
import com.facetlauncher.app.data.NotificationAccessRepository
import com.facetlauncher.app.data.NotificationBadgeRepository
import com.facetlauncher.app.data.SettingsRepository
import com.facetlauncher.app.data.model.AppInfo
import com.facetlauncher.app.data.model.AppShortcut
import com.facetlauncher.app.data.model.ContactConnection
import com.facetlauncher.app.data.model.ContactInfo
import com.facetlauncher.app.data.model.LauncherSettings
import com.facetlauncher.app.domain.AddAppToDockUseCase
import com.facetlauncher.app.domain.AddAppToFavoritesUseCase
import com.facetlauncher.app.domain.ObserveQuickAddStateUseCase
import com.facetlauncher.app.domain.QuickAddState
import com.facetlauncher.app.domain.RankBySearchRelevanceUseCase
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
import kotlinx.coroutines.launch

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
    private val rankBySearchRelevance: RankBySearchRelevanceUseCase,
    observeQuickAddState: ObserveQuickAddStateUseCase,
    private val addAppToFavorites: AddAppToFavoritesUseCase,
    private val addAppToDock: AddAppToDockUseCase,
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
            flow {
                // The Provider's own LIKE-based query already narrows to substring matches on
                // DISPLAY_NAME; re-rank so a name that *starts with* the query (e.g. "Ann" for
                // "Anna Lee") outranks one that merely contains it ("Marianna") — same relevance
                // rule the Drawer's own app search uses (see chat history).
                val matches = contactRepository.searchContacts(query, MAX_CONTACT_RESULTS)
                emit(rankBySearchRelevance(matches, query) { it.displayName })
            }
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    private val contactsPermissionPromptDismissed = MutableStateFlow(false)

    /**
     * True while the drawer search wants to show contacts (the setting is on, and there's a real
     * query) but `READ_CONTACTS` isn't actually granted — most commonly because it was revoked via
     * system Settings after being granted once (the in-app toggle itself already reverts to off on
     * an initial denial, so this specifically covers that external-revocation case). Stays false
     * once the user taps the prompt's own button, regardless of whether they actually complete the
     * grant afterward — same dismiss-on-click contract as Home's usage-access prompt (see chat history).
     */
    @OptIn(ExperimentalCoroutinesApi::class)
    val showContactsPermissionPrompt: StateFlow<Boolean> = combine(
        searchQuery,
        settingsRepository.settings,
        contactsPermissionPromptDismissed,
    ) { query, settings, dismissed ->
        query.isNotBlank() && settings.searchContactsEnabled && !dismissed && !contactPermissionRepository.isGranted()
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), false)

    /** Called the moment the contacts-permission prompt's own button is tapped. */
    fun dismissContactsPermissionPrompt() {
        contactsPermissionPromptDismissed.value = true
    }

    /** F12's long-press context menu — fetched fresh per app, only when its menu actually opens. */
    suspend fun getShortcuts(app: AppInfo): List<AppShortcut> = appShortcutRepository.getShortcuts(app.packageName)

    fun launchShortcut(shortcut: AppShortcut) = appShortcutRepository.launchShortcut(shortcut)

    /** Governs F12's own "Add to Favorites"/"Add to Dock" rows — see [ObserveQuickAddStateUseCase]'s own doc. */
    val quickAddState: StateFlow<QuickAddState> = observeQuickAddState()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), QuickAddState())

    fun addToFavorites(app: AppInfo) {
        viewModelScope.launch { addAppToFavorites(app) }
    }

    fun addToDock(app: AppInfo) {
        viewModelScope.launch { addAppToDock(app) }
    }

    /** Phase 9's connections sheet — fetched fresh per contact, only when their sheet actually opens (same fetch-on-open precedent as [getShortcuts]). */
    suspend fun getConnections(contact: ContactInfo): List<ContactConnection> = contactRepository.getConnections(contact.id)
}
