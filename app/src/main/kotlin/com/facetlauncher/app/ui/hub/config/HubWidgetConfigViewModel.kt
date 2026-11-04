package com.facetlauncher.app.ui.hub.config

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.facetlauncher.app.data.CalendarPermissionRepository
import com.facetlauncher.app.data.CalendarRepository
import com.facetlauncher.app.data.SettingsRepository
import com.facetlauncher.app.data.WidgetPlacementRepository
import com.facetlauncher.app.data.model.CalendarInfo
import com.facetlauncher.app.data.model.NativeWidgetConfig
import com.facetlauncher.app.data.model.WeatherCity
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class HubWidgetConfigUiState(
    val calendars: ImmutableList<CalendarInfo> = persistentListOf(),
    /** The cities saved in the launcher-wide weather settings. */
    val savedCities: ImmutableList<WeatherCity> = persistentListOf(),
)

/** Reads what a Hub widget's settings sheet lists (calendars, saved cities) and writes what it is set to. */
@HiltViewModel
class HubWidgetConfigViewModel @Inject constructor(
    settingsRepository: SettingsRepository,
    private val calendarRepository: CalendarRepository,
    private val calendarPermissionRepository: CalendarPermissionRepository,
    private val widgetPlacementRepository: WidgetPlacementRepository,
) : ViewModel() {

    private val calendars = MutableStateFlow<List<CalendarInfo>>(emptyList())

    val uiState: StateFlow<HubWidgetConfigUiState> = combine(calendars, settingsRepository.settings.map { it.savedWeatherCities }) { calendarList, cities ->
        HubWidgetConfigUiState(calendars = calendarList.toImmutableList(), savedCities = cities.toImmutableList())
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), HubWidgetConfigUiState())

    init {
        viewModelScope.launch {
            calendars.value = if (calendarPermissionRepository.isGranted()) calendarRepository.getCalendars() else emptyList()
        }
    }

    fun save(appWidgetId: Int, config: NativeWidgetConfig) {
        viewModelScope.launch { widgetPlacementRepository.setConfig(appWidgetId, config) }
    }
}
