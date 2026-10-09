package com.facetlauncher.app.ui.onboarding

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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import com.facetlauncher.app.R
import com.facetlauncher.app.data.DefaultFavoriteAppRepository
import com.facetlauncher.app.data.DockAppRepository
import com.facetlauncher.app.data.model.AppInfo
import com.facetlauncher.app.data.model.AppListLimits
import com.facetlauncher.app.data.model.DrawerPresentation
import com.facetlauncher.app.data.model.ListContentMode
import com.facetlauncher.app.ui.components.AppIcon
import com.facetlauncher.app.ui.components.AppIconSize
import com.facetlauncher.app.ui.components.CardDivider
import com.facetlauncher.app.ui.components.ConfirmDialog
import com.facetlauncher.app.ui.components.LabeledDropdownRow
import com.facetlauncher.app.ui.components.LocalSettingsRowInset
import com.facetlauncher.app.ui.components.ReorderRowDefaults
import com.facetlauncher.app.ui.components.SettingsCard
import com.facetlauncher.app.ui.components.TextActionButton
import com.facetlauncher.app.ui.components.rememberDragReorderState
import com.facetlauncher.app.ui.theme.Faint
import com.facetlauncher.app.ui.theme.Ink
import com.facetlauncher.app.ui.theme.FacetLauncherTheme
import com.facetlauncher.app.ui.theme.Muted
import com.facetlauncher.app.ui.theme.Surface

/**
 * Onboarding step 2 (`4g`, extended) — the launcher-wide default HOME APPS content mode and DOCK.
 * Both sections mirror their Settings counterparts (`DockSettingsScreen`, `HomeAppsListSettingsScreen`):
 * a live row that supports drag-to-reorder in place, plus a clickable row that opens the same
 * full-screen picker Settings uses ([com.facetlauncher.app.ui.dock.DockAppPickerScreen],
 * [com.facetlauncher.app.ui.facets.FavoritesPickerScreen]) for adding/removing apps — reused as
 * is rather than duplicating a second inline search list here.
 */
@Composable
fun OnboardingHomeSetupPage(
    uiState: OnboardingUiState,
    onReorderDockApps: (List<AppInfo>) -> Unit,
    onOpenDockPicker: () -> Unit,
    onListContentModeChange: (ListContentMode) -> Unit,
    onAppsToShowCountChange: (Int) -> Unit,
    drawerPresentation: DrawerPresentation,
    onDrawerPresentationChange: (DrawerPresentation) -> Unit,
    onOpenFavoritesPicker: () -> Unit,
    onReorderFavorites: (List<AppInfo>) -> Unit,
    onClearFavorites: () -> Unit,
    onClearDockApps: () -> Unit,
    onBack: () -> Unit,
    onNext: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier.fillMaxSize().testTag("onboarding_home_setup_page")) {
        Column(modifier = Modifier.padding(horizontal = 24.dp).padding(top = 48.dp)) {
            Text(text = stringResource(R.string.onboarding_home_setup_headline), style = MaterialTheme.typography.headlineSmall, color = Ink)
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = stringResource(R.string.onboarding_home_setup_subhead),
                style = MaterialTheme.typography.bodyLarge,
                color = Muted,
            )
        }

        LazyColumn(modifier = Modifier.weight(1f).fillMaxWidth().padding(horizontal = 24.dp)) {
            item {
                Spacer(modifier = Modifier.height(24.dp))
                HomeAppsSection(
                    listContentMode = uiState.listContentMode,
                    onListContentModeChange = onListContentModeChange,
                    appsToShowCount = uiState.appsToShowCount,
                    onAppsToShowCountChange = onAppsToShowCountChange,
                    favoriteApps = uiState.favoriteApps,
                    favoriteCountLabel = stringResource(R.string.format_count_of_max, uiState.favoriteApps.size, DefaultFavoriteAppRepository.MAX_FAVORITES),
                    onEditFavoritesClick = onOpenFavoritesPicker,
                    onReorderFavorites = onReorderFavorites,
                    onClearFavorites = onClearFavorites,
                )
                Spacer(modifier = Modifier.height(40.dp))
                DockSection(
                    dockApps = uiState.dockApps,
                    countLabel = stringResource(R.string.format_count_of_max, uiState.dockApps.size, DockAppRepository.MAX_APPS),
                    onReorder = onReorderDockApps,
                    onManageClick = onOpenDockPicker,
                    onClearDockApps = onClearDockApps,
                )
                Spacer(modifier = Modifier.height(40.dp))
                AppDrawerSection(
                    presentation = drawerPresentation,
                    onPresentationChange = onDrawerPresentationChange,
                )
            }
            item { Spacer(modifier = Modifier.height(96.dp)) }
        }

        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp).padding(bottom = 34.dp, top = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            OnboardingDots(step = 1, totalSteps = ONBOARDING_STEP_COUNT)
            Row(horizontalArrangement = Arrangement.spacedBy(20.dp)) {
                TextActionButton(
                    text = stringResource(R.string.action_back),
                    onClick = onBack,
                    color = Muted,
                    modifier = Modifier.testTag("onboarding_back"),
                )
                TextActionButton(
                    text = stringResource(R.string.action_next),
                    onClick = onNext,
                    modifier = Modifier.testTag("onboarding_next"),
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
    onClearDockApps: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var showClearConfirm by remember { mutableStateOf(false) }
    Column(modifier = modifier.fillMaxWidth()) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(text = stringResource(R.string.onboarding_dock_section_header), style = MaterialTheme.typography.labelSmall, color = Muted)
            if (dockApps.isNotEmpty()) {
                TextActionButton(
                    text = stringResource(R.string.action_clear_all),
                    onClick = { showClearConfirm = true },
                    style = MaterialTheme.typography.labelSmall,
                    modifier = Modifier.testTag("onboarding_clear_dock"),
                )
            }
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = stringResource(R.string.onboarding_dock_section_subhead),
            style = MaterialTheme.typography.bodyMedium,
            color = Muted,
        )
        Spacer(modifier = Modifier.height(8.dp))
        SettingsCard(fullBleedRows = true) {
            DockClickableRow(countLabel = countLabel, onClick = onManageClick)
            if (dockApps.isNotEmpty()) {
                CardDivider()
                DockAppsReorderRow(dockApps = dockApps, onReorder = onReorder, modifier = Modifier.padding(horizontal = LocalSettingsRowInset.current, vertical = 12.dp))
            }
        }

    }
    if (showClearConfirm) {
        ConfirmDialog(
            title = stringResource(R.string.onboarding_clear_dock_title),
            message = stringResource(R.string.onboarding_clear_dock_message),
            confirmLabel = stringResource(R.string.action_clear_all),
            onConfirm = { onClearDockApps(); showClearConfirm = false },
            onDismiss = { showClearConfirm = false },
        )
    }
}

/** Mirrors `HomeAppsSection`'s own [FavoritesClickableRow] shape — title left, count + chevron trailing. */
@Composable
private fun DockClickableRow(countLabel: String, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .testTag("onboarding_manage_dock")
            .padding(horizontal = LocalSettingsRowInset.current, vertical = 13.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(text = stringResource(R.string.onboarding_manage_dock_apps), style = MaterialTheme.typography.bodyLarge, color = Ink)
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(text = countLabel, style = MaterialTheme.typography.bodyMedium, color = Muted)
            Icon(imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = null, tint = Muted)
        }
    }
}

/** Mirrors `AppDrawerSettingsScreen`'s own "Show apps as" row — List vs Grid, default List. */
@Composable
private fun AppDrawerSection(presentation: DrawerPresentation, onPresentationChange: (DrawerPresentation) -> Unit, modifier: Modifier = Modifier) {
    Column(modifier = modifier.fillMaxWidth()) {
        Text(text = stringResource(R.string.onboarding_app_drawer_section_header), style = MaterialTheme.typography.labelSmall, color = Muted)
        Spacer(modifier = Modifier.height(8.dp))
        SettingsCard {
            LabeledDropdownRow(
                title = stringResource(R.string.app_drawer_show_apps_as),
                options = DrawerPresentation.entries,
                selected = presentation,
                label = { stringResource(it.displayNameRes) },
                onSelect = onPresentationChange,
                testTag = "onboarding_drawer_presentation_row",
            )
        }
    }
}

private val FAVORITE_ROW_SHAPE = RoundedCornerShape(12.dp)

/**
 * Drag-to-reorder for the dock preview row — mirrors `DockSettingsScreen`'s `DockAppsRow` exactly
 * (same headless [com.facetlauncher.app.ui.components.DragReorderState], same tile sizing).
 * `remember` is keyed on stable component identity, not the raw [dockApps] list — see that file's
 * own note on why (a fresh `AppInfo.icon` bitmap on every `LauncherApps` re-emission). Add/remove
 * isn't handled here — that's what `onManageClick`'s full-screen picker is for, so a tap-to-remove
 * doesn't have to compete with the drag gesture on the same tile.
 */
@Composable
private fun DockAppsReorderRow(dockApps: List<AppInfo>, onReorder: (List<AppInfo>) -> Unit, modifier: Modifier = Modifier) {
    val componentsKey = dockApps.map { it.packageName to it.activityName }
    var order by remember(componentsKey) { mutableStateOf(dockApps) }
    val slotWidthPx = with(LocalDensity.current) { (AppIconSize.TILE + ReorderRowDefaults.DOCK_TILE_SPACING).toPx() }
    val reorderState = rememberDragReorderState(
        items = order,
        key = { it.packageName to it.activityName },
        axis = Orientation.Horizontal,
        slotSizePx = slotWidthPx,
        onOrderChange = { order = it },
        onDragCommit = onReorder,
    )

    LazyRow(
        modifier = modifier.height(AppIconSize.TILE),
        horizontalArrangement = Arrangement.spacedBy(ReorderRowDefaults.DOCK_TILE_SPACING),
        userScrollEnabled = false,
    ) {
        items(order, key = { it.packageName + it.activityName }) { app ->
            val isDragging = reorderState.isDragging(app)
            AppIcon(
                icon = app.icon,
                size = AppIconSize.TILE,
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
    onListContentModeChange: (ListContentMode) -> Unit,
    appsToShowCount: Int,
    onAppsToShowCountChange: (Int) -> Unit,
    favoriteApps: List<AppInfo>,
    favoriteCountLabel: String,
    onEditFavoritesClick: () -> Unit,
    onReorderFavorites: (List<AppInfo>) -> Unit,
    onClearFavorites: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var showClearConfirm by remember { mutableStateOf(false) }
    Column(modifier = modifier.fillMaxWidth()) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(text = stringResource(R.string.onboarding_home_apps_section_header), style = MaterialTheme.typography.labelSmall, color = Muted)
            if (listContentMode == ListContentMode.FAVORITES && favoriteApps.isNotEmpty()) {
                TextActionButton(
                    text = stringResource(R.string.action_clear_all),
                    onClick = { showClearConfirm = true },
                    style = MaterialTheme.typography.labelSmall,
                    modifier = Modifier.testTag("onboarding_clear_favorites"),
                )
            }
        }
        Spacer(modifier = Modifier.height(8.dp))
        SettingsCard(fullBleedRows = true) {
            LabeledDropdownRow(
                title = stringResource(R.string.onboarding_show_label),
                options = ListContentMode.entries,
                selected = listContentMode,
                label = { it.onboardingDisplayLabel() },
                onSelect = onListContentModeChange,
                testTag = "onboarding_list_content_mode_row",
            )
            if (listContentMode == ListContentMode.FAVORITES) {
                CardDivider()
                FavoritesClickableRow(countLabel = favoriteCountLabel, onClick = onEditFavoritesClick)
                if (favoriteApps.isNotEmpty()) {
                    CardDivider()
                    FavoritesReorderList(
                        favorites = favoriteApps,
                        onReorder = onReorderFavorites,
                        modifier = Modifier.padding(horizontal = LocalSettingsRowInset.current, vertical = 8.dp),
                    )
                }
            } else {
                CardDivider()
                LabeledDropdownRow(
                    title = stringResource(R.string.home_apps_list_apps_to_show),
                    options = AppListLimits.APPS_TO_SHOW_OPTIONS,
                    selected = appsToShowCount,
                    label = { it.toString() },
                    onSelect = onAppsToShowCountChange,
                    testTag = "onboarding_apps_to_show_row",
                )
            }
        }
    }
    if (showClearConfirm) {
        ConfirmDialog(
            title = stringResource(R.string.onboarding_clear_favorites_title),
            message = stringResource(R.string.onboarding_clear_favorites_message),
            confirmLabel = stringResource(R.string.action_clear_all),
            onConfirm = { onClearFavorites(); showClearConfirm = false },
            onDismiss = { showClearConfirm = false },
        )
    }
}

@Composable
private fun ListContentMode.onboardingDisplayLabel(): String = when (this) {
    ListContentMode.FAVORITES -> stringResource(R.string.onboarding_list_content_favorites)
    ListContentMode.RECENTS -> stringResource(R.string.onboarding_list_content_recently_used)
    ListContentMode.MOST_USED -> stringResource(R.string.onboarding_list_content_most_used)
}

@Composable
private fun FavoritesClickableRow(countLabel: String, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .testTag("onboarding_edit_favorites")
            .padding(horizontal = LocalSettingsRowInset.current, vertical = 13.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(text = stringResource(R.string.onboarding_list_content_favorites), style = MaterialTheme.typography.bodyLarge, color = Ink)
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
        onOrderChange = { order = it },
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
                    contentDescription = stringResource(R.string.drag_to_reorder_content_description),
                    tint = Faint,
                    modifier = Modifier
                        .testTag("onboarding_favorite_reorder_handle_${app.packageName}")
                        .then(reorderState.dragModifier(app)),
                )
                AppIcon(icon = app.icon, size = 30.dp, contentDescription = null)
                Text(text = app.label, style = MaterialTheme.typography.bodyLarge, color = Ink, modifier = Modifier.weight(1f))
            }
        }
    }
}

@Preview(showBackground = true, widthDp = 390, heightDp = 844)
@Preview(name = "Dark", showBackground = true, widthDp = 390, heightDp = 844, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun OnboardingHomeSetupPagePreview() {
    FacetLauncherTheme {
        OnboardingHomeSetupPage(
            uiState = OnboardingUiState(
                dockApps = (1..4).map { AppInfo("com.example.$it", ".Main", "D$it", null) },
                favoriteApps = (1..3).map { AppInfo("com.example.f$it", ".Main", "Favorite $it", null) },
            ),
            onReorderDockApps = {},
            onOpenDockPicker = {},
            onListContentModeChange = {},
            onAppsToShowCountChange = {},
            drawerPresentation = DrawerPresentation.LIST,
            onDrawerPresentationChange = {},
            onOpenFavoritesPicker = {},
            onReorderFavorites = {},
            onClearFavorites = {},
            onClearDockApps = {},
            onBack = {},
            onNext = {},
        )
    }
}
