package com.facetlauncher.app.ui.launcher

import com.facetlauncher.app.ui.home.widget.ClockWidgetPickerTarget
import androidx.compose.runtime.getValue
import android.content.Intent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.input.nestedscroll.nestedScroll
import com.facetlauncher.app.data.model.SwipeDirection
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.clearAndSetSemantics
import com.facetlauncher.app.ui.components.GestureHintOverlay
import com.facetlauncher.app.ui.onboarding.SetDefaultLauncherSheet
import com.facetlauncher.app.ui.theme.FACET_TRANSITION_DURATION_MS
import com.facetlauncher.app.ui.theme.FacetTransitionEasing

/**
 * The Hub, off the edge its swipe pulls it from ([direction]) at progress 0, flush with the screen at 1, drawn over Home
 * (as the drawer is). It has a fainter default opacity than the drawer by design; a drag back the way it came closes it,
 * on its surface or, once its grid can scroll no further, through the grid. Also keeps the shared `AppWidgetHost` listening while the Hub is open
 * or a clock widget is on Home: the platform does not count start/stop calls, so whichever surface
 * closed last would otherwise silence the other. [onHubShow] and [onHubHide] are the Hub view
 * model's pass-throughs to that host. [content] is the Hub screen itself.
 */
@Composable
internal fun HomeHubOverlay(
    hub: SwipeAxisState,
    direction: SwipeDirection,
    containerWidthPx: Float,
    containerHeightPx: Float,
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
            .offset { panelOffset(direction, hub.progress.value, containerWidthPx, containerHeightPx) }
            .nestedScroll(remember(hub, direction) { panelCloseNestedScroll(hub, direction) })
            .panelSurfaceDrag(hub, direction),
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
 * Switch Facets, off the edge its swipe pulls it from ([direction]), drawn last so it is frontmost over the Hub, drawer and pickers.
 * Its own surface handles empty-space swipes back to Home; the carousel forwards the first page's
 * drag through its `onDismissDrag`, since its pager would otherwise swallow it as a dead overscroll.
 *
 * Its cards are live copies of Home, and it stays composed while closed (just off-screen), so
 * while closed its semantics are cleared: otherwise a lookup for Home's own text could match a
 * card's copy.
 */
@Composable
internal fun HomeFacetCarouselOverlay(
    facet: SwipeAxisState,
    direction: SwipeDirection,
    containerWidthPx: Float,
    containerHeightPx: Float,
    isOpen: Boolean,
    content: @Composable () -> Unit,
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .offset { panelOffset(direction, facet.progress.value, containerWidthPx, containerHeightPx) }
            .then(if (isOpen) Modifier else Modifier.clearAndSetSemantics {})
            .panelSurfaceDrag(facet, direction),
    ) {
        content()
    }
}

/** The clock widget picker, over everything else: reached from the Home sheet, not from the Hub. */
@Composable
internal fun HomeClockWidgetPickerOverlay(overlays: HomeOverlayState, onReset: () -> Unit, content: @Composable (target: ClockWidgetPickerTarget) -> Unit) {
    val currentOnReset by rememberUpdatedState(onReset)
    // Clear its search and target page whenever its panel closes.
    LaunchedEffect(overlays.showClockWidgetPicker) {
        if (!overlays.showClockWidgetPicker) {
            currentOnReset()
            overlays.clockWidgetPickerTarget = null
        }
    }
    overlays.clockWidgetPickerTarget?.let { target ->
        AnimatedVisibility(
            visible = overlays.showClockWidgetPicker,
            enter = slideInHorizontally(initialOffsetX = { it }, animationSpec = tween(FACET_TRANSITION_DURATION_MS, easing = FacetTransitionEasing)),
            exit = slideOutHorizontally(targetOffsetX = { it }, animationSpec = tween(FACET_TRANSITION_DURATION_MS, easing = FacetTransitionEasing)),
            modifier = Modifier.testTag("clock_widget_picker_overlay"),
        ) {
            content(target)
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
