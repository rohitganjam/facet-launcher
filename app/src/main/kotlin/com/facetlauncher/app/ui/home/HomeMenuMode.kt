package com.facetlauncher.app.ui.home

import com.facetlauncher.app.data.model.PlacedItem

/**
 * [MENU] shows the Home sheet (a long-press on free space: facet and launcher settings); [WIDGET_MENU] shows the widget's own sheet
 * (a long-press on the clock, its calendar or any widget page); [CALENDAR_SETTINGS] shows the clock's calendar settings sheet, opened from that one; [ADJUST] shows the move handle *and* the resize handles together.
 */
enum class HomeMenuMode { NONE, MENU, WIDGET_MENU, CALENDAR_SETTINGS, ADJUST }

/** A [PlacedItem]'s stable identity for keying/reordering — a folder's own id, or its app's component. */
internal fun PlacedItem.stableKey(): Any = when (this) {
    is PlacedItem.SingleApp -> app.packageName to app.activityName
    is PlacedItem.FolderItem -> "folder_${folder.id}"
}
