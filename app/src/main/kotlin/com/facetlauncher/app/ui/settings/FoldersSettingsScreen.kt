package com.facetlauncher.app.ui.settings

import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.outlined.DeleteOutline
import androidx.compose.material.icons.outlined.Folder
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.facetlauncher.app.data.model.Folder
import com.facetlauncher.app.ui.components.BackButton
import com.facetlauncher.app.ui.components.CardDivider
import com.facetlauncher.app.ui.components.ConfirmDialog
import com.facetlauncher.app.ui.components.RenameDialog
import com.facetlauncher.app.ui.components.SettingsCard
import com.facetlauncher.app.ui.theme.Accent
import com.facetlauncher.app.ui.theme.ErrorColor
import com.facetlauncher.app.ui.theme.Ink
import com.facetlauncher.app.ui.theme.FacetLauncherTheme
import com.facetlauncher.app.ui.theme.IconTile
import com.facetlauncher.app.ui.theme.Muted
import com.facetlauncher.app.ui.theme.SurfaceContainer

/**
 * Settings' "Folders" screen — the whole folder library in one place, sourced directly from
 * [com.facetlauncher.app.data.FolderRepository] rather than any one Dock/Favorites list, since a
 * folder is a first-class entity independent of where (if anywhere) it's placed. Tapping a row
 * navigates to [FolderDetailScreen] (its own draggable apps list, reached via [onOpenFolder]).
 * Delete sits directly on the row itself as its own icon button — this app's own convention for
 * "few, always-visible actions" over a "..." overflow menu (see `FacetCarouselScreen.kt`) — and
 * is the *only* place a folder is actually deleted, gated by [ConfirmDialog] since it removes the
 * folder everywhere it's placed (every Dock/Favorites list it currently occupies).
 */
@Composable
fun FoldersSettingsScreen(
    onBack: () -> Unit,
    onOpenFolder: (Long) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: FoldersSettingsViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    FoldersSettingsContent(
        uiState = uiState,
        onBack = onBack,
        onOpenFolder = onOpenFolder,
        onCreate = viewModel::createFolder,
        onDelete = viewModel::deleteFolder,
        modifier = modifier,
    )
}

@Composable
internal fun FoldersSettingsContent(
    uiState: FoldersSettingsUiState,
    onBack: () -> Unit,
    onOpenFolder: (Long) -> Unit,
    onCreate: (String) -> Unit,
    onDelete: (Long) -> Unit,
    modifier: Modifier = Modifier,
) {
    var pendingDeleteFolder by remember { mutableStateOf<Folder?>(null) }
    var showCreateDialog by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(SurfaceContainer)
            .testTag("folders_settings_screen")
            .windowInsetsPadding(WindowInsets.systemBars)
            .padding(horizontal = 24.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(top = 24.dp, bottom = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            BackButton(onClick = onBack)
            Text(text = "Folders", style = MaterialTheme.typography.headlineSmall, color = Ink, modifier = Modifier.weight(1f))
            Row(
                modifier = Modifier
                    .clickable(onClick = { showCreateDialog = true })
                    .testTag("folders_settings_create_button")
                    .padding(horizontal = 8.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                Icon(imageVector = Icons.Default.Add, contentDescription = null, tint = Accent)
                Text(text = "Folder", style = MaterialTheme.typography.bodyLarge, color = Accent)
            }
        }

        if (uiState.folders.isEmpty()) {
            Text(
                text = "No folders yet. Long-press an app and choose \"Add to folder\" to create one.",
                style = MaterialTheme.typography.bodyMedium,
                color = Muted,
                modifier = Modifier.padding(top = 8.dp),
            )
        } else {
            SettingsCard {
                uiState.folders.forEachIndexed { index, folder ->
                    if (index > 0) CardDivider()
                    FolderRow(
                        folder = folder,
                        onClick = { onOpenFolder(folder.id) },
                        onDelete = { pendingDeleteFolder = folder },
                    )
                }
            }
        }
    }

    pendingDeleteFolder?.let { folder ->
        ConfirmDialog(
            title = "Delete folder",
            message = "This permanently deletes \"${folder.name}\" and removes it from every Dock and Favorites list it's placed in.",
            confirmLabel = "Delete",
            onConfirm = {
                pendingDeleteFolder = null
                onDelete(folder.id)
            },
            onDismiss = { pendingDeleteFolder = null },
        )
    }

    if (showCreateDialog) {
        // Genuinely empty on creation — no app is required, unlike the Drawer/Dock's own "Add
        // to folder" flow, which always names a folder alongside adding its first member.
        RenameDialog(
            title = "New folder",
            explanation = "Name this folder.",
            initialValue = "",
            onSave = { name ->
                showCreateDialog = false
                onCreate(name)
            },
            onDismiss = { showCreateDialog = false },
        )
    }
}

@Composable
private fun FolderRow(folder: Folder, onClick: () -> Unit, onDelete: () -> Unit, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier
            .testTag("folder_row_${folder.id}")
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 13.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(14.dp)) {
            Box(
                modifier = Modifier.size(32.dp).background(IconTile, CircleShape),
                contentAlignment = Alignment.Center,
            ) {
                Icon(imageVector = Icons.Outlined.Folder, contentDescription = null, tint = Ink, modifier = Modifier.size(18.dp))
            }
            Column {
                Text(text = folder.name, style = MaterialTheme.typography.bodyLarge, color = Ink)
                Text(text = "${folder.apps.size} apps", style = MaterialTheme.typography.bodyMedium, color = Muted)
            }
        }
        IconButton(onClick = onDelete, modifier = Modifier.testTag("folder_row_${folder.id}_delete")) {
            Icon(imageVector = Icons.Outlined.DeleteOutline, contentDescription = "Delete folder", tint = ErrorColor)
        }
    }
}

@Preview(showBackground = true, widthDp = 390, heightDp = 844)
@Preview(name = "Dark", showBackground = true, widthDp = 390, heightDp = 844, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun FoldersSettingsScreenEmptyPreview() {
    FacetLauncherTheme {
        FoldersSettingsContent(
            uiState = FoldersSettingsUiState(),
            onBack = {},
            onOpenFolder = {},
            onCreate = {},
            onDelete = {},
        )
    }
}

@Preview(showBackground = true, widthDp = 390, heightDp = 844)
@Composable
private fun FoldersSettingsScreenPreview() {
    FacetLauncherTheme {
        FoldersSettingsContent(
            uiState = FoldersSettingsUiState(
                folders = listOf(
                    Folder(
                        id = 1L,
                        name = "Games",
                        apps = listOf(com.facetlauncher.app.data.model.AppInfo(packageName = "com.example.a", activityName = ".Main", label = "App A", icon = null)),
                    ),
                ),
            ),
            onBack = {},
            onOpenFolder = {},
            onCreate = {},
            onDelete = {},
        )
    }
}
