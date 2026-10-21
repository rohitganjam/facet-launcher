package com.facetlauncher.app.ui.home

import com.facetlauncher.app.data.model.PlacedItem

/** [MENU] shows the long-press bottom sheet; [ADJUST] shows the move handle *and* the resize handles together. */
enum class ClockAdjustMode { NONE, MENU, ADJUST }

/** A [PlacedItem]'s stable identity for keying/reordering — a folder's own id, or its app's component. */
internal fun PlacedItem.stableKey(): Any = when (this) {
    is PlacedItem.SingleApp -> app.packageName to app.activityName
    is PlacedItem.FolderItem -> "folder_${folder.id}"
}
