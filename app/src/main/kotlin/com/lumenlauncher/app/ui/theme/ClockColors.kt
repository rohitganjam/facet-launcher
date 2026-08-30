package com.lumenlauncher.app.ui.theme

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import com.lumenlauncher.app.data.model.ClockColorOption

/**
 * Resolves a [ClockColorOption] to a real [Color] — [ClockColorOption.INK] is theme-aware
 * ([Ink]); [ClockColorOption.WHITE]/[ClockColorOption.BLACK] are fixed; `WALLPAPER_PRIMARY`/
 * `WALLPAPER_SECONDARY` are [wallpaperPrimaryAndSecondary]'s two wallpaper-derived tones, picked
 * explicitly rather than following the app's current theme mode (see that function's own doc,
 * and chat history).
 */
@Composable
fun ClockColorOption.resolve(): Color = when (this) {
    ClockColorOption.INK -> Ink
    ClockColorOption.WHITE -> Color.White
    ClockColorOption.BLACK -> Color.Black
    ClockColorOption.WALLPAPER_PRIMARY -> wallpaperPrimaryAndSecondary().first
    ClockColorOption.WALLPAPER_SECONDARY -> wallpaperPrimaryAndSecondary().second
}
