package com.facetlauncher.app.ui.settings

import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Checkbox
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.facetlauncher.app.data.model.AppInfo
import com.facetlauncher.app.ui.components.AppIcon
import com.facetlauncher.app.ui.components.AppIconSize
import com.facetlauncher.app.ui.components.BackButton
import com.facetlauncher.app.ui.components.StickyHeaderLayout
import com.facetlauncher.app.ui.theme.Ink
import com.facetlauncher.app.ui.theme.FacetLauncherTheme
import com.facetlauncher.app.ui.theme.Muted
import com.facetlauncher.app.ui.theme.Surface

/**
 * A folder's own "Add to folder" picker, reached from [FolderDetailScreen]'s header — full-screen,
 * own search, checkbox rows, same idiom as [com.facetlauncher.app.ui.dock.DockAppPickerScreen]'s
 * own Apps tab, but apps-only: a folder can't contain another folder, so there's no Folders tab
 * here at all. No membership cap either. Removal only ever happens by unchecking a row here;
 * drag-reordering the folder's own contents lives on [FolderDetailScreen], not here.
 */
@Composable
fun FolderAppPickerScreen(
    onDone: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: FolderAppPickerViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    FolderAppPickerContent(
        uiState = uiState,
        onQueryChanged = viewModel::onQueryChanged,
        onToggleApp = viewModel::toggleApp,
        onDone = onDone,
        modifier = modifier,
    )
}

@Composable
private fun FolderAppPickerContent(
    uiState: FolderAppPickerUiState,
    onQueryChanged: (String) -> Unit,
    onToggleApp: (AppInfo) -> Unit,
    onDone: () -> Unit,
    modifier: Modifier = Modifier,
) {
    StickyHeaderLayout(
        modifier = modifier
            .fillMaxSize()
            .background(Surface)
            .testTag("folder_app_picker_screen"),
        header = {
            FolderAppPickerHeader(title = uiState.folderName, onDone = onDone)
        },
        content = { headerHeight ->
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .windowInsetsPadding(WindowInsets.systemBars)
                    .padding(horizontal = 24.dp),
                contentPadding = PaddingValues(top = headerHeight),
            ) {
                item {
                    OutlinedTextField(
                        value = uiState.query,
                        onValueChange = onQueryChanged,
                        placeholder = { Text("Search apps") },
                        modifier = Modifier.fillMaxWidth().testTag("folder_app_picker_search").padding(bottom = 12.dp),
                        singleLine = true,
                    )
                }

                if (uiState.selectedResults.isNotEmpty()) {
                    item { PickerSectionHeader("IN FOLDER") }
                    items(uiState.selectedResults, key = { it.packageName + it.activityName }) { app ->
                        val isInFolder = (app.packageName to app.activityName) in uiState.folderAppComponents
                        FolderAppPickerRow(app = app, checked = isInFolder, onToggle = { onToggleApp(app) })
                    }
                }
                if (uiState.otherResults.isNotEmpty()) {
                    item { PickerSectionHeader("ALL APPS") }
                    items(uiState.otherResults, key = { it.packageName + it.activityName }) { app ->
                        val isInFolder = (app.packageName to app.activityName) in uiState.folderAppComponents
                        FolderAppPickerRow(app = app, checked = isInFolder, onToggle = { onToggleApp(app) })
                    }
                }
            }
        },
    )
}

@Composable
private fun FolderAppPickerHeader(title: String, onDone: () -> Unit, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(Surface)
            .windowInsetsPadding(WindowInsets.systemBars.only(WindowInsetsSides.Top))
            .padding(horizontal = 24.dp)
            .padding(top = 24.dp, bottom = 16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            BackButton(onClick = onDone)
            Text(text = title, style = MaterialTheme.typography.headlineSmall, color = Ink)
        }
        Text(
            text = "Done",
            style = MaterialTheme.typography.bodyLarge,
            color = Ink,
            modifier = Modifier.testTag("folder_app_picker_done").clickable(onClick = onDone),
        )
    }
}

@Composable
private fun PickerSectionHeader(title: String, modifier: Modifier = Modifier) {
    Text(
        text = title,
        style = MaterialTheme.typography.labelSmall,
        color = Muted,
        modifier = modifier.padding(top = 12.dp, bottom = 6.dp),
    )
}

@Composable
private fun FolderAppPickerRow(app: AppInfo, checked: Boolean, onToggle: () -> Unit, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onToggle)
            .padding(vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        AppIcon(icon = app.icon, size = AppIconSize.ROW_COMPACT, contentDescription = null)
        Text(text = app.label, style = MaterialTheme.typography.bodyLarge, color = Ink, modifier = Modifier.weight(1f))
        Checkbox(
            checked = checked,
            onCheckedChange = { onToggle() },
            modifier = Modifier.testTag("folder_app_picker_row_${app.packageName}"),
        )
    }
}

@Preview(showBackground = true, widthDp = 390, heightDp = 844)
@Preview(name = "Dark", showBackground = true, widthDp = 390, heightDp = 844, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun FolderAppPickerScreenPreview() {
    FacetLauncherTheme {
        FolderAppPickerContent(
            uiState = FolderAppPickerUiState(
                folderName = "Games",
                selectedResults = (1..3).map { AppInfo("com.example.$it", ".Main", "App $it", null) },
                otherResults = (4..8).map { AppInfo("com.example.$it", ".Main", "App $it", null) },
            ),
            onQueryChanged = {},
            onToggleApp = {},
            onDone = {},
        )
    }
}
