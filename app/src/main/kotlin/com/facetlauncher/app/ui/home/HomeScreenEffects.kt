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
import androidx.compose.ui.res.stringResource
import com.facetlauncher.app.R
import com.facetlauncher.app.data.model.NativeWidgetRef
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.toSize
import com.facetlauncher.app.ui.components.ThemedModalBottomSheet
import com.facetlauncher.app.ui.home.HomeMenuMode
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
    // The effect outlives the composition it launched in, so it reads the latest frame, not the one it captured.
    val latestFrame by rememberUpdatedState(frame)
    LaunchedEffect(clock.templateId, clock.placement.alignment, clock.dateStyle, clock.placement.zoneHeightDp, clock.placementFacetId, frame.isLandscape) {
        // The fit maths is portrait geometry; running it in landscape would save a wrong scale.
        if (frame.isLandscape) return@LaunchedEffect
        // Never settling means no trustworthy measurement, so leave the saved scale alone.
        withTimeoutOrNull(GEOMETRY_SETTLE_TIMEOUT_MS) {
            snapshotFlow {
                listOf(layout.clockBoxSize, layout.clockBoxOriginInRoot, layout.rootSize, latestFrame.geometry.handle, latestFrame.metrics)
            }
                .filter { isClockGeometryMeasured(layout.clockBoxSize, layout.clockBoxOriginInRoot, layout.rootSize, latestFrame.geometry.handle) }
                .debounce(GEOMETRY_SETTLE_DEBOUNCE_MS)
                .first()
        } ?: return@LaunchedEffect
        val box = layout.measureClockBox() ?: return@LaunchedEffect
        // The frame the effect started with predates this measurement: its scale and commit conversion used the previous page's natural size.
        val settled = latestFrame
        val fit = settled.geometry.fit.maxScale(box.size, box.topLeft, box.rootWidthPx)
        if (settled.state.clock.placement.scale > fit + 0.01f) settled.actions.clock.onScaleCommit(fit)
    }
}

/**
 * Until the content height reaches the geometry, the height handle's ceiling collapses onto its floor and the clock
 * sits at the top, so a fit taken then reads as the minimum scale.
 */
internal fun isClockGeometryMeasured(boxSize: IntSize?, boxOrigin: Offset?, rootSize: IntSize?, handle: HandleBounds): Boolean =
    boxSize != null && boxOrigin != null && rootSize != null && handle.ceilingPx > handle.floorPx

/** Everything above the divider in portrait (the clock block's bottom is just above it); `null` in landscape, where the clock pane's own bounds serve, or before it is measured. */
private fun clockZoneBounds(layout: HomeScreenLayout, landscape: Boolean): Rect? {
    val width = layout.rootSize?.width?.toFloat()
    val bottom = layout.clockBlockBottomInRoot
    if (landscape || width == null || bottom == null) return null
    return Rect(0f, 0f, width, bottom - (layout.rootOriginInRoot?.y ?: 0f))
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
        frame.actions.onClockZoneBoundsChange(clockZoneBounds(layout, frame.isLandscape))
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
 * Taps on empty space leave adjust mode, or (when nothing is active) a long-press opens the clock adjust menu and a
 * double-tap calls [onDoubleTap].
 * Attached to the root Box, an ancestor of every child, so a descendant's own gesture (an app icon, the dock, the
 * clock's detector) claims a touch first and only an unconsumed one counts as empty space. Keyed on the mode:
 * ADJUST and NONE install a pointerInput in the same slot, and sharing a key would reuse the old detector's
 * coroutine across the switch, leaving a stale handler running.
 */
internal fun Modifier.homeEmptySpaceGestures(
    mode: HomeMenuMode,
    onModeChange: (HomeMenuMode) -> Unit,
    onDoubleTap: () -> Unit = {},
): Modifier = this.then(
    when (mode) {
        HomeMenuMode.ADJUST -> Modifier.pointerInput(mode) { detectTapGestures(onTap = { onModeChange(HomeMenuMode.NONE) }) }
        HomeMenuMode.NONE -> Modifier.pointerInput(mode) {
            detectTapGestures(onDoubleTap = { onDoubleTap() }, onLongPress = { onModeChange(HomeMenuMode.MENU) })
        }
        HomeMenuMode.MENU, HomeMenuMode.WIDGET_MENU, HomeMenuMode.CALENDAR_SETTINGS -> Modifier
    },
)

/** What the stack row of the sheets says is in the stack. */
@Composable
private fun stackSummaryText(stack: HomeClockStackState): String = when {
    stack.isBlank -> stringResource(R.string.widget_sheet_stack_empty)
    stack.pageCount == 1 -> stringResource(if (stack.hasClock) R.string.widget_sheet_stack_clock else R.string.widget_sheet_stack_one_widget)
    else -> stringResource(R.string.widget_sheet_stack_pages, stack.pageCount)
}

/** The Home sheet, opened by a long-press on free space: the facet's and the launcher's settings (and add a widget and the widget stack while it is empty, adjust the widget showing otherwise). */
@Composable
internal fun HomeMenuSheet(stack: HomeClockStackState, actions: HomeClockActions) {
    val close = { actions.onAdjustModeChange(HomeMenuMode.NONE) }
    ThemedModalBottomSheet(
        onDismissRequest = close,
        skipPartiallyExpanded = true,
        containerColor = SurfaceContainer,
    ) {
        HomeMenuSheetContent(
            onSwitchFacetClick = {
                close()
                actions.onOpenFacetCarousel()
            },
            onFacetSettingsClick = {
                close()
                actions.onNavigateToFacetSettings()
            },
            onLauncherSettingsClick = {
                close()
                actions.onNavigateToSettings()
            },
            onAddWidgetClick = {
                close()
                actions.onAddWidgetClick()
            },
            onWidgetStackClick = {
                close()
                actions.onWidgetStackClick()
            },
            onAdjustClick = { actions.onAdjustModeChange(HomeMenuMode.ADJUST) },
            stackSummary = stackSummaryText(stack),
            stackEmpty = stack.isBlank,
        )
    }
}

/** What a long-press on the page showing acts on: the clock when there is no widget, a native widget by its type, a hosted widget otherwise. */
internal fun HomeClockWidgetState?.sheetTarget(): WidgetSheetTarget = when {
    this == null -> WidgetSheetTarget.Clock
    nativeType != null -> WidgetSheetTarget.Native(nativeType)
    else -> WidgetSheetTarget.Hosted
}

/**
 * The widget's own sheet, opened by a long-press on the clock, its calendar or any widget page: that widget's settings, then adjust size and
 * position, replace, remove and the widget stack. A native widget's settings row opens the same settings its 3-dot button used to.
 */
@Composable
internal fun WidgetSheet(stack: HomeClockStackState, widget: HomeClockWidgetState?, actions: HomeClockActions) {
    val close = { actions.onAdjustModeChange(HomeMenuMode.NONE) }
    ThemedModalBottomSheet(
        onDismissRequest = close,
        skipPartiallyExpanded = true,
        containerColor = SurfaceContainer,
    ) {
        WidgetSheetContent(
            target = widget.sheetTarget(),
            onClockStylesClick = {
                close()
                actions.onClockStylesClick()
            },
            onCalendarSettingsClick = { actions.onAdjustModeChange(HomeMenuMode.CALENDAR_SETTINGS) },
            onWidgetSettingsClick = {
                close()
                widget?.takeIf { it.nativeType != null }?.let { actions.onConfigureNativeWidget(NativeWidgetRef.Home(it.itemId)) }
            },
            onAdjustClick = { actions.onAdjustModeChange(HomeMenuMode.ADJUST) },
            onReplaceWidgetClick = {
                close()
                actions.onReplaceWidgetClick()
            },
            onRemoveWidgetClick = {
                close()
                actions.onRemoveWidgetClick()
            },
            onWidgetStackClick = {
                close()
                actions.onWidgetStackClick()
            },
            stackSummary = stackSummaryText(stack),
        )
    }
}
