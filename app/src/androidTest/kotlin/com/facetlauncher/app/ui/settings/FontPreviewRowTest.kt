package com.facetlauncher.app.ui.settings

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.getUnclippedBoundsInRoot
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.unit.height
import com.facetlauncher.app.data.model.FontScaleOption
import com.facetlauncher.app.data.model.LauncherSettings
import com.facetlauncher.app.ui.theme.FacetLauncherTheme
import com.facetlauncher.app.ui.theme.UnscaledTypography
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class FontPreviewRowTest {

    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun previewShowsTheSampleLinesAndGrowsWithTheFontSizeOption() {
        // Given the preview inside Settings' unscaled typography, at the Default font size
        var option by mutableStateOf(FontScaleOption.DEFAULT)
        composeRule.setContent {
            FacetLauncherTheme(fontScaleOption = option) {
                UnscaledTypography { FontPreviewRow(settings = LauncherSettings()) }
            }
        }
        composeRule.onNodeWithTag("font_preview_card").assertIsDisplayed()
        composeRule.onNodeWithText("Home app list").assertIsDisplayed()
        val before = composeRule.onNodeWithText("Home app list").getUnclippedBoundsInRoot().height

        // When the Font size option goes to Huge
        option = FontScaleOption.HUGE
        composeRule.waitForIdle()

        // Then the sample text grows, even though Settings' own text does not
        val after = composeRule.onNodeWithText("Home app list").getUnclippedBoundsInRoot().height
        assertTrue(after > before)
    }
}
