package com.facetlauncher.app.ui.launcher

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.pointer.PointerInputChange
import androidx.compose.ui.input.pointer.util.VelocityTracker
import androidx.compose.ui.unit.Velocity
import kotlin.math.abs
import kotlinx.coroutines.CoroutineScope

/**
 * Turns Home's drags into the Drawer (up), Hub (right) and Switch Facets (left) open/close axes.
 * The first move of a drag picks vertical or horizontal for that whole gesture, and whichever of
 * Hub and carousel a horizontal drag engages first keeps it, so one gesture never drives two panels.
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
) {
    val drawer = SwipeAxisState(containerHeightPx, coroutineScope)
    val hub = SwipeAxisState(containerWidthPx, coroutineScope)
    val facet = SwipeAxisState(containerWidthPx, coroutineScope)
    private val shadeDistancePx = { containerHeightPx() * SWIPE_DOWN_SHADE_FRACTION }

    private val velocityTracker = VelocityTracker()
    private var dragAxis: Axis? = null
    private var dragStartedClosed = false
    private var rawDragDistance = 0f

    private var appListLeftoverDragActive = false

    /** A hosted clock widget's nested scroll, feeding this controller. */
    val widgetScroll = WidgetScrollBridge(this)

    /** Whether the drag in progress locked to the horizontal axis. */
    val isHorizontalDrag: Boolean get() = dragAxis == Axis.HORIZONTAL

    fun start() {
        dragAxis = null
        velocityTracker.resetTracking()
        dragStartedClosed = drawer.progress.value == 0f
        rawDragDistance = 0f
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
            Axis.HORIZONTAL -> when {
                hub.dragActive || (!facet.dragActive && dx > 0f) -> hub.dragBy(dx)
                facet.dragActive || (!hub.dragActive && dx < 0f) -> facet.dragBy(-dx)
            }
        }
    }

    /** [velocity] only matters for a vertical drag (the shade-expand threshold); a horizontal settle ignores it. */
    fun settle(velocity: Float) {
        when (dragAxis) {
            Axis.VERTICAL -> {
                drawer.settle(velocity = -velocity)
                if (dragStartedClosed && (rawDragDistance >= shadeDistancePx() || velocity >= VELOCITY_THRESHOLD_PX)) {
                    expandNotificationShade()
                }
            }
            Axis.HORIZONTAL -> settleHorizontal()
            null -> Unit
        }
    }

    fun cancel() {
        when (dragAxis) {
            Axis.VERTICAL -> drawer.settle()
            Axis.HORIZONTAL -> settleHorizontal()
            null -> Unit
        }
    }

    private fun settleHorizontal() {
        if (hub.dragActive) hub.settle()
        if (facet.dragActive) facet.settle()
    }

    /** The pointer detector's callbacks. */
    fun onPointerDragStart() {
        widgetScroll.reset()
        start()
    }

    fun onPointerDragEnd() = settle(velocityTracker.calculateVelocity().y)

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

    fun start() {
        reset()
        swipe.start()
    }

    fun leftoverScroll(dxPx: Float, dyPx: Float) = swipe.drag(-dxPx, -dyPx)

    fun stop() {
        if (!flingHandledSettle) swipe.settle(0f)
    }

    fun leftoverFling(velocityXPx: Float, velocityYPx: Float) {
        flingHandledSettle = true
        swipe.settle(if (swipe.isHorizontalDrag) velocityXPx else velocityYPx)
    }
}
