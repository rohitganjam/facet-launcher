package com.facetlauncher.app.data.model

import androidx.annotation.StringRes
import com.facetlauncher.app.R

/**
 * F11 — global (not per-app) app-icon rendering mode. [SYSTEM_DEFAULT] draws each app's own icon.
 * The two monochrome modes recolor it with a `BlendMode.Color` tint (see `ui/components/AppIcon.kt`
 * for why that and not the app's own monochrome layer): [MONOCHROME_BLACK_WHITE] in the theme's ink
 * tone, [MONOCHROME_ACCENT] in the accent. [GLYPH_ACCENT] and [OUTLINE_ACCENT] draw only the icon's
 * shape, as a filled silhouette or as line art, in the accent, from the app's themed monochrome layer
 * where it has one and from the plain icon otherwise (see `IconStyler`); an icon neither can handle
 * is drawn whole, in tones of the accent.
 */
enum class IconRenderMode(@param:StringRes val displayNameRes: Int) {
    SYSTEM_DEFAULT(R.string.icon_render_system_default),
    MONOCHROME_BLACK_WHITE(R.string.icon_render_monochrome_black_white),
    MONOCHROME_ACCENT(R.string.icon_render_monochrome_accent),
    GLYPH_ACCENT(R.string.icon_render_glyph_accent),
    OUTLINE_ACCENT(R.string.icon_render_outline_accent),
}
