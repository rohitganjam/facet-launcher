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
import java.util.Locale
import kotlin.math.roundToInt

private val SHEET_SIDE_PADDING = 20.dp

/**
 * The weather after a tap on the accessory: a one-line rain summary, the place and the temperature, the next 24 hours, the
 * current details and the days ahead. [onRefresh] goes through the repository's gate, so the button cannot reach MET more
 * than it allows. Closes on Home press like every sheet over Home ([ThemedModalBottomSheet]).
 */
@Composable
fun WeatherSheet(
    uiState: WeatherUiState,
    onRefresh: () -> Unit,
    onDismiss: () -> Unit,
    onChooseCity: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val requestLocation = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { onRefresh() }
    ThemedModalBottomSheet(onDismissRequest = onDismiss, modifier = modifier) {
        Column(
            modifier = Modifier.fillMaxWidth().verticalScroll(rememberScrollState()).padding(horizontal = SHEET_SIDE_PADDING).padding(bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            val weather = uiState.weather
            if (weather == null) EmptyWeather() else WeatherContent(weather, uiState)
            NoticeLine(uiState.notice, onAllowLocation = { requestLocation.launch(Manifest.permission.ACCESS_COARSE_LOCATION) }, onChooseCity = onChooseCity)
            FooterRow(uiState, onRefresh)
        }
    }
}

@Composable
private fun EmptyWeather() {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp), modifier = Modifier.padding(top = 8.dp)) {
        Text(stringResource(R.string.weather_empty), style = MaterialTheme.typography.titleLarge, color = Ink)
        Text(stringResource(R.string.weather_empty_hint), style = MaterialTheme.typography.bodyMedium, color = Muted)
    }
}

@Composable
private fun WeatherContent(weather: WeatherState, uiState: WeatherUiState) {
    val units = uiState.units
    val pattern = if (uiState.use24HourTime) "HH:mm" else "h:mm a"
    val timeFormat = remember(pattern, weather.zone) { DateTimeFormatter.ofPattern(pattern, Locale.getDefault()).withZone(weather.zone) }
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        rainSummaryText(weather.summary, timeFormat, units.rain)?.let { SummaryBar(it) }
        Header(weather, units.temperature)
        HourlyStrip(weather, uiState, timeFormat)
        DetailsGrid(weather, units)
        Outlook(weather, units.temperature)
    }
}

@Composable
private fun SummaryBar(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.bodyMedium,
        color = Ink,
        modifier = Modifier.fillMaxWidth().background(SurfaceContainer, MaterialTheme.shapes.small).padding(horizontal = 12.dp, vertical = 10.dp),
    )
}

@Composable
private fun Header(weather: WeatherState, unit: com.facetlauncher.app.data.model.TemperatureUnit) {
    val condition = conditionOf(weather.now.symbolCode, weather.now.precipitationMm)
    val today = weather.days.firstOrNull()
    Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
            weather.placeName?.let { name ->
                Text(
                    text = if (weather.placeNameIsApproximate) stringResource(R.string.weather_place_near, name) else name,
                    style = MaterialTheme.typography.bodyMedium,
                    color = Muted,
                )
            }
            Text(temperatureText(weather.now.temperatureC, unit), style = MaterialTheme.typography.displaySmall, color = Ink)
            val conditionText = condition?.let { conditionName(it) }
            val range = today?.let { stringResource(R.string.weather_sheet_high_low, temperatureText(it.highC, unit), temperatureText(it.lowC, unit)) }
            Text(listOfNotNull(conditionText, range).joinToString(" · "), style = MaterialTheme.typography.bodyMedium, color = Muted)
        }
        WeatherIcon(weatherIconKind(condition, isNightSymbol(weather.now.symbolCode)), Ink, Modifier.size(56.dp))
    }
}

@Composable
private fun NoticeLine(notice: WeatherNotice?, onAllowLocation: () -> Unit, onChooseCity: () -> Unit) {
    if (notice == null) return
    val text = when (notice) {
        WeatherNotice.UPDATED -> R.string.weather_refresh_updated
        WeatherNotice.UP_TO_DATE -> R.string.weather_refresh_up_to_date
        WeatherNotice.TOO_SOON -> R.string.weather_refresh_too_soon
        WeatherNotice.BUSY -> R.string.weather_refresh_busy
        WeatherNotice.OFFLINE -> R.string.weather_refresh_offline
        WeatherNotice.FAILED -> R.string.weather_refresh_failed
        WeatherNotice.NEEDS_LOCATION_PERMISSION -> R.string.weather_needs_location
        WeatherNotice.NO_LOCATION -> R.string.weather_no_location
        WeatherNotice.NO_CITY -> R.string.weather_no_city
    }
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(stringResource(text), style = MaterialTheme.typography.bodyMedium, color = Ink)
        when (notice) {
            WeatherNotice.NEEDS_LOCATION_PERMISSION -> TextActionButton(stringResource(R.string.weather_allow_location), onAllowLocation)
            WeatherNotice.NO_CITY -> TextActionButton(stringResource(R.string.weather_choose_city), onChooseCity)
            else -> Unit
        }
    }
}

@Composable
private fun FooterRow(uiState: WeatherUiState, onRefresh: () -> Unit) {
    val weather = uiState.weather
    val updated = weather?.updatedAt?.let { fetched ->
        val time = DateTimeFormatter.ofPattern(if (uiState.use24HourTime) "HH:mm" else "h:mm a", Locale.getDefault()).withZone(weather.zone).format(fetched)
        stringResource(if (weather.isOutdated) R.string.weather_updated_outdated else R.string.weather_updated, time)
    }
    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
            Text(updated.orEmpty(), style = MaterialTheme.typography.bodySmall, color = Muted)
            if (uiState.isRefreshing) {
                CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp, color = Accent)
            } else {
                TextActionButton(stringResource(R.string.weather_refresh), onRefresh)
            }
        }
        Text(stringResource(R.string.weather_credit), style = MaterialTheme.typography.labelSmall, color = Muted)
    }
}
