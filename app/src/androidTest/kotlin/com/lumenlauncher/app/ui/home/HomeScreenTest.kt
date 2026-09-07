package com.lumenlauncher.app.ui.home

import android.content.res.Configuration
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsNotDisplayed
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.longClick
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.swipeUp
import com.lumenlauncher.app.data.model.AppInfo
import com.lumenlauncher.app.data.model.AppListVerticalAlignment
import com.lumenlauncher.app.data.model.AppRowPosition
import com.lumenlauncher.app.data.model.AppRowPresentation
import com.lumenlauncher.app.data.model.CalendarEvent
import com.lumenlauncher.app.data.model.ClockAlignment
import com.lumenlauncher.app.data.model.DockDisplayMode
import com.lumenlauncher.app.data.model.ListContentMode
import com.lumenlauncher.app.ui.theme.LumenLauncherTheme
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class HomeScreenTest {

    @get:Rule
    val composeRule = createComposeRule()

    private fun apps(count: Int) = (1..count).map {
        AppInfo(packageName = "com.example.app$it", activityName = ".Main", label = "App $it", icon = null)
    }

    /** Long-presses the clock to reveal its (otherwise hidden) grab handle. */
    private fun enableClockDragMode() {
        composeRule.onNodeWithTag("home_clock_block").performTouchInput { longClick() }
        composeRule.waitForIdle()
    }

    /**
     * Drags the (already-revealed) zone handle down by [totalDeltaY] px, as separate
     * `performTouchInput` calls with a `waitForIdle()` between each — unlike a single combined
     * gesture block, this gives Compose's snapshot system a real chance to recompose between
     * steps. Injecting the whole drag (down + every move + up) inside one `performTouchInput`
     * block fires all the underlying state writes in one synchronous burst with no frame boundary
     * in between, so HomeScreen's own recomposition never observes the intermediate position —
     * only the net result once the gesture finishes, which release's own onDragEnd reset can then
     * cancel out entirely. A real finger drag doesn't have this problem (each move arrives on its
     * own frame), so this only matters for synthetic test input.
     */
    private fun dragHandleBy(totalDeltaY: Float, steps: Int = 6) {
        composeRule.onNodeWithTag("home_clock_zone_handle").performTouchInput { down(center) }
        composeRule.waitForIdle()
        repeat(steps) {
            composeRule.onNodeWithTag("home_clock_zone_handle").performTouchInput { moveBy(Offset(0f, totalDeltaY / steps)) }
            composeRule.waitForIdle()
        }
        composeRule.onNodeWithTag("home_clock_zone_handle").performTouchInput { up() }
        composeRule.waitForIdle()
    }

    @Test
    fun showsFirstFiveAppsAsFavoritesAndDockAppsSeparately() {
        // Given 9 apps, the first 5 as favorites and the rest as the (independently-sourced) dock
        val all = apps(9)
        composeRule.setContent {
            LumenLauncherTheme {
                HomeScreen(appListItems = all.take(5), dockApps = all.drop(5), onAppClick = {})
            }
        }

        // Then App 1..5 render as favorites rows (visible label text)
        for (i in 1..5) {
            composeRule.onNodeWithText("App $i").assertExists()
        }
        // And App 6..9 render as dock icons (content description, no visible label text)
        for (i in 6..9) {
            composeRule.onNodeWithContentDescription("App $i").assertExists()
        }
    }

    @Test
    fun tappingAFavoriteRowReportsThatApp() {
        // Given a favorite row for "App 2"
        var clicked: AppInfo? = null
        composeRule.setContent {
            LumenLauncherTheme {
                HomeScreen(appListItems = apps(5), dockApps = emptyList(), onAppClick = { clicked = it })
            }
        }

        // When it is tapped
        composeRule.onNodeWithText("App 2").performClick()

        // Then the callback receives that exact AppInfo
        assertEquals("com.example.app2", clicked?.packageName)
    }

    @Test
    fun tappingTheClockFiresOnClockClick() {
        // Given a HomeScreen with no calendar events
        var clicked = false
        composeRule.setContent {
            LumenLauncherTheme {
                HomeScreen(appListItems = apps(1), dockApps = emptyList(), onAppClick = {}, onClockClick = { clicked = true })
            }
        }

        // When the clock is tapped — a raw touch, not performClick(), since this is a plain
        // pointerInput tap/long-press detector, not Modifier.clickable (see HomeScreen.kt's own
        // comment on why the tap-to-open and long-press-to-drag gestures share one detector).
        composeRule.onNodeWithTag("home_clock_block").performTouchInput {
            down(center)
            up()
        }

        // Then onClockClick fires
        assertEquals(true, clicked)
    }

    @Test
    fun tappingACalendarEventDoesNotFireOnClockClick() {
        // Given a HomeScreen with one calendar event, both callbacks wired
        val event = CalendarEvent(
            id = 1,
            calendarId = "1",
            title = "Standup",
            startTimeMillis = System.currentTimeMillis() + 3_600_000,
            endTimeMillis = System.currentTimeMillis() + 5_400_000,
            isAllDay = false,
        )
        var eventClicked = false
        var clockClicked = false
        composeRule.setContent {
            LumenLauncherTheme {
                HomeScreen(
                    appListItems = apps(1),
                    dockApps = emptyList(),
                    onAppClick = {},
                    calendarEvents = listOf(event),
                    onEventClick = { eventClicked = true },
                    onClockClick = { clockClicked = true },
                )
            }
        }

        // When the event row is tapped (not the time/date)
        composeRule.onNodeWithTag("clock_event_row_1").performClick()

        // Then only the event's own callback fires
        assertEquals(true, eventClicked)
        assertEquals(false, clockClicked)
    }

    @Test
    fun emptyDockIsOmittedEntirelyRatherThanShownAsAnEmptyRow() {
        // Given no dock apps (the dock floor was removed — 0 is now a valid dock size)
        composeRule.setContent {
            LumenLauncherTheme {
                HomeScreen(appListItems = apps(3), dockApps = emptyList(), onAppClick = {})
            }
        }

        // Then the dock row isn't rendered at all
        composeRule.onNodeWithTag("home_dock_row").assertDoesNotExist()
    }

    @Test
    fun dockTextModeRendersLabelsInsteadOfIcons() {
        // Given a dock in Text display mode
        val dockApps = apps(4)
        composeRule.setContent {
            LumenLauncherTheme {
                HomeScreen(
                    appListItems = emptyList(),
                    dockApps = dockApps,
                    dockDisplayMode = DockDisplayMode.TEXT,
                    onAppClick = {},
                )
            }
        }

        // Then dock apps render as visible text labels, not just icon content descriptions
        dockApps.forEach { app -> composeRule.onNodeWithText(app.label).assertExists() }
    }

    @Test
    fun dockTextModeStillShowsANotificationBadgeTrailingTheLabel() {
        // Given a dock in Text display mode where one app has a pending notification
        val dockApps = apps(2)
        composeRule.setContent {
            LumenLauncherTheme {
                HomeScreen(
                    appListItems = emptyList(),
                    dockApps = dockApps,
                    dockDisplayMode = DockDisplayMode.TEXT,
                    badgeCounts = mapOf("com.example.app1" to 3),
                    onAppClick = {},
                )
            }
        }

        // Then the badged app's label still renders, alongside a real badge — Text mode has no
        // icon corner to anchor a badge to (see DockIcon), so it must not simply drop the count.
        // useUnmergedTree = true — the badge lives inside the dock's own clickable Row, which
        // merges descendant semantics (same reasoning as the icon/label tag lookups above).
        composeRule.onNodeWithText("App 1").assertExists()
        composeRule.onNodeWithTag("app_icon_badge", useUnmergedTree = true).assertExists()
    }

    @Test
    fun sectionLabelReflectsTheActiveListContentMode() {
        // The heading only renders alongside a real list (see HomeScreen.kt's own
        // `if (appListItems.isNotEmpty())` gate) — an empty list here would never show any
        // heading text regardless of listContentMode, which isn't what this test means to cover.
        composeRule.setContent {
            LumenLauncherTheme {
                HomeScreen(appListItems = apps(1), dockApps = emptyList(), onAppClick = {}, listContentMode = ListContentMode.RECENTS)
            }
        }

        composeRule.onNodeWithText("RECENTS").assertExists()
        composeRule.onNodeWithText("FAVORITES").assertDoesNotExist()
    }

    @Test
    fun usageAccessPromptReplacesTheListAndFiresItsCallbackOnTap() {
        // Given Recents mode with usage access not yet granted
        var clicked = false
        composeRule.setContent {
            LumenLauncherTheme {
                HomeScreen(
                    appListItems = apps(3),
                    dockApps = emptyList(),
                    onAppClick = {},
                    listContentMode = ListContentMode.RECENTS,
                    showUsageAccessPrompt = true,
                    onUsageAccessPromptClick = { clicked = true },
                )
            }
        }

        // Then the strip renders instead of any app rows
        composeRule.onNodeWithTag("home_usage_access_strip").assertExists()
        composeRule.onNodeWithText("App 1").assertDoesNotExist()

        // And tapping it fires the callback
        composeRule.onNodeWithTag("home_usage_access_strip").performClick()
        assertEquals(true, clicked)
    }

    @Test
    fun longPressingAFavoriteRowOpensTheContextMenu() {
        // Given a favorite row
        composeRule.setContent {
            LumenLauncherTheme {
                HomeScreen(appListItems = apps(1), dockApps = emptyList(), onAppClick = {})
            }
        }
        composeRule.onNodeWithTag("app_context_menu").assertDoesNotExist()

        // When it's long-pressed (F12)
        composeRule.onNodeWithText("App 1").performTouchInput { longClick() }

        // Then the context menu opens
        composeRule.onNodeWithTag("app_context_menu").assertExists()
    }

    @Test
    fun leftPositionRendersTheIconBeforeTheLabel() {
        // Given the default Left position
        composeRule.setContent {
            LumenLauncherTheme {
                HomeScreen(appListItems = apps(1), dockApps = emptyList(), onAppClick = {}, appRowPosition = AppRowPosition.LEFT)
            }
        }

        // Then the icon renders before (further left than) the label — normal reading order.
        // `useUnmergedTree = true` because both live inside AppRow's clickable Row, which merges
        // descendant semantics into itself for accessibility (a single "App 1, button" unit) —
        // testTag deliberately isn't included in that merge, so the default merged-tree query
        // can't find either tag standalone (see chat history).
        val iconLeft = composeRule.onNodeWithTag("home_app_icon_com.example.app1", useUnmergedTree = true).fetchSemanticsNode().boundsInRoot.left
        val labelLeft = composeRule.onNodeWithTag("home_app_label_com.example.app1", useUnmergedTree = true).fetchSemanticsNode().boundsInRoot.left
        assert(iconLeft < labelLeft)
    }

    @Test
    fun rightPositionRendersTheLabelBeforeTheIconAndPacksTheRowAgainstTheRightEdge() {
        // Given Right position
        composeRule.setContent {
            LumenLauncherTheme {
                HomeScreen(appListItems = apps(1), dockApps = emptyList(), onAppClick = {}, appRowPosition = AppRowPosition.RIGHT)
            }
        }

        // Then the internal order flips — label renders before (further left than) the icon
        // (useUnmergedTree = true — see leftPositionRendersTheIconBeforeTheLabel's note above)
        val iconNode = composeRule.onNodeWithTag("home_app_icon_com.example.app1", useUnmergedTree = true).fetchSemanticsNode()
        val labelLeft = composeRule.onNodeWithTag("home_app_label_com.example.app1", useUnmergedTree = true).fetchSemanticsNode().boundsInRoot.left
        assert(labelLeft < iconNode.boundsInRoot.left)

        // And the icon — now the row's trailing element — sits in the right portion of the
        // screen rather than packed to the left, i.e. the whole row hugs the right edge, not
        // just a reversed order while staying left-anchored
        val rootWidth = composeRule.onRoot().fetchSemanticsNode().boundsInRoot.right
        assert(iconNode.boundsInRoot.right > rootWidth * 0.7f)
    }

    @Test
    fun rightPositionAlsoRightAlignsTheSectionHeading() {
        // Given Left position (default) — the "FAVORITES" heading hugs the left edge
        var appRowPosition by mutableStateOf(AppRowPosition.LEFT)
        composeRule.setContent {
            LumenLauncherTheme {
                HomeScreen(appListItems = apps(1), dockApps = emptyList(), onAppClick = {}, appRowPosition = appRowPosition)
            }
        }
        val rootWidth = composeRule.onRoot().fetchSemanticsNode().boundsInRoot.right
        val leftPositionHeadingLeft = composeRule.onNodeWithText("FAVORITES").fetchSemanticsNode().boundsInRoot.left
        assert(leftPositionHeadingLeft < rootWidth * 0.3f)

        // When switching to Right position
        appRowPosition = AppRowPosition.RIGHT
        composeRule.waitForIdle()

        // Then the heading moves with the app rows, now hugging the right edge instead
        val rightPositionHeadingRight = composeRule.onNodeWithText("FAVORITES").fetchSemanticsNode().boundsInRoot.right
        assert(rightPositionHeadingRight > rootWidth * 0.7f)
    }

    @Test
    fun iconOnlyPresentationHidesTheLabelButKeepsTheIconAccessible() {
        // Given Icon Only presentation
        composeRule.setContent {
            LumenLauncherTheme {
                HomeScreen(
                    appListItems = apps(1),
                    dockApps = emptyList(),
                    onAppClick = {},
                    appRowPresentation = AppRowPresentation.ICON_ONLY,
                )
            }
        }

        // Then the visible label text is gone, but the icon still renders and carries the app's
        // name for accessibility (mirroring the Dock's own icons-only mode)
        composeRule.onNodeWithText("App 1").assertDoesNotExist()
        composeRule.onNodeWithTag("home_app_icon_com.example.app1", useUnmergedTree = true).assertExists()
        composeRule.onNodeWithContentDescription("App 1").assertExists()
    }

    @Test
    fun textOnlyPresentationHidesTheIconButKeepsTheLabel() {
        // Given Text Only presentation
        composeRule.setContent {
            LumenLauncherTheme {
                HomeScreen(
                    appListItems = apps(1),
                    dockApps = emptyList(),
                    onAppClick = {},
                    appRowPresentation = AppRowPresentation.TEXT_ONLY,
                )
            }
        }

        // Then the label still renders, but the icon does not
        composeRule.onNodeWithText("App 1").assertExists()
        composeRule.onNodeWithTag("home_app_icon_com.example.app1").assertDoesNotExist()
    }

    @Test
    fun tappingARowStillLaunchesTheAppUnderIconOnlyPresentation() {
        // Given Icon Only presentation — the row has no visible text to tap, only the icon area
        var clicked: AppInfo? = null
        composeRule.setContent {
            LumenLauncherTheme {
                HomeScreen(
                    appListItems = apps(1),
                    dockApps = emptyList(),
                    onAppClick = { clicked = it },
                    appRowPresentation = AppRowPresentation.ICON_ONLY,
                )
            }
        }

        // When tapping the icon (its own node, now the row's only visible content) —
        // useUnmergedTree = true since its own bounds are still real regardless of the tree used;
        // the click still bubbles up to AppRow's registered handler
        composeRule.onNodeWithTag("home_app_icon_com.example.app1", useUnmergedTree = true).performClick()

        // Then the click still reaches the row's own click handler
        assertEquals("com.example.app1", clicked?.packageName)
    }

    @Test
    fun rendersWithoutCrashingUnderDarkMode() {
        // Given the system is in dark mode (Phase 5's dark color scheme)
        val all = apps(9)
        composeRule.setContent {
            val darkConfiguration = Configuration(LocalConfiguration.current).apply {
                uiMode = (uiMode and Configuration.UI_MODE_NIGHT_MASK.inv()) or Configuration.UI_MODE_NIGHT_YES
            }
            CompositionLocalProvider(LocalConfiguration provides darkConfiguration) {
                LumenLauncherTheme {
                    HomeScreen(appListItems = all.take(5), dockApps = all.drop(5), onAppClick = {})
                }
            }
        }

        // Then it renders its content normally — no crash, real content present
        composeRule.onNodeWithText("App 1").assertExists()
        composeRule.onNodeWithContentDescription("App 6").assertExists()
    }

    @Test
    fun zoneHandleIsHiddenUntilTheClockIsLongPressed() {
        // Given the default layout
        composeRule.setContent {
            LumenLauncherTheme {
                HomeScreen(appListItems = apps(3), dockApps = emptyList(), onAppClick = {})
            }
        }
        composeRule.onNodeWithTag("home_clock_zone_handle").assertDoesNotExist()

        // When the clock is long-pressed
        enableClockDragMode()

        // Then the handle appears
        composeRule.onNodeWithTag("home_clock_zone_handle").assertExists()
    }

    @Test
    fun tappingAwayFromTheRevealedHandleHidesItAgain() {
        // Given the handle revealed via a long-press on the clock
        composeRule.setContent {
            LumenLauncherTheme {
                HomeScreen(appListItems = apps(3), dockApps = emptyList(), onAppClick = {})
            }
        }
        enableClockDragMode()
        composeRule.onNodeWithTag("home_clock_zone_handle").assertExists()

        // When tapping a blank part of the screen — the gap between the clock's bottom edge and
        // the (still default, bottom-anchored) list's top edge — the dismiss gesture lives on the
        // Home root itself now (an ancestor of the handle, not a same-bounds sibling; see
        // HomeScreen.kt's own comment on why), so any unconsumed tap anywhere reaches it.
        val clockBottom = composeRule.onNodeWithTag("home_clock_block").fetchSemanticsNode().boundsInRoot.bottom
        val listTop = composeRule.onNodeWithTag("home_app_list_scroll_region").fetchSemanticsNode().boundsInRoot.top
        val rootBounds = composeRule.onRoot().fetchSemanticsNode().boundsInRoot
        composeRule.onRoot().performTouchInput {
            down(Offset(rootBounds.center.x, (clockBottom + listTop) / 2f))
            up()
        }

        // Then it's hidden again
        composeRule.onNodeWithTag("home_clock_zone_handle").assertDoesNotExist()
    }

    @Test
    fun draggingTheZoneHandleMovesTheClockBlockDownTogetherWithIt() {
        // Given the default (untouched) layout — clockZoneHeightDp is fed back in from the commit
        // callback, same as the real app's HomeUiState/HomeViewModel round-trip, so the position
        // persists after release instead of snapping back to the (still-null) default the moment
        // the drag ends. A stateless HomeScreen(onClockZoneHeightCommit = { local var }) call with
        // no such feedback loop would correctly revert on release by design (see chat history) —
        // that's not what this test means to exercise.
        var committedHeightDp: Float? = null
        composeRule.setContent {
            var zoneHeightDp by remember { mutableStateOf<Float?>(null) }
            LumenLauncherTheme {
                HomeScreen(
                    appListItems = apps(3),
                    dockApps = emptyList(),
                    onAppClick = {},
                    clockZoneHeightDp = zoneHeightDp,
                    onClockZoneHeightCommit = {
                        committedHeightDp = it
                        zoneHeightDp = it
                    },
                )
            }
        }
        val topBefore = composeRule.onNodeWithTag("home_clock_block").fetchSemanticsNode().boundsInRoot.top

        // When the clock is long-pressed to reveal the handle, then the handle is dragged down
        enableClockDragMode()
        dragHandleBy(150f)

        // Then the clock+calendar block slides down together with it (not just the boundary below it)
        val topAfter = composeRule.onNodeWithTag("home_clock_block").fetchSemanticsNode().boundsInRoot.top
        assertTrue(topAfter > topBefore)
        assertTrue(committedHeightDp != null)
    }

    @Test
    fun zoneHandleCannotBeDraggedAboveTheClocksMinimumGap() {
        // Given the default layout
        composeRule.setContent {
            LumenLauncherTheme {
                HomeScreen(appListItems = apps(3), dockApps = emptyList(), onAppClick = {})
            }
        }

        // When the handle is revealed and dragged far upward, past where the clock itself sits
        enableClockDragMode()
        dragHandleBy(-2000f)

        // Then the clock block never leaves the visible content area (its top never goes negative)
        val clockTop = composeRule.onNodeWithTag("home_clock_block").fetchSemanticsNode().boundsInRoot.top
        assertTrue(clockTop >= 0f)
    }

    @Test
    fun zoneHeightCannotExceedHalfOfContentHeight() {
        // Given the default layout
        composeRule.setContent {
            LumenLauncherTheme {
                HomeScreen(appListItems = apps(3), dockApps = emptyList(), onAppClick = {})
            }
        }
        val rootHeight = composeRule.onRoot().fetchSemanticsNode().boundsInRoot.bottom

        // When the handle is revealed and dragged far downward
        enableClockDragMode()
        dragHandleBy(5000f)

        // Then it's clamped well short of the bottom of the screen, not free to reach it
        val handleTop = composeRule.onNodeWithTag("home_clock_zone_handle").fetchSemanticsNode().boundsInRoot.top
        assertTrue(handleTop < rootHeight * 0.75f)
    }

    @Test
    fun clockAlignmentCenterRendersTheClockContentAwayFromBothEdges() {
        // Given Center alignment
        composeRule.setContent {
            LumenLauncherTheme {
                HomeScreen(appListItems = apps(1), dockApps = emptyList(), onAppClick = {}, clockAlignment = ClockAlignment.CENTER)
            }
        }

        // Then the time text itself (not just home_clock_block, which is full-width so its own
        // content can align within it) doesn't hug either edge of the screen. The exact time isn't
        // known (HomeScreen uses the real system clock), so match on the ":" every time string
        // contains rather than a specific value.
        val rootWidth = composeRule.onRoot().fetchSemanticsNode().boundsInRoot.right
        val timeBounds = composeRule.onNode(hasText(":", substring = true)).fetchSemanticsNode().boundsInRoot
        assertTrue(timeBounds.left > rootWidth * 0.1f)
        assertTrue(timeBounds.right < rootWidth * 0.9f)
    }

    @Test
    fun clockAlignmentRightRendersTheClockContentPackedAgainstTheRightEdge() {
        // Given Right alignment
        composeRule.setContent {
            LumenLauncherTheme {
                HomeScreen(appListItems = apps(1), dockApps = emptyList(), onAppClick = {}, clockAlignment = ClockAlignment.RIGHT)
            }
        }

        // Then the time text sits in the right portion of the screen (see the Center test above
        // for why this matches on ":" rather than a known time string)
        val rootWidth = composeRule.onRoot().fetchSemanticsNode().boundsInRoot.right
        val timeRight = composeRule.onNode(hasText(":", substring = true)).fetchSemanticsNode().boundsInRoot.right
        assertTrue(timeRight > rootWidth * 0.7f)
    }

    @Test
    fun topListAlignmentAnchorsTheListImmediatelyBelowTheGrabHandle() {
        // Given Top app-list alignment
        composeRule.setContent {
            LumenLauncherTheme {
                HomeScreen(
                    appListItems = apps(3),
                    dockApps = emptyList(),
                    onAppClick = {},
                    appListVerticalAlignment = AppListVerticalAlignment.TOP,
                )
            }
        }

        // Then the list's scroll region starts right where the (revealed) handle sits, not near the bottom
        enableClockDragMode()
        val handleTop = composeRule.onNodeWithTag("home_clock_zone_handle").fetchSemanticsNode().boundsInRoot.top
        val listTop = composeRule.onNodeWithTag("home_app_list_scroll_region").fetchSemanticsNode().boundsInRoot.top
        assertEquals(handleTop, listTop, 1f)
    }

    @Test
    fun bottomListAlignmentAnchorsTheListAboveTheDockExactlyAsToday() {
        // Given Bottom app-list alignment (the default) and a short list, with plenty of room above it
        composeRule.setContent {
            LumenLauncherTheme {
                HomeScreen(
                    appListItems = apps(2),
                    dockApps = emptyList(),
                    onAppClick = {},
                    appListVerticalAlignment = AppListVerticalAlignment.BOTTOM,
                )
            }
        }

        // Then the list sits well below the (revealed) handle — bottom-anchored, not flush against it
        enableClockDragMode()
        val handleTop = composeRule.onNodeWithTag("home_clock_zone_handle").fetchSemanticsNode().boundsInRoot.top
        val listTop = composeRule.onNodeWithTag("home_app_list_scroll_region").fetchSemanticsNode().boundsInRoot.top
        assertTrue(listTop > handleTop + 50f)
    }

    @Test
    fun aLongAppListScrollsIndependentlyOnceTheZoneHeightForcesItTo() {
        // Given a persisted clock zone height large enough to force the app list to overflow
        // (driven directly rather than via a live gesture, for a deterministic setup)
        composeRule.setContent {
            LumenLauncherTheme {
                HomeScreen(
                    appListItems = apps(10),
                    dockApps = emptyList(),
                    onAppClick = {},
                    clockZoneHeightDp = 2000f,
                )
            }
        }

        // Then the last row isn't visible yet, but becomes so after scrolling the list
        composeRule.onNodeWithText("App 10").assertIsNotDisplayed()
        composeRule.onNodeWithTag("home_app_list_scroll_region").performTouchInput { swipeUp() }
        composeRule.onNodeWithText("App 10").assertIsDisplayed()
    }

    @Test
    fun scrollingTheAppListNeverMovesTheClockBlock() {
        // Given the same forced-overflow setup as aLongAppListScrollsIndependentlyOnceTheZoneHeightForcesItTo
        composeRule.setContent {
            LumenLauncherTheme {
                HomeScreen(
                    appListItems = apps(10),
                    dockApps = emptyList(),
                    onAppClick = {},
                    clockZoneHeightDp = 2000f,
                )
            }
        }
        val clockBoundsBefore = composeRule.onNodeWithTag("home_clock_block").fetchSemanticsNode().boundsInRoot

        // When the list is scrolled
        composeRule.onNodeWithTag("home_app_list_scroll_region").performTouchInput { swipeUp() }

        // Then the clock's own position is unchanged
        val clockBoundsAfter = composeRule.onNodeWithTag("home_clock_block").fetchSemanticsNode().boundsInRoot
        assertEquals(clockBoundsBefore.top, clockBoundsAfter.top, 1f)
    }
}
