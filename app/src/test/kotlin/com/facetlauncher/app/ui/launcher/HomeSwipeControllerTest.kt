package com.facetlauncher.app.ui.launcher

import androidx.compose.runtime.MonotonicFrameClock
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.input.pointer.PointerId
import androidx.compose.ui.input.pointer.PointerInputChange
import androidx.compose.ui.input.pointer.PointerType
import androidx.compose.ui.unit.IntOffset
import com.facetlauncher.app.data.model.SwipeAction
import com.facetlauncher.app.data.model.SwipeActions
import com.facetlauncher.app.data.model.SwipeDirection
import com.facetlauncher.app.ui.home.ClockPagerState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.test.TestCoroutineScheduler
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/** A frame clock that ticks 16ms per frame as soon as it is asked, so settle animations finish inside the test. */
private class SteppingFrameClock : MonotonicFrameClock {
    private var nanos = 0L

    override suspend fun <R> withFrameNanos(onFrame: (Long) -> R): R {
        nanos += 16_000_000L
        return onFrame(nanos)
    }
}

class HomeSwipeControllerTest {

    private val testScheduler = TestCoroutineScheduler()
    private var clockAdjustEnded = 0
    private var shadeExpanded = 0

    private var pageCount = 1
    private var pageIndex = 0
    private val clockZone = Rect(0f, 0f, 1000f, 600f)

    private fun controller(width: Float = 1000f, height: Float = 2000f): HomeSwipeController {
        val scope = CoroutineScope(UnconfinedTestDispatcher(testScheduler) + SteppingFrameClock())
        return HomeSwipeController(
            containerHeightPx = { height },
            containerWidthPx = { width },
            coroutineScope = scope,
            endClockAdjust = { clockAdjustEnded++ },
            expandNotificationShade = { shadeExpanded++ },
            clockPager = ClockPagerState(
                pageCount = { pageCount },
                pageIndex = { pageIndex },
                onPageChange = { pageIndex = it },
                widthPx = { width },
                coroutineScope = scope,
            ),
            isInClockZone = { clockZone.contains(it) },
        )
    }

    @Test
    fun `a mostly vertical drag up pulls the drawer open and leaves the other panels shut`() {
        val swipe = controller()

        swipe.start()
        swipe.drag(dx = 10f, dy = -400f)

        assertEquals(0.2f, swipe.drawer.progress.value, 0.0001f)
        assertEquals(0f, swipe.hub.progress.value, 0f)
        assertEquals(0f, swipe.facet.progress.value, 0f)
    }

    @Test
    fun `a mostly horizontal drag right pulls the hub and left pulls the facet carousel`() {
        val right = controller()
        right.start()
        right.drag(dx = 300f, dy = 10f)

        val left = controller()
        left.start()
        left.drag(dx = -300f, dy = 10f)

        assertEquals(0.3f, right.hub.progress.value, 0.0001f)
        assertEquals(0f, right.facet.progress.value, 0f)
        assertEquals(0.3f, left.facet.progress.value, 0.0001f)
        assertEquals(0f, left.hub.progress.value, 0f)
        assertEquals(0f, right.drawer.progress.value, 0f)
    }

    @Test
    fun `a horizontal drag keeps driving the panel it engaged first even when it reverses`() {
        val swipe = controller()

        swipe.start()
        swipe.drag(dx = 200f, dy = 0f)
        swipe.drag(dx = -100f, dy = 0f)

        assertEquals(0.1f, swipe.hub.progress.value, 0.0001f)
        assertEquals(0f, swipe.facet.progress.value, 0f)
    }

    @Test
    fun `the first move decides the axis for the whole gesture`() {
        val swipe = controller()

        swipe.start()
        swipe.drag(dx = 5f, dy = -100f)
        swipe.drag(dx = 400f, dy = 0f)

        assertEquals(0f, swipe.hub.progress.value, 0f)
        assertTrue(swipe.drawer.progress.value > 0f)
    }

    @Test
    fun `moving a panel ends clock adjust mode`() {
        val swipe = controller()

        swipe.start()
        swipe.drag(dx = 0f, dy = -50f)

        assertTrue(clockAdjustEnded > 0)
    }

    @Test
    fun `releasing a long drag from closed opens the drawer`() {
        val swipe = controller()

        swipe.start()
        swipe.drag(dx = 0f, dy = -600f)
        swipe.settle(velocity = 0f)

        assertEquals(1f, swipe.drawer.progress.value, 0f)
    }

    @Test
    fun `releasing a short drag springs the drawer back shut`() {
        val swipe = controller()

        swipe.start()
        swipe.drag(dx = 0f, dy = -100f)
        swipe.settle(velocity = 0f)

        assertEquals(0f, swipe.drawer.progress.value, 0f)
    }

    @Test
    fun `a long downward drag from a closed drawer expands the notification shade`() {
        val swipe = controller(height = 2000f)

        swipe.start()
        swipe.drag(dx = 0f, dy = 500f)
        swipe.settle(velocity = 0f)

        assertEquals(1, shadeExpanded)
    }

    @Test
    fun `a downward drag fills the shade pull and is fully faded at the commit distance`() {
        val swipe = controller(height = 2000f)

        swipe.start()
        swipe.drag(dx = 0f, dy = 200f)
        val halfway = swipe.shade.progress.value
        swipe.drag(dx = 0f, dy = 400f)

        assertEquals(0.5f, halfway, 0.0001f)
        assertEquals(1f, swipe.shade.progress.value, 0f)
        assertEquals(0f, homeLayerFade(swipe.shade.progress.value, 0f, 0f).alpha, 0f)
    }

    @Test
    fun `an uncommitted shade pull returns to zero right after release`() {
        val short = controller(height = 2000f)
        short.start()
        short.drag(dx = 0f, dy = 100f)
        short.settle(velocity = 0f)

        assertEquals(0f, short.shade.progress.value, 0f)
    }

    @Test
    fun `a committed shade pull stays faded while the shade opens then returns`() {
        val long = controller(height = 2000f)
        long.start()
        long.drag(dx = 0f, dy = 500f)
        long.settle(velocity = 0f)

        val heldAfterRelease = long.shade.progress.value
        testScheduler.advanceTimeBy(SHADE_OPEN_HOLD_MS + 1)
        testScheduler.runCurrent()

        assertEquals(1f, heldAfterRelease, 0f)
        assertEquals(0f, long.shade.progress.value, 0f)
    }

    @Test
    fun `a cancelled downward drag returns the shade pull to zero`() {
        val swipe = controller(height = 2000f)

        swipe.start()
        swipe.drag(dx = 0f, dy = 300f)
        swipe.cancel()

        assertEquals(0f, swipe.shade.progress.value, 0f)
    }

    @Test
    fun `a downward drag does not pull the shade when swipe down is none or the drawer started open`() {
        val off = controller(height = 2000f).also { it.actions = SwipeActions(down = SwipeAction.NONE) }
        off.start()
        off.drag(dx = 0f, dy = 300f)

        val open = controller(height = 2000f)
        open.start()
        open.drag(dx = 0f, dy = -1000f)
        open.settle(velocity = 0f)
        open.start()
        open.drag(dx = 0f, dy = 300f)

        assertEquals(0f, off.shade.progress.value, 0f)
        assertEquals(0f, open.shade.progress.value, 0f)
    }

    @Test
    fun `a short slow downward drag does not expand the shade`() {
        val swipe = controller(height = 2000f)

        swipe.start()
        swipe.drag(dx = 0f, dy = 100f)
        swipe.settle(velocity = 0f)

        assertEquals(0, shadeExpanded)
    }

    @Test
    fun `a fast downward fling expands the shade even over a short drag`() {
        val swipe = controller()

        swipe.start()
        swipe.drag(dx = 0f, dy = 100f)
        swipe.settle(velocity = VELOCITY_THRESHOLD_PX + 1f)

        assertEquals(1, shadeExpanded)
    }

    @Test
    fun `a downward drag that began with the drawer open never expands the shade`() {
        val swipe = controller()
        swipe.start()
        swipe.drag(dx = 0f, dy = -800f)
        swipe.settle(velocity = 0f)

        swipe.start()
        swipe.drag(dx = 0f, dy = 900f)
        swipe.settle(velocity = 0f)

        assertEquals(0, shadeExpanded)
    }

    @Test
    fun `a settle with no drag in progress does nothing`() {
        val swipe = controller()

        swipe.settle(velocity = 5000f)
        swipe.cancel()

        assertEquals(0f, swipe.drawer.progress.value, 0f)
        assertEquals(0, shadeExpanded)
    }

    @Test
    fun `a cancelled horizontal drag settles the panel it was moving`() {
        val swipe = controller()

        swipe.start()
        swipe.drag(dx = 100f, dy = 0f)
        swipe.cancel()

        assertEquals(0f, swipe.hub.progress.value, 0f)
    }

    @Test
    fun `a widget scroll that ends without a fling settles the drag`() {
        val swipe = controller()

        swipe.widgetScroll.start()
        swipe.widgetScroll.leftoverScroll(dxPx = 0f, dyPx = 100f)
        swipe.widgetScroll.stop()

        assertEquals(0f, swipe.drawer.progress.value, 0f)
    }

    @Test
    fun `a widget scroll negates its scroll offsets into finger movement`() {
        val swipe = controller()

        swipe.widgetScroll.start()
        // A scroll offset of +400 down the content is the finger moving 400 up
        swipe.widgetScroll.leftoverScroll(dxPx = 0f, dyPx = 400f)

        assertEquals(0.2f, swipe.drawer.progress.value, 0.0001f)
    }

    @Test
    fun `a widget fling decides the outcome and the stop that follows does not undo it`() {
        val swipe = controller()

        swipe.widgetScroll.start()
        swipe.widgetScroll.leftoverScroll(dxPx = 0f, dyPx = 100f)
        swipe.widgetScroll.leftoverFling(velocityXPx = 0f, velocityYPx = -(VELOCITY_THRESHOLD_PX + 500f))
        swipe.widgetScroll.stop()

        assertEquals(1f, swipe.drawer.progress.value, 0f)
    }

    private fun clockDrag(swipe: HomeSwipeController, dx: Float, down: Offset = Offset(500f, 300f), velocity: Float = 0f) {
        swipe.onPointerDragStart(down)
        swipe.drag(dx = dx, dy = 5f)
        swipe.settle(velocity)
    }

    @Test
    fun `a horizontal drag in the clock zone turns the page and leaves Hub and the carousel shut`() {
        // Given a three-page stack on the middle page
        pageCount = 3
        pageIndex = 1
        val swipe = controller()

        // When dragging left past the commit distance inside the clock zone
        clockDrag(swipe, dx = -300f)

        // Then the next page shows and neither panel moved
        assertEquals(2, pageIndex)
        assertEquals(0f, swipe.facet.progress.value, 0f)
        assertEquals(0f, swipe.hub.progress.value, 0f)
    }

    @Test
    fun `dragging right in the clock zone turns to the previous page`() {
        pageCount = 3
        pageIndex = 1
        val swipe = controller()

        clockDrag(swipe, dx = 300f)

        assertEquals(0, pageIndex)
        assertEquals(0f, swipe.hub.progress.value, 0f)
    }

    @Test
    fun `at the last page a left drag in the clock zone goes to the facet carousel`() {
        // Given the last page of a stack
        pageCount = 3
        pageIndex = 2
        val swipe = controller()

        // When dragging left in the clock zone
        swipe.onPointerDragStart(Offset(500f, 300f))
        swipe.drag(dx = -300f, dy = 5f)

        // Then the carousel follows and the page did not change
        assertEquals(0.3f, swipe.facet.progress.value, 0.0001f)
        assertEquals(2, pageIndex)
    }

    @Test
    fun `at the first page a right drag in the clock zone goes to the hub`() {
        pageCount = 3
        pageIndex = 0
        val swipe = controller()

        swipe.onPointerDragStart(Offset(500f, 300f))
        swipe.drag(dx = 300f, dy = 5f)

        assertEquals(0.3f, swipe.hub.progress.value, 0.0001f)
        assertEquals(0, pageIndex)
    }

    @Test
    fun `a drag outside the clock zone ignores the pager`() {
        // Given a stack with pages both ways, and a finger that starts below the divider
        pageCount = 3
        pageIndex = 1
        val swipe = controller()

        // When dragging left there
        swipe.onPointerDragStart(Offset(500f, 1200f))
        swipe.drag(dx = -300f, dy = 5f)

        // Then it is the carousel's drag as before
        assertEquals(0.3f, swipe.facet.progress.value, 0.0001f)
        assertEquals(1, pageIndex)
    }

    @Test
    fun `a stack of one page never takes a drag`() {
        pageCount = 1
        pageIndex = 0
        val swipe = controller()

        swipe.onPointerDragStart(Offset(500f, 300f))
        swipe.drag(dx = -300f, dy = 5f)

        assertEquals(0.3f, swipe.facet.progress.value, 0.0001f)
    }

    @Test
    fun `a drag the pager took keeps belonging to it when the finger reverses`() {
        pageCount = 3
        pageIndex = 1
        val swipe = controller()

        swipe.onPointerDragStart(Offset(500f, 300f))
        swipe.drag(dx = -200f, dy = 0f)
        swipe.drag(dx = 100f, dy = 0f)

        assertEquals(-0.1f, swipe.clockPager.offset.value, 0.0001f)
        assertEquals(0f, swipe.hub.progress.value, 0f)
        assertEquals(0f, swipe.facet.progress.value, 0f)
    }

    @Test
    fun `a drag handed over by a hosted widget starts in the clock zone`() {
        // Given a widget's nested scroll handing over a leftover drag at the last page's neighbour
        pageCount = 3
        pageIndex = 1
        val swipe = controller()

        // When it starts and drags left (a scroll offset is the negation of the finger)
        swipe.widgetScroll.start()
        swipe.widgetScroll.leftoverScroll(dxPx = 300f, dyPx = 0f)
        swipe.widgetScroll.stop()

        // Then the page turned
        assertEquals(2, pageIndex)
    }

    @Test
    fun `a fast fling from a widget turns the page even after a short drag`() {
        pageCount = 3
        pageIndex = 1
        val swipe = controller()

        swipe.widgetScroll.start()
        swipe.widgetScroll.leftoverScroll(dxPx = 100f, dyPx = 0f)
        swipe.widgetScroll.leftoverFling(velocityXPx = 4000f, velocityYPx = 0f)

        assertEquals(2, pageIndex)
    }

    private fun change(x: Float, timeMs: Long) = PointerInputChange(
        id = PointerId(0), uptimeMillis = timeMs, position = Offset(x, 100f), pressed = true,
        previousUptimeMillis = timeMs - 16, previousPosition = Offset(x, 100f), previousPressed = true,
        isInitiallyConsumed = false, type = PointerType.Touch,
    )

    @Test
    fun `while adjusting a drag in the clock zone turns the page and never ends the adjust or opens a panel`() {
        pageCount = 3
        val swipe = controller()

        swipe.onAdjustDragStart(Offset(500f, 100f))
        swipe.onAdjustDrag(change(500f, 16), dx = -600f)
        swipe.onAdjustDragEnd()

        assertEquals(1, pageIndex)
        assertEquals(0, clockAdjustEnded)
        assertEquals(0f, swipe.facet.progress.value, 0f)
        assertEquals(0f, swipe.hub.progress.value, 0f)
    }

    @Test
    fun `while adjusting a drag past the last page goes nowhere`() {
        pageCount = 2
        pageIndex = 1
        val swipe = controller()

        swipe.onAdjustDragStart(Offset(500f, 100f))
        swipe.onAdjustDrag(change(500f, 16), dx = -600f)
        swipe.onAdjustDragEnd()

        assertEquals(1, pageIndex)
        assertEquals(0f, swipe.facet.progress.value, 0f)
    }

    @Test
    fun `while adjusting a drag outside the clock zone is ignored`() {
        pageCount = 3
        val swipe = controller()

        swipe.onAdjustDragStart(Offset(500f, 1500f))
        swipe.onAdjustDrag(change(500f, 16), dx = -600f)
        swipe.onAdjustDragEnd()

        assertEquals(0, pageIndex)
    }

    @Test
    fun `a swipe set to none opens nothing but the other side still works`() {
        // Given swipe left set to None
        val actions = SwipeActions(left = SwipeAction.NONE)
        val swipe = controller().also { it.actions = actions }

        // When swiping left
        swipe.start()
        swipe.drag(dx = -300f, dy = 10f)

        // Then nothing moves
        assertEquals(0f, swipe.facet.progress.value, 0f)
        assertEquals(0f, swipe.hub.progress.value, 0f)

        // And swiping right still opens the hub
        val other = controller().also { it.actions = actions }
        other.start()
        other.drag(dx = 300f, dy = 10f)
        assertEquals(0.3f, other.hub.progress.value, 0.0001f)
    }

    @Test
    fun `a drag first toward a none swipe engages the other direction once it reverses`() {
        // Given swipe left set to None
        val swipe = controller().also { it.actions = SwipeActions(left = SwipeAction.NONE) }

        // When the drag goes left first, then right
        swipe.start()
        swipe.drag(dx = -100f, dy = 0f)
        swipe.drag(dx = 300f, dy = 0f)

        // Then the hub follows the rightward part
        assertEquals(0.3f, swipe.hub.progress.value, 0.0001f)
    }

    @Test
    fun `the app drawer on swipe left slides in from the right and opens on release`() {
        // Given the drawer on swipe left
        val swipe = controller(width = 1000f).also { it.actions = SwipeActions(up = SwipeAction.NONE, left = SwipeAction.APP_DRAWER) }

        // When swiping left far enough
        swipe.start()
        swipe.drag(dx = -400f, dy = 0f)
        val during = swipe.drawer.progress.value
        swipe.settle(velocity = 0f)

        // Then the drawer follows the finger by the screen's width, comes from the left swipe's side, and opens
        assertEquals(0.4f, during, 0.0001f)
        assertEquals(SwipeDirection.LEFT, swipe.drawerDirection)
        assertEquals(1f, swipe.drawer.progress.value, 0f)
        assertEquals(0f, swipe.facet.progress.value, 0f)
    }

    @Test
    fun `the hub on swipe up opens from below and swipe up no longer opens the drawer`() {
        // Given the hub on swipe up and the drawer on swipe right
        val swipe = controller(height = 2000f).also { it.actions = SwipeActions(up = SwipeAction.HUB, right = SwipeAction.APP_DRAWER) }

        // When swiping up
        swipe.start()
        swipe.drag(dx = 0f, dy = -400f)

        // Then the hub follows by the screen's height and the drawer stays shut
        assertEquals(0.2f, swipe.hub.progress.value, 0.0001f)
        assertEquals(SwipeDirection.UP, swipe.hubDirection)
        assertEquals(0f, swipe.drawer.progress.value, 0f)
    }

    @Test
    fun `the shade on swipe up expands after a long upward drag`() {
        // Given the shade on swipe up and the drawer on swipe down
        val swipe = controller(height = 2000f).also {
            it.actions = SwipeActions(up = SwipeAction.NOTIFICATION_SHADE, down = SwipeAction.APP_DRAWER)
        }

        // When swiping up past the shade distance
        swipe.start()
        swipe.drag(dx = 0f, dy = -500f)
        swipe.settle(velocity = 0f)

        // Then the shade expands and the drawer stays shut
        assertEquals(1, shadeExpanded)
        assertEquals(0f, swipe.drawer.progress.value, 0f)
    }

    @Test
    fun `an action on two swipes slides in from whichever one opened it`() {
        // Given the drawer on both swipe up and swipe down
        val swipe = controller(height = 2000f).also { it.actions = SwipeActions(down = SwipeAction.APP_DRAWER) }

        // When swiping down
        swipe.start()
        swipe.drag(dx = 0f, dy = 400f)

        // Then the drawer comes from above
        assertEquals(SwipeDirection.DOWN, swipe.drawerDirection)
        assertEquals(0.2f, swipe.drawer.progress.value, 0.0001f)
        assertEquals(0f, swipe.shade.progress.value, 0f)

        // And after it closes, a swipe up brings it from below again
        swipe.settle(velocity = -(VELOCITY_THRESHOLD_PX + 1f))
        assertEquals(0f, swipe.drawer.progress.value, 0f)
        swipe.start()
        swipe.drag(dx = 0f, dy = -400f)
        assertEquals(SwipeDirection.UP, swipe.drawerDirection)
    }

    @Test
    fun `a panel whose swipe is reassigned goes back to its first remaining swipe`() {
        // Given the drawer last opened from swipe down
        val swipe = controller().also { it.actions = SwipeActions(down = SwipeAction.APP_DRAWER) }
        swipe.start()
        swipe.drag(dx = 0f, dy = 400f)
        swipe.settle(velocity = 0f)

        // When swipe down becomes the shade again
        swipe.actions = SwipeActions()

        // Then the drawer slides from below again
        assertEquals(SwipeDirection.UP, swipe.drawerDirection)
    }

    @Test
    fun `a fast horizontal fling opens the hub even over a short drag`() {
        val swipe = controller()

        swipe.start()
        swipe.drag(dx = 50f, dy = 0f)
        swipe.settle(velocity = VELOCITY_THRESHOLD_PX + 1f)

        assertEquals(1f, swipe.hub.progress.value, 0f)
    }

    @Test
    fun `a long swipe down opens the shade unless swipe down is none`() {
        val on = controller()
        on.start()
        on.drag(dx = 0f, dy = 500f)
        on.settle(velocity = 0f)

        val off = controller().also { it.actions = SwipeActions(down = SwipeAction.NONE) }
        off.start()
        off.drag(dx = 0f, dy = 500f)
        off.settle(velocity = 0f)

        assertEquals(1, shadeExpanded)
    }

    @Test
    fun `panel offsets put each panel off the edge its swipe pulls it from`() {
        assertEquals(IntOffset(0, 2000), panelOffset(SwipeDirection.UP, 0f, 1000f, 2000f))
        assertEquals(IntOffset(0, -1000), panelOffset(SwipeDirection.DOWN, 0.5f, 1000f, 2000f))
        assertEquals(IntOffset(1000, 0), panelOffset(SwipeDirection.LEFT, 0f, 1000f, 2000f))
        assertEquals(IntOffset(-500, 0), panelOffset(SwipeDirection.RIGHT, 0.5f, 1000f, 2000f))
        assertEquals(IntOffset(0, 0), panelOffset(SwipeDirection.RIGHT, 1f, 1000f, 2000f))
    }
}
