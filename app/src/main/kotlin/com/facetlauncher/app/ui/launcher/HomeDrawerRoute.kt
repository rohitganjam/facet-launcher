package com.facetlauncher.app.ui.launcher

import androidx.activity.compose.LocalActivity
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.runtime.setValue
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.unit.dp
import com.facetlauncher.app.ui.home.HomeLayoutMode
import com.facetlauncher.app.ui.home.HomeLiveState
import com.facetlauncher.app.ui.home.HomeScreenContent
import com.facetlauncher.app.ui.home.HomeWidgetSizing
import com.facetlauncher.app.ui.home.WhatsNewPillHost
import com.facetlauncher.app.ui.home.WhatsNewPillViewModel
import com.facetlauncher.app.ui.home.toHomeScreenState
import android.provider.Settings
import androidx.activity.compose.BackHandler
import androidx.compose.animation.core.Animatable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.layout
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.platform.testTag
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.facetlauncher.app.data.model.AppInfo
import com.facetlauncher.app.data.model.ProReason
import com.facetlauncher.app.ui.components.AppMenuNotifications
import com.facetlauncher.app.ui.components.LocalHomeSwipeGate
import com.facetlauncher.app.ui.drawer.DrawerViewModel
import com.facetlauncher.app.ui.drawer.PrivateSpaceViewModel
import com.facetlauncher.app.ui.facets.FacetCarouselScreen
import com.facetlauncher.app.ui.facets.FacetCarouselViewModel
import com.facetlauncher.app.ui.home.ClockAdjustMode
import com.facetlauncher.app.ui.home.HomeItemActions
import com.facetlauncher.app.ui.home.HomeScreen
import com.facetlauncher.app.ui.home.HomeViewModel
import com.facetlauncher.app.ui.home.rememberHomeLandscapeSpace
import com.facetlauncher.app.ui.home.rememberHomeLayoutMode
import com.facetlauncher.app.ui.home.weather.WeatherSheet
import com.facetlauncher.app.ui.home.weather.WeatherViewModel
import com.facetlauncher.app.ui.home.widget.ClockWidgetPickerScreen
import com.facetlauncher.app.ui.home.widget.ClockWidgetPickerViewModel
import com.facetlauncher.app.ui.hub.HubScreen
import com.facetlauncher.app.ui.hub.HubViewModel
import com.facetlauncher.app.ui.hub.picker.HubWidgetPickerScreen
import com.facetlauncher.app.ui.hub.picker.HubWidgetPickerViewModel
import com.facetlauncher.app.ui.launcher.HomeDrawerRoute
import com.facetlauncher.app.ui.launcher.LauncherViewModel
import com.facetlauncher.app.ui.theme.SystemBarsBackdrop
import com.facetlauncher.app.ui.theme.SystemBarsBackdropEffect
import kotlinx.collections.immutable.ImmutableList
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

/**
 * Home with the Drawer, Hub and Switch Facets over it, as one route rather than NavHost
 * destinations: a follow-finger drag needs an [Animatable] per panel driven every frame, which
 * NavHost's declarative transitions cannot do.
 *
 * Dragging up on Home opens the Drawer, right opens the Hub, left opens the carousel. The first
 * move of a drag locks its axis, and whichever of Hub and carousel a horizontal drag engages first
 * keeps it, so one gesture never drives two panels. A release commits open or closed once the drag
 * travelled [COMMIT_TRAVEL_FRACTION] of the container from where it started, else it springs back.
 * [HomeSwipeController] holds that logic; the sub-screens are in `HomeOverlays.kt` and
 * `HomeAppDrawerOverlay.kt`.
 */
@Composable
fun HomeDrawerRoute(
    apps: ImmutableList<AppInfo>,
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
    /** Opens the Units and location settings (where the weather location is chosen), from the weather sheet. */
    onNavigateToUnitsAndLocation: () -> Unit = {},
    onNavigateToPermissions: () -> Unit = {},
    /** Opens What's new, from the pill that announces an update. */
    onNavigateToWhatsNew: () -> Unit = {},
    modifier: Modifier = Modifier,
    homeViewModel: HomeViewModel = hiltViewModel(),
    whatsNewPillViewModel: WhatsNewPillViewModel = hiltViewModel(),
    weatherViewModel: WeatherViewModel = hiltViewModel(),
    drawerViewModel: DrawerViewModel = hiltViewModel(),
    privateSpaceViewModel: PrivateSpaceViewModel = hiltViewModel(),
    hubViewModel: HubViewModel = hiltViewModel(),
    widgetPickerViewModel: HubWidgetPickerViewModel = hiltViewModel(),
    clockWidgetPickerViewModel: ClockWidgetPickerViewModel = hiltViewModel(),
    facetViewModel: FacetCarouselViewModel = hiltViewModel(),
    launcherViewModel: LauncherViewModel,
) {
    val homeUiState by homeViewModel.uiState.collectAsStateWithLifecycle()
    val weatherUiState by weatherViewModel.uiState.collectAsStateWithLifecycle()
    val whatsNewVersion by whatsNewPillViewModel.version.collectAsStateWithLifecycle()
    val clockWidgetIsPro by homeViewModel.clockWidgetFacet.isPro.collectAsStateWithLifecycle()
    val launcherUiState by launcherViewModel.uiState.collectAsStateWithLifecycle()
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
    val weatherShown by rememberUpdatedState(homeUiState.style.clock.showWeather)
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                homeViewModel.refresh()
                weatherViewModel.onHomeResumed(weatherShown)
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }
    val coroutineScope = rememberCoroutineScope()
    val focusManager = LocalFocusManager.current

    var containerHeightPx by remember { mutableFloatStateOf(2000f) }
    var containerWidthPx by remember { mutableFloatStateOf(1000f) }

    val overlays = remember { HomeOverlayState() }
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
            overlays.showPrivateSpace -> SystemBarsBackdrop.DARK_SURFACE
            overlays.showWidgetPicker || overlays.showClockWidgetPicker -> SystemBarsBackdrop.THEME_SURFACE
            else -> SystemBarsBackdrop.WALLPAPER
        },
    )
    LaunchedEffect(launcherViewModel) {
        launcherViewModel.homePressedEvent.collect {
            focusManager.clearFocus()
            clockAdjustMode = ClockAdjustMode.NONE
            overlays.showClockWidgetPicker = false
            // Each close in its own job: a drag that interrupts one animation throws a
            // CancellationException, which must end that job only — inline, it escaped `collect`
            // and silently unsubscribed this collector, so Home stopped closing anything.
            coroutineScope.launch { swipe.drawer.close() }
            coroutineScope.launch { swipe.hub.close() }
            coroutineScope.launch { swipe.facet.close() }
        }
    }
    var draggingHandle by remember { mutableStateOf(false) }
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
    val activity = LocalActivity.current

    val isDrawerOpen by remember { derivedStateOf { swipe.drawer.progress.value > 0f } }
    val isHubOpen by remember { derivedStateOf { swipe.hub.progress.value > 0f } }
    val isFacetOpen by remember { derivedStateOf { swipe.facet.progress.value > 0f } }

    val clockWidgetNeedsListening = homeUiState.clockWidgetAppWidgetId != null

    // Clear focus whenever the drawer, hub, or facet carousel starts moving via a drag.
    LaunchedEffect(swipe.drawer.dragActive, swipe.hub.dragActive, swipe.facet.dragActive) {
        if (swipe.drawer.dragActive || swipe.hub.dragActive || swipe.facet.dragActive) {
            focusManager.clearFocus()
        }
    }

    // Cancel clock adjustment when navigating away from Home.
    LaunchedEffect(isDrawerOpen, isHubOpen, isFacetOpen) {
        if (isDrawerOpen || isHubOpen || isFacetOpen) {
            clockAdjustMode = ClockAdjustMode.NONE
        }
    }

    BackHandler(enabled = isDrawerOpen || isHubOpen || isFacetOpen || overlays.showWidgetPicker || overlays.showClockWidgetPicker || overlays.showPrivateSpace) {
        focusManager.clearFocus()
        when {
            overlays.showClockWidgetPicker -> overlays.showClockWidgetPicker = false
            overlays.showWidgetPicker -> overlays.showWidgetPicker = false
            overlays.showPrivateSpace -> overlays.showPrivateSpace = false
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

    val appListNestedScroll = remember(swipe, homeSwipeBlocked) { swipe.appListNestedScroll(homeSwipeBlocked) }

    val itemActions = HomeItemActions(
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
    )
    val drawerState = rememberHomeDrawerState(drawerViewModel, appMenuNotifications)
        .copy(apps = apps, workProfiles = launcherUiState.workProfiles, folders = folders)
    val drawerActions = remember(drawerViewModel) { homeDrawerActions(drawerViewModel) }
    val privateSpaceState = rememberHomePrivateSpaceState(
        privateSpaceViewModel,
        drawerState.privateSpaceState,
        drawerState.settings.recentlyInstalledPosition,
    )

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
                    weather = weatherUiState.weather,
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
            actions = homeScreenActions(
                viewModel = homeViewModel,
                inputs = HomeActionsInputs(homeUiState, clockWidgetIsPro, homeLayoutMode, context, coroutineScope),
                itemActions = itemActions,
                swipe = swipe,
                hooks = HomeRouteHooks(
                    setAdjustMode = { clockAdjustMode = it },
                    setDraggingHandle = { draggingHandle = it },
                    setClockWidgetBounds = { clockWidgetBoundsInHomeRoot = it },
                    setAppListBounds = { appListBoundsInHomeRoot = it },
                    openClockWidgetPicker = { facetId ->
                        overlays.clockWidgetPickerFacetId = facetId
                        overlays.showClockWidgetPicker = true
                    },
                    openWeather = {
                        overlays.showWeatherSheet = true
                        weatherViewModel.refresh(userInitiated = false)
                    },
                ),
                navigation = HomeNavigation(
                    toSettings = onNavigateToSettings,
                    toFacetSettings = onNavigateToFacetSettings,
                    toFacetPro = onNavigateToFacetPro,
                    toUsageAccessExplanation = onNavigateToUsageAccessExplanation,
                    toClockStyleGallery = onNavigateToClockStyleGallery,
                ),
            ),
            layoutMode = homeLayoutMode,
            landscapeSpace = homeLandscapeSpace,
            // Marks the app as fully drawn once Home has faded in, so startup timing covers what the user sees.
            onFullyReveal = { activity?.reportFullyDrawn() },
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

        HomeHubOverlay(
            hub = swipe.hub,
            containerWidthPx = containerWidthPx,
            isHubOpen = isHubOpen,
            clockWidgetNeedsListening = clockWidgetNeedsListening,
            overlays = overlays,
            onHubShow = hubViewModel::onHubVisible,
            onHubHide = hubViewModel::onHubHidden,
        ) {
            HubScreen(
                onAddClick = { overlays.showWidgetPicker = true },
                onManageClick = {}, // wired once there's a dedicated manage flow
                modifier = Modifier.fillMaxSize().testTag("hub_screen"),
                viewModel = hubViewModel,
            )
        }
        HomeAppDrawerOverlay(
            state = drawerState,
            actions = drawerActions,
            itemActions = itemActions,
            drawer = swipe.drawer,
            containerHeightPx = containerHeightPx,
            isDrawerOpen = isDrawerOpen,
            overlays = overlays,
            onNavigateToSettings = onNavigateToSettings,
        )
        HomePrivateSpaceOverlay(
            overlays = overlays,
            state = privateSpaceState,
            onQueryChange = privateSpaceViewModel::onQueryChange,
            onAppClick = { app ->
                onAppClick(app)
                overlays.showPrivateSpace = false
                coroutineScope.launch { swipe.drawer.close() }
            },
        )
        HomeWidgetPickerOverlay(overlays, onReset = widgetPickerViewModel::reset) {
            HubWidgetPickerScreen(onDone = { overlays.showWidgetPicker = false }, viewModel = widgetPickerViewModel)
        }
        HomeFacetCarouselOverlay(swipe.facet, containerWidthPx, isOpen = isFacetOpen) {
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
        HomeClockWidgetPickerOverlay(overlays, onReset = clockWidgetPickerViewModel::reset) { facetId ->
            ClockWidgetPickerScreen(
                facetId = facetId,
                onDone = { overlays.showClockWidgetPicker = false },
                viewModel = clockWidgetPickerViewModel,
            )
        }
        if (overlays.showWeatherSheet) {
            WeatherSheet(
                uiState = weatherUiState.copy(iconStyle = homeUiState.style.clock.weatherIconStyle),
                onRefresh = { weatherViewModel.refresh() },
                onDismiss = { overlays.showWeatherSheet = false },
                onChooseCity = {
                    overlays.showWeatherSheet = false
                    onNavigateToUnitsAndLocation()
                },
                onOpenPermissions = {
                    overlays.showWeatherSheet = false
                    onNavigateToPermissions()
                },
            )
        }
        HomeOneTimePrompts(
            showSetDefaultPrompt = homeUiState.showSetDefaultPrompt,
            showGestureHint = homeUiState.showGestureHint,
            homeAtRest = !isDrawerOpen && !isHubOpen && !isFacetOpen,
            requestDefaultLauncherIntent = homeViewModel::requestDefaultLauncherIntent,
            onFinishSetDefault = homeViewModel::dismissSetDefaultPrompt,
            onDismissGestureHint = homeViewModel::dismissGestureHint,
        )
        // Hovers over Home, below the status bar, without moving anything; waits for the first-run prompts and a resting Home.
        WhatsNewPillHost(
            version = whatsNewVersion,
            ready = homeUiState.firstRunPromptsDone && !isDrawerOpen && !isHubOpen && !isFacetOpen,
            onShow = whatsNewPillViewModel::onShown,
            onOpen = onNavigateToWhatsNew,
            modifier = Modifier.align(Alignment.TopCenter).windowInsetsPadding(WindowInsets.statusBars).padding(top = 12.dp),
        )
    }
}
