package com.facetlauncher.app.data.model

/**
 * A text color choice shared by the clock, calendar, and app-list/dock labels
 * ([LauncherSettings.appLabelColorOption]), independently pickable per surface. Lives in
 * `data/model` so it can be a real typed field on [LauncherSettings] — the actual
 * [androidx.compose.ui.graphics.Color] each option resolves to (`ui/theme/ClockColors.kt`'s
 * `ClockColorOption.resolve()`) stays in the UI layer, since [THEME]/[THEME_INVERTED] are
 * theme(light/dark)-aware.
 *
 * [THEME] (the default) is exactly today's [Ink] — dark text in light theme, light text in dark
 * theme. [THEME_INVERTED] is its deliberate opposite — light text in light theme, dark text in
 * dark theme — replacing the old fixed `WHITE`/`BLACK` picks: in a strictly two-mode (light/dark)
 * theme system, a *fixed* White or Black pick only ever reads correctly in one of the two modes,
 * so this always flips to whichever of the two actually contrasts once the app's theme mode
 * changes, rather than needing to be re-picked (see chat history).
 *
 * [ACCENT_PRIMARY]/[ACCENT_SECONDARY] track Settings → Theme → Accent color's own two tonal
 * extremes (`ui/theme/Color.kt`'s `accentTonalExtremes()`) — the same "Basic colors" swatch pick,
 * or the wallpaper's own Material You tones when "Wallpaper colors" is selected there, rather than
 * a fixed color of their own. Offered as two explicit, independently-pickable choices instead of
 * one that silently follows the app's current theme mode, so a user can pin their clock text to a
 * specific accent tone regardless of light/dark theme (see chat history). Whichever option is
 * picked overall, `homeTextShadow()`'s existing luminance-based flip automatically shadows it with
 * the tone that contrasts correctly, since that's resolved from the text color's own luminance.
 */
enum class ClockColorOption(val displayName: String) {
    THEME("Theme"),
    THEME_INVERTED("Theme Inverted"),
    ACCENT_PRIMARY("Accent primary"),
    ACCENT_SECONDARY("Accent secondary"),
}
