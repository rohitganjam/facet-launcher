package com.facetlauncher.app.ui.home.clock

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.test.junit4.createComposeRule
import com.facetlauncher.app.data.model.ClockFontOption
import com.facetlauncher.app.data.model.ClockTemplateId
import com.facetlauncher.app.ui.theme.FacetLauncherTheme
import java.time.LocalDateTime
import org.junit.Rule
import org.junit.Test

class ClockDisplayTest {

    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun everyTemplateRendersWithAlarmAndBatteryAccessories() {
        // Given each template id in turn, with both accessories present
        var templateId by mutableStateOf(ClockTemplateId.entries.first())
        composeRule.setContent {
            FacetLauncherTheme {
                ClockDisplay(
                    templateId = templateId,
                    fontOption = ClockFontOption.entries.first(),
                    now = LocalDateTime.of(2026, 10, 22, 9, 41),
                    use24HourTime = false,
                    showMeridiem = true,
                    textColor = Color.Black,
                    mutedTextColor = Color.Gray,
                    nextAlarmMillis = 1_800_000_000_000L,
                    batteryPercent = 80,
                    isCharging = true,
                )
            }
        }

        // When switching through all of them
        ClockTemplateId.entries.forEach { id ->
            templateId = id
            composeRule.waitForIdle()
        }

        // Then none crashes while rendering (a missing renderer or bad template wiring would throw)
    }
}
