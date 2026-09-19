package com.facetlauncher.app.ui.settings

import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.DragHandle
import androidx.compose.material.icons.outlined.DeleteOutline
import androidx.compose.material.icons.outlined.DriveFileRenameOutline
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.facetlauncher.app.R
import com.facetlauncher.app.data.model.AppInfo
import com.facetlauncher.app.data.model.Folder
import com.facetlauncher.app.ui.components.AppIcon
import com.facetlauncher.app.ui.components.AppIconSize
import com.facetlauncher.app.ui.components.BackButton
import com.facetlauncher.app.ui.components.RenameDialog
import com.facetlauncher.app.ui.components.ReorderRowDefaults
import com.facetlauncher.app.ui.components.SettingsCard
import com.facetlauncher.app.ui.components.StickyHeaderLayout
import com.facetlauncher.app.ui.components.TonalButton
import com.facetlauncher.app.ui.components.rememberDragReorderState
import com.facetlauncher.app.ui.theme.Accent
import com.facetlauncher.app.ui.theme.ErrorColor
import com.facetlauncher.app.ui.theme.Faint
import com.facetlauncher.app.ui.theme.Ink
import com.facetlauncher.app.ui.theme.FacetLauncherTheme
import com.facetlauncher.app.ui.theme.Muted
import com.facetlauncher.app.ui.theme.Surface
import com.facetlauncher.app.ui.theme.SurfaceContainer

/**
 * Settings → Folders → one folder — a real, dedicated screen rather than the shared
 * [com.facetlauncher.app.ui.components.FolderContentsSheet] bottom sheet used elsewhere, so its
 * apps can be drag-reordered the same way Dock/Favorites are on their own settings screens (see
 * [FolderAppsReorderList], which mirrors `HomeAppsListSettingsScreen`'s own favorites list, and is
 * wrapped in the same [com.facetlauncher.app.ui.components.SettingsCard] the folder library itself
 * uses in [FoldersSettingsScreen] — omitted entirely for a 0-app folder, matching that screen's own
 * empty state).
 * Membership is *added* via [onAddToFolder] → [FolderAppPickerScreen] — a real [TonalButton]
 * pinned as the first list item, above the reorderable rows (not inside the `StickyHeaderLayout`
 * header, and not one of the draggable rows; the header keeps only the folder name and its rename
 * action). Membership can also be *removed* directly from each row here via [onRemoveApp] — a
 * deliberate exception to this app's usual "removal only happens on the picker" convention
 * (Dock/Favorites), since a folder's own dedicated screen is a more natural place to prune it
 * than round-tripping through the picker for every app.
 */
@Composable
fun FolderDetailScreen(
    onBack: () -> Unit,
    onAddToFolder: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: FolderDetailViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    FolderDetailContent(
        uiState = uiState,
        onBack = onBack,
        onAddToFolder = onAddToFolder,
        onRename = viewModel::renameFolder,
        onReorder = viewModel::reorderApps,
        onRemoveApp = viewModel::removeApp,
        modifier = modifier,
    )
}

@Composable
internal fun FolderDetailContent(
    uiState: FolderDetailUiState,
    onBack: () -> Unit,
    onAddToFolder: () -> Unit,
    onRename: (String) -> Unit,
    onReorder: (List<AppInfo>) -> Unit,
    onRemoveApp: (AppInfo) -> Unit,
    modifier: Modifier = Modifier,
) {
    val folder = uiState.folder
    StickyHeaderLayout(
        modifier = modifier,
        header = {
            FolderDetailHeader(
                name = folder?.name.orEmpty(),
                onBack = onBack,
                onRename = onRename,
            )
        },
        content = { headerHeight ->
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .background(SurfaceContainer)
                    .testTag("folder_detail_screen")
                    .windowInsetsPadding(WindowInsets.systemBars)
                    .padding(horizontal = 24.dp),
                contentPadding = PaddingValues(top = headerHeight, bottom = 24.dp),
            ) {
                if (folder == null) return@LazyColumn
                item {
                    TonalButton(
                        text = stringResource(R.string.folder_add_to_folder),
                        leadingIcon = Icons.Filled.Add,
                        onClick = onAddToFolder,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 8.dp, bottom = 16.dp)
                            .testTag("folder_detail_add_to_folder"),
                    )
                }
                if (folder.apps.isEmpty()) {
                    item {
                        Text(
                            text = stringResource(R.string.folder_detail_no_apps_yet),
                            style = MaterialTheme.typography.bodyMedium,
                            color = Muted,
                        )
                    }
                } else {
                    item {
                        SettingsCard {
                            FolderAppsReorderList(apps = folder.apps, onReorder = onReorder, onRemoveApp = onRemoveApp)
                        }
                    }
                }
            }
        },
    )
}

@Composable
private fun FolderDetailHeader(
    name: String,
    onBack: () -> Unit,
    onRename: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    var showRenameDialog by remember { mutableStateOf(false) }
    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(SurfaceContainer)
            .windowInsetsPadding(WindowInsets.systemBars.only(WindowInsetsSides.Top))
            .padding(horizontal = 24.dp)
            .padding(top = 24.dp, bottom = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        BackButton(onClick = onBack)
        Text(
            text = name,
            style = MaterialTheme.typography.headlineSmall,
            color = Ink,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f),
        )
        Row(
            modifier = Modifier
                .clickable(onClick = { showRenameDialog = true })
                .testTag("folder_detail_rename")
                .padding(horizontal = 8.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Icon(imageVector = Icons.Outlined.DriveFileRenameOutline, contentDescription = null, tint = Accent)
            Text(text = stringResource(R.string.action_rename), style = MaterialTheme.typography.bodyLarge, color = Accent)
        }
    }

    if (showRenameDialog) {
        RenameDialog(
            title = stringResource(R.string.folder_rename_title),
            explanation = stringResource(R.string.folder_rename_explanation),
            initialValue = name,
            onSave = { newName ->
                showRenameDialog = false
                onRename(newName)
            },
            onDismiss = { showRenameDialog = false },
        )
    }
}

private val FOLDER_APP_ROW_SHAPE = RoundedCornerShape(12.dp)

/**
 * Drag-to-reorder for this folder's own apps — mirrors `HomeAppsListSettingsScreen`'s own
 * `DefaultFavoritesReorderList` exactly (same row height/drag visuals), plus a per-row delete
 * action ([onRemoveApp]) this screen adds on top of that shared pattern (see this file's own top
 * doc comment for why folders are the one exception to "removal only happens on the picker").
 */
@Composable
private fun FolderAppsReorderList(
    apps: List<AppInfo>,
    onReorder: (List<AppInfo>) -> Unit,
    onRemoveApp: (AppInfo) -> Unit,
    modifier: Modifier = Modifier,
) {
    val componentsKey = apps.map { it.packageName to it.activityName }
    var order by remember(componentsKey) { mutableStateOf(apps) }
    val rowHeightPx = with(LocalDensity.current) { ReorderRowDefaults.FAVORITE_ROW_HEIGHT.toPx() }
    val reorderState = rememberDragReorderState(
        items = order,
        key = { it.packageName to it.activityName },
        axis = Orientation.Vertical,
        slotSizePx = rowHeightPx,
        onOrderChange = { order = it },
        onDragCommit = { onReorder(it) },
    )

    LazyColumn(
        modifier = modifier.fillMaxWidth().height(ReorderRowDefaults.FAVORITE_ROW_HEIGHT * order.size),
        userScrollEnabled = false,
    ) {
        items(order, key = { it.packageName + it.activityName }) { app ->
            val isDragging = reorderState.isDragging(app)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(ReorderRowDefaults.FAVORITE_ROW_HEIGHT)
                    .testTag("folder_detail_reorder_row_${app.packageName}")
                    .zIndex(if (isDragging) 1f else 0f)
                    .graphicsLayer {
                        translationY = if (isDragging) reorderState.dragOffset else 0f
                        scaleX = if (isDragging) ReorderRowDefaults.DRAG_SCALE else 1f
                        scaleY = if (isDragging) ReorderRowDefaults.DRAG_SCALE else 1f
                        shadowElevation = if (isDragging) ReorderRowDefaults.DRAG_ELEVATION.toPx() else 0f
                        shape = FOLDER_APP_ROW_SHAPE
                        clip = false
                    }
                    .then(if (isDragging) Modifier.background(Surface, FOLDER_APP_ROW_SHAPE) else Modifier)
                    .then(if (isDragging) Modifier else Modifier.animateItem())
                    .padding(horizontal = if (isDragging) 10.dp else 0.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(14.dp),
            ) {
                Icon(
                    Icons.Default.DragHandle,
                    contentDescription = stringResource(R.string.drag_to_reorder_content_description),
                    tint = Faint,
                    modifier = Modifier
                        .testTag("folder_detail_reorder_handle_${app.packageName}")
                        .then(reorderState.dragModifier(app)),
                )
                AppIcon(icon = app.icon, size = AppIconSize.ROW_COMPACT, contentDescription = null)
                Text(text = app.label, style = MaterialTheme.typography.bodyLarge, color = Ink, modifier = Modifier.weight(1f))
                IconButton(
                    onClick = { onRemoveApp(app) },
                    modifier = Modifier.testTag("folder_detail_remove_${app.packageName}"),
                ) {
                    Icon(imageVector = Icons.Outlined.DeleteOutline, contentDescription = stringResource(R.string.folder_remove_from_folder), tint = ErrorColor)
                }
            }
        }
    }
}

@Preview(showBackground = true, widthDp = 390, heightDp = 844)
@Preview(name = "Dark", showBackground = true, widthDp = 390, heightDp = 844, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun FolderDetailScreenPreview() {
    FacetLauncherTheme {
        FolderDetailContent(
            uiState = FolderDetailUiState(
                folder = Folder(
                    id = 1L,
                    name = "Games",
                    apps = (1..3).map { AppInfo(packageName = "com.example.app$it", activityName = ".Main", label = "App $it", icon = null) },
                ),
            ),
            onBack = {},
            onAddToFolder = {},
            onRename = {},
            onReorder = {},
            onRemoveApp = {},
        )
    }
}

@Preview(showBackground = true, widthDp = 390, heightDp = 844)
@Composable
private fun FolderDetailScreenEmptyPreview() {
    FacetLauncherTheme {
        FolderDetailContent(
            uiState = FolderDetailUiState(folder = Folder(id = 1L, name = "Empty", apps = emptyList())),
            onBack = {},
            onAddToFolder = {},
            onRename = {},
            onReorder = {},
            onRemoveApp = {},
        )
    }
}
