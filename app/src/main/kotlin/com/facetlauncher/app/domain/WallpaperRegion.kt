package com.facetlauncher.app.domain

import android.graphics.Bitmap
import android.graphics.RectF
import com.facetlauncher.app.data.model.ScreenSize

/**
 * The part of a photo to set as the wallpaper once the user framed [visible]: the framing widened evenly around its centre along its shorter side as
 * far as the photo allows, up to a square of its longer side (the nearer edge stops both sides, so the framing stays at the centre). The orientation it
 * was framed in shows exactly the framing, the other as much more as the photo had.
 */
internal fun wallpaperRegion(visible: RectF, photoWidth: Int, photoHeight: Int): RectF {
    val side = maxOf(visible.width(), visible.height())
    return if (visible.width() < visible.height()) {
        val half = minOf(side / 2, visible.centerX(), photoWidth - visible.centerX()).coerceAtLeast(visible.width() / 2)
        RectF(visible.centerX() - half, visible.top, visible.centerX() + half, visible.bottom)
    } else {
        val half = minOf(side / 2, visible.centerY(), photoHeight - visible.centerY()).coerceAtLeast(visible.height() / 2)
        RectF(visible.left, visible.centerY() - half, visible.right, visible.centerY() + half)
    }
}

/** The size to give a wallpaper cut from [region]: its longer side as long as the longest of [screens], the other in proportion. */
internal fun wallpaperSize(region: RectF, screens: List<ScreenSize>): Pair<Int, Int> {
    val longest = screens.maxOf { maxOf(it.width, it.height) }
    val scale = longest / maxOf(region.width(), region.height())
    return if (region.width() >= region.height()) longest to (region.height() * scale).toInt().coerceAtLeast(1) else (region.width() * scale).toInt().coerceAtLeast(1) to longest
}

/** What [view] shows of this wallpaper, once the system has covered the view with it, centred and magnified: the glass copy for that view. */
internal fun Bitmap.copyFor(view: ScreenSize): Bitmap = croppedTo(view.coverRegion(width, height, WALLPAPER_SYSTEM_ZOOM), view.width, view.height)
