package com.facetlauncher.app.ui.launcher

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.pointer.PointerInputChange
import androidx.compose.ui.input.pointer.util.VelocityTracker
import androidx.compose.ui.unit.Velocity
import com.facetlauncher.app.ui.home.ClockPagerState
import kotlin.math.abs
import kotlinx.coroutines.CoroutineScope

/**
 * Turns Home's drags into the Drawer (up), Hub (right) and Switch Facets (left) open/close axes.
 * The first move of a drag picks vertical or horizontal for that whole gesture, and whichever of
 * Hub and carousel a horizontal drag engages first keeps it, so one gesture never drives two panels.
 * A horizontal drag that starts in the clock zone first belongs to [clockPager], while there is a page in
 * that direction; at either end of the stack it goes on to Hub or the carousel.
 *
 * Three sources feed the same [start]/[drag]/[settle] so they cannot drift apart: Home's own
 * pointer detector, a hosted clock widget's nested scroll, and the app list's leftover scroll.
 * [endClockAdjust] runs when a drag begins moving something; [expandNotificationShade] when a
 * downward drag from a closed drawer clears the shade threshold.
 */
internal class HomeSwipeController(
    containerHeightPx: () -> Float,
    containerWidthPx: () -> Float,
    coroutineScope: CoroutineScope,
    private val endClockAdjust: () -> Unit,
    private val expandNotificationShade: () -> Unit,
    val clockPager: ClockPagerState,
    private val isInClockZone: (Offset) -> Boolean,
) {
    val drawer = SwipeAxisState(containerHeightPx, coroutineScope)
    val hub = SwipeAxisState(containerWidthPx, coroutineScope)
    val facet = SwipeAxisState(containerWidthPx, coroutineScope)
    private val shadeDistancePx = { containerHeightPx() * SWIPE_DOWN_SHADE_FRACTION }

    private val velocityTracker = VelocityTracker()
    private var dragAxis: Axis? = null
    private var dragStartedClosed = false
    private var rawDragDistance = 0f
    private var dragStartedInClockZone = false

    /** `null` until a horizontal drag has moved, then whether the clock pager (not Hub or the carousel) took it for the whole drag. */
    private var pagerOwnsDrag: Boolean? = null

    private var appListLeftoverDragActive = false

    /** A hosted clock widget's nested scroll, feeding this controller. */
    val widgetScroll = WidgetScrollBridge(this)

    /** Whether the drag in progress locked to the horizontal axis. */
    val isHorizontalDrag: Boolean get() = dragAxis == Axis.HORIZONTAL

    fun start(inClockZone: Boolean = false) {
        dragAxis = null
        velocityTracker.resetTracking()
        dragStartedClosed = drawer.progress.value == 0f
        rawDragDistance = 0f
        dragStartedInClockZone = inClockZone
        pagerOwnsDrag = null
    }

    /**
     * [dx] and [dy] are raw finger movement (positive is right and down), the convention Compose's
     * own drag amounts use. A nested-scroll source negates its own deltas before calling this.
     */
    fun drag(dx: Float, dy: Float) {
        val axis = dragAxis ?: (if (abs(dy) > VERTICAL_DOMINANCE * abs(dx)) Axis.VERTICAL else Axis.HORIZONTAL).also { dragAxis = it }
        endClockAdjust()
        when (axis) {
            Axis.VERTICAL -> {
                drawer.dragBy(-dy)
                rawDragDistance += dy
            }
            Axis.HORIZONTAL -> dragHorizontally(dx)
        }
    }

    /** The clock pager first, if the drag began in the clock zone and has a page that way; else Hub (right) or the carousel (left), whichever the drag engaged first. */
    private fun dragHorizontally(dx: Float) {
        if (pagerOwnsDrag == null && dx != 0f) pagerOwnsDrag = dragStartedInClockZone && clockPager.canMove(dx)
        when {
            pagerOwnsDrag == true -> clockPager.dragBy(dx)
            hub.dragActive || (!facet.dragActive && dx > 0f) -> hub.dragBy(dx)
            facet.dragActive || (!hub.dragActive && dx < 0f) -> facet.dragBy(-dx)
        }
    }

    /** [velocity] is along the drag's own axis: it decides the shade-expand threshold when vertical and the page turn when the pager owns a horizontal drag. */
    fun settle(velocity: Float) {
        when (dragAxis) {
            Axis.VERTICAL -> {
                drawer.settle(velocity = -velocity)
                if (dragStartedClosed && (rawDragDistance >= shadeDistancePx() || velocity >= VELOCITY_THRESHOLD_PX)) {
                    expandNotificationShade()
                }
            }
            Axis.HORIZONTAL -> if (pagerOwnsDrag == true) clockPager.settle(velocity) else settleHorizontal()
            null -> Unit
        }
    }

    fun cancel() {
        when (dragAxis) {
            Axis.VERTICAL -> drawer.settle()
            Axis.HORIZONTAL -> if (pagerOwnsDrag == true) clockPager.settle(0f) else settleHorizontal()
            null -> Unit
        }
    }

    private fun settleHorizontal() {
        if (hub.dragActive) hub.settle()
        if (facet.dragActive) facet.settle()
    }

    /** The pointer detector's callbacks. */
    fun onPointerDragStart(down: Offset) {
        widgetScroll.reset()
        start(inClockZone = isInClockZone(down))
    }

    fun onPointerDragEnd() {
        val velocity = velocityTracker.calculateVelocity()
        settle(if (isHorizontalDrag) velocity.x else velocity.y)
    }

    fun onPointerDrag(change: PointerInputChange, dragAmount: Offset) {
        drag(dragAmount.x, dragAmount.y)
        // Only the vertical (shade-expand) case reads it, but keeping it populated is harmless.
        velocityTracker.addPosition(change.uptimeMillis, change.position)
    }

    /**
     * The app list's own scroll hands back the vertical drag it could not consume once it tops or
     * bottoms out, so the Drawer and shade can still open from it. Blocked while a long-press sheet is open.
     */
    fun appListNestedScroll(blocked: Boolean): NestedScrollConnection = object : NestedScrollConnection {
        override fun onPostScroll(consumed: Offset, available: Offset, source: NestedScrollSource): Offset {
            if (blocked || source != NestedScrollSource.Drag || available.y == 0f) return Offset.Zero
            if (!appListLeftoverDragActive) {
                appListLeftoverDragActive = true
                start()
            }
            drag(0f, available.y)
            return available
        }

        override suspend fun onPreFling(available: Velocity): Velocity {
            if (appListLeftoverDragActive) {
                appListLeftoverDragActive = false
                settle(available.y)
            }
            return super.onPreFling(available)
        }
    }
}

/**
 * A hosted clock widget hands the scroll it cannot use to Home's swipes. Its deltas are scroll
 * offsets, the negation of finger movement. A fling settles the axis itself, so the stop that
 * always follows must not settle it a second time and flip the outcome.
 */
internal class WidgetScrollBridge(private val swipe: HomeSwipeController) {
    private var flingHandledSettle = false

    fun reset() {
        flingHandledSettle = false
    }

    /** A hosted widget only sits in the clock zone, so a drag it hands over starts there. */
    fun start() {
        reset()
        swipe.start(inClockZone = true)
    }

    fun leftoverScroll(dxPx: Float, dyPx: Float) = swipe.drag(-dxPx, -dyPx)

    fun stop() {
        if (!flingHandledSettle) swipe.settle(0f)
    }

    fun leftoverFling(velocityXPx: Float, velocityYPx: Float) {
        flingHandledSettle = true
        // A view's fling velocity is in scroll direction, the opposite of the finger's, which the pager settles on.
        swipe.settle(if (swipe.isHorizontalDrag) -velocityXPx else velocityYPx)
    }
}
