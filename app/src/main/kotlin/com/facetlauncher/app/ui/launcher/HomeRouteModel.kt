package com.facetlauncher.app.ui.launcher

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import com.facetlauncher.app.data.model.AppInfo
import com.facetlauncher.app.data.model.Folder
import com.facetlauncher.app.data.model.NativeWidgetRef
import com.facetlauncher.app.ui.components.AppMenuNotifications
import com.facetlauncher.app.ui.drawer.DrawerViewModel
import com.facetlauncher.app.ui.home.HomeItemActions
import com.facetlauncher.app.ui.home.HomeLayoutMode
import com.facetlauncher.app.ui.home.HomeLiveState
import com.facetlauncher.app.ui.home.HomeScreenActions
import com.facetlauncher.app.ui.home.HomeScreenContent
import com.facetlauncher.app.ui.home.HomeScreenState
import com.facetlauncher.app.ui.home.HomeUiState
import com.facetlauncher.app.ui.home.HomeViewModel
import com.facetlauncher.app.ui.home.HomeWidgetSizing
import com.facetlauncher.app.ui.home.toHomeScreenState
import com.facetlauncher.app.ui.home.weather.WeatherUiState
import com.facetlauncher.app.ui.home.weather.WeatherViewModel
import com.facetlauncher.app.ui.nativewidget.NativeWidgetAccessControls
import kotlinx.collections.immutable.ImmutableList
import kotlinx.coroutines.CoroutineScope

/** What an app's menu shows and does about its notifications. */
@Composable
internal fun rememberAppMenuNotifications(drawerViewModel: DrawerViewModel, onEnableAccess: () -> Unit): AppMenuNotifications {
    val currentOnEnableAccess by rememberUpdatedState(onEnableAccess)
    return remember(drawerViewModel) {
        AppMenuNotifications(
            observe = drawerViewModel::observeAppNotifications,
            onOpen = drawerViewModel::openNotification,
            onDismiss = drawerViewModel::dismissNotifications,
            onEnableAccess = { currentOnEnableAccess() },
        )
    }
}

internal fun homeItemActions(drawerViewModel: DrawerViewModel, homeViewModel: HomeViewModel, onAppClick: (AppInfo) -> Unit) = HomeItemActions(
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
    onReorderList = drawerViewModel::onReorderList,
    onMoveGridItems = drawerViewModel::onMoveGridItems,
    onReorderFolderApps = drawerViewModel::onReorderFolderApps,
)

/** What Home draws, from the view models' states and the route's own. */
internal class HomeRouteStateInputs(
    val homeUiState: HomeUiState,
    val weatherUiState: WeatherUiState,
    val folders: ImmutableList<Folder>,
    val appMenuNotifications: AppMenuNotifications,
    val widgetData: ClockWidgetRouteData,
    val layoutMode: HomeLayoutMode,
    val widgetAccess: NativeWidgetAccessControls,
)

internal fun homeScreenState(inputs: HomeRouteStateInputs, state: HomeRouteState): HomeScreenState {
    val homeUiState = inputs.homeUiState
    val widgetData = inputs.widgetData
    val styled = homeUiState.style.toHomeScreenState(
        landscape = inputs.layoutMode == HomeLayoutMode.LANDSCAPE,
        settings = homeUiState.settings,
        content = HomeScreenContent(
            listItems = homeUiState.appListItems,
            listCells = homeUiState.favoriteCells,
            dockItems = homeUiState.dockApps,
            calendarEvents = homeUiState.calendarEvents,
            badgeCounts = homeUiState.badgeCounts,
            appMenuNotifications = inputs.appMenuNotifications,
            folderCandidates = inputs.folders,
            showUsageListHint = homeUiState.showUsageListHint,
            clockAccessories = homeUiState.clockAccessories,
            mediaPlayer = homeUiState.mediaPlayer,
            weather = inputs.weatherUiState.weather,
            weatherRefreshing = inputs.weatherUiState.isRefreshing,
        ),
        // The persisted size (once the user has actually resized) wins over the provider's own default.
        widgetSizing = HomeWidgetSizing(
            defaultSizeDp = { widgetData.defaultSizesDp[it] },
            resizeFloorDp = { widgetData.resizeFloorsDp[it] },
            isOrphaned = { widgetData.orphans[it] == true },
        ),
        live = HomeLiveState(
            adjustMode = state.homeMenuMode,
            draggingHandle = state.draggingHandle,
            placementFacetId = homeUiState.activeFacet?.id,
            visibleClockItemId = homeUiState.visibleClockItemId,
            isPro = homeUiState.isPro,
        ),
    )
    return styled.copy(nativeWidgets = styled.nativeWidgets.copy(access = inputs.widgetAccess.access, overrides = inputs.widgetAccess.overrides))
}

internal fun homeRouteScreenActions(
    homeViewModel: HomeViewModel,
    weatherViewModel: WeatherViewModel,
    stateInputs: HomeRouteStateInputs,
    itemActions: HomeItemActions,
    swipe: HomeSwipeController,
    navigation: HomeNavigation,
    context: Context,
    coroutineScope: CoroutineScope,
    state: HomeRouteState,
): HomeScreenActions {
    val overlays = state.overlays
    val widgetAccess = stateInputs.widgetAccess
    val openWeatherSheet: (NativeWidgetRef?) -> Unit = { ref ->
        overlays.weatherSheetRef = ref
        overlays.showWeatherSheet = true
        weatherViewModel.refresh(userInitiated = false)
    }
    val built = homeScreenActions(
        viewModel = homeViewModel,
        inputs = HomeActionsInputs(
            stateInputs.homeUiState,
            stateInputs.layoutMode,
            context,
            coroutineScope,
            stateInputs.widgetData.hosts,
        ),
        itemActions = itemActions,
        swipe = swipe,
        hooks = state.hooks(openWeather = { openWeatherSheet(null) }),
        navigation = navigation,
    )
    return built.copy(
        clock = built.clock.copy(
            onOpenNativeWeather = openWeatherSheet,
            onConfigureNativeWidget = { overlays.configureRef = it },
            onRequestCalendarAccess = widgetAccess.requestCalendar,
            onRequestLocationAccess = widgetAccess.requestLocation,
        ),
    )
}
