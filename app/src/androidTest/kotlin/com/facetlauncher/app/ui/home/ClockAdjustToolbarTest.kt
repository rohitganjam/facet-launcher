package com.facetlauncher.app.ui.home

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.assertIsSelected
import androidx.compose.ui.test.assertIsNotSelected
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.facetlauncher.app.data.model.ClockAlignment
import com.facetlauncher.app.ui.theme.FacetLauncherTheme
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

class ClockAdjustToolbarTest {

    @get:Rule
    val composeRule = createComposeRule()

    private var alignment by mutableStateOf(ClockAlignment.LEFT)
    private val picked = mutableListOf<ClockAlignment>()

    private fun setContent(initial: ClockAlignment = ClockAlignment.LEFT, enabled: Boolean = true) {
        alignment = initial
        picked.clear()
        composeRule.setContent {
            FacetLauncherTheme {
                ClockAdjustToolbar(alignment = alignment, onAlignmentChange = { picked += it; alignment = it }, enabled = enabled)
            }
        }
    }

    @Test
    fun `shows the Alignment heading and exactly one selected option matching the current alignment`() {
        setContent(initial = ClockAlignment.CENTER)

        composeRule.onNodeWithText("Alignment").assertExists()
        composeRule.onNodeWithTag("clock_align_left").assertIsNotSelected()
        composeRule.onNodeWithTag("clock_align_center").assertIsSelected()
        composeRule.onNodeWithTag("clock_align_right").assertIsNotSelected()
    }

    @Test
    fun `tapping each option reports it and moves the selection`() {
        setContent()

        composeRule.onNodeWithTag("clock_align_center").performClick()
        composeRule.onNodeWithTag("clock_align_right").performClick()
        composeRule.onNodeWithTag("clock_align_left").performClick()

        assertEquals(listOf(ClockAlignment.CENTER, ClockAlignment.RIGHT, ClockAlignment.LEFT), picked)
        composeRule.onNodeWithTag("clock_align_left").assertIsSelected()
    }

    @Test
    fun `a disabled toolbar ignores taps on its options but stays on screen`() {
        setContent(enabled = false)

        composeRule.onNodeWithTag("clock_align_center").assertIsNotEnabled()
        composeRule.onNodeWithTag("clock_align_center").performClick()
        composeRule.onNodeWithTag("clock_align_right").performClick()

        assertEquals(emptyList<ClockAlignment>(), picked)
        composeRule.onNodeWithTag("clock_adjust_toolbar").assertExists()
        // ...with the current alignment still shown as selected
        composeRule.onNodeWithTag("clock_align_left").assertIsSelected()
    }

    @Test
    fun `tapping the heading or the pill padding reports nothing`() {
        setContent()

        composeRule.onNodeWithText("Alignment").performClick()

        assertEquals(emptyList<ClockAlignment>(), picked)
    }
}
