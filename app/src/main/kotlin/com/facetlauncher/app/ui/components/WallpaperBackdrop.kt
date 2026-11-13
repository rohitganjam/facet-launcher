package com.facetlauncher.app.ui.components

import android.content.res.Configuration
import android.graphics.Bitmap
import android.os.Build
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
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
 * shows, so a copy from a screenshot never stands in for it. Only in portrait on API 31 and up: the copy is the size of a portrait
 * screen, and below 31 the blur does not exist. Without a backdrop it is just a [Box].
 */
@Composable
fun WallpaperBackdropHost(copy: Bitmap?, modifier: Modifier = Modifier, content: @Composable BoxScope.() -> Unit) {
    val usable = copy != null && Build.VERSION.SDK_INT >= MIN_BACKDROP_SDK && LocalConfiguration.current.orientation == Configuration.ORIENTATION_PORTRAIT
    val image = remember(copy) { copy?.asImageBitmap() }
    // The copy goes into the recorded layer only; the empty box under it draws nothing on screen.
    val onDraw: ContentDrawScope.() -> Unit = remember(image) { { image?.let { drawImageCropped(it) } } }
    val backdrop = rememberLayerBackdrop(onDraw = onDraw)
    Box(modifier = modifier.fillMaxSize()) {
        if (usable && image != null) {
            Box(Modifier.fillMaxSize().layerBackdrop(backdrop))
        }
        CompositionLocalProvider(LocalWallpaperBackdrop provides backdrop.takeIf { usable }) { content() }
    }
}

/** [image] scaled to cover this scope and centred, as [androidx.compose.ui.layout.ContentScale.Crop] would draw it. */
private fun DrawScope.drawImageCropped(image: ImageBitmap) {
    if (image.width <= 0 || image.height <= 0 || size.width <= 0f || size.height <= 0f) return
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
