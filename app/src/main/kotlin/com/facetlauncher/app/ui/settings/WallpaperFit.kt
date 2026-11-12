package com.facetlauncher.app.ui.settings

import android.graphics.RectF
import androidx.compose.runtime.Immutable
import kotlin.math.max

/** The most the fit step zooms in, relative to the zoom that just covers the screen. */
internal const val MAX_FIT_ZOOM = 5f

/** The sizes the fit step works between: the picked [imageWidth] x [imageHeight] photo and the [boxWidth] x [boxHeight] screen-shaped window onto it. */
internal data class FitGeometry(val imageWidth: Int, val imageHeight: Int, val boxWidth: Float, val boxHeight: Float) {
    /** The scale at which the photo exactly covers the box: any smaller would leave an edge empty. */
    val coverScale: Float get() = max(boxWidth / imageWidth, boxHeight / imageHeight)
}

/**
 * Which part of the photo is in the window: [zoom] 1 is the part that just covers it, more is closer; [centerX], [centerY] are the
 * middle of the visible part in photo pixels. Every step keeps the window inside the photo.
 */
@Immutable
internal data class WallpaperFit(val zoom: Float, val centerX: Float, val centerY: Float) {

    /** The part of the photo in the window, in photo pixels, always inside the photo and always the box's shape. */
    fun visible(geometry: FitGeometry): RectF {
        val scale = geometry.coverScale * zoom
        val width = geometry.boxWidth / scale
        val height = geometry.boxHeight / scale
        // The room is clamped at 0: a photo already the box's shape can leave a rounding error just below it.
        val left = (centerX - width / 2f).coerceIn(0f, (geometry.imageWidth - width).coerceAtLeast(0f))
        val top = (centerY - height / 2f).coerceIn(0f, (geometry.imageHeight - height).coerceAtLeast(0f))
        return RectF(left, top, left + width, top + height)
    }

    /** A drag of ([dx], [dy]) box pixels: the photo follows the finger, so the visible part moves the other way. */
    fun panned(dx: Float, dy: Float, geometry: FitGeometry): WallpaperFit {
        val scale = geometry.coverScale * zoom
        return moved(centerX - dx / scale, centerY - dy / scale, zoom, geometry)
    }

    /** A pinch by [factor] about the box point ([focusX], [focusY]): the photo point under it stays under it. */
    fun zoomed(factor: Float, focusX: Float, focusY: Float, geometry: FitGeometry): WallpaperFit {
        val before = visible(geometry)
        val newZoom = (zoom * factor).coerceIn(1f, MAX_FIT_ZOOM)
        val oldScale = geometry.coverScale * zoom
        val newScale = geometry.coverScale * newZoom
        // The photo point under the focus, which must not move on screen.
        val pointX = before.left + focusX / oldScale
        val pointY = before.top + focusY / oldScale
        val left = pointX - focusX / newScale
        val top = pointY - focusY / newScale
        return moved(left + geometry.boxWidth / newScale / 2f, top + geometry.boxHeight / newScale / 2f, newZoom, geometry)
    }

    private fun moved(newCenterX: Float, newCenterY: Float, newZoom: Float, geometry: FitGeometry): WallpaperFit {
        val candidate = WallpaperFit(newZoom, newCenterX, newCenterY).visible(geometry)
        return WallpaperFit(newZoom, candidate.centerX(), candidate.centerY())
    }

    companion object {
        /** Fully zoomed out, centred. */
        fun initial(geometry: FitGeometry) = WallpaperFit(1f, geometry.imageWidth / 2f, geometry.imageHeight / 2f)
    }
}
