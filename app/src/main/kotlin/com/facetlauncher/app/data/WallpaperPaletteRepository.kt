package com.facetlauncher.app.data

import android.app.WallpaperManager
import android.graphics.Bitmap
import android.os.Build
import android.os.Handler
import android.os.Looper
import com.facetlauncher.app.data.di.DefaultDispatcher
import com.facetlauncher.app.data.model.WallpaperPalette
import com.materialkolor.hct.Hct
import com.materialkolor.quantize.QuantizerCelebi
import com.materialkolor.scheme.SchemeTonalSpot
import com.materialkolor.score.Score
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.withContext
import kotlin.math.max
import kotlin.math.roundToInt

/**
 * Material You accent colours computed in Facet with Google's material-color-utilities, so they match the wallpaper Facet shows and
 * exist on every API level. From Facet's own wallpaper copy when there is one; with none, `null` on Android 12+ (the theme then reads
 * the system's Material You, which keeps a palette picked in Wallpaper & style), and on Android 11 a palette seeded from the colours
 * Android reports for the wallpaper.
 */
@Singleton
open class WallpaperPaletteRepository @Inject constructor(
    private val wallpaperManager: WallpaperManager,
    @DefaultDispatcher private val defaultDispatcher: CoroutineDispatcher,
) {
    /** The palette for [copy], Facet's current (still valid) wallpaper copy, or `null` to use the system's Material You. */
    open suspend fun paletteFor(copy: Bitmap?): WallpaperPalette? = withContext(defaultDispatcher) {
        val seed = copy?.let { seedFromPixels(it.samplePixels()) }
            ?: if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S) systemSeed() else null
        seed?.let(::wallpaperPaletteFrom)
    }

    /** Emits when Android reports new colours for the Home wallpaper, wherever it was set. */
    open val systemColorChanges: Flow<Unit> = callbackFlow {
        val listener = WallpaperManager.OnColorsChangedListener { _, which ->
            if (which and WallpaperManager.FLAG_SYSTEM != 0) trySend(Unit)
        }
        wallpaperManager.addOnColorsChangedListener(listener, Handler(Looper.getMainLooper()))
        awaitClose { wallpaperManager.removeOnColorsChangedListener(listener) }
    }

    private fun systemSeed(): Int? =
        runCatching { wallpaperManager.getWallpaperColors(WallpaperManager.FLAG_SYSTEM)?.primaryColor?.toArgb() }.getOrNull()

    /** The bitmap's pixels at most [SAMPLE_SIDE] on its long side: plenty for the quantizer, and fast. */
    private fun Bitmap.samplePixels(): IntArray {
        val scale = SAMPLE_SIDE.toFloat() / max(width, height)
        val sample = if (scale >= 1f) this else Bitmap.createScaledBitmap(this, (width * scale).roundToInt().coerceAtLeast(1), (height * scale).roundToInt().coerceAtLeast(1), true)
        return IntArray(sample.width * sample.height).also { sample.getPixels(it, 0, sample.width, 0, 0, sample.width, sample.height) }
    }

    private companion object {
        const val SAMPLE_SIDE = 128
    }
}

private const val MAX_QUANTIZED_COLORS = 128

/** The seed Android's own Material You would pick from these pixels: quantized, then scored for how well each colour suits a theme. */
internal fun seedFromPixels(pixels: IntArray): Int = Score.score(QuantizerCelebi.quantize(pixels, MAX_QUANTIZED_COLORS)).first()

/** The Tonal Spot scheme (Android's default Material You style) for [seed], light and dark. */
internal fun wallpaperPaletteFrom(seed: Int): WallpaperPalette {
    fun roles(isDark: Boolean) = SchemeTonalSpot(Hct.fromInt(seed), isDark, 0.0).let {
        WallpaperPalette.Roles(primary = it.primary, secondary = it.secondary, tertiary = it.tertiary)
    }
    return WallpaperPalette(light = roles(isDark = false), dark = roles(isDark = true))
}
