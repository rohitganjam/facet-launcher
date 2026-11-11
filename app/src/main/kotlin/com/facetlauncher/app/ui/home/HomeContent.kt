package com.facetlauncher.app.ui.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.layout
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.layout.positionInRoot
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import com.facetlauncher.app.data.model.GlassStyle
import com.facetlauncher.app.data.model.landscape
import com.facetlauncher.app.data.model.AppListVerticalAlignment
import com.facetlauncher.app.data.model.ClockAlignment
import com.facetlauncher.app.data.model.DockDisplayMode
import com.facetlauncher.app.ui.components.glassBackground
import com.facetlauncher.app.ui.home.HomeMenuMode
import com.facetlauncher.app.ui.theme.launcherSystemBarInsets
import kotlin.math.roundToInt

/** The screen's own side margin in portrait; the grid layout spans past it so its columns can line up with the dock. */
internal val HOME_SIDE_MARGIN = 24.dp

/** Portrait Home: the clock, media row and app list stacked over the dock row. */
@Composable
internal fun HomePortraitContent(frame: HomeFrame) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            // Edge-to-edge is enforced at this app's targetSdk, so without this the dock draws under the
            // gesture/nav bar. It is on this Column, not the root Box, so the Box stays truly full-screen
            // for the resize handles' root-coordinate math.
            .padding(launcherSystemBarInsets().asPaddingValues())
            .padding(bottom = 22.dp),
    ) {
        // Everything but the dock keeps the screen's own 24dp side margin. The dock row sits outside it so
        // SpaceEvenly spaces its icons against the true screen edge.
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(horizontal = HOME_SIDE_MARGIN)
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
                    .then(if (frame.state.dock.glass != GlassStyle.OFF) Modifier.padding(horizontal = 12.dp) else Modifier)
                    .glassBackground(frame.state.dock.glass, MaterialTheme.shapes.large)
                    // Even space inside the panel; text tiles already carry 14dp above and below.
                    .then(if (frame.state.dock.glass == GlassStyle.OFF) Modifier else if (frame.state.dock.displayMode == DockDisplayMode.TEXT) Modifier.padding(horizontal = 10.dp) else Modifier.padding(12.dp))
                    .testTag(homeTag("home_dock_row")),
                // Inside a glass panel the outer icons sit the same distance from its edge as from its top and bottom.
                horizontalArrangement = if (frame.state.dock.glass != GlassStyle.OFF) Arrangement.SpaceBetween else Arrangement.SpaceEvenly,
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
            // Placed from the height measured in this very pass, so a page of another height never shows a frame at the old one's place.
            .layout { measurable, constraints ->
                val placeable = measurable.measure(constraints)
                layout(placeable.width, placeable.height) { placeable.place(0, (handlePx - frame.metrics.minGapPx - placeable.height).roundToInt()) }
            }
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
    if (player != null && media.fraction > 0f && frame.state.clock.adjustMode == HomeMenuMode.NONE) {
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
    // Under the divider, and under the media row when it shows; the list is pushed down to make room.
    if (frame.state.clockStack.pageCount > 1) {
        val dotsInsetPx = (with(frame.density) { CLOCK_PAGE_DOTS_ROW_HEIGHT.toPx() } - with(frame.density) { CLOCK_PAGE_DOT_ROW_CONTENT.toPx() }) / 2f
        ClockPageDots(
            pageCount = frame.state.clockStack.pageCount,
            pageIndex = frame.state.clockStack.pageIndex,
            dragging = frame.actions.clock.pageDragging(),
            forceShown = frame.state.clock.adjustMode == HomeMenuMode.ADJUST,
            modifier = Modifier
                .align(Alignment.TopCenter)
                .offset { IntOffset(0, (media.dotsTopPx + dotsInsetPx).roundToInt()) },
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
    val currentOnClockPaneBoundsChange by rememberUpdatedState(frame.actions.onClockPaneBoundsChange)
    DisposableEffect(Unit) { onDispose { currentOnClockPaneBoundsChange(null) } }
    val clockPaneAlignment = when (frame.state.clock.placement.alignment) {
        ClockAlignment.LEFT -> Alignment.TopStart
        ClockAlignment.CENTER -> Alignment.CenterStart
        ClockAlignment.RIGHT -> Alignment.BottomStart
    }
    HomeLandscapeLayout(
        hasDock = frame.state.dock.items.isNotEmpty(),
        dockAtBottom = frame.dockAtBottom,
        onDockRowHeightChange = { layout.landscapeDockRowHeightPx = it.toFloat() },
        onClockPaneBoundsChange = {
            layout.landscapeClockPaneHeightPx = it.height
            currentOnClockPaneBoundsChange(it.translate(-(layout.rootOriginInRoot ?: Offset.Zero)))
        },
        clockPaneAlignment = clockPaneAlignment,
        clockPaneWidth = with(frame.density) { frame.geometry.landscapePaneWidthPx.toDp() },
        modifier = Modifier
            .padding(launcherSystemBarInsets().asPaddingValues())
            .padding(top = HOME_LANDSCAPE_VERTICAL_PADDING_TOP, bottom = HOME_LANDSCAPE_VERTICAL_PADDING_BOTTOM)
            .onSizeChanged { layout.landscapeContentWidthPx = it.width.toFloat() },
        onAppsPaneSizeChange = {
            layout.contentHeightPx = it.height.toFloat()
            layout.appsPaneWidthPx = it.width.toFloat()
        },
        clock = {
            HomeClockSlot(
                frame,
                ClockAlignment.LEFT,
                frame.geometry.landscape.originY,
                Modifier.graphicsLayer { alpha = frame.clockAppearAlpha.value },
                scaledFootprint = true,
                anchor = clockPaneAlignment,
            )
        },
        clockPaneOverlay = {
            val stack = frame.state.clockStack
            if (stack.pageCount > 1) {
                ClockPageDots(
                    pageCount = stack.pageCount,
                    pageIndex = stack.pageIndex,
                    dragging = frame.actions.clock.pageDragging(),
                    forceShown = frame.state.clock.adjustMode == HomeMenuMode.ADJUST,
                    modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = HOME_LANDSCAPE_CLOCK_DOTS_BOTTOM),
                )
            }
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
                .padding(bottom = HOME_MEDIA_ROW_GAP)
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
