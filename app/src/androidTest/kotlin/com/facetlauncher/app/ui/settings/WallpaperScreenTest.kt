package com.facetlauncher.app.ui.settings

import android.graphics.Bitmap
import android.graphics.RectF
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertTextContains
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.facetlauncher.app.ui.theme.FacetLauncherTheme
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Rule
import org.junit.Test

class WallpaperScreenTest {

    @get:Rule
    val composeRule = createComposeRule()

    private class Calls {
        var choseImage = 0
        var setSystem = 0
        var accepted = 0
        var screenshot = 0
        var takeScreenshot = 0
        var declined = 0
        var removed = 0
        var cancelled = 0
        var confirmedRect: RectF? = null
        var confirmedLock: Boolean? = null
    }

    private fun bitmap(width: Int, height: Int) = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)

    private fun setContent(state: WallpaperUiState, calls: Calls) {
        composeRule.setContent {
            FacetLauncherTheme {
                WallpaperContent(
                    uiState = state,
                    onBack = {},
                    onChooseImage = { calls.choseImage++ },
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
        assertEquals(1, calls.choseImage)
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
        composeRule.onNodeWithTag("wallpaper_screenshot_link").assertIsDisplayed()
        composeRule.onNodeWithTag("wallpaper_screenshot_link").assertTextContains("Already in use", substring = true)
    }

    @Test
    fun `with no copy or a stale one the current wallpaper card is not marked in use`() {
        // Given a stale copy
        setContent(WallpaperUiState(copy = bitmap(10, 20), stale = true), Calls())

        // Then the card offers the screenshot instead
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
}
