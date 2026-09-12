package com.facetlauncher.app.ui.settings

import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.DragHandle
import androidx.compose.material3.Icon
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
import com.facetlauncher.app.data.model.AppInfo
import com.facetlauncher.app.data.model.AppListLimits
import com.facetlauncher.app.data.model.AppListVerticalAlignment
import com.facetlauncher.app.data.model.AppRowPosition
import com.facetlauncher.app.data.model.AppRowPresentation
import com.facetlauncher.app.data.model.ListContentMode
import com.facetlauncher.app.ui.components.AppIcon
import com.facetlauncher.app.ui.components.AppIconSize
import com.facetlauncher.app.ui.components.BackButton
import com.facetlauncher.app.ui.components.CardDivider
import com.facetlauncher.app.ui.components.HomeSurfacePreview
import com.facetlauncher.app.ui.components.LabeledDropdownRow
import com.facetlauncher.app.ui.components.ReorderRowDefaults
import com.facetlauncher.app.ui.components.SettingsCard
import com.facetlauncher.app.ui.components.StickyHeaderLayout
import com.facetlauncher.app.ui.components.rememberDragReorderState
import com.facetlauncher.app.ui.theme.Faint
import com.facetlauncher.app.ui.theme.Ink
import com.facetlauncher.app.ui.theme.FacetLauncherTheme
import com.facetlauncher.app.ui.theme.Muted
import com.facetlauncher.app.ui.theme.Surface
import com.facetlauncher.app.ui.theme.SurfaceContainer
import com.facetlauncher.app.ui.theme.resolve

/** Settings → Home & Apps → Home Apps List — also a facet's own Apps-list screen when `viewModel` is facet-scoped. */
@Composable
fun HomeAppsListSettingsScreen(
    onBack: () -> Unit,
    onEditFavorites: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: HomeAppsListSettingsViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()
    HomeAppsListSettingsContent(
        uiState = uiState,
        onBack = onBack,
        onEditFavorites = onEditFavorites,
        onAppRowPositionChanged = viewModel::setAppRowPosition,
        onAppRowPresentationChanged = viewModel::setAppRowPresentation,
        onListContentModeChanged = viewModel::setListContentMode,
        onAppsToShowCountChanged = viewModel::setAppsToShowCount,
        onAppListVerticalAlignmentChanged = viewModel::setAppListVerticalAlignment,
        onReorderFavorites = viewModel::reorderFavorites,
        modifier = modifier,
    )
}

@Composable
private fun HomeAppsListSettingsContent(
    uiState: HomeAppsListUiState,
    onBack: () -> Unit,
    onEditFavorites: () -> Unit,
    onAppRowPositionChanged: (AppRowPosition) -> Unit,
    onAppRowPresentationChanged: (AppRowPresentation) -> Unit,
    onListContentModeChanged: (ListContentMode) -> Unit,
    onAppsToShowCountChanged: (Int) -> Unit,
    onAppListVerticalAlignmentChanged: (AppListVerticalAlignment) -> Unit,
    onReorderFavorites: (List<AppInfo>) -> Unit,
    modifier: Modifier = Modifier,
) {
    StickyHeaderLayout(
        modifier = modifier,
        header = { HomeAppsListSettingsHeader(onBack = onBack) },
        content = { headerHeight ->
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .background(SurfaceContainer)
                    .testTag("home_apps_list_settings_screen")
                    .windowInsetsPadding(WindowInsets.systemBars)
                    .padding(horizontal = 24.dp),
                contentPadding = PaddingValues(top = headerHeight, bottom = 24.dp),
            ) {
                item {
                    HomeSurfacePreview(
                        appList = uiState.previewApps,
                        dockApps = emptyList(),
                        appRowPosition = uiState.appRowPosition,
                        appRowPresentation = uiState.appRowPresentation,
                        labelColor = uiState.appLabelColorOption.resolve(),
                        labelFontWeight = uiState.homeAppsFontWeight.resolve(),
                        homeWallpaper = uiState.homeWallpaper,
                        modifier = Modifier.padding(bottom = 20.dp).testTag("home_apps_list_preview_card"),
                    )
                }
                item {
                    SettingsCard {
                        LabeledDropdownRow(
                            title = "Position",
                            options = AppRowPosition.entries,
                            selected = uiState.appRowPosition,
                            label = { it.homeAppsListDisplayLabel() },
                            onSelect = onAppRowPositionChanged,
                            testTag = "default_app_row_position_row",
                        )
                        CardDivider()
                        LabeledDropdownRow(
                            title = "List position",
                            options = AppListVerticalAlignment.entries,
                            selected = uiState.appListVerticalAlignment,
                            label = { it.homeAppsListDisplayLabel() },
                            onSelect = onAppListVerticalAlignmentChanged,
                            testTag = "app_list_vertical_alignment_row",
                        )
                        CardDivider()
                        LabeledDropdownRow(
                            title = "Presentation",
                            options = AppRowPresentation.entries,
                            selected = uiState.appRowPresentation,
                            label = { it.homeAppsListDisplayLabel() },
                            onSelect = onAppRowPresentationChanged,
                            testTag = "default_app_row_presentation_row",
                        )
                        CardDivider()
                        LabeledDropdownRow(
                            title = if (uiState.isFacetScoped) "App list content" else "Default App list content",
                            options = ListContentMode.entries,
                            selected = uiState.listContentMode,
                            label = { it.homeAppsListDisplayLabel() },
                            onSelect = onListContentModeChanged,
                            testTag = "default_list_content_row",
                        )
                        if (uiState.listContentMode != ListContentMode.FAVORITES) {
                            CardDivider()
                            LabeledDropdownRow(
                                title = "Apps to show",
                                options = AppListLimits.APPS_TO_SHOW_OPTIONS,
                                selected = uiState.appsToShowCount,
                                label = { it.toString() },
                                onSelect = onAppsToShowCountChanged,
                                testTag = "default_apps_to_show_row",
                            )
                        }
                        CardDivider()
                        HomeAppsListClickableRow(
                            title = if (uiState.isFacetScoped) "Favorites" else "Default favorites",
                            subtitle = uiState.favoritesLabel,
                            onClick = onEditFavorites,
                            testTag = "default_favorites_row",
                        )
                        if (uiState.favorites.isNotEmpty()) {
                            CardDivider()
                            DefaultFavoritesReorderList(favorites = uiState.favorites, onReorder = onReorderFavorites)
                        }
                    }
                }
            }
        },
    )
}

@Composable
private fun HomeAppsListSettingsHeader(onBack: () -> Unit, modifier: Modifier = Modifier) {
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
        Text(text = "Home Apps List", style = MaterialTheme.typography.headlineSmall, color = Ink)
    }
}

private fun AppRowPosition.homeAppsListDisplayLabel(): String = when (this) {
    AppRowPosition.LEFT -> "Left"
    AppRowPosition.RIGHT -> "Right"
}

private fun AppRowPresentation.homeAppsListDisplayLabel(): String = when (this) {
    AppRowPresentation.ICON_ONLY -> "Icon Only"
    AppRowPresentation.ICON_AND_TEXT -> "Icon & Text"
    AppRowPresentation.TEXT_ONLY -> "Text Only"
}

private fun ListContentMode.homeAppsListDisplayLabel(): String = when (this) {
    ListContentMode.FAVORITES -> "Favorites"
    ListContentMode.RECENTS -> "Recents"
    ListContentMode.MOST_USED -> "Most used"
}

private fun AppListVerticalAlignment.homeAppsListDisplayLabel(): String = when (this) {
    AppListVerticalAlignment.TOP -> "Top"
    AppListVerticalAlignment.BOTTOM -> "Bottom"
}

@Composable
private fun HomeAppsListClickableRow(title: String, subtitle: String?, onClick: () -> Unit, testTag: String, modifier: Modifier = Modifier) {
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
        Icon(imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = null, tint = Muted)
    }
}

private val DEFAULT_FAVORITE_ROW_SHAPE = RoundedCornerShape(12.dp)

/**
 * Drag-to-reorder for this screen's favorites list. `remember` is keyed on stable component
 * identity (packageName+activityName), not the raw [favorites] list — that list's own `AppInfo.icon`
 * is a freshly-decoded bitmap on every `LauncherApps` re-emission, so it's structurally "new" far
 * more often than the membership/order actually changes, and keying on it would reset drag state
 * mid-drag. Adding/removing a favorite happens on the picker screen reached via the "Favorites"
 * row above this list, never here.
 */
@Composable
private fun DefaultFavoritesReorderList(favorites: List<AppInfo>, onReorder: (List<AppInfo>) -> Unit, modifier: Modifier = Modifier) {
    val componentsKey = favorites.map { it.packageName to it.activityName }
    var order by remember(componentsKey) { mutableStateOf(favorites) }
    val rowHeightPx = with(LocalDensity.current) { ReorderRowDefaults.FAVORITE_ROW_HEIGHT.toPx() }
    val reorderState = rememberDragReorderState(
        items = order,
        key = { it.packageName to it.activityName },
        axis = Orientation.Vertical,
        slotSizePx = rowHeightPx,
        onOrderChanged = { order = it },
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
                    .testTag("default_favorite_reorder_row_${app.packageName}")
                    .zIndex(if (isDragging) 1f else 0f)
                    .graphicsLayer {
                        translationY = if (isDragging) reorderState.dragOffset else 0f
                        scaleX = if (isDragging) ReorderRowDefaults.DRAG_SCALE else 1f
                        scaleY = if (isDragging) ReorderRowDefaults.DRAG_SCALE else 1f
                        shadowElevation = if (isDragging) ReorderRowDefaults.DRAG_ELEVATION.toPx() else 0f
                        shape = DEFAULT_FAVORITE_ROW_SHAPE
                        clip = false
                    }
                    .then(if (isDragging) Modifier.background(Surface, DEFAULT_FAVORITE_ROW_SHAPE) else Modifier)
                    .then(if (isDragging) Modifier else Modifier.animateItem())
                    .padding(horizontal = if (isDragging) 10.dp else 0.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(14.dp),
            ) {
                Icon(
                    Icons.Default.DragHandle,
                    contentDescription = "Drag to reorder",
                    tint = Faint,
                    modifier = Modifier
                        .testTag("default_favorite_reorder_handle_${app.packageName}")
                        .then(reorderState.dragModifier(app)),
                )
                AppIcon(icon = app.icon, size = AppIconSize.ROW_COMPACT, contentDescription = null)
                Text(text = app.label, style = MaterialTheme.typography.bodyLarge, color = Ink, modifier = Modifier.weight(1f))
            }
        }
    }
}

@Preview(showBackground = true, widthDp = 390, heightDp = 844)
@Preview(name = "Dark", showBackground = true, widthDp = 390, heightDp = 844, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun HomeAppsListSettingsScreenPreview() {
    FacetLauncherTheme {
        HomeAppsListSettingsContent(
            uiState = HomeAppsListUiState(),
            onBack = {},
            onEditFavorites = {},
            onAppRowPositionChanged = {},
            onAppRowPresentationChanged = {},
            onListContentModeChanged = {},
            onAppsToShowCountChanged = {},
            onAppListVerticalAlignmentChanged = {},
            onReorderFavorites = {},
        )
    }
}
