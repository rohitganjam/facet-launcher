package com.facetlauncher.app.ui.components

import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.awaitHorizontalTouchSlopOrCancellation
import androidx.compose.foundation.gestures.horizontalDrag
import androidx.compose.ui.Modifier
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.AnimationVector1D
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.key
import androidx.compose.runtime.derivedStateOf
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.onSizeChanged
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import androidx.compose.ui.input.pointer.positionChange
import androidx.compose.ui.input.pointer.PointerInputScope
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlin.math.abs
import kotlin.math.sign

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
 * Tabbed content that follows a horizontal swipe. [content] draws one tab; while the finger drags, the current tab moves
 * with it and the neighbouring tab (the next one for a leftward drag, the previous for a rightward one) shows beside it,
 * so the next screen is already there. On release past 64dp the two finish sliding, [onSelect] switches tab, and a shorter
 * drag springs back. A drag past the first or last tab follows at a third of the travel. With fewer than two [keys], or
 * [enabled] off, it just draws the selected tab. A touch that starts within [startExclusion] of the left edge or
 * [endExclusion] of the right edge is left alone (the back gesture, or a letter rail), and so is any drag a child takes
 * first, so vertical scrolling is unaffected.
 */
/** The slide state of a [SwipeTabs]: how far its content is dragged, and which tab the main slot draws. */
@Stable
private class SwipeTabsState(selected: String) {
    val offset = Animatable(0f)
    var width by mutableIntStateOf(0)

    /** The tab drawn in the main slot. It trails the selected tab while a tab tapped elsewhere slides in. */
    var shown by mutableStateOf(selected)

    val swipeSide by derivedStateOf { sign(offset.value) }

    /** The tab sitting beside [shown] while content slides, and which side it is on (negative: to the right). */
    fun neighbour(keys: List<String>, selected: String): Pair<String, Float>? {
        val index = keys.indexOf(shown)
        if (shown != selected) return selected to (if (keys.indexOf(selected) > index) -1f else 1f)
        val key = when {
            swipeSide < 0f -> keys.getOrNull(index + 1)
            swipeSide > 0f -> keys.getOrNull(index - 1)
            else -> null
        }
        return key?.let { it to swipeSide }
    }
}

/**
 * Tabbed content that follows a horizontal swipe. [content] draws one tab; while the finger drags, the current tab moves
 * with it and the neighbouring tab (the next one for a leftward drag, the previous for a rightward one) shows beside it,
 * so the next screen is already there. On release past 64dp the two finish sliding, [onSelect] switches tab, and a shorter
 * drag springs back. Choosing a tab elsewhere slides it in the same way. A drag past the first or last tab follows at a
 * third of the travel. With fewer than two [keys], or [enabled] off, it only draws. A touch that starts within
 * [startExclusion] of the left edge or [endExclusion] of the right edge is left alone (the back gesture, or a letter rail),
 * and so is any drag a child takes first, so vertical scrolling is unaffected.
 */
@Suppress("UnstableCollections", "ContentSlotReused") // Few tab keys, compared by value; two tabs draw side by side on purpose, each under its own key.
@Composable
fun SwipeTabs(
    keys: List<String>,
    selected: String,
    onSelect: (String) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    startExclusion: Dp = SWIPE_TAB_EDGE_EXCLUSION,
    endExclusion: Dp = SWIPE_TAB_EDGE_EXCLUSION,
    content: @Composable (key: String) -> Unit,
) {
    val active = enabled && keys.size >= 2
    val state = remember { SwipeTabsState(selected) }
    val scope = rememberCoroutineScope()
    // A tab chosen from the tab row slides in the same way a swipe does.
    LaunchedEffect(selected, state.width) { slideInSelected(state, keys, selected) }
    val swipe = if (active) {
        Modifier.pointerInput(keys, state.shown, selected, onSelect, startExclusion, endExclusion) {
            val limits = SwipeLimits(SWIPE_TAB_DISTANCE.toPx(), startExclusion.toPx(), endExclusion.toPx())
            detectTabSwipe(state, scope, keys, selected, limits, onSelect)
        }
    } else {
        Modifier
    }
    Box(modifier = modifier.clipToBounds().onSizeChanged { state.width = it.width }.then(swipe)) {
        val shown = if (active) state.shown else selected
        key(shown) { Box(Modifier.fillMaxSize().graphicsLayer { translationX = state.offset.value }) { content(shown) } }
        val neighbour = if (active) state.neighbour(keys, selected) else null
        if (neighbour != null) {
            key(neighbour.first) {
                Box(Modifier.fillMaxSize().graphicsLayer { translationX = state.offset.value + (if (neighbour.second < 0f) state.width else -state.width) }) { content(neighbour.first) }
            }
        }
    }
}

private class SwipeLimits(val distance: Float, val start: Float, val end: Float)

private suspend fun slideInSelected(state: SwipeTabsState, keys: List<String>, selected: String) {
    if (state.shown == selected) return
    if (state.width <= 0) {
        state.shown = selected
        return
    }
    val direction = if (keys.indexOf(selected) > keys.indexOf(state.shown)) -1f else 1f
    state.offset.animateTo(direction * state.width, tween(SWIPE_TAB_SETTLE_MS))
    state.shown = selected
    state.offset.snapTo(0f)
}

/** Follows a horizontal drag with [SwipeTabsState.offset] and, on release, finishes the slide or springs back. */
private suspend fun PointerInputScope.detectTabSwipe(
    state: SwipeTabsState,
    scope: CoroutineScope,
    keys: List<String>,
    selected: String,
    limits: SwipeLimits,
    onSelect: (String) -> Unit,
) {
    awaitEachGesture {
        val down = awaitFirstDown(requireUnconsumed = false)
        // A gesture on an edge, or while another tab is still sliding in, is not ours.
        if (down.position.x < limits.start || down.position.x > size.width - limits.end || state.shown != selected) return@awaitEachGesture
        var travelled = 0f
        fun follow() {
            scope.launch(start = CoroutineStart.UNDISPATCHED) { state.offset.snapTo(resisted(travelled, keys, state.shown)) }
        }
        val claimed = awaitHorizontalTouchSlopOrCancellation(down.id) { change, overSlop ->
            change.consume()
            travelled += overSlop
            follow()
        } ?: return@awaitEachGesture
        horizontalDrag(claimed.id) { change ->
            travelled += change.positionChange().x
            change.consume()
            follow()
        }
        val next = adjacentTab(keys, state.shown, travelled, limits.distance)
        scope.launch { settle(state.offset, travelled, next, state.width.toFloat(), onShown = { state.shown = it }, onSelect = onSelect) }
    }
}

/** Past the first or last tab there is nowhere to go, so the content follows the finger at a third of its travel. */
private fun resisted(travelled: Float, keys: List<String>, selected: String): Float {
    val index = keys.indexOf(selected)
    val blocked = if (travelled < 0f) index >= keys.lastIndex else index <= 0
    return if (blocked) travelled / SWIPE_TAB_OVERSCROLL_DIVISOR else travelled
}

/** Finishes the slide the way it was dragged and switches tab; without a [next] tab it springs back. */
private suspend fun settle(
    offset: Animatable<Float, AnimationVector1D>,
    travelled: Float,
    next: String?,
    width: Float,
    onShown: (String) -> Unit,
    onSelect: (String) -> Unit,
) {
    if (next == null || width <= 0f) {
        offset.animateTo(0f, tween(SWIPE_TAB_SETTLE_MS))
        return
    }
    val direction = if (travelled < 0f) -1f else 1f
    offset.animateTo(direction * width, tween(SWIPE_TAB_SETTLE_MS))
    onShown(next)
    onSelect(next)
    offset.snapTo(0f)
}

private const val SWIPE_TAB_OVERSCROLL_DIVISOR = 3f
private const val SWIPE_TAB_SETTLE_MS = 180
