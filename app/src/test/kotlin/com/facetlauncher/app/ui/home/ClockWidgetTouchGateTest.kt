package com.facetlauncher.app.ui.home

import android.content.Context
import android.os.SystemClock
import android.view.MotionEvent
import android.view.View
import android.view.ViewConfiguration
import androidx.core.view.ViewCompat
import androidx.test.core.app.ApplicationProvider
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.Shadows.shadowOf
import java.util.concurrent.TimeUnit

@RunWith(RobolectricTestRunner::class)
class ClockWidgetTouchGateTest {

    private lateinit var context: Context
    private lateinit var gate: ClockWidgetTouchGate
    private var longPresses = 0

    @Before
    fun setUp() {
        context = ApplicationProvider.getApplicationContext()
        gate = ClockWidgetTouchGate(context).apply { onLongPress = { longPresses++ } }
    }

    private fun event(action: Int, x: Float = 10f, y: Float = 10f): MotionEvent {
        val now = SystemClock.uptimeMillis()
        return MotionEvent.obtain(now, now, action, x, y, 0)
    }

    private fun advancePastLongPress() {
        shadowOf(android.os.Looper.getMainLooper())
            .idleFor(ViewConfiguration.getLongPressTimeout() + 50L, TimeUnit.MILLISECONDS)
    }

    @Test
    fun `holding still past the long-press timeout fires onLongPress once and starts intercepting`() {
        // Given a finger down on the widget
        gate.onInterceptTouchEvent(event(MotionEvent.ACTION_DOWN))

        // When it stays put past the platform long-press timeout
        advancePastLongPress()

        // Then the long-press fires once and later events are intercepted
        assertEquals(1, longPresses)
        assertTrue(gate.onInterceptTouchEvent(event(MotionEvent.ACTION_MOVE)))
    }

    @Test
    fun `a plain tap is never intercepted and does not long-press`() {
        assertFalse(gate.onInterceptTouchEvent(event(MotionEvent.ACTION_DOWN)))
        assertFalse(gate.onInterceptTouchEvent(event(MotionEvent.ACTION_UP)))

        advancePastLongPress()

        assertEquals(0, longPresses)
    }

    @Test
    fun `moving past touch slop cancels the pending long-press`() {
        val slop = ViewConfiguration.get(context).scaledTouchSlop
        gate.onInterceptTouchEvent(event(MotionEvent.ACTION_DOWN, x = 10f))

        gate.onInterceptTouchEvent(event(MotionEvent.ACTION_MOVE, x = 10f + slop + 5f))
        advancePastLongPress()

        assertEquals(0, longPresses)
    }

    @Test
    fun `movement within touch slop keeps the long-press pending`() {
        gate.onInterceptTouchEvent(event(MotionEvent.ACTION_DOWN, x = 10f))

        gate.onInterceptTouchEvent(event(MotionEvent.ACTION_MOVE, x = 11f))
        advancePastLongPress()

        assertEquals(1, longPresses)
    }

    @Test
    fun `a child disallowing intercept cancels the pending long-press`() {
        gate.onInterceptTouchEvent(event(MotionEvent.ACTION_DOWN))

        gate.requestDisallowInterceptTouchEvent(true)
        advancePastLongPress()

        assertEquals(0, longPresses)
    }

    @Test
    fun `releasing the finger before the timeout cancels the long-press`() {
        gate.onInterceptTouchEvent(event(MotionEvent.ACTION_DOWN))
        gate.onInterceptTouchEvent(event(MotionEvent.ACTION_UP))

        advancePastLongPress()

        assertEquals(0, longPresses)
    }

    @Test
    fun `touch after a recognized long-press is consumed and the release ends interception`() {
        gate.onInterceptTouchEvent(event(MotionEvent.ACTION_DOWN))
        advancePastLongPress()

        assertTrue(gate.onTouchEvent(event(MotionEvent.ACTION_UP)))

        assertFalse(gate.onInterceptTouchEvent(event(MotionEvent.ACTION_MOVE)))
    }

    @Test
    fun `nested scroll leftover is handed to onLeftoverScroll and reported as consumed`() {
        var leftover: Pair<Float, Float>? = null
        gate.onLeftoverScroll = { dx, dy -> leftover = dx to dy }
        val consumed = intArrayOf(0, 0)

        gate.onNestedScroll(View(context), 0, 3, 4, 9, ViewCompat.TYPE_TOUCH, consumed)

        assertEquals(4f to 9f, leftover)
        assertEquals(4, consumed[0])
        assertEquals(9, consumed[1])
    }

    @Test
    fun `nested scroll with nothing unconsumed does not call onLeftoverScroll`() {
        var called = false
        gate.onLeftoverScroll = { _, _ -> called = true }

        gate.onNestedScroll(View(context), 5, 5, 0, 0, ViewCompat.TYPE_TOUCH, intArrayOf(0, 0))

        assertFalse(called)
    }

    @Test
    fun `nested scroll session start and stop notify callbacks`() {
        var started = 0
        var stopped = 0
        gate.onNestedScrollStart = { started++ }
        gate.onNestedScrollStop = { stopped++ }
        val child = View(context)

        assertTrue(gate.onStartNestedScroll(child, child, ViewCompat.SCROLL_AXIS_VERTICAL, ViewCompat.TYPE_TOUCH))
        gate.onNestedScrollAccepted(child, child, ViewCompat.SCROLL_AXIS_VERTICAL, ViewCompat.TYPE_TOUCH)
        gate.onStopNestedScroll(child, ViewCompat.TYPE_TOUCH)

        assertEquals(1, started)
        assertEquals(1, stopped)
    }

    @Test
    fun `a nested scroll on no axis is not accepted`() {
        val child = View(context)

        assertFalse(gate.onStartNestedScroll(child, child, ViewCompat.SCROLL_AXIS_NONE, ViewCompat.TYPE_TOUCH))
    }

    @Test
    fun `an unconsumed nested fling is handed to onLeftoverFling but a consumed one is not`() {
        var fling: Pair<Float, Float>? = null
        gate.onLeftoverFling = { vx, vy -> fling = vx to vy }

        assertFalse(gate.onNestedFling(View(context), 1f, 2f, consumed = true))
        assertEquals(null, fling)

        assertTrue(gate.onNestedFling(View(context), 1f, 2f, consumed = false))
        assertEquals(1f to 2f, fling)
    }
}
