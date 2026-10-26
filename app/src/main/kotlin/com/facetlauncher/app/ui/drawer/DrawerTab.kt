package com.facetlauncher.app.ui.drawer

import android.os.UserHandle
import com.facetlauncher.app.data.WorkProfileInfo
import com.facetlauncher.app.data.model.AppInfo
import com.facetlauncher.app.data.model.AppProfile
import com.facetlauncher.app.data.model.DrawerFolderDisplayMode
import com.facetlauncher.app.data.model.DrawerItem
import com.facetlauncher.app.data.model.DrawerPresentation
import com.facetlauncher.app.data.model.Folder
import com.facetlauncher.app.data.model.RecentlyInstalledPosition
import com.facetlauncher.app.domain.GroupAppsByLetterUseCase
import com.facetlauncher.app.domain.GroupedItems
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList

/**
 * Which profile's apps the App Drawer is currently browsing — a browse-mode filter only, see
 * [AppDrawerScreen]'s own `isSearching` doc. [Personal] is always present and merges
 * [AppProfile.PERSONAL] with [AppProfile.OTHER] (an unclassifiable profile — most commonly an OEM
 * clone/dual-app profile — gets a badge on its tile, not its own tab, since below API 35 there's
 * no way to positively identify what it actually is). [Work] is one per live, positively-classified
 * Work Profile handle — almost always 0 or 1 in practice (stock Android provisions at most one),
 * but keyed by the real handle rather than the display category so it stays correct even if that
 * ever isn't true.
 */
internal sealed class DrawerTab {
    data object Personal : DrawerTab()
    data class Work(val handle: UserHandle, val label: String) : DrawerTab()
}

/** Personal first, then one tab per Work Profile. */
internal fun drawerTabsFor(workProfiles: List<WorkProfileInfo>): ImmutableList<DrawerTab> =
    (listOf(DrawerTab.Personal) + workProfiles.map { DrawerTab.Work(it.handle, it.label) }).toImmutableList()

/** The apps the browse list is scoped to: every app while [filterByTab] is off (searching, or no Work Profile), else the selected tab's. */
internal fun appsForTab(apps: List<AppInfo>, filterByTab: Boolean, tab: DrawerTab): List<AppInfo> = when {
    !filterByTab -> apps
    tab is DrawerTab.Work -> apps.filter { it.userHandle == tab.handle }
    else -> apps.filter { it.profile == AppProfile.PERSONAL || it.profile == AppProfile.OTHER }
}

/** The folders pinned to the top or bottom of the drawer: all of [sortedFolders] for `SHOW_FIRST`/`SHOW_LAST`, none otherwise. */
internal fun pinnedFoldersFor(sortedFolders: List<Folder>, mode: DrawerFolderDisplayMode): ImmutableList<Folder> =
    if (mode == DrawerFolderDisplayMode.SHOW_FIRST || mode == DrawerFolderDisplayMode.SHOW_LAST) sortedFolders.toImmutableList() else persistentListOf()

/**
 * Leading-slot count only (List-only, and only when `SHOW_FIRST`): an icon header plus the "Recently installed" row itself, so
 * index math adds it wherever that leading section shifts a later index. A trailing `SHOW_LAST` row needs no offset: nothing
 * comes after it.
 */
internal fun recentlyInstalledLeadingSlots(presentation: DrawerPresentation, position: RecentlyInstalledPosition, hasApps: Boolean): Int =
    if (presentation != DrawerPresentation.GRID && position == RecentlyInstalledPosition.SHOW_FIRST && hasApps) RECENTLY_INSTALLED_SLOTS else 0

private const val RECENTLY_INSTALLED_SLOTS = 2

/**
 * Apps are always wrapped as [DrawerItem], even outside `INLINE`, so the list and grid have a single item type to render
 * whatever the folder mode; only `INLINE` merges [sortedFolders] into the alphabetical grouping (`SHOW_FIRST`/`SHOW_LAST` keep
 * folders out of it, in their own pinned section).
 *
 * Grouping only buckets by leading letter and never reorders within a bucket, so folders are merged in by sorting the combined
 * list by name first (apps arrive pre-sorted, so this only folds the folders into that order).
 */
internal fun groupDrawerItems(
    apps: List<AppInfo>,
    sortedFolders: List<Folder>,
    mode: DrawerFolderDisplayMode,
    group: GroupAppsByLetterUseCase,
): GroupedItems<DrawerItem> {
    val appItems: List<DrawerItem> = apps.map { DrawerItem.AppEntry(it) }
    val items = if (mode == DrawerFolderDisplayMode.INLINE) {
        (appItems + sortedFolders.map { DrawerItem.FolderEntry(it) }).sortedBy { it.displayName.lowercase() }
    } else {
        appItems
    }
    return group(items) { it.displayName }
}
