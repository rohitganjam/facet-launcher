package com.facetlauncher.app.ui.launcher

import android.content.Intent
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.facetlauncher.app.data.model.AppInfo
import com.facetlauncher.app.data.model.ClockStackItemKind
import com.facetlauncher.app.data.model.ProReason
import com.facetlauncher.app.data.model.SwipeDirection
import com.facetlauncher.app.ui.facets.FacetCarouselScreen
import com.facetlauncher.app.ui.facets.PagerDismissEdge
import com.facetlauncher.app.ui.home.HomeItemActions
import com.facetlauncher.app.ui.home.HomeMenuMode
import com.facetlauncher.app.ui.home.HomeScreenActions
import com.facetlauncher.app.ui.home.HomeScreenState
import com.facetlauncher.app.ui.home.HomeUiState
import com.facetlauncher.app.ui.home.WallpaperCopyOffer
import com.facetlauncher.app.ui.home.WallpaperCopyPillHost
import com.facetlauncher.app.ui.home.WhatsNewPillHost
import com.facetlauncher.app.ui.home.weather.WeatherSheet
import com.facetlauncher.app.ui.home.weather.WeatherUiState
import com.facetlauncher.app.ui.home.widget.ClockWidgetPickerScreen
import com.facetlauncher.app.ui.hub.HubScreen
import com.facetlauncher.app.ui.hub.picker.HubWidgetPickerScreen
import com.facetlauncher.app.ui.nativewidget.NativeWidgetAccessControls
import com.facetlauncher.app.ui.nativewidget.NativeWidgetActions
import com.facetlauncher.app.ui.nativewidget.NativeWidgetEnvironment
import com.facetlauncher.app.ui.nativewidget.config.NativeWidgetConfigSheet
import com.facetlauncher.app.ui.nativewidget.withWeatherOverride
import com.facetlauncher.app.ui.settings.CalendarSettingsSheet
import com.facetlauncher.app.ui.theme.launcherSystemBarInsets
import kotlinx.coroutines.launch

/** The Hub and the Drawer, the two panels that follow the finger over Home (the facet carousel is [HomeFacetCarousel]). */
@Composable
internal fun BoxScope.HomePanelOverlays(
    swipe: HomeSwipeController,
    overlays: HomeOverlayState,
    homeScreenState: HomeScreenState,
    screenActions: HomeScreenActions,
    widgetAccess: NativeWidgetAccessControls,
    drawerState: HomeDrawerState,
    drawerActions: HomeDrawerActions,
    itemActions: HomeItemActions,
    open: PanelsOpen,
    searchFocusRequest: Int,
    clockWidgetNeedsListening: Boolean,
    onHubShow: () -> Unit,
    onHubHide: () -> Unit,
    onNavigateToSettings: () -> Unit,
) {
    HomeHubOverlay(
        hub = swipe.hub,
        direction = swipe.hubDirection,
        isHubOpen = open.hub,
        clockWidgetNeedsListening = clockWidgetNeedsListening,
        overlays = overlays,
        onHubShow = onHubShow,
        onHubHide = onHubHide,
    ) {
        HubScreen(
            onAddClick = { overlays.showWidgetPicker = true },
            onManageClick = {}, // wired once there's a dedicated manage flow
            modifier = Modifier.fillMaxSize().testTag("hub_screen"),
            nativeWidgets = NativeWidgetEnvironment(
                state = homeScreenState.nativeWidgets,
                actions = NativeWidgetActions(
                    onOpenWeather = screenActions.clock.onOpenNativeWeather,
                    onConfigure = screenActions.clock.onConfigureNativeWidget,
                    onOpenEvent = screenActions.clock.onEventClick,
                    media = screenActions.media,
                    onRequestCalendarAccess = widgetAccess.requestCalendar,
                    onRequestLocationAccess = widgetAccess.requestLocation,
                ),
            ),
        )
    }
    HomeAppDrawerOverlay(
        state = drawerState,
        actions = drawerActions,
        itemActions = itemActions,
        drawer = swipe.drawer,
        direction = swipe.drawerDirection,
        isDrawerOpen = open.drawer,
        overlays = overlays,
        onNavigateToSettings = onNavigateToSettings,
        searchFocusRequest = searchFocusRequest,
    )
}

/** The Switch Facets carousel, dragged in from whichever edge the gesture started at. */
@Composable
internal fun HomeFacetCarousel(
    swipe: HomeSwipeController,
    isOpen: Boolean,
    onNavigateToSettings: () -> Unit,
    onNavigateToFacetSettings: (facetId: Long) -> Unit,
    onNavigateToManageFacets: () -> Unit,
    onNavigateToFacetPro: (ProReason) -> Unit,
) {
    val coroutineScope = rememberCoroutineScope()
    val facetDirection = swipe.facetDirection
    // A carousel that came in from a side is dismissed by its pager's edge page; one from above or below by its surface.
    val facetPagerDismissEdge = when (facetDirection) {
        SwipeDirection.LEFT -> PagerDismissEdge.FIRST
        SwipeDirection.RIGHT -> PagerDismissEdge.LAST
        else -> null
    }
    HomeFacetCarouselOverlay(swipe.facet, facetDirection, isOpen = isOpen) {
        FacetCarouselScreen(
            isOpen = isOpen,
            onFacetApply = { coroutineScope.launch { swipe.facet.close() } },
            onEditFacet = onNavigateToFacetSettings,
            onReorderFacets = onNavigateToManageFacets,
            onNavigateToSettings = onNavigateToSettings,
            onOpenFacetPro = onNavigateToFacetPro,
            pagerDismissEdge = facetPagerDismissEdge,
            onDismissDrag = { deltaPx -> swipe.facet.dragBy(deltaPx * facetDirection.fingerSign) },
            onDismissDragEnd = { swipe.facet.settle() },
            modifier = Modifier.fillMaxSize(),
        )
    }
}

/** Whether each of the three panels is open, so a composable that only needs to know doesn't take the swipe controller. */
internal data class PanelsOpen(val drawer: Boolean, val hub: Boolean, val facet: Boolean) {
    val any: Boolean get() = drawer || hub || facet
}

/** The full-screen screens over Home: Private Space, the App Vault and the Hub's widget picker. */
@Composable
internal fun HomePickerOverlays(
    swipe: HomeSwipeController,
    overlays: HomeOverlayState,
    privateSpaceState: HomePrivateSpaceState,
    onAppClick: (AppInfo) -> Unit,
    onPrivateSpaceQueryChange: (String) -> Unit,
    onWidgetPickerReset: () -> Unit,
) {
    val coroutineScope = rememberCoroutineScope()
    HomePrivateSpaceOverlay(
        overlays = overlays,
        state = privateSpaceState,
        onQueryChange = onPrivateSpaceQueryChange,
        onAppClick = { app ->
            onAppClick(app)
            overlays.showPrivateSpace = false
            coroutineScope.launch { swipe.drawer.close() }
        },
    )
    HomeAppVaultOverlay(
        overlays = overlays,
        onAppClick = { app ->
            onAppClick(app)
            overlays.showAppVault = false
            coroutineScope.launch { swipe.drawer.close() }
        },
    )
    HomeWidgetPickerOverlay(overlays, onReset = onWidgetPickerReset) {
        HubWidgetPickerScreen(onDone = { overlays.showWidgetPicker = false }, onConfigureNewWidget = { overlays.configureRef = it })
    }
}

/** The picker that adds or replaces a clock-stack page; whether the plain clock is still available comes from [homeUiState]. */
@Composable
internal fun HomeClockWidgetPicker(overlays: HomeOverlayState, homeUiState: HomeUiState, onReset: () -> Unit) {
    HomeClockWidgetPickerOverlay(overlays, onReset = onReset) { target ->
        ClockWidgetPickerScreen(
            target = target,
            onDone = { overlays.showClockWidgetPicker = false },
            onConfigureNewWidget = { overlays.configureRef = it },
            clockAvailable = homeUiState.clockStack.none { it.kind == ClockStackItemKind.CLOCK },
        )
    }
}

/** What Home opens from its own controls: the weather sheet, the calendar settings sheet and a native widget's configuration. */
@Composable
internal fun HomeSheetOverlays(
    overlays: HomeOverlayState,
    homeUiState: HomeUiState,
    weatherUiState: WeatherUiState,
    widgetAccess: NativeWidgetAccessControls,
    homeMenuMode: HomeMenuMode,
    onCloseCalendarSettings: () -> Unit,
    onRefreshWeather: () -> Unit,
    onNavigateToClockAccessories: (facetId: Long?) -> Unit,
    onNavigateToUnitsAndLocation: () -> Unit,
    onNavigateToPermissions: () -> Unit,
) {
    if (overlays.showWeatherSheet) {
        val sheetRef = overlays.weatherSheetRef
        WeatherSheet(
            uiState = weatherUiState.copy(iconStyle = homeUiState.style.clock.weatherIconStyle).withWeatherOverride(sheetRef?.let { widgetAccess.overrides[it] }),
            onConfigure = if (sheetRef != null) {
                {
                    overlays.showWeatherSheet = false
                    overlays.configureRef = sheetRef
                }
            } else {
                // The clock's own weather: its settings are the active facet's Clock accessories.
                {
                    overlays.showWeatherSheet = false
                    onNavigateToClockAccessories(homeUiState.activeFacet?.id)
                }
            },
            onRefresh = onRefreshWeather,
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
    if (homeMenuMode == HomeMenuMode.CALENDAR_SETTINGS) {
        homeUiState.activeFacet?.let { facet -> CalendarSettingsSheet(facetId = facet.id, onDismiss = onCloseCalendarSettings) }
    }
    overlays.configureRef?.let { ref -> NativeWidgetConfigSheet(ref = ref, onDismiss = { overlays.configureRef = null }) }
}

/** The one-time prompts, and the What's new and wallpaper-copy pills that hover under the status bar. */
@Composable
internal fun BoxScope.HomePromptOverlays(
    homeUiState: HomeUiState,
    open: PanelsOpen,
    whatsNewVersion: String?,
    wallpaperCopyOffer: WallpaperCopyOffer?,
    prompts: HomePromptActions,
) {
    HomeOneTimePrompts(
        showSetDefaultPrompt = homeUiState.showSetDefaultPrompt,
        showGestureHint = homeUiState.showGestureHint,
        homeAtRest = !open.any,
        requestDefaultLauncherIntent = prompts.requestDefaultLauncherIntent,
        onFinishSetDefault = prompts.onFinishSetDefault,
        onDismissGestureHint = prompts.onDismissGestureHint,
    )
    // Hovers over Home, below the status bar, without moving anything; waits for the first-run prompts and a resting Home.
    WhatsNewPillHost(
        version = whatsNewVersion,
        ready = homeUiState.firstRunPromptsDone && !open.any,
        onShow = prompts.onWhatsNewShown,
        onOpen = prompts.onOpenWhatsNew,
        modifier = Modifier.align(Alignment.TopCenter)
            .windowInsetsPadding(launcherSystemBarInsets().only(WindowInsetsSides.Top))
            .padding(top = 12.dp),
    )
    // Same spot as the What's new pill, which goes first.
    WallpaperCopyPillHost(
        offer = wallpaperCopyOffer,
        ready = homeUiState.firstRunPromptsDone && whatsNewVersion == null && !open.any,
        onShow = prompts.onWallpaperCopyShown,
        onOpen = prompts.onOpenWallpaperCapture,
        modifier = Modifier.align(Alignment.TopCenter)
            .windowInsetsPadding(launcherSystemBarInsets().only(WindowInsetsSides.Top))
            .padding(top = 12.dp),
    )
}

/** What the prompts and pills call back into, bundled so the route hands over one object instead of eight lambdas. */
internal class HomePromptActions(
    val requestDefaultLauncherIntent: () -> Intent,
    val onFinishSetDefault: () -> Unit,
    val onDismissGestureHint: () -> Unit,
    val onWhatsNewShown: () -> Unit,
    val onOpenWhatsNew: () -> Unit,
    val onWallpaperCopyShown: (String) -> Unit,
    val onOpenWallpaperCapture: () -> Unit,
)
