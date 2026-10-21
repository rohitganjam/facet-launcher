package com.facetlauncher.app.data.model

import androidx.compose.runtime.Immutable

/**
 * Landscape's own value for each prop that differs between orientations; `null` means "use the portrait
 * value". One group, so storage, resolution and writing handle every twin the same way.
 *
 * Launcher-wide settings hold one of these, and so does each facet (stored as flat columns, see
 * `Facet.landscape`). The widget size is facet-only: a hosted clock widget belongs to one facet, so
 * the launcher-wide group never sets it.
 */
@Immutable
data class LandscapeOverrides(
    val clockAlignment: ClockAlignment? = null,
    /** The divider position: in landscape, the width of the clock pane. */
    val clockZoneHeightDp: Float? = null,
    val clockScale: Float? = null,
    val clockWidgetWidthDp: Int? = null,
    val clockWidgetHeightDp: Int? = null,
    val appRowPosition: AppRowPosition? = null,
    val appListVerticalAlignment: AppListVerticalAlignment? = null,
    val appListColumnAlignment: AppListColumnAlignment? = null,
)
