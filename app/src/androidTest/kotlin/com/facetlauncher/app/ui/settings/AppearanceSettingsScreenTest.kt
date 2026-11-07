package com.facetlauncher.app.ui.settings

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertTextContains
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollToNode
import androidx.room.Room
import com.facetlauncher.app.data.AppRepository
import com.facetlauncher.app.data.getTestableApps
import com.facetlauncher.app.data.FacetRepository
import com.facetlauncher.app.data.SettingsRepository
import com.facetlauncher.app.data.model.AppInfo
import com.facetlauncher.app.data.model.CalendarEvent
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import com.facetlauncher.app.data.PlacementRepository
import com.facetlauncher.app.data.model.PlacementTarget
import com.facetlauncher.app.data.model.PlacementList

class AppearanceSettingsScreenTest {

    @get:Rule
    val composeRule = createComposeRule()

    private val host = AppearanceScreenTestHost(composeRule)

    private fun awaitFacetScoped() = host.awaitFacetScoped()

    private fun setContent(
        onBack: () -> Unit = {},
        onNavigateToNotificationSettings: () -> Unit = {},
        onNavigateToDockAppsAppearance: () -> Unit = {},
        onNavigateToAppDrawerSettings: () -> Unit = {},
        onNavigateToThemesAndColors: () -> Unit = {},
        onNavigateToFonts: () -> Unit = {},
        onNavigateToIcons: () -> Unit = {},
        facetId: Long? = null,
        calendarGranted: Boolean = false,
        notificationAccessGranted: Boolean = true,
        calendarEvents: List<CalendarEvent> = emptyList(),
        seed: suspend (FacetRepository, Long?) -> Unit = { _, _ -> },
        seedApps: suspend (AppRepository, PlacementRepository, Long?) -> Unit = { _, _, _ -> },
    ): SettingsRepository = host.setContent(facetId, calendarGranted, notificationAccessGranted, calendarEvents, seed, seedApps) { viewModel ->
        AppearanceSettingsScreen(
            onBack = onBack,
            onNavigateToNotificationSettings = onNavigateToNotificationSettings,
            onNavigateToDockAppsAppearance = onNavigateToDockAppsAppearance,
            onNavigateToAppDrawerSettings = onNavigateToAppDrawerSettings,
            onNavigateToThemesAndColors = onNavigateToThemesAndColors,
            onNavigateToFonts = onNavigateToFonts,
            onNavigateToIcons = onNavigateToIcons,
            viewModel = viewModel,
        )
    }

    @Test
    fun headerStaysVisibleAfterScrollingToTheBottom() {
        // Given the screen, scrolled all the way down
        setContent()
        composeRule.onNodeWithTag("appearance_settings_screen").performScrollToNode(hasTestTag("general_icons_row"))

        // Then the pinned header (title + back button) is still on screen, not scrolled away
        composeRule.onNodeWithText("Customization").assertIsDisplayed()
        composeRule.onNodeWithTag("back_button").assertIsDisplayed()
    }

    @Test
    fun previewCardShowsTheRealFavoritesAndDockApps() {
        // Given the screen — the live preview card reuses the real AppRow/DockIcon, backed by
        // this scope's actual configured favorites/dock, not mock/sample data (see chat history).
        // Seeded with real installed apps, not a synthetic AppInfo — see setContent's own doc:
        // DefaultFavoriteAppRepository/DockAppRepository hydrate against the device's real
        // installed-app list, silently dropping any row that isn't actually installed.
        var favorite1Label = ""
        var favorite2Label = ""
        var dockLabel = ""
        setContent(
            seedApps = { appRepository, placementRepository, _ ->
                val installed = appRepository.getTestableApps()
                favorite1Label = installed[0].label
                favorite2Label = installed[1].label
                dockLabel = installed[2].label
                placementRepository.addApp(PlacementTarget.GlobalFavorites, installed[0], position = 0)
                placementRepository.addApp(PlacementTarget.GlobalFavorites, installed[1], position = 1)
                placementRepository.addApp(PlacementTarget.GlobalDock, installed[2], position = 0)
            },
        )

        // Then the preview card renders the real configured apps' own labels — polled, since the
        // favorites/dock repositories are backed by real Room queries, which settle asynchronously
        // after the first composition (the same "real I/O" lesson as elsewhere — see CLAUDE.md).
        composeRule.onNodeWithTag("appearance_preview_card").assertIsDisplayed()
        composeRule.waitUntil(timeoutMillis = 5_000) {
            composeRule.onAllNodesWithText(favorite1Label).fetchSemanticsNodes().isNotEmpty()
        }
        composeRule.onNodeWithText(favorite1Label).assertExists()
        composeRule.onNodeWithText(favorite2Label).assertExists()
        composeRule.onNodeWithContentDescription(dockLabel).assertExists()
    }

    @Test
    fun previewCardRendersTheWallpaperBehindItsContent() {
        // Given the screen, one real (installed) favorite seeded
        var favoriteLabel = ""
        setContent(
            seedApps = { appRepository, placementRepository, _ ->
                val app = appRepository.getTestableApps()[0]
                favoriteLabel = app.label
                placementRepository.addApp(PlacementTarget.GlobalFavorites, app, position = 0)
            },
        )

        // Then the preview card paints a wallpaper backdrop, with its real content still on top —
        // polled, same real-Room-query settle lesson as previewCardShowsTheRealFavoritesAndDockApps.
        composeRule.onNodeWithTag("appearance_preview_card").assertIsDisplayed()
        composeRule.onNodeWithTag("wallpaper_background").assertExists()
        composeRule.waitUntil(timeoutMillis = 5_000) {
            composeRule.onAllNodesWithText(favoriteLabel).fetchSemanticsNodes().isNotEmpty()
        }
        composeRule.onNodeWithText(favoriteLabel).assertExists()
    }

    @Test
    fun previewCardShowsAClockInGlobalMode() {
        // Given the global (non-facet-scoped) screen — the preview now includes the real clock
        // (Part A's deferred "shared mini-home-preview" follow-up, scoped down to just this
        // screen's own preview — see chat history)
        setContent()

        composeRule.onNodeWithTag("appearance_preview_home_clock_block").assertIsDisplayed()
    }

    @Test
    fun globalModeShowsThreeSeparateSectionsInOrderAndNoClockSection() {
        // Dock & Home / Notifications / General are each their own card, in that order, and a clock has
        // no launcher-wide settings, so there is no Clock & calendar section. Each header is asserted
        // right as it scrolls into view — not via cross-scroll bounds comparison, which breaks once an
        // earlier item scrolls far enough to be disposed from the LazyColumn's composed range.
        setContent()

        composeRule.onNodeWithTag("clock_style_gallery_row").assertDoesNotExist()
        composeRule.onNodeWithTag("appearance_calendar_settings_row").assertDoesNotExist()
        composeRule.onNodeWithTag("appearance_settings_screen").performScrollToNode(hasTestTag("appearance_dock_apps_row"))
        composeRule.onNodeWithText("Dock & home").assertIsDisplayed()
        composeRule.onNodeWithTag("appearance_settings_screen").performScrollToNode(hasTestTag("notification_settings_row"))
        composeRule.onNodeWithTag("notification_settings_row").assertIsDisplayed()
        composeRule.onNodeWithTag("appearance_settings_screen").performScrollToNode(hasTestTag("general_icons_row"))
        composeRule.onNodeWithText("Look & Feel").assertIsDisplayed()
    }

    @Test
    fun notificationRowReflectsEnabledStateAndNavigates() {
        // Given the global screen — badges are on by default with the Dot style
        var navigated = false
        setContent(onNavigateToNotificationSettings = { navigated = true })
        composeRule.onNodeWithTag("appearance_settings_screen").performScrollToNode(hasTestTag("notification_settings_row"))
        composeRule.onNodeWithTag("notification_settings_row").assertTextContains("Notifications and media", substring = true)
        // The access state arrives a frame after the first composition, so wait for the subtext like the other tests here
        awaitNotificationSubtext("Dot badges · Media player off")

        // When tapping it
        composeRule.onNodeWithTag("notification_settings_row").performClick()

        // Then its callback fires
        assertEquals(true, navigated)
    }

    private fun awaitNotificationSubtext(expected: String) {
        composeRule.onNodeWithTag("appearance_settings_screen").performScrollToNode(hasTestTag("notification_settings_row"))
        composeRule.waitUntil(timeoutMillis = 3_000) {
            runCatching { composeRule.onNodeWithTag("notification_settings_row").assertTextContains(expected, substring = true) }.isSuccess
        }
    }

    @Test
    fun notificationSubtextShowsCountBadgesAndMediaPlayerOn() {
        // Given Count badges and the media player turned on, with access granted
        val settingsRepository = setContent()
        runBlocking {
            settingsRepository.setNotificationBadgeStyle(com.facetlauncher.app.data.model.NotificationBadgeStyle.COUNT)
            settingsRepository.setShowMediaPlayerOnHome(true)
        }

        // Then the subtext reads both
        awaitNotificationSubtext("Count badges · Media player on")
    }

    @Test
    fun notificationSubtextShowsBadgesOffAndMediaPlayerOn() {
        // Given badges turned off and the media player on
        val settingsRepository = setContent()
        runBlocking {
            settingsRepository.setNotificationDotsEnabled(false)
            settingsRepository.setShowMediaPlayerOnHome(true)
        }

        // Then the subtext reads both
        awaitNotificationSubtext("Badges off · Media player on")
    }

    @Test
    fun notificationSubtextShowsBadgesOffAndMediaPlayerOff() {
        // Given everything turned off
        val settingsRepository = setContent()
        runBlocking { settingsRepository.setNotificationDotsEnabled(false) }

        // Then the subtext reads both as off
        awaitNotificationSubtext("Badges off · Media player off")
    }

    @Test
    fun notificationSubtextSaysAccessIsNeededWhenNotGrantedEvenIfSettingsAreOn() {
        // Given the stored settings on (badges default on, media player on) but no notification access
        val settingsRepository = setContent(notificationAccessGranted = false)
        runBlocking { settingsRepository.setShowMediaPlayerOnHome(true) }

        // Then the row says access is needed instead of reporting a stored "on" that isn't working
        awaitNotificationSubtext("Needs notification access")
    }

    @Test
    fun previewCardShowsRealCalendarEventsWhenPermissionIsGranted() {
        // The calendar preview moved here from ClockStyleGalleryScreen (see chat history) — real
        // events for the effective calendar selection, not sample/mock data. Timed relative to
        // real "now", not a fixed epoch instant — CalendarEventsBlock itself filters out any
        // non-all-day event whose endTimeMillis has already passed (an earlier version of this
        // fixture used endTimeMillis = 1, which is always in the past, so it was silently dropped
        // and the test could never pass no matter how long it polled).
        val now = System.currentTimeMillis()
        val event = CalendarEvent(id = 1, calendarId = "1", title = "Team standup", startTimeMillis = now, endTimeMillis = now + 3_600_000, isAllDay = false)
        setContent(calendarGranted = true, calendarEvents = listOf(event))

        // Polled — the calendar events are read inside the combine()'s own async settle, same as
        // the favorites/dock lists (see previewCardShowsTheRealFavoritesAndDockApps's own comment).
        composeRule.waitUntil(timeoutMillis = 5_000) {
            composeRule.onAllNodesWithText("Team standup").fetchSemanticsNodes().isNotEmpty()
        }
        composeRule.onNodeWithText("Team standup").assertIsDisplayed()
    }

    @Test
    fun previewCardShowsNoCalendarEventsWithoutPermissionRatherThanASampleFallback() {
        // Given calendar permission not granted (this screen's setContent default)
        setContent()

        // Then the preview shows no events at all — never a fake/sample fallback (see chat history)
        composeRule.onNodeWithText("Team standup").assertDoesNotExist()
    }

    @Test
    fun dockAppsRowSummarisesDockAndHomeAndNavigates() {
        // Given the global screen — dock icons and the Home list as icon + text by default
        var navigated = false
        setContent(onNavigateToDockAppsAppearance = { navigated = true })
        composeRule.onNodeWithTag("appearance_settings_screen").performScrollToNode(hasTestTag("appearance_dock_apps_row"))
        composeRule.onNodeWithTag("appearance_dock_apps_row").assertTextContains("Dock & Apps appearance", substring = true)
        composeRule.onNodeWithTag("appearance_dock_apps_row").assertTextContains("Dock icons · Home icon & text", substring = true)

        // When tapping it
        composeRule.onNodeWithTag("appearance_dock_apps_row").performClick()

        // Then its callback fires
        assertEquals(true, navigated)
    }

    @Test
    fun generalRowsNameTheirCurrentValueAndNavigate() {
        // Given the global screen
        val navigated = mutableListOf<String>()
        setContent(
            onNavigateToThemesAndColors = { navigated += "themes" },
            onNavigateToFonts = { navigated += "fonts" },
            onNavigateToIcons = { navigated += "icons" },
        )
        composeRule.onNodeWithTag("appearance_settings_screen").performScrollToNode(hasTestTag("general_icons_row"))

        // Then General reads its static subtitle and the others name their page and current value
        composeRule.onNodeWithTag("general_themes_colors_row").assertTextContains("Colors and Themes", substring = true)
        composeRule.onNodeWithTag("general_themes_colors_row").assertTextContains("Themes, accent, status bar", substring = true)
        composeRule.onNodeWithTag("general_fonts_row").assertTextContains("Fonts", substring = true)
        composeRule.onNodeWithTag("general_icons_row").assertTextContains("Default", substring = true)

        // When tapping each
        listOf("general_themes_colors_row", "general_fonts_row", "general_icons_row").forEach { composeRule.onNodeWithTag(it).performClick() }

        // Then its callback fires, in order
        assertEquals(listOf("themes", "fonts", "icons"), navigated)
    }

    @Test
    fun appDrawerRowSitsBesideDockAppsRowAndNavigates() {
        // Given the global Appearance screen
        var navigated = false
        setContent(onNavigateToAppDrawerSettings = { navigated = true })

        // When the App Drawer row in the Dock & home card is tapped
        composeRule.onNodeWithTag("appearance_settings_screen").performScrollToNode(hasTestTag("app_drawer_settings_row"))
        composeRule.onNodeWithTag("app_drawer_settings_row").performClick()

        // Then it navigates to the App Drawer screen
        assertEquals(true, navigated)
    }
}
