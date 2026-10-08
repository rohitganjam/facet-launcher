package com.facetlauncher.app.ui.components

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
import androidx.compose.runtime.DisposableEffect
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
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import com.facetlauncher.app.R
import com.facetlauncher.app.data.model.AppInfo
import com.facetlauncher.app.data.model.AppSortOption
import com.facetlauncher.app.data.model.Folder
import com.facetlauncher.app.data.model.SortDirection
import com.facetlauncher.app.ui.home.FolderTileGlyph
import com.facetlauncher.app.ui.theme.Accent
import com.facetlauncher.app.ui.theme.FacetLauncherTheme
import com.facetlauncher.app.ui.theme.Ink
import com.facetlauncher.app.ui.theme.Muted
import com.facetlauncher.app.ui.theme.Surface
import com.facetlauncher.app.ui.theme.SurfaceContainer

/** Shared by the panel's slide and the tab indicator's own slide so both move in lockstep. */
private const val TAB_TRANSITION_DURATION_MS = 220

private enum class AppPickerTab { APPS, FOLDERS }

/**
 * Shared full-screen "pick apps" UI behind the Favorites, Dock, and Folder pickers (`4j`/`4k` and
 * a folder's own "Add to folder" screen) — own search, a sort control for the not-yet-selected
 * section, checkbox rows, and an optional Apps/Folders tab (folders can't yet contain folders, so
 * [FolderAppPickerScreen] passes `showFoldersTab = false`). In-list apps show first, in whatever
 * order the destination stood in when the screen was opened — that split/order is the caller's own
 * frozen [selectedResults], never touched here. [otherResults] arrives pre-sorted by the caller's
 * ViewModel (this composable never computes domain data itself, per this repo's layering rule) —
 * [sortOption]/[sortDirection] here are read-only display state for [AppSortControl].
 */
@Composable
fun AppPickerScreen(
    title: String,
    query: String,
    selectedResults: List<AppInfo>,
    otherResults: List<AppInfo>,
    selectedComponents: Set<Pair<String, String>>,
    canAddMore: Boolean,
    canRemove: Boolean,
    sortOption: AppSortOption,
    sortDirection: SortDirection,
    selectedSectionLabel: String,
    screenTestTag: String,
    tagPrefix: String,
    onQueryChange: (String) -> Unit,
    onSortOptionChange: (AppSortOption) -> Unit,
    onSortDirectionToggle: () -> Unit,
    onToggleApp: (AppInfo) -> Unit,
    onDone: () -> Unit,
    modifier: Modifier = Modifier,
    folders: List<Folder> = emptyList(),
    placedFolderIds: Set<Long> = emptySet(),
    showFoldersTab: Boolean = false,
    onToggleFolder: (Folder) -> Unit = {},
    onResume: () -> Unit = {},
) {
    // Local, not persisted — purely which section of this one visit is on screen.
    var selectedTab by remember { mutableStateOf(AppPickerTab.APPS) }
    val lifecycleOwner = LocalLifecycleOwner.current

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) onResume()
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    StickyHeaderLayout(
        modifier = modifier
            .fillMaxSize()
            .background(Surface)
            .testTag(screenTestTag),
        header = { AppPickerHeader(title = title, onDone = onDone, tagPrefix = tagPrefix) },
        content = { headerHeight ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .windowInsetsPadding(WindowInsets.systemBars)
                    .padding(horizontal = 24.dp),
            ) {
                Spacer(modifier = Modifier.height(headerHeight))
                if (showFoldersTab) {
                    AppPickerTabRow(
                        selected = selectedTab,
                        onSelect = { selectedTab = it },
                        tagPrefix = tagPrefix,
                        modifier = Modifier.padding(bottom = 12.dp),
                    )
                    AnimatedContent(
                        targetState = selectedTab,
                        transitionSpec = {
                            // Slide only, no fade — a fade stacked on top of the slide meant two
                            // full lists sat semi-transparent over each other mid-transition,
                            // which read as visual noise/jank rather than a clean motion. Pure
                            // slide (plus a snappier duration) reads much smoother.
                            if (targetState == AppPickerTab.FOLDERS) {
                                slideInHorizontally(animationSpec = tween(TAB_TRANSITION_DURATION_MS)) { it }
                                    .togetherWith(slideOutHorizontally(animationSpec = tween(TAB_TRANSITION_DURATION_MS)) { -it })
                            } else {
                                slideInHorizontally(animationSpec = tween(TAB_TRANSITION_DURATION_MS)) { -it }
                                    .togetherWith(slideOutHorizontally(animationSpec = tween(TAB_TRANSITION_DURATION_MS)) { it })
                            }
                        },
                        modifier = Modifier.weight(1f).fillMaxWidth(),
                        label = "app_picker_tab_content",
                    ) { tab ->
                        if (tab == AppPickerTab.APPS) {
                            AppPickerAppsList(
                                query = query,
                                selectedResults = selectedResults,
                                otherResults = otherResults,
                                selectedComponents = selectedComponents,
                                canAddMore = canAddMore,
                                canRemove = canRemove,
                                sortOption = sortOption,
                                sortDirection = sortDirection,
                                selectedSectionLabel = selectedSectionLabel,
                                tagPrefix = tagPrefix,
                                onQueryChange = onQueryChange,
                                onSortOptionChange = onSortOptionChange,
                                onSortDirectionToggle = onSortDirectionToggle,
                                onToggleApp = onToggleApp,
                            )
                        } else {
                            AppPickerFoldersList(
                                folders = folders,
                                placedFolderIds = placedFolderIds,
                                canAddMore = canAddMore,
                                canRemove = canRemove,
                                tagPrefix = tagPrefix,
                                onToggleFolder = onToggleFolder,
                            )
                        }
                    }
                } else {
                    AppPickerAppsList(
                        query = query,
                        selectedResults = selectedResults,
                        otherResults = otherResults,
                        selectedComponents = selectedComponents,
                        canAddMore = canAddMore,
                        canRemove = canRemove,
                        sortOption = sortOption,
                        sortDirection = sortDirection,
                        selectedSectionLabel = selectedSectionLabel,
                        tagPrefix = tagPrefix,
                        onQueryChange = onQueryChange,
                        onSortOptionChange = onSortOptionChange,
                        onSortDirectionToggle = onSortDirectionToggle,
                        onToggleApp = onToggleApp,
                        modifier = Modifier.weight(1f),
                    )
                }
            }
        },
    )
}

@Composable
private fun AppPickerAppsList(
    query: String,
    selectedResults: List<AppInfo>,
    otherResults: List<AppInfo>,
    selectedComponents: Set<Pair<String, String>>,
    canAddMore: Boolean,
    canRemove: Boolean,
    sortOption: AppSortOption,
    sortDirection: SortDirection,
    selectedSectionLabel: String,
    tagPrefix: String,
    onQueryChange: (String) -> Unit,
    onSortOptionChange: (AppSortOption) -> Unit,
    onSortDirectionToggle: () -> Unit,
    onToggleApp: (AppInfo) -> Unit,
    modifier: Modifier = Modifier,
) {
    LazyColumn(modifier = modifier.fillMaxSize()) {
        item {
            OutlinedTextField(
                value = query,
                onValueChange = onQueryChange,
                placeholder = { Text(stringResource(R.string.app_picker_search_apps)) },
                modifier = Modifier.fillMaxWidth().testTag("${tagPrefix}_search").padding(bottom = 12.dp),
                singleLine = true,
            )
        }
        item {
            AppSortControl(
                sortOption = sortOption,
                sortDirection = sortDirection,
                onSortOptionChange = onSortOptionChange,
                onSortDirectionToggle = onSortDirectionToggle,
                tagPrefix = tagPrefix,
                modifier = Modifier.padding(bottom = 12.dp),
            )
        }

        if (selectedResults.isNotEmpty()) {
            item { AppPickerSectionHeader(selectedSectionLabel) }
            items(selectedResults, key = { it.packageName + it.activityName }) { app ->
                val isSelected = (app.packageName to app.activityName) in selectedComponents
                AppPickerRow(
                    app = app,
                    checked = isSelected,
                    enabled = if (isSelected) canRemove else canAddMore,
                    tagPrefix = tagPrefix,
                    onToggle = { onToggleApp(app) },
                )
            }
        }
        if (otherResults.isNotEmpty()) {
            item { AppPickerSectionHeader(stringResource(R.string.app_picker_all_apps)) }
            items(otherResults, key = { it.packageName + it.activityName }) { app ->
                val isSelected = (app.packageName to app.activityName) in selectedComponents
                AppPickerRow(
                    app = app,
                    checked = isSelected,
                    enabled = if (isSelected) canRemove else canAddMore,
                    tagPrefix = tagPrefix,
                    onToggle = { onToggleApp(app) },
                )
            }
        }
    }
}

@Composable
private fun AppPickerFoldersList(
    folders: List<Folder>,
    placedFolderIds: Set<Long>,
    canAddMore: Boolean,
    canRemove: Boolean,
    tagPrefix: String,
    onToggleFolder: (Folder) -> Unit,
    modifier: Modifier = Modifier,
) {
    if (folders.isEmpty()) {
        Text(
            text = stringResource(R.string.app_picker_no_folders_yet),
            style = MaterialTheme.typography.bodyMedium,
            color = Muted,
            modifier = modifier.fillMaxSize().padding(top = 8.dp),
        )
    } else {
        LazyColumn(modifier = modifier.fillMaxSize()) {
            items(folders, key = { it.id }) { folder ->
                val isPlaced = folder.id in placedFolderIds
                AppPickerFolderRow(
                    folder = folder,
                    checked = isPlaced,
                    enabled = if (isPlaced) canRemove else canAddMore,
                    tagPrefix = tagPrefix,
                    onToggle = { onToggleFolder(folder) },
                )
            }
        }
    }
}

@Composable
private fun AppPickerTabRow(selected: AppPickerTab, onSelect: (AppPickerTab) -> Unit, tagPrefix: String, modifier: Modifier = Modifier) {
    PillTabPair(
        startLabel = stringResource(R.string.app_picker_tab_apps),
        endLabel = stringResource(R.string.app_picker_tab_folders),
        endSelected = selected == AppPickerTab.FOLDERS,
        onSelect = { onSelect(if (it) AppPickerTab.FOLDERS else AppPickerTab.APPS) },
        startTag = "${tagPrefix}_tab_apps",
        endTag = "${tagPrefix}_tab_folders",
        modifier = modifier,
    )
}

/** The Folders tab's own row — checking places this folder in the current list (capacity-gated, same as an app); unchecking removes the placement only, never deletes the folder itself. */
@Composable
private fun AppPickerFolderRow(folder: Folder, checked: Boolean, enabled: Boolean, tagPrefix: String, onToggle: () -> Unit, modifier: Modifier = Modifier) {
    // Matches this screen's pre-consolidation tag ("favorites_folder_picker_row_x"/"dock_folder_picker_row_x") rather than "${tagPrefix}_folder_row_x".
    val rowTagPrefix = tagPrefix.replace("_picker", "_folder_picker")
    Row(
        modifier = modifier
            .testTag("${rowTagPrefix}_row_${folder.id}")
            .fillMaxWidth()
            .alpha(if (enabled) 1f else 0.4f)
            .clickable(enabled = enabled, onClick = onToggle)
            .padding(vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        FolderTileGlyph(folder = folder)
        Column(modifier = Modifier.weight(1f)) {
            Text(text = folder.name, style = MaterialTheme.typography.bodyLarge, color = Ink)
            Text(
                text = pluralStringResource(R.plurals.folder_app_count, folder.apps.size, folder.apps.size),
                style = MaterialTheme.typography.bodyMedium,
                color = Muted,
            )
        }
        Checkbox(
            checked = checked,
            onCheckedChange = { onToggle() },
            enabled = enabled,
            modifier = Modifier.testTag("${rowTagPrefix}_row_${folder.id}_checkbox"),
        )
    }
}

@Composable
private fun AppPickerHeader(title: String, onDone: () -> Unit, tagPrefix: String, modifier: Modifier = Modifier) {
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
            text = stringResource(R.string.action_done),
            style = MaterialTheme.typography.bodyLarge,
            color = Ink,
            modifier = Modifier.testTag("${tagPrefix}_done").clickable(onClick = onDone),
        )
    }
}

@Composable
private fun AppPickerSectionHeader(title: String, modifier: Modifier = Modifier) {
    Text(
        text = title,
        style = MaterialTheme.typography.labelSmall,
        color = Muted,
        modifier = modifier.padding(top = 12.dp, bottom = 6.dp),
    )
}

@Composable
private fun AppPickerRow(app: AppInfo, checked: Boolean, enabled: Boolean, tagPrefix: String, onToggle: () -> Unit, modifier: Modifier = Modifier) {
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
            modifier = Modifier.testTag("${tagPrefix}_row_${app.packageName}"),
        )
    }
}

@Preview(showBackground = true, widthDp = 390, heightDp = 844)
@Preview(name = "Dark", showBackground = true, widthDp = 390, heightDp = 844, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun AppPickerScreenPreview() {
    FacetLauncherTheme {
        AppPickerScreen(
            title = "Favorites",
            query = "",
            selectedResults = (1..3).map { AppInfo("com.example.$it", ".Main", "App $it", null) },
            otherResults = (4..8).map { AppInfo("com.example.$it", ".Main", "App $it", null) },
            selectedComponents = emptySet(),
            canAddMore = true,
            canRemove = true,
            sortOption = AppSortOption.ALPHABETICAL,
            sortDirection = SortDirection.ASCENDING,
            selectedSectionLabel = "FAVORITES",
            screenTestTag = "app_picker_screen_preview",
            tagPrefix = "preview_picker",
            onQueryChange = {},
            onSortOptionChange = {},
            onSortDirectionToggle = {},
            onToggleApp = {},
            onDone = {},
            showFoldersTab = true,
        )
    }
}
