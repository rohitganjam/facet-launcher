package com.facetlauncher.app.data.icon

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Rect
import android.graphics.drawable.AdaptiveIconDrawable
import android.graphics.drawable.Drawable
import android.os.Build
import com.facetlauncher.app.data.ADAPTIVE_ICON_CANVAS_SIZE
import com.facetlauncher.app.data.ADAPTIVE_ICON_SCALE

/** Side, in pixels, the glyph and outline masks are computed at. */
const val ICON_MASK_SIZE = 192

/** Side, in pixels, of what is kept per app: the themed layer's coverage and the finished glyph and outline pictures. */
const val STORED_ICON_SIZE = 128

/** The app's themed-icon layer drawn like `flattenIcon` draws the others, or null when the app ships none. */
fun monochromeLayerBitmap(drawable: Drawable): Bitmap? {
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU || drawable !is AdaptiveIconDrawable) return null
    val layer = drawable.monochrome ?: return null
    val width = drawable.intrinsicWidth.takeIf { it > 0 } ?: ADAPTIVE_ICON_CANVAS_SIZE
    val height = drawable.intrinsicHeight.takeIf { it > 0 } ?: ADAPTIVE_ICON_CANVAS_SIZE
    val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
    val insetX = (width * (ADAPTIVE_ICON_SCALE - 1f) / 2f).toInt()
    val insetY = (height * (ADAPTIVE_ICON_SCALE - 1f) / 2f).toInt()
    layer.bounds = Rect(-insetX, -insetY, width + insetX, height + insetY)
    layer.draw(Canvas(bitmap))
    return bitmap
}

/** The themed layer's coverage alone, small enough to keep for every app. */
fun monochromeCoverage(drawable: Drawable): Bitmap? =
    monochromeLayerBitmap(drawable)?.let { Bitmap.createScaledBitmap(it, STORED_ICON_SIZE, STORED_ICON_SIZE, true).extractAlpha() }

/** A finished glyph or outline picture, from [ICON_MASK_SIZE] ARGB pixels down to [STORED_ICON_SIZE]. */
fun styledBitmap(argb: IntArray): Bitmap =
    Bitmap.createScaledBitmap(Bitmap.createBitmap(argb, ICON_MASK_SIZE, ICON_MASK_SIZE, Bitmap.Config.ARGB_8888), STORED_ICON_SIZE, STORED_ICON_SIZE, true)

fun Bitmap.pixels(): IntArray {
    val scaled = if (width == ICON_MASK_SIZE && height == ICON_MASK_SIZE) this else Bitmap.createScaledBitmap(this, ICON_MASK_SIZE, ICON_MASK_SIZE, true)
    return IntArray(ICON_MASK_SIZE * ICON_MASK_SIZE).also { scaled.getPixels(it, 0, ICON_MASK_SIZE, 0, 0, ICON_MASK_SIZE, ICON_MASK_SIZE) }
}
