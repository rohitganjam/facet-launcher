package com.facetlauncher.app.ui.home

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.getUnclippedBoundsInRoot
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.swipeUp
import androidx.compose.ui.unit.dp
import com.facetlauncher.app.ui.theme.FacetLauncherTheme
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class HomeLandscapeClockPaneTest {

    @get:Rule
    val composeRule = createComposeRule()

    private var reportedBounds: Rect? = null
    private var leftoverScrollPx = 0f

    // A 200dp tall window with a clock slot of twenty 40dp rows, far taller than the pane
    private fun setLandscape(withOuterScrollConnection: Boolean = false) {
        val outer = object : NestedScrollConnection {
            override fun onPostScroll(consumed: Offset, available: Offset, source: NestedScrollSource): Offset {
                leftoverScrollPx += kotlin.math.abs(available.y)
                return Offset.Zero
            }
        }
        composeRule.setContent {
            FacetLauncherTheme {
                Box(Modifier.size(width = 600.dp, height = 200.dp).then(if (withOuterScrollConnection) Modifier.nestedScroll(outer) else Modifier)) {
                    HomeLandscapeLayout(
                        hasDock = false,
                        clockPaneAlignment = Alignment.TopStart,
                        clockPaneWidth = 240.dp,
                        clock = { Column { repeat(20) { Text("row $it", Modifier.height(40.dp)) } } },
                        apps = {},
                        onClockPaneBoundsChange = { reportedBounds = it },
                        dock = {},
                    )
                }
            }
        }
    }

    @Test
    fun aClockPaneTallerThanTheWindowScrollsItsLastRowIntoView() {
        // Given a clock pane whose content is far taller than the window
        setLandscape()

        // When swiping up inside the pane
        val before = composeRule.onNodeWithText("row 19").getUnclippedBoundsInRoot().top
        repeat(12) { composeRule.onNodeWithTag("home_clock_pane").performTouchInput { swipeUp() } }

        // Then the last row has scrolled up into view
        val after = composeRule.onNodeWithText("row 19").getUnclippedBoundsInRoot().top
        assertTrue("row 19 moved from $before to $after", after < before - 100.dp)
        composeRule.onNodeWithText("row 19").assertIsDisplayed()
    }

    @Test
    fun theClockPaneReportsItsBounds() {
        // Given a landscape layout
        setLandscape()
        composeRule.waitForIdle()

        // Then its clock pane reported a rectangle as wide as the pane
        assertNotNull(reportedBounds)
        assertEquals(with(composeRule.density) { 240.dp.toPx() }, reportedBounds!!.width, 1f)
    }

    @Test
    fun scrollPastTheEndOfTheClockPaneNeverReachesAnOuterScrollConnection() {
        // Given an outer connection standing in for Home's swipe-up-to-open-drawer handoff
        setLandscape(withOuterScrollConnection = true)

        // When swiping up well past the end of the pane's content
        repeat(8) { composeRule.onNodeWithTag("home_clock_pane").performTouchInput { swipeUp() } }

        // Then the pane swallowed the leftover, so the outer connection saw none
        assertEquals(0f, leftoverScrollPx, 0.5f)
    }

    @Test
    fun aClockPaneThatFitsDoesNotScroll() {
        // Given a clock slot shorter than the window
        composeRule.setContent {
            FacetLauncherTheme {
                Box(Modifier.size(width = 600.dp, height = 200.dp)) {
                    HomeLandscapeLayout(
                        hasDock = false,
                        clockPaneAlignment = Alignment.TopStart,
                        clockPaneWidth = 240.dp,
                        clock = { Text("only row", Modifier.height(40.dp)) },
                        apps = {},
                        onClockPaneBoundsChange = { reportedBounds = it },
                        dock = {},
                    )
                }
            }
        }

        // When swiping up in the pane
        val before = composeRule.onNodeWithText("only row").getUnclippedBoundsInRoot()
        composeRule.onNodeWithTag("home_clock_pane").performTouchInput { swipeUp() }

        // Then the row stays exactly where it was
        val after = composeRule.onNodeWithText("only row").getUnclippedBoundsInRoot()
        assertEquals(before.top.value, after.top.value, 0.5f)
    }
}
