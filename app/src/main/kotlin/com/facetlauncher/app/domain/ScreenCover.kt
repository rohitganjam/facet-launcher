package com.facetlauncher.app.domain

import android.graphics.RectF
import com.facetlauncher.app.data.model.ScreenSize

/**
 * The part of an [imageWidth] x [imageHeight] image that this screen shows once the system has covered the screen with it, centred. One
 * wallpaper image is cropped like this for each screen of a foldable, so the smaller screen's glass copy is cut from the image the
 * larger one was framed on. Measured on a Pixel Fold the system also zooms in about 10% on both screens, which cancels between them.
 */
internal fun ScreenSize.coverRegion(imageWidth: Int, imageHeight: Int): RectF {
    val scale = maxOf(width.toFloat() / imageWidth, height.toFloat() / imageHeight)
    val visibleWidth = (width / scale).coerceAtMost(imageWidth.toFloat())
    val visibleHeight = (height / scale).coerceAtMost(imageHeight.toFloat())
    val left = (imageWidth - visibleWidth) / 2f
    val top = (imageHeight - visibleHeight) / 2f
    return RectF(left, top, left + visibleWidth, top + visibleHeight)
}
