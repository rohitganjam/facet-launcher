package com.facetlauncher.app.ui.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.onClick
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.facetlauncher.app.data.model.AppInfo
import com.facetlauncher.app.data.model.AppListColumnAlignment
import com.facetlauncher.app.data.model.AppListGridDisplayMode
import com.facetlauncher.app.data.model.AppRowPosition
import com.facetlauncher.app.data.model.AppRowPresentation
import com.facetlauncher.app.data.model.Folder
import com.facetlauncher.app.data.model.PlacedItem
import com.facetlauncher.app.data.model.toColumnPositions
import com.facetlauncher.app.ui.components.AppIcon
import com.facetlauncher.app.ui.components.AppIconSize
import com.facetlauncher.app.ui.components.dashedBorder
import com.facetlauncher.app.ui.components.longPressReleaseClickable
import com.facetlauncher.app.ui.home.AppRow
import com.facetlauncher.app.ui.home.FolderRow
import com.facetlauncher.app.ui.home.FolderTileGlyph
import com.facetlauncher.app.ui.home.stableKey
import com.facetlauncher.app.ui.theme.homeAppLabelShadow
import kotlinx.coroutines.flow.map

internal fun Modifier.dashedBorder(color: Color, cornerRadius: Dp, strokeWidth: Dp = 1.dp): Modifier = drawBehind {
    drawRoundRect(
        color = color,
        style = Stroke(width = strokeWidth.toPx(), pathEffect = PathEffect.dashPathEffect(floatArrayOf(8f, 6f), 0f)),
        cornerRadius = CornerRadius(cornerRadius.toPx()),
    )
}

/**
 * [AppListLayout.TWO_COLUMN]: the list split interleaved by original order (0, 2, 4… left, 1, 3, 5… right)
 * into two equal [HomeAppListColumn]s. [AppRowPosition] LEFT and RIGHT already pack a row to its own
 * container's edge, so [columnAlignment] just picks which of the two each column uses.
 */
@Composable
internal fun HomeAppTwoColumnList(
    content: HomeAppListContent,
    actions: HomeItemActions,
    columnAlignment: AppListColumnAlignment,
    appRowPresentation: AppRowPresentation,
    verticalPadding: Dp,
    iconSize: Dp,
    modifier: Modifier = Modifier,
) {
    val columns = remember(content) {
        listOf(0, 1).map { parity -> HomeAppListContent(content.items.filterIndexed { index, _ -> index % 2 == parity }, content.style) }
    }
    val (leftPosition, rightPosition) = columnAlignment.toColumnPositions()
    Row(modifier = modifier.fillMaxWidth()) {
        columns.zip(listOf(leftPosition, rightPosition)).forEach { (column, position) ->
            HomeAppListColumn(
                content = column,
                actions = actions,
                position = position,
                appRowPresentation = appRowPresentation,
                verticalPadding = verticalPadding,
                iconSize = iconSize,
                modifier = Modifier.weight(1f),
            )
        }
    }
}

/**
 * One column of Home's app list: an [AppRow] or [FolderRow] per item, packed by [position]. [rowWidthFraction] and
 * [horizontalAlignment] let the single-column layout use it for rows narrower than the list, anchored to a side.
 */
@Composable
internal fun HomeAppListColumn(
    content: HomeAppListContent,
    actions: HomeItemActions,
    position: AppRowPosition,
    appRowPresentation: AppRowPresentation,
    verticalPadding: Dp,
    iconSize: Dp,
    modifier: Modifier = Modifier,
    rowWidthFraction: Float = 1f,
    horizontalAlignment: Alignment.Horizontal = Alignment.Start,
) {
    Column(modifier = modifier, horizontalAlignment = horizontalAlignment) {
        content.items.forEach { item ->
            val rowModifier = Modifier.fillMaxWidth(rowWidthFraction)
            when (item) {
                is PlacedItem.SingleApp -> AppRow(
                    app = item.app,
                    actions = actions,
                    style = content.style,
                    modifier = rowModifier,
                    position = position,
                    presentation = appRowPresentation,
                    verticalPadding = verticalPadding,
                    iconSize = iconSize,
                )
                is PlacedItem.FolderItem -> FolderRow(
                    folder = item.folder,
                    actions = actions,
                    style = content.style,
                    modifier = rowModifier,
                    position = position,
                    presentation = appRowPresentation,
                    verticalPadding = verticalPadding,
                    iconSize = iconSize,
                )
            }
        }
    }
}

/**
 * [AppListLayout.GRID]: a plain [LazyVerticalGrid] sharing the reserved region the other layouts use, so each tile
 * takes its own intrinsic height. A tile shows an icon or a label, never both, as [DockIcon] does for the dock.
 */
@Composable
internal fun HomeAppGrid(
    content: HomeAppListContent,
    actions: HomeItemActions,
    columns: Int,
    displayMode: AppListGridDisplayMode,
    modifier: Modifier = Modifier,
) {
    LazyVerticalGrid(
        columns = GridCells.Fixed(columns),
        modifier = modifier.fillMaxWidth().testTag("home_app_grid"),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        items(content.items, key = { it.stableKey() }) { item ->
            when (item) {
                is PlacedItem.SingleApp -> HomeAppGridTile(item.app, displayMode, actions, content.style)
                is PlacedItem.FolderItem -> HomeFolderGridTile(item.folder, displayMode, actions, content.style)
            }
        }
    }
}

/** One [HomeAppGrid] tile. */
@Composable
internal fun HomeAppGridTile(
    app: AppInfo,
    displayMode: AppListGridDisplayMode,
    actions: HomeItemActions,
    style: HomeItemStyle,
    modifier: Modifier = Modifier,
) {
    var menuExpanded by remember { mutableStateOf(false) }
    Box(
        modifier = modifier
            .fillMaxWidth()
            .longPressReleaseClickable(onClick = { actions.onAppClick(app) }, onLongPress = { menuExpanded = true })
            .padding(vertical = 8.dp),
        contentAlignment = Alignment.Center,
    ) {
        if (displayMode == AppListGridDisplayMode.TEXT) {
            Text(
                text = app.label,
                style = MaterialTheme.typography.labelSmall.copy(shadow = homeAppLabelShadow(style.labelColor), fontWeight = style.labelFontWeight),
                color = style.labelColor,
                textAlign = TextAlign.Center,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.testTag("home_app_label_${app.packageName}"),
            )
        } else {
            AppIcon(
                app = app,
                size = AppIconSize.TILE,
                contentDescription = app.label,
                notificationCount = style.badgeCounts[app.packageName],
                badgeStyle = style.badgeStyle,
                modifier = Modifier.testTag("home_app_icon_${app.packageName}"),
            )
        }
        AppItemMenu(app, menuExpanded, { menuExpanded = false }, actions, style)
    }
}

/** [HomeAppGridTile]'s folder counterpart: the contents sheet on tap, the folder menu on long-press. */
@Composable
internal fun HomeFolderGridTile(
    folder: Folder,
    displayMode: AppListGridDisplayMode,
    actions: HomeItemActions,
    style: HomeItemStyle,
    modifier: Modifier = Modifier,
) {
    var sheetOpen by remember { mutableStateOf(false) }
    var menuExpanded by remember { mutableStateOf(false) }
    Box(
        modifier = modifier
            .fillMaxWidth()
            .longPressReleaseClickable(onClick = { sheetOpen = true }, onLongPress = { menuExpanded = true })
            .testTag("home_folder_grid_tile_${folder.id}")
            .padding(vertical = 8.dp),
        contentAlignment = Alignment.Center,
    ) {
        if (displayMode == AppListGridDisplayMode.TEXT) {
            Text(
                text = folder.name,
                style = MaterialTheme.typography.labelSmall.copy(shadow = homeAppLabelShadow(style.labelColor), fontWeight = style.labelFontWeight),
                color = style.labelColor,
                textAlign = TextAlign.Center,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
        } else {
            FolderTileGlyph(folder = folder)
        }
        if (sheetOpen) FolderItemSheet(folder, { sheetOpen = false }, actions, style)
        FolderItemMenu(folder, menuExpanded, { menuExpanded = false }, actions)
    }
}
