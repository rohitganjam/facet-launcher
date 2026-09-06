package com.lumenlauncher.app.ui.settings

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
import com.lumenlauncher.app.data.DefaultFavoriteAppRepository
import com.lumenlauncher.app.data.model.AppInfo
import com.lumenlauncher.app.data.model.AppRowPosition
import com.lumenlauncher.app.data.model.AppRowPresentation
import com.lumenlauncher.app.data.model.ListContentMode
import com.lumenlauncher.app.ui.components.AppIcon
import com.lumenlauncher.app.ui.components.BackButton
import com.lumenlauncher.app.ui.components.CardDivider
import com.lumenlauncher.app.ui.components.LabeledDropdownRow
import com.lumenlauncher.app.ui.components.SettingsCard
import com.lumenlauncher.app.ui.components.StickyHeaderLayout
import com.lumenlauncher.app.ui.components.rememberDragReorderState
import com.lumenlauncher.app.ui.theme.Faint
import com.lumenlauncher.app.ui.theme.Ink
import com.lumenlauncher.app.ui.theme.LumenLauncherTheme
import com.lumenlauncher.app.ui.theme.Muted
import com.lumenlauncher.app.ui.theme.Surface
import com.lumenlauncher.app.ui.theme.SurfaceContainer

/** Settings → Home & Apps → Home Apps List. */
@Composable
fun HomeAppsListSettingsScreen(
    onBack: () -> Unit,
    onEditDefaultFavorites: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: HomeAppsListSettingsViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()
    HomeAppsListSettingsContent(
        uiState = uiState,
        onBack = onBack,
        onEditDefaultFavorites = onEditDefaultFavorites,
        onAppRowPositionChanged = viewModel::setAppRowPosition,
        onAppRowPresentationChanged = viewModel::setAppRowPresentation,
        onListContentModeChanged = viewModel::setListContentMode,
        onAppsToShowCountChanged = viewModel::setAppsToShowCount,
        onReorderDefaultFavorites = viewModel::reorderDefaultFavorites,
        modifier = modifier,
    )
}

@Composable
private fun HomeAppsListSettingsContent(
    uiState: HomeAppsListUiState,
    onBack: () -> Unit,
    onEditDefaultFavorites: () -> Unit,
    onAppRowPositionChanged: (AppRowPosition) -> Unit,
    onAppRowPresentationChanged: (AppRowPresentation) -> Unit,
    onListContentModeChanged: (ListContentMode) -> Unit,
    onAppsToShowCountChanged: (Int) -> Unit,
    onReorderDefaultFavorites: (List<AppInfo>) -> Unit,
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
                    SettingsCard {
                        LabeledDropdownRow(
                            title = "Position",
                            options = AppRowPosition.entries,
                            selected = uiState.settings.appRowPosition,
                            label = { it.homeAppsListDisplayLabel() },
                            onSelect = onAppRowPositionChanged,
                            testTag = "default_app_row_position_row",
                        )
                        CardDivider()
                        LabeledDropdownRow(
                            title = "Presentation",
                            options = AppRowPresentation.entries,
                            selected = uiState.settings.appRowPresentation,
                            label = { it.homeAppsListDisplayLabel() },
                            onSelect = onAppRowPresentationChanged,
                            testTag = "default_app_row_presentation_row",
                        )
                        CardDivider()
                        LabeledDropdownRow(
                            title = "Default App list content",
                            options = ListContentMode.entries,
                            selected = uiState.settings.listContentMode,
                            label = { it.homeAppsListDisplayLabel() },
                            onSelect = onListContentModeChanged,
                            testTag = "default_list_content_row",
                        )
                        if (uiState.settings.listContentMode != ListContentMode.FAVORITES) {
                            CardDivider()
                            LabeledDropdownRow(
                                title = "Apps to show",
                                options = HOME_APPS_LIST_DEFAULT_APPS_TO_SHOW_OPTIONS,
                                selected = uiState.settings.appsToShowCount,
                                label = { it.toString() },
                                onSelect = onAppsToShowCountChanged,
                                testTag = "default_apps_to_show_row",
                            )
                        }
                        CardDivider()
                        HomeAppsListClickableRow(
                            title = "Default favorites",
                            subtitle = "${uiState.defaultFavorites.size} of ${DefaultFavoriteAppRepository.MAX_FAVORITES}",
                            onClick = onEditDefaultFavorites,
                            testTag = "default_favorites_row",
                        )
                        if (uiState.defaultFavorites.isNotEmpty()) {
                            CardDivider()
                            DefaultFavoritesReorderList(favorites = uiState.defaultFavorites, onReorder = onReorderDefaultFavorites)
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

/** README `3d`'s 4…8 range. */
private val HOME_APPS_LIST_DEFAULT_APPS_TO_SHOW_OPTIONS = (4..8).toList()

private const val DEFAULT_FAVORITE_ROW_HEIGHT_DP = 52
private val DEFAULT_FAVORITE_ROW_SHAPE = RoundedCornerShape(12.dp)
private val REORDER_DRAG_ELEVATION = 6.dp
private const val REORDER_DRAG_SCALE = 1.04f

/**
 * Drag-to-reorder for this screen's own "Default favorites" list — the same shape as
 * [com.lumenlauncher.app.ui.profiles.ProfileSettingsScreen]'s `FavoritesReorderList` (see that
 * file's own doc comment for why `remember` is keyed on stable component identity rather than the
 * raw [favorites] list). Adding/removing a favorite happens on the picker screen reached via the
 * "Default favorites" row above this list, never here.
 */
@Composable
private fun DefaultFavoritesReorderList(favorites: List<AppInfo>, onReorder: (List<AppInfo>) -> Unit, modifier: Modifier = Modifier) {
    val componentsKey = favorites.map { it.packageName to it.activityName }
    var order by remember(componentsKey) { mutableStateOf(favorites) }
    val rowHeightPx = with(LocalDensity.current) { DEFAULT_FAVORITE_ROW_HEIGHT_DP.dp.toPx() }
    val reorderState = rememberDragReorderState(
        items = order,
        key = { it.packageName to it.activityName },
        axis = Orientation.Vertical,
        slotSizePx = rowHeightPx,
        onOrderChanged = { order = it },
        onDragCommit = { onReorder(it) },
    )

    LazyColumn(
        modifier = modifier.fillMaxWidth().height((DEFAULT_FAVORITE_ROW_HEIGHT_DP * order.size).dp),
        userScrollEnabled = false,
    ) {
        items(order, key = { it.packageName + it.activityName }) { app ->
            val isDragging = reorderState.isDragging(app)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(DEFAULT_FAVORITE_ROW_HEIGHT_DP.dp)
                    .testTag("default_favorite_reorder_row_${app.packageName}")
                    .zIndex(if (isDragging) 1f else 0f)
                    .graphicsLayer {
                        translationY = if (isDragging) reorderState.dragOffset else 0f
                        scaleX = if (isDragging) REORDER_DRAG_SCALE else 1f
                        scaleY = if (isDragging) REORDER_DRAG_SCALE else 1f
                        shadowElevation = if (isDragging) REORDER_DRAG_ELEVATION.toPx() else 0f
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
                AppIcon(icon = app.icon, size = 32.dp, cornerRadius = 9.dp, contentDescription = null)
                Text(text = app.label, style = MaterialTheme.typography.bodyLarge, color = Ink, modifier = Modifier.weight(1f))
            }
        }
    }
}

@Preview(showBackground = true, widthDp = 390, heightDp = 844)
@Preview(name = "Dark", showBackground = true, widthDp = 390, heightDp = 844, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun HomeAppsListSettingsScreenPreview() {
    LumenLauncherTheme {
        HomeAppsListSettingsContent(
            uiState = HomeAppsListUiState(),
            onBack = {},
            onEditDefaultFavorites = {},
            onAppRowPositionChanged = {},
            onAppRowPresentationChanged = {},
            onListContentModeChanged = {},
            onAppsToShowCountChanged = {},
            onReorderDefaultFavorites = {},
        )
    }
}
