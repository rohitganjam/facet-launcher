package com.facetlauncher.app.ui.settings

import android.graphics.RectF
import androidx.compose.runtime.Immutable
import kotlin.math.max

/** The most the fit step zooms in, relative to the zoom that just covers the screen. */
internal const val MAX_FIT_ZOOM = 5f

/**
 * The sizes the fit step works between: the picked [imageWidth] x [imageHeight] photo and the [boxWidth] x [boxHeight] screen-shaped window onto it.
 * [systemZoom] is how much the system magnifies a wallpaper about its middle once set (1 for a screenshot, which is already as shown): the window then
 * shows what the screen will show, so it can never reach closer to an edge than the extra the system hides there.
 */
internal data class FitGeometry(val imageWidth: Int, val imageHeight: Int, val boxWidth: Float, val boxHeight: Float, val systemZoom: Float = 1f) {
    /** The scale at which the photo exactly covers the box: any smaller would leave an edge empty. */
    val coverScale: Float get() = max(boxWidth / imageWidth, boxHeight / imageHeight)
}

/**
 * Which part of the photo is in the window: [zoom] 1 is the part that just covers it (the least is the geometry's system zoom), more is closer; [centerX], [centerY] are the
 * middle of the visible part in photo pixels. Every step keeps the window inside the photo.
 */
@Immutable
internal data class WallpaperFit(val zoom: Float, val centerX: Float, val centerY: Float) {

    /** The part of the photo in the window, in photo pixels, always inside the photo and always the box's shape. */
    fun visible(geometry: FitGeometry): RectF {
        val scale = geometry.coverScale * zoom
        val width = geometry.boxWidth / scale
        val height = geometry.boxHeight / scale
        // What the system magnifies away must still be in the photo: the window keeps that margin from each edge, and the room is clamped at the margin
        // since a photo already the box's shape can leave a rounding error just below it.
        val marginX = width * (geometry.systemZoom - 1f) / 2f
        val marginY = height * (geometry.systemZoom - 1f) / 2f
        val left = (centerX - width / 2f).coerceIn(marginX, (geometry.imageWidth - width - marginX).coerceAtLeast(marginX))
        val top = (centerY - height / 2f).coerceIn(marginY, (geometry.imageHeight - height - marginY).coerceAtLeast(marginY))
        return RectF(left, top, left + width, top + height)
    }

    /** The part of the photo to set as the wallpaper so that, magnified by the system's zoom about its middle, the screen shows [visible]. */
    fun wallpaperPart(geometry: FitGeometry): RectF {
        val shown = visible(geometry)
        val halfWidth = shown.width() * geometry.systemZoom / 2f
        val halfHeight = shown.height() * geometry.systemZoom / 2f
        return RectF(shown.centerX() - halfWidth, shown.centerY() - halfHeight, shown.centerX() + halfWidth, shown.centerY() + halfHeight)
    }

    /** A drag of ([dx], [dy]) box pixels: the photo follows the finger, so the visible part moves the other way. */
    fun panned(dx: Float, dy: Float, geometry: FitGeometry): WallpaperFit {
        val scale = geometry.coverScale * zoom
        return moved(centerX - dx / scale, centerY - dy / scale, zoom, geometry)
    }

    /** A pinch by [factor] about the box point ([focusX], [focusY]): the photo point under it stays under it. */
    fun zoomed(factor: Float, focusX: Float, focusY: Float, geometry: FitGeometry): WallpaperFit {
        val before = visible(geometry)
        val newZoom = (zoom * factor).coerceIn(geometry.systemZoom, MAX_FIT_ZOOM)
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
        /** Fully zoomed out (as far as the system's zoom allows), centred. */
        fun initial(geometry: FitGeometry) = WallpaperFit(geometry.systemZoom, geometry.imageWidth / 2f, geometry.imageHeight / 2f)
    }
}
