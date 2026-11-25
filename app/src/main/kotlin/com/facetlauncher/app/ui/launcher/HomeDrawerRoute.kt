package com.facetlauncher.app.ui.launcher

import androidx.activity.compose.LocalActivity
import androidx.compose.foundation.layout.only
import com.facetlauncher.app.ui.nativewidget.NativeWidgetAccessControls
import com.facetlauncher.app.ui.nativewidget.rememberNativeWidgetAccess
import com.facetlauncher.app.ui.home.WallpaperCopyPillViewModel
import com.facetlauncher.app.ui.home.WhatsNewPillViewModel
import androidx.compose.animation.core.Animatable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.facetlauncher.app.data.model.AppInfo
import com.facetlauncher.app.data.model.ProReason
import com.facetlauncher.app.ui.components.LocalHomeSwipeGate
import com.facetlauncher.app.ui.components.LocalIconGlassSamplesWallpaper
import com.facetlauncher.app.ui.drawer.DrawerViewModel
import com.facetlauncher.app.ui.drawer.PrivateSpaceViewModel
import com.facetlauncher.app.ui.vault.AppVaultViewModel
import com.facetlauncher.app.ui.home.HomeMenuMode
import com.facetlauncher.app.ui.home.HomeScreen
import com.facetlauncher.app.ui.home.HomeViewModel
import com.facetlauncher.app.ui.home.rememberHomeLandscapeSpace
import com.facetlauncher.app.ui.home.rememberHomeLayoutMode
import com.facetlauncher.app.ui.home.weather.WeatherViewModel
import com.facetlauncher.app.ui.home.widget.ClockWidgetPickerViewModel
import com.facetlauncher.app.ui.hub.HubViewModel
import com.facetlauncher.app.ui.hub.picker.HubWidgetPickerViewModel
import com.facetlauncher.app.ui.launcher.HomeDrawerRoute
import com.facetlauncher.app.ui.launcher.LauncherViewModel
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.flow.first

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
    launcherViewModel: LauncherViewModel,
    modifier: Modifier = Modifier,
    /** Opens one facet's clock style options, and the calendar settings of its clock. */
    onNavigateToClockStyle: (facetId: Long) -> Unit = { _ -> },
    onNavigateToClockStack: () -> Unit = {},
    /** Opens the screen that explains and grants notification access, from an app menu's "Enable notifications". */
    onNavigateToNotificationAccessExplanation: () -> Unit = {},
    /** Opens the Units and location settings (where the weather location is chosen), from the weather sheet. */
    onNavigateToUnitsAndLocation: () -> Unit = {},
    onNavigateToClockAccessories: (facetId: Long?) -> Unit = {},
    onNavigateToPermissions: () -> Unit = {},
    /** Opens What's new, from the pill that announces an update. */
    onNavigateToWhatsNew: () -> Unit = {},
    /** Opens the capture screen, from the pill that says glass effects need a capture of this screen's wallpaper. */
    onNavigateToWallpaperCapture: () -> Unit = {},
    homeViewModel: HomeViewModel = hiltViewModel(),
    whatsNewPillViewModel: WhatsNewPillViewModel = hiltViewModel(),
    wallpaperCopyPillViewModel: WallpaperCopyPillViewModel = hiltViewModel(),
    weatherViewModel: WeatherViewModel = hiltViewModel(),
    drawerViewModel: DrawerViewModel = hiltViewModel(),
    privateSpaceViewModel: PrivateSpaceViewModel = hiltViewModel(),
    widgetAccess: NativeWidgetAccessControls = rememberNativeWidgetAccess(),
) {
    // Resolved here, not passed in: the screens below resolve the same instances, since they share this destination's store.
    val appVaultViewModel: AppVaultViewModel = hiltViewModel()
    val hubViewModel: HubViewModel = hiltViewModel()
    val widgetPickerViewModel: HubWidgetPickerViewModel = hiltViewModel()
    val clockWidgetPickerViewModel: ClockWidgetPickerViewModel = hiltViewModel()
    val homeUiState by homeViewModel.uiState.collectAsStateWithLifecycle()
    val weatherUiState by weatherViewModel.uiState.collectAsStateWithLifecycle()
    val launcherUiState by launcherViewModel.uiState.collectAsStateWithLifecycle()
    val folders by drawerViewModel.folders.collectAsStateWithLifecycle()
    val appMenuNotifications = rememberAppMenuNotifications(drawerViewModel, onNavigateToNotificationAccessExplanation)

    if (homeUiState.isLoading) {
        // Real Home state (dock/app list, theme-driven colors and fonts) hasn't loaded yet —
        // render nothing rather than a frame styled with defaults, mirroring LauncherActivity's
        // own gate (see chat history and HomeViewModel's own doc).
        Box(modifier = modifier.fillMaxSize())
        return
    }

    val state = rememberHomeRouteState()
    val overlays = state.overlays
    val swipe = rememberHomeSwipeController(
        state = state,
        expandNotificationShade = homeViewModel::expandNotificationShade,
        onClockPageChange = homeViewModel::onClockPageChange,
        homeUiState = homeUiState,
        swipeActions = launcherUiState.swipeActions,
    )
    val open = rememberPanelsOpen(swipe)
    val hubHasWeather by hubViewModel.hasNativeWeather.collectAsStateWithLifecycle()
    val weatherShown = homeUiState.style.clock.showWeather || homeUiState.showsNativeWeather || hubHasWeather
    HomeRouteEffects(
        state = state,
        swipe = swipe,
        open = open,
        hooks = HomeRouteEffectHooks(
            onResume = {
                homeViewModel.refresh()
                state.resumeCount++
                weatherViewModel.onHomeResumed(weatherShown)
            },
            homePresses = launcherViewModel.homePressedEvent,
            homePressOpensSearch = { launcherViewModel.homePressedWhileResumed && launcherViewModel.uiState.value.homeOpensSearch },
            onVaultBack = appVaultViewModel::onBack,
        ),
    )
    // A long-press sheet (app/folder context menu) is open — Home's swipes must not act on the
    // still-down finger that opened it, or on anything else, until it closes (back still works).
    val homeSwipeGate = LocalHomeSwipeGate.current
    val homeSwipeBlocked = homeSwipeGate?.isBlocked == true
    val homeLayoutMode = rememberHomeLayoutMode()
    val widgetData = rememberClockWidgetRouteData(homeUiState.clockStackWidgetIds.toImmutableList(), homeViewModel.clockWidgetHost, state.resumeCount)
    val itemActions = homeItemActions(drawerViewModel, homeViewModel, onAppClick)
    val stateInputs = HomeRouteStateInputs(homeUiState, weatherUiState, folders, appMenuNotifications, widgetData, homeLayoutMode, widgetAccess)
    val homeScreenState = homeScreenState(stateInputs, state)
    val screenActions = homeRouteScreenActions(
        homeViewModel = homeViewModel,
        weatherViewModel = weatherViewModel,
        stateInputs = stateInputs,
        itemActions = itemActions,
        swipe = swipe,
        navigation = HomeNavigation(
            toSettings = onNavigateToSettings,
            toFacetSettings = onNavigateToFacetSettings,
            toFacetPro = onNavigateToFacetPro,
            toClockStyle = onNavigateToClockStyle,
            toClockAccessories = onNavigateToClockAccessories,
            toClockStack = onNavigateToClockStack,
        ),
        context = LocalContext.current,
        coroutineScope = rememberCoroutineScope(),
        state = state,
    )
    // Read by the swipe detector when a touch comes down, so it must not restart that detector.
    val clockWidgetIsNative by rememberUpdatedState(homeScreenState.widget?.nativeType != null)
    val appListNestedScroll = remember(swipe, homeSwipeBlocked) { swipe.appListNestedScroll(homeSwipeBlocked) }
    val drawerState = rememberHomeDrawerState(drawerViewModel, appMenuNotifications)
        .copy(apps = apps, workProfiles = launcherUiState.workProfiles, folders = folders)
    val drawerActions = remember(drawerViewModel) { homeDrawerActions(drawerViewModel) }
    val activity = LocalActivity.current

    Box(
        modifier = modifier
            .fillMaxSize()
            .onSizeChanged {
                state.containerHeightPx = it.height.toFloat()
                state.containerWidthPx = it.width.toFloat()
            },
    ) {
        CompositionLocalProvider(LocalIconGlassSamplesWallpaper provides true) {
            HomeScreen(
                state = homeScreenState,
                actions = screenActions,
                layoutMode = homeLayoutMode,
                landscapeSpace = rememberHomeLandscapeSpace(),
                // Marks the app as fully drawn once Home has faded in, so startup timing covers what the user sees.
                onFullyReveal = { activity?.reportFullyDrawn() },
                modifier = Modifier
                    .fillMaxSize()
                    .graphicsLayer {
                        // Home gives way to whichever panel (Drawer, Hub, Switch Facets) is opening, the same
                        // way for all three: fades out and shrinks slightly, leaving only the wallpaper and
                        // the panel's own scrim. The shade pull-down fades Home like the Drawer does.
                        val fade = homeLayerFade(maxOf(swipe.drawer.progress.value, swipe.shade.progress.value), swipe.hub.progress.value, swipe.facet.progress.value)
                        alpha = fade.alpha
                        scaleX = fade.scale
                        scaleY = fade.scale
                    }
                    // Receives the app list's own leftover vertical scroll once it bottoms/tops out.
                    .nestedScroll(appListNestedScroll)
                    .homeSwipeInput(
                        swipe = swipe,
                        homeMenuMode = state.homeMenuMode,
                        // Suppress screen-wide gestures while dragging a handle or with a long-press sheet open.
                        suppressed = state.draggingHandle || homeSwipeBlocked,
                        isBlocked = { homeSwipeGate?.isBlocked == true },
                        claimAt = { position ->
                            homeTouchClaim(position, state.clockWidgetBounds, clockWidgetIsNative, state.appListBounds, state.clockPaneBounds)
                        },
                    ),
            )
        }

        HomePanelOverlays(
            swipe = swipe,
            overlays = overlays,
            homeScreenState = homeScreenState,
            screenActions = screenActions,
            widgetAccess = widgetAccess,
            drawerState = drawerState,
            drawerActions = drawerActions,
            itemActions = itemActions,
            open = open,
            searchFocusRequest = state.searchFocusRequest,
            clockWidgetNeedsListening = homeUiState.clockStackWidgetIds.isNotEmpty(),
            onHubShow = hubViewModel::onHubVisible,
            onHubHide = hubViewModel::onHubHidden,
            onNavigateToSettings = onNavigateToSettings,
        )
        HomePickerOverlays(
            swipe = swipe,
            overlays = overlays,
            privateSpaceState = rememberHomePrivateSpaceState(
                privateSpaceViewModel,
                drawerState.privateSpaceState,
                drawerState.settings.recentlyInstalledPosition,
                drawerState.settings.drawerListItemSize,
            ),
            onAppClick = onAppClick,
            onPrivateSpaceQueryChange = privateSpaceViewModel::onQueryChange,
            onWidgetPickerReset = widgetPickerViewModel::reset,
            onWidgetPickerOpen = widgetPickerViewModel::refresh,
        )
        HomeFacetCarousel(
            swipe = swipe,
            isOpen = open.facet,
            onNavigateToSettings = onNavigateToSettings,
            onNavigateToFacetSettings = onNavigateToFacetSettings,
            onNavigateToManageFacets = onNavigateToManageFacets,
            onNavigateToFacetPro = onNavigateToFacetPro,
        )
        HomeClockWidgetPicker(overlays, homeUiState, onReset = clockWidgetPickerViewModel::reset, onOpen = clockWidgetPickerViewModel::refresh)
        HomeSheetOverlays(
            overlays = overlays,
            homeUiState = homeUiState,
            weatherUiState = weatherUiState,
            widgetAccess = widgetAccess,
            homeMenuMode = state.homeMenuMode,
            onCloseCalendarSettings = { state.homeMenuMode = HomeMenuMode.NONE },
            onRefreshWeather = { weatherViewModel.refresh() },
            onNavigateToClockAccessories = onNavigateToClockAccessories,
            onNavigateToUnitsAndLocation = onNavigateToUnitsAndLocation,
            onNavigateToPermissions = onNavigateToPermissions,
        )
        val whatsNewVersion by whatsNewPillViewModel.version.collectAsStateWithLifecycle()
        val wallpaperCopyOffer by wallpaperCopyPillViewModel.offer.collectAsStateWithLifecycle()
        HomePromptOverlays(
            homeUiState = homeUiState,
            open = open,
            whatsNewVersion = whatsNewVersion,
            wallpaperCopyOffer = wallpaperCopyOffer,
            prompts = HomePromptActions(
                requestDefaultLauncherIntent = homeViewModel::requestDefaultLauncherIntent,
                onFinishSetDefault = homeViewModel::dismissSetDefaultPrompt,
                onDismissGestureHint = homeViewModel::dismissGestureHint,
                onWhatsNewShown = whatsNewPillViewModel::onShown,
                onOpenWhatsNew = onNavigateToWhatsNew,
                onWallpaperCopyShown = wallpaperCopyPillViewModel::onShown,
                onOpenWallpaperCapture = onNavigateToWallpaperCapture,
            ),
        )
    }
}
