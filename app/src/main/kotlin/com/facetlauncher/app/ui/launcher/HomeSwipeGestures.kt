package com.facetlauncher.app.ui.launcher

import androidx.compose.runtime.setValue
import androidx.compose.runtime.getValue
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.PointerInputChange
import androidx.compose.ui.input.pointer.PointerInputScope
import androidx.compose.ui.input.pointer.changedToUpIgnoreConsumed
import androidx.compose.ui.input.pointer.positionChange
import androidx.compose.ui.unit.dp
import com.facetlauncher.app.ui.theme.FACET_TRANSITION_DURATION_MS
import com.facetlauncher.app.ui.theme.FacetTransitionEasing
import kotlin.math.abs
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

/** README's drawer-transform timing/easing (`.34s cubic-bezier(.32,.72,0,1)`), reused for the settle animation. */
private val DRAWER_SETTLE_SPEC = tween<Float>(durationMillis = FACET_TRANSITION_DURATION_MS, easing = FacetTransitionEasing)

/** A drag needs to travel at least this fraction of the container size, in either direction, to commit open/closed. */
internal const val COMMIT_TRAVEL_FRACTION = 0.20f
internal const val VELOCITY_THRESHOLD_PX = 1500f

/** A Home drag must travel this far before it locks to an axis, and vertical must beat horizontal by [VERTICAL_DOMINANCE] to count as vertical. */
private val HOME_SWIPE_SLOP = 16.dp
internal const val VERTICAL_DOMINANCE = 1.2f
private const val HOME_FADE_SCALE_RANGE = 0.03f

/** How Home's layer looks while a panel is opening. */
internal data class HomeLayerFade(val alpha: Float, val scale: Float)

/** Home yields to whichever of the Drawer, Hub and Switch Facets is furthest open: fully faded and [HOME_FADE_SCALE_RANGE] smaller when it is. */
internal fun homeLayerFade(drawerProgress: Float, hubProgress: Float, facetProgress: Float): HomeLayerFade {
    val panelProgress = maxOf(drawerProgress, hubProgress, facetProgress)
    return HomeLayerFade(alpha = 1f - panelProgress, scale = 1f - panelProgress * HOME_FADE_SCALE_RANGE)
}

/** A downward swipe from a fully closed drawer that travels this fraction of the container height, or clears [VELOCITY_THRESHOLD_PX], expands the notification shade. */
internal const val SWIPE_DOWN_SHADE_FRACTION = 0.20f

/**
 * One follow-finger open/close axis — [progress] is 0f (closed) to 1f (fully open), driven by
 * [dragBy] during a drag and resolved to 0f/1f by [settle] on release. Two independent instances
 * back this route: Home↔Drawer (vertical) and Home↔Hub (horizontal) — pulled out to a shared
 * class instead of duplicating this bookkeeping inline twice.
 */
internal class SwipeAxisState(private val containerSizePx: () -> Float, private val coroutineScope: CoroutineScope) {
    val progress = Animatable(0f)
    var dragStartProgress = 0f
        private set
    var dragTargetProgress = 0f
        private set

    // Must be observable Compose state, not a plain var — the "clear focus while drag-closing"
    // LaunchedEffect below is keyed on this, and a plain var's mutation doesn't itself trigger
    // recomposition, so that effect could silently never re-fire mid-drag (see chat history).
    var dragActive by mutableStateOf(false)
        private set

    fun dragBy(deltaPx: Float) {
        if (!dragActive) {
            dragActive = true
            dragStartProgress = progress.value
            dragTargetProgress = progress.value
        }
        val size = containerSizePx()
        dragTargetProgress = (dragTargetProgress + if (size > 0f) deltaPx / size else 0f).coerceIn(0f, 1f)
        coroutineScope.launch { progress.snapTo(dragTargetProgress) }
    }

    fun settle(velocity: Float = 0f) {
        val currentProgress = if (dragActive) dragTargetProgress else progress.value
        val target = swipeSettleTarget(dragStartProgress, currentProgress, velocity)
        dragActive = false
        coroutineScope.launch { progress.animateTo(target, DRAWER_SETTLE_SPEC) }
    }

    suspend fun close() {
        // Already closed and idle: skip the no-op tween, which would still tick the frame clock for its full duration.
        if (progress.value == 0f && !progress.isRunning && !dragActive) return
        dragActive = false
        progress.animateTo(0f, DRAWER_SETTLE_SPEC)
    }
}

/**
 * Where a released drag settles: a fast fling decides it, then a drag that travelled [COMMIT_TRAVEL_FRACTION] or more
 * from where it started, and otherwise it springs back to the side it started closer to.
 */
internal fun swipeSettleTarget(dragStartProgress: Float, currentProgress: Float, velocity: Float): Float {
    val traveled = currentProgress - dragStartProgress
    return when {
        velocity > VELOCITY_THRESHOLD_PX -> 1f
        velocity < -VELOCITY_THRESHOLD_PX -> 0f
        traveled >= COMMIT_TRAVEL_FRACTION -> 1f
        traveled <= -COMMIT_TRAVEL_FRACTION -> 0f
        dragStartProgress > 0.5f -> 1f
        else -> 0f
    }
}

/** Which direction a not-yet-locked drag on Home resolved to, once it clears touch slop. */
internal enum class Axis { VERTICAL, HORIZONTAL }

/**
 * Same drag-gesture semantics as [androidx.compose.foundation.gestures.detectDragGestures], but
 * watches every event at [PointerEventPass.Initial] instead of the default (post-child) Main pass
 * — otherwise a finger coming down on a descendant's own clickable/combinedClickable (an app
 * icon's tap-or-long-press detector, e.g. [com.facetlauncher.app.ui.home.AppRow]) claims the touch
 * before this ancestor's Main-pass detector ever sees it, so a swipe starting on top of a home
 * screen app icon was silently swallowed and only empty space between icons could open the
 * Drawer/Hub/carousel. Below touch slop nothing is consumed, so a plain tap or long-press on that
 * icon still reaches it untouched; only once the drag actually commits does this detector start
 * winning the race — the same Initial-pass technique already used by
 * [com.facetlauncher.app.ui.hub.detectGrabOrResizeGesture] for Hub widget tiles.
 */
internal suspend fun PointerInputScope.detectHomeSwipeGestures(
    /**
     * `false` for a down position this detector should leave completely alone this gesture — not
     * even watched, let alone consumed — so a descendant with its own independent scroll (a hosted
     * clock widget's own `ListView`, see [com.facetlauncher.app.ui.home.HomeScreen]'s own
     * `onClockWidgetBoundsChange` doc) gets the whole gesture undisturbed. Defaults to always
     * claiming, this function's original behavior.
     */
    shouldClaim: (Offset) -> Boolean = { true },
    /** For a down position [shouldClaim] rejected: still watch it, but claim only a horizontally-dominant drag — a vertical one is left to the descendant's own scroll. */
    claimHorizontalOnly: (Offset) -> Boolean = { false },
    onDragStart: () -> Unit,
    onDragEnd: () -> Unit,
    onDragCancel: () -> Unit,
    onDrag: (change: PointerInputChange, dragAmount: Offset) -> Unit,
) {
    val slop = maxOf(viewConfiguration.touchSlop, HOME_SWIPE_SLOP.toPx())
    awaitEachGesture {
        val down = awaitFirstDown(requireUnconsumed = false)
        val horizontalOnly = !shouldClaim(down.position)
        if (horizontalOnly && !claimHorizontalOnly(down.position)) return@awaitEachGesture
        var accumulated = Offset.Zero
        var dragging = false
        var completedCleanly = true
        var abandoned = false
        var finished = false
        while (!finished) {
            val event = awaitPointerEvent(PointerEventPass.Initial)
            val change = event.changes.firstOrNull { it.id == down.id }
            when {
                change == null -> {
                    completedCleanly = false
                    finished = true
                }
                change.changedToUpIgnoreConsumed() -> finished = true
                !dragging -> {
                    accumulated += change.positionChange()
                    if (accumulated.getDistance() > slop) {
                        // A vertical drag that began where only horizontal ones are claimed is left alone.
                        abandoned = horizontalOnly && abs(accumulated.y) > VERTICAL_DOMINANCE * abs(accumulated.x)
                        finished = abandoned
                        if (!abandoned) {
                            dragging = true
                            change.consume()
                            onDragStart()
                            onDrag(change, accumulated)
                        }
                    }
                }
                else -> {
                    val delta = change.positionChange()
                    change.consume()
                    onDrag(change, delta)
                }
            }
        }
        if (abandoned) return@awaitEachGesture
        if (dragging) {
            if (completedCleanly) onDragEnd() else onDragCancel()
        }
    }
}
