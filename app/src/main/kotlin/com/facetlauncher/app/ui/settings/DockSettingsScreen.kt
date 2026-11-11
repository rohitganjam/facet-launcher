package com.facetlauncher.app.ui.settings

import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
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
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.facetlauncher.app.R
import com.facetlauncher.app.data.model.GlassStyle
import com.facetlauncher.app.data.model.AppInfo
import com.facetlauncher.app.data.model.DockDisplayMode
import com.facetlauncher.app.data.model.HomeWallpaper
import com.facetlauncher.app.data.model.PlacedItem
import com.facetlauncher.app.ui.components.AppIcon
import com.facetlauncher.app.ui.components.AppIconSize
import com.facetlauncher.app.ui.components.BackButton
import com.facetlauncher.app.ui.components.CardDivider
import com.facetlauncher.app.ui.components.HomeSurfacePreview
import com.facetlauncher.app.ui.components.CopyFromFacetCard
import com.facetlauncher.app.ui.components.DefaultsNote
import com.facetlauncher.app.ui.components.ResetToDefaultCard
import com.facetlauncher.app.ui.components.LabeledDropdownRow
import com.facetlauncher.app.ui.components.ReorderRowDefaults
import com.facetlauncher.app.ui.components.LocalSettingsRowInset
import com.facetlauncher.app.ui.components.SettingsCard
import com.facetlauncher.app.ui.components.StickyHeaderLayout
import com.facetlauncher.app.ui.components.rememberDragReorderState
import com.facetlauncher.app.ui.home.FolderTileGlyph
import com.facetlauncher.app.ui.theme.Ink
import com.facetlauncher.app.ui.theme.FacetLauncherTheme
import com.facetlauncher.app.ui.theme.Muted
import com.facetlauncher.app.ui.theme.SurfaceContainer
import com.facetlauncher.app.ui.theme.resolve
import com.facetlauncher.app.data.model.AppListLimits
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList

/** A facet's own Dock screen: its apps, display style, and a Reset to default action. */
@Composable
fun DockSettingsScreen(
    onBack: () -> Unit,
    onAddDockApp: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: DockSettingsViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    DockSettingsContent(
        uiState = uiState,
        onBack = onBack,
        onAddDockApp = onAddDockApp,
        onDockDisplayModeChange = viewModel::setDockDisplayMode,
        onDockGlassChange = viewModel::setDockGlass,
        onReorderDockItems = viewModel::reorderDockItems,
        onResetToDefault = viewModel::resetToDefault,
        onCopyFromFacet = viewModel::copyFromFacet,
        modifier = modifier,
    )
}

@Composable
private fun DockSettingsContent(
    uiState: DockSettingsUiState,
    onBack: () -> Unit,
    onAddDockApp: () -> Unit,
    onDockDisplayModeChange: (DockDisplayMode) -> Unit,
    onDockGlassChange: (GlassStyle) -> Unit,
    onReorderDockItems: (List<PlacedItem>) -> Unit,
    onResetToDefault: () -> Unit,
    modifier: Modifier = Modifier,
    onCopyFromFacet: (Long) -> Unit = {},
) {
    StickyHeaderLayout(
        modifier = modifier,
        header = { DockSettingsHeader(onBack = onBack) },
        content = { headerHeight ->
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .background(SurfaceContainer)
                    .testTag("dock_settings_screen")
                    .windowInsetsPadding(WindowInsets.systemBars)
                    .padding(horizontal = 24.dp),
                contentPadding = PaddingValues(top = headerHeight, bottom = 24.dp),
            ) {
                item {
                    HomeSurfacePreview(
                        appList = persistentListOf(),
                        dockApps = uiState.dockItems,
                        dockDisplayMode = uiState.dockDisplayMode,
                        dockGlass = uiState.dockGlass,
                        labelColor = uiState.appLabelColorOption.resolve(),
                        labelFontWeight = uiState.homeAppsFontWeight.resolve(),
                        homeWallpaper = uiState.homeWallpaper,
                        modifier = Modifier.padding(bottom = 20.dp).testTag("dock_settings_preview_card"),
                    )
                }
                item { DockStyleCard(uiState, onDockDisplayModeChange, onDockGlassChange) }
                dockAppsItem(uiState, onAddDockApp, onReorderDockItems)
                dockResetItems(onResetToDefault)
                item { Spacer(modifier = Modifier.height(16.dp)) }
                item {
                    CopyFromFacetCard(
                        facets = uiState.otherFacets,
                        subtitle = stringResource(R.string.copy_from_facet_dock_subtitle),
                        confirmMessageRes = R.string.copy_from_facet_dock_message,
                        onCopy = onCopyFromFacet,
                        testTagPrefix = "dock_settings",
                    )
                }
            }
        },
    )
}

/** The dock's display style and the background behind it. */
@Composable
private fun DockStyleCard(uiState: DockSettingsUiState, onDockDisplayModeChange: (DockDisplayMode) -> Unit, onDockGlassChange: (GlassStyle) -> Unit) {
    SettingsCard(modifier = Modifier.padding(bottom = 16.dp)) {
        LabeledDropdownRow(
            title = stringResource(R.string.dock_display_style),
            options = DockDisplayMode.entries,
            selected = uiState.dockDisplayMode,
            label = { stringResource(it.displayNameRes) },
            onSelect = onDockDisplayModeChange,
            testTag = "dock_display_style_row",
        )
        CardDivider()
        LabeledDropdownRow(
            title = stringResource(R.string.dock_background),
            options = GlassStyle.entries,
            selected = uiState.dockGlass,
            label = { stringResource(it.displayNameRes) },
            onSelect = onDockGlassChange,
            testTag = "dock_glass_row",
        )
    }
}

private fun LazyListScope.dockResetItems(onResetToDefault: () -> Unit) {
    item { Spacer(modifier = Modifier.height(16.dp)) }
    item {
        ResetToDefaultCard(
            subtitle = stringResource(R.string.reset_dock_subtitle),
            confirmMessage = stringResource(R.string.reset_dock_confirm_message),
            onReset = onResetToDefault,
            testTagPrefix = "dock_settings",
        )
    }
}

/** The "Select apps" row and, once there are dock apps, the reorderable row under it. */
private fun LazyListScope.dockAppsItem(
    uiState: DockSettingsUiState,
    onAddDockApp: () -> Unit,
    onReorderDockItems: (List<PlacedItem>) -> Unit,
) {
    item {
        SettingsCard(fullBleedRows = true) {
            DockClickableRow(
                title = stringResource(R.string.dock_select_apps),
                subtitle = stringResource(R.string.format_count_of_max, uiState.dockItems.size, AppListLimits.MAX_DOCK_APPS),
                onClick = onAddDockApp,
                testTag = "add_dock_app_row",
            )
            if (uiState.dockItems.isNotEmpty()) {
                CardDivider()
                DockAppsRow(
                    dockItems = uiState.dockItems,
                    onReorder = onReorderDockItems,
                )
            }
        }
    }
}

@Composable
private fun DockSettingsHeader(onBack: () -> Unit, modifier: Modifier = Modifier) {
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
            text = stringResource(R.string.facet_settings_dock_settings_title),
            style = MaterialTheme.typography.headlineSmall,
            color = Ink,
        )
    }
}

@Composable
private fun DockClickableRow(title: String, subtitle: String?, onClick: () -> Unit, testTag: String, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier
            .testTag(testTag)
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = LocalSettingsRowInset.current, vertical = 13.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column {
            Text(text = title, style = MaterialTheme.typography.bodyLarge, color = Ink)
            if (subtitle != null) {
                Text(text = subtitle, style = MaterialTheme.typography.bodyMedium, color = Muted)
            }
        }
    }
}

private val DOCK_TILE_SHAPE = RoundedCornerShape(14.dp)

/** A [PlacedItem]'s stable identity for keying/reordering — a folder's own id, or its app's component. */
private fun PlacedItem.reorderKey(): Any = when (this) {
    is PlacedItem.SingleApp -> app.listKey
    is PlacedItem.FolderItem -> "folder_${folder.id}"
}

@Composable
private fun DockAppsRow(
    dockItems: ImmutableList<PlacedItem>,
    onReorder: (List<PlacedItem>) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier.padding(horizontal = LocalSettingsRowInset.current, vertical = 8.dp)) {
        // Keyed on component identity only, not the raw `dockItems` list — an app's AppInfo.icon
        // is a freshly-decoded bitmap on every LauncherApps re-emission, so keying `remember` on
        // the raw list would reset drag state mid-drag on re-emissions unrelated to the dock.
        val componentsKey = dockItems.map { it.reorderKey() }
        var order by remember(componentsKey) { mutableStateOf<List<PlacedItem>>(dockItems) }
        val slotWidthPx = with(LocalDensity.current) { (AppIconSize.ICON + ReorderRowDefaults.DOCK_TILE_SPACING).toPx() }
        val reorderState = rememberDragReorderState(
            items = order,
            key = { it.reorderKey() },
            axis = Orientation.Horizontal,
            slotSizePx = slotWidthPx,
            onOrderChange = { order = it },
            onDragCommit = { onReorder(it) },
        )

        LazyRow(
            modifier = Modifier.height(AppIconSize.ICON),
            horizontalArrangement = Arrangement.spacedBy(ReorderRowDefaults.DOCK_TILE_SPACING),
            verticalAlignment = Alignment.CenterVertically,
            userScrollEnabled = false,
        ) {
            items(order, key = { it.reorderKey() }) { item ->
                val isDragging = reorderState.isDragging(item)
                Box(
                    modifier = Modifier
                        .testTag(
                            when (item) {
                                is PlacedItem.SingleApp -> "dock_app_${item.app.packageName}"
                                is PlacedItem.FolderItem -> "dock_folder_${item.folder.id}"
                            },
                        )
                        .zIndex(if (isDragging) 1f else 0f)
                        .graphicsLayer {
                            translationX = if (isDragging) reorderState.dragOffset else 0f
                            scaleX = if (isDragging) ReorderRowDefaults.DRAG_SCALE else 1f
                            scaleY = if (isDragging) ReorderRowDefaults.DRAG_SCALE else 1f
                            shadowElevation = if (isDragging) ReorderRowDefaults.DRAG_ELEVATION.toPx() else 0f
                            shape = DOCK_TILE_SHAPE
                            clip = false
                        }
                        .then(if (isDragging) Modifier else Modifier.animateItem())
                        .then(reorderState.dragModifier(item)),
                ) {
                    when (item) {
                        is PlacedItem.SingleApp -> AppIcon(app = item.app, size = AppIconSize.ICON, contentDescription = item.app.label)
                        is PlacedItem.FolderItem -> FolderTileGlyph(folder = item.folder)
                    }
                }
            }
        }
        Text(
            text = stringResource(R.string.drag_to_reorder_content_description),
            style = MaterialTheme.typography.bodyMedium,
            color = Muted,
            modifier = Modifier.padding(top = 8.dp),
        )
    }
}

@Preview(showBackground = true, widthDp = 390, heightDp = 844)
@Preview(name = "Dark", showBackground = true, widthDp = 390, heightDp = 844, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun DockSettingsScreenPreview() {
    FacetLauncherTheme {
        DockSettingsContent(
            uiState = DockSettingsUiState(
                dockItems = (1..4).map {
                    PlacedItem.SingleApp(AppInfo(packageName = "com.example.$it", activityName = ".Main", label = "App $it", icon = null))
                }.toImmutableList(),
            ),
            onBack = {},
            onAddDockApp = {},
            onDockDisplayModeChange = {},
            onDockGlassChange = {},
            onReorderDockItems = {},
            onResetToDefault = {},
        )
    }
}
