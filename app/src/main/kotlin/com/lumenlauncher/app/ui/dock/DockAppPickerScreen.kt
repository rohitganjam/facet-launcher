package com.lumenlauncher.app.ui.dock

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
import androidx.compose.foundation.layout.size
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
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.lumenlauncher.app.data.model.AppInfo
import com.lumenlauncher.app.ui.components.AppIcon
import com.lumenlauncher.app.ui.components.BackButton
import com.lumenlauncher.app.ui.components.StickyHeaderLayout
import com.lumenlauncher.app.ui.theme.Muted
import com.lumenlauncher.app.ui.theme.Ink
import com.lumenlauncher.app.ui.theme.LumenLauncherTheme
import com.lumenlauncher.app.ui.theme.Surface

/**
 * Dock app picker (`4k`): full-screen, own search, checkbox rows — not a reuse of AppDrawerScreen's
 * tap-to-launch list. In-dock apps show first, ordered however the dock stood when this screen
 * was opened; that split and order are frozen for the rest of this visit — checking/unchecking
 * apps never reshuffles either section, only a fresh open of this screen does. Removal only ever
 * happens by unchecking a row here; drag-reordering the dock itself lives in Settings, not here.
 */
@Composable
fun DockAppPickerScreen(
    onDone: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: DockAppPickerViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    DockAppPickerContent(
        uiState = uiState,
        onQueryChanged = viewModel::onQueryChanged,
        onToggleApp = viewModel::toggleDockApp,
        onDone = onDone,
        modifier = modifier,
    )
}

@Composable
private fun DockAppPickerContent(
    uiState: DockAppPickerUiState,
    onQueryChanged: (String) -> Unit,
    onToggleApp: (AppInfo) -> Unit,
    onDone: () -> Unit,
    modifier: Modifier = Modifier,
) {
    StickyHeaderLayout(
        modifier = modifier
            .fillMaxSize()
            .background(Surface)
            .testTag("dock_app_picker_screen"),
        header = { DockAppPickerHeader(onDone = onDone) },
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
                    modifier = Modifier.fillMaxWidth().testTag("dock_picker_search").padding(bottom = 12.dp),
                    singleLine = true,
                )
            }

            if (uiState.selectedResults.isNotEmpty()) {
                item { PickerSectionHeader("IN DOCK") }
                items(uiState.selectedResults, key = { it.packageName + it.activityName }) { app ->
                    val isInDock = (app.packageName to app.activityName) in uiState.dockPackageComponents
                    DockPickerRow(
                        app = app,
                        checked = isInDock,
                        enabled = if (isInDock) uiState.canRemove else uiState.canAddMore,
                        onToggle = { onToggleApp(app) },
                    )
                }
            }
            if (uiState.otherResults.isNotEmpty()) {
                item { PickerSectionHeader("ALL APPS") }
                items(uiState.otherResults, key = { it.packageName + it.activityName }) { app ->
                    val isInDock = (app.packageName to app.activityName) in uiState.dockPackageComponents
                    DockPickerRow(
                        app = app,
                        checked = isInDock,
                        enabled = if (isInDock) uiState.canRemove else uiState.canAddMore,
                        onToggle = { onToggleApp(app) },
                    )
                }
            }
        }
        },
    )
}

@Composable
private fun DockAppPickerHeader(onDone: () -> Unit, modifier: Modifier = Modifier) {
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
            Text(text = "Dock", style = MaterialTheme.typography.headlineSmall, color = Ink)
        }
        Text(
            text = "Done",
            style = MaterialTheme.typography.bodyLarge,
            color = Ink,
            modifier = Modifier.testTag("dock_picker_done").clickable(onClick = onDone),
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
private fun DockPickerRow(app: AppInfo, checked: Boolean, enabled: Boolean, onToggle: () -> Unit, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .alpha(if (enabled) 1f else 0.4f)
            .clickable(enabled = enabled, onClick = onToggle)
            .padding(vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        AppIcon(icon = app.icon, size = 32.dp, cornerRadius = 9.dp, contentDescription = null)
        Text(text = app.label, style = MaterialTheme.typography.bodyLarge, color = Ink, modifier = Modifier.weight(1f))
        Checkbox(
            checked = checked,
            onCheckedChange = { onToggle() },
            enabled = enabled,
            modifier = Modifier.testTag("dock_picker_row_${app.packageName}"),
        )
    }
}

@Preview(showBackground = true, widthDp = 390, heightDp = 844)
@Preview(name = "Dark", showBackground = true, widthDp = 390, heightDp = 844, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun DockAppPickerScreenPreview() {
    LumenLauncherTheme {
        DockAppPickerContent(
            uiState = DockAppPickerUiState(
                selectedResults = (1..3).map { AppInfo("com.example.$it", ".Main", "App $it", null) },
                otherResults = (4..8).map { AppInfo("com.example.$it", ".Main", "App $it", null) },
            ),
            onQueryChanged = {},
            onToggleApp = {},
            onDone = {},
        )
    }
}
