package com.facetlauncher.app.ui.nativewidget

import androidx.compose.ui.platform.LocalConfiguration
import android.content.res.Configuration
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import com.facetlauncher.app.R
import com.facetlauncher.app.data.model.WeatherHour
import com.facetlauncher.app.domain.WeatherDay
import com.facetlauncher.app.domain.WeatherState
import com.facetlauncher.app.domain.WidgetTier
import com.facetlauncher.app.domain.conditionOf
import com.facetlauncher.app.domain.isNightSymbol
import com.facetlauncher.app.ui.home.weather.ResolvedWeatherUnits
import com.facetlauncher.app.ui.home.weather.WeatherIcon
import com.facetlauncher.app.ui.home.weather.WeatherUiState
import com.facetlauncher.app.ui.home.weather.conditionName
import com.facetlauncher.app.ui.home.weather.temperatureText
import com.facetlauncher.app.ui.home.weather.weatherIconKind
import com.facetlauncher.app.ui.theme.Accent
import com.facetlauncher.app.ui.theme.FacetLauncherTheme
import com.facetlauncher.app.ui.theme.Ink
import com.facetlauncher.app.ui.theme.Muted
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle as DateTextStyle

private val HOUR_COLUMN_WIDTH = 52.dp
private val DAY_ROW_HEIGHT = 30.dp

/** Weather in the box it is given: the icon and temperature, then the condition and range, then the hours and the days ahead. */
@Composable
internal fun WeatherNativeWidget(tier: WidgetTier, size: DpSize, ui: WeatherUiState, onOpen: () -> Unit, locationMissing: Boolean, onAllowLocation: () -> Unit, modifier: Modifier = Modifier) {
    val weather = ui.weather
    Box(modifier = modifier.fillMaxSize().clickable(onClick = onOpen), contentAlignment = Alignment.Center) {
        if (weather == null && ui.isRefreshing) {
            // A refresh from nothing is running, so the permission or empty text would only be stale until it ends.
            CircularProgressIndicator(modifier = Modifier.size(24.dp).testTag("weather_loading"), strokeWidth = 2.dp, color = Accent)
        } else if (weather == null && locationMissing) {
            AccessPrompt(stringResource(R.string.native_widget_location_off), onAllowLocation, "weather_location_prompt")
        } else if (weather == null) {
            Text(stringResource(R.string.weather_empty), style = MaterialTheme.typography.bodyMedium, color = Muted, maxLines = 2, overflow = TextOverflow.Ellipsis)
        } else {
            when (tier) {
                WidgetTier.SMALL -> SmallWeather(weather, ui)
                WidgetTier.MEDIUM -> MediumWeather(weather, ui, size)
                WidgetTier.LARGE -> LargeWeather(weather, ui, size)
            }
        }
    }
}

@Composable
private fun SmallWeather(weather: WeatherState, ui: WeatherUiState) {
    Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(2.dp)) {
        NowIcon(weather, ui, Modifier.size(28.dp))
        Text(temperatureText(weather.now.temperatureC, ui.units.temperature), style = MaterialTheme.typography.titleMedium, color = Ink, maxLines = 1)
    }
}

@Composable
private fun MediumWeather(weather: WeatherState, ui: WeatherUiState, size: DpSize) {
    val tall = size.height >= 100.dp
    Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        NowIcon(weather, ui, Modifier.size(if (tall) 48.dp else 36.dp))
        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(temperatureText(weather.now.temperatureC, ui.units.temperature), style = MaterialTheme.typography.headlineMedium, color = Ink, maxLines = 1)
            ConditionLine(weather, ui)
            if (tall) PlaceLine(weather)
        }
    }
}

@Composable
private fun LargeWeather(weather: WeatherState, ui: WeatherUiState, size: DpSize) {
    val hoursShown = (size.width / HOUR_COLUMN_WIDTH).toInt().coerceIn(1, weather.hourly.size.coerceAtLeast(1))
    val daysShown = ((size.height - 150.dp) / DAY_ROW_HEIGHT).toInt().coerceIn(0, weather.days.size)
    val pattern = if (ui.use24HourTime) "HH" else "ha"
    val locale = LocalConfiguration.current.locales[0]
    val hourFormat = remember(pattern, weather.zone, locale) { DateTimeFormatter.ofPattern(pattern, locale).withZone(weather.zone) }
    Column(modifier = Modifier.fillMaxSize(), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            NowIcon(weather, ui, Modifier.size(48.dp))
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(temperatureText(weather.now.temperatureC, ui.units.temperature), style = MaterialTheme.typography.headlineMedium, color = Ink, maxLines = 1)
                ConditionLine(weather, ui)
            }
            PlaceLine(weather)
        }
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            weather.hourly.take(hoursShown).forEachIndexed { index, hour -> HourCell(hour, isNow = index == 0, hourFormat, ui) }
        }
        weather.days.take(daysShown).forEach { DayRow(it, ui) }
    }
}

@Composable
private fun NowIcon(weather: WeatherState, ui: WeatherUiState, modifier: Modifier = Modifier) {
    val condition = conditionOf(weather.now.symbolCode, weather.now.precipitationMm)
    WeatherIcon(weatherIconKind(condition, isNightSymbol(weather.now.symbolCode)), Ink, modifier, ui.iconStyle)
}

@Composable
private fun ConditionLine(weather: WeatherState, ui: WeatherUiState) {
    val condition = conditionOf(weather.now.symbolCode, weather.now.precipitationMm)
    val today = weather.days.firstOrNull()
    val range = today?.let {
        stringResource(R.string.weather_sheet_high_low, temperatureText(it.highC, ui.units.temperature), temperatureText(it.lowC, ui.units.temperature))
    }
    val text = listOfNotNull(condition?.let { conditionName(it) }, range).joinToString(" · ")
    Text(text, style = MaterialTheme.typography.bodySmall, color = Muted, maxLines = 2, overflow = TextOverflow.Ellipsis)
}

@Composable
private fun PlaceLine(weather: WeatherState) {
    val name = weather.placeName ?: return
    val text = if (weather.placeNameIsApproximate) stringResource(R.string.weather_place_near, name) else name
    Text(text, style = MaterialTheme.typography.labelMedium, color = Muted, maxLines = 1, overflow = TextOverflow.Ellipsis)
}

@Composable
private fun HourCell(hour: WeatherHour, isNow: Boolean, format: DateTimeFormatter, ui: WeatherUiState) {
    val condition = conditionOf(hour.symbolCode, hour.precipitationMm)
    Column(modifier = Modifier.width(HOUR_COLUMN_WIDTH), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(if (isNow) stringResource(R.string.weather_sheet_now) else format.format(hour.time), style = MaterialTheme.typography.labelSmall, color = Muted, maxLines = 1)
        WeatherIcon(weatherIconKind(condition, isNightSymbol(hour.symbolCode)), Ink, Modifier.size(22.dp), ui.iconStyle)
        Text(temperatureText(hour.temperatureC, ui.units.temperature), style = MaterialTheme.typography.labelMedium, color = Ink, maxLines = 1)
    }
}

@Composable
private fun DayRow(day: WeatherDay, ui: WeatherUiState) {
    val condition = conditionOf(day.symbolCode, day.precipitationMm)
    val locale = LocalConfiguration.current.locales[0]
    Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Text(day.date.dayOfWeek.getDisplayName(DateTextStyle.SHORT, locale), style = MaterialTheme.typography.bodySmall, color = Ink, modifier = Modifier.width(44.dp), maxLines = 1)
        WeatherIcon(weatherIconKind(condition, night = false), Ink, Modifier.size(20.dp), ui.iconStyle)
        Spacer(Modifier.weight(1f))
        Text(
            stringResource(R.string.weather_sheet_high_low, temperatureText(day.highC, ui.units.temperature), temperatureText(day.lowC, ui.units.temperature)),
            style = MaterialTheme.typography.bodySmall,
            color = Muted,
            maxLines = 1,
        )
    }
}

internal fun previewWeatherState(): WeatherUiState {
    val zone = java.time.ZoneId.of("UTC")
    val start = java.time.Instant.parse("2026-06-01T09:00:00Z")
    val hours = (0 until 8).map {
        WeatherHour(start.plusSeconds(3600L * it), 18.0 + it, 60.0, 1012.0, 40.0, 3.0, 220.0, if (it == 3) 0.6 else 0.0, if (it < 3) "partlycloudy_day" else "rain")
    }
    val days = (0 until 4).map { WeatherDay(java.time.LocalDate.of(2026, 6, 1 + it), 12.0 + it, 21.0 + it, "partlycloudy_day", 0.0) }
    return WeatherUiState(
        weather = WeatherState("Oslo", false, hours.first(), hours, days, com.facetlauncher.app.domain.RainSummary.Dry, start, false, zone),
        units = ResolvedWeatherUnits(),
    )
}

@Preview(name = "Small", showBackground = true, widthDp = 72, heightDp = 72)
@Preview(name = "Small dark", showBackground = true, widthDp = 72, heightDp = 72, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun WeatherSmallPreview() = FacetLauncherTheme { NativeWidgetPreviewBox(com.facetlauncher.app.data.model.NativeWidgetType.WEATHER, 72, 72) }

@Preview(name = "Medium", showBackground = true, widthDp = 148, heightDp = 148)
@Preview(name = "Medium dark", showBackground = true, widthDp = 148, heightDp = 148, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun WeatherMediumPreview() = FacetLauncherTheme { NativeWidgetPreviewBox(com.facetlauncher.app.data.model.NativeWidgetType.WEATHER, 148, 148) }

@Preview(name = "Large", showBackground = true, widthDp = 300, heightDp = 300)
@Preview(name = "Large dark", showBackground = true, widthDp = 300, heightDp = 300, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun WeatherLargePreview() = FacetLauncherTheme { NativeWidgetPreviewBox(com.facetlauncher.app.data.model.NativeWidgetType.WEATHER, 300, 300) }
