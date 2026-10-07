package com.facetlauncher.app.data

import android.annotation.SuppressLint
import android.app.WallpaperManager
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import androidx.core.graphics.drawable.toBitmap
import com.facetlauncher.app.data.model.HomeWallpaper
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/** Largest side of the bitmap handed to the UI — wallpapers are often 2× screen for parallax
 *  scroll, and the preview surfaces that consume this render well under a screenful. */
private const val MAX_WALLPAPER_DIMENSION = 1080

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
) {
    /**
     * Degrades in order: the set home wallpaper, then the built-in default, then the wallpaper's
     * Material You tones (the only thing a live wallpaper exposes), then [HomeWallpaper.Unavailable].
     * `peekDrawable`/`builtInDrawable` need Facet to be the active launcher on API 33 — a denial
     * just advances the chain.
     */
    @SuppressLint("MissingPermission") // a SecurityException is caught below and just advances the chain
    open suspend fun currentHomeWallpaper(): HomeWallpaper = withContext(Dispatchers.IO) {
        val drawable = runCatching { wallpaperManager.peekDrawable() }.getOrNull()
            ?: runCatching { wallpaperManager.builtInDrawable }.getOrNull()
        drawable?.toBitmapOrNull()?.let { return@withContext HomeWallpaper.Image(it.downscaled()) }

        val tones = runCatching { wallpaperManager.getWallpaperColors(WallpaperManager.FLAG_SYSTEM) }.getOrNull()
            ?.let { listOfNotNull(it.primaryColor, it.secondaryColor, it.tertiaryColor).map(Color::toArgb) }
        if (!tones.isNullOrEmpty()) return@withContext HomeWallpaper.Tones(tones)

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
