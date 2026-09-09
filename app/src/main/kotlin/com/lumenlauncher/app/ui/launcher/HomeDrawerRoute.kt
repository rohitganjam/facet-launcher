package com.lumenlauncher.app.ui.launcher

import android.content.ContentUris
import android.content.Intent
import android.net.Uri
import android.provider.AlarmClock
import android.provider.CalendarContract
import android.provider.Settings
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.util.VelocityTracker
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.lumenlauncher.app.data.model.AppInfo
import com.lumenlauncher.app.data.model.DrawerPresentation
import com.lumenlauncher.app.ui.drawer.AppDrawerScreen
import com.lumenlauncher.app.ui.drawer.DrawerViewModel
import com.lumenlauncher.app.ui.home.ClockAdjustMode
import com.lumenlauncher.app.ui.home.HomeScreen
import com.lumenlauncher.app.ui.home.HomeViewModel
import com.lumenlauncher.app.ui.hub.HubScreen
import com.lumenlauncher.app.ui.hub.HubViewModel
import com.lumenlauncher.app.ui.hub.picker.HubWidgetPickerScreen
import com.lumenlauncher.app.ui.hub.picker.HubWidgetPickerViewModel
import com.lumenlauncher.app.ui.theme.resolve
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlin.math.abs

/** README's drawer-transform timing/easing (`.34s cubic-bezier(.32,.72,0,1)`), reused for the settle animation. */
private val DRAWER_SETTLE_SPEC = tween<Float>(durationMillis = 340, easing = CubicBezierEasing(0.32f, 0.72f, 0f, 1f))

/** A drag needs to travel at least this fraction of the container size, in either direction, to commit open/closed. */
private const val COMMIT_TRAVEL_FRACTION = 0.20f
private const val VELOCITY_THRESHOLD_PX = 1000f
private const val HOME_FADE_SCALE_RANGE = 0.03f

/** README's swipe-up-opens-drawer distance (`>55px`), reused as the swipe-down-opens-shade distance — a downward swipe starting from a fully closed drawer that clears either this or [VELOCITY_THRESHOLD_PX] expands the notification shade instead of just springing back. */
private val SWIPE_DOWN_SHADE_DISTANCE = 55.dp

/**
 * One follow-finger open/close axis — [progress] is 0f (closed) to 1f (fully open), driven by
 * [dragBy] during a drag and resolved to 0f/1f by [settle] on release. Two independent instances
 * back this route: Home↔Drawer (vertical) and Home↔Hub (horizontal) — pulled out to a shared
 * class instead of duplicating this bookkeeping inline twice.
 */
private class SwipeAxisState(private val containerSizePx: () -> Float, private val coroutineScope: CoroutineScope) {
    val progress = Animatable(0f)
    var dragStartProgress = 0f
        private set
    var dragTargetProgress = 0f
        private set

    // Must be observable Compose state, not a plain var — the "clear focus while drag-closing"
    // LaunchedEffect below is keyed on this, and a plain var's mutation doesn't itself trigger
    // recomposition, so that effect could silently never re-fire mid-drag (see chat history).
    var dragActive by mutableStateOf(false)
        private set

    fun dragBy(deltaPx: Float) {
        if (!dragActive) {
            dragActive = true
            dragStartProgress = progress.value
            dragTargetProgress = progress.value
        }
        val size = containerSizePx()
        dragTargetProgress = (dragTargetProgress + if (size > 0f) deltaPx / size else 0f).coerceIn(0f, 1f)
        coroutineScope.launch { progress.snapTo(dragTargetProgress) }
    }

    fun settle(velocity: Float = 0f) {
        val currentProgress = if (dragActive) dragTargetProgress else progress.value
        val traveled = currentProgress - dragStartProgress
        val target = when {
            velocity > VELOCITY_THRESHOLD_PX -> 1f
            velocity < -VELOCITY_THRESHOLD_PX -> 0f
            traveled >= COMMIT_TRAVEL_FRACTION -> 1f
            traveled <= -COMMIT_TRAVEL_FRACTION -> 0f
            dragStartProgress > 0.5f -> 1f
            else -> 0f
        }
        dragActive = false
        coroutineScope.launch { progress.animateTo(target, DRAWER_SETTLE_SPEC) }
    }

    suspend fun close() {
        dragActive = false
        progress.animateTo(0f, DRAWER_SETTLE_SPEC)
    }
}

/** Which direction a not-yet-locked drag on Home resolved to, once it clears touch slop. */
private enum class Axis { VERTICAL, HORIZONTAL }

/**
 * Home + Drawer + Hub, merged into one composable/route rather than separate `NavHost`
 * destinations. A follow-finger drag needs continuous, cancellable animation (an [Animatable]
 * driving each layer's position every frame) — `NavHost`'s declarative enter/exit transitions
 * only animate already-committed navigation events, and F10 already frames the Drawer as "an
 * overlay on top of the home background," not a distinct screen; the Hub (F5, to Home's left)
 * follows the same model on its own horizontal axis. Dragging up on Home (or down on the
 * Drawer's own list, only once scrolled to the top) opens/closes the Drawer; dragging right on
 * Home (or left on the Hub) opens/closes the Hub — whichever direction a drag on Home's own
 * surface resolves to first wins for that gesture (see [Axis]), so a diagonal swipe can't
 * trigger both. Releasing commits open/closed once the drag has travelled at least
 * [COMMIT_TRAVEL_FRACTION] of the container size *from wherever this particular drag started*
 * (not a fixed 50% of the full range — that felt too hard to trigger); anything short of that
 * springs back to that starting state instead.
 */
@Composable
fun HomeDrawerRoute(
    apps: List<AppInfo>,
    onAppClick: (AppInfo) -> Unit,
    onNavigateToSettings: () -> Unit,
    onNavigateToProfileCarousel: () -> Unit,
    onNavigateToUsageAccessExplanation: () -> Unit,
    /** Navigates to the clock style gallery, either global (null) or profile-scoped. */
    onNavigateToClockStyleGallery: (Long?) -> Unit = { _ -> },
    modifier: Modifier = Modifier,
    homeViewModel: HomeViewModel = hiltViewModel(),
    drawerViewModel: DrawerViewModel = hiltViewModel(),
    hubViewModel: HubViewModel = hiltViewModel(),
    widgetPickerViewModel: HubWidgetPickerViewModel = hiltViewModel(),
    launcherViewModel: LauncherViewModel,
) {
    val homeUiState by homeViewModel.uiState.collectAsStateWithLifecycle()
    val drawerSettings by drawerViewModel.settings.collectAsStateWithLifecycle()
    val contactResults by drawerViewModel.contactResults.collectAsStateWithLifecycle()
    val drawerBadgeCounts by drawerViewModel.badgeCounts.collectAsStateWithLifecycle()
    val showContactsPermissionPrompt by drawerViewModel.showContactsPermissionPrompt.collectAsStateWithLifecycle()

    if (homeUiState.isLoading) {
        // Real Home state (dock/app list, theme-driven colors and fonts) hasn't loaded yet —
        // render nothing rather than a frame styled with defaults, mirroring LauncherActivity's
        // own gate (see chat history and HomeViewModel's own doc).
        Box(modifier = modifier.fillMaxSize())
        return
    }

    // PACKAGE_USAGE_STATS has no grant-change callback — re-check whenever the user returns to
    // Home (e.g. from the usage-access Settings redirect) rather than only on profile changes.
    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) homeViewModel.refresh()
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }
    val listState = rememberLazyListState()
    val gridState = rememberLazyGridState()
    val coroutineScope = rememberCoroutineScope()
    val focusManager = LocalFocusManager.current

    var containerHeightPx by remember { mutableFloatStateOf(2000f) }
    var containerWidthPx by remember { mutableFloatStateOf(1000f) }
    val drawerAxis = remember { SwipeAxisState({ containerHeightPx }, coroutineScope) }
    val hubAxis = remember { SwipeAxisState({ containerWidthPx }, coroutineScope) }

    LaunchedEffect(launcherViewModel) {
        launcherViewModel.homePressedEvent.collect {
            focusManager.clearFocus()
            drawerAxis.close()
            hubAxis.close()
        }
    }
    // Rendered in-place as an overlay below (not a NavHost destination — see chat history):
    // navigating away would tear down this whole composable, resetting hubAxis and losing Hub's
    // own open/scroll state, and would visually replace Hub entirely instead of loading over it.
    var showWidgetPicker by remember { mutableStateOf(false) }
    var clockAdjustMode by remember { mutableStateOf(ClockAdjustMode.NONE) }
    var draggingHandle by remember { mutableStateOf(false) }
    var drawerQuery by remember { mutableStateOf("") }
    var homeDragStartedClosed by remember { mutableStateOf(false) }
    var homeRawDragDistance by remember { mutableFloatStateOf(0f) }
    var homeDragAxis by remember { mutableStateOf<Axis?>(null) }
    val velocityTracker = remember { VelocityTracker() }
    val context = LocalContext.current
    val density = LocalDensity.current
    val swipeDownShadeDistancePx = with(density) { SWIPE_DOWN_SHADE_DISTANCE.toPx() }

    val nestedScrollConnection = remember(listState, gridState, drawerSettings.drawerPresentation) {
        object : NestedScrollConnection {
            override fun onPreScroll(available: Offset, source: NestedScrollSource): Offset {
                // Fling-sourced deltas are the list's own post-release deceleration, not the
                // user's finger — once onPreFling below has resolved open/closed via settle(),
                // letting those leftover frames keep calling dragBy() would re-arm dragActive
                // and snapTo() over the in-flight settle animation, stranding the drawer
                // wherever the residual fling happened to decay to (the "low-velocity fling
                // stalls partway" bug). Only real finger movement should drive the drag.
                if (source != NestedScrollSource.Drag) return Offset.Zero
                val atTop = if (drawerSettings.drawerPresentation == DrawerPresentation.GRID) {
                    gridState.firstVisibleItemIndex == 0 && gridState.firstVisibleItemScrollOffset == 0
                } else {
                    listState.firstVisibleItemIndex == 0 && listState.firstVisibleItemScrollOffset == 0
                }
                if (available.y > 0f && atTop && drawerAxis.progress.value > 0f) {
                    drawerAxis.dragBy(-available.y)
                    return available
                }
                return Offset.Zero
            }

            override suspend fun onPreFling(available: androidx.compose.ui.unit.Velocity): androidx.compose.ui.unit.Velocity {
                if (drawerAxis.dragActive) {
                    drawerAxis.settle(velocity = -available.y)
                }
                return super.onPreFling(available)
            }
        }
    }

    val isDrawerOpen by remember { derivedStateOf { drawerAxis.progress.value > 0f } }
    val isHubOpen by remember { derivedStateOf { hubAxis.progress.value > 0f } }

    // AppWidgetHost.startListening() is what actually makes hosted AppWidgetHostViews render/
    // update — without it, widget tiles stay blank. Only listen while the Hub is actually
    // visible, matching every other AppWidgetHost caller's lifecycle discipline.
    DisposableEffect(isHubOpen) {
        if (isHubOpen) hubViewModel.onHubVisible()
        onDispose { if (isHubOpen) hubViewModel.onHubHidden() }
    }

    // Clear focus whenever the drawer or hub starts closing via a drag.
    LaunchedEffect(drawerAxis.dragActive, hubAxis.dragActive) {
        if (drawerAxis.dragActive || hubAxis.dragActive) {
            focusManager.clearFocus()
        }
    }

    // Reset drawer state when it's fully closed.
    LaunchedEffect(isDrawerOpen) {
        if (!isDrawerOpen) {
            drawerQuery = ""
            drawerViewModel.onQueryChanged("")
            listState.scrollToItem(0)
            gridState.scrollToItem(0)
        }
    }

    // Reset hub widget picker state when it's fully closed.
    LaunchedEffect(isHubOpen) {
        if (!isHubOpen) {
            showWidgetPicker = false
            widgetPickerViewModel.reset()
        }
    }

    // Cancel clock adjustment when navigating away from Home.
    LaunchedEffect(isDrawerOpen, isHubOpen) {
        if (isDrawerOpen || isHubOpen) {
            clockAdjustMode = ClockAdjustMode.NONE
        }
    }

    BackHandler(enabled = isDrawerOpen || isHubOpen || showWidgetPicker) {
        focusManager.clearFocus()
        when {
            showWidgetPicker -> showWidgetPicker = false
            isDrawerOpen -> coroutineScope.launch { drawerAxis.close() }
            isHubOpen -> coroutineScope.launch { hubAxis.close() }
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .onSizeChanged {
                containerHeightPx = it.height.toFloat()
                containerWidthPx = it.width.toFloat()
            },
    ) {
        HomeScreen(
            appListItems = homeUiState.appListItems,
            listContentMode = homeUiState.activeListContentMode,
            appRowPosition = homeUiState.activeAppRowPosition,
            appRowPresentation = homeUiState.activeAppRowPresentation,
            showUsageAccessPrompt = homeUiState.showUsageAccessPrompt,
            onUsageAccessPromptClick = {
                homeViewModel.dismissUsageAccessPrompt()
                onNavigateToUsageAccessExplanation()
            },
            dockApps = homeUiState.dockApps,
            dockDisplayMode = homeUiState.settings.dockDisplayMode,
            notificationBadgeStyle = homeUiState.settings.notificationBadgeStyle,
            badgeCounts = homeUiState.badgeCounts,
            use24HourTime = homeUiState.effectiveUse24HourTime,
            clockTemplateId = homeUiState.clockTemplateId,
            clockFontOption = homeUiState.clockFontOption,
            clockColorOption = homeUiState.clockColorOption,
            clockAccentColorOption = homeUiState.clockAccentColorOption,
            clockShowMeridiem = homeUiState.clockShowMeridiem,
            clockDateStyle = homeUiState.clockDateStyle,
            clockAlignment = homeUiState.clockAlignment,
            calendarAlignment = homeUiState.calendarAlignment,
            clockZoneHeightDp = homeUiState.clockZoneHeightDp,
            onClockZoneHeightCommit = homeViewModel::onClockZoneHeightCommit,
            clockScale = homeUiState.clockScale,
            clockPositionOwnerProfileId = homeUiState.clockPositionOwningProfile?.id,
            clockAdjustMode = clockAdjustMode,
            onAdjustModeChange = { clockAdjustMode = it },
            draggingHandle = draggingHandle,
            onDraggingHandleChange = { draggingHandle = it },
            onClockScaleCommit = homeViewModel::onClockScaleCommit,
            onEditClockStyles = {
                clockAdjustMode = ClockAdjustMode.NONE
                onNavigateToClockStyleGallery(homeUiState.clockPositionOwningProfile?.id)
            },
            appListVerticalAlignment = homeUiState.activeAppListVerticalAlignment,
            calendarEvents = homeUiState.calendarEvents,
            calendarColors = homeUiState.settings.calendarColors,
            calendarFontOption = homeUiState.activeCalendarFontOption,
            calendarColorOption = homeUiState.activeCalendarColorOption,
            calendarFontWeight = homeUiState.activeCalendarFontWeight,
            onEventClick = { event ->
                val uri = ContentUris.withAppendedId(CalendarContract.Events.CONTENT_URI, event.id)
                runCatching { context.startActivity(Intent(Intent.ACTION_VIEW, uri)) }
            },
            onClockClick = {
                // AlarmClock.ACTION_SHOW_ALARMS is a semantic "jump straight to the alarms list"
                // intent, by design — on this Samsung/One UI build it resolves to a narrow,
                // single-purpose alarms screen with none of the Clock app's own tab navigation
                // (Alarm/Clock/Timer/Stopwatch), unlike AOSP's Clock which still shows its full
                // shell there. So: use that intent only to find WHICH app is "the clock app" on
                // this device, then launch that package's own normal entry point instead — the
                // same way tapping its icon would, landing on its full navigable UI rather than
                // the walled-off alarms subview. No fallback to the alarms intent itself — every
                // real clock app declares a launcher activity, so resolveActivity() finding a
                // package but getLaunchIntentForPackage() returning null for it doesn't happen.
                runCatching {
                    Intent(AlarmClock.ACTION_SHOW_ALARMS).resolveActivity(context.packageManager)
                        ?.packageName
                        ?.let { context.packageManager.getLaunchIntentForPackage(it) }
                        ?.let { context.startActivity(it) }
                }
            },
            onAppClick = onAppClick,
            launcherFontOption = homeUiState.settings.launcherFontOption,
            appLabelColorOption = homeUiState.settings.appLabelColorOption,
            homeAppsFontWeight = homeUiState.settings.homeAppsFontWeight,
            onRequestShortcuts = drawerViewModel::getShortcuts,
            onLaunchShortcut = drawerViewModel::launchShortcut,
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer {
                    alpha = 1f - drawerAxis.progress.value
                    val scale = 1f - drawerAxis.progress.value * HOME_FADE_SCALE_RANGE
                    scaleX = scale
                    scaleY = scale
                }
                .pointerInput(draggingHandle, clockAdjustMode) {
                    // Suppress screen-wide gestures if we're in any adjustment mode
                    // or actively dragging a handle.
                    if (draggingHandle || clockAdjustMode != ClockAdjustMode.NONE) return@pointerInput

                    detectDragGestures(
                        onDragStart = {
                            homeDragAxis = null
                            velocityTracker.resetTracking()
                            homeDragStartedClosed = drawerAxis.progress.value == 0f
                            homeRawDragDistance = 0f
                        },
                        onDragEnd = {
                            when (homeDragAxis) {
                                Axis.VERTICAL -> {
                                    val velocity = velocityTracker.calculateVelocity().y
                                    drawerAxis.settle(velocity = -velocity)
                                    if (homeDragStartedClosed &&
                                        (homeRawDragDistance >= swipeDownShadeDistancePx || velocity >= VELOCITY_THRESHOLD_PX)
                                    ) {
                                        homeViewModel.expandNotificationShade()
                                    }
                                }
                                Axis.HORIZONTAL -> hubAxis.settle(velocity = velocityTracker.calculateVelocity().x)
                                null -> Unit
                            }
                        },
                        onDragCancel = {
                            when (homeDragAxis) {
                                Axis.VERTICAL -> drawerAxis.settle()
                                Axis.HORIZONTAL -> hubAxis.settle()
                                null -> Unit
                            }
                        },
                    ) { change, dragAmount ->
                        // First post-touch-slop movement decides the axis for the rest of this
                        // gesture — whichever of x/y is larger wins, so a diagonal swipe commits
                        // to one axis instead of driving both.
                        if (homeDragAxis == null) {
                            homeDragAxis = if (abs(dragAmount.x) > abs(dragAmount.y)) Axis.HORIZONTAL else Axis.VERTICAL
                        }
                        // If a screen gesture successfully starts (crosses slop) while we're in
                        // an adjustment mode (but not holding a handle), cancel adjustment.
                        if (clockAdjustMode != ClockAdjustMode.NONE) {
                            clockAdjustMode = ClockAdjustMode.NONE
                        }

                        when (homeDragAxis) {
                            Axis.VERTICAL -> {
                                drawerAxis.dragBy(-dragAmount.y)
                                homeRawDragDistance += dragAmount.y
                                velocityTracker.addPosition(change.uptimeMillis, change.position)
                            }
                            Axis.HORIZONTAL -> hubAxis.dragBy(dragAmount.x)
                            null -> Unit
                        }
                        change.consume()
                    }
                }
                // README specifies a 420ms/8px long-press. detectTapGestures's onLongPress is
                // Compose's own primitive for exactly this — it uses the platform's default
                // long-press timeout (~500ms) and touch slop instead of the literal 420ms/8px,
                // a close enough match that it isn't perceptible. Goes straight to the profile
                // carousel now — the options sheet this used to open is gone (see chat history);
                // Launcher settings and Change wallpaper moved to the carousel screen itself and
                // Settings respectively.
                .pointerInput(Unit) {
                    detectTapGestures(onLongPress = { onNavigateToProfileCarousel() })
                },
        )

        // Hub — sits to Home's left: off-screen at progress 0, flush with the screen at progress
        // 1. Drawn after HomeScreen so it slides in ON TOP of Home (matching AppDrawerScreen
        // below), not behind it. Uses its own, much fainter default opacity than the Drawer
        // (see HubScreen's own doc comment) rather than drawerSettings.drawerOpacity — the two
        // are deliberately independent. There's no separate scrollable content to hand a
        // NestedScrollConnection to yet (HubGrid's own vertical scroll and this horizontal
        // close-drag don't conflict since they're on different axes), so a direct drag detector
        // on the Hub's own surface is enough to close it, mirroring the Drawer's closed-state
        // opening gesture on Home.
        Box(
            modifier = Modifier
                .fillMaxSize()
                .offset { IntOffset(((hubAxis.progress.value - 1f) * containerWidthPx).toInt(), 0) }
                .pointerInput(Unit) {
                    detectHorizontalDragGestures(
                        onDragEnd = { hubAxis.settle() },
                        onDragCancel = { hubAxis.settle() },
                    ) { change, dragAmount ->
                        hubAxis.dragBy(dragAmount)
                        change.consume()
                    }
                },
        ) {
            HubScreen(
                onAddClick = { showWidgetPicker = true },
                onManageClick = {}, // wired once there's a dedicated manage flow
                modifier = Modifier.fillMaxSize().testTag("hub_screen"),
                viewModel = hubViewModel,
            )
        }

        AppDrawerScreen(
            apps = apps,
            onAppClick = { app ->
                focusManager.clearFocus()
                onAppClick(app)
                coroutineScope.launch { drawerAxis.close() }
            },
            listState = listState,
            gridState = gridState,
            presentation = drawerSettings.drawerPresentation,
            gridSize = drawerSettings.drawerGridSize,
            listItemSize = drawerSettings.drawerListItemSize,
            showIcons = drawerSettings.showDrawerIcons,
            showLabels = drawerSettings.showDrawerLabels,
            labelFontWeight = drawerSettings.homeAppsFontWeight.resolve(),
            opacity = drawerSettings.drawerOpacity,
            notificationBadgeStyle = drawerSettings.notificationBadgeStyle,
            badgeCounts = drawerBadgeCounts,
            query = drawerQuery,
            onQueryChanged = { drawerQuery = it; drawerViewModel.onQueryChanged(it) },
            searchBarPosition = drawerSettings.searchBarPosition,
            onNavigateToSettings = onNavigateToSettings,
            contacts = contactResults,
            onRequestShortcuts = drawerViewModel::getShortcuts,
            onLaunchShortcut = drawerViewModel::launchShortcut,
            onRequestConnections = drawerViewModel::getConnections,
            showContactsPermissionPrompt = showContactsPermissionPrompt,
            onContactsPermissionPromptClick = {
                drawerViewModel.dismissContactsPermissionPrompt()
                context.startActivity(
                    Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.fromParts("package", context.packageName, null)),
                )
            },
            modifier = Modifier
                .fillMaxSize()
                .offset { IntOffset(0, ((1f - drawerAxis.progress.value) * containerHeightPx).toInt()) }
                .nestedScroll(nestedScrollConnection),
        )

        // Add-widget picker — an in-place overlay (see the showWidgetPicker declaration above for
        // why), sliding in over the Hub the same direction a NavHost push would have, so it still
        // reads as "opening a sub-screen" even though Hub itself never leaves composition.
        AnimatedVisibility(
            visible = showWidgetPicker,
            enter = slideInHorizontally(initialOffsetX = { it }, animationSpec = tween(340, easing = CubicBezierEasing(0.32f, 0.72f, 0f, 1f))),
            exit = slideOutHorizontally(targetOffsetX = { it }, animationSpec = tween(340, easing = CubicBezierEasing(0.32f, 0.72f, 0f, 1f))),
            modifier = Modifier.testTag("hub_widget_picker_overlay"),
        ) {
            HubWidgetPickerScreen(
                onDone = { showWidgetPicker = false },
                viewModel = widgetPickerViewModel,
            )
        }
    }
}
