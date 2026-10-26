package com.facetlauncher.app.ui.components

import android.content.res.Configuration
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Dock
import androidx.compose.material.icons.outlined.DriveFileRenameOutline
import androidx.compose.material.icons.outlined.StarBorder
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.facetlauncher.app.R
import com.facetlauncher.app.data.model.Folder
import com.facetlauncher.app.domain.QuickAddState
import com.facetlauncher.app.domain.QuickPlacementAction
import com.facetlauncher.app.ui.home.FolderTileGlyph
import com.facetlauncher.app.ui.theme.FacetLauncherTheme
import com.facetlauncher.app.ui.theme.Ink
import com.facetlauncher.app.ui.theme.Muted
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList

/**
 * Long-pressing a folder *tile* itself (on Home, in the Dock, or in a Favorites list) — distinct
 * from [AppContextMenu], since a folder has no app-info/uninstall of its own. Same layout: a header
 * (the folder's glyph, name and app count, with Rename as an icon button) over the Favorites and
 * Dock actions from [com.facetlauncher.app.domain.ObserveQuickAddStateUseCase] via
 * [onRequestQuickAddState] — a folder placed in the Dock/Favorites occupies a slot exactly like a
 * standalone app, so it gets the same quick-placement tiles. A folder has no notifications.
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

    if (!expanded) return

    val favoritesName = stringResource(R.string.quick_placement_favorites)
    val dockName = stringResource(R.string.quick_placement_dock)
    val placementActions = buildList {
        quickAddState.favoritesAction?.let { action ->
            add(
                PlacementActionUi(
                    tag = "folder_tile_context_menu_add_to_favorites",
                    icon = Icons.Outlined.StarBorder,
                    label = quickPlacementLabel(action, favoritesName),
                    scope = PlacementScope(action.facetName),
                    isRemove = action is QuickPlacementAction.Remove,
                    onClick = {
                        onDismissRequest()
                        onFavoritesAction(folder, action)
                    },
                ),
            )
        }
        quickAddState.dockAction?.let { action ->
            add(
                PlacementActionUi(
                    tag = "folder_tile_context_menu_add_to_dock",
                    icon = Icons.Outlined.Dock,
                    label = quickPlacementLabel(action, dockName),
                    scope = PlacementScope(action.facetName),
                    isRemove = action is QuickPlacementAction.Remove,
                    onClick = {
                        onDismissRequest()
                        onDockAction(folder, action)
                    },
                ),
            )
        }
    }.toImmutableList()

    ThemedModalBottomSheet(
        onDismissRequest = onDismissRequest,
        modifier = modifier.testTag("folder_tile_context_menu"),
        skipPartiallyExpanded = true,
    ) {
        FolderMenuContent(folder = folder, placementActions = placementActions, onRename = { showRenameDialog = true })
    }

    if (showRenameDialog) {
        RenameDialog(
            title = stringResource(R.string.folder_rename_title),
            explanation = stringResource(R.string.folder_rename_explanation),
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

@Composable
internal fun FolderMenuContent(
    folder: Folder,
    placementActions: ImmutableList<PlacementActionUi>,
    onRename: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier.fillMaxWidth().verticalScroll(rememberScrollState())) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(start = MENU_SIDE_PADDING, end = MENU_SIDE_PADDING - 12.dp, top = 8.dp, bottom = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            FolderTileGlyph(folder = folder)
            Column(modifier = Modifier.weight(1f).padding(horizontal = 16.dp)) {
                Text(
                    text = folder.name,
                    style = MaterialTheme.typography.titleLarge,
                    color = Ink,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    text = pluralStringResource(R.plurals.folder_app_count, folder.apps.size, folder.apps.size),
                    style = MaterialTheme.typography.bodyMedium,
                    color = Muted,
                )
            }
            MenuIconButton(
                icon = Icons.Outlined.DriveFileRenameOutline,
                contentDescription = stringResource(R.string.action_rename),
                tint = Muted,
                onClick = onRename,
                modifier = Modifier.testTag("folder_tile_context_menu_rename"),
            )
        }
        if (placementActions.isNotEmpty()) {
            CardDivider()
            PlacementActions(
                actions = placementActions,
                modifier = Modifier.padding(horizontal = MENU_SIDE_PADDING, vertical = 16.dp),
            )
        }
    }
}

@Preview(showBackground = true, widthDp = 390)
@Preview(name = "Dark", showBackground = true, widthDp = 390, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun FolderMenuContentPreview() {
    FacetLauncherTheme {
        FolderMenuContent(
            folder = Folder(id = 1L, name = "Games", apps = emptyList()),
            placementActions = persistentListOf(
                PlacementActionUi("p_fav", Icons.Outlined.StarBorder, "Remove from Favorites", PlacementScope(null), isRemove = true, onClick = {}),
                PlacementActionUi("p_dock", Icons.Outlined.Dock, "Remove from Dock", PlacementScope(null), isRemove = true, onClick = {}),
            ),
            onRename = {},
        )
    }
}
