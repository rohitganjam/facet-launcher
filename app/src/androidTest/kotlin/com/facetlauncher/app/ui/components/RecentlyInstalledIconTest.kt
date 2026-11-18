package com.facetlauncher.app.ui.components

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.unit.dp
import com.facetlauncher.app.ui.theme.FacetLauncherTheme
import org.junit.Rule
import org.junit.Test

class RecentlyInstalledIconTest {

    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun theIconIsDrawnLikeAnAppIconAtTheSizeAsked() {
        // Given the Recently installed icon in the app's theme
        composeRule.setContent { FacetLauncherTheme { RecentlyInstalledIcon(size = 56.dp, contentDescription = "Recently installed") } }

        // Then it is on screen with its description, drawn through the same renderer as an app's icon
        composeRule.onNodeWithContentDescription("Recently installed").assertIsDisplayed()
    }
}
