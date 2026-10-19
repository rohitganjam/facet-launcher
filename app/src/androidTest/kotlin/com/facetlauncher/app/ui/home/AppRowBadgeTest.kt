package com.facetlauncher.app.ui.home

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.getUnclippedBoundsInRoot
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.unit.dp
import com.facetlauncher.app.data.model.AppInfo
import com.facetlauncher.app.data.model.AppRowPosition
import com.facetlauncher.app.data.model.AppRowPresentation
import com.facetlauncher.app.data.model.NotificationBadgeStyle
import com.facetlauncher.app.ui.components.AppMenuNotifications
import com.facetlauncher.app.ui.theme.FacetLauncherTheme
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class AppRowBadgeTest {

    @get:Rule
    val composeRule = createComposeRule()

    private val app = AppInfo(packageName = "com.example.mail", activityName = ".Main", label = "Mail", icon = null)

    private fun setContent(position: AppRowPosition, presentation: AppRowPresentation) {
        composeRule.setContent {
            FacetLauncherTheme {
                AppRow(
                    app = app,
                    onClick = {},
                    badgeCount = 3,
                    badgeStyle = NotificationBadgeStyle.DOT,
                    appMenuNotifications = AppMenuNotifications.None,
                    position = position,
                    presentation = presentation,
                    enableLongPressMenu = false,
                )
            }
        }
        composeRule.waitForIdle()
    }

    @Test
    fun `icon only row badges the icon itself`() {
        // Given an icon-only row with an unread count
        setContent(AppRowPosition.LEFT, AppRowPresentation.ICON_ONLY)

        // Then exactly one badge shows, overlapping the icon's bounds
        val icon = composeRule.onNodeWithTag("home_app_icon_com.example.mail", useUnmergedTree = true).getUnclippedBoundsInRoot()
        val badge = composeRule.onNodeWithTag("app_icon_badge", useUnmergedTree = true)
        badge.assertIsDisplayed()
        val b = badge.getUnclippedBoundsInRoot()
        assertTrue(b.left < icon.right + 4.dp && b.right > icon.left)
    }

    @Test
    fun `right aligned row puts the badge left of the label`() {
        // Given a right-aligned icon and text row
        setContent(AppRowPosition.RIGHT, AppRowPresentation.ICON_AND_TEXT)

        // Then the badge sits to the left of the label
        val badge = composeRule.onNodeWithTag("app_icon_badge", useUnmergedTree = true).getUnclippedBoundsInRoot()
        val label = composeRule.onNodeWithTag("home_app_label_com.example.mail", useUnmergedTree = true).getUnclippedBoundsInRoot()
        assertTrue(badge.right <= label.left)
    }

    @Test
    fun `left aligned row puts the badge right of the label`() {
        // Given a left-aligned icon and text row
        setContent(AppRowPosition.LEFT, AppRowPresentation.ICON_AND_TEXT)

        // Then the badge sits to the right of the label
        val badge = composeRule.onNodeWithTag("app_icon_badge", useUnmergedTree = true).getUnclippedBoundsInRoot()
        val label = composeRule.onNodeWithTag("home_app_label_com.example.mail", useUnmergedTree = true).getUnclippedBoundsInRoot()
        assertTrue(badge.left >= label.right)
    }
}
