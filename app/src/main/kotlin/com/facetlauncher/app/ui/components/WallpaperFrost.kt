package com.facetlauncher.app.ui.components

import android.graphics.Bitmap
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.ImageBitmap
import kotlin.math.max
import kotlin.math.roundToInt

/**
 * The wallpaper copy already frosted, one picture per theme, small enough to be cheap: the full-screen panels draw it as a plain image instead
 * of blurring the copy again on every frame. A picture is `null` while it is being made, and the panels use their theme tint until then.
 */
@Immutable
class WallpaperFrost(val light: ImageBitmap?, val dark: ImageBitmap?)

/** `null` where there is no usable wallpaper copy; see [WallpaperBackdropHost]. */
val LocalWallpaperFrost = staticCompositionLocalOf<WallpaperFrost?> { null }

/** The long side the copy is reduced to before it is blurred: the blur then costs next to nothing and the smooth result stretches back up cleanly. */
private const val FROST_LONG_SIDE = 320
private const val BLUR_PASSES = 3
private const val BYTE_MAX = 255
private const val OPAQUE = 0xFF shl 24

/**
 * [copy] reduced, blurred by about [blurPx] (measured at the copy's own size), then lifted by [brightness] (added to each channel, 0..1) and
 * made [saturation] times as vivid. Three box passes stand in for the Gaussian blur the live glass uses.
 */
internal fun frostedCopy(copy: Bitmap, blurPx: Float, brightness: Float, saturation: Float): Bitmap {
    var small = if (copy.config == Bitmap.Config.HARDWARE) copy.copy(Bitmap.Config.ARGB_8888, false) else copy
    while (max(small.width, small.height) > FROST_LONG_SIDE && small.width >= 2 && small.height >= 2) {
        small = Bitmap.createScaledBitmap(small, small.width / 2, small.height / 2, true)
    }
    val width = small.width
    val height = small.height
    val pixels = IntArray(width * height).also { small.getPixels(it, 0, width, 0, 0, width, height) }
    val radius = max(1, (blurPx * width / copy.width).roundToInt())
    repeat(BLUR_PASSES) {
        val line = IntArray(max(width, height))
        for (y in 0 until height) blurLine(pixels, y * width, 1, width, radius, line)
        for (x in 0 until width) blurLine(pixels, x, width, height, radius, line)
    }
    for (i in pixels.indices) pixels[i] = adjusted(pixels[i], brightness, saturation)
    return Bitmap.createBitmap(pixels, width, height, Bitmap.Config.ARGB_8888)
}

/** One row or column of [pixels] ([n] of them from [start], [step] apart) blurred in place with a sliding window, edges clamped. */
private fun blurLine(pixels: IntArray, start: Int, step: Int, n: Int, radius: Int, line: IntArray) {
    for (i in 0 until n) line[i] = pixels[start + i * step]
    var red = 0
    var green = 0
    var blue = 0
    for (k in -radius..radius) {
        val c = line[k.coerceIn(0, n - 1)]
        red += c shr 16 and BYTE_MAX
        green += c shr 8 and BYTE_MAX
        blue += c and BYTE_MAX
    }
    val window = 2 * radius + 1
    for (i in 0 until n) {
        pixels[start + i * step] = OPAQUE or (red / window shl 16) or (green / window shl 8) or (blue / window)
        val out = line[(i - radius).coerceIn(0, n - 1)]
        val next = line[(i + radius + 1).coerceIn(0, n - 1)]
        red += (next shr 16 and BYTE_MAX) - (out shr 16 and BYTE_MAX)
        green += (next shr 8 and BYTE_MAX) - (out shr 8 and BYTE_MAX)
        blue += (next and BYTE_MAX) - (out and BYTE_MAX)
    }
}

private const val LUMA_RED = 0.213f
private const val LUMA_GREEN = 0.715f
private const val LUMA_BLUE = 0.072f

private fun adjusted(pixel: Int, brightness: Float, saturation: Float): Int {
    val red = (pixel shr 16 and BYTE_MAX) / BYTE_MAX.toFloat()
    val green = (pixel shr 8 and BYTE_MAX) / BYTE_MAX.toFloat()
    val blue = (pixel and BYTE_MAX) / BYTE_MAX.toFloat()
    val luma = LUMA_RED * red + LUMA_GREEN * green + LUMA_BLUE * blue
    fun channel(value: Float) = ((luma + (value - luma) * saturation + brightness).coerceIn(0f, 1f) * BYTE_MAX).roundToInt()
    return OPAQUE or (channel(red) shl 16) or (channel(green) shl 8) or channel(blue)
}
