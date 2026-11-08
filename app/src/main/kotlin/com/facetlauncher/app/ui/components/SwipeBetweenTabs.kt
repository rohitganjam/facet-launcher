package com.facetlauncher.app.ui.components

import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.awaitHorizontalTouchSlopOrCancellation
import androidx.compose.foundation.gestures.horizontalDrag
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.positionChange
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlin.math.abs

/** Keeps a tab swipe clear of the system back gesture, which starts at the screen edge. */
val SWIPE_TAB_EDGE_EXCLUSION: Dp = 24.dp

/** How far a horizontal drag must travel to change tabs. */
private val SWIPE_TAB_DISTANCE = 64.dp

/**
 * The tab a horizontal drag of [dragPx] moves to from [selected], or null when it should stay: the drag is shorter than
 * [thresholdPx], or there is no tab that way. A leftward drag (negative) goes to the next tab, a rightward one to the previous.
 */
internal fun adjacentTab(keys: List<String>, selected: String, dragPx: Float, thresholdPx: Float): String? {
    if (abs(dragPx) < thresholdPx) return null
    val index = keys.indexOf(selected)
    if (index < 0) return null
    return keys.getOrNull(if (dragPx < 0f) index + 1 else index - 1)
}

/**
 * Swiping sideways over the content moves to the next or previous tab. With fewer than two [keys], or [enabled] off, it does
 * nothing. A touch that starts within [startExclusion] of the left edge or [endExclusion] of the right edge is left alone
 * (the back gesture, or a letter rail), and so is any drag a child takes first, so vertical scrolling is unaffected.
 */
fun Modifier.swipeBetweenTabs(
    keys: List<String>,
    selected: String,
    onSelect: (String) -> Unit,
    enabled: Boolean = true,
    startExclusion: Dp = SWIPE_TAB_EDGE_EXCLUSION,
    endExclusion: Dp = SWIPE_TAB_EDGE_EXCLUSION,
): Modifier {
    if (!enabled || keys.size < 2) return this
    // Keyed on what it reads: a selection only changes after a gesture ends, so a restart never cuts one short.
    return pointerInput(keys, selected, onSelect, startExclusion, endExclusion) {
        val threshold = SWIPE_TAB_DISTANCE.toPx()
        val start = startExclusion.toPx()
        val end = endExclusion.toPx()
        awaitEachGesture {
            val down = awaitFirstDown(requireUnconsumed = false)
            if (down.position.x < start || down.position.x > size.width - end) return@awaitEachGesture
            var travelled = 0f
            val claimed = awaitHorizontalTouchSlopOrCancellation(down.id) { change, overSlop ->
                change.consume()
                travelled += overSlop
            } ?: return@awaitEachGesture
            horizontalDrag(claimed.id) { change ->
                travelled += change.positionChange().x
                change.consume()
            }
            adjacentTab(keys, selected, travelled, threshold)?.let(onSelect)
        }
    }
}
