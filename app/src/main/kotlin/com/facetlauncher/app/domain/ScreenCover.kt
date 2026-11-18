package com.facetlauncher.app.domain

import android.graphics.RectF
import com.facetlauncher.app.data.model.ScreenSize

/** How much more than a plain cover fit the system magnifies a wallpaper on screen. */
internal const val WALLPAPER_SYSTEM_ZOOM = 1.1f

/**
 * The part of an [imageWidth] x [imageHeight] image that this screen shows once the system has covered the screen with it, centred. One
 * wallpaper image is cropped like this for each screen of a foldable, so the smaller screen's glass copy is cut from the image the
 * larger one was framed on. The system also zooms in about 10% on every screen ([WALLPAPER_SYSTEM_ZOOM], measured on a Pixel Fold and on
 * an emulator), which cancels between screens: pass it as [zoom] to get what the screen really shows.
 */
internal fun ScreenSize.coverRegion(imageWidth: Int, imageHeight: Int, zoom: Float = 1f): RectF {
    val scale = maxOf(width.toFloat() / imageWidth, height.toFloat() / imageHeight) * zoom
    val visibleWidth = (width / scale).coerceAtMost(imageWidth.toFloat())
    val visibleHeight = (height / scale).coerceAtMost(imageHeight.toFloat())
    val left = (imageWidth - visibleWidth) / 2f
    val top = (imageHeight - visibleHeight) / 2f
    return RectF(left, top, left + visibleWidth, top + visibleHeight)
}
