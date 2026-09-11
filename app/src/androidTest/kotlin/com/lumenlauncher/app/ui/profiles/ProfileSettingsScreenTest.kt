package com.lumenlauncher.app.ui.profiles

import android.content.pm.LauncherApps
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.test.assertTextContains
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollToNode
import androidx.compose.ui.test.performTextReplacement
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.lifecycle.SavedStateHandle
import androidx.room.Room
import com.lumenlauncher.app.data.AppRepository
import com.lumenlauncher.app.data.DefaultFavoriteAppRepository
import com.lumenlauncher.app.data.DockAppRepository
import com.lumenlauncher.app.data.FavoriteAppRepository
import com.lumenlauncher.app.data.ProfileDockAppRepository
import com.lumenlauncher.app.data.ProfileRepository
import com.lumenlauncher.app.data.SettingsRepository
import com.lumenlauncher.app.data.local.LumenDatabase
import com.lumenlauncher.app.ui.theme.LumenLauncherTheme
import java.io.File
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

/**
 * The top-level per-profile screen is now a short nav list — Rename, three Inherit/Override
 * cards, and a row into each area's own settings screen. The detailed controls (position,
 * presentation, dock display style, favorites, …) are exercised by
 * `com.lumenlauncher.app.ui.settings.HomeAppsListSettingsScreenTest` /
 * `DockSettingsScreenTest`, which cover both the global and the profile-scoped instance.
 */
class ProfileSettingsScreenTest {

    @get:Rule
    val composeRule = createComposeRule()

    /** Set by [setContent] alongside the [ProfileRepository] it returns — tests that need to seed the global settings (e.g. calendar selection) read this directly rather than widening every existing `setContent()` call site's return type. */
    private lateinit var settingsRepository: SettingsRepository

    private fun setContent(
        onNavigateToAppsList: (Long) -> Unit = {},
        onNavigateToDockSettings: (Long) -> Unit = {},
        onNavigateToCalendarSettings: (Long) -> Unit = {},
        onNavigateToClockStyleGallery: (Long) -> Unit = {},
    ): ProfileRepository {
        lateinit var profileRepository: ProfileRepository
        composeRule.setContent {
            val context = LocalContext.current
            val viewModel = remember {
                val database = Room.inMemoryDatabaseBuilder(context, LumenDatabase::class.java).allowMainThreadQueries().build()
                val launcherApps = context.getSystemService(LauncherApps::class.java)
                profileRepository = ProfileRepository(database.profileDao())
                val appRepository = AppRepository(launcherApps)
                settingsRepository = SettingsRepository(
                    PreferenceDataStoreFactory.create(
                        produceFile = { File(context.cacheDir, "profile-settings-test-${System.nanoTime()}.preferences_pb") },
                    ),
                )
                val profileId = runBlocking { profileRepository.addProfile().id }
                ProfileSettingsViewModel(
                    SavedStateHandle(mapOf("profileId" to profileId)),
                    profileRepository,
                    settingsRepository,
                    FavoriteAppRepository(database.favoriteAppDao(), appRepository),
                    DefaultFavoriteAppRepository(database.defaultFavoriteAppDao(), appRepository),
                    ProfileDockAppRepository(database.profileDockAppDao(), appRepository),
                    DockAppRepository(database.dockAppDao(), appRepository),
                )
            }
            LumenLauncherTheme {
                ProfileSettingsScreen(
                    onBack = {},
                    onNavigateToAppsList = onNavigateToAppsList,
                    onNavigateToDockSettings = onNavigateToDockSettings,
                    onNavigateToCalendarSettings = onNavigateToCalendarSettings,
                    onNavigateToClockStyleGallery = onNavigateToClockStyleGallery,
                    viewModel = viewModel,
                )
            }
        }
        composeRule.waitForIdle()
        return profileRepository
    }

    private fun awaitProfileLoaded() {
        composeRule.waitUntil(timeoutMillis = 3_000) {
            composeRule.onAllNodesWithText("Profile 1").fetchSemanticsNodes().isNotEmpty()
        }
    }

    private fun scrollToRow(tag: String) {
        composeRule.onNodeWithTag("profile_settings_screen").performScrollToNode(hasTestTag(tag))
    }

    @Test
    fun renameCardRendersDirectlyUnderTheHeaderBeforeApps() {
        setContent()

        val renameTop = composeRule.onNodeWithTag("profile_settings_rename_row").fetchSemanticsNode().boundsInRoot.top
        val appsSectionTop = composeRule.onNodeWithText("APPS LIST").fetchSemanticsNode().boundsInRoot.top
        assert(renameTop < appsSectionTop)
    }

    @Test
    fun headerAndRenameSubtitleShowTheProfilesName() {
        setContent()
        awaitProfileLoaded()

        composeRule.onNodeWithTag("back_button").assertExists()
        composeRule.onNodeWithTag("profile_settings_rename_row").assertTextContains("Profile 1")
    }

    @Test
    fun renamingUpdatesTheDisplayedNameAndPersists() {
        val profileRepository = setContent()
        awaitProfileLoaded()

        composeRule.onNodeWithTag("profile_settings_rename_row").performClick()
        composeRule.onNodeWithTag("rename_dialog_field").performTextReplacement("Work")
        composeRule.onNodeWithTag("rename_dialog_save").performClick()

        composeRule.waitUntil(timeoutMillis = 3_000) {
            runCatching { composeRule.onNodeWithTag("profile_settings_rename_row").assertTextContains("Work") }.isSuccess
        }
        runBlocking { assertEquals("Work", profileRepository.observeProfiles().first().first().name) }
    }

    @Test
    fun appsListRowNavigatesWithTheProfilesId() {
        var navigatedProfileId: Long? = null
        setContent(onNavigateToAppsList = { navigatedProfileId = it })

        composeRule.onNodeWithTag("profile_apps_list_row").performClick()

        assertEquals(true, navigatedProfileId != null)
    }

    @Test
    fun dockSettingsRowNavigatesWithTheProfilesId() {
        var navigatedProfileId: Long? = null
        setContent(onNavigateToDockSettings = { navigatedProfileId = it })

        scrollToRow("profile_dock_settings_row")
        composeRule.onNodeWithTag("profile_dock_settings_row").performClick()

        assertEquals(true, navigatedProfileId != null)
    }

    @Test
    fun clockStyleAndCalendarRowsNavigateWithTheProfilesId() {
        var navigatedFromClock: Long? = null
        var navigatedFromCalendar: Long? = null
        setContent(
            onNavigateToClockStyleGallery = { navigatedFromClock = it },
            onNavigateToCalendarSettings = { navigatedFromCalendar = it },
        )

        scrollToRow("profile_clock_style_gallery_row")
        composeRule.onNodeWithTag("profile_clock_style_gallery_row").performClick()
        assertEquals(true, navigatedFromClock != null)

        scrollToRow("profile_calendar_settings_row")
        composeRule.onNodeWithTag("profile_calendar_settings_row").performClick()
        assertEquals(true, navigatedFromCalendar != null)
    }

    @Test
    fun overridingTheAppsCardPersistsTheFlag() {
        val profileRepository = setContent()
        awaitProfileLoaded()

        composeRule.onNodeWithTag("profile_apps_override_row").performClick()

        composeRule.waitUntil(timeoutMillis = 3_000) {
            runBlocking { profileRepository.observeProfiles().first().first().overrideApps }
        }
    }

    @Test
    fun overridingTheDockCardPersistsTheFlag() {
        val profileRepository = setContent()
        awaitProfileLoaded()

        scrollToRow("profile_dock_override_row")
        composeRule.onNodeWithTag("profile_dock_override_row").performClick()

        composeRule.waitUntil(timeoutMillis = 3_000) {
            runBlocking { profileRepository.observeProfiles().first().first().overrideDock }
        }
    }

    @Test
    fun overridingTheClockCardPersistsTheFlag() {
        val profileRepository = setContent()
        awaitProfileLoaded()

        scrollToRow("profile_clock_override_row")
        composeRule.onNodeWithTag("profile_clock_override_row").performClick()

        composeRule.waitUntil(timeoutMillis = 3_000) {
            runBlocking { profileRepository.observeProfiles().first().first().overrideClock }
        }
        // The "Clock & Calendar Style" row's subtitle then flips to "Overriding defaults · tap to
        // edit" (a plain `if (isOverridingClock)` branch — asserted at the unit level).
    }

    @Test
    fun dockRowSubtitleReflectsTheEffectiveDisplayStyle() {
        setContent()
        awaitProfileLoaded()

        // Not overriding -> inherits the global default (Icons)
        scrollToRow("profile_dock_settings_row")
        composeRule.onNodeWithTag("profile_dock_settings_row").assertTextContains("Icons", substring = true)
    }

    @Test
    fun calendarRowSubtitleShowsZeroSelectedWhenNothingHasBeenExplicitlyChosenYet() {
        // Given a fresh profile that isn't overriding, and the global selection never touched (`null` — "none decided", not "all")
        setContent()
        awaitProfileLoaded()
        scrollToRow("profile_calendar_settings_row")

        // Then the row shows a literal zero, not a device calendar total standing in for "all"
        composeRule.onNodeWithTag("profile_calendar_settings_row").assertTextContains("0 calendars selected", substring = true)
    }

    @Test
    fun calendarRowSubtitleReflectsTheGlobalSelectionWhileInheriting() {
        // Given 2 calendars explicitly selected globally, and this profile inheriting (not overriding)
        setContent()
        awaitProfileLoaded()
        runBlocking { settingsRepository.setSelectedCalendarIds(setOf("cal-1", "cal-2")) }
        scrollToRow("profile_calendar_settings_row")

        // Then the row reflects that inherited count and the all-day events visibility
        composeRule.waitUntil(timeoutMillis = 3_000) {
            runCatching {
                composeRule.onNodeWithTag("profile_calendar_settings_row").assertTextContains("2 calendars selected · All-day events visible", substring = true)
            }.isSuccess
        }
    }
}
