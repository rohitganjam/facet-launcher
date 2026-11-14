package com.facetlauncher.app.ui.launcher

import androidx.compose.runtime.setValue
import androidx.compose.runtime.getValue
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.Velocity
import com.facetlauncher.app.data.model.SwipeDirection
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

    suspend fun open() {
        dragActive = false
        progress.animateTo(1f, DRAWER_SETTLE_SPEC)
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

/** How much of a touch Home's swipe detector takes, by where it came down. */
internal enum class HomeTouchClaim { ALL, HORIZONTAL_ONLY, NONE }

/**
 * The app list, the landscape clock pane and a Facet widget page (the Agenda's list) scroll vertically themselves, so Home
 * takes only horizontal drags there, which still turn the clock stack. A hosted widget is left alone: `ClockWidgetTouchGate`
 * hands its unclaimed drags over itself.
 */
internal fun homeTouchClaim(position: Offset, clockWidget: Rect?, clockWidgetIsNative: Boolean, appList: Rect?, clockPane: Rect?): HomeTouchClaim {
    val inWidget = clockWidget?.contains(position) == true
    return when {
        appList?.contains(position) == true || clockPane?.contains(position) == true -> HomeTouchClaim.HORIZONTAL_ONLY
        inWidget && clockWidgetIsNative -> HomeTouchClaim.HORIZONTAL_ONLY
        inWidget -> HomeTouchClaim.NONE
        else -> HomeTouchClaim.ALL
    }
}

/** A down where only horizontal drags are claimed, and that the caller does not claim even then, is not watched at all. */
private fun ignoresTouch(horizontalOnly: Boolean, claimsHorizontal: Boolean) = horizontalOnly && !claimsHorizontal

/** A vertical drag that began where only horizontal ones are claimed belongs to the descendant's own scroll. */
private fun leavesToDescendant(horizontalOnly: Boolean, travel: Offset) =
    horizontalOnly && abs(travel.y) > VERTICAL_DOMINANCE * abs(travel.x)

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
    /** True once something below has taken the touch (a held cell being picked up): this detector then stands down for the gesture. */
    isBlocked: () -> Boolean = { false },
    /** Called when the drag locks, with where the finger first came down. */
    onDragStart: (down: Offset) -> Unit,
    onDragEnd: () -> Unit,
    onDragCancel: () -> Unit,
    onDrag: (change: PointerInputChange, dragAmount: Offset) -> Unit,
) {
    val slop = maxOf(viewConfiguration.touchSlop, HOME_SWIPE_SLOP.toPx())
    awaitEachGesture {
        val down = awaitFirstDown(requireUnconsumed = false)
        val horizontalOnly = !shouldClaim(down.position)
        if (ignoresTouch(horizontalOnly, claimHorizontalOnly(down.position))) return@awaitEachGesture
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
                !dragging && isBlocked() -> finished = true
                !dragging -> {
                    accumulated += change.positionChange()
                    if (accumulated.getDistance() > slop) {
                        // A vertical drag that began where only horizontal ones are claimed is left alone.
                        abandoned = leavesToDescendant(horizontalOnly, accumulated)
                        finished = abandoned
                        if (!abandoned) {
                            dragging = true
                            change.consume()
                            onDragStart(down.position)
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

/** +1 when finger movement toward +x or +y (right or down) is movement in this direction, -1 otherwise. */
internal val SwipeDirection.fingerSign: Float get() = if (this == SwipeDirection.DOWN || this == SwipeDirection.RIGHT) 1f else -1f

/** Where a panel opened by a swipe in [direction] sits at [progress]: off the edge the swipe pulls it from at 0, flush with the screen at 1. */
internal fun panelOffset(direction: SwipeDirection, progress: Float, widthPx: Float, heightPx: Float): IntOffset {
    val hidden = 1f - progress
    return when (direction) {
        SwipeDirection.UP -> IntOffset(0, (hidden * heightPx).toInt())
        SwipeDirection.DOWN -> IntOffset(0, (-hidden * heightPx).toInt())
        SwipeDirection.LEFT -> IntOffset((hidden * widthPx).toInt(), 0)
        SwipeDirection.RIGHT -> IntOffset((-hidden * widthPx).toInt(), 0)
    }
}

/** A drag on an open panel's own surface along [direction]'s axis: back the way it came closes it, the other way reopens it. */
internal fun Modifier.panelSurfaceDrag(panel: SwipeAxisState, direction: SwipeDirection): Modifier = pointerInput(panel, direction) {
    val onDrag = { change: PointerInputChange, amount: Float ->
        panel.dragBy(amount * direction.fingerSign)
        change.consume()
    }
    if (direction.isVertical) {
        detectVerticalDragGestures(onDragEnd = { panel.settle() }, onDragCancel = { panel.settle() }, onVerticalDrag = onDrag)
    } else {
        detectHorizontalDragGestures(onDragEnd = { panel.settle() }, onDragCancel = { panel.settle() }, onHorizontalDrag = onDrag)
    }
}

/**
 * A panel's scrolling content closes it once it can scroll no further back the way the panel came (a drawer that slid
 * up closes from its list's top), and keeps driving the panel for the rest of that drag. A fling settles it. Only drags
 * along [direction]'s axis count; [blocked] stands it down (a long-press sheet holds the finger).
 */
internal fun panelCloseNestedScroll(panel: SwipeAxisState, direction: SwipeDirection, blocked: () -> Boolean = { false }): NestedScrollConnection =
    object : NestedScrollConnection {
        private fun along(offset: Offset) = if (direction.isVertical) offset.y else offset.x
        private fun offsetOf(amount: Float) = if (direction.isVertical) Offset(0f, amount) else Offset(amount, 0f)

        override fun onPreScroll(available: Offset, source: NestedScrollSource): Offset {
            if (source != NestedScrollSource.Drag || !panel.dragActive) return Offset.Zero
            panel.dragBy(along(available) * direction.fingerSign)
            return offsetOf(along(available))
        }

        override fun onPostScroll(consumed: Offset, available: Offset, source: NestedScrollSource): Offset {
            val amount = along(available)
            if (!closesOpenPanel(amount, source)) return Offset.Zero
            panel.dragBy(amount * direction.fingerSign)
            return offsetOf(amount)
        }

        /** A finger drag back the way the panel came, while it shows and nothing holds the finger. */
        private fun closesOpenPanel(amount: Float, source: NestedScrollSource): Boolean {
            val dragsBack = source == NestedScrollSource.Drag && amount * direction.fingerSign < 0f
            return dragsBack && panel.progress.value > 0f && !blocked()
        }

        override suspend fun onPreFling(available: Velocity): Velocity {
            if (!panel.dragActive) return Velocity.Zero
            val velocity = if (direction.isVertical) available.y else available.x
            panel.settle(velocity * direction.fingerSign)
            return if (direction.isVertical) Velocity(0f, available.y) else Velocity(available.x, 0f)
        }
    }
