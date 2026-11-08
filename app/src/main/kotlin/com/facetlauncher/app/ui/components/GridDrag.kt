package com.facetlauncher.app.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.animateValueAsState
import androidx.compose.animation.core.VectorConverter
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.boundsInRoot
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.zIndex

/** Where a dragged grid cell would land: the cell whose bounds hold [point]. Pure, so the rule is tested without a screen. */
internal fun cellIndexAt(bounds: Map<Int, Rect>, point: Offset): Int? = bounds.entries.firstOrNull { it.value.contains(point) }?.key

/**
 * The picked-up and dragged state for Home's grid. Cells report their bounds by display index; the lifted item's
 * [targetIndex] is the cell under where it was lifted from plus the finger's travel. [scrollCompensation] is how far the
 * grid has scrolled under the finger during an edge auto-scroll, so the lifted cell stays under it.
 */
@Stable
class GridDragState internal constructor() : PickUpDragHost {
    internal val bounds = mutableStateMapOf<Int, Rect>()
    internal var indexOfKey: (Any) -> Int? = { null }
    internal var onDrop: (fromIndex: Int, toIndex: Int) -> Unit = { _, _ -> }

    override var liftedKey by mutableStateOf<Any?>(null)
        private set
    override var dragging by mutableStateOf(false)
        private set
    var travel by mutableStateOf(Offset.Zero)
        private set
    private var scroll by mutableFloatStateOf(0f)
    private var origin = Offset.Zero

    /** Where the lifted cell is drawn, relative to its layout position. */
    val offset: Offset get() = Offset(travel.x, travel.y + scroll)
    override val dragOffset: Offset get() = offset

    /** The finger, in root coordinates. */
    val pointer: Offset get() = origin + travel

    val targetIndex: Int?
        get() = if (dragging) cellIndexAt(bounds, origin + travel) else null

    /** True for a list read row by row where a drop inserts at the target and the cells between slide along, false for a grid where it swaps. */
    internal var insertMode = false

    /** How far the cell holding [key] slides to make room while the held one is over another cell (insert mode only). */
    internal fun shiftFor(key: Any): Offset {
        if (!insertMode || !dragging) return Offset.Zero
        val index = indexOfKey(key) ?: return Offset.Zero
        val from = liftedKey?.let(indexOfKey) ?: return Offset.Zero
        val to = targetIndex ?: return Offset.Zero
        val destination = when {
            from < to && index in (from + 1)..to -> index - 1
            from > to && index in to until from -> index + 1
            else -> return Offset.Zero
        }
        val here = bounds[index]?.center ?: return Offset.Zero
        return (bounds[destination]?.center ?: return Offset.Zero) - here
    }

    internal fun recordBounds(index: Int, rect: Rect) {
        if (bounds[index] != rect) bounds[index] = rect
    }

    internal fun addScroll(delta: Float) {
        scroll += delta
    }

    override fun pickUp(key: Any) {
        liftedKey = key
        dragging = false
        travel = Offset.Zero
        scroll = 0f
        origin = indexOfKey(key)?.let { bounds[it]?.center } ?: Offset.Zero
    }

    override fun startDrag() {
        if (liftedKey != null) dragging = true
    }

    override fun dragTo(travel: Offset) {
        this.travel = travel
    }

    override fun release() {
        val from = liftedKey?.let(indexOfKey)
        val to = targetIndex
        val moved = dragging && from != null && to != null && to != from
        liftedKey = null
        dragging = false
        travel = Offset.Zero
        scroll = 0f
        if (moved) onDrop(from!!, to!!)
    }
}

internal val LocalGridDrag = compositionLocalOf<GridDragState?> { null }

/** Put on every grid cell, empty ones too: reports where it is. */
@Composable
fun Modifier.gridCellBounds(index: Int): Modifier {
    val state = LocalGridDrag.current ?: return this
    return onGloballyPositioned { state.recordBounds(index, it.boundsInRoot()) }
}

/** Put on an item's outermost modifier: it follows the finger and grows while picked up. */
@Composable
fun Modifier.gridItemLayer(key: Any): Modifier {
    val state = LocalGridDrag.current ?: return this
    val lifted = state.liftedKey == key
    val scale by animateFloatAsState(if (lifted) PICK_UP_SCALE else 1f, label = "gridPickUpScale")
    val shiftTarget by remember(state, key) { derivedStateOf { if (state.liftedKey == key) Offset.Zero else state.shiftFor(key) } }
    val shift by animateValueAsState(shiftTarget, Offset.VectorConverter, label = "gridShift")
    return zIndex(if (lifted) 1f else 0f).graphicsLayer {
        val move = if (lifted) state.offset else shift
        translationX = move.x
        translationY = move.y
        scaleX = scale
        scaleY = scale
    }
}
