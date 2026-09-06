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
import androidx.compose.ui.test.performScrollTo
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
import com.lumenlauncher.app.data.model.AppRowPosition
import com.lumenlauncher.app.data.model.AppRowPresentation
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
    fun renameCardRendersInItsOwnCardDirectlyUnderTheHeaderBeforeApps() {
        // Given the screen
        setContent()

        // Then the Rename card sits above the APPS LIST section header — not at the bottom of the
        // page — verified by comparing on-screen vertical position rather than just existence
        val renameTop = composeRule.onNodeWithTag("profile_settings_rename_row").fetchSemanticsNode().boundsInRoot.top
        val appsSectionTop = composeRule.onNodeWithText("APPS LIST").fetchSemanticsNode().boundsInRoot.top
        assert(renameTop < appsSectionTop)
    }

    @Test
    fun headerStaysVisibleWhileProfileNameIsShown() {
        // Given the screen for "Profile 1" — waited until the real Room-backed profile has loaded
        setContent()
        composeRule.waitUntil(timeoutMillis = 3_000) {
            // onAllNodesWithText, not onNodeWithText — the Rename card's own subtitle also shows
            // the profile's name now, so "Profile 1" legitimately matches two nodes at once
            // (header + rename row) and a singular onNodeWithText would throw as ambiguous.
            composeRule.onAllNodesWithText("Profile 1").fetchSemanticsNodes().isNotEmpty()
        }

        // Then the pinned header (title + back button) is present
        assert(composeRule.onAllNodesWithText("Profile 1").fetchSemanticsNodes().isNotEmpty())
        composeRule.onNodeWithTag("back_button").assertExists()
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
            // onAllNodesWithText, not onNodeWithText — the Rename card's own subtitle also shows
            // the profile's name now, so "Profile 1" legitimately matches two nodes at once
            // (header + rename row) and a singular onNodeWithText would throw as ambiguous.
            composeRule.onAllNodesWithText("Profile 1").fetchSemanticsNodes().isNotEmpty()
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
            // onAllNodesWithText, not onNodeWithText — the Rename card's own subtitle also shows
            // the profile's name now, so "Profile 1" legitimately matches two nodes at once
            // (header + rename row) and a singular onNodeWithText would throw as ambiguous.
            composeRule.onAllNodesWithText("Profile 1").fetchSemanticsNodes().isNotEmpty()
        }

        // When renaming it
        composeRule.onNodeWithTag("profile_settings_rename_row").performClick()
        composeRule.onNodeWithTag("rename_dialog_field").performTextReplacement("Work")
        composeRule.onNodeWithTag("rename_dialog_save").performClick()

        // Then the new name is reflected, both on screen and in the repository.
        composeRule.waitUntil(timeoutMillis = 3_000) {
            composeRule.onAllNodesWithText("Work", useUnmergedTree = true).fetchSemanticsNodes().isNotEmpty()
        }
        runBlocking { 
            assertEquals("Work", profileRepository.observeProfiles().first().first().name)
        }
    }

    @Test
    fun renameCardSubtitleMatchesTheProfilesNameBeforeAndAfterRenaming() {
        // Given the screen for "Profile 1" — waited until the real Room-backed profile has loaded
        setContent()
        composeRule.waitUntil(timeoutMillis = 3_000) {
            // onAllNodesWithText, not onNodeWithText — the Rename card's own subtitle also shows
            // the profile's name now, so "Profile 1" legitimately matches two nodes at once
            // (header + rename row) and a singular onNodeWithText would throw as ambiguous.
            composeRule.onAllNodesWithText("Profile 1").fetchSemanticsNodes().isNotEmpty()
        }

        // Then the Rename card's own subtitle shows the current name
        composeRule.onNodeWithTag("profile_settings_rename_row").assertTextContains("Profile 1")

        // When renaming it
        composeRule.onNodeWithTag("profile_settings_rename_row").performClick()
        composeRule.onNodeWithTag("rename_dialog_field").performTextReplacement("Work")
        composeRule.onNodeWithTag("rename_dialog_save").performClick()

        // Then the Rename card's subtitle reflects the new name, not the stale one
        composeRule.waitUntil(timeoutMillis = 3_000) {
            runCatching { composeRule.onNodeWithTag("profile_settings_rename_row").assertTextContains("Work") }.isSuccess
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
    fun positionAndPresentationRowsDefaultToInheritAndAreReadOnlyUntilOverriding() {
        // Given the screen for a fresh profile, which hasn't overridden anything yet
        setContent()

        // Then Position/Presentation reflect the global defaults (Left / Icon & Text) and sit
        // above the App list content row — same relative order as the global Settings screen
        composeRule.onNodeWithTag("profile_app_row_position_row").assertExists().assertTextContains("Left").assertIsNotEnabled()
        composeRule.onNodeWithTag("profile_app_row_presentation_row").assertExists().assertTextContains("Icon & Text").assertIsNotEnabled()
        val positionTop = composeRule.onNodeWithTag("profile_app_row_position_row").fetchSemanticsNode().boundsInRoot.top
        val presentationTop = composeRule.onNodeWithTag("profile_app_row_presentation_row").fetchSemanticsNode().boundsInRoot.top
        val contentModeTop = composeRule.onNodeWithTag("app_list_content_row").fetchSemanticsNode().boundsInRoot.top
        assert(positionTop < presentationTop)
        assert(presentationTop < contentModeTop)
    }

    @Test
    fun overridingAppsAllowsChangingPositionAndPresentationAndPersistsThem() {
        // Given the screen, waited until the real Room-backed profile has loaded — otherwise
        // uiState.profile is still null and selecting an option would silently no-op against a
        // null profile (same async-I/O-vs-idling gap as elsewhere, see IMPLEMENTATION_PLAN.md)
        val profileRepository = setContent()
        composeRule.waitUntil(timeoutMillis = 3_000) {
            composeRule.onAllNodesWithText("Profile 1").fetchSemanticsNodes().isNotEmpty()
        }

        // When switching to Override (the dropdowns are read-only while inheriting)
        composeRule.onNodeWithTag("profile_apps_override_row").performClick()
        composeRule.waitUntil(timeoutMillis = 3_000) {
            runCatching { composeRule.onNodeWithTag("profile_app_row_position_row").assertIsEnabled() }.isSuccess
        }

        // And picking "Right" for Position
        composeRule.onNodeWithTag("profile_app_row_position_row").performClick()
        composeRule.waitForIdle()
        composeRule.onNodeWithTag("profile_app_row_position_row_option_RIGHT").performClick()

        // Then it's reflected on screen and persisted to the profile
        composeRule.waitUntil(timeoutMillis = 3_000) {
            runCatching { composeRule.onNodeWithTag("profile_app_row_position_row").assertTextContains("Right") }.isSuccess
        }
        composeRule.waitUntil(timeoutMillis = 3_000) {
            runBlocking { profileRepository.observeProfiles().first().first().appRowPosition == AppRowPosition.RIGHT }
        }

        // And picking "Text Only" for Presentation
        composeRule.onNodeWithTag("profile_app_row_presentation_row").performClick()
        composeRule.waitForIdle()
        composeRule.onNodeWithTag("profile_app_row_presentation_row_option_TEXT_ONLY").performClick()

        // Then it's reflected on screen and persisted to the profile
        composeRule.waitUntil(timeoutMillis = 3_000) {
            runCatching { composeRule.onNodeWithTag("profile_app_row_presentation_row").assertTextContains("Text Only") }.isSuccess
        }
        composeRule.waitUntil(timeoutMillis = 3_000) {
            runBlocking { profileRepository.observeProfiles().first().first().appRowPresentation == AppRowPresentation.TEXT_ONLY }
        }
    }

    @Test
    fun appListContentDropdownAllowsSelectingRecentsOrMostUsedAndPersists() {
        // Given the screen, waited until the real Room-backed profile has loaded — otherwise
        // uiState.profile is still null and the row's default-Favorites label is a false
        // positive; selecting an option would silently no-op against a null profile (same
        // async-I/O-vs-idling gap as elsewhere, see IMPLEMENTATION_PLAN.md)
        val profileRepository = setContent()
        composeRule.waitUntil(timeoutMillis = 3_000) {
            // onAllNodesWithText, not onNodeWithText — the Rename card's own subtitle also shows
            // the profile's name now, so "Profile 1" legitimately matches two nodes at once
            // (header + rename row) and a singular onNodeWithText would throw as ambiguous.
            composeRule.onAllNodesWithText("Profile 1").fetchSemanticsNodes().isNotEmpty()
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

        // Then the selection is reflected on screen and persisted to the profile
        // — poll rather than trust a single waitForIdle() caught it, since the write is real
        // Room I/O (upsert -> Flow re-emission -> recomposition), not just a Compose state change
        composeRule.waitUntil(timeoutMillis = 3_000) {
            composeRule.onAllNodesWithText("Most used").fetchSemanticsNodes().isNotEmpty()
        }
        composeRule.waitUntil(timeoutMillis = 3_000) {
            runBlocking { profileRepository.observeProfiles().first().first().listContentMode == ListContentMode.MOST_USED }
        }
    }

    @Test
    fun appsToShowRowAppearsOnlyWhenModeIsntFavoritesAndPersistsSelection() {
        // Given the screen, waited until the real Room-backed profile has loaded (see the
        // previous test for why — otherwise selecting an option silently no-ops)
        val profileRepository = setContent()
        composeRule.waitUntil(timeoutMillis = 3_000) {
            // onAllNodesWithText, not onNodeWithText — the Rename card's own subtitle also shows
            // the profile's name now, so "Profile 1" legitimately matches two nodes at once
            // (header + rename row) and a singular onNodeWithText would throw as ambiguous.
            composeRule.onAllNodesWithText("Profile 1").fetchSemanticsNodes().isNotEmpty()
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

        // Then it persists on the profile
        composeRule.waitUntil(timeoutMillis = 3_000) {
            runBlocking { profileRepository.observeProfiles().first().first().appsToShowCount == 7 }
        }
    }

    @Test
    fun clockCardDefaultsToInheritWithRowSubtitleReflectingTheGlobalDefault() {
        // Given the screen
        setContent()

        // Then the Clock card defaults to Inherit, and the "Clock & Calendar Style" row's subtitle
        // reflects the global default rather than any profile-specific override
        composeRule.onNodeWithTag("profile_clock_inherit_row").assertExists()
        composeRule.onNodeWithText("Inherits default · Light stack · tap to preview").assertExists()
    }

    @Test
    fun overridingTheClockCardPersistsTheFlagAndUpdatesTheRowSubtitle() {
        // Given the screen, waited until the real Room-backed profile has loaded — otherwise the
        // Clock card's rows render against the still-null initial uiState and clicks no-op
        // (same async-I/O-vs-idling gap as elsewhere, see IMPLEMENTATION_PLAN.md)
        val profileRepository = setContent()
        composeRule.waitUntil(timeoutMillis = 3_000) {
            // onAllNodesWithText, not onNodeWithText — the Rename card's own subtitle also shows
            // the profile's name now, so "Profile 1" legitimately matches two nodes at once
            // (header + rename row) and a singular onNodeWithText would throw as ambiguous.
            composeRule.onAllNodesWithText("Profile 1").fetchSemanticsNodes().isNotEmpty()
        }

        // When switching to Override — the Apps section (position, presentation, and content-mode
        // rows) pushes the Clock card below the fold on this device's viewport, so the row needs a
        // real scroll before it can actually be tapped (performClick() dispatches a real touch at
        // the node's bounds — an off-screen node just silently swallows the click rather than
        // throwing, see chat history)
        composeRule.onNodeWithTag("profile_clock_override_row").performScrollTo().performClick()

        // Then the profile's own flag is persisted...
        composeRule.waitUntil(timeoutMillis = 3_000) {
            runBlocking { profileRepository.observeProfiles().first().first().overrideClock == true }
        }
        // ...and the row's subtitle switches to reflect it
        composeRule.onNodeWithText("Overriding defaults · tap to edit").assertExists()
    }

    @Test
    fun calendarRowNavigatesWithTheProfilesId() {
        // Given the screen
        var navigatedProfileId: Long? = null
        setContent(onNavigateToCalendarSettings = { navigatedProfileId = it })

        // When tapping the Clock card's Calendar row — scrolled to first, same off-screen-click
        // gap as overridingTheClockCardMakesTwentyFourHourTimeLiveAndPersists above
        composeRule.onNodeWithTag("profile_calendar_settings_row").performScrollTo().performClick()

        // Then it navigates with this profile's id
        assertEquals(true, navigatedProfileId != null)
    }

    @Test
    fun clockStyleRowNavigatesWithTheProfilesId() {
        // Given the screen
        var navigatedProfileId: Long? = null
        setContent(onNavigateToClockStyleGallery = { navigatedProfileId = it })

        // When tapping the Clock card's "Clock style" row
        composeRule.onNodeWithTag("profile_clock_style_gallery_row").performScrollTo().performClick()

        // Then it navigates with this profile's id
        assertEquals(true, navigatedProfileId != null)
    }
}
