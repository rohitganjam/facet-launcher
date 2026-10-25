package com.facetlauncher.app.ui.settings

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.facetlauncher.app.data.FacetRepository
import com.facetlauncher.app.data.LocationRepository
import com.facetlauncher.app.data.SettingsRepository
import com.facetlauncher.app.data.model.Facet
import com.facetlauncher.app.data.model.LauncherSettings
import com.facetlauncher.app.data.model.NO_ACTIVE_FACET_ID
import com.facetlauncher.app.data.model.WeatherIconStyle
import com.facetlauncher.app.domain.resolveFacetStyle
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/**
 * The Clock accessories screen: alarm, battery and weather switches and the weather icon style. [facet] is the facet this screen is scoped to, `null` for the launcher defaults.
 */
data class ClockAccessoriesUiState(
    val facet: Facet? = null,
    val settings: LauncherSettings = LauncherSettings(),
    /** Whether any facet (or the launcher default) shows weather: the Icons card and the Units and location link only exist then. */
    val weatherEnabledAnywhere: Boolean = false,
    val locationPermissionGranted: Boolean = false,
) {
    val isFacetScoped: Boolean get() = facet != null

    /** A facet's own switches count only while it overrides the clock; until then they show what it inherits and cannot be edited here. */
    val isInheriting: Boolean get() = facet != null && !facet.overrideClock
    val clockStyle get() = resolveFacetStyle(facet, settings).clock
    val showAlarm: Boolean get() = clockStyle.showAlarm
    val showBattery: Boolean get() = clockStyle.showBattery
    val showWeather: Boolean get() = clockStyle.showWeather
    val weatherIconStyle: WeatherIconStyle get() = clockStyle.weatherIconStyle


    /** The Icons card: on the default screen while weather is on somewhere, on a facet's while that facet shows weather. */
    val showIconStyleCard: Boolean get() = if (isFacetScoped) showWeather else weatherEnabledAnywhere

    /** Units and location are launcher-wide, so the default screen and every facet link to the same screen while weather is on somewhere. */
    val showUnitsAndLocationLink: Boolean get() = weatherEnabledAnywhere
}

@HiltViewModel
class ClockAccessoriesViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val settingsRepository: SettingsRepository,
    private val facetRepository: FacetRepository,
    private val locationRepository: LocationRepository,
) : ViewModel() {

    private val facetId: Long = savedStateHandle.get<Long>("facetId") ?: NO_ACTIVE_FACET_ID
    private val permissionGranted = MutableStateFlow(locationRepository.isPermissionGranted())

    val uiState: StateFlow<ClockAccessoriesUiState> = combine(
        settingsRepository.settings,
        facetRepository.observeFacets(),
        permissionGranted,
    ) { settings, facets, granted ->
        ClockAccessoriesUiState(
            facet = if (facetId == NO_ACTIVE_FACET_ID) null else facets.find { it.id == facetId },
            settings = settings,
            weatherEnabledAnywhere = settings.showWeather || facets.any { it.overrideClock && it.clockShowWeather },
            locationPermissionGranted = granted,
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(SUBSCRIPTION_TIMEOUT_MS), ClockAccessoriesUiState())

    /** Called when the screen resumes and after the permission dialog: the grant can change outside the app. */
    fun refreshPermission() {
        permissionGranted.value = locationRepository.isPermissionGranted()
    }

    fun setShowAlarm(enabled: Boolean) = write({ setShowNextAlarm(enabled) }, { setClockShowAlarm(it, enabled) })

    fun setShowBattery(enabled: Boolean) = write({ setShowBatteryPercentage(enabled) }, { setClockShowBattery(it, enabled) })

    fun setShowWeather(enabled: Boolean) = write({ setShowWeather(enabled) }, { setClockShowWeather(it, enabled) })

    fun setWeatherIconStyle(style: WeatherIconStyle) = write({ setWeatherIconStyle(style) }, { setClockWeatherIconStyle(it, style) })

    /** Writes to this facet when the screen is scoped to one, else to the launcher defaults. */
    private fun write(global: suspend SettingsRepository.() -> Unit, facet: suspend FacetRepository.(Long) -> Unit) = launch {
        if (facetId == NO_ACTIVE_FACET_ID) settingsRepository.global() else facetRepository.facet(facetId)
    }

    private fun launch(block: suspend () -> Unit) {
        viewModelScope.launch { block() }
    }

    private companion object {
        const val SUBSCRIPTION_TIMEOUT_MS = 5_000L
    }
}
