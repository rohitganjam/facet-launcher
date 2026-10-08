package com.facetlauncher.app.data.model

import androidx.annotation.StringRes
import com.facetlauncher.app.R

/**
 * Global app-icon outline, independent of [IconRenderMode]'s color treatment. [SQUIRCLE] is the
 * default (superellipse n=4); [ROUNDED] is a rounder superellipse (n=2.6, about One UI's icon mask).
 */
enum class IconShape(@param:StringRes val displayNameRes: Int) {
    SQUIRCLE(R.string.icon_shape_squircle),
    ROUNDED(R.string.icon_shape_rounded),
    CIRCLE(R.string.icon_shape_circle),
    SQUARE(R.string.icon_shape_square),
}
