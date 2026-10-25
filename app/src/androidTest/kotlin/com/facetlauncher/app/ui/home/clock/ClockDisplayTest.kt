package com.facetlauncher.app.ui.home.clock

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import com.facetlauncher.app.data.model.ClockAlignment
import com.facetlauncher.app.data.model.ClockFontOption
import com.facetlauncher.app.data.model.ClockTemplateId
import com.facetlauncher.app.ui.home.weather.WeatherIconKind
import com.facetlauncher.app.ui.theme.FacetLauncherTheme
import java.time.LocalDateTime
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
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

    private fun setHourTile(alignment: ClockAlignment) {
        composeRule.setContent {
            FacetLauncherTheme {
                ClockDisplay(
                    templateId = ClockTemplateId.HOUR_TILE,
                    fontOption = ClockFontOption.entries.first(),
                    now = LocalDateTime.of(2026, 10, 22, 9, 41),
                    use24HourTime = false,
                    showMeridiem = false,
                    textColor = Color.Black,
                    mutedTextColor = Color.Gray,
                    nextAlarmMillis = 1_800_000_000_000L,
                    batteryPercent = 80,
                    weather = ClockWeatherAccessory(WeatherIconKind.PARTLY_SUN, "21°C", "21°C, Partly cloudy", onClick = {}),
                    clockAlignment = alignment,
                )
            }
        }
        composeRule.waitForIdle()
    }

    /** How far the weather text starts from the left edge of the weather item: about an icon's width when the icon leads, none when it follows. */
    private fun weatherTextInset(): Float {
        val item = composeRule.onNodeWithContentDescription("21°C, Partly cloudy").fetchSemanticsNode().boundsInRoot
        val text = composeRule.onNodeWithTag("clock_weather_text", useUnmergedTree = true).fetchSemanticsNode().boundsInRoot
        return text.left - item.left
    }

    @Test
    fun hourTileAccessoriesPutTheIconBeforeTheTextWhenLeftAligned() {
        // Given the hour tile, left aligned
        setHourTile(ClockAlignment.LEFT)

        // Then the icon comes first, so the text starts after it
        assertTrue("text inset ${weatherTextInset()}", weatherTextInset() > 8f)
    }

    @Test
    fun hourTileAccessoriesPutTheIconBeforeTheTextWhenCentred() {
        // Given the hour tile, centred
        setHourTile(ClockAlignment.CENTER)

        // Then the icon still comes first
        assertTrue("text inset ${weatherTextInset()}", weatherTextInset() > 8f)
    }

    @Test
    fun hourTileAccessoriesPutTheIconAfterTheTextWhenRightAligned() {
        // Given the hour tile, right aligned
        setHourTile(ClockAlignment.RIGHT)

        // Then the text comes first and the icon is shifted to the right of it
        assertEquals(0f, weatherTextInset(), 1f)
    }

    @Test
    fun hourTileAccessoriesAreRightAlignedWhenTheClockIsRightAligned() {
        // Given the hour tile, right aligned, with the alarm and weather items stacked
        setHourTile(ClockAlignment.RIGHT)

        // Then the weather item ends at the same right edge as the clock's accessory column
        val weather = composeRule.onNodeWithContentDescription("21°C, Partly cloudy").fetchSemanticsNode().boundsInRoot
        val battery = composeRule.onNodeWithText("80%", useUnmergedTree = true).fetchSemanticsNode().boundsInRoot
        assertTrue("weather ${weather.right} vs battery text ${battery.right}", weather.right >= battery.right - 1f)
    }
}
