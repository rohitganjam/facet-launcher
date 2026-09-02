package com.lumenlauncher.app.ui.hub

import android.appwidget.AppWidgetHostView
import android.content.Context
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.VectorConverter
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.scrollBy
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInRoot
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import com.lumenlauncher.app.domain.HUB_MAX_ROWS
import com.lumenlauncher.app.ui.components.ThemedDropdownMenu
import com.lumenlauncher.app.ui.components.ThemedDropdownMenuItem
import com.lumenlauncher.app.ui.theme.Accent
import kotlin.math.roundToInt
import kotlinx.coroutines.launch

private val HUB_GRID_GAP = 8.dp
private val HUB_GRID_HORIZONTAL_PADDING = 24.dp

/** README `4a` — "Six rows fit the screen"; the grid still scrolls past that for however many more are placed. */
private const val HUB_VISIBLE_ROWS = 6

/**
 * "Picked up" drag-lift treatment — a visible scale/shadow/upward nudge the instant a widget is
 * grabbed, before any drag movement, so it's obvious it's now in motion rather than just sitting
 * there (see chat history — this was previously too subtle to notice on a real device).
 */
private val GRAB_ELEVATION = 16.dp
private val GRAB_LIFT = 10.dp
private const val GRAB_SCALE = 1.08f

/** Dragging a grabbed widget within this distance of the viewport's top/bottom edge auto-scrolls the grid, so a widget can be placed below (or above) whatever's currently on screen. */
private val AUTO_SCROLL_EDGE_ZONE = 48.dp
private const val AUTO_SCROLL_SPEED_PX_PER_FRAME = 14f

/** Which side of a widget a resize handle is anchored to — START (left/top) moves that edge, and with it row/col; END (right/bottom) only grows/shrinks span. */
private enum class ResizeEdge { START, END }

/**
 * Hand-rolled absolute-positioned grid, **not** `LazyVerticalGrid` — `GridCells`' own `span` API
 * only spans columns, not rows, which doesn't fit widgets needing both a `colSpan` and `rowSpan`
 * (e.g. "Month 2×2"). The grid is hard-capped at [com.lumenlauncher.app.domain.HUB_MAX_ROWS]
 * rows/[com.lumenlauncher.app.domain.HUB_MAX_WIDGETS] widgets, so lazy recycling buys nothing
 * meaningful — a deliberate, scoped departure from `CLAUDE.md`'s usual `LazyVerticalGrid`
 * convention (see chat history). This grid itself has no background of its own — `HubScreen`'s
 * root already carries the Drawer's own translucent scrim behind it.
 *
 * Long-pressing a tile has two outcomes (see [detectGrabOrResizeGesture]): dragging afterward
 * grabs it (move to a new cell) — see [GRAB_SCALE]/[GRAB_ELEVATION]/[GRAB_LIFT] for the "picked up"
 * feedback, and the drop-target preview rendered below the widgets for where it'll land;
 * releasing without moving instead opens a context menu with options to resize or remove.
 * one [WidgetResizeHandle] per edge (four total), each straddling its own edge half-on-half-off.
 * Dragging the right/bottom handles only changes span (top-left cell fixed); dragging left/top
 * changes span **and** shifts that edge's row/col, so the opposite edge stays fixed instead —
 * both resolved by the same [com.lumenlauncher.app.domain.ResizeWidgetUseCase], which validates
 * whatever (row, col, colSpan, rowSpan) rectangle it's given regardless of which edge moved. A
 * tap anywhere in the grid while resizing cancels it without committing. While anything is
 * grabbed or resizing, `change.consume()` inside that tile's own gesture keeps the ancestor
 * Home<->Hub horizontal swipe-close gesture (`HomeDrawerRoute.kt`) from also reacting to the same
 * pointer stream — ordinary Compose consumption arbitration, no explicit wiring needed.
 */
@Composable
fun HubGrid(
    widgets: List<HubWidgetUi>,
    columns: Int,
    createHostView: (Context, Int) -> AppWidgetHostView?,
    onRemoveOrphan: (Int) -> Unit,
    onKeepOrphanSpace: (Int) -> Unit,
    onWidgetDropped: (appWidgetId: Int, row: Int, col: Int, colSpan: Int, rowSpan: Int) -> Unit,
    onWidgetDroppedOnTrash: (Int) -> Unit,
    canResizeTo: (appWidgetId: Int, row: Int, col: Int, colSpan: Int, rowSpan: Int) -> Boolean,
    onWidgetResized: (appWidgetId: Int, row: Int, col: Int, colSpan: Int, rowSpan: Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    val density = LocalDensity.current
    var grabbedAppWidgetId by remember { mutableStateOf<Int?>(null) }
    var dragOffsetPx by remember { mutableStateOf(Offset.Zero) }
    var resizingAppWidgetId by remember { mutableStateOf<Int?>(null) }
    var showContextMenuForAppWidgetId by remember { mutableStateOf<Int?>(null) }
    var resizeDeltaPx by remember { mutableStateOf(Offset.Zero) }
    var grabbedTileCenterInRoot by remember { mutableStateOf<Offset?>(null) }
    // Live preview of where a grabbed widget would land if released right now.
    var dropTargetRow by remember { mutableStateOf<Int?>(null) }
    var dropTargetCol by remember { mutableStateOf<Int?>(null) }
    val currentOnWidgetDropped = rememberUpdatedState(onWidgetDropped)
    val currentCanResizeTo = rememberUpdatedState(canResizeTo)
    val currentOnWidgetResized = rememberUpdatedState(onWidgetResized)
    val scrollState = rememberScrollState()
    // -1/0/1 — which way (if any) a grabbed widget parked near the viewport's edge is auto-scrolling.
    var autoScrollDirection by remember { mutableIntStateOf(0) }

    // Scrolling while grabbed must also nudge dragOffsetPx by exactly what scrolled, or the widget
    // would visually slide away from the finger as new content-space is revealed underneath it —
    // its own offset is content-relative, so keeping it under a stationary finger during a scroll
    // means growing that offset by the same amount the viewport just moved.
    //
    // Keyed on autoScrollDirection (not Unit) and only looping while it's non-zero: an
    // unconditional `while (true) { withFrameNanos {} }` never completes, which — beyond wasting a
    // frame callback every frame for the Hub's entire visible lifetime, not just while dragging —
    // means Compose's own idle detection (and Espresso's IdlingResource bridge, which instrumented
    // tests rely on) never sees this composition go idle. Restarting per direction change and
    // exiting the loop the instant it's back to 0 keeps this coroutine alive only when it's doing
    // real work.
    LaunchedEffect(autoScrollDirection) {
        while (autoScrollDirection != 0) {
            withFrameNanos {}
            val consumed = scrollState.scrollBy(autoScrollDirection * AUTO_SCROLL_SPEED_PX_PER_FRAME)
            // Whichever gesture is actually driving the scroll gets nudged by exactly what
            // scrolled, so it stays under a stationary finger — a move follows dragOffsetPx, a
            // resize follows resizeDeltaPx (both are content-relative, same reasoning as the
            // move-only comment this used to be).
            if (grabbedAppWidgetId != null) {
                dragOffsetPx += Offset(0f, consumed)
            } else if (resizingAppWidgetId != null) {
                resizeDeltaPx += Offset(0f, consumed)
            }
            // Already at a scroll boundary (e.g. a widget starting at row 0 sits inside the
            // top-edge zone the instant it's grabbed, with nowhere left to scroll up into) —
            // nothing else will change autoScrollDirection until the next onGrabDrag/onDrag, so
            // without this the loop spins forever doing nothing, the "never idle" bug above.
            if (consumed == 0f) break
        }
    }

    BoxWithConstraints(modifier = modifier.fillMaxSize()) {
        val availableWidth = maxWidth - HUB_GRID_HORIZONTAL_PADDING * 2
        val cellWidth = ((availableWidth - HUB_GRID_GAP * (columns - 1)) / columns).coerceAtLeast(0.dp)
        // Hub grid cells are designed to be square (README F5 update) — the same cellWidth
        // applies to both axes, rather than rowHeight being fixed to a viewport fraction.
        val rowHeight = cellWidth
        val cellWidthPx = with(density) { cellWidth.toPx() }
        val rowHeightPx = with(density) { rowHeight.toPx() }
        val maxRow = widgets.maxOfOrNull { it.row + it.rowSpan } ?: 0
        // While a widget is grabbed OR being resized, the scroll range extends all the way to the
        // grid's own row cap — not just as far as existing widgets reach — so there's always room
        // to auto-scroll into. In normal scroll mode, we always ensure there's at least one
        // additional empty row at the bottom to make the bottom-most element easier to grab
        // and resize from its bottom edge.
        val effectiveMaxRow = if (grabbedAppWidgetId != null || resizingAppWidgetId != null) {
            HUB_MAX_ROWS
        } else {
            (maxRow + 1).coerceAtMost(HUB_MAX_ROWS)
        }
        val contentHeight = (rowHeight * effectiveMaxRow + HUB_GRID_GAP * (effectiveMaxRow - 1).coerceAtLeast(0)).coerceAtLeast(maxHeight)
        val viewportHeightPx = with(density) { maxHeight.toPx() }
        val autoScrollEdgeZonePx = with(density) { AUTO_SCROLL_EDGE_ZONE.toPx() }

        // The scrollable container itself must stay viewport-sized (fillMaxSize) — giving IT the
        // full contentHeight (as a previous version of this code did) makes its own measured size
        // already equal its content size, so verticalScroll sees no overflow and never actually
        // scrolls; widgets past the visible rows just get clipped by the outer constraint instead.
        // Only the nested Box below — the one actually holding the absolute-positioned tiles — gets
        // sized to contentHeight, which can exceed the viewport; that's what verticalScroll measures
        // against to find the real overflow (see chat history — this was a real, long-latent bug,
        // never caught earlier because no widget list had extended past the first 6 rows).
        Box(
            modifier = Modifier
                .fillMaxSize()
                .testTag("hub_grid")
                .verticalScroll(scrollState)
                .padding(horizontal = HUB_GRID_HORIZONTAL_PADDING),
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(contentHeight)
                    // A tap anywhere in the grid while resizing — empty space or the widget
                    // itself — cancels resize mode without committing. A quick tap (no
                    // long-press) never gets consumed by the per-tile gesture below (it only
                    // consumes once a long-press is recognized), so it reaches this detector.
                    .then(
                        if (resizingAppWidgetId != null) {
                            Modifier.pointerInput(resizingAppWidgetId) {
                                detectTapGestures(onTap = { resizingAppWidgetId = null })
                            }
                        } else {
                            Modifier
                        },
                    ),
            ) {
                widgets.forEach { widget ->
                    key(widget.appWidgetId) {
                        val isGrabbed = widget.appWidgetId == grabbedAppWidgetId
                        val isResizing = widget.appWidgetId == resizingAppWidgetId
                        val baseColSpan = widget.colSpan
                        val baseRowSpan = widget.rowSpan

                        // Which edge (if any) is currently being dragged, per axis — a left/top
                        // drag shifts that edge's row/col to keep the OPPOSITE edge fixed; a
                        // right/bottom drag only changes span, anchored at the existing row/col.
                        var horizontalResizeEdge by remember { mutableStateOf<ResizeEdge?>(null) }
                        var verticalResizeEdge by remember { mutableStateOf<ResizeEdge?>(null) }

                        val liveColSpan = if (isResizing) {
                            when (horizontalResizeEdge) {
                                ResizeEdge.END -> resizedSpan(baseColSpan, resizeDeltaPx.x, cellWidthPx)
                                ResizeEdge.START -> resizedSpan(baseColSpan, -resizeDeltaPx.x, cellWidthPx)
                                null -> baseColSpan
                            }
                        } else {
                            baseColSpan
                        }
                        val liveRowSpan = if (isResizing) {
                            when (verticalResizeEdge) {
                                ResizeEdge.END -> resizedSpan(baseRowSpan, resizeDeltaPx.y, rowHeightPx)
                                ResizeEdge.START -> resizedSpan(baseRowSpan, -resizeDeltaPx.y, rowHeightPx)
                                null -> baseRowSpan
                            }
                        } else {
                            baseRowSpan
                        }
                        // A left/top drag keeps the opposite edge fixed by shrinking/growing the
                        // anchor cell by exactly however much the span itself shrank/grew.
                        val liveCol = if (isResizing && horizontalResizeEdge == ResizeEdge.START) {
                            widget.col + baseColSpan - liveColSpan
                        } else {
                            widget.col
                        }
                        val liveRow = if (isResizing && verticalResizeEdge == ResizeEdge.START) {
                            widget.row + baseRowSpan - liveRowSpan
                        } else {
                            widget.row
                        }

                        val tileWidth = cellWidth * liveColSpan + HUB_GRID_GAP * (liveColSpan - 1)
                        val tileHeight = rowHeight * liveRowSpan + HUB_GRID_GAP * (liveRowSpan - 1)
                        val baseOffsetX = (cellWidth + HUB_GRID_GAP) * widget.col
                        val baseOffsetY = (rowHeight + HUB_GRID_GAP) * widget.row
                        val resizeOffsetX = (cellWidth + HUB_GRID_GAP) * liveCol
                        val resizeOffsetY = (rowHeight + HUB_GRID_GAP) * liveRow

                        // Animates a non-grabbed, non-resizing widget's own position whenever its
                        // (row, col) changes — most notably when ResolveWidgetDropUseCase
                        // displaces it out of a drop's way, so it visibly slides into its new
                        // cell rather than teleporting there. Keyed on appWidgetId (via the outer
                        // key() above) so this survives list reordering.
                        val targetOffsetPx = Offset(baseOffsetX.toPx(density), baseOffsetY.toPx(density))
                        val animatedOffsetPx = remember { Animatable(targetOffsetPx, Offset.VectorConverter) }
                        val positionCoroutineScope = rememberCoroutineScope()
                        LaunchedEffect(targetOffsetPx, isGrabbed) {
                            if (!isGrabbed) {
                                animatedOffsetPx.animateTo(
                                    targetOffsetPx,
                                    animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessMedium),
                                )
                            }
                        }

                        Box(
                            modifier = Modifier
                                // offset must precede size/fillMaxSize/testTag for a node whose
                                // position needs to be trustworthy (boundsInRoot/onGloballyPositioned)
                                // — confirmed empirically; see chat history.
                                .offset {
                                    when {
                                        isGrabbed -> IntOffset(
                                            (baseOffsetX.toPx(density) + dragOffsetPx.x).roundToInt(),
                                            (baseOffsetY.toPx(density) + dragOffsetPx.y).roundToInt(),
                                        )
                                        isResizing -> IntOffset(resizeOffsetX.toPx(density).roundToInt(), resizeOffsetY.toPx(density).roundToInt())
                                        else -> IntOffset(animatedOffsetPx.value.x.roundToInt(), animatedOffsetPx.value.y.roundToInt())
                                    }
                                }
                                .size(width = tileWidth, height = tileHeight)
                                .testTag("hub_widget_tile_${widget.appWidgetId}")
                                // Resize mode's own footprint indicator — otherwise the exact cell
                                // bounds a resize is currently claiming aren't visible against the
                                // transparent Hub background, only the handles themselves are.
                                .then(if (isResizing) Modifier.border(1.dp, Accent) else Modifier)
                                .then(
                                    if (isGrabbed) {
                                        Modifier
                                            .graphicsLayer {
                                                scaleX = GRAB_SCALE
                                                scaleY = GRAB_SCALE
                                                shadowElevation = GRAB_ELEVATION.toPx()
                                                translationY = -GRAB_LIFT.toPx()
                                            }
                                            .onGloballyPositioned { coordinates ->
                                                val bounds = coordinates.positionInRoot()
                                                val size = coordinates.size
                                                grabbedTileCenterInRoot = Offset(bounds.x + size.width / 2f, bounds.y + size.height / 2f)
                                            }
                                    } else {
                                        Modifier
                                    },
                                )
                                // baseColSpan/baseRowSpan are in the key list deliberately: onGrabEnd
                                // below closes over them, and without a key change on resize, this
                                // coroutine wouldn't restart — so a move commit right after a resize
                                // would silently re-commit the widget's OLD (pre-resize) span,
                                // reverting it (see chat history).
                                .pointerInput(widget.appWidgetId, columns, cellWidthPx, rowHeightPx, baseColSpan, baseRowSpan) {
                                    detectGrabOrResizeGesture(
                                        onLongPressHold = {
                                            grabbedAppWidgetId = widget.appWidgetId
                                            dragOffsetPx = Offset.Zero
                                        },
                                        onDragStart = {
                                            // Elevation already handled by onLongPressHold.
                                        },
                                        onDrag = { delta ->
                                            dragOffsetPx += delta
                                            // Auto-scroll once the (content-relative) dragged position
                                            // gets within AUTO_SCROLL_EDGE_ZONE of either edge of the
                                            // viewport — lets a widget be dropped below/above whatever's
                                            // on screen now.
                                            val contentTopPx = baseOffsetY.toPx(density) + dragOffsetPx.y
                                            val contentBottomPx = contentTopPx + tileHeight.toPx(density)
                                            val viewportTopPx = scrollState.value.toFloat()
                                            val viewportBottomPx = viewportTopPx + viewportHeightPx
                                            // Guarded by scrollState.value so a widget starting at row 0
                                            // (already sitting inside the top-edge zone the instant it's
                                            // grabbed) doesn't set a direction with nowhere to scroll.
                                            autoScrollDirection = when {
                                                contentBottomPx > viewportBottomPx - autoScrollEdgeZonePx && scrollState.value < scrollState.maxValue -> 1
                                                contentTopPx < viewportTopPx + autoScrollEdgeZonePx && scrollState.value > 0 -> -1
                                                else -> 0
                                            }
                                            dropTargetCol = (widget.col + (dragOffsetPx.x / cellWidthPx).roundToInt()).coerceIn(0, columns - baseColSpan)
                                            dropTargetRow = (widget.row + (dragOffsetPx.y / rowHeightPx).roundToInt()).coerceAtLeast(0)
                                        },
                                        onDragEnd = {
                                            val targetCol = (widget.col + (dragOffsetPx.x / cellWidthPx).roundToInt()).coerceIn(0, columns - baseColSpan)
                                            val targetRow = (widget.row + (dragOffsetPx.y / rowHeightPx).roundToInt()).coerceAtLeast(0)
                                            currentOnWidgetDropped.value(widget.appWidgetId, targetRow, targetCol, baseColSpan, baseRowSpan)
                                            
                                            // Sync the animatable to exactly where the widget was visually
                                            // sitting (base + drag) before flipping isGrabbed off — without
                                            // this the next frame's animateTo would start from a stale
                                            // pre-grab value and visibly jump before easing.
                                            positionCoroutineScope.launch {
                                                animatedOffsetPx.snapTo(Offset(baseOffsetX.toPx(density) + dragOffsetPx.x, baseOffsetY.toPx(density) + dragOffsetPx.y))
                                            }
                                            grabbedAppWidgetId = null
                                            grabbedTileCenterInRoot = null
                                            dragOffsetPx = Offset.Zero
                                            autoScrollDirection = 0
                                            dropTargetRow = null
                                            dropTargetCol = null
                                        },
                                        onDragCancel = {
                                            positionCoroutineScope.launch {
                                                animatedOffsetPx.snapTo(Offset(baseOffsetX.toPx(density) + dragOffsetPx.x, baseOffsetY.toPx(density) + dragOffsetPx.y))
                                            }
                                            grabbedAppWidgetId = null
                                            grabbedTileCenterInRoot = null
                                            dragOffsetPx = Offset.Zero
                                            autoScrollDirection = 0
                                            dropTargetRow = null
                                            dropTargetCol = null
                                        },
                                        onReleaseInPlace = {
                                            grabbedAppWidgetId = null
                                            showContextMenuForAppWidgetId = widget.appWidgetId
                                        },
                                        onTap = { resizingAppWidgetId = null }
                                    )
                                },
                        ) {
                            if (widget.isOrphaned) {
                                OrphanedWidgetTile(
                                    providerLabel = widget.providerLabel,
                                    onRemove = { onRemoveOrphan(widget.appWidgetId) },
                                    onKeepSpace = { onKeepOrphanSpace(widget.appWidgetId) },
                                )
                            } else {
                                HubWidgetTile(
                                    appWidgetId = widget.appWidgetId,
                                    tileWidth = tileWidth,
                                    tileHeight = tileHeight,
                                    isInteracting = isGrabbed || isResizing,
                                    createHostView = createHostView
                                )
                            }

                            if (isResizing) {
                                // One handle per edge, each straddling its own edge half-on-half-off
                                // rather than sitting flush inside the widget. Right/bottom only
                                // change span (top-left cell fixed); left/top also shift that edge's
                                // row/col so the OPPOSITE edge stays fixed — see liveCol/liveRow above.
                                WidgetResizeHandle(
                                    onDrag = { delta -> horizontalResizeEdge = ResizeEdge.END; resizeDeltaPx += Offset(delta.x, 0f) },
                                    // WidgetResizeHandle's own pointerInput is Unit-keyed so a live
                                    // drag is never interrupted by recomposition — which also means
                                    // this lambda is captured exactly once and never gets a fresh
                                    // closure. Read resizeDeltaPx (a State, live through the stale
                                    // closure) and recompute the final rect here rather than closing
                                    // over liveColSpan/liveRowSpan (plain vals, frozen at that one
                                    // capture).
                                    onDragEnd = {
                                        val finalColSpan = resizedSpan(baseColSpan, resizeDeltaPx.x, cellWidthPx)
                                        if (currentCanResizeTo.value(widget.appWidgetId, widget.row, widget.col, finalColSpan, baseRowSpan)) {
                                            currentOnWidgetResized.value(widget.appWidgetId, widget.row, widget.col, finalColSpan, baseRowSpan)
                                        }
                                        resizingAppWidgetId = null
                                        resizeDeltaPx = Offset.Zero
                                    },
                                    testTag = "hub_resize_handle_right",
                                    modifier = Modifier.align(Alignment.CenterEnd).offset(x = WIDGET_RESIZE_HANDLE_SIZE / 2),
                                )
                                WidgetResizeHandle(
                                    onDrag = { delta -> horizontalResizeEdge = ResizeEdge.START; resizeDeltaPx += Offset(delta.x, 0f) },
                                    onDragEnd = {
                                        val finalColSpan = resizedSpan(baseColSpan, -resizeDeltaPx.x, cellWidthPx)
                                        val finalCol = widget.col + baseColSpan - finalColSpan
                                        if (currentCanResizeTo.value(widget.appWidgetId, widget.row, finalCol, finalColSpan, baseRowSpan)) {
                                            currentOnWidgetResized.value(widget.appWidgetId, widget.row, finalCol, finalColSpan, baseRowSpan)
                                        }
                                        resizingAppWidgetId = null
                                        resizeDeltaPx = Offset.Zero
                                    },
                                    testTag = "hub_resize_handle_left",
                                    modifier = Modifier.align(Alignment.CenterStart).offset(x = -WIDGET_RESIZE_HANDLE_SIZE / 2),
                                )
                                WidgetResizeHandle(
                                    onDrag = { delta ->
                                        verticalResizeEdge = ResizeEdge.END
                                        resizeDeltaPx += Offset(0f, delta.y)
                                        // Same edge-zone auto-scroll as a grab-move (see the
                                        // LaunchedEffect above), triggered by the LIVE growing
                                        // bottom edge instead of a drag position — resizeDeltaPx is
                                        // read fresh here (a State), unlike tileHeight/baseOffsetY
                                        // which are safe to read stale since row/col/rowHeight don't
                                        // change mid-resize.
                                        val liveSpan = resizedSpan(baseRowSpan, resizeDeltaPx.y, rowHeightPx)
                                        val contentBottomPx = baseOffsetY.toPx(density) + (rowHeight * liveSpan + HUB_GRID_GAP * (liveSpan - 1)).toPx(density)
                                        val viewportBottomPx = scrollState.value.toFloat() + viewportHeightPx
                                        autoScrollDirection = if (contentBottomPx > viewportBottomPx - autoScrollEdgeZonePx && scrollState.value < scrollState.maxValue) 1 else 0
                                    },
                                    onDragEnd = {
                                        val finalRowSpan = resizedSpan(baseRowSpan, resizeDeltaPx.y, rowHeightPx)
                                        if (currentCanResizeTo.value(widget.appWidgetId, widget.row, widget.col, baseColSpan, finalRowSpan)) {
                                            currentOnWidgetResized.value(widget.appWidgetId, widget.row, widget.col, baseColSpan, finalRowSpan)
                                        }
                                        resizingAppWidgetId = null
                                        resizeDeltaPx = Offset.Zero
                                        autoScrollDirection = 0
                                    },
                                    testTag = "hub_resize_handle_bottom",
                                    modifier = Modifier.align(Alignment.BottomCenter).offset(y = WIDGET_RESIZE_HANDLE_SIZE / 2),
                                )
                                WidgetResizeHandle(
                                    onDrag = { delta ->
                                        verticalResizeEdge = ResizeEdge.START
                                        resizeDeltaPx += Offset(0f, delta.y)
                                        val liveSpan = resizedSpan(baseRowSpan, -resizeDeltaPx.y, rowHeightPx)
                                        val liveRow = widget.row + baseRowSpan - liveSpan
                                        val contentTopPx = (liveRow * (rowHeightPx + HUB_GRID_GAP.toPx(density)))
                                        val viewportTopPx = scrollState.value.toFloat()
                                        autoScrollDirection = if (contentTopPx < viewportTopPx + autoScrollEdgeZonePx && scrollState.value > 0) -1 else 0
                                    },
                                    onDragEnd = {
                                        val finalRowSpan = resizedSpan(baseRowSpan, -resizeDeltaPx.y, rowHeightPx)
                                        val finalRow = widget.row + baseRowSpan - finalRowSpan
                                        if (currentCanResizeTo.value(widget.appWidgetId, finalRow, widget.col, baseColSpan, finalRowSpan)) {
                                            currentOnWidgetResized.value(widget.appWidgetId, finalRow, widget.col, baseColSpan, finalRowSpan)
                                        }
                                        resizingAppWidgetId = null
                                        resizeDeltaPx = Offset.Zero
                                        autoScrollDirection = 0
                                    },
                                    testTag = "hub_resize_handle_top",
                                    modifier = Modifier.align(Alignment.TopCenter).offset(y = -WIDGET_RESIZE_HANDLE_SIZE / 2),
                                )
                            }
                        }

                        ThemedDropdownMenu(
                            expanded = showContextMenuForAppWidgetId == widget.appWidgetId,
                            onDismissRequest = { showContextMenuForAppWidgetId = null },
                            shape = MaterialTheme.shapes.medium,
                        ) {
                            ThemedDropdownMenuItem(
                                label = "Resize widget",
                                onClick = {
                                    showContextMenuForAppWidgetId = null
                                    resizingAppWidgetId = widget.appWidgetId
                                    resizeDeltaPx = Offset.Zero
                                    horizontalResizeEdge = null
                                    verticalResizeEdge = null
                                },
                                enabled = !widget.isOrphaned,
                            )
                            ThemedDropdownMenuItem(
                                label = "Remove widget",
                                onClick = {
                                    showContextMenuForAppWidgetId = null
                                    onWidgetDroppedOnTrash(widget.appWidgetId)
                                },
                                destructive = true,
                            )
                        }
                    }
                }

                // Drop-target preview — the cell(s) a grabbed widget would land in if released
                // right now, so the user can see the target before committing (matches how other
                // launchers preview a drag's destination). Drawn after the widgets so it's not
                // hidden underneath any of them; the grabbed widget's own elevation still keeps
                // it visually on top of this.
                val previewWidget = widgets.find { it.appWidgetId == grabbedAppWidgetId }
                val previewRow = dropTargetRow
                val previewCol = dropTargetCol
                if (previewWidget != null && previewRow != null && previewCol != null) {
                    val previewWidth = cellWidth * previewWidget.colSpan + HUB_GRID_GAP * (previewWidget.colSpan - 1)
                    val previewHeight = rowHeight * previewWidget.rowSpan + HUB_GRID_GAP * (previewWidget.rowSpan - 1)
                    val previewOffsetX = (cellWidth + HUB_GRID_GAP) * previewCol
                    val previewOffsetY = (rowHeight + HUB_GRID_GAP) * previewRow
                    Box(
                        modifier = Modifier
                            .offset { IntOffset(previewOffsetX.toPx(density).roundToInt(), previewOffsetY.toPx(density).roundToInt()) }
                            .size(width = previewWidth, height = previewHeight)
                            .testTag("hub_drop_target_preview")
                            .background(Accent.copy(alpha = 0.18f), RoundedCornerShape(12.dp))
                            .border(1.dp, Accent, RoundedCornerShape(12.dp)),
                    )
                }
            }
        }
    }
}

private fun Dp.toPx(density: Density): Float = with(density) { this@toPx.toPx() }

/** Grows/shrinks by whole cells only, floored at 1 — matches how the commit-time span is computed. */
private fun resizedSpan(baseSpan: Int, deltaPx: Float, cellSizePx: Float): Int =
    (baseSpan + (deltaPx / cellSizePx).roundToInt()).coerceAtLeast(1)
