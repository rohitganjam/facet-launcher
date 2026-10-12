package com.facetlauncher.app.ui.settings

import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.assertIsOff
import androidx.compose.ui.test.assertIsOn
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import com.facetlauncher.app.data.SettingsRepository
import com.facetlauncher.app.data.model.MediaProgressStyle
import com.facetlauncher.app.data.model.NotificationBadgeStyle
import com.facetlauncher.app.ui.theme.FacetLauncherTheme
import java.io.File
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

class NotificationSettingsScreenTest {

    @get:Rule
    val composeRule = createComposeRule()

    private fun setContent(
        onBack: () -> Unit = {},
        onNavigateToNotificationAccessExplanation: () -> Unit = {},
        accessGranted: Boolean = true,
    ): SettingsRepository {
        lateinit var settingsRepository: SettingsRepository
        composeRule.setContent {
            val context = LocalContext.current
            val viewModel = remember {
                settingsRepository = SettingsRepository(
                    PreferenceDataStoreFactory.create(
                        produceFile = { File(context.cacheDir, "notification-settings-test-${System.nanoTime()}.preferences_pb") },
                    ),
                )
                NotificationSettingsViewModel(settingsRepository, FakeNotificationAccessRepository(context, granted = accessGranted))
            }
            FacetLauncherTheme {
                NotificationSettingsScreen(
                    onBack = onBack,
                    onNavigateToNotificationAccessExplanation = onNavigateToNotificationAccessExplanation,
                    viewModel = viewModel,
                )
            }
        }
        composeRule.waitForIdle()
        return settingsRepository
    }

    @Test
    fun badgesOnByDefaultWithBadgeStyleEnabled() {
        // Given the screen, badges on by default and access already granted
        setContent(accessGranted = true)

        // Then the toggle is on and the style row is enabled
        composeRule.onNodeWithTag("notification_badges_toggle").assertIsOn()
        composeRule.onNodeWithTag("badge_style_row").assertIsEnabled()
        composeRule.onNodeWithText("Dot").assertExists()
    }

    @Test
    fun toggleStaysOffWithoutRealAccessEvenThoughTheStoredPreferenceDefaultsOn() {
        // Given the screen — the stored "Notification badges" preference defaults to true, but
        // notification listener access itself was never actually granted (e.g. revoked
        // externally, or simply never granted — see chat history for the real bug this covers:
        // the switch used to read the stored preference alone and showed "on" while nothing
        // worked, with no visible reason why)
        setContent(accessGranted = false)

        // Then the switch honors the real permission, not just the stored preference
        composeRule.onNodeWithTag("notification_badges_toggle").assertIsOff()
        composeRule.onNodeWithTag("badge_style_row").assertIsNotEnabled()
    }

    @Test
    fun turningBadgesOffDisablesTheStyleRowAndPersists() {
        // Given the screen, access granted so badges start genuinely on
        val settingsRepository = setContent(accessGranted = true)

        // When turning badges off
        composeRule.onNodeWithTag("notification_badges_toggle").performClick()

        // Then it persists (a real DataStore write — the round trip back into uiState isn't
        // caught by a single waitForIdle(), same async-I/O-vs-idling gap documented elsewhere
        // in this codebase's test suite), and the toggle/style row reflect it once it lands
        composeRule.waitUntil(timeoutMillis = 3_000) {
            runBlocking { !settingsRepository.settings.first().notificationDotsEnabled }
        }
        composeRule.waitUntil(timeoutMillis = 3_000) {
            runCatching { composeRule.onNodeWithTag("notification_badges_toggle").assertIsOff() }.isSuccess
        }
        composeRule.onNodeWithTag("badge_style_row").assertIsNotEnabled()
    }

    @Test
    fun selectingCountUpdatesTheRowAndPersists() {
        // Given the screen, badges on (default)
        val settingsRepository = setContent()

        // When opening the style dropdown and choosing "Count"
        composeRule.onNodeWithTag("badge_style_row").performClick()
        composeRule.waitForIdle()
        composeRule.onNodeWithTag("badge_style_row_option_COUNT").performClick()

        // Then the row reflects it and it persists to the repository
        composeRule.waitUntil(timeoutMillis = 3_000) {
            runCatching { composeRule.onNodeWithText("Count").assertExists() }.isSuccess
        }
        composeRule.waitUntil(timeoutMillis = 3_000) {
            runBlocking { settingsRepository.settings.first().notificationBadgeStyle == NotificationBadgeStyle.COUNT }
        }
    }

    @Test
    fun tappingTheOffSwitchWithoutAccessGrantedRoutesToTheExplanationScreenInsteadOfPersisting() {
        // Given the screen with the stored preference at its true default but access not
        // granted — the switch already reads off per the gating above, exactly the state a real
        // user lands on before ever visiting the explanation screen
        var navigatedToExplanation = false
        val settingsRepository = setContent(
            onNavigateToNotificationAccessExplanation = { navigatedToExplanation = true },
            accessGranted = false,
        )
        composeRule.onNodeWithTag("notification_badges_toggle").assertIsOff()

        // When tapping it (attempting to turn it on)
        composeRule.onNodeWithTag("notification_badges_toggle").performClick()

        // Then it routes to the explanation screen instead of persisting the setting directly,
        // and the stored preference is left untouched at its original default
        assertEquals(true, navigatedToExplanation)
        composeRule.onNodeWithTag("notification_badges_toggle").assertIsOff()
        composeRule.waitUntil(timeoutMillis = 1_000) {
            runBlocking { settingsRepository.settings.first().notificationDotsEnabled }
        }
    }

    @Test
    fun mediaPlayerIsOffByDefaultAndSkipButtonsAreDisabledUntilItIsOn() {
        // Given the screen with access granted and settings at their defaults
        setContent(accessGranted = true)

        // Then the media player is opt-in, so it's off and the skip-buttons switch can't be used yet
        composeRule.onNodeWithTag("media_player_toggle").assertIsOff()
        composeRule.onNodeWithTag("media_skip_buttons_toggle").assertIsNotEnabled()
    }

    @Test
    fun turningTheMediaPlayerOnPersistsAndEnablesSkipButtons() {
        // Given the screen with access granted
        val settingsRepository = setContent(accessGranted = true)

        // When turning the media player on
        composeRule.onNodeWithTag("media_player_toggle").performClick()

        // Then it persists and the skip-buttons switch becomes usable
        composeRule.waitUntil(timeoutMillis = 3_000) {
            runBlocking { settingsRepository.settings.first().showMediaPlayerOnHome }
        }
        composeRule.waitUntil(timeoutMillis = 3_000) {
            runCatching { composeRule.onNodeWithTag("media_skip_buttons_toggle").assertIsEnabled() }.isSuccess
        }
    }

    @Test
    fun turningTheSkipButtonsOffPersists() {
        // Given the media player on, so the skip-buttons switch is usable
        val settingsRepository = setContent(accessGranted = true)
        composeRule.onNodeWithTag("media_player_toggle").performClick()
        composeRule.waitUntil(timeoutMillis = 3_000) {
            runCatching { composeRule.onNodeWithTag("media_skip_buttons_toggle").assertIsEnabled() }.isSuccess
        }

        // When turning skip buttons off
        composeRule.onNodeWithTag("media_skip_buttons_toggle").performClick()

        // Then the choice persists
        composeRule.waitUntil(timeoutMillis = 3_000) {
            runBlocking { !settingsRepository.settings.first().showMediaSkipButtons }
        }
    }

    @Test
    fun choosingFlatProgressPersists() {
        // Given the media player on, so the progress style row is usable
        val settingsRepository = setContent(accessGranted = true)
        composeRule.onNodeWithTag("media_player_toggle").performClick()
        composeRule.waitUntil(timeoutMillis = 3_000) {
            runCatching { composeRule.onNodeWithTag("media_progress_style_row").assertIsEnabled() }.isSuccess
        }

        // When choosing Flat from the dropdown (Wavy is the default)
        composeRule.onNodeWithTag("media_progress_style_row").performClick()
        composeRule.waitForIdle()
        composeRule.onNodeWithTag("media_progress_style_row_option_FLAT").performClick()

        // Then it persists
        composeRule.waitUntil(timeoutMillis = 3_000) {
            runBlocking { settingsRepository.settings.first().mediaProgressStyle == MediaProgressStyle.FLAT }
        }
    }

    @Test
    fun turningTheMediaPlayerOnWithoutAccessRoutesToTheExplanationScreen() {
        // Given the screen with notification access not granted
        var navigatedToExplanation = false
        val settingsRepository = setContent(
            onNavigateToNotificationAccessExplanation = { navigatedToExplanation = true },
            accessGranted = false,
        )

        // When tapping the media player switch
        composeRule.onNodeWithTag("media_player_toggle").performClick()

        // Then it routes to the explanation screen, remembers the choice for when access lands,
        // and the switch itself stays off until the grant is real
        assertEquals(true, navigatedToExplanation)
        composeRule.waitUntil(timeoutMillis = 3_000) {
            runBlocking { settingsRepository.settings.first().showMediaPlayerOnHome }
        }
        composeRule.onNodeWithTag("media_player_toggle").assertIsOff()
    }

    @Test
    fun backButtonInvokesOnBack() {
        // Given the screen
        var backInvoked = false
        setContent(onBack = { backInvoked = true })

        // When tapping the back button
        composeRule.onNodeWithTag("back_button").performClick()

        // Then it navigates back
        assertEquals(true, backInvoked)
    }
}
