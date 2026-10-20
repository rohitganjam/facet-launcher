package com.facetlauncher.app.ui.home

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import com.facetlauncher.app.data.local.landscape
import com.facetlauncher.app.data.model.ClockAlignment
import com.facetlauncher.app.ui.home.ClockAdjustMode
import com.facetlauncher.app.ui.home.HomeClockState
import com.facetlauncher.app.ui.home.HomeClockWidgetState
import com.facetlauncher.app.ui.home.HomeScreenActions
import kotlin.math.roundToInt
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.onStart

/** A hosted clock's measured box: its size, its top-left and the root's origin, all in root coordinates. */
internal data class ClockBoxMeasure(val size: IntSize, val topLeft: Offset, val rootOrigin: Offset, val rootWidthPx: Int) {
    val sizePx: Size get() = Size(size.width.toFloat(), size.height.toFloat())
}

/** What the clock adjust overlays read: the measurements, the resolved geometry, and the clock and widget state and actions. */
@Stable
internal class ClockAdjustEnv(
    val layout: HomeScreenLayout,
    val geometry: ClockGeometry,
    val metrics: ClockMetrics,
    val clock: HomeClockState,
    val widget: HomeClockWidgetState?,
    val actions: HomeScreenActions,
    /** The hosted widget's smallest size in px for the current mode. */
    val widgetMinPx: Offset,
    /** Landscape's bottom dock row, kept out of the divider and the pill. */
    val landscapeDockReserve: Dp,
) {
    /** `null` until the clock's box and the root have both been measured. */
    val box: ClockBoxMeasure? = layout.measureClockBox()
}

/** The handles for the current mode: the height handle or the divider, and the resize handles for the native clock or the hosted widget. */
@Composable
internal fun ClockAdjustOverlays(env: ClockAdjustEnv, landscape: Boolean) {
    if (env.clock.adjustMode != ClockAdjustMode.ADJUST) return
    val box = env.box
    val hasWidget = env.widget != null
    if (landscape) LandscapeDividerHandle(env) else PortraitHeightHandle(env)
    if (box != null) {
        when {
            landscape && hasWidget -> LandscapeWidgetResize(env, box)
            landscape -> LandscapeNativeClockResize(env, box)
            hasWidget -> PortraitWidgetResize(env, box)
            else -> PortraitNativeClockResize(env, box)
        }
    }
}

/** One corner-handle drag: its three callbacks, shared by both of a frame's handles. */
internal class CornerDrag(val onStart: () -> Unit, val onDrag: (Offset) -> Unit, val onEnd: () -> Unit)

/** The clock's measured box, or `null` until it and the root have both been measured. */
internal fun HomeScreenLayout.measureClockBox(): ClockBoxMeasure? = run {
    val size = clockBoxSize ?: return@run null
    val origin = clockBoxOriginInRoot ?: return@run null
    val rootOrigin = rootOriginInRoot ?: return@run null
    val root = rootSize ?: return@run null
    ClockBoxMeasure(size, origin - rootOrigin, rootOrigin, root.width)
}

private fun ClockAdjustEnv.dragging(isDragging: Boolean) = actions.clock.onDraggingHandleChange(isDragging)

/** Portrait's height handle. On release it also saves the scale if the move forced a shrink-to-fit, so it doesn't snap back. */
@Composable
internal fun PortraitHeightHandle(env: ClockAdjustEnv) {
    val density = LocalDensity.current
    val bounds = env.geometry.handle
    ClockZoneHandle(
        modifier = Modifier
            .padding(horizontal = 24.dp)
            .offset { IntOffset(0, bounds.handlePx.roundToInt()) },
        onDragStart = { env.dragging(true) },
        onDrag = { deltaDp ->
            // Clamped here too, so the handle follows the finger straight away when a drag reverses.
            env.layout.liveHandlePx = ((env.layout.liveHandlePx ?: bounds.basePx) + with(density) { deltaDp.dp.toPx() }).coerceIn(bounds.floorPx, bounds.ceilingPx)
        },
        onDragEnd = {
            env.dragging(false)
            env.actions.clock.onZoneHeightCommit(with(density) { bounds.handlePx.toDp().value })
            // liveHandlePx is held until the zone height's LaunchedEffect sees the commit land.
            val scale = env.geometry.effectiveScale
            if (scale < env.clock.placement.scale - 0.005f) env.actions.clock.onScaleCommit(scale)
        },
    )
}

/**
 * The divider between landscape's clock and apps panes: dragging it sets the clock pane's width. A narrower pane
 * shrinks the clock in real time (the native clock to its smallest scale, a hosted widget to its own minimum),
 * and on release the shrunk scale or size is saved so it doesn't snap back.
 */
@Composable
internal fun LandscapeDividerHandle(env: ClockAdjustEnv) {
    val density = LocalDensity.current
    val paneWidthPx = env.geometry.landscapePaneWidthPx
    Box(
        modifier = Modifier
            .fillMaxSize()
            .windowInsetsPadding(WindowInsets.systemBars)
            .padding(top = HOME_LANDSCAPE_VERTICAL_PADDING_TOP, bottom = HOME_LANDSCAPE_VERTICAL_PADDING_BOTTOM + env.landscapeDockReserve),
    ) {
        ClockZoneHandle(
            dragOrientation = Orientation.Horizontal,
            modifier = Modifier.offset {
                val centerInGapPx = with(density) { ((HOME_LANDSCAPE_PANE_GAP - CLOCK_ZONE_HANDLE_TOUCH_HEIGHT) / 2).toPx() }
                IntOffset((paneWidthPx + centerInGapPx).roundToInt(), 0)
            },
            onDragStart = { env.dragging(true) },
            onDrag = { deltaDp ->
                val landscape = env.geometry.landscape
                env.layout.liveDividerWidthPx = landscapeDividerDragWidthPx(
                    currentPx = env.layout.liveDividerWidthPx ?: paneWidthPx,
                    deltaPx = with(density) { deltaDp.dp.toPx() },
                    floorPx = env.metrics.landscapeMarginsPx + landscape.minClockWidthPx,
                    contentWidthPx = landscape.contentWidthPx,
                )
            },
            onDragEnd = { commitDividerDrag(env, density) },
        )
    }
}

private fun commitDividerDrag(env: ClockAdjustEnv, density: Density) {
    env.dragging(false)
    // liveDividerWidthPx is held until the zone width's LaunchedEffect sees the commit land.
    env.actions.clock.onZoneHeightCommit(with(density) { env.geometry.landscapePaneWidthPx.toDp().value })
    val shrunkWidget = env.geometry.effectiveWidgetSizePx
    val widget = env.widget
    if (widget != null && shrunkWidget != null) {
        if (shrunkWidget.x < with(density) { (widget.widthDp ?: 0.dp).toPx() } - 1f) {
            env.actions.widget.onSizeCommit(
                with(density) { shrunkWidget.x.toDp().value }.roundToInt(),
                with(density) { shrunkWidget.y.toDp().value }.roundToInt(),
            )
        }
    } else if (env.geometry.effectiveScale < env.clock.placement.scale - 0.005f) {
        env.actions.clock.onScaleCommit(env.geometry.effectiveScale)
    }
}

/**
 * The alignment pills: portrait's sits below the height handle (its touch strip plus a gap), landscape's in the
 * clock pane at its bottom edge, or at the top when the clock is aligned to the bottom, so neither covers the clock.
 * During any handle drag they dim and their options are disabled until a moment after the drag ends, so a thumb that
 * has just let go can't tap them. They stay composed, so they don't pop in and out, and keep absorbing taps.
 */
@Composable
internal fun BoxScope.ClockAdjustPills(env: ClockAdjustEnv, adjusting: Boolean, landscape: Boolean) {
    val density = LocalDensity.current
    val adjustMode = env.clock.adjustMode
    val draggingHandle = env.clock.draggingHandle
    val toolbarActive by produceState(initialValue = false, adjustMode, draggingHandle) {
        value = if (adjustMode == ClockAdjustMode.ADJUST && !draggingHandle) {
            delay(CLOCK_ADJUST_TOOLBAR_REAPPEAR_DELAY_MS)
            true
        } else {
            false
        }
    }
    // Not `by` — read .value only inside graphicsLayer, so the fade runs in the draw phase.
    val toolbarAlpha = animateFloatAsState(
        targetValue = if (toolbarActive) 1f else CLOCK_ADJUST_TOOLBAR_DIMMED_ALPHA,
        animationSpec = tween(if (toolbarActive) 150 else 120),
        label = "clockAdjustToolbarAlpha",
    )
    val alignment = env.clock.placement.alignment
    AnimatedVisibility(
        visible = adjusting && !landscape,
        enter = fadeIn(tween(120)),
        exit = fadeOut(tween(80)),
        modifier = Modifier
            .align(Alignment.TopCenter)
            .offset { IntOffset(0, (env.geometry.handle.handlePx + with(density) { (CLOCK_ZONE_HANDLE_TOUCH_HEIGHT + CLOCK_ADJUST_TOOLBAR_GAP).toPx() }).roundToInt()) },
    ) {
        ClockAdjustToolbar(alignment, env.actions.clock.onAlignmentChange, Modifier.graphicsLayer { alpha = toolbarAlpha.value }, enabled = toolbarActive)
    }
    AnimatedVisibility(
        visible = adjusting && landscape,
        enter = fadeIn(tween(120)),
        exit = fadeOut(tween(80)),
        modifier = Modifier
            .align(if (alignment == ClockAlignment.RIGHT) Alignment.TopStart else Alignment.BottomStart)
            .windowInsetsPadding(WindowInsets.systemBars)
            .padding(
                start = HOME_LANDSCAPE_CLOCK_PANE_START_PADDING,
                top = HOME_LANDSCAPE_VERTICAL_PADDING_TOP,
                bottom = HOME_LANDSCAPE_VERTICAL_PADDING_BOTTOM + env.landscapeDockReserve,
            ),
    ) {
        ClockAdjustToolbar(alignment, env.actions.clock.onAlignmentChange, Modifier.graphicsLayer { alpha = toolbarAlpha.value }, enabled = toolbarActive, vertical = true)
    }
}
