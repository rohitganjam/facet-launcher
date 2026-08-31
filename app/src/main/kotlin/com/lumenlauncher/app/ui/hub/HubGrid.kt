package com.lumenlauncher.app.ui.hub

import android.appwidget.AppWidgetHostView
import android.content.Context
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp

private val HUB_GRID_GAP = 8.dp
private val HUB_GRID_HORIZONTAL_PADDING = 24.dp

/** README `4a` — "Six rows fit the screen"; the grid still scrolls past that for however many more are placed. */
private const val HUB_VISIBLE_ROWS = 6

/**
 * Hand-rolled absolute-positioned grid, **not** `LazyVerticalGrid` — `GridCells`' own `span` API
 * only spans columns, not rows, which doesn't fit widgets needing both a `colSpan` and `rowSpan`
 * (e.g. "Month 2×2"). The grid is hard-capped at [com.lumenlauncher.app.domain.HUB_MAX_ROWS]
 * rows/[com.lumenlauncher.app.domain.HUB_MAX_WIDGETS] widgets, so lazy recycling buys nothing
 * meaningful — a deliberate, scoped departure from `CLAUDE.md`'s usual `LazyVerticalGrid`
 * convention (see chat history). Transparent background — the Hub sits directly on Home/the
 * wallpaper, same as every other Home-surface composable, not an opaque `Surface` panel.
 */
@Composable
fun HubGrid(
    widgets: List<HubWidgetUi>,
    columns: Int,
    createHostView: (Context, Int) -> AppWidgetHostView?,
    onRemoveOrphan: (Int) -> Unit,
    onKeepOrphanSpace: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    BoxWithConstraints(modifier = modifier.fillMaxSize()) {
        val availableWidth = maxWidth - HUB_GRID_HORIZONTAL_PADDING * 2
        val cellWidth = ((availableWidth - HUB_GRID_GAP * (columns - 1)) / columns).coerceAtLeast(0.dp)
        val rowHeight = ((maxHeight - HUB_GRID_GAP * (HUB_VISIBLE_ROWS - 1)) / HUB_VISIBLE_ROWS).coerceAtLeast(0.dp)
        val maxRow = widgets.maxOfOrNull { it.row + it.rowSpan } ?: 0
        val contentHeight = (rowHeight * maxRow + HUB_GRID_GAP * (maxRow - 1).coerceAtLeast(0)).coerceAtLeast(maxHeight)

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(contentHeight)
                .testTag("hub_grid")
                .verticalScroll(rememberScrollState())
                .padding(horizontal = HUB_GRID_HORIZONTAL_PADDING),
        ) {
            widgets.forEach { widget ->
                val tileWidth = cellWidth * widget.colSpan + HUB_GRID_GAP * (widget.colSpan - 1)
                val tileHeight = rowHeight * widget.rowSpan + HUB_GRID_GAP * (widget.rowSpan - 1)
                Box(
                    modifier = Modifier
                        // offset must precede size/fillMaxSize for a node that also carries a
                        // testTag — otherwise boundsInRoot doesn't reflect the shift even though
                        // rendering/hit-testing are correct (confirmed empirically; see chat history).
                        .offset(x = (cellWidth + HUB_GRID_GAP) * widget.col, y = (rowHeight + HUB_GRID_GAP) * widget.row)
                        .size(width = tileWidth, height = tileHeight)
                        .testTag("hub_widget_tile_${widget.appWidgetId}"),
                ) {
                    if (widget.isOrphaned) {
                        OrphanedWidgetTile(
                            providerLabel = widget.providerLabel,
                            onRemove = { onRemoveOrphan(widget.appWidgetId) },
                            onKeepSpace = { onKeepOrphanSpace(widget.appWidgetId) },
                        )
                    } else {
                        HubWidgetTile(appWidgetId = widget.appWidgetId, createHostView = createHostView)
                    }
                }
            }
        }
    }
}
