package com.facetlauncher.app.ui.components

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.facetlauncher.app.R
import com.facetlauncher.app.data.model.AppListColumnAlignment
import com.facetlauncher.app.data.model.AppListGridColumns
import com.facetlauncher.app.data.model.AppListGridDisplayMode
import com.facetlauncher.app.data.model.AppListLayout
import com.facetlauncher.app.data.model.AppRowPosition
import com.facetlauncher.app.data.model.AppRowPresentation
import com.facetlauncher.app.data.model.DockDisplayMode
import com.facetlauncher.app.data.model.DrawerPresentation
import com.facetlauncher.app.data.model.HomeWallpaper
import com.facetlauncher.app.data.model.PlacedItem
import com.facetlauncher.app.data.model.NotificationBadgeStyle
import com.facetlauncher.app.data.model.toColumnPositions
import com.facetlauncher.app.ui.home.AppRow
import com.facetlauncher.app.ui.home.DockIcon
import com.facetlauncher.app.ui.home.FolderRow
import com.facetlauncher.app.ui.home.FolderTileGlyph
import com.facetlauncher.app.ui.home.stableKey
import com.facetlauncher.app.ui.theme.Hairline
import com.facetlauncher.app.ui.theme.Muted

/**
 * "This is how Home looks" preview card, shared by Settings → Appearance / Home Apps List / Dock
 * and by a facet's own Apps-list / Dock sub-screens. Uses the real production [AppRow]/[FolderRow]
 * and [DockIcon] over the device's actual wallpaper ([WallpaperBackground]) rather than a mockup,
 * so the position/presentation/display-style/label choices render exactly as they would on Home —
 * including a folder rendering as a folder tile/row rather than its member apps spilling out
 * flattened (see chat history — a folder placed in the dock or favorites used to render this way).
 *
 * Pass an empty [appList] to omit the app rows, an empty [dockApps] to omit the dock — callers
 * that only govern one of the two surfaces show only that one. Both empty renders a short
 * placeholder rather than an empty card.
 */
@Composable
fun HomeSurfacePreview(
    appList: List<PlacedItem>,
    dockApps: List<PlacedItem>,
    labelColor: Color,
    labelFontWeight: FontWeight,
    homeWallpaper: HomeWallpaper,
    modifier: Modifier = Modifier,
    appRowPosition: AppRowPosition = AppRowPosition.LEFT,
    appRowPresentation: AppRowPresentation = AppRowPresentation.ICON_AND_TEXT,
    dockDisplayMode: DockDisplayMode = DockDisplayMode.ICONS,
    appListLayout: AppListLayout = AppListLayout.SINGLE_COLUMN,
    appListColumnAlignment: AppListColumnAlignment = AppListColumnAlignment.BOTH_LEFT,
    appListGridColumns: AppListGridColumns = AppListGridColumns.FOUR,
    appListGridDisplayMode: AppListGridDisplayMode = AppListGridDisplayMode.ICONS,
    maxAppItems: Int = 3,
) {
    val items = appList.take(maxAppItems)
    val shape = RoundedCornerShape(14.dp)
    Box(
        modifier = modifier
            .fillMaxWidth()
            .shadow(elevation = 4.dp, shape = shape)
            .clip(shape)
            .border(1.dp, Hairline, shape),
    ) {
        // The real system wallpaper — same backdrop Home renders these components over, so their
        // text-shadow treatment reads exactly as it would there. Bottom-anchored: this card is a
        // short band, so it shows the wallpaper's lower part (where Home's dock sits).
        WallpaperBackground(homeWallpaper, Modifier.matchParentSize(), alignment = Alignment.BottomCenter)

        Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
            if (items.isEmpty() && dockApps.isEmpty()) {
                Text(
                    text = stringResource(R.string.home_surface_preview_nothing_yet),
                    style = MaterialTheme.typography.bodyMedium,
                    color = Muted,
                )
            }
            when (appListLayout) {
                AppListLayout.GRID -> HomeSurfacePreviewGrid(
                    items = items,
                    columns = appListGridColumns.columns,
                    displayMode = appListGridDisplayMode,
                    labelColor = labelColor,
                    labelFontWeight = labelFontWeight,
                )
                AppListLayout.TWO_COLUMN -> HomeSurfacePreviewTwoColumns(
                    items = items,
                    columnAlignment = appListColumnAlignment,
                    appRowPresentation = appRowPresentation,
                    labelColor = labelColor,
                    labelFontWeight = labelFontWeight,
                )
                AppListLayout.SINGLE_COLUMN, AppListLayout.LAUNCHER_DEFAULT -> items.forEach { item ->
                    HomeSurfacePreviewRow(
                        item = item,
                        position = appRowPosition,
                        presentation = appRowPresentation,
                        labelColor = labelColor,
                        labelFontWeight = labelFontWeight,
                    )
                }
            }
            if (dockApps.isNotEmpty()) {
                Spacer(modifier = Modifier.height(12.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                    dockApps.forEach { item ->
                        DockIcon(
                            item = item,
                            displayMode = dockDisplayMode,
                            onClick = {},
                            labelColor = labelColor,
                            labelFontWeight = labelFontWeight,
                            enableLongPressMenu = false,
                        )
                    }
                }
            }
        }
    }
}

/** [HomeSurfacePreview]'s [AppListLayout.TWO_COLUMN] region — same interleaved split and [AppListColumnAlignment.toColumnPositions] mapping as [HomeScreen][com.facetlauncher.app.ui.home.HomeScreen]'s own `HomeAppTwoColumnList`. */
@Composable
private fun HomeSurfacePreviewTwoColumns(
    items: List<PlacedItem>,
    columnAlignment: AppListColumnAlignment,
    appRowPresentation: AppRowPresentation,
    labelColor: Color,
    labelFontWeight: FontWeight,
) {
    val (leftPosition, rightPosition) = columnAlignment.toColumnPositions()
    Row(modifier = Modifier.fillMaxWidth()) {
        listOf(
            items.filterIndexed { index, _ -> index % 2 == 0 } to leftPosition,
            items.filterIndexed { index, _ -> index % 2 == 1 } to rightPosition,
        ).forEach { (columnItems, position) ->
            Column(modifier = Modifier.weight(1f)) {
                columnItems.forEach { item ->
                    HomeSurfacePreviewRow(
                        item = item,
                        position = position,
                        presentation = appRowPresentation,
                        labelColor = labelColor,
                        labelFontWeight = labelFontWeight,
                    )
                }
            }
        }
    }
}

/** [HomeSurfacePreview]'s single/two-column row dispatch — identical [AppRow]/[FolderRow] call either layout needs, just with a different [position] and item subset. */
@Composable
private fun HomeSurfacePreviewRow(item: PlacedItem, position: AppRowPosition, presentation: AppRowPresentation, labelColor: Color, labelFontWeight: FontWeight) {
    when (item) {
        is PlacedItem.SingleApp -> AppRow(
            app = item.app,
            onClick = {},
            badgeCount = null,
            badgeStyle = NotificationBadgeStyle.DOT,
            onRequestShortcuts = { emptyList() },
            onLaunchShortcut = {},
            onAppInfo = {},
            position = position,
            presentation = presentation,
            labelColor = labelColor,
            labelFontWeight = labelFontWeight,
            enableLongPressMenu = false,
            verticalPadding = 6.dp,
            // Distinguishes this preview card's rows from Home's own real AppRow for
            // the same app, in case both are ever present in one semantics tree.
            testTagPrefix = "home_surface_preview_",
        )
        is PlacedItem.FolderItem -> FolderRow(
            folder = item.folder,
            onAppClick = {},
            onRequestShortcuts = { emptyList() },
            onLaunchShortcut = {},
            onAppInfo = {},
            onRemoveFromFolder = { _, _ -> },
            onRenameFolder = { _, _ -> },
            drawerPresentation = DrawerPresentation.LIST,
            position = position,
            presentation = presentation,
            labelColor = labelColor,
            labelFontWeight = labelFontWeight,
            enableLongPressMenu = false,
            onClick = {},
            testTagPrefix = "home_surface_preview_",
        )
    }
}

/** [HomeSurfacePreview]'s [AppListLayout.GRID] region — read-only, so a plain centered icon-or-label tile rather than [HomeScreen][com.facetlauncher.app.ui.home.HomeScreen]'s own clickable/long-pressable one. */
@Composable
private fun HomeSurfacePreviewGrid(
    items: List<PlacedItem>,
    columns: Int,
    displayMode: AppListGridDisplayMode,
    labelColor: Color,
    labelFontWeight: FontWeight,
) {
    LazyVerticalGrid(
        columns = GridCells.Fixed(columns),
        modifier = Modifier.fillMaxWidth().height(160.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        userScrollEnabled = false,
    ) {
        items(items, key = { it.stableKey() }) { item ->
            Box(modifier = Modifier.padding(vertical = 4.dp), contentAlignment = Alignment.Center) {
                val label = when (item) {
                    is PlacedItem.SingleApp -> item.app.label
                    is PlacedItem.FolderItem -> item.folder.name
                }
                if (displayMode == AppListGridDisplayMode.TEXT) {
                    Text(
                        text = label,
                        style = MaterialTheme.typography.labelSmall,
                        color = labelColor,
                        maxLines = 1,
                        fontWeight = labelFontWeight,
                    )
                } else {
                    when (item) {
                        is PlacedItem.SingleApp -> AppIcon(icon = item.app.icon, size = AppIconSize.TILE, contentDescription = label)
                        is PlacedItem.FolderItem -> FolderTileGlyph(folder = item.folder)
                    }
                }
            }
        }
    }
}
