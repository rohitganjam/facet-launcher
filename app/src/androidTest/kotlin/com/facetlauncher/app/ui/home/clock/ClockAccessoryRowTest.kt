package com.facetlauncher.app.ui.home.clock

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.text.font.FontFamily
import com.facetlauncher.app.ui.theme.Accent
import com.facetlauncher.app.ui.theme.FacetLauncherTheme
import com.facetlauncher.app.ui.theme.Muted
import org.junit.Rule
import org.junit.Test

class ClockAccessoryRowTest {

    @get:Rule
    val composeRule = createComposeRule()

    private fun setContent(nextAlarmText: String?, batteryPercent: Int?, isCharging: Boolean = false) {
        composeRule.setContent {
            FacetLauncherTheme {
                ClockAccessoryRow(
                    nextAlarmText = nextAlarmText,
                    batteryPercent = batteryPercent,
                    isCharging = isCharging,
                    family = FontFamily.Default,
                    mutedColor = Muted,
                    accentColor = Accent,
                )
            }
        }
    }

    @Test
    fun `renders both items when both are present`() {
        // Given both a next alarm and a battery reading
        setContent(nextAlarmText = "7:00", batteryPercent = 64)

        // When rendered
        // Then both items are displayed
        composeRule.onNodeWithText("7:00").assertIsDisplayed()
        composeRule.onNodeWithText("64%").assertIsDisplayed()
    }

    @Test
    fun `drops the alarm item when there is no next alarm`() {
        // Given no next alarm but a battery reading
        setContent(nextAlarmText = null, batteryPercent = 64)

        // When rendered
        // Then only the battery item shows — no orphaned separator or placeholder for the alarm
        composeRule.onNodeWithText("64%").assertIsDisplayed()
    }

    @Test
    fun `drops the battery item when battery is unknown`() {
        // Given a next alarm but no battery reading yet
        setContent(nextAlarmText = "7:00", batteryPercent = null)

        // When rendered
        // Then only the alarm item shows
        composeRule.onNodeWithText("7:00").assertIsDisplayed()
    }

    @Test
    fun `renders nothing when neither is present`() {
        // Given neither a next alarm nor a battery reading
        setContent(nextAlarmText = null, batteryPercent = null)

        // When rendered
        // Then neither item exists in the tree at all
        composeRule.onNodeWithText("7:00").assertDoesNotExist()
        composeRule.onNodeWithText("64%").assertDoesNotExist()
    }
}
