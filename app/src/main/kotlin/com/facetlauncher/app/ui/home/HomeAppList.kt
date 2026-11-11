package com.facetlauncher.app.ui.home

import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.BoxWithConstraints
import kotlinx.coroutines.delay
import com.facetlauncher.app.ui.components.longPressReorderClickable
import com.facetlauncher.app.ui.components.glassBackground
import com.facetlauncher.app.ui.components.gridCellBounds
import com.facetlauncher.app.ui.components.LocalGridDrag
import com.facetlauncher.app.ui.components.GridDragState
import com.facetlauncher.app.ui.components.LiftableCell
import com.facetlauncher.app.data.model.GlassStyle
import com.facetlauncher.app.data.model.DrawerGridDensity
import com.facetlauncher.app.ui.components.HeldAutoScroll
import com.facetlauncher.app.ui.drawer.targetRowPitch
import com.facetlauncher.app.ui.components.ReorderGeometry
import androidx.compose.ui.zIndex
import com.facetlauncher.app.ui.components.edgeScrollDelta
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.boundsInRoot
import androidx.compose.ui.geometry.Rect
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.scrollBy
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
    onReorder: ((List<PlacedItem>) -> Unit)? = null,
) {
    // A finished drag shows at once; the saved order replaces it when it arrives.
    var local by remember(content.items) { mutableStateOf<List<PlacedItem>?>(null) }
    val items = local ?: content.items
    val columns = remember(items, content.style) {
        listOf(0, 1).map { parity -> HomeAppListContent(items.filterIndexed { index, _ -> index % 2 == parity }, content.style, content.glass) }
    }
    val (leftPosition, rightPosition) = columnAlignment.toColumnPositions()
    val drag = remember { GridDragState() }
    val currentOnReorder by rememberUpdatedState(onReorder)
    val enabled = onReorder != null && items.size > 1
    drag.insertMode = true
    HeldAutoScroll(dragging = drag.dragging, pointer = { drag.pointer.y }, onScroll = drag::addScroll)
    // The list reads row by row (0 and 1 on the first row), so a cell's index is its place in the order.
    drag.indexOfKey = { key -> items.indexOfFirst { it.stableKey() == key }.takeIf { it >= 0 } }
    drag.onDrop = { from, to ->
        val reordered = ReorderGeometry.move(items, from, to)
        local = reordered
        currentOnReorder?.invoke(reordered)
    }
    CompositionLocalProvider(LocalGridDrag provides drag.takeIf { enabled }) {
        Row(modifier = modifier.fillMaxWidth()) {
            columns.zip(listOf(leftPosition, rightPosition)).forEach { (column, position) ->
                // The column holding the lifted row is drawn over the other, so the row is not hidden when it crosses.
                val holdsLifted = drag.liftedKey != null && column.items.any { it.stableKey() == drag.liftedKey }
                HomeAppListColumn(
                    content = column,
                    actions = actions,
                    position = position,
                    appRowPresentation = appRowPresentation,
                    verticalPadding = verticalPadding,
                    iconSize = iconSize,
                    modifier = Modifier.weight(1f).zIndex(if (holdsLifted) 1f else 0f),
                    cellIndexOf = if (enabled) { item -> items.indexOf(item) } else null,
                )
            }
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
    /** Two-column list only: a row's place in the whole list, so a drag can tell which cell it is over. */
    cellIndexOf: ((PlacedItem) -> Int)? = null,
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
            val rowModifier = Modifier.fillMaxWidth(rowWidthFraction).then(if (cellIndexOf != null) Modifier.gridCellBounds(cellIndexOf(item)) else Modifier)
                .then(if (content.glass != GlassStyle.OFF) Modifier.padding(vertical = 2.dp) else Modifier)
                .glassBackground(content.glass)
                // The row's own 8dp side padding plus this equals its vertical padding, so the content sits evenly inside the panel.
                .then(if (content.glass != GlassStyle.OFF) Modifier.padding(horizontal = (verticalPadding - 8.dp).coerceAtLeast(0.dp)) else Modifier)
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
 * lays the grid out from the bottom, so row 0 is the bottom row). The grid has as many rows as fit in [availableHeight], or
 * more if an item sits lower, and scrolls. With [onMove], a long-press picks an item up: dropping on an empty cell moves it,
 * on another item swaps the two, and an empty row shows past the last occupied one while dragging.
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
    onMove: ((cells: Map<String, GridCell>) -> Unit)? = null,
) {
    val cellHeight = gridCellHeight()
    val width = columns.coerceAtLeast(1)
    // A finished drag shows at once; the saved cells replace it when they arrive.
    var moved by remember(content.items, savedCells, landscape, width) { mutableStateOf<Map<String, GridCell>?>(null) }
    val placed = remember(content.items, savedCells, landscape, width, moved) {
        val saved = savedCells.of(landscape)
        HomeGridPlacement.resolve(content.items.map { GridEntry(it.gridKey(), moved?.get(it.gridKey()) ?: saved[it.gridKey()]) }, width)
    }
    val itemByKey = remember(content.items) { content.items.associateBy { it.gridKey() } }
    val keyAtCell = remember(placed) { placed.entries.associate { it.value to it.key } }
    val drag = remember { GridDragState() }
    val enabled = onMove != null && content.items.isNotEmpty()
    val fitRows = (availableHeight / cellHeight).toInt().coerceAtLeast(1)
    val rows = if (enabled && drag.liftedKey != null) HomeGridPlacement.rowCountWhileDragging(placed.values, fitRows) else HomeGridPlacement.rowCount(placed.values, fitRows)
    val currentOnMove by rememberUpdatedState(onMove)
    drag.indexOfKey = { key -> placed[key]?.let { it.row * width + it.column } }
    drag.onDrop = { from, to ->
        val fromKey = keyAtCell[cellAtIndex(from, width)]
        if (fromKey != null) {
            val next = HomeGridPlacement.drop(placed, fromKey, cellAtIndex(to, width)).mapKeys { it.key.toString() }
            moved = next
            currentOnMove?.invoke(next)
        }
    }
    val gridState = rememberLazyGridState()
    var viewport by remember { mutableStateOf(Rect.Zero) }
    GridEdgeAutoScroll(drag, gridState, viewport, reversed = anchoredAtBottom)
    // Laid out as the Dock is: the same icon width with the gaps spread evenly, so a grid with as many columns as the Dock has icons lines up with it.
    val iconWidth = AppIconSize.scaled()
    CompositionLocalProvider(LocalGridDrag provides drag.takeIf { enabled }) {
        BoxWithConstraints(modifier = modifier.fillMaxWidth()) {
            val gap = ((maxWidth - iconWidth * width) / (width + 1)).coerceAtLeast(0.dp)
            LazyVerticalGrid(
                columns = GridCells.Fixed(width),
                state = gridState,
                reverseLayout = anchoredAtBottom,
                contentPadding = PaddingValues(horizontal = gap / 2),
                modifier = Modifier.fillMaxWidth().onGloballyPositioned { viewport = it.boundsInRoot() }.testTag(homeTag("home_app_grid")),
            ) {
                items(rows * width, key = { index -> keyAtCell[cellAtIndex(index, width)] ?: "empty_$index" }) { index ->
                    val cell = cellAtIndex(index, width)
                    val item = keyAtCell[cell]?.let(itemByKey::get)
                    val tileModifier = Modifier.height(cellHeight).gridCellBounds(index)
                    val glassTile = if (item != null) tileModifier.glassBackground(content.glass) else tileModifier
                    when (item) {
                        is PlacedItem.SingleApp -> HomeAppGridTile(item.app, displayMode, actions, content.style, glassTile, item.gridKey())
                        is PlacedItem.FolderItem -> HomeFolderGridTile(item.folder, displayMode, actions, content.style, glassTile, item.gridKey())
                        null -> GridEmptyCell(guide = drag.liftedKey != null, isTarget = drag.targetIndex == index, modifier = tileModifier)
                    }
                }
            }
        }
    }
}

/** Free space on the grid; while an item is held it shows a faint guide, a stronger one under the cell it would drop into. */
@Composable
private fun GridEmptyCell(guide: Boolean, isTarget: Boolean, modifier: Modifier = Modifier) {
    val alpha = if (isTarget) GRID_TARGET_ALPHA else if (guide) GRID_GUIDE_ALPHA else 0f
    Spacer(modifier.padding(GRID_GUIDE_INSET).background(Color.White.copy(alpha = alpha), MaterialTheme.shapes.medium))
}

/** Scrolls the grid while the held item is near its top or bottom edge, and tells [drag] how far, so the item stays under the finger. */
@Composable
private fun GridEdgeAutoScroll(drag: GridDragState, state: LazyGridState, viewport: Rect, reversed: Boolean) {
    val edge = with(LocalDensity.current) { GRID_AUTO_SCROLL_EDGE.toPx() }
    val step = with(LocalDensity.current) { GRID_AUTO_SCROLL_STEP.toPx() }
    LaunchedEffect(drag.dragging, viewport, reversed) {
        while (drag.dragging && !viewport.isEmpty) {
            val y = drag.pointer.y
            val visual = edgeScrollDelta(y, viewport.top, viewport.bottom, edge, step)
            if (visual != 0f) {
                val consumed = state.scrollBy(if (reversed) -visual else visual)
                drag.addScroll(if (reversed) -consumed else consumed)
            }
            delay(GRID_AUTO_SCROLL_FRAME_MS)
        }
    }
}

private fun cellAtIndex(index: Int, columns: Int) = GridCell(index / columns, index % columns)

private val GRID_AUTO_SCROLL_EDGE = 56.dp
private val GRID_AUTO_SCROLL_STEP = 10.dp
private const val GRID_AUTO_SCROLL_FRAME_MS = 16L
private const val GRID_GUIDE_ALPHA = 0.06f
private const val GRID_TARGET_ALPHA = 0.18f
private val GRID_GUIDE_INSET = 4.dp

/** A cell is as tall as a row of the App Drawer's grid at its regular density, whatever the display mode. */
private fun gridCellHeight(): Dp = DrawerGridDensity.REGULAR.targetRowPitch()

/** One [HomeAppGrid] tile. */
@Composable
internal fun HomeAppGridTile(
    app: AppInfo,
    displayMode: AppListGridDisplayMode,
    actions: HomeItemActions,
    style: HomeItemStyle,
    modifier: Modifier = Modifier,
    dragKey: Any = app.listKey,
) {
    var menuExpanded by remember { mutableStateOf(false) }
    LiftableCell(
        key = dragKey,
        modifier = modifier,
        copy = { Box(Modifier.fillMaxWidth().padding(vertical = 8.dp), contentAlignment = Alignment.Center) { AppGridTileBody(app, displayMode, style) } },
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .longPressReorderClickable(dragKey, onClick = { actions.onAppClick(app) }, onLongPress = { menuExpanded = true })
                .padding(vertical = 8.dp),
            contentAlignment = Alignment.Center,
        ) {
            AppGridTileBody(app, displayMode, style)
            AppItemMenu(app, menuExpanded, { menuExpanded = false }, actions, style)
        }
    }
}

/** What a grid tile shows for an app: the name, or the icon with its name under it. */
@Composable
private fun AppGridTileBody(app: AppInfo, displayMode: AppListGridDisplayMode, style: HomeItemStyle) {
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
    dragKey: Any = "folder_${folder.id}",
) {
    var sheetOpen by remember { mutableStateOf(false) }
    var menuExpanded by remember { mutableStateOf(false) }
    LiftableCell(
        key = dragKey,
        modifier = modifier,
        copy = { Box(Modifier.fillMaxWidth().padding(vertical = 8.dp), contentAlignment = Alignment.Center) { FolderGridTileBody(folder, displayMode, style) } },
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .longPressReorderClickable(dragKey, onClick = { sheetOpen = true }, onLongPress = { menuExpanded = true })
                .testTag(homeTag("home_folder_grid_tile_${folder.id}"))
                .padding(vertical = 8.dp),
            contentAlignment = Alignment.Center,
        ) {
            FolderGridTileBody(folder, displayMode, style)
            if (sheetOpen) FolderItemSheet(folder, { sheetOpen = false }, actions, style)
            FolderItemMenu(folder, menuExpanded, { menuExpanded = false }, actions)
        }
    }
}

/** What a grid tile shows for a folder: its name, or its glyph with the name under it. */
@Composable
private fun FolderGridTileBody(folder: Folder, displayMode: AppListGridDisplayMode, style: HomeItemStyle) {
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
}
