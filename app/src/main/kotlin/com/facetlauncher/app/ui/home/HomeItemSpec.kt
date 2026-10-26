package com.facetlauncher.app.ui.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.facetlauncher.app.data.model.AppInfo
import com.facetlauncher.app.data.model.AppRowPosition
import com.facetlauncher.app.data.model.DrawerPresentation
import com.facetlauncher.app.data.model.Folder
import com.facetlauncher.app.data.model.NotificationBadgeStyle
import com.facetlauncher.app.data.model.PlacedItem
import com.facetlauncher.app.domain.QuickAddState
import com.facetlauncher.app.domain.QuickPlacementAction
import com.facetlauncher.app.ui.components.AppContextMenu
import com.facetlauncher.app.ui.components.AppMenuNotifications
import com.facetlauncher.app.ui.components.FolderContentsSheet
import com.facetlauncher.app.ui.components.FolderSheetHeaderAction
import com.facetlauncher.app.ui.components.FolderTileContextMenu
import com.facetlauncher.app.ui.home.HomeItemActions
import com.facetlauncher.app.ui.home.HomeItemStyle
import kotlinx.collections.immutable.ImmutableList

/**
 * What every app and folder tile on Home does when tapped, long-pressed or edited. One group, so the
 * rows, grid tiles and dock icons share one parameter instead of a dozen callbacks each.
 */
@Immutable
data class HomeItemActions(
    val onAppClick: (AppInfo) -> Unit,
    val onAppInfo: (AppInfo) -> Unit = {},
    val onRequestQuickAddState: suspend (AppInfo) -> QuickAddState = { QuickAddState() },
    val onFavoritesAction: (AppInfo, QuickPlacementAction) -> Unit = { _, _ -> },
    val onDockAction: (AppInfo, QuickPlacementAction) -> Unit = { _, _ -> },
    val onRequestFolderQuickAddState: suspend (Folder) -> QuickAddState = { QuickAddState() },
    val onFolderFavoritesAction: (Folder, QuickPlacementAction) -> Unit = { _, _ -> },
    val onFolderDockAction: (Folder, QuickPlacementAction) -> Unit = { _, _ -> },
    val onCreateFolder: (AppInfo, String) -> Unit = { _, _ -> },
    val onAddToFolder: (AppInfo, Long) -> Unit = { _, _ -> },
    val onRemoveFromFolder: (Long, AppInfo) -> Unit = { _, _ -> },
    val onRenameFolder: (Long, String) -> Unit = { _, _ -> },
)

/** How Home's app and folder tiles look, and the context their menus offer. */
@Immutable
data class HomeItemStyle(
    /** Home's label colour; it is a themed value, so callers pass it in. */
    val labelColor: Color,
    /** Notification counts by package name. */
    val badgeCounts: Map<String, Int> = emptyMap(),
    val badgeStyle: NotificationBadgeStyle = NotificationBadgeStyle.DOT,
    val appMenuNotifications: AppMenuNotifications = AppMenuNotifications.None,
    /** The full folder library: see [AppContextMenu]'s own doc for what non-null means. */
    val folderCandidates: ImmutableList<Folder>? = null,
    val drawerPresentation: DrawerPresentation = DrawerPresentation.LIST,
    val labelFontWeight: FontWeight = FontWeight.Normal,
)

/** The items an app list shows, with the style they are drawn in. */
@Immutable
data class HomeAppListContent(val items: List<PlacedItem>, val style: HomeItemStyle)

/** [app]'s long-press menu, wired to [actions] and [style]. */
@Composable
internal fun AppItemMenu(app: AppInfo, expanded: Boolean, onDismiss: () -> Unit, actions: HomeItemActions, style: HomeItemStyle) {
    AppContextMenu(
        app = app,
        expanded = expanded,
        onDismissRequest = onDismiss,
        appMenuNotifications = style.appMenuNotifications,
        onAppInfo = actions.onAppInfo,
        onRequestQuickAddState = actions.onRequestQuickAddState,
        onFavoritesAction = actions.onFavoritesAction,
        onDockAction = actions.onDockAction,
        folderCandidates = style.folderCandidates,
        onCreateFolder = actions.onCreateFolder,
        onAddToFolder = actions.onAddToFolder,
        drawerPresentation = style.drawerPresentation,
    )
}

/** The sheet a folder tile opens on tap: its apps, with a Rename header. */
@Composable
internal fun FolderItemSheet(folder: Folder, onDismiss: () -> Unit, actions: HomeItemActions, style: HomeItemStyle) {
    FolderContentsSheet(
        folder = folder,
        onDismissRequest = onDismiss,
        onAppClick = actions.onAppClick,
        presentation = style.drawerPresentation,
        onRemoveFromFolder = actions.onRemoveFromFolder,
        headerAction = FolderSheetHeaderAction.Rename(onRename = actions.onRenameFolder),
        appMenuNotifications = style.appMenuNotifications,
        onAppInfo = actions.onAppInfo,
    )
}

/** A folder tile's long-press menu (Rename, plus the Favorites and Dock placement actions). */
@Composable
internal fun FolderItemMenu(folder: Folder, expanded: Boolean, onDismiss: () -> Unit, actions: HomeItemActions) {
    FolderTileContextMenu(
        folder = folder,
        expanded = expanded,
        onDismissRequest = onDismiss,
        onRename = actions.onRenameFolder,
        onRequestQuickAddState = actions.onRequestFolderQuickAddState,
        onFavoritesAction = actions.onFolderFavoritesAction,
        onDockAction = actions.onFolderDockAction,
    )
}

/**
 * How a row's content is packed. RIGHT hugs the trailing edge, CENTER centres the group, LEFT (and the
 * `LAUNCHER_DEFAULT` sentinel, which callers always resolve away) hugs the start.
 */
internal fun appRowArrangement(position: AppRowPosition): Arrangement.Horizontal = when (position) {
    AppRowPosition.RIGHT -> Arrangement.spacedBy(14.dp, Alignment.End)
    AppRowPosition.CENTER -> Arrangement.spacedBy(14.dp, Alignment.CenterHorizontally)
    AppRowPosition.LEFT, AppRowPosition.LAUNCHER_DEFAULT -> Arrangement.spacedBy(14.dp)
}
