package com.facetlauncher.app.ui.home

import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.toSize
import com.facetlauncher.app.ui.components.ThemedModalBottomSheet
import com.facetlauncher.app.ui.home.ClockAdjustMode
import com.facetlauncher.app.ui.home.HomeClockActions
import com.facetlauncher.app.ui.home.HomeClockState
import com.facetlauncher.app.ui.theme.SurfaceContainer
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withTimeoutOrNull

/** The effects that clear in-flight drag values once a commit lands, and re-clamp the clock's saved scale when its look changes. */
@Composable
internal fun HomeScreenEffects(frame: HomeFrame) {
    val layout = frame.layout
    val placement = frame.state.clock.placement
    val widget = frame.state.widget
    LaunchedEffect(placement.zoneHeightDp) {
        layout.liveHandlePx = null
        layout.liveDividerWidthPx = null
    }
    LaunchedEffect(placement.scale) { layout.liveScale = null }
    LaunchedEffect(widget?.widthDp, widget?.heightDp) {
        layout.liveWidgetWidthPx = null
        layout.liveWidgetHeightPx = null
    }
    ClockScaleReclampEffect(frame)
}

/**
 * One-shot re-clamp: a style or position change resizes the clock's natural box, so a scale that fit the old
 * look can now overflow. After the new geometry settles, check once and only save a smaller value if it
 * genuinely clips.
 */
@Composable
private fun ClockScaleReclampEffect(frame: HomeFrame) {
    val layout = frame.layout
    val clock = frame.state.clock
    LaunchedEffect(clock.templateId, clock.placement.alignment, clock.dateStyle, clock.placement.zoneHeightDp, clock.positionOwnerFacetId, frame.isLandscape) {
        // The fit maths is portrait geometry; running it in landscape would save a wrong scale.
        if (frame.isLandscape) return@LaunchedEffect
        withTimeoutOrNull(GEOMETRY_SETTLE_TIMEOUT_MS) {
            snapshotFlow { Triple(layout.clockBoxSize, layout.clockBoxOriginInRoot, layout.rootSize) }
                .filter { it.first != null && it.second != null && it.third != null }
                .debounce(GEOMETRY_SETTLE_DEBOUNCE_MS)
                .first()
        }
        val box = layout.measureClockBox() ?: return@LaunchedEffect
        val fit = frame.geometry.fit.maxScale(box.size, box.topLeft, box.rootWidthPx)
        if (clock.placement.scale > fit + 0.01f) frame.actions.clock.onScaleCommit(fit)
    }
}

/**
 * Reports the hosted widget's bounds and the app list's viewport to the route, so its swipe-to-open-drawer gesture
 * can leave them alone. The widget's is `null` for the native clock (nothing independently scrollable to protect)
 * or before it is measured. The list's goes through a snapshotFlow, not a SideEffect: onGloballyPositioned writes
 * its size after layout, so a SideEffect only saw the last unrelated recomposition's values.
 */
@Composable
internal fun HomeBoundsReporting(frame: HomeFrame) {
    val layout = frame.layout
    val hasWidget = frame.state.widget != null
    SideEffect {
        val box = layout.measureClockBox()
        frame.actions.widget.onBoundsChange(if (hasWidget && box != null) Rect(box.topLeft, box.sizePx) else null)
    }
    val currentOnAppListBoundsChange by rememberUpdatedState(frame.actions.onAppListBoundsChange)
    LaunchedEffect(Unit) {
        snapshotFlow {
            val size = layout.appListSize
            val origin = layout.appListOriginInRoot
            val rootOrigin = layout.rootOriginInRoot
            if (size != null && origin != null && rootOrigin != null) Rect(offset = origin - rootOrigin, size = size.toSize()) else null
        }.collect { currentOnAppListBoundsChange(it) }
    }
}

/**
 * Taps on empty space leave adjust mode, or (when nothing is active) a long-press opens the clock adjust menu.
 * Attached to the root Box, an ancestor of every child, so a descendant's own gesture (an app icon, the dock, the
 * clock's detector) claims a touch first and only an unconsumed one counts as empty space. Keyed on the mode:
 * ADJUST and NONE install a pointerInput in the same slot, and sharing a key would reuse the old detector's
 * coroutine across the switch, leaving a stale handler running.
 */
internal fun Modifier.homeEmptySpaceGestures(mode: ClockAdjustMode, onModeChange: (ClockAdjustMode) -> Unit): Modifier = this.then(
    when (mode) {
        ClockAdjustMode.ADJUST -> Modifier.pointerInput(mode) { detectTapGestures(onTap = { onModeChange(ClockAdjustMode.NONE) }) }
        ClockAdjustMode.NONE -> Modifier.pointerInput(mode) { detectTapGestures(onLongPress = { onModeChange(ClockAdjustMode.MENU) }) }
        ClockAdjustMode.MENU -> Modifier
    },
)

/** The clock adjust menu sheet. */
@Composable
internal fun ClockMenuSheet(clock: HomeClockState, hasWidget: Boolean, actions: HomeClockActions) {
    val close = { actions.onAdjustModeChange(ClockAdjustMode.NONE) }
    ThemedModalBottomSheet(
        onDismissRequest = close,
        skipPartiallyExpanded = true,
        containerColor = SurfaceContainer,
    ) {
        ClockAdjustSheet(
            onAdjustClick = { actions.onAdjustModeChange(ClockAdjustMode.ADJUST) },
            onEditStylesClick = actions.onEditClockStyles,
            onFacetSettingsClick = {
                close()
                actions.onNavigateToFacetSettings()
            },
            onLauncherSettingsClick = {
                close()
                actions.onNavigateToSettings()
            },
            onUseCustomWidgetClick = {
                close()
                actions.onUseCustomWidgetClick()
            },
            onSwitchToLauncherClockClick = {
                close()
                actions.onSwitchToLauncherClockClick()
            },
            overrideFacetName = clock.positionOwnerFacetName,
            hasCustomClockWidget = hasWidget,
            isPro = clock.isPro,
        )
    }
}
