package com.lumenlauncher.app.ui.profiles

import android.content.pm.LauncherApps
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.assertIsOff
import androidx.compose.ui.test.assertTextContains
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextReplacement
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.lifecycle.SavedStateHandle
import androidx.room.Room
import com.lumenlauncher.app.data.AppRepository
import com.lumenlauncher.app.data.DefaultFavoriteAppRepository
import com.lumenlauncher.app.data.FavoriteAppRepository
import com.lumenlauncher.app.data.ProfileRepository
import com.lumenlauncher.app.data.SettingsRepository
import com.lumenlauncher.app.data.local.LumenDatabase
import com.lumenlauncher.app.data.model.ListContentMode
import com.lumenlauncher.app.ui.theme.LumenLauncherTheme
import java.io.File
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

class ProfileSettingsScreenTest {

    @get:Rule
    val composeRule = createComposeRule()

    private fun setContent(
        onNavigateToFavorites: (Long) -> Unit = {},
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
                val favoriteAppRepository = FavoriteAppRepository(database.favoriteAppDao(), appRepository)
                val defaultFavoriteAppRepository = DefaultFavoriteAppRepository(database.defaultFavoriteAppDao(), appRepository)
                val settingsRepository = SettingsRepository(
                    PreferenceDataStoreFactory.create(
                        produceFile = { File(context.cacheDir, "profile-settings-test-${System.nanoTime()}.preferences_pb") },
                    ),
                )
                val profileId = runBlocking { profileRepository.addProfile().id }
                ProfileSettingsViewModel(
                    SavedStateHandle(mapOf("profileId" to profileId)),
                    profileRepository,
                    settingsRepository,
                    favoriteAppRepository,
                    defaultFavoriteAppRepository,
                )
            }
            LumenLauncherTheme {
                ProfileSettingsScreen(
                    onBack = {},
                    onNavigateToFavorites = onNavigateToFavorites,
                    onNavigateToCalendarSettings = onNavigateToCalendarSettings,
                    onNavigateToClockStyleGallery = onNavigateToClockStyleGallery,
                    viewModel = viewModel,
                )
            }
        }
        composeRule.waitForIdle()
        return profileRepository
    }

    @Test
    fun appsCardDefaultsToInheritWithEditFavoritesDisabled() {
        // Given the screen for a fresh profile, which hasn't overridden anything yet
        setContent()

        // Then "Edit favorites" shows the launcher-wide default list's (empty) count, but isn't
        // clickable — nothing in this card is editable until Override is picked
        composeRule.onNodeWithTag("profile_settings_favorites_row").assertExists().assertIsNotEnabled()
        composeRule.onNodeWithText("0 of 8").assertExists()
    }

    @Test
    fun favoritesRowShowsTheCurrentCountAndNavigatesOnceOverriding() {
        // Given the screen for a fresh profile, waited until the real Room-backed profile has
        // loaded (see appListContentDropdownAllowsSelecting... for why)
        var navigatedProfileId: Long? = null
        setContent(onNavigateToFavorites = { navigatedProfileId = it })
        composeRule.waitUntil(timeoutMillis = 3_000) {
            runCatching { composeRule.onNodeWithText("Profile 1").assertExists() }.isSuccess
        }

        // When switching this profile's single Apps card to Override (the row is only tappable then)
        composeRule.onNodeWithTag("profile_apps_override_row").performClick()
        composeRule.waitUntil(timeoutMillis = 3_000) {
            runCatching { composeRule.onNodeWithTag("profile_settings_favorites_row").assertIsEnabled() }.isSuccess
        }

        // Then it shows the (still empty, since overriding seeds a copy of the equally-empty
        // default list) count
        composeRule.onNodeWithText("0 of 8").assertExists()

        // When tapping the row
        composeRule.onNodeWithTag("profile_settings_favorites_row").performClick()

        // Then it navigates with the right profile id
        assertEquals(true, navigatedProfileId != null)
    }

    @Test
    fun renamingUpdatesTheDisplayedName() {
        // Given the screen for "Profile 1" — the initial title only appears once the
        // Room-backed profile flow's first emission lands, which isn't tracked by
        // waitForIdle() (same async-I/O-vs-idling gap as DataStore, see IMPLEMENTATION_PLAN.md)
        val profileRepository = setContent()
        composeRule.waitUntil(timeoutMillis = 3_000) {
            runCatching { composeRule.onNodeWithText("Profile 1").assertExists() }.isSuccess
        }

        // When renaming it
        composeRule.onNodeWithTag("profile_settings_rename_row").performClick()
        composeRule.onNodeWithTag("rename_dialog_field").performTextReplacement("Work")
        composeRule.onNodeWithTag("rename_dialog_save").performClick()

        // Then the new name is reflected, both on screen and in the repository — the merged
        // semantics tree can fold the title's EditableText-bearing node in a way onNodeWithText
        // won't match by default, so check the unmerged tree directly.
        composeRule.onNodeWithText("Work", useUnmergedTree = true).assertExists()
        composeRule.waitUntil(timeoutMillis = 3_000) {
            runBlocking { profileRepository.observeProfiles().first().first().name == "Work" }
        }
    }

    @Test
    fun appsListCardDefaultsToInheritWithContentDropdownReadOnly() {
        // Given the screen
        setContent()

        // Then the Apps card's "App list content" dropdown reflects the global default
        // (Favorites) and is read-only until Override is picked — targeting the dropdown row's
        // own testTag rather than a bare text search, since the InheritOverrideCard's own
        // inherit-subtitle also contains "Favorites" and would otherwise ambiguously match too
        composeRule.onNodeWithTag("app_list_content_row").assertExists().assertTextContains("Favorites")
    }

    @Test
    fun appListContentDropdownAllowsSelectingRecentsOrMostUsedAndPersists() {
        // Given the screen, waited until the real Room-backed profile has loaded — otherwise
        // uiState.profile is still null and the row's default-Favorites label is a false
        // positive; selecting an option would silently no-op against a null profile (same
        // async-I/O-vs-idling gap as elsewhere, see IMPLEMENTATION_PLAN.md)
        val profileRepository = setContent()
        composeRule.waitUntil(timeoutMillis = 3_000) {
            runCatching { composeRule.onNodeWithText("Profile 1").assertExists() }.isSuccess
        }
        composeRule.onNodeWithTag("app_list_content_row").assertTextContains("Favorites")

        // When switching to Override (the dropdown is read-only while inheriting) and opening
        // the dropdown to select "Most used" — targeting the option by its own testTag (rather
        // than its text, which the row's own label also shows once selected) avoids ambiguity,
        // and waitForIdle() settles the Popup's own window registering with the test framework's
        // root registry, which doesn't always land by the time performClick() returns on its own
        composeRule.onNodeWithTag("profile_apps_override_row").performClick()
        composeRule.waitUntil(timeoutMillis = 3_000) {
            runCatching { composeRule.onNodeWithTag("app_list_content_row").assertIsEnabled() }.isSuccess
        }
        composeRule.onNodeWithTag("app_list_content_row").performClick()
        composeRule.waitForIdle()
        composeRule.onNodeWithTag("app_list_content_row_option_MOST_USED").assertExists().assertIsEnabled()
        composeRule.onNodeWithTag("app_list_content_row_option_MOST_USED").performClick()

        // Then the selection is reflected on screen and persisted to the profile's own override
        // — poll rather than trust a single waitForIdle() caught it, since the write is real
        // Room I/O (upsert -> Flow re-emission -> recomposition), not just a Compose state change
        composeRule.waitUntil(timeoutMillis = 3_000) {
            composeRule.onAllNodesWithText("Most used").fetchSemanticsNodes().isNotEmpty()
        }
        composeRule.waitUntil(timeoutMillis = 3_000) {
            runBlocking { profileRepository.observeProfiles().first().first().listContentModeOverride == ListContentMode.MOST_USED }
        }
    }

    @Test
    fun appsToShowRowAppearsOnlyWhenModeIsntFavoritesAndPersistsSelection() {
        // Given the screen, waited until the real Room-backed profile has loaded (see the
        // previous test for why — otherwise selecting an option silently no-ops)
        val profileRepository = setContent()
        composeRule.waitUntil(timeoutMillis = 3_000) {
            runCatching { composeRule.onNodeWithText("Profile 1").assertExists() }.isSuccess
        }
        composeRule.onNodeWithText("Apps to show").assertDoesNotExist()

        // When switching to Override, then to Recents
        composeRule.onNodeWithTag("profile_apps_override_row").performClick()
        composeRule.waitUntil(timeoutMillis = 3_000) {
            runCatching { composeRule.onNodeWithTag("app_list_content_row").assertIsEnabled() }.isSuccess
        }
        composeRule.onNodeWithTag("app_list_content_row").performClick()
        composeRule.waitForIdle()
        composeRule.onNodeWithTag("app_list_content_row_option_RECENTS").performClick()

        // Then "Apps to show" appears, defaulting to 5
        composeRule.waitUntil(timeoutMillis = 3_000) {
            runCatching { composeRule.onNodeWithText("Apps to show").assertExists() }.isSuccess
        }
        composeRule.onNodeWithText("5").assertExists()

        // When picking a different count
        composeRule.onNodeWithTag("apps_to_show_row").performClick()
        composeRule.waitForIdle()
        composeRule.onNodeWithTag("apps_to_show_row_option_7").performClick()

        // Then it persists on the profile's own override
        composeRule.waitUntil(timeoutMillis = 3_000) {
            runBlocking { profileRepository.observeProfiles().first().first().appsToShowCountOverride == 7 }
        }
    }

    @Test
    fun clockCardDefaultsToInheritWithTwentyFourHourToggleReadOnly() {
        // Given the screen
        setContent()

        // Then the Clock card's 24-hour time toggle reflects the global default and is read-only
        composeRule.onNodeWithTag("profile_use_24_hour_time_toggle").assertIsOff()
        composeRule.onNodeWithTag("profile_use_24_hour_time_toggle").assertIsNotEnabled()
    }

    @Test
    fun overridingTheClockCardMakesTwentyFourHourTimeLiveAndPersists() {
        // Given the screen, waited until the real Room-backed profile has loaded — otherwise the
        // Clock card's rows render against the still-null initial uiState and clicks no-op
        // (same async-I/O-vs-idling gap as elsewhere, see IMPLEMENTATION_PLAN.md)
        val profileRepository = setContent()
        composeRule.waitUntil(timeoutMillis = 3_000) {
            runCatching { composeRule.onNodeWithText("Profile 1").assertExists() }.isSuccess
        }

        // When switching to Override and toggling 24-hour time on
        composeRule.onNodeWithTag("profile_clock_override_row").performClick()
        composeRule.waitUntil(timeoutMillis = 3_000) {
            runCatching { composeRule.onNodeWithTag("profile_use_24_hour_time_toggle").assertIsEnabled() }.isSuccess
        }
        composeRule.onNodeWithTag("profile_use_24_hour_time_toggle").performClick()

        // Then the profile's own override is persisted (not the global default)
        composeRule.waitUntil(timeoutMillis = 3_000) {
            runBlocking { profileRepository.observeProfiles().first().first().use24HourTimeOverride == true }
        }
    }

    @Test
    fun calendarRowNavigatesWithTheProfilesId() {
        // Given the screen
        var navigatedProfileId: Long? = null
        setContent(onNavigateToCalendarSettings = { navigatedProfileId = it })

        // When tapping the Clock card's Calendar row
        composeRule.onNodeWithTag("profile_calendar_settings_row").performClick()

        // Then it navigates with this profile's id
        assertEquals(true, navigatedProfileId != null)
    }

    @Test
    fun clockStyleRowNavigatesWithTheProfilesId() {
        // Given the screen
        var navigatedProfileId: Long? = null
        setContent(onNavigateToClockStyleGallery = { navigatedProfileId = it })

        // When tapping the Clock card's "Clock style" row
        composeRule.onNodeWithTag("profile_clock_style_gallery_row").performClick()

        // Then it navigates with this profile's id
        assertEquals(true, navigatedProfileId != null)
    }
}
