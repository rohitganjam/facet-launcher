package com.facetlauncher.app.ui.launcher

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.pointer.PointerInputChange
import androidx.compose.ui.input.pointer.util.VelocityTracker
import androidx.compose.ui.unit.Velocity
import com.facetlauncher.app.data.model.SwipeAction
import com.facetlauncher.app.data.model.SwipeActions
import com.facetlauncher.app.data.model.SwipeDirection
import com.facetlauncher.app.ui.home.ClockPagerState
import kotlin.math.abs
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

private val PANEL_ACTIONS = listOf(SwipeAction.APP_DRAWER, SwipeAction.HUB, SwipeAction.SWITCH_FACETS)
private val DEFAULT_SIDES = mapOf(
    SwipeAction.APP_DRAWER to SwipeDirection.UP,
    SwipeAction.HUB to SwipeDirection.RIGHT,
    SwipeAction.SWITCH_FACETS to SwipeDirection.LEFT,
)

/** How long Home stays faded after a committed pull-down, covering the system shade's start-up lag. */
internal const val SHADE_OPEN_HOLD_MS = 250L

/**
 * Turns Home's drags into the actions Settings → Gestures assigns to each swipe ([actions]): the Drawer, Hub and Switch
 * Facets panels, which slide in from the side the swipe pulls them from, and the notification shade.
 * The first move of a drag picks vertical or horizontal for that whole gesture, and the first direction along it that
 * has an action keeps that action for the gesture, so one gesture never drives two panels.
 * A horizontal drag that starts in the clock zone first belongs to [clockPager], while there is a page in
 * that direction; at either end of the stack it goes on to the swipe's action.
 *
 * Three sources feed the same [start]/[drag]/[settle] so they cannot drift apart: Home's own
 * pointer detector, a hosted clock widget's nested scroll, and the app list's leftover scroll.
 * [endClockAdjust] runs when a drag begins moving something; [expandNotificationShade] when a
 * shade swipe from closed Home clears the shade threshold.
 */
internal class HomeSwipeController(
    private val containerHeightPx: () -> Float,
    private val containerWidthPx: () -> Float,
    private val coroutineScope: CoroutineScope,
    private val endClockAdjust: () -> Unit,
    private val expandNotificationShade: () -> Unit,
    val clockPager: ClockPagerState,
    private val isInClockZone: (Offset) -> Boolean,
) {
    /** Settings → Gestures: what each swipe does. A panel that no longer sits on the swipe it last came from goes back to its first one. */
    var actions = SwipeActions()
        set(value) {
            field = value
            drawerDirection = homeSideOf(SwipeAction.APP_DRAWER, drawerDirection)
            hubDirection = homeSideOf(SwipeAction.HUB, hubDirection)
            facetDirection = homeSideOf(SwipeAction.SWITCH_FACETS, facetDirection)
        }

    /**
     * The side each panel slides in from: the swipe that last opened it (an action can sit on two swipes), else its first
     * swipe, else its usual side while no swipe opens it (Switch Facets still opens from the Home menu).
     */
    var drawerDirection by mutableStateOf(SwipeDirection.UP)
        private set
    var hubDirection by mutableStateOf(SwipeDirection.RIGHT)
        private set
    var facetDirection by mutableStateOf(SwipeDirection.LEFT)
        private set

    private fun homeSideOf(action: SwipeAction, current: SwipeDirection): SwipeDirection {
        val sides = actions.directionsOf(action)
        return if (current in sides) current else sides.firstOrNull() ?: DEFAULT_SIDES.getValue(action)
    }

    val drawer = SwipeAxisState({ sizeAlong(drawerDirection) }, coroutineScope)
    val hub = SwipeAxisState({ sizeAlong(hubDirection) }, coroutineScope)
    val facet = SwipeAxisState({ sizeAlong(facetDirection) }, coroutineScope)
    private val shadeDistancePx = { containerHeightPx() * SWIPE_DOWN_SHADE_FRACTION }

    /** A shade swipe from closed Home: 0 to 1 over the distance that commits the shade, so Home is fully faded when letting go would open it. */
    val shade = SwipeAxisState(shadeDistancePx, coroutineScope)

    private val velocityTracker = VelocityTracker()
    private var dragAxis: Axis? = null
    private var dragStartedClosed = false

    /** The swipe whose action this drag drives, once it has moved toward one that has an action. */
    private var engaged: SwipeDirection? = null

    /** A panel still showing when the drag began (mid-settle): the drag drives it along its own axis instead. */
    private var startedOn: SwipeAction? = null

    /** Net finger travel toward [engaged]'s direction. */
    private var engagedTravel = 0f
    private var dragStartedInClockZone = false

    /** `null` until a horizontal drag has moved, then whether the clock pager (not a panel) took it for the whole drag. */
    private var pagerOwnsDrag: Boolean? = null

    private var appListLeftoverDragActive = false

    /** While adjusting the clock: whether the horizontal drag in progress turns the clock pager (`null` until it has moved). */
    private var adjustPagerOwnsDrag: Boolean? = null
    private var adjustDragInClockZone = false

    /** A hosted clock widget's nested scroll, feeding this controller. */
    val widgetScroll = WidgetScrollBridge(this)

    /** Whether the drag in progress locked to the horizontal axis. */
    val isHorizontalDrag: Boolean get() = dragAxis == Axis.HORIZONTAL

    private fun sizeAlong(direction: SwipeDirection) = if (direction.isVertical) containerHeightPx() else containerWidthPx()

    /** The panel [action] slides, `null` for the shade (handled apart) and None. */
    private fun panelFor(action: SwipeAction): SwipeAxisState? = when (action) {
        SwipeAction.APP_DRAWER -> drawer
        SwipeAction.HUB -> hub
        SwipeAction.SWITCH_FACETS -> facet
        SwipeAction.NOTIFICATION_SHADE, SwipeAction.NONE -> null
    }

    fun start(inClockZone: Boolean = false) {
        dragAxis = null
        velocityTracker.resetTracking()
        startedOn = PANEL_ACTIONS.firstOrNull { panelFor(it)?.progress?.value != 0f }
        dragStartedClosed = startedOn == null
        engaged = null
        engagedTravel = 0f
        dragStartedInClockZone = inClockZone
        pagerOwnsDrag = null
    }

    private fun panelDirection(action: SwipeAction) = when (action) {
        SwipeAction.HUB -> hubDirection
        SwipeAction.SWITCH_FACETS -> facetDirection
        else -> drawerDirection
    }

    /**
     * [dx] and [dy] are raw finger movement (positive is right and down), the convention Compose's
     * own drag amounts use. A nested-scroll source negates its own deltas before calling this.
     */
    fun drag(dx: Float, dy: Float) {
        val axis = dragAxis ?: (if (abs(dy) > VERTICAL_DOMINANCE * abs(dx)) Axis.VERTICAL else Axis.HORIZONTAL).also { dragAxis = it }
        endClockAdjust()
        when (axis) {
            Axis.VERTICAL -> dragAlong(dy, vertical = true)
            Axis.HORIZONTAL -> dragHorizontally(dx)
        }
    }

    /** The clock pager first, if the drag began in the clock zone and has a page that way; else the swipe's action. */
    private fun dragHorizontally(dx: Float) {
        if (pagerOwnsDrag == null && dx != 0f) pagerOwnsDrag = dragStartedInClockZone && clockPager.canMove(dx)
        if (pagerOwnsDrag == true) clockPager.dragBy(dx) else dragAlong(dx, vertical = false)
    }

    /** [delta] is finger movement along the drag's axis, positive down or right. */
    private fun dragAlong(delta: Float, vertical: Boolean) {
        if (engaged == null && delta != 0f) {
            engaged = engageFor(if (vertical) directionOfVertical(delta) else directionOfHorizontal(delta))
            engaged?.let { if (startedOn == null) pointPanelAt(it) }
        }
        val direction = engaged ?: return
        val towardIt = delta * direction.fingerSign
        engagedTravel += towardIt
        when (val action = engagedAction(direction)) {
            SwipeAction.NOTIFICATION_SHADE -> shade.dragBy(towardIt)
            else -> panelFor(action)?.dragBy(towardIt)
        }
    }

    /** The panel this swipe opens slides in from the swipe's own side. */
    private fun pointPanelAt(direction: SwipeDirection) {
        when (actions.actionFor(direction)) {
            SwipeAction.APP_DRAWER -> drawerDirection = direction
            SwipeAction.HUB -> hubDirection = direction
            SwipeAction.SWITCH_FACETS -> facetDirection = direction
            SwipeAction.NOTIFICATION_SHADE, SwipeAction.NONE -> Unit
        }
    }

    private fun engagedAction(direction: SwipeDirection) = startedOn ?: actions.actionFor(direction)

    /** The swipe a drag first moving in [direction] drives, or `null` to keep waiting for one that does something. */
    private fun engageFor(direction: SwipeDirection): SwipeDirection? {
        val showing = startedOn?.let(::panelDirection)
        if (showing != null) return showing.takeIf { it.isVertical == direction.isVertical }
        return direction.takeIf { actions.actionFor(it) != SwipeAction.NONE }
    }

    private fun directionOfVertical(dy: Float) = if (dy > 0f) SwipeDirection.DOWN else SwipeDirection.UP

    private fun directionOfHorizontal(dx: Float) = if (dx > 0f) SwipeDirection.RIGHT else SwipeDirection.LEFT

    /** [velocity] is finger velocity along the drag's own axis (positive down or right): it decides a panel's settle, the shade, and a page turn the pager owns. */
    fun settle(velocity: Float) {
        when {
            dragAxis == null -> Unit
            dragAxis == Axis.HORIZONTAL && pagerOwnsDrag == true -> clockPager.settle(velocity)
            else -> settleEngaged(velocity)
        }
    }

    private fun settleEngaged(velocity: Float) {
        val direction = engaged ?: return
        val towardIt = velocity * direction.fingerSign
        val action = engagedAction(direction)
        if (action == SwipeAction.NOTIFICATION_SHADE) {
            val expands = dragStartedClosed && (engagedTravel >= shadeDistancePx() || towardIt >= VELOCITY_THRESHOLD_PX)
            releaseShadePull(holdFaded = expands)
            if (expands) expandNotificationShade()
        } else {
            panelFor(action)?.settle(towardIt)
        }
    }

    fun cancel() {
        when {
            dragAxis == null -> Unit
            dragAxis == Axis.HORIZONTAL && pagerOwnsDrag == true -> clockPager.settle(0f)
            else -> {
                val direction = engaged ?: return
                val action = engagedAction(direction)
                if (action == SwipeAction.NOTIFICATION_SHADE) releaseShadePull() else panelFor(action)?.settle()
            }
        }
    }

    /** Home returns from the pull-down fade; when the shade is opening it stays faded until the system shade has had time to cover it. */
    private fun releaseShadePull(holdFaded: Boolean = false) {
        coroutineScope.launch {
            if (holdFaded) {
                shade.open()
                delay(SHADE_OPEN_HOLD_MS)
            }
            shade.close()
        }
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

    /**
     * Clock adjust mode takes no Hub, carousel or drawer swipes and must not end the adjust, but a horizontal
     * drag in the clock zone still turns the clock stack's pages, so each page can be adjusted in turn.
     */
    fun onAdjustDragStart(position: Offset) {
        velocityTracker.resetTracking()
        adjustPagerOwnsDrag = null
        adjustDragInClockZone = isInClockZone(position)
    }

    fun onAdjustDrag(change: PointerInputChange, dx: Float) {
        if (adjustPagerOwnsDrag == null && dx != 0f) adjustPagerOwnsDrag = adjustDragInClockZone && clockPager.canMove(dx)
        if (adjustPagerOwnsDrag == true) clockPager.dragBy(dx)
        velocityTracker.addPosition(change.uptimeMillis, change.position)
    }

    fun onAdjustDragEnd() {
        if (adjustPagerOwnsDrag == true) clockPager.settle(velocityTracker.calculateVelocity().x)
        adjustPagerOwnsDrag = null
    }

    fun onAdjustDragCancel() {
        if (adjustPagerOwnsDrag == true) clockPager.settle(0f)
        adjustPagerOwnsDrag = null
    }

    fun onPointerDrag(change: PointerInputChange, dragAmount: Offset) {
        drag(dragAmount.x, dragAmount.y)
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
