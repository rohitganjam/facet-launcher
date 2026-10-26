package com.facetlauncher.app.ui.drawer

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.facetlauncher.app.data.WorkProfileInfo
import com.facetlauncher.app.data.model.AppInfo
import com.facetlauncher.app.data.model.DrawerFolderDisplayMode
import com.facetlauncher.app.data.model.DrawerItem
import com.facetlauncher.app.data.model.DrawerPresentation
import com.facetlauncher.app.data.model.Folder
import com.facetlauncher.app.data.model.RecentlyInstalledPosition
import com.facetlauncher.app.domain.GroupedItems
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList

/** Search shows at most this many apps (contacts and settings have their own sections). */
internal const val MAX_APP_SEARCH_RESULTS = 5

/** What the drawer derives from its inputs for browsing and searching: the tabs, the grouped list and the rail's state. */
@Immutable
internal class DrawerBrowseModel(
    val tabs: ImmutableList<DrawerTab>,
    val selectedTab: DrawerTab,
    val onSelectTab: (DrawerTab) -> Unit,
    val searchResultApps: ImmutableList<AppInfo>,
    val groupedItems: GroupedItems<DrawerItem>,
    val pinnedFolders: ImmutableList<Folder>,
    val recentlyInstalledApps: ImmutableList<AppInfo>,
    val recentlyInstalledOffset: Int,
    val railLetters: ImmutableList<String>,
    val railFolderPosition: RailFolderPosition,
    val railRecentlyInstalledPosition: RailFolderPosition,
)

/**
 * Browse mode is scoped to the selected tab; search spans every profile regardless of which tab is active — the tab is a
 * browsing filter, not a search filter (the point of switching tabs is narrowing what you scroll through, not what you can find).
 * Search-mode never shows folders, so a folder never has to be search-ranked.
 */
@Composable
internal fun rememberDrawerBrowseModel(
    apps: ImmutableList<AppInfo>,
    workProfiles: ImmutableList<WorkProfileInfo>,
    query: String,
    folderCandidates: ImmutableList<Folder>?,
    presentation: DrawerPresentation,
    folderDisplayMode: DrawerFolderDisplayMode,
    recentlyInstalledPosition: RecentlyInstalledPosition,
    listUseCases: DrawerListUseCases,
): DrawerBrowseModel {
    val isSearching = query.isNotBlank()
    val tabs = remember(workProfiles) { drawerTabsFor(workProfiles) }
    var selectedTab by remember { mutableStateOf<DrawerTab>(DrawerTab.Personal) }
    // A Work Profile can vanish (unenrolled) while its tab is selected — fall back to Personal
    // rather than keep filtering by a handle that no longer resolves to anything.
    LaunchedEffect(tabs) {
        if (selectedTab !in tabs) selectedTab = DrawerTab.Personal
    }
    val tabScopedApps = remember(apps, isSearching, selectedTab, workProfiles) {
        appsForTab(apps, filterByTab = !isSearching && workProfiles.isNotEmpty(), tab = selectedTab)
    }
    val rankBySearchRelevance = listUseCases.rank
    val filteredApps = remember(tabScopedApps, query) { rankBySearchRelevance(tabScopedApps, query) { it.label } }
    val sortedFolders = remember(folderCandidates) { folderCandidates.orEmpty().sortedBy { it.name } }
    val pinnedFolders = remember(sortedFolders, folderDisplayMode) { pinnedFoldersFor(sortedFolders, folderDisplayMode) }
    // Scoped to tabScopedApps, not the raw apps param — unlike pinnedFolders, deliberately NOT tab-filtered.
    val recentlyInstalledUseCase = listUseCases.recentlyInstalled
    val recentlyInstalledApps = remember(tabScopedApps, isSearching, recentlyInstalledPosition) {
        if (recentlyInstalledPosition != RecentlyInstalledPosition.DO_NOT_SHOW && !isSearching) {
            recentlyInstalledUseCase(tabScopedApps).toImmutableList()
        } else {
            persistentListOf()
        }
    }
    val recentlyInstalledOffset = recentlyInstalledLeadingSlots(presentation, recentlyInstalledPosition, recentlyInstalledApps.isNotEmpty())
    val groupUseCase = listUseCases.group
    val groupedItems = remember(filteredApps, sortedFolders, folderDisplayMode) {
        groupDrawerItems(filteredApps, sortedFolders, folderDisplayMode, groupUseCase)
    }
    return DrawerBrowseModel(
        tabs = tabs,
        selectedTab = selectedTab,
        onSelectTab = { selectedTab = it },
        searchResultApps = remember(filteredApps) { filteredApps.take(MAX_APP_SEARCH_RESULTS).toImmutableList() },
        groupedItems = groupedItems,
        pinnedFolders = pinnedFolders,
        recentlyInstalledApps = recentlyInstalledApps,
        recentlyInstalledOffset = recentlyInstalledOffset,
        railLetters = remember(groupedItems) { groupedItems.letters.toImmutableList() },
        railFolderPosition = railFolderPositionFor(pinnedFolders.isNotEmpty(), folderDisplayMode),
        railRecentlyInstalledPosition = railRecentlyInstalledPositionFor(
            hasLeadingRow = recentlyInstalledOffset > 0,
            hasTrailingRow = presentation != DrawerPresentation.GRID &&
                recentlyInstalledPosition == RecentlyInstalledPosition.SHOW_LAST &&
                recentlyInstalledApps.isNotEmpty(),
        ),
    )
}

/** The rail's folder glyph sits at the end where the pinned section is, and is absent with no folders pinned. */
internal fun railFolderPositionFor(hasPinnedFolders: Boolean, mode: DrawerFolderDisplayMode): RailFolderPosition = when {
    !hasPinnedFolders -> RailFolderPosition.NONE
    mode == DrawerFolderDisplayMode.SHOW_FIRST -> RailFolderPosition.TOP
    mode == DrawerFolderDisplayMode.SHOW_LAST -> RailFolderPosition.BOTTOM
    else -> RailFolderPosition.NONE
}

/** The "Recently installed" glyph sits at the top for a leading row, the bottom for a trailing one. */
internal fun railRecentlyInstalledPositionFor(hasLeadingRow: Boolean, hasTrailingRow: Boolean): RailFolderPosition = when {
    hasLeadingRow -> RailFolderPosition.TOP
    hasTrailingRow -> RailFolderPosition.BOTTOM
    else -> RailFolderPosition.NONE
}
