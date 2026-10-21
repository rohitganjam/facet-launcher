package com.facetlauncher.app.ui.launcher

import com.facetlauncher.app.ui.home.HomeLayoutMode
import com.facetlauncher.app.ui.home.HomeLiveState
import com.facetlauncher.app.ui.home.HomeScreenContent
import com.facetlauncher.app.ui.home.HomeWidgetSizing
import com.facetlauncher.app.ui.home.toHomeScreenState
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
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
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
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
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
import androidx.compose.ui.layout.layout
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.Velocity
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.facetlauncher.app.data.PrivateSpaceState
import com.facetlauncher.app.data.model.AppInfo
import com.facetlauncher.app.data.model.DrawerPresentation
import com.facetlauncher.app.data.model.ProReason
import com.facetlauncher.app.ui.components.AppMenuNotifications
import com.facetlauncher.app.ui.components.GestureHintOverlay
import com.facetlauncher.app.ui.components.LocalHomeSwipeGate
import com.facetlauncher.app.ui.drawer.AppDrawerScreen
import com.facetlauncher.app.ui.drawer.DrawerViewModel
import com.facetlauncher.app.ui.drawer.PrivateSpaceScreen
import com.facetlauncher.app.ui.drawer.PrivateSpaceViewModel
import com.facetlauncher.app.ui.facets.FacetCarouselScreen
import com.facetlauncher.app.ui.facets.FacetCarouselViewModel
import com.facetlauncher.app.ui.home.ClockAdjustMode
import com.facetlauncher.app.ui.home.HomeCalendarState
import com.facetlauncher.app.ui.home.HomeClockActions
import com.facetlauncher.app.ui.home.HomeClockState
import com.facetlauncher.app.ui.home.HomeClockWidgetActions
import com.facetlauncher.app.ui.home.HomeClockWidgetState
import com.facetlauncher.app.ui.home.HomeDockState
import com.facetlauncher.app.ui.home.HomeItemActions
import com.facetlauncher.app.ui.home.HomeItemLook
import com.facetlauncher.app.ui.home.HomeListState
import com.facetlauncher.app.ui.home.HomeMediaActions
import com.facetlauncher.app.ui.home.HomeMediaState
import com.facetlauncher.app.ui.home.HomeScreen
import com.facetlauncher.app.ui.home.HomeScreenActions
import com.facetlauncher.app.ui.home.HomeScreenState
import com.facetlauncher.app.ui.home.HomeViewModel
import com.facetlauncher.app.ui.home.rememberHomeLandscapeSpace
import com.facetlauncher.app.ui.home.rememberHomeLayoutMode
import com.facetlauncher.app.ui.home.widget.ClockWidgetPickerScreen
import com.facetlauncher.app.ui.home.widget.ClockWidgetPickerViewModel
import com.facetlauncher.app.ui.hub.HubScreen
import com.facetlauncher.app.ui.hub.HubViewModel
import com.facetlauncher.app.ui.hub.picker.HubWidgetPickerScreen
import com.facetlauncher.app.ui.hub.picker.HubWidgetPickerViewModel
import com.facetlauncher.app.ui.launcher.HomeDrawerRoute
import com.facetlauncher.app.ui.launcher.LauncherViewModel
import com.facetlauncher.app.ui.onboarding.SetDefaultLauncherSheet
import com.facetlauncher.app.ui.theme.FACET_TRANSITION_DURATION_MS
import com.facetlauncher.app.ui.theme.FacetTransitionEasing
import com.facetlauncher.app.ui.theme.SystemBarsBackdrop
import com.facetlauncher.app.ui.theme.SystemBarsBackdropEffect
import com.facetlauncher.app.ui.theme.resolve
import kotlin.math.abs
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

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
    onNavigateToFacetPro: (ProReason) -> Unit,
    onNavigateToUsageAccessExplanation: () -> Unit,
    /** Navigates to the clock style gallery, either global (null) or facet-scoped. */
    onNavigateToClockStyleGallery: (Long?) -> Unit = { _ -> },
    /** Opens the screen that explains and grants notification access, from an app menu's "Enable notifications". */
    onNavigateToNotificationAccessExplanation: () -> Unit = {},
    modifier: Modifier = Modifier,
    homeViewModel: HomeViewModel = hiltViewModel(),
    drawerViewModel: DrawerViewModel = hiltViewModel(),
    privateSpaceViewModel: PrivateSpaceViewModel = hiltViewModel(),
    hubViewModel: HubViewModel = hiltViewModel(),
    widgetPickerViewModel: HubWidgetPickerViewModel = hiltViewModel(),
    clockWidgetPickerViewModel: ClockWidgetPickerViewModel = hiltViewModel(),
    facetViewModel: FacetCarouselViewModel = hiltViewModel(),
    launcherViewModel: LauncherViewModel,
) {
    val homeUiState by homeViewModel.uiState.collectAsStateWithLifecycle()
    val clockWidgetIsPro by homeViewModel.clockWidgetFacet.isPro.collectAsStateWithLifecycle()
    val launcherUiState by launcherViewModel.uiState.collectAsStateWithLifecycle()
    val drawerSettings by drawerViewModel.settings.collectAsStateWithLifecycle()
    val contactResults by drawerViewModel.contactResults.collectAsStateWithLifecycle()
    val settingsResults by drawerViewModel.settingsResults.collectAsStateWithLifecycle()
    val drawerBadgeCounts by drawerViewModel.badgeCounts.collectAsStateWithLifecycle()
    val showContactsPermissionPrompt by drawerViewModel.showContactsPermissionPrompt.collectAsStateWithLifecycle()
    val showContactsSettingPrompt by drawerViewModel.showContactsSettingPrompt.collectAsStateWithLifecycle()
    val privateSpaceState by drawerViewModel.privateSpaceState.collectAsStateWithLifecycle()
    val privateSpaceApps by privateSpaceViewModel.filteredApps.collectAsStateWithLifecycle()
    val privateSpaceRecentlyInstalledApps by privateSpaceViewModel.recentlyInstalledApps.collectAsStateWithLifecycle()
    var privateSpaceQuery by remember { mutableStateOf("") }
    val folders by drawerViewModel.folders.collectAsStateWithLifecycle()
    val currentOnNavigateToNotificationAccess by rememberUpdatedState(onNavigateToNotificationAccessExplanation)
    val appMenuNotifications = remember(drawerViewModel) {
        AppMenuNotifications(
            observe = drawerViewModel::observeAppNotifications,
            onOpen = drawerViewModel::openNotification,
            onDismiss = drawerViewModel::dismissNotifications,
            onEnableAccess = { currentOnNavigateToNotificationAccess() },
        )
    }

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

    // Rendered in-place as an overlay below (not a NavHost destination — see chat history):
    // navigating away would tear down this whole composable, resetting swipe.hub and losing Hub's
    // own open/scroll state, and would visually replace Hub entirely instead of loading over it.
    var showWidgetPicker by remember { mutableStateOf(false) }
    // PRD F15 — the clock widget picker, an in-place overlay over Home itself (not gated by
    // isHubOpen like showWidgetPicker above, since it's opened from ClockAdjustSheet on Home, not
    // from the Hub). clockWidgetPickerFacetId is set once when the flow starts and stays fixed for
    // its duration, mirroring ClockWidgetPickerViewModel's own pendingFacetId — see its doc.
    var showClockWidgetPicker by remember { mutableStateOf(false) }
    var clockWidgetPickerFacetId by remember { mutableStateOf<Long?>(null) }
    var showPrivateSpaceDrawer by remember { mutableStateOf(false) }
    var clockAdjustMode by remember { mutableStateOf(ClockAdjustMode.NONE) }
    val swipe = remember {
        HomeSwipeController(
            containerHeightPx = { containerHeightPx },
            containerWidthPx = { containerWidthPx },
            coroutineScope = coroutineScope,
            endClockAdjust = { clockAdjustMode = ClockAdjustMode.NONE },
            expandNotificationShade = homeViewModel::expandNotificationShade,
        )
    }
    // What's behind the system bars: wallpaper, except while an opaque picker or the always-dark Private Space covers it.
    SystemBarsBackdropEffect(
        when {
            showPrivateSpaceDrawer -> SystemBarsBackdrop.DARK_SURFACE
            showWidgetPicker || showClockWidgetPicker -> SystemBarsBackdrop.THEME_SURFACE
            else -> SystemBarsBackdrop.WALLPAPER
        },
    )
    LaunchedEffect(launcherViewModel) {
        launcherViewModel.homePressedEvent.collect {
            focusManager.clearFocus()
            clockAdjustMode = ClockAdjustMode.NONE
            showClockWidgetPicker = false
            // Each close in its own job: a drag that interrupts one animation throws a
            // CancellationException, which must end that job only — inline, it escaped `collect`
            // and silently unsubscribed this collector, so Home stopped closing anything.
            coroutineScope.launch { swipe.drawer.close() }
            coroutineScope.launch { swipe.hub.close() }
            coroutineScope.launch { swipe.facet.close() }
        }
    }
    var draggingHandle by remember { mutableStateOf(false) }
    var drawerQuery by remember { mutableStateOf("") }
    // PRD F15 — reported by HomeScreen's own onClockWidgetBoundsChange; see its doc and
    // detectHomeSwipeGestures' own shouldClaim param for why this exists.
    var clockWidgetBoundsInHomeRoot by remember { mutableStateOf<Rect?>(null) }
    // Reported by HomeScreen's own onAppListBoundsChange — same reasoning as
    // clockWidgetBoundsInHomeRoot above, for the favorites/recents/most-used list's own scroll.
    var appListBoundsInHomeRoot by remember { mutableStateOf<Rect?>(null) }
    // A long-press sheet (app/folder context menu) is open — Home's swipes must not act on the
    // still-down finger that opened it, or on anything else, until it closes (back still works).
    val homeSwipeBlocked = LocalHomeSwipeGate.current?.isBlocked == true
    val context = LocalContext.current
    val density = LocalDensity.current

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
                if (available.y > 0f && atTop && swipe.drawer.progress.value > 0f) {
                    swipe.drawer.dragBy(-available.y)
                    return available
                }
                return Offset.Zero
            }

            override suspend fun onPreFling(available: androidx.compose.ui.unit.Velocity): androidx.compose.ui.unit.Velocity {
                if (swipe.drawer.dragActive) {
                    swipe.drawer.settle(velocity = -available.y)
                }
                return super.onPreFling(available)
            }
        }
    }

    val isDrawerOpen by remember { derivedStateOf { swipe.drawer.progress.value > 0f } }
    val isHubOpen by remember { derivedStateOf { swipe.hub.progress.value > 0f } }
    val isFacetOpen by remember { derivedStateOf { swipe.facet.progress.value > 0f } }

    // AppWidgetHost.startListening() is what actually makes hosted AppWidgetHostViews render/
    // update — without it, widget tiles stay blank. hubViewModel/homeViewModel share the exact
    // same singleton AppWidgetRepository/AppWidgetHost (WidgetModule.kt — "never create a second
    // one with this id"), and start/stop aren't reference-counted by the platform: whichever
    // surface closes last would otherwise silently stop listening for the other too. So this is
    // ONE combined trigger for "does anything need the shared host listening right now" —
    // PRD F15's clock widget (always potentially visible on Home) or the Hub grid (only while
    // open) — routed through hubViewModel's existing methods since they're a thin pass-through to
    // the same repository either way, not because this is conceptually Hub-specific.
    val clockWidgetNeedsListening = homeUiState.clockWidgetAppWidgetId != null
    DisposableEffect(isHubOpen, clockWidgetNeedsListening) {
        val shouldListen = isHubOpen || clockWidgetNeedsListening
        if (shouldListen) hubViewModel.onHubVisible()
        onDispose { if (shouldListen) hubViewModel.onHubHidden() }
    }

    // Clear focus whenever the drawer, hub, or facet carousel starts moving via a drag.
    LaunchedEffect(swipe.drawer.dragActive, swipe.hub.dragActive, swipe.facet.dragActive) {
        if (swipe.drawer.dragActive || swipe.hub.dragActive || swipe.facet.dragActive) {
            focusManager.clearFocus()
        }
    }

    // Reset drawer state when it's fully closed.
    LaunchedEffect(isDrawerOpen) {
        if (!isDrawerOpen) {
            drawerQuery = ""
            drawerViewModel.onQueryChange("")
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

    // Clear the clock widget picker's search/target facet whenever its own panel closes.
    LaunchedEffect(showClockWidgetPicker) {
        if (!showClockWidgetPicker) {
            clockWidgetPickerViewModel.reset()
            clockWidgetPickerFacetId = null
        }
    }

    // Clear the Private Space screen's own search whenever it closes — independent of the main
    // Drawer's own query state (drawerQuery), which it never shares.
    LaunchedEffect(showPrivateSpaceDrawer) {
        if (!showPrivateSpaceDrawer) {
            privateSpaceQuery = ""
            privateSpaceViewModel.onQueryChange("")
        }
    }

    // Cancel clock adjustment when navigating away from Home.
    LaunchedEffect(isDrawerOpen, isHubOpen, isFacetOpen) {
        if (isDrawerOpen || isHubOpen || isFacetOpen) {
            clockAdjustMode = ClockAdjustMode.NONE
        }
    }

    BackHandler(enabled = isDrawerOpen || isHubOpen || isFacetOpen || showWidgetPicker || showClockWidgetPicker || showPrivateSpaceDrawer) {
        focusManager.clearFocus()
        when {
            showClockWidgetPicker -> showClockWidgetPicker = false
            showWidgetPicker -> showWidgetPicker = false
            showPrivateSpaceDrawer -> showPrivateSpaceDrawer = false
            isDrawerOpen -> coroutineScope.launch { swipe.drawer.close() }
            isHubOpen -> coroutineScope.launch { swipe.hub.close() }
            isFacetOpen -> coroutineScope.launch { swipe.facet.close() }
        }
    }

    // PRD F15 — the provider's own declared default (getAppWidgetInfo is a real system query, so
    // resolved once per bound widget id, not every recomposition). The persisted
    // size (homeUiState.clockWidgetWidthDp/HeightDp) — once the user has actually dragged a resize
    // handle — takes precedence over this default; see HomeScreen's own effectiveClockWidgetSize.
    val clockWidgetDefaultSizeDp = homeUiState.clockWidgetAppWidgetId?.let { appWidgetId ->
        remember(appWidgetId) { homeViewModel.clockWidgetHost.defaultSizeDp(appWidgetId) }
    }
    val clockWidgetResizeFloorDp = homeUiState.clockWidgetAppWidgetId?.let { appWidgetId ->
        remember(appWidgetId) { homeViewModel.clockWidgetHost.resizeFloorDp(context, appWidgetId) }
    }
    // The persisted size (once the user has actually resized) wins over the provider's own default.
    // The layout mode, and everything that differs between orientations, resolved once: landscape's own
    // values where saved, the portrait ones otherwise.
    val homeLayoutMode = rememberHomeLayoutMode()
    val homeLandscapeSpace = rememberHomeLandscapeSpace()
    val clockPlacement = homeUiState.clockPlacement(homeLayoutMode)
    val listAlignment = homeUiState.listAlignment(homeLayoutMode)
    val savedClockWidgetSize = homeUiState.clockWidgetSizeDp(homeLayoutMode)
    val clockWidgetEffectiveWidthDp = savedClockWidgetSize?.first ?: clockWidgetDefaultSizeDp?.first
    val clockWidgetEffectiveHeightDp = savedClockWidgetSize?.second ?: clockWidgetDefaultSizeDp?.second

    val appListNestedScroll = remember(swipe, homeSwipeBlocked) { swipe.appListNestedScroll(homeSwipeBlocked) }

    Box(
        modifier = modifier
            .fillMaxSize()
            .onSizeChanged {
                containerHeightPx = it.height.toFloat()
                containerWidthPx = it.width.toFloat()
            },
    ) {
        HomeScreen(
            state = homeUiState.style.toHomeScreenState(
                landscape = homeLayoutMode == HomeLayoutMode.LANDSCAPE,
                settings = homeUiState.settings,
                content = HomeScreenContent(
                    listItems = homeUiState.appListItems,
                    dockItems = homeUiState.dockApps,
                    calendarEvents = homeUiState.calendarEvents,
                    badgeCounts = homeUiState.badgeCounts,
                    appMenuNotifications = appMenuNotifications,
                    folderCandidates = folders,
                    showUsageAccessPrompt = homeUiState.showUsageAccessPrompt,
                    clockAccessories = homeUiState.clockAccessories,
                    mediaPlayer = homeUiState.mediaPlayer,
                ),
                widgetSizing = HomeWidgetSizing(
                    defaultSizeDp = { clockWidgetDefaultSizeDp },
                    resizeFloorDp = { clockWidgetResizeFloorDp },
                ),
                live = HomeLiveState(
                    adjustMode = clockAdjustMode,
                    draggingHandle = draggingHandle,
                    isPro = clockWidgetIsPro,
                    positionOwnerFacetId = homeUiState.clockPositionOwningFacet?.id,
                    positionOwnerFacetName = homeUiState.clockPositionOwningFacet?.name,
                ),
            ),
            actions = HomeScreenActions(
                item = HomeItemActions(
                    onAppClick = onAppClick,
                    onAppInfo = drawerViewModel::openAppInfo,
                    onRequestQuickAddState = { app -> homeViewModel.quickAddStateForApp(app) },
                    onFavoritesAction = drawerViewModel::onFavoritesAction,
                    onDockAction = drawerViewModel::onDockAction,
                    onRequestFolderQuickAddState = { folder -> homeViewModel.quickAddStateForFolder(folder) },
                    onFolderFavoritesAction = drawerViewModel::onFolderFavoritesAction,
                    onFolderDockAction = drawerViewModel::onFolderDockAction,
                    onCreateFolder = drawerViewModel::createFolder,
                    onAddToFolder = drawerViewModel::addToFolder,
                    onRemoveFromFolder = drawerViewModel::removeFromFolder,
                    onRenameFolder = drawerViewModel::renameFolder,
                ),
                clock = HomeClockActions(
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
                    onEditClockStyles = {
                        clockAdjustMode = ClockAdjustMode.NONE
                        onNavigateToClockStyleGallery(homeUiState.clockPositionOwningFacet?.id)
                    },
                    onNavigateToSettings = onNavigateToSettings,
                    onNavigateToFacetSettings = {
                        homeUiState.activeFacet?.let { onNavigateToFacetSettings(it.id) }
                    },
                    onUseCustomWidgetClick = {
                        if (!clockWidgetIsPro) {
                            onNavigateToFacetPro(ProReason.CUSTOM_WIDGET)
                        } else {
                            homeUiState.activeFacet?.let { facet ->
                                clockWidgetPickerFacetId = facet.id
                                showClockWidgetPicker = true
                            }
                        }
                    },
                    onSwitchToLauncherClockClick = {
                        homeUiState.activeFacet?.let { facet ->
                            coroutineScope.launch { homeViewModel.clockWidgetFacet.switchToNativeClock(facet) }
                        }
                    },
                    onEventClick = { event ->
                        val uri = ContentUris.withAppendedId(CalendarContract.Events.CONTENT_URI, event.id)
                        runCatching { context.startActivity(Intent(Intent.ACTION_VIEW, uri)) }
                    },
                    onZoneHeightCommit = { homeViewModel.onClockZoneHeightCommit(it, homeLayoutMode) },
                    onScaleCommit = { homeViewModel.onClockScaleCommit(it, homeLayoutMode) },
                    onAlignmentChange = { homeViewModel.onClockAlignmentCommit(it, homeLayoutMode) },
                    onAdjustModeChange = { clockAdjustMode = it },
                    onDraggingHandleChange = { draggingHandle = it },
                ),
                widget = HomeClockWidgetActions(
                    createHostView = homeViewModel.clockWidgetHost::createHostView,
                    onSizeChange = homeViewModel.clockWidgetHost::updateSize,
                    onSizeCommit = { widthDp, heightDp ->
                        homeUiState.activeFacet?.let { facet ->
                            coroutineScope.launch { homeViewModel.clockWidgetFacet.commitSize(facet, widthDp, heightDp, homeLayoutMode) }
                        }
                    },
                    onBoundsChange = { clockWidgetBoundsInHomeRoot = it },
                    onNestedScrollStart = swipe.widgetScroll::start,
                    onLeftoverScroll = swipe.widgetScroll::leftoverScroll,
                    onNestedScrollStop = swipe.widgetScroll::stop,
                    onLeftoverFling = swipe.widgetScroll::leftoverFling,
                ),
                media = HomeMediaActions(
                    onPrevious = homeViewModel::onMediaPrevious,
                    onPlayPause = homeViewModel::onMediaPlayPause,
                    onNext = homeViewModel::onMediaNext,
                    onOpen = homeViewModel::onMediaOpen,
                ),
                onUsageAccessPromptClick = {
                    homeViewModel.dismissUsageAccessPrompt()
                    onNavigateToUsageAccessExplanation()
                },
                onAppListBoundsChange = { appListBoundsInHomeRoot = it },
            ),
            layoutMode = homeLayoutMode,
            landscapeSpace = homeLandscapeSpace,
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer {
                    // Home gives way to whichever panel (Drawer, Hub, Switch Facets) is opening, the same
                    // way for all three: fades out and shrinks slightly, leaving only the wallpaper and
                    // the panel's own scrim.
                    val fade = homeLayerFade(swipe.drawer.progress.value, swipe.hub.progress.value, swipe.facet.progress.value)
                    alpha = fade.alpha
                    scaleX = fade.scale
                    scaleY = fade.scale
                }
                // Receives the app list's own leftover vertical scroll once it bottoms/tops out.
                .nestedScroll(appListNestedScroll)
                .pointerInput(draggingHandle, clockAdjustMode, homeSwipeBlocked) {
                    // Suppress screen-wide gestures if we're in any adjustment mode,
                    // actively dragging a handle, or a long-press sheet is open.
                    if (draggingHandle || clockAdjustMode != ClockAdjustMode.NONE || homeSwipeBlocked) return@pointerInput

                    detectHomeSwipeGestures(
                        shouldClaim = { position ->
                            clockWidgetBoundsInHomeRoot?.contains(position) != true &&
                                appListBoundsInHomeRoot?.contains(position) != true
                        },
                        // App list: vertical drags belong to its own scroll, horizontal ones to Hub/carousel.
                        claimHorizontalOnly = { position -> appListBoundsInHomeRoot?.contains(position) == true },
                        onDragStart = swipe::onPointerDragStart,
                        onDragEnd = swipe::onPointerDragEnd,
                        onDragCancel = swipe::cancel,
                        onDrag = swipe::onPointerDrag,
                    )
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
                .offset { IntOffset(((swipe.hub.progress.value - 1f) * containerWidthPx).toInt(), 0) }
                .pointerInput(Unit) {
                    detectHorizontalDragGestures(
                        onDragEnd = { swipe.hub.settle() },
                        onDragCancel = { swipe.hub.settle() },
                    ) { change, dragAmount ->
                        swipe.hub.dragBy(dragAmount)
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
                coroutineScope.launch { swipe.drawer.close() }
            },
            listState = listState,
            gridState = gridState,
            searchListState = searchListState,
            presentation = drawerSettings.drawerPresentation,
            gridDensity = drawerSettings.drawerGridDensity,
            listItemSize = drawerSettings.drawerListItemSize,
            showIcons = drawerSettings.showDrawerIcons,
            showLabels = drawerSettings.showDrawerLabels,
            labelFontWeight = drawerSettings.homeAppsFontWeight.resolve(),
            opacity = drawerSettings.drawerOpacity,
            notificationBadgeStyle = drawerSettings.notificationBadgeStyle,
            badgeCounts = drawerBadgeCounts,
            query = drawerQuery,
            onQueryChange = { drawerQuery = it; drawerViewModel.onQueryChange(it) },
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
            appMenuNotifications = appMenuNotifications,
            onAppInfo = drawerViewModel::openAppInfo,
            onRequestQuickAddState = { app -> homeViewModel.quickAddStateForApp(app) },
            onFavoritesAction = drawerViewModel::onFavoritesAction,
            onDockAction = drawerViewModel::onDockAction,
            folderCandidates = folders,
            onCreateFolder = drawerViewModel::createFolder,
            onAddToFolder = drawerViewModel::addToFolder,
            folderDisplayMode = drawerSettings.drawerFolderDisplayMode,
            recentlyInstalledPosition = drawerSettings.recentlyInstalledPosition,
            onRemoveFromFolder = drawerViewModel::removeFromFolder,
            onRenameFolder = drawerViewModel::renameFolder,
            onRequestFolderQuickAddState = { folder -> homeViewModel.quickAddStateForFolder(folder) },
            onFolderFavoritesAction = drawerViewModel::onFolderFavoritesAction,
            onFolderDockAction = drawerViewModel::onFolderDockAction,
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
                .offset { IntOffset(0, ((1f - swipe.drawer.progress.value) * containerHeightPx).toInt()) }
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
                recentlyInstalledApps = privateSpaceRecentlyInstalledApps,
                recentlyInstalledPosition = drawerSettings.recentlyInstalledPosition,
                query = privateSpaceQuery,
                onQueryChange = { privateSpaceQuery = it; privateSpaceViewModel.onQueryChange(it) },
                onAppClick = { app ->
                    onAppClick(app)
                    showPrivateSpaceDrawer = false
                    coroutineScope.launch { swipe.drawer.close() }
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
                .offset { IntOffset(((1f - swipe.facet.progress.value) * containerWidthPx).toInt(), 0) }
                .then(if (isFacetOpen) Modifier else Modifier.clearAndSetSemantics {})
                .pointerInput(Unit) {
                    detectHorizontalDragGestures(
                        onDragEnd = { swipe.facet.settle() },
                        onDragCancel = { swipe.facet.settle() },
                    ) { change, dragAmount ->
                        swipe.facet.dragBy(-dragAmount)
                        change.consume()
                    }
                },
        ) {
            FacetCarouselScreen(
                onFacetApply = { coroutineScope.launch { swipe.facet.close() } },
                onEditFacet = onNavigateToFacetSettings,
                onReorderFacets = onNavigateToManageFacets,
                onNavigateToSettings = onNavigateToSettings,
                onOpenFacetPro = onNavigateToFacetPro,
                onDismissDrag = { deltaPx -> swipe.facet.dragBy(-deltaPx) },
                onDismissDragEnd = { swipe.facet.settle() },
                modifier = Modifier.fillMaxSize(),
                viewModel = facetViewModel,
            )
        }

        // PRD F15's clock widget picker — an in-place overlay over everything above (drawn last,
        // same "sub-screen without leaving composition" precedent as the Hub's own widget picker),
        // since it's reached from ClockAdjustSheet on Home itself rather than from the Hub.
        clockWidgetPickerFacetId?.let { facetId ->
            AnimatedVisibility(
                visible = showClockWidgetPicker,
                enter = slideInHorizontally(initialOffsetX = { it }, animationSpec = tween(FACET_TRANSITION_DURATION_MS, easing = FacetTransitionEasing)),
                exit = slideOutHorizontally(targetOffsetX = { it }, animationSpec = tween(FACET_TRANSITION_DURATION_MS, easing = FacetTransitionEasing)),
                modifier = Modifier.testTag("clock_widget_picker_overlay"),
            ) {
                ClockWidgetPickerScreen(
                    facetId = facetId,
                    onDone = { showClockWidgetPicker = false },
                    viewModel = clockWidgetPickerViewModel,
                )
            }
        }

        // One-time "make Facet your home screen" prompt, over real Home itself rather than a
        // mocked-up preview card — see SetDefaultLauncherSheet's own doc for why this replaced
        // onboarding's old SET_DEFAULT step. Takes priority over the gesture hint below (both are
        // one-time and gated the same way; this one matters more and fires first in practice).
        val homeAtRest = !isDrawerOpen && !isHubOpen && !isFacetOpen
        if (homeUiState.showSetDefaultPrompt && homeAtRest) {
            SetDefaultLauncherSheet(
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
