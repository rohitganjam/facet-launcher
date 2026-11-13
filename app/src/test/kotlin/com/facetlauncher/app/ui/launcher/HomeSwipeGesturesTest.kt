package com.facetlauncher.app.ui.launcher

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
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

    @Test
    fun `a touch on a Facet widget page is claimed only for horizontal drags so the stack still turns`() {
        // Given an Agenda page in the clock zone
        val widget = Rect(0f, 100f, 400f, 400f)

        // When a touch comes down on it
        val claim = homeTouchClaim(Offset(200f, 250f), widget, clockWidgetIsNative = true, appList = null, clockPane = null)

        // Then Home takes a horizontal drag and leaves a vertical one to the widget's own list
        assertEquals(HomeTouchClaim.HORIZONTAL_ONLY, claim)
    }

    @Test
    fun `a touch on a hosted widget page is left to the widget`() {
        // Given a hosted widget page in the clock zone
        val widget = Rect(0f, 100f, 400f, 400f)

        // When a touch comes down on it
        val claim = homeTouchClaim(Offset(200f, 250f), widget, clockWidgetIsNative = false, appList = null, clockPane = null)

        // Then Home does not watch it: the widget's touch gate hands over what it does not use
        assertEquals(HomeTouchClaim.NONE, claim)
    }

    @Test
    fun `a touch on the app list or the landscape clock pane is claimed only for horizontal drags`() {
        // Given the app list and a clock pane, with a hosted widget inside the pane
        val appList = Rect(0f, 500f, 400f, 900f)
        val pane = Rect(400f, 0f, 800f, 400f)
        val widget = Rect(450f, 50f, 750f, 350f)

        // When touches come down on the list and on the widget in the pane
        val onList = homeTouchClaim(Offset(200f, 700f), widget, clockWidgetIsNative = false, appList = appList, clockPane = pane)
        val onPaneWidget = homeTouchClaim(Offset(600f, 200f), widget, clockWidgetIsNative = false, appList = appList, clockPane = pane)

        // Then both are horizontal-only
        assertEquals(HomeTouchClaim.HORIZONTAL_ONLY, onList)
        assertEquals(HomeTouchClaim.HORIZONTAL_ONLY, onPaneWidget)
    }

    @Test
    fun `a touch on free space is claimed for every direction`() {
        // Given a widget page and the app list, neither under the touch
        val widget = Rect(0f, 100f, 400f, 400f)
        val appList = Rect(0f, 500f, 400f, 900f)

        // When a touch comes down between them
        val claim = homeTouchClaim(Offset(200f, 450f), widget, clockWidgetIsNative = true, appList = appList, clockPane = null)

        // Then Home takes the drag whichever way it goes
        assertEquals(HomeTouchClaim.ALL, claim)
    }
}
