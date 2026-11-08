package com.facetlauncher.app.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.gestures.scrollBy
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import androidx.compose.foundation.LocalIndication
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.indication
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.PressInteraction
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.SnapshotStateMap
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.LayoutCoordinates
import androidx.compose.ui.layout.boundsInRoot
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.semantics.onClick
import androidx.compose.ui.semantics.onLongClick
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.zIndex
import kotlin.math.abs
import kotlinx.coroutines.launch

/** How far a picked-up cell grows. */
internal const val PICK_UP_SCALE = 1.12f

/** Where a dragged cell lands, and how its neighbours make room. Pure, so it is tested without a screen. */
internal object ReorderGeometry {
    /** The slot whose resting centre is nearest to where the dragged cell's centre now is. */
    fun targetIndex(centers: List<Float>, from: Int, offset: Float): Int {
        if (from !in centers.indices) return from
        val moved = centers[from] + offset
        return centers.indices.minByOrNull { abs(centers[it] - moved) } ?: from
    }

    /** How far the cell at [index] slides to make room while [from] is dragged over [to]. */
    fun neighbourShift(centers: List<Float>, index: Int, from: Int, to: Int): Float = when {
        index == from || from == to -> 0f
        from < to && index in (from + 1)..to -> centers[index - 1] - centers[index]
        from > to && index in to until from -> centers[index + 1] - centers[index]
        else -> 0f
    }

    fun <T> move(list: List<T>, from: Int, to: Int): List<T> {
        if (from == to || from !in list.indices || to !in list.indices) return list
        return list.toMutableList().apply { add(to, removeAt(from)) }
    }
}

/**
 * The picked-up/dragged state for one reorderable run of cells (Home's list, or the Dock). [keys] is the
 * cells' current order; [onMove] saves a finished drag.
 */
/** What a long-press cell talks to while it is picked up and dragged: Home's list or Dock ([PickUpReorderState]) or its grid ([GridDragState]). */
interface PickUpDragHost {
    fun pickUp(key: Any)
    fun startDrag()
    fun dragTo(travel: Offset)
    fun release()
}

@Stable
class PickUpReorderState internal constructor(internal val axis: Orientation) : PickUpDragHost {
    private val centerByKey: SnapshotStateMap<Any, Float> = mutableStateMapOf()
    internal var keys: List<Any> = emptyList()
    internal var onMove: (from: Int, to: Int) -> Unit = { _, _ -> }

    var liftedKey by mutableStateOf<Any?>(null)
        private set
    var dragging by mutableStateOf(false)
        private set
    private var travelAxis by mutableFloatStateOf(0f)
    private var scroll by mutableFloatStateOf(0f)
    private var origin = 0f

    /** Where the lifted cell is drawn along the axis, relative to its layout position: the finger's travel plus any scroll under it. */
    val offset: Float get() = travelAxis + scroll

    /** The finger along the axis, in root coordinates. */
    val pointer: Float get() = origin + travelAxis

    internal fun addScroll(delta: Float) {
        scroll += delta
    }

    internal fun recordCenter(key: Any, center: Float) {
        if (centerByKey[key] != center) centerByKey[key] = center
    }

    private fun centers(): List<Float>? = keys.map { centerByKey[it] ?: return null }

    private val fromIndex: Int get() = keys.indexOf(liftedKey)

    /** The slot the dragged cell would drop into right now. */
    val targetIndex: Int
        get() = if (!dragging) fromIndex else centers()?.let { ReorderGeometry.targetIndex(it, fromIndex, offset) } ?: fromIndex

    internal fun shiftFor(key: Any): Float {
        if (!dragging) return 0f
        val centers = centers() ?: return 0f
        return ReorderGeometry.neighbourShift(centers, keys.indexOf(key), fromIndex, targetIndex)
    }

    override fun pickUp(key: Any) {
        liftedKey = key
        dragging = false
        travelAxis = 0f
        scroll = 0f
        origin = centerByKey[key] ?: 0f
    }

    override fun startDrag() {
        if (liftedKey != null) dragging = true
    }

    override fun dragTo(travel: Offset) {
        travelAxis = if (axis == Orientation.Vertical) travel.y else travel.x
    }

    /** Ends the touch; a drag that moved the cell to another slot is reported to [onMove] first. */
    override fun release() {
        val from = fromIndex
        val to = targetIndex
        val moved = dragging && from >= 0 && to != from
        liftedKey = null
        dragging = false
        travelAxis = 0f
        scroll = 0f
        if (moved) onMove(from, to)
    }
}

internal val LocalPickUpReorder = compositionLocalOf<PickUpReorderState?> { null }

/** How far to scroll a frame while a held cell's finger is at [pointer]: positive moves the content up (finger near [end]), negative down, 0 away from the edges. */
internal fun edgeScrollDelta(pointer: Float, start: Float, end: Float, edge: Float, step: Float): Float = when {
    pointer > end - edge -> step
    pointer < start + edge -> -step
    else -> 0f
}

/** The scrollable a held list row can auto-scroll: Home's app list region. [viewport] is its on-screen bounds. */
@Stable
class ListAutoScroll(val scrollState: ScrollState, val viewport: () -> Rect)

val LocalListAutoScroll = compositionLocalOf<ListAutoScroll?> { null }

/**
 * Makes the cells emitted by [content] reorderable by a long-press and drag. It adds no layout of its own, so the
 * cells stay in their parent's Row or Column; each cell opts in with [reorderItemLayer] and [longPressReorderClickable].
 * [items] is the saved order; a finished drag shows the new order at once and calls [onReorder], and the saved
 * order replaces the local one when it arrives. [enabled] false (an automatic list, say) leaves the cells with the
 * plain long-press menu.
 */
@Suppress("UnstableCollections") // Compared by value to drop the local order once the saved one arrives.
@Composable
fun <T : Any> PickUpReorderHost(
    items: List<T>,
    key: (T) -> Any,
    axis: Orientation,
    enabled: Boolean,
    onReorder: (List<T>) -> Unit,
    content: @Composable (shown: List<T>) -> Unit,
) {
    var local by remember(items) { mutableStateOf<List<T>?>(null) }
    val shown = local ?: items
    val currentOnReorder = rememberUpdatedState(onReorder)
    val state = remember(axis) { PickUpReorderState(axis) }
    state.keys = shown.map(key)
    ListEdgeAutoScroll(state)
    state.onMove = { from, to ->
        val reordered = ReorderGeometry.move(shown, from, to)
        local = reordered
        currentOnReorder.value(reordered)
    }
    CompositionLocalProvider(LocalPickUpReorder provides if (enabled && shown.size > 1) state else null) { content(shown) }
}

/** Put on a cell's outermost modifier: records its resting centre, and moves and lifts it while it is picked up or making room. */
@Composable
fun Modifier.reorderItemLayer(key: Any): Modifier {
    val state = LocalPickUpReorder.current ?: return this
    val lifted = state.liftedKey == key
    val shiftTarget by remember(state, key) { derivedStateOf { if (state.liftedKey == key) 0f else state.shiftFor(key) } }
    val shift by animateFloatAsState(shiftTarget, label = "reorderShift")
    val scale by animateFloatAsState(if (lifted) PICK_UP_SCALE else 1f, label = "reorderScale")
    return this
        .onGloballyPositioned { coordinates ->
            val bounds = coordinates.boundsInRoot()
            state.recordCenter(key, if (state.axis == Orientation.Vertical) bounds.center.y else bounds.center.x)
        }
        .zIndex(if (lifted) 1f else 0f)
        .graphicsLayer {
            // Not dragging: no residual shift, since a finished drag has already moved the cells in the layout.
            val move = if (lifted) state.offset else if (state.dragging) shift else 0f
            if (state.axis == Orientation.Vertical) translationY = move else translationX = move
            scaleX = scale
            scaleY = scale
        }
}

/**
 * [longPressReleaseClickable] for a cell that may be reordered. Without a [PickUpReorderHost] around it, it is exactly
 * that: the menu opens at the long-press threshold. Inside one, the threshold picks the cell up (haptic tick, lift),
 * releasing in place calls [onLongPress] (the context sheet), and moving drags it.
 */
@Composable
fun Modifier.longPressReorderClickable(key: Any, onClick: () -> Unit, onLongPress: () -> Unit): Modifier {
    val state: PickUpDragHost = LocalPickUpReorder.current ?: LocalGridDrag.current ?: return longPressReleaseClickable(onClick, onLongPress)
    val interactionSource = remember { MutableInteractionSource() }
    val haptics = LocalHapticFeedback.current
    val scope = rememberCoroutineScope()
    val currentOnClick = rememberUpdatedState(onClick)
    val currentOnLongPress = rememberUpdatedState(onLongPress)
    var coordinates by remember { mutableStateOf<LayoutCoordinates?>(null) }
    // While held, Home's own swipes are off: its detector watches the same touch ahead of this one.
    val gate = LocalHomeSwipeGate.current
    val registered = remember { booleanArrayOf(false) }
    fun release() {
        if (registered[0]) gate?.unregister()
        registered[0] = false
    }
    DisposableEffect(gate) { onDispose { release() } }
    return this
        .indication(interactionSource, LocalIndication.current)
        .onGloballyPositioned { coordinates = it }
        .pointerInput(key, state) {
            var press: PressInteraction.Press? = null
            fun emit(interaction: PressInteraction) {
                scope.launch { interactionSource.emit(interaction) }
            }
            detectPickUpDragGesture(
                toRoot = { local -> coordinates?.takeIf { it.isAttached }?.localToRoot(local) ?: local },
                onPress = { position -> PressInteraction.Press(position).also { press = it; emit(it) } },
                onPressEnd = {
                    release()
                    press?.let { emit(PressInteraction.Release(it)) }
                    press = null
                },
                onPickUp = {
                    haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                    gate?.register()
                    registered[0] = true
                    state.pickUp(key)
                },
                onTap = { currentOnClick.value() },
                onReleaseInPlace = {
                    state.release()
                    currentOnLongPress.value()
                },
                onDragStart = { state.startDrag() },
                onDrag = { travel -> state.dragTo(travel) },
                onDragEnd = { state.release() },
            )
        }
        .semantics(mergeDescendants = false) {
            onClick { currentOnClick.value(); true }
            onLongClick { currentOnLongPress.value(); true }
        }
}

/** Scrolls the surrounding list while a held row's finger is near its top or bottom edge, so the row can be carried past the visible part. */
@Composable
private fun ListEdgeAutoScroll(state: PickUpReorderState) {
    val target = LocalListAutoScroll.current
    if (target == null || state.axis != Orientation.Vertical) return
    val density = LocalDensity.current
    val edge = with(density) { LIST_AUTO_SCROLL_EDGE.toPx() }
    val step = with(density) { LIST_AUTO_SCROLL_STEP.toPx() }
    LaunchedEffect(state.dragging, target) {
        while (state.dragging) {
            val viewport = target.viewport()
            val delta = edgeScrollDelta(state.pointer, viewport.top, viewport.bottom, edge, step)
            if (delta != 0f && !viewport.isEmpty) state.addScroll(target.scrollState.scrollBy(delta))
            delay(LIST_AUTO_SCROLL_FRAME_MS)
        }
    }
}

private val LIST_AUTO_SCROLL_EDGE = 56.dp
private val LIST_AUTO_SCROLL_STEP = 10.dp
private const val LIST_AUTO_SCROLL_FRAME_MS = 16L
