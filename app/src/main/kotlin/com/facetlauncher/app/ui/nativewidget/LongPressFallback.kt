package com.facetlauncher.app.ui.nativewidget

import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.AwaitPointerEventScope
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.changedToUp
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.Composable
import androidx.compose.ui.input.pointer.pointerInput
import kotlinx.coroutines.withTimeoutOrNull

/**
 * Runs [onLongPress] when a touch anywhere in the content stays down, even over a child that handles taps. The widgets have no
 * long-press action of their own, so this is Home's. It watches before the children do and takes the rest of that touch once
 * it fires, so the child's tap does not also run on release. A quick tap or a drag is left to the children untouched.
 */
@Composable
fun Modifier.longPressFallback(onLongPress: () -> Unit): Modifier {
    // Not a pointerInput key: Home builds a new lambda on every recomposition, and a restart mid-hold would drop the touch.
    val latest = rememberUpdatedState(onLongPress)
    // After the children have seen the touch, claim it so Home's free-space long-press does not also start on it and race this one.
    return this.pointerInput(Unit) {
        awaitEachGesture {
            awaitFirstDown(requireUnconsumed = false).consume()
            do {
                val event = awaitPointerEvent()
            } while (event.changes.any { it.pressed })
        }
    }.pointerInput(Unit) {
        awaitEachGesture {
            val down = awaitFirstDown(requireUnconsumed = false, pass = PointerEventPass.Initial)
            val endedEarly = withTimeoutOrNull(viewConfiguration.longPressTimeoutMillis) {
                var ended = false
                while (!ended) {
                    val change = awaitPointerEvent(PointerEventPass.Initial).changes.firstOrNull { it.id == down.id }
                    ended = change == null || change.changedToUp() || (change.position - down.position).getDistance() > viewConfiguration.touchSlop
                }
                true
            }
            if (endedEarly == null) {
                latest.value()
                consumeRestOfTouch()
            }
        }
    }
}

private suspend fun AwaitPointerEventScope.consumeRestOfTouch() {
    do {
        val event = awaitPointerEvent(PointerEventPass.Initial)
        event.changes.forEach { it.consume() }
    } while (event.changes.any { it.pressed })
}
