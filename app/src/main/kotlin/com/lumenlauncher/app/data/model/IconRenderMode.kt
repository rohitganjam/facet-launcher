package com.lumenlauncher.app.data.model

/**
 * F11 — global (not per-app) app-icon rendering mode. [SYSTEM_DEFAULT] renders each app's own
 * icon unchanged. The two monochrome modes recolor every app's regular icon (a `BlendMode.Color`
 * tint, keeping its own shading so icons stay distinguishable from each other — see
 * `ui/components/AppIcon.kt`), differing only in which color: [MONOCHROME_BLACK_WHITE] uses the
 * current theme's ink tone (black in light theme, white in dark), [MONOCHROME_ACCENT] uses the
 * app's [LauncherSettings]-driven accent color. Deliberately does *not* prefer an app's own
 * `Icon.getMonochrome()` layer even when it provides one — that asset is a flat single-color
 * glyph with no internal shading, so using it looked flat and out of place next to every other
 * (detailed, recolored) icon; tried and reverted (see chat history).
 */
enum class IconRenderMode(val displayName: String) {
    SYSTEM_DEFAULT("System default"),
    MONOCHROME_BLACK_WHITE("Monochrome (Black & white)"),
    MONOCHROME_ACCENT("Monochrome (Accent)"),
}
