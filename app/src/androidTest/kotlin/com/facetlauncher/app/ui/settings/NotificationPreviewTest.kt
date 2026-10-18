package com.facetlauncher.app.ui.settings

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.onAllNodesWithTag
import com.facetlauncher.app.data.model.NotificationBadgeStyle
import com.facetlauncher.app.ui.theme.FacetLauncherTheme
import org.junit.Rule
import org.junit.Test

class NotificationPreviewTest {

    @get:Rule
    val composeRule = createComposeRule()

    private fun setContent(badgeStyle: NotificationBadgeStyle, showSkipButtons: Boolean) {
        composeRule.setContent {
            FacetLauncherTheme { NotificationPreview(badgeStyle = badgeStyle, showSkipButtons = showSkipButtons) }
        }
        composeRule.waitForIdle()
    }

    @Test
    fun `shows media player and sample app with badge`() {
        // Given the preview with default settings
        setContent(NotificationBadgeStyle.DOT, showSkipButtons = true)

        // Then both the media player and a badged sample app render
        composeRule.onNodeWithTag("home_media_player").assertIsDisplayed()
        composeRule.onNodeWithTag("app_icon_badge").assertIsDisplayed()
    }

    @Test
    fun `count style shows the badge number`() {
        // Given the count badge style
        setContent(NotificationBadgeStyle.COUNT, showSkipButtons = true)

        // Then the sample badge shows its count
        composeRule.onNodeWithText("3").assertIsDisplayed()
    }

    @Test
    fun `skip buttons follow the setting`() {
        // Given skip buttons turned off
        setContent(NotificationBadgeStyle.DOT, showSkipButtons = false)

        // Then only play/pause is shown
        composeRule.onNodeWithTag("home_media_play_pause").assertIsDisplayed()
        composeRule.onAllNodesWithTag("home_media_previous").assertCountEquals(0)
        composeRule.onAllNodesWithTag("home_media_next").assertCountEquals(0)
    }
}
