package com.facetlauncher.app.ui.home

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp
import com.facetlauncher.app.R
import com.facetlauncher.app.data.DockAppRepository
import com.facetlauncher.app.data.model.AppListColumnAlignment
import com.facetlauncher.app.data.model.AppListGridColumns
import com.facetlauncher.app.data.model.AppListGridDisplayMode
import com.facetlauncher.app.data.model.AppListLayout
import com.facetlauncher.app.data.model.AppListVerticalAlignment
import com.facetlauncher.app.data.model.AppRowPosition
import com.facetlauncher.app.data.model.AppRowPresentation
import com.facetlauncher.app.data.model.CalendarDisplayOptions
import com.facetlauncher.app.data.model.CalendarEvent
import com.facetlauncher.app.data.model.ClockAlignment
import com.facetlauncher.app.data.model.ClockColorOption
import com.facetlauncher.app.data.model.ClockDateStyle
import com.facetlauncher.app.data.model.ClockFontOption
import com.facetlauncher.app.data.model.ClockTemplateId
import com.facetlauncher.app.data.model.DockDisplayMode
import com.facetlauncher.app.data.model.DrawerPresentation
import com.facetlauncher.app.data.model.FontWeightOption
import com.facetlauncher.app.data.model.HomeWallpaper
import com.facetlauncher.app.data.model.LauncherFontOption
import com.facetlauncher.app.data.model.NotificationBadgeStyle
import com.facetlauncher.app.data.model.PlacedItem
import com.facetlauncher.app.data.model.toColumnPositions
import com.facetlauncher.app.ui.components.AppIcon
import com.facetlauncher.app.ui.components.AppIconSize
import com.facetlauncher.app.ui.components.AppMenuNotifications
import com.facetlauncher.app.ui.components.WallpaperBackground
import com.facetlauncher.app.ui.theme.Muted
import com.facetlauncher.app.ui.theme.resolve

/**
 * Row budget the scaled card can show before crowding out the clock/dock. Grid and two-column pack
 * several items per row, so the same budget shows proportionally more of the real list.
 */
private const val PREVIEW_ROW_BUDGET = 6

private fun AppListLayout.previewItemCap(gridColumns: Int): Int = when (this) {
    AppListLayout.GRID -> PREVIEW_ROW_BUDGET * gridColumns
    AppListLayout.TWO_COLUMN -> PREVIEW_ROW_BUDGET * 2
    AppListLayout.SINGLE_COLUMN, AppListLayout.LAUNCHER_DEFAULT -> PREVIEW_ROW_BUDGET
}

/**
 * The scaled-down Home used by the facet carousel and the Appearance screens: wallpaper, then the
 * real [ClockBlock]/[AppRow]/[DockIcon] under a density shrunk to the card's size. The caller
 * supplies the card chrome (size, shape, border) through [modifier]. Clock alignment, zone height
 * and scale, and the app list's row/column/vertical alignment follow [state] exactly as Home does.
 * [onClick] is the tap-to-apply action (carousel); null leaves the preview read-only.
 */
@Composable
internal fun HomePreview(
    state: HomePreviewState,
    homeWallpaper: HomeWallpaper,
    testTagPrefix: String,
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    showEmptyHint: Boolean = false,
    clockTestTag: String? = null,
) {
    BoxWithConstraints(modifier = modifier) {
        WallpaperBackground(homeWallpaper, Modifier.matchParentSize())

        // Shrink every dp/sp inside by card height / real screen height so the card is a true scale model.
        val screenHeight = LocalConfiguration.current.screenHeightDp.dp
        val contentScale = (maxHeight / screenHeight).coerceIn(0.4f, 1f)
        val baseDensity = LocalDensity.current
        CompositionLocalProvider(
            LocalDensity provides Density(baseDensity.density * contentScale, baseDensity.fontScale),
        ) {
            HomePreviewContent(state, testTagPrefix, onClick, showEmptyHint, clockTestTag)
        }
    }
}

@Composable
private fun HomePreviewContent(
    state: HomePreviewState,
    testTagPrefix: String,
    onClick: (() -> Unit)?,
    showEmptyHint: Boolean,
    clockTestTag: String?,
) {
    // Same formula as HomeScreen: a null zone height is the original fixed top offset, else the
    // handle sits at the zone height and the clock's bottom edge ends HOME_CLOCK_MIN_GAP above it.
    var clockNaturalHeightDp by remember { mutableFloatStateOf(0f) }
    val previewDensity = LocalDensity.current
    val topOffsetDp = state.clockZoneHeightDp
        ?.let { (it - HOME_CLOCK_MIN_GAP.value - clockNaturalHeightDp).coerceAtLeast(0f) }
        ?: HOME_CLOCK_DEFAULT_TOP_OFFSET.value
    Column(
        modifier = Modifier.fillMaxSize().padding(18.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        ClockBlock(
            use24HourTime = state.use24HourTime,
            templateId = state.clockTemplateId,
            fontOption = state.clockFontOption,
            colorOption = state.clockColorOption,
            accentColorOption = state.clockAccentColorOption,
            showMeridiem = state.clockShowMeridiem,
            dateStyle = state.clockDateStyle,
            clockAlignment = state.clockAlignment,
            events = state.calendarEvents,
            calendarColors = state.calendarColors,
            calendarDisplay = state.calendarDisplay,
            homeAppsFontWeight = state.homeAppsFontWeight,
            appLabelColorOption = state.appLabelColorOption,
            launcherFontOption = state.launcherFontOption,
            clockScale = state.clockScale,
            modifier = Modifier
                .align(state.clockAlignment.resolve())
                .padding(top = topOffsetDp.dp)
                .onGloballyPositioned { clockNaturalHeightDp = with(previewDensity) { it.size.height.toDp().value } }
                .let { if (clockTestTag != null) it.testTag(clockTestTag) else it },
        )

        if (state.appListVerticalAlignment == AppListVerticalAlignment.TOP) {
            Spacer(modifier = Modifier.height(HOME_CLOCK_MIN_GAP))
            HomePreviewFavorites(state, testTagPrefix, onClick, showEmptyHint)
            Spacer(modifier = Modifier.weight(1f))
        } else {
            Spacer(modifier = Modifier.weight(1f))
            HomePreviewFavorites(state, testTagPrefix, onClick, showEmptyHint)
        }

        Spacer(modifier = Modifier.height(16.dp))
        HomePreviewDock(state, onClick)
    }
}

@Composable
private fun HomePreviewFavorites(state: HomePreviewState, testTagPrefix: String, onClick: (() -> Unit)?, showEmptyHint: Boolean) {
    if (state.favorites.isNotEmpty()) {
        HomePreviewAppList(state = state, testTagPrefix = testTagPrefix, onClick = onClick)
    } else if (showEmptyHint) {
        Text(
            text = stringResource(R.string.facet_carousel_no_favorites),
            style = MaterialTheme.typography.bodyMedium,
            color = Muted,
            modifier = Modifier.fillMaxWidth(),
        )
    }
}

@Composable
private fun HomePreviewDock(state: HomePreviewState, onClick: (() -> Unit)?) {
    if (state.dockItems.isEmpty()) return
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
        state.dockItems.take(DockAppRepository.MAX_APPS).forEach { item ->
            DockIcon(
                item = item,
                displayMode = state.dockDisplayMode,
                onClick = { onClick?.invoke() },
                labelColor = state.appLabelColorOption.resolve(),
                labelFontWeight = state.homeAppsFontWeight.resolve(),
                enableLongPressMenu = false,
            )
        }
    }
}

/** The app-list region's 3-way [AppListLayout] branch, split out to keep nesting shallow. */
@Composable
private fun HomePreviewAppList(
    state: HomePreviewState,
    testTagPrefix: String,
    onClick: (() -> Unit)?,
) {
    val favorites = state.favorites.take(state.appListLayout.previewItemCap(state.appListGridColumns.columns))
    Column(modifier = Modifier.fillMaxWidth()) {
        when (state.appListLayout) {
            AppListLayout.GRID -> {
                // Fixed-slot columns, not SpaceEvenly: a short last row packs left like HomeScreen's LazyVerticalGrid.
                favorites.chunked(state.appListGridColumns.columns).forEach { rowItems ->
                    Row(modifier = Modifier.fillMaxWidth()) {
                        repeat(state.appListGridColumns.columns) { columnIndex ->
                            Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.Center) {
                                rowItems.getOrNull(columnIndex)?.let { item ->
                                    HomePreviewGridTile(item, state, testTagPrefix, onClick)
                                }
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }
            AppListLayout.TWO_COLUMN -> {
                val (leftPosition, rightPosition) = state.appListColumnAlignment.toColumnPositions()
                Row(modifier = Modifier.fillMaxWidth()) {
                    listOf(
                        favorites.filterIndexed { index, _ -> index % 2 == 0 } to leftPosition,
                        favorites.filterIndexed { index, _ -> index % 2 == 1 } to rightPosition,
                    ).forEach { (items, position) ->
                        Column(modifier = Modifier.weight(1f)) {
                            items.forEach { item -> HomePreviewRow(item, position, state, testTagPrefix, onClick) }
                        }
                    }
                }
            }
            AppListLayout.SINGLE_COLUMN, AppListLayout.LAUNCHER_DEFAULT -> {
                favorites.forEach { item -> HomePreviewRow(item, state.appRowPosition, state, testTagPrefix, onClick) }
            }
        }
    }
}

@Composable
private fun HomePreviewRow(
    item: PlacedItem,
    position: AppRowPosition,
    state: HomePreviewState,
    testTagPrefix: String,
    onClick: (() -> Unit)?,
) {
    when (item) {
        is PlacedItem.SingleApp -> AppRow(
            app = item.app,
            onClick = { onClick?.invoke() },
            badgeCount = null,
            badgeStyle = NotificationBadgeStyle.DOT,
            appMenuNotifications = AppMenuNotifications.None,
            onAppInfo = {},
            position = position,
            presentation = state.appRowPresentation,
            labelColor = state.appLabelColorOption.resolve(),
            labelFontWeight = state.homeAppsFontWeight.resolve(),
            // A preview, not a place to manage apps — no real Uninstall/App Info menu.
            enableLongPressMenu = false,
            // Distinct prefix keeps tag lookups unambiguous next to Home's own rows.
            testTagPrefix = testTagPrefix,
        )
        is PlacedItem.FolderItem -> FolderRow(
            folder = item.folder,
            onAppClick = {},
            appMenuNotifications = AppMenuNotifications.None,
            onAppInfo = {},
            onRemoveFromFolder = { _, _ -> },
            onRenameFolder = { _, _ -> },
            drawerPresentation = DrawerPresentation.LIST,
            position = position,
            presentation = state.appRowPresentation,
            labelColor = state.appLabelColorOption.resolve(),
            labelFontWeight = state.homeAppsFontWeight.resolve(),
            enableLongPressMenu = false,
            onClick = { onClick?.invoke() },
            testTagPrefix = testTagPrefix,
        )
    }
}

/** Grid-mode tile: a strict icon-or-text branch, see [AppListGridDisplayMode]. */
@Composable
private fun HomePreviewGridTile(
    item: PlacedItem,
    state: HomePreviewState,
    testTagPrefix: String,
    onClick: (() -> Unit)?,
) {
    val tagSuffix = when (item) {
        is PlacedItem.SingleApp -> item.app.packageName
        is PlacedItem.FolderItem -> "folder_${item.folder.id}"
    }
    val label = when (item) {
        is PlacedItem.SingleApp -> item.app.label
        is PlacedItem.FolderItem -> item.folder.name
    }
    val tileModifier = if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier
    Box(modifier = tileModifier.padding(vertical = 4.dp), contentAlignment = Alignment.Center) {
        if (state.appListGridDisplayMode == AppListGridDisplayMode.TEXT) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = state.appLabelColorOption.resolve(),
                maxLines = 1,
                fontWeight = state.homeAppsFontWeight.resolve(),
                modifier = Modifier.testTag("${testTagPrefix}grid_label_$tagSuffix"),
            )
        } else {
            when (item) {
                is PlacedItem.SingleApp -> AppIcon(
                    icon = item.app.icon,
                    size = AppIconSize.TILE,
                    contentDescription = label,
                    modifier = Modifier.testTag("${testTagPrefix}grid_icon_$tagSuffix"),
                )
                is PlacedItem.FolderItem -> FolderTileGlyph(folder = item.folder)
            }
        }
    }
}
