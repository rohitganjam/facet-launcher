package com.facetlauncher.app.ui.home

import android.content.res.Configuration
import androidx.compose.runtime.Composable
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
import com.facetlauncher.app.data.model.AppInfo
import com.facetlauncher.app.data.model.AppListVerticalAlignment
import com.facetlauncher.app.data.model.AppRowPosition
import com.facetlauncher.app.data.model.AppRowPresentation
import com.facetlauncher.app.data.model.CalendarEvent
import com.facetlauncher.app.data.model.ClockAlignment
import com.facetlauncher.app.data.model.DockDisplayMode
import com.facetlauncher.app.data.model.ListContentMode
import com.facetlauncher.app.ui.theme.FacetLauncherTheme
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

    /**
     * [HomeScreen] with the clock-adjust state hoisted the way [com.facetlauncher.app.ui.launcher.HomeDrawerRoute]
     * does it in the real app — so a long-press actually reveals the menu/handles. A bare
     * `HomeScreen(...)` call leaves `onAdjustModeChange` a no-op.
     */
    @Composable
    private fun TestHomeScreen(
        appListItems: List<AppInfo>,
        dockApps: List<AppInfo> = emptyList(),
        appListVerticalAlignment: AppListVerticalAlignment = AppListVerticalAlignment.BOTTOM,
        clockAlignment: ClockAlignment = ClockAlignment.LEFT,
        clockZoneHeightDp: Float? = null,
        onClockZoneHeightCommit: (Float) -> Unit = {},
        clockScale: Float = 0.8f,
        onClockScaleCommit: (Float) -> Unit = {},
    ) {
        var adjustMode by remember { mutableStateOf(ClockAdjustMode.NONE) }
        var dragging by remember { mutableStateOf(false) }
        FacetLauncherTheme {
            HomeScreen(
                appListItems = appListItems,
                dockApps = dockApps,
                onAppClick = {},
                appListVerticalAlignment = appListVerticalAlignment,
                clockAlignment = clockAlignment,
                clockZoneHeightDp = clockZoneHeightDp,
                onClockZoneHeightCommit = onClockZoneHeightCommit,
                clockScale = clockScale,
                onClockScaleCommit = onClockScaleCommit,
                clockAdjustMode = adjustMode,
                onAdjustModeChange = { adjustMode = it },
                draggingHandle = dragging,
                onDraggingHandleChange = { dragging = it },
            )
        }
    }

    /** Long-presses the clock, then picks "Adjust size & position" — reveals the move handle AND the resize handles. */
    private fun enterAdjustMode() {
        composeRule.onNodeWithTag("home_clock_block").performTouchInput { longClick() }
        composeRule.waitForIdle()
        composeRule.onNodeWithTag("clock_adjust_open").performClick()
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
            FacetLauncherTheme {
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
            FacetLauncherTheme {
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
            FacetLauncherTheme {
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
    fun longPressingBesideTheClockDoesNotRevealTheDragHandle() {
        // Given a left-aligned (default) clock, which today doesn't fill the screen's width —
        // home_clock_block now hugs only the clock's own rendered content (see HomeScreen.kt's
        // own doc comment on the hit-box fix), so there's real empty space to its right.
        composeRule.setContent {
            FacetLauncherTheme {
                HomeScreen(appListItems = apps(1), dockApps = emptyList(), onAppClick = {})
            }
        }
        val rootWidth = composeRule.onRoot().fetchSemanticsNode().boundsInRoot.right
        val clockBounds = composeRule.onNodeWithTag("home_clock_block").fetchSemanticsNode().boundsInRoot
        val besideClock = Offset((clockBounds.right + rootWidth) / 2f, clockBounds.center.y)

        // When that empty space, not the clock itself, is long-pressed
        composeRule.onRoot().performTouchInput { longClick(besideClock) }
        composeRule.waitForIdle()

        // Then drag mode never activates — the touch fell outside the clock's own hit box
        composeRule.onNodeWithTag("home_clock_zone_handle").assertDoesNotExist()
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
            FacetLauncherTheme {
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
            FacetLauncherTheme {
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
            FacetLauncherTheme {
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
            FacetLauncherTheme {
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
            FacetLauncherTheme {
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
            FacetLauncherTheme {
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
            FacetLauncherTheme {
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
            FacetLauncherTheme {
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
            FacetLauncherTheme {
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
            FacetLauncherTheme {
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
            FacetLauncherTheme {
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
            FacetLauncherTheme {
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
            FacetLauncherTheme {
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
                FacetLauncherTheme {
                    HomeScreen(appListItems = all.take(5), dockApps = all.drop(5), onAppClick = {})
                }
            }
        }

        // Then it renders its content normally — no crash, real content present
        composeRule.onNodeWithText("App 1").assertExists()
        composeRule.onNodeWithContentDescription("App 6").assertExists()
    }

    @Test
    fun adjustMenuSheetIsHiddenUntilTheClockIsLongPressed() {
        // Given the default layout
        composeRule.setContent { TestHomeScreen(appListItems = apps(3)) }
        composeRule.onNodeWithTag("clock_adjust_sheet").assertDoesNotExist()

        // When the clock is long-pressed
        composeRule.onNodeWithTag("home_clock_block").performTouchInput { longClick() }
        composeRule.waitForIdle()

        // Then the adjustment menu sheet appears
        composeRule.onNodeWithTag("clock_adjust_sheet").assertIsDisplayed()
    }

    @Test
    fun tappingAwayFromTheRevealedSheetHidesItAgain() {
        // Given the sheet revealed via a long-press on the clock
        composeRule.setContent { TestHomeScreen(appListItems = apps(3)) }
        composeRule.onNodeWithTag("home_clock_block").performTouchInput { longClick() }
        composeRule.waitForIdle()
        composeRule.onNodeWithTag("clock_adjust_sheet").assertIsDisplayed()

        // When tapping the scrim (outside the sheet)
        composeRule.onNodeWithTag("clock_adjust_scrim").performClick()
        composeRule.waitForIdle()

        // Then it's hidden again
        composeRule.onNodeWithTag("clock_adjust_sheet").assertDoesNotExist()
    }

    @Test
    fun longPressingEmptyHomeSpaceOpensTheSameAdjustMenuSheet() {
        // Given the default (left-aligned) layout, which leaves real empty space to the clock's right
        composeRule.setContent { TestHomeScreen(appListItems = apps(3)) }
        composeRule.onNodeWithTag("clock_adjust_sheet").assertDoesNotExist()
        val rootWidth = composeRule.onRoot().fetchSemanticsNode().boundsInRoot.right
        val clockBounds = composeRule.onNodeWithTag("home_clock_block").fetchSemanticsNode().boundsInRoot
        val besideClock = Offset((clockBounds.right + rootWidth) / 2f, clockBounds.center.y)

        // When that empty space, not the clock itself, is long-pressed
        composeRule.onNodeWithTag("home_screen_root").performTouchInput { longClick(besideClock) }
        composeRule.waitForIdle()

        // Then the same clock adjust sheet opens
        composeRule.onNodeWithTag("clock_adjust_sheet").assertIsDisplayed()
    }

    @Test
    fun tappingLauncherSettingsInTheSheetNavigatesAndClosesIt() {
        // Given the sheet opened via a long-press on the clock, with a hoisted settings callback
        var navigated = false
        composeRule.setContent {
            var adjustMode by remember { mutableStateOf(ClockAdjustMode.NONE) }
            FacetLauncherTheme {
                HomeScreen(
                    appListItems = apps(3),
                    dockApps = emptyList(),
                    onAppClick = {},
                    clockAdjustMode = adjustMode,
                    onAdjustModeChange = { adjustMode = it },
                    onNavigateToSettings = { navigated = true },
                )
            }
        }
        composeRule.onNodeWithTag("home_clock_block").performTouchInput { longClick() }
        composeRule.waitForIdle()
        composeRule.onNodeWithTag("clock_adjust_sheet").assertIsDisplayed()

        // When "Launcher settings" is tapped
        composeRule.onNodeWithTag("clock_adjust_launcher_settings").performClick()
        composeRule.waitForIdle()

        // Then the callback fires and the sheet closes
        assertTrue(navigated)
        composeRule.onNodeWithTag("clock_adjust_sheet").assertDoesNotExist()
    }

    @Test
    fun tappingProfileSettingsInTheSheetNavigatesAndClosesIt() {
        // Given the sheet opened via a long-press on the clock, with a hoisted profile-settings callback
        var navigated = false
        composeRule.setContent {
            var adjustMode by remember { mutableStateOf(ClockAdjustMode.NONE) }
            FacetLauncherTheme {
                HomeScreen(
                    appListItems = apps(3),
                    dockApps = emptyList(),
                    onAppClick = {},
                    clockAdjustMode = adjustMode,
                    onAdjustModeChange = { adjustMode = it },
                    onNavigateToProfileSettings = { navigated = true },
                )
            }
        }
        composeRule.onNodeWithTag("home_clock_block").performTouchInput { longClick() }
        composeRule.waitForIdle()
        composeRule.onNodeWithTag("clock_adjust_sheet").assertIsDisplayed()

        // When "Profile settings" is tapped
        composeRule.onNodeWithTag("clock_adjust_profile_settings").performClick()
        composeRule.waitForIdle()

        // Then the callback fires and the sheet closes
        assertTrue(navigated)
        composeRule.onNodeWithTag("clock_adjust_sheet").assertDoesNotExist()
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
            TestHomeScreen(
                appListItems = apps(3),
                clockZoneHeightDp = zoneHeightDp,
                onClockZoneHeightCommit = {
                    committedHeightDp = it
                    zoneHeightDp = it
                },
            )
        }
        val topBefore = composeRule.onNodeWithTag("home_clock_block").fetchSemanticsNode().boundsInRoot.top

        // When the clock is long-pressed to reveal the handle, then the handle is dragged down
        enterAdjustMode()
        dragHandleBy(150f)

        // Then the clock+calendar block slides down together with it (not just the boundary below it)
        val topAfter = composeRule.onNodeWithTag("home_clock_block").fetchSemanticsNode().boundsInRoot.top
        assertTrue(topAfter > topBefore)
        assertTrue(committedHeightDp != null)
    }

    @Test
    fun zoneHandleCannotBeDraggedAboveTheClocksMinimumGap() {
        // Given the default layout
        composeRule.setContent { TestHomeScreen(appListItems = apps(3)) }

        // When the handle is revealed and dragged far upward, past where the clock itself sits
        enterAdjustMode()
        dragHandleBy(-2000f)

        // Then the clock block never leaves the visible content area (its top never goes negative)
        val clockTop = composeRule.onNodeWithTag("home_clock_block").fetchSemanticsNode().boundsInRoot.top
        assertTrue(clockTop >= 0f)
    }

    @Test
    fun zoneHeightCannotExceedHalfOfContentHeight() {
        // Given the default layout
        composeRule.setContent { TestHomeScreen(appListItems = apps(3)) }
        val rootHeight = composeRule.onRoot().fetchSemanticsNode().boundsInRoot.bottom

        // When the handle is revealed and dragged far downward
        enterAdjustMode()
        dragHandleBy(5000f)

        // Then it's clamped well short of the bottom of the screen, not free to reach it
        val handleTop = composeRule.onNodeWithTag("home_clock_zone_handle").fetchSemanticsNode().boundsInRoot.top
        assertTrue(handleTop < rootHeight * 0.75f)
    }

    @Test
    fun draggingTheResizeHandleScalesTheClock() {
        var committedScale = 1f
        composeRule.setContent {
            var scale by remember { mutableStateOf(1f) }
            // Clock pushed down so there's headroom above it to actually grow into.
            TestHomeScreen(
                appListItems = apps(1),
                clockZoneHeightDp = 900f,
                clockScale = scale,
                onClockScaleCommit = { committedScale = it; scale = it },
            )
        }
        val widthBefore = composeRule.onNodeWithTag("home_clock_block").fetchSemanticsNode().boundsInRoot.width
        val heightBefore = composeRule.onNodeWithTag("home_clock_block").fetchSemanticsNode().boundsInRoot.height

        // When resize mode is enabled and the right handle is dragged right and up. The move is
        // split — the first event only captures the finger's offset from the corner (so the clock
        // doesn't pop), the rest actually scale.
        enterAdjustMode()
        composeRule.onNodeWithTag("clock_resize_handle_right").performTouchInput {
            down(center)
            moveBy(Offset(20f, -10f))
            moveBy(Offset(60f, -30f))
            moveBy(Offset(60f, -30f))
            up()
        }
        composeRule.waitForIdle()

        // Then the clock grows on both axes (uniform scale) and commits a value > 1f
        val widthAfter = composeRule.onNodeWithTag("home_clock_block").fetchSemanticsNode().boundsInRoot.width
        val heightAfter = composeRule.onNodeWithTag("home_clock_block").fetchSemanticsNode().boundsInRoot.height
        assertTrue("Width should have increased", widthAfter > widthBefore)
        assertTrue("Height should have increased", heightAfter > heightBefore)
        assertTrue("Committed scale should be > 1f", committedScale > 1f)
    }

    @Test
    fun clockScaleIsClampedToItsBounds() {
        var committedScale = 1f
        composeRule.setContent {
            var scale by remember { mutableStateOf(1f) }
            TestHomeScreen(
                appListItems = apps(1),
                clockZoneHeightDp = 900f,
                clockScale = scale,
                onClockScaleCommit = { committedScale = it; scale = it },
            )
        }

        // When dragging far out, the committed scale can't exceed the 2.0 ceiling (it may land
        // lower — the real cap is whatever still fits the screen). First move captures the grab
        // offset, the rest scale.
        enterAdjustMode()
        composeRule.onNodeWithTag("clock_resize_handle_right").performTouchInput {
            down(center)
            moveBy(Offset(40f, -20f))
            moveBy(Offset(200f, -150f))
            moveBy(Offset(3000f, -2000f))
            up()
        }
        composeRule.waitForIdle()
        assertTrue("grew past 1f", committedScale > 1f)
        assertTrue("clamped at or below 2.0", committedScale <= 2.0f + 0.01f)

        // When dragging far in (still in adjust mode), it clamps to the hard 0.5 floor.
        composeRule.onNodeWithTag("clock_resize_handle_right").performTouchInput {
            down(center)
            moveBy(Offset(-40f, 20f))
            moveBy(Offset(-200f, 150f))
            moveBy(Offset(-3000f, 2000f))
            up()
        }
        composeRule.waitForIdle()
        assertEquals(0.5f, committedScale, 0.01f)
    }

    @Test
    fun repositioningTheClockHigherShrinksItToFitAndPersistsThatScale() {
        // Given a large clock, low on the screen (lots of headroom above it)
        var committedScale = 2f
        composeRule.setContent {
            var scale by remember { mutableStateOf(2f) }
            var zone by remember { mutableStateOf<Float?>(900f) }
            TestHomeScreen(
                appListItems = apps(3),
                clockZoneHeightDp = zone,
                onClockZoneHeightCommit = { zone = it },
                clockScale = scale,
                onClockScaleCommit = { committedScale = it; scale = it },
            )
        }
        val heightBefore = composeRule.onNodeWithTag("home_clock_block").fetchSemanticsNode().boundsInRoot.height

        // When the move handle is dragged far up, toward the status bar
        enterAdjustMode()
        dragHandleBy(-1500f)

        // Then the clock shrank in real time to stay within the now-reduced headroom, and the
        // smaller scale was persisted so it doesn't snap back
        val heightAfter = composeRule.onNodeWithTag("home_clock_block").fetchSemanticsNode().boundsInRoot.height
        assertTrue("clock shrank to fit", heightAfter < heightBefore)
        assertTrue("the shrunk scale was committed", committedScale < 2f)
    }

    @Test
    fun clockAlignmentCenterRendersTheClockContentAwayFromBothEdges() {
        // Given Center alignment
        composeRule.setContent {
            FacetLauncherTheme {
                HomeScreen(appListItems = apps(1), dockApps = emptyList(), onAppClick = {}, clockAlignment = ClockAlignment.CENTER)
            }
        }

        // Then the time text doesn't hug either edge of the screen. The exact time isn't known
        // (HomeScreen uses the real system clock), so match on the ":" every time string contains
        // rather than a specific value.
        val rootWidth = composeRule.onRoot().fetchSemanticsNode().boundsInRoot.right
        val timeBounds = composeRule.onNode(hasText(":", substring = true)).fetchSemanticsNode().boundsInRoot
        assertTrue(timeBounds.left > rootWidth * 0.1f)
        assertTrue(timeBounds.right < rootWidth * 0.9f)
    }

    @Test
    fun clockAlignmentRightRendersTheClockContentPackedAgainstTheRightEdge() {
        // Given Right alignment
        composeRule.setContent {
            FacetLauncherTheme {
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
            TestHomeScreen(appListItems = apps(3), appListVerticalAlignment = AppListVerticalAlignment.TOP)
        }

        // Then the list's scroll region starts at the (revealed) handle, not near the bottom —
        // within one handle-height below it, never above and never bottom-anchored.
        enterAdjustMode()
        val handleTop = composeRule.onNodeWithTag("home_clock_zone_handle").fetchSemanticsNode().boundsInRoot.top
        val listTop = composeRule.onNodeWithTag("home_app_list_scroll_region").fetchSemanticsNode().boundsInRoot.top
        assertTrue("list starts at/below the handle", listTop >= handleTop - 1f)
        assertTrue("list starts immediately below the handle, not near the bottom", listTop < handleTop + 130f)
    }

    @Test
    fun bottomListAlignmentAnchorsTheListAboveTheDockExactlyAsToday() {
        // Given Bottom app-list alignment (the default) and a short list, with plenty of room above it
        composeRule.setContent {
            TestHomeScreen(appListItems = apps(2), appListVerticalAlignment = AppListVerticalAlignment.BOTTOM)
        }

        // Then the list sits well below the (revealed) handle — bottom-anchored, not flush against it
        enterAdjustMode()
        val handleTop = composeRule.onNodeWithTag("home_clock_zone_handle").fetchSemanticsNode().boundsInRoot.top
        val listTop = composeRule.onNodeWithTag("home_app_list_scroll_region").fetchSemanticsNode().boundsInRoot.top
        assertTrue(listTop > handleTop + 50f)
    }

    @Test
    fun aLongAppListScrollsIndependentlyOnceTheZoneHeightForcesItTo() {
        // Given a persisted clock zone height large enough to force the app list to overflow
        // (driven directly rather than via a live gesture, for a deterministic setup)
        composeRule.setContent {
            FacetLauncherTheme {
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
            FacetLauncherTheme {
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
