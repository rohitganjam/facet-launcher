package com.facetlauncher.app.ui.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.systemGestureExclusion
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.movableContentOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
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
 * With [dockAtBottom] (roomy windows) the dock is a full-width row along the bottom instead of a column.
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
    dockAtBottom: Boolean = false,
    onAppsPaneSizeChange: (IntSize) -> Unit = {},
    onDockRowHeightChange: (Int) -> Unit = {},
    dock: @Composable () -> Unit,
) {
    // One movable slot for the dock, so it keeps its state whichever branch below places it.
    val currentDock by rememberUpdatedState(dock)
    val dockContent = remember { movableContentOf { currentDock() } }
    val panes: @Composable RowScope.() -> Unit = {
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
    }
    if (dockAtBottom) {
        // Roomy windows: the dock runs along the bottom as in portrait, using the dock style the user chose.
        Column(modifier = modifier.fillMaxSize().testTag("home_landscape_layout")) {
            Row(modifier = Modifier.weight(1f).fillMaxWidth(), content = panes)
            if (hasDock) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        // Measured with its padding, so what the divider and pill keep clear is the whole row.
                        .onSizeChanged { onDockRowHeightChange(it.height) }
                        .padding(top = 12.dp, bottom = 4.dp)
                        .testTag("home_dock_row"),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                ) { dockContent() }
            }
        }
        return
    }
    Row(modifier = modifier.fillMaxSize().testTag("home_landscape_layout")) {
        panes()
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
                ) { dockContent() }
            }
        }
    }
}

/** The pane width one phone-sized grid is laid out for; a roomy pane gets proportionally more columns. */
private val HOME_LANDSCAPE_ROOMY_REFERENCE_PANE_WIDTH = 360.dp
private const val HOME_LANDSCAPE_ROOMY_MAX_GRID_COLUMNS = 8

/**
 * Home's grid column count in a pane of [paneWidth]. A compact window narrows [requested] so each cell
 * stays at least [HOME_LANDSCAPE_MIN_GRID_CELL_WIDTH] wide. A roomy one scales it up with the pane, so
 * tiles stay about the size they are on a phone and there are more of them.
 */
internal fun landscapeGridColumns(requested: Int, paneWidth: Dp, space: HomeLandscapeSpace = HomeLandscapeSpace.COMPACT): Int = when {
    paneWidth <= 0.dp -> requested
    space == HomeLandscapeSpace.ROOMY ->
        (requested * (paneWidth / HOME_LANDSCAPE_ROOMY_REFERENCE_PANE_WIDTH)).toInt()
            .coerceIn(requested, maxOf(requested, HOME_LANDSCAPE_ROOMY_MAX_GRID_COLUMNS))
    else -> minOf(requested, (paneWidth / HOME_LANDSCAPE_MIN_GRID_CELL_WIDTH).toInt().coerceAtLeast(HOME_LANDSCAPE_MIN_GRID_COLUMNS))
}
