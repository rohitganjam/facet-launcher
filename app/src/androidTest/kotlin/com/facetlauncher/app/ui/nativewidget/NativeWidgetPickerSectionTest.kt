package com.facetlauncher.app.ui.nativewidget

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import com.facetlauncher.app.ui.theme.FacetLauncherTheme
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

class NativeWidgetPickerSectionTest {

    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun theClockIsOfferedFirstWhenAskedForAndPickingItReports() {
        // Given the section asked to offer the clock
        var picked = 0
        composeRule.setContent {
            FacetLauncherTheme { NativeWidgetPickerSection(types = ALL_NATIVE_WIDGETS, onSelect = {}, showClock = true, onClockSelect = { picked++ }) }
        }

        // Then the clock sits beside the Facet widgets, and choosing it reports
        composeRule.onNodeWithTag("native_widget_option_CLOCK").assertIsDisplayed()
        composeRule.onNodeWithTag("native_widget_option_WEATHER").assertIsDisplayed()
        composeRule.onNodeWithTag("native_widget_option_CLOCK").performClick()
        assertEquals(1, picked)
    }

    @Test
    fun theClockIsLeftOutByDefault() {
        // Given the section as the Hub uses it
        composeRule.setContent { FacetLauncherTheme { NativeWidgetPickerSection(types = ALL_NATIVE_WIDGETS, onSelect = {}) } }

        // Then there is no clock option
        composeRule.onNodeWithTag("native_widget_option_CLOCK").assertDoesNotExist()
        composeRule.onNodeWithTag("native_widget_option_WEATHER").assertIsDisplayed()
    }
}
