package com.facetlauncher.app.ui.components

import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import kotlin.math.roundToInt

/**
 * Drag-to-reorder mechanics shared by every reorderable list/row in the app (the facet list,
 * per-facet favorites, Settings' default favorites, and the dock). Deliberately headless: it
 * tracks which item is being dragged and by how much, and splices [items] into a new order live
 * as the drag crosses each slot boundary — but it renders nothing and owns no colors/shape/sizing.
 * Each call site keeps its own row/tile composable, its own "lifted" graphicsLayer treatment, and
 * attaches [dragModifier] to whichever part of that row should act as the handle, reading
 * [isDragging]/[dragOffset] to drive its own visuals. That keeps every list's styling exactly as
 * it was — this only replaces the copy-pasted gesture/index-math that used to sit next to it.
 *
 * [onOrderChanged] fires on every slot boundary the drag crosses, so it must be cheap — update
 * local Compose state only, never a repository/Room write. [onDragCommit] fires once, when the
 * finger lifts (or the gesture is cancelled), with the final order — that's where a caller should
 * persist, matching the two-tier callback the facet list's reorder screen already used.
 */
@Stable
class DragReorderState<T> internal constructor(
    private val items: State<List<T>>,
    private val itemKey: (T) -> Any,
    private val axis: Orientation,
    private val slotSizePx: State<Float>,
    private val onOrderChanged: State<(List<T>) -> Unit>,
    private val onDragCommit: State<(List<T>) -> Unit>,
) {
    var draggingKey by mutableStateOf<Any?>(null)
        private set

    var dragOffset by mutableFloatStateOf(0f)
        private set

    fun isDragging(item: T): Boolean = draggingKey == itemKey(item)

    /** Attach to whatever should act as the drag handle — a handle icon, or the whole row/tile. */
    fun dragModifier(item: T): Modifier {
        val key = itemKey(item)
        return Modifier.pointerInput(key) {
            detectDragGestures(
                onDragStart = { draggingKey = key; dragOffset = 0f },
                onDragEnd = { endDrag() },
                onDragCancel = { endDrag() },
            ) { change, dragAmount ->
                change.consume()
                onDrag(key, if (axis == Orientation.Vertical) dragAmount.y else dragAmount.x)
            }
        }
    }

    private fun onDrag(key: Any, delta: Float) {
        dragOffset += delta
        val slot = slotSizePx.value
        if (slot <= 0f) return
        val shift = (dragOffset / slot).roundToInt()
        if (shift == 0) return
        val list = items.value
        val currentIndex = list.indexOfFirst { itemKey(it) == key }
        if (currentIndex == -1) return
        val targetIndex = (currentIndex + shift).coerceIn(0, list.lastIndex)
        if (targetIndex != currentIndex) {
            onOrderChanged.value(list.toMutableList().apply { add(targetIndex, removeAt(currentIndex)) })
        }
        dragOffset -= shift * slot
    }

    private fun endDrag() {
        onDragCommit.value(items.value)
        draggingKey = null
        dragOffset = 0f
    }
}

/**
 * Remembers a [DragReorderState] for a reorderable list. [slotSizePx] is the pixel size (row
 * height for a vertical list, tile width for a horizontal one) of one step — how far the finger
 * has to move before the dragged item swaps with its neighbor.
 */
@Composable
fun <T> rememberDragReorderState(
    items: List<T>,
    key: (T) -> Any,
    axis: Orientation,
    slotSizePx: Float,
    onOrderChanged: (List<T>) -> Unit,
    onDragCommit: (List<T>) -> Unit = {},
): DragReorderState<T> {
    val currentItems = rememberUpdatedState(items)
    val currentSlotSizePx = rememberUpdatedState(slotSizePx)
    val currentOnOrderChanged = rememberUpdatedState(onOrderChanged)
    val currentOnDragCommit = rememberUpdatedState(onDragCommit)
    return remember {
        DragReorderState(
            items = currentItems,
            itemKey = key,
            axis = axis,
            slotSizePx = currentSlotSizePx,
            onOrderChanged = currentOnOrderChanged,
            onDragCommit = currentOnDragCommit,
        )
    }
}
