package com.facetlauncher.app.ui.home

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.unit.IntSize
import com.facetlauncher.app.data.model.ClockAlignment

/** The handle's position (measured from the top of the content area) can never exceed this fraction of the content height. */
internal const val HOME_CLOCK_ZONE_MAX_FRACTION = 0.5f

/** Where the handle (the divider above the app list) sits, as a fraction of the content height, until the user moves it. */
internal const val HOME_CLOCK_DEFAULT_ZONE_FRACTION = 0.4f

/** The clamp for the clock's scale factor, shared by the live render, the handle positioning and the committed value. */
internal const val HOME_CLOCK_MIN_SCALE = 0.5f
internal const val HOME_CLOCK_MAX_SCALE = 2.0f

/** The fixed measures the clock geometry works in, already in px. */
internal data class ClockMetrics(
    val topInsetPx: Float,
    val minGapPx: Float,
    /** Headroom above the clock's pinned bottom: past the status bar, plus room for the resize handle. */
    val topReservePx: Float,
    val sideMarginPx: Float,
    /** What a handle needs above the clock: status bar and resize handles, without the top inset. */
    val handleHeadroomPx: Float,
    val landscapeMarginsPx: Float,
)

/** A hosted clock widget's size in px: what is saved, what a drag holds in flight, and the smallest it may be. */
internal data class ClockWidgetMeasure(
    /** `null` while the widget is bound but its size is not known yet. */
    val saved: Offset?,
    val liveWidthPx: Float?,
    val liveHeightPx: Float?,
    val minWidthPx: Float,
    val minHeightPx: Float,
)

/** Everything the clock geometry reads, as plain numbers, so the maths needs no composition. */
internal data class ClockGeometryInputs(
    val landscape: Boolean,
    val adjusting: Boolean,
    val alignment: ClockAlignment,
    val scale: Float,
    val liveScale: Float?,
    val zoneHeightPx: Float?,
    val liveHandlePx: Float?,
    val liveDividerWidthPx: Float?,
    /** `null` while the native clock is shown. */
    val widget: ClockWidgetMeasure?,
    val boxSize: IntSize?,
    val boxOriginInRoot: Offset?,
    val rootOriginInRoot: Offset?,
    val rootWidthPx: Int?,
    val clockNaturalHeightPx: Float,
    val contentHeightPx: Float,
    val landscapeContentWidthPx: Float,
    /** Bottom edge of the whole portrait clock block, measured in the same pass as the box. */
    val blockBottomInRoot: Float?,
    val metrics: ClockMetrics,
)

/** What the clock may grow to, given where it sits: the portrait fit limits for the native clock and a hosted widget. */
internal data class ClockFit(
    val alignment: ClockAlignment,
    val topReservePx: Float,
    val sideMarginPx: Float,
    /** Where the clock block's bottom is meant to be: the handle less the minimum gap, below the top inset. */
    val targetBottomPx: Float,
    val blockBottomInRoot: Float?,
    val rootOriginY: Float,
) {
    /** The largest scale at which the native clock, scaled around its bottom and aligned edge, still fits. */
    fun maxScale(natSize: IntSize, natTopLeft: Offset, rootW: Int): Float {
        val natW = natSize.width.toFloat()
        val natH = natSize.height.toFloat()
        if (natW <= 0f || natH <= 0f) return HOME_CLOCK_MAX_SCALE
        val byHeight = (natTopLeft.y + natH - topReservePx) / natH
        val byWidth = when (alignment) {
            ClockAlignment.CENTER -> (rootW - 2f * sideMarginPx) / natW
            ClockAlignment.LEFT -> (rootW - sideMarginPx - natTopLeft.x) / natW
            ClockAlignment.RIGHT -> (natTopLeft.x + natW - sideMarginPx) / natW
        }
        return minOf(HOME_CLOCK_MAX_SCALE, byHeight, byWidth).coerceIn(HOME_CLOCK_MIN_SCALE, HOME_CLOCK_MAX_SCALE)
    }

    /**
     * The largest real size a hosted widget may be, for its current box. The box's bottom is the block's target
     * bottom less what sits under the box; taking it from the box's own (a frame stale) position made the height
     * feed back on itself and jiggle.
     */
    fun maxWidgetSize(boxSize: IntSize, boxTopLeft: Offset, rootW: Int): Offset {
        val boxW = boxSize.width.toFloat()
        val boxH = boxSize.height.toFloat()
        val tailPx = blockBottomInRoot?.let { it - rootOriginY - (boxTopLeft.y + boxH) }
        val boxBottom = if (tailPx != null) targetBottomPx - tailPx else boxTopLeft.y + boxH
        val maxWidth = when (alignment) {
            ClockAlignment.CENTER -> rootW - 2f * sideMarginPx
            ClockAlignment.LEFT -> rootW - sideMarginPx - boxTopLeft.x
            ClockAlignment.RIGHT -> boxTopLeft.x + boxW - sideMarginPx
        }
        return Offset(maxWidth, boxBottom - topReservePx)
    }
}

/** Where the portrait height handle may sit, and where it is. */
internal data class HandleBounds(val basePx: Float, val floorPx: Float, val ceilingPx: Float, val handlePx: Float)

/** Landscape's limits: what its clock pane can take, before the pane is sized to the clock actually drawn. */
internal data class LandscapeLimits(
    val originY: Float,
    val contentWidthPx: Float,
    val naturalSize: Size,
    val minClockWidthPx: Float,
    val maxWidgetSize: Size,
    val maxScale: Float,
)

/** The clock's resolved geometry for one frame. */
internal data class ClockGeometry(
    val handle: HandleBounds,
    val fit: ClockFit,
    val landscape: LandscapeLimits,
    /** The scale the native clock renders at. */
    val effectiveScale: Float,
    /** The hosted widget's rendered size, `null` while there is none. */
    val effectiveWidgetSizePx: Offset?,
    val landscapePaneWidthPx: Float,
)

internal fun computeClockGeometry(i: ClockGeometryInputs): ClockGeometry {
    val handle = portraitHandleBounds(i)
    val fit = ClockFit(
        alignment = i.alignment,
        topReservePx = i.metrics.topReservePx,
        sideMarginPx = i.metrics.sideMarginPx,
        targetBottomPx = i.metrics.topInsetPx + handle.handlePx - i.metrics.minGapPx,
        blockBottomInRoot = i.blockBottomInRoot,
        rootOriginY = i.rootOriginInRoot?.y ?: 0f,
    )
    val landscape = landscapeLimits(i)
    val effectiveScale = effectiveClockScale(i, fit, landscape.maxScale)
    val effectiveWidget = effectiveWidgetSize(i, fit, landscape.maxWidgetSize)
    return ClockGeometry(
        handle = handle,
        fit = fit,
        landscape = landscape,
        effectiveScale = effectiveScale,
        effectiveWidgetSizePx = effectiveWidget,
        landscapePaneWidthPx = landscapePaneWidth(i, landscape, effectiveScale, effectiveWidget),
    )
}

/** One formula covers the persisted and default (`null`) cases: the default places the handle so the block's own offset resolves to its original position. */
internal fun portraitHandleBounds(i: ClockGeometryInputs): HandleBounds {
    val m = i.metrics
    val base = i.zoneHeightPx ?: (HOME_CLOCK_DEFAULT_ZONE_FRACTION * i.contentHeightPx)
    val floor = clockHandleFloorPx(
        blockHeightPx = i.clockNaturalHeightPx,
        clockBoxHeightPx = i.boxSize?.height?.toFloat() ?: i.clockNaturalHeightPx,
        headroomPx = m.handleHeadroomPx,
        minGapPx = m.minGapPx,
        minScale = HOME_CLOCK_MIN_SCALE,
        canShrink = i.widget == null && i.boxSize != null,
    )
    // maxOf guards against the content height still being 0 before the first layout, which would put the ceiling below the floor.
    val ceiling = maxOf(floor, HOME_CLOCK_ZONE_MAX_FRACTION * i.contentHeightPx)
    return HandleBounds(base, floor, ceiling, (i.liveHandlePx ?: base).coerceIn(floor, ceiling))
}

internal fun landscapeLimits(i: ClockGeometryInputs): LandscapeLimits {
    val margins = i.metrics.landscapeMarginsPx
    val contentWidth = if (i.landscapeContentWidthPx > 0f) i.landscapeContentWidthPx else (i.rootWidthPx?.toFloat() ?: 0f)
    val natural = i.boxSize?.let { Size(it.width.toFloat(), it.height.toFloat()) } ?: Size.Zero
    // The narrowest the clock may be: the native clock at its smallest scale, or the hosted widget's own minimum.
    val minClockWidth = i.widget?.minWidthPx ?: (natural.width * HOME_CLOCK_MIN_SCALE)
    // Resolved against the smallest clock, so a narrow pane shrinks the clock to fit rather than being refused.
    val paneForFit = landscapeClockPaneWidthPx(wantedPaneWidthPx(i), contentWidth, floorPx = margins + minClockWidth)
    return LandscapeLimits(
        originY = when (i.alignment) {
            ClockAlignment.LEFT -> 0f
            ClockAlignment.CENTER -> 0.5f
            ClockAlignment.RIGHT -> 1f
        },
        contentWidthPx = contentWidth,
        naturalSize = natural,
        minClockWidthPx = minClockWidth,
        maxWidgetSize = if (contentWidth > 0f) {
            landscapeMaxWidgetSize(paneForFit, i.contentHeightPx, margins)
        } else {
            Size(Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY)
        },
        maxScale = landscapeMaxClockScale(natural, paneForFit, i.contentHeightPx, margins, HOME_CLOCK_MIN_SCALE, HOME_CLOCK_MAX_SCALE),
    )
}

/** The divider's live width while dragging, else the saved one (which is also portrait's zone height). */
private fun wantedPaneWidthPx(i: ClockGeometryInputs): Float? = i.liveDividerWidthPx ?: i.zoneHeightPx

/**
 * The scale the clock renders at: the persisted value (live while dragging), clamped to what fits. Landscape
 * always clamps to its pane, portrait only while adjusting, and neither changes the saved value.
 */
internal fun effectiveClockScale(i: ClockGeometryInputs, fit: ClockFit, landscapeMaxScale: Float): Float {
    val raw = i.liveScale ?: i.scale
    val size = i.boxSize
    val origin = i.boxOriginInRoot
    val rootW = i.rootWidthPx
    return when {
        i.landscape -> raw.coerceIn(HOME_CLOCK_MIN_SCALE, landscapeMaxScale)
        i.adjusting && size != null && origin != null && rootW != null ->
            raw.coerceIn(HOME_CLOCK_MIN_SCALE, fit.maxScale(size, origin - (i.rootOriginInRoot ?: Offset.Zero), rootW))
        else -> raw.coerceIn(HOME_CLOCK_MIN_SCALE, HOME_CLOCK_MAX_SCALE)
    }
}

/**
 * A hosted widget's rendered size: real width and height clamped to what fits (landscape's pane, or portrait's
 * measured room, in read mode too, so a size saved at a larger window can't spill out). The saved size is untouched.
 */
internal fun effectiveWidgetSize(i: ClockGeometryInputs, fit: ClockFit, landscapeMax: Size): Offset? {
    val widget = i.widget
    val saved = widget?.saved
    if (widget == null || saved == null) return null
    val width = widget.liveWidthPx ?: saved.x
    val height = widget.liveHeightPx ?: saved.y
    val size = i.boxSize
    val origin = i.boxOriginInRoot
    val rootW = i.rootWidthPx
    return when {
        i.landscape -> clampWidget(widget, width, height, landscapeMax.width, landscapeMax.height)
        size != null && origin != null && rootW != null -> {
            val max = fit.maxWidgetSize(size, origin - (i.rootOriginInRoot ?: Offset.Zero), rootW)
            clampWidget(widget, width, height, max.x, max.y)
        }
        else -> Offset(width.coerceAtLeast(widget.minWidthPx), height.coerceAtLeast(widget.minHeightPx))
    }
}

private fun clampWidget(widget: ClockWidgetMeasure, width: Float, height: Float, maxWidth: Float, maxHeight: Float) = Offset(
    width.coerceIn(widget.minWidthPx, maxOf(widget.minWidthPx, maxWidth)),
    height.coerceIn(widget.minHeightPx, maxOf(widget.minHeightPx, maxHeight)),
)

/** Landscape's clock pane width: at least the margins plus the clock as drawn (a widget its own width, the native clock its natural width times its scale). */
internal fun landscapePaneWidth(i: ClockGeometryInputs, landscape: LandscapeLimits, effectiveScale: Float, effectiveWidget: Offset?): Float {
    val drawnWidth = if (i.widget != null) effectiveWidget?.x ?: landscape.naturalSize.width else landscape.naturalSize.width * effectiveScale
    return landscapeClockPaneWidthPx(wantedPaneWidthPx(i), landscape.contentWidthPx, floorPx = i.metrics.landscapeMarginsPx + drawnWidth)
}
