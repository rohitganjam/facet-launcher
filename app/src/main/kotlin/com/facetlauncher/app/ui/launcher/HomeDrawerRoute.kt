package com.facetlauncher.app.ui.launcher

import android.content.ContentUris
import android.content.Intent
import android.net.Uri
import android.provider.AlarmClock
import android.provider.CalendarContract
import android.provider.Settings
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
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
import androidx.compose.ui.graphics.BlurEffect
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.PointerInputChange
import androidx.compose.ui.input.pointer.PointerInputScope
import androidx.compose.ui.input.pointer.changedToUpIgnoreConsumed
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.positionChange
import androidx.compose.ui.input.pointer.util.VelocityTracker
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.facetlauncher.app.data.PrivateSpaceState
import com.facetlauncher.app.data.model.AppInfo
import com.facetlauncher.app.data.model.DrawerPresentation
import com.facetlauncher.app.ui.components.GestureHintOverlay
import com.facetlauncher.app.ui.drawer.AppDrawerScreen
import com.facetlauncher.app.ui.drawer.DrawerViewModel
import com.facetlauncher.app.ui.drawer.PrivateSpaceScreen
import com.facetlauncher.app.ui.drawer.PrivateSpaceViewModel
import com.facetlauncher.app.ui.home.ClockAdjustMode
import com.facetlauncher.app.ui.home.HomeScreen
import com.facetlauncher.app.ui.home.HomeViewModel
import com.facetlauncher.app.ui.hub.HubScreen
import com.facetlauncher.app.ui.hub.HubViewModel
import com.facetlauncher.app.ui.hub.picker.HubWidgetPickerScreen
import com.facetlauncher.app.ui.hub.picker.HubWidgetPickerViewModel
import com.facetlauncher.app.ui.facets.FacetCarouselScreen
import com.facetlauncher.app.ui.facets.FacetCarouselViewModel
import com.facetlauncher.app.ui.onboarding.SetDefaultLauncherSheet
import com.facetlauncher.app.ui.theme.FACET_TRANSITION_DURATION_MS
import com.facetlauncher.app.ui.theme.FacetTransitionEasing
import com.facetlauncher.app.ui.theme.resolve
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlin.math.abs

/** README's drawer-transform timing/easing (`.34s cubic-bezier(.32,.72,0,1)`), reused for the settle animation. */
private val DRAWER_SETTLE_SPEC = tween<Float>(durationMillis = FACET_TRANSITION_DURATION_MS, easing = FacetTransitionEasing)

/** A drag needs to travel at least this fraction of the container size, in either direction, to commit open/closed. */
private const val COMMIT_TRAVEL_FRACTION = 0.20f
private const val VELOCITY_THRESHOLD_PX = 1000f
private const val HOME_FADE_SCALE_RANGE = 0.03f

/** Home blurs behind the Hub/Switch Facets panels (not the Drawer — see README's "Not blurred"), scaling with whichever axis's progress is furthest open. */
private val HOME_PANEL_BLUR_RADIUS = 24.dp

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
 * Same drag-gesture semantics as [androidx.compose.foundation.gestures.detectDragGestures], but
 * watches every event at [PointerEventPass.Initial] instead of the default (post-child) Main pass
 * — otherwise a finger coming down on a descendant's own clickable/combinedClickable (an app
 * icon's tap-or-long-press detector, e.g. [com.facetlauncher.app.ui.home.AppRow]) claims the touch
 * before this ancestor's Main-pass detector ever sees it, so a swipe starting on top of a home
 * screen app icon was silently swallowed and only empty space between icons could open the
 * Drawer/Hub/carousel. Below touch slop nothing is consumed, so a plain tap or long-press on that
 * icon still reaches it untouched; only once the drag actually commits does this detector start
 * winning the race — the same Initial-pass technique already used by
 * [com.facetlauncher.app.ui.hub.detectGrabOrResizeGesture] for Hub widget tiles.
 */
private suspend fun PointerInputScope.detectHomeSwipeGestures(
    onDragStart: () -> Unit,
    onDragEnd: () -> Unit,
    onDragCancel: () -> Unit,
    onDrag: (change: PointerInputChange, dragAmount: Offset) -> Unit,
) {
    val slop = viewConfiguration.touchSlop
    awaitEachGesture {
        val down = awaitFirstDown(requireUnconsumed = false)
        var accumulated = Offset.Zero
        var dragging = false
        var completedCleanly = true
        while (true) {
            val event = awaitPointerEvent(PointerEventPass.Initial)
            val change = event.changes.firstOrNull { it.id == down.id }
            if (change == null) {
                completedCleanly = false
                break
            }
            if (change.changedToUpIgnoreConsumed()) break
            if (!dragging) {
                accumulated += change.positionChange()
                if (accumulated.getDistance() > slop) {
                    dragging = true
                    change.consume()
                    onDragStart()
                    onDrag(change, accumulated)
                }
            } else {
                val delta = change.positionChange()
                change.consume()
                onDrag(change, delta)
            }
        }
        if (dragging) {
            if (completedCleanly) onDragEnd() else onDragCancel()
        }
    }
}

/**
 * Home + Drawer + Hub + Switch Facets, merged into one composable/route rather than separate
 * `NavHost` destinations. A follow-finger drag needs continuous, cancellable animation (an
 * [Animatable] driving each layer's position every frame) — `NavHost`'s declarative enter/exit
 * transitions only animate already-committed navigation events, and F10 already frames the
 * Drawer as "an overlay on top of the home background," not a distinct screen; the Hub (F5, to
 * Home's left) and the Switch Facets carousel (to Home's right) follow the same model, each on
 * its own share of the horizontal axis. Dragging up on Home (or down on the Drawer's own list,
 * only once scrolled to the top) opens/closes the Drawer; dragging right on Home (or left on the
 * Hub) opens/closes the Hub; dragging left on Home (or right on the carousel, in empty space or
 * on its first page's card — see [FacetCarouselScreen]'s own doc) opens/closes the carousel —
 * whichever direction a drag on Home's own surface resolves to first wins for that gesture (see
 * [Axis]), and whichever of Hub/carousel a horizontal drag engages first (by its very first
 * frame's direction) keeps owning it for the rest of the gesture even through a reversal, so a
 * diagonal or wavering swipe never drives two surfaces at once. Releasing commits open/closed
 * once the drag has travelled at least [COMMIT_TRAVEL_FRACTION] of the container size *from
 * wherever this particular drag started* (not a fixed 50% of the full range — that felt too hard
 * to trigger); anything short of that springs back to that starting state instead.
 */
@Composable
fun HomeDrawerRoute(
    apps: List<AppInfo>,
    onAppClick: (AppInfo) -> Unit,
    onNavigateToSettings: () -> Unit,
    onNavigateToFacetSettings: (facetId: Long) -> Unit,
    onNavigateToManageFacets: () -> Unit,
    onNavigateToUsageAccessExplanation: () -> Unit,
    /** Navigates to the clock style gallery, either global (null) or facet-scoped. */
    onNavigateToClockStyleGallery: (Long?) -> Unit = { _ -> },
    modifier: Modifier = Modifier,
    homeViewModel: HomeViewModel = hiltViewModel(),
    drawerViewModel: DrawerViewModel = hiltViewModel(),
    privateSpaceViewModel: PrivateSpaceViewModel = hiltViewModel(),
    hubViewModel: HubViewModel = hiltViewModel(),
    widgetPickerViewModel: HubWidgetPickerViewModel = hiltViewModel(),
    facetViewModel: FacetCarouselViewModel = hiltViewModel(),
    launcherViewModel: LauncherViewModel,
) {
    val homeUiState by homeViewModel.uiState.collectAsStateWithLifecycle()
    val launcherUiState by launcherViewModel.uiState.collectAsStateWithLifecycle()
    val drawerSettings by drawerViewModel.settings.collectAsStateWithLifecycle()
    val contactResults by drawerViewModel.contactResults.collectAsStateWithLifecycle()
    val settingsResults by drawerViewModel.settingsResults.collectAsStateWithLifecycle()
    val drawerBadgeCounts by drawerViewModel.badgeCounts.collectAsStateWithLifecycle()
    val showContactsPermissionPrompt by drawerViewModel.showContactsPermissionPrompt.collectAsStateWithLifecycle()
    val showContactsSettingPrompt by drawerViewModel.showContactsSettingPrompt.collectAsStateWithLifecycle()
    val privateSpaceState by drawerViewModel.privateSpaceState.collectAsStateWithLifecycle()
    val privateSpaceApps by privateSpaceViewModel.filteredApps.collectAsStateWithLifecycle()
    var privateSpaceQuery by remember { mutableStateOf("") }
    val folders by drawerViewModel.folders.collectAsStateWithLifecycle()

    if (homeUiState.isLoading) {
        // Real Home state (dock/app list, theme-driven colors and fonts) hasn't loaded yet —
        // render nothing rather than a frame styled with defaults, mirroring LauncherActivity's
        // own gate (see chat history and HomeViewModel's own doc).
        Box(modifier = modifier.fillMaxSize())
        return
    }

    // PACKAGE_USAGE_STATS has no grant-change callback — re-check whenever the user returns to
    // Home (e.g. from the usage-access Settings redirect) rather than only on facet changes.
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
    val searchListState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()
    val focusManager = LocalFocusManager.current

    var containerHeightPx by remember { mutableFloatStateOf(2000f) }
    var containerWidthPx by remember { mutableFloatStateOf(1000f) }
    val drawerAxis = remember { SwipeAxisState({ containerHeightPx }, coroutineScope) }
    val hubAxis = remember { SwipeAxisState({ containerWidthPx }, coroutineScope) }
    val facetAxis = remember { SwipeAxisState({ containerWidthPx }, coroutineScope) }

    LaunchedEffect(launcherViewModel) {
        launcherViewModel.homePressedEvent.collect {
            focusManager.clearFocus()
            drawerAxis.close()
            hubAxis.close()
            facetAxis.close()
        }
    }
    // Rendered in-place as an overlay below (not a NavHost destination — see chat history):
    // navigating away would tear down this whole composable, resetting hubAxis and losing Hub's
    // own open/scroll state, and would visually replace Hub entirely instead of loading over it.
    var showWidgetPicker by remember { mutableStateOf(false) }
    var showPrivateSpaceDrawer by remember { mutableStateOf(false) }
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

    val isDrawerSearching = drawerQuery.isNotBlank()
    val nestedScrollConnection = remember(listState, gridState, searchListState, drawerSettings.drawerPresentation, isDrawerSearching) {
        object : NestedScrollConnection {
            override fun onPreScroll(available: Offset, source: NestedScrollSource): Offset {
                // Fling-sourced deltas are the list's own post-release deceleration, not the
                // user's finger — once onPreFling below has resolved open/closed via settle(),
                // letting those leftover frames keep calling dragBy() would re-arm dragActive
                // and snapTo() over the in-flight settle animation, stranding the drawer
                // wherever the residual fling happened to decay to (the "low-velocity fling
                // stalls partway" bug). Only real finger movement should drive the drag.
                if (source != NestedScrollSource.Drag) return Offset.Zero
                // Search results are always a LazyColumn of their own (see DrawerSearchResults),
                // regardless of list/grid presentation — check its own state, not the browse-mode
                // list/grid's (which stays untouched, and therefore always "at top", while
                // searching — that mismatch used to let a swipe down on a scrolled search list
                // close the drawer instead of scrolling it back up; see chat history).
                val atTop = if (isDrawerSearching) {
                    searchListState.firstVisibleItemIndex == 0 && searchListState.firstVisibleItemScrollOffset == 0
                } else if (drawerSettings.drawerPresentation == DrawerPresentation.GRID) {
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
    val isFacetOpen by remember { derivedStateOf { facetAxis.progress.value > 0f } }

    // AppWidgetHost.startListening() is what actually makes hosted AppWidgetHostViews render/
    // update — without it, widget tiles stay blank. Only listen while the Hub is actually
    // visible, matching every other AppWidgetHost caller's lifecycle discipline.
    DisposableEffect(isHubOpen) {
        if (isHubOpen) hubViewModel.onHubVisible()
        onDispose { if (isHubOpen) hubViewModel.onHubHidden() }
    }

    // Clear focus whenever the drawer, hub, or facet carousel starts moving via a drag.
    LaunchedEffect(drawerAxis.dragActive, hubAxis.dragActive, facetAxis.dragActive) {
        if (drawerAxis.dragActive || hubAxis.dragActive || facetAxis.dragActive) {
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
            searchListState.scrollToItem(0)
            showPrivateSpaceDrawer = false
        }
    }

    // Reset hub widget picker state when it's fully closed.
    LaunchedEffect(isHubOpen) {
        if (!isHubOpen) {
            showWidgetPicker = false
        }
    }

    // Clear the widget picker's search whenever its own panel goes out of view — including
    // when just the picker closes (back/onDone) while the Hub itself stays open, not only
    // when the Hub closes underneath it.
    LaunchedEffect(showWidgetPicker) {
        if (!showWidgetPicker) {
            widgetPickerViewModel.reset()
        }
    }

    // Clear the Private Space screen's own search whenever it closes — independent of the main
    // Drawer's own query state (drawerQuery), which it never shares.
    LaunchedEffect(showPrivateSpaceDrawer) {
        if (!showPrivateSpaceDrawer) {
            privateSpaceQuery = ""
            privateSpaceViewModel.onQueryChanged("")
        }
    }

    // Cancel clock adjustment when navigating away from Home.
    LaunchedEffect(isDrawerOpen, isHubOpen, isFacetOpen) {
        if (isDrawerOpen || isHubOpen || isFacetOpen) {
            clockAdjustMode = ClockAdjustMode.NONE
        }
    }

    BackHandler(enabled = isDrawerOpen || isHubOpen || isFacetOpen || showWidgetPicker || showPrivateSpaceDrawer) {
        focusManager.clearFocus()
        when {
            showWidgetPicker -> showWidgetPicker = false
            showPrivateSpaceDrawer -> showPrivateSpaceDrawer = false
            isDrawerOpen -> coroutineScope.launch { drawerAxis.close() }
            isHubOpen -> coroutineScope.launch { hubAxis.close() }
            isFacetOpen -> coroutineScope.launch { facetAxis.close() }
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
            dockDisplayMode = homeUiState.activeDockDisplayMode,
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
            clockPositionOwnerFacetId = homeUiState.clockPositionOwningFacet?.id,
            clockPositionOwnerFacetName = homeUiState.clockPositionOwningFacet?.name,
            clockAdjustMode = clockAdjustMode,
            onAdjustModeChange = { clockAdjustMode = it },
            draggingHandle = draggingHandle,
            onDraggingHandleChange = { draggingHandle = it },
            onClockScaleCommit = homeViewModel::onClockScaleCommit,
            onEditClockStyles = {
                clockAdjustMode = ClockAdjustMode.NONE
                onNavigateToClockStyleGallery(homeUiState.clockPositionOwningFacet?.id)
            },
            onNavigateToSettings = onNavigateToSettings,
            onNavigateToFacetSettings = {
                homeUiState.activeFacet?.let { onNavigateToFacetSettings(it.id) }
            },
            appListVerticalAlignment = homeUiState.activeAppListVerticalAlignment,
            nextAlarmMillis = homeUiState.clockAccessories.nextAlarmMillis,
            batteryPercent = homeUiState.clockAccessories.batteryPercent,
            isCharging = homeUiState.clockAccessories.isCharging,
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
            onAppInfo = drawerViewModel::openAppInfo,
            onRequestQuickAddState = { app -> homeViewModel.quickAddStateForApp(app) },
            onFavoritesAction = drawerViewModel::onFavoritesAction,
            onDockAction = drawerViewModel::onDockAction,
            onRequestFolderQuickAddState = { folder -> homeViewModel.quickAddStateForFolder(folder) },
            onFolderFavoritesAction = drawerViewModel::onFolderFavoritesAction,
            onFolderDockAction = drawerViewModel::onFolderDockAction,
            folderCandidates = folders,
            onCreateFolder = drawerViewModel::createFolder,
            onAddToFolder = drawerViewModel::addToFolder,
            onRemoveFromFolder = drawerViewModel::removeFromFolder,
            onRenameFolder = drawerViewModel::renameFolder,
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer {
                    alpha = 1f - drawerAxis.progress.value
                    val scale = 1f - drawerAxis.progress.value * HOME_FADE_SCALE_RANGE
                    scaleX = scale
                    scaleY = scale
                    val panelProgress = maxOf(hubAxis.progress.value, facetAxis.progress.value)
                    renderEffect = if (panelProgress > 0f) {
                        val radiusPx = HOME_PANEL_BLUR_RADIUS.toPx() * panelProgress
                        BlurEffect(radiusPx, radiusPx)
                    } else {
                        null
                    }
                }
                .pointerInput(draggingHandle, clockAdjustMode) {
                    // Suppress screen-wide gestures if we're in any adjustment mode
                    // or actively dragging a handle.
                    if (draggingHandle || clockAdjustMode != ClockAdjustMode.NONE) return@pointerInput

                    detectHomeSwipeGestures(
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
                                // Exactly one of these is ever dragActive per gesture (see the
                                // ownership rule in the drag callback below) — settle whichever
                                // one it is.
                                Axis.HORIZONTAL -> {
                                    if (hubAxis.dragActive) hubAxis.settle()
                                    if (facetAxis.dragActive) facetAxis.settle()
                                }
                                null -> Unit
                            }
                        },
                        onDragCancel = {
                            when (homeDragAxis) {
                                Axis.VERTICAL -> drawerAxis.settle()
                                Axis.HORIZONTAL -> {
                                    if (hubAxis.dragActive) hubAxis.settle()
                                    if (facetAxis.dragActive) facetAxis.settle()
                                }
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
                            Axis.HORIZONTAL -> {
                                // Whichever axis this gesture engages first (by its very first
                                // horizontal delta) owns it for the rest of the gesture, even
                                // through a direction reversal — dragBy() already handles negative
                                // deltas fine (it just walks progress back down), so once an axis
                                // goes dragActive it keeps tracking the finger instead of control
                                // flipping to the other panel mid-drag.
                                when {
                                    hubAxis.dragActive || (!facetAxis.dragActive && dragAmount.x > 0f) ->
                                        hubAxis.dragBy(dragAmount.x)
                                    facetAxis.dragActive || (!hubAxis.dragActive && dragAmount.x < 0f) ->
                                        facetAxis.dragBy(-dragAmount.x)
                                }
                            }
                            null -> Unit
                        }
                    }
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
            workProfiles = launcherUiState.workProfiles,
            onAppClick = { app ->
                focusManager.clearFocus()
                onAppClick(app)
                coroutineScope.launch { drawerAxis.close() }
            },
            listState = listState,
            gridState = gridState,
            searchListState = searchListState,
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
            secureFolderIntent = drawerViewModel.secureFolderIntent,
            onOpenSecureFolder = { intent -> runCatching { context.startActivity(intent) } },
            showPrivateSpaceRow = privateSpaceState !is PrivateSpaceState.NotConfigured,
            onPrivateSpaceRowClick = {
                when (privateSpaceState) {
                    is PrivateSpaceState.Unlocked -> showPrivateSpaceDrawer = true
                    is PrivateSpaceState.Locked -> drawerViewModel.requestUnlockPrivateSpace()
                    is PrivateSpaceState.NotConfigured -> Unit
                }
            },
            contacts = contactResults,
            onRequestShortcuts = drawerViewModel::getShortcuts,
            onLaunchShortcut = drawerViewModel::launchShortcut,
            onAppInfo = drawerViewModel::openAppInfo,
            onRequestQuickAddState = { app -> homeViewModel.quickAddStateForApp(app) },
            onFavoritesAction = drawerViewModel::onFavoritesAction,
            onDockAction = drawerViewModel::onDockAction,
            folderCandidates = folders,
            onCreateFolder = drawerViewModel::createFolder,
            onAddToFolder = drawerViewModel::addToFolder,
            onRequestConnections = drawerViewModel::getConnections,
            showContactsPermissionPrompt = showContactsPermissionPrompt,
            onContactsPermissionPromptClick = {
                drawerViewModel.dismissContactsPermissionPrompt()
                context.startActivity(
                    Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.fromParts("package", context.packageName, null)),
                )
            },
            showContactsSettingPrompt = showContactsSettingPrompt,
            onContactsSettingPromptClick = drawerViewModel::enableContactSearch,
            settingsEntries = settingsResults,
            onSettingsEntryClick = { entry -> runCatching { context.startActivity(Intent(entry.action)) } },
            isDrawerOpen = isDrawerOpen,
            modifier = Modifier
                .fillMaxSize()
                .offset { IntOffset(0, ((1f - drawerAxis.progress.value) * containerHeightPx).toInt()) }
                .nestedScroll(nestedScrollConnection),
        )

        // Private Space — an in-place overlay over the App Drawer, same "sub-screen without
        // leaving composition" precedent as the Hub widget picker below (one back press returns
        // here; a second closes the Drawer to Home, per the BackHandler above).
        AnimatedVisibility(
            visible = showPrivateSpaceDrawer,
            enter = fadeIn(animationSpec = tween(FACET_TRANSITION_DURATION_MS, easing = FacetTransitionEasing)),
            exit = fadeOut(animationSpec = tween(FACET_TRANSITION_DURATION_MS, easing = FacetTransitionEasing)),
            modifier = Modifier.testTag("private_space_screen_overlay"),
        ) {
            PrivateSpaceScreen(
                state = privateSpaceState,
                apps = privateSpaceApps,
                query = privateSpaceQuery,
                onQueryChanged = { privateSpaceQuery = it; privateSpaceViewModel.onQueryChanged(it) },
                onAppClick = { app ->
                    onAppClick(app)
                    showPrivateSpaceDrawer = false
                    coroutineScope.launch { drawerAxis.close() }
                },
            )
        }

        // Add-widget picker — an in-place overlay (see the showWidgetPicker declaration above for
        // why), sliding in over the Hub the same direction a NavHost push would have, so it still
        // reads as "opening a sub-screen" even though Hub itself never leaves composition.
        AnimatedVisibility(
            visible = showWidgetPicker,
            enter = slideInHorizontally(initialOffsetX = { it }, animationSpec = tween(FACET_TRANSITION_DURATION_MS, easing = FacetTransitionEasing)),
            exit = slideOutHorizontally(targetOffsetX = { it }, animationSpec = tween(FACET_TRANSITION_DURATION_MS, easing = FacetTransitionEasing)),
            modifier = Modifier.testTag("hub_widget_picker_overlay"),
        ) {
            HubWidgetPickerScreen(
                onDone = { showWidgetPicker = false },
                viewModel = widgetPickerViewModel,
            )
        }

        // Switch Facets — sits to Home's right: off-screen at progress 0, flush with the screen
        // at progress 1. Drawn last so it's frontmost when open, over Hub/Drawer/the widget picker
        // alike (it used to be a modal NavHost destination above everything; this keeps that same
        // read). Its own surface handles empty-space swipes back to Home the same way Hub's own
        // Box does; the first-page-card case (no previous facet to browse to) is handled inside
        // FacetCarouselScreen itself via a NestedScrollConnection that forwards raw drag deltas
        // here through onDismissDrag/onDismissDragEnd, since the pager underneath would otherwise
        // swallow that drag as a dead overscroll before it ever reached this Box.
        //
        // Real bug found: unlike Hub, each preview card here is a genuine live copy of Home's own
        // content (clock, FAVORITES/RECENTS/MOST USED list, dock) — being permanently composed
        // (just off-screen) instead of a NavHost destination means that content sits in the
        // semantics tree even while fully closed, so e.g. onNodeWithText("FAVORITES") could match
        // either Home's real list or this card's copy of it. clearAndSetSemantics wipes this
        // whole subtree from the tree while closed; Compose's own assertIsNotDisplayed() already
        // treats "node doesn't exist" as passing, so nothing here needs a test-only workaround.
        Box(
            modifier = Modifier
                .fillMaxSize()
                .offset { IntOffset(((1f - facetAxis.progress.value) * containerWidthPx).toInt(), 0) }
                .then(if (isFacetOpen) Modifier else Modifier.clearAndSetSemantics {})
                .pointerInput(Unit) {
                    detectHorizontalDragGestures(
                        onDragEnd = { facetAxis.settle() },
                        onDragCancel = { facetAxis.settle() },
                    ) { change, dragAmount ->
                        facetAxis.dragBy(-dragAmount)
                        change.consume()
                    }
                },
        ) {
            FacetCarouselScreen(
                onFacetApplied = { coroutineScope.launch { facetAxis.close() } },
                onEditFacet = onNavigateToFacetSettings,
                onReorderFacets = onNavigateToManageFacets,
                onNavigateToSettings = onNavigateToSettings,
                onDismissDrag = { deltaPx -> facetAxis.dragBy(-deltaPx) },
                onDismissDragEnd = { facetAxis.settle() },
                modifier = Modifier.fillMaxSize(),
                viewModel = facetViewModel,
            )
        }

        // One-time "make Facet your home screen" prompt, over real Home itself rather than a
        // mocked-up preview card — see SetDefaultLauncherSheet's own doc for why this replaced
        // onboarding's old SET_DEFAULT step. Takes priority over the gesture hint below (both are
        // one-time and gated the same way; this one matters more and fires first in practice).
        AnimatedVisibility(
            visible = homeUiState.showSetDefaultPrompt && !isDrawerOpen && !isHubOpen && !isFacetOpen,
            enter = fadeIn(animationSpec = tween(240)),
            exit = fadeOut(animationSpec = tween(240)),
        ) {
            SetDefaultLauncherSheet(
                isDefaultLauncher = homeUiState.isDefaultLauncher,
                requestDefaultLauncherIntent = homeViewModel::requestDefaultLauncherIntent,
                onFinish = homeViewModel::dismissSetDefaultPrompt,
            )
        }

        // One-time post-onboarding gesture hint — only while Home is genuinely at rest, same
        // overlay-on-top-of-already-composed-content precedent as the widget picker above.
        AnimatedVisibility(
            visible = homeUiState.showGestureHint && !homeUiState.showSetDefaultPrompt && !isDrawerOpen && !isHubOpen && !isFacetOpen,
            enter = fadeIn(animationSpec = tween(240)),
            exit = fadeOut(animationSpec = tween(240)),
        ) {
            GestureHintOverlay(onDismiss = homeViewModel::dismissGestureHint)
        }
    }
}
