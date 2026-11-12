package com.facetlauncher.app.ui.components

import android.content.res.Configuration
import android.graphics.Bitmap
import android.os.Build
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import com.kyant.backdrop.backdrops.LayerBackdrop
import com.kyant.backdrop.backdrops.layerBackdrop
import com.kyant.backdrop.backdrops.rememberLayerBackdrop

/** The wallpaper copy glass panels sample, or `null` when there is none (they then draw their plain tint). */
val LocalWallpaperBackdrop = staticCompositionLocalOf<LayerBackdrop?> { null }

/** Blur needs RenderEffect, which starts at API 31; below that the copy would show unblurred inside a panel, so there is no backdrop. */
private const val MIN_BACKDROP_SDK = Build.VERSION_CODES.S

/**
 * Draws the wallpaper [copy] as the bottom layer and marks it as the backdrop for [content], so glass panels can sample it. It is
 * only drawn in portrait on API 31 and up: the copy is the size of a portrait screen, and below 31 the blur does not exist. Without a
 * backdrop it is just a [Box], so the system wallpaper shows through as before.
 */
@Composable
fun WallpaperBackdropHost(copy: Bitmap?, modifier: Modifier = Modifier, content: @Composable BoxScope.() -> Unit) {
    val usable = copy != null && Build.VERSION.SDK_INT >= MIN_BACKDROP_SDK && LocalConfiguration.current.orientation == Configuration.ORIENTATION_PORTRAIT
    val backdrop = rememberLayerBackdrop()
    val image = remember(copy) { copy?.asImageBitmap() }
    Box(modifier = modifier.fillMaxSize()) {
        if (usable && image != null) {
            Image(bitmap = image, contentDescription = null, contentScale = ContentScale.Crop, modifier = Modifier.fillMaxSize().layerBackdrop(backdrop))
        }
        CompositionLocalProvider(LocalWallpaperBackdrop provides backdrop.takeIf { usable }) { content() }
    }
}
