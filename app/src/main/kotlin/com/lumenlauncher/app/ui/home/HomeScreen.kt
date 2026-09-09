package com.lumenlauncher.app.ui.home

import android.content.res.Configuration
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.layout.positionInRoot
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import com.lumenlauncher.app.data.model.AppInfo
import com.lumenlauncher.app.data.model.AppListVerticalAlignment
import com.lumenlauncher.app.data.model.AppRowPosition
import com.lumenlauncher.app.data.model.AppRowPresentation
import com.lumenlauncher.app.data.model.AppShortcut
import com.lumenlauncher.app.data.model.CalendarEvent
import com.lumenlauncher.app.data.model.ClockAlignment
import com.lumenlauncher.app.data.model.ClockColorOption
import com.lumenlauncher.app.data.model.ClockDateStyle
import com.lumenlauncher.app.data.model.ClockFontOption
import com.lumenlauncher.app.data.model.ClockTemplateId
import com.lumenlauncher.app.data.model.DockDisplayMode
import com.lumenlauncher.app.data.model.FontWeightOption
import com.lumenlauncher.app.data.model.LauncherFontOption
import com.lumenlauncher.app.data.model.ListContentMode
import com.lumenlauncher.app.data.model.NotificationBadgeStyle
import com.lumenlauncher.app.ui.components.AppContextMenu
import com.lumenlauncher.app.ui.components.AppIcon
import com.lumenlauncher.app.ui.components.NotificationBadge
import com.lumenlauncher.app.ui.theme.Accent
import com.lumenlauncher.app.ui.theme.HomeAppTextColor
import com.lumenlauncher.app.ui.theme.Ink
import com.lumenlauncher.app.ui.theme.LumenLauncherTheme
import com.lumenlauncher.app.ui.theme.Muted
import com.lumenlauncher.app.ui.theme.Scrim
import com.lumenlauncher.app.ui.theme.homeAppLabelShadow
import com.lumenlauncher.app.ui.theme.resolve
import kotlin.math.abs
import kotlin.math.roundToInt
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withTimeoutOrNull

/** [MENU] shows the long-press bottom sheet; [ADJUST] shows the move handle *and* the resize handles together. */
enum class ClockAdjustMode { NONE, MENU, ADJUST }

/**
 * Home surface (`1a`, Airy density `1e`): clock + a short curated app list + dock. Both
 * [appListItems] (Favorites/Recents/Most Used depending on [listContentMode], from
 * [com.lumenlauncher.app.data.FavoriteAppRepository] or
 * [com.lumenlauncher.app.data.UsageStatsRepository]) and [dockApps] (from
 * [com.lumenlauncher.app.data.DockAppRepository]) are real persisted state — this composable
 * only renders, it never slices/computes them itself.
 */
@Composable
fun HomeScreen(
    appListItems: List<AppInfo>,
    dockApps: List<AppInfo>,
    onAppClick: (AppInfo) -> Unit,
    modifier: Modifier = Modifier,
    listContentMode: ListContentMode = ListContentMode.FAVORITES,
    appRowPosition: AppRowPosition = AppRowPosition.LEFT,
    appRowPresentation: AppRowPresentation = AppRowPresentation.ICON_AND_TEXT,
    showUsageAccessPrompt: Boolean = false,
    onUsageAccessPromptClick: () -> Unit = {},
    dockDisplayMode: DockDisplayMode = DockDisplayMode.ICONS,
    notificationBadgeStyle: NotificationBadgeStyle = NotificationBadgeStyle.DOT,
    badgeCounts: Map<String, Int> = emptyMap(),
    use24HourTime: Boolean = false,
    clockTemplateId: ClockTemplateId = ClockTemplateId.LIGHT_STACK,
    clockFontOption: ClockFontOption = ClockFontOption.SYSTEM,
    clockColorOption: ClockColorOption = ClockColorOption.THEME,
    clockAccentColorOption: ClockColorOption = ClockColorOption.ACCENT_PRIMARY,
    clockShowMeridiem: Boolean = false,
    clockDateStyle: ClockDateStyle = ClockDateStyle.FULL,
    clockAlignment: ClockAlignment = ClockAlignment.LEFT,
    /** Independent of [clockAlignment] — positions the calendar events strip separately from the clock. */
    calendarAlignment: ClockAlignment = ClockAlignment.LEFT,
    /** Home clock's scale factor — see [com.lumenlauncher.app.data.model.LauncherSettings.clockScale]. */
    clockScale: Float = 0.8f,
    /** Current adjustment mode — hoisted to the caller (e.g. [com.lumenlauncher.app.ui.launcher.HomeDrawerRoute]) so screen gestures can be coordinated. */
    clockAdjustMode: ClockAdjustMode = ClockAdjustMode.NONE,
    /** `null` until the user drags the clock's grab handle for the first time — see [ClockZoneHandle] and this composable's own body for how the default and persisted cases resolve to one shared position formula. */
    clockZoneHeightDp: Float? = null,
    /** Fired once, on release, by the clock's grab handle. */
    onClockZoneHeightCommit: (Float) -> Unit = {},
    /** Fired once, on release, by the clock's resize handles. */
    onClockScaleCommit: (Float) -> Unit = { _ -> },
    /** Fired whenever the adjustment mode changes (e.g. via the bottom sheet or tap-away). */
    onAdjustModeChange: (ClockAdjustMode) -> Unit = {},
    /** True when a handle is actively being dragged — used to suppress screen gestures. */
    draggingHandle: Boolean = false,
    /** Fired when the user starts or stops actively dragging one of the adjustment handles. */
    onDraggingHandleChange: (Boolean) -> Unit = {},
    /** The profile whose `overrideClock` bundle actually governs the clock widget's live position right now, or `null` if the global default applies. */
    clockPositionOwningProfile: com.lumenlauncher.app.data.local.ProfileEntity? = null,
    /** Fired by a plain tap on the clock (time/date), not a calendar event row — opens the device's default clock app. */
    onClockClick: () -> Unit = {},
    /** Fired when the user selects "Edit Styles" from the clock's adjustment menu. */
    onEditClockStyles: () -> Unit = {},
    appListVerticalAlignment: AppListVerticalAlignment = AppListVerticalAlignment.BOTTOM,
    calendarEvents: List<CalendarEvent> = emptyList(),
    calendarColors: Map<String, String> = emptyMap(),
    calendarFontOption: ClockFontOption = ClockFontOption.SYSTEM,
    calendarColorOption: ClockColorOption = ClockColorOption.THEME,
    calendarFontWeight: FontWeightOption = FontWeightOption.REGULAR,
    onEventClick: (CalendarEvent) -> Unit = {},
    onRequestShortcuts: suspend (AppInfo) -> List<AppShortcut> = { emptyList() },
    onLaunchShortcut: (AppShortcut) -> Unit = {},
    launcherFontOption: LauncherFontOption = LauncherFontOption.SYSTEM,
    appLabelColorOption: ClockColorOption = ClockColorOption.THEME,
    homeAppsFontWeight: FontWeightOption = FontWeightOption.REGULAR,
) {
    val appLabelColor = appLabelColorOption.resolve()
    val appLabelFontWeight = homeAppsFontWeight.resolve()
    val density = LocalDensity.current
    // Measured once per layout pass — contentHeightPx is the content Box's own height;
    // clockNaturalHeightPx is the clock+calendar block's intrinsic height (offset-independent, so
    // this is a stable one-frame-lag measurement, same pattern as e.g. ui/components/HubGrid.kt).
    var contentHeightPx by remember { mutableFloatStateOf(0f) }
    var clockNaturalHeightPx by remember { mutableFloatStateOf(0f) }
    // Live delta accumulated for the drag gesture in progress only — added to the persisted
    // clockZoneHeightDp for the live preview position, then zeroed on commit (mirrors
    // ui/components/DragReorderState's own onOrderChanged/onDragCommit split: cheap local state
    // during the drag, one persisting call on release).
    var liveDragDeltaPx by remember { mutableFloatStateOf(0f) }

    // The clock's live scale while resizing, as an absolute value (not a delta — a delta briefly
    // double-counts against the committed clockScale in the frame the commit lands). Held past the
    // drag's end until clockScale round-trips back through the repo/StateFlow, then released so any
    // external change to clockScale (reset, profile switch) takes effect.
    var liveScale by remember { mutableStateOf<Float?>(null) }
    LaunchedEffect(clockScale) { liveScale = null }

    // Stored as primitives, not the LayoutCoordinates object: onGloballyPositioned reuses one
    // instance across layout passes and only mutates its size/position, so `state = it` would
    // structurally dedupe to a no-op and never recompose when the clock box changed size (e.g. on
    // a template switch). IntSize/Offset are value types with real equals.
    var rootSize by remember { mutableStateOf<IntSize?>(null) }
    var rootOriginInRoot by remember { mutableStateOf<Offset?>(null) }
    var clockBoxSize by remember { mutableStateOf<IntSize?>(null) }
    var clockBoxOriginInRoot by remember { mutableStateOf<Offset?>(null) }

    // NaN until the first drag event captures the offset between where the finger grabbed and the
    // scale that point implies, so the clock doesn't pop on the first move.
    var dragGrabOffset by remember { mutableFloatStateOf(Float.NaN) }

    val defaultTopOffsetPx = with(density) { HOME_CLOCK_DEFAULT_TOP_OFFSET.toPx() }
    val minGapPx = with(density) { HOME_CLOCK_MIN_GAP.toPx() }
    val topInsetPx = WindowInsets.systemBars.getTop(density).toFloat()

    // One formula covers both the persisted and default (null) cases: when clockZoneHeightDp is
    // null, this evaluates to "the default top offset + the clock's natural height + the minimum
    // gap" — which places the handle exactly where it needs to be for the block's own offset
    // formula below to resolve to its original, untouched top-offset position. No separate
    // rendering branch is needed for the default state.
    val basePersistedHandlePx = clockZoneHeightDp?.let { with(density) { it.dp.toPx() } }
        ?: (defaultTopOffsetPx + clockNaturalHeightPx + minGapPx)
    val floorPx = clockNaturalHeightPx + minGapPx
    // maxOf guards against contentHeightPx still being 0f before the first layout pass, which
    // would otherwise make the ceiling fall below the floor and crash coerceIn.
    val ceilingPx = maxOf(floorPx, HOME_CLOCK_ZONE_MAX_FRACTION * contentHeightPx)
    val handlePx = (basePersistedHandlePx + liveDragDeltaPx).coerceIn(floorPx, ceilingPx)

    // The clock renders at its persisted [clockScale] and nothing recomputes it frame to frame.
    // The largest scale that still fits — headroom above the pinned bottom (past the status bar,
    // plus room for the resize handle to clear the notification-shade strip) and room to the sides
    // — is only needed while dragging a handle, and once when the style/position changes (below).
    val clockTopReservePx = topInsetPx + with(density) { (HOME_CLOCK_RESIZE_HANDLE_INSET + 12.dp).toPx() }
    val sideMarginPx = with(density) { 24.dp.toPx() }
    val maxScaleThatFits: (natSize: IntSize, natTopLeft: Offset, rootW: Int) -> Float = { natSize, natTopLeft, rootW ->
        val natW = natSize.width.toFloat()
        val natH = natSize.height.toFloat()
        if (natW <= 0f || natH <= 0f) {
            HOME_CLOCK_MAX_SCALE
        } else {
            val byHeight = (natTopLeft.y + natH - clockTopReservePx) / natH
            val byWidth = when (clockAlignment) {
                ClockAlignment.CENTER -> (rootW - 2f * sideMarginPx) / natW
                ClockAlignment.LEFT -> (rootW - sideMarginPx - natTopLeft.x) / natW
                ClockAlignment.RIGHT -> (natTopLeft.x + natW - sideMarginPx) / natW
            }
            minOf(HOME_CLOCK_MAX_SCALE, byHeight, byWidth).coerceIn(HOME_CLOCK_MIN_SCALE, HOME_CLOCK_MAX_SCALE)
        }
    }

    // One-shot re-clamp: a style or position change resizes the clock's natural box, so a scale
    // that fit the old look can now overflow. After the new geometry settles, check once — and
    // only persist a smaller value if it genuinely clips. Never runs otherwise.
    LaunchedEffect(clockTemplateId, clockAlignment, clockDateStyle, clockZoneHeightDp, clockPositionOwningProfile?.id) {
        withTimeoutOrNull(3_000) {
            snapshotFlow { Triple(clockBoxSize, clockBoxOriginInRoot, rootSize) }
                .filter { it.first != null && it.second != null && it.third != null }
                .debounce(150)
                .first()
        }
        val size = clockBoxSize ?: return@LaunchedEffect
        val origin = clockBoxOriginInRoot ?: return@LaunchedEffect
        val rootW = rootSize?.width ?: return@LaunchedEffect
        val fit = maxScaleThatFits(size, origin - (rootOriginInRoot ?: Offset.Zero), rootW)
        if (clockScale > fit + 0.01f) onClockScaleCommit(fit)
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
            snapshotFlow { clockBoxSize }.filter { it != null }.debounce(120).first()
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

    // The scale the clock actually renders at. Normally just the persisted value (constant, no
    // geometry read). While ADJUST mode is live it's also clamped to whatever currently fits —
    // reading the clock's live position — so dragging the move handle upward shrinks the clock in
    // real time instead of letting it clip.
    val effectiveClockScale = run {
        val raw = liveScale ?: clockScale
        val size = clockBoxSize
        val origin = clockBoxOriginInRoot
        val rootW = rootSize?.width
        if (clockAdjustMode == ClockAdjustMode.ADJUST && size != null && origin != null && rootW != null) {
            raw.coerceIn(HOME_CLOCK_MIN_SCALE, maxScaleThatFits(size, origin - (rootOriginInRoot ?: Offset.Zero), rootW))
        } else {
            raw.coerceIn(HOME_CLOCK_MIN_SCALE, HOME_CLOCK_MAX_SCALE)
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .onGloballyPositioned { rootSize = it.size; rootOriginInRoot = it.positionInRoot() }
            // Dismisses drag mode on a tap anywhere else on the screen. Attached to this outer Box
            // (an ancestor of every child, including the handle below) rather than a separate
            // full-size sibling Box — Compose dispatches pointer events child-before-parent, so the
            // handle's own drag detector (and any other child's gesture/clickable) always gets
            // first claim on a touch landing on it; only an up event that reaches this ancestor
            // unconsumed counts as "away from" everything else. A sibling overlay occupying the
            // same bounds as the handle would instead receive the same events independently and
            // could interfere with the handle's own gesture recognition (see chat history).
            .then(
                if (clockAdjustMode == ClockAdjustMode.ADJUST) {
                    Modifier.pointerInput(Unit) { detectTapGestures(onTap = { onAdjustModeChange(ClockAdjustMode.NONE) }) }
                } else {
                    Modifier
                }
            ),
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
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
                    .onSizeChanged { contentHeightPx = it.height.toFloat() },
            ) {
                ClockBlock(
                    modifier = Modifier
                        .offset { IntOffset(0, (handlePx - minGapPx - clockNaturalHeightPx).roundToInt()) }
                        .onGloballyPositioned { clockNaturalHeightPx = it.size.height.toFloat() }
                        .graphicsLayer { alpha = clockAppearAlpha.value },
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
                    // long-press (open the profile carousel) from ever firing there (see chat
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
                        clockBoxSize = it.size
                        clockBoxOriginInRoot = it.positionInRoot()
                    },
                    use24HourTime = use24HourTime,
                    templateId = clockTemplateId,
                    fontOption = clockFontOption,
                    colorOption = clockColorOption,
                    accentColorOption = clockAccentColorOption,
                    showMeridiem = clockShowMeridiem,
                    dateStyle = clockDateStyle,
                    clockAlignment = clockAlignment,
                    calendarAlignment = calendarAlignment,
                    clockScale = effectiveClockScale,
                    events = calendarEvents,
                    calendarColors = calendarColors,
                    calendarFontOption = calendarFontOption,
                    calendarColorOption = calendarColorOption,
                    calendarFontWeight = calendarFontWeight,
                    onEventClick = onEventClick,
                    launcherFontOption = launcherFontOption,
                )

                Column(
                    modifier = Modifier
                        .let {
                            if (appListVerticalAlignment == AppListVerticalAlignment.TOP) {
                                it.align(Alignment.TopStart).offset { IntOffset(0, handlePx.roundToInt()) }
                            } else {
                                // Today's unchanged behavior — hugs the bottom of the content area.
                                it.align(Alignment.BottomStart)
                            }
                        }
                        .fillMaxWidth()
                        // Inert (never binds) whenever there's ample room below the handle — only
                        // clips/scrolls in the genuine overflow case, which today has no fallback at all.
                        .heightIn(max = with(density) { (contentHeightPx - handlePx).coerceAtLeast(0f).toDp() })
                        .verticalScroll(rememberScrollState())
                        .testTag("home_app_list_scroll_region"),
                ) {
                    if (appListItems.isNotEmpty()) {
                        val listLabelColor = Muted
                        Text(
                            text = when (listContentMode) {
                                ListContentMode.FAVORITES -> "FAVORITES"
                                ListContentMode.RECENTS -> "RECENTS"
                                ListContentMode.MOST_USED -> "MOST USED"
                            },
                            style = MaterialTheme.typography.labelSmall.copy(
                                shadow = homeAppLabelShadow(listLabelColor),
                                textAlign = if (appRowPosition == AppRowPosition.RIGHT) TextAlign.End else TextAlign.Start,
                            ),
                            color = listLabelColor,
                            modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
                        )
                    }
                    if (showUsageAccessPrompt) {
                        UsageAccessStrip(onClick = onUsageAccessPromptClick)
                    } else {
                        Column {
                            appListItems.forEach { app ->
                                AppRow(
                                    app = app,
                                    onClick = { onAppClick(app) },
                                    badgeCount = badgeCounts[app.packageName],
                                    badgeStyle = notificationBadgeStyle,
                                    onRequestShortcuts = onRequestShortcuts,
                                    onLaunchShortcut = onLaunchShortcut,
                                    position = appRowPosition,
                                    presentation = appRowPresentation,
                                    labelColor = appLabelColor,
                                    labelFontWeight = appLabelFontWeight,
                                )
                            }
                        }
                    }
                }
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
                    dockApps.forEach { app ->
                        DockIcon(
                            app = app,
                            displayMode = dockDisplayMode,
                            onClick = { onAppClick(app) },
                            badgeCount = badgeCounts[app.packageName],
                            badgeStyle = notificationBadgeStyle,
                            onRequestShortcuts = onRequestShortcuts,
                            onLaunchShortcut = onLaunchShortcut,
                            labelColor = appLabelColor,
                            labelFontWeight = appLabelFontWeight,
                        )
                    }
                }
            }
        }

        if (clockAdjustMode == ClockAdjustMode.ADJUST) {
            // Move handle — always present in adjust mode. On release it also persists the scale if
            // the reposition forced a shrink-to-fit, so it doesn't snap back.
            ClockZoneHandle(
                modifier = Modifier
                    .padding(horizontal = 24.dp)
                    .offset { IntOffset(0, handlePx.roundToInt()) },
                onDragStart = { onDraggingHandleChange(true) },
                onDrag = { deltaDp ->
                    liveDragDeltaPx += with(density) { deltaDp.dp.toPx() }
                },
                onDragEnd = {
                    onDraggingHandleChange(false)
                    onClockZoneHeightCommit(with(density) { handlePx.toDp().value })
                    liveDragDeltaPx = 0f
                    if (effectiveClockScale < clockScale - 0.005f) onClockScaleCommit(effectiveClockScale)
                },
            )
        }

        if (clockAdjustMode == ClockAdjustMode.ADJUST && clockBoxSize != null && clockBoxOriginInRoot != null && rootOriginInRoot != null && rootSize != null) {
            val rootOrigin = rootOriginInRoot!!
            // The clock's natural (unscaled) box in root coords — stable throughout a drag (uniformScale
            // keeps the layout footprint at natural size). Everything below is a pure function of this
            // box and `eff`, so the handles/border stay locked to the glyph with no lag and nothing
            // reflows on release.
            val natW = clockBoxSize!!.width.toFloat()
            val natH = clockBoxSize!!.height.toFloat()
            val natTopLeft = clockBoxOriginInRoot!! - rootOrigin
            val clockMaxScale = maxScaleThatFits(clockBoxSize!!, natTopLeft, rootSize!!.width)
            val eff = effectiveClockScale

            val natBottom = natTopLeft.y + natH
            // The x that stays put as the glyph scales (matches uniformScale's transformOrigin).
            val anchorX = when (clockAlignment) {
                ClockAlignment.LEFT -> natTopLeft.x
                ClockAlignment.RIGHT -> natTopLeft.x + natW
                ClockAlignment.CENTER -> natTopLeft.x + natW / 2f
            }

            val visualWidth = natW * eff
            val visualHeight = natH * eff
            val visualTopLeftY = natBottom - visualHeight
            val visualTopLeftX = when (clockAlignment) {
                ClockAlignment.LEFT -> anchorX
                ClockAlignment.RIGHT -> anchorX - visualWidth
                ClockAlignment.CENTER -> anchorX - visualWidth / 2f
            }

            // Scale a touch point in root coords implies, if that point were the dragged corner.
            val scaleAtTouch: (Offset) -> Float = { t ->
                val sx = when (clockAlignment) {
                    ClockAlignment.LEFT -> (t.x - anchorX) / natW
                    ClockAlignment.RIGHT -> (anchorX - t.x) / natW
                    ClockAlignment.CENTER -> abs(t.x - anchorX) * 2f / natW
                }
                val sy = (natBottom - t.y) / natH
                (sx + sy) / 2f
            }
            val onHandleDragStart: () -> Unit = {
                dragGrabOffset = Float.NaN
                onDraggingHandleChange(true)
            }
            val onHandleDrag: (Offset) -> Unit = { touchInAbsolute ->
                val implied = scaleAtTouch(touchInAbsolute - rootOrigin)
                // First move: capture where the finger sits vs the corner, so the clock doesn't pop.
                if (dragGrabOffset.isNaN()) dragGrabOffset = implied - (liveScale ?: clockScale)
                liveScale = (implied - dragGrabOffset).coerceIn(HOME_CLOCK_MIN_SCALE, clockMaxScale)
            }
            val onHandleDragEnd: () -> Unit = {
                onDraggingHandleChange(false)
                // liveScale is held until LaunchedEffect(clockScale) sees the commit land.
                onClockScaleCommit((liveScale ?: clockScale).coerceIn(HOME_CLOCK_MIN_SCALE, clockMaxScale))
            }

            val handleInsetPx = with(density) { HOME_CLOCK_RESIZE_HANDLE_INSET.toPx() }
            // Safety net for the top handles: never let them sit in the status-bar strip even if
            // clockMaxScale is momentarily stale (e.g. right after a template switch).
            val handleTopPx = (visualTopLeftY - handleInsetPx).coerceAtLeast(topInsetPx)
            Box(
                modifier = Modifier
                    .offset { IntOffset(visualTopLeftX.roundToInt(), visualTopLeftY.roundToInt()) }
                    .size(with(density) { visualWidth.toDp() }, with(density) { visualHeight.toDp() })
                    .border(1.dp, Accent.copy(alpha = 0.5f), RoundedCornerShape(12.dp)),
            )
            ClockCornerHandle(
                isRightSide = false,
                modifier = Modifier.offset {
                    IntOffset((visualTopLeftX - handleInsetPx).roundToInt(), handleTopPx.roundToInt())
                },
                onDragStart = onHandleDragStart,
                onDrag = onHandleDrag,
                onDragEnd = onHandleDragEnd,
            )
            ClockCornerHandle(
                isRightSide = true,
                modifier = Modifier.offset {
                    IntOffset((visualTopLeftX + visualWidth - handleInsetPx).roundToInt(), handleTopPx.roundToInt())
                },
                onDragStart = onHandleDragStart,
                onDrag = onHandleDrag,
                onDragEnd = onHandleDragEnd,
            )
        }

        // Adjustment menu sheet
        AnimatedVisibility(
            visible = clockAdjustMode == ClockAdjustMode.MENU,
            enter = fadeIn(tween(240)),
            exit = fadeOut(tween(240)),
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Scrim)
                    .testTag("clock_adjust_scrim")
                    .clickable { onAdjustModeChange(ClockAdjustMode.NONE) }
            )
        }

        AnimatedVisibility(
            visible = clockAdjustMode == ClockAdjustMode.MENU,
            enter = slideInVertically(
                initialOffsetY = { it },
                animationSpec = tween(340, easing = CubicBezierEasing(0.32f, 0.72f, 0f, 1f))
            ),
            exit = slideOutVertically(
                targetOffsetY = { it },
                animationSpec = tween(340, easing = CubicBezierEasing(0.32f, 0.72f, 0f, 1f))
            ),
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .windowInsetsPadding(WindowInsets.systemBars.only(WindowInsetsSides.Bottom))
        ) {
            ClockAdjustSheet(
                onAdjustClick = { onAdjustModeChange(ClockAdjustMode.ADJUST) },
                onEditStylesClick = onEditClockStyles,
                isOverridden = clockPositionOwningProfile != null,
            )
        }
    }
}

/** README `4p`'s shared permission-denied/empty-state strip styling, with a real tap target — shown when [listContentMode] needs `PACKAGE_USAGE_STATS` and it isn't granted. */
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
            text = "Most used needs usage access from system settings.",
            style = MaterialTheme.typography.bodyMedium,
            color = Muted,
            modifier = Modifier.weight(1f),
        )
        Text(text = "Open settings", style = MaterialTheme.typography.bodyMedium, color = Accent)
    }
}

/**
 * The clock's original, unchanged top inset — reused as the anchor the default
 * (`clockZoneHeightDp == null`) case resolves to. Not private: reused by
 * [com.lumenlauncher.app.ui.profiles.ProfileCarouselScreen]'s own preview card so it reproduces
 * the same zone-height positioning at its own (scaled-down) size, rather than ignoring
 * `clockZoneHeightDp` entirely.
 */
internal val HOME_CLOCK_DEFAULT_TOP_OFFSET = 52.dp

/** Minimum gap always kept between the clock+calendar block's bottom edge and its grab handle. Not private — see [HOME_CLOCK_DEFAULT_TOP_OFFSET]'s own doc. */
internal val HOME_CLOCK_MIN_GAP = 24.dp

/** The grab handle's position (measured from the top of the content area) can never exceed this fraction of the content area's own height. */
private const val HOME_CLOCK_ZONE_MAX_FRACTION = 0.5f

/** Shared clamp for the clock's scale factor — live render, handle positioning, and the committed value all use this pair. */
private const val HOME_CLOCK_MIN_SCALE = 0.5f
private const val HOME_CLOCK_MAX_SCALE = 2.0f

/** Half the resize handle's hit box — the amount it extends past the clock corner it sits on. */
private val HOME_CLOCK_RESIZE_HANDLE_INSET = 20.dp

/** M3 emphasized-decelerate easing (`cubic-bezier(.05,.7,.1,1)`) — an element coming to rest as it enters. */
private val EmphasizedDecelerateEasing = CubicBezierEasing(0.05f, 0.7f, 0.1f, 1f)

private fun Modifier.dashedBorder(color: Color, cornerRadius: Dp, strokeWidth: Dp = 1.dp): Modifier = drawBehind {
    drawRoundRect(
        color = color,
        style = Stroke(width = strokeWidth.toPx(), pathEffect = PathEffect.dashPathEffect(floatArrayOf(8f, 6f), 0f)),
        cornerRadius = CornerRadius(cornerRadius.toPx()),
    )
}

/**
 * [position] governs both internal ordering (icon-then-label vs. label-then-icon) and whether the
 * row's content packs against the start or end of the available width — `RIGHT` needs both, not
 * just a reversed order while staying left-anchored, for the row to actually hug the screen's
 * right edge. [presentation] independently governs which of icon/label actually render; the
 * unused one's slot composable simply emits nothing rather than branching the whole layout.
 *
 * Not private: reused by the profile carousel's preview cards
 * ([com.lumenlauncher.app.ui.profiles.ProfileCarouselScreen]) so the favorites list renders with
 * the same position/presentation/color/weight styling there as it does on the real Home screen —
 * same reasoning as [DockIcon]'s own visibility.
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
internal fun AppRow(
    app: AppInfo,
    onClick: () -> Unit,
    badgeCount: Int?,
    badgeStyle: NotificationBadgeStyle,
    onRequestShortcuts: suspend (AppInfo) -> List<AppShortcut>,
    onLaunchShortcut: (AppShortcut) -> Unit,
    modifier: Modifier = Modifier,
    position: AppRowPosition = AppRowPosition.LEFT,
    presentation: AppRowPresentation = AppRowPresentation.ICON_AND_TEXT,
    labelColor: Color = HomeAppTextColor,
    labelFontWeight: FontWeight = FontWeight.Normal,
    // False for the profile carousel's read-only preview cards (see
    // com.lumenlauncher.app.ui.profiles.ProfileCarouselScreen) — long-press there must not open
    // Home's real Uninstall/App Info/shortcuts menu, since the preview isn't a place you manage apps.
    enableLongPressMenu: Boolean = true,
    // 16dp matches App Drawer's own Regular list-item spacing exactly (base 8dp +
    // DrawerListItemSize.REGULAR's 8dp extraRowPaddingDp), per direct request that Home's app list
    // read as the same density as Drawer's default (see chat history) — that's Home's own real
    // density and stays the default here. A caller rendering this inside a small preview card
    // (the profile carousel, the Appearance screen's own live preview) overrides it tighter, since
    // Home's real row height would read as oversized on a compact card (see chat history).
    verticalPadding: Dp = 16.dp,
) {
    var menuExpanded by remember { mutableStateOf(false) }
    val showIcon = presentation != AppRowPresentation.TEXT_ONLY
    val showLabel = presentation != AppRowPresentation.ICON_ONLY
    Box {
        Row(
            modifier = modifier
                .fillMaxWidth()
                // Large/16dp (MaterialTheme.shapes.large — see CLAUDE.md's shape table, which
                // names this the "navigation drawers" token) clips the ripple/press state to a
                // rounded rect instead of a full-bleed rectangle (see chat history).
                .clip(MaterialTheme.shapes.large)
                .then(
                    if (enableLongPressMenu) {
                        Modifier.combinedClickable(onClick = onClick, onLongClick = { menuExpanded = true })
                    } else {
                        Modifier.clickable(onClick = onClick)
                    }
                )
                .padding(horizontal = 8.dp, vertical = verticalPadding),
            verticalAlignment = Alignment.CenterVertically,
            // RIGHT packs the whole row's content against the trailing edge (not just reversed
            // order while left-anchored) so the row visually hugs the screen's right edge.
            horizontalArrangement = if (position == AppRowPosition.RIGHT) {
                Arrangement.spacedBy(14.dp, Alignment.End)
            } else {
                Arrangement.spacedBy(14.dp)
            },
        ) {
            val icon: @Composable () -> Unit = {
                if (showIcon) {
                    AppIcon(
                        icon = app.icon,
                        size = 34.dp,
                        cornerRadius = 10.dp,
                        // No visible label to carry the a11y name when text is hidden — mirrors
                        // the Dock's own icons-only mode (see DockIcon below).
                        contentDescription = if (showLabel) null else app.label,
                        modifier = Modifier.testTag("home_app_icon_${app.packageName}"),
                    )
                }
            }
            val label: @Composable () -> Unit = {
                if (showLabel) {
                    Text(
                        text = app.label,
                        style = MaterialTheme.typography.titleMedium.copy(shadow = homeAppLabelShadow(labelColor), fontWeight = labelFontWeight),
                        color = labelColor,
                        modifier = Modifier.testTag("home_app_label_${app.packageName}"),
                    )
                }
            }
            val badge: @Composable () -> Unit = {
                if (badgeCount != null && badgeCount > 0) NotificationBadge(count = badgeCount, style = badgeStyle)
            }
            if (position == AppRowPosition.RIGHT) {
                // Badge first, then label (leftmost of the packed content), icon last — landing
                // on the row's true trailing edge since the row itself is now end-packed.
                badge()
                label()
                icon()
            } else {
                icon()
                label()
                badge()
            }
        }
        if (enableLongPressMenu) {
            AppContextMenu(
                app = app,
                expanded = menuExpanded,
                onDismissRequest = { menuExpanded = false },
                onRequestShortcuts = onRequestShortcuts,
                onLaunchShortcut = onLaunchShortcut,
            )
        }
    }
}

/** Not private: reused by the profile carousel's preview cards ([com.lumenlauncher.app.ui.profiles.ProfileCarouselScreen]) so the dock renders identically there. */
@OptIn(ExperimentalFoundationApi::class)
@Composable
internal fun DockIcon(
    app: AppInfo,
    displayMode: DockDisplayMode,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    badgeCount: Int? = null,
    badgeStyle: NotificationBadgeStyle = NotificationBadgeStyle.DOT,
    onRequestShortcuts: suspend (AppInfo) -> List<AppShortcut> = { emptyList() },
    onLaunchShortcut: (AppShortcut) -> Unit = {},
    labelColor: Color = HomeAppTextColor,
    labelFontWeight: FontWeight = FontWeight.Normal,
    // False for the profile carousel's read-only preview cards (see
    // com.lumenlauncher.app.ui.profiles.ProfileCarouselScreen) — long-press there must not open
    // Home's real Uninstall/App Info/shortcuts menu, since the preview isn't a place you manage apps.
    enableLongPressMenu: Boolean = true,
) {
    var menuExpanded by remember { mutableStateOf(false) }
    val clickModifier = if (enableLongPressMenu) {
        Modifier.combinedClickable(onClick = onClick, onLongClick = { menuExpanded = true })
    } else {
        Modifier.clickable(onClick = onClick)
    }
    Box {
        when (displayMode) {
            DockDisplayMode.ICONS -> Box(
                modifier = modifier.then(clickModifier),
            ) {
                AppIcon(
                    icon = app.icon,
                    size = 50.dp,
                    cornerRadius = 15.dp,
                    contentDescription = app.label,
                    notificationCount = badgeCount,
                    badgeStyle = badgeStyle,
                )
            }
            DockDisplayMode.TEXT -> Row(
                modifier = modifier
                    .then(clickModifier)
                    .padding(vertical = 14.dp, horizontal = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                Text(
                    text = app.label,
                    style = MaterialTheme.typography.bodyMedium.copy(shadow = homeAppLabelShadow(labelColor), fontWeight = labelFontWeight),
                    color = labelColor,
                )
                // Icons mode anchors the badge to the icon's own corner (see AppIcon); Text mode has
                // no icon to anchor to, so it trails the label instead — same placement AppRow uses
                // for the favorites list's own icon-less badge slot.
                if (badgeCount != null && badgeCount > 0) NotificationBadge(count = badgeCount, style = badgeStyle)
            }
        }
        if (enableLongPressMenu) {
            AppContextMenu(
                app = app,
                expanded = menuExpanded,
                onDismissRequest = { menuExpanded = false },
                onRequestShortcuts = onRequestShortcuts,
                onLaunchShortcut = onLaunchShortcut,
            )
        }
    }
}

@Preview(showBackground = true, widthDp = 390, heightDp = 844)
@Preview(name = "Dark", showBackground = true, widthDp = 390, heightDp = 844, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun HomeScreenPreview() {
    LumenLauncherTheme {
        val apps = (1..9).map {
            AppInfo(packageName = "com.example.app$it", activityName = ".MainActivity", label = "App $it", icon = null)
        }
        HomeScreen(
            appListItems = apps.take(5),
            dockApps = apps.drop(5).take(4),
            onAppClick = {},
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
    LumenLauncherTheme {
        val apps = (1..5).map {
            AppInfo(packageName = "com.example.app$it", activityName = ".MainActivity", label = "App $it", icon = null)
        }
        HomeScreen(
            appListItems = apps,
            dockApps = emptyList(),
            onAppClick = {},
            appRowPosition = AppRowPosition.RIGHT,
        )
    }
}

/** Presentation = Text Only: no icons in the app list. */
@Preview(name = "Text only presentation", showBackground = true, widthDp = 390, heightDp = 844)
@Composable
private fun HomeScreenTextOnlyPresentationPreview() {
    LumenLauncherTheme {
        val apps = (1..5).map {
            AppInfo(packageName = "com.example.app$it", activityName = ".MainActivity", label = "App $it", icon = null)
        }
        HomeScreen(
            appListItems = apps,
            dockApps = emptyList(),
            onAppClick = {},
            appRowPresentation = AppRowPresentation.TEXT_ONLY,
        )
    }
}
