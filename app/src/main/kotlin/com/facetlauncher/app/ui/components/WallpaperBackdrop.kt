package com.facetlauncher.app.ui.components

import android.content.res.Configuration
import android.graphics.Bitmap
import android.os.Build
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.ContentDrawScope
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import com.kyant.backdrop.Backdrop
import com.kyant.backdrop.backdrops.layerBackdrop
import com.kyant.backdrop.backdrops.rememberLayerBackdrop
import kotlin.math.max
import kotlin.math.roundToInt

/** The wallpaper copy glass panels sample, or `null` when there is none (they then draw their plain tint). */
val LocalWallpaperBackdrop = staticCompositionLocalOf<Backdrop?> { null }

/**
 * A flat colour a screen draws over the wallpaper (the Hub's dim), which glass panels on it add to what they blur so the blur matches what the
 * screen shows. `null` where nothing is drawn over the wallpaper.
 */
val LocalBackdropTint = staticCompositionLocalOf<Color?> { null }

/** `true` inside the live Home screen, where an icon's glass background samples the wallpaper copy; elsewhere (settings, previews, pickers) it stays a tint. */
val LocalIconGlassSamplesWallpaper = staticCompositionLocalOf { false }

/** Blur needs RenderEffect, which starts at API 31; below that the copy would show unblurred inside a panel, so there is no backdrop. */
private const val MIN_BACKDROP_SDK = Build.VERSION_CODES.S

/**
 * Records the wallpaper [copy] as the backdrop glass panels in [content] sample, without drawing it: the system wallpaper stays what
 * shows, so a copy from a screenshot never stands in for it. Only when the window is turned the way the copy is (a phone's copy is
 * portrait, so not rotated; a foldable's inner screen has a landscape one) on API 31 and up, since below 31 the blur does not exist.
 * Without a backdrop it is just a [Box].
 */
@Composable
fun WallpaperBackdropHost(copy: Bitmap?, modifier: Modifier = Modifier, content: @Composable BoxScope.() -> Unit) {
    val windowLandscape = LocalConfiguration.current.orientation == Configuration.ORIENTATION_LANDSCAPE
    val usable = copy != null && Build.VERSION.SDK_INT >= MIN_BACKDROP_SDK && (copy.width > copy.height) == windowLandscape
    val image = remember(copy) { copy?.asImageBitmap() }
    // The copy goes into the recorded layer only; the empty box under it draws nothing on screen.
    val onDraw: ContentDrawScope.() -> Unit = remember(image) { { image?.let { drawImageCropped(it) } } }
    val backdrop = rememberLayerBackdrop(onDraw = onDraw)
    val frost = rememberWallpaperFrost(copy.takeIf { usable })
    Box(modifier = modifier.fillMaxSize()) {
        if (usable && image != null) {
            Box(Modifier.fillMaxSize().layerBackdrop(backdrop))
        }
        CompositionLocalProvider(LocalWallpaperBackdrop provides backdrop.takeIf { usable }, LocalWallpaperFrost provides frost) { content() }
    }
}

/**
 * The frosted pictures of [copy], made off the main thread: nothing while there is no copy, and a new pair whenever the copy changes (a
 * different wallpaper, or a new capture). Until they are ready both pictures are `null`, which the panels read as "use the tint".
 */
@Composable
private fun rememberWallpaperFrost(copy: Bitmap?): WallpaperFrost? {
    val density = LocalDensity.current.density
    return produceState<WallpaperFrost?>(initialValue = null, copy, density) {
        value = copy?.let { WallpaperFrost(light = null, dark = null) }
        value = copy?.let {
            withContext(Dispatchers.Default) {
                fun frosted(dark: Boolean) = frostSpec(dark).let { spec -> frostedCopy(it, spec.blurDp * density, spec.brightness, spec.saturation).asImageBitmap() }
                WallpaperFrost(light = frosted(false), dark = frosted(true))
            }
        }
    }.value
}

/** [image] scaled to cover this scope and centred, as [androidx.compose.ui.layout.ContentScale.Crop] would draw it. */
internal fun DrawScope.drawImageCropped(image: ImageBitmap) {
    if (image.width <= 0 || image.height <= 0 || size.isEmpty()) return
    val scale = max(size.width / image.width, size.height / image.height)
    val srcWidth = (size.width / scale).roundToInt().coerceIn(1, image.width)
    val srcHeight = (size.height / scale).roundToInt().coerceIn(1, image.height)
    drawImage(
        image = image,
        srcOffset = IntOffset((image.width - srcWidth) / 2, (image.height - srcHeight) / 2),
        srcSize = IntSize(srcWidth, srcHeight),
        dstSize = IntSize(size.width.roundToInt(), size.height.roundToInt()),
    )
}
