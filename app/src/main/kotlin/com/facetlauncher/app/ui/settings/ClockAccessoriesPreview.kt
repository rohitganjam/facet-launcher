package com.facetlauncher.app.ui.settings

import com.facetlauncher.app.data.model.ClockAlignment
import androidx.compose.ui.Alignment
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.unit.dp
import com.facetlauncher.app.ui.home.clock.ClockDisplay
import com.facetlauncher.app.ui.home.clock.ClockWeatherAccessory
import com.facetlauncher.app.domain.WeatherUnits
import com.facetlauncher.app.ui.home.weather.WeatherIconKind
import com.facetlauncher.app.ui.home.weather.temperatureText
import com.facetlauncher.app.ui.theme.Hairline
import com.facetlauncher.app.ui.theme.Surface
import com.facetlauncher.app.ui.theme.resolve
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId

private const val SAMPLE_TEMPERATURE_C = 21.0
private const val SAMPLE_BATTERY_PERCENT = 64
private const val SAMPLE_ALARM_HOUR = 7
private val SAMPLE_NOW: LocalDateTime = LocalDateTime.of(2026, 8, 27, 9, 5)

/**
 * The clock as it is set up for this scope, with sample alarm, battery and weather, so the switches and the icon style show
 * their effect straight away. A facet that inherits shows what it inherits.
 */
@Composable
internal fun ClockAccessoriesPreview(uiState: ClockAccessoriesUiState, modifier: Modifier = Modifier) {
    val clock = uiState.clockStyle
    val settings = uiState.settings
    val alarmMillis = remember {
        LocalDate.now().plusDays(1).atTime(SAMPLE_ALARM_HOUR, 0).atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
    }
    val color = clock.colorOption.resolve()
    val temperature = temperatureText(SAMPLE_TEMPERATURE_C, WeatherUnits.resolve(settings.temperatureUnit, LocalConfiguration.current.locales[0]))
    val weather = if (clock.showWeather) {
        remember(clock.weatherIconStyle, temperature) {
            ClockWeatherAccessory(WeatherIconKind.PARTLY_SUN, temperature, "$temperature, Partly cloudy", clock.weatherIconStyle) {}
        }
    } else {
        null
    }
    val shape = MaterialTheme.shapes.medium
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(shape)
            .background(Surface, shape)
            .border(1.dp, Hairline, shape)
            .testTag("clock_accessories_preview")
            .padding(16.dp),
        horizontalAlignment = Alignment.Start,
    ) {
        ClockDisplay(
            templateId = clock.templateId,
            fontOption = clock.fontOption,
            now = SAMPLE_NOW,
            use24HourTime = clock.use24HourTime,
            showMeridiem = clock.showMeridiem,
            textColor = color,
            mutedTextColor = color,
            accentColor = clock.accentColorOption.resolve(),
            nextAlarmMillis = alarmMillis.takeIf { clock.showAlarm },
            batteryPercent = SAMPLE_BATTERY_PERCENT.takeIf { clock.showBattery },
            weather = weather,
            dateStyle = clock.dateStyle,
            launcherFontOption = settings.launcherFontOption,
            clockAlignment = ClockAlignment.LEFT,
        )
    }
}
