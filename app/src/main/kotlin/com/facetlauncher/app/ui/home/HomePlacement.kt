package com.facetlauncher.app.ui.home

import androidx.compose.runtime.Immutable
import com.facetlauncher.app.data.model.AppListColumnAlignment
import com.facetlauncher.app.data.model.AppListVerticalAlignment
import com.facetlauncher.app.data.model.AppRowPosition
import com.facetlauncher.app.data.model.ClockAlignment

/** The clock's placement in one orientation, already resolved from facet and launcher settings (see [HomeUiState.clockPlacement]). */
@Immutable
data class ClockPlacement(
    val alignment: ClockAlignment,
    /** The divider position; in landscape, the clock pane's width. `null` until it has been dragged. */
    val zoneHeightDp: Float?,
    val scale: Float,
)

/** How the apps list is aligned in one orientation, already resolved (see [HomeUiState.listAlignment]). */
@Immutable
data class ListAlignment(
    val rowPosition: AppRowPosition,
    val verticalAlignment: AppListVerticalAlignment,
    val columnAlignment: AppListColumnAlignment,
)
