package com.facetlauncher.app.data.model

import android.graphics.Bitmap
import androidx.compose.runtime.Immutable

/**
 * The current system wallpaper, resolved for rendering behind a preview surface (the facet
 * carousel cards, the Appearance preview card) that can't rely on the Activity's own
 * `windowShowWallpaper` compositing the way Home does. Produced by
 * [com.facetlauncher.app.data.WallpaperRepository]; the fallbacks degrade in order —
 * [Image] (an actual wallpaper bitmap), then [Tones] (its Material You colours, the only thing
 * available for a live wallpaper), then [Unavailable] (render the plain `Wallpaper` token).
 *
 * Framework/primitive types only — no `androidx.compose.ui.graphics` here.
 */
@Immutable
sealed interface HomeWallpaper {
    data class Image(val bitmap: Bitmap) : HomeWallpaper

    /** One to three ARGB ints (primary/secondary/tertiary), painted as a vertical gradient. */
    data class Tones(val argb: List<Int>) : HomeWallpaper

    data object Unavailable : HomeWallpaper
}
