package com.facetlauncher.app.ui.components

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.pointer.AwaitPointerEventScope
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.PointerInputChange
import androidx.compose.ui.input.pointer.PointerEventTimeoutCancellationException
import androidx.compose.ui.input.pointer.PointerInputScope
import androidx.compose.ui.input.pointer.changedToUpIgnoreConsumed
import androidx.compose.ui.input.pointer.positionChanged
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.waitForUpOrCancellation
import androidx.compose.ui.unit.dp

/** Travel after the pick-up before it counts as a drag; a finger shifts a little when a hold ends, so this is wider than touch slop. */
internal val PICK_UP_DRAG_SLOP = 14.dp

/** Whether a held touch has become a drag yet, from how far the finger has travelled since the pick-up. */
internal class PickUpDragTracker(private val slopPx: Float) {
    var dragging = false
        private set

    /** True on the update that crosses the slop; from then on [dragging] stays true until the gesture ends. */
    fun update(travelX: Float, travelY: Float): Boolean {
        if (dragging) return false
        if (travelX * travelX + travelY * travelY <= slopPx * slopPx) return false
        dragging = true
        return true
    }
}

/**
 * A press on a row or tile that can be picked up. Before the long-press threshold it behaves like
 * [detectLongPressReleaseGesture] (a tap, or nothing if an ancestor takes the swipe). At the threshold
 * [onPickUp] fires and the touch is claimed, with no menu yet:
 *  - released without moving: [onReleaseInPlace] (callers open the context sheet);
 *  - moved past [PICK_UP_DRAG_SLOP]: [onDragStart], then [onDrag] with the finger's position in
 *    the caller's own coordinate space (via [toRoot]), then [onDragEnd].
 * [toRoot] maps this node's local position to a space that does not move with the dragged row.
 */
suspend fun PointerInputScope.detectPickUpDragGesture(
    toRoot: (Offset) -> Offset,
    onPress: (Offset) -> Unit,
    onPressEnd: () -> Unit,
    onPickUp: () -> Unit,
    onTap: () -> Unit,
    onReleaseInPlace: () -> Unit,
    onDragStart: () -> Unit,
    onDrag: (travel: Offset) -> Unit,
    onDragEnd: () -> Unit,
) {
    awaitEachGesture {
        val down = awaitFirstDown(requireUnconsumed = false)
        down.consume()
        onPress(down.position)

        var heldPastThreshold = false
        val releasedCleanly = try {
            withTimeout(viewConfiguration.longPressTimeoutMillis) { waitForUpOrCancellation() }
        } catch (_: PointerEventTimeoutCancellationException) {
            heldPastThreshold = true
            null
        }

        if (!heldPastThreshold) {
            onPressEnd()
            if (releasedCleanly != null) onTap()
            return@awaitEachGesture
        }

        onPickUp()
        trackPickedUp(
            down = down,
            toRoot = toRoot,
            tracker = PickUpDragTracker(PICK_UP_DRAG_SLOP.toPx()),
            onDragStart = onDragStart,
            onDrag = onDrag,
            onFinish = { dragged -> if (dragged) onDragEnd() else onReleaseInPlace() },
        )
        onPressEnd()
    }
}

/** Follows the held touch, claiming every event, until it lifts or vanishes; [onFinish] says whether it had become a drag. */
private suspend fun AwaitPointerEventScope.trackPickedUp(
    down: PointerInputChange,
    toRoot: (Offset) -> Offset,
    tracker: PickUpDragTracker,
    onDragStart: () -> Unit,
    onDrag: (travel: Offset) -> Unit,
    onFinish: (dragged: Boolean) -> Unit,
) {
    val start = toRoot(down.position)
    var finished = false
    while (!finished) {
        val change = awaitPointerEvent(PointerEventPass.Initial).changes.firstOrNull { it.id == down.id }
        val lifted = change == null || change.changedToUpIgnoreConsumed()
        if (change != null && !lifted && change.positionChanged()) {
            val travel = toRoot(change.position) - start
            if (tracker.update(travel.x, travel.y)) onDragStart()
            if (tracker.dragging) onDrag(travel)
        }
        change?.consume()
        if (lifted) onFinish(tracker.dragging)
        finished = lifted
    }
}
