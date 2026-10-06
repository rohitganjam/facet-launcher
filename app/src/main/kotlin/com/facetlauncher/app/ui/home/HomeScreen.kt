package com.facetlauncher.app.ui.home

import android.appwidget.AppWidgetHostView
import android.content.Context
import android.content.res.Configuration
import android.graphics.Rect as AndroidRect
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Folder as FolderIcon
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.layout.positionInRoot
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.toSize
import androidx.compose.ui.unit.dp
import com.facetlauncher.app.R
import com.facetlauncher.app.data.model.AppInfo
import com.facetlauncher.app.data.model.AppListColumnAlignment
import com.facetlauncher.app.data.model.AppListGridColumns
import com.facetlauncher.app.data.model.AppListGridDisplayMode
import com.facetlauncher.app.data.model.AppListLayout
import com.facetlauncher.app.data.model.AppListVerticalAlignment
import com.facetlauncher.app.data.model.toColumnPositions
import com.facetlauncher.app.data.model.AppRowPosition
import com.facetlauncher.app.data.model.AppRowPresentation
import com.facetlauncher.app.data.model.AppShortcut
import com.facetlauncher.app.data.model.CalendarEvent
import com.facetlauncher.app.data.model.ClockAlignment
import com.facetlauncher.app.data.model.ClockColorOption
import com.facetlauncher.app.data.model.ClockDateStyle
import com.facetlauncher.app.data.model.ClockFontOption
import com.facetlauncher.app.data.model.ClockTemplateId
import com.facetlauncher.app.data.model.DockDisplayMode
import com.facetlauncher.app.data.model.DrawerPresentation
import com.facetlauncher.app.data.model.Folder
import com.facetlauncher.app.domain.QuickAddState
import com.facetlauncher.app.domain.QuickPlacementAction
import com.facetlauncher.app.data.model.FontWeightOption
import com.facetlauncher.app.data.model.LauncherFontOption
import com.facetlauncher.app.data.model.NotificationBadgeStyle
import com.facetlauncher.app.data.model.PlacedItem
import com.facetlauncher.app.ui.components.AppContextMenu
import com.facetlauncher.app.ui.components.AppIcon
import com.facetlauncher.app.ui.components.AppIconSize
import com.facetlauncher.app.ui.components.FolderContentsSheet
import com.facetlauncher.app.ui.components.ThemedModalBottomSheet
import com.facetlauncher.app.ui.components.FolderSheetHeaderAction
import com.facetlauncher.app.ui.components.FolderTileContextMenu
import com.facetlauncher.app.ui.components.NotificationBadge
import com.facetlauncher.app.ui.components.longPressReleaseClickable
import com.facetlauncher.app.ui.theme.Accent
import com.facetlauncher.app.ui.theme.FolderGlyphBackground
import com.facetlauncher.app.ui.theme.HomeAppTextColor
import com.facetlauncher.app.ui.theme.Ink
import com.facetlauncher.app.ui.theme.FacetLauncherTheme
import com.facetlauncher.app.ui.theme.Muted
import com.facetlauncher.app.ui.theme.homeAppLabelShadow
import com.facetlauncher.app.ui.theme.resolve
import kotlin.math.abs
import kotlin.math.roundToInt
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.delay
import kotlinx.coroutines.withTimeoutOrNull

/** [MENU] shows the long-press bottom sheet; [ADJUST] shows the move handle *and* the resize handles together. */
enum class ClockAdjustMode { NONE, MENU, ADJUST }

/** A [PlacedItem]'s stable identity for keying/reordering — a folder's own id, or its app's component. */
internal fun PlacedItem.stableKey(): Any = when (this) {
    is PlacedItem.SingleApp -> app.packageName to app.activityName
    is PlacedItem.FolderItem -> "folder_${folder.id}"
}

/**
 * Home surface (`1a`, Airy density `1e`): clock + a short curated app list + dock. Both
 * [appListItems] (Favorites/Recents/Most Used depending on the active list content mode, from
 * [com.facetlauncher.app.data.FavoriteAppRepository] or
 * [com.facetlauncher.app.data.UsageStatsRepository]) and [dockApps] (from
 * [com.facetlauncher.app.data.DockAppRepository]) are real persisted state — this composable
 * only renders, it never slices/computes them itself.
 */
@Composable
fun HomeScreen(
    appListItems: List<PlacedItem>,
    dockApps: List<PlacedItem>,
    onAppClick: (AppInfo) -> Unit,
    modifier: Modifier = Modifier,
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
    /** Home clock's scale factor — see [com.facetlauncher.app.data.model.LauncherSettings.clockScale]. */
    clockScale: Float = 0.8f,
    /** Current adjustment mode — hoisted to the caller (e.g. [com.facetlauncher.app.ui.launcher.HomeDrawerRoute]) so screen gestures can be coordinated. */
    clockAdjustMode: ClockAdjustMode = ClockAdjustMode.NONE,
    /** `null` until the user drags the clock's grab handle for the first time — see [ClockZoneHandle] and this composable's own body for how the default and persisted cases resolve to one shared position formula. */
    clockZoneHeightDp: Float? = null,
    /** Fired once, on release, by the clock's grab handle. */
    onClockZoneHeightCommit: (Float) -> Unit = {},
    /** Fired once, on release, by the clock's resize handles. */
    onClockScaleCommit: (Float) -> Unit = { _ -> },
    /** Fired when the user picks left/center/right on the adjust-mode alignment toolbar. */
    onClockAlignmentChange: (ClockAlignment) -> Unit = {},
    /** Fired whenever the adjustment mode changes (e.g. via the bottom sheet or tap-away). */
    onAdjustModeChange: (ClockAdjustMode) -> Unit = {},
    /** True when a handle is actively being dragged — used to suppress screen gestures. */
    draggingHandle: Boolean = false,
    /** Fired when the user starts or stops actively dragging one of the adjustment handles. */
    onDraggingHandleChange: (Boolean) -> Unit = {},
    /** Id of the facet whose `overrideClock` bundle governs the clock widget's position/scale right now, or `null` if the global default applies. */
    clockPositionOwnerFacetId: Long? = null,
    /** That same facet's own real name, for [ClockAdjustSheet]'s "Edit clock & calendar styles" row badge — `null` alongside [clockPositionOwnerFacetId] when the global default applies. */
    clockPositionOwnerFacetName: String? = null,
    /** Fired by a plain tap on the clock (time/date), not a calendar event row — opens the device's default clock app. */
    onClockClick: () -> Unit = {},
    /** Fired when the user selects "Edit Styles" from the clock's adjustment menu. */
    onEditClockStyles: () -> Unit = {},
    /** Fired when the user selects "Launcher settings" from the long-press menu. */
    onNavigateToSettings: () -> Unit = {},
    /** Fired when the user selects "Facet settings" from the long-press menu — the caller resolves this to the active facet. */
    onNavigateToFacetSettings: () -> Unit = {},
    /** PRD F15 — whether the active facet currently has a hosted `AppWidget` bound as its clock. */
    hasCustomClockWidget: Boolean = false,
    /** Fired when the user selects "Use custom widget" from the clock's adjustment menu. */
    onUseCustomWidgetClick: () -> Unit = {},
    /** Fired when the user selects "Switch to launcher clock widget" from the clock's adjustment menu. */
    onSwitchToLauncherClockClick: () -> Unit = {},
    /** PRD F15 — non-null when [hasCustomClockWidget] is true; threaded straight to [ClockBlock]'s own identically-named param. */
    clockWidgetAppWidgetId: Int? = null,
    createClockWidgetHostView: (Context, Int) -> AppWidgetHostView? = { _, _ -> null },
    /** Current effective size — the persisted value if the user has resized it before, otherwise the provider's own declared default (the caller resolves that fallback; see `HomeDrawerRoute`). */
    clockWidgetWidthDp: Dp? = null,
    clockWidgetHeightDp: Dp? = null,
    /** The floor an interactive resize drag may not shrink below — see `ClockWidgetHostController.resizeFloorDp`'s own doc. */
    clockWidgetMinWidthDp: Dp? = null,
    clockWidgetMinHeightDp: Dp? = null,
    onClockWidgetSizeChange: (appWidgetId: Int, widthDp: Int, heightDp: Int) -> Unit = { _, _, _ -> },
    /** Fired once, on release, by the hosted clock widget's own resize handles — see `ClockWidgetFacetController.commitSize`'s own doc. */
    onClockWidgetSizeCommit: (widthDp: Int, heightDp: Int) -> Unit = { _, _ -> },
    /**
     * The hosted clock widget's current on-screen bounds, relative to this composable's own root —
     * the same frame [com.facetlauncher.app.ui.launcher.HomeDrawerRoute]'s own swipe-gesture
     * `pointerInput` (applied directly to this composable's [modifier]) sees touch positions in.
     * `null` whenever no widget is active. Real bug found on-device: that swipe detector claims
     * drags at `PointerEventPass.Initial` specifically so it can win against a descendant's own
     * tap/long-press detector (e.g. an app icon's) — correct there, but it meant a swipe starting
     * on a *hosted widget's own scrollable content* was being claimed as "open the drawer/shade"
     * before the widget's own internal `ListView` ever got a chance to recognize its own scroll.
     * The caller uses this to exclude the widget's own touch region from that claim.
     */
    onClockWidgetBoundsChange: (Rect?) -> Unit = {},
    /** PRD F15's nested-scroll handoff, threaded straight to [ClockBlock]'s own identically-named params — see [ClockWidgetTile]'s own doc. */
    onClockWidgetNestedScrollStart: () -> Unit = {},
    onClockWidgetLeftoverScroll: (dxPx: Float, dyPx: Float) -> Unit = { _, _ -> },
    onClockWidgetNestedScrollStop: () -> Unit = {},
    onClockWidgetLeftoverFling: (velocityXPx: Float, velocityYPx: Float) -> Unit = { _, _ -> },
    /**
     * Reports the app list's own scrollable viewport bounds, in the same root-relative coordinate
     * space [onClockWidgetBoundsChange] uses — so [com.facetlauncher.app.ui.launcher.HomeDrawerRoute]'s
     * swipe-gesture detector can exclude it from its `PointerEventPass.Initial` claim exactly the way
     * it already does for a hosted clock widget's own bounds (see that param's doc for why the
     * Initial-pass claim otherwise wins before a descendant's own scroll ever gets a chance). Real
     * bug found on-device: once the favorites/recents/most-used list had more rows than fit on
     * screen, dragging within it always opened the Drawer/notification shade instead of scrolling,
     * since the swipe detector claimed every vertical drag on Home's surface regardless of where it
     * started. `null` whenever the list hasn't been measured yet.
     */
    onAppListBoundsChange: (Rect?) -> Unit = {},
    appListVerticalAlignment: AppListVerticalAlignment = AppListVerticalAlignment.BOTTOM,
    /** Single column (today's unchanged layout), two columns, or a grid — see [AppListLayout]'s own doc. */
    appListLayout: AppListLayout = AppListLayout.SINGLE_COLUMN,
    /** Only meaningful while [appListLayout] is [AppListLayout.TWO_COLUMN]. */
    appListColumnAlignment: AppListColumnAlignment = AppListColumnAlignment.BOTH_LEFT,
    /** Only meaningful while [appListLayout] is [AppListLayout.GRID]. */
    appListGridColumns: AppListGridColumns = AppListGridColumns.FOUR,
    /** Only meaningful while [appListLayout] is [AppListLayout.GRID]. */
    appListGridDisplayMode: AppListGridDisplayMode = AppListGridDisplayMode.ICONS,
    /** Millis since epoch of the system's next alarm, or `null` when none is set — see [com.facetlauncher.app.domain.ObserveClockAccessoriesUseCase]. */
    nextAlarmMillis: Long? = null,
    batteryPercent: Int? = null,
    isCharging: Boolean = false,
    calendarEvents: List<CalendarEvent> = emptyList(),
    calendarColors: Map<String, String> = emptyMap(),
    onEventClick: (CalendarEvent) -> Unit = {},
    onRequestShortcuts: suspend (AppInfo) -> List<AppShortcut> = { emptyList() },
    onLaunchShortcut: (AppShortcut) -> Unit = {},
    onAppInfo: (AppInfo) -> Unit = {},
    /** F12's long-press "Add to Favorites"/"Add to Dock" (or "Remove from…") rows — see [com.facetlauncher.app.domain.ObserveQuickAddStateUseCase]'s own doc. */
    onRequestQuickAddState: suspend (AppInfo) -> QuickAddState = { QuickAddState() },
    onFavoritesAction: (AppInfo, QuickPlacementAction) -> Unit = { _, _ -> },
    onDockAction: (AppInfo, QuickPlacementAction) -> Unit = { _, _ -> },
    /** Folder-tile counterpart of [onRequestQuickAddState]/[onFavoritesAction]/[onDockAction]. */
    onRequestFolderQuickAddState: suspend (Folder) -> QuickAddState = { QuickAddState() },
    onFolderFavoritesAction: (Folder, QuickPlacementAction) -> Unit = { _, _ -> },
    onFolderDockAction: (Folder, QuickPlacementAction) -> Unit = { _, _ -> },
    /** F-Folders' "Add to folder" row on Dock icons' long-press menu, and folder-tile rendering/interaction — see [DockIcon]'s own doc. The full folder library, not just those already placed here. */
    folderCandidates: List<Folder>? = null,
    onCreateFolder: (AppInfo, String) -> Unit = { _, _ -> },
    onAddToFolder: (AppInfo, Long) -> Unit = { _, _ -> },
    onRemoveFromFolder: (Long, AppInfo) -> Unit = { _, _ -> },
    onRenameFolder: (Long, String) -> Unit = { _, _ -> },
    /** Folder-contents sheets (Home tiles, and the "Add to folder" preview) follow this exactly, mirroring the App Drawer's own List/Grid setting. */
    drawerPresentation: DrawerPresentation = DrawerPresentation.LIST,
    launcherFontOption: LauncherFontOption = LauncherFontOption.SYSTEM,
    appLabelColorOption: ClockColorOption = ClockColorOption.THEME,
    homeAppsFontWeight: FontWeightOption = FontWeightOption.REGULAR,
) {
    val appLabelColor = appLabelColorOption.resolve()
    val appLabelFontWeight = homeAppsFontWeight.resolve()
    val density = LocalDensity.current
    // The resize handles can land right at the screen edge (e.g. a full-width hosted widget) —
    // real bug found on-device: Android's own edge-swipe back gesture (wider on some OEM skins,
    // e.g. Samsung One UI) intercepts the touch-down before the handle's own pointerInput ever
    // sees it, making it ungrabbable. View.setSystemGestureExclusionRects (API 29+) is the
    // platform's own mechanism for exactly this — an app with edge-anchored interactive controls
    // opts that specific region out of the gesture-nav intercept. Registered/cleared below,
    // scoped to whichever ADJUST block (native clock or hosted widget) is actually active.
    val view = LocalView.current
    // Measured once per layout pass — contentHeightPx is the content Box's own height;
    // clockNaturalHeightPx is the clock+calendar block's intrinsic height (offset-independent, so
    // this is a stable one-frame-lag measurement, same pattern as e.g. ui/components/HubGrid.kt).
    var contentHeightPx by remember { mutableFloatStateOf(0f) }
    var clockNaturalHeightPx by remember { mutableFloatStateOf(0f) }
    // Natural (unconstrained) height of the app-list content, measured inside the scrollable
    // region — see the one-shot compact-spacing check below.
    var appListNaturalHeightPx by remember { mutableFloatStateOf(0f) }
    // Whether the app list's rows render at App Drawer's own COMPACT row density (8dp vertical
    // padding, matching DrawerListItemSize.COMPACT) instead of Home's default 16dp — see the
    // one-shot check below.
    var useCompactAppSpacing by remember { mutableStateOf(false) }
    // The move handle's live position while dragging, as an absolute value (not a delta — mirrors
    // liveScale below exactly, and for the same reason: a real flicker found on-device, where an
    // eagerly-zeroed delta briefly exposed the *old* basePersistedHandlePx for the one-or-two
    // frames between the drag ending and the new clockZoneHeightDp round-tripping back through the
    // repo/StateFlow — a visible snap-back-then-forward right at release). Held past the drag's
    // end until clockZoneHeightDp round-trips, then released so any external change (reset, facet
    // switch) takes effect.
    var liveHandlePx by remember { mutableStateOf<Float?>(null) }
    LaunchedEffect(clockZoneHeightDp) { liveHandlePx = null }

    // The clock's live scale while resizing, as an absolute value (not a delta — a delta briefly
    // double-counts against the committed clockScale in the frame the commit lands). Held past the
    // drag's end until clockScale round-trips back through the repo/StateFlow, then released so any
    // external change to clockScale (reset, facet switch) takes effect.
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
    var appListSize by remember { mutableStateOf<IntSize?>(null) }
    var appListOriginInRoot by remember { mutableStateOf<Offset?>(null) }

    // NaN until the first drag event captures the offset between where the finger grabbed and the
    // scale that point implies, so the clock doesn't pop on the first move.
    var dragGrabOffset by remember { mutableFloatStateOf(Float.NaN) }

    // PRD F15 — the hosted clock widget's own live width/height while resizing, real px (not a
    // scale factor — see ClockBlock's own doc for why a hosted widget needs its actual box size).
    // Mirrors liveScale/dragGrabOffset's own held-until-round-trip and NaN-until-first-move
    // conventions exactly, kept fully separate since the two resize mechanisms never apply to the
    // same clockAdjustMode session (hasCustomClockWidget picks one or the other).
    var liveWidgetWidthPx by remember { mutableStateOf<Float?>(null) }
    var liveWidgetHeightPx by remember { mutableStateOf<Float?>(null) }
    LaunchedEffect(clockWidgetWidthDp, clockWidgetHeightDp) {
        liveWidgetWidthPx = null
        liveWidgetHeightPx = null
    }
    var widgetDragGrabOffset by remember { mutableStateOf<Offset?>(null) }

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
    val handlePx = (liveHandlePx ?: basePersistedHandlePx).coerceIn(floorPx, ceilingPx)

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

    // PRD F15's analog of maxScaleThatFits above, for the hosted clock widget's own resize — real
    // px width/height directly (no scale ratio: the widget's current box IS its real size, not a
    // natural-size-times-scale like the native clock's own uniformScale layer), same bottom- and
    // alignment-anchored reasoning. [boxTopLeft]/[boxSize] are the widget's own current box, so
    // this is only meaningful once it's been measured at least once (same precondition as
    // maxScaleThatFits's own callers).
    val maxWidgetSizePxThatFits: (boxSize: IntSize, boxTopLeft: Offset, rootW: Int) -> Offset = { boxSize, boxTopLeft, rootW ->
        val boxW = boxSize.width.toFloat()
        val boxH = boxSize.height.toFloat()
        val maxHeight = boxTopLeft.y + boxH - clockTopReservePx
        val maxWidth = when (clockAlignment) {
            ClockAlignment.CENTER -> rootW - 2f * sideMarginPx
            ClockAlignment.LEFT -> rootW - sideMarginPx - boxTopLeft.x
            ClockAlignment.RIGHT -> boxTopLeft.x + boxW - sideMarginPx
        }
        Offset(maxWidth, maxHeight)
    }

    // One-shot re-clamp: a style or position change resizes the clock's natural box, so a scale
    // that fit the old look can now overflow. After the new geometry settles, check once — and
    // only persist a smaller value if it genuinely clips. Never runs otherwise. Shares its
    // settle-wait timing (GEOMETRY_SETTLE_*) with the compact-spacing check below — same pattern,
    // same tuning.
    LaunchedEffect(clockTemplateId, clockAlignment, clockDateStyle, clockZoneHeightDp, clockPositionOwnerFacetId) {
        withTimeoutOrNull(GEOMETRY_SETTLE_TIMEOUT_MS) {
            snapshotFlow { Triple(clockBoxSize, clockBoxOriginInRoot, rootSize) }
                .filter { it.first != null && it.second != null && it.third != null }
                .debounce(GEOMETRY_SETTLE_DEBOUNCE_MS)
                .first()
        }
        val size = clockBoxSize ?: return@LaunchedEffect
        val origin = clockBoxOriginInRoot ?: return@LaunchedEffect
        val rootW = rootSize?.width ?: return@LaunchedEffect
        val fit = maxScaleThatFits(size, origin - (rootOriginInRoot ?: Offset.Zero), rootW)
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
    LaunchedEffect(appListKey, appRowPresentation, showUsageAccessPrompt, clockZoneHeightDp) {
        useCompactAppSpacing = false
        withTimeoutOrNull(GEOMETRY_SETTLE_TIMEOUT_MS) {
            snapshotFlow { appListNaturalHeightPx to (contentHeightPx - handlePx) }
                .filter { (natural, available) -> natural > 0f && available > 0f }
                .debounce(GEOMETRY_SETTLE_DEBOUNCE_MS)
                .first()
        }
        useCompactAppSpacing = appListNaturalHeightPx > (contentHeightPx - handlePx)
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

    // PRD F15's analog of effectiveClockScale above — real width/height, only meaningful once
    // clockWidgetWidthDp/HeightDp are both resolved (the caller only supplies these once a hosted
    // widget is actually bound). While ADJUST mode is live it's clamped to whatever currently fits
    // (maxWidgetSizePxThatFits), same "dragging the move handle upward shrinks it in real time"
    // behavior the native clock's own effectiveClockScale already has.
    val effectiveClockWidgetSizePx = if (clockWidgetWidthDp == null || clockWidgetHeightDp == null) {
        null
    } else {
        val rawWidthPx = liveWidgetWidthPx ?: with(density) { clockWidgetWidthDp.toPx() }
        val rawHeightPx = liveWidgetHeightPx ?: with(density) { clockWidgetHeightDp.toPx() }
        val minWidthPx = clockWidgetMinWidthDp?.let { with(density) { it.toPx() } } ?: 1f
        val minHeightPx = clockWidgetMinHeightDp?.let { with(density) { it.toPx() } } ?: 1f
        val size = clockBoxSize
        val origin = clockBoxOriginInRoot
        val rootW = rootSize?.width
        if (clockAdjustMode == ClockAdjustMode.ADJUST && size != null && origin != null && rootW != null) {
            val maxSize = maxWidgetSizePxThatFits(size, origin - (rootOriginInRoot ?: Offset.Zero), rootW)
            Offset(
                rawWidthPx.coerceIn(minWidthPx, maxOf(minWidthPx, maxSize.x)),
                rawHeightPx.coerceIn(minHeightPx, maxOf(minHeightPx, maxSize.y)),
            )
        } else {
            Offset(rawWidthPx.coerceAtLeast(minWidthPx), rawHeightPx.coerceAtLeast(minHeightPx))
        }
    }
    val effectiveClockWidgetWidthDp = effectiveClockWidgetSizePx?.let { with(density) { it.x.toDp() } } ?: clockWidgetWidthDp
    val effectiveClockWidgetHeightDp = effectiveClockWidgetSizePx?.let { with(density) { it.y.toDp() } } ?: clockWidgetHeightDp

    Box(
        modifier = modifier
            .fillMaxSize()
            .onGloballyPositioned { rootSize = it.size; rootOriginInRoot = it.positionInRoot() }
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
                    clockScale = effectiveClockScale,
                    events = calendarEvents,
                    calendarColors = calendarColors,
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
                        .onGloballyPositioned {
                            appListSize = it.size
                            appListOriginInRoot = it.positionInRoot()
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
                            .onGloballyPositioned { appListNaturalHeightPx = it.size.height.toFloat() }
                            .graphicsLayer { alpha = appListAppearAlpha.value },
                    ) {
                        if (showUsageAccessPrompt) {
                            UsageAccessStrip(onClick = onUsageAccessPromptClick)
                        } else {
                            when (appListLayout) {
                                AppListLayout.TWO_COLUMN -> HomeAppTwoColumnList(
                                    appListItems = appListItems,
                                    columnAlignment = appListColumnAlignment,
                                    onAppClick = onAppClick,
                                    badgeCounts = badgeCounts,
                                    notificationBadgeStyle = notificationBadgeStyle,
                                    onRequestShortcuts = onRequestShortcuts,
                                    onLaunchShortcut = onLaunchShortcut,
                                    onAppInfo = onAppInfo,
                                    onRequestQuickAddState = onRequestQuickAddState,
                                    onFavoritesAction = onFavoritesAction,
                                    onDockAction = onDockAction,
                                    folderCandidates = folderCandidates,
                                    onCreateFolder = onCreateFolder,
                                    onAddToFolder = onAddToFolder,
                                    onRemoveFromFolder = onRemoveFromFolder,
                                    onRenameFolder = onRenameFolder,
                                    onRequestFolderQuickAddState = onRequestFolderQuickAddState,
                                    onFolderFavoritesAction = onFolderFavoritesAction,
                                    onFolderDockAction = onFolderDockAction,
                                    drawerPresentation = drawerPresentation,
                                    appRowPresentation = appRowPresentation,
                                    labelColor = appLabelColor,
                                    labelFontWeight = appLabelFontWeight,
                                    verticalPadding = if (useCompactAppSpacing) HOME_APP_ROW_COMPACT_VERTICAL_PADDING else HOME_APP_ROW_REGULAR_VERTICAL_PADDING,
                                    iconSize = if (useCompactAppSpacing) AppIconSize.ROW_COMPACT else AppIconSize.ROW_REGULAR,
                                )
                                AppListLayout.GRID -> HomeAppGrid(
                                    appListItems = appListItems,
                                    columns = appListGridColumns.columns,
                                    displayMode = appListGridDisplayMode,
                                    onAppClick = onAppClick,
                                    badgeCounts = badgeCounts,
                                    notificationBadgeStyle = notificationBadgeStyle,
                                    onRequestShortcuts = onRequestShortcuts,
                                    onLaunchShortcut = onLaunchShortcut,
                                    onAppInfo = onAppInfo,
                                    onRequestQuickAddState = onRequestQuickAddState,
                                    onFavoritesAction = onFavoritesAction,
                                    onDockAction = onDockAction,
                                    folderCandidates = folderCandidates,
                                    onCreateFolder = onCreateFolder,
                                    onAddToFolder = onAddToFolder,
                                    onRemoveFromFolder = onRemoveFromFolder,
                                    onRenameFolder = onRenameFolder,
                                    onRequestFolderQuickAddState = onRequestFolderQuickAddState,
                                    onFolderFavoritesAction = onFolderFavoritesAction,
                                    onFolderDockAction = onFolderDockAction,
                                    drawerPresentation = drawerPresentation,
                                    labelColor = appLabelColor,
                                    labelFontWeight = appLabelFontWeight,
                                )
                                AppListLayout.SINGLE_COLUMN, AppListLayout.LAUNCHER_DEFAULT -> Column(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalAlignment = when (appRowPosition) {
                                        AppRowPosition.RIGHT -> Alignment.End
                                        AppRowPosition.CENTER -> Alignment.CenterHorizontally
                                        AppRowPosition.LEFT, AppRowPosition.LAUNCHER_DEFAULT -> Alignment.Start
                                    },
                                ) {
                                    appListItems.forEach { item ->
                                        when (item) {
                                            is PlacedItem.SingleApp -> AppRow(
                                                modifier = Modifier.fillMaxWidth(SINGLE_COLUMN_ROW_WIDTH_FRACTION),
                                                app = item.app,
                                                onClick = { onAppClick(item.app) },
                                                badgeCount = badgeCounts[item.app.packageName],
                                                badgeStyle = notificationBadgeStyle,
                                                onRequestShortcuts = onRequestShortcuts,
                                                onLaunchShortcut = onLaunchShortcut,
                                                onAppInfo = onAppInfo,
                                                onRequestQuickAddState = onRequestQuickAddState,
                                                onFavoritesAction = onFavoritesAction,
                                                onDockAction = onDockAction,
                                                folderCandidates = folderCandidates,
                                                onCreateFolder = onCreateFolder,
                                                onAddToFolder = onAddToFolder,
                                                drawerPresentation = drawerPresentation,
                                                position = appRowPosition,
                                                presentation = appRowPresentation,
                                                labelColor = appLabelColor,
                                                labelFontWeight = appLabelFontWeight,
                                                verticalPadding = if (useCompactAppSpacing) HOME_APP_ROW_COMPACT_VERTICAL_PADDING else HOME_APP_ROW_REGULAR_VERTICAL_PADDING,
                                                iconSize = if (useCompactAppSpacing) AppIconSize.ROW_COMPACT else AppIconSize.ROW_REGULAR,
                                            )
                                            is PlacedItem.FolderItem -> FolderRow(
                                                modifier = Modifier.fillMaxWidth(SINGLE_COLUMN_ROW_WIDTH_FRACTION),
                                                folder = item.folder,
                                                onAppClick = onAppClick,
                                                onRequestShortcuts = onRequestShortcuts,
                                                onLaunchShortcut = onLaunchShortcut,
                                                onAppInfo = onAppInfo,
                                                onRemoveFromFolder = onRemoveFromFolder,
                                                onRenameFolder = onRenameFolder,
                                                drawerPresentation = drawerPresentation,
                                                onRequestQuickAddState = onRequestFolderQuickAddState,
                                                onFavoritesAction = onFolderFavoritesAction,
                                                onDockAction = onFolderDockAction,
                                                position = appRowPosition,
                                                presentation = appRowPresentation,
                                                labelColor = appLabelColor,
                                                labelFontWeight = appLabelFontWeight,
                                                verticalPadding = if (useCompactAppSpacing) HOME_APP_ROW_COMPACT_VERTICAL_PADDING else HOME_APP_ROW_REGULAR_VERTICAL_PADDING,
                                                iconSize = if (useCompactAppSpacing) AppIconSize.ROW_COMPACT else AppIconSize.ROW_REGULAR,
                                            )
                                        }
                                    }
                                }
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
                    dockApps.forEach { item ->
                        DockIcon(
                            item = item,
                            displayMode = dockDisplayMode,
                            onClick = onAppClick,
                            badgeCount = (item as? PlacedItem.SingleApp)?.let { badgeCounts[it.app.packageName] },
                            badgeStyle = notificationBadgeStyle,
                            onRequestShortcuts = onRequestShortcuts,
                            onLaunchShortcut = onLaunchShortcut,
                            onAppInfo = onAppInfo,
                            onRequestQuickAddState = onRequestQuickAddState,
                            onFavoritesAction = onFavoritesAction,
                            onDockAction = onDockAction,
                            onRequestFolderQuickAddState = onRequestFolderQuickAddState,
                            onFolderFavoritesAction = onFolderFavoritesAction,
                            onFolderDockAction = onFolderDockAction,
                            folderCandidates = folderCandidates,
                            onCreateFolder = onCreateFolder,
                            onAddToFolder = onAddToFolder,
                            onRemoveFromFolder = onRemoveFromFolder,
                            onRenameFolder = onRenameFolder,
                            drawerPresentation = drawerPresentation,
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
                    liveHandlePx = (liveHandlePx ?: basePersistedHandlePx) + with(density) { deltaDp.dp.toPx() }
                },
                onDragEnd = {
                    onDraggingHandleChange(false)
                    onClockZoneHeightCommit(with(density) { handlePx.toDp().value })
                    // liveHandlePx is deliberately left set — held until LaunchedEffect(clockZoneHeightDp)
                    // above sees the commit land, same reasoning as liveScale below.
                    if (effectiveClockScale < clockScale - 0.005f) onClockScaleCommit(effectiveClockScale)
                },
            )
        }

        // The alignment pill: below the height handle (its 48dp touch strip + a gap), not under the clock — the
        // clock block is pinned only 24dp above the handle, too little room for it. During any handle/resize
        // drag it dims to a ghost and its options are disabled, and it only becomes active again a moment after
        // the drag ends, so a thumb that has just let go of the handle can't tap it. It stays composed (so it
        // doesn't pop in and out) and keeps absorbing taps, which would otherwise reach the root and exit adjust mode.
        val toolbarActive by produceState(initialValue = false, clockAdjustMode, draggingHandle) {
            value = if (clockAdjustMode == ClockAdjustMode.ADJUST && !draggingHandle) {
                delay(CLOCK_ADJUST_TOOLBAR_REAPPEAR_DELAY_MS)
                true
            } else {
                false
            }
        }
        // Not `by` — read .value only inside graphicsLayer below, so the fade runs in the draw phase.
        val toolbarAlpha = animateFloatAsState(
            targetValue = if (toolbarActive) 1f else CLOCK_ADJUST_TOOLBAR_DIMMED_ALPHA,
            animationSpec = tween(if (toolbarActive) 150 else 120),
            label = "clockAdjustToolbarAlpha",
        )
        AnimatedVisibility(
            visible = clockAdjustMode == ClockAdjustMode.ADJUST,
            enter = fadeIn(tween(120)),
            exit = fadeOut(tween(80)),
            modifier = Modifier
                .align(Alignment.TopCenter)
                .offset { IntOffset(0, (handlePx + with(density) { (CLOCK_ZONE_HANDLE_TOUCH_HEIGHT + CLOCK_ADJUST_TOOLBAR_GAP).toPx() }).roundToInt()) },
        ) {
            ClockAdjustToolbar(
                alignment = clockAlignment,
                onAlignmentChange = onClockAlignmentChange,
                modifier = Modifier.graphicsLayer { alpha = toolbarAlpha.value },
                enabled = toolbarActive,
            )
        }

        // Plain local var, not remembered state — read once via the SideEffect below, after both
        // ADJUST blocks (at most one of which is ever active) have had a chance to populate it.
        var handleExclusionRects: List<AndroidRect> = emptyList()
        val handleExclusionSizePx = with(density) { 40.dp.toPx() }.roundToInt()
        fun handleExclusionRect(xPx: Float, yPx: Float): AndroidRect {
            val left = xPx.roundToInt()
            val top = yPx.roundToInt()
            return AndroidRect(left, top, left + handleExclusionSizePx, top + handleExclusionSizePx)
        }

        // PRD F15 — the native clock's own scale-based resize handles only apply when there's no
        // hosted widget to resize instead (see the parallel, real-size-based block below).
        val clockGeometryReady = clockBoxSize != null && clockBoxOriginInRoot != null && rootOriginInRoot != null && rootSize != null
        if (clockAdjustMode == ClockAdjustMode.ADJUST && clockWidgetAppWidgetId == null && clockGeometryReady) {
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
            handleExclusionRects = listOf(
                handleExclusionRect(visualTopLeftX - handleInsetPx, handleTopPx),
                handleExclusionRect(visualTopLeftX + visualWidth - handleInsetPx, handleTopPx),
            )
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

        // PRD F15 — the hosted clock widget's own resize handles: real width/height directly (the
        // widget's current box size IS its real size — no natural-size-times-scale layer to derive
        // from, unlike the native clock's own block above), otherwise the same bottom- and
        // alignment-anchored geometry, corner-handle placement, and live-clamped-while-dragging
        // behavior.
        if (clockAdjustMode == ClockAdjustMode.ADJUST && clockWidgetAppWidgetId != null && clockGeometryReady) {
            val rootOrigin = rootOriginInRoot!!
            val boxW = clockBoxSize!!.width.toFloat()
            val boxH = clockBoxSize!!.height.toFloat()
            val boxTopLeft = clockBoxOriginInRoot!! - rootOrigin
            val maxWidgetSize = maxWidgetSizePxThatFits(clockBoxSize!!, boxTopLeft, rootSize!!.width)
            val minWidthPx = clockWidgetMinWidthDp?.let { with(density) { it.toPx() } } ?: 1f
            val minHeightPx = clockWidgetMinHeightDp?.let { with(density) { it.toPx() } } ?: 1f

            val boxBottom = boxTopLeft.y + boxH
            // The x that stays put as the box resizes (matches the widget's own alignment anchor).
            val anchorX = when (clockAlignment) {
                ClockAlignment.LEFT -> boxTopLeft.x
                ClockAlignment.RIGHT -> boxTopLeft.x + boxW
                ClockAlignment.CENTER -> boxTopLeft.x + boxW / 2f
            }

            // Real width/height a touch point implies, if that point were the dragged corner.
            val sizeAtTouch: (Offset) -> Offset = { t ->
                val w = when (clockAlignment) {
                    ClockAlignment.LEFT -> t.x - anchorX
                    ClockAlignment.RIGHT -> anchorX - t.x
                    ClockAlignment.CENTER -> abs(t.x - anchorX) * 2f
                }
                Offset(w, boxBottom - t.y)
            }
            val onWidgetHandleDragStart: () -> Unit = {
                widgetDragGrabOffset = null
                onDraggingHandleChange(true)
            }
            val onWidgetHandleDrag: (Offset) -> Unit = { touchInAbsolute ->
                val implied = sizeAtTouch(touchInAbsolute - rootOrigin)
                // First move: capture where the finger sits vs the corner, so the box doesn't pop.
                val grab = widgetDragGrabOffset
                    ?: Offset(implied.x - (liveWidgetWidthPx ?: boxW), implied.y - (liveWidgetHeightPx ?: boxH))
                        .also { widgetDragGrabOffset = it }
                liveWidgetWidthPx = (implied.x - grab.x).coerceIn(minWidthPx, maxOf(minWidthPx, maxWidgetSize.x))
                liveWidgetHeightPx = (implied.y - grab.y).coerceIn(minHeightPx, maxOf(minHeightPx, maxWidgetSize.y))
            }
            val onWidgetHandleDragEnd: () -> Unit = {
                onDraggingHandleChange(false)
                val finalWidthPx = (liveWidgetWidthPx ?: boxW).coerceIn(minWidthPx, maxOf(minWidthPx, maxWidgetSize.x))
                val finalHeightPx = (liveWidgetHeightPx ?: boxH).coerceIn(minHeightPx, maxOf(minHeightPx, maxWidgetSize.y))
                val widthDp = with(density) { finalWidthPx.toDp().value }.roundToInt()
                val heightDp = with(density) { finalHeightPx.toDp().value }.roundToInt()
                onClockWidgetSizeCommit(widthDp, heightDp)
            }

            val handleInsetPx = with(density) { HOME_CLOCK_RESIZE_HANDLE_INSET.toPx() }
            val handleTopPx = (boxTopLeft.y - handleInsetPx).coerceAtLeast(topInsetPx)
            handleExclusionRects = listOf(
                handleExclusionRect(boxTopLeft.x - handleInsetPx, handleTopPx),
                handleExclusionRect(boxTopLeft.x + boxW - handleInsetPx, handleTopPx),
            )
            Box(
                modifier = Modifier
                    .offset { IntOffset(boxTopLeft.x.roundToInt(), boxTopLeft.y.roundToInt()) }
                    .size(with(density) { boxW.toDp() }, with(density) { boxH.toDp() })
                    .border(1.dp, Accent.copy(alpha = 0.5f), RoundedCornerShape(12.dp)),
            )
            ClockCornerHandle(
                isRightSide = false,
                modifier = Modifier.offset {
                    IntOffset((boxTopLeft.x - handleInsetPx).roundToInt(), handleTopPx.roundToInt())
                },
                onDragStart = onWidgetHandleDragStart,
                onDrag = onWidgetHandleDrag,
                onDragEnd = onWidgetHandleDragEnd,
            )
            ClockCornerHandle(
                isRightSide = true,
                modifier = Modifier.offset {
                    IntOffset((boxTopLeft.x + boxW - handleInsetPx).roundToInt(), handleTopPx.roundToInt())
                },
                onDragStart = onWidgetHandleDragStart,
                onDrag = onWidgetHandleDrag,
                onDragEnd = onWidgetHandleDragEnd,
            )
        }

        // Opts the resize handles' own touch region out of the system's edge-swipe back gesture —
        // see handleExclusionRects' own declaration above. Empty (clearing any previously
        // registered rects) whenever neither ADJUST block above ran this composition, e.g. leaving
        // ADJUST mode or a hosted widget being sized small enough that its handles never land near
        // an edge (this still runs, it just sets an empty list, a cheap no-op call).
        SideEffect { view.systemGestureExclusionRects = handleExclusionRects }

        // Reports the hosted widget's current bounds so HomeDrawerRoute's own swipe-to-open-drawer
        // gesture can exclude them — see onClockWidgetBoundsChange's own doc. null whenever no
        // widget is active (the native clock has no independent scrollable content to protect, so
        // a swipe starting on it keeps opening the drawer/shade exactly as it does today) or its
        // box hasn't been measured yet.
        SideEffect {
            val size = clockBoxSize
            val origin = clockBoxOriginInRoot
            val rootOrigin = rootOriginInRoot
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
        // doc. null whenever it hasn't been measured yet.
        SideEffect {
            val size = appListSize
            val origin = appListOriginInRoot
            val rootOrigin = rootOriginInRoot
            val bounds = if (size != null && origin != null && rootOrigin != null) {
                Rect(offset = origin - rootOrigin, size = size.toSize())
            } else {
                null
            }
            onAppListBoundsChange(bounds)
        }

        // Adjustment menu sheet
        if (clockAdjustMode == ClockAdjustMode.MENU) {
            ThemedModalBottomSheet(
                onDismissRequest = { onAdjustModeChange(ClockAdjustMode.NONE) },
                skipPartiallyExpanded = true,
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

/** Visible gap between the height handle's touch strip and the alignment toolbar below it — Material's minimum between touch targets. */
internal val CLOCK_ADJUST_TOOLBAR_GAP = 8.dp

/** How long the toolbar stays dimmed and disabled after a drag ends — see its use in [HomeScreen]. */
private const val CLOCK_ADJUST_TOOLBAR_REAPPEAR_DELAY_MS = 200L

/** The toolbar's opacity while a drag is active or just ended — a visible ghost, not gone. */
private const val CLOCK_ADJUST_TOOLBAR_DIMMED_ALPHA = 0.25f

/** The grab handle's position (measured from the top of the content area) can never exceed this fraction of the content area's own height. */
private const val HOME_CLOCK_ZONE_MAX_FRACTION = 0.5f

/** Shared clamp for the clock's scale factor — live render, handle positioning, and the committed value all use this pair. */
private const val HOME_CLOCK_MIN_SCALE = 0.5f
private const val HOME_CLOCK_MAX_SCALE = 2.0f

/** Half the resize handle's hit box — the amount it extends past the clock corner it sits on. */
private val HOME_CLOCK_RESIZE_HANDLE_INSET = 20.dp

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

private fun Modifier.dashedBorder(color: Color, cornerRadius: Dp, strokeWidth: Dp = 1.dp): Modifier = drawBehind {
    drawRoundRect(
        color = color,
        style = Stroke(width = strokeWidth.toPx(), pathEffect = PathEffect.dashPathEffect(floatArrayOf(8f, 6f), 0f)),
        cornerRadius = CornerRadius(cornerRadius.toPx()),
    )
}

/**
 * [AppListLayout.TWO_COLUMN] — the list split interleaved by original order (index 0,2,4… go in
 * the left column top-to-bottom; 1,3,5… in the right, each column preserving that order) into a
 * `Row` of two `weight(1f)` [HomeAppListColumn]s. Reuses [AppRow]/[FolderRow] unchanged for each
 * column's own rows — [AppRowPosition.LEFT]/`RIGHT` already fully implement the "pack to my own
 * container's edge + reverse icon/label order" mirroring a column needs, so [columnAlignment]
 * just picks which of those two existing values each column renders with.
 */
@Composable
private fun HomeAppTwoColumnList(
    appListItems: List<PlacedItem>,
    columnAlignment: AppListColumnAlignment,
    onAppClick: (AppInfo) -> Unit,
    badgeCounts: Map<String, Int>,
    notificationBadgeStyle: NotificationBadgeStyle,
    onRequestShortcuts: suspend (AppInfo) -> List<AppShortcut>,
    onLaunchShortcut: (AppShortcut) -> Unit,
    onAppInfo: (AppInfo) -> Unit,
    onRequestQuickAddState: suspend (AppInfo) -> QuickAddState,
    onFavoritesAction: (AppInfo, QuickPlacementAction) -> Unit,
    onDockAction: (AppInfo, QuickPlacementAction) -> Unit,
    folderCandidates: List<Folder>?,
    onCreateFolder: (AppInfo, String) -> Unit,
    onAddToFolder: (AppInfo, Long) -> Unit,
    onRemoveFromFolder: (Long, AppInfo) -> Unit,
    onRenameFolder: (Long, String) -> Unit,
    onRequestFolderQuickAddState: suspend (Folder) -> QuickAddState,
    onFolderFavoritesAction: (Folder, QuickPlacementAction) -> Unit,
    onFolderDockAction: (Folder, QuickPlacementAction) -> Unit,
    drawerPresentation: DrawerPresentation,
    appRowPresentation: AppRowPresentation,
    labelColor: Color,
    labelFontWeight: FontWeight,
    verticalPadding: Dp,
    iconSize: Dp,
) {
    val leftItems = appListItems.filterIndexed { index, _ -> index % 2 == 0 }
    val rightItems = appListItems.filterIndexed { index, _ -> index % 2 == 1 }
    val (leftPosition, rightPosition) = columnAlignment.toColumnPositions()
    Row(modifier = Modifier.fillMaxWidth()) {
        listOf(leftItems to leftPosition, rightItems to rightPosition).forEach { (items, position) ->
            HomeAppListColumn(
                items = items,
                position = position,
                onAppClick = onAppClick,
                badgeCounts = badgeCounts,
                notificationBadgeStyle = notificationBadgeStyle,
                onRequestShortcuts = onRequestShortcuts,
                onLaunchShortcut = onLaunchShortcut,
                onAppInfo = onAppInfo,
                onRequestQuickAddState = onRequestQuickAddState,
                onFavoritesAction = onFavoritesAction,
                onDockAction = onDockAction,
                folderCandidates = folderCandidates,
                onCreateFolder = onCreateFolder,
                onAddToFolder = onAddToFolder,
                onRemoveFromFolder = onRemoveFromFolder,
                onRenameFolder = onRenameFolder,
                onRequestFolderQuickAddState = onRequestFolderQuickAddState,
                onFolderFavoritesAction = onFolderFavoritesAction,
                onFolderDockAction = onFolderDockAction,
                drawerPresentation = drawerPresentation,
                appRowPresentation = appRowPresentation,
                labelColor = labelColor,
                labelFontWeight = labelFontWeight,
                verticalPadding = verticalPadding,
                iconSize = iconSize,
                modifier = Modifier.weight(1f),
            )
        }
    }
}

/** One [HomeAppTwoColumnList] column — identical to [HomeScreen]'s own single-column `forEach` over [AppRow]/[FolderRow], just over a subset of [items] with its own [position]. */
@Composable
private fun HomeAppListColumn(
    items: List<PlacedItem>,
    position: AppRowPosition,
    onAppClick: (AppInfo) -> Unit,
    badgeCounts: Map<String, Int>,
    notificationBadgeStyle: NotificationBadgeStyle,
    onRequestShortcuts: suspend (AppInfo) -> List<AppShortcut>,
    onLaunchShortcut: (AppShortcut) -> Unit,
    onAppInfo: (AppInfo) -> Unit,
    onRequestQuickAddState: suspend (AppInfo) -> QuickAddState,
    onFavoritesAction: (AppInfo, QuickPlacementAction) -> Unit,
    onDockAction: (AppInfo, QuickPlacementAction) -> Unit,
    folderCandidates: List<Folder>?,
    onCreateFolder: (AppInfo, String) -> Unit,
    onAddToFolder: (AppInfo, Long) -> Unit,
    onRemoveFromFolder: (Long, AppInfo) -> Unit,
    onRenameFolder: (Long, String) -> Unit,
    onRequestFolderQuickAddState: suspend (Folder) -> QuickAddState,
    onFolderFavoritesAction: (Folder, QuickPlacementAction) -> Unit,
    onFolderDockAction: (Folder, QuickPlacementAction) -> Unit,
    drawerPresentation: DrawerPresentation,
    appRowPresentation: AppRowPresentation,
    labelColor: Color,
    labelFontWeight: FontWeight,
    verticalPadding: Dp,
    iconSize: Dp,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier) {
        items.forEach { item ->
            when (item) {
                is PlacedItem.SingleApp -> AppRow(
                    app = item.app,
                    onClick = { onAppClick(item.app) },
                    badgeCount = badgeCounts[item.app.packageName],
                    badgeStyle = notificationBadgeStyle,
                    onRequestShortcuts = onRequestShortcuts,
                    onLaunchShortcut = onLaunchShortcut,
                    onAppInfo = onAppInfo,
                    onRequestQuickAddState = onRequestQuickAddState,
                    onFavoritesAction = onFavoritesAction,
                    onDockAction = onDockAction,
                    folderCandidates = folderCandidates,
                    onCreateFolder = onCreateFolder,
                    onAddToFolder = onAddToFolder,
                    drawerPresentation = drawerPresentation,
                    position = position,
                    presentation = appRowPresentation,
                    labelColor = labelColor,
                    labelFontWeight = labelFontWeight,
                    verticalPadding = verticalPadding,
                    iconSize = iconSize,
                )
                is PlacedItem.FolderItem -> FolderRow(
                    folder = item.folder,
                    onAppClick = onAppClick,
                    onRequestShortcuts = onRequestShortcuts,
                    onLaunchShortcut = onLaunchShortcut,
                    onAppInfo = onAppInfo,
                    onRemoveFromFolder = onRemoveFromFolder,
                    onRenameFolder = onRenameFolder,
                    drawerPresentation = drawerPresentation,
                    onRequestQuickAddState = onRequestFolderQuickAddState,
                    onFavoritesAction = onFolderFavoritesAction,
                    onDockAction = onFolderDockAction,
                    position = position,
                    presentation = appRowPresentation,
                    labelColor = labelColor,
                    labelFontWeight = labelFontWeight,
                    verticalPadding = verticalPadding,
                    iconSize = iconSize,
                )
            }
        }
    }
}

/**
 * [AppListLayout.GRID] — a plain [LazyVerticalGrid] (not [AppDrawerScreen][com.facetlauncher.app.ui.drawer.AppDrawerScreen]'s
 * own `DrawerGridContent`, which is built to exactly fill an entire viewport at a fixed row count;
 * Home's grid is a small, bounded, ≤[AppListLimits.MAX_FAVORITES]-item list sharing the same
 * reserved region the other two layouts use, so each tile just takes its own intrinsic height
 * instead of a computed fixed row height). Tile look mirrors the App Drawer's own `DrawerGridTile`/
 * `DrawerFolderTile` ([AppIconSize.TILE], centered), but branches [AppListGridDisplayMode] the way
 * [DockIcon] branches [DockDisplayMode] (icon-only or text-only, never both) rather than
 * `DrawerGridTile`'s own `showLabel: Boolean` (icon always shown, label optional).
 */
@Composable
private fun HomeAppGrid(
    appListItems: List<PlacedItem>,
    columns: Int,
    displayMode: AppListGridDisplayMode,
    onAppClick: (AppInfo) -> Unit,
    badgeCounts: Map<String, Int>,
    notificationBadgeStyle: NotificationBadgeStyle,
    onRequestShortcuts: suspend (AppInfo) -> List<AppShortcut>,
    onLaunchShortcut: (AppShortcut) -> Unit,
    onAppInfo: (AppInfo) -> Unit,
    onRequestQuickAddState: suspend (AppInfo) -> QuickAddState,
    onFavoritesAction: (AppInfo, QuickPlacementAction) -> Unit,
    onDockAction: (AppInfo, QuickPlacementAction) -> Unit,
    folderCandidates: List<Folder>?,
    onCreateFolder: (AppInfo, String) -> Unit,
    onAddToFolder: (AppInfo, Long) -> Unit,
    onRemoveFromFolder: (Long, AppInfo) -> Unit,
    onRenameFolder: (Long, String) -> Unit,
    onRequestFolderQuickAddState: suspend (Folder) -> QuickAddState,
    onFolderFavoritesAction: (Folder, QuickPlacementAction) -> Unit,
    onFolderDockAction: (Folder, QuickPlacementAction) -> Unit,
    drawerPresentation: DrawerPresentation,
    labelColor: Color,
    labelFontWeight: FontWeight,
) {
    LazyVerticalGrid(
        columns = GridCells.Fixed(columns),
        modifier = Modifier
            .fillMaxWidth()
            .testTag("home_app_grid"),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        items(appListItems, key = { it.stableKey() }) { item ->
            when (item) {
                is PlacedItem.SingleApp -> HomeAppGridTile(
                    app = item.app,
                    displayMode = displayMode,
                    onClick = { onAppClick(item.app) },
                    badgeCount = badgeCounts[item.app.packageName],
                    badgeStyle = notificationBadgeStyle,
                    onRequestShortcuts = onRequestShortcuts,
                    onLaunchShortcut = onLaunchShortcut,
                    onAppInfo = onAppInfo,
                    onRequestQuickAddState = onRequestQuickAddState,
                    onFavoritesAction = onFavoritesAction,
                    onDockAction = onDockAction,
                    folderCandidates = folderCandidates,
                    onCreateFolder = onCreateFolder,
                    onAddToFolder = onAddToFolder,
                    drawerPresentation = drawerPresentation,
                    labelColor = labelColor,
                    labelFontWeight = labelFontWeight,
                )
                is PlacedItem.FolderItem -> HomeFolderGridTile(
                    folder = item.folder,
                    displayMode = displayMode,
                    onAppClick = onAppClick,
                    onRequestShortcuts = onRequestShortcuts,
                    onLaunchShortcut = onLaunchShortcut,
                    onAppInfo = onAppInfo,
                    onRemoveFromFolder = onRemoveFromFolder,
                    onRenameFolder = onRenameFolder,
                    onRequestQuickAddState = onRequestFolderQuickAddState,
                    onFavoritesAction = onFolderFavoritesAction,
                    onDockAction = onFolderDockAction,
                    drawerPresentation = drawerPresentation,
                    labelColor = labelColor,
                    labelFontWeight = labelFontWeight,
                )
            }
        }
    }
}

/** One [HomeAppGrid] tile — see [DockIcon]'s own doc for why [displayMode] is a strict icon-or-text branch, not [AppRowPresentation]'s combinable one. */
@Composable
private fun HomeAppGridTile(
    app: AppInfo,
    displayMode: AppListGridDisplayMode,
    onClick: () -> Unit,
    badgeCount: Int?,
    badgeStyle: NotificationBadgeStyle,
    onRequestShortcuts: suspend (AppInfo) -> List<AppShortcut>,
    onLaunchShortcut: (AppShortcut) -> Unit,
    onAppInfo: (AppInfo) -> Unit,
    onRequestQuickAddState: suspend (AppInfo) -> QuickAddState,
    onFavoritesAction: (AppInfo, QuickPlacementAction) -> Unit,
    onDockAction: (AppInfo, QuickPlacementAction) -> Unit,
    folderCandidates: List<Folder>?,
    onCreateFolder: (AppInfo, String) -> Unit,
    onAddToFolder: (AppInfo, Long) -> Unit,
    drawerPresentation: DrawerPresentation,
    labelColor: Color,
    labelFontWeight: FontWeight,
) {
    var menuExpanded by remember { mutableStateOf(false) }
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .longPressReleaseClickable(onClick = onClick, onLongPress = { menuExpanded = true })
            .padding(vertical = 8.dp),
        contentAlignment = Alignment.Center,
    ) {
        if (displayMode == AppListGridDisplayMode.TEXT) {
            Text(
                text = app.label,
                style = MaterialTheme.typography.labelSmall.copy(shadow = homeAppLabelShadow(labelColor), fontWeight = labelFontWeight),
                color = labelColor,
                textAlign = TextAlign.Center,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.testTag("home_app_label_${app.packageName}"),
            )
        } else {
            AppIcon(
                icon = app.icon,
                size = AppIconSize.TILE,
                contentDescription = app.label,
                notificationCount = badgeCount,
                badgeStyle = badgeStyle,
                modifier = Modifier.testTag("home_app_icon_${app.packageName}"),
            )
        }
        AppContextMenu(
            app = app,
            expanded = menuExpanded,
            onDismissRequest = { menuExpanded = false },
            onRequestShortcuts = onRequestShortcuts,
            onLaunchShortcut = onLaunchShortcut,
            onAppInfo = onAppInfo,
            onRequestQuickAddState = onRequestQuickAddState,
            onFavoritesAction = onFavoritesAction,
            onDockAction = onDockAction,
            folderCandidates = folderCandidates,
            onCreateFolder = onCreateFolder,
            onAddToFolder = onAddToFolder,
            drawerPresentation = drawerPresentation,
        )
    }
}

/** [HomeAppGridTile]'s folder counterpart — see [FolderDockIcon]'s own doc for the tap/long-press shape this mirrors ([FolderContentsSheet] on tap, [FolderTileContextMenu] on long-press). */
@Composable
private fun HomeFolderGridTile(
    folder: Folder,
    displayMode: AppListGridDisplayMode,
    onAppClick: (AppInfo) -> Unit,
    onRequestShortcuts: suspend (AppInfo) -> List<AppShortcut>,
    onLaunchShortcut: (AppShortcut) -> Unit,
    onAppInfo: (AppInfo) -> Unit,
    onRemoveFromFolder: (Long, AppInfo) -> Unit,
    onRenameFolder: (Long, String) -> Unit,
    onRequestQuickAddState: suspend (Folder) -> QuickAddState,
    onFavoritesAction: (Folder, QuickPlacementAction) -> Unit,
    onDockAction: (Folder, QuickPlacementAction) -> Unit,
    drawerPresentation: DrawerPresentation,
    labelColor: Color,
    labelFontWeight: FontWeight,
) {
    var sheetOpen by remember { mutableStateOf(false) }
    var menuExpanded by remember { mutableStateOf(false) }
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .longPressReleaseClickable(onClick = { sheetOpen = true }, onLongPress = { menuExpanded = true })
            .testTag("home_folder_grid_tile_${folder.id}")
            .padding(vertical = 8.dp),
        contentAlignment = Alignment.Center,
    ) {
        if (displayMode == AppListGridDisplayMode.TEXT) {
            Text(
                text = folder.name,
                style = MaterialTheme.typography.labelSmall.copy(shadow = homeAppLabelShadow(labelColor), fontWeight = labelFontWeight),
                color = labelColor,
                textAlign = TextAlign.Center,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
        } else {
            FolderTileGlyph(folder = folder)
        }
        if (sheetOpen) {
            FolderContentsSheet(
                folder = folder,
                onDismissRequest = { sheetOpen = false },
                onAppClick = onAppClick,
                presentation = drawerPresentation,
                onRemoveFromFolder = onRemoveFromFolder,
                headerAction = FolderSheetHeaderAction.Rename(onRename = onRenameFolder),
                onRequestShortcuts = onRequestShortcuts,
                onLaunchShortcut = onLaunchShortcut,
                onAppInfo = onAppInfo,
            )
        }
        FolderTileContextMenu(
            folder = folder,
            expanded = menuExpanded,
            onDismissRequest = { menuExpanded = false },
            onRename = onRenameFolder,
            onRequestQuickAddState = onRequestQuickAddState,
            onFavoritesAction = onFavoritesAction,
            onDockAction = onDockAction,
        )
    }
}

/**
 * [position] governs both internal ordering (icon-then-label vs. label-then-icon) and whether the
 * row's content packs against the start or end of the available width — `RIGHT` needs both, not
 * just a reversed order while staying left-anchored, for the row to actually hug the screen's
 * right edge. [presentation] independently governs which of icon/label actually render; the
 * unused one's slot composable simply emits nothing rather than branching the whole layout.
 *
 * Not private: reused by the facet carousel's preview cards
 * ([com.facetlauncher.app.ui.facets.FacetCarouselScreen]) so the favorites list renders with
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
    onAppInfo: (AppInfo) -> Unit = {},
    modifier: Modifier = Modifier,
    onRequestQuickAddState: suspend (AppInfo) -> QuickAddState = { QuickAddState() },
    onFavoritesAction: (AppInfo, QuickPlacementAction) -> Unit = { _, _ -> },
    onDockAction: (AppInfo, QuickPlacementAction) -> Unit = { _, _ -> },
    /** The full folder library — see [AppContextMenu]'s own doc for what non-null means. Non-Dock-only: this closes the earlier "Dock-only" gap for Favorites' own long-press. */
    folderCandidates: List<Folder>? = null,
    onCreateFolder: (AppInfo, String) -> Unit = { _, _ -> },
    onAddToFolder: (AppInfo, Long) -> Unit = { _, _ -> },
    drawerPresentation: DrawerPresentation = DrawerPresentation.LIST,
    position: AppRowPosition = AppRowPosition.LEFT,
    presentation: AppRowPresentation = AppRowPresentation.ICON_AND_TEXT,
    labelColor: Color = HomeAppTextColor,
    labelFontWeight: FontWeight = FontWeight.Normal,
    // False for the facet carousel's read-only preview cards (see
    // com.facetlauncher.app.ui.facets.FacetCarouselScreen) — long-press there must not open
    // Home's real Uninstall/App Info/shortcuts menu, since the preview isn't a place you manage apps.
    enableLongPressMenu: Boolean = true,
    // 16dp matches App Drawer's own Regular list-item spacing exactly (base 8dp +
    // DrawerListItemSize.REGULAR's 8dp extraRowPaddingDp), per direct request that Home's app list
    // read as the same density as Drawer's default (see chat history) — that's Home's own real
    // density and stays the default here. Home itself overrides this to
    // HOME_APP_ROW_COMPACT_VERTICAL_PADDING once its own one-shot check finds the list would
    // otherwise need to scroll; a caller rendering this inside a small preview card (the facet
    // carousel, the Appearance screen's own live preview) overrides it tighter for its own reasons
    // (see chat history).
    verticalPadding: Dp = HOME_APP_ROW_REGULAR_VERTICAL_PADDING,
    // App Drawer's own Regular tier — matches [verticalPadding]'s own default density. Home
    // overrides this to [AppIconSize.ROW_COMPACT] alongside the padding once the compact-spacing
    // check finds the list would otherwise need to scroll (see chat history).
    iconSize: Dp = AppIconSize.ROW_REGULAR,
    // Distinguishes this row's testTags from another AppRow rendering the same app elsewhere in
    // the same semantics tree — e.g. a facet carousel preview card can be present alongside
    // Home's own real AppRow for the same favorite app. Callers other than Home's own real list
    // must override this to something unique to their surface.
    testTagPrefix: String = "home_",
) {
    var menuExpanded by remember { mutableStateOf(false) }
    val showIcon = presentation != AppRowPresentation.TEXT_ONLY
    val showLabel = presentation != AppRowPresentation.ICON_ONLY
    Box(modifier = modifier) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                // Large/16dp (MaterialTheme.shapes.large — see CLAUDE.md's shape table, which
                // names this the "navigation drawers" token) clips the ripple/press state to a
                // rounded rect instead of a full-bleed rectangle (see chat history).
                .clip(MaterialTheme.shapes.large)
                .then(
                    if (enableLongPressMenu) {
                        Modifier.longPressReleaseClickable(onClick = onClick, onLongPress = { menuExpanded = true })
                    } else {
                        Modifier.clickable(onClick = onClick)
                    }
                )
                .padding(horizontal = 8.dp, vertical = verticalPadding),
            verticalAlignment = Alignment.CenterVertically,
            // RIGHT packs the whole row's content against the trailing edge (not just reversed
            // order while left-anchored) so the row visually hugs the screen's right edge; CENTER
            // centers that same (unreversed) group within the row's full width instead.
            // LAUNCHER_DEFAULT never reaches this composable — callers always pass an already-
            // resolved effective value (HomeUiState.activeAppRowPosition/FacetCarouselViewModel's
            // own resolver), which resolves the sentinel away before it gets here. Handled the same
            // as LEFT only because AppRowPosition's `when` must stay exhaustive.
            horizontalArrangement = when (position) {
                AppRowPosition.RIGHT -> Arrangement.spacedBy(14.dp, Alignment.End)
                AppRowPosition.CENTER -> Arrangement.spacedBy(14.dp, Alignment.CenterHorizontally)
                AppRowPosition.LEFT, AppRowPosition.LAUNCHER_DEFAULT -> Arrangement.spacedBy(14.dp)
            },
        ) {
            val icon: @Composable () -> Unit = {
                if (showIcon) {
                    AppIcon(
                        icon = app.icon,
                        size = iconSize,
                        // No visible label to carry the a11y name when text is hidden — mirrors
                        // the Dock's own icons-only mode (see DockIcon below).
                        contentDescription = if (showLabel) null else app.label,
                        modifier = Modifier.testTag("${testTagPrefix}app_icon_${app.packageName}"),
                    )
                }
            }
            val label: @Composable () -> Unit = {
                if (showLabel) {
                    Text(
                        text = app.label,
                        style = MaterialTheme.typography.titleMedium.copy(shadow = homeAppLabelShadow(labelColor), fontWeight = labelFontWeight),
                        color = labelColor,
                        modifier = Modifier.testTag("${testTagPrefix}app_label_${app.packageName}"),
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
                onAppInfo = onAppInfo,
                onRequestQuickAddState = onRequestQuickAddState,
                onFavoritesAction = onFavoritesAction,
                onDockAction = onDockAction,
                folderCandidates = folderCandidates,
                onCreateFolder = onCreateFolder,
                onAddToFolder = onAddToFolder,
                drawerPresentation = drawerPresentation,
            )
        }
    }
}

/**
 * Not private: reused by the facet carousel's preview cards ([com.facetlauncher.app.ui.facets.FacetCarouselScreen])
 * so the dock renders identically there.
 *
 * [item] is either a standalone app ([PlacedItem.SingleApp], rendered exactly as before) or a
 * folder ([PlacedItem.FolderItem]) — a `medium`/12dp-shaped tile (CLAUDE.md's M3 shape table, tile
 * scale) showing a 2x2 mini-grid of its first 4 app icons. Tapping a folder tile opens
 * [FolderContentsSheet]; long-pressing it opens [com.facetlauncher.app.ui.components.FolderTileContextMenu]
 * (Rename only — no removal, see that composable's own doc) instead of [AppContextMenu] — a folder
 * has no app-info/uninstall/shortcuts of its own. Both are gated by [enableLongPressMenu] exactly
 * like [AppContextMenu] below, since the facet carousel's read-only preview cards aren't a place
 * you manage folders either.
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
internal fun DockIcon(
    item: PlacedItem,
    displayMode: DockDisplayMode,
    onClick: (AppInfo) -> Unit,
    modifier: Modifier = Modifier,
    badgeCount: Int? = null,
    badgeStyle: NotificationBadgeStyle = NotificationBadgeStyle.DOT,
    onRequestShortcuts: suspend (AppInfo) -> List<AppShortcut> = { emptyList() },
    onLaunchShortcut: (AppShortcut) -> Unit = {},
    onAppInfo: (AppInfo) -> Unit = {},
    onRequestQuickAddState: suspend (AppInfo) -> QuickAddState = { QuickAddState() },
    onFavoritesAction: (AppInfo, QuickPlacementAction) -> Unit = { _, _ -> },
    onDockAction: (AppInfo, QuickPlacementAction) -> Unit = { _, _ -> },
    onRequestFolderQuickAddState: suspend (Folder) -> QuickAddState = { QuickAddState() },
    onFolderFavoritesAction: (Folder, QuickPlacementAction) -> Unit = { _, _ -> },
    onFolderDockAction: (Folder, QuickPlacementAction) -> Unit = { _, _ -> },
    folderCandidates: List<Folder>? = null,
    onCreateFolder: (AppInfo, String) -> Unit = { _, _ -> },
    onAddToFolder: (AppInfo, Long) -> Unit = { _, _ -> },
    onRemoveFromFolder: (Long, AppInfo) -> Unit = { _, _ -> },
    onRenameFolder: (Long, String) -> Unit = { _, _ -> },
    drawerPresentation: DrawerPresentation = DrawerPresentation.LIST,
    labelColor: Color = HomeAppTextColor,
    labelFontWeight: FontWeight = FontWeight.Normal,
    // False for the facet carousel's read-only preview cards (see
    // com.facetlauncher.app.ui.facets.FacetCarouselScreen) — long-press there must not open
    // Home's real Uninstall/App Info/shortcuts menu, since the preview isn't a place you manage apps.
    enableLongPressMenu: Boolean = true,
) {
    when (item) {
        is PlacedItem.SingleApp -> SingleAppDockIcon(
            app = item.app,
            displayMode = displayMode,
            onClick = { onClick(item.app) },
            modifier = modifier,
            badgeCount = badgeCount,
            badgeStyle = badgeStyle,
            onRequestShortcuts = onRequestShortcuts,
            onLaunchShortcut = onLaunchShortcut,
            onAppInfo = onAppInfo,
            onRequestQuickAddState = onRequestQuickAddState,
            onFavoritesAction = onFavoritesAction,
            onDockAction = onDockAction,
            folderCandidates = folderCandidates,
            onCreateFolder = onCreateFolder,
            onAddToFolder = onAddToFolder,
            drawerPresentation = drawerPresentation,
            labelColor = labelColor,
            labelFontWeight = labelFontWeight,
            enableLongPressMenu = enableLongPressMenu,
        )
        is PlacedItem.FolderItem -> FolderDockIcon(
            folder = item.folder,
            displayMode = displayMode,
            onAppClick = onClick,
            modifier = modifier,
            labelColor = labelColor,
            labelFontWeight = labelFontWeight,
            onRequestShortcuts = onRequestShortcuts,
            onLaunchShortcut = onLaunchShortcut,
            onAppInfo = onAppInfo,
            onRequestQuickAddState = onRequestFolderQuickAddState,
            onFavoritesAction = onFolderFavoritesAction,
            onDockAction = onFolderDockAction,
            onRemoveFromFolder = onRemoveFromFolder,
            onRenameFolder = onRenameFolder,
            drawerPresentation = drawerPresentation,
            enableLongPressMenu = enableLongPressMenu,
        )
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun SingleAppDockIcon(
    app: AppInfo,
    displayMode: DockDisplayMode,
    onClick: () -> Unit,
    modifier: Modifier,
    badgeCount: Int?,
    badgeStyle: NotificationBadgeStyle,
    onRequestShortcuts: suspend (AppInfo) -> List<AppShortcut>,
    onLaunchShortcut: (AppShortcut) -> Unit,
    onAppInfo: (AppInfo) -> Unit = {},
    onRequestQuickAddState: suspend (AppInfo) -> QuickAddState,
    onFavoritesAction: (AppInfo, QuickPlacementAction) -> Unit,
    onDockAction: (AppInfo, QuickPlacementAction) -> Unit,
    folderCandidates: List<Folder>?,
    onCreateFolder: (AppInfo, String) -> Unit,
    onAddToFolder: (AppInfo, Long) -> Unit,
    drawerPresentation: DrawerPresentation,
    labelColor: Color,
    labelFontWeight: FontWeight,
    enableLongPressMenu: Boolean,
) {
    var menuExpanded by remember { mutableStateOf(false) }
    val clickModifier = if (enableLongPressMenu) {
        Modifier.longPressReleaseClickable(onClick = onClick, onLongPress = { menuExpanded = true })
    } else {
        Modifier.clickable(onClick = onClick)
    }
    Box(modifier = modifier) {
        // Treated as a straight boolean rather than an exhaustive `when` over DockDisplayMode:
        // LAUNCHER_DEFAULT never reaches this composable (callers always pass an already-resolved
        // effective value — see HomeUiState.activeDockDisplayMode/FacetCarouselViewModel's own
        // resolver), so it renders as Icons here the same as ICONS, without a case of its own.
        if (displayMode == DockDisplayMode.TEXT) {
            Row(
                modifier = clickModifier
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
        } else {
            Box(
                modifier = clickModifier,
            ) {
                AppIcon(
                    icon = app.icon,
                    size = AppIconSize.TILE,
                    contentDescription = app.label,
                    notificationCount = badgeCount,
                    badgeStyle = badgeStyle,
                )
            }
        }
        if (enableLongPressMenu) {
            AppContextMenu(
                app = app,
                expanded = menuExpanded,
                onDismissRequest = { menuExpanded = false },
                onRequestShortcuts = onRequestShortcuts,
                onLaunchShortcut = onLaunchShortcut,
                onAppInfo = onAppInfo,
                onRequestQuickAddState = onRequestQuickAddState,
                onFavoritesAction = onFavoritesAction,
                onDockAction = onDockAction,
                folderCandidates = folderCandidates,
                onCreateFolder = onCreateFolder,
                onAddToFolder = onAddToFolder,
                drawerPresentation = drawerPresentation,
            )
        }
    }
}

/** [PlacedItem.FolderItem]'s own tile — a mini-grid preview instead of a single app icon. See [DockIcon]'s own doc for tap/long-press behavior. */
@Composable
private fun FolderDockIcon(
    folder: Folder,
    displayMode: DockDisplayMode,
    onAppClick: (AppInfo) -> Unit,
    modifier: Modifier,
    labelColor: Color,
    labelFontWeight: FontWeight,
    onRequestShortcuts: suspend (AppInfo) -> List<AppShortcut>,
    onLaunchShortcut: (AppShortcut) -> Unit,
    onAppInfo: (AppInfo) -> Unit = {},
    onRequestQuickAddState: suspend (Folder) -> QuickAddState,
    onFavoritesAction: (Folder, QuickPlacementAction) -> Unit,
    onDockAction: (Folder, QuickPlacementAction) -> Unit,
    onRemoveFromFolder: (Long, AppInfo) -> Unit,
    onRenameFolder: (Long, String) -> Unit,
    drawerPresentation: DrawerPresentation,
    enableLongPressMenu: Boolean,
) {
    var sheetOpen by remember { mutableStateOf(false) }
    var menuExpanded by remember { mutableStateOf(false) }
    val clickModifier = if (enableLongPressMenu) {
        Modifier.longPressReleaseClickable(onClick = { sheetOpen = true }, onLongPress = { menuExpanded = true })
    } else {
        Modifier.clickable(onClick = {})
    }
    Box(modifier = modifier) {
        // See DockIcon's own identical treatment above for why this is a boolean, not an
        // exhaustive `when` over DockDisplayMode.
        if (displayMode == DockDisplayMode.TEXT) {
            Row(
                modifier = clickModifier
                    .testTag("dock_folder_tile_${folder.id}")
                    .padding(vertical = 14.dp, horizontal = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                Text(
                    text = folder.name,
                    style = MaterialTheme.typography.bodyMedium.copy(shadow = homeAppLabelShadow(labelColor), fontWeight = labelFontWeight),
                    color = labelColor,
                )
            }
        } else {
            Box(modifier = clickModifier.testTag("dock_folder_tile_${folder.id}")) {
                FolderTileGlyph(folder = folder)
            }
        }
        if (enableLongPressMenu && sheetOpen) {
            FolderContentsSheet(
                folder = folder,
                onDismissRequest = { sheetOpen = false },
                onAppClick = onAppClick,
                presentation = drawerPresentation,
                onRemoveFromFolder = onRemoveFromFolder,
                headerAction = FolderSheetHeaderAction.Rename(onRename = onRenameFolder),
                onRequestShortcuts = onRequestShortcuts,
                onLaunchShortcut = onLaunchShortcut,
                onAppInfo = onAppInfo,
            )
        }
        if (enableLongPressMenu) {
            FolderTileContextMenu(
                folder = folder,
                expanded = menuExpanded,
                onDismissRequest = { menuExpanded = false },
                onRename = onRenameFolder,
                onRequestQuickAddState = onRequestQuickAddState,
                onFavoritesAction = onFavoritesAction,
                onDockAction = onDockAction,
            )
        }
    }
}

/** A [AppIconSize.TILE]-sized, `medium`/12dp-shaped tile showing up to 4 of the folder's app icons — a 1x2 row for 1-2 apps, a 2x2 grid for 3-4 — CLAUDE.md's M3 shape table, tile scale. No inner padding: icons run to the tile's own edge so the outer rounded-corner clip crops their corners slightly, the same way a real folder preview reads as "full" rather than an icon floating in a frame. A 0-app folder (valid, persistent — never auto-deleted) renders a single centered muted folder glyph instead. */
@Composable
internal fun FolderTileGlyph(folder: Folder, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .size(AppIconSize.TILE)
            .clip(RoundedCornerShape(12.dp))
            .background(FolderGlyphBackground)
            .drawWithContent {
                drawContent()
                // Slight vignette for inset depth, rather than a flat plate.
                drawRect(
                    brush = Brush.radialGradient(
                        colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.18f)),
                        center = center,
                        radius = size.maxDimension * 0.75f,
                    ),
                )
            },
    ) {
        if (folder.apps.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = Icons.Outlined.FolderIcon,
                    contentDescription = null,
                    tint = Muted,
                    modifier = Modifier.size(AppIconSize.SHORTCUT),
                )
            }
        } else {
            val previewApps = folder.apps.take(4)
            if (previewApps.size <= 2) {
                Row(
                    modifier = Modifier.fillMaxSize(),
                    horizontalArrangement = Arrangement.spacedBy(2.dp, Alignment.CenterHorizontally),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    for (colIndex in 0..1) {
                        FolderTileGlyphSlot(app = previewApps.getOrNull(colIndex))
                    }
                }
            } else {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(2.dp, Alignment.CenterVertically),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    for (rowIndex in 0..1) {
                        Row(horizontalArrangement = Arrangement.spacedBy(2.dp, Alignment.CenterHorizontally)) {
                            for (colIndex in 0..1) {
                                FolderTileGlyphSlot(app = previewApps.getOrNull(rowIndex * 2 + colIndex))
                            }
                        }
                    }
                }
            }
        }
    }
}

/** One cell of [FolderTileGlyph]'s grid/row — an app icon, or a blank spacer when the folder has fewer apps than slots. */
@Composable
private fun FolderTileGlyphSlot(app: AppInfo?) {
    if (app != null) {
        AppIcon(icon = app.icon, size = AppIconSize.SHORTCUT, contentDescription = null, cornerRadius = 3.dp)
    } else {
        Box(modifier = Modifier.size(AppIconSize.SHORTCUT))
    }
}

/**
 * The app-list (Favorites) counterpart to [FolderDockIcon] — a row instead of a tile, mirroring
 * [AppRow]'s exact layout (icon + label, position/presentation-aware) since a folder can now
 * occupy a slot in the Favorites list, not just the Dock. Tapping opens [FolderContentsSheet];
 * long-pressing opens [FolderTileContextMenu] (Rename only), exactly like [FolderDockIcon].
 */
@Composable
internal fun FolderRow(
    folder: Folder,
    onAppClick: (AppInfo) -> Unit,
    onRequestShortcuts: suspend (AppInfo) -> List<AppShortcut>,
    onLaunchShortcut: (AppShortcut) -> Unit,
    onAppInfo: (AppInfo) -> Unit = {},
    onRemoveFromFolder: (Long, AppInfo) -> Unit,
    onRenameFolder: (Long, String) -> Unit,
    drawerPresentation: DrawerPresentation,
    modifier: Modifier = Modifier,
    onRequestQuickAddState: suspend (Folder) -> QuickAddState = { QuickAddState() },
    onFavoritesAction: (Folder, QuickPlacementAction) -> Unit = { _, _ -> },
    onDockAction: (Folder, QuickPlacementAction) -> Unit = { _, _ -> },
    position: AppRowPosition = AppRowPosition.LEFT,
    presentation: AppRowPresentation = AppRowPresentation.ICON_AND_TEXT,
    labelColor: Color = HomeAppTextColor,
    labelFontWeight: FontWeight = FontWeight.Normal,
    verticalPadding: Dp = HOME_APP_ROW_REGULAR_VERTICAL_PADDING,
    iconSize: Dp = AppIconSize.ROW_REGULAR,
    testTagPrefix: String = "home_",
    // False for the facet carousel's read-only preview cards — see [AppRow]'s own doc for why;
    // when disabled, a tap falls through to [onClick] (e.g. "apply this facet") instead of opening
    // the folder's own contents sheet, and no long-press menu is offered at all.
    enableLongPressMenu: Boolean = true,
    onClick: () -> Unit = {},
) {
    var sheetOpen by remember { mutableStateOf(false) }
    var menuExpanded by remember { mutableStateOf(false) }
    val showIcon = presentation != AppRowPresentation.TEXT_ONLY
    val showLabel = presentation != AppRowPresentation.ICON_ONLY
    val clickModifier = if (enableLongPressMenu) {
        Modifier.longPressReleaseClickable(onClick = { sheetOpen = true }, onLongPress = { menuExpanded = true })
    } else {
        Modifier.clickable(onClick = onClick)
    }
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(MaterialTheme.shapes.large)
            .then(clickModifier)
            .testTag("${testTagPrefix}folder_row_${folder.id}")
            .padding(horizontal = 8.dp, vertical = verticalPadding),
        verticalAlignment = Alignment.CenterVertically,
        // See AppRow's own identical `when` above for why LAUNCHER_DEFAULT falls in with LEFT here.
        horizontalArrangement = when (position) {
            AppRowPosition.RIGHT -> Arrangement.spacedBy(14.dp, Alignment.End)
            AppRowPosition.CENTER -> Arrangement.spacedBy(14.dp, Alignment.CenterHorizontally)
            AppRowPosition.LEFT, AppRowPosition.LAUNCHER_DEFAULT -> Arrangement.spacedBy(14.dp)
        },
    ) {
        val icon: @Composable () -> Unit = {
            if (showIcon) FolderTileGlyph(folder = folder, modifier = Modifier.size(iconSize))
        }
        val label: @Composable () -> Unit = {
            if (showLabel) {
                Text(
                    text = folder.name,
                    style = MaterialTheme.typography.titleMedium.copy(shadow = homeAppLabelShadow(labelColor), fontWeight = labelFontWeight),
                    color = labelColor,
                )
            }
        }
        if (position == AppRowPosition.RIGHT) {
            label()
            icon()
        } else {
            icon()
            label()
        }
    }
    if (enableLongPressMenu && sheetOpen) {
        FolderContentsSheet(
            folder = folder,
            onDismissRequest = { sheetOpen = false },
            onAppClick = onAppClick,
            presentation = drawerPresentation,
            onRemoveFromFolder = onRemoveFromFolder,
            headerAction = FolderSheetHeaderAction.Rename(onRename = onRenameFolder),
            onRequestShortcuts = onRequestShortcuts,
            onLaunchShortcut = onLaunchShortcut,
            onAppInfo = onAppInfo,
        )
    }
    if (enableLongPressMenu) {
        FolderTileContextMenu(
            folder = folder,
            expanded = menuExpanded,
            onDismissRequest = { menuExpanded = false },
            onRename = onRenameFolder,
            onRequestQuickAddState = onRequestQuickAddState,
            onFavoritesAction = onFavoritesAction,
            onDockAction = onDockAction,
        )
    }
}

/** Every [FolderTileGlyph] slot count side by side: 0 (muted glyph fallback), 1-2 (1x2 row), 3-4 (2x2 grid). */
@Preview(name = "Folder glyph", showBackground = true)
@Preview(name = "Folder glyph - Dark", showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun FolderTileGlyphPreview() {
    FacetLauncherTheme {
        val apps = (1..4).map {
            AppInfo(packageName = "com.example.app$it", activityName = ".MainActivity", label = "App $it", icon = null)
        }
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.padding(16.dp)) {
            for (count in 0..4) {
                FolderTileGlyph(folder = Folder(id = count.toLong(), name = "Folder", apps = apps.take(count)))
            }
        }
    }
}

@Preview(showBackground = true, widthDp = 390, heightDp = 844)
@Preview(name = "Dark", showBackground = true, widthDp = 390, heightDp = 844, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun HomeScreenPreview() {
    FacetLauncherTheme {
        val apps = (1..9).map {
            AppInfo(packageName = "com.example.app$it", activityName = ".MainActivity", label = "App $it", icon = null)
        }
        HomeScreen(
            appListItems = apps.take(5).map { PlacedItem.SingleApp(it) },
            dockApps = apps.drop(5).take(4).map { PlacedItem.SingleApp(it) },
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
    FacetLauncherTheme {
        val apps = (1..5).map {
            AppInfo(packageName = "com.example.app$it", activityName = ".MainActivity", label = "App $it", icon = null)
        }
        HomeScreen(
            appListItems = apps.map { PlacedItem.SingleApp(it) },
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
    FacetLauncherTheme {
        val apps = (1..5).map {
            AppInfo(packageName = "com.example.app$it", activityName = ".MainActivity", label = "App $it", icon = null)
        }
        HomeScreen(
            appListItems = apps.map { PlacedItem.SingleApp(it) },
            dockApps = emptyList(),
            onAppClick = {},
            appRowPresentation = AppRowPresentation.TEXT_ONLY,
        )
    }
}
