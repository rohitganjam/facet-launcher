package com.facetlauncher.app.ui.settings

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertTextContains
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollToNode
import androidx.room.Room
import com.facetlauncher.app.data.AppRepository
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
        onNavigateToClockStyleGallery: () -> Unit = {},
        onNavigateToCalendarSettings: () -> Unit = {},
        onNavigateToNotificationSettings: () -> Unit = {},
        onNavigateToDockAppsAppearance: () -> Unit = {},
        onNavigateToFontsThemesIcons: () -> Unit = {},
        facetId: Long? = null,
        calendarGranted: Boolean = false,
        notificationAccessGranted: Boolean = true,
        calendarEvents: List<CalendarEvent> = emptyList(),
        seed: suspend (FacetRepository, Long?) -> Unit = { _, _ -> },
        seedApps: suspend (AppRepository, PlacementRepository, Long?) -> Unit = { _, _, _ -> },
    ): SettingsRepository = host.setContent(facetId, calendarGranted, notificationAccessGranted, calendarEvents, seed, seedApps) { viewModel ->
        AppearanceSettingsScreen(
            onBack = onBack,
            onNavigateToClockStyleGallery = onNavigateToClockStyleGallery,
            onNavigateToCalendarSettings = onNavigateToCalendarSettings,
            onNavigateToNotificationSettings = onNavigateToNotificationSettings,
            onNavigateToDockAppsAppearance = onNavigateToDockAppsAppearance,
            onNavigateToFontsThemesIcons = onNavigateToFontsThemesIcons,
            viewModel = viewModel,
        )
    }

    @Test
    fun headerStaysVisibleAfterScrollingToTheBottom() {
        // Given the screen, scrolled all the way down
        setContent()
        composeRule.onNodeWithTag("appearance_settings_screen").performScrollToNode(hasTestTag("appearance_fonts_themes_icons_row"))

        // Then the pinned header (title + back button) is still on screen, not scrolled away
        composeRule.onNodeWithText("Appearance").assertIsDisplayed()
        composeRule.onNodeWithTag("back_button").assertIsDisplayed()
    }

    @Test
    fun clockStyleGalleryRowIsClickable() {
        // Given the screen, scrolled to the Clock style row (moved in from the main Settings
        // list — see chat history: calendar/appearance styling consolidation)
        var navigated = false
        setContent(onNavigateToClockStyleGallery = { navigated = true })
        composeRule.onNodeWithTag("appearance_settings_screen").performScrollToNode(hasTestTag("clock_style_gallery_row"))

        // When tapping it
        composeRule.onNodeWithTag("clock_style_gallery_row").performClick()

        // Then its callback fires
        org.junit.Assert.assertEquals(true, navigated)
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
                val installed = appRepository.getInstalledApps()
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
                val app = appRepository.getInstalledApps()[0]
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

        composeRule.onNodeWithTag("appearance_preview_clock").assertIsDisplayed()
    }

    @Test
    fun facetScopedModeKeepsClockRowAndPreviewButHidesEveryGlobalOnlyRow() {
        // Given a facet-scoped entry point — waited for the facet-scoped combine() to settle first
        // (the `facets` flow is a real Room query; the screen briefly renders as global/unscoped on
        // its very first composition until it emits — see chat history).
        setContent(facetId = 1L)
        awaitFacetScoped()

        // Then the Dock & Apps appearance row (the moved-in look fields) and the Clock row are here...
        composeRule.onNodeWithTag("appearance_settings_screen").performScrollToNode(hasTestTag("appearance_dock_apps_row"))
        composeRule.onNodeWithTag("appearance_dock_apps_row").assertExists()
        composeRule.onNodeWithTag("appearance_settings_screen").performScrollToNode(hasTestTag("clock_style_gallery_row"))
        composeRule.onNodeWithTag("clock_style_gallery_row").assertExists()
        // ...but the purely-global rows (notifications, fonts / themes / icons) are gone — no
        // per-facet override exists for those yet (see this screen's own doc comment)
        composeRule.onNodeWithTag("notification_settings_row").assertDoesNotExist()
        composeRule.onNodeWithTag("appearance_fonts_themes_icons_row").assertDoesNotExist()
    }

    @Test
    fun facetScopedClockRowReflectsTheFacetsOwnClockOverride() {
        // Given a facet overriding the clock with a different template than the global default
        setContent(facetId = 1L, seed = { facetRepository, facetId ->
            val facet = com.facetlauncher.app.data.model.Facet(id = requireNotNull(facetId), name = "P", position = 0)
            facetRepository.setOverrideClock(facet.id, true)
            facetRepository.setClockTemplateId(facet.id, com.facetlauncher.app.data.model.ClockTemplateId.RULE_MERIDIEM)
        })
        awaitFacetScoped()

        // Then the preview clock is shown and the Clock row names the facet's own template, not the global one
        composeRule.onNodeWithTag("appearance_preview_clock").assertExists()
        composeRule.onNodeWithTag("appearance_settings_screen").performScrollToNode(hasTestTag("clock_style_gallery_row"))
        composeRule.onNodeWithTag("clock_style_gallery_row").assertTextContains("Ruler", substring = true)
    }

    @Test
    fun facetScopedClockRowOpensTheClockStyleGallery() {
        // Given a facet-scoped screen
        var navigated = false
        setContent(facetId = 1L, onNavigateToClockStyleGallery = { navigated = true })
        awaitFacetScoped()

        // When tapping the Clock row
        composeRule.onNodeWithTag("appearance_settings_screen").performScrollToNode(hasTestTag("clock_style_gallery_row"))
        composeRule.onNodeWithTag("clock_style_gallery_row").performClick()

        // Then it navigates
        composeRule.waitUntil(timeoutMillis = 3_000) { navigated }
    }

    @Test
    fun globalModeShowsFourSeparateSectionsInOrder() {
        // Clock & calendar / Dock & Home / Notifications / General are each their own card, in that
        // order. Each header is asserted right as it scrolls into view — not via cross-scroll bounds
        // comparison, which breaks once an earlier item scrolls far enough to be disposed from the
        // LazyColumn's composed range.
        setContent()

        composeRule.onNodeWithTag("appearance_settings_screen").performScrollToNode(hasTestTag("clock_style_gallery_row"))
        composeRule.onNodeWithText("Clock & calendar").assertIsDisplayed()
        composeRule.onNodeWithTag("appearance_settings_screen").performScrollToNode(hasTestTag("appearance_dock_apps_row"))
        composeRule.onNodeWithText("Dock & home").assertIsDisplayed()
        composeRule.onNodeWithTag("appearance_settings_screen").performScrollToNode(hasTestTag("notification_settings_row"))
        composeRule.onNodeWithTag("notification_settings_row").assertIsDisplayed()
        composeRule.onNodeWithTag("appearance_settings_screen").performScrollToNode(hasTestTag("appearance_fonts_themes_icons_row"))
        composeRule.onNodeWithText("General").assertIsDisplayed()
    }

    @Test
    fun calendarRowNavigatesAndSitsUnderTheClockRow() {
        // Given the screen
        var navigated = false
        setContent(onNavigateToCalendarSettings = { navigated = true })
        composeRule.onNodeWithTag("appearance_settings_screen").performScrollToNode(hasTestTag("appearance_calendar_settings_row"))

        // When tapping the Calendars to display row
        composeRule.onNodeWithTag("appearance_calendar_settings_row").performClick()

        // Then its callback fires
        assertEquals(true, navigated)
    }

    @Test
    fun calendarRowSubtitleShowsZeroWhenNothingHasBeenChosenYet() {
        // Given a fresh install with calendar access — selectedCalendarIds is null, which is "none decided", not "all"
        setContent(calendarGranted = true)
        composeRule.onNodeWithTag("appearance_settings_screen").performScrollToNode(hasTestTag("appearance_calendar_settings_row"))

        // Then the row shows a literal zero (once the ViewModel's first real state replaces its initial one)
        composeRule.waitUntil(timeoutMillis = 3_000) {
            runCatching {
                composeRule.onNodeWithTag("appearance_calendar_settings_row").assertTextContains("0 calendars selected", substring = true)
            }.isSuccess
        }
    }

    @Test
    fun calendarRowSaysAccessIsNeededWhenCalendarPermissionIsNotGranted() {
        // Given a stored selection but no calendar permission (the default in these tests)
        val settingsRepository = setContent(calendarGranted = false)
        runBlocking { settingsRepository.setSelectedCalendarIds(setOf("cal-1", "cal-2")) }
        composeRule.onNodeWithTag("appearance_settings_screen").performScrollToNode(hasTestTag("appearance_calendar_settings_row"))

        // Then the row says access is needed, not a count of calendars nothing can read
        composeRule.onNodeWithTag("appearance_calendar_settings_row").assertTextContains("Needs calendar access", substring = true)
        composeRule.onAllNodesWithText("2 calendars selected", substring = true).assertCountEquals(0)
    }

    @Test
    fun facetScopedCalendarRowAlsoSaysAccessIsNeeded() {
        // Given a facet-scoped screen without calendar permission
        setContent(facetId = 1L, calendarGranted = false)
        awaitFacetScoped()
        composeRule.onNodeWithTag("appearance_settings_screen").performScrollToNode(hasTestTag("appearance_calendar_settings_row"))

        // Then the facet's row says so too
        composeRule.onNodeWithTag("appearance_calendar_settings_row").assertTextContains("Needs calendar access", substring = true)
    }

    @Test
    fun calendarRowSubtitleReflectsAnExplicitSelection() {
        // Given 2 calendars explicitly selected, with calendar access
        val settingsRepository = setContent(calendarGranted = true)
        runBlocking { settingsRepository.setSelectedCalendarIds(setOf("cal-1", "cal-2")) }
        composeRule.onNodeWithTag("appearance_settings_screen").performScrollToNode(hasTestTag("appearance_calendar_settings_row"))

        // Then the row reflects the count and the all-day events visibility
        composeRule.waitUntil(timeoutMillis = 3_000) {
            runCatching {
                composeRule.onNodeWithTag("appearance_calendar_settings_row").assertTextContains("2 calendars selected · All-day events visible", substring = true)
            }.isSuccess
        }
    }

    @Test
    fun facetScopedCalendarRowSubtitleReflectsTheInheritedGlobalSelection() {
        // Given a facet that isn't overriding calendar, with 2 calendars selected globally and calendar access
        val settingsRepository = setContent(facetId = 1L, calendarGranted = true)
        awaitFacetScoped()
        runBlocking { settingsRepository.setSelectedCalendarIds(setOf("cal-1", "cal-2")) }
        composeRule.onNodeWithTag("appearance_settings_screen").performScrollToNode(hasTestTag("appearance_calendar_settings_row"))

        // Then the facet's Appearance row shows the inherited count
        composeRule.waitUntil(timeoutMillis = 3_000) {
            runCatching {
                composeRule.onNodeWithTag("appearance_calendar_settings_row").assertTextContains("2 calendars selected", substring = true)
            }.isSuccess
        }
    }

    @Test
    fun notificationRowReflectsEnabledStateAndNavigates() {
        // Given the global screen — badges are on by default with the Dot style
        var navigated = false
        setContent(onNavigateToNotificationSettings = { navigated = true })
        composeRule.onNodeWithTag("appearance_settings_screen").performScrollToNode(hasTestTag("notification_settings_row"))
        composeRule.onNodeWithTag("notification_settings_row").assertTextContains("Notifications and media", substring = true)
        composeRule.onNodeWithTag("notification_settings_row").assertTextContains("Dot badges · Media player off", substring = true)

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
    fun notificationRowIsGlobalOnly() {
        // Given a facet-scoped Appearance screen
        setContent(facetId = 1L)
        awaitFacetScoped()

        // Then there's no Notifications row (notifications have no per-facet override)
        composeRule.onNodeWithTag("notification_settings_row").assertDoesNotExist()
    }

    @Test
    fun facetScopedModeShowsClockAndDockAndHomeButNotGeneral() {
        // The Clock & calendar section lives in facet mode too (above Dock & home); General is global-only.
        setContent(facetId = 1L)
        composeRule.waitUntil(timeoutMillis = 5_000) {
            composeRule.onAllNodesWithText("Clock & calendar").fetchSemanticsNodes().isNotEmpty()
        }

        composeRule.onNodeWithText("Clock & calendar").assertIsDisplayed()
        composeRule.onNodeWithTag("appearance_settings_screen").performScrollToNode(hasTestTag("appearance_dock_apps_row"))
        composeRule.onNodeWithText("Dock & home").assertIsDisplayed()
        composeRule.onNodeWithText("General").assertDoesNotExist()
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
    fun facetScopedPreviewShowsTheDefaultFavoritesAndDockWhileNotOverriding() {
        // "in facet settings - consume the resolved output between facet and defaults" (see chat
        // history) — a facet not overriding apps/dock content shows the launcher-wide default in
        // its own preview, not its own (unused) stale rows.
        val facetId = 1L
        var globalFavoriteLabel = ""
        var globalDockLabel = ""
        var facetFavoriteLabel = ""
        var facetDockLabel = ""
        setContent(
            facetId = facetId,
            seedApps = { appRepository, placementRepository, id ->
                val installed = appRepository.getInstalledApps()
                globalFavoriteLabel = installed[0].label
                globalDockLabel = installed[1].label
                facetFavoriteLabel = installed[2].label
                facetDockLabel = installed[3].label
                placementRepository.addApp(PlacementTarget.GlobalFavorites, installed[0], position = 0)
                placementRepository.addApp(PlacementTarget.GlobalDock, installed[1], position = 0)
                placementRepository.addApp(PlacementTarget(PlacementList.FAVORITES, id!!), installed[2], position = 0)
                placementRepository.addApp(PlacementTarget(PlacementList.DOCK, id), installed[3], position = 0)
            },
        )

        // Polled — same real-Room-query settle lesson as previewCardShowsTheRealFavoritesAndDockApps.
        composeRule.waitUntil(timeoutMillis = 5_000) {
            composeRule.onAllNodesWithText(globalFavoriteLabel).fetchSemanticsNodes().isNotEmpty()
        }
        composeRule.onNodeWithText(globalFavoriteLabel).assertExists()
        composeRule.onNodeWithContentDescription(globalDockLabel).assertExists()
        composeRule.onNodeWithText(facetFavoriteLabel).assertDoesNotExist()
        composeRule.onNodeWithContentDescription(facetDockLabel).assertDoesNotExist()
    }

    @Test
    fun facetScopedPreviewShowsThatFacetsOwnFavoritesAndDockWhileOverriding() {
        // The inverse of facetScopedPreviewShowsTheDefaultFavoritesAndDockWhileNotOverriding — once
        // a facet overrides apps/dock content, its own rows win over the launcher-wide default.
        val facetId = 1L
        var globalFavoriteLabel = ""
        var globalDockLabel = ""
        var facetFavoriteLabel = ""
        var facetDockLabel = ""
        setContent(
            facetId = facetId,
            seed = { facetRepository, id ->
                // Re-fetched between writes — `facetDao.update` replaces the whole row, so reusing
                // the same (now-stale) `facet` snapshot for the second call would clobber the first
                // call's own field change back to its old value.
                val facet = facetRepository.getById(id!!)!!
                facetRepository.setOverrideApps(facet.id, overriding = true)
                val updated = facetRepository.getById(id)!!
                facetRepository.updateOverridingDock(updated.id, overriding = true)
            },
            seedApps = { appRepository, placementRepository, id ->
                val installed = appRepository.getInstalledApps()
                globalFavoriteLabel = installed[0].label
                globalDockLabel = installed[1].label
                facetFavoriteLabel = installed[2].label
                facetDockLabel = installed[3].label
                placementRepository.addApp(PlacementTarget.GlobalFavorites, installed[0], position = 0)
                placementRepository.addApp(PlacementTarget.GlobalDock, installed[1], position = 0)
                placementRepository.addApp(PlacementTarget(PlacementList.FAVORITES, id!!), installed[2], position = 0)
                placementRepository.addApp(PlacementTarget(PlacementList.DOCK, id), installed[3], position = 0)
            },
        )

        // Polled — same real-Room-query settle lesson as previewCardShowsTheRealFavoritesAndDockApps.
        composeRule.waitUntil(timeoutMillis = 5_000) {
            composeRule.onAllNodesWithText(facetFavoriteLabel).fetchSemanticsNodes().isNotEmpty()
        }
        composeRule.onNodeWithText(globalFavoriteLabel).assertDoesNotExist()
        composeRule.onNodeWithContentDescription(globalDockLabel).assertDoesNotExist()
        composeRule.onNodeWithText(facetFavoriteLabel).assertExists()
        composeRule.onNodeWithContentDescription(facetDockLabel).assertExists()
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
    fun facetScopedDockAppsRowNavigates() {
        // Given a facet-scoped screen
        var navigated = false
        setContent(facetId = 1L, onNavigateToDockAppsAppearance = { navigated = true })
        awaitFacetScoped()
        composeRule.onNodeWithTag("appearance_settings_screen").performScrollToNode(hasTestTag("appearance_dock_apps_row"))

        // When tapping the row
        composeRule.onNodeWithTag("appearance_dock_apps_row").performClick()

        // Then its callback fires
        assertEquals(true, navigated)
    }

    @Test
    fun fontsThemesAndIconsRowNavigates() {
        // Given the global screen
        var navigated = false
        setContent(onNavigateToFontsThemesIcons = { navigated = true })
        composeRule.onNodeWithTag("appearance_settings_screen").performScrollToNode(hasTestTag("appearance_fonts_themes_icons_row"))
        composeRule.onNodeWithTag("appearance_fonts_themes_icons_row").assertTextContains("Fonts, themes and icons", substring = true)

        // When tapping it
        composeRule.onNodeWithTag("appearance_fonts_themes_icons_row").performClick()

        // Then its callback fires
        assertEquals(true, navigated)
    }
}
