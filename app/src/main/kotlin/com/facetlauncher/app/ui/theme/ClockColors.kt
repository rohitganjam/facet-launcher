package com.facetlauncher.app.ui.theme

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import com.facetlauncher.app.data.model.ClockColorOption

/**
 * Resolves a [ClockColorOption] to a real [Color] — [ClockColorOption.THEME]/`THEME_INVERTED` are
 * theme-aware ([Ink]/[InkInverted]); `ACCENT_PRIMARY`/`ACCENT_SECONDARY` are
 * [accentTonalExtremes]' two tonal extremes, picked explicitly rather than following the app's
 * current theme mode (see that function's own doc, and chat history). This means they follow
 * Settings → Theme → Accent color's own source: the "Basic colors" swatch when one is picked, or
 * the wallpaper's own Material You tones when "Wallpaper colors" is selected — never a fixed
 * color of their own.
 */
@Composable
fun ClockColorOption.resolve(): Color = when (this) {
    ClockColorOption.THEME -> Ink
    ClockColorOption.THEME_INVERTED -> InkInverted
    ClockColorOption.ACCENT_PRIMARY -> accentTonalExtremes().first
    ClockColorOption.ACCENT_SECONDARY -> accentTonalExtremes().second
}
