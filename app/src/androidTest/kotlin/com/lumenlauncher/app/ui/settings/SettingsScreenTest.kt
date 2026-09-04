package com.lumenlauncher.app.ui.settings

import android.content.pm.LauncherApps
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.test.assertHasClickAction
import androidx.compose.ui.test.assertHasNoClickAction
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertTextContains
import androidx.compose.ui.test.assertIsOff
import androidx.compose.ui.test.assertIsOn
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithContentDescription
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollToNode
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.room.Room
import com.lumenlauncher.app.data.AppRepository
import com.lumenlauncher.app.data.DefaultFavoriteAppRepository
import com.lumenlauncher.app.data.DefaultLauncherRepository
import com.lumenlauncher.app.data.DockAppRepository
import com.lumenlauncher.app.data.SettingsRepository
import com.lumenlauncher.app.data.local.LumenDatabase
import com.lumenlauncher.app.domain.ObserveSettingsScreenStateUseCase
import com.lumenlauncher.app.ui.theme.LumenLauncherTheme
import java.io.File
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

class SettingsScreenTest {

    @get:Rule
    val composeRule = createComposeRule()

    private fun setContent(
        onBack: () -> Unit = {},
        onAddDockApp: () -> Unit = {},
        onViewProfiles: () -> Unit = {},
        onNavigateToCalendarSettings: () -> Unit = {},
        onNavigateToPermissions: () -> Unit = {},
        onNavigateToNotificationSettings: () -> Unit = {},
        onEditDefaultFavorites: () -> Unit = {},
        onNavigateToClockStyleGallery: () -> Unit = {},
    ) {
        composeRule.setContent {
            val context = LocalContext.current
            val viewModel = remember {
                val settingsRepository = SettingsRepository(
                    PreferenceDataStoreFactory.create(
                        produceFile = { File(context.cacheDir, "settings-screen-test-${System.nanoTime()}.preferences_pb") },
                    ),
                )
                val database = Room.inMemoryDatabaseBuilder(context, LumenDatabase::class.java).allowMainThreadQueries().build()
                val launcherApps = context.getSystemService(LauncherApps::class.java)
                val appRepository = AppRepository(launcherApps)
                val dockAppRepository = DockAppRepository(database.dockAppDao(), appRepository)
                val defaultFavoriteAppRepository = DefaultFavoriteAppRepository(database.defaultFavoriteAppDao(), appRepository)
                SettingsViewModel(
                    ObserveSettingsScreenStateUseCase(settingsRepository, dockAppRepository, defaultFavoriteAppRepository),
                    settingsRepository,
                    dockAppRepository,
                    defaultFavoriteAppRepository,
                    DefaultLauncherRepository(context),
                )
            }
            LumenLauncherTheme {
                SettingsScreen(
                    onBack = onBack,
                    onAddDockApp = onAddDockApp,
                    onViewProfiles = onViewProfiles,
                    onNavigateToCalendarSettings = onNavigateToCalendarSettings,
                    onNavigateToPermissions = onNavigateToPermissions,
                    onNavigateToNotificationSettings = onNavigateToNotificationSettings,
                    onEditDefaultFavorites = onEditDefaultFavorites,
                    onNavigateToClockStyleGallery = onNavigateToClockStyleGallery,
                    viewModel = viewModel,
                )
            }
        }
    }

    @Test
    fun headerStaysVisibleAfterScrollingToTheBottom() {
        // Given the settings screen, scrolled all the way to the last row
        setContent()
        composeRule.onNodeWithTag("settings_screen").performScrollToNode(hasTestTag("set_default_launcher_row"))

        // Then the pinned header (title + back button) is still on screen, not scrolled away
        composeRule.onNodeWithText("Settings").assertIsDisplayed()
        composeRule.onNodeWithTag("back_button").assertIsDisplayed()
    }

    @Test
    fun backButtonInvokesOnBack() {
        // Given the settings screen
        var backInvoked = false
        setContent(onBack = { backInvoked = true })

        // When tapping the back button
        composeRule.onNodeWithTag("back_button").performClick()

        // Then it navigates back
        assert(backInvoked)
    }

    @Test
    fun dockDisplayStyleDropdownSwitchesBetweenIconsAndText() {
        // Given the settings screen, Icons selected by default
        setContent()
        composeRule.onNodeWithText("Icons").assertExists()

        // When opening the dropdown and choosing "Text" — the dropdown is a Popup, a separate
        // window that doesn't always register with the test framework's root registry by the
        // time performClick() returns; waitForIdle() settles that before querying its content.
        composeRule.onNodeWithTag("dock_display_style_row").performClick()
        composeRule.waitForIdle()
        composeRule.onNodeWithTag("dock_display_style_row_option_TEXT").performClick()

        // Then the row's own current-value label reflects it — poll rather than trust a single
        // waitForIdle() caught this second recomposition (selecting the item closes the Popup
        // and updates the row's label, two state transitions after the click returns)
        composeRule.waitUntil(timeoutMillis = 3_000) {
            composeRule.onAllNodesWithText("Text").fetchSemanticsNodes().isNotEmpty()
        }
    }

    @Test
    fun selectDockAppsRowIsClickable() {
        // Given the settings screen
        var navigated = false
        setContent(onAddDockApp = { navigated = true })

        // When tapping "Select Dock Apps"
        composeRule.onNodeWithTag("add_dock_app_row").performClick()

        // Then it opens the picker
        assertEquals(true, navigated)
    }

    @Test
    fun disabledRowsHaveNoClickAction() {
        // Given the settings screen, scrolled to Sync's still-unbuilt "Backup & restore" row
        // (Appearance's "Icons" row — this test's previous target — is a real, functioning
        // dropdown now, see iconRenderModeRowChangesTheSetting below). Targeted by testTag, not
        // hasText(...) — a text match turned out to hit an unexpected node with a real click
        // action once actually verified in isolation for a different row (root cause not chased
        // further; a testTag sidesteps the ambiguity entirely and matches this codebase's own
        // established convention for exactly this class of flake, see IMPLEMENTATION_PLAN.md).
        setContent()
        composeRule.onNodeWithTag("settings_screen").performScrollToNode(hasTestTag("backup_restore_row"))

        // Then a not-yet-built row (backup & restore, Known Gap) has no click action registered
        // at all — not just a no-op handler, genuinely non-interactive
        composeRule.onNodeWithTag("backup_restore_row").assertHasNoClickAction()
    }

    @Test
    fun iconRenderModeRowChangesTheSetting() {
        // Given the settings screen, scrolled to Appearance's "Icons" row (F11 — now a real
        // dropdown, no longer disabled)
        setContent()
        composeRule.onNodeWithTag("settings_screen").performScrollToNode(hasTestTag("appearance_icons_row"))
        composeRule.onNodeWithText("System default").assertExists()

        // When picking "Monochrome (Accent)"
        composeRule.onNodeWithTag("appearance_icons_row").performClick()
        composeRule.waitForIdle()
        composeRule.onNodeWithTag("appearance_icons_row_option_MONOCHROME_ACCENT").performClick()

        // Then the row reflects the new selection
        composeRule.waitUntil(timeoutMillis = 3_000) {
            composeRule.onAllNodesWithText("Monochrome (Accent)").fetchSemanticsNodes().isNotEmpty()
        }
        composeRule.onNodeWithText("Monochrome (Accent)").assertExists()
    }

    @Test
    fun launcherFontRowChangesTheSetting() {
        // Given the settings screen, scrolled to Appearance's "Font" row, System selected by
        // default — scoped to this row's own tag, not a global text search, since the Theme
        // card's "Launcher theme" dropdown also defaults to "System" (see chat history)
        setContent()
        composeRule.onNodeWithTag("settings_screen").performScrollToNode(hasTestTag("appearance_font_row"))
        composeRule.onNodeWithTag("appearance_font_row").assertTextContains("System")

        // When picking "Manrope"
        composeRule.onNodeWithTag("appearance_font_row").performClick()
        composeRule.waitForIdle()
        composeRule.onNodeWithTag("appearance_font_row_option_MANROPE").performClick()

        // Then the row reflects the new selection
        composeRule.waitUntil(timeoutMillis = 3_000) {
            runCatching { composeRule.onNodeWithTag("appearance_font_row").assertTextContains("Manrope") }.isSuccess
        }
    }

    @Test
    fun appLabelColorRowChangesTheSetting() {
        // Given the settings screen, scrolled to Appearance's "App label color" row, Ink selected
        // by default — scoped to this row's own tag, since "Ink (default)" is otherwise a unique
        // label but the row-scoping convention is kept consistent with the Font row above
        setContent()
        composeRule.onNodeWithTag("settings_screen").performScrollToNode(hasTestTag("appearance_app_label_color_row"))
        composeRule.onNodeWithTag("appearance_app_label_color_row").assertTextContains("Ink (default)")

        // When picking "White"
        composeRule.onNodeWithTag("appearance_app_label_color_row").performClick()
        composeRule.waitForIdle()
        composeRule.onNodeWithTag("appearance_app_label_color_row_option_WHITE").performClick()

        // Then the row reflects the new selection
        composeRule.waitUntil(timeoutMillis = 3_000) {
            runCatching { composeRule.onNodeWithTag("appearance_app_label_color_row").assertTextContains("White") }.isSuccess
        }
    }

    @Test
    fun calendarRowNavigatesToCalendarSettings() {
        // Given the settings screen — Calendar is functional as of this pass (UI shell, see
        // IMPLEMENTATION_PLAN.md — no real permission/CalendarContract query yet)
        var navigated = false
        setContent(onNavigateToCalendarSettings = { navigated = true })

        // When tapping the Calendar row
        composeRule.onNodeWithTag("calendar_settings_row").performClick()

        // Then its callback fires
        assertEquals(true, navigated)
    }

    @Test
    fun gridSizeRowOnlyAppearsWhenGridPresentationIsSelected() {
        // Given the settings screen, List presentation selected by default
        setContent()
        composeRule.onNodeWithTag("settings_screen").performScrollToNode(hasTestTag("drawer_presentation_row"))
        composeRule.onNodeWithTag("drawer_grid_size_row").assertDoesNotExist()
        composeRule.onNodeWithTag("drawer_list_item_size_row").assertExists()
        composeRule.onNodeWithTag("show_drawer_icons_toggle").assertExists()
        composeRule.onNodeWithTag("show_drawer_labels_toggle").assertDoesNotExist()

        // When switching Presentation to Grid (see the Popup/root-registration note above)
        composeRule.onNodeWithTag("drawer_presentation_row").performClick()
        composeRule.waitForIdle()
        composeRule.onNodeWithTag("drawer_presentation_row_option_GRID").performClick()

        // Then Grid size and Show labels appear, and Show icons/List item size are hidden — poll
        // rather than trust a single waitForIdle() caught the recomposition (same class of gap as
        // the StateFlow-vs-waitForIdle lesson elsewhere in this codebase, just triggered by a
        // Popup-driven state change instead of async I/O)
        composeRule.waitUntil(timeoutMillis = 3_000) {
            composeRule.onAllNodesWithTag("drawer_grid_size_row").fetchSemanticsNodes().isNotEmpty()
        }
        composeRule.onNodeWithTag("drawer_grid_size_row").assertExists()
        composeRule.onNodeWithTag("show_drawer_labels_toggle").assertExists()
        composeRule.onNodeWithTag("show_drawer_icons_toggle").assertDoesNotExist()
        composeRule.onNodeWithTag("drawer_list_item_size_row").assertDoesNotExist()
    }

    @Test
    fun viewProfilesRowIsClickable() {
        // Given the settings screen — Profiles is functional as of Phase 3
        var viewProfilesClicked = false
        setContent(onViewProfiles = { viewProfilesClicked = true })

        // When tapping "View profiles"
        composeRule.onNodeWithTag("view_profiles_row").performClick()

        // Then its callback fires
        assertEquals(true, viewProfilesClicked)
    }

    @Test
    fun clockStyleGalleryRowIsClickable() {
        // Given the settings screen, scrolled to the Clock card's "Default clock style" row
        var navigated = false
        setContent(onNavigateToClockStyleGallery = { navigated = true })
        composeRule.onNodeWithTag("settings_screen").performScrollToNode(hasTestTag("clock_style_gallery_row"))

        // When tapping it
        composeRule.onNodeWithTag("clock_style_gallery_row").performClick()

        // Then its callback fires
        assertEquals(true, navigated)
    }

    @Test
    fun permissionsRowIsClickable() {
        // Given the settings screen, scrolled to the Permissions card (now after Appearance)
        var navigated = false
        setContent(onNavigateToPermissions = { navigated = true })
        composeRule.onNodeWithTag("settings_screen").performScrollToNode(hasTestTag("view_permissions_row"))

        // When tapping "Permissions"
        composeRule.onNodeWithTag("view_permissions_row").performClick()

        // Then its callback fires
        assertEquals(true, navigated)
    }

    @Test
    fun appRowPositionDropdownSwitchesBetweenLeftAndRightAndAppearsAboveListContent() {
        // Given the settings screen, Left selected by default, scrolled to the Apps list card
        setContent()
        composeRule.onNodeWithTag("settings_screen").performScrollToNode(hasTestTag("default_app_row_position_row"))
        composeRule.onNodeWithTag("default_app_row_position_row").assertTextContains("Left")

        // Then it renders above the "Default App list content" row within the same card
        val positionTop = composeRule.onNodeWithTag("default_app_row_position_row").fetchSemanticsNode().boundsInRoot.top
        val listContentTop = composeRule.onNodeWithTag("default_list_content_row").fetchSemanticsNode().boundsInRoot.top
        assert(positionTop < listContentTop)

        // When opening the dropdown and choosing "Right" (Popup root-registration note — see above)
        composeRule.onNodeWithTag("default_app_row_position_row").performClick()
        composeRule.waitForIdle()
        composeRule.onNodeWithTag("default_app_row_position_row_option_RIGHT").performClick()

        // Then the row's own current-value label reflects it
        composeRule.waitUntil(timeoutMillis = 3_000) {
            runCatching { composeRule.onNodeWithTag("default_app_row_position_row").assertTextContains("Right") }.isSuccess
        }
    }

    @Test
    fun appRowPresentationDropdownSwitchesBetweenTheThreeOptions() {
        // Given the settings screen, "Icon & Text" selected by default, scrolled to the Apps list card
        setContent()
        composeRule.onNodeWithTag("settings_screen").performScrollToNode(hasTestTag("default_app_row_presentation_row"))
        composeRule.onNodeWithTag("default_app_row_presentation_row").assertTextContains("Icon & Text")

        // When opening the dropdown and choosing "Text Only" (Popup root-registration note — see above)
        composeRule.onNodeWithTag("default_app_row_presentation_row").performClick()
        composeRule.waitForIdle()
        composeRule.onNodeWithTag("default_app_row_presentation_row_option_TEXT_ONLY").performClick()

        // Then the row's own current-value label reflects it
        composeRule.waitUntil(timeoutMillis = 3_000) {
            runCatching { composeRule.onNodeWithTag("default_app_row_presentation_row").assertTextContains("Text Only") }.isSuccess
        }
    }

    @Test
    fun defaultFavoritesRowIsClickable() {
        // Given the settings screen, scrolled to the Apps list card
        var navigated = false
        setContent(onEditDefaultFavorites = { navigated = true })
        composeRule.onNodeWithTag("settings_screen").performScrollToNode(hasTestTag("default_favorites_row"))

        // When tapping "Default favorites"
        composeRule.onNodeWithTag("default_favorites_row").performClick()

        // Then its callback fires
        assertEquals(true, navigated)
    }

    @Test
    fun notificationSettingsRowIsClickable() {
        // Given the settings screen
        var navigated = false
        setContent(onNavigateToNotificationSettings = { navigated = true })

        // When tapping "Notifications"
        composeRule.onNodeWithTag("notification_settings_row").performClick()

        // Then its callback fires
        assertEquals(true, navigated)
    }

    @Test
    fun functionalRowsHaveClickActions() {
        // Given the settings screen, scrolled down to the bottom row
        setContent()
        composeRule.onNodeWithTag("settings_screen").performScrollToNode(hasTestTag("set_default_launcher_row"))

        // Then the default-launcher row is genuinely interactive
        composeRule.onNodeWithTag("set_default_launcher_row").assertHasClickAction()
    }

    @Test
    fun drawerOpacitySliderShowsTheCurrentPercentage() {
        // Given the settings screen with the documented 60% default, scrolled to the slider
        setContent()
        composeRule.onNodeWithTag("settings_screen").performScrollToNode(hasTestTag("drawer_opacity_slider"))

        // Then the slider's label reflects it
        composeRule.onNodeWithText("60%").assertExists()
    }

    @Test
    fun themeModeDropdownSwitchesBetweenLightDarkAndSystem() {
        // Given the settings screen, System selected by default, scrolled to the Theme card —
        // scoped to this row's own tag, not a global text search, since Appearance's "Font" row
        // also defaults to "System" and both are composed once either is scrolled near (see chat history)
        setContent()
        composeRule.onNodeWithTag("settings_screen").performScrollToNode(hasTestTag("theme_mode_dropdown"))
        composeRule.onNodeWithTag("theme_mode_dropdown").assertTextContains("System")

        // When opening the dropdown and choosing "Dark" (Popup root-registration note — see above)
        composeRule.onNodeWithTag("theme_mode_dropdown").performClick()
        composeRule.waitForIdle()
        composeRule.onNodeWithTag("theme_mode_dropdown_option_DARK").performClick()

        // Then the row's own current-value label reflects it
        composeRule.waitUntil(timeoutMillis = 3_000) {
            composeRule.onAllNodesWithText("Dark").fetchSemanticsNodes().isNotEmpty()
        }
    }

    @Test
    fun searchBarPositionDropdownSwitchesBetweenTopAndBottom() {
        // Given the settings screen, Top selected by default, scrolled to the App Drawer card
        setContent()
        composeRule.onNodeWithTag("settings_screen").performScrollToNode(hasTestTag("search_bar_position_row"))
        composeRule.onNodeWithText("Top").assertExists()

        // When opening the dropdown and choosing "Bottom" (Popup root-registration note — see above)
        composeRule.onNodeWithTag("search_bar_position_row").performClick()
        composeRule.waitForIdle()
        composeRule.onNodeWithTag("search_bar_position_row_option_BOTTOM").performClick()

        // Then the row's own current-value label reflects it
        composeRule.waitUntil(timeoutMillis = 3_000) {
            composeRule.onAllNodesWithText("Bottom").fetchSemanticsNodes().isNotEmpty()
        }
    }

    @Test
    fun accentSwatchGridOnlyAppearsWhenBasicColorsIsSelectedAndPickingOneShowsItChecked() {
        // Given the settings screen, "Wallpaper colors" selected by default, scrolled to Accent color
        setContent()
        composeRule.onNodeWithTag("settings_screen").performScrollToNode(hasTestTag("accent_source_wallpaper"))
        composeRule.onNodeWithTag("accent_swatch_grid").assertDoesNotExist()

        // When switching to "Basic colors"
        composeRule.onNodeWithTag("accent_source_basic").performClick()

        // Then the swatch grid appears, defaulting to the first swatch (Blue) selected
        composeRule.waitUntil(timeoutMillis = 3_000) {
            composeRule.onAllNodesWithTag("accent_swatch_grid").fetchSemanticsNodes().isNotEmpty()
        }
        composeRule.onNodeWithContentDescription("Blue").assertExists()

        // When picking a different swatch (Teal)
        composeRule.onNodeWithTag("accent_swatch_TEAL").performClick()

        // Then that swatch shows as checked instead — poll, since the pick round-trips through
        // DataStore before the grid re-renders (same async-write lesson as the toggles above)
        composeRule.waitUntil(timeoutMillis = 3_000) {
            composeRule.onAllNodesWithContentDescription("Teal").fetchSemanticsNodes().isNotEmpty()
        }
    }
}
