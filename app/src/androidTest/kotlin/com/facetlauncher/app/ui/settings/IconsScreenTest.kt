package com.facetlauncher.app.ui.settings

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.graphics.toPixelMap
import androidx.compose.ui.test.assertIsSelected
import androidx.compose.ui.test.captureToImage
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollToNode
import com.facetlauncher.app.data.SettingsRepository
import com.facetlauncher.app.data.model.IconRenderMode
import com.facetlauncher.app.data.model.IconShape
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

class IconsScreenTest {

    @get:Rule
    val composeRule = createComposeRule()

    private val host = AppearanceScreenTestHost(composeRule)

    private fun setContent(): SettingsRepository =
        host.setContent(null, false, true, emptyList(), { _, _ -> }, { _, _, _ -> }) { viewModel ->
            IconsScreen(onBack = {}, viewModel = viewModel)
        }

    private fun SettingsRepository.awaitMode(mode: IconRenderMode) =
        composeRule.waitUntil(timeoutMillis = 3_000) { runBlocking { settings.first().iconRenderMode == mode } }

    @Test
    fun styleCardsSetTheStyleAndKeepTheColour() {
        // Given the screen on the default style
        val settingsRepository = setContent()
        composeRule.onNodeWithTag("icons_style_DEFAULT").assertIsSelected()

        // When picking Glyph and then Outline
        composeRule.onNodeWithTag("icons_style_GLYPH").performClick()
        settingsRepository.awaitMode(IconRenderMode.GLYPH_ACCENT)
        composeRule.onNodeWithTag("icons_style_OUTLINE").performClick()

        // Then the accent colour carries over
        settingsRepository.awaitMode(IconRenderMode.OUTLINE_ACCENT)
        composeRule.onNodeWithTag("icons_style_OUTLINE").assertIsSelected()
    }

    @Test
    fun colourDropdownOffersThemeInvertedForGlyphAndPersistsIt() {
        // Given the glyph style selected
        val settingsRepository = setContent()
        composeRule.onNodeWithTag("icons_style_GLYPH").performClick()
        settingsRepository.awaitMode(IconRenderMode.GLYPH_ACCENT)

        // When choosing Theme inverted
        composeRule.onNodeWithTag("icons_colour_row").performClick()
        composeRule.waitForIdle()
        composeRule.onNodeWithTag("icons_colour_row_option_THEME_INVERTED").performClick()

        // Then the inverted glyph mode is stored
        settingsRepository.awaitMode(IconRenderMode.GLYPH_INVERTED)
    }

    @Test
    fun backgroundDropdownPutsGlyphAndOutlineOnATile() {
        // Given the outline style
        val settingsRepository = setContent()
        composeRule.onNodeWithTag("icons_style_OUTLINE").performClick()
        settingsRepository.awaitMode(IconRenderMode.OUTLINE_ACCENT)

        // When choosing the tile background
        composeRule.onNodeWithTag("icons_background_row").performClick()
        composeRule.waitForIdle()
        composeRule.onNodeWithTag("icons_background_row_option_TILE").performClick()

        // Then the tiled outline mode is stored
        settingsRepository.awaitMode(IconRenderMode.OUTLINE_TILE_ACCENT)
    }

    @Test
    fun themeInvertedSamplesGetADifferentBackdrop() {
        // Given the glyph style in the theme colour
        val settingsRepository = setContent()
        composeRule.onNodeWithTag("icons_style_GLYPH").performClick()
        settingsRepository.awaitMode(IconRenderMode.GLYPH_ACCENT)
        composeRule.onNodeWithTag("icons_colour_row").performClick()
        composeRule.waitForIdle()
        composeRule.onNodeWithTag("icons_colour_row_option_THEME").performClick()
        settingsRepository.awaitMode(IconRenderMode.GLYPH_INK)
        val backdropOf = { composeRule.onNodeWithTag("icons_style_GLYPH").captureToImage().toPixelMap().let { it[it.width / 2, 6] } }
        val themed = backdropOf()

        // When switching to theme inverted
        composeRule.onNodeWithTag("icons_colour_row").performClick()
        composeRule.waitForIdle()
        composeRule.onNodeWithTag("icons_colour_row_option_THEME_INVERTED").performClick()
        settingsRepository.awaitMode(IconRenderMode.GLYPH_INVERTED)

        // Then the sample's backdrop is no longer the page tone
        composeRule.waitUntil(timeoutMillis = 3_000) { backdropOf() != themed }
    }

    @Test
    fun colourRowIsHiddenForTheDefaultStyle() {
        // Given the default style
        setContent()

        // Then there is no colour row
        assertEquals(0, composeRule.onAllNodesWithText("Colour").fetchSemanticsNodes().size)
    }

    @Test
    fun iconShapePickerPersistsTheSelectedShape() {
        // Given the screen, with the picker scrolled into view
        val settingsRepository = setContent()
        composeRule.onNodeWithTag("icons_screen").performScrollToNode(hasTestTag("appearance_icon_shape_row"))

        // When picking Circle
        composeRule.onNodeWithTag("icon_shape_CIRCLE").performClick()

        // Then it's selected and persisted
        composeRule.waitUntil(timeoutMillis = 3_000) { runBlocking { settingsRepository.settings.first().iconShape == IconShape.CIRCLE } }
        composeRule.onNodeWithTag("icon_shape_CIRCLE").assertIsSelected()
    }

    @Test
    fun previewAndStyleCardsAreShown() {
        // Given the screen
        setContent()

        // Then the Home preview and all four style cards are there
        composeRule.onNodeWithTag("appearance_preview_card").assertIsDisplayed()
        listOf("DEFAULT", "MONOCHROME", "GLYPH", "OUTLINE").forEach { composeRule.onNodeWithTag("icons_style_$it").assertIsDisplayed() }
    }
}
