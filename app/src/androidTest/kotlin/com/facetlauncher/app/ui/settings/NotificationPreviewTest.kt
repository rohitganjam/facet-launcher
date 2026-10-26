package com.facetlauncher.app.ui.settings

import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import kotlinx.collections.immutable.toImmutableList
import org.junit.Assert.assertTrue
import com.facetlauncher.app.data.model.AppInfo
import com.facetlauncher.app.data.model.AppRowPresentation
import com.facetlauncher.app.data.model.DockDisplayMode
import com.facetlauncher.app.data.model.LauncherSettings
import com.facetlauncher.app.data.model.NotificationBadgeStyle
import com.facetlauncher.app.data.model.PlacedItem
import com.facetlauncher.app.ui.theme.FacetLauncherTheme
import org.junit.Rule
import org.junit.Test

class NotificationPreviewTest {

    @get:Rule
    val composeRule = createComposeRule()

    private fun app(name: String) = PlacedItem.SingleApp(AppInfo(packageName = "com.example.$name", activityName = ".Main", label = name, icon = null))

    private fun setContent(
        settings: LauncherSettings = LauncherSettings(),
        accessGranted: Boolean = true,
        onGrantAccess: () -> Unit = {},
    ) {
        composeRule.setContent {
            FacetLauncherTheme {
                NotificationPreview(
                    uiState = NotificationSettingsUiState(
                        settings = settings,
                        isAccessGranted = accessGranted,
                        homePreviewApps = listOf(app("Mail"), app("Maps")).toImmutableList(),
                        dockPreviewApps = listOf(app("Phone"), app("Camera")).toImmutableList(),
                    ),
                    onGrantAccess = onGrantAccess,
                )
            }
        }
        composeRule.waitForIdle()
    }

    @Test
    fun `shows media player and badged home and dock apps`() {
        // Given the preview with the media player on
        setContent(LauncherSettings(showMediaPlayerOnHome = true))

        // Then the media player renders and the sample apps carry badges
        composeRule.onNodeWithTag("home_media_player", useUnmergedTree = true).assertIsDisplayed()
        composeRule.onNodeWithTag("notification_home_preview", useUnmergedTree = true).assertIsDisplayed()
        composeRule.onAllNodesWithTag("app_icon_badge", useUnmergedTree = true).assertCountEquals(3)
    }

    @Test
    fun `count style shows the badge numbers`() {
        // Given the count badge style
        setContent(LauncherSettings(notificationBadgeStyle = NotificationBadgeStyle.COUNT))

        // Then the sample badges show their counts
        composeRule.onNodeWithText("3").assertIsDisplayed()
        composeRule.onNodeWithText("9+").assertIsDisplayed()
    }

    @Test
    fun `skip buttons follow the setting`() {
        // Given skip buttons turned off
        setContent(LauncherSettings(showMediaPlayerOnHome = true, showMediaSkipButtons = false))

        // Then only play/pause is shown
        composeRule.onNodeWithTag("home_media_play_pause", useUnmergedTree = true).assertIsDisplayed()
        composeRule.onAllNodesWithTag("home_media_previous", useUnmergedTree = true).assertCountEquals(0)
        composeRule.onAllNodesWithTag("home_media_next", useUnmergedTree = true).assertCountEquals(0)
    }

    @Test
    fun `text only dock shows the badge beside the label`() {
        // Given a text-only dock and text-only home rows
        setContent(LauncherSettings(dockDisplayMode = DockDisplayMode.TEXT, appRowPresentation = AppRowPresentation.TEXT_ONLY))

        // Then the home labels and the badges are still present
        composeRule.onNodeWithTag("home_surface_preview_app_label_com.example.Mail", useUnmergedTree = true).assertExists()
        composeRule.onAllNodesWithTag("app_icon_badge", useUnmergedTree = true).assertCountEquals(3)
    }

    @Test
    fun `no access shows a prompt instead of the preview`() {
        // Given notification access has not been granted
        var requested = false
        setContent(accessGranted = false, onGrantAccess = { requested = true })

        // Then no sample is shown, only the prompt, whose button asks for access
        composeRule.onNodeWithTag("notification_preview_no_access").assertIsDisplayed()
        composeRule.onAllNodesWithTag("home_media_player", useUnmergedTree = true).assertCountEquals(0)
        composeRule.onAllNodesWithTag("notification_home_preview", useUnmergedTree = true).assertCountEquals(0)
        composeRule.onNodeWithTag("notification_preview_allow_access").performClick()
        assertTrue(requested)
    }

    @Test
    fun `badges and media player both off shows the disabled message`() {
        // Given access granted but both features off
        setContent(LauncherSettings(notificationDotsEnabled = false, showMediaPlayerOnHome = false))

        // Then the disabled message replaces the sample
        composeRule.onNodeWithTag("notification_preview_disabled").assertIsDisplayed()
        composeRule.onAllNodesWithTag("notification_home_preview", useUnmergedTree = true).assertCountEquals(0)
    }

    @Test
    fun `only the enabled feature is previewed`() {
        // Given badges off but the media player on
        setContent(LauncherSettings(notificationDotsEnabled = false, showMediaPlayerOnHome = true))

        // Then the media player shows and no badges do
        composeRule.onNodeWithTag("home_media_player", useUnmergedTree = true).assertIsDisplayed()
        composeRule.onAllNodesWithTag("app_icon_badge", useUnmergedTree = true).assertCountEquals(0)
    }
}
