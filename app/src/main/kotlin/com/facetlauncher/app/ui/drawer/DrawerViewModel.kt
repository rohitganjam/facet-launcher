package com.facetlauncher.app.ui.drawer

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.facetlauncher.app.data.AppShortcutRepository
import com.facetlauncher.app.data.ContactPermissionRepository
import com.facetlauncher.app.data.ContactRepository
import com.facetlauncher.app.data.FolderRepository
import com.facetlauncher.app.data.NotificationAccessRepository
import com.facetlauncher.app.data.NotificationBadgeRepository
import com.facetlauncher.app.data.SettingsRepository
import com.facetlauncher.app.data.SystemSettingsRepository
import com.facetlauncher.app.data.model.AppInfo
import com.facetlauncher.app.data.model.AppShortcut
import com.facetlauncher.app.data.model.ContactConnection
import com.facetlauncher.app.data.model.ContactInfo
import com.facetlauncher.app.data.model.Folder
import com.facetlauncher.app.data.model.LauncherSettings
import com.facetlauncher.app.data.model.SettingsSearchEntry
import com.facetlauncher.app.domain.AddAppToDockUseCase
import com.facetlauncher.app.domain.AddAppToFavoritesUseCase
import com.facetlauncher.app.domain.AddFolderToDockUseCase
import com.facetlauncher.app.domain.AddFolderToFavoritesUseCase
import com.facetlauncher.app.domain.ObserveQuickAddStateUseCase
import com.facetlauncher.app.domain.QuickAddState
import com.facetlauncher.app.domain.QuickPlacementAction
import com.facetlauncher.app.domain.RankBySearchRelevanceUseCase
import com.facetlauncher.app.domain.RemoveAppFromDockUseCase
import com.facetlauncher.app.domain.RemoveAppFromFavoritesUseCase
import com.facetlauncher.app.domain.RemoveFolderFromDockUseCase
import com.facetlauncher.app.domain.RemoveFolderFromFavoritesUseCase
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

/** Per-query system Settings matches, capped the same as [MAX_CONTACT_RESULTS] — same "small, scannable section" rationale. */
private const val MAX_SETTINGS_RESULTS = 5

/** Read-only settings needed to render the Drawer (icon visibility, opacity, left-edge rail toggle), plus F6's contacts search. */
@HiltViewModel
class DrawerViewModel @Inject constructor(
    private val settingsRepository: SettingsRepository,
    private val contactPermissionRepository: ContactPermissionRepository,
    private val contactRepository: ContactRepository,
    private val systemSettingsRepository: SystemSettingsRepository,
    private val appShortcutRepository: AppShortcutRepository,
    private val notificationBadgeRepository: NotificationBadgeRepository,
    private val notificationAccessRepository: NotificationAccessRepository,
    private val rankBySearchRelevance: RankBySearchRelevanceUseCase,
    private val observeQuickAddState: ObserveQuickAddStateUseCase,
    private val addAppToFavorites: AddAppToFavoritesUseCase,
    private val removeAppFromFavorites: RemoveAppFromFavoritesUseCase,
    private val addAppToDock: AddAppToDockUseCase,
    private val removeAppFromDock: RemoveAppFromDockUseCase,
    private val addFolderToFavorites: AddFolderToFavoritesUseCase,
    private val removeFolderFromFavorites: RemoveFolderFromFavoritesUseCase,
    private val addFolderToDock: AddFolderToDockUseCase,
    private val removeFolderFromDock: RemoveFolderFromDockUseCase,
    private val folderRepository: FolderRepository,
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

    /** Search's system Settings section — no permission gating (unlike [contactResults]), just the "Search settings" toggle and a real query. */
    @OptIn(ExperimentalCoroutinesApi::class)
    val settingsResults: StateFlow<List<SettingsSearchEntry>> = combine(searchQuery, settingsRepository.settings) { query, settings ->
        query to settings.searchSettingsEnabled
    }.flatMapLatest { (query, enabled) ->
        if (!enabled || query.isBlank()) {
            flowOf(emptyList())
        } else {
            flow {
                // Not [rankBySearchRelevance]: a match here can come from a keyword alias (e.g.
                // "internet" for the "Wi-Fi" entry) rather than the label itself, and that use
                // case's own contains-filter would incorrectly drop an alias-only match since the
                // label doesn't literally contain the query. The repository already did the real
                // filtering; this only orders what it returned.
                val matches = systemSettingsRepository.search(query)
                val ranked = matches.sortedWith(
                    compareByDescending<SettingsSearchEntry> { it.label.startsWith(query, ignoreCase = true) }.thenBy { it.label.lowercase() },
                )
                emit(ranked.take(MAX_SETTINGS_RESULTS))
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

    private val contactsSettingPromptDismissed = MutableStateFlow(false)

    /**
     * True while the drawer search has a real query and `READ_CONTACTS` is already granted, but
     * the "Search contacts" setting itself is off — most commonly because it was granted from
     * Settings → Permissions (which only requests the OS permission, see `PermissionsScreen`'s own
     * `PermissionKind.CONTACTS` branch) without also flipping the App Drawer's own toggle. The
     * mirror image of [showContactsPermissionPrompt]: that one needs the setting on and the
     * permission missing, this one needs the permission granted and the setting off — the two can
     * never both be true. Same dismiss-on-click contract as [showContactsPermissionPrompt].
     */
    @OptIn(ExperimentalCoroutinesApi::class)
    val showContactsSettingPrompt: StateFlow<Boolean> = combine(
        searchQuery,
        settingsRepository.settings,
        contactsSettingPromptDismissed,
    ) { query, settings, dismissed ->
        query.isNotBlank() && !settings.searchContactsEnabled && !dismissed && contactPermissionRepository.isGranted()
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), false)

    /**
     * Called the moment the contacts-setting prompt's own button is tapped — turns the setting on
     * directly rather than sending the user anywhere, since the OS permission is already granted
     * and there's nothing left to ask for.
     */
    fun enableContactSearch() {
        contactsSettingPromptDismissed.value = true
        viewModelScope.launch { settingsRepository.setSearchContactsEnabled(true) }
    }

    /** F12's long-press context menu — fetched fresh per app, only when its menu actually opens. */
    suspend fun getShortcuts(app: AppInfo): List<AppShortcut> = appShortcutRepository.getShortcuts(app.packageName)

    fun launchShortcut(shortcut: AppShortcut) = appShortcutRepository.launchShortcut(shortcut)

    /** Governs F12's own "Add to Favorites"/"Add to Dock" (or "Remove from…") rows — fetched fresh per app/folder, only when its menu actually opens. */
    suspend fun quickAddStateForApp(app: AppInfo): QuickAddState = observeQuickAddState.forApp(app)

    suspend fun quickAddStateForFolder(folder: Folder): QuickAddState = observeQuickAddState.forFolder(folder)

    fun onFavoritesAction(app: AppInfo, action: QuickPlacementAction) {
        viewModelScope.launch {
            when (action) {
                is QuickPlacementAction.Add -> addAppToFavorites(app)
                is QuickPlacementAction.Remove -> removeAppFromFavorites(app)
            }
        }
    }

    fun onDockAction(app: AppInfo, action: QuickPlacementAction) {
        viewModelScope.launch {
            when (action) {
                is QuickPlacementAction.Add -> addAppToDock(app)
                is QuickPlacementAction.Remove -> removeAppFromDock(app)
            }
        }
    }

    fun onFolderFavoritesAction(folder: Folder, action: QuickPlacementAction) {
        viewModelScope.launch {
            when (action) {
                is QuickPlacementAction.Add -> addFolderToFavorites(folder)
                is QuickPlacementAction.Remove -> removeFolderFromFavorites(folder)
            }
        }
    }

    fun onFolderDockAction(folder: Folder, action: QuickPlacementAction) {
        viewModelScope.launch {
            when (action) {
                is QuickPlacementAction.Add -> addFolderToDock(folder)
                is QuickPlacementAction.Remove -> removeFolderFromDock(folder)
            }
        }
    }

    /**
     * The full folder library — backs F-Folders' "Add to folder" page in
     * [com.facetlauncher.app.ui.components.AppContextMenu]. A folder is global, so this is the
     * one list every long-press context (Drawer, Search) offers to, regardless of where (if
     * anywhere) each folder is currently placed.
     */
    val folders: StateFlow<List<Folder>> = folderRepository.observeFolders()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    /** Creates a brand-new folder and adds [app] as its first member. Membership only — placing the folder onto a Dock/Favorites list is a separate, explicit action (the picker). */
    fun createFolder(app: AppInfo, name: String) {
        viewModelScope.launch {
            val folderId = folderRepository.createFolder(name)
            folderRepository.addAppToFolder(folderId, app)
        }
    }

    fun addToFolder(app: AppInfo, folderId: Long) {
        viewModelScope.launch { folderRepository.addAppToFolder(folderId, app) }
    }

    fun removeFromFolder(folderId: Long, app: AppInfo) {
        viewModelScope.launch { folderRepository.removeAppFromFolder(folderId, app) }
    }

    fun renameFolder(folderId: Long, name: String) {
        viewModelScope.launch { folderRepository.renameFolder(folderId, name) }
    }

    /** Phase 9's connections sheet — fetched fresh per contact, only when their sheet actually opens (same fetch-on-open precedent as [getShortcuts]). */
    suspend fun getConnections(contact: ContactInfo): List<ContactConnection> = contactRepository.getConnections(contact.id)
}
