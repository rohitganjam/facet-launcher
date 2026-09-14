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
 * [ACCENT_PRIMARY]/[ACCENT_SECONDARY] both track Settings → Theme → Accent color's own two tonal
 * extremes (`ui/theme/Color.kt`'s `accentTonalExtremes()`) — the same "Basic colors" swatch pick,
 * or the wallpaper's own Material You tones when "Wallpaper colors" is selected there, rather than
 * a fixed color of their own. [ACCENT_PRIMARY] always matches the phone's current theme mode
 * (`ui/theme/ClockColors.kt`'s `resolve()` — effectively the same tone as [Accent] itself);
 * [ACCENT_SECONDARY] is deliberately the *other* tonal extreme, kept available as its own pickable
 * option for a user who prefers that specific tone regardless of theme (see chat history — an
 * earlier version pinned both options to a fixed light/dark tone rather than tracking the theme).
 * Whichever option is picked overall, `homeTextShadow()`'s existing luminance-based flip
 * automatically shadows it with the tone that contrasts correctly, since that's resolved from the
 * text color's own luminance.
 */
enum class ClockColorOption(val displayName: String) {
    THEME("Theme"),
    THEME_INVERTED("Theme Inverted"),
    ACCENT_PRIMARY("Accent primary"),
    ACCENT_SECONDARY("Accent secondary"),
}
