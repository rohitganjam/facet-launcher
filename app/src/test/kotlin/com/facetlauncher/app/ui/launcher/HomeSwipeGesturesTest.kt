package com.facetlauncher.app.ui.launcher

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class HomeSwipeGesturesTest {

    @Test
    fun `a fast fling up opens whatever the drag distance`() {
        assertEquals(1f, swipeSettleTarget(dragStartProgress = 0f, currentProgress = 0.01f, velocity = VELOCITY_THRESHOLD_PX + 1f), 0f)
    }

    @Test
    fun `a fast fling down closes even a nearly open panel`() {
        assertEquals(0f, swipeSettleTarget(dragStartProgress = 1f, currentProgress = 0.95f, velocity = -(VELOCITY_THRESHOLD_PX + 1f)), 0f)
    }

    @Test
    fun `a slow drag that travels far enough commits open`() {
        assertEquals(1f, swipeSettleTarget(dragStartProgress = 0f, currentProgress = COMMIT_TRAVEL_FRACTION, velocity = 0f), 0f)
    }

    @Test
    fun `a slow drag that travels far enough back commits closed`() {
        assertEquals(0f, swipeSettleTarget(dragStartProgress = 1f, currentProgress = 1f - COMMIT_TRAVEL_FRACTION - 0.001f, velocity = 0f), 0f)
    }

    @Test
    fun `a short drag from closed springs back closed`() {
        assertEquals(0f, swipeSettleTarget(dragStartProgress = 0f, currentProgress = COMMIT_TRAVEL_FRACTION - 0.01f, velocity = 0f), 0f)
    }

    @Test
    fun `a short drag from open springs back open`() {
        assertEquals(1f, swipeSettleTarget(dragStartProgress = 1f, currentProgress = 1f - COMMIT_TRAVEL_FRACTION + 0.01f, velocity = 0f), 0f)
    }

    @Test
    fun `a velocity just under the threshold does not decide the outcome`() {
        assertEquals(0f, swipeSettleTarget(dragStartProgress = 0f, currentProgress = 0.05f, velocity = VELOCITY_THRESHOLD_PX), 0f)
    }

    private fun axis(containerPx: Float = 1000f) = SwipeAxisState({ containerPx }, CoroutineScope(UnconfinedTestDispatcher()))

    @Test
    fun `dragging moves progress by the fraction of the container`() {
        val axis = axis(containerPx = 1000f)

        axis.dragBy(250f)

        assertEquals(0.25f, axis.progress.value, 0.0001f)
        assertTrue(axis.dragActive)
    }

    @Test
    fun `progress is kept between closed and open`() {
        val axis = axis(containerPx = 1000f)

        axis.dragBy(5000f)
        assertEquals(1f, axis.progress.value, 0f)

        axis.dragBy(-9000f)
        assertEquals(0f, axis.progress.value, 0f)
    }

    @Test
    fun `a drag remembers where it started`() {
        val axis = axis(containerPx = 1000f)

        axis.dragBy(100f)
        axis.dragBy(150f)

        assertEquals(0f, axis.dragStartProgress, 0f)
        assertEquals(0.25f, axis.dragTargetProgress, 0.0001f)
    }

    @Test
    fun `an unmeasured container does not move the axis`() {
        val axis = axis(containerPx = 0f)

        axis.dragBy(300f)

        assertEquals(0f, axis.progress.value, 0f)
        assertFalse(axis.progress.isRunning)
    }
}
