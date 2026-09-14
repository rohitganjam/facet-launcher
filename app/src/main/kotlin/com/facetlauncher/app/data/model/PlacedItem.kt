package com.facetlauncher.app.data.model

/** A folder's live, hydrated contents — an app no longer installed is dropped, never shown as a dead tile. */
data class Folder(
    val id: Long,
    val name: String,
    val apps: List<AppInfo>,
)

/** One slot in a Dock or Favorites list — either a standalone app or a folder. */
sealed interface PlacedItem {
    data class SingleApp(val app: AppInfo) : PlacedItem
    data class FolderItem(val folder: Folder) : PlacedItem
}
