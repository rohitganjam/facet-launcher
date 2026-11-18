package com.facetlauncher.app.ui.home

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.performClick
import com.facetlauncher.app.ui.theme.FacetLauncherTheme
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

class WallpaperCopyPillTest {

    @get:Rule
    val composeRule = createComposeRule()

    private val shown = mutableListOf<String>()
    private var opened = 0
    private var offer by mutableStateOf<WallpaperCopyOffer?>(WallpaperCopyOffer("1080x2092"))
    private var ready by mutableStateOf(true)

    private fun setHost() {
        composeRule.setContent {
            FacetLauncherTheme { WallpaperCopyPillHost(offer = offer, ready = ready, onShow = { shown += it }, onOpen = { opened++ }) }
        }
        composeRule.waitForIdle()
    }

    @Test
    fun pillStaysAwayUntilHomeIsReady() {
        // Given a screen needing a copy but a Home that is not at rest
        ready = false
        setHost()

        // Then there is no pill
        composeRule.onNodeWithTag("wallpaper_copy_pill").assertDoesNotExist()

        // When Home is ready
        ready = true
        composeRule.waitForIdle()

        // Then the pill shows and reports its screen once
        composeRule.onNodeWithTag("wallpaper_copy_pill").assertExists()
        assertEquals(listOf("1080x2092"), shown)
    }

    @Test
    fun noPillWithoutAScreenNeedingACopy() {
        // Given no screen needs a copy
        offer = null
        setHost()

        // Then nothing shows
        composeRule.onNodeWithTag("wallpaper_copy_pill").assertDoesNotExist()
    }

    @Test
    fun tappingThePillOpensTheCaptureScreen() {
        // Given the pill showing
        setHost()

        // When it is tapped
        composeRule.onNodeWithTag("wallpaper_copy_pill").performClick()
        composeRule.waitForIdle()

        // Then the capture screen is opened and the pill goes
        assertEquals(1, opened)
        composeRule.waitUntil(timeoutMillis = 3_000) { composeRule.onAllNodesWithTagCount("wallpaper_copy_pill") == 0 }
    }

    @Test
    fun theWordsAskToCaptureTheWallpaperForGlassEffects() {
        // Given a view with no capture
        setHost()

        // Then the pill says so, whichever orientation it is
        composeRule.onNodeWithContentDescription("Capture wallpaper for glass effects. Opens the capture screen").assertExists()
    }

    private fun androidx.compose.ui.test.junit4.ComposeContentTestRule.onAllNodesWithTagCount(tag: String) =
        onAllNodes(androidx.compose.ui.test.hasTestTag(tag)).fetchSemanticsNodes().size
}
