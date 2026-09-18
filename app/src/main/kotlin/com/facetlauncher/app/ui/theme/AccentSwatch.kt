package com.facetlauncher.app.ui.theme

import androidx.annotation.StringRes
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import com.facetlauncher.app.R

/**
 * The curated "Basic colors" accent palette (Settings → Theme → Accent color → Basic colors) —
 * no fixed swatch list exists in the design handoff, so this is a standard Material-style hue
 * set. Each swatch carries its own light/dark pair, stored and restored independently (picking
 * "Teal" and switching the app between light/dark shows Teal's own light or dark value, not a
 * single fixed color) — mirrors every other design token in this app already being a light/dark
 * pair, not a single color. [BLUE] is exactly this app's original default accent stand-in.
 */
enum class AccentSwatch(@param:StringRes val labelRes: Int, val light: Color, val dark: Color) {
    BLUE(R.string.accent_swatch_blue, Color(0xFF2563EB), Color(0xFFA8C7FA)),
    INDIGO(R.string.accent_swatch_indigo, Color(0xFF4F46E5), Color(0xFFB4B8FF)),
    PURPLE(R.string.accent_swatch_purple, Color(0xFF7C3AED), Color(0xFFD1B3FF)),
    PINK(R.string.accent_swatch_pink, Color(0xFFDB2777), Color(0xFFF8A5C2)),
    RED(R.string.accent_swatch_red, Color(0xFFDC2626), Color(0xFFF2857F)),
    ORANGE(R.string.accent_swatch_orange, Color(0xFFEA580C), Color(0xFFFFB68A)),
    AMBER(R.string.accent_swatch_amber, Color(0xFFAE5E04), Color(0xFFFFCC80)),
    GREEN(R.string.accent_swatch_green, Color(0xFF16A34A), Color(0xFF7FD493)),
    TEAL(R.string.accent_swatch_teal, Color(0xFF0D9488), Color(0xFF80CBC4)),
    CYAN(R.string.accent_swatch_cyan, Color(0xFF0891B2), Color(0xFF80DEEA)),
}

/** This swatch's [light] or [dark] value for the current theme — see [LocalIsDarkTheme]. */
@Composable
fun AccentSwatch.resolvedColor(): Color = if (LocalIsDarkTheme.current) dark else light
