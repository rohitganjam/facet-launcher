package com.facetlauncher.app.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.facetlauncher.app.data.LocationRepository
import com.facetlauncher.app.data.SettingsRepository
import com.facetlauncher.app.data.model.WeatherCity
import com.facetlauncher.app.data.model.WeatherLocationMode
import com.facetlauncher.app.data.weather.WeatherRepository
import com.facetlauncher.app.domain.PlaceCandidate
import com.facetlauncher.app.domain.SearchPlacesUseCase
import com.facetlauncher.app.domain.TurnOffWeatherUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class WeatherLocationUiState(
    val mode: WeatherLocationMode = WeatherLocationMode.CURRENT,
    /** The city weather is shown for under "Choose a city", if one is picked. */
    val city: WeatherCity? = null,
    val savedCities: List<WeatherCity> = emptyList(),
    /** Picking a new city was refused because the saved list is full. */
    val cityLimitHit: Boolean = false,
    val permissionGranted: Boolean = false,
    /** The name of the place the weather is for now: the chosen city, or the resolved name of the position. */
    val showingFor: String? = null,
    val query: String = "",
    val results: List<PlaceCandidate> = emptyList(),
    val isSearching: Boolean = false,
)

/**
 * Where the weather is for: the current position or one chosen city. Launcher-wide, so there is no facet scope. Choosing a city
 * saves it and switches to "Choose a city"; the search runs 400 ms after the last keystroke and drops answers that a newer
 * query has replaced.
 */
@OptIn(ExperimentalCoroutinesApi::class, FlowPreview::class)
@HiltViewModel
class WeatherLocationViewModel @Inject constructor(
    private val settingsRepository: SettingsRepository,
    private val weatherRepository: WeatherRepository,
    private val locationRepository: LocationRepository,
    private val searchPlaces: SearchPlacesUseCase,
    private val turnOffWeather: TurnOffWeatherUseCase,
) : ViewModel() {

    private val query = MutableStateFlow("")
    private val permissionGranted = MutableStateFlow(locationRepository.isPermissionGranted())
    private val cityLimitHit = MutableStateFlow(false)

    private val search = query.debounce { if (it.isBlank()) 0L else SEARCH_DEBOUNCE_MS }.distinctUntilChanged().flatMapLatest { text ->
        flow {
            if (text.isBlank()) {
                emit(emptyList<PlaceCandidate>() to false)
            } else {
                emit(emptyList<PlaceCandidate>() to true)
                emit(searchPlaces(text) to false)
            }
        }
    }

    val uiState: StateFlow<WeatherLocationUiState> = combine(
        settingsRepository.settings,
        weatherRepository.observeState(),
        permissionGranted,
        query,
        search,
    ) { settings, state, granted, text, (results, searching) ->
        WeatherLocationUiState(
            mode = settings.weatherLocationMode,
            city = settings.weatherCity,
            savedCities = settings.savedWeatherCities,
            permissionGranted = granted,
            showingFor = if (settings.weatherLocationMode == WeatherLocationMode.CITY) settings.weatherCity?.name else state.placeName,
            query = text,
            results = results,
            isSearching = searching,
        )
    }.combine(cityLimitHit) { state, limitHit -> state.copy(cityLimitHit = limitHit) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(SUBSCRIPTION_TIMEOUT_MS), WeatherLocationUiState())

    fun onQueryChange(text: String) {
        query.value = text
        cityLimitHit.value = false
    }

    fun refreshPermission() {
        permissionGranted.value = locationRepository.isPermissionGranted()
    }

    fun useCurrentLocation() {
        viewModelScope.launch { settingsRepository.setWeatherLocationMode(WeatherLocationMode.CURRENT) }
    }

    /** Switches to "Choose a city", with or without a city picked yet. */
    fun useCityMode() {
        viewModelScope.launch { settingsRepository.setWeatherLocationMode(WeatherLocationMode.CITY) }
    }

    /** Saves [place] with the other cities and makes it the one weather is for; refused when the list is full. */
    fun chooseCity(place: PlaceCandidate) = choose(place.toWeatherCity(), clearQuery = true)

    /** Makes a saved city the one weather is for. */
    fun selectSavedCity(city: WeatherCity) = choose(city, clearQuery = false)

    fun removeCity(city: WeatherCity) {
        viewModelScope.launch { settingsRepository.removeWeatherCity(city) }
        cityLimitHit.value = false
    }

    /** Turns weather off for the default and every facet, then calls [onDone]. */
    fun turnWeatherOff(onDone: () -> Unit) {
        viewModelScope.launch {
            turnOffWeather()
            onDone()
        }
    }

    private fun choose(city: WeatherCity, clearQuery: Boolean) {
        viewModelScope.launch {
            val accepted = settingsRepository.chooseWeatherCity(city)
            cityLimitHit.value = !accepted
            if (accepted && clearQuery) query.value = ""
        }
    }

    private companion object {
        const val SEARCH_DEBOUNCE_MS = 400L
        const val SUBSCRIPTION_TIMEOUT_MS = 5_000L
    }
}
