package com.facetlauncher.app.ui.components

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Dock
import androidx.compose.material.icons.outlined.DriveFileRenameOutline
import androidx.compose.material.icons.outlined.StarBorder
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.facetlauncher.app.data.model.Folder
import com.facetlauncher.app.domain.QuickAddState
import com.facetlauncher.app.domain.QuickPlacementAction
import com.facetlauncher.app.ui.home.FolderTileGlyph
import com.facetlauncher.app.ui.launcher.LocalHomePressedEvent
import com.facetlauncher.app.ui.theme.Ink
import com.facetlauncher.app.ui.theme.Muted
import com.facetlauncher.app.ui.theme.Scrim
import com.facetlauncher.app.ui.theme.Surface

/**
 * Long-pressing a folder *tile* itself (on Home, in the Dock, or in a Favorites list) — distinct
 * from [AppContextMenu], since a folder has no app-info/uninstall/shortcuts of its own. Header
 * mirrors [AppContextMenu]'s own (icon + name) using [FolderTileGlyph] in place of [AppIcon]. Rename,
 * plus the same membership-aware "Add to"/"Remove from Favorites"/"Dock" rows as [AppContextMenu]
 * (from [com.facetlauncher.app.domain.ObserveQuickAddStateUseCase] via [onRequestQuickAddState]) —
 * a folder placed in the Dock/Favorites occupies a slot exactly like a standalone app, so it gets
 * the same quick-placement affordance.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FolderTileContextMenu(
    folder: Folder,
    expanded: Boolean,
    onDismissRequest: () -> Unit,
    onRename: (Long, String) -> Unit,
    modifier: Modifier = Modifier,
    onRequestQuickAddState: suspend (Folder) -> QuickAddState = { QuickAddState() },
    onFavoritesAction: (Folder, QuickPlacementAction) -> Unit = { _, _ -> },
    onDockAction: (Folder, QuickPlacementAction) -> Unit = { _, _ -> },
) {
    var showRenameDialog by remember { mutableStateOf(false) }
    var quickAddState by remember(folder.id) { mutableStateOf(QuickAddState()) }

    LaunchedEffect(expanded, folder.id) {
        if (expanded) quickAddState = onRequestQuickAddState(folder)
    }

    LaunchedEffect(expanded) {
        if (!expanded) showRenameDialog = false
    }

    BackHandler(enabled = expanded, onBack = onDismissRequest)
    val homePressedEvent = LocalHomePressedEvent.current
    LaunchedEffect(expanded, homePressedEvent) {
        if (expanded) homePressedEvent?.collect { onDismissRequest() }
    }

    if (!expanded) return

    ModalBottomSheet(
        onDismissRequest = onDismissRequest,
        modifier = modifier.testTag("folder_tile_context_menu"),
        sheetState = rememberModalBottomSheetState(),
        shape = SHEET_SHAPE,
        containerColor = Surface,
        scrimColor = Scrim,
        dragHandle = { AppContextMenuDragHandle() },
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(start = 24.dp, end = 24.dp, top = 16.dp, bottom = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            FolderTileGlyph(folder = folder)
            Text(
                text = folder.name,
                style = MaterialTheme.typography.titleLarge,
                color = Ink,
                modifier = Modifier.padding(start = 16.dp),
            )
        }
        AppContextMenuItem(
            label = "Rename",
            modifier = Modifier.testTag("folder_tile_context_menu_rename"),
            leadingIcon = { Icon(imageVector = Icons.Outlined.DriveFileRenameOutline, contentDescription = null, tint = Muted) },
            onClick = { showRenameDialog = true },
        )
        quickAddState.favoritesAction?.let { action ->
            AppContextMenuItem(
                label = quickPlacementLabel(action, "Favorites"),
                modifier = Modifier.testTag("folder_tile_context_menu_add_to_favorites"),
                leadingIcon = { Icon(imageVector = Icons.Outlined.StarBorder, contentDescription = null, tint = Muted) },
                trailingContent = { QuickPlacementBadge(action = action) },
                onClick = {
                    onDismissRequest()
                    onFavoritesAction(folder, action)
                },
            )
        }
        quickAddState.dockAction?.let { action ->
            AppContextMenuItem(
                label = quickPlacementLabel(action, "Dock"),
                modifier = Modifier.testTag("folder_tile_context_menu_add_to_dock"),
                leadingIcon = { Icon(imageVector = Icons.Outlined.Dock, contentDescription = null, tint = Muted) },
                trailingContent = { QuickPlacementBadge(action = action) },
                onClick = {
                    onDismissRequest()
                    onDockAction(folder, action)
                },
            )
        }
    }

    if (showRenameDialog) {
        RenameDialog(
            title = "Rename folder",
            explanation = "Choose a new name for this folder.",
            initialValue = folder.name,
            onSave = { name ->
                showRenameDialog = false
                onDismissRequest()
                onRename(folder.id, name)
            },
            onDismiss = { showRenameDialog = false },
        )
    }
}
