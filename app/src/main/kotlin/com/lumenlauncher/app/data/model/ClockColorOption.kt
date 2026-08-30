package com.lumenlauncher.app.data.model

/**
 * A clock/calendar text color choice, shared by both, independently pickable. Lives in
 * `data/model` so it can be a real typed field on [LauncherSettings] — the actual
 * [androidx.compose.ui.graphics.Color] each option resolves to (`ui/theme/ClockColors.kt`'s
 * `ClockColorOption.resolve()`) stays in the UI layer, since [INK] is theme(light/dark)-aware.
 *
 * [WALLPAPER_PRIMARY]/[WALLPAPER_SECONDARY] are the wallpaper's own two Material You tones —
 * light-scheme primary and dark-scheme primary — always the actual wallpaper-derived colors,
 * never the curated `AccentSwatch` a user may have picked under "Basic colors" for the rest of
 * the app's chrome (see `ui/theme/Color.kt`'s `wallpaperPrimaryAndSecondary()`) — offered as two
 * explicit, independently-pickable choices rather than one that silently follows the app's
 * current theme mode, so a user can pin their clock text to a specific wallpaper tone regardless
 * of light/dark theme (see chat history). Whichever one is picked, `homeTextShadow()`'s existing
 * luminance-based flip automatically shadows it with the *other* tone, since that already
 * contrasts correctly by construction.
 */
enum class ClockColorOption(val displayName: String) {
    INK("Ink (default)"),
    WHITE("White"),
    BLACK("Black"),
    WALLPAPER_PRIMARY("Wallpaper primary"),
    WALLPAPER_SECONDARY("Wallpaper secondary"),
}
