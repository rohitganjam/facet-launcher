package com.facetlauncher.app.ui.components

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.outlined.FolderOpen
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.compose.ui.window.DialogWindowProvider
import com.facetlauncher.app.R
import com.facetlauncher.app.data.model.AppInfo
import com.facetlauncher.app.data.model.AppShortcut
import com.facetlauncher.app.data.model.DrawerPresentation
import com.facetlauncher.app.data.model.Folder
import com.facetlauncher.app.ui.theme.Accent
import com.facetlauncher.app.ui.theme.Faint
import com.facetlauncher.app.ui.theme.Ink
import com.facetlauncher.app.ui.theme.Muted
import com.facetlauncher.app.ui.theme.Scrim
import com.facetlauncher.app.ui.theme.Surface

/** The sheet's header trailing action — either editing this folder in place, or confirming a pending add into it. */
sealed interface FolderSheetHeaderAction {
    /** The normal case: tapping a folder tile opens its contents with a rename pencil. */
    data class Rename(val onRename: (Long, String) -> Unit) : FolderSheetHeaderAction

    /** Reached from [AppContextMenu]'s "Add to folder" list — previews this folder's real contents before the pending app is actually added; tapping Add performs the add and dismisses. [onBack] returns to that folder list (a leading chevron in the header, since this preview sits on top of it rather than replacing it) rather than dismissing the whole flow. */
    data class AddHere(val onAdd: () -> Unit, val onBack: () -> Unit) : FolderSheetHeaderAction
}

/**
 * Tapping a folder tile (Home, Dock, or the app list) opens this — same hand-rolled bottom-sheet
 * idiom as [com.facetlauncher.app.ui.drawer.ContactConnectionsSheet] (dynamically sized between
 * 30%-70% of available height, [CardDivider]-separated rows), deliberately not a plain
 * `Dialog`/`Popup` appearance, since that's this app's established shape for "tap something small,
 * reveal what's inside" — as opposed to [AppContextMenu]'s real `ModalBottomSheet`, reserved for
 * long-press context menus. Wrapped in a full-screen [Dialog] so it can be hosted locally without
 * threading extra state through a large prop surface — [ContactConnectionsSheet] hoists the
 * equivalent state to its host screen's own top level instead, but the visual result is the same.
 *
 * Content follows the App Drawer's own [presentation] (List/Grid) exactly, and shows an explicit
 * empty-state message for a 0-app folder rather than a bare blank sheet — a folder is never
 * auto-deleted for being empty, so this is a real, reachable state.
 *
 * [headerAction] governs the trailing header action, both rendered as the same `Accent` text-button
 * treatment: [FolderSheetHeaderAction.Rename] ("Rename", the normal case) or
 * [FolderSheetHeaderAction.AddHere] ("Add to folder", reached from "Add to folder"'s existing-folder
 * row — see [AppContextMenu]'s own doc for why picking an existing folder previews instead of adding
 * immediately). Long-pressing a contained app reuses [AppContextMenu] a third time (Dock/Drawer/
 * Search's "Add to folder" being the other two), with [AppContextMenu.removeFromFolderId] swapping
 * in "Remove from folder" in place of "Add to folder".
 */
@Composable
fun FolderContentsSheet(
    folder: Folder,
    onDismissRequest: () -> Unit,
    onAppClick: (AppInfo) -> Unit,
    presentation: DrawerPresentation,
    onRemoveFromFolder: (Long, AppInfo) -> Unit,
    modifier: Modifier = Modifier,
    headerAction: FolderSheetHeaderAction = FolderSheetHeaderAction.Rename(onRename = { _, _ -> }),
    gridColumns: Int = 5,
    onRequestShortcuts: suspend (AppInfo) -> List<AppShortcut> = { emptyList() },
    onLaunchShortcut: (AppShortcut) -> Unit = {},
    onAppInfo: (AppInfo) -> Unit = {},
) {
    var showRenameDialog by remember { mutableStateOf(false) }

    Dialog(onDismissRequest = onDismissRequest, properties = DialogProperties(usePlatformDefaultWidth = false)) {
        // Disable the platform Dialog's own default window dim — it would stack on top of the Scrim Box below.
        val view = LocalView.current
        SideEffect { (view.parent as? DialogWindowProvider)?.window?.setDimAmount(0f) }
        Box(
            modifier = modifier
                .fillMaxSize()
                .then(if (headerAction is FolderSheetHeaderAction.AddHere) Modifier else Modifier.background(Scrim))
                .clickable(onClick = onDismissRequest),
        ) {
            BoxWithConstraints(modifier = Modifier.fillMaxWidth().align(Alignment.BottomCenter)) {
                val minHeight = maxHeight * 0.3f
                val maxSheetHeight = maxHeight * 0.7f
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("folder_contents_sheet")
                        // M3's ModalBottomSheet default shape (SheetDefaults.ExpandedShape) is
                        // extraLarge (28dp), applied top-only — see CLAUDE.md's Material 3 shape section.
                        .background(Surface, shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp))
                        // clickable after background/clip so a ripple (if this were ever enabled) would
                        // respect the shape above rather than fill the full rectangular bounds.
                        .clickable(enabled = false, onClick = {})
                        .heightIn(min = minHeight, max = maxSheetHeight),
                ) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.CenterHorizontally)
                            .padding(top = 12.dp, bottom = 8.dp)
                            .size(width = 34.dp, height = 4.dp)
                            .background(color = Faint, shape = RoundedCornerShape(2.dp)),
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(start = 12.dp, end = 24.dp, bottom = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        if (headerAction is FolderSheetHeaderAction.AddHere) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                                contentDescription = stringResource(R.string.content_description_back),
                                tint = Ink,
                                modifier = Modifier
                                    .testTag("folder_contents_sheet_back")
                                    .clickable(onClick = headerAction.onBack)
                                    .padding(8.dp),
                            )
                        }
                        Text(
                            text = folder.name,
                            style = MaterialTheme.typography.titleMedium,
                            color = Ink,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f).padding(start = if (headerAction is FolderSheetHeaderAction.AddHere) 0.dp else 12.dp),
                        )
                        when (headerAction) {
                            is FolderSheetHeaderAction.Rename -> Text(
                                text = stringResource(R.string.action_rename),
                                style = MaterialTheme.typography.bodyLarge,
                                color = Accent,
                                modifier = Modifier
                                    .testTag("folder_contents_sheet_rename")
                                    .clickable(onClick = { showRenameDialog = true }),
                            )
                            is FolderSheetHeaderAction.AddHere -> Text(
                                text = stringResource(R.string.folder_add_to_folder),
                                style = MaterialTheme.typography.bodyLarge,
                                color = Accent,
                                modifier = Modifier
                                    .testTag("folder_contents_sheet_add")
                                    .clickable(onClick = headerAction.onAdd),
                            )
                        }
                    }
                    if (folder.apps.isEmpty()) {
                        FolderContentsEmptyState()
                    } else if (presentation == DrawerPresentation.GRID) {
                        FolderContentsGrid(
                            folder = folder,
                            columns = gridColumns,
                            onAppClick = { app -> onDismissRequest(); onAppClick(app) },
                            onRequestShortcuts = onRequestShortcuts,
                            onLaunchShortcut = onLaunchShortcut,
                            onAppInfo = onAppInfo,
                            onRemoveFromFolder = { app -> onRemoveFromFolder(folder.id, app) },
                        )
                    } else {
                        FolderContentsList(
                            folder = folder,
                            onAppClick = { app -> onDismissRequest(); onAppClick(app) },
                            onRequestShortcuts = onRequestShortcuts,
                            onLaunchShortcut = onLaunchShortcut,
                            onAppInfo = onAppInfo,
                            onRemoveFromFolder = { app -> onRemoveFromFolder(folder.id, app) },
                        )
                    }
                }
            }
        }
    }

    if (showRenameDialog) {
        val renameAction = headerAction as? FolderSheetHeaderAction.Rename
        RenameDialog(
            title = stringResource(R.string.folder_rename_title),
            explanation = stringResource(R.string.folder_rename_explanation),
            initialValue = folder.name,
            onSave = { name ->
                showRenameDialog = false
                renameAction?.onRename?.invoke(folder.id, name)
            },
            onDismiss = { showRenameDialog = false },
        )
    }
}

/** Shown for a genuinely empty (0-app) folder — a folder is never auto-deleted for reaching this state, so it's a real, reachable UI, not just a loading gap. */
@Composable
private fun FolderContentsEmptyState(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier.fillMaxWidth().padding(horizontal = 24.dp, vertical = 32.dp),
        contentAlignment = Alignment.Center,
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(imageVector = Icons.Outlined.FolderOpen, contentDescription = null, tint = Muted, modifier = Modifier.size(32.dp))
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = stringResource(R.string.folder_no_apps_yet),
                style = MaterialTheme.typography.bodyMedium,
                color = Muted,
                textAlign = TextAlign.Center,
            )
        }
    }
}

@Composable
private fun FolderContentsList(
    folder: Folder,
    onAppClick: (AppInfo) -> Unit,
    onRequestShortcuts: suspend (AppInfo) -> List<AppShortcut>,
    onLaunchShortcut: (AppShortcut) -> Unit,
    onAppInfo: (AppInfo) -> Unit = {},
    onRemoveFromFolder: (AppInfo) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier.verticalScroll(rememberScrollState())) {
        folder.apps.forEachIndexed { index, app ->
            if (index > 0) CardDivider(modifier = Modifier.padding(horizontal = 24.dp))
            FolderContentsAppRow(
                app = app,
                onClick = { onAppClick(app) },
                onRequestShortcuts = onRequestShortcuts,
                onLaunchShortcut = onLaunchShortcut,
                onAppInfo = onAppInfo,
                onRemoveFromFolder = { onRemoveFromFolder(app) },
            )
        }
        Box(modifier = Modifier.padding(bottom = 24.dp))
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun FolderContentsAppRow(
    app: AppInfo,
    onClick: () -> Unit,
    onRequestShortcuts: suspend (AppInfo) -> List<AppShortcut>,
    onLaunchShortcut: (AppShortcut) -> Unit,
    onAppInfo: (AppInfo) -> Unit = {},
    onRemoveFromFolder: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var menuExpanded by remember { mutableStateOf(false) }
    Box(modifier = modifier) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .testTag("folder_contents_app_${app.packageName}")
                .longPressReleaseClickable(onClick = onClick, onLongPress = { menuExpanded = true })
                .padding(horizontal = 24.dp, vertical = 13.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            AppIcon(icon = app.icon, size = AppIconSize.ROW_REGULAR, contentDescription = null)
            Text(text = app.label, style = MaterialTheme.typography.bodyLarge, color = Ink)
        }
        AppContextMenu(
            app = app,
            expanded = menuExpanded,
            onDismissRequest = { menuExpanded = false },
            onRequestShortcuts = onRequestShortcuts,
            onLaunchShortcut = onLaunchShortcut,
            onAppInfo = onAppInfo,
            removeFromFolderId = 0L,
            onRemoveFromFolder = { _, _ -> onRemoveFromFolder() },
        )
    }
}

/** A small, non-lazy grid (folder membership is always small) replicating the App Drawer's own [com.facetlauncher.app.ui.drawer.AppDrawerScreen] grid-tile visual spec, since its own tile composable is private and too Drawer-specific to import directly. */
@Composable
private fun FolderContentsGrid(
    folder: Folder,
    columns: Int,
    onAppClick: (AppInfo) -> Unit,
    onRequestShortcuts: suspend (AppInfo) -> List<AppShortcut>,
    onLaunchShortcut: (AppShortcut) -> Unit,
    onAppInfo: (AppInfo) -> Unit = {},
    onRemoveFromFolder: (AppInfo) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier.verticalScroll(rememberScrollState()).padding(horizontal = 20.dp, vertical = 12.dp)) {
        folder.apps.chunked(columns.coerceAtLeast(1)).forEach { rowApps ->
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.padding(bottom = 16.dp)) {
                rowApps.forEach { app ->
                    FolderContentsGridTile(
                        app = app,
                        onClick = { onAppClick(app) },
                        onRequestShortcuts = onRequestShortcuts,
                        onLaunchShortcut = onLaunchShortcut,
                        onAppInfo = onAppInfo,
                        onRemoveFromFolder = { onRemoveFromFolder(app) },
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun FolderContentsGridTile(
    app: AppInfo,
    onClick: () -> Unit,
    onRequestShortcuts: suspend (AppInfo) -> List<AppShortcut>,
    onLaunchShortcut: (AppShortcut) -> Unit,
    onAppInfo: (AppInfo) -> Unit = {},
    onRemoveFromFolder: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var menuExpanded by remember { mutableStateOf(false) }
    Box(modifier = modifier) {
        Column(
            modifier = Modifier
                .longPressReleaseClickable(onClick = onClick, onLongPress = { menuExpanded = true })
                .testTag("folder_contents_grid_tile_${app.packageName}"),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            AppIcon(icon = app.icon, size = AppIconSize.TILE, contentDescription = null)
            Text(
                text = app.label,
                style = MaterialTheme.typography.labelSmall,
                color = Ink,
                textAlign = TextAlign.Center,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.padding(top = 7.dp).widthIn(max = 56.dp),
            )
        }
        AppContextMenu(
            app = app,
            expanded = menuExpanded,
            onDismissRequest = { menuExpanded = false },
            onRequestShortcuts = onRequestShortcuts,
            onLaunchShortcut = onLaunchShortcut,
            onAppInfo = onAppInfo,
            removeFromFolderId = 0L,
            onRemoveFromFolder = { _, _ -> onRemoveFromFolder() },
        )
    }
}
