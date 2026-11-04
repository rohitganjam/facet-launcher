package com.facetlauncher.app.ui.components

import androidx.compose.ui.test.getUnclippedBoundsInRoot
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.height
import androidx.compose.ui.unit.width
import com.facetlauncher.app.ui.theme.FacetLauncherTheme
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

class AppIconWorkBadgeTest {

    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun workBadgeIsAFifthOfTheIconHeightAtItsBottomLeft() {
        // Given a Work app's 50dp icon
        composeRule.setContent {
            FacetLauncherTheme {
                AppIcon(icon = null, size = 50.dp, contentDescription = null, isWorkApp = true)
            }
        }

        // When reading the badge's bounds
        val bounds = composeRule.onNodeWithTag("work_profile_badge", useUnmergedTree = true).getUnclippedBoundsInRoot()

        // Then it is 10dp square, flush with the icon's bottom-left corner
        assertEquals(10f, bounds.height.value, 0.5f)
        assertEquals(bounds.height.value, bounds.width.value, 0.5f)
        assertEquals(0f, bounds.left.value, 0.5f)
        assertEquals(50f, bounds.bottom.value, 0.5f)
    }
}
