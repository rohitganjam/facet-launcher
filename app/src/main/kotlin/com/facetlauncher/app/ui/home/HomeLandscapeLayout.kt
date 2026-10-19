package com.facetlauncher.app.ui.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.systemGestureExclusion
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp

/** Share of the width the clock pane takes until the divider handle saves a width. */
internal const val HOME_LANDSCAPE_CLOCK_PANE_FRACTION = 0.4f

/** The clock pane's left margin, inside its width. */
internal val HOME_LANDSCAPE_CLOCK_PANE_START_PADDING = 24.dp

/** Vertical padding around the panes, below the system bars. */
internal val HOME_LANDSCAPE_VERTICAL_PADDING_TOP = 8.dp
internal val HOME_LANDSCAPE_VERTICAL_PADDING_BOTTOM = 16.dp

/** Room kept between the clock and the pane's right edge, inside its width. */
internal val HOME_LANDSCAPE_CLOCK_PANE_END_MARGIN = 16.dp

/** Width of the gap between the clock and apps panes — where the divider handle sits. */
internal val HOME_LANDSCAPE_PANE_GAP = 24.dp

/** Home grids need at least this many columns, and each cell at least this wide, to stay readable. */
private const val HOME_LANDSCAPE_MIN_GRID_COLUMNS = 3
private val HOME_LANDSCAPE_MIN_GRID_CELL_WIDTH = 64.dp

/**
 * Landscape Home: `[ clock pane | apps pane | dock column ]`. Pure layout — the clock, app list and
 * dock arrive as slots, so this knows nothing about their state. The caller applies the system
 * bar insets.
 *
 * [clockPaneAlignment] places the clock pane's content (top, middle or bottom) and also fixes its
 * left edge. [clockPaneWidth] includes the pane's own left margin. The dock column is icons only, scrolls if it overflows, and sits inset from the right
 * edge and excluded from the system back gesture, so a swipe that starts on it is not taken as Back.
 */
@Composable
internal fun HomeLandscapeLayout(
    hasDock: Boolean,
    clockPaneAlignment: Alignment,
    clockPaneWidth: Dp,
    clock: @Composable BoxScope.() -> Unit,
    apps: @Composable BoxScope.() -> Unit,
    modifier: Modifier = Modifier,
    onAppsPaneSizeChange: (IntSize) -> Unit = {},
    dock: @Composable ColumnScope.() -> Unit,
) {
    Row(modifier = modifier.fillMaxSize().testTag("home_landscape_layout")) {
        Box(
            modifier = Modifier
                .width(clockPaneWidth)
                .fillMaxHeight()
                .testTag("home_clock_pane")
                .padding(start = HOME_LANDSCAPE_CLOCK_PANE_START_PADDING),
            contentAlignment = clockPaneAlignment,
            content = clock,
        )
        Spacer(modifier = Modifier.width(HOME_LANDSCAPE_PANE_GAP))
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight()
                .onSizeChanged(onAppsPaneSizeChange)
                .testTag("home_apps_pane"),
            content = apps,
        )
        if (hasDock) {
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .padding(start = 12.dp, end = 16.dp)
                    .systemGestureExclusion()
                    .testTag("home_dock_column"),
                contentAlignment = Alignment.Center,
            ) {
                Column(
                    modifier = Modifier.verticalScroll(rememberScrollState()),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    content = dock,
                )
            }
        }
    }
}

/** Home's grid column count, narrowed so each cell stays at least [HOME_LANDSCAPE_MIN_GRID_CELL_WIDTH] wide in a pane of [paneWidth]. */
internal fun landscapeGridColumns(requested: Int, paneWidth: Dp): Int {
    if (paneWidth <= 0.dp) return requested
    val fitting = (paneWidth / HOME_LANDSCAPE_MIN_GRID_CELL_WIDTH).toInt().coerceAtLeast(HOME_LANDSCAPE_MIN_GRID_COLUMNS)
    return minOf(requested, fitting)
}
