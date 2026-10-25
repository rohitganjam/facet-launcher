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
import com.facetlauncher.app.data.model.WeatherIconStyle
import com.facetlauncher.app.data.model.WeatherLocationMode
import com.facetlauncher.app.ui.components.StickyHeaderScrollColumn
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

/** What this screen does beyond editing: open the clock style (which owns a facet's Inherit/Override switch) and the units and location screen. */
data class ClockAccessoriesNavigation(
    val onBack: () -> Unit,
    val onOpenClockStyle: () -> Unit,
    val onOpenUnitsAndLocation: () -> Unit = {},
)

/**
 * "Clock accessories": the next alarm, the battery percentage and the weather next to the clock. Cards: the alarm and
 * battery switches, the weather switch, Icons (while weather is on), then a link to Units and location (launcher-wide, so the same
 * screen for the default and for every facet, shown while weather is on somewhere). Scoped to a facet the switches and Icons
 * apply while that facet overrides the clock.
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
            onIconStyle = viewModel::setWeatherIconStyle,
        ),
        modifier = modifier,
    )
}

data class ClockAccessoriesActions(
    val onShowAlarm: (Boolean) -> Unit = {},
    val onShowBattery: (Boolean) -> Unit = {},
    val onShowWeather: (Boolean) -> Unit = {},
    val onIconStyle: (WeatherIconStyle) -> Unit = {},
)

@Composable
internal fun ClockAccessoriesContent(
    uiState: ClockAccessoriesUiState,
    navigation: ClockAccessoriesNavigation,
    actions: ClockAccessoriesActions,
    modifier: Modifier = Modifier,
) {
    StickyHeaderScrollColumn(
        title = stringResource(R.string.clock_accessories_title),
        onBack = navigation.onBack,
        testTag = "clock_accessories_screen",
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        ClockAccessoriesPreview(uiState)
        if (uiState.isInheriting) InheritingNote(navigation.onOpenClockStyle)
        SwitchesCards(uiState, actions)
        if (uiState.showIconStyleCard) IconStyleCard(uiState, actions)
        if (uiState.showUnitsAndLocationLink) UnitsAndLocationCard(navigation.onOpenUnitsAndLocation)
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

/** Monochrome or coloured weather pictures, with every picture shown in the chosen style. A facet's own while it overrides the clock. */
@Composable
private fun IconStyleCard(uiState: ClockAccessoriesUiState, actions: ClockAccessoriesActions) {
    SettingsSectionHeader(stringResource(R.string.clock_accessories_icons))
    SettingsCard {
        LabeledDropdownRow(
            title = stringResource(R.string.weather_icon_style),
            options = WeatherIconStyle.entries,
            selected = uiState.weatherIconStyle,
            label = { stringResource(weatherIconStyleLabel(it)) },
            onSelect = actions.onIconStyle,
            enabled = !uiState.isInheriting,
            testTag = "icon_style_row",
        )
        CardDivider()
        WeatherIconsPreviewRow(style = uiState.weatherIconStyle)
    }
}

/** A link to the launcher-wide units and weather location, from the default screen and from every facet. */
@Composable
private fun UnitsAndLocationCard(onOpenUnitsAndLocation: () -> Unit) {
    SettingsCard(fullBleedRows = true) {
        ClickableRow(
            title = stringResource(R.string.units_and_location_title),
            subtitle = stringResource(R.string.units_and_location_subtitle),
            onClick = onOpenUnitsAndLocation,
            testTag = "accessory_units_and_location_row",
            trailing = { NavigationChevron() },
        )
    }
}

@Preview(showBackground = true, widthDp = 390, heightDp = 900)
@Preview(name = "Dark", showBackground = true, widthDp = 390, heightDp = 900, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun ClockAccessoriesScreenPreview() {
    FacetLauncherTheme {
        ClockAccessoriesContent(
            uiState = ClockAccessoriesUiState(settings = LauncherSettings(showWeather = true), weatherEnabledAnywhere = true, locationPermissionGranted = true),
            navigation = ClockAccessoriesNavigation(onBack = {}, onOpenClockStyle = {}),
            actions = ClockAccessoriesActions(),
        )
    }
}
