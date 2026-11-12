package com.facetlauncher.app.data.model

import androidx.annotation.StringRes
import com.facetlauncher.app.R

/** The frosted-glass panel behind a Home component; [OFF] draws none. Each component (dock, apps list items, calendar, media row) has its own. */
enum class GlassStyle(@param:StringRes val displayNameRes: Int) {
    OFF(R.string.glass_style_off),

    /** A soft white wash, brighter at the top, with a hairline edge. */
    FROST(R.string.glass_style_frost),

    /** A blurred dark tint with a faint rim; the best contrast for text on any wallpaper. */
    DARK_FROST(R.string.glass_style_dark_frost),

    /** An almost clear fill with a bright diagonal edge. */
    CLEAR_GLASS(R.string.glass_style_clear_glass),

    /** Clear glass under a dark wash: the wallpaper stays sharp, darker. */
    DARK_GLASS(R.string.glass_style_dark_glass),

    /** A strong white fill with an inner highlight along the top. */
    BRIGHT(R.string.glass_style_bright),

    /** The launcher accent colour as the wash. */
    ACCENT(R.string.glass_style_accent),

    /** Not glass: the theme's own surface at 60% opacity. Offered by widgets only. */
    THEME(R.string.glass_style_theme),

    /** Not glass: the theme's text colour as the fill at 60% opacity, so light in a dark theme and dark in a light one. Offered by widgets only. */
    THEME_INVERTED(R.string.glass_style_theme_inverted),
    ;

    /** `true` for the two solid, non-glass fills, which only widgets offer. */
    val isSolid: Boolean get() = this == THEME || this == THEME_INVERTED

    companion object {
        /** What every glass picker offers: all but the solid fills. */
        val glassOnly: List<GlassStyle> = entries.filterNot { it.isSolid }
    }
}
