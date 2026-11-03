package com.facetlauncher.app.ui.onboarding

import androidx.compose.ui.test.assertIsOff
import androidx.compose.ui.test.assertIsOn
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import com.facetlauncher.app.data.model.LauncherSettings
import com.facetlauncher.app.ui.settings.NotificationSettingsUiState
import com.facetlauncher.app.ui.theme.FacetLauncherTheme
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

class OnboardingNotificationsPageTest {

    @get:Rule
    val composeRule = createComposeRule()

    private fun setContent(
        accessGranted: Boolean = true,
        settings: LauncherSettings = LauncherSettings(),
        onBadgesEnabledChange: (Boolean) -> Unit = {},
        onShowMediaPlayerChange: (Boolean) -> Unit = {},
        onGrantAccess: () -> Unit = {},
    ) {
        composeRule.setContent {
            FacetLauncherTheme {
                OnboardingNotificationsContent(
                    uiState = NotificationSettingsUiState(settings = settings, isAccessGranted = accessGranted),
                    onBadgesEnabledChange = onBadgesEnabledChange,
                    onBadgeStyleChange = {},
                    onShowMediaPlayerChange = onShowMediaPlayerChange,
                    onGrantAccess = onGrantAccess,
                    onBack = {},
                    onNext = {},
                )
            }
        }
        composeRule.waitForIdle()
    }

    @Test
    fun `page shows the access note and omits the in-app menu and skip-button rows`() {
        // Given the page
        setContent()

        // Then the disclosure and the three wanted controls show, and the two hidden ones don't exist
        composeRule.onNodeWithTag("onboarding_notifications_access_note").assertExists()
        composeRule.onNodeWithTag("notification_badges_toggle").assertExists()
        composeRule.onNodeWithTag("badge_style_row").assertExists()
        composeRule.onNodeWithTag("media_player_toggle").assertExists()
        composeRule.onNodeWithTag("notification_app_menu_toggle").assertDoesNotExist()
        composeRule.onNodeWithTag("media_skip_buttons_toggle").assertDoesNotExist()
    }

    @Test
    fun `switches show off without access even though badges default to on`() {
        // Given no notification access
        setContent(accessGranted = false, settings = LauncherSettings(notificationDotsEnabled = true, showMediaPlayerOnHome = true))

        // Then neither switch claims to be on, and the preview asks for access
        composeRule.onNodeWithTag("notification_badges_toggle").assertIsOff()
        composeRule.onNodeWithTag("media_player_toggle").assertIsOff()
        composeRule.onNodeWithTag("notification_preview_no_access").performScrollTo().assertExists()
    }

    @Test
    fun `switches reflect the stored settings once access is granted`() {
        // Given access and both features enabled
        setContent(accessGranted = true, settings = LauncherSettings(notificationDotsEnabled = true, showMediaPlayerOnHome = true))

        // Then both switches show on
        composeRule.onNodeWithTag("notification_badges_toggle").assertIsOn()
        composeRule.onNodeWithTag("media_player_toggle").assertIsOn()
    }

    @Test
    fun `flipping a switch reports the new value`() {
        // Given access and everything off
        var badges: Boolean? = null
        var media: Boolean? = null
        setContent(
            settings = LauncherSettings(notificationDotsEnabled = false, showMediaPlayerOnHome = false),
            onBadgesEnabledChange = { badges = it },
            onShowMediaPlayerChange = { media = it },
        )

        // When both are flipped
        composeRule.onNodeWithTag("notification_badges_toggle").performClick()
        composeRule.onNodeWithTag("media_player_toggle").performClick()

        // Then each callback gets true
        assertEquals(true, badges)
        assertEquals(true, media)
    }

    @Test
    fun `Allow access in the preview triggers the grant flow`() {
        // Given no access
        var grantRequests = 0
        setContent(accessGranted = false, onGrantAccess = { grantRequests++ })

        // When the preview's allow button is tapped
        composeRule.onNodeWithTag("notification_preview_allow_access").performScrollTo().performClick()

        // Then the grant flow starts
        assertEquals(1, grantRequests)
    }
}
