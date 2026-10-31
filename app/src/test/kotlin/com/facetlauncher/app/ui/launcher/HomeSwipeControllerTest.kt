package com.facetlauncher.app.ui.launcher

import androidx.compose.runtime.MonotonicFrameClock
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import com.facetlauncher.app.ui.home.ClockPagerState
import kotlinx.coroutines.CoroutineScope
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

    private var clockAdjustEnded = 0
    private var shadeExpanded = 0

    private var pageCount = 1
    private var pageIndex = 0
    private val clockZone = Rect(0f, 0f, 1000f, 600f)

    private fun controller(width: Float = 1000f, height: Float = 2000f): HomeSwipeController {
        val scope = CoroutineScope(UnconfinedTestDispatcher() + SteppingFrameClock())
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
}
