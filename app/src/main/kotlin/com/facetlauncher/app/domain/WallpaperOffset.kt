package com.facetlauncher.app.domain

import android.os.Build

/** Android 14 moved wallpaper cropping into the system, and with it a pan range of about one screen width that starts at the wallpaper's left edge. */
private const val FIRST_SDK_WITH_CAPPED_PAN = Build.VERSION_CODES.UPSIDE_DOWN_CAKE

/** Where the system's pan range starts and how long it is, as fractions of the screen width (measured on API 36 with a square and a 16:9 wallpaper). */
private const val PAN_START = 0.033f
private const val PAN_RANGE = 0.95f
private const val CENTRE = 0.5f

/**
 * The horizontal offset that makes the system show the middle of a wallpaper of [bitmapWidth] x [bitmapHeight] on a [screenWidth] x [screenHeight] screen
 * (portrait: it covers the height and pans sideways). Before Android 14 the offset is a fraction of the whole
 * slack, so 0.5 is the middle; from 14 on it moves a window along a range of about a screen width from the left edge, so the offset for the middle is
 * worked out from the wallpaper's size and kept within 0 to 1.
 */
internal fun wallpaperOffsetX(bitmapWidth: Int, bitmapHeight: Int, screenWidth: Int, screenHeight: Int, sdk: Int = Build.VERSION.SDK_INT): Float {
    if (sdk < FIRST_SDK_WITH_CAPPED_PAN || bitmapWidth <= 0 || bitmapHeight <= 0) return CENTRE
    val cover = maxOf(screenWidth.toFloat() / bitmapWidth, screenHeight.toFloat() / bitmapHeight)
    val window = screenWidth.toFloat()
    val windowLeft = (bitmapWidth * cover - window) / 2f
    return ((windowLeft - PAN_START * screenWidth) / (PAN_RANGE * screenWidth)).coerceIn(0f, 1f)
}
