package com.facetlauncher.app.ui.pro

import androidx.annotation.StringRes
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.Sensors
import androidx.compose.material.icons.filled.Wallpaper
import androidx.compose.material.icons.filled.Widgets
import androidx.compose.ui.graphics.vector.ImageVector
import com.facetlauncher.app.R
import com.facetlauncher.app.data.model.ProReason

/**
 * What Pro unlocks, in screen order. Both Facet Pro screens draw their rows from this list. [ownedBody] is the line
 * under the row once bought; null means the live facet count.
 */
internal enum class ProFeature(
    val icon: ImageVector,
    @StringRes val title: Int,
    @StringRes val body: Int,
    @StringRes val ownedBody: Int?,
) {
    FACETS(Icons.Default.Layers, R.string.pro_feature_facets_title, R.string.pro_feature_facets_body, null),
    AUTOMATION(Icons.Default.Sensors, R.string.pro_feature_automation_title, R.string.pro_feature_automation_body, R.string.pro_owned_automation_body),
    WIDGET_STACKS(Icons.Default.Widgets, R.string.pro_feature_widget_stacks_title, R.string.pro_feature_widget_stacks_body, R.string.pro_owned_widget_stacks_body),
    DAILY_WALLPAPER(Icons.Default.Wallpaper, R.string.pro_feature_daily_wallpaper_title, R.string.pro_feature_daily_wallpaper_body, R.string.pro_owned_daily_wallpaper_body),
}

/** The row a Pro limit belongs to; null when the screen was opened from Settings. A new [ProReason] fails to compile here until it has a row. */
internal val ProReason.feature: ProFeature?
    get() = when (this) {
        ProReason.FACET_LIMIT, ProReason.FACET_LOCKED -> ProFeature.FACETS
        ProReason.TRIGGER, ProReason.RULE_LIMIT -> ProFeature.AUTOMATION
        ProReason.WIDGET_STACK -> ProFeature.WIDGET_STACKS
        ProReason.DAILY_WALLPAPER -> ProFeature.DAILY_WALLPAPER
        ProReason.ABOUT -> null
    }
