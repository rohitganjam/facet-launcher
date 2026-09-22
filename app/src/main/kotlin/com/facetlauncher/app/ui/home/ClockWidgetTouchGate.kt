package com.facetlauncher.app.ui.home

import android.content.Context
import android.os.Handler
import android.os.Looper
import android.view.MotionEvent
import android.view.View
import android.view.ViewConfiguration
import android.widget.FrameLayout
import androidx.core.view.NestedScrollingParent3
import androidx.core.view.NestedScrollingParentHelper
import androidx.core.view.ViewCompat

/**
 * Wraps a hosted clock widget's own [android.appwidget.AppWidgetHostView] so a long-press on its
 * rendered surface opens Facet's own clock-adjust sheet instead of letting the widget's own tap
 * action (its `PendingIntent`-backed click, wired internally when `AppWidgetHostView` inflates the
 * provider's `RemoteViews`) fire. A Compose ancestor `pointerInput`/`detectTapGestures` — the
 * mechanism `HomeScreen.kt`'s `clockContentModifier` already uses for the native clock's own
 * long-press — can't reliably win this race: `AndroidView` interop forwards raw `MotionEvent`s
 * straight into the embedded View's own `dispatchTouchEvent`, and a plain View's `OnClickListener`
 * only cares about touch-slop, not hold duration, so it treats "long hold, then release" as a
 * valid tap regardless of how long the finger stayed down (confirmed on-device: a long-press on a
 * hosted widget fired the widget's own tap action instead of opening the sheet).
 *
 * Intercepts touch natively instead, via the same [onInterceptTouchEvent] contract
 * `ScrollView`/`RecyclerView` use to reclaim an in-progress gesture from their children: once a
 * real long-press is recognized (measured against the platform's own
 * [ViewConfiguration.getLongPressTimeout], cancelled by [ViewConfiguration.getScaledTouchSlop]
 * worth of movement like any other gesture), this view starts intercepting, which makes the
 * framework deliver an `ACTION_CANCEL` to the child and route the rest of that gesture (the
 * eventual `ACTION_UP`) here instead — so the widget's own click never fires. A plain tap, or a
 * drag/scroll starting on the widget, is never intercepted and passes through to the child exactly
 * as it would without this wrapper.
 *
 * Also honors [requestDisallowInterceptTouchEvent] — a real bug found on-device: a widget with its
 * own scrollable content (a `ListView`/`GridView`/`StackView` collection inside its `RemoteViews`,
 * the same real internal scroll mechanism `AbsListView` uses everywhere) calls this on its ancestors
 * the moment *it* recognizes a scroll starting, which is the standard Android signal for "back off,
 * I'm handling this gesture myself." Per the platform's own `ViewGroup` contract, once that's
 * called, the framework stops invoking [onInterceptTouchEvent] for the rest of the gesture — so
 * without this override, the pending long-press timer scheduled at `ACTION_DOWN` never gets
 * cancelled by the scroll's own movement (that cancellation lives inside [onInterceptTouchEvent]'s
 * own `ACTION_MOVE` branch, which stops running) and can fire mid-scroll regardless of how far the
 * finger has already moved, hijacking a gesture that was never a long-press to begin with.
 *
 * Also implements [NestedScrollingParent3] — a real bug found on-device: even with the fixes
 * above, a swipe starting *on the widget's own surface* was still fully claimed by Home's own
 * swipe-to-open-drawer/shade gesture (a Compose `pointerInput` living *above* this View in the
 * tree — see [com.facetlauncher.app.ui.home.HomeScreen]'s own `onClockWidgetBoundsChange` doc),
 * even once the widget's list had nothing left to scroll (already at its own top/bottom). The
 * standard `requestDisallowInterceptTouchEvent`-based fix only stops raw-touch conflicts *below*
 * this View; it can't hand anything back to an ancestor *above* it, because that ancestor lives in
 * Compose, not the native View tree, and doesn't speak that protocol at all. Real nested scrolling
 * (the same `NestedScrollingParent`/`NestedScrollingChild` protocol `AbsListView` — so `ListView`/
 * `GridView`/`StackView`, the collection widgets `RemoteViews` supports — already participates in
 * everywhere) is the platform's own answer to exactly this: the child (the widget's own scrollable
 * content) always scrolls itself first, and only the *leftover* it couldn't use (already at an
 * edge) reaches [onLeftoverScroll] here, for the caller to hand off to Home's own swipe axis. If a
 * specific widget's own content never participates in this protocol, these callbacks are simply
 * never invoked — a silent, harmless no-op, not a regression from the behavior these fixes already
 * established (the widget still keeps a swipe starting on it; it just can't hand off the leftover).
 */
class ClockWidgetTouchGate(context: Context) : FrameLayout(context), NestedScrollingParent3 {

    /** Fired once, the moment a long-press is recognized — not on release. */
    var onLongPress: (() -> Unit)? = null

    /** Fired once, when the widget's own scrollable content accepts a nested scroll session that might have leftover to hand off. */
    var onNestedScrollStart: (() -> Unit)? = null

    /**
     * Fired with the real px delta the widget's own scrollable content couldn't use (already at
     * its own scroll boundary) — the caller drives Home's own swipe axis with this instead, same
     * as a direct drag on Home's own empty space would.
     */
    var onLeftoverScroll: ((dxPx: Float, dyPx: Float) -> Unit)? = null

    /** Fired once the nested scroll session ends — settle whichever Home axis was driven. */
    var onNestedScrollStop: (() -> Unit)? = null

    /** Fired with leftover fling velocity the widget's own content couldn't use — lets Home's swipe settle with real momentum, same as a direct drag's own release velocity would. */
    var onLeftoverFling: ((velocityXPx: Float, velocityYPx: Float) -> Unit)? = null

    private val nestedScrollingParentHelper = NestedScrollingParentHelper(this)

    override fun onStartNestedScroll(child: View, target: View, axes: Int, type: Int): Boolean =
        (axes and (ViewCompat.SCROLL_AXIS_VERTICAL or ViewCompat.SCROLL_AXIS_HORIZONTAL)) != 0

    override fun onNestedScrollAccepted(child: View, target: View, axes: Int, type: Int) {
        nestedScrollingParentHelper.onNestedScrollAccepted(child, target, axes, type)
        onNestedScrollStart?.invoke()
    }

    override fun onStopNestedScroll(target: View, type: Int) {
        nestedScrollingParentHelper.onStopNestedScroll(target, type)
        onNestedScrollStop?.invoke()
    }

    // Deliberately a no-op — the widget's own scrollable content always gets first crack at every
    // scroll delta; onNestedScroll (below) only ever hands us its own *leftover*.
    override fun onNestedPreScroll(target: View, dx: Int, dy: Int, consumed: IntArray, type: Int) = Unit

    override fun onNestedScroll(target: View, dxConsumed: Int, dyConsumed: Int, dxUnconsumed: Int, dyUnconsumed: Int, type: Int) {
        onNestedScroll(target, dxConsumed, dyConsumed, dxUnconsumed, dyUnconsumed, type, IntArray(2))
    }

    override fun onNestedScroll(
        target: View,
        dxConsumed: Int,
        dyConsumed: Int,
        dxUnconsumed: Int,
        dyUnconsumed: Int,
        type: Int,
        consumed: IntArray,
    ) {
        if (dxUnconsumed == 0 && dyUnconsumed == 0) return
        onLeftoverScroll?.invoke(dxUnconsumed.toFloat(), dyUnconsumed.toFloat())
        // Reported back as fully used, even though it actually drove Home's own swipe axis rather
        // than scrolling anything in this view — otherwise the widget's own content might also
        // show its own edge-glow/overscroll effect for the same motion, which would look redundant.
        consumed[0] += dxUnconsumed
        consumed[1] += dyUnconsumed
    }

    override fun onNestedFling(target: View, velocityX: Float, velocityY: Float, consumed: Boolean): Boolean {
        if (consumed) return false
        onLeftoverFling?.invoke(velocityX, velocityY)
        return true
    }

    override fun onNestedPreFling(target: View, velocityX: Float, velocityY: Float): Boolean = false

    override fun getNestedScrollAxes(): Int = nestedScrollingParentHelper.nestedScrollAxes

    private val longPressTimeoutMs = ViewConfiguration.getLongPressTimeout().toLong()
    private val touchSlopSquared = ViewConfiguration.get(context).scaledTouchSlop.let { it * it }
    private val handler = Handler(Looper.getMainLooper())
    private var downX = 0f
    private var downY = 0f
    private var intercepting = false

    private val longPressRunnable = Runnable {
        intercepting = true
        onLongPress?.invoke()
    }

    override fun onInterceptTouchEvent(ev: MotionEvent): Boolean {
        when (ev.actionMasked) {
            MotionEvent.ACTION_DOWN -> {
                downX = ev.x
                downY = ev.y
                intercepting = false
                handler.postDelayed(longPressRunnable, longPressTimeoutMs)
            }
            MotionEvent.ACTION_MOVE -> {
                val dx = ev.x - downX
                val dy = ev.y - downY
                if (dx * dx + dy * dy > touchSlopSquared) {
                    handler.removeCallbacks(longPressRunnable)
                }
            }
            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                handler.removeCallbacks(longPressRunnable)
            }
        }
        return intercepting
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        // Only reached once onInterceptTouchEvent has claimed the gesture — consume the rest of it
        // (the eventual ACTION_UP) so nothing beneath ever sees it as a completed click.
        if (event.actionMasked == MotionEvent.ACTION_UP || event.actionMasked == MotionEvent.ACTION_CANCEL) {
            intercepting = false
        }
        return true
    }

    override fun requestDisallowInterceptTouchEvent(disallowIntercept: Boolean) {
        if (disallowIntercept) {
            // A descendant (e.g. the widget's own scrollable list) has claimed this gesture for
            // itself — definitely not a long-press on our own surface, so stop waiting to
            // intercept it. See this class's own doc for why this can't just be left to the
            // ACTION_MOVE branch in onInterceptTouchEvent above.
            handler.removeCallbacks(longPressRunnable)
        }
        super.requestDisallowInterceptTouchEvent(disallowIntercept)
    }
}
