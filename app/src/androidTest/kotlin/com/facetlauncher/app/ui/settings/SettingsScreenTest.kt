package com.facetlauncher.app.ui.settings

import android.content.pm.LauncherApps
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.test.assertHasClickAction
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertTextContains
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollToNode
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.room.Room
import com.facetlauncher.app.data.AppRepository
import com.facetlauncher.app.data.DefaultFavoriteAppRepository
import com.facetlauncher.app.data.DefaultLauncherRepository
import com.facetlauncher.app.data.DockAppRepository
import com.facetlauncher.app.data.SettingsRepository
import com.facetlauncher.app.data.local.FacetDatabase
import com.facetlauncher.app.domain.ObserveSettingsScreenStateUseCase
import com.facetlauncher.app.ui.theme.FacetLauncherTheme
import java.io.File
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

class SettingsScreenTest {

    @get:Rule
    val composeRule = createComposeRule()

    private fun setContent(
        onBack: () -> Unit = {},
        onViewProfiles: () -> Unit = {},
        onNavigateToAppearance: () -> Unit = {},
        onNavigateToClockStyleGallery: () -> Unit = {},
        onNavigateToCalendarSettings: () -> Unit = {},
        onNavigateToDockSettings: () -> Unit = {},
        onNavigateToHomeAppsListSettings: () -> Unit = {},
        onNavigateToAppDrawerSettings: () -> Unit = {},
        onNavigateToNotificationSettings: () -> Unit = {},
        onNavigateToPermissions: () -> Unit = {},
        onNavigateToBackupRestore: () -> Unit = {},
    ): SettingsRepository {
        lateinit var settingsRepository: SettingsRepository
        composeRule.setContent {
            val context = LocalContext.current
            val viewModel = remember {
                settingsRepository = SettingsRepository(
                    PreferenceDataStoreFactory.create(
                        produceFile = { File(context.cacheDir, "settings-screen-test-${System.nanoTime()}.preferences_pb") },
                    ),
                )
                val database = Room.inMemoryDatabaseBuilder(context, FacetDatabase::class.java).allowMainThreadQueries().build()
                val appRepository = AppRepository(context.getSystemService(LauncherApps::class.java))
                val dockAppRepository = DockAppRepository(database.dockAppDao(), appRepository)
                val defaultFavoriteAppRepository = DefaultFavoriteAppRepository(database.defaultFavoriteAppDao(), appRepository)
                SettingsViewModel(
                    ObserveSettingsScreenStateUseCase(settingsRepository, dockAppRepository, defaultFavoriteAppRepository),
                    DefaultLauncherRepository(context),
                )
            }
            FacetLauncherTheme {
                SettingsScreen(
                    onBack = onBack,
                    onViewProfiles = onViewProfiles,
                    onNavigateToAppearance = onNavigateToAppearance,
                    onNavigateToClockStyleGallery = onNavigateToClockStyleGallery,
                    onNavigateToCalendarSettings = onNavigateToCalendarSettings,
                    onNavigateToDockSettings = onNavigateToDockSettings,
                    onNavigateToHomeAppsListSettings = onNavigateToHomeAppsListSettings,
                    onNavigateToAppDrawerSettings = onNavigateToAppDrawerSettings,
                    onNavigateToNotificationSettings = onNavigateToNotificationSettings,
                    onNavigateToPermissions = onNavigateToPermissions,
                    onNavigateToBackupRestore = onNavigateToBackupRestore,
                    viewModel = viewModel,
                )
            }
        }
        composeRule.waitForIdle()
        return settingsRepository
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
    fun backupRestoreRowIsClickable() {
        // Given the settings screen, scrolled to the "Backup & restore" row (now grouped under
        // SYSTEM alongside Permissions and Set as default launcher)
        var navigated = false
        setContent(onNavigateToBackupRestore = { navigated = true })
        composeRule.onNodeWithTag("settings_screen").performScrollToNode(hasTestTag("backup_restore_row"))

        // When tapping it
        composeRule.onNodeWithTag("backup_restore_row").performClick()

        // Then its callback fires
        assertEquals(true, navigated)
    }

    @Test
    fun appearanceRowIsClickableAndShowsAGenericSubtitle() {
        // Given the settings screen, scrolled to the "Launcher Appearance" row — the theme/accent/
        // icons/font block lives on its own screen, reached through this single summary row
        // instead of inline controls. The subtitle is a static, generic description rather than
        // the live theme/font values (see chat history — the dynamic subtitle wasn't wanted here).
        var navigated = false
        setContent(onNavigateToAppearance = { navigated = true })
        composeRule.onNodeWithTag("settings_screen").performScrollToNode(hasTestTag("appearance_row"))
        composeRule.onNodeWithTag("appearance_row").assertTextContains("Theme, accent, icons, fonts", substring = true)

        // When tapping it
        composeRule.onNodeWithTag("appearance_row").performClick()

        // Then its callback fires
        assertEquals(true, navigated)
    }

    @Test
    fun dockRowIsClickableAndReflectsDockAppCount() {
        // Given the settings screen, scrolled to the "Dock" row under HOME & APPS — Dock and Home
        // Apps List used to be one combined row/screen; they're now separate (see chat history).
        var navigated = false
        setContent(onNavigateToDockSettings = { navigated = true })
        composeRule.onNodeWithTag("settings_screen").performScrollToNode(hasTestTag("dock_settings_row"))
        composeRule.onNodeWithTag("dock_settings_row").assertTextContains("0 Dock Apps", substring = true)

        // When tapping it
        composeRule.onNodeWithTag("dock_settings_row").performClick()

        // Then its callback fires
        assertEquals(true, navigated)
    }

    @Test
    fun homeAppsListRowIsClickableAndReflectsListContentCount() {
        // Given the settings screen, scrolled to the "Home Apps List" row under HOME & APPS.
        // Defaults: Favorites content mode with 0 default favorites.
        var navigated = false
        setContent(onNavigateToHomeAppsListSettings = { navigated = true })
        composeRule.onNodeWithTag("settings_screen").performScrollToNode(hasTestTag("home_apps_list_settings_row"))
        composeRule.onNodeWithTag("home_apps_list_settings_row").assertTextContains("0 Favorites", substring = true)

        // When tapping it
        composeRule.onNodeWithTag("home_apps_list_settings_row").performClick()

        // Then its callback fires
        assertEquals(true, navigated)
    }

    @Test
    fun appDrawerRowIsClickable() {
        // Given the settings screen, scrolled to the "App Drawer" row under HOME & APPS — its
        // seven controls used to be inline on this list; they now live on their own screen.
        var navigated = false
        setContent(onNavigateToAppDrawerSettings = { navigated = true })
        composeRule.onNodeWithTag("settings_screen").performScrollToNode(hasTestTag("app_drawer_settings_row"))

        // When tapping it
        composeRule.onNodeWithTag("app_drawer_settings_row").performClick()

        // Then its callback fires
        assertEquals(true, navigated)
    }

    @Test
    fun notificationSettingsRowIsClickableAndReflectsEnabledStateWithCrossCuttingScope() {
        // Given the settings screen — notification dots are enabled by default (Dot badge style),
        // and the row's subtitle states the cross-cutting scope explicitly (see chat history: dots
        // render on Dock, Home Apps List, and App Drawer icons alike, not just one surface), using
        // the actual configured badge style rather than a generic "dots/badges" placeholder.
        var navigated = false
        setContent(onNavigateToNotificationSettings = { navigated = true })
        composeRule.onNodeWithTag("settings_screen").performScrollToNode(hasTestTag("notification_settings_row"))
        composeRule.onNodeWithTag("notification_settings_row").assertTextContains("Enabled · Dots on Dock, Home & Drawer")

        // When tapping "Notifications"
        composeRule.onNodeWithTag("notification_settings_row").performClick()

        // Then its callback fires
        assertEquals(true, navigated)
    }

    @Test
    fun calendarRowNavigatesToCalendarSettings() {
        // Given the settings screen
        var navigated = false
        setContent(onNavigateToCalendarSettings = { navigated = true })

        // When tapping the Calendar row
        composeRule.onNodeWithTag("calendar_settings_row").performClick()

        // Then its callback fires
        assertEquals(true, navigated)
    }

    @Test
    fun calendarRowSubtitleShowsZeroSelectedWhenNothingHasBeenExplicitlyChosenYet() {
        // Given a fresh install — selectedCalendarIds is `null` (never touched), which is "none decided", not "all"
        setContent()
        composeRule.onNodeWithTag("settings_screen").performScrollToNode(hasTestTag("calendar_settings_row"))

        // Then the row shows a literal zero, not a device calendar total standing in for "all"
        composeRule.onNodeWithTag("calendar_settings_row").assertTextContains("0 calendars selected", substring = true)
    }

    @Test
    fun calendarRowSubtitleReflectsAnExplicitSelection() {
        // Given 2 calendars explicitly selected
        val settingsRepository = setContent()
        runBlocking { settingsRepository.setSelectedCalendarIds(setOf("cal-1", "cal-2")) }
        composeRule.onNodeWithTag("settings_screen").performScrollToNode(hasTestTag("calendar_settings_row"))

        // Then the row reflects that exact count and the all-day events visibility
        composeRule.waitUntil(timeoutMillis = 3_000) {
            runCatching {
                composeRule.onNodeWithTag("calendar_settings_row").assertTextContains("2 calendars selected · All-day events visible", substring = true)
            }.isSuccess
        }
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
        // Given the settings screen, scrolled to the Clock & Calendar card's style row
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
        // Given the settings screen, scrolled to the Permissions row (now under SYSTEM)
        var navigated = false
        setContent(onNavigateToPermissions = { navigated = true })
        composeRule.onNodeWithTag("settings_screen").performScrollToNode(hasTestTag("view_permissions_row"))

        // When tapping "Permissions"
        composeRule.onNodeWithTag("view_permissions_row").performClick()

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
    fun changeWallpaperRowIsInteractive() {
        // Given the settings screen, scrolled to the "Change wallpaper" row — same category as
        // "set_default_launcher_row" above, a system-intent launch that isn't itself verifiable
        // from a Compose test.
        setContent()
        composeRule.onNodeWithTag("settings_screen").performScrollToNode(hasTestTag("change_wallpaper_row"))

        // Then it's genuinely interactive
        composeRule.onNodeWithTag("change_wallpaper_row").assertHasClickAction()
    }
}
