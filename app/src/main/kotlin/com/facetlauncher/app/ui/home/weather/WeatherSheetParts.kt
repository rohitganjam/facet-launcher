package com.facetlauncher.app.ui.home.weather

import android.Manifest
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.background
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.platform.LocalConfiguration
import com.facetlauncher.app.data.model.WeatherIconStyle
import com.facetlauncher.app.R
import com.facetlauncher.app.data.model.RainUnit
import com.facetlauncher.app.data.model.WeatherHour
import com.facetlauncher.app.domain.RainIntensity
import com.facetlauncher.app.domain.RainSummary
import com.facetlauncher.app.domain.WeatherCondition
import com.facetlauncher.app.domain.WeatherDay
import com.facetlauncher.app.domain.WeatherState
import com.facetlauncher.app.domain.conditionOf
import com.facetlauncher.app.domain.isNightSymbol
import com.facetlauncher.app.ui.components.TextActionButton
import com.facetlauncher.app.ui.components.ThemedModalBottomSheet
import com.facetlauncher.app.ui.theme.Accent
import com.facetlauncher.app.ui.theme.FacetLauncherTheme
import com.facetlauncher.app.ui.theme.Hairline
import com.facetlauncher.app.ui.theme.Ink
import com.facetlauncher.app.ui.theme.Muted
import com.facetlauncher.app.ui.theme.SurfaceContainer
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle as DateTextStyle
import kotlin.math.roundToInt

private val HOUR_COLUMN_WIDTH = 68.dp
private val HOUR_ICON_SIZE = 24.dp

@Composable
internal fun HourlyStrip(weather: WeatherState, uiState: WeatherUiState, timeFormat: DateTimeFormatter) {
    val units = uiState.units
    Column(modifier = Modifier.fillMaxWidth().background(SurfaceContainer, MaterialTheme.shapes.medium).padding(vertical = 12.dp)) {
        Text(
            stringResource(R.string.weather_next_24_hours),
            style = MaterialTheme.typography.labelLarge,
            color = Muted,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 2.dp),
        )
        LazyRow(contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 6.dp), modifier = Modifier.padding(top = 8.dp)) {
            items(weather.hourly, key = { it.time.epochSecond }) { hour ->
                HourColumn(hour, isNow = hour === weather.hourly.first(), timeFormat, uiState)
            }
        }
        Row(modifier = Modifier.padding(horizontal = 12.dp).padding(top = 8.dp), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            Text(stringResource(R.string.weather_legend_rain, stringResource(rainUnitRes(units.rain))), style = MaterialTheme.typography.labelSmall, color = Muted)
            Text(stringResource(R.string.weather_legend_wind, stringResource(windUnitRes(units.wind))), style = MaterialTheme.typography.labelSmall, color = Muted)
        }
    }
}

@Composable
internal fun HourColumn(hour: WeatherHour, isNow: Boolean, timeFormat: DateTimeFormatter, uiState: WeatherUiState) {
    val condition = conditionOf(hour.symbolCode, hour.precipitationMm)
    val units = uiState.units
    val label = if (isNow) stringResource(R.string.weather_sheet_now) else timeFormat.format(hour.time)
    Column(
        modifier = Modifier.width(HOUR_COLUMN_WIDTH).semantics(mergeDescendants = true) { },
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Text(label, style = MaterialTheme.typography.labelSmall, color = Muted, maxLines = 1)
        WeatherIcon(weatherIconKind(condition, isNightSymbol(hour.symbolCode)), Ink, Modifier.size(HOUR_ICON_SIZE), uiState.iconStyle)
        Text(temperatureText(hour.temperatureC, units.temperature), style = MaterialTheme.typography.bodyMedium, color = Ink)
        val rain = hour.precipitationMm?.takeIf { it > 0.0 }
        Text(
            text = if (rain != null) rainWithUnit(rain, units.rain) else " ",
            style = MaterialTheme.typography.labelSmall,
            color = Accent,
            modifier = Modifier.height(16.dp),
        )
        val speed = hour.windSpeedMps
        val direction = hour.windFromDegrees
        if (speed != null) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                if (direction != null) {
                    // MET gives the direction the wind comes *from*, so the arrow points the other way
                    WindArrow((direction + 180.0).toFloat(), Muted, Modifier.size(12.dp))
                }
                Text(windWithUnit(speed, units.wind), style = MaterialTheme.typography.labelSmall, color = Muted, maxLines = 1)
            }
        }
    }
}

@Composable
internal fun DetailsGrid(weather: WeatherState, units: ResolvedWeatherUnits) {
    val now = weather.now
    val cells = listOfNotNull(
        now.humidityPercent?.let { stringResource(R.string.weather_detail_humidity) to stringResource(R.string.percent_format, it.roundToInt()) },
        now.pressureHpa?.let { stringResource(R.string.weather_detail_pressure) to pressureWithUnit(it, units.pressure) },
        now.cloudPercent?.let { stringResource(R.string.weather_detail_cloud_cover) to stringResource(R.string.percent_format, it.roundToInt()) },
        now.windSpeedMps?.let { stringResource(R.string.weather_detail_wind) to windWithUnit(it, units.wind, now.windFromDegrees) },
    )
    cells.chunked(2).forEach { pair ->
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
            pair.forEach { (label, value) -> DetailCell(label, value, Modifier.weight(1f)) }
            if (pair.size == 1) Spacer(Modifier.weight(1f))
        }
    }
}

@Composable
internal fun DetailCell(label: String, value: String, modifier: Modifier = Modifier) {
    Column(modifier = modifier.background(SurfaceContainer, MaterialTheme.shapes.small).padding(horizontal = 12.dp, vertical = 10.dp)) {
        Text(label, style = MaterialTheme.typography.labelMedium, color = Muted)
        Text(value, style = MaterialTheme.typography.bodyLarge, color = Ink)
    }
}

@Composable
internal fun Outlook(weather: WeatherState, unit: com.facetlauncher.app.data.model.TemperatureUnit, iconStyle: WeatherIconStyle) {
    val today = LocalDate.now(weather.zone)
    Column(modifier = Modifier.fillMaxWidth().background(SurfaceContainer, MaterialTheme.shapes.medium).padding(horizontal = 12.dp, vertical = 4.dp)) {
        weather.days.take(OUTLOOK_DAYS).forEachIndexed { index, day ->
            if (index > 0) HorizontalDivider(color = Hairline)
            OutlookRow(day, isToday = day.date == today, unit, iconStyle)
        }
    }
}

@Composable
internal fun OutlookRow(day: WeatherDay, isToday: Boolean, unit: com.facetlauncher.app.data.model.TemperatureUnit, iconStyle: WeatherIconStyle) {
    val condition = conditionOf(day.symbolCode)
    val label = if (isToday) {
        stringResource(R.string.weather_sheet_today)
    } else {
        day.date.dayOfWeek.getDisplayName(DateTextStyle.SHORT, LocalConfiguration.current.locales[0]) + " " + day.date.dayOfMonth
    }
    Row(modifier = Modifier.fillMaxWidth().padding(vertical = 10.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        Text(label, style = MaterialTheme.typography.bodyMedium, color = Ink, modifier = Modifier.width(72.dp))
        WeatherIcon(weatherIconKind(condition, night = false), Ink, Modifier.size(HOUR_ICON_SIZE), iconStyle)
        Spacer(Modifier.weight(1f))
        Text(temperatureText(day.lowC, unit), style = MaterialTheme.typography.bodyMedium, color = Muted)
        Text(temperatureText(day.highC, unit), style = MaterialTheme.typography.bodyMedium, color = Ink, modifier = Modifier.width(44.dp))
    }
}

private const val OUTLOOK_DAYS = 9
