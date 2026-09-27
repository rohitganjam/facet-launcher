package com.facetlauncher.app.ui.components

import androidx.compose.foundation.LocalIndication
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.waitForUpOrCancellation
import androidx.compose.foundation.indication
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.PressInteraction
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.PointerEventTimeoutCancellationException
import androidx.compose.ui.input.pointer.PointerInputScope
import androidx.compose.ui.input.pointer.changedToUpIgnoreConsumed
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.semantics.onClick
import androidx.compose.ui.semantics.onLongClick
import androidx.compose.ui.semantics.semantics
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

/**
 * A long-press on a row/tile that only opens a context menu. [onLongPressHold] fires the instant
 * the long-press threshold is met — that's when callers open the menu and fire haptics, not on
 * release.
 *
 * Distinguishes three outcomes for the hold, matching `detectTapGestures`'s own three-way split
 * (`waitForLongPress`'s `Success`/`Released`/`Canceled`): released cleanly before the threshold →
 * [onTap]; held past the threshold without being claimed elsewhere → [onLongPressHold]; claimed by
 * an ancestor before either (e.g. a scrollable or a swipe-to-close gesture taking over the drag) →
 * neither. That third case matters here specifically: swiping to scroll or to close the
 * Drawer/Home list starts with a down on some row, and treating "the ancestor took it" the same as
 * "it was a tap" (as an earlier version of this function did, by only checking whether the
 * long-press timed out) launched the app underneath the finger on every swipe instead of letting
 * the swipe through.
 *
 * [onPress]/[onPressEnd] report the down position for ripple purposes. They're plain callbacks
 * rather than suspend calls because `awaitEachGesture`'s scope is a restricted coroutine scope —
 * only pointer-event-awaiting suspend calls are allowed inside it, so any actual suspending work
 * (like emitting to a [MutableInteractionSource]) has to happen outside, in the caller.
 */
suspend fun PointerInputScope.detectLongPressReleaseGesture(
    onPress: (Offset) -> Unit,
    onPressEnd: () -> Unit,
    onLongPressHold: () -> Unit,
    onTap: () -> Unit,
) {
    awaitEachGesture {
        val down = awaitFirstDown(requireUnconsumed = false)
        // Claim the down immediately, same as `detectTapGestures` does — an ancestor's own
        // long-press-on-empty-space detector (e.g. HomeScreen's root Box) only backs off once it
        // sees this row's touch as consumed; leaving it unconsumed lets both long-presses race
        // and fire together.
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
            // releasedCleanly == null here means the ancestor claimed the gesture (a swipe/scroll),
            // not that the pointer went up — nothing should fire in that case.
            if (releasedCleanly != null) onTap()
            return@awaitEachGesture
        }

        onLongPressHold()

        var lifted = false
        while (!lifted) {
            val event = awaitPointerEvent(PointerEventPass.Initial)
            val change = event.changes.firstOrNull { it.id == down.id }
            if (change == null) {
                lifted = true
            } else {
                lifted = change.changedToUpIgnoreConsumed()
                change.consume()
            }
        }

        onPressEnd()
    }
}

/**
 * Drop-in replacement for `Modifier.combinedClickable(onClick, onLongClick)` on a row/tile whose
 * long-press opens a context menu: same ripple (via [LocalIndication]) and the same TalkBack
 * semantics (a synthetic long-click still invokes [onLongPress] directly, bypassing real pointer
 * timing). A real touch's [onLongPress] fires at the long-press threshold itself (see
 * [detectLongPressReleaseGesture]), alongside the [HapticFeedbackType.LongPress] tick.
 */
@Composable
fun Modifier.longPressReleaseClickable(onClick: () -> Unit, onLongPress: () -> Unit): Modifier {
    val interactionSource = remember { MutableInteractionSource() }
    val haptics = LocalHapticFeedback.current
    val scope = rememberCoroutineScope()
    val currentOnClick = rememberUpdatedState(onClick)
    val currentOnLongPress = rememberUpdatedState(onLongPress)
    return this
        .indication(interactionSource, LocalIndication.current)
        .pointerInput(Unit) {
            var pressInteraction: PressInteraction.Press? = null
            detectLongPressReleaseGesture(
                onPress = { position ->
                    val press = PressInteraction.Press(position)
                    pressInteraction = press
                    emitInteraction(scope, interactionSource, press)
                },
                onPressEnd = {
                    pressInteraction?.let { press -> emitInteraction(scope, interactionSource, PressInteraction.Release(press)) }
                    pressInteraction = null
                },
                onLongPressHold = {
                    haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                    currentOnLongPress.value()
                },
                onTap = { currentOnClick.value() },
            )
        }
        .semantics(mergeDescendants = false) {
            onClick { currentOnClick.value(); true }
            onLongClick { currentOnLongPress.value(); true }
        }
}

private fun emitInteraction(scope: CoroutineScope, interactionSource: MutableInteractionSource, interaction: PressInteraction) {
    scope.launch { interactionSource.emit(interaction) }
}
