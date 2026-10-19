package com.facetlauncher.app.ui.home

import android.content.pm.ActivityInfo
import android.content.res.Configuration
import androidx.activity.ComponentActivity
import android.appwidget.AppWidgetHostView
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.getUnclippedBoundsInRoot
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.longClick
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.width
import com.facetlauncher.app.data.model.AppInfo
import com.facetlauncher.app.data.model.AppListLayout
import com.facetlauncher.app.data.model.ClockAlignment
import com.facetlauncher.app.data.model.AppListVerticalAlignment
import com.facetlauncher.app.data.model.DockDisplayMode
import com.facetlauncher.app.data.model.MediaPlayerState
import com.facetlauncher.app.data.model.PlacedItem
import com.facetlauncher.app.ui.theme.FacetLauncherTheme
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Before
import org.junit.Rule
import org.junit.Test

/**
 * Landscape Home (panes, divider, dock column) in a genuinely landscape window: the test activity is
 * rotated before each test and `HomeScreen` picks its layout from the window, as it does for real.
 */
class HomeScreenLandscapeTest {

    @get:Rule
    val composeRule = createAndroidComposeRule<ComponentActivity>()

    @Before
    fun rotateToLandscape() {
        composeRule.activityRule.scenario.onActivity { it.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE }
        InstrumentationRegistry.getInstrumentation().waitForIdleSync()
        composeRule.waitUntil(timeoutMillis = 5_000) {
            composeRule.activity.resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE
        }
        composeRule.waitForIdle()
    }

    /** A hosted clock widget for [LandscapeHome]: its size, minimum size and what to do with a saved size. */
    private class TestClockWidget(
        val widthDp: Int,
        val heightDp: Int,
        val minWidthDp: Int = 60,
        val minHeightDp: Int = 40,
        val onSizeCommit: (Int, Int) -> Unit = { _, _ -> },
    )

    /** Long-presses empty space (the top strip) to open the clock menu, then picks "Adjust". */
    private fun enterAdjustModeFromEmptySpace() {
        composeRule.onNodeWithTag("home_screen_root").performTouchInput { longClick(Offset(width * 0.75f, 4f)) }
        composeRule.waitForIdle()
        composeRule.onNodeWithTag("clock_adjust_open").performClick()
        composeRule.waitForIdle()
    }

    private fun apps(count: Int, prefix: String = "App") = (1..count).map {
        AppInfo(packageName = "com.example.${prefix.lowercase()}$it", activityName = ".Main", label = "$prefix $it", icon = null)
    }

    @Composable
    private fun LandscapeHome(
        appListItems: List<AppInfo> = apps(3),
        dockApps: List<AppInfo> = apps(3, prefix = "Dock"),
        clockAlignment: ClockAlignment = ClockAlignment.LEFT,
        appListLayout: AppListLayout = AppListLayout.SINGLE_COLUMN,
        dockDisplayMode: DockDisplayMode = DockDisplayMode.ICONS,
        clockZoneHeightDp: Float? = null,
        onClockZoneHeightCommit: (Float) -> Unit = {},
        clockScale: Float = 0.8f,
        onClockScaleCommit: (Float) -> Unit = {},
        onClockAlignmentChange: (ClockAlignment) -> Unit = {},
        mediaPlayer: MediaPlayerState? = null,
        appListVerticalAlignment: AppListVerticalAlignment = AppListVerticalAlignment.BOTTOM,
        widget: TestClockWidget? = null,
    ) {
        var adjustMode by remember { mutableStateOf(ClockAdjustMode.NONE) }
        var dragging by remember { mutableStateOf(false) }
        FacetLauncherTheme {
            HomeScreen(
                hasCustomClockWidget = widget != null,
                clockWidgetAppWidgetId = widget?.let { WIDGET_ID },
                createClockWidgetHostView = { context, _ -> AppWidgetHostView(context) },
                clockWidgetWidthDp = widget?.widthDp?.dp,
                clockWidgetHeightDp = widget?.heightDp?.dp,
                clockWidgetMinWidthDp = widget?.minWidthDp?.dp,
                clockWidgetMinHeightDp = widget?.minHeightDp?.dp,
                onClockWidgetSizeCommit = { w, h -> widget?.onSizeCommit?.invoke(w, h) },
                appListItems = appListItems.map { PlacedItem.SingleApp(it) },
                dockApps = dockApps.map { PlacedItem.SingleApp(it) },
                onAppClick = {},
                clockAlignment = clockAlignment,
                appListLayout = appListLayout,
                dockDisplayMode = dockDisplayMode,
                clockZoneHeightDp = clockZoneHeightDp,
                onClockZoneHeightCommit = onClockZoneHeightCommit,
                clockScale = clockScale,
                onClockScaleCommit = onClockScaleCommit,
                onClockAlignmentChange = onClockAlignmentChange,
                mediaPlayer = mediaPlayer,
                appListVerticalAlignment = appListVerticalAlignment,
                clockAdjustMode = adjustMode,
                onAdjustModeChange = { adjustMode = it },
                draggingHandle = dragging,
                onDraggingHandleChange = { dragging = it },
            )
        }
    }

    /** Long-presses the clock, then picks "Adjust size & position". */
    private fun enterAdjustMode() {
        composeRule.onNodeWithTag("home_clock_block").performTouchInput { longClick() }
        composeRule.waitForIdle()
        composeRule.onNodeWithTag("clock_adjust_open").performClick()
        composeRule.waitForIdle()
    }

    /** Drags the divider by [totalDeltaX] px in separate steps, so each move gets its own recomposition. */
    private fun dragDividerBy(totalDeltaX: Float, steps: Int = 6) {
        composeRule.onNodeWithTag("home_clock_zone_handle").performTouchInput { down(center) }
        composeRule.waitForIdle()
        repeat(steps) {
            composeRule.onNodeWithTag("home_clock_zone_handle").performTouchInput { moveBy(Offset(totalDeltaX / steps, 0f)) }
            composeRule.waitForIdle()
        }
        composeRule.onNodeWithTag("home_clock_zone_handle").performTouchInput { up() }
        composeRule.waitForIdle()
    }

    private fun boundsOf(tag: String) = composeRule.onNodeWithTag(tag).fetchSemanticsNode().boundsInRoot

    @Test
    fun showsClockAppsAndDockPanesAndNoPortraitDockRow() {
        // Given the landscape Home
        composeRule.setContent { LandscapeHome() }

        // Then each pane is present and the portrait dock row is not
        composeRule.onNodeWithTag("home_landscape_layout").assertIsDisplayed()
        composeRule.onNodeWithTag("home_clock_pane").assertIsDisplayed()
        composeRule.onNodeWithTag("home_apps_pane").assertIsDisplayed()
        composeRule.onNodeWithTag("home_dock_column").assertIsDisplayed()
        composeRule.onNodeWithTag("home_dock_row").assertDoesNotExist()
    }

    @Test
    fun panesSitLeftToRightWithTheDockAtTheRightEdge() {
        composeRule.setContent { LandscapeHome() }
        composeRule.waitForIdle()

        val clock = boundsOf("home_clock_pane")
        val apps = boundsOf("home_apps_pane")
        val dock = boundsOf("home_dock_column")

        // Then clock, apps and dock follow each other, with room between the clock and apps panes
        assert(clock.right < apps.left) { "clock $clock should end before apps $apps begin" }
        assert(apps.right <= dock.left) { "apps $apps should end before the dock $dock begins" }
        assert(dock.right > apps.right) { "dock $dock should be right of the apps pane $apps" }
    }

    @Test
    fun appListRendersInsideTheAppsPane() {
        composeRule.setContent { LandscapeHome(appListItems = apps(2)) }
        composeRule.waitForIdle()

        val pane = boundsOf("home_apps_pane")
        val row = boundsOf("home_app_label_com.example.app1")

        assert(row.left >= pane.left && row.right <= pane.right) { "row $row should be inside the apps pane $pane" }
    }

    @Test
    fun dockShowsIconsOnlyEvenWhenTheDockIsSetToText() {
        // Given a dock set to text-only
        composeRule.setContent { LandscapeHome(dockDisplayMode = DockDisplayMode.TEXT) }
        composeRule.waitForIdle()

        // Then landscape still shows icons, not labels
        composeRule.onNodeWithContentDescription("Dock 1").assertExists()
        composeRule.onNodeWithText("Dock 1").assertDoesNotExist()
    }

    @Test
    fun noDockColumnWhenTheDockIsEmpty() {
        composeRule.setContent { LandscapeHome(dockApps = emptyList()) }

        composeRule.onNodeWithTag("home_dock_column").assertDoesNotExist()
        composeRule.onNodeWithTag("home_apps_pane").assertIsDisplayed()
    }

    @Test
    fun clockAlignmentMapsToTopMiddleAndBottomOfItsPane() {
        // Given the clock alignment driven by state, so one composition can step through all three
        var alignment by mutableStateOf(ClockAlignment.LEFT)
        composeRule.setContent { LandscapeHome(clockAlignment = alignment) }
        composeRule.waitForIdle()
        val top = boundsOf("home_clock_block").center.y

        alignment = ClockAlignment.CENTER
        composeRule.waitForIdle()
        val middle = boundsOf("home_clock_block").center.y

        alignment = ClockAlignment.RIGHT
        composeRule.waitForIdle()
        val bottom = boundsOf("home_clock_block").center.y

        // Then left, center and right become top, middle and bottom
        assert(top < middle && middle < bottom) { "expected top < middle < bottom, got $top, $middle, $bottom" }
    }

    @Test
    fun twoColumnListKeepsItsTwoColumnsInLandscape() {
        composeRule.setContent { LandscapeHome(appListItems = apps(4), appListLayout = AppListLayout.TWO_COLUMN) }
        composeRule.waitForIdle()

        val labels = (1..4).map { boundsOf("home_app_label_com.example.app$it") }

        // Then the apps sit in two columns, so some of them share a row and the left edges differ
        assert(labels.map { it.top.toInt() }.distinct().size < labels.size) { "expected two apps to share a row: ${labels.map { it.top }}" }
        assert(labels.map { it.left.toInt() }.distinct().size >= 2) { "expected two columns: ${labels.map { it.left }}" }
    }

    private val track = MediaPlayerState(
        packageName = "com.example.music", title = "Song", artist = "Artist", art = null, isPlaying = true,
        positionMs = 0, durationMs = 180_000, positionUpdatedAtMs = 0, playbackSpeed = 1f, canSkipPrevious = true, canSkipNext = true,
    )

    @Test
    fun mediaRowIsPinnedToTheTopOfTheAppsPaneWhateverTheListAlignment() {
        // Given a playing track and the list alignment driven by state, so one composition covers both
        var alignment by mutableStateOf(AppListVerticalAlignment.BOTTOM)
        composeRule.setContent { LandscapeHome(mediaPlayer = track, appListVerticalAlignment = alignment) }
        composeRule.waitUntil(timeoutMillis = 3_000) {
            composeRule.onAllNodesWithTag("home_media_player").fetchSemanticsNodes().isNotEmpty()
        }
        composeRule.waitForIdle()

        listOf(AppListVerticalAlignment.BOTTOM, AppListVerticalAlignment.TOP).forEach { each ->
            alignment = each
            composeRule.waitForIdle()
            val pane = boundsOf("home_apps_pane")
            val media = boundsOf("home_media_player")
            val firstApp = boundsOf("home_app_label_com.example.app1")

            // Then the row is in the apps pane, at its top, and the list is below it
            assert(media.left >= pane.left && media.right <= pane.right) { "$each: media $media should be inside the apps pane $pane" }
            assert(kotlin.math.abs(media.top - pane.top) < 2f) { "$each: media $media should sit at the pane's top $pane" }
            assert(firstApp.top >= media.bottom) { "$each: the list ($firstApp) should start below the media row ($media)" }
        }
    }

    @Test
    fun mediaRowIsNotInTheClockPane() {
        composeRule.setContent { LandscapeHome(mediaPlayer = track) }
        composeRule.waitUntil(timeoutMillis = 3_000) {
            composeRule.onAllNodesWithTag("home_media_player").fetchSemanticsNodes().isNotEmpty()
        }
        composeRule.waitForIdle()

        val clockPane = boundsOf("home_clock_pane")
        val media = boundsOf("home_media_player")

        assert(media.left >= clockPane.right) { "media $media should be right of the clock pane $clockPane" }
    }

    @Test
    fun singleColumnListStaysOneColumnInLandscape() {
        composeRule.setContent { LandscapeHome(appListItems = apps(3), appListLayout = AppListLayout.SINGLE_COLUMN) }
        composeRule.waitForIdle()

        val labels = (1..3).map { boundsOf("home_app_label_com.example.app$it") }

        assert(labels.map { it.left.toInt() }.distinct().size == 1) { "expected one column: ${labels.map { it.left }}" }
        assert(labels.map { it.top.toInt() }.distinct().size == 3) { "expected three rows: ${labels.map { it.top }}" }
    }

    @Test
    fun adjustModeShowsTheDividerTheVerticalPillAndTheScaleHandles() {
        composeRule.setContent { LandscapeHome() }

        // When the clock menu's adjust row is chosen
        enterAdjustMode()

        // Then landscape's own controls appear: the divider, a top / middle / bottom pill, and scale handles
        composeRule.onNodeWithTag("home_clock_zone_handle").assertIsDisplayed()
        composeRule.onNodeWithTag("clock_adjust_toolbar").assertIsDisplayed()
        composeRule.onNodeWithTag("clock_align_top").assertExists()
        composeRule.onNodeWithTag("clock_align_middle").assertExists()
        composeRule.onNodeWithTag("clock_align_bottom").assertExists()
        composeRule.onNodeWithTag("clock_align_left").assertDoesNotExist()
        composeRule.onNodeWithTag("clock_resize_handle_left").assertExists()
        composeRule.onNodeWithTag("clock_resize_handle_right").assertExists()
    }

    @Test
    fun pillButtonsReportLeftCenterAndRightAsTopMiddleAndBottom() {
        val picked = mutableListOf<ClockAlignment>()
        composeRule.setContent { LandscapeHome(onClockAlignmentChange = { picked += it }) }
        enterAdjustMode()
        // The pill stays dimmed and disabled for a moment after adjust mode starts
        composeRule.waitUntil(timeoutMillis = 3_000) {
            runCatching { composeRule.onNodeWithTag("clock_align_middle").assertIsEnabled() }.isSuccess
        }

        composeRule.onNodeWithTag("clock_align_top").performClick()
        composeRule.onNodeWithTag("clock_align_middle").performClick()
        composeRule.onNodeWithTag("clock_align_bottom").performClick()

        assert(picked == listOf(ClockAlignment.LEFT, ClockAlignment.CENTER, ClockAlignment.RIGHT)) { "got $picked" }
    }

    @Test
    fun draggingTheDividerResizesTheClockPaneAndSavesTheWidth() {
        // Given a saved width that is driven by state, as the real route does
        var saved by mutableStateOf<Float?>(null)
        composeRule.setContent { LandscapeHome(clockZoneHeightDp = saved, onClockZoneHeightCommit = { saved = it }) }
        enterAdjustMode()
        val before = boundsOf("home_clock_pane").width

        // When the divider is dragged right
        dragDividerBy(120f)

        // Then the pane is wider and the width was saved
        val after = boundsOf("home_clock_pane").width
        assert(after > before) { "pane should widen: $before -> $after" }
        assert(saved != null) { "the width should be committed" }
    }

    @Test
    fun draggingTheDividerLeftShrinksTheClockDownToItsSmallestScaleAndStopsThere() {
        // Given the default 0.8 clock scale
        var saved by mutableStateOf<Float?>(null)
        var scaleCommitted: Float? = null
        composeRule.setContent {
            LandscapeHome(clockZoneHeightDp = saved, onClockZoneHeightCommit = { saved = it }, onClockScaleCommit = { scaleCommitted = it })
        }
        enterAdjustMode()
        val paneBefore = boundsOf("home_clock_pane").width
        val clockBefore = boundsOf("home_clock_block").width

        // When the divider is pulled far to the left
        dragDividerBy(-3000f)

        // Then the pane narrowed and the clock shrank with it, but only to 0.5 / 0.8 of its size
        val paneAfter = boundsOf("home_clock_pane").width
        val clockAfter = boundsOf("home_clock_block")
        assert(paneAfter < paneBefore) { "pane should narrow: $paneBefore -> $paneAfter" }
        assert(clockAfter.width < clockBefore) { "clock should shrink: $clockBefore -> ${clockAfter.width}" }
        assert(clockAfter.width >= clockBefore * 0.5f / 0.8f - 2f) { "clock should not go below its smallest scale: $clockBefore -> ${clockAfter.width}" }
        assert(clockAfter.right <= boundsOf("home_clock_pane").right) { "clock $clockAfter should still fit its pane" }
        // And, as in portrait, the shrunk scale was saved on release
        assert(scaleCommitted != null && scaleCommitted!! < 0.8f) { "expected the shrunk scale to be saved, got $scaleCommitted" }
    }

    @Test
    fun theDividerFollowsTheFingerStraightAwayAfterHittingTheLimit() {
        var saved by mutableStateOf<Float?>(null)
        composeRule.setContent { LandscapeHome(clockZoneHeightDp = saved, onClockZoneHeightCommit = { saved = it }) }
        enterAdjustMode()

        // When the divider is pulled well past its limit and then back right by a little
        composeRule.onNodeWithTag("home_clock_zone_handle").performTouchInput { down(center) }
        composeRule.waitForIdle()
        repeat(4) {
            composeRule.onNodeWithTag("home_clock_zone_handle").performTouchInput { moveBy(Offset(-400f, 0f)) }
            composeRule.waitForIdle()
        }
        val atLimit = boundsOf("home_clock_pane").width
        composeRule.onNodeWithTag("home_clock_zone_handle").performTouchInput { moveBy(Offset(60f, 0f)) }
        composeRule.waitForIdle()
        val afterReverse = boundsOf("home_clock_pane").width
        composeRule.onNodeWithTag("home_clock_zone_handle").performTouchInput { up() }

        // Then the pane widened right away instead of the finger sitting in a dead zone
        assert(afterReverse > atLimit + 20f) { "pane should widen at once: $atLimit -> $afterReverse" }
    }

    @Test
    fun theSavedWidthSetsThePaneWidth() {
        // Given a saved width driven by state, so one composition can find the narrowest and widest pane
        var saved by mutableStateOf<Float?>(1f)
        composeRule.setContent { LandscapeHome(clockZoneHeightDp = saved) }
        composeRule.waitForIdle()
        val narrowest = with(composeRule.density) { boundsOf("home_clock_pane").width.toDp().value }
        saved = 5000f
        composeRule.waitForIdle()
        val widest = with(composeRule.density) { boundsOf("home_clock_pane").width.toDp().value }

        // When a width halfway between them is saved
        val middle = (narrowest + widest) / 2f
        saved = middle
        composeRule.waitForIdle()
        val actual = with(composeRule.density) { boundsOf("home_clock_pane").width.toDp().value }

        // Then the pane is that wide
        assert(widest > narrowest) { "expected room between $narrowest and $widest" }
        assert(kotlin.math.abs(actual - middle) < 2f) { "pane should be about ${middle}dp wide, was $actual (narrowest $narrowest, widest $widest)" }
    }

    @Test
    fun aSavedWidthTooNarrowForTheClockIsRaisedSoTheClockStillFits() {
        // Given a saved width of 1dp, far narrower than any clock
        composeRule.setContent { LandscapeHome(clockZoneHeightDp = 1f) }
        enterAdjustMode()

        val pane = boundsOf("home_clock_pane")
        val clock = boundsOf("home_clock_block")

        // Then the pane is wide enough for the shrunk clock and its margins, and the clock is inside it
        assert(pane.width > clock.width) { "pane $pane should be wider than the clock $clock" }
        assert(clock.left >= pane.left && clock.right <= pane.right) { "clock $clock should be inside the pane $pane" }
    }

    @Test
    fun paneWidthIsCappedAtHalfTheWindow() {
        composeRule.setContent { LandscapeHome(clockZoneHeightDp = 5000f) }
        composeRule.waitForIdle()

        val pane = boundsOf("home_clock_pane").width
        val root = boundsOf("home_landscape_layout").width

        assert(pane <= root * 0.5f + 2f) { "pane $pane should be at most half of $root" }
    }

    @Test
    fun draggingAScaleHandleSavesANewScale() {
        var committed: Float? = null
        composeRule.setContent { LandscapeHome(onClockScaleCommit = { committed = it }) }
        enterAdjustMode()

        composeRule.onNodeWithTag("clock_resize_handle_right").performTouchInput {
            down(center)
            moveBy(Offset(30f, 30f))
            up()
        }
        composeRule.waitForIdle()

        assert(committed != null) { "a scale should be committed after dragging a handle" }
    }

    @Test
    fun theLongPressMenuOffersAdjustInLandscape() {
        composeRule.setContent { LandscapeHome() }

        composeRule.onNodeWithTag("home_clock_block").performTouchInput { longClick() }
        composeRule.waitForIdle()

        composeRule.onNodeWithTag("clock_adjust_sheet").assertExists()
        composeRule.onNodeWithTag("clock_adjust_open").assertExists()
    }

    // --- A hosted (custom) clock widget in landscape ---

    private fun widgetBounds() = composeRule.onNodeWithTag("clock_widget_host_$WIDGET_ID").getUnclippedBoundsInRoot()

    @Test
    fun aCustomClockWidgetStillOffersAdjustInLandscape() {
        composeRule.setContent { LandscapeHome(widget = TestClockWidget(widthDp = 180, heightDp = 90)) }
        composeRule.onNodeWithTag("clock_widget_host_$WIDGET_ID").assertExists()

        composeRule.onNodeWithTag("home_screen_root").performTouchInput { longClick(Offset(width * 0.75f, 4f)) }
        composeRule.waitForIdle()

        composeRule.onNodeWithTag("clock_adjust_open").assertExists()
    }

    @Test
    fun adjustModeForACustomWidgetShowsTheDividerThePillAndCornerHandles() {
        composeRule.setContent { LandscapeHome(widget = TestClockWidget(widthDp = 180, heightDp = 90)) }

        enterAdjustModeFromEmptySpace()

        composeRule.onNodeWithTag("home_clock_zone_handle").assertIsDisplayed()
        composeRule.onNodeWithTag("clock_adjust_toolbar").assertIsDisplayed()
        composeRule.onNodeWithTag("clock_align_top").assertExists()
        composeRule.onNodeWithTag("clock_resize_handle_left").assertExists()
        composeRule.onNodeWithTag("clock_resize_handle_right").assertExists()
    }

    @Test
    fun draggingACustomWidgetCornerHandleSavesTheNewSize() {
        var committed: Pair<Int, Int>? = null
        composeRule.setContent {
            LandscapeHome(widget = TestClockWidget(widthDp = 180, heightDp = 90, onSizeCommit = { w, h -> committed = w to h }))
        }
        enterAdjustModeFromEmptySpace()

        // When the bottom right handle (the widget is top aligned) is pulled out and down
        // (in small steps, like a finger: the first move only records where the handle was grabbed)
        composeRule.onNodeWithTag("clock_resize_handle_right").performTouchInput {
            down(center)
            repeat(8) { moveBy(Offset(10f, 7f)) }
            up()
        }
        composeRule.waitForIdle()

        // Then a larger size is saved
        val size = committed
        assert(size != null && size.first > 180 && size.second > 90) { "expected a size larger than 180 x 90dp, got $size" }
    }

    @Test
    fun aCustomWidgetWiderThanItsPaneIsShownInsideThePane() {
        composeRule.setContent { LandscapeHome(widget = TestClockWidget(widthDp = 900, heightDp = 90)) }
        composeRule.waitForIdle()

        val pane = composeRule.onNodeWithTag("home_clock_pane").getUnclippedBoundsInRoot()
        assert(widgetBounds().right <= pane.right) { "widget ${widgetBounds()} should stay inside its pane $pane" }
    }

    @Test
    fun pullingTheDividerLeftShrinksACustomWidgetOnlyToItsMinimumAndSavesIt() {
        var committed: Pair<Int, Int>? = null
        composeRule.setContent {
            LandscapeHome(
                widget = TestClockWidget(widthDp = 200, heightDp = 90, minWidthDp = 120, minHeightDp = 60, onSizeCommit = { w, h -> committed = w to h }),
            )
        }
        enterAdjustModeFromEmptySpace()

        // When the divider is pulled far to the left
        composeRule.onNodeWithTag("home_clock_zone_handle").performTouchInput {
            down(center)
            repeat(10) { moveBy(Offset(-300f, 0f)) }
            up()
        }
        composeRule.waitForIdle()

        // Then the widget shrank to its minimum width and no further, and that size was saved
        val width = widgetBounds().width.value
        assert(width < 200f && width >= 119f) { "widget should be about 120dp wide, was $width" }
        assert(committed != null && committed!!.first in 119..121) { "expected the shrunk size to be saved, got $committed" }
    }

    private companion object {
        const val WIDGET_ID = 42
    }
}
