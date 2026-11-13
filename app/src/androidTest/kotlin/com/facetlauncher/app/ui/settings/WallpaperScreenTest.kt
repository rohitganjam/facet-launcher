package com.facetlauncher.app.ui.settings

import android.graphics.Bitmap
import android.graphics.RectF
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertTextContains
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.swipe
import androidx.compose.ui.test.pinch
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.doubleClick
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollToNode
import com.facetlauncher.app.ui.theme.FacetLauncherTheme
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Rule
import org.junit.Test

class WallpaperScreenTest {

    @get:Rule
    val composeRule = createComposeRule()

    private class Calls {
        var setSystem = 0
        var accepted = 0
        var chose = 0
        var screenshot = 0
        var takeScreenshot = 0
        var declined = 0
        var removed = 0
        var cancelled = 0
        var confirmedRect: RectF? = null
        var confirmedLock: Boolean? = null
    }

    /** The screen is a list; what is below the first rows needs scrolling to before it can be touched. */
    private fun scrollTo(tag: String) = composeRule.onNodeWithTag("wallpaper_screen").performScrollToNode(androidx.compose.ui.test.hasTestTag(tag))

    private fun bitmap(width: Int, height: Int) = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)

    private fun setContent(state: WallpaperUiState, calls: Calls) {
        composeRule.setContent {
            FacetLauncherTheme {
                WallpaperContent(
                    uiState = state,
                    onBack = {},
                    onChooseWallpaper = { calls.chose++ },
                    onSetSystemWallpaper = { calls.setSystem++ },
                    onUseScreenshot = { calls.screenshot++ },
                    onTakeScreenshot = { calls.takeScreenshot++ },
                    onAcceptOffer = { calls.accepted++ },
                    onDeclineOffer = { calls.declined++ },
                    onRemoveCopy = { calls.removed++ },
                    onCancelFit = { calls.cancelled++ },
                    onConfirmFit = { rect, lock ->
                        calls.confirmedRect = rect
                        calls.confirmedLock = lock
                    },
                )
            }
        }
    }

    @Test
    fun `with no copy the status says tint and both choices work`() {
        // Given no glass copy
        val calls = Calls()
        setContent(WallpaperUiState(), calls)

        // Then the status says panels use a plain tint
        composeRule.onNodeWithTag("wallpaper_status").assertTextContains("plain tint", substring = true)

        // When each choice is tapped
        composeRule.onNodeWithTag("wallpaper_choose_image_row").performClick()
        composeRule.onNodeWithTag("wallpaper_system_row").performClick()

        // Then each reports once
        assertEquals(1, calls.chose)
        assertEquals(1, calls.setSystem)
    }

    @Test
    fun `the screenshot link explains first and offers taking one or using an image`() {
        // Given no copy
        val calls = Calls()
        setContent(WallpaperUiState(), calls)

        // When the link is tapped
        composeRule.onNodeWithTag("wallpaper_screenshot_link").performClick()

        // Then a dialog explains and nothing has happened yet
        composeRule.onNodeWithTag("wallpaper_screenshot_dialog").assertIsDisplayed()
        assertEquals(0, calls.screenshot + calls.takeScreenshot)

        // And Take screenshot reports once and closes it
        composeRule.onNodeWithTag("wallpaper_screenshot_take").performClick()
        assertEquals(1, calls.takeScreenshot)
        composeRule.onNodeWithTag("wallpaper_screenshot_dialog").assertDoesNotExist()
    }

    @Test
    fun `I have an image already opens the picker`() {
        // Given the dialog is open
        val calls = Calls()
        setContent(WallpaperUiState(), calls)
        composeRule.onNodeWithTag("wallpaper_screenshot_link").performClick()

        // When the second button is tapped
        composeRule.onNodeWithTag("wallpaper_screenshot_have_image").performClick()

        // Then the picker is requested once
        assertEquals(1, calls.screenshot)
    }

    @Test
    fun `with a matching copy the current wallpaper card stays and says it is in use`() {
        // Given a matching copy
        setContent(WallpaperUiState(copy = bitmap(10, 20)), Calls())

        // Then the card is still there and marks itself as already in use
        scrollTo("wallpaper_screenshot_link")
        composeRule.onNodeWithTag("wallpaper_screenshot_link").assertIsDisplayed()
        composeRule.onNodeWithTag("wallpaper_screenshot_link").assertTextContains("Already in use", substring = true)
    }

    @Test
    fun `with no copy or a stale one the current wallpaper card is not marked in use`() {
        // Given a stale copy
        setContent(WallpaperUiState(copy = bitmap(10, 20), stale = true), Calls())

        // Then the card offers the screenshot instead
        scrollTo("wallpaper_screenshot_link")
        composeRule.onNodeWithTag("wallpaper_screenshot_link").assertTextContains("Take a screenshot", substring = true)
    }

    @Test
    fun `the fit step for a screenshot has no home or lock choice`() {
        // Given a screenshot being framed
        setContent(WallpaperUiState(picked = bitmap(200, 400), copyOnly = true), Calls())

        // Then the home and lock options are absent and the button says what it does
        composeRule.onNodeWithTag("wallpaper_fit_home").assertDoesNotExist()
        composeRule.onNodeWithText("Use for glass").assertIsDisplayed()
    }

    @Test
    fun `the offer after the system picker can be accepted or declined`() {
        // Given the offer is showing
        val calls = Calls()
        setContent(WallpaperUiState(offerCopy = true), calls)

        // When each button is tapped
        composeRule.onNodeWithTag("wallpaper_offer_accept").performClick()
        composeRule.onNodeWithTag("wallpaper_offer_decline").performClick()

        // Then each reports once
        assertEquals(1, calls.accepted)
        assertEquals(1, calls.declined)
    }

    @Test
    fun `without an offer the offer card is absent`() {
        // Given no offer
        setContent(WallpaperUiState(), Calls())

        // Then the card does not exist
        composeRule.onNodeWithTag("wallpaper_offer").assertDoesNotExist()
    }

    @Test
    fun `with a copy the status says glass is on and the copy can be removed`() {
        // Given a stored copy
        val calls = Calls()
        setContent(WallpaperUiState(copy = bitmap(10, 20)), calls)

        // Then it says glass is on
        composeRule.onNodeWithTag("wallpaper_status").assertTextContains("Glass is on", substring = true)

        // When the copy is removed
        scrollTo("wallpaper_remove_row")
        composeRule.onNodeWithTag("wallpaper_remove_row").performClick()

        // Then removal is reported
        assertEquals(1, calls.removed)
    }

    @Test
    fun `there is no remove row without a copy`() {
        // Given no copy
        setContent(WallpaperUiState(), Calls())

        // Then nothing offers to remove one
        composeRule.onNodeWithTag("wallpaper_remove_row").assertDoesNotExist()
    }

    @Test
    fun `a stale copy says the wallpaper changed`() {
        // Given a copy that no longer matches the system wallpaper
        setContent(WallpaperUiState(copy = bitmap(10, 20), stale = true), Calls())

        // Then the screen says so
        composeRule.onNodeWithTag("wallpaper_stale").assertIsDisplayed()
    }

    @Test
    fun `the fit step sets for home by default and for the lock screen when chosen`() {
        // Given a photo being framed
        val calls = Calls()
        setContent(WallpaperUiState(picked = bitmap(300, 400)), calls)
        composeRule.onNodeWithTag("wallpaper_fit_set").assertIsEnabled()

        // When set with the default choice
        composeRule.onNodeWithTag("wallpaper_fit_set").performClick()

        // Then it is for Home only, with the part of the photo in view
        assertEquals(false, calls.confirmedLock)
        assertNotNull(calls.confirmedRect)

        // And after choosing Home and lock it is for both
        composeRule.onNodeWithTag("wallpaper_fit_home_lock").performClick()
        composeRule.onNodeWithTag("wallpaper_fit_set").performClick()
        assertEquals(true, calls.confirmedLock)
    }

    @Test
    fun `cancelling the fit step reports it`() {
        // Given a photo being framed
        val calls = Calls()
        setContent(WallpaperUiState(picked = bitmap(300, 400)), calls)

        // When cancelled
        composeRule.onNodeWithTag("wallpaper_fit_cancel").performClick()

        // Then it is reported and nothing was confirmed
        assertEquals(1, calls.cancelled)
        assertNull(calls.confirmedLock)
    }

    /** A landscape photo framed in the screen-shaped window: it can be moved sideways and zoomed. */
    private fun fitStep(calls: Calls) = setContent(WallpaperUiState(picked = bitmap(1200, 600)), calls)

    private fun confirm() = composeRule.onNodeWithTag("wallpaper_fit_set").performClick()

    @Test
    fun `a long drag moves the framed part by the whole distance`() {
        // Given a photo wider than the window, framed at its centre
        val calls = Calls()
        fitStep(calls)

        // When it is dragged left across most of the window in one continuous gesture
        composeRule.onNodeWithTag("wallpaper_fit_window").performTouchInput {
            swipe(start = Offset(width * 0.8f, centerY), end = Offset(width * 0.2f, centerY), durationMillis = 800)
        }
        confirm()

        // Then the framed part moved right by about 60% of its own width, not just the first step of the drag
        val rect = calls.confirmedRect!!
        val centredLeft = (1200f - rect.width()) / 2f
        val expected = 0.6f * rect.width()
        assertTrue("moved ${rect.left - centredLeft} of about $expected", rect.left - centredLeft in (expected * 0.85f)..(expected * 1.15f))
    }

    @Test
    fun `pinching apart zooms in and double tap resets it`() {
        // Given a photo framed to cover the window, which shows all of its height
        val calls = Calls()
        fitStep(calls)

        // When two fingers spread apart
        composeRule.onNodeWithTag("wallpaper_fit_window").performTouchInput {
            pinch(
                start0 = Offset(width * 0.4f, centerY), end0 = Offset(width * 0.2f, centerY),
                start1 = Offset(width * 0.6f, centerY), end1 = Offset(width * 0.8f, centerY),
                durationMillis = 600,
            )
        }
        confirm()

        // Then a smaller part of the photo is shown
        assertTrue("height ${calls.confirmedRect!!.height()}", calls.confirmedRect!!.height() < 300f)

        // And a double tap shows the whole height again
        composeRule.onNodeWithTag("wallpaper_fit_window").performTouchInput { doubleClick() }
        confirm()
        assertEquals(600f, calls.confirmedRect!!.height(), 1f)
    }

    @Test
    fun `the credit of a daily photo shows on the framed photo exactly as given`() {
        // Given a daily photo with a credit being framed
        setContent(WallpaperUiState(picked = bitmap(1200, 600), credit = "© lightphoto/Getty Image"), Calls())

        // Then the credit shows as given
        composeRule.onNodeWithTag("wallpaper_fit_credit").assertIsDisplayed()
        composeRule.onNodeWithTag("wallpaper_fit_credit").assertTextContains("© lightphoto/Getty Image")
    }

    @Test
    fun `a photo the user chose has no credit footer`() {
        // Given a chosen photo with no credit
        setContent(WallpaperUiState(picked = bitmap(1200, 600)), Calls())

        // Then there is no footer
        composeRule.onNodeWithTag("wallpaper_fit_credit").assertDoesNotExist()
    }

    @Test
    fun `a loader covers the screen while the wallpaper is being set`() {
        // Given a photo being set as the wallpaper
        setContent(WallpaperUiState(picked = bitmap(1200, 600), isWorking = true), Calls())

        // Then the loader shows with its message
        composeRule.onNodeWithTag("wallpaper_working").assertIsDisplayed()
        composeRule.onNodeWithText("Setting your wallpaper…").assertIsDisplayed()
    }

    @Test
    fun `the loader says saving when only a copy is being stored`() {
        // Given a screenshot being stored
        setContent(WallpaperUiState(picked = bitmap(1200, 600), isWorking = true, copyOnly = true), Calls())
        composeRule.onNodeWithText("Saving…").assertIsDisplayed()
    }

    @Test
    fun `the loader says opening while a chosen photo is being read`() {
        // Given a photo being read, before there is one to frame
        setContent(WallpaperUiState(isWorking = true), Calls())

        // Then the loader shows with its own message
        composeRule.onNodeWithTag("wallpaper_working").assertIsDisplayed()
        composeRule.onNodeWithText("Opening the photo…").assertIsDisplayed()
    }

    @Test
    fun `there is no loader when nothing is being done`() {
        // Given a photo being framed and nothing running
        setContent(WallpaperUiState(picked = bitmap(1200, 600)), Calls())

        // Then no loader shows
        composeRule.onNodeWithTag("wallpaper_working").assertDoesNotExist()
    }
}
