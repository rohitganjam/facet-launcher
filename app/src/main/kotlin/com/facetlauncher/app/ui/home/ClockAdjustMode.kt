package com.facetlauncher.app.ui.home

import com.facetlauncher.app.data.model.PlacedItem

/**
 * [MENU] shows the Home sheet (opened by a long-press on free space or on a widget page); [STYLE_MENU] shows the clock's own two-row
 * sheet (a long-press on the clock or its calendar); [ADJUST] shows the move handle *and* the resize handles together.
 */
enum class ClockAdjustMode { NONE, MENU, STYLE_MENU, ADJUST }

/** A [PlacedItem]'s stable identity for keying/reordering — a folder's own id, or its app's component. */
internal fun PlacedItem.stableKey(): Any = when (this) {
    is PlacedItem.SingleApp -> app.packageName to app.activityName
    is PlacedItem.FolderItem -> "folder_${folder.id}"
}
