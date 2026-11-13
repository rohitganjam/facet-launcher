package com.facetlauncher.app.domain

import com.facetlauncher.app.data.model.ImageRegion

/** The part of a photo to decode for a portrait wallpaper, and the power-of-two [sampleSize] to decode it at. */
data class PortraitCrop(val region: ImageRegion, val sampleSize: Int)

/** The most a daily photo's crop may be enlarged to fill the screen's width before it looks soft. */
internal const val MAX_DAILY_UPSCALE = 1.35

/**
 * The centre of an [imageWidth] x [imageHeight] photo in the screen's shape, full height for a wider photo and full width for a taller one,
 * or `null` when that part would have to be enlarged more than [MAX_DAILY_UPSCALE] times to fill the screen. The sample size is the largest
 * that still leaves the part at least screen-sized, so a large photo is not decoded at full size.
 */
fun portraitCentreCrop(imageWidth: Int, imageHeight: Int, screenWidth: Int, screenHeight: Int): PortraitCrop? {
    if (imageWidth <= 0 || imageHeight <= 0 || screenWidth <= 0 || screenHeight <= 0) return null
    val screenAspect = screenWidth.toDouble() / screenHeight
    val wider = imageWidth.toDouble() / imageHeight > screenAspect
    val width = if (wider) (imageHeight * screenAspect).toInt() else imageWidth
    val height = if (wider) imageHeight else (imageWidth / screenAspect).toInt()
    if (width <= 0 || height <= 0 || screenWidth.toDouble() / width > MAX_DAILY_UPSCALE) return null
    val left = (imageWidth - width) / 2
    val top = (imageHeight - height) / 2
    var sampleSize = 1
    while (width / (sampleSize * 2) >= screenWidth && height / (sampleSize * 2) >= screenHeight) sampleSize *= 2
    return PortraitCrop(ImageRegion(left, top, left + width, top + height), sampleSize)
}
