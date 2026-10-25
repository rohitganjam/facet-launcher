package com.facetlauncher.app.ui.home

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import com.facetlauncher.app.ui.theme.FacetLauncherTheme
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

class WhatsNewPillTest {

    @get:Rule
    val composeRule = createComposeRule()

    private var shown = 0
    private var opened = 0
    private var version by mutableStateOf<String?>("9.9.9")
    private var ready by mutableStateOf(true)

    private fun setHost() {
        composeRule.setContent {
            FacetLauncherTheme { WhatsNewPillHost(version = version, ready = ready, onShow = { shown++ }, onOpen = { opened++ }) }
        }
        composeRule.waitForIdle()
    }

    @Test
    fun pillStaysAwayUntilHomeIsReady() {
        // Given an announced version but a Home that is not ready (a first-run prompt is still pending)
        ready = false
        setHost()

        // Then there is no pill and nothing was marked seen
        composeRule.onNodeWithTag("whats_new_pill").assertDoesNotExist()
        assertEquals(0, shown)

        // When Home becomes ready
        ready = true
        composeRule.waitForIdle()

        // Then the pill drops in, once
        composeRule.onNodeWithTag("whats_new_pill").assertExists()
        assertEquals(1, shown)
    }

    @Test
    fun pillStaysAfterTheVersionIsRetired() {
        // Given the pill showing
        setHost()
        composeRule.onNodeWithTag("whats_new_pill").assertExists()

        // When showing it retires the version (the announcement flow goes back to nothing)
        version = null
        composeRule.waitForIdle()

        // Then it stays for its few seconds, and was announced only once
        composeRule.onNodeWithTag("whats_new_pill").assertExists()
        assertEquals(1, shown)
    }

    @Test
    fun tappingThePillOpensWhatsNewAndHidesIt() {
        // Given the pill showing
        setHost()

        // When tapped
        composeRule.onNodeWithTag("whats_new_pill").performClick()

        // Then What's new opens and the pill leaves
        assertEquals(1, opened)
        composeRule.waitUntil(timeoutMillis = 3_000) { composeRule.onAllNodesWithTagCount("whats_new_pill") == 0 }
    }

    @Test
    fun pillLeavesOnItsOwnAfterAFewSeconds() {
        // Given the pill showing, with the test clock under our control
        composeRule.mainClock.autoAdvance = false
        setHost()
        composeRule.mainClock.advanceTimeBy(1_000)
        composeRule.onNodeWithTag("whats_new_pill").assertExists()

        // When ten seconds pass
        composeRule.mainClock.advanceTimeBy(10_000)

        // Then it has gone
        assertEquals(0, composeRule.onAllNodesWithTagCount("whats_new_pill"))
    }

    @Test
    fun noVersionMeansNoPill() {
        // Given nothing to announce
        version = null
        setHost()

        // Then no pill, and nothing marked
        composeRule.onNodeWithTag("whats_new_pill").assertDoesNotExist()
        assertEquals(0, shown)
    }

    private fun androidx.compose.ui.test.junit4.ComposeContentTestRule.onAllNodesWithTagCount(tag: String): Int =
        onAllNodes(androidx.compose.ui.test.hasTestTag(tag)).fetchSemanticsNodes().size
}
