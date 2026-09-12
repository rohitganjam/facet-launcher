package com.facetlauncher.app.ui.hub

import android.util.Log
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.awaitLongPressOrCancellation
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.PointerInputScope
import androidx.compose.ui.input.pointer.changedToUpIgnoreConsumed
import androidx.compose.ui.input.pointer.positionChange

private const val TAG = "WidgetGrabGesture"

/**
 * A long-press on a Hub widget tile has two outcomes, decided by what happens next: dragging
 * afterward grabs the widget (move to new cell); releasing without moving instead opens a
 * context menu.
 *
 * The gesture triggers [onLongPressHold] as soon as the long-press threshold is met, allowing the
 * UI to show visual feedback (elevation/shadow) even while the finger is still held down.
 */
suspend fun PointerInputScope.detectGrabOrResizeGesture(
    onLongPressHold: () -> Unit,
    onDragStart: () -> Unit,
    onDrag: (Offset) -> Unit,
    onDragEnd: () -> Unit,
    onDragCancel: () -> Unit,
    onReleaseInPlace: () -> Unit,
    onTap: () -> Unit = {},
) {
    val slop = viewConfiguration.touchSlop
    awaitEachGesture {
        val down = awaitFirstDown(requireUnconsumed = false)
        Log.d(TAG, "DOWN received at ${down.position}")
        
        val longPress = awaitLongPressOrCancellation(down.id)
        if (longPress == null) {
            Log.d(TAG, "Long press cancelled or timed out (it was a tap)")
            onTap()
            return@awaitEachGesture
        }
        
        Log.d(TAG, "Long press detected at ${longPress.position}")
        onLongPressHold()

        var grabbed = false
        var accumulated = Offset.Zero
        var completedCleanly = true
        while (true) {
            // Intercept every subsequent event in the Initial pass once long-press is achieved.
            val event = awaitPointerEvent(PointerEventPass.Initial)
            val change = event.changes.firstOrNull { it.id == longPress.id }
            if (change == null) {
                Log.d(TAG, "Pointer ID lost")
                completedCleanly = false
                break
            }
            if (change.changedToUpIgnoreConsumed()) {
                Log.d(TAG, "UP received at ${change.position}")
                change.consume()
                Log.d(TAG, "UP consumed: ${change.isConsumed}")
                break
            }
            val delta = change.positionChange()
            change.consume()
            if (grabbed) {
                onDrag(delta)
            } else {
                accumulated += delta
                if (accumulated.getDistance() > slop) {
                    Log.d(TAG, "Slop exceeded, GRAB starting")
                    grabbed = true
                    onDragStart()
                    onDrag(accumulated)
                }
            }
        }

        if (grabbed) {
            Log.d(TAG, "GRAB ended (clean: $completedCleanly)")
            if (completedCleanly) onDragEnd() else onDragCancel()
        } else {
            Log.d(TAG, "RELEASE IN PLACE triggered")
            onReleaseInPlace()
        }
    }
}
