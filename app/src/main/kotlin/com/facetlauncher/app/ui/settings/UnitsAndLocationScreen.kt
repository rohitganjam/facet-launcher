package com.facetlauncher.app.ui.settings

import android.content.res.Configuration
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
import com.facetlauncher.app.ui.components.StickyHeaderScrollColumn
import com.facetlauncher.app.ui.components.CardDivider
import com.facetlauncher.app.ui.components.LabeledDropdownRow
import com.facetlauncher.app.ui.components.SettingsCard
import com.facetlauncher.app.ui.components.SettingsSectionHeader
import com.facetlauncher.app.ui.theme.FacetLauncherTheme
import com.facetlauncher.app.ui.theme.Ink
import com.facetlauncher.app.ui.theme.Muted
import com.facetlauncher.app.ui.theme.SurfaceContainer

/** What this screen does beyond editing: open the location screen and the permission it needs. */
data class UnitsAndLocationNavigation(
    val onBack: () -> Unit,
    val onOpenLocation: () -> Unit,
    val onOpenPermissions: () -> Unit,
)

data class UnitsAndLocationActions(
    val onTemperatureUnit: (TemperatureUnit) -> Unit = {},
    val onWindSpeedUnit: (WindSpeedUnit) -> Unit = {},
    val onRainUnit: (RainUnit) -> Unit = {},
    val onPressureUnit: (PressureUnit) -> Unit = {},
)

/**
 * "Units and location": how weather numbers are written and where the weather is for. Launcher-wide, so one screen for every
 * facet, opened from the Clock accessories screen of the launcher defaults and of each facet.
 */
@Composable
fun UnitsAndLocationScreen(
    navigation: UnitsAndLocationNavigation,
    modifier: Modifier = Modifier,
    viewModel: UnitsAndLocationViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event -> if (event == Lifecycle.Event.ON_RESUME) viewModel.refreshPermission() }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }
    UnitsAndLocationContent(
        uiState = uiState,
        navigation = navigation,
        actions = UnitsAndLocationActions(
            onTemperatureUnit = viewModel::setTemperatureUnit,
            onWindSpeedUnit = viewModel::setWindSpeedUnit,
            onRainUnit = viewModel::setRainUnit,
            onPressureUnit = viewModel::setPressureUnit,
        ),
        modifier = modifier,
    )
}

@Composable
internal fun UnitsAndLocationContent(
    uiState: UnitsAndLocationUiState,
    navigation: UnitsAndLocationNavigation,
    actions: UnitsAndLocationActions,
    modifier: Modifier = Modifier,
) {
    StickyHeaderScrollColumn(
        title = stringResource(R.string.units_and_location_title),
        onBack = navigation.onBack,
        testTag = "units_and_location_screen",
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Text(
            stringResource(R.string.units_and_location_note),
            style = MaterialTheme.typography.bodyMedium,
            color = Muted,
            modifier = Modifier.padding(horizontal = 4.dp).testTag("units_and_location_note"),
        )
        UnitsCard(uiState.settings, actions)
        LocationCard(uiState, navigation.onOpenLocation, navigation.onOpenPermissions)
    }
}

/** How weather numbers are written. */
@Composable
private fun UnitsCard(settings: LauncherSettings, actions: UnitsAndLocationActions) {
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

/** A link to where the weather is for, and the permission it needs while it follows the phone. */
@Composable
private fun LocationCard(uiState: UnitsAndLocationUiState, onOpenLocation: () -> Unit, onOpenPermissions: () -> Unit) {
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
            WeatherLocationPermissionRow(uiState.locationPermissionGranted, onOpenPermissions)
        }
    }
}

@Preview(showBackground = true, widthDp = 390, heightDp = 900)
@Preview(name = "Dark", showBackground = true, widthDp = 390, heightDp = 900, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun UnitsAndLocationScreenPreview() {
    FacetLauncherTheme {
        UnitsAndLocationContent(
            uiState = UnitsAndLocationUiState(settings = LauncherSettings(showWeather = true), locationPermissionGranted = true),
            navigation = UnitsAndLocationNavigation(onBack = {}, onOpenLocation = {}, onOpenPermissions = {}),
            actions = UnitsAndLocationActions(),
        )
    }
}
