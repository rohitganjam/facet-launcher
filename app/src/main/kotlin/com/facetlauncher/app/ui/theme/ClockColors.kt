package com.facetlauncher.app.ui.theme

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import com.facetlauncher.app.data.model.ClockColorOption

/**
 * Resolves a [ClockColorOption] to a real [Color] — [ClockColorOption.THEME]/`THEME_INVERTED` are
 * theme-aware ([Ink]/[InkInverted]); `ACCENT_PRIMARY`/`ACCENT_SECONDARY` both draw from
 * [accentTonalExtremes]' two tonal extremes, but which extreme each one picks depends on the
 * current theme mode rather than being pinned to a fixed light/dark tone: `ACCENT_PRIMARY` always
 * matches the phone's own current theme (light theme → the light-scheme tone, dark theme → the
 * dark-scheme tone — exactly [Accent] itself), and `ACCENT_SECONDARY` is deliberately the *other*
 * one, so a user who prefers that tone specifically can still pick it regardless of theme (see
 * chat history — corrects an earlier fixed-tone design). Either way, this follows Settings →
 * Theme → Accent color's own source: the "Basic colors" swatch when one is picked, or the
 * wallpaper's own Material You tones when "Wallpaper colors" is selected — never a fixed color of
 * their own.
 */
@Composable
fun ClockColorOption.resolve(): Color = when (this) {
    ClockColorOption.THEME -> Ink
    ClockColorOption.THEME_INVERTED -> InkInverted
    ClockColorOption.ACCENT_PRIMARY -> accentTonalExtremes().let { (light, dark) -> if (LocalIsDarkTheme.current) dark else light }
    ClockColorOption.ACCENT_SECONDARY -> accentTonalExtremes().let { (light, dark) -> if (LocalIsDarkTheme.current) light else dark }
}
