package com.facetlauncher.app.ui.settings

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertTextContains
import androidx.compose.ui.test.click
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTouchInput
import com.facetlauncher.app.ui.theme.FacetLauncherTheme
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

class WallpaperCaptureScreenTest {

    @get:Rule
    val composeRule = createComposeRule()

    private var chosen = 0
    private var returned = 0

    private fun setContent() {
        composeRule.setContent { FacetLauncherTheme { WallpaperCaptureScreen(onChooseScreenshot = { chosen++ }, onReturn = { returned++ }) } }
    }

    @Test
    fun `the card stays until Take screenshot is tapped`() {
        // Given the screen has just opened and a long time passes
        composeRule.mainClock.autoAdvance = false
        setContent()
        composeRule.mainClock.advanceTimeBy(30_000)

        // Then the card is still there
        composeRule.onNodeWithTag("wallpaper_capture_hint").assertIsDisplayed()

        // When Take screenshot is tapped
        composeRule.onNodeWithTag("wallpaper_capture_take").performClick()
        composeRule.mainClock.advanceTimeBy(1_000)

        // Then it clears, leaving nothing but the empty screen
        composeRule.onNodeWithTag("wallpaper_capture_hint").assertDoesNotExist()
        composeRule.onNodeWithTag("wallpaper_capture").assertIsDisplayed()
    }

    @Test
    fun `tapping the empty screen brings the card back`() {
        // Given the card has been cleared
        composeRule.mainClock.autoAdvance = false
        setContent()
        composeRule.mainClock.advanceTimeBy(500)
        composeRule.onNodeWithTag("wallpaper_capture_take").performClick()
        composeRule.mainClock.advanceTimeBy(1_000)
        composeRule.onNodeWithTag("wallpaper_capture_hint").assertDoesNotExist()

        // When the screen is tapped
        composeRule.onNodeWithTag("wallpaper_capture").performTouchInput { click(center) }
        composeRule.mainClock.advanceTimeBy(500)

        // Then the card is back
        composeRule.onNodeWithTag("wallpaper_capture_hint").assertIsDisplayed()
    }

    @Test
    fun `choose screenshot and return each report once`() {
        // Given the card is showing
        setContent()

        // When each is tapped
        composeRule.onNodeWithTag("wallpaper_capture_choose").performClick()
        composeRule.onNodeWithTag("wallpaper_capture_return").performClick()

        // Then each reports once
        assertEquals(1, chosen)
        assertEquals(1, returned)
    }

    @Test
    fun `the card gives three steps and says not to hold the buttons down`() {
        // Given the card is showing
        setContent()

        // Then all three steps show, the second saying tap and not hold
        composeRule.onNodeWithTag("wallpaper_capture_step1").assertIsDisplayed()
        composeRule.onNodeWithTag("wallpaper_capture_step2").assertTextContains("Don't hold down the buttons", substring = true)
        composeRule.onNodeWithTag("wallpaper_capture_step3").assertIsDisplayed()
    }
}
