package com.facetlauncher.app.ui.nativewidget.config

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
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
import com.facetlauncher.app.data.model.selectsCalendar
import com.facetlauncher.app.ui.components.GlassBackgroundRow
import com.facetlauncher.app.ui.components.ThemedModalBottomSheet
import com.facetlauncher.app.ui.components.WeatherLocationPicker
import com.facetlauncher.app.ui.components.calendarSubtitle
import com.facetlauncher.app.ui.components.calendarTitle
import com.facetlauncher.app.ui.components.workCalendarPosition
import com.facetlauncher.app.ui.nativewidget.labelRes
import com.facetlauncher.app.ui.theme.Accent
import com.facetlauncher.app.ui.theme.AccentSwatch
import com.facetlauncher.app.ui.theme.Ink
import com.facetlauncher.app.ui.theme.Muted
import com.facetlauncher.app.ui.theme.Surface
import com.facetlauncher.app.ui.theme.SurfaceContainer
import com.facetlauncher.app.ui.theme.resolvedColor
import kotlinx.collections.immutable.ImmutableList

/** The settings of one native widget, on the Hub or on Home: its background, and for Weather and Agenda its own location or calendars, or the launcher's. */
@Composable
fun NativeWidgetConfigSheet(
    ref: NativeWidgetRef,
    onDismiss: () -> Unit,
    viewModel: NativeWidgetConfigViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    LaunchedEffect(ref) { viewModel.reloadCalendars() }
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
            GlassBackgroundRow(
                title = stringResource(R.string.native_widget_background),
                selected = config.glass ?: NativeWidgetConfig.DEFAULT_GLASS,
                onSelect = { onConfigChange(config.copy(glass = it)) },
                testTag = "hub_widget_config_glass",
            )
            when (type) {
                NativeWidgetType.AGENDA -> AgendaConfig(config, state, onConfigChange)
                NativeWidgetType.WEATHER -> WeatherConfig(config, state.savedCities, onConfigChange)
                NativeWidgetType.NOW_PLAYING -> Unit
            }
        }
    }
}

/** An Agenda's own calendar settings: all-day events on or off, and which calendars. A new Agenda shows every calendar, calendars added later included. */
@Composable
private fun AgendaConfig(config: NativeWidgetConfig, state: NativeWidgetConfigUiState, onConfigChange: (NativeWidgetConfig) -> Unit) {
    val showAllDay = config.showAllDayEvents ?: true
    // With nothing chosen an Agenda shows every calendar (the same rule the query uses).
    val chosen = state.calendars.filter { config.calendarIds.selectsCalendar(it.id) }.map { it.id }.toSet()
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
                val swatch = state.calendarColors[calendar.id]?.let { runCatching { AccentSwatch.valueOf(it) }.getOrNull() }
                Box(
                    modifier = Modifier
                        .padding(start = 12.dp)
                        .size(12.dp)
                        .clip(CircleShape)
                        .background(swatch?.resolvedColor() ?: Accent)
                        .testTag("hub_widget_config_calendar_color_${calendar.id}"),
                )
                Column(modifier = Modifier.padding(start = 12.dp)) {
                    Text(text = workCalendarPosition(calendar, state.calendars).let { (number, count) -> calendarTitle(calendar, number, count) }, style = MaterialTheme.typography.bodyLarge, color = Ink)
                    Text(text = calendarSubtitle(calendar), style = MaterialTheme.typography.bodySmall, color = Muted)
                }
            }
        }
    }
}

@Composable
private fun WeatherConfig(config: NativeWidgetConfig, savedCities: ImmutableList<WeatherCity>, onConfigChange: (NativeWidgetConfig) -> Unit) {
    WeatherLocationPicker(
        location = config.weatherLocation,
        savedCities = savedCities,
        onChange = { onConfigChange(config.copy(weatherLocation = it)) },
        tagPrefix = "hub_widget_config",
    )
}

@Composable
private fun SectionLabel(text: String) {
    Text(text = text, style = MaterialTheme.typography.labelLarge, color = Muted, modifier = Modifier.padding(top = 12.dp))
}
