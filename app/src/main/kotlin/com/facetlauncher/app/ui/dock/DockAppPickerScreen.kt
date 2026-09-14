package com.facetlauncher.app.ui.dock

import android.content.res.Configuration
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Checkbox
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.facetlauncher.app.data.model.AppInfo
import com.facetlauncher.app.data.model.Folder
import com.facetlauncher.app.ui.components.AppIcon
import com.facetlauncher.app.ui.components.AppIconSize
import com.facetlauncher.app.ui.components.BackButton
import com.facetlauncher.app.ui.components.StickyHeaderLayout
import com.facetlauncher.app.ui.home.FolderTileGlyph
import com.facetlauncher.app.ui.theme.Accent
import com.facetlauncher.app.ui.theme.Muted
import com.facetlauncher.app.ui.theme.Ink
import com.facetlauncher.app.ui.theme.FacetLauncherTheme
import com.facetlauncher.app.ui.theme.Surface
import com.facetlauncher.app.ui.theme.SurfaceContainer

/**
 * Dock app picker (`4k`): full-screen, own search, checkbox rows — not a reuse of AppDrawerScreen's
 * tap-to-launch list. In-dock apps show first, ordered however the dock stood when this screen
 * was opened; that split and order are frozen for the rest of this visit — checking/unchecking
 * apps never reshuffles either section, only a fresh open of this screen does. Removal only ever
 * happens by unchecking a row here; drag-reordering the dock itself lives in Settings, not here.
 * Switching between the Apps/Folders tabs slides+fades the panel (same `AnimatedContent` idiom as
 * [com.facetlauncher.app.ui.components.AppContextMenu]'s own page transition) rather than cutting
 * instantly — each tab keeps its own real `LazyColumn` (see [DockAppsList]/[DockFoldersList]) so
 * switching still virtualizes a long app list instead of composing it eagerly.
 */
@Composable
fun DockAppPickerScreen(
    onDone: () -> Unit,
    modifier: Modifier = Modifier,
    // False during onboarding — no folder can exist yet that early (they're only ever created
    // via the Drawer/Search's own "Add to folder" long-press flow, which isn't reachable from
    // onboarding), so the tab would only ever show an empty, dead-end Folders page.
    showFoldersTab: Boolean = true,
    viewModel: DockAppPickerViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    DockAppPickerContent(
        uiState = uiState,
        onQueryChanged = viewModel::onQueryChanged,
        onToggleApp = viewModel::toggleDockApp,
        onToggleFolder = viewModel::toggleFolder,
        onDone = onDone,
        showFoldersTab = showFoldersTab,
        modifier = modifier,
    )
}

private enum class DockPickerTab { APPS, FOLDERS }

/** Shared by the panel's slide and the tab indicator's own slide so both move in lockstep. */
private const val TAB_TRANSITION_DURATION_MS = 220

@Composable
private fun DockAppPickerContent(
    uiState: DockAppPickerUiState,
    onQueryChanged: (String) -> Unit,
    onToggleApp: (AppInfo) -> Unit,
    onToggleFolder: (Folder) -> Unit,
    onDone: () -> Unit,
    showFoldersTab: Boolean = true,
    modifier: Modifier = Modifier,
) {
    // Local, not persisted — purely which section of this one visit is on screen.
    var selectedTab by remember { mutableStateOf(DockPickerTab.APPS) }

    StickyHeaderLayout(
        modifier = modifier
            .fillMaxSize()
            .background(Surface)
            .testTag("dock_app_picker_screen"),
        header = {
            DockAppPickerHeader(
                title = if (uiState.isFacetScoped) "Dock" else "Default dock",
                onDone = onDone,
            )
        },
        content = { headerHeight ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .windowInsetsPadding(WindowInsets.systemBars)
                    .padding(horizontal = 24.dp),
            ) {
                Spacer(modifier = Modifier.height(headerHeight))
                if (showFoldersTab) {
                    DockPickerTabRow(
                        selected = selectedTab,
                        onSelect = { selectedTab = it },
                        modifier = Modifier.padding(bottom = 12.dp),
                    )
                    AnimatedContent(
                        targetState = selectedTab,
                        transitionSpec = {
                            // Slide only, no fade — a fade stacked on top of the slide meant two
                            // full lists sat semi-transparent over each other mid-transition,
                            // which read as visual noise/jank rather than a clean motion. Pure
                            // slide (plus a snappier duration) reads much smoother.
                            if (targetState == DockPickerTab.FOLDERS) {
                                slideInHorizontally(animationSpec = tween(TAB_TRANSITION_DURATION_MS)) { it }
                                    .togetherWith(slideOutHorizontally(animationSpec = tween(TAB_TRANSITION_DURATION_MS)) { -it })
                            } else {
                                slideInHorizontally(animationSpec = tween(TAB_TRANSITION_DURATION_MS)) { -it }
                                    .togetherWith(slideOutHorizontally(animationSpec = tween(TAB_TRANSITION_DURATION_MS)) { it })
                            }
                        },
                        modifier = Modifier.weight(1f).fillMaxWidth(),
                        label = "dock_picker_tab_content",
                    ) { tab ->
                        if (tab == DockPickerTab.APPS) {
                            DockAppsList(uiState = uiState, onQueryChanged = onQueryChanged, onToggleApp = onToggleApp)
                        } else {
                            DockFoldersList(uiState = uiState, onToggleFolder = onToggleFolder)
                        }
                    }
                } else {
                    DockAppsList(
                        uiState = uiState,
                        onQueryChanged = onQueryChanged,
                        onToggleApp = onToggleApp,
                        modifier = Modifier.weight(1f),
                    )
                }
            }
        },
    )
}

@Composable
private fun DockAppsList(
    uiState: DockAppPickerUiState,
    onQueryChanged: (String) -> Unit,
    onToggleApp: (AppInfo) -> Unit,
    modifier: Modifier = Modifier,
) {
    LazyColumn(modifier = modifier.fillMaxSize()) {
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
}

@Composable
private fun DockFoldersList(uiState: DockAppPickerUiState, onToggleFolder: (Folder) -> Unit, modifier: Modifier = Modifier) {
    if (uiState.folders.isEmpty()) {
        Text(
            text = "No folders yet. Long-press an app and choose \"Add to folder\" to create one.",
            style = MaterialTheme.typography.bodyMedium,
            color = Muted,
            modifier = modifier.fillMaxSize().padding(top = 8.dp),
        )
    } else {
        LazyColumn(modifier = modifier.fillMaxSize()) {
            items(uiState.folders, key = { it.id }) { folder ->
                val isPlaced = folder.id in uiState.placedFolderIds
                DockFolderPickerRow(
                    folder = folder,
                    checked = isPlaced,
                    enabled = if (isPlaced) uiState.canRemove else uiState.canAddMore,
                    onToggle = { onToggleFolder(folder) },
                )
            }
        }
    }
}

/** The Folders tab's own row — checking places this folder in the current Dock (capacity-gated, same as an app); unchecking removes the placement only, never deletes the folder itself. */
@Composable
private fun DockFolderPickerRow(folder: Folder, checked: Boolean, enabled: Boolean, onToggle: () -> Unit, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier
            .testTag("dock_folder_picker_row_${folder.id}")
            .fillMaxWidth()
            .alpha(if (enabled) 1f else 0.4f)
            .clickable(enabled = enabled, onClick = onToggle)
            .padding(vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        // FolderTileGlyph always renders at AppIconSize.TILE internally (its own `.size()` call
        // wins over any passed here) — slightly larger than DockPickerRow's ROW_COMPACT app
        // icons in this same list; a minor size mismatch, not a functional issue.
        FolderTileGlyph(folder = folder)
        Column(modifier = Modifier.weight(1f)) {
            Text(text = folder.name, style = MaterialTheme.typography.bodyLarge, color = Ink)
            Text(text = "${folder.apps.size} apps", style = MaterialTheme.typography.bodyMedium, color = Muted)
        }
        Checkbox(
            checked = checked,
            onCheckedChange = { onToggle() },
            enabled = enabled,
            modifier = Modifier.testTag("dock_folder_picker_row_${folder.id}_checkbox"),
        )
    }
}

@Composable
private fun DockPickerTabRow(selected: DockPickerTab, onSelect: (DockPickerTab) -> Unit, modifier: Modifier = Modifier) {
    val tabs = DockPickerTab.entries
    val selectedIndex = tabs.indexOf(selected)
    val density = LocalDensity.current
    var rowSizePx by remember { mutableStateOf(IntSize.Zero) }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(CircleShape)
            .background(SurfaceContainer)
            .padding(4.dp)
            .onSizeChanged { rowSizePx = it },
    ) {
        if (rowSizePx.width > 0) {
            val tabWidth = with(density) { (rowSizePx.width / tabs.size).toDp() }
            val tabHeight = with(density) { rowSizePx.height.toDp() }
            val indicatorOffset by animateDpAsState(
                targetValue = tabWidth * selectedIndex,
                animationSpec = tween(TAB_TRANSITION_DURATION_MS),
                label = "dock_picker_tab_indicator",
            )
            Box(
                modifier = Modifier
                    .offset(x = indicatorOffset)
                    .size(width = tabWidth, height = tabHeight)
                    .clip(CircleShape)
                    .background(Accent),
            )
        }
        Row {
            tabs.forEach { tab ->
                val isSelected = tab == selected
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clickable(onClick = { onSelect(tab) })
                        .testTag("dock_picker_tab_${tab.name.lowercase()}")
                        .padding(vertical = 8.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = if (tab == DockPickerTab.APPS) "Apps" else "Folders",
                        style = MaterialTheme.typography.bodyMedium,
                        color = if (isSelected) Surface else Ink,
                    )
                }
            }
        }
    }
}

@Composable
private fun DockAppPickerHeader(title: String, onDone: () -> Unit, modifier: Modifier = Modifier) {
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
        AppIcon(icon = app.icon, size = AppIconSize.ROW_COMPACT, contentDescription = null)
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
    FacetLauncherTheme {
        DockAppPickerContent(
            uiState = DockAppPickerUiState(
                selectedResults = (1..3).map { AppInfo("com.example.$it", ".Main", "App $it", null) },
                otherResults = (4..8).map { AppInfo("com.example.$it", ".Main", "App $it", null) },
            ),
            onQueryChanged = {},
            onToggleApp = {},
            onToggleFolder = {},
            onDone = {},
        )
    }
}
