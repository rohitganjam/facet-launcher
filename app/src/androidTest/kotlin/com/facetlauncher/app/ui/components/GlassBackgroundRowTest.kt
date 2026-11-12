package com.facetlauncher.app.ui.components

import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.facetlauncher.app.data.model.GlassStyle
import com.facetlauncher.app.ui.theme.FacetLauncherTheme
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

class GlassBackgroundRowTest {

    @get:Rule
    val composeRule = createComposeRule()

    private class Calls {
        var selected: GlassStyle? = null
        var chose = 0
        var declined = 0
    }

    private fun setContent(needsCopy: Boolean, calls: Calls, selected: GlassStyle = GlassStyle.OFF) {
        composeRule.setContent {
            FacetLauncherTheme {
                CompositionLocalProvider(LocalGlassBackgroundPrompt provides GlassBackgroundPrompt(needsCopy, { calls.chose++ }, { calls.declined++ })) {
                    GlassBackgroundRow(title = "Dock background", selected = selected, onSelect = { calls.selected = it }, testTag = "row")
                }
            }
        }
    }

    @Test
    fun `choosing a glass style with no copy applies it and shows the prompt`() {
        // Given no wallpaper copy
        val calls = Calls()
        setContent(needsCopy = true, calls)

        // When a glass style is picked
        composeRule.onNodeWithTag("row").performClick()
        composeRule.onNodeWithText("Clear glass").performClick()

        // Then it is applied and the prompt shows
        assertEquals(GlassStyle.CLEAR_GLASS, calls.selected)
        composeRule.onNodeWithTag("glass_prompt").assertIsDisplayed()
    }

    @Test
    fun `the prompt buttons report and close it`() {
        // Given the prompt is showing
        val calls = Calls()
        setContent(needsCopy = true, calls)
        composeRule.onNodeWithTag("row").performClick()
        composeRule.onNodeWithText("Dark frost").performClick()

        // When Choose an image is tapped
        composeRule.onNodeWithTag("glass_prompt_choose").performClick()

        // Then it reports and the prompt is gone
        assertEquals(1, calls.chose)
        composeRule.onNodeWithTag("glass_prompt").assertDoesNotExist()
    }

    @Test
    fun `with a copy already there no prompt shows`() {
        // Given a copy exists
        val calls = Calls()
        setContent(needsCopy = false, calls)

        // When a glass style is picked
        composeRule.onNodeWithTag("row").performClick()
        composeRule.onNodeWithText("Bright").performClick()

        // Then it is applied without a prompt
        assertEquals(GlassStyle.BRIGHT, calls.selected)
        composeRule.onNodeWithTag("glass_prompt").assertDoesNotExist()
    }

    @Test
    fun `turning glass off never prompts`() {
        // Given no copy and a glass style already chosen
        val calls = Calls()
        setContent(needsCopy = true, calls, selected = GlassStyle.BRIGHT)

        // When Off is picked
        composeRule.onNodeWithTag("row").performClick()
        composeRule.onNodeWithText("Off").performClick()

        // Then there is no prompt
        composeRule.onNodeWithTag("glass_prompt").assertDoesNotExist()
    }
}
