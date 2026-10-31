package com.facetlauncher.app.ui.home

import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.facetlauncher.app.data.model.landscape
import com.facetlauncher.app.data.model.MediaPlayerState
import com.facetlauncher.app.ui.home.ClockAdjustMode
import com.facetlauncher.app.ui.home.HomeClockWidgetState
import com.facetlauncher.app.ui.home.HomeItemLook
import com.facetlauncher.app.ui.home.HomeItemStyle
import com.facetlauncher.app.ui.home.HomeLayoutMode
import com.facetlauncher.app.ui.home.HomeScreenActions
import com.facetlauncher.app.ui.home.HomeScreenState
import com.facetlauncher.app.ui.home.stableKey
import com.facetlauncher.app.ui.hub.HUB_LANDSCAPE_CELL_SIZE
import com.facetlauncher.app.ui.theme.resolve
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withTimeoutOrNull

/**
 * Everything one frame of Home is drawn from: the state and actions, the measurements, and the geometry
 * and reveal values resolved from them. Built once per recomposition by [rememberHomeFrame].
 */
@Stable
internal class HomeFrame(
    val state: HomeScreenState,
    val actions: HomeScreenActions,
    val layout: HomeScreenLayout,
    val mode: HomeLayoutMode,
    val space: HomeLandscapeSpace,
    val metrics: ClockMetrics,
    val geometry: ClockGeometry,
    /** The hosted widget's smallest size in px for this mode. */
    val widgetMinPx: Offset,
    val media: HomeMediaLayout,
    val clockAppearAlpha: State<Float>,
    val listAppearAlpha: State<Float>,
    val itemStyle: HomeItemStyle,
    val listContent: HomeAppListContent,
    val density: Density,
) {
    val isLandscape: Boolean get() = mode == HomeLayoutMode.LANDSCAPE

    /** The clock and the app list (when there is one) have finished their fade-in. */
    val isFullyRevealed: Boolean
        get() = clockAppearAlpha.value >= 1f && (listAppearAlpha.value >= 1f || state.list.items.isEmpty())

    /** Roomy landscape windows put the dock along the bottom. */
    val dockAtBottom: Boolean get() = isLandscape && space == HomeLandscapeSpace.ROOMY

    /** The height the bottom dock row takes, kept out of the divider and the pill. */
    val landscapeDockReserve: Dp
        get() = if (dockAtBottom && state.dock.items.isNotEmpty()) with(density) { layout.landscapeDockRowHeightPx.toDp() } else 0.dp
}

/** Where the media row and the app list sit below the clock, and how far the row has faded in. */
@Immutable
internal data class HomeMediaLayout(
    /** The player shown: the current one, or the last one while it fades out. */
    val shownPlayer: MediaPlayerState?,
    val fraction: Float,
    val rowTopPx: Float,
    /** The app list's top now, following the row as it fades in or out. */
    val listTopPx: Float,
    /** Where the clock stack's page dots sit: under the divider, or under the media row when it shows. Only the portrait layout draws them. */
    val dotsTopPx: Float,
    /** Where the app list's top settles once the row's fade finishes. */
    val listTopTargetPx: Float,
)

@Composable
internal fun rememberHomeFrame(
    state: HomeScreenState,
    actions: HomeScreenActions,
    layout: HomeScreenLayout,
    mode: HomeLayoutMode,
    space: HomeLandscapeSpace,
): HomeFrame {
    val density = LocalDensity.current
    val landscape = mode == HomeLayoutMode.LANDSCAPE
    val metrics = clockMetrics(density)
    val widgetMinPx = clockWidgetMinPx(state.widget, landscape, density)
    val geometry = computeClockGeometry(clockGeometryInputs(state, layout, landscape, metrics, widgetMinPx, density))
    val media = rememberHomeMediaLayout(state.media.player, layout, geometry.handle.handlePx, metrics.minGapPx, landscape, state.clockStack.pageCount > 1, density)
    val itemStyle = rememberHomeItemStyle(state.list.look)
    return HomeFrame(
        state = state,
        actions = actions,
        layout = layout,
        mode = mode,
        space = space,
        metrics = metrics,
        geometry = geometry,
        widgetMinPx = widgetMinPx,
        media = media,
        clockAppearAlpha = rememberClockAppearAlpha(layout),
        listAppearAlpha = rememberListAppearAlpha(state, layout, media),
        itemStyle = itemStyle,
        listContent = remember(state.list.items, itemStyle) { HomeAppListContent(state.list.items, itemStyle) },
        density = density,
    )
}

@Composable
private fun clockMetrics(density: Density): ClockMetrics {
    val topInsetPx = WindowInsets.systemBars.getTop(density)
    val headroomPx = with(density) { (HOME_CLOCK_RESIZE_HANDLE_INSET + 12.dp).toPx() }
    return ClockMetrics(
        topInsetPx = topInsetPx.toFloat(),
        minGapPx = with(density) { HOME_CLOCK_MIN_GAP.toPx() },
        topReservePx = topInsetPx + headroomPx,
        sideMarginPx = with(density) { 24.dp.toPx() },
        handleHeadroomPx = headroomPx,
        landscapeMarginsPx = with(density) { (HOME_LANDSCAPE_CLOCK_PANE_START_PADDING + HOME_LANDSCAPE_CLOCK_PANE_END_MARGIN).toPx() },
    )
}

/** The resize floor is one Hub cell in landscape, where cells are a fixed size; in portrait it is the provider's own minimum. */
private fun clockWidgetMinPx(widget: HomeClockWidgetState?, landscape: Boolean, density: Density): Offset {
    if (landscape) return with(density) { HUB_LANDSCAPE_CELL_SIZE.toPx() }.let { Offset(it, it) }
    return Offset(
        widget?.minWidthDp?.let { with(density) { it.toPx() } } ?: 1f,
        widget?.minHeightDp?.let { with(density) { it.toPx() } } ?: 1f,
    )
}

private fun clockGeometryInputs(
    state: HomeScreenState,
    layout: HomeScreenLayout,
    landscape: Boolean,
    metrics: ClockMetrics,
    widgetMinPx: Offset,
    density: Density,
): ClockGeometryInputs {
    val placement = state.clock.placement
    return ClockGeometryInputs(
        landscape = landscape,
        adjusting = state.clock.adjustMode == ClockAdjustMode.ADJUST,
        alignment = placement.alignment,
        scale = placement.scale,
        liveScale = layout.liveScale,
        zoneHeightPx = placement.zoneHeightDp?.let { with(density) { it.dp.toPx() } },
        liveHandlePx = layout.liveHandlePx,
        liveDividerWidthPx = layout.liveDividerWidthPx,
        widget = state.widget?.let { clockWidgetMeasure(it, layout, widgetMinPx, density) },
        boxSize = layout.clockBoxSize,
        boxOriginInRoot = layout.clockBoxOriginInRoot,
        rootOriginInRoot = layout.rootOriginInRoot,
        rootWidthPx = layout.rootSize?.width,
        clockNaturalHeightPx = layout.clockNaturalHeightPx,
        contentHeightPx = layout.contentHeightPx,
        landscapeContentWidthPx = layout.landscapeContentWidthPx,
        blockBottomInRoot = layout.clockBlockBottomInRoot,
        metrics = metrics,
    )
}

private fun clockWidgetMeasure(widget: HomeClockWidgetState, layout: HomeScreenLayout, minPx: Offset, density: Density): ClockWidgetMeasure {
    val width = widget.widthDp
    val height = widget.heightDp
    return ClockWidgetMeasure(
        saved = if (width != null && height != null) Offset(with(density) { width.toPx() }, with(density) { height.toPx() }) else null,
        liveWidthPx = layout.liveWidgetWidthPx,
        liveHeightPx = layout.liveWidgetHeightPx,
        minWidthPx = minPx.x,
        minHeightPx = minPx.y,
    )
}

/** The media row's fade, and the list tops that follow it. Landscape's list fills its own pane from the top, so nothing is reserved above it there. */
@Composable
private fun rememberHomeMediaLayout(
    player: MediaPlayerState?,
    layout: HomeScreenLayout,
    handlePx: Float,
    minGapPx: Float,
    landscape: Boolean,
    showsPageDots: Boolean,
    density: Density,
): HomeMediaLayout {
    LaunchedEffect(player) { if (player != null) layout.lastMediaPlayer = player }
    val fraction by animateFloatAsState(
        targetValue = if (player != null) 1f else 0f,
        animationSpec = tween(durationMillis = 200, easing = EmphasizedDecelerateEasing),
        label = "mediaPlayerAppearance",
    )
    val rowTopPx = handlePx - minGapPx + with(density) { HOME_MEDIA_ROW_TOP_GAP.toPx() }
    val belowGapPx = with(density) { HOME_MEDIA_ROW_BOTTOM_GAP.toPx() }
    // The dots get their own row under the divider (and under the media row), which pushes the list down while the stack has several pages.
    val dotsRowPx = if (showsPageDots && !landscape) with(density) { CLOCK_PAGE_DOTS_ROW_HEIGHT.toPx() } else 0f
    fun belowMediaPx(reservePx: Float) = if (reservePx > 0f) maxOf(handlePx, rowTopPx + reservePx + belowGapPx) else handlePx
    fun listTopFor(reservePx: Float) = belowMediaPx(reservePx) + dotsRowPx
    return HomeMediaLayout(
        shownPlayer = player ?: layout.lastMediaPlayer,
        fraction = fraction,
        rowTopPx = rowTopPx,
        listTopPx = listTopFor(layout.mediaRowHeightPx * fraction),
        dotsTopPx = belowMediaPx(layout.mediaRowHeightPx * fraction),
        listTopTargetPx = if (landscape) 0f else listTopFor(if (player != null) layout.mediaRowHeightPx else 0f),
    )
}

@Composable
private fun rememberHomeItemStyle(look: HomeItemLook): HomeItemStyle {
    val labelColor = look.labelColorOption.resolve()
    val labelFontWeight = look.labelFontWeightOption.resolve()
    return remember(look, labelColor, labelFontWeight) {
        HomeItemStyle(
            badgeCounts = look.badgeCounts,
            badgeStyle = look.badgeStyle,
            appMenuNotifications = look.appMenuNotifications,
            folderCandidates = look.folderCandidates,
            drawerPresentation = look.drawerPresentation,
            labelColor = labelColor,
            labelFontWeight = labelFontWeight,
        )
    }
}

/**
 * Fades the clock in once its box has been measured. The bundled fonts load synchronously, so the box is the
 * same on every pass (checked on a cold start at several clock scales). One-way; reset with the composable on a rebuild.
 */
@Composable
private fun rememberClockAppearAlpha(layout: HomeScreenLayout): State<Float> {
    var revealed by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        withTimeoutOrNull(1_500) {
            snapshotFlow { layout.clockBoxSize }.filter { it != null }.first()
        }
        revealed = true
    }
    // The State itself, read only inside graphicsLayer, so the fade runs in the draw phase without recomposing Home each frame.
    return animateFloatAsState(
        targetValue = if (revealed) 1f else 0f,
        animationSpec = tween(durationMillis = 250, easing = EmphasizedDecelerateEasing),
        label = "clockAppearance",
    )
}

/**
 * The app list stays hidden until the compact-spacing decision has been made for the current list, then
 * fades in once, so it never renders at regular spacing and snaps to compact. The list is first measured at
 * regular density off-screen. Once that settles, it switches to the tighter density if it would otherwise need
 * to scroll. Evaluated once per key change, not continuously: flipping to compact shrinks the content and
 * re-triggers the measurement, but the effect has finished by then and doesn't see its own result.
 */
@Composable
private fun rememberListAppearAlpha(state: HomeScreenState, layout: HomeScreenLayout, media: HomeMediaLayout): State<Float> {
    // AppInfo.icon has no structural equality, so the raw list "changes" on every relist; its stable keys don't.
    val listKey = state.list.items.map { it.stableKey() }
    val presentation = state.list.rowPresentation
    val usagePrompt = state.list.showUsageAccessPrompt
    var revealed by remember(listKey, presentation, usagePrompt) { mutableStateOf(false) }
    val hasMedia = state.media.player != null
    LaunchedEffect(listKey, presentation, usagePrompt, state.clock.placement.zoneHeightDp, hasMedia, layout.mediaRowHeightPx) {
        layout.useCompactAppSpacing = false
        withTimeoutOrNull(GEOMETRY_SETTLE_TIMEOUT_MS) {
            snapshotFlow { layout.appListNaturalHeightPx to (layout.contentHeightPx - media.listTopTargetPx) }
                .first { (natural, available) -> natural > 0f && available > 0f }
        }
        layout.useCompactAppSpacing = layout.appListNaturalHeightPx > (layout.contentHeightPx - media.listTopTargetPx)
        revealed = true
    }
    return animateFloatAsState(
        targetValue = if (revealed) 1f else 0f,
        animationSpec = tween(durationMillis = 200, easing = EmphasizedDecelerateEasing),
        label = "appListAppearance",
    )
}

/** Shared timing for the "wait for layout geometry to settle, then decide once" checks: long enough to skip transient double-measurement, bounded so a check that never gets valid geometry doesn't hang. */
internal const val GEOMETRY_SETTLE_DEBOUNCE_MS = 150L
internal const val GEOMETRY_SETTLE_TIMEOUT_MS = 3_000L

/** M3 emphasized-decelerate easing (`cubic-bezier(.05,.7,.1,1)`) — an element coming to rest as it enters. */
internal val EmphasizedDecelerateEasing = CubicBezierEasing(0.05f, 0.7f, 0.1f, 1f)
