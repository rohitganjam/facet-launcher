package com.facetlauncher.app.ui.launcher

import android.content.ContentUris
import android.content.Context
import android.content.Intent
import android.provider.AlarmClock
import android.provider.CalendarContract
import androidx.compose.ui.geometry.Rect
import com.facetlauncher.app.data.model.ProReason
import com.facetlauncher.app.ui.home.ClockAdjustMode
import com.facetlauncher.app.ui.home.HomeClockActions
import com.facetlauncher.app.ui.home.HomeClockWidgetActions
import com.facetlauncher.app.ui.home.HomeItemActions
import com.facetlauncher.app.ui.home.HomeLayoutMode
import com.facetlauncher.app.ui.home.HomeMediaActions
import com.facetlauncher.app.ui.home.HomeScreenActions
import com.facetlauncher.app.ui.home.HomeUiState
import com.facetlauncher.app.ui.home.HomeViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

/** Where Home's actions can send the user. */
internal class HomeNavigation(
    val toSettings: () -> Unit,
    val toFacetSettings: (facetId: Long) -> Unit,
    val toFacetPro: (ProReason) -> Unit,
    val toUsageAccessExplanation: () -> Unit,
    /** The clock style gallery, global (`null`) or for one facet. */
    val toClockStyleGallery: (Long?) -> Unit,
)

/** The route's own state that Home's actions change. */
internal class HomeRouteHooks(
    val setAdjustMode: (ClockAdjustMode) -> Unit,
    val setDraggingHandle: (Boolean) -> Unit,
    val setClockWidgetBounds: (Rect?) -> Unit,
    val setAppListBounds: (Rect?) -> Unit,
    val setClockPaneBounds: (Rect?) -> Unit,
    val openClockWidgetPicker: (facetId: Long) -> Unit,
    val openWeather: () -> Unit = {},
)

/** What [homeScreenActions] reads: the values Home's actions act on. */
internal class HomeActionsInputs(
    val homeUiState: HomeUiState,
    val clockWidgetIsPro: Boolean,
    val layoutMode: HomeLayoutMode,
    val context: Context,
    val coroutineScope: CoroutineScope,
)

/** Everything [HomeScreen][com.facetlauncher.app.ui.home.HomeScreen] can do, wired to the view model, the swipes and the route. */
internal fun homeScreenActions(
    viewModel: HomeViewModel,
    inputs: HomeActionsInputs,
    itemActions: HomeItemActions,
    swipe: HomeSwipeController,
    hooks: HomeRouteHooks,
    navigation: HomeNavigation,
): HomeScreenActions = HomeScreenActions(
    item = itemActions,
    clock = homeClockActions(viewModel, inputs, hooks, navigation),
    widget = homeClockWidgetActions(viewModel, inputs, swipe, hooks),
    media = HomeMediaActions(
        onPrevious = viewModel::onMediaPrevious,
        onPlayPause = viewModel::onMediaPlayPause,
        onNext = viewModel::onMediaNext,
        onOpen = viewModel::onMediaOpen,
    ),
    onUsageAccessPromptClick = {
        viewModel.dismissUsageAccessPrompt()
        navigation.toUsageAccessExplanation()
    },
    onAppListBoundsChange = hooks.setAppListBounds,
    onClockPaneBoundsChange = hooks.setClockPaneBounds,
)

private fun homeClockActions(
    viewModel: HomeViewModel,
    inputs: HomeActionsInputs,
    hooks: HomeRouteHooks,
    navigation: HomeNavigation,
): HomeClockActions {
    val state = inputs.homeUiState
    val mode = inputs.layoutMode
    return HomeClockActions(
        onClockClick = { inputs.context.openClockApp() },
        onWeatherClick = hooks.openWeather,
        onBatteryClick = { inputs.context.openBatterySettings() },
        onEditClockStyles = {
            hooks.setAdjustMode(ClockAdjustMode.NONE)
            navigation.toClockStyleGallery(state.clockPositionOwningFacet?.id)
        },
        onNavigateToSettings = navigation.toSettings,
        onNavigateToFacetSettings = { state.activeFacet?.let { navigation.toFacetSettings(it.id) } },
        onUseCustomWidgetClick = {
            if (inputs.clockWidgetIsPro) {
                state.activeFacet?.let { hooks.openClockWidgetPicker(it.id) }
            } else {
                navigation.toFacetPro(ProReason.CUSTOM_WIDGET)
            }
        },
        onSwitchToLauncherClockClick = {
            state.activeFacet?.let { facet ->
                inputs.coroutineScope.launch { viewModel.clockWidgetFacet.switchToNativeClock(facet) }
            }
        },
        onEventClick = { event ->
            val uri = ContentUris.withAppendedId(CalendarContract.Events.CONTENT_URI, event.id)
            runCatching { inputs.context.startActivity(Intent(Intent.ACTION_VIEW, uri)) }
        },
        onZoneHeightCommit = { viewModel.onClockZoneHeightCommit(it, mode) },
        onScaleCommit = { viewModel.onClockScaleCommit(it, mode) },
        onAlignmentChange = { viewModel.onClockAlignmentCommit(it, mode) },
        onAdjustModeChange = hooks.setAdjustMode,
        onDraggingHandleChange = hooks.setDraggingHandle,
    )
}

private fun homeClockWidgetActions(
    viewModel: HomeViewModel,
    inputs: HomeActionsInputs,
    swipe: HomeSwipeController,
    hooks: HomeRouteHooks,
): HomeClockWidgetActions {
    return HomeClockWidgetActions(
        createHostView = viewModel.clockWidgetHost::createHostView,
        onSizeChange = viewModel.clockWidgetHost::updateSize,
        onSizeCommit = { widthDp, heightDp ->
            inputs.homeUiState.activeFacet?.let { facet ->
                inputs.coroutineScope.launch { viewModel.clockWidgetFacet.commitSize(facet, widthDp, heightDp, inputs.layoutMode) }
            }
        },
        onBoundsChange = hooks.setClockWidgetBounds,
        onNestedScrollStart = swipe.widgetScroll::start,
        onLeftoverScroll = swipe.widgetScroll::leftoverScroll,
        onNestedScrollStop = swipe.widgetScroll::stop,
        onLeftoverFling = swipe.widgetScroll::leftoverFling,
    )
}

/** Opens system Settings > Battery. */
private fun Context.openBatterySettings() {
    runCatching { startActivity(Intent(Intent.ACTION_POWER_USAGE_SUMMARY).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)) }
}

/**
 * Opens the clock app: `ACTION_SHOW_ALARMS` only finds which package is the clock app, then that
 * package's own launcher entry opens. On some OEM builds the alarms intent itself lands on a narrow
 * alarms screen without the app's tabs. Every real clock app declares a launcher activity.
 */
private fun Context.openClockApp() {
    runCatching {
        Intent(AlarmClock.ACTION_SHOW_ALARMS).resolveActivity(packageManager)
            ?.packageName
            ?.let { packageManager.getLaunchIntentForPackage(it) }
            ?.let { startActivity(it) }
    }
}
