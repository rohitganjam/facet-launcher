package com.facetlauncher.app.ui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.down
import androidx.compose.ui.test.getUnclippedBoundsInRoot
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.moveBy
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.up
import com.facetlauncher.app.ui.theme.FacetLauncherTheme
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class SwipeBetweenTabsTest {

    @get:Rule
    val composeRule = createComposeRule()

    private val keys = listOf("personal", "work", "other")
    private var selected by mutableStateOf("personal")

    private fun setContent() {
        composeRule.setContent {
            FacetLauncherTheme {
                SwipeTabs(
                    keys = keys,
                    selected = selected,
                    onSelect = { selected = it },
                    modifier = Modifier.fillMaxSize().testTag("tabs"),
                    startExclusion = androidx.compose.ui.unit.Dp.Hairline,
                    endExclusion = androidx.compose.ui.unit.Dp.Hairline,
                ) { key ->
                    Box(Modifier.fillMaxSize().testTag("content_$key")) { Text(key) }
                }
            }
        }
    }

    @Test
    fun swipingLeftMovesToTheNextTabAndTheContentSlidesBackToRest() {
        // Given the first of three tabs
        setContent()

        // When swiping left across the content
        composeRule.onNodeWithTag("tabs").performTouchInput {
            down(center)
            moveBy(Offset(-150f, 0f))
            moveBy(Offset(-150f, 0f))
            up()
        }

        // Then the next tab shows, filling the area at rest
        composeRule.waitUntil(timeoutMillis = 3_000) { selected == "work" }
        composeRule.waitForIdle()
        assertEquals(0f, composeRule.onNodeWithTag("content_work").getUnclippedBoundsInRoot().left.value, 1f)
        composeRule.onNodeWithTag("content_work").assertIsDisplayed()
    }

    @Test
    fun theNextTabIsAlreadyBesideTheCurrentOneWhileDragging() {
        // Given a drag held part way across
        setContent()
        composeRule.mainClock.autoAdvance = false
        composeRule.onNodeWithTag("tabs").performTouchInput {
            down(center)
            moveBy(Offset(-100f, 0f))
            moveBy(Offset(-100f, 0f))
        }
        composeRule.mainClock.advanceTimeByFrame()

        // Then the next tab's content exists, just to the right of the current one
        composeRule.onNodeWithTag("content_work").assertExists()
        val current = composeRule.onNodeWithTag("content_personal").getUnclippedBoundsInRoot()
        val next = composeRule.onNodeWithTag("content_work").getUnclippedBoundsInRoot()
        assertTrue(next.left > current.left)

        composeRule.onNodeWithTag("tabs").performTouchInput { up() }
        composeRule.mainClock.autoAdvance = true
    }

    @Test
    fun pickingATabElsewhereSlidesItsContentIn() {
        // Given the first tab showing
        setContent()

        // When a tab is chosen from the tab row
        composeRule.runOnIdle { selected = "other" }

        // Then its content slides in and ends filling the area, with the first gone
        composeRule.waitUntil(timeoutMillis = 3_000) { composeRule.onAllNodesWithTag("content_personal").fetchSemanticsNodes().isEmpty() }
        composeRule.waitForIdle()
        assertEquals(0f, composeRule.onNodeWithTag("content_other").getUnclippedBoundsInRoot().left.value, 1f)
    }

    @Test
    fun swipingRightOnTheFirstTabStaysPut() {
        setContent()

        composeRule.onNodeWithTag("tabs").performTouchInput {
            down(center)
            moveBy(Offset(150f, 0f))
            moveBy(Offset(150f, 0f))
            up()
        }
        composeRule.waitForIdle()

        assertEquals("personal", selected)
    }

    @Test
    fun aShortSwipeSpringsBackWithoutChangingTab() {
        setContent()

        composeRule.onNodeWithTag("tabs").performTouchInput {
            down(center)
            moveBy(Offset(-20f, 0f))
            moveBy(Offset(-20f, 0f))
            up()
        }
        composeRule.waitForIdle()

        assertTrue(selected == "personal")
    }
}
