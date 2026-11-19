package com.facetlauncher.app.ui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Color
import com.facetlauncher.app.ui.theme.LocalIsDarkTheme
import com.kyant.backdrop.backdrops.layerBackdrop
import com.kyant.backdrop.backdrops.rememberCombinedBackdrop
import com.kyant.backdrop.backdrops.rememberLayerBackdrop

/**
 * The full-screen backing of the app drawer, widget hub and facet carousel. With a wallpaper copy it is that copy already frosted
 * ([WallpaperFrost], made once per copy and theme), light in a light theme and dark in a dark one so the theme's own text and icon colours contrast
 * with it, drawn as a plain image: a live blur of a full screen on every frame is what made these panels slow. Icons and widgets on it that carry
 * glass blur the copy and this frost together (an icon only blurs if its glass style is on). Otherwise, with no copy or while the frost is being
 * made (a new wallpaper makes a new one), it is the theme tint [fallback], which keeps Home showing through. Nothing to switch on.
 */
@Composable
fun FrostSurface(fallback: Color, modifier: Modifier = Modifier, content: @Composable BoxScope.() -> Unit) {
    val parent = LocalWallpaperBackdrop.current
    val dark = LocalIsDarkTheme.current
    val picture = LocalWallpaperFrost.current?.let { if (dark) it.dark else it.light }
    val frosted = parent != null && picture != null
    val wash = remember(dark) { frostSpec(dark).wash }
    val layer = rememberLayerBackdrop()
    val nested = if (parent != null) rememberCombinedBackdrop(parent, layer) else null
    val tint = LocalBackdropTint.current
    val samplesWallpaper = LocalIconGlassSamplesWallpaper.current
    Box(modifier = modifier) {
        Box(
            modifier = Modifier
                .matchParentSize()
                .then(if (frosted) Modifier.layerBackdrop(layer) else Modifier)
                .drawBehind {
                    if (picture != null && frosted) {
                        drawImageCropped(picture)
                        drawRect(wash)
                    } else {
                        drawRect(fallback)
                    }
                },
        )
        // The frost is the dim now: nothing on it adds the hub's old tint (60% of the theme surface) on top of it.
        CompositionLocalProvider(
            LocalWallpaperBackdrop provides if (frosted) nested else parent,
            LocalBackdropTint provides if (frosted) null else tint,
            LocalIconGlassSamplesWallpaper provides (frosted || samplesWallpaper),
        ) { content() }
    }
}
