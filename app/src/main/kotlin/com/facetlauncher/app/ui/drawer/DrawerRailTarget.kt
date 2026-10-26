package com.facetlauncher.app.ui.drawer

import com.facetlauncher.app.data.model.DrawerFolderDisplayMode
import com.facetlauncher.app.data.model.DrawerPresentation
import com.facetlauncher.app.data.model.RecentlyInstalledPosition
import com.facetlauncher.app.domain.GroupedItems

/**
 * The lazy list or grid index the drawer scrolls to when the alphabet rail selects [selection], or `null` when there is nothing
 * to jump to. Indices are absolute, so [recentlyInstalledOffset] (the leading "Recently installed" slots) shifts all of them.
 *
 * - A letter: List scrolls to that letter's header row, Grid (no headers, one continuous grid) to its first item. A `SHOW_FIRST`
 *   folder section sits ahead of every letter group, so letters shift by its slot count.
 * - Folders: the start of the pinned section, ahead of or after the lettered content.
 * - Recently installed: the very top, or after the lettered content and any trailing folder section.
 */
internal fun railScrollTarget(
    selection: RailSelection,
    presentation: DrawerPresentation,
    groupedItems: GroupedItems<*>,
    folderDisplayMode: DrawerFolderDisplayMode,
    pinnedFolderCount: Int,
    recentlyInstalledPosition: RecentlyInstalledPosition,
    recentlyInstalledOffset: Int,
): Int? {
    val isGrid = presentation == DrawerPresentation.GRID
    val folderSlots = pinnedSectionSlots(isGrid, pinnedFolderCount)
    val afterLettered = recentlyInstalledOffset + groupedItems.groups.values.sumOf { if (isGrid) it.size else 1 + it.size }
    return when (selection) {
        is RailSelection.Letter -> {
            val indexMap = if (isGrid) groupedItems.firstAppIndexForLetter else groupedItems.headerIndexForLetter
            val leading = recentlyInstalledOffset + if (folderDisplayMode == DrawerFolderDisplayMode.SHOW_FIRST) folderSlots else 0
            indexMap[selection.letter]?.plus(leading)
        }
        RailSelection.Folders -> when (folderDisplayMode) {
            DrawerFolderDisplayMode.SHOW_FIRST -> recentlyInstalledOffset
            DrawerFolderDisplayMode.SHOW_LAST -> afterLettered
            else -> null
        }
        RailSelection.RecentlyInstalled -> when (recentlyInstalledPosition) {
            RecentlyInstalledPosition.SHOW_FIRST -> 0
            RecentlyInstalledPosition.SHOW_LAST -> afterLettered + if (folderDisplayMode == DrawerFolderDisplayMode.SHOW_LAST) folderSlots else 0
            RecentlyInstalledPosition.DO_NOT_SHOW -> null
        }
    }
}

/** Lazy slots the pinned folder section takes: its tiles, plus a header row in List (Grid renders no headers). None at all without folders. */
private fun pinnedSectionSlots(isGrid: Boolean, folderCount: Int): Int = when {
    folderCount == 0 -> 0
    isGrid -> folderCount
    else -> 1 + folderCount
}
