package com.facetlauncher.app.ui.settings

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
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

    private var phase by mutableStateOf(CapturePhase.Prompt)
    private var captures = 0
    private var chosen = 0
    private var returned = 0

    private fun setContent(start: CapturePhase = CapturePhase.Prompt) {
        phase = start
        composeRule.setContent {
            FacetLauncherTheme {
                WallpaperCaptureContent(
                    phase = phase,
                    onCapture = { captures++ },
                    onManualRoute = { phase = CapturePhase.Manual },
                    onBackToPrompt = { phase = CapturePhase.Prompt },
                    onChooseScreenshot = { chosen++ },
                    onReturn = { returned++ },
                )
            }
        }
    }

    @Test
    fun `the prompt offers capture and the manual route but not choose screenshot`() {
        // Given the prompt is showing
        setContent()

        // Then capture and the manual route are there and Choose screenshot is not
        composeRule.onNodeWithTag("wallpaper_capture_start").assertIsDisplayed()
        composeRule.onNodeWithTag("wallpaper_capture_manual").assertIsDisplayed()
        composeRule.onNodeWithTag("wallpaper_capture_choose").assertDoesNotExist()

        // When capture is tapped
        composeRule.onNodeWithTag("wallpaper_capture_start").performClick()

        // Then it reports once
        assertEquals(1, captures)
    }

    @Test
    fun `nothing is drawn while capturing`() {
        // Given a capture is under way
        composeRule.mainClock.autoAdvance = false
        setContent(CapturePhase.Capturing)
        composeRule.mainClock.advanceTimeBy(1_000)

        // Then the card is gone, leaving only the empty screen
        composeRule.onNodeWithTag("wallpaper_capture_hint").assertDoesNotExist()
        composeRule.onNodeWithTag("wallpaper_capture").assertIsDisplayed()
    }

    @Test
    fun `the cancelled card offers try again and still no choose screenshot`() {
        // Given the capture failed
        setContent(CapturePhase.Failed)

        // Then Try again is there and Choose screenshot is not
        composeRule.onNodeWithTag("wallpaper_capture_retry").assertIsDisplayed()
        composeRule.onNodeWithTag("wallpaper_capture_choose").assertDoesNotExist()

        // When Try again is tapped
        composeRule.onNodeWithTag("wallpaper_capture_retry").performClick()

        // Then it asks to capture again
        assertEquals(1, captures)
    }

    @Test
    fun `the manual route shows the steps and choose screenshot and back returns to the prompt`() {
        // Given the prompt
        setContent()

        // When the manual route is chosen
        composeRule.onNodeWithTag("wallpaper_capture_manual").performClick()

        // Then the three steps and Choose screenshot show
        composeRule.onNodeWithTag("wallpaper_capture_step1").assertIsDisplayed()
        composeRule.onNodeWithTag("wallpaper_capture_step2").assertTextContains("Don't hold down the buttons", substring = true)
        composeRule.onNodeWithTag("wallpaper_capture_step3").assertIsDisplayed()
        composeRule.onNodeWithTag("wallpaper_capture_choose").performClick()
        assertEquals(1, chosen)

        // When Back is tapped
        composeRule.onNodeWithTag("wallpaper_capture_back").performClick()

        // Then the prompt is back
        composeRule.onNodeWithTag("wallpaper_capture_start").assertIsDisplayed()
    }

    @Test
    fun `in the manual route the card stays until Take screenshot and a tap brings it back`() {
        // Given the manual card and a long wait
        composeRule.mainClock.autoAdvance = false
        setContent(CapturePhase.Manual)
        composeRule.mainClock.advanceTimeBy(30_000)
        composeRule.onNodeWithTag("wallpaper_capture_hint").assertIsDisplayed()

        // When Take screenshot is tapped
        composeRule.onNodeWithTag("wallpaper_capture_take").performClick()
        composeRule.mainClock.advanceTimeBy(1_000)

        // Then the card clears
        composeRule.onNodeWithTag("wallpaper_capture_hint").assertDoesNotExist()

        // When the empty screen is tapped
        composeRule.onNodeWithTag("wallpaper_capture").performTouchInput { click(center) }
        composeRule.mainClock.advanceTimeBy(500)

        // Then the card is back
        composeRule.onNodeWithTag("wallpaper_capture_hint").assertIsDisplayed()
    }

    @Test
    fun `return reports once`() {
        // Given the prompt
        setContent()

        // When Return is tapped
        composeRule.onNodeWithTag("wallpaper_capture_return").performClick()

        // Then it reports once
        assertEquals(1, returned)
    }
}
