package com.lumenlauncher.app.ui.onboarding

import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.DragHandle
import androidx.compose.material3.Icon
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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import com.lumenlauncher.app.data.model.AppInfo
import com.lumenlauncher.app.data.model.AppListLimits
import com.lumenlauncher.app.data.model.ListContentMode
import com.lumenlauncher.app.ui.components.AppIcon
import com.lumenlauncher.app.ui.components.LabeledDropdownRow
import com.lumenlauncher.app.ui.components.ReorderRowDefaults
import com.lumenlauncher.app.ui.components.rememberDragReorderState
import com.lumenlauncher.app.ui.theme.Accent
import com.lumenlauncher.app.ui.theme.Faint
import com.lumenlauncher.app.ui.theme.Ink
import com.lumenlauncher.app.ui.theme.LumenLauncherTheme
import com.lumenlauncher.app.ui.theme.Muted
import com.lumenlauncher.app.ui.theme.Surface

/**
 * Onboarding step 2 (`4g`, extended) — the launcher-wide default HOME APPS content mode and DOCK.
 * Both sections mirror their Settings counterparts (`DockSettingsScreen`, `HomeAppsListSettingsScreen`):
 * a live row that supports drag-to-reorder in place, plus a clickable row that opens the same
 * full-screen picker Settings uses ([com.lumenlauncher.app.ui.dock.DockAppPickerScreen],
 * [com.lumenlauncher.app.ui.profiles.FavoritesPickerScreen]) for adding/removing apps — reused as
 * is rather than duplicating a second inline search list here.
 */
@Composable
fun OnboardingHomeSetupPage(
    uiState: OnboardingUiState,
    onReorderDockApps: (List<AppInfo>) -> Unit,
    onOpenDockPicker: () -> Unit,
    onListContentModeChanged: (ListContentMode) -> Unit,
    onAppsToShowCountChanged: (Int) -> Unit,
    onOpenFavoritesPicker: () -> Unit,
    onReorderFavorites: (List<AppInfo>) -> Unit,
    onSkip: () -> Unit,
    onNext: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier.fillMaxSize().testTag("onboarding_home_setup_page")) {
        Column(modifier = Modifier.padding(horizontal = 24.dp).padding(top = 56.dp)) {
            Text(text = "Set up your home screen", style = MaterialTheme.typography.headlineSmall, color = Ink)
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Pick the apps you want on Home. You can change all of this later.",
                style = MaterialTheme.typography.bodyMedium,
                color = Muted,
            )
        }

        LazyColumn(modifier = Modifier.weight(1f).fillMaxWidth().padding(horizontal = 24.dp)) {
            item {
                Spacer(modifier = Modifier.height(24.dp))
                HomeAppsSection(
                    listContentMode = uiState.listContentMode,
                    onListContentModeChanged = onListContentModeChanged,
                    appsToShowCount = uiState.appsToShowCount,
                    onAppsToShowCountChanged = onAppsToShowCountChanged,
                    favoriteApps = uiState.favoriteApps,
                    favoriteCountLabel = uiState.favoriteCountLabel,
                    onEditFavoritesClick = onOpenFavoritesPicker,
                    onReorderFavorites = onReorderFavorites,
                )
                Spacer(modifier = Modifier.height(40.dp))
                DockSection(
                    dockApps = uiState.dockApps,
                    countLabel = uiState.dockCountLabel,
                    onReorder = onReorderDockApps,
                    onManageClick = onOpenDockPicker,
                )
            }
            item { Spacer(modifier = Modifier.height(96.dp)) }
        }

        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp).padding(bottom = 34.dp, top = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            OnboardingDots(step = 1, totalSteps = 4)
            Row(horizontalArrangement = Arrangement.spacedBy(20.dp)) {
                Text(
                    text = "Skip",
                    style = MaterialTheme.typography.bodyLarge,
                    color = Muted,
                    modifier = Modifier.clickable(onClick = onSkip).testTag("onboarding_skip"),
                )
                Text(
                    text = "Next",
                    style = MaterialTheme.typography.bodyLarge,
                    color = Accent,
                    modifier = Modifier.clickable(onClick = onNext).testTag("onboarding_next"),
                )
            }
        }
    }
}

@Composable
private fun DockSection(
    dockApps: List<AppInfo>,
    countLabel: String,
    onReorder: (List<AppInfo>) -> Unit,
    onManageClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(text = "DOCK", style = MaterialTheme.typography.labelSmall, color = Faint)
            Text(text = countLabel, style = MaterialTheme.typography.labelSmall, color = Muted)
        }
        Spacer(modifier = Modifier.height(12.dp))
        if (dockApps.isNotEmpty()) {
            DockAppsReorderRow(dockApps = dockApps, onReorder = onReorder)
            Spacer(modifier = Modifier.height(12.dp))
        }
        Text(
            text = "Manage dock apps",
            style = MaterialTheme.typography.bodyMedium,
            color = Accent,
            modifier = Modifier.clickable(onClick = onManageClick).testTag("onboarding_manage_dock"),
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "The default dock — each profile can customize its own later. Drag to reorder.",
            style = MaterialTheme.typography.bodySmall,
            color = Muted,
        )
    }
}

private val FAVORITE_ROW_SHAPE = RoundedCornerShape(12.dp)

/**
 * Drag-to-reorder for the dock preview row — mirrors `DockSettingsScreen`'s `DockAppsRow` exactly
 * (same headless [com.lumenlauncher.app.ui.components.DragReorderState], same tile sizing).
 * `remember` is keyed on stable component identity, not the raw [dockApps] list — see that file's
 * own note on why (a fresh `AppInfo.icon` bitmap on every `LauncherApps` re-emission). Add/remove
 * isn't handled here — that's what `onManageClick`'s full-screen picker is for, so a tap-to-remove
 * doesn't have to compete with the drag gesture on the same tile.
 */
@Composable
private fun DockAppsReorderRow(dockApps: List<AppInfo>, onReorder: (List<AppInfo>) -> Unit, modifier: Modifier = Modifier) {
    val componentsKey = dockApps.map { it.packageName to it.activityName }
    var order by remember(componentsKey) { mutableStateOf(dockApps) }
    val slotWidthPx = with(LocalDensity.current) { (ReorderRowDefaults.DOCK_TILE_SIZE + ReorderRowDefaults.DOCK_TILE_SPACING).toPx() }
    val reorderState = rememberDragReorderState(
        items = order,
        key = { it.packageName to it.activityName },
        axis = Orientation.Horizontal,
        slotSizePx = slotWidthPx,
        onOrderChanged = { order = it },
        onDragCommit = onReorder,
    )

    LazyRow(
        modifier = modifier.height(ReorderRowDefaults.DOCK_TILE_SIZE),
        horizontalArrangement = Arrangement.spacedBy(ReorderRowDefaults.DOCK_TILE_SPACING),
        userScrollEnabled = false,
    ) {
        items(order, key = { it.packageName + it.activityName }) { app ->
            val isDragging = reorderState.isDragging(app)
            AppIcon(
                icon = app.icon,
                size = ReorderRowDefaults.DOCK_TILE_SIZE,
                cornerRadius = 15.dp,
                contentDescription = app.label,
                modifier = Modifier
                    .testTag("onboarding_dock_tile_${app.packageName}")
                    .zIndex(if (isDragging) 1f else 0f)
                    .graphicsLayer {
                        translationX = if (isDragging) reorderState.dragOffset else 0f
                        scaleX = if (isDragging) ReorderRowDefaults.DRAG_SCALE else 1f
                        scaleY = if (isDragging) ReorderRowDefaults.DRAG_SCALE else 1f
                        shadowElevation = if (isDragging) ReorderRowDefaults.DRAG_ELEVATION.toPx() else 0f
                        clip = false
                    }
                    .then(if (isDragging) Modifier else Modifier.animateItem())
                    .then(reorderState.dragModifier(app)),
            )
        }
    }
}

@Composable
private fun HomeAppsSection(
    listContentMode: ListContentMode,
    onListContentModeChanged: (ListContentMode) -> Unit,
    appsToShowCount: Int,
    onAppsToShowCountChanged: (Int) -> Unit,
    favoriteApps: List<AppInfo>,
    favoriteCountLabel: String,
    onEditFavoritesClick: () -> Unit,
    onReorderFavorites: (List<AppInfo>) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Text(text = "HOME APPS", style = MaterialTheme.typography.labelSmall, color = Faint)
        LabeledDropdownRow(
            title = "Show",
            options = ListContentMode.entries,
            selected = listContentMode,
            label = { it.onboardingDisplayLabel() },
            onSelect = onListContentModeChanged,
            testTag = "onboarding_list_content_mode_row",
        )
        if (listContentMode == ListContentMode.FAVORITES) {
            FavoritesClickableRow(countLabel = favoriteCountLabel, onClick = onEditFavoritesClick)
            if (favoriteApps.isNotEmpty()) {
                Spacer(modifier = Modifier.height(4.dp))
                FavoritesReorderList(favorites = favoriteApps, onReorder = onReorderFavorites)
            }
        } else {
            LabeledDropdownRow(
                title = "Apps to show",
                options = AppListLimits.APPS_TO_SHOW_OPTIONS,
                selected = appsToShowCount,
                label = { it.toString() },
                onSelect = onAppsToShowCountChanged,
                testTag = "onboarding_apps_to_show_row",
            )
        }
    }
}

private fun ListContentMode.onboardingDisplayLabel(): String = when (this) {
    ListContentMode.FAVORITES -> "Favorites"
    ListContentMode.RECENTS -> "Recently used"
    ListContentMode.MOST_USED -> "Most used"
}

@Composable
private fun FavoritesClickableRow(countLabel: String, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .testTag("onboarding_edit_favorites")
            .padding(vertical = 13.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(text = "Favorites", style = MaterialTheme.typography.bodyLarge, color = Ink)
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(text = countLabel, style = MaterialTheme.typography.bodyMedium, color = Muted)
            Icon(imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = null, tint = Muted)
        }
    }
}

/** Drag-to-reorder for the favorites list — mirrors `HomeAppsListSettingsScreen`'s `DefaultFavoritesReorderList`. */
@Composable
private fun FavoritesReorderList(favorites: List<AppInfo>, onReorder: (List<AppInfo>) -> Unit, modifier: Modifier = Modifier) {
    val componentsKey = favorites.map { it.packageName to it.activityName }
    var order by remember(componentsKey) { mutableStateOf(favorites) }
    val rowHeightPx = with(LocalDensity.current) { ReorderRowDefaults.FAVORITE_ROW_HEIGHT.toPx() }
    val reorderState = rememberDragReorderState(
        items = order,
        key = { it.packageName to it.activityName },
        axis = Orientation.Vertical,
        slotSizePx = rowHeightPx,
        onOrderChanged = { order = it },
        onDragCommit = onReorder,
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
                    .testTag("onboarding_favorite_reorder_row_${app.packageName}")
                    .zIndex(if (isDragging) 1f else 0f)
                    .graphicsLayer {
                        translationY = if (isDragging) reorderState.dragOffset else 0f
                        scaleX = if (isDragging) ReorderRowDefaults.DRAG_SCALE else 1f
                        scaleY = if (isDragging) ReorderRowDefaults.DRAG_SCALE else 1f
                        shadowElevation = if (isDragging) ReorderRowDefaults.DRAG_ELEVATION.toPx() else 0f
                        shape = FAVORITE_ROW_SHAPE
                        clip = false
                    }
                    .then(if (isDragging) Modifier.background(Surface, FAVORITE_ROW_SHAPE) else Modifier)
                    .then(if (isDragging) Modifier else Modifier.animateItem())
                    .padding(horizontal = if (isDragging) 10.dp else 0.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Icon(
                    imageVector = Icons.Default.DragHandle,
                    contentDescription = "Drag to reorder",
                    tint = Faint,
                    modifier = Modifier
                        .testTag("onboarding_favorite_reorder_handle_${app.packageName}")
                        .then(reorderState.dragModifier(app)),
                )
                AppIcon(icon = app.icon, size = 30.dp, cornerRadius = 10.dp, contentDescription = null)
                Text(text = app.label, style = MaterialTheme.typography.bodyLarge, color = Ink, modifier = Modifier.weight(1f))
            }
        }
    }
}

@Preview(showBackground = true, widthDp = 390, heightDp = 844)
@Preview(name = "Dark", showBackground = true, widthDp = 390, heightDp = 844, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun OnboardingHomeSetupPagePreview() {
    LumenLauncherTheme {
        OnboardingHomeSetupPage(
            uiState = OnboardingUiState(
                dockApps = (1..4).map { AppInfo("com.example.$it", ".Main", "D$it", null) },
                favoriteApps = (1..3).map { AppInfo("com.example.f$it", ".Main", "Favorite $it", null) },
            ),
            onReorderDockApps = {},
            onOpenDockPicker = {},
            onListContentModeChanged = {},
            onAppsToShowCountChanged = {},
            onOpenFavoritesPicker = {},
            onReorderFavorites = {},
            onSkip = {},
            onNext = {},
        )
    }
}
