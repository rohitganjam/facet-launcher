package com.facetlauncher.app.ui.launcher

import androidx.activity.compose.BackHandler
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import com.facetlauncher.app.ui.components.LocalHomeSwipeGate
import com.facetlauncher.app.ui.home.HomeMenuMode
import com.facetlauncher.app.ui.theme.SystemBarsBackdrop
import com.facetlauncher.app.ui.theme.SystemBarsBackdropEffect
import kotlinx.coroutines.launch

/** Resumes, the system bars' backdrop, Home presses, focus and the back button: everything the route does that draws nothing. */
@Composable
internal fun HomeRouteEffects(state: HomeRouteState, swipe: HomeSwipeController, open: PanelsOpen, hooks: HomeRouteEffectHooks) {
    ResumeEffect(hooks.onResume)
    // What's behind the system bars: wallpaper, except while an opaque picker or the always-dark Private Space covers it.
    val overlays = state.overlays
    SystemBarsBackdropEffect(
        when {
            overlays.showPrivateSpace -> SystemBarsBackdrop.DARK_SURFACE
            overlays.showWidgetPicker || overlays.showClockWidgetPicker || overlays.showAppVault -> SystemBarsBackdrop.THEME_SURFACE
            else -> SystemBarsBackdrop.WALLPAPER
        },
    )
    HomePressEffect(state, swipe, hooks)
    FocusAndAdjustEffects(state, swipe, open)
    RouteBackHandler(state, swipe, open, hooks.onVaultBack)
}

@Composable
private fun ResumeEffect(onResume: () -> Unit) {
    val lifecycleOwner = LocalLifecycleOwner.current
    val currentOnResume by rememberUpdatedState(onResume)
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event -> if (event == Lifecycle.Event.ON_RESUME) currentOnResume() }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }
}

/** A Home press closes everything, and on bare Home (with the setting on) opens the drawer's search. */
@Composable
private fun HomePressEffect(state: HomeRouteState, swipe: HomeSwipeController, hooks: HomeRouteEffectHooks) {
    val overlays = state.overlays
    val coroutineScope = rememberCoroutineScope()
    val focusManager = LocalFocusManager.current
    val homeSwipeGate = LocalHomeSwipeGate.current
    val currentOpensSearch by rememberUpdatedState(hooks.homePressOpensSearch)
    LaunchedEffect(hooks.homePresses) {
        hooks.homePresses.collect {
            val onBareHome = currentOpensSearch() &&
                homeSwipeGate?.isBlocked != true &&
                swipe.drawer.progress.value == 0f && swipe.hub.progress.value == 0f && swipe.facet.progress.value == 0f &&
                state.homeMenuMode == HomeMenuMode.NONE && !overlays.showWidgetPicker &&
                !overlays.showClockWidgetPicker && !overlays.showPrivateSpace && !overlays.showAppVault
            focusManager.clearFocus()
            state.homeMenuMode = HomeMenuMode.NONE
            overlays.showClockWidgetPicker = false
            // Each close in its own job: a drag that interrupts one animation throws a
            // CancellationException, which must end that job only — inline, it escaped `collect`
            // and silently unsubscribed this collector, so Home stopped closing anything.
            coroutineScope.launch { swipe.drawer.close() }
            coroutineScope.launch { swipe.hub.close() }
            coroutineScope.launch { swipe.facet.close() }
            if (onBareHome) {
                state.searchFocusRequest++
                coroutineScope.launch { swipe.drawer.open() }
            }
        }
    }
}

@Composable
private fun FocusAndAdjustEffects(state: HomeRouteState, swipe: HomeSwipeController, open: PanelsOpen) {
    val focusManager = LocalFocusManager.current
    // Clear focus whenever the drawer, hub, or facet carousel starts moving via a drag.
    LaunchedEffect(swipe.drawer.dragActive, swipe.hub.dragActive, swipe.facet.dragActive) {
        if (swipe.drawer.dragActive || swipe.hub.dragActive || swipe.facet.dragActive) focusManager.clearFocus()
    }
    // Cancel clock adjustment when navigating away from Home.
    LaunchedEffect(open.any) {
        if (open.any) state.homeMenuMode = HomeMenuMode.NONE
    }
}

@Composable
private fun RouteBackHandler(state: HomeRouteState, swipe: HomeSwipeController, open: PanelsOpen, onVaultBack: () -> Boolean) {
    val overlays = state.overlays
    val coroutineScope = rememberCoroutineScope()
    val focusManager = LocalFocusManager.current
    BackHandler(enabled = open.any || overlays.showWidgetPicker || overlays.showClockWidgetPicker || overlays.showPrivateSpace || overlays.showAppVault) {
        focusManager.clearFocus()
        when {
            overlays.showClockWidgetPicker -> overlays.showClockWidgetPicker = false
            overlays.showWidgetPicker -> overlays.showWidgetPicker = false
            overlays.showAppVault -> if (!onVaultBack()) overlays.showAppVault = false
            overlays.showPrivateSpace -> overlays.showPrivateSpace = false
            open.drawer -> coroutineScope.launch { swipe.drawer.close() }
            open.hub -> coroutineScope.launch { swipe.hub.close() }
            open.facet -> coroutineScope.launch { swipe.facet.close() }
        }
    }
}
