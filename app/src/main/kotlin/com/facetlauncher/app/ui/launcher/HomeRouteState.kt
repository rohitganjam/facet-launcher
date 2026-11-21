package com.facetlauncher.app.ui.launcher

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.geometry.Rect
import com.facetlauncher.app.data.model.SwipeActions
import com.facetlauncher.app.ui.home.ClockPagerState
import com.facetlauncher.app.ui.home.HomeMenuMode
import com.facetlauncher.app.ui.home.HomeUiState
import com.facetlauncher.app.ui.home.visibleIndex
import com.facetlauncher.app.ui.home.widget.ClockWidgetPickerTarget
import com.facetlauncher.app.ui.theme.LocalSystemBarsState
import kotlinx.coroutines.flow.Flow

/** The route's own mutable state: what its container measures, which overlays are up, and where Home reported its touch zones. */
internal class HomeRouteState {
    var containerHeightPx by mutableFloatStateOf(2000f)
    var containerWidthPx by mutableFloatStateOf(1000f)
    val overlays = HomeOverlayState()
    var homeMenuMode by mutableStateOf(HomeMenuMode.NONE)
    var draggingHandle by mutableStateOf(false)
    var searchFocusRequest by mutableIntStateOf(0)

    /** Counts resumes so the orphaned-widget check re-runs: the app can be uninstalled while Home is away. */
    var resumeCount by mutableIntStateOf(0)

    // Landscape only: the clock pane scrolls its own content, so swiping up to open the drawer works on the apps side only.
    var clockPaneBounds by mutableStateOf<Rect?>(null)

    // Portrait only: everything above the divider. A horizontal drag in it or in the landscape clock pane turns the clock stack's pages.
    var clockZoneBounds by mutableStateOf<Rect?>(null)

    // Reported by HomeScreen: see detectHomeSwipeGestures' shouldClaim for why the swipe needs them.
    var clockWidgetBounds by mutableStateOf<Rect?>(null)
    var appListBounds by mutableStateOf<Rect?>(null)

    /** The state Home's actions change, as the hooks they take. */
    fun hooks(openWeather: () -> Unit) = HomeRouteHooks(
        setAdjustMode = { homeMenuMode = it },
        setDraggingHandle = { draggingHandle = it },
        setClockWidgetBounds = { clockWidgetBounds = it },
        setAppListBounds = { appListBounds = it },
        setClockPaneBounds = { clockPaneBounds = it },
        setClockZoneBounds = { clockZoneBounds = it },
        openClockWidgetPicker = { itemId ->
            overlays.clockWidgetPickerTarget = ClockWidgetPickerTarget.Replace(itemId)
            overlays.showClockWidgetPicker = true
        },
        openAddClockWidgetPicker = { facetId ->
            overlays.clockWidgetPickerTarget = ClockWidgetPickerTarget.Add(facetId)
            overlays.showClockWidgetPicker = true
        },
        openWeather = openWeather,
    )
}

/** What the route's effects ask of the view models, so they never hold one. */
internal class HomeRouteEffectHooks(
    /** Home came back to the front: re-read what changed while it was away. */
    val onResume: () -> Unit,
    val homePresses: Flow<Unit>,
    /** Whether the Home press that just arrived should open search: the setting is on and Home was in front. */
    val homePressOpensSearch: () -> Boolean,
    /** Steps the App Vault back one level, and says whether it did. */
    val onVaultBack: () -> Boolean,
)

@Composable
internal fun rememberHomeRouteState(): HomeRouteState = remember { HomeRouteState() }

/** The swipe controller over [state]'s container, following the latest [homeUiState] and [swipeActions]. */
@Composable
internal fun rememberHomeSwipeController(
    state: HomeRouteState,
    expandNotificationShade: () -> Unit,
    onClockPageChange: (itemId: Long) -> Unit,
    homeUiState: HomeUiState,
    swipeActions: SwipeActions,
): HomeSwipeController {
    val coroutineScope = rememberCoroutineScope()
    val currentHomeUiState by rememberUpdatedState(homeUiState)
    val systemBarsState = LocalSystemBarsState.current
    val swipe = remember {
        HomeSwipeController(
            containerHeightPx = { state.containerHeightPx },
            containerWidthPx = { state.containerWidthPx },
            coroutineScope = coroutineScope,
            endClockAdjust = { state.homeMenuMode = HomeMenuMode.NONE },
            // A hidden status bar comes back first; the next swipe-down (bar showing) opens the shade.
            expandNotificationShade = {
                if (systemBarsState?.revealStatusBar() != true) expandNotificationShade()
            },
            clockPager = ClockPagerState(
                pageCount = { currentHomeUiState.clockStack.size },
                pageIndex = { currentHomeUiState.clockStack.visibleIndex(currentHomeUiState.visibleClockItemId) },
                onPageChange = { index -> currentHomeUiState.clockStack.getOrNull(index)?.let { onClockPageChange(it.id) } },
                // The pane in landscape, where the clock page is the pane's width.
                widthPx = { state.clockPaneBounds?.width ?: state.containerWidthPx },
                coroutineScope = coroutineScope,
            ),
            isInClockZone = { position -> state.clockZoneBounds?.contains(position) == true || state.clockPaneBounds?.contains(position) == true },
        )
    }
    LaunchedEffect(swipeActions) { swipe.actions = swipeActions }
    return swipe
}

/** Which of the three panels is open, read in composition so the route recomposes when one starts or finishes opening. */
@Composable
internal fun rememberPanelsOpen(swipe: HomeSwipeController): PanelsOpen {
    val drawer by remember { derivedStateOf { swipe.drawer.progress.value > 0f } }
    val hub by remember { derivedStateOf { swipe.hub.progress.value > 0f } }
    val facet by remember { derivedStateOf { swipe.facet.progress.value > 0f } }
    return PanelsOpen(drawer = drawer, hub = hub, facet = facet)
}
