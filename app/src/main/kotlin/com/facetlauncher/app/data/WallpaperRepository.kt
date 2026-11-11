package com.facetlauncher.app.data

import android.annotation.SuppressLint
import android.app.WallpaperManager
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import androidx.core.graphics.ColorUtils
import androidx.core.graphics.drawable.toBitmap
import com.facetlauncher.app.data.di.IoDispatcher
import com.facetlauncher.app.data.model.HomeWallpaper
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/** Largest side of the bitmap handed to the UI — wallpapers are often 2× screen for parallax
 *  scroll, and the preview surfaces that consume this render well under a screenful. */
private const val MAX_WALLPAPER_DIMENSION = 1080

/** How much of the secondary colour reaches the bottom of the wash; the rest stays primary. */
private const val SECONDARY_SHARE = 0.4f

/**
 * Reads the current *system* wallpaper (whoever set it — the OS picker, another launcher, a
 * factory default) for rendering inside a preview surface. [WallpaperManager] always reflects
 * what's live on screen; Facet's own "Change wallpaper" row just fires `ACTION_SET_WALLPAPER`
 * and owns nothing.
 *
 * `open` (and `open` methods) so tests can substitute a fixed value — same pattern as
 * [CalendarPermissionRepository].
 */
@Singleton
open class WallpaperRepository @Inject constructor(
    private val wallpaperManager: WallpaperManager,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO,
) {
    /**
     * Degrades in order: the set home wallpaper, then its primary colour washing toward a blend of its
     * secondary (the only thing a live wallpaper exposes), then [HomeWallpaper.Unavailable]. The colours
     * are raw: [com.facetlauncher.app.ui.components.WallpaperBackground] sets brightness for the theme.
     * `peekDrawable` throws without storage access (API 30 and 36, default launcher or not), so
     * the image is rare. The built-in default is deliberately not used: it is not the user's wallpaper.
     */
    @SuppressLint("MissingPermission") // a SecurityException is caught below and just advances the chain
    open suspend fun currentHomeWallpaper(): HomeWallpaper = withContext(ioDispatcher) {
        runCatching { wallpaperManager.peekDrawable() }.getOrNull()?.toBitmapOrNull()
            ?.let { return@withContext HomeWallpaper.Image(it.downscaled()) }

        val colors = runCatching { wallpaperManager.getWallpaperColors(WallpaperManager.FLAG_SYSTEM) }.getOrNull()
        val primary = colors?.primaryColor?.toArgb()
        val secondary = colors?.secondaryColor?.toArgb()
        if (primary != null) {
            val bottom = secondary?.let { ColorUtils.blendARGB(primary, it, SECONDARY_SHARE) } ?: primary
            return@withContext HomeWallpaper.Tones(listOf(primary, bottom))
        }

        HomeWallpaper.Unavailable
    }

    private fun Drawable.toBitmapOrNull(): Bitmap? {
        (this as? BitmapDrawable)?.bitmap?.let { return it }
        if (intrinsicWidth <= 0 || intrinsicHeight <= 0) return null
        return runCatching { toBitmap() }.getOrNull()
    }

    private fun Bitmap.downscaled(): Bitmap {
        val longest = maxOf(width, height)
        if (longest <= MAX_WALLPAPER_DIMENSION) return this
        val ratio = MAX_WALLPAPER_DIMENSION.toFloat() / longest
        return Bitmap.createScaledBitmap(this, (width * ratio).toInt(), (height * ratio).toInt(), true)
    }
}
