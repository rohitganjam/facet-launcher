package com.facetlauncher.app.data.model

import androidx.annotation.StringRes
import com.facetlauncher.app.R

/** Which text colours a widget Facet draws itself uses, whatever the launcher's theme: [AUTO] follows it. */
enum class WidgetTextTheme(@param:StringRes val displayNameRes: Int) {
    AUTO(R.string.widget_text_auto),

    /** Light text, for a dark panel or wallpaper. */
    LIGHT_TEXT(R.string.widget_text_light),

    /** Dark text, for a light panel or wallpaper. */
    DARK_TEXT(R.string.widget_text_dark),
}
