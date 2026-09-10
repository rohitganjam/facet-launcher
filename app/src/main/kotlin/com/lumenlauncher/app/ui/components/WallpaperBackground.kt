package com.lumenlauncher.app.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import com.lumenlauncher.app.data.model.HomeWallpaper
import com.lumenlauncher.app.ui.theme.Wallpaper

/**
 * Paints the current system wallpaper (from [com.lumenlauncher.app.data.WallpaperRepository])
 * behind a preview surface that can't get it from the window compositor the way real Home does.
 * The caller owns sizing and clip-to-shape; this only fills [modifier]. No scrim — legibility
 * rests on the same crisp text-shadow Home uses.
 *
 * [alignment] is the crop anchor for a bitmap wallpaper: [Alignment.Center] (default) for a
 * card locked to the screen's aspect ratio, [Alignment.BottomCenter] for a short card that
 * shows only a band — so the visible slice is the wallpaper's lower part, where the dock sits.
 */
@Composable
fun WallpaperBackground(
    wallpaper: HomeWallpaper,
    modifier: Modifier = Modifier,
    alignment: Alignment = Alignment.Center,
) {
    val tagged = modifier.testTag("wallpaper_background")
    when (wallpaper) {
        is HomeWallpaper.Image ->
            Image(
                bitmap = wallpaper.bitmap.asImageBitmap(),
                contentDescription = null,
                alignment = alignment,
                contentScale = ContentScale.Crop,
                modifier = tagged,
            )

        is HomeWallpaper.Tones -> {
            val stops = wallpaper.argb.map { Color(it) }.let { if (it.size >= 2) it else it + it }
            Box(tagged.background(Brush.verticalGradient(stops)))
        }

        HomeWallpaper.Unavailable ->
            Box(tagged.background(Wallpaper))
    }
}
