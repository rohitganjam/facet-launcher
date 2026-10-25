package com.facetlauncher.app.ui.home

import com.facetlauncher.app.domain.ListAlignment
import android.content.res.Configuration
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInRoot
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.facetlauncher.app.data.model.landscape
import com.facetlauncher.app.data.model.AppInfo
import com.facetlauncher.app.data.model.AppListColumnAlignment
import com.facetlauncher.app.data.model.AppListVerticalAlignment
import com.facetlauncher.app.data.model.AppRowPosition
import com.facetlauncher.app.data.model.AppRowPresentation
import com.facetlauncher.app.data.model.PlacedItem
import com.facetlauncher.app.ui.home.ClockAdjustMode
import com.facetlauncher.app.ui.home.HomeDockState
import com.facetlauncher.app.ui.home.HomeItemActions
import com.facetlauncher.app.ui.home.HomeLayoutMode
import com.facetlauncher.app.ui.home.HomeListState
import com.facetlauncher.app.ui.home.HomeScreen
import com.facetlauncher.app.ui.home.HomeScreenActions
import com.facetlauncher.app.ui.home.HomeScreenState
import com.facetlauncher.app.ui.home.rememberHomeLandscapeSpace
import com.facetlauncher.app.ui.home.rememberHomeLayoutMode
import com.facetlauncher.app.ui.theme.FacetLauncherTheme
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

/**
 * Home surface (`1a`, Airy density `1e`): clock + a short curated app list + dock. It only renders [state]
 * and reports through [actions]; [HomeFrame] resolves the geometry, and the layouts, slots and adjust
 * overlays live beside it.
 */
@Composable
fun HomeScreen(
    state: HomeScreenState,
    actions: HomeScreenActions,
    modifier: Modifier = Modifier,
    layoutMode: HomeLayoutMode = rememberHomeLayoutMode(),
    landscapeSpace: HomeLandscapeSpace = rememberHomeLandscapeSpace(),
    /** Called once, when the clock and the app list have both finished fading in: Home is then fully drawn. */
    onFullyReveal: () -> Unit = {},
) {
    val layout = rememberHomeScreenLayout()
    val frame = rememberHomeFrame(state, actions, layout, layoutMode, landscapeSpace)
    HomeScreenEffects(frame)
    val currentFrame by rememberUpdatedState(frame)
    val currentOnFullyReveal by rememberUpdatedState(onFullyReveal)
    LaunchedEffect(Unit) {
        snapshotFlow { currentFrame.isFullyRevealed }.first { it }
        currentOnFullyReveal()
    }
    val clock = state.clock
    Box(
        modifier = modifier
            .fillMaxSize()
            .onGloballyPositioned {
                layout.rootSize = it.size
                layout.rootOriginInRoot = it.positionInRoot()
            }
            .homeEmptySpaceGestures(clock.adjustMode, actions.clock.onAdjustModeChange)
            .testTag(homeTag("home_screen_root")),
    ) {
        if (frame.isLandscape) HomeLandscapeContent(frame) else HomePortraitContent(frame)
        val adjustEnv = ClockAdjustEnv(layout, frame.geometry, frame.metrics, clock, state.widget, actions, frame.widgetMinPx, frame.landscapeDockReserve)
        ClockAdjustOverlays(adjustEnv, landscape = frame.isLandscape)
        ClockAdjustPills(adjustEnv, adjusting = clock.adjustMode == ClockAdjustMode.ADJUST, landscape = frame.isLandscape)
        HomeBoundsReporting(frame)
        if (clock.adjustMode == ClockAdjustMode.MENU) ClockMenuSheet(clock, state.widget != null, actions.clock)
    }
}

/** Minimum gap always kept between the clock+calendar block's bottom edge and its grab handle. */
internal val HOME_CLOCK_MIN_GAP = 24.dp

/** Gap between the clock block's bottom edge and the media row, and between the media row and the app list below it. */
internal val HOME_MEDIA_ROW_TOP_GAP = 8.dp
internal val HOME_MEDIA_ROW_BOTTOM_GAP = 12.dp

/** Visible gap between the height handle's touch strip and the alignment toolbar below it — Material's minimum between touch targets. */
internal val CLOCK_ADJUST_TOOLBAR_GAP = 8.dp

/** How long the toolbar stays dimmed and disabled after a drag ends — see its use in [HomeScreen]. */
internal const val CLOCK_ADJUST_TOOLBAR_REAPPEAR_DELAY_MS = 200L

/** The toolbar's opacity while a drag is active or just ended — a visible ghost, not gone. */
internal const val CLOCK_ADJUST_TOOLBAR_DIMMED_ALPHA = 0.25f



/** Half the resize handle's hit box — the amount it extends past the clock corner it sits on. */
internal val HOME_CLOCK_RESIZE_HANDLE_INSET = 20.dp

internal val HOME_APP_ROW_REGULAR_VERTICAL_PADDING = 16.dp

/**
 * [AppRow]'s tighter per-row vertical padding, switched to automatically (see this file's
 * one-shot compact-spacing check) when the regular density would make the app list need to
 * scroll. Matches App Drawer's own `DrawerListItemSize.COMPACT` total row padding (base 8dp +
 * 0 extra), so Home's compact density reads the same as Drawer's.
 */
internal val HOME_APP_ROW_COMPACT_VERTICAL_PADDING = 8.dp

@Preview(showBackground = true, widthDp = 390, heightDp = 844)
@Preview(name = "Dark", showBackground = true, widthDp = 390, heightDp = 844, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun HomeScreenPreview() {
    FacetLauncherTheme {
        val apps = (1..9).map {
            AppInfo(packageName = "com.example.app$it", activityName = ".MainActivity", label = "App $it", icon = null)
        }
        HomeScreen(
            state = HomeScreenState(
                list = HomeListState(items = apps.take(5).map { PlacedItem.SingleApp(it) }),
                dock = HomeDockState(items = apps.drop(5).take(4).map { PlacedItem.SingleApp(it) }),
            ),
            actions = HomeScreenActions(item = HomeItemActions(onAppClick = {})),
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
            state = HomeScreenState(
                list = HomeListState(
                    items = apps.map { PlacedItem.SingleApp(it) },
                    alignment = ListAlignment(AppRowPosition.RIGHT, AppListVerticalAlignment.BOTTOM, AppListColumnAlignment.BOTH_LEFT),
                ),
                dock = HomeDockState(items = emptyList()),
            ),
            actions = HomeScreenActions(item = HomeItemActions(onAppClick = {})),
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
            state = HomeScreenState(
                list = HomeListState(items = apps.map { PlacedItem.SingleApp(it) }, rowPresentation = AppRowPresentation.TEXT_ONLY),
                dock = HomeDockState(items = emptyList()),
            ),
            actions = HomeScreenActions(item = HomeItemActions(onAppClick = {})),
        )
    }
}
