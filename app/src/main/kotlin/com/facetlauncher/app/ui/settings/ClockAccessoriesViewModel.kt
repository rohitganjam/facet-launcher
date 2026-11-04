package com.facetlauncher.app.ui.settings

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.facetlauncher.app.data.FacetRepository
import com.facetlauncher.app.data.LocationRepository
import com.facetlauncher.app.data.SettingsRepository
import com.facetlauncher.app.data.model.ClockWidgetConfig
import com.facetlauncher.app.data.model.Facet
import com.facetlauncher.app.data.model.LauncherSettings
import com.facetlauncher.app.data.model.NO_ACTIVE_FACET_ID
import com.facetlauncher.app.data.model.WeatherCity
import com.facetlauncher.app.data.model.WeatherIconStyle
import com.facetlauncher.app.data.model.WeatherLocationMode
import com.facetlauncher.app.data.model.WidgetWeatherLocation
import com.facetlauncher.app.domain.UpdateClockConfigUseCase
import com.facetlauncher.app.domain.effectiveClockConfig
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/** The Clock accessories screen: alarm, battery and weather switches, the weather icon style and the clock's own weather place, for one facet's clock. */
data class ClockAccessoriesUiState(
    val facet: Facet? = null,
    val settings: LauncherSettings = LauncherSettings(),
    val locationPermissionGranted: Boolean = false,
) {
    val clockStyle: ClockWidgetConfig get() = effectiveClockConfig(facet, settings)
    val showAlarm: Boolean get() = clockStyle.showAlarm
    val showBattery: Boolean get() = clockStyle.showBattery
    val showWeather: Boolean get() = clockStyle.showWeather
    val weatherIconStyle: WeatherIconStyle get() = clockStyle.weatherIconStyle

    /** This clock's own weather place, `null` (same as Weather settings) while it follows them. */
    val weatherLocation: WidgetWeatherLocation? get() = clockStyle.weatherLocation
    val savedCities: ImmutableList<WeatherCity> get() = settings.savedWeatherCities.toImmutableList()

    /** Whether the weather this clock shows is for the phone's position, which needs the location permission. */
    val weatherNeedsPosition: Boolean
        get() = when (val own = weatherLocation) {
            WidgetWeatherLocation.Current -> true
            is WidgetWeatherLocation.City -> false
            null -> settings.weatherLocationMode == WeatherLocationMode.CURRENT
        }

    /** The Location and Icons cards exist while the clock shows weather. */
    val showLocationCard: Boolean get() = showWeather
    val showIconStyleCard: Boolean get() = showWeather
}

@HiltViewModel
class ClockAccessoriesViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val settingsRepository: SettingsRepository,
    private val facetRepository: FacetRepository,
    private val locationRepository: LocationRepository,
    private val updateClockConfig: UpdateClockConfigUseCase,
) : ViewModel() {

    private val facetId: Long = savedStateHandle.get<Long>("facetId") ?: NO_ACTIVE_FACET_ID
    private val permissionGranted = MutableStateFlow(locationRepository.isPermissionGranted())

    val uiState: StateFlow<ClockAccessoriesUiState> = combine(
        settingsRepository.settings,
        facetRepository.observeFacets(),
        permissionGranted,
    ) { settings, facets, granted ->
        ClockAccessoriesUiState(facet = facets.find { it.id == facetId }, settings = settings, locationPermissionGranted = granted)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(SUBSCRIPTION_TIMEOUT_MS), ClockAccessoriesUiState())

    /** Called when the screen resumes and after the permission dialog: the grant can change outside the app. */
    fun refreshPermission() {
        permissionGranted.value = locationRepository.isPermissionGranted()
    }

    fun setShowAlarm(enabled: Boolean) = change { it.copy(showAlarm = enabled) }

    fun setShowBattery(enabled: Boolean) = change { it.copy(showBattery = enabled) }

    fun setShowWeather(enabled: Boolean) = change { it.copy(showWeather = enabled) }

    fun setWeatherIconStyle(style: WeatherIconStyle) = change { it.copy(weatherIconStyle = style) }

    /** This clock's own place for its weather; `null` follows Weather settings. */
    fun setWeatherLocation(location: WidgetWeatherLocation?) = change { it.copy(weatherLocation = location) }

    private fun change(edit: (ClockWidgetConfig) -> ClockWidgetConfig) {
        if (facetId == NO_ACTIVE_FACET_ID) return
        viewModelScope.launch { updateClockConfig(facetId, edit) }
    }

    private companion object {
        const val SUBSCRIPTION_TIMEOUT_MS = 5_000L
    }
}
