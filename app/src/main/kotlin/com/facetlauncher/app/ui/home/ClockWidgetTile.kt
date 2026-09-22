package com.facetlauncher.app.ui.home

import android.appwidget.AppWidgetHostView
import android.content.Context
import android.view.View
import android.widget.FrameLayout
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.key
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.viewinterop.AndroidView

/**
 * PRD F15's hosted clock widget tile — a clock-specific analog of
 * [com.facetlauncher.app.ui.hub.HubWidgetTile], not a reuse of it, for two reasons specific to
 * this surface (both found on real-device testing, not theoretical):
 *
 * 1. **Long-press must reach Facet's own clock-adjust sheet, not the widget's own tap action.**
 *    Wraps the widget in [ClockWidgetTouchGate] — see its own doc for why a Compose ancestor
 *    gesture detector (what the native clock itself uses) can't reliably win this against a real
 *    hosted View tree.
 * 2. **The real system-level size push must not fire on every drag frame.** [committedWidthDp]/
 *    [committedHeightDp] — not this composable's own [modifier]-driven box size — drive
 *    [AppWidgetHostView.updateAppWidgetSize]/[onSizeChange]; the box itself can resize live, every
 *    frame, for a smooth drag feel (cheap — just Compose layout). Confirmed on-device: feeding the
 *    *live* per-frame drag size into `updateAppWidgetOptions` on every touch-move event floods the
 *    widget's own provider process with rapid, out-of-order size-change IPC calls — some providers
 *    respond by giving up and showing "Can't show content," not just for that drag but for the
 *    rest of that `AppWidgetHost` id's lifetime. Real launchers show a live-resize *preview*
 *    during the drag and only commit the provider-visible reflow once, on release; this mirrors
 *    that — the caller is responsible for only changing [committedWidthDp]/[committedHeightDp] on
 *    commit (see `HomeScreen.kt`'s own `effectiveClockWidgetSizePx` vs. its raw incoming
 *    `clockWidgetWidthDp`/`HeightDp` params).
 *
 * [appWidgetId] is wrapped in [key] — a real bug found on-device: `AndroidView`'s `factory` only
 * ever runs once per composable "slot" (like `remember`'s own semantics); it does **not** re-run
 * just because a parameter it closed over — here, [appWidgetId] itself — changed. Without [key],
 * switching to a different widget kept showing the *old* `AppWidgetHostView` until Home happened to
 * leave and re-enter composition for an unrelated reason (e.g. navigating away and back), which
 * tore this composable down and rebuilt it fresh. [key] makes that identity change explicit, so
 * Compose tears down the old `AndroidView` (and calls [createHostView] again for the new id)
 * immediately on a switch, not incidentally on the next unrelated recomposition.
 *
 * [onNestedScrollStart]/[onLeftoverScroll]/[onNestedScrollStop]/[onLeftoverFling] surface
 * [ClockWidgetTouchGate]'s own `NestedScrollingParent3` callbacks — see its own doc for why a
 * swipe starting on the widget's surface needs this real platform protocol, not just the
 * touch-interception fixes above, to correctly hand off to Home's own swipe axis only once the
 * widget's own scrollable content genuinely has nothing left to do with it.
 */
@Composable
fun ClockWidgetTile(
    appWidgetId: Int,
    committedWidthDp: Dp,
    committedHeightDp: Dp,
    createHostView: (Context, Int) -> AppWidgetHostView?,
    onSizeChange: (appWidgetId: Int, widthDp: Int, heightDp: Int) -> Unit,
    onLongPress: () -> Unit,
    modifier: Modifier = Modifier,
    onNestedScrollStart: () -> Unit = {},
    onLeftoverScroll: (dxPx: Float, dyPx: Float) -> Unit = { _, _ -> },
    onNestedScrollStop: () -> Unit = {},
    onLeftoverFling: (velocityXPx: Float, velocityYPx: Float) -> Unit = { _, _ -> },
) = key(appWidgetId) {
    var hostView: AppWidgetHostView? = remember { null }
    val currentOnLongPress = rememberUpdatedState(onLongPress)
    val currentOnNestedScrollStart = rememberUpdatedState(onNestedScrollStart)
    val currentOnLeftoverScroll = rememberUpdatedState(onLeftoverScroll)
    val currentOnNestedScrollStop = rememberUpdatedState(onNestedScrollStop)
    val currentOnLeftoverFling = rememberUpdatedState(onLeftoverFling)

    // Explicit `this.` throughout — this composable's own params share names with
    // ClockWidgetTouchGate's own properties, and assignment-target resolution shouldn't be left to
    // rely on which scope wins.
    fun ClockWidgetTouchGate.bindCallbacks() {
        this.onLongPress = { currentOnLongPress.value() }
        this.onNestedScrollStart = { currentOnNestedScrollStart.value() }
        this.onLeftoverScroll = { dx, dy -> currentOnLeftoverScroll.value(dx, dy) }
        this.onNestedScrollStop = { currentOnNestedScrollStop.value() }
        this.onLeftoverFling = { vx, vy -> currentOnLeftoverFling.value(vx, vy) }
    }

    AndroidView(
        modifier = modifier.fillMaxSize().testTag("clock_widget_host_$appWidgetId"),
        factory = { context ->
            val gate = ClockWidgetTouchGate(context)
            gate.bindCallbacks()
            val view = createHostView(context, appWidgetId) ?: View(context)
            if (view is AppWidgetHostView) hostView = view
            gate.addView(view, FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT))
            gate
        },
        update = { gate ->
            gate.bindCallbacks()
            val view = hostView ?: return@AndroidView
            val widthDp = committedWidthDp.value.toInt()
            val heightDp = committedHeightDp.value.toInt()
            if (widthDp > 0 && heightDp > 0) {
                view.updateAppWidgetSize(null, widthDp, heightDp, widthDp, heightDp)
                onSizeChange(appWidgetId, widthDp, heightDp)
            }
        },
    )
}
