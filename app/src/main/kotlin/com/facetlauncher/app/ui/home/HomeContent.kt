package com.facetlauncher.app.ui.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.layout.positionInRoot
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import com.facetlauncher.app.data.model.landscape
import com.facetlauncher.app.data.model.AppListVerticalAlignment
import com.facetlauncher.app.data.model.ClockAlignment
import com.facetlauncher.app.data.model.DockDisplayMode
import com.facetlauncher.app.ui.home.ClockAdjustMode
import kotlin.math.roundToInt

/** Portrait Home: the clock, media row and app list stacked over the dock row. */
@Composable
internal fun HomePortraitContent(frame: HomeFrame) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            // Edge-to-edge is enforced at this app's targetSdk, so without this the dock draws under the
            // gesture/nav bar. It is on this Column, not the root Box, so the Box stays truly full-screen
            // for the resize handles' root-coordinate math.
            .windowInsetsPadding(WindowInsets.systemBars)
            .padding(bottom = 22.dp),
    ) {
        // Everything but the dock keeps the screen's own 24dp side margin. The dock row sits outside it so
        // SpaceEvenly spaces its icons against the true screen edge.
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .onSizeChanged { frame.layout.contentHeightPx = it.height.toFloat() },
        ) {
            PortraitClockZone(frame)
        }
        Spacer(modifier = Modifier.height(16.dp))
        if (frame.state.dock.items.isNotEmpty()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 12.dp, bottom = 4.dp)
                    .testTag("home_dock_row"),
                horizontalArrangement = Arrangement.SpaceEvenly,
            ) {
                HomeDockItems(frame, frame.state.dock.displayMode)
            }
        }
    }
}

@Composable
private fun BoxScope.PortraitClockZone(frame: HomeFrame) {
    val layout = frame.layout
    val media = frame.media
    val handlePx = frame.geometry.handle.handlePx
    HomeClockSlot(
        frame = frame,
        modifier = Modifier
            .offset { IntOffset(0, (handlePx - frame.metrics.minGapPx - layout.clockNaturalHeightPx).roundToInt()) }
            .onGloballyPositioned {
                layout.clockNaturalHeightPx = it.size.height.toFloat()
                layout.clockBlockBottomInRoot = it.positionInRoot().y + it.size.height
            }
            .graphicsLayer { alpha = frame.clockAppearAlpha.value },
        alignment = frame.state.clock.placement.alignment,
        scaleOriginY = 1f,
    )
    // Not composed in clock adjust mode (the handles own that space); mediaRowHeightPx is kept, so the list doesn't move.
    val player = media.shownPlayer
    if (player != null && media.fraction > 0f && frame.state.clock.adjustMode == ClockAdjustMode.NONE) {
        HomeMediaSlot(
            frame,
            player,
            Modifier
                .align(Alignment.TopStart)
                .offset { IntOffset(0, media.rowTopPx.roundToInt()) }
                .onSizeChanged { if (it.height > 0) layout.mediaRowHeightPx = it.height.toFloat() }
                .graphicsLayer { alpha = media.fraction },
        )
    }
    val listModifier = if (frame.state.list.alignment.verticalAlignment == AppListVerticalAlignment.TOP) {
        Modifier.align(Alignment.TopStart).offset { IntOffset(0, media.listTopPx.roundToInt()) }
    } else {
        // Hugs the bottom of the content area.
        Modifier.align(Alignment.BottomStart)
    }
    HomeAppListRegion(frame, with(frame.density) { (layout.contentHeightPx - media.listTopPx).coerceAtLeast(0f).toDp() }, listModifier)
}

/** Landscape Home: `[ clock pane | apps pane | dock ]`, with the dock along the bottom in roomy windows. */
@Composable
internal fun HomeLandscapeContent(frame: HomeFrame) {
    val layout = frame.layout
    HomeLandscapeLayout(
        hasDock = frame.state.dock.items.isNotEmpty(),
        dockAtBottom = frame.dockAtBottom,
        onDockRowHeightChange = { layout.landscapeDockRowHeightPx = it.toFloat() },
        clockPaneAlignment = when (frame.state.clock.placement.alignment) {
            ClockAlignment.LEFT -> Alignment.TopStart
            ClockAlignment.CENTER -> Alignment.CenterStart
            ClockAlignment.RIGHT -> Alignment.BottomStart
        },
        clockPaneWidth = with(frame.density) { frame.geometry.landscapePaneWidthPx.toDp() },
        modifier = Modifier
            .windowInsetsPadding(WindowInsets.systemBars)
            .padding(top = HOME_LANDSCAPE_VERTICAL_PADDING_TOP, bottom = HOME_LANDSCAPE_VERTICAL_PADDING_BOTTOM)
            .onSizeChanged { layout.landscapeContentWidthPx = it.width.toFloat() },
        onAppsPaneSizeChange = {
            layout.contentHeightPx = it.height.toFloat()
            layout.appsPaneWidthPx = it.width.toFloat()
        },
        clock = {
            HomeClockSlot(frame, ClockAlignment.LEFT, frame.geometry.landscape.originY, Modifier.graphicsLayer { alpha = frame.clockAppearAlpha.value })
        },
        apps = { LandscapeAppsPane(frame) },
        // The side column is icons only; the roomy bottom row follows the user's dock style, as portrait does.
        dock = { HomeDockItems(frame, if (frame.dockAtBottom) frame.state.dock.displayMode else DockDisplayMode.ICONS) },
    )
}

/**
 * The media row is pinned to the top of the pane whatever the list's alignment; the list sits below it
 * (top-aligned) or at the bottom, in the height the row leaves. The row's height is kept across hides and
 * animated in, as in portrait, so the list doesn't jump.
 */
@Composable
private fun BoxScope.LandscapeAppsPane(frame: HomeFrame) {
    val layout = frame.layout
    val media = frame.media
    val reservedPx = layout.mediaRowHeightPx * media.fraction
    val player = media.shownPlayer
    if (player != null && media.fraction > 0f) {
        HomeMediaSlot(
            frame,
            player,
            Modifier
                .align(Alignment.TopStart)
                .onSizeChanged { if (it.height > 0) layout.mediaRowHeightPx = it.height.toFloat() }
                .padding(bottom = HOME_MEDIA_ROW_BOTTOM_GAP)
                .graphicsLayer { alpha = media.fraction },
        )
    }
    val listModifier = if (frame.state.list.alignment.verticalAlignment == AppListVerticalAlignment.TOP) {
        Modifier.align(Alignment.TopStart).offset { IntOffset(0, reservedPx.roundToInt()) }
    } else {
        Modifier.align(Alignment.BottomStart)
    }
    HomeAppListRegion(frame, with(frame.density) { (layout.contentHeightPx - reservedPx).coerceAtLeast(0f).toDp() }, listModifier)
}
