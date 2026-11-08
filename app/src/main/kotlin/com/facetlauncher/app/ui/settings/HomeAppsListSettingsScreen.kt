package com.facetlauncher.app.ui.settings

import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.foundation.lazy.LazyItemScope
import androidx.compose.foundation.lazy.LazyListScope
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import androidx.hilt.navigation.compose.hiltViewModel
import com.facetlauncher.app.R
import com.facetlauncher.app.data.model.AppListLimits
import com.facetlauncher.app.data.model.AppListVerticalAlignment
import com.facetlauncher.app.data.model.AppRowPosition
import com.facetlauncher.app.data.model.AppRowPresentation
import com.facetlauncher.app.data.model.ListContentMode
import com.facetlauncher.app.data.model.PlacedItem
import com.facetlauncher.app.ui.components.AppIcon
import com.facetlauncher.app.ui.components.AppIconSize
import com.facetlauncher.app.ui.components.BackButton
import com.facetlauncher.app.ui.components.CardDivider
import com.facetlauncher.app.ui.components.HomeSurfacePreview
import com.facetlauncher.app.ui.components.CopyToFacetsCard
import com.facetlauncher.app.ui.components.DefaultsNote
import com.facetlauncher.app.ui.components.ResetToDefaultCard
import com.facetlauncher.app.ui.components.LabeledDropdownRow
import com.facetlauncher.app.ui.components.ReorderRowDefaults
import com.facetlauncher.app.ui.components.LocalSettingsRowInset
import com.facetlauncher.app.ui.components.SettingsCard
import com.facetlauncher.app.ui.components.StickyHeaderLayout
import com.facetlauncher.app.ui.components.DragReorderState
import com.facetlauncher.app.ui.components.rememberDragReorderState
import com.facetlauncher.app.ui.home.FolderTileGlyph
import com.facetlauncher.app.ui.theme.Faint
import com.facetlauncher.app.ui.theme.Ink
import com.facetlauncher.app.ui.theme.FacetLauncherTheme
import com.facetlauncher.app.ui.theme.Muted
import com.facetlauncher.app.ui.theme.Surface
import com.facetlauncher.app.ui.theme.SurfaceContainer
import com.facetlauncher.app.ui.theme.resolve
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf

/** Settings → Home & Apps → Home Apps List (the defaults a new facet starts with) — also a facet's own Apps-list screen when `viewModel` is facet-scoped, with a Reset to default action. */
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
        onListContentModeChange = viewModel::setListContentMode,
        onAppsToShowCountChange = viewModel::setAppsToShowCount,
        onReorderFavorites = viewModel::reorderFavorites,
        lookActions = AppListLookActions(
            onAllowLandscapeChange = viewModel::setAllowLandscape,
            onLayoutChange = viewModel::setAppListLayout,
            onPresentationChange = viewModel::setAppRowPresentation,
            onGridColumnsChange = viewModel::setAppListGridColumns,
            onGridDisplayModeChange = viewModel::setAppListGridDisplayMode,
            onShrinkToFitChange = viewModel::setAppListShrinkToFit,
            onRowPositionChange = viewModel::setAppRowPosition,
            onColumnAlignmentChange = viewModel::setAppListColumnAlignment,
            onVerticalAlignmentChange = viewModel::setAppListVerticalAlignment,
            onLandscapeRowPositionChange = viewModel::setLandscapeAppRowPosition,
            onLandscapeColumnAlignmentChange = viewModel::setLandscapeAppListColumnAlignment,
            onLandscapeVerticalAlignmentChange = viewModel::setLandscapeAppListVerticalAlignment,
        ),
        onResetToDefault = viewModel::resetToDefault,
        onCopyToFacets = viewModel::copyToFacets,
        modifier = modifier,
    )
}

@Composable
private fun HomeAppsListSettingsContent(
    uiState: HomeAppsListUiState,
    onBack: () -> Unit,
    onEditFavorites: () -> Unit,
    onListContentModeChange: (ListContentMode) -> Unit,
    onAppsToShowCountChange: (Int) -> Unit,
    onReorderFavorites: (List<PlacedItem>) -> Unit,
    lookActions: AppListLookActions,
    onResetToDefault: () -> Unit,
    onCopyToFacets: (Set<Long>) -> Unit,
    modifier: Modifier = Modifier,
) {
    StickyHeaderLayout(
        modifier = modifier,
        header = { HomeAppsListSettingsHeader(isFacetScoped = uiState.isFacetScoped, onBack = onBack) },
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
                        dockApps = persistentListOf(),
                        appRowPosition = uiState.appRowPosition,
                        appRowPresentation = uiState.appRowPresentation,
                        appListLayout = uiState.appListLayout,
                        appListColumnAlignment = uiState.appListColumnAlignment,
                        appListGridColumns = uiState.appListGridColumns,
                        appListGridDisplayMode = uiState.appListGridDisplayMode,
                        labelColor = uiState.appLabelColorOption.resolve(),
                        labelFontWeight = uiState.homeAppsFontWeight.resolve(),
                        homeWallpaper = uiState.homeWallpaper,
                        modifier = Modifier.padding(bottom = 20.dp).testTag("home_apps_list_preview_card"),
                    )
                }
                listContentItem(uiState, onListContentModeChange, onAppsToShowCountChange, onEditFavorites, onReorderFavorites)
                item { Spacer(modifier = Modifier.height(16.dp)) }
                appListLookItems(uiState, lookActions)
                appsResetOrCopyItems(uiState, onResetToDefault, onCopyToFacets)
            }
        },
    )
}

/** Facet scope offers "Reset to default"; launcher scope offers "Copy to facets". */
private fun LazyListScope.appsResetOrCopyItems(
    uiState: HomeAppsListUiState,
    onResetToDefault: () -> Unit,
    onCopyToFacets: (Set<Long>) -> Unit,
) {
    if (uiState.isFacetScoped) {
        item { Spacer(modifier = Modifier.height(16.dp)) }
        item {
            ResetToDefaultCard(
                subtitle = stringResource(R.string.reset_apps_subtitle),
                confirmMessage = stringResource(R.string.reset_apps_confirm_message),
                onReset = onResetToDefault,
                testTagPrefix = "home_apps_list",
            )
        }
    } else {
        item { Spacer(modifier = Modifier.height(16.dp)) }
        item {
            CopyToFacetsCard(
                facets = uiState.facets,
                activeFacetId = uiState.settings.activeFacetId,
                subtitle = stringResource(R.string.copy_apps_subtitle),
                sheetMessage = stringResource(R.string.copy_apps_sheet_message),
                onCopy = onCopyToFacets,
                testTagPrefix = "home_apps_list",
            )
        }
        item { DefaultsNote(modifier = Modifier.padding(top = 12.dp)) }
    }
}

/** What the list shows, how many apps, and (for favorites) which apps and their order. */
private fun LazyListScope.listContentItem(
    uiState: HomeAppsListUiState,
    onListContentModeChange: (ListContentMode) -> Unit,
    onAppsToShowCountChange: (Int) -> Unit,
    onEditFavorites: () -> Unit,
    onReorderFavorites: (List<PlacedItem>) -> Unit,
) {
    item {
        SettingsCard(fullBleedRows = true) {
            LabeledDropdownRow(
                title = stringResource(if (uiState.isFacetScoped) R.string.home_apps_list_content_facet else R.string.home_apps_list_content_default),
                options = ListContentMode.entries,
                selected = uiState.listContentMode,
                label = { stringResource(it.displayNameRes) },
                onSelect = onListContentModeChange,
                testTag = "default_list_content_row",
            )
            if (uiState.listContentMode != ListContentMode.FAVORITES) {
                CardDivider()
                LabeledDropdownRow(
                    title = stringResource(R.string.home_apps_list_apps_to_show),
                    options = AppListLimits.APPS_TO_SHOW_OPTIONS,
                    selected = uiState.appsToShowCount,
                    label = { it.toString() },
                    onSelect = onAppsToShowCountChange,
                    testTag = "default_apps_to_show_row",
                )
            }
            CardDivider()
            HomeAppsListClickableRow(
                title = stringResource(if (uiState.isFacetScoped) R.string.home_apps_list_favorites_facet else R.string.home_apps_list_favorites_default),
                subtitle = uiState.favoritesLabel,
                onClick = onEditFavorites,
                testTag = "default_favorites_row",
            )
            if (uiState.favorites.isNotEmpty()) {
                CardDivider()
                DefaultFavoritesReorderList(
                    favorites = uiState.favorites,
                    onReorder = onReorderFavorites,
                )
            }
        }
    }
}

@Composable
private fun HomeAppsListSettingsHeader(isFacetScoped: Boolean, onBack: () -> Unit, modifier: Modifier = Modifier) {
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
            text = stringResource(if (isFacetScoped) R.string.facet_settings_apps_list_settings_title else R.string.home_apps_list_default_header_title),
            style = MaterialTheme.typography.headlineSmall,
            color = Ink,
        )
    }
}

@Composable
private fun HomeAppsListClickableRow(title: String, subtitle: String?, onClick: () -> Unit, testTag: String, modifier: Modifier = Modifier) {
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
        Icon(imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = null, tint = Muted)
    }
}

private val DEFAULT_FAVORITE_ROW_SHAPE = RoundedCornerShape(12.dp)

/** A [PlacedItem]'s stable identity for keying/reordering — a folder's own id, or its app's component. */
private fun PlacedItem.reorderKey(): Any = when (this) {
    is PlacedItem.SingleApp -> app.listKey
    is PlacedItem.FolderItem -> "folder_${folder.id}"
}

/**
 * Drag-to-reorder for this screen's favorites list — apps and folders interleaved, same as
 * Home's own favorites list. `remember` is keyed on stable component identity
 * ([PlacedItem.reorderKey]), not the raw [favorites] list — an app's `AppInfo.icon` is a
 * freshly-decoded bitmap on every `LauncherApps` re-emission, so it's structurally "new" far more
 * often than the membership/order actually changes, and keying on it would reset drag state
 * mid-drag. Adding/removing a favorite happens on the picker screen reached via the "Favorites"
 * row above this list, never here.
 */
@Composable
private fun DefaultFavoritesReorderList(favorites: ImmutableList<PlacedItem>, onReorder: (List<PlacedItem>) -> Unit, modifier: Modifier = Modifier) {
    val componentsKey = favorites.map { it.reorderKey() }
    var order by remember(componentsKey) { mutableStateOf<List<PlacedItem>>(favorites) }
    val rowHeightPx = with(LocalDensity.current) { ReorderRowDefaults.FAVORITE_ROW_HEIGHT.toPx() }
    val reorderState = rememberDragReorderState(
        items = order,
        key = { it.reorderKey() },
        axis = Orientation.Vertical,
        slotSizePx = rowHeightPx,
        onOrderChange = { order = it },
        onDragCommit = { onReorder(it) },
    )

    LazyColumn(
        modifier = modifier.fillMaxWidth().padding(horizontal = LocalSettingsRowInset.current).height(ReorderRowDefaults.FAVORITE_ROW_HEIGHT * order.size),
        userScrollEnabled = false,
    ) {
        items(order, key = { it.reorderKey() }) { item -> FavoriteReorderRow(item, reorderState) }
    }
}

@Composable
private fun LazyItemScope.FavoriteReorderRow(item: PlacedItem, reorderState: DragReorderState<PlacedItem>) {
    val isDragging = reorderState.isDragging(item)
    val testTagSuffix = when (item) {
        is PlacedItem.SingleApp -> item.app.packageName
        is PlacedItem.FolderItem -> "folder_${item.folder.id}"
    }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(ReorderRowDefaults.FAVORITE_ROW_HEIGHT)
            .testTag("default_favorite_reorder_row_$testTagSuffix")
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
            contentDescription = stringResource(R.string.drag_to_reorder_content_description),
            tint = Faint,
            modifier = Modifier
                .testTag("default_favorite_reorder_handle_$testTagSuffix")
                .then(reorderState.dragModifier(item)),
        )
        when (item) {
            is PlacedItem.SingleApp -> {
                AppIcon(app = item.app, size = AppIconSize.COMPACT, contentDescription = null)
                Text(text = item.app.label, style = MaterialTheme.typography.bodyLarge, color = Ink, modifier = Modifier.weight(1f))
            }
            is PlacedItem.FolderItem -> {
                FolderTileGlyph(folder = item.folder)
                Text(text = item.folder.name, style = MaterialTheme.typography.bodyLarge, color = Ink, modifier = Modifier.weight(1f))
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
            onListContentModeChange = {},
            onAppsToShowCountChange = {},
            onReorderFavorites = {},
            lookActions = AppListLookActions.None,
            onResetToDefault = {},
            onCopyToFacets = {},
        )
    }
}
