package com.facetlauncher.app.ui.home

import android.graphics.Rect as AndroidRect
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import com.facetlauncher.app.data.model.landscape
import com.facetlauncher.app.data.model.ClockAlignment
import com.facetlauncher.app.ui.theme.Accent
import kotlin.math.abs
import kotlin.math.roundToInt
import kotlinx.coroutines.flow.onStart

/** The native clock's resize handles in portrait: its glyph's corners, scaling around the bottom and the aligned edge. */
@Composable
internal fun PortraitNativeClockResize(env: ClockAdjustEnv, box: ClockBoxMeasure) {
    val density = LocalDensity.current
    val alignment = env.clock.placement.alignment
    val natW = box.size.width.toFloat()
    val natH = box.size.height.toFloat()
    // The clock's natural (unscaled) box: stable throughout a drag, since uniformScale keeps the layout footprint at natural size.
    val natTopLeft = box.topLeft
    val scale = env.geometry.effectiveScale
    val natBottom = natTopLeft.y + natH
    // The x that stays put as the box resizes (matches uniformScale's transformOrigin).
    val anchorX = natTopLeft.x + natW * alignment.originFraction()
    // The box being dragged, else the saved one, else just the drawn clock: the text fits its tighter side.
    val boxPx = nativeBoxPx(env, natW, natH, scale, density)
    val frame = Rect(
        offset = Offset(anchorX - boxPx.width * alignment.originFraction(), natBottom - boxPx.height),
        size = boxPx,
    )
    // The box may grow to the room above and beside the clock.
    val maxSize = env.geometry.fit.maxWidgetSize(box.size, natTopLeft, box.rootWidthPx).let { Size(it.x, it.y) }
    // The box a touch point in root coords implies, if that point were the dragged corner.
    val sizeAtTouch: (Offset) -> Size = { t ->
        val w = when (alignment) {
            ClockAlignment.LEFT -> t.x - anchorX
            ClockAlignment.RIGHT -> anchorX - t.x
            ClockAlignment.CENTER -> abs(t.x - anchorX) * 2f
        }
        Size(w, natBottom - t.y)
    }
    // Never let the top handles sit in the status-bar strip, even if the room is momentarily stale (e.g. after a template switch).
    val handleTopPx = (frame.top - with(density) { HOME_CLOCK_RESIZE_HANDLE_INSET.toPx() }).coerceAtLeast(env.metrics.topInsetPx)
    ClockResizeFrame(frame, handleTopPx, nativeBoxDrag(env, box, boxPx, maxSize, sizeAtTouch, density))
}

/** The native clock's box in px: the one being dragged, else the saved one, else the drawn clock's own bounds. */
private fun nativeBoxPx(env: ClockAdjustEnv, natW: Float, natH: Float, scale: Float, density: Density): Size {
    env.layout.liveBoxPx?.let { return it }
    val savedW = env.clock.boxWidthDp
    val savedH = env.clock.boxHeightDp
    return if (savedW != null && savedH != null) {
        Size(with(density) { savedW.dp.toPx() }, with(density) { savedH.dp.toPx() })
    } else {
        Size(natW * scale, natH * scale)
    }
}

/** The native clock's drag in portrait: width and height move freely, the text scale follows the tighter side, and the box is saved in dp on release. */
private fun nativeBoxDrag(env: ClockAdjustEnv, box: ClockBoxMeasure, boxPx: Size, maxSize: Size, sizeAtTouch: (Offset) -> Size, density: Density): CornerDrag {
    val layout = env.layout
    val natW = box.size.width.toFloat()
    val natH = box.size.height.toFloat()
    val minW = natW * HOME_CLOCK_MIN_SCALE
    val minH = natH * HOME_CLOCK_MIN_SCALE
    fun clamped(size: Size) = Size(
        size.width.coerceIn(minW, maxOf(minW, maxSize.width)),
        size.height.coerceIn(minH, maxOf(minH, maxSize.height)),
    )
    return CornerDrag(
        onStart = {
            layout.widgetDragGrabOffset = null
            env.actions.clock.onDraggingHandleChange(true)
        },
        onDrag = { touchInAbsolute ->
            val implied = sizeAtTouch(touchInAbsolute - box.rootOrigin)
            // First move: capture where the finger sits vs the corner, so the box doesn't pop.
            val grab = layout.widgetDragGrabOffset
                ?: Offset(implied.width - boxPx.width, implied.height - boxPx.height).also { layout.widgetDragGrabOffset = it }
            val live = clamped(Size(implied.width - grab.x, implied.height - grab.y))
            layout.liveBoxPx = live
            layout.liveScale = minOf(live.width / natW, live.height / natH)
        },
        onEnd = {
            env.actions.clock.onDraggingHandleChange(false)
            val saved = clamped(layout.liveBoxPx ?: boxPx)
            env.actions.clock.onBoxSizeCommit(
                with(density) { saved.width.toDp().value }.roundToInt(),
                with(density) { saved.height.toDp().value }.roundToInt(),
            )
        },
    )
}

/** The native clock's resize handles in landscape: a free box on the corners away from the anchor, kept to the pane, the text fitting its tighter side. */
@Composable
internal fun LandscapeNativeClockResize(env: ClockAdjustEnv, box: ClockBoxMeasure) {
    val density = LocalDensity.current
    val landscape = env.geometry.landscape
    val natural = landscape.naturalSize
    val boxPx = nativeBoxPx(env, natural.width, natural.height, env.geometry.effectiveScale, density)
    // The box keeps the clock's left edge and the vertical anchor (top, middle or bottom) of its natural box.
    val anchorY = box.topLeft.y + natural.height * landscape.originY
    val frame = Rect(Offset(box.topLeft.x, anchorY - landscape.originY * boxPx.height), boxPx)
    val sizeAtTouch: (Offset) -> Size = { t -> landscapeWidgetSizeAtTouch(t, box.topLeft, natural, landscape.originY) }
    ClockResizeFrame(frame, landscapeHandleTopPx(env, frame, density), nativeBoxDrag(env, box, boxPx, landscape.maxWidgetSize, sizeAtTouch, density))
}

/** A hosted widget's resize handles in portrait: its real width and height, around the bottom and the aligned edge. */
@Composable
internal fun PortraitWidgetResize(env: ClockAdjustEnv, box: ClockBoxMeasure) {
    val density = LocalDensity.current
    val alignment = env.clock.placement.alignment
    val boxSize = box.sizePx
    val maxSize = env.geometry.fit.maxWidgetSize(box.size, box.topLeft, box.rootWidthPx).let { Size(it.x, it.y) }
    val boxBottom = box.topLeft.y + boxSize.height
    // The x that stays put as the box resizes (matches the widget's own alignment anchor).
    val anchorX = box.topLeft.x + boxSize.width * alignment.originFraction()
    // Real width and height a touch point implies, if that point were the dragged corner.
    val sizeAtTouch: (Offset) -> Size = { t ->
        val w = when (alignment) {
            ClockAlignment.LEFT -> t.x - anchorX
            ClockAlignment.RIGHT -> anchorX - t.x
            ClockAlignment.CENTER -> abs(t.x - anchorX) * 2f
        }
        Size(w, boxBottom - t.y)
    }
    val handleTopPx = (box.topLeft.y - with(density) { HOME_CLOCK_RESIZE_HANDLE_INSET.toPx() }).coerceAtLeast(env.metrics.topInsetPx)
    ClockResizeFrame(Rect(box.topLeft, boxSize), handleTopPx, widgetCornerDrag(env, box, maxSize, sizeAtTouch, density))
}

/** A hosted widget's resize handles in landscape: its real size, kept to the pane, on the corners away from its anchor. */
@Composable
internal fun LandscapeWidgetResize(env: ClockAdjustEnv, box: ClockBoxMeasure) {
    val density = LocalDensity.current
    val landscape = env.geometry.landscape
    val frame = Rect(box.topLeft, landscape.naturalSize)
    val sizeAtTouch: (Offset) -> Size = { t -> landscapeWidgetSizeAtTouch(t, box.topLeft, landscape.naturalSize, landscape.originY) }
    ClockResizeFrame(frame, landscapeHandleTopPx(env, frame, density), widgetCornerDrag(env, box, landscape.maxWidgetSize, sizeAtTouch, density))
}

/** The dashed frame around the clock being resized, and the two corner handles that drag it. */
@Composable
private fun ClockResizeFrame(frame: Rect, handleTopPx: Float, drag: CornerDrag) {
    val density = LocalDensity.current
    val insetPx = with(density) { HOME_CLOCK_RESIZE_HANDLE_INSET.toPx() }
    val leftPx = frame.left - insetPx
    val rightPx = frame.right - insetPx
    HandleGestureExclusion(exclusionRect(leftPx, handleTopPx, density), exclusionRect(rightPx, handleTopPx, density))
    // One full-size Box, so the frame and both handles position against the same origin as the root.
    Box(modifier = Modifier.fillMaxSize()) {
        Box(
            modifier = Modifier
                .offset { IntOffset(frame.left.roundToInt(), frame.top.roundToInt()) }
                .size(with(density) { frame.width.toDp() }, with(density) { frame.height.toDp() })
                .border(1.dp, Accent.copy(alpha = 0.5f), RoundedCornerShape(12.dp)),
        )
        ClockCornerHandle(
            isRightSide = false,
            modifier = Modifier.offset { IntOffset(leftPx.roundToInt(), handleTopPx.roundToInt()) },
            onDragStart = drag.onStart,
            onDrag = drag.onDrag,
            onDragEnd = drag.onEnd,
        )
        ClockCornerHandle(
            isRightSide = true,
            modifier = Modifier.offset { IntOffset(rightPx.roundToInt(), handleTopPx.roundToInt()) },
            onDragStart = drag.onStart,
            onDrag = drag.onDrag,
            onDragEnd = drag.onEnd,
        )
    }
}

/**
 * Opts the handles' own touch region out of the system's edge-swipe back gesture: the handles can land right at
 * the screen edge (a full-width widget), and Android's gesture (wider on some OEM skins) would take the touch first.
 */
@Composable
private fun HandleGestureExclusion(left: AndroidRect, right: AndroidRect) {
    val view = LocalView.current
    DisposableEffect(view, left, right) {
        view.systemGestureExclusionRects = listOf(left, right)
        onDispose { view.systemGestureExclusionRects = emptyList() }
    }
}

private fun exclusionRect(xPx: Float, yPx: Float, density: Density): AndroidRect {
    val sizePx = with(density) { 40.dp.toPx() }.roundToInt()
    val left = xPx.roundToInt()
    val top = yPx.roundToInt()
    return AndroidRect(left, top, left + sizePx, top + sizePx)
}

/** Landscape's handles sit on the bottom corners of a top-anchored clock, the top corners otherwise. */
private fun landscapeHandleTopPx(env: ClockAdjustEnv, frame: Rect, density: Density): Float {
    val insetPx = with(density) { HOME_CLOCK_RESIZE_HANDLE_INSET.toPx() }
    return if (env.geometry.landscape.originY <= 0f) frame.bottom - insetPx else (frame.top - insetPx).coerceAtLeast(env.metrics.topInsetPx)
}

/** 0 for a left-aligned clock, 0.5 centred, 1 right-aligned: how far across its width the clock's fixed edge sits. */
private fun ClockAlignment.originFraction(): Float = when (this) {
    ClockAlignment.LEFT -> 0f
    ClockAlignment.CENTER -> 0.5f
    ClockAlignment.RIGHT -> 1f
}

/** A hosted widget's drag: live size clamped to [maxSize] and the widget's minimum, saved in dp on release. */
private fun widgetCornerDrag(env: ClockAdjustEnv, box: ClockBoxMeasure, maxSize: Size, sizeAtTouch: (Offset) -> Size, density: Density): CornerDrag {
    val layout = env.layout
    val min = env.widgetMinPx
    val boxSize = box.sizePx
    fun clampWidth(px: Float) = px.coerceIn(min.x, maxOf(min.x, maxSize.width))
    fun clampHeight(px: Float) = px.coerceIn(min.y, maxOf(min.y, maxSize.height))
    return CornerDrag(
        onStart = {
            layout.widgetDragGrabOffset = null
            env.actions.clock.onDraggingHandleChange(true)
        },
        onDrag = { touchInAbsolute ->
            val implied = sizeAtTouch(touchInAbsolute - box.rootOrigin)
            // First move: capture where the finger sits vs the corner, so the box doesn't pop.
            val grab = layout.widgetDragGrabOffset
                ?: Offset(implied.width - (layout.liveWidgetWidthPx ?: boxSize.width), implied.height - (layout.liveWidgetHeightPx ?: boxSize.height))
                    .also { layout.widgetDragGrabOffset = it }
            layout.liveWidgetWidthPx = clampWidth(implied.width - grab.x)
            layout.liveWidgetHeightPx = clampHeight(implied.height - grab.y)
        },
        onEnd = {
            env.actions.clock.onDraggingHandleChange(false)
            env.actions.widget.onSizeCommit(
                with(density) { clampWidth(layout.liveWidgetWidthPx ?: boxSize.width).toDp().value }.roundToInt(),
                with(density) { clampHeight(layout.liveWidgetHeightPx ?: boxSize.height).toDp().value }.roundToInt(),
            )
        },
    )
}
