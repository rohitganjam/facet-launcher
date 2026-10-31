package com.facetlauncher.app.ui.home

import android.appwidget.AppWidgetHostView
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextReplacement
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.longClick
import androidx.compose.ui.test.getUnclippedBoundsInRoot
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.width
import com.facetlauncher.app.data.model.ClockAlignment
import com.facetlauncher.app.data.model.AppInfo
import com.facetlauncher.app.data.model.Folder
import com.facetlauncher.app.data.model.MediaPlayerState
import com.facetlauncher.app.data.model.NotificationBadgeStyle
import com.facetlauncher.app.data.model.PlacedItem
import com.facetlauncher.app.domain.QuickAddState
import com.facetlauncher.app.domain.QuickPlacementAction
import com.facetlauncher.app.ui.theme.FacetLauncherTheme
import org.junit.Assert.assertTrue
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Rule
import org.junit.Test

/**
 * `HomeScreen` takes its state and callbacks as groups. These tests set each group's fields to distinct values
 * and check they reach the part of Home they are for, so a field wired to the wrong slot (a favorites callback
 * where a dock one belongs, a folder callback where an app one does) fails here. Behaviour inside each part is
 * covered by that part's own tests.
 */
class HomeScreenWiringTest {

    @get:Rule
    val composeRule = createComposeRule()

    private val mail = AppInfo(packageName = "com.example.mail", activityName = ".Main", label = "Mail", icon = null)
    private val games = Folder(id = 7L, name = "Games", apps = listOf(mail))
    private val track = MediaPlayerState(
        packageName = "com.example.music", title = "I Dont Care", artist = "Apocalyptica", art = null,
        isPlaying = true, positionMs = 0L, durationMs = 200_000L, positionUpdatedAtMs = 0L, playbackSpeed = 1f,
        canSkipPrevious = true, canSkipNext = true,
    )
    private val addToBoth = QuickAddState(
        favoritesAction = QuickPlacementAction.Add(facetName = null),
        dockAction = QuickPlacementAction.Add(facetName = null),
    )

    private fun appState(vararg items: PlacedItem) = HomeScreenState(
        list = HomeListState(items = items.toList()),
        dock = HomeDockState(items = emptyList()),
    )

    /** Hoists the adjust mode as `HomeDrawerRoute` does, so a long-press on the clock really opens its menu. */
    private fun setHome(state: HomeScreenState, actions: HomeScreenActions) {
        composeRule.setContent {
            var adjustMode by remember { mutableStateOf(ClockAdjustMode.NONE) }
            FacetLauncherTheme {
                HomeScreen(
                    state = state.copy(clock = state.clock.copy(adjustMode = adjustMode)),
                    actions = actions.copy(clock = actions.clock.copy(onAdjustModeChange = { adjustMode = it })),
                    layoutMode = HomeLayoutMode.PORTRAIT,
                )
            }
        }
        composeRule.waitForIdle()
    }

    private fun longPress(tag: String) {
        composeRule.onNodeWithTag(tag).performTouchInput { longClick() }
        composeRule.waitForIdle()
    }

    // ---- app tiles

    @Test
    fun appInfoFromAnAppsMenuReachesTheItemActions() {
        // Given an app list whose item actions record the app they were asked about
        var infoFor: AppInfo? = null
        setHome(appState(PlacedItem.SingleApp(mail)), HomeScreenActions(item = HomeItemActions(onAppClick = {}, onAppInfo = { infoFor = it })))

        // When its menu's App info button is tapped
        longPress("home_app_label_com.example.mail")
        composeRule.onNodeWithTag("app_context_menu_app_info").performClick()

        // Then onAppInfo got that app
        assertEquals(mail, infoFor)
    }

    @Test
    fun anAppsFavoritesAndDockActionsReachTheirOwnCallbacks() {
        // Given item actions that record which callback fired, for an app and for a folder
        val calls = mutableListOf<String>()
        val actions = HomeItemActions(
            onAppClick = {},
            onRequestQuickAddState = { addToBoth },
            onFavoritesAction = { _, _ -> calls += "app-favorites" },
            onDockAction = { _, _ -> calls += "app-dock" },
            onFolderFavoritesAction = { _, _ -> calls += "folder-favorites" },
            onFolderDockAction = { _, _ -> calls += "folder-dock" },
        )
        setHome(appState(PlacedItem.SingleApp(mail)), HomeScreenActions(item = actions))

        // When the app's menu asks for Favorites, then (reopened) for Dock
        longPress("home_app_label_com.example.mail")
        composeRule.onNodeWithTag("app_context_menu_add_to_favorites").performClick()
        longPress("home_app_label_com.example.mail")
        composeRule.onNodeWithTag("app_context_menu_add_to_dock").performClick()

        // Then each went to the app callback of its own kind, and no folder callback fired
        assertEquals(listOf("app-favorites", "app-dock"), calls)
    }

    @Test
    fun aFoldersMenuUsesTheFolderCallbacksNotTheAppOnes() {
        // Given the same recording actions with a folder in the list
        val calls = mutableListOf<String>()
        var renamed: Pair<Long, String>? = null
        val actions = HomeItemActions(
            onAppClick = {},
            onRequestFolderQuickAddState = { addToBoth },
            onFavoritesAction = { _, _ -> calls += "app-favorites" },
            onDockAction = { _, _ -> calls += "app-dock" },
            onFolderFavoritesAction = { _, _ -> calls += "folder-favorites" },
            onFolderDockAction = { _, _ -> calls += "folder-dock" },
            onRenameFolder = { id, name -> renamed = id to name },
        )
        setHome(appState(PlacedItem.FolderItem(games)), HomeScreenActions(item = actions))

        // When the folder's menu is used for Favorites, Dock and Rename
        longPress("home_folder_row_7")
        composeRule.onNodeWithTag("folder_tile_context_menu_add_to_favorites").performClick()
        longPress("home_folder_row_7")
        composeRule.onNodeWithTag("folder_tile_context_menu_add_to_dock").performClick()
        longPress("home_folder_row_7")
        composeRule.onNodeWithTag("folder_tile_context_menu_rename").performClick()
        composeRule.onNodeWithTag("rename_dialog_field").performTextReplacement("Arcade")
        composeRule.onNodeWithTag("rename_dialog_save").performClick()

        // Then only the folder callbacks fired, in their own slots
        assertEquals(listOf("folder-favorites", "folder-dock"), calls)
        assertEquals(7L to "Arcade", renamed)
    }

    @Test
    fun theBadgeStyleDecidesWhetherACountShows() {
        // Given an app with 3 unread, drawn once as a count and once as a dot
        fun withStyle(style: NotificationBadgeStyle) = HomeScreenState(
            list = HomeListState(
                items = listOf(PlacedItem.SingleApp(mail)),
                look = HomeItemLook(badgeCounts = mapOf(mail.packageName to 3), badgeStyle = style),
            ),
            dock = HomeDockState(items = emptyList()),
        )
        setHome(withStyle(NotificationBadgeStyle.COUNT), HomeScreenActions(item = HomeItemActions(onAppClick = {})))

        // Then the count is drawn (and with the dot style, in the next test, it is not)
        composeRule.onNodeWithText("3").assertExists()
    }

    @Test
    fun aDotBadgeDrawsNoCount() {
        val state = HomeScreenState(
            list = HomeListState(
                items = listOf(PlacedItem.SingleApp(mail)),
                look = HomeItemLook(badgeCounts = mapOf(mail.packageName to 3), badgeStyle = NotificationBadgeStyle.DOT),
            ),
            dock = HomeDockState(items = emptyList()),
        )
        setHome(state, HomeScreenActions(item = HomeItemActions(onAppClick = {})))

        composeRule.onAllNodesWithText("3").assertCountEquals(0)
    }

    // ---- media

    @Test
    fun theMediaButtonsReachTheirOwnCallbacks() {
        // Given a track and a counter per callback
        val calls = mutableListOf<String>()
        val state = appState().copy(media = HomeMediaState(player = track))
        val actions = HomeScreenActions(
            item = HomeItemActions(onAppClick = {}),
            media = HomeMediaActions(
                onPrevious = { calls += "previous" },
                onPlayPause = { calls += "play-pause" },
                onNext = { calls += "next" },
                onOpen = { calls += "open" },
            ),
        )
        setHome(state, actions)

        // When each control is tapped
        composeRule.onNodeWithTag("home_media_previous").performClick()
        composeRule.onNodeWithTag("home_media_play_pause").performClick()
        composeRule.onNodeWithTag("home_media_next").performClick()
        composeRule.onNodeWithTag("home_media_art").performClick()

        // Then each one fired its own callback
        assertEquals(listOf("previous", "play-pause", "next", "open"), calls)
    }

    @Test
    fun hidingTheSkipButtonsRemovesPreviousAndNext() {
        val state = appState().copy(media = HomeMediaState(player = track, showSkipButtons = false))
        setHome(state, HomeScreenActions(item = HomeItemActions(onAppClick = {})))

        composeRule.onNodeWithTag("home_media_play_pause").assertExists()
        composeRule.onNodeWithTag("home_media_previous").assertDoesNotExist()
        composeRule.onNodeWithTag("home_media_next").assertDoesNotExist()
    }

    // ---- clock

    @Test
    fun theClockMenuRowsReachTheClockActions() {
        // Given a native clock and counters on the menu's actions
        val calls = mutableListOf<String>()
        val actions = HomeScreenActions(
            item = HomeItemActions(onAppClick = {}),
            clock = HomeClockActions(
                onClockSettingsClick = { calls += "clock-settings" },
                onReplaceWithWidgetClick = { calls += "replace-with-widget" },
                onReplaceWithClockClick = { calls += "replace-with-clock" },
                onNavigateToSettings = { calls += "settings" },
                onNavigateToFacetSettings = { calls += "facet-settings" },
            ),
        )
        setHome(appState(PlacedItem.SingleApp(mail)), actions)

        // When the clock is long-pressed and each row tapped (a tap closes the sheet, so it is reopened each time)
        longPress("home_clock_block")
        composeRule.onNodeWithTag("clock_adjust_replace_with_clock").assertDoesNotExist()
        composeRule.onNodeWithTag("clock_adjust_clock_settings").performClick()
        longPress("home_clock_block")
        composeRule.onNodeWithTag("clock_adjust_replace_with_widget").performClick()
        longPress("home_clock_block")
        composeRule.onNodeWithTag("clock_adjust_facet_settings").performClick()
        longPress("home_clock_block")
        composeRule.onNodeWithTag("clock_adjust_launcher_settings").performClick()

        // Then each reached its own callback, and a native clock page has no "replace with clock" row
        assertEquals(listOf("clock-settings", "replace-with-widget", "facet-settings", "settings"), calls)
    }

    @Test
    fun aHostedWidgetsMenuOffersReplacingItWithTheClockAndTheWidgetIsHosted() {
        // Given a hosted clock widget
        var switched = 0
        var hostedId: Int? = null
        var listBounds: Rect? = null
        var widgetBounds: Rect? = null
        val state = appState(PlacedItem.SingleApp(mail)).copy(
            widget = HomeClockWidgetState(itemId = 1L, appWidgetId = 42, widthDp = 160.dp, heightDp = 90.dp, minWidthDp = 64.dp, minHeightDp = 64.dp),
            clockStack = HomeClockStackState(pageCount = 1, pageIndex = 0, hasClock = false),
        )
        val actions = HomeScreenActions(
            item = HomeItemActions(onAppClick = {}),
            clock = HomeClockActions(onReplaceWithClockClick = { switched++ }),
            widget = HomeClockWidgetActions(
                createHostView = { context, id ->
                    hostedId = id
                    AppWidgetHostView(context)
                },
                onBoundsChange = { widgetBounds = it },
            ),
            onAppListBoundsChange = { listBounds = it },
        )
        setHome(state, actions)

        // Then the widget was hosted by its id, and Home reported where the widget and the list are
        assertEquals(42, hostedId)
        assertNotNull(widgetBounds)
        assertNotNull(listBounds)

        // When the menu's "Replace with clock" row is tapped
        // The widget host swallows its own touches, so the menu is opened from empty space as in the landscape tests.
        composeRule.onNodeWithTag("home_screen_root").performTouchInput { longClick(Offset(width * 0.75f, 4f)) }
        composeRule.waitForIdle()
        composeRule.onNodeWithTag("clock_adjust_replace_with_clock").performClick()

        // Then that action fired
        assertEquals(1, switched)
    }

    @Test
    fun aWidgetKeepsItsWidthWhileThePageIsSwipedAside() {
        // Given a hosted widget page
        var offset by mutableStateOf(0f)
        val state = appState(PlacedItem.SingleApp(mail)).copy(
            widget = HomeClockWidgetState(itemId = 1L, appWidgetId = 42, widthDp = 300.dp, heightDp = 90.dp, minWidthDp = 64.dp, minHeightDp = 64.dp),
            clockStack = HomeClockStackState(pageCount = 2, pageIndex = 1, hasClock = true),
        )
        val actions = HomeScreenActions(
            item = HomeItemActions(onAppClick = {}),
            clock = HomeClockActions(pageOffset = { offset }),
            widget = HomeClockWidgetActions(createHostView = { context, _ -> AppWidgetHostView(context) }),
        )
        setHome(state, actions)
        val restingWidth = composeRule.onNodeWithTag("clock_widget_host_42").getUnclippedBoundsInRoot().width

        // When the page is dragged part-way aside
        composeRule.runOnIdle { offset = 0.3f }
        composeRule.waitForIdle()

        // Then the widget is moved, not squeezed into the room that is left
        assertEquals(restingWidth.value, composeRule.onNodeWithTag("clock_widget_host_42").getUnclippedBoundsInRoot().width.value, 0.5f)
    }

    @Test
    fun theNextPageSlidesInBesideTheVisibleOneWhileSwiping() {
        // Given the clock page visible, a widget page after it, and a swipe part-way
        var offset by mutableStateOf(0f)
        val state = appState(PlacedItem.SingleApp(mail)).copy(
            clockStack = HomeClockStackState(
                pageCount = 2, pageIndex = 0, hasClock = true, visibleItemId = 1L,
                next = HomeClockNeighbor(
                    itemId = 2L,
                    widget = HomeClockWidgetState(itemId = 2L, appWidgetId = 42, widthDp = 200.dp, heightDp = 90.dp, minWidthDp = 64.dp, minHeightDp = 64.dp),
                    alignment = ClockAlignment.LEFT,
                ),
            ),
        )
        val actions = HomeScreenActions(
            item = HomeItemActions(onAppClick = {}),
            clock = HomeClockActions(pageOffset = { offset }),
            widget = HomeClockWidgetActions(createHostView = { context, _ -> AppWidgetHostView(context) }),
        )
        setHome(state, actions)

        // Then at rest only the clock is drawn
        composeRule.onNodeWithTag("clock_widget_host_42").assertDoesNotExist()

        // When the clock is dragged towards the next page
        composeRule.runOnIdle { offset = -0.3f }
        composeRule.waitForIdle()

        // Then the clock is still there and the widget is coming in beside it, to the right of where it rests
        composeRule.onNodeWithTag("home_clock_block", useUnmergedTree = true).assertExists()
        composeRule.onNodeWithTag("clock_widget_host_42").assertExists()
        val widgetLeft = composeRule.onNodeWithTag("clock_widget_host_42").getUnclippedBoundsInRoot().left
        val rootWidth = composeRule.onRoot().getUnclippedBoundsInRoot().width
        assertTrue("widget left $widgetLeft in $rootWidth", widgetLeft > rootWidth * 0.5f)

        // And back at rest it is gone again
        composeRule.runOnIdle { offset = 0f }
        composeRule.waitForIdle()
        composeRule.onNodeWithTag("clock_widget_host_42").assertDoesNotExist()
    }

    @Test
    fun anOrphanedWidgetShowsAPlaceholderWhoseRemoveFires() {
        // Given a widget page whose app was uninstalled
        var removed = 0
        var hosted = false
        val state = appState(PlacedItem.SingleApp(mail)).copy(
            widget = HomeClockWidgetState(
                itemId = 1L, appWidgetId = 42, widthDp = 160.dp, heightDp = 110.dp, minWidthDp = null, minHeightDp = null, isOrphaned = true,
            ),
            clockStack = HomeClockStackState(pageCount = 1, pageIndex = 0, hasClock = false),
        )
        val actions = HomeScreenActions(
            item = HomeItemActions(onAppClick = {}),
            widget = HomeClockWidgetActions(createHostView = { _, _ -> hosted = true; null }, onRemoveOrphan = { removed++ }),
        )
        setHome(state, actions)

        // Then a placeholder is drawn in place of the widget, with no Keep space action
        composeRule.onNodeWithTag("hub_orphaned_widget").assertExists()
        composeRule.onNodeWithTag("hub_orphaned_keep_space").assertDoesNotExist()
        assertEquals(false, hosted)

        // When Remove is tapped
        composeRule.onNodeWithTag("hub_orphaned_remove").performClick()

        // Then the removal fired
        assertEquals(1, removed)
    }

    @Test
    fun theFacetThatOwnsTheClockStyleIsNamedOnTheClockSettingsRow() {
        // Given a clock style owned by a facet called Work
        val state = appState(PlacedItem.SingleApp(mail)).copy(clock = HomeClockState(placementFacetId = 3L, styleOwnerFacetName = "Work"))
        setHome(state, HomeScreenActions(item = HomeItemActions(onAppClick = {})))

        // When the clock menu opens
        longPress("home_clock_block")

        // Then the edit-styles row carries that name
        composeRule.onNodeWithText("Work", substring = true).assertExists()
    }

    @Test
    fun theBatteryReadingReachesTheClockAccessories() {
        // Given a battery reading
        val state = appState().copy(calendar = HomeCalendarState(batteryPercent = 64))
        setHome(state, HomeScreenActions(item = HomeItemActions(onAppClick = {})))

        // Then the clock shows it
        composeRule.onNodeWithText("64%").assertExists()
    }
}
