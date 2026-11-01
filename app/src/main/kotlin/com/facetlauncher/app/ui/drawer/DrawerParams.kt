package com.facetlauncher.app.ui.drawer

import android.content.Intent
import androidx.compose.runtime.Immutable
import androidx.compose.ui.text.font.FontWeight
import com.facetlauncher.app.data.model.AppInfo
import com.facetlauncher.app.data.model.ContactConnection
import com.facetlauncher.app.data.model.ContactInfo
import com.facetlauncher.app.data.model.DrawerFolderDisplayMode
import com.facetlauncher.app.data.model.DrawerGridDensity
import com.facetlauncher.app.data.model.DrawerListItemSize
import com.facetlauncher.app.data.model.DrawerPresentation
import com.facetlauncher.app.data.model.Folder
import com.facetlauncher.app.data.model.NotificationBadgeStyle
import com.facetlauncher.app.data.model.RecentlyInstalledPosition
import com.facetlauncher.app.data.model.SearchBarPosition
import com.facetlauncher.app.data.model.SettingsSearchEntry
import com.facetlauncher.app.domain.QuickAddState
import com.facetlauncher.app.domain.QuickPlacementAction
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.ImmutableMap
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.persistentMapOf

/** What a tap or long-press on an app or folder in the drawer does. The quick-add states say what each long-press row offers. */
@Immutable
data class DrawerItemActions(
    val onAppClick: (AppInfo) -> Unit = {},
    val onAppInfo: (AppInfo) -> Unit = {},
    val onRequestQuickAddState: suspend (AppInfo) -> QuickAddState = { QuickAddState() },
    val onFavoritesAction: (AppInfo, QuickPlacementAction) -> Unit = { _, _ -> },
    val onDockAction: (AppInfo, QuickPlacementAction) -> Unit = { _, _ -> },
    val onCreateFolder: (AppInfo, String) -> Unit = { _, _ -> },
    val onAddToFolder: (AppInfo, Long) -> Unit = { _, _ -> },
    val onRemoveFromFolder: (Long, AppInfo) -> Unit = { _, _ -> },
    val onRenameFolder: (Long, String) -> Unit = { _, _ -> },
    val onRequestFolderQuickAddState: suspend (Folder) -> QuickAddState = { QuickAddState() },
    val onFolderFavoritesAction: (Folder, QuickPlacementAction) -> Unit = { _, _ -> },
    val onFolderDockAction: (Folder, QuickPlacementAction) -> Unit = { _, _ -> },
)

/** How the drawer looks: the layout, the item styling and where folders and recently installed apps sit. */
@Immutable
data class DrawerAppearance(
    val presentation: DrawerPresentation = DrawerPresentation.LIST,
    val gridDensity: DrawerGridDensity = DrawerGridDensity.COMPACT,
    val listItemSize: DrawerListItemSize = DrawerListItemSize.REGULAR,
    val showIcons: Boolean = true,
    val showLabels: Boolean = true,
    val labelFontWeight: FontWeight = FontWeight.Normal,
    val opacity: Float = 0.6f,
    val notificationBadgeStyle: NotificationBadgeStyle = NotificationBadgeStyle.DOT,
    val badgeCounts: ImmutableMap<String, Int> = persistentMapOf(),
    /** Settings → App Drawer → "Folders in drawer" — browse-mode only; search results never include folders. */
    val folderDisplayMode: DrawerFolderDisplayMode = DrawerFolderDisplayMode.DO_NOT_SHOW,
    /** Settings → App Drawer → "Recently installed" — `SHOW_FIRST`/`SHOW_LAST` place the category above/below everything (including a same-side pinned folder section); `DO_NOT_SHOW` hides it. Browse-mode only, same as [folderDisplayMode]. */
    val recentlyInstalledPosition: RecentlyInstalledPosition = RecentlyInstalledPosition.SHOW_FIRST,
)

/** The search bar and what hangs off it: its position, the settings link, and the Secure Folder and Private Space rows. */
@Immutable
data class DrawerSearchBarConfig(
    val position: SearchBarPosition = SearchBarPosition.TOP,
    val onNavigateToSettings: () -> Unit = {},
    /** `null` when Secure Folder isn't installed — see [DrawerSearchBar]'s own doc for why this isn't Work-Profile-style enumeration. */
    val secureFolderIntent: Intent? = null,
    val onOpenSecureFolder: (Intent) -> Unit = {},
    /** Shown for [com.facetlauncher.app.data.PrivateSpaceState.Locked]/`.Unlocked`, hidden for `.NotConfigured`. */
    val showPrivateSpaceRow: Boolean = false,
    val onPrivateSpaceRowClick: () -> Unit = {},
    /** Each increase focuses the search field, as if tapped. */
    val focusRequest: Int = 0,
)

/** The contacts and system Settings sections of the search results, and the prompts that ask for what they need. */
@Immutable
data class DrawerSearchSources(
    val contacts: ImmutableList<ContactInfo> = persistentListOf(),
    val onRequestConnections: suspend (ContactInfo) -> List<ContactConnection> = { emptyList() },
    /** True while the search wants to show contacts but `READ_CONTACTS` isn't granted — see `DrawerViewModel.showContactsPermissionPrompt`'s own doc for the exact condition. */
    val showContactsPermissionPrompt: Boolean = false,
    val onContactsPermissionPromptClick: () -> Unit = {},
    /** True while `READ_CONTACTS` is granted but the "Search contacts" setting is off — see `DrawerViewModel.showContactsSettingPrompt`'s own doc for the exact condition. */
    val showContactsSettingPrompt: Boolean = false,
    val onContactsSettingPromptClick: () -> Unit = {},
    /** Search's system Settings section (Settings → App Drawer → "Search settings") — see `DrawerViewModel.settingsResults`. */
    val settingsEntries: ImmutableList<SettingsSearchEntry> = persistentListOf(),
    val onSettingsEntryClick: (SettingsSearchEntry) -> Unit = {},
)
