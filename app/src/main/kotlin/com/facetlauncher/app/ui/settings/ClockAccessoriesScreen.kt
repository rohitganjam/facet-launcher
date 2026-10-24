package com.facetlauncher.app.ui.settings

import android.Manifest
import android.content.res.Configuration
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.facetlauncher.app.R
import com.facetlauncher.app.data.model.LauncherSettings
import com.facetlauncher.app.data.model.PressureUnit
import com.facetlauncher.app.data.model.RainUnit
import com.facetlauncher.app.data.model.TemperatureUnit
import com.facetlauncher.app.data.model.WeatherLocationMode
import com.facetlauncher.app.data.model.WindSpeedUnit
import com.facetlauncher.app.ui.components.BackButton
import com.facetlauncher.app.ui.components.CardDivider
import com.facetlauncher.app.ui.components.LabeledDropdownRow
import com.facetlauncher.app.ui.components.SettingsCard
import com.facetlauncher.app.ui.components.SettingsSectionHeader
import com.facetlauncher.app.ui.components.SettingsToggleRow
import com.facetlauncher.app.ui.components.TextActionButton
import com.facetlauncher.app.ui.theme.FacetLauncherTheme
import com.facetlauncher.app.ui.theme.Ink
import com.facetlauncher.app.ui.theme.Muted
import com.facetlauncher.app.ui.theme.SurfaceContainer

/** What this screen does beyond editing: open the clock style (which owns a facet's Inherit/Override switch) and the location screen. */
data class ClockAccessoriesNavigation(
    val onBack: () -> Unit,
    val onOpenClockStyle: () -> Unit,
    val onOpenLocation: () -> Unit,
)

/**
 * "Clock accessories": the next alarm, the battery percentage and the weather next to the clock. Four cards: the alarm and
 * battery switches, the weather switch, then Units and Location (launcher-wide, so only on the default screen and only while
 * weather is on somewhere; each is hidden as a whole card). Scoped to a facet it shows just the switches, and they apply while
 * that facet overrides the clock.
 */
@Composable
fun ClockAccessoriesScreen(
    navigation: ClockAccessoriesNavigation,
    modifier: Modifier = Modifier,
    viewModel: ClockAccessoriesViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event -> if (event == Lifecycle.Event.ON_RESUME) viewModel.refreshPermission() }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }
    val requestLocation = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { viewModel.refreshPermission() }
    ClockAccessoriesContent(
        uiState = uiState,
        navigation = navigation,
        actions = ClockAccessoriesActions(
            onShowAlarm = viewModel::setShowAlarm,
            onShowBattery = viewModel::setShowBattery,
            onShowWeather = { enabled ->
                viewModel.setShowWeather(enabled)
                // "Use current location" needs the permission; ask as the switch goes on, so the weather can appear straight away.
                val needsPermission = enabled && uiState.settings.weatherLocationMode == WeatherLocationMode.CURRENT && !uiState.locationPermissionGranted
                if (needsPermission) requestLocation.launch(Manifest.permission.ACCESS_COARSE_LOCATION)
            },
            onTemperatureUnit = viewModel::setTemperatureUnit,
            onWindSpeedUnit = viewModel::setWindSpeedUnit,
            onRainUnit = viewModel::setRainUnit,
            onPressureUnit = viewModel::setPressureUnit,
            onAllowLocation = { requestLocation.launch(Manifest.permission.ACCESS_COARSE_LOCATION) },
        ),
        modifier = modifier,
    )
}

data class ClockAccessoriesActions(
    val onShowAlarm: (Boolean) -> Unit = {},
    val onShowBattery: (Boolean) -> Unit = {},
    val onShowWeather: (Boolean) -> Unit = {},
    val onTemperatureUnit: (TemperatureUnit) -> Unit = {},
    val onWindSpeedUnit: (WindSpeedUnit) -> Unit = {},
    val onRainUnit: (RainUnit) -> Unit = {},
    val onPressureUnit: (PressureUnit) -> Unit = {},
    val onAllowLocation: () -> Unit = {},
)

@Composable
internal fun ClockAccessoriesContent(
    uiState: ClockAccessoriesUiState,
    navigation: ClockAccessoriesNavigation,
    actions: ClockAccessoriesActions,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(SurfaceContainer)
            .testTag("clock_accessories_screen")
            .windowInsetsPadding(WindowInsets.systemBars)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(top = 24.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            BackButton(onClick = navigation.onBack)
            Text(text = stringResource(R.string.clock_accessories_title), style = MaterialTheme.typography.headlineSmall, color = Ink)
        }
        if (uiState.isInheriting) InheritingNote(navigation.onOpenClockStyle)
        SwitchesCards(uiState, actions)
        if (uiState.showGlobalCards) {
            UnitsCard(uiState.settings, actions)
            LocationCard(uiState, actions, navigation.onOpenLocation)
        }
        if (uiState.isFacetScoped && uiState.weatherEnabledAnywhere) {
            Text(stringResource(R.string.clock_accessories_facet_note), style = MaterialTheme.typography.bodyMedium, color = Muted, modifier = Modifier.padding(horizontal = 4.dp))
        } else if (uiState.showGlobalCards) {
            Text(stringResource(R.string.clock_accessories_global_note), style = MaterialTheme.typography.bodyMedium, color = Muted, modifier = Modifier.padding(horizontal = 4.dp))
        }
    }
}

@Composable
private fun InheritingNote(onOpenClockStyle: () -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(stringResource(R.string.clock_accessories_inheriting), style = MaterialTheme.typography.bodyMedium, color = Ink)
        TextActionButton(stringResource(R.string.clock_accessories_open_clock_style), onOpenClockStyle)
    }
}

/** Cards one and two: the alarm and battery switches, then the weather switch. A facet that inherits shows what it inherits, dimmed. */
@Composable
private fun SwitchesCards(uiState: ClockAccessoriesUiState, actions: ClockAccessoriesActions) {
    val editable = !uiState.isInheriting
    SettingsCard {
        SettingsToggleRow(stringResource(R.string.clock_accessories_show_alarm), uiState.showAlarm, actions.onShowAlarm, "accessory_alarm_toggle", enabled = editable)
        CardDivider()
        SettingsToggleRow(stringResource(R.string.clock_accessories_show_battery), uiState.showBattery, actions.onShowBattery, "accessory_battery_toggle", enabled = editable)
    }
    SettingsCard {
        SettingsToggleRow(stringResource(R.string.clock_accessories_show_weather), uiState.showWeather, actions.onShowWeather, "accessory_weather_toggle", enabled = editable)
    }
}

/** Card three: how weather numbers are written. Global, never per facet. */
@Composable
private fun UnitsCard(settings: LauncherSettings, actions: ClockAccessoriesActions) {
    SettingsSectionHeader(stringResource(R.string.clock_accessories_units))
    SettingsCard {
        LabeledDropdownRow(
            title = stringResource(R.string.clock_accessories_unit_temperature),
            options = TemperatureUnit.entries,
            selected = settings.temperatureUnit,
            label = { stringResource(temperatureUnitLabel(it)) },
            onSelect = actions.onTemperatureUnit,
            testTag = "unit_temperature_row",
        )
        CardDivider()
        LabeledDropdownRow(
            title = stringResource(R.string.clock_accessories_unit_wind),
            options = WindSpeedUnit.entries,
            selected = settings.windSpeedUnit,
            label = { stringResource(windSpeedUnitLabel(it)) },
            onSelect = actions.onWindSpeedUnit,
            testTag = "unit_wind_row",
        )
        CardDivider()
        LabeledDropdownRow(
            title = stringResource(R.string.clock_accessories_unit_rain),
            options = RainUnit.entries,
            selected = settings.rainUnit,
            label = { stringResource(rainUnitLabel(it)) },
            onSelect = actions.onRainUnit,
            testTag = "unit_rain_row",
        )
        CardDivider()
        LabeledDropdownRow(
            title = stringResource(R.string.clock_accessories_unit_pressure),
            options = PressureUnit.entries,
            selected = settings.pressureUnit,
            label = { stringResource(pressureUnitLabel(it)) },
            onSelect = actions.onPressureUnit,
            testTag = "unit_pressure_row",
        )
    }
}

/** Card four: a link to where the weather is for, and the permission it needs while it follows the phone. Global, never per facet. */
@Composable
private fun LocationCard(uiState: ClockAccessoriesUiState, actions: ClockAccessoriesActions, onOpenLocation: () -> Unit) {
    val settings = uiState.settings
    SettingsSectionHeader(stringResource(R.string.clock_accessories_location))
    SettingsCard(fullBleedRows = true) {
        ClickableRow(
            title = stringResource(R.string.weather_location_title),
            subtitle = if (settings.weatherLocationMode == WeatherLocationMode.CITY) {
                settings.weatherCity?.name ?: stringResource(R.string.weather_location_none)
            } else {
                stringResource(R.string.weather_location_current_short)
            },
            onClick = onOpenLocation,
            testTag = "accessory_location_row",
            trailing = { NavigationChevron() },
        )
        if (settings.weatherLocationMode == WeatherLocationMode.CURRENT) {
            CardDivider()
            PermissionRow(uiState.locationPermissionGranted, actions.onAllowLocation)
        }
    }
}

@Composable
internal fun PermissionRow(granted: Boolean, onAllow: () -> Unit, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier.fillMaxWidth().padding(horizontal = com.facetlauncher.app.ui.components.LocalSettingsRowInset.current, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(stringResource(R.string.weather_location_access), style = MaterialTheme.typography.bodyLarge, color = Ink)
        if (granted) {
            Text(stringResource(R.string.weather_location_access_allowed), style = MaterialTheme.typography.bodyMedium, color = Muted)
        } else {
            TextActionButton(stringResource(R.string.weather_location_allow), onAllow)
        }
    }
}

@Preview(showBackground = true, widthDp = 390, heightDp = 900)
@Preview(name = "Dark", showBackground = true, widthDp = 390, heightDp = 900, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun ClockAccessoriesScreenPreview() {
    FacetLauncherTheme {
        ClockAccessoriesContent(
            uiState = ClockAccessoriesUiState(settings = LauncherSettings(showWeather = true), weatherEnabledAnywhere = true, locationPermissionGranted = true),
            navigation = ClockAccessoriesNavigation(onBack = {}, onOpenClockStyle = {}, onOpenLocation = {}),
            actions = ClockAccessoriesActions(),
        )
    }
}
