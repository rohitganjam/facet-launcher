package com.facetlauncher.app.ui.home

import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import com.facetlauncher.app.domain.HomeGridPlacement
import com.facetlauncher.app.domain.GridEntry
import com.facetlauncher.app.domain.GridCell
import com.facetlauncher.app.domain.SavedGridCells
import com.facetlauncher.app.data.model.gridKey
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
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
import com.facetlauncher.app.ui.components.PickUpReorderHost
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
    onReorder: ((List<PlacedItem>) -> Unit)? = null,
) {
    Column(modifier = modifier, horizontalAlignment = horizontalAlignment) {
        PickUpReorderHost(
            items = content.items,
            key = { it.stableKey() },
            axis = Orientation.Vertical,
            enabled = onReorder != null,
            onReorder = { onReorder?.invoke(it) },
        ) { shown ->
        shown.forEach { item -> key(item.stableKey()) {
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
        } }
        }
    }
}

/**
 * [AppListLayout.GRID]: fixed-height cells in [columns] columns, so every item has a cell and an empty cell or a gap is free
 * space. Items sit on their [savedCells] or, until moved, fill the grid row by row from the anchor edge ([anchoredAtBottom]
 * puts row 0 at the bottom). The grid has as many rows as fit in [availableHeight], or more if an item sits lower, and scrolls.
 */
@Composable
internal fun HomeAppGrid(
    content: HomeAppListContent,
    actions: HomeItemActions,
    columns: Int,
    displayMode: AppListGridDisplayMode,
    savedCells: SavedGridCells,
    landscape: Boolean,
    anchoredAtBottom: Boolean,
    availableHeight: Dp,
    modifier: Modifier = Modifier,
) {
    val cellHeight = gridCellHeight(displayMode)
    val width = columns.coerceAtLeast(1)
    val placed = remember(content.items, savedCells, landscape, width) {
        val cells = savedCells.of(landscape)
        HomeGridPlacement.resolve(content.items.map { GridEntry(it.gridKey(), cells[it.gridKey()]) }, width)
    }
    val itemByKey = remember(content.items) { content.items.associateBy { it.gridKey() } }
    val keyAtCell = remember(placed) { placed.entries.associate { it.value to it.key } }
    val rows = HomeGridPlacement.rowCount(placed.values, minimumRows = (availableHeight / cellHeight).toInt().coerceAtLeast(1))
    LazyVerticalGrid(
        columns = GridCells.Fixed(width),
        modifier = modifier.fillMaxWidth().testTag(homeTag("home_app_grid")),
        horizontalArrangement = Arrangement.spacedBy(2.dp),
    ) {
        items(rows * width, key = { index -> keyAtCell[cellAt(index, width, rows, anchoredAtBottom)] ?: "empty_$index" }) { index ->
            val tileModifier = Modifier.height(cellHeight)
            when (val item = keyAtCell[cellAt(index, width, rows, anchoredAtBottom)]?.let(itemByKey::get)) {
                is PlacedItem.SingleApp -> HomeAppGridTile(item.app, displayMode, actions, content.style, tileModifier)
                is PlacedItem.FolderItem -> HomeFolderGridTile(item.folder, displayMode, actions, content.style, tileModifier)
                null -> Spacer(tileModifier)
            }
        }
    }
}

/** The grid cell shown at [index] (reading left to right, top to bottom on screen). */
private fun cellAt(index: Int, columns: Int, rows: Int, anchoredAtBottom: Boolean) =
    GridCell(HomeGridPlacement.anchorRow(index / columns, rows, anchoredAtBottom), index % columns)

/** A cell is one fixed height for its display mode: the icon, and its name under it where shown. */
@Composable
private fun gridCellHeight(displayMode: AppListGridDisplayMode): Dp = when (displayMode) {
    AppListGridDisplayMode.ICONS -> AppIconSize.scaled() + GRID_CELL_ICON_PADDING
    AppListGridDisplayMode.ICONS_AND_TEXT -> AppIconSize.scaled() + GRID_CELL_ICON_PADDING + GRID_CELL_LABEL_HEIGHT
    AppListGridDisplayMode.TEXT -> GRID_CELL_TEXT_HEIGHT
}

private val GRID_CELL_ICON_PADDING = 16.dp
private val GRID_CELL_LABEL_HEIGHT = 28.dp
private val GRID_CELL_TEXT_HEIGHT = 56.dp

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
                style = MaterialTheme.typography.labelLarge.copy(shadow = homeAppLabelShadow(style.labelColor), fontWeight = style.labelFontWeight),
                color = style.labelColor,
                textAlign = TextAlign.Center,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.testTag(homeTag("home_app_label_${app.packageName}")),
            )
        } else {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                AppIcon(
                    app = app,
                    size = AppIconSize.scaled(),
                    contentDescription = app.label,
                    notificationCount = style.badgeCounts[app.packageName],
                    badgeStyle = style.badgeStyle,
                    modifier = Modifier.testTag(homeTag("home_app_icon_${app.packageName}")),
                )
                if (displayMode == AppListGridDisplayMode.ICONS_AND_TEXT) {
                    HomeTileLabel(app.label, style, Modifier.testTag(homeTag("home_app_label_${app.packageName}")), Dp.Unspecified)
                }
            }
        }
        AppItemMenu(app, menuExpanded, { menuExpanded = false }, actions, style)
    }
}

/**
 * An icon tile's name under it ("Icons and text" on Home's grid and the dock): one line, in Home's label style.
 * [maxWidth] caps it where the cell is not known (the dock); a grid tile passes [Dp.Unspecified] to take its whole cell.
 */
@Composable
internal fun HomeTileLabel(
    text: String,
    style: HomeItemStyle,
    modifier: Modifier = Modifier,
    maxWidth: Dp = AppIconSize.scaled(AppIconSize.GRID_LABEL_WIDTH),
) {
    Text(
        text = text,
        style = MaterialTheme.typography.labelLarge.copy(shadow = homeAppLabelShadow(style.labelColor), fontWeight = style.labelFontWeight),
        color = style.labelColor,
        textAlign = TextAlign.Center,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis,
        modifier = modifier.padding(top = 7.dp).widthIn(max = maxWidth),
    )
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
            .testTag(homeTag("home_folder_grid_tile_${folder.id}"))
            .padding(vertical = 8.dp),
        contentAlignment = Alignment.Center,
    ) {
        if (displayMode == AppListGridDisplayMode.TEXT) {
            Text(
                text = folder.name,
                style = MaterialTheme.typography.labelLarge.copy(shadow = homeAppLabelShadow(style.labelColor), fontWeight = style.labelFontWeight),
                color = style.labelColor,
                textAlign = TextAlign.Center,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
        } else {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                FolderTileGlyph(folder = folder, size = AppIconSize.scaled())
                if (displayMode == AppListGridDisplayMode.ICONS_AND_TEXT) HomeTileLabel(folder.name, style, maxWidth = Dp.Unspecified)
            }
        }
        if (sheetOpen) FolderItemSheet(folder, { sheetOpen = false }, actions, style)
        FolderItemMenu(folder, menuExpanded, { menuExpanded = false }, actions)
    }
}
