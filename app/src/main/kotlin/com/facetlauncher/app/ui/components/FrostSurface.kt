package com.facetlauncher.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import com.facetlauncher.app.data.model.GlassStyle
import com.facetlauncher.app.ui.theme.LocalIsDarkTheme

/**
 * The full-screen backing of the app drawer, widget hub and facet carousel. While the wallpaper copy is being drawn ([LocalWallpaperBackdrop]) it is
 * frosted glass over that copy, light in a light theme and dark in a dark one, so the theme's own text and icon colours contrast with it; icons and
 * widgets and icons on it that carry glass blur the copy and this frost together (an icon only blurs if its glass style is on). Otherwise it is the theme tint [fallback], as before, which keeps Home showing
 * through. Nothing to switch on: it follows whether a copy exists.
 */
@Composable
fun FrostSurface(fallback: Color, modifier: Modifier = Modifier, content: @Composable BoxScope.() -> Unit) {
    if (LocalWallpaperBackdrop.current == null) {
        Box(modifier = modifier.background(fallback), content = content)
    } else {
        // The frost is the dim now: neither the glass layer itself nor what sits on it may add the hub's old tint (60% of the theme surface) on top.
        CompositionLocalProvider(LocalBackdropTint provides null) {
            GlassPanel(style = if (LocalIsDarkTheme.current) GlassStyle.DARK_FROST else GlassStyle.FROST, shape = RectangleShape, modifier = modifier) {
                CompositionLocalProvider(LocalIconGlassSamplesWallpaper provides true) { content() }
            }
        }
    }
}
