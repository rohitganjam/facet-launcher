package com.facetlauncher.app.ui.hub

import androidx.compose.ui.unit.DpSize
import com.facetlauncher.app.ui.nativewidget.NativeWidgetEnvironment
import com.facetlauncher.app.data.model.NativeWidgetRef
import com.facetlauncher.app.ui.nativewidget.NativeWidget
import com.facetlauncher.app.domain.minSpan
import android.appwidget.AppWidgetHostView
import android.content.Context
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.VectorConverter
import androidx.compose.animation.core.spring
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.scrollBy
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.State
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
import androidx.compose.ui.hapticfeedback.HapticFeedback
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.round
import com.facetlauncher.app.R
import com.facetlauncher.app.ui.components.ThemedDropdownMenu
import com.facetlauncher.app.ui.components.ThemedDropdownMenuItem
import com.facetlauncher.app.ui.theme.Accent
import kotlinx.collections.immutable.ImmutableList
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

private val HUB_LANDSCAPE_VERTICAL_PADDING = 8.dp

/** What the landscape grid needs from its screen, bundled so the pieces below take one argument. */
private class LandscapeCallbacks(
    val createHostView: (Context, Int) -> AppWidgetHostView?,
    val onWidgetSizeChange: (appWidgetId: Int, widthDp: Int, heightDp: Int) -> Unit,
    val onRemoveOrphan: (Int) -> Unit,
    val onKeepOrphanSpace: (Int) -> Unit,
    val onWidgetDrop: (appWidgetId: Int, dropRow: Int, dropCol: Int) -> Unit,
    val onWidgetDroppedOnTrash: (Int) -> Unit,
    val onWidgetResize: (appWidgetId: Int, colSpan: Int, rowSpan: Int) -> Unit,
    val nativeWidgets: NativeWidgetEnvironment,
)

/** The widgets as one stable value, so the pieces below can take them without an unstable `List` parameter. */
@Immutable
private class LandscapeWidgets(val items: List<HubLandscapeWidgetUi>)

/** Pixel measurements the gestures work in, fixed for one layout pass. */
private class LandscapeGeometry(
    val rows: Int,
    val pitchPx: Float,
    val gapPx: Float,
    val viewportWidthPx: Float,
    val edgeZonePx: Float,
) {
    /** The grid a resize works on: square cells and as many rows as show. */
    val resizeGrid: HubResizeGrid get() = resizeGridFor(columns = 1)

    /** A resize grid whose right limit is [widget]'s own: the saved size can't be wider than the Hub's columns allow from its portrait column. */
    fun resizeGridFor(widget: HubLandscapeWidgetUi): HubResizeGrid = resizeGridFor(columns = widget.col + widget.maxColSpan)

    private fun resizeGridFor(columns: Int) = HubResizeGrid(pitchPx - gapPx, pitchPx - gapPx, gapPx, columns, rows)

    /** The cell a widget dragged by [dragOffsetPx] would drop onto. */
    fun dropCellFor(widget: HubLandscapeWidgetUi, dragOffsetPx: Offset): HubCell =
        hubLandscapeDropCell(widget.row, widget.col, widget.rowSpan, dragOffsetPx.x, dragOffsetPx.y, pitchPx, pitchPx, rows)

    /** Which way to scroll (1 right, -1 left, 0 not) for something spanning [leftPx] (or only its right edge) to [rightPx], given where the viewport is. */
    fun edgeScrollDirection(scrollState: ScrollState, leftPx: Float?, rightPx: Float): Int {
        val viewportLeft = scrollState.value.toFloat()
        val viewportRight = viewportLeft + viewportWidthPx
        return when {
            rightPx > viewportRight - edgeZonePx && scrollState.value < scrollState.maxValue -> 1
            leftPx != null && leftPx < viewportLeft + edgeZonePx && scrollState.value > 0 -> -1
            else -> 0
        }
    }
}

/** A resize released on [colSpan] × [rowSpan] while the widget was saved on [from]. */
private data class CommittedLandscapeResize(val appWidgetId: Int, val from: HubSpan, val colSpan: Int, val rowSpan: Int)

private val HubLandscapeWidgetUi.span get() = HubSpan(row, col, colSpan, rowSpan)
private val HubLandscapeWidgetUi.minColSpan get() = nativeWidgetType?.minSpan()?.cols ?: 1
private val HubLandscapeWidgetUi.minRowSpan get() = nativeWidgetType?.minSpan()?.rows ?: 1

/** Everything the landscape grid is doing right now (a grab, a resize, an open menu), and the rules for changing it. */
@Stable
private class LandscapeGridState(val scrollState: ScrollState) {
    var grabbedId by mutableStateOf<Int?>(null)
    var dragOffsetPx by mutableStateOf(Offset.Zero)
    var dropCell by mutableStateOf<HubCell?>(null)
    var resizingId by mutableStateOf<Int?>(null)
    var resizeDeltaPx by mutableStateOf(Offset.Zero)
    var menuForId by mutableStateOf<Int?>(null)

    /** -1, 0 or 1: which way a widget held near a viewport edge is scrolling the grid. */
    var autoScrollDirection by mutableIntStateOf(0)

    val isBusy: Boolean get() = grabbedId != null || resizingId != null

    /** Whether a resize handle is being dragged right now. */
    var resizeDragging by mutableStateOf(false)

    /** The resizing widget's drawn box: it follows the finger while a handle is dragged, then glides into its cells. */
    val resizeRectPx = Animatable(Rect.Zero, Rect.VectorConverter)

    /** The widget drawn from [resizeRectPx] (set once that holds its rect), kept until it settles after resize mode ends. */
    var resizeVisualId by mutableStateOf<Int?>(null)

    /** The size a resize was released on, shown until the saved size changes or [RESIZE_SAVE_GRACE_MS] passes. */
    var committedResize by mutableStateOf<CommittedLandscapeResize?>(null)

    /** The cells the widget takes: under the finger while dragging, then the released size until it's saved. */
    fun liveSpan(widget: HubLandscapeWidgetUi, geometry: LandscapeGeometry): HubSpan = when {
        widget.appWidgetId != resizingId && widget.appWidgetId != resizeVisualId -> widget.span
        resizeDragging && widget.appWidgetId == resizingId ->
            geometry.resizeGridFor(widget).snappedSpan(widget.span, ResizeEdge.END, ResizeEdge.END, resizeDeltaPx, widget.minColSpan, widget.minRowSpan)
        else -> committedResize?.takeIf { it.appWidgetId == widget.appWidgetId }
            ?.let { widget.span.copy(colSpan = it.colSpan, rowSpan = it.rowSpan.coerceAtMost(maxOf(1, geometry.rows - widget.row))) }
            ?: widget.span
    }

    fun liveColSpan(widget: HubLandscapeWidgetUi, geometry: LandscapeGeometry): Int = liveSpan(widget, geometry).colSpan

    fun liveRowSpan(widget: HubLandscapeWidgetUi, geometry: LandscapeGeometry): Int = liveSpan(widget, geometry).rowSpan

    /** The box under the finger while a handle is dragged, `null` otherwise. */
    fun freeRect(widget: HubLandscapeWidgetUi, geometry: LandscapeGeometry): Rect? =
        if (resizeDragging && widget.appWidgetId == resizingId) {
            geometry.resizeGridFor(widget).freeRect(widget.span, ResizeEdge.END, ResizeEdge.END, resizeDeltaPx, widget.minColSpan, widget.minRowSpan)
        } else {
            null
        }

    /** The box a widget is drawn in while resizing or settling from a resize, in px; `null` draws its cells. */
    fun resizeBox(widget: HubLandscapeWidgetUi, geometry: LandscapeGeometry): Rect? =
        freeRect(widget, geometry) ?: resizeRectPx.value.takeIf { widget.appWidgetId == resizeVisualId && grabbedId != widget.appWidgetId }

    fun grab(id: Int) {
        resizingId = null
        resizeVisualId = null
        committedResize = null
        resizeDragging = false
        resizeDeltaPx = Offset.Zero
        grabbedId = id
        dragOffsetPx = Offset.Zero
    }

    /** Moves the grabbed widget by [delta], then works out where it would drop and whether it is near an edge to scroll. */
    fun dragBy(delta: Offset, widget: HubLandscapeWidgetUi, geometry: LandscapeGeometry) {
        dragOffsetPx += delta
        val left = geometry.pitchPx * widget.col + dragOffsetPx.x
        val right = left + geometry.pitchPx * widget.colSpan - geometry.gapPx
        autoScrollDirection = geometry.edgeScrollDirection(scrollState, left, right)
        dropCell = geometry.dropCellFor(widget, dragOffsetPx)
    }

    fun endDrag() {
        grabbedId = null
        dragOffsetPx = Offset.Zero
        autoScrollDirection = 0
        dropCell = null
    }

    fun openMenu(id: Int) {
        grabbedId = null
        menuForId = id
    }

    /** Starts resize mode once the drawn box holds the widget's rect, so it never shows a stale one. */
    suspend fun startResize(widget: HubLandscapeWidgetUi, geometry: LandscapeGeometry) {
        menuForId = null
        resizingId = widget.appWidgetId
        resizeDeltaPx = Offset.Zero
        resizeRectPx.snapTo(geometry.resizeGrid.rectOf(widget.span))
        resizeVisualId = widget.appWidgetId
    }

    fun cancelResize() {
        if (resizingId != null) resizingId = null
    }

    /** One resize drag step; a growing right edge near the viewport edge scrolls the grid. */
    fun resizeBy(delta: Offset, widget: HubLandscapeWidgetUi, geometry: LandscapeGeometry) {
        resizeDragging = true
        resizeDeltaPx += delta
        if (delta.x == 0f) return
        val right = freeRect(widget, geometry)?.right ?: return
        autoScrollDirection = geometry.edgeScrollDirection(scrollState, null, right)
    }

    /** Ends a handle drag on the cells under the finger ([onCommit]); the box glides there from where the finger left it. */
    suspend fun endResizeDrag(widget: HubLandscapeWidgetUi, geometry: LandscapeGeometry, onCommit: (colSpan: Int, rowSpan: Int) -> Unit) {
        val free = freeRect(widget, geometry)
        if (free != null) {
            val target = liveSpan(widget, geometry)
            onCommit(target.colSpan, target.rowSpan)
            resizeRectPx.snapTo(free)
            committedResize = CommittedLandscapeResize(widget.appWidgetId, from = widget.span, colSpan = target.colSpan, rowSpan = target.rowSpan)
        }
        resizeDragging = false
        resizeDeltaPx = Offset.Zero
        autoScrollDirection = 0
    }

    /** Scrolling must also move whatever is being dragged, or it would slide away from a stationary finger. */
    fun nudgeBy(scrolledPx: Float) {
        if (grabbedId != null) {
            dragOffsetPx += Offset(scrolledPx, 0f)
        } else if (resizingId != null) {
            resizeDeltaPx += Offset(scrolledPx, 0f)
        }
    }
}

/**
 * The Hub in landscape: a grid of fixed-size cells with as many rows as fit the height, growing
 * to the right and scrolling sideways. Widgets keep their size in cells and sit where
 * [com.facetlauncher.app.domain.LandscapeHubUseCase] packs them; this screen only reports how many
 * rows fit ([onRowsChange]) and what the user does.
 *
 * The gestures match [HubGrid]'s: a long press then drag lifts a widget and drops it into the flow
 * at the cell it lands on ([onWidgetDrop], which reorders, since landscape positions follow the
 * order); a long press released in place opens a menu to resize or remove. A resize here has a
 * handle on the right and bottom edges only (the top-left corner is where the flow put it), and
 * its new size is shared with portrait. Dragging near the left or right edge scrolls the grid.
 */
@Composable
fun HubLandscapeGrid(
    widgets: ImmutableList<HubLandscapeWidgetUi>,
    createHostView: (Context, Int) -> AppWidgetHostView?,
    onWidgetSizeChange: (appWidgetId: Int, widthDp: Int, heightDp: Int) -> Unit,
    onRowsChange: (Int) -> Unit,
    onRemoveOrphan: (Int) -> Unit,
    onKeepOrphanSpace: (Int) -> Unit,
    onWidgetDrop: (appWidgetId: Int, dropRow: Int, dropCol: Int) -> Unit,
    onWidgetDroppedOnTrash: (Int) -> Unit,
    onWidgetResize: (appWidgetId: Int, colSpan: Int, rowSpan: Int) -> Unit,
    modifier: Modifier = Modifier,
    nativeWidgets: NativeWidgetEnvironment = NativeWidgetEnvironment(),
) {
    val density = LocalDensity.current
    val scrollState = rememberScrollState()
    val gridState = remember(scrollState) { LandscapeGridState(scrollState) }
    val currentOnRowsChange by rememberUpdatedState(onRowsChange)
    val callbacks = remember(
        createHostView, onWidgetSizeChange, onRemoveOrphan, onKeepOrphanSpace, onWidgetDrop, onWidgetDroppedOnTrash, onWidgetResize, nativeWidgets,
    ) {
        LandscapeCallbacks(
            createHostView, onWidgetSizeChange, onRemoveOrphan, onKeepOrphanSpace, onWidgetDrop, onWidgetDroppedOnTrash, onWidgetResize, nativeWidgets,
        )
    }
    AutoScrollEffect(gridState)

    BoxWithConstraints(modifier = modifier.fillMaxSize()) {
        val rows = hubLandscapeRows(maxHeight - HUB_LANDSCAPE_VERTICAL_PADDING * 2, gap = HUB_GRID_GAP)
        LaunchedEffect(rows) { currentOnRowsChange(rows) }
        val geometry = LandscapeGeometry(
            rows = rows,
            pitchPx = with(density) { (HUB_LANDSCAPE_CELL_SIZE + HUB_GRID_GAP).toPx() },
            gapPx = with(density) { HUB_GRID_GAP.toPx() },
            viewportWidthPx = with(density) { maxWidth.toPx() },
            edgeZonePx = with(density) { AUTO_SCROLL_EDGE_ZONE.toPx() },
        )
        // One spare column to drop or resize into, and a second while something is being moved.
        val columnsInUse = widgets.maxOfOrNull { it.col + it.colSpan } ?: 0
        val contentColumns = columnsInUse + if (gridState.isBusy) 2 else 1
        // Read here: inside the nested Box this scope's maxWidth is out of reach.
        // The side padding sits inside the scroll area, so the floor leaves it out: otherwise the grid always scrolls by twice the padding.
        val contentWidth = hubLandscapeSpanSize(contentColumns, gap = HUB_GRID_GAP).coerceAtLeast(maxWidth - HUB_GRID_HORIZONTAL_PADDING * 2)
        Box(
            modifier = Modifier
                .fillMaxSize()
                .testTag("hub_landscape_grid")
                .horizontalScroll(scrollState)
                .padding(horizontal = HUB_GRID_HORIZONTAL_PADDING, vertical = HUB_LANDSCAPE_VERTICAL_PADDING),
        ) {
            LandscapeGridContent(
                widgets = remember(widgets) { LandscapeWidgets(widgets) },
                contentWidth = contentWidth,
                geometry = geometry,
                state = gridState,
                callbacks = callbacks,
            )
        }
    }
}

/** Scrolls the grid while a widget is held near its edge, and ends as soon as there is nothing left to scroll (so it never keeps the composition busy). */
@Composable
private fun AutoScrollEffect(state: LandscapeGridState) {
    LaunchedEffect(state.autoScrollDirection) {
        while (state.autoScrollDirection != 0) {
            withFrameNanos {}
            val consumed = state.scrollState.scrollBy(state.autoScrollDirection * AUTO_SCROLL_SPEED_PX_PER_FRAME)
            state.nudgeBy(consumed)
            if (consumed == 0f) break
        }
    }
}

@Composable
private fun LandscapeGridContent(
    widgets: LandscapeWidgets,
    contentWidth: Dp,
    geometry: LandscapeGeometry,
    state: LandscapeGridState,
    callbacks: LandscapeCallbacks,
) {
    Box(
        modifier = Modifier
            .width(contentWidth)
            .fillMaxHeight()
            // A tap anywhere in the grid while resizing cancels it. Unit-keyed because a per-tile gesture
            // clears the resizing widget mid-gesture, which must not detach this detector (as in HubGrid).
            .pointerInput(Unit) { detectTapGestures(onTap = { state.cancelResize() }) },
    ) {
        LandscapeResizeSettleEffects(widgets, geometry, state)
        widgets.items.forEach { widget ->
            key(widget.appWidgetId) { LandscapeTile(widget, geometry, state, callbacks) }
        }
        LandscapeResizeTarget(widgets.items.find { it.appWidgetId == state.resizingId }, geometry, state)
        LandscapeResizeHandles(widgets.items.find { it.appWidgetId == state.resizingId }, geometry, state, callbacks)
        LandscapeDropPreview(widgets.items.find { it.appWidgetId == state.grabbedId }, state)
    }
}

/** Eases a resized widget's box into its cells once released, and lets the released size go when the saved one changes. */
@Composable
private fun LandscapeResizeSettleEffects(widgets: LandscapeWidgets, geometry: LandscapeGeometry, state: LandscapeGridState) {
    val visual = widgets.items.find { it.appWidgetId == state.resizeVisualId }
    val target = visual?.let { geometry.resizeGrid.rectOf(state.liveSpan(it, geometry)) }
    LaunchedEffect(state.resizeVisualId, state.resizingId, target, state.resizeDragging) {
        if (target != null && !state.resizeDragging) {
            state.resizeRectPx.animateTo(target, RESIZE_SETTLE_SPEC)
            if (state.resizingId == null) state.resizeVisualId = null
        }
    }
    val committed = state.committedResize
    val saved = committed?.let { c -> widgets.items.find { it.appWidgetId == c.appWidgetId } }?.let { it.colSpan to it.rowSpan }
    LaunchedEffect(committed, saved) {
        if (committed == null) return@LaunchedEffect
        if (saved != committed.from.colSpan to committed.from.rowSpan) {
            state.committedResize = null
        } else {
            delay(RESIZE_SAVE_GRACE_MS)
            state.committedResize = null
        }
    }
}

/** While a handle is dragged, the cells the widget will take on release. */
@Composable
private fun LandscapeResizeTarget(widget: HubLandscapeWidgetUi?, geometry: LandscapeGeometry, state: LandscapeGridState) {
    if (widget == null || !state.resizeDragging) return
    val density = LocalDensity.current
    val rect = geometry.resizeGrid.rectOf(state.liveSpan(widget, geometry))
    val shape = RoundedCornerShape(12.dp)
    Box(
        modifier = Modifier
            .offset { rect.topLeft.round() }
            .size(width = with(density) { rect.width.toDp() }, height = with(density) { rect.height.toDp() })
            .testTag("hub_resize_target_preview")
            .background(Accent.copy(alpha = 0.18f), shape)
            .border(1.dp, Accent, shape),
    )
}

@Composable
private fun LandscapeTile(
    widget: HubLandscapeWidgetUi,
    geometry: LandscapeGeometry,
    state: LandscapeGridState,
    callbacks: LandscapeCallbacks,
) {
    val isGrabbed = widget.appWidgetId == state.grabbedId
    val isResizing = widget.appWidgetId == state.resizingId
    val tileWidth = hubLandscapeSpanSize(state.liveColSpan(widget, geometry), gap = HUB_GRID_GAP)
    val tileHeight = hubLandscapeSpanSize(state.liveRowSpan(widget, geometry), gap = HUB_GRID_GAP)
    val origin = Offset(geometry.pitchPx * widget.col, geometry.pitchPx * widget.row)
    val resizeBox = state.resizeBox(widget, geometry)
    val density = LocalDensity.current
    val boxWidth = resizeBox?.let { with(density) { it.width.toDp() } } ?: tileWidth
    val boxHeight = resizeBox?.let { with(density) { it.height.toDp() } } ?: tileHeight

    // A resting widget eases to its place when the flow changes, so after a drop it glides to its new cell.
    val animatedOrigin = remember { Animatable(origin, Offset.VectorConverter) }
    LaunchedEffect(origin, isGrabbed) {
        if (!isGrabbed) {
            animatedOrigin.animateTo(origin, spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessMedium))
        }
    }
    val scope = rememberCoroutineScope()
    val onDrop = rememberUpdatedState(callbacks.onWidgetDrop)

    Box(
        modifier = Modifier
            .offset {
                when {
                    isGrabbed -> origin + state.dragOffsetPx
                    resizeBox != null -> resizeBox.topLeft
                    else -> animatedOrigin.value
                }.round()
            }
            .size(width = boxWidth, height = boxHeight)
            .testTag("hub_widget_tile_${widget.appWidgetId}")
            .then(if (isResizing || widget.appWidgetId == state.menuForId) Modifier.border(1.dp, Accent) else Modifier)
            .then(if (isGrabbed) Modifier.liftedLook() else Modifier)
            .landscapeTileGesture(
                widget = widget,
                geometry = geometry,
                state = state,
                haptics = LocalHapticFeedback.current,
                onDrop = onDrop,
                // Start the glide from where the widget visibly is, not its old cell.
                onSettleAt = { visibleAt -> scope.launch { animatedOrigin.snapTo(visibleAt) } },
            ),
    ) {
        LandscapeTileContent(widget, tileWidth, tileHeight, DpSize(boxWidth, boxHeight), geometry, state, callbacks)
    }
}

/** The "picked up" look: a little bigger, raised and shadowed, so it is obvious the widget is in motion. */
private fun Modifier.liftedLook(): Modifier = graphicsLayer {
    scaleX = GRAB_SCALE
    scaleY = GRAB_SCALE
    shadowElevation = GRAB_ELEVATION.toPx()
    translationY = -GRAB_LIFT.toPx()
}

/** Long press then drag grabs the widget; a long press released in place opens its menu. The keys include position and size because the gesture closes over them. */
private fun Modifier.landscapeTileGesture(
    widget: HubLandscapeWidgetUi,
    geometry: LandscapeGeometry,
    state: LandscapeGridState,
    haptics: HapticFeedback,
    onDrop: State<(appWidgetId: Int, dropRow: Int, dropCol: Int) -> Unit>,
    onSettleAt: (Offset) -> Unit,
): Modifier = pointerInput(widget.appWidgetId, widget.row, widget.col, widget.colSpan, widget.rowSpan, geometry.rows, geometry.pitchPx) {
    val origin = Offset(geometry.pitchPx * widget.col, geometry.pitchPx * widget.row)
    detectGrabOrResizeGesture(
        onLongPressHold = {
            haptics.performHapticFeedback(HapticFeedbackType.LongPress)
            state.grab(widget.appWidgetId)
        },
        onDragStart = {},
        onDrag = { delta -> state.dragBy(delta, widget, geometry) },
        onDragEnd = {
            val target = geometry.dropCellFor(widget, state.dragOffsetPx)
            onDrop.value(widget.appWidgetId, target.row, target.col)
            onSettleAt(origin + state.dragOffsetPx)
            state.endDrag()
        },
        onDragCancel = {
            onSettleAt(origin + state.dragOffsetPx)
            state.endDrag()
        },
        onReleaseInPlace = { state.openMenu(widget.appWidgetId) },
        onTap = { state.cancelResize() },
    )
}

@Composable
private fun LandscapeTileContent(
    widget: HubLandscapeWidgetUi,
    tileWidth: Dp,
    tileHeight: Dp,
    boxSize: DpSize,
    geometry: LandscapeGeometry,
    state: LandscapeGridState,
    callbacks: LandscapeCallbacks,
) {
    val isResizing = widget.appWidgetId == state.resizingId
    if (widget.nativeWidgetType != null) {
        NativeWidget(
            type = widget.nativeWidgetType,
            size = boxSize,
            state = callbacks.nativeWidgets.state,
            actions = callbacks.nativeWidgets.actions,
            ref = NativeWidgetRef.Hub(widget.appWidgetId),
        )
    } else if (widget.isOrphaned) {
        OrphanedWidgetTile(
            providerLabel = widget.providerLabel,
            onRemove = { callbacks.onRemoveOrphan(widget.appWidgetId) },
            onKeepSpace = { callbacks.onKeepOrphanSpace(widget.appWidgetId) },
        )
    } else {
        HubWidgetTile(
            appWidgetId = widget.appWidgetId,
            tileWidth = tileWidth,
            tileHeight = tileHeight,
            isInteracting = widget.appWidgetId == state.grabbedId || isResizing,
            createHostView = callbacks.createHostView,
            onSizeChange = callbacks.onWidgetSizeChange,
        )
    }

    // While another widget is being resized, a tap here only cancels that, as in HubGrid.
    if (state.resizingId != null && !isResizing) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(state.resizingId) { detectTapGestures(onTap = { state.cancelResize() }) },
        )
    }

    LandscapeWidgetMenu(widget, geometry, state, callbacks)
}

/** The long-press menu of one landscape tile. */
@Composable
private fun LandscapeWidgetMenu(
    widget: HubLandscapeWidgetUi,
    geometry: LandscapeGeometry,
    state: LandscapeGridState,
    callbacks: LandscapeCallbacks,
) {
    val scope = rememberCoroutineScope()
    ThemedDropdownMenu(
        expanded = widget.appWidgetId == state.menuForId,
        onDismissRequest = { state.menuForId = null },
        shape = MaterialTheme.shapes.medium,
    ) {
        if (widget.nativeWidgetType != null) {
            ThemedDropdownMenuItem(
                label = stringResource(R.string.hub_grid_configure_widget),
                onClick = {
                    state.menuForId = null
                    callbacks.nativeWidgets.actions.onConfigure(NativeWidgetRef.Hub(widget.appWidgetId))
                },
            )
        }
        ThemedDropdownMenuItem(
            label = stringResource(R.string.hub_grid_resize_widget),
            onClick = { scope.launch { state.startResize(widget, geometry) } },
            enabled = !widget.isOrphaned,
        )
        ThemedDropdownMenuItem(
            label = stringResource(R.string.hub_grid_remove_widget),
            onClick = {
                state.menuForId = null
                callbacks.onWidgetDroppedOnTrash(widget.appWidgetId)
            },
            destructive = true,
        )
    }
}

/** The resize handles, in one overlay after every tile so they win hit-testing over neighbouring widgets. */
@Composable
private fun LandscapeResizeHandles(
    widget: HubLandscapeWidgetUi?,
    geometry: LandscapeGeometry,
    state: LandscapeGridState,
    callbacks: LandscapeCallbacks,
) {
    if (widget == null) return
    val density = LocalDensity.current
    val scope = rememberCoroutineScope()
    // On the box the widget is drawn in, so the handles stay under the finger.
    val box = state.resizeBox(widget, geometry) ?: geometry.resizeGrid.rectOf(state.liveSpan(widget, geometry))
    val endDrag: () -> Unit = {
        scope.launch { state.endResizeDrag(widget, geometry) { colSpan, rowSpan -> callbacks.onWidgetResize(widget.appWidgetId, colSpan, rowSpan) } }
    }
    Box(
        modifier = Modifier
            .offset { box.topLeft.round() }
            .size(width = with(density) { box.width.toDp() }, height = with(density) { box.height.toDp() }),
    ) {
        // The handles' gestures are launched once; WidgetResizeHandle keeps their callbacks current.
        WidgetResizeHandle(
            onDrag = { delta -> state.resizeBy(Offset(delta.x, 0f), widget, geometry) },
            onDragEnd = endDrag,
            testTag = "hub_resize_handle_right",
            modifier = Modifier.align(Alignment.CenterEnd).offset(x = WIDGET_RESIZE_HANDLE_TOUCH_TARGET_SIZE / 2),
        )
        WidgetResizeHandle(
            onDrag = { delta -> state.resizeBy(Offset(0f, delta.y), widget, geometry) },
            onDragEnd = endDrag,
            testTag = "hub_resize_handle_bottom",
            modifier = Modifier.align(Alignment.BottomCenter).offset(y = WIDGET_RESIZE_HANDLE_TOUCH_TARGET_SIZE / 2),
        )
    }
}

/** Where the grabbed widget would land if released now. */
@Composable
private fun LandscapeDropPreview(widget: HubLandscapeWidgetUi?, state: LandscapeGridState) {
    val cell = state.dropCell
    if (widget == null || cell == null) return
    val density = LocalDensity.current
    val shape = RoundedCornerShape(12.dp)
    Box(
        modifier = Modifier
            .offset {
                IntOffset(
                    with(density) { hubLandscapeCellOrigin(cell.col, gap = HUB_GRID_GAP).roundToPx() },
                    with(density) { hubLandscapeCellOrigin(cell.row, gap = HUB_GRID_GAP).roundToPx() },
                )
            }
            .size(width = hubLandscapeSpanSize(widget.colSpan, gap = HUB_GRID_GAP), height = hubLandscapeSpanSize(widget.rowSpan, gap = HUB_GRID_GAP))
            .testTag("hub_drop_target_preview")
            .background(Accent.copy(alpha = 0.18f), shape)
            .border(1.dp, Accent, shape),
    )
}
