package com.facetlauncher.app.domain

import androidx.compose.runtime.Immutable
import com.facetlauncher.app.data.model.AppListColumnAlignment
import com.facetlauncher.app.data.model.AppListVerticalAlignment
import com.facetlauncher.app.data.model.AppRowPosition
import com.facetlauncher.app.data.model.ClockAlignment

/** The clock's placement in one orientation, already resolved from facet and launcher settings (see [ResolvedFacetStyle.clockPlacement]). */
@Immutable
data class ClockPlacement(
    val alignment: ClockAlignment,
    /** The divider position; in landscape, the clock pane's width. `null` until it has been dragged. */
    val zoneHeightDp: Float?,
    /** The scale a clock without a saved box draws at ([com.facetlauncher.app.data.model.DEFAULT_CLOCK_SCALE]); the box on its page sets the size otherwise. */
    val scale: Float,
)

/** How the apps list is aligned in one orientation, already resolved (see [ResolvedFacetStyle.listAlignment]). */
@Immutable
data class ListAlignment(
    val rowPosition: AppRowPosition,
    val verticalAlignment: AppListVerticalAlignment,
    val columnAlignment: AppListColumnAlignment,
)
