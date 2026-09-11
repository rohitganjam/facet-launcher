package com.lumenlauncher.app.ui.settings

import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import androidx.hilt.navigation.compose.hiltViewModel
import com.lumenlauncher.app.data.DockAppRepository
import com.lumenlauncher.app.data.model.AppInfo
import com.lumenlauncher.app.data.model.DockDisplayMode
import com.lumenlauncher.app.data.model.HomeWallpaper
import com.lumenlauncher.app.ui.components.AppIcon
import com.lumenlauncher.app.ui.components.BackButton
import com.lumenlauncher.app.ui.components.CardDivider
import com.lumenlauncher.app.ui.components.HomeSurfacePreview
import com.lumenlauncher.app.ui.components.LabeledDropdownRow
import com.lumenlauncher.app.ui.components.ReorderRowDefaults
import com.lumenlauncher.app.ui.components.SettingsCard
import com.lumenlauncher.app.ui.components.StickyHeaderLayout
import com.lumenlauncher.app.ui.components.rememberDragReorderState
import com.lumenlauncher.app.ui.theme.Ink
import com.lumenlauncher.app.ui.theme.LumenLauncherTheme
import com.lumenlauncher.app.ui.theme.Muted
import com.lumenlauncher.app.ui.theme.SurfaceContainer
import com.lumenlauncher.app.ui.theme.resolve

/** Settings → Home & Apps → Dock. */
@Composable
fun DockSettingsScreen(
    onBack: () -> Unit,
    onAddDockApp: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: DockSettingsViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()
    DockSettingsContent(
        uiState = uiState,
        onBack = onBack,
        onAddDockApp = onAddDockApp,
        onDockDisplayModeChanged = viewModel::setDockDisplayMode,
        onReorderDockApps = viewModel::reorderDockApps,
        modifier = modifier,
    )
}

@Composable
private fun DockSettingsContent(
    uiState: DockSettingsUiState,
    onBack: () -> Unit,
    onAddDockApp: () -> Unit,
    onDockDisplayModeChanged: (DockDisplayMode) -> Unit,
    onReorderDockApps: (List<AppInfo>) -> Unit,
    modifier: Modifier = Modifier,
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
                        appList = emptyList(),
                        dockApps = uiState.dockApps,
                        dockDisplayMode = uiState.dockDisplayMode,
                        labelColor = uiState.appLabelColorOption.resolve(),
                        labelFontWeight = uiState.homeAppsFontWeight.resolve(),
                        homeWallpaper = uiState.homeWallpaper,
                        modifier = Modifier.padding(bottom = 20.dp).testTag("dock_settings_preview_card"),
                    )
                }
                item {
                    SettingsCard {
                        LabeledDropdownRow(
                            title = "Display style",
                            options = DockDisplayMode.entries,
                            selected = uiState.dockDisplayMode,
                            label = { it.dockDisplayLabel() },
                            onSelect = onDockDisplayModeChanged,
                            testTag = "dock_display_style_row",
                        )
                        CardDivider()
                        DockClickableRow(
                            title = "Select Dock Apps",
                            subtitle = "${uiState.dockApps.size} of ${DockAppRepository.MAX_APPS}",
                            onClick = onAddDockApp,
                            testTag = "add_dock_app_row",
                        )
                        if (uiState.dockApps.isNotEmpty()) {
                            CardDivider()
                            DockAppsRow(dockApps = uiState.dockApps, onReorder = onReorderDockApps)
                        }
                    }
                }
            }
        },
    )
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
        Text(text = "Dock", style = MaterialTheme.typography.headlineSmall, color = Ink)
    }
}

private fun DockDisplayMode.dockDisplayLabel(): String = when (this) {
    DockDisplayMode.ICONS -> "Icons"
    DockDisplayMode.TEXT -> "Text"
}

@Composable
private fun DockClickableRow(title: String, subtitle: String?, onClick: () -> Unit, testTag: String, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier
            .testTag(testTag)
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 13.dp),
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

@Composable
private fun DockAppsRow(
    dockApps: List<AppInfo>,
    onReorder: (List<AppInfo>) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier.padding(vertical = 8.dp)) {
        // Keyed on component identity only, not the raw `dockApps` list — that list's AppInfo.icon
        // is a freshly-decoded bitmap on every LauncherApps re-emission, so keying `remember` on
        // the raw list would reset drag state mid-drag on re-emissions unrelated to the dock.
        val componentsKey = dockApps.map { it.packageName to it.activityName }
        var order by remember(componentsKey) { mutableStateOf(dockApps) }
        val slotWidthPx = with(LocalDensity.current) { (ReorderRowDefaults.DOCK_TILE_SIZE + ReorderRowDefaults.DOCK_TILE_SPACING).toPx() }
        val reorderState = rememberDragReorderState(
            items = order,
            key = { it.packageName to it.activityName },
            axis = Orientation.Horizontal,
            slotSizePx = slotWidthPx,
            onOrderChanged = { order = it },
            onDragCommit = { onReorder(it) },
        )

        LazyRow(
            modifier = Modifier.height(ReorderRowDefaults.DOCK_TILE_SIZE),
            horizontalArrangement = Arrangement.spacedBy(ReorderRowDefaults.DOCK_TILE_SPACING),
            verticalAlignment = Alignment.CenterVertically,
            userScrollEnabled = false,
        ) {
            items(order, key = { it.packageName + it.activityName }) { app ->
                val isDragging = reorderState.isDragging(app)
                Box(
                    modifier = Modifier
                        .testTag("dock_app_${app.packageName}")
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
                        .then(reorderState.dragModifier(app)),
                ) {
                    AppIcon(icon = app.icon, size = ReorderRowDefaults.DOCK_TILE_SIZE, cornerRadius = 14.dp, contentDescription = app.label)
                }
            }
        }
        Text(
            text = "Drag to reorder",
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
    LumenLauncherTheme {
        DockSettingsContent(
            uiState = DockSettingsUiState(
                dockApps = (1..4).map {
                    AppInfo(packageName = "com.example.$it", activityName = ".Main", label = "App $it", icon = null)
                },
            ),
            onBack = {},
            onAddDockApp = {},
            onDockDisplayModeChanged = {},
            onReorderDockApps = {},
        )
    }
}
