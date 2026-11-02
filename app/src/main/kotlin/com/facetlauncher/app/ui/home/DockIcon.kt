package com.facetlauncher.app.ui.home

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.onClick
import androidx.compose.ui.unit.dp
import com.facetlauncher.app.data.model.AppInfo
import com.facetlauncher.app.data.model.DockDisplayMode
import com.facetlauncher.app.data.model.Folder
import com.facetlauncher.app.data.model.PlacedItem
import com.facetlauncher.app.ui.components.AppIcon
import com.facetlauncher.app.ui.components.AppIconSize
import com.facetlauncher.app.ui.components.NotificationBadge
import com.facetlauncher.app.ui.components.longPressReleaseClickable
import com.facetlauncher.app.ui.home.DockIcon
import com.facetlauncher.app.ui.home.FolderTileGlyph
import com.facetlauncher.app.ui.theme.homeAppLabelShadow

/**
 * One dock slot: an app, or a folder drawn as a mini-grid of its first apps. Not private: the facet carousel's
 * preview cards reuse it so the dock renders the same there. A tap launches the app or opens the folder's
 * sheet; a long-press opens the app or folder menu, both off when [enableLongPressMenu] is off (the carousel's
 * read-only preview cards aren't a place to manage apps).
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
internal fun DockIcon(
    item: PlacedItem,
    displayMode: DockDisplayMode,
    actions: HomeItemActions,
    style: HomeItemStyle,
    modifier: Modifier = Modifier,
    enableLongPressMenu: Boolean = true,
) {
    when (item) {
        is PlacedItem.SingleApp -> SingleAppDockIcon(item.app, displayMode, actions, style, modifier, enableLongPressMenu)
        is PlacedItem.FolderItem -> FolderDockIcon(item.folder, displayMode, actions, style, modifier, enableLongPressMenu)
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun SingleAppDockIcon(
    app: AppInfo,
    displayMode: DockDisplayMode,
    actions: HomeItemActions,
    style: HomeItemStyle,
    modifier: Modifier = Modifier,
    enableLongPressMenu: Boolean = true,
) {
    var menuExpanded by remember { mutableStateOf(false) }
    val onClick = { actions.onAppClick(app) }
    val clickModifier = if (enableLongPressMenu) {
        Modifier.longPressReleaseClickable(onClick = onClick, onLongPress = { menuExpanded = true })
    } else {
        Modifier.clickable(onClick = onClick)
    }
    val badgeCount = style.badgeCounts[app.packageName]
    Box(modifier = modifier) {
        // A straight boolean, not a `when` over DockDisplayMode: callers pass a resolved value, so
        // LAUNCHER_DEFAULT never arrives and renders as icons like ICONS.
        if (displayMode == DockDisplayMode.TEXT) {
            Row(
                modifier = clickModifier.padding(vertical = 14.dp, horizontal = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                Text(
                    text = app.label,
                    style = MaterialTheme.typography.bodyMedium.copy(shadow = homeAppLabelShadow(style.labelColor), fontWeight = style.labelFontWeight),
                    color = style.labelColor,
                )
                // Text mode has no icon to anchor the badge to, so it trails the label.
                if (badgeCount != null && badgeCount > 0) NotificationBadge(count = badgeCount, style = style.badgeStyle)
            }
        } else {
            Column(modifier = clickModifier, horizontalAlignment = Alignment.CenterHorizontally) {
                AppIcon(app = app, size = AppIconSize.scaled(), contentDescription = app.label, notificationCount = badgeCount, badgeStyle = style.badgeStyle)
                if (displayMode == DockDisplayMode.ICONS_AND_TEXT) HomeTileLabel(app.label, style, Modifier.testTag(homeTag("dock_label_${app.packageName}")))
            }
        }
        if (enableLongPressMenu) AppItemMenu(app, menuExpanded, { menuExpanded = false }, actions, style)
    }
}

/** [PlacedItem.FolderItem]'s own tile. A tap opens the folder's sheet, a long-press its menu. */
@Composable
private fun FolderDockIcon(
    folder: Folder,
    displayMode: DockDisplayMode,
    actions: HomeItemActions,
    style: HomeItemStyle,
    modifier: Modifier = Modifier,
    enableLongPressMenu: Boolean = true,
) {
    var sheetOpen by remember { mutableStateOf(false) }
    var menuExpanded by remember { mutableStateOf(false) }
    val clickModifier = if (enableLongPressMenu) {
        Modifier.longPressReleaseClickable(onClick = { sheetOpen = true }, onLongPress = { menuExpanded = true })
    } else {
        Modifier.clickable(onClick = {})
    }
    Box(modifier = modifier) {
        if (displayMode == DockDisplayMode.TEXT) {
            Row(
                modifier = clickModifier.testTag(homeTag("dock_folder_tile_${folder.id}")).padding(vertical = 14.dp, horizontal = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                Text(
                    text = folder.name,
                    style = MaterialTheme.typography.bodyMedium.copy(shadow = homeAppLabelShadow(style.labelColor), fontWeight = style.labelFontWeight),
                    color = style.labelColor,
                )
            }
        } else {
            Column(modifier = clickModifier.testTag(homeTag("dock_folder_tile_${folder.id}")), horizontalAlignment = Alignment.CenterHorizontally) {
                FolderTileGlyph(folder = folder, size = AppIconSize.scaled())
                if (displayMode == DockDisplayMode.ICONS_AND_TEXT) HomeTileLabel(folder.name, style)
            }
        }
        if (enableLongPressMenu && sheetOpen) FolderItemSheet(folder, { sheetOpen = false }, actions, style)
        if (enableLongPressMenu) FolderItemMenu(folder, menuExpanded, { menuExpanded = false }, actions)
    }
}
