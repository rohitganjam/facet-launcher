package com.facetlauncher.app.ui.home

import androidx.compose.runtime.MonotonicFrameClock
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
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

class ClockPagerStateTest {

    private var pageCount = 3
    private var pageIndex = 1
    private val changes = mutableListOf<Int>()

    private fun pager(width: Float = 1000f) = ClockPagerState(
        pageCount = { pageCount },
        pageIndex = { pageIndex },
        onPageChange = {
            changes += it
            pageIndex = it
        },
        widthPx = { width },
        coroutineScope = CoroutineScope(UnconfinedTestDispatcher() + SteppingFrameClock()),
    )

    @Test
    fun `a drag needs a page in its direction`() {
        // Given the middle page of three
        val pager = pager()

        // Then both directions can move, and a standstill cannot
        assertTrue(pager.canMove(-1f))
        assertTrue(pager.canMove(1f))
        assertFalse(pager.canMove(0f))
    }

    @Test
    fun `the first page has nothing to its right and the last nothing to its left`() {
        pageIndex = 0
        assertFalse(pager().canMove(1f))
        assertTrue(pager().canMove(-1f))

        pageIndex = 2
        assertTrue(pager().canMove(1f))
        assertFalse(pager().canMove(-1f))
    }

    @Test
    fun `a single page cannot move either way`() {
        pageCount = 1
        pageIndex = 0

        assertFalse(pager().canMove(1f))
        assertFalse(pager().canMove(-1f))
    }

    @Test
    fun `an empty stack cannot move`() {
        pageCount = 0
        pageIndex = 0

        assertFalse(pager().canMove(-1f))
    }

    @Test
    fun `dragging follows the finger as a fraction of the width`() {
        val pager = pager(width = 1000f)

        pager.dragBy(-250f)

        assertEquals(-0.25f, pager.offset.value, 0.0001f)
        assertTrue(pager.dragActive)
    }

    @Test
    fun `a drag cannot go past the ends of the stack`() {
        // Given the first page, which has a next page but no previous one
        pageIndex = 0
        val pager = pager(width = 1000f)

        // When dragging right (towards a missing previous page) and then far left
        pager.dragBy(400f)
        val atStart = pager.offset.value
        pager.dragBy(-5000f)

        // Then it stays put one way and stops at a full page the other
        assertEquals(0f, atStart, 0f)
        assertEquals(-1f, pager.offset.value, 0f)
    }

    @Test
    fun `a drag past the commit fraction turns to the next page and ends at rest`() {
        val pager = pager()

        pager.dragBy(-300f)
        pager.settle(velocityPx = 0f)

        assertEquals(listOf(2), changes)
        assertEquals(0f, pager.offset.value, 0f)
        assertFalse(pager.dragActive)
    }

    @Test
    fun `a drag to the right turns to the previous page`() {
        val pager = pager()

        pager.dragBy(300f)
        pager.settle(velocityPx = 0f)

        assertEquals(listOf(0), changes)
        assertEquals(0f, pager.offset.value, 0f)
    }

    @Test
    fun `a short slow drag springs back without changing the page`() {
        val pager = pager()

        pager.dragBy(-100f)
        pager.settle(velocityPx = 0f)

        assertTrue(changes.isEmpty())
        assertEquals(0f, pager.offset.value, 0f)
    }

    @Test
    fun `a fast fling turns the page even after a short drag`() {
        val pager = pager()

        pager.dragBy(-50f)
        pager.settle(velocityPx = -4000f)

        assertEquals(listOf(2), changes)
    }

    @Test
    fun `a fling against the drag direction wins over the distance`() {
        // Given a drag most of the way left, released with a hard fling back to the right
        val pager = pager()

        pager.dragBy(-300f)
        pager.settle(velocityPx = 4000f)

        // Then it goes to the previous page, as the fling says
        assertEquals(listOf(0), changes)
    }

    @Test
    fun `settling at the end of the stack springs back instead of changing the page`() {
        // Given the last page, released with a left fling that has no page to go to
        pageIndex = 2
        val pager = pager()

        pager.settle(velocityPx = -4000f)

        assertTrue(changes.isEmpty())
        assertEquals(0f, pager.offset.value, 0f)
    }

    @Test
    fun `the old page stays out until the new one has replaced it`() {
        // Given a page change that only lands on the fifth look at the index, as the ViewModel's round trip does
        var reads = 0
        var landed = 1
        var requested = 1
        lateinit var pager: ClockPagerState
        var offsetWhenLanded = Float.NaN
        pager = ClockPagerState(
            pageCount = { 3 },
            pageIndex = {
                if (++reads > 5 && landed != requested) {
                    landed = requested
                    offsetWhenLanded = pager.offset.value
                }
                landed
            },
            onPageChange = { requested = it },
            widthPx = { 1000f },
            coroutineScope = CoroutineScope(UnconfinedTestDispatcher() + SteppingFrameClock()),
        )

        // When a drag past the commit fraction to the left is released
        pager.dragBy(-400f)
        pager.settle(0f)

        // Then the page was still fully out (offset at the edge) when the new one landed, and it then slid in to rest
        assertEquals(-1f, offsetWhenLanded, 0f)
        assertEquals(2, landed)
        assertEquals(0f, pager.offset.value, 0f)
    }

    @Test
    fun `swipes are measured from the page they began on until the page has landed`() {
        // Given the middle page, where a drag begins
        val pager = pager()
        assertEquals(-1, pager.anchorIndex)
        pager.dragBy(-100f)

        // Then the swipe is anchored to that page
        assertEquals(1, pager.anchorIndex)

        // And once it has settled on the next page the anchor is cleared
        pager.settle(-5000f)
        assertEquals(2, pageIndex)
        assertEquals(-1, pager.anchorIndex)
    }

    @Test
    fun `a swipe that springs back clears the anchor`() {
        val pager = pager()

        pager.dragBy(-100f)
        pager.settle(0f)

        assertEquals(1, pageIndex)
        assertEquals(-1, pager.anchorIndex)
    }
}
