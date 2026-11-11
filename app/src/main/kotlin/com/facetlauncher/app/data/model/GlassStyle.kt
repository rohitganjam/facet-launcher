package com.facetlauncher.app.data.model

import androidx.annotation.StringRes
import com.facetlauncher.app.R

/** The frosted-glass panel behind a Home component; [OFF] draws none. Each component (dock, apps list items, calendar, media row) has its own. */
enum class GlassStyle(@param:StringRes val displayNameRes: Int) {
    OFF(R.string.glass_style_off),

    /** A soft white wash, brighter at the top, with a hairline edge. */
    FROST(R.string.glass_style_frost),

    /** A dark tint with a faint rim; the best contrast for text on any wallpaper. */
    SMOKED(R.string.glass_style_smoked),

    /** A strong white fill with an inner highlight along the top. */
    BRIGHT(R.string.glass_style_bright),

    /** An almost clear fill with a bright diagonal edge. */
    CLEAR_GLASS(R.string.glass_style_clear_glass),

    /** The launcher accent colour as the wash. */
    ACCENT(R.string.glass_style_accent),
}
