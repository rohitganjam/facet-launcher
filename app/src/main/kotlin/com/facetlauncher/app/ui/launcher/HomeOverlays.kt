package com.facetlauncher.app.ui.launcher

import androidx.compose.runtime.getValue
import android.content.Intent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.unit.IntOffset
import com.facetlauncher.app.ui.components.GestureHintOverlay
import com.facetlauncher.app.ui.onboarding.SetDefaultLauncherSheet
import com.facetlauncher.app.ui.theme.FACET_TRANSITION_DURATION_MS
import com.facetlauncher.app.ui.theme.FacetTransitionEasing

/**
 * The Hub, to Home's left: off-screen at progress 0, flush with the screen at 1, drawn over Home
 * (as the drawer is). It has a fainter default opacity than the drawer by design, and its own
 * horizontal drag closes it. Also keeps the shared `AppWidgetHost` listening while the Hub is open
 * or a clock widget is on Home: the platform does not count start/stop calls, so whichever surface
 * closed last would otherwise silence the other. [onHubShow] and [onHubHide] are the Hub view
 * model's pass-throughs to that host. [content] is the Hub screen itself.
 */
@Composable
internal fun HomeHubOverlay(
    hub: SwipeAxisState,
    containerWidthPx: Float,
    isHubOpen: Boolean,
    clockWidgetNeedsListening: Boolean,
    overlays: HomeOverlayState,
    onHubShow: () -> Unit,
    onHubHide: () -> Unit,
    content: @Composable () -> Unit,
) {
    val currentOnHubShow by rememberUpdatedState(onHubShow)
    val currentOnHubHide by rememberUpdatedState(onHubHide)
    DisposableEffect(isHubOpen, clockWidgetNeedsListening) {
        val shouldListen = isHubOpen || clockWidgetNeedsListening
        if (shouldListen) currentOnHubShow()
        onDispose { if (shouldListen) currentOnHubHide() }
    }
    // The picker goes with the Hub when it closes.
    LaunchedEffect(isHubOpen) {
        if (!isHubOpen) overlays.showWidgetPicker = false
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .offset { IntOffset(((hub.progress.value - 1f) * containerWidthPx).toInt(), 0) }
            .pointerInput(Unit) {
                detectHorizontalDragGestures(
                    onDragEnd = { hub.settle() },
                    onDragCancel = { hub.settle() },
                ) { change, dragAmount ->
                    hub.dragBy(dragAmount)
                    change.consume()
                }
            },
    ) {
        content()
    }
}

/** The Hub's add-widget picker, sliding in over the Hub the way a NavHost push would, though the Hub never leaves composition. */
@Composable
internal fun HomeWidgetPickerOverlay(overlays: HomeOverlayState, onReset: () -> Unit, content: @Composable () -> Unit) {
    val currentOnReset by rememberUpdatedState(onReset)
    // Clear its search whenever the picker goes out of view, including when only it closes under an open Hub.
    LaunchedEffect(overlays.showWidgetPicker) {
        if (!overlays.showWidgetPicker) currentOnReset()
    }
    AnimatedVisibility(
        visible = overlays.showWidgetPicker,
        enter = slideInHorizontally(initialOffsetX = { it }, animationSpec = tween(FACET_TRANSITION_DURATION_MS, easing = FacetTransitionEasing)),
        exit = slideOutHorizontally(targetOffsetX = { it }, animationSpec = tween(FACET_TRANSITION_DURATION_MS, easing = FacetTransitionEasing)),
        modifier = Modifier.testTag("hub_widget_picker_overlay"),
    ) {
        content()
    }
}

/**
 * Switch Facets, to Home's right, drawn last so it is frontmost over the Hub, drawer and pickers.
 * Its own surface handles empty-space swipes back to Home; the carousel forwards the first page's
 * drag through its `onDismissDrag`, since its pager would otherwise swallow it as a dead overscroll.
 *
 * Its cards are live copies of Home, and it stays composed while closed (just off-screen), so
 * while closed its semantics are cleared: otherwise a lookup for Home's own text could match a
 * card's copy.
 */
@Composable
internal fun HomeFacetCarouselOverlay(facet: SwipeAxisState, containerWidthPx: Float, isOpen: Boolean, content: @Composable () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .offset { IntOffset(((1f - facet.progress.value) * containerWidthPx).toInt(), 0) }
            .then(if (isOpen) Modifier else Modifier.clearAndSetSemantics {})
            .pointerInput(Unit) {
                detectHorizontalDragGestures(
                    onDragEnd = { facet.settle() },
                    onDragCancel = { facet.settle() },
                ) { change, dragAmount ->
                    facet.dragBy(-dragAmount)
                    change.consume()
                }
            },
    ) {
        content()
    }
}

/** The clock widget picker, over everything else: reached from the clock adjust sheet on Home, not from the Hub. */
@Composable
internal fun HomeClockWidgetPickerOverlay(overlays: HomeOverlayState, onReset: () -> Unit, content: @Composable (itemId: Long) -> Unit) {
    val currentOnReset by rememberUpdatedState(onReset)
    // Clear its search and target page whenever its panel closes.
    LaunchedEffect(overlays.showClockWidgetPicker) {
        if (!overlays.showClockWidgetPicker) {
            currentOnReset()
            overlays.clockWidgetPickerItemId = null
        }
    }
    overlays.clockWidgetPickerItemId?.let { itemId ->
        AnimatedVisibility(
            visible = overlays.showClockWidgetPicker,
            enter = slideInHorizontally(initialOffsetX = { it }, animationSpec = tween(FACET_TRANSITION_DURATION_MS, easing = FacetTransitionEasing)),
            exit = slideOutHorizontally(targetOffsetX = { it }, animationSpec = tween(FACET_TRANSITION_DURATION_MS, easing = FacetTransitionEasing)),
            modifier = Modifier.testTag("clock_widget_picker_overlay"),
        ) {
            content(itemId)
        }
    }
}

/**
 * The one-time "make Facet your home screen" prompt, then the post-onboarding gesture hint, both
 * only while Home is at rest. The prompt comes first and holds the hint back.
 */
@Composable
internal fun HomeOneTimePrompts(
    showSetDefaultPrompt: Boolean,
    showGestureHint: Boolean,
    homeAtRest: Boolean,
    requestDefaultLauncherIntent: () -> Intent,
    onFinishSetDefault: () -> Unit,
    onDismissGestureHint: () -> Unit,
) {
    if (showSetDefaultPrompt && homeAtRest) {
        SetDefaultLauncherSheet(
            requestDefaultLauncherIntent = requestDefaultLauncherIntent,
            onFinish = onFinishSetDefault,
        )
    }
    AnimatedVisibility(
        visible = showGestureHint && !showSetDefaultPrompt && homeAtRest,
        enter = fadeIn(animationSpec = tween(240)),
        exit = fadeOut(animationSpec = tween(240)),
    ) {
        GestureHintOverlay(onDismiss = onDismissGestureHint)
    }
}
