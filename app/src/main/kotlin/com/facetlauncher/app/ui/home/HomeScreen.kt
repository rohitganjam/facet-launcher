package com.facetlauncher.app.ui.home

import android.content.res.Configuration
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.layout.positionInRoot
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.onClick
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.toSize
import com.facetlauncher.app.R
import com.facetlauncher.app.data.local.landscape
import com.facetlauncher.app.data.model.AppInfo
import com.facetlauncher.app.data.model.AppListColumnAlignment
import com.facetlauncher.app.data.model.AppListLayout
import com.facetlauncher.app.data.model.AppListVerticalAlignment
import com.facetlauncher.app.data.model.AppRowPosition
import com.facetlauncher.app.data.model.AppRowPresentation
import com.facetlauncher.app.data.model.ClockAlignment
import com.facetlauncher.app.data.model.DockDisplayMode
import com.facetlauncher.app.data.model.MediaPlayerState
import com.facetlauncher.app.data.model.PlacedItem
import com.facetlauncher.app.ui.components.AppIconSize
import com.facetlauncher.app.ui.components.ThemedModalBottomSheet
import com.facetlauncher.app.ui.components.dashedBorder
import com.facetlauncher.app.ui.home.ClockAdjustMode
import com.facetlauncher.app.ui.home.DockIcon
import com.facetlauncher.app.ui.home.HomeDockState
import com.facetlauncher.app.ui.home.HomeItemActions
import com.facetlauncher.app.ui.home.HomeItemStyle
import com.facetlauncher.app.ui.home.HomeLayoutMode
import com.facetlauncher.app.ui.home.HomeListState
import com.facetlauncher.app.ui.home.HomeScreen
import com.facetlauncher.app.ui.home.HomeScreenActions
import com.facetlauncher.app.ui.home.HomeScreenState
import com.facetlauncher.app.ui.home.MediaPlayerRow
import com.facetlauncher.app.ui.home.rememberHomeLandscapeSpace
import com.facetlauncher.app.ui.home.rememberHomeLayoutMode
import com.facetlauncher.app.ui.home.stableKey
import com.facetlauncher.app.ui.hub.HUB_LANDSCAPE_CELL_SIZE
import com.facetlauncher.app.ui.theme.Accent
import com.facetlauncher.app.ui.theme.FacetLauncherTheme
import com.facetlauncher.app.ui.theme.Ink
import com.facetlauncher.app.ui.theme.Muted
import com.facetlauncher.app.ui.theme.SurfaceContainer
import com.facetlauncher.app.ui.theme.resolve
import kotlin.math.roundToInt
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withTimeoutOrNull

/**
 * Home surface (`1a`, Airy density `1e`): clock + a short curated app list + dock. Both
 * [appListItems] (Favorites/Recents/Most Used depending on the active list content mode, from
 * [com.facetlauncher.app.data.PlacementRepository] or [com.facetlauncher.app.data.UsageStatsRepository])
 * and [dockApps] (from [com.facetlauncher.app.data.PlacementRepository]) are real persisted state — this composable
 * only renders, it never slices/computes them itself.
 */
@Composable
fun HomeScreen(
    state: HomeScreenState,
    actions: HomeScreenActions,
    modifier: Modifier = Modifier,
    layoutMode: HomeLayoutMode = rememberHomeLayoutMode(),
    landscapeSpace: HomeLandscapeSpace = rememberHomeLandscapeSpace(),
) {
    // Short names for the groups below; phase D replaces them as the body is split up.
    val appListItems = state.list.items
    val dockApps = state.dock.items
    val onAppClick = actions.item.onAppClick
    val appRowPosition = state.list.alignment.rowPosition
    val appRowPresentation = state.list.rowPresentation
    val showUsageAccessPrompt = state.list.showUsageAccessPrompt
    val onUsageAccessPromptClick = actions.onUsageAccessPromptClick
    val dockDisplayMode = state.dock.displayMode
    val notificationBadgeStyle = state.list.look.badgeStyle
    val badgeCounts = state.list.look.badgeCounts
    val mediaPlayer = state.media.player
    val showMediaSkipButtons = state.media.showSkipButtons
    val onMediaPrevious = actions.media.onPrevious
    val onMediaPlayPause = actions.media.onPlayPause
    val onMediaNext = actions.media.onNext
    val onMediaOpen = actions.media.onOpen
    val use24HourTime = state.clock.use24HourTime
    val clockTemplateId = state.clock.templateId
    val clockFontOption = state.clock.fontOption
    val clockColorOption = state.clock.colorOption
    val clockAccentColorOption = state.clock.accentColorOption
    val clockShowMeridiem = state.clock.showMeridiem
    val clockDateStyle = state.clock.dateStyle
    val clockAlignment = state.clock.placement.alignment
    val clockScale = state.clock.placement.scale
    val clockZoneHeightDp = state.clock.placement.zoneHeightDp
    val clockAdjustMode = state.clock.adjustMode
    val onClockZoneHeightCommit = actions.clock.onZoneHeightCommit
    val onClockScaleCommit = actions.clock.onScaleCommit
    val onClockAlignmentChange = actions.clock.onAlignmentChange
    val onAdjustModeChange = actions.clock.onAdjustModeChange
    val draggingHandle = state.clock.draggingHandle
    val onDraggingHandleChange = actions.clock.onDraggingHandleChange
    val clockPositionOwnerFacetId = state.clock.positionOwnerFacetId
    val clockPositionOwnerFacetName = state.clock.positionOwnerFacetName
    val onClockClick = actions.clock.onClockClick
    val onEditClockStyles = actions.clock.onEditClockStyles
    val onNavigateToSettings = actions.clock.onNavigateToSettings
    val onNavigateToFacetSettings = actions.clock.onNavigateToFacetSettings
    val hasCustomClockWidget = state.widget != null
    val isPro = state.clock.isPro
    val onUseCustomWidgetClick = actions.clock.onUseCustomWidgetClick
    val onSwitchToLauncherClockClick = actions.clock.onSwitchToLauncherClockClick
    val clockWidgetAppWidgetId = state.widget?.appWidgetId
    val createClockWidgetHostView = actions.widget.createHostView
    val clockWidgetWidthDp = state.widget?.widthDp
    val clockWidgetHeightDp = state.widget?.heightDp
    val clockWidgetMinWidthDp = state.widget?.minWidthDp
    val clockWidgetMinHeightDp = state.widget?.minHeightDp
    val onClockWidgetSizeChange = actions.widget.onSizeChange
    val onClockWidgetSizeCommit = actions.widget.onSizeCommit
    val onClockWidgetBoundsChange = actions.widget.onBoundsChange
    val onClockWidgetNestedScrollStart = actions.widget.onNestedScrollStart
    val onClockWidgetLeftoverScroll = actions.widget.onLeftoverScroll
    val onClockWidgetNestedScrollStop = actions.widget.onNestedScrollStop
    val onClockWidgetLeftoverFling = actions.widget.onLeftoverFling
    val onAppListBoundsChange = actions.onAppListBoundsChange
    val appListVerticalAlignment = state.list.alignment.verticalAlignment
    val appListLayout = state.list.layout
    val appListColumnAlignment = state.list.alignment.columnAlignment
    val appListGridColumns = state.list.gridColumns
    val appListGridDisplayMode = state.list.gridDisplayMode
    val nextAlarmMillis = state.calendar.nextAlarmMillis
    val batteryPercent = state.calendar.batteryPercent
    val isCharging = state.calendar.isCharging
    val calendarEvents = state.calendar.events
    val calendarColors = state.calendar.colors
    val calendarDisplay = state.calendar.display
    val onEventClick = actions.clock.onEventClick
    val appMenuNotifications = state.list.look.appMenuNotifications
    val onAppInfo = actions.item.onAppInfo
    val onRequestQuickAddState = actions.item.onRequestQuickAddState
    val onFavoritesAction = actions.item.onFavoritesAction
    val onDockAction = actions.item.onDockAction
    val onRequestFolderQuickAddState = actions.item.onRequestFolderQuickAddState
    val onFolderFavoritesAction = actions.item.onFolderFavoritesAction
    val onFolderDockAction = actions.item.onFolderDockAction
    val folderCandidates = state.list.look.folderCandidates
    val onCreateFolder = actions.item.onCreateFolder
    val onAddToFolder = actions.item.onAddToFolder
    val onRemoveFromFolder = actions.item.onRemoveFromFolder
    val onRenameFolder = actions.item.onRenameFolder
    val drawerPresentation = state.list.look.drawerPresentation
    val launcherFontOption = state.clock.launcherFontOption
    val appLabelColorOption = state.list.look.labelColorOption
    val homeAppsFontWeight = state.list.look.labelFontWeightOption

    val isLandscape = layoutMode == HomeLayoutMode.LANDSCAPE
    val appLabelColor = appLabelColorOption.resolve()
    val appLabelFontWeight = homeAppsFontWeight.resolve()
    val density = LocalDensity.current
    val layout = rememberHomeScreenLayout()
    // The resize handles can land right at the screen edge (e.g. a full-width hosted widget) —
    // real bug found on-device: Android's own edge-swipe back gesture (wider on some OEM skins,
    // e.g. Samsung One UI) intercepts the touch-down before the handle's own pointerInput ever
    // sees it, making it ungrabbable. View.setSystemGestureExclusionRects (API 29+) is the
    // platform's own mechanism for exactly this — an app with edge-anchored interactive controls
    // opts that specific region out of the gesture-nav intercept. Registered/cleared below,
    // scoped to whichever ADJUST block (native clock or hosted widget) is actually active.
    val view = LocalView.current
    // Roomy landscape windows put the dock along the bottom; the height it takes is kept out of the divider and pill.
    val landscapeDockAtBottom = isLandscape && landscapeSpace == HomeLandscapeSpace.ROOMY
    val landscapeDockReserve = if (landscapeDockAtBottom && dockApps.isNotEmpty()) with(density) { layout.landscapeDockRowHeightPx.toDp() } else 0.dp
    LaunchedEffect(clockZoneHeightDp) {
        layout.liveHandlePx = null
        layout.liveDividerWidthPx = null
    }

    LaunchedEffect(clockScale) { layout.liveScale = null }



    LaunchedEffect(clockWidgetWidthDp, clockWidgetHeightDp) {
        layout.liveWidgetWidthPx = null
        layout.liveWidgetHeightPx = null
    }

    val metrics = ClockMetrics(
        topInsetPx = WindowInsets.systemBars.getTop(density).toFloat(),
        defaultTopOffsetPx = with(density) { HOME_CLOCK_DEFAULT_TOP_OFFSET.toPx() },
        minGapPx = with(density) { HOME_CLOCK_MIN_GAP.toPx() },
        topReservePx = WindowInsets.systemBars.getTop(density) + with(density) { (HOME_CLOCK_RESIZE_HANDLE_INSET + 12.dp).toPx() },
        sideMarginPx = with(density) { 24.dp.toPx() },
        handleHeadroomPx = with(density) { (HOME_CLOCK_RESIZE_HANDLE_INSET + 12.dp).toPx() },
        landscapeMarginsPx = with(density) { (HOME_LANDSCAPE_CLOCK_PANE_START_PADDING + HOME_LANDSCAPE_CLOCK_PANE_END_MARGIN).toPx() },
    )
    // The resize floor is one Hub cell; landscape's cells are a fixed size, not a share of the window width.
    val clockWidgetMinWidthPx = if (isLandscape) with(density) { HUB_LANDSCAPE_CELL_SIZE.toPx() } else clockWidgetMinWidthDp?.let { with(density) { it.toPx() } } ?: 1f
    val clockWidgetMinHeightPx = if (isLandscape) clockWidgetMinWidthPx else clockWidgetMinHeightDp?.let { with(density) { it.toPx() } } ?: 1f
    val geometry = computeClockGeometry(
        ClockGeometryInputs(
            landscape = isLandscape,
            adjusting = clockAdjustMode == ClockAdjustMode.ADJUST,
            alignment = clockAlignment,
            scale = clockScale,
            liveScale = layout.liveScale,
            zoneHeightPx = clockZoneHeightDp?.let { with(density) { it.dp.toPx() } },
            liveHandlePx = layout.liveHandlePx,
            liveDividerWidthPx = layout.liveDividerWidthPx,
            widget = clockWidgetAppWidgetId?.let {
                ClockWidgetMeasure(
                    saved = if (clockWidgetWidthDp != null && clockWidgetHeightDp != null) {
                        Offset(with(density) { clockWidgetWidthDp.toPx() }, with(density) { clockWidgetHeightDp.toPx() })
                    } else {
                        null
                    },
                    liveWidthPx = layout.liveWidgetWidthPx,
                    liveHeightPx = layout.liveWidgetHeightPx,
                    minWidthPx = clockWidgetMinWidthPx,
                    minHeightPx = clockWidgetMinHeightPx,
                )
            },
            boxSize = layout.clockBoxSize,
            boxOriginInRoot = layout.clockBoxOriginInRoot,
            rootOriginInRoot = layout.rootOriginInRoot,
            rootWidthPx = layout.rootSize?.width,
            clockNaturalHeightPx = layout.clockNaturalHeightPx,
            contentHeightPx = layout.contentHeightPx,
            landscapeContentWidthPx = layout.landscapeContentWidthPx,
            blockBottomInRoot = layout.clockBlockBottomInRoot,
            metrics = metrics,
        ),
    )
    // Short names for the geometry's parts, used by the layouts and overlays below.
    val handlePx = geometry.handle.handlePx
    val floorPx = geometry.handle.floorPx
    val ceilingPx = geometry.handle.ceilingPx
    val basePersistedHandlePx = geometry.handle.basePx
    val minGapPx = metrics.minGapPx
    val topInsetPx = metrics.topInsetPx
    val defaultTopOffsetPx = metrics.defaultTopOffsetPx
    val clockTopReservePx = metrics.topReservePx
    val sideMarginPx = metrics.sideMarginPx
    val maxScaleThatFits = geometry.fit::maxScale
    val maxWidgetSizePxThatFits = geometry.fit::maxWidgetSize
    val effectiveClockScale = geometry.effectiveScale
    val effectiveClockWidgetSizePx = geometry.effectiveWidgetSizePx
    val landscapeOriginY = geometry.landscape.originY
    val landscapeMarginsPx = metrics.landscapeMarginsPx
    val landscapeContentWidth = geometry.landscape.contentWidthPx
    val landscapeNaturalSize = geometry.landscape.naturalSize
    val landscapeMinClockWidthPx = geometry.landscape.minClockWidthPx
    val landscapeMaxWidgetSize = geometry.landscape.maxWidgetSize
    val landscapeMaxScale = geometry.landscape.maxScale
    val landscapePaneWidthPx = geometry.landscapePaneWidthPx

    LaunchedEffect(mediaPlayer) { if (mediaPlayer != null) layout.lastMediaPlayer = mediaPlayer }
    val shownMediaPlayer = mediaPlayer ?: layout.lastMediaPlayer
    val mediaFraction by animateFloatAsState(
        targetValue = if (mediaPlayer != null) 1f else 0f,
        animationSpec = tween(durationMillis = 200, easing = EmphasizedDecelerateEasing),
        label = "mediaPlayerAppearance",
    )
    val mediaRowTopPx = handlePx - minGapPx + with(density) { HOME_MEDIA_ROW_TOP_GAP.toPx() }
    val mediaBelowGapPx = with(density) { HOME_MEDIA_ROW_BOTTOM_GAP.toPx() }
    val appListTopFor: (Float) -> Float = { reservePx ->
        if (reservePx > 0f) maxOf(handlePx, mediaRowTopPx + reservePx + mediaBelowGapPx) else handlePx
    }
    val appListTopPx = appListTopFor(layout.mediaRowHeightPx * mediaFraction)
    // Landscape's list fills its own pane from the top, so nothing is reserved above it.
    val appListTopTargetPx = if (isLandscape) 0f else appListTopFor(if (mediaPlayer != null) layout.mediaRowHeightPx else 0f)


    // One-shot re-clamp: a style or position change resizes the clock's natural box, so a scale
    // that fit the old look can now overflow. After the new geometry settles, check once — and
    // only persist a smaller value if it genuinely clips. Never runs otherwise. Shares its
    // settle-wait timing (GEOMETRY_SETTLE_*) with the compact-spacing check below — same pattern,
    // same tuning.
    LaunchedEffect(clockTemplateId, clockAlignment, clockDateStyle, clockZoneHeightDp, clockPositionOwnerFacetId, isLandscape) {
        // The fit maths is portrait geometry; running it in landscape would persist a wrong scale.
        if (isLandscape) return@LaunchedEffect
        withTimeoutOrNull(GEOMETRY_SETTLE_TIMEOUT_MS) {
            snapshotFlow { Triple(layout.clockBoxSize, layout.clockBoxOriginInRoot, layout.rootSize) }
                .filter { it.first != null && it.second != null && it.third != null }
                .debounce(GEOMETRY_SETTLE_DEBOUNCE_MS)
                .first()
        }
        val size = layout.clockBoxSize ?: return@LaunchedEffect
        val origin = layout.clockBoxOriginInRoot ?: return@LaunchedEffect
        val rootW = layout.rootSize?.width ?: return@LaunchedEffect
        val fit = maxScaleThatFits(size, origin - (layout.rootOriginInRoot ?: Offset.Zero), rootW)
        if (clockScale > fit + 0.01f) onClockScaleCommit(fit)
    }

    // Stable identity for appListItems — AppInfo.icon (an ImageBitmap) has no structural equality,
    // so the raw list "changes" on every incidental relist even when its actual membership/order
    // hasn't (same reasoning as the drag-jump bug fixed elsewhere via this exact pattern — see
    // e.g. DockSettingsScreen's own componentsKey). Keying on this instead of appListItems itself
    // keeps the effect below from re-firing (and re-hiding the list) on every such relist.
    val appListKey = appListItems.map { it.stableKey() }

    // Hidden until the compact-spacing decision below has actually been made for the current
    // list, then fades in once — same reveal mechanism as the clock's own clockAppearAlpha. Without
    // this, the list would render at regular spacing first and visibly snap to compact once the
    // measurement settles; gating visibility on the same one-shot decision means only the already-
    // decided result is ever shown. Keyed (not manually reset) so a new list starts hidden again.
    var appListRevealed by remember(appListKey, appRowPresentation, showUsageAccessPrompt) { mutableStateOf(false) }
    val appListAppearAlpha = animateFloatAsState(
        targetValue = if (appListRevealed) 1f else 0f,
        animationSpec = tween(durationMillis = 200, easing = EmphasizedDecelerateEasing),
        label = "appListAppearance",
    )

    // One-shot compact-spacing check, same pattern as the re-clamp above: this facet's own app
    // list (a different list each time this key set changes — a facet switch, a favorites edit,
    // a content-mode/presentation change, or the clock's own zone-height drag committing, which
    // changes handlePx and therefore how much room is actually left for the list — previously
    // missing from this key list, so lowering the clock left the list stuck at whatever spacing
    // was decided before the drag until something else happened to change appListKey, e.g.
    // switching facets and back; see chat history) is first measured at Home's regular row
    // density, off-screen (see appListRevealed above). Once that measurement settles, compare its
    // natural (unconstrained) height against the space actually available, switch every row to
    // App Drawer's tighter COMPACT density if it would otherwise need to scroll, then reveal.
    // Evaluated once per key-set change, not continuously — flipping to compact shrinks the
    // content and re-triggers this same measurement, but this effect has already finished by
    // then, so it doesn't see its own result and flip back.
    LaunchedEffect(appListKey, appRowPresentation, showUsageAccessPrompt, clockZoneHeightDp, mediaPlayer != null, layout.mediaRowHeightPx) {
        layout.useCompactAppSpacing = false
        withTimeoutOrNull(GEOMETRY_SETTLE_TIMEOUT_MS) {
            snapshotFlow { layout.appListNaturalHeightPx to (layout.contentHeightPx - appListTopTargetPx) }
                .filter { (natural, available) -> natural > 0f && available > 0f }
                .debounce(GEOMETRY_SETTLE_DEBOUNCE_MS)
                .first()
        }
        layout.useCompactAppSpacing = layout.appListNaturalHeightPx > (layout.contentHeightPx - appListTopTargetPx)
        appListRevealed = true
    }

    // Fade in once, after the clock's measured box has held steady for a beat — not just when it's
    // first non-null. The box is measured twice on a cold start (a variable font finishes loading
    // between passes), and uniformScale anchors the glyph to its bottom edge, so revealing between
    // the passes shows it grow upward from the bottom. This is a one-time wait, not a per-frame
    // recompute. M3 emphasized-decelerate; honours "remove animations" for free (Compose scales
    // tween durations by ANIMATOR_DURATION_SCALE). One-way; reset with the composable on a rebuild.
    var clockRevealed by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        withTimeoutOrNull(1_500) {
            snapshotFlow { layout.clockBoxSize }.filter { it != null }.debounce(120).first()
        }
        clockRevealed = true
    }
    // Not `by` — read .value only inside graphicsLayer below, so the fade animates in the draw
    // phase without recomposing this (large) composable every frame.
    val clockAppearAlpha = animateFloatAsState(
        targetValue = if (clockRevealed) 1f else 0f,
        animationSpec = tween(durationMillis = 250, easing = EmphasizedDecelerateEasing),
        label = "clockAppearance",
    )

    val effectiveClockWidgetWidthDp = effectiveClockWidgetSizePx?.let { with(density) { it.x.toDp() } } ?: clockWidgetWidthDp
    val effectiveClockWidgetHeightDp = effectiveClockWidgetSizePx?.let { with(density) { it.y.toDp() } } ?: clockWidgetHeightDp

    Box(
        modifier = modifier
            .fillMaxSize()
            .onGloballyPositioned { layout.rootSize = it.size; layout.rootOriginInRoot = it.positionInRoot() }
            // Dismisses drag mode on a tap anywhere else on the screen, or (when nothing is active)
            // opens the same long-press menu the clock itself opens on a long-press elsewhere in
            // this empty space — README's original "long-press empty home space" gesture, now
            // repurposed to reach the clock adjust sheet instead of the old removed sheet. Attached
            // to this outer Box (an ancestor of every child, including the handle below) rather than
            // a separate full-size sibling Box — Compose dispatches pointer events child-before-
            // parent, so any descendant's own gesture/clickable (an app icon, the dock, the clock's
            // own tap/long-press detector) always gets first claim on a touch landing on it; only a
            // touch that reaches this ancestor unconsumed counts as "empty space." A sibling overlay
            // occupying the same bounds as the handle would instead receive the same events
            // independently and could interfere with the handle's own gesture recognition (see chat
            // history).
            .then(
                when (clockAdjustMode) {
                    // Keyed on clockAdjustMode itself, not Unit: ADJUST and NONE both install a
                    // pointerInput at this same modifier-chain slot, so if they shared a key
                    // Compose would reuse the existing gesture-detector node (and its already-
                    // running coroutine) across an ADJUST->NONE transition instead of cancelling
                    // and relaunching it — leaving the stale ADJUST tap-to-dismiss handler running
                    // forever with no onLongPress, so nothing reopens the menu until something
                    // else (e.g. navigating away and back) forces the whole tree to recompose.
                    ClockAdjustMode.ADJUST -> Modifier.pointerInput(clockAdjustMode) {
                        detectTapGestures(onTap = { onAdjustModeChange(ClockAdjustMode.NONE) })
                    }
                    ClockAdjustMode.NONE -> Modifier.pointerInput(clockAdjustMode) {
                        detectTapGestures(onLongPress = { onAdjustModeChange(ClockAdjustMode.MENU) })
                    }
                    ClockAdjustMode.MENU -> Modifier
                }
            )
            .testTag("home_screen_root"),
    ) {
        // Shared by the portrait and landscape layouts below.
        val clockBlock: @Composable (Modifier, ClockAlignment, Float) -> Unit = { blockModifier, blockAlignment, scaleOriginY ->
            ClockBlock(
                modifier = blockModifier,
                // A plain tap opens the default clock app; a long-press instead reveals the
                // grab handle ("drag mode"). Both live in the SAME detector deliberately — an
                // earlier attempt put the tap on a separate, nested pointerInput/clickable
                // around just ClockDisplay, but any child gesture recognizer (even a bare
                // detectTapGestures with no clickable/ripple involved) claims/consumes the down
                // event as soon as it starts watching it, which silently prevented this
                // ancestor's long-press from ever completing (see chat history — broke every
                // drag-mode test the moment the clock became independently tappable). A tap
                // that lands on one of CalendarEventsBlock's own event rows is unaffected —
                // those are a genuine descendant clickable, which (per Compose's
                // child-before-parent dispatch) claims the gesture before this detector ever
                // sees it, so onTap correctly never fires for an event tap. Deliberately scoped
                // to just the rendered clock content (via ClockBlock's own clockContentModifier
                // param, applied inside ClockBlock to a wrap-content box around ClockDisplay
                // only) rather than the old full-width ClockBlock modifier — the old full-width
                // box swallowed every long-press landing in the empty space beside/around the
                // actual clock text, which silently prevented HomeDrawerRoute's own outer
                // long-press (open the facet carousel) from ever firing there (see chat
                // history). Only the clock's own visible bounds are tappable now; everything
                // outside — including beside a Left/Right-aligned clock, and beside/below the
                // independently-aligned CalendarEventsBlock — falls through untouched.
                clockContentModifier = Modifier
                    .pointerInput(Unit) {
                        detectTapGestures(
                            onTap = { onClockClick() },
                            onLongPress = { onAdjustModeChange(ClockAdjustMode.MENU) },
                        )
                    }
                    .testTag("home_clock_block"),
                clockBoxModifier = Modifier.onGloballyPositioned {
                    layout.clockBoxSize = it.size
                    layout.clockBoxOriginInRoot = it.positionInRoot()
                },
                use24HourTime = use24HourTime,
                templateId = clockTemplateId,
                fontOption = clockFontOption,
                colorOption = clockColorOption,
                accentColorOption = clockAccentColorOption,
                showMeridiem = clockShowMeridiem,
                dateStyle = clockDateStyle,
                clockAlignment = blockAlignment,
                clockScale = effectiveClockScale,
                clockScaleOriginY = scaleOriginY,
                events = calendarEvents,
                calendarColors = calendarColors,
                calendarDisplay = calendarDisplay,
                homeAppsFontWeight = homeAppsFontWeight,
                appLabelColorOption = appLabelColorOption,
                onEventClick = onEventClick,
                launcherFontOption = launcherFontOption,
                nextAlarmMillis = nextAlarmMillis,
                batteryPercent = batteryPercent,
                isCharging = isCharging,
                clockWidgetAppWidgetId = clockWidgetAppWidgetId,
                createClockWidgetHostView = createClockWidgetHostView,
                clockWidgetWidthDp = effectiveClockWidgetWidthDp,
                clockWidgetHeightDp = effectiveClockWidgetHeightDp,
                // The raw incoming (not live-drag-adjusted) size — only changes once a commit
                // round-trips through Room, unlike effectiveClockWidgetWidthDp/HeightDp above.
                // See ClockWidgetTile's own doc for why the real provider push must not track
                // every drag frame.
                clockWidgetCommittedWidthDp = clockWidgetWidthDp,
                clockWidgetCommittedHeightDp = clockWidgetHeightDp,
                onClockWidgetSizeChange = onClockWidgetSizeChange,
                onClockWidgetLongPress = { onAdjustModeChange(ClockAdjustMode.MENU) },
                onClockWidgetNestedScrollStart = onClockWidgetNestedScrollStart,
                onClockWidgetLeftoverScroll = onClockWidgetLeftoverScroll,
                onClockWidgetNestedScrollStop = onClockWidgetNestedScrollStop,
                onClockWidgetLeftoverFling = onClockWidgetLeftoverFling,
            )
        }
        val mediaRow: @Composable (MediaPlayerState, Modifier) -> Unit = { mediaState, rowModifier ->
            MediaPlayerRow(
                state = mediaState,
                showSkipButtons = showMediaSkipButtons,
                onPrevious = onMediaPrevious,
                onPlayPause = onMediaPlayPause,
                onNext = onMediaNext,
                onOpen = onMediaOpen,
                labelColor = appLabelColor,
                labelFontWeight = appLabelFontWeight,
                modifier = rowModifier,
            )
        }
        val itemActions = actions.item
        val itemStyle = remember(badgeCounts, notificationBadgeStyle, appMenuNotifications, folderCandidates, drawerPresentation, appLabelColor, appLabelFontWeight) {
            HomeItemStyle(
                badgeCounts = badgeCounts,
                badgeStyle = notificationBadgeStyle,
                appMenuNotifications = appMenuNotifications,
                folderCandidates = folderCandidates,
                drawerPresentation = drawerPresentation,
                labelColor = appLabelColor,
                labelFontWeight = appLabelFontWeight,
            )
        }
        val listContent = remember(appListItems, itemStyle) { HomeAppListContent(appListItems, itemStyle) }
        val appListRegion: @Composable (Modifier, Dp) -> Unit = { positionModifier, maxHeight ->
            Column(
                modifier = positionModifier
                    .fillMaxWidth()
                    // Inert (never binds) whenever there's ample room below the handle — only
                    // clips/scrolls in the genuine overflow case, which today has no fallback at all.
                    .heightIn(max = maxHeight)
                    .onGloballyPositioned {
                        layout.appListSize = it.size
                        layout.appListOriginInRoot = it.positionInRoot()
                    }
                    // Grid owns its own internal scroll (LazyVerticalGrid) — wrapping it in a
                    // second verticalScroll would double up the scroll gesture. Single-
                    // column/two-column still use this Column's own scroll, same as today.
                    .let { if (appListLayout == AppListLayout.GRID) it else it.verticalScroll(rememberScrollState()) }
                    .testTag("home_app_list_scroll_region"),
            ) {
                // A scrollable Column measures its content at its natural, unconstrained
                // height regardless of the heightIn(max=...) clamp above — wrapping the real
                // content in this inner Column lets the compact-spacing check above read that
                // natural height directly, even while only part of it is visible on screen.
                // graphicsLayer alpha is a draw-phase property only, so this stays fully
                // measured (and the check above keeps working) while invisible pre-reveal.
                Column(
                    modifier = Modifier
                        .onGloballyPositioned { layout.appListNaturalHeightPx = it.size.height.toFloat() }
                        .graphicsLayer { alpha = appListAppearAlpha.value },
                ) {
                    if (showUsageAccessPrompt) {
                        UsageAccessStrip(onClick = onUsageAccessPromptClick)
                    } else {
                        val rowPadding = if (layout.useCompactAppSpacing) HOME_APP_ROW_COMPACT_VERTICAL_PADDING else HOME_APP_ROW_REGULAR_VERTICAL_PADDING
                        val rowIconSize = if (layout.useCompactAppSpacing) AppIconSize.ROW_COMPACT else AppIconSize.ROW_REGULAR
                        when (appListLayout) {
                            AppListLayout.TWO_COLUMN -> HomeAppTwoColumnList(
                                content = listContent,
                                actions = itemActions,
                                columnAlignment = appListColumnAlignment,
                                appRowPresentation = appRowPresentation,
                                verticalPadding = rowPadding,
                                iconSize = rowIconSize,
                            )
                            AppListLayout.GRID -> HomeAppGrid(
                                content = listContent,
                                actions = itemActions,
                                columns = if (isLandscape) {
                                    landscapeGridColumns(appListGridColumns.columns, with(density) { layout.appsPaneWidthPx.toDp() }, landscapeSpace)
                                } else {
                                    appListGridColumns.columns
                                },
                                displayMode = appListGridDisplayMode,
                            )
                            AppListLayout.SINGLE_COLUMN, AppListLayout.LAUNCHER_DEFAULT -> HomeAppListColumn(
                                content = listContent,
                                actions = itemActions,
                                position = appRowPosition,
                                appRowPresentation = appRowPresentation,
                                verticalPadding = rowPadding,
                                iconSize = rowIconSize,
                                modifier = Modifier.fillMaxWidth(),
                                rowWidthFraction = SINGLE_COLUMN_ROW_WIDTH_FRACTION,
                                horizontalAlignment = when (appRowPosition) {
                                    AppRowPosition.RIGHT -> Alignment.End
                                    AppRowPosition.CENTER -> Alignment.CenterHorizontally
                                    AppRowPosition.LEFT, AppRowPosition.LAUNCHER_DEFAULT -> Alignment.Start
                                },
                            )
                        }
                    }
                }
            }
        }
        val dockIcon: @Composable (PlacedItem, DockDisplayMode) -> Unit = { item, mode ->
            DockIcon(item = item, displayMode = mode, actions = itemActions, style = itemStyle)
        }

        if (isLandscape) {
            HomeLandscapeLayout(
                hasDock = dockApps.isNotEmpty(),
                dockAtBottom = landscapeDockAtBottom,
                onDockRowHeightChange = { layout.landscapeDockRowHeightPx = it.toFloat() },
                clockPaneAlignment = when (clockAlignment) {
                    ClockAlignment.LEFT -> Alignment.TopStart
                    ClockAlignment.CENTER -> Alignment.CenterStart
                    ClockAlignment.RIGHT -> Alignment.BottomStart
                },
                clockPaneWidth = with(density) { landscapePaneWidthPx.toDp() },
                modifier = Modifier
                    .windowInsetsPadding(WindowInsets.systemBars)
                    .padding(top = HOME_LANDSCAPE_VERTICAL_PADDING_TOP, bottom = HOME_LANDSCAPE_VERTICAL_PADDING_BOTTOM)
                    .onSizeChanged { layout.landscapeContentWidthPx = it.width.toFloat() },
                onAppsPaneSizeChange = {
                    layout.contentHeightPx = it.height.toFloat()
                    layout.appsPaneWidthPx = it.width.toFloat()
                },
                clock = {
                    clockBlock(Modifier.graphicsLayer { alpha = clockAppearAlpha.value }, ClockAlignment.LEFT, landscapeOriginY)
                },
                apps = {
                    // The media row is pinned to the top of the pane whatever the list's alignment; the
                    // list sits below it (top-aligned) or at the bottom, in the height the row leaves.
                    // Its height is kept across hides and animated in, as in portrait, so the list doesn't jump.
                    val mediaReservedPx = layout.mediaRowHeightPx * mediaFraction
                    if (shownMediaPlayer != null && mediaFraction > 0f) {
                        mediaRow(
                            shownMediaPlayer,
                            Modifier
                                .align(Alignment.TopStart)
                                .onSizeChanged { if (it.height > 0) layout.mediaRowHeightPx = it.height.toFloat() }
                                .padding(bottom = HOME_MEDIA_ROW_BOTTOM_GAP)
                                .graphicsLayer { alpha = mediaFraction },
                        )
                    }
                    appListRegion(
                        if (appListVerticalAlignment == AppListVerticalAlignment.TOP) {
                            Modifier.align(Alignment.TopStart).offset { IntOffset(0, mediaReservedPx.roundToInt()) }
                        } else {
                            Modifier.align(Alignment.BottomStart)
                        },
                        with(density) { (layout.contentHeightPx - mediaReservedPx).coerceAtLeast(0f).toDp() },
                    )
                },
                dock = {
                    // The side column is icons only; the roomy bottom row follows the user's dock style, as portrait does.
                    val landscapeDockMode = if (landscapeDockAtBottom) dockDisplayMode else DockDisplayMode.ICONS
                    dockApps.forEach { item -> dockIcon(item, landscapeDockMode) }
                },
            )
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    // Edge-to-edge is enforced unconditionally at this app's targetSdk (36) — without
                    // this the dock at the bottom draws under the gesture/nav bar. On the content
                    // Column, not the outer Box, so the Box stays truly full-screen for the resize
                    // handles' root-coordinate math.
                    .windowInsetsPadding(WindowInsets.systemBars)
                    .padding(bottom = 22.dp)
            ) {
                // Everything but the dock keeps the screen's own 24dp side margin. The dock row is
                // deliberately outside this padding (see below) so Arrangement.SpaceEvenly can space its
                // icons against the true screen edge, not this inset.
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp)
                        .onSizeChanged { layout.contentHeightPx = it.height.toFloat() },
                ) {
                    clockBlock(
                        Modifier
                            .offset { IntOffset(0, (handlePx - minGapPx - layout.clockNaturalHeightPx).roundToInt()) }
                            .onGloballyPositioned {
                                layout.clockNaturalHeightPx = it.size.height.toFloat()
                                layout.clockBlockBottomInRoot = it.positionInRoot().y + it.size.height
                            }
                            .graphicsLayer { alpha = clockAppearAlpha.value },
                        clockAlignment,
                        1f,
                    )

                    // Not composed in clock adjust mode (the handles own that space); mediaRowHeightPx
                    // is kept, so the list doesn't move.
                    if (shownMediaPlayer != null && mediaFraction > 0f && clockAdjustMode == ClockAdjustMode.NONE) {
                        mediaRow(
                            shownMediaPlayer,
                            Modifier
                                .align(Alignment.TopStart)
                                .offset { IntOffset(0, mediaRowTopPx.roundToInt()) }
                                .onSizeChanged { if (it.height > 0) layout.mediaRowHeightPx = it.height.toFloat() }
                                .graphicsLayer { alpha = mediaFraction },
                        )
                    }

                    appListRegion(
                        Modifier.let {
                            if (appListVerticalAlignment == AppListVerticalAlignment.TOP) {
                                it.align(Alignment.TopStart).offset { IntOffset(0, appListTopPx.roundToInt()) }
                            } else {
                                // Today's unchanged behavior — hugs the bottom of the content area.
                                it.align(Alignment.BottomStart)
                            }
                        },
                        with(density) { (layout.contentHeightPx - appListTopPx).coerceAtLeast(0f).toDp() },
                    )
                }

                // Fixed gap between the app list and whatever follows — the dock, or (when the
                // dock is empty, see MIN_APPS=0) the screen's own bottom padding.
                Spacer(modifier = Modifier.height(16.dp))

                if (dockApps.isNotEmpty()) {
                    // Full screen width, no horizontal inset — SpaceEvenly then puts an equal gap before
                    // the first icon, between every pair of icons, and after the last, so dock icons sit
                    // at the same distance from each other as from the screen edges.
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 12.dp, bottom = 4.dp)
                            .testTag("home_dock_row"),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                    ) {
                        dockApps.forEach { item ->
                            dockIcon(item, dockDisplayMode)
                        }
                    }
                }
            }
        }

        val adjustEnv = ClockAdjustEnv(layout, geometry, metrics, state.clock, state.widget, actions, Offset(clockWidgetMinWidthPx, clockWidgetMinHeightPx), landscapeDockReserve)
        ClockAdjustOverlays(adjustEnv, landscape = isLandscape)
        ClockAdjustPills(adjustEnv, adjusting = clockAdjustMode == ClockAdjustMode.ADJUST, landscape = isLandscape)

        // Reports the hosted widget's current bounds so HomeDrawerRoute's own swipe-to-open-drawer
        // gesture can exclude them — see onClockWidgetBoundsChange's own doc. null whenever no
        // widget is active (the native clock has no independent scrollable content to protect, so
        // a swipe starting on it keeps opening the drawer/shade exactly as it does today) or its
        // box hasn't been measured yet.
        SideEffect {
            val size = layout.clockBoxSize
            val origin = layout.clockBoxOriginInRoot
            val rootOrigin = layout.rootOriginInRoot
            val boundsReady = size != null && origin != null && rootOrigin != null
            onClockWidgetBoundsChange(
                if (clockWidgetAppWidgetId != null && boundsReady) {
                    Rect(offset = origin - rootOrigin, size = size.toSize())
                } else {
                    null
                },
            )
        }

        // Reports the app list's own scrollable viewport bounds — see onAppListBoundsChange's own
        // doc. null whenever it hasn't been measured yet. A snapshotFlow, not a SideEffect: nothing in
        // composition reads appListSize/appListOriginInRoot (onGloballyPositioned writes them after
        // layout), so a SideEffect only ever saw what they held at the last unrelated recomposition —
        // e.g. the empty list's zero height — and a drag inside the populated, overflowing list was
        // claimed by the drawer instead of scrolling it.
        val currentOnAppListBoundsChange by rememberUpdatedState(onAppListBoundsChange)
        LaunchedEffect(Unit) {
            snapshotFlow {
                val size = layout.appListSize
                val origin = layout.appListOriginInRoot
                val rootOrigin = layout.rootOriginInRoot
                if (size != null && origin != null && rootOrigin != null) {
                    Rect(offset = origin - rootOrigin, size = size.toSize())
                } else {
                    null
                }
            }.collect { currentOnAppListBoundsChange(it) }
        }

        // Adjustment menu sheet
        if (clockAdjustMode == ClockAdjustMode.MENU) {
            ThemedModalBottomSheet(
                onDismissRequest = { onAdjustModeChange(ClockAdjustMode.NONE) },
                skipPartiallyExpanded = true,
                containerColor = SurfaceContainer,
            ) {
                ClockAdjustSheet(
                    onAdjustClick = { onAdjustModeChange(ClockAdjustMode.ADJUST) },
                    onEditStylesClick = onEditClockStyles,
                    onFacetSettingsClick = {
                        onAdjustModeChange(ClockAdjustMode.NONE)
                        onNavigateToFacetSettings()
                    },
                    onLauncherSettingsClick = {
                        onAdjustModeChange(ClockAdjustMode.NONE)
                        onNavigateToSettings()
                    },
                    onUseCustomWidgetClick = {
                        onAdjustModeChange(ClockAdjustMode.NONE)
                        onUseCustomWidgetClick()
                    },
                    onSwitchToLauncherClockClick = {
                        onAdjustModeChange(ClockAdjustMode.NONE)
                        onSwitchToLauncherClockClick()
                    },
                    overrideFacetName = clockPositionOwnerFacetName,
                    hasCustomClockWidget = hasCustomClockWidget,
                    isPro = isPro,
                )
            }
        }
    }
}

/** README `4p`'s shared permission-denied/empty-state strip styling, with a real tap target — shown when the active list content mode needs `PACKAGE_USAGE_STATS` and it isn't granted. */
@Composable
private fun UsageAccessStrip(onClick: () -> Unit, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .testTag("home_usage_access_strip")
            .dashedBorder(color = Ink.copy(alpha = 0.16f), cornerRadius = 10.dp)
            .padding(horizontal = 13.dp, vertical = 11.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = stringResource(R.string.home_usage_access_needed),
            style = MaterialTheme.typography.bodyMedium,
            color = Muted,
            modifier = Modifier.weight(1f),
        )
        Text(text = stringResource(R.string.open_settings), style = MaterialTheme.typography.bodyMedium, color = Accent)
    }
}

/**
 * The clock's original, unchanged top inset — reused as the anchor the default
 * (`clockZoneHeightDp == null`) case resolves to. Not private: reused by
 * [com.facetlauncher.app.ui.facets.FacetCarouselScreen]'s own preview card so it reproduces
 * the same zone-height positioning at its own (scaled-down) size, rather than ignoring
 * `clockZoneHeightDp` entirely.
 */
internal val HOME_CLOCK_DEFAULT_TOP_OFFSET = 52.dp

/** Minimum gap always kept between the clock+calendar block's bottom edge and its grab handle. Not private — see [HOME_CLOCK_DEFAULT_TOP_OFFSET]'s own doc. */
internal val HOME_CLOCK_MIN_GAP = 24.dp

/** Gap between the clock block's bottom edge and the media row, and between the media row and the app list below it. */
internal val HOME_MEDIA_ROW_TOP_GAP = 8.dp
internal val HOME_MEDIA_ROW_BOTTOM_GAP = 12.dp

/** Visible gap between the height handle's touch strip and the alignment toolbar below it — Material's minimum between touch targets. */
internal val CLOCK_ADJUST_TOOLBAR_GAP = 8.dp

/** How long the toolbar stays dimmed and disabled after a drag ends — see its use in [HomeScreen]. */
internal const val CLOCK_ADJUST_TOOLBAR_REAPPEAR_DELAY_MS = 200L

/** The toolbar's opacity while a drag is active or just ended — a visible ghost, not gone. */
internal const val CLOCK_ADJUST_TOOLBAR_DIMMED_ALPHA = 0.25f



/** Half the resize handle's hit box — the amount it extends past the clock corner it sits on. */
internal val HOME_CLOCK_RESIZE_HANDLE_INSET = 20.dp

/** Shared timing for this file's "wait for layout geometry to settle, then decide once" checks (the clock's own re-clamp and the app list's compact-spacing check) — long enough to skip transient double-measurement, bounded so a check that never gets valid geometry doesn't hang. */
private const val GEOMETRY_SETTLE_DEBOUNCE_MS = 150L
private const val GEOMETRY_SETTLE_TIMEOUT_MS = 3_000L

/** [AppRow]'s regular per-row vertical padding — Home's own default density. See [HOME_APP_ROW_COMPACT_VERTICAL_PADDING]. */
/** Single-column rows are only this fraction of the list's width (tap target + ripple), anchored by the row position. */
private const val SINGLE_COLUMN_ROW_WIDTH_FRACTION = 0.8f

internal val HOME_APP_ROW_REGULAR_VERTICAL_PADDING = 16.dp

/**
 * [AppRow]'s tighter per-row vertical padding, switched to automatically (see this file's
 * one-shot compact-spacing check) when the regular density would make the app list need to
 * scroll. Matches App Drawer's own `DrawerListItemSize.COMPACT` total row padding (base 8dp +
 * 0 extra), so Home's compact density reads the same as Drawer's.
 */
internal val HOME_APP_ROW_COMPACT_VERTICAL_PADDING = 8.dp

/** M3 emphasized-decelerate easing (`cubic-bezier(.05,.7,.1,1)`) — an element coming to rest as it enters. */
private val EmphasizedDecelerateEasing = CubicBezierEasing(0.05f, 0.7f, 0.1f, 1f)

@Preview(showBackground = true, widthDp = 390, heightDp = 844)
@Preview(name = "Dark", showBackground = true, widthDp = 390, heightDp = 844, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun HomeScreenPreview() {
    FacetLauncherTheme {
        val apps = (1..9).map {
            AppInfo(packageName = "com.example.app$it", activityName = ".MainActivity", label = "App $it", icon = null)
        }
        HomeScreen(
            state = HomeScreenState(
                list = HomeListState(items = apps.take(5).map { PlacedItem.SingleApp(it) }),
                dock = HomeDockState(items = apps.drop(5).take(4).map { PlacedItem.SingleApp(it) }),
            ),
            actions = HomeScreenActions(item = HomeItemActions(onAppClick = {})),
        )
    }
}

/** Position = Right: rows hug the right edge, label-then-icon. */
@Preview(name = "Right position", showBackground = true, widthDp = 390, heightDp = 844)
@Preview(
    name = "Right position - Dark",
    showBackground = true,
    widthDp = 390,
    heightDp = 844,
    uiMode = Configuration.UI_MODE_NIGHT_YES,
)
@Composable
private fun HomeScreenRightPositionPreview() {
    FacetLauncherTheme {
        val apps = (1..5).map {
            AppInfo(packageName = "com.example.app$it", activityName = ".MainActivity", label = "App $it", icon = null)
        }
        HomeScreen(
            state = HomeScreenState(
                list = HomeListState(
                    items = apps.map { PlacedItem.SingleApp(it) },
                    alignment = ListAlignment(AppRowPosition.RIGHT, AppListVerticalAlignment.BOTTOM, AppListColumnAlignment.BOTH_LEFT),
                ),
                dock = HomeDockState(items = emptyList()),
            ),
            actions = HomeScreenActions(item = HomeItemActions(onAppClick = {})),
        )
    }
}

/** Presentation = Text Only: no icons in the app list. */
@Preview(name = "Text only presentation", showBackground = true, widthDp = 390, heightDp = 844)
@Composable
private fun HomeScreenTextOnlyPresentationPreview() {
    FacetLauncherTheme {
        val apps = (1..5).map {
            AppInfo(packageName = "com.example.app$it", activityName = ".MainActivity", label = "App $it", icon = null)
        }
        HomeScreen(
            state = HomeScreenState(
                list = HomeListState(items = apps.map { PlacedItem.SingleApp(it) }, rowPresentation = AppRowPresentation.TEXT_ONLY),
                dock = HomeDockState(items = emptyList()),
            ),
            actions = HomeScreenActions(item = HomeItemActions(onAppClick = {})),
        )
    }
}
