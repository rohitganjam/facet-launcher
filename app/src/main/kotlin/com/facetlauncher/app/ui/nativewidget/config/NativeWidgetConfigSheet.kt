package com.facetlauncher.app.ui.nativewidget.config

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.hilt.navigation.compose.hiltViewModel
import com.facetlauncher.app.R
import com.facetlauncher.app.data.model.NativeWidgetConfig
import com.facetlauncher.app.data.model.NativeWidgetRef
import com.facetlauncher.app.data.model.NativeWidgetType
import com.facetlauncher.app.data.model.WeatherCity
import com.facetlauncher.app.data.model.WidgetWeatherLocation
import com.facetlauncher.app.data.model.isSamePlaceAs
import com.facetlauncher.app.ui.components.ThemedModalBottomSheet
import com.facetlauncher.app.ui.nativewidget.labelRes
import com.facetlauncher.app.ui.theme.Accent
import com.facetlauncher.app.ui.theme.Ink
import com.facetlauncher.app.ui.theme.Muted
import com.facetlauncher.app.ui.theme.Surface
import com.facetlauncher.app.ui.theme.SurfaceContainer
import kotlinx.collections.immutable.ImmutableList

/** The settings of one Weather or Agenda widget, on the Hub or on Home: its own calendars or location, or the launcher's. */
@Composable
fun NativeWidgetConfigSheet(
    ref: NativeWidgetRef,
    onDismiss: () -> Unit,
    viewModel: NativeWidgetConfigViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val settings by remember(ref) { viewModel.settings(ref) }.collectAsStateWithLifecycle(initialValue = null)
    settings?.let {
        NativeWidgetConfigContent(
            type = it.type,
            config = it.config,
            state = state,
            onDismiss = onDismiss,
            onConfigChange = { config -> viewModel.save(ref, config) },
        )
    }
}

@Composable
internal fun NativeWidgetConfigContent(
    type: NativeWidgetType,
    config: NativeWidgetConfig,
    state: NativeWidgetConfigUiState,
    onDismiss: () -> Unit,
    onConfigChange: (NativeWidgetConfig) -> Unit,
) {
    ThemedModalBottomSheet(onDismissRequest = onDismiss, skipPartiallyExpanded = true, containerColor = SurfaceContainer) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(start = 24.dp, end = 24.dp, top = 4.dp, bottom = 24.dp)
                .testTag("hub_widget_config_sheet"),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Text(text = stringResource(type.labelRes()), style = MaterialTheme.typography.titleMedium, color = Ink)
            when (type) {
                NativeWidgetType.AGENDA -> AgendaConfig(config, state, onConfigChange)
                NativeWidgetType.WEATHER -> WeatherConfig(config, state.savedCities, onConfigChange)
                NativeWidgetType.NOW_PLAYING -> Unit
            }
        }
    }
}

/** The launcher's Calendar settings, per widget: all-day events on or off, and which calendars. Each starts from the launcher's current value. */
@Composable
private fun AgendaConfig(config: NativeWidgetConfig, state: NativeWidgetConfigUiState, onConfigChange: (NativeWidgetConfig) -> Unit) {
    val showAllDay = config.showAllDayEvents ?: state.launcherShowAllDay
    val chosen = config.calendarIds ?: state.launcherCalendarIds ?: state.calendars.map { it.id }.toSet()
    fun change(allDay: Boolean = showAllDay, ids: Set<String> = chosen) = onConfigChange(config.copy(showAllDayEvents = allDay, calendarIds = ids))
    Column {
        Row(
            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Text(text = stringResource(R.string.calendar_settings_show_all_day_events), style = MaterialTheme.typography.bodyLarge, color = Ink, modifier = Modifier.weight(1f))
            Switch(
                checked = showAllDay,
                onCheckedChange = { change(allDay = it) },
                colors = SwitchDefaults.colors(checkedTrackColor = Accent, checkedThumbColor = Surface),
                modifier = Modifier.testTag("hub_widget_config_all_day"),
            )
        }
        SectionLabel(stringResource(R.string.hub_widget_config_calendars))
        if (state.calendars.isEmpty()) Text(text = stringResource(R.string.hub_widget_config_no_calendars), style = MaterialTheme.typography.bodyMedium, color = Muted)
        state.calendars.forEach { calendar ->
            val selected = calendar.id in chosen
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { change(ids = if (selected) chosen - calendar.id else chosen + calendar.id) }
                    .padding(vertical = 4.dp)
                    .testTag("hub_widget_config_calendar_${calendar.id}"),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Checkbox(checked = selected, onCheckedChange = null, colors = CheckboxDefaults.colors(checkedColor = Accent))
                Column(modifier = Modifier.padding(start = 12.dp)) {
                    Text(text = calendar.displayName, style = MaterialTheme.typography.bodyLarge, color = Ink)
                    Text(text = calendar.accountName, style = MaterialTheme.typography.bodySmall, color = Muted)
                }
            }
        }
    }
}

@Composable
private fun WeatherConfig(config: NativeWidgetConfig, savedCities: ImmutableList<WeatherCity>, onConfigChange: (NativeWidgetConfig) -> Unit) {
    val location = config.weatherLocation
    Column {
        LocationRow(stringResource(R.string.hub_widget_config_use_launcher), location == null, "hub_widget_config_follow") { onConfigChange(config.copy(weatherLocation = null)) }
        LocationRow(stringResource(R.string.hub_widget_config_current_location), location == WidgetWeatherLocation.Current, "hub_widget_config_current") {
            onConfigChange(config.copy(weatherLocation = WidgetWeatherLocation.Current))
        }
        SectionLabel(stringResource(R.string.hub_widget_config_location))
        if (savedCities.isEmpty()) Text(text = stringResource(R.string.hub_widget_config_no_cities), style = MaterialTheme.typography.bodyMedium, color = Muted)
        savedCities.forEach { city ->
            val selected = (location as? WidgetWeatherLocation.City)?.city?.isSamePlaceAs(city) == true
            LocationRow(city.name, selected, "hub_widget_config_city_${city.name}") { onConfigChange(config.copy(weatherLocation = WidgetWeatherLocation.City(city))) }
        }
    }
}

@Composable
private fun LocationRow(label: String, selected: Boolean, tag: String, onClick: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick).padding(vertical = 4.dp).testTag(tag),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        RadioButton(selected = selected, onClick = null, colors = RadioButtonDefaults.colors(selectedColor = Accent))
        Text(text = label, style = MaterialTheme.typography.bodyLarge, color = Ink, modifier = Modifier.padding(start = 12.dp))
    }
}

@Composable
private fun SectionLabel(text: String) {
    Text(text = text, style = MaterialTheme.typography.labelLarge, color = Muted, modifier = Modifier.padding(top = 12.dp))
}
