package com.facetlauncher.app.ui.nativewidget.config

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.facetlauncher.app.data.CalendarPermissionRepository
import com.facetlauncher.app.data.CalendarRepository
import com.facetlauncher.app.data.SettingsRepository
import com.facetlauncher.app.data.model.CalendarInfo
import com.facetlauncher.app.data.model.NativeWidgetConfig
import com.facetlauncher.app.data.model.NativeWidgetRef
import com.facetlauncher.app.data.model.WeatherCity
import com.facetlauncher.app.domain.NativeWidgetSettings
import com.facetlauncher.app.domain.ObserveNativeWidgetSettingsUseCase
import com.facetlauncher.app.domain.SetNativeWidgetConfigUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.ImmutableSet
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList
import kotlinx.collections.immutable.toImmutableSet
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class NativeWidgetConfigUiState(
    val calendars: ImmutableList<CalendarInfo> = persistentListOf(),
    /** The cities saved in the launcher-wide weather settings. */
    val savedCities: ImmutableList<WeatherCity> = persistentListOf(),
    /** What the launcher's Calendar settings are now: an Agenda's choices start from these. `null` ids means every calendar. */
    val launcherCalendarIds: ImmutableSet<String>? = null,
    val launcherShowAllDay: Boolean = true,
)

/** What a widget's settings sheet lists (calendars, saved cities) and writes back, for a widget on the Hub or on Home alike. */
@HiltViewModel
class NativeWidgetConfigViewModel @Inject constructor(
    settingsRepository: SettingsRepository,
    private val calendarRepository: CalendarRepository,
    private val calendarPermissionRepository: CalendarPermissionRepository,
    private val observeSettings: ObserveNativeWidgetSettingsUseCase,
    private val setConfig: SetNativeWidgetConfigUseCase,
) : ViewModel() {

    private val calendars = MutableStateFlow<List<CalendarInfo>>(emptyList())

    val uiState: StateFlow<NativeWidgetConfigUiState> = combine(calendars, settingsRepository.settings) { calendarList, settings ->
        NativeWidgetConfigUiState(
            calendars = calendarList.toImmutableList(),
            savedCities = settings.savedWeatherCities.toImmutableList(),
            launcherCalendarIds = settings.selectedCalendarIds?.toImmutableSet(),
            launcherShowAllDay = settings.showAllDayEvents,
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), NativeWidgetConfigUiState())

    init {
        viewModelScope.launch {
            calendars.value = if (calendarPermissionRepository.isGranted()) calendarRepository.getCalendars() else emptyList()
        }
    }

    /** What the widget is, and what it is set to now; `null` once it is gone. */
    fun settings(ref: NativeWidgetRef): Flow<NativeWidgetSettings?> = observeSettings(ref)

    fun save(ref: NativeWidgetRef, config: NativeWidgetConfig) {
        viewModelScope.launch { setConfig(ref, config) }
    }
}
