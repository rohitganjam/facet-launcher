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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.key
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.facetlauncher.app.R
import com.facetlauncher.app.data.model.AppListColumnAlignment
import com.facetlauncher.app.data.model.AppListGridColumns
import com.facetlauncher.app.data.model.AppListGridDisplayMode
import com.facetlauncher.app.data.model.AppListLayout
import com.facetlauncher.app.data.model.AppRowPosition
import com.facetlauncher.app.data.model.AppRowPresentation
import com.facetlauncher.app.data.model.DockDisplayMode
import com.facetlauncher.app.data.model.HomeWallpaper
import com.facetlauncher.app.data.model.NotificationBadgeStyle
import com.facetlauncher.app.data.model.PlacedItem
import com.facetlauncher.app.data.model.toColumnPositions
import com.facetlauncher.app.ui.components.AppIcon
import com.facetlauncher.app.ui.components.AppIconSize
import com.facetlauncher.app.ui.components.HomeSurfacePreview
import com.facetlauncher.app.ui.components.WallpaperBackground
import com.facetlauncher.app.ui.home.AppRow
import com.facetlauncher.app.ui.home.DockIcon
import com.facetlauncher.app.ui.home.FolderRow
import com.facetlauncher.app.ui.home.FolderTileGlyph
import com.facetlauncher.app.ui.home.HomeItemActions
import com.facetlauncher.app.ui.home.HomeItemStyle
import com.facetlauncher.app.ui.home.stableKey
import com.facetlauncher.app.ui.theme.Hairline
import com.facetlauncher.app.ui.theme.LauncherScaledTypography
import com.facetlauncher.app.ui.theme.Muted
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList

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
    appList: ImmutableList<PlacedItem>,
    dockApps: ImmutableList<PlacedItem>,
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
    notificationBadgeStyle: NotificationBadgeStyle = NotificationBadgeStyle.DOT,
    /** Sample unread count for a package; the default draws no badges. */
    badgeCountFor: (String) -> Int? = { null },
) {
    val items = remember(appList, maxAppItems) { appList.take(maxAppItems).toImmutableList() }
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

        LauncherScaledTypography {
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
                        badgeStyle = notificationBadgeStyle,
                        badgeCountFor = badgeCountFor,
                    )
                    AppListLayout.TWO_COLUMN -> HomeSurfacePreviewTwoColumns(
                        items = items,
                        columnAlignment = appListColumnAlignment,
                        appRowPresentation = appRowPresentation,
                        labelColor = labelColor,
                        labelFontWeight = labelFontWeight,
                        badgeStyle = notificationBadgeStyle,
                        badgeCountFor = badgeCountFor,
                    )
                    AppListLayout.SINGLE_COLUMN, AppListLayout.LAUNCHER_DEFAULT -> items.forEach { item ->
                        HomeSurfacePreviewRow(
                            item = item,
                            position = appRowPosition,
                            presentation = appRowPresentation,
                            labelColor = labelColor,
                            labelFontWeight = labelFontWeight,
                            badgeStyle = notificationBadgeStyle,
                            badgeCountFor = badgeCountFor,
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
                                actions = PreviewItemActions,
                                style = previewItemStyle(item, labelColor, labelFontWeight, notificationBadgeStyle, badgeCountFor),
                                enableLongPressMenu = false,
                            )
                        }
                    }
                }
            }
        }
    }
}

/** [HomeSurfacePreview]'s [AppListLayout.TWO_COLUMN] region — same interleaved split and [AppListColumnAlignment.toColumnPositions] mapping as [HomeScreen][com.facetlauncher.app.ui.home.HomeScreen]'s own `HomeAppTwoColumnList`. */
@Composable
private fun HomeSurfacePreviewTwoColumns(
    items: ImmutableList<PlacedItem>,
    columnAlignment: AppListColumnAlignment,
    appRowPresentation: AppRowPresentation,
    labelColor: Color,
    labelFontWeight: FontWeight,
    badgeStyle: NotificationBadgeStyle,
    badgeCountFor: (String) -> Int?,
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
                        badgeStyle = badgeStyle,
                        badgeCountFor = badgeCountFor,
                    )
                }
            }
        }
    }
}

/** The preview is read-only, so nothing it draws does anything when tapped. */
private val PreviewItemActions = HomeItemActions(onAppClick = {})

/** The style for one preview [item]: its sample badge count (if it is an app) and the card's label look. */
private fun previewItemStyle(
    item: PlacedItem,
    labelColor: Color,
    labelFontWeight: FontWeight,
    badgeStyle: NotificationBadgeStyle,
    badgeCountFor: (String) -> Int?,
): HomeItemStyle {
    val packageName = (item as? PlacedItem.SingleApp)?.app?.packageName
    val count = packageName?.let(badgeCountFor)
    return HomeItemStyle(
        labelColor = labelColor,
        labelFontWeight = labelFontWeight,
        badgeStyle = badgeStyle,
        badgeCounts = if (packageName != null && count != null) mapOf(packageName to count) else emptyMap(),
    )
}

/** [HomeSurfacePreview]'s single/two-column row dispatch — identical [AppRow]/[FolderRow] call either layout needs, just with a different [position] and item subset. */
@Composable
private fun HomeSurfacePreviewRow(
    item: PlacedItem,
    position: AppRowPosition,
    presentation: AppRowPresentation,
    labelColor: Color,
    labelFontWeight: FontWeight,
    badgeStyle: NotificationBadgeStyle,
    badgeCountFor: (String) -> Int?,
) {
    val style = previewItemStyle(item, labelColor, labelFontWeight, badgeStyle, badgeCountFor)
    when (item) {
        is PlacedItem.SingleApp -> AppRow(
            app = item.app,
            actions = PreviewItemActions,
            style = style,
            position = position,
            presentation = presentation,
            verticalPadding = 6.dp,
            enableLongPressMenu = false,
            // Distinguishes this preview card's rows from Home's own real AppRow for
            // the same app, in case both are ever present in one semantics tree.
            testTagPrefix = "home_surface_preview_",
        )
        is PlacedItem.FolderItem -> FolderRow(
            folder = item.folder,
            actions = PreviewItemActions,
            style = style,
            position = position,
            presentation = presentation,
            testTagPrefix = "home_surface_preview_",
            enableLongPressMenu = false,
        )
    }
}

/** [HomeSurfacePreview]'s [AppListLayout.GRID] region — read-only, so a plain centered icon-or-label tile rather than [HomeScreen][com.facetlauncher.app.ui.home.HomeScreen]'s own clickable/long-pressable one. */
@Composable
private fun HomeSurfacePreviewGrid(
    items: ImmutableList<PlacedItem>,
    columns: Int,
    displayMode: AppListGridDisplayMode,
    labelColor: Color,
    labelFontWeight: FontWeight,
    badgeStyle: NotificationBadgeStyle,
    badgeCountFor: (String) -> Int?,
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
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        when (item) {
                            is PlacedItem.SingleApp -> AppIcon(
                                app = item.app,
                                size = AppIconSize.scaled(),
                                contentDescription = label,
                                notificationCount = badgeCountFor(item.app.packageName),
                                badgeStyle = badgeStyle,
                            )
                            is PlacedItem.FolderItem -> FolderTileGlyph(folder = item.folder, size = AppIconSize.scaled())
                        }
                        if (displayMode == AppListGridDisplayMode.ICONS_AND_TEXT) {
                            Text(
                                text = label,
                                style = MaterialTheme.typography.labelSmall,
                                color = labelColor,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                fontWeight = labelFontWeight,
                                modifier = Modifier.padding(top = 4.dp).widthIn(max = AppIconSize.scaled(AppIconSize.GRID_LABEL_WIDTH)),
                            )
                        }
                    }
                }
            }
        }
    }
}
