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
    val city: WeatherCity? = null,
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
) : ViewModel() {

    private val query = MutableStateFlow("")
    private val permissionGranted = MutableStateFlow(locationRepository.isPermissionGranted())

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
            permissionGranted = granted,
            showingFor = if (settings.weatherLocationMode == WeatherLocationMode.CITY) settings.weatherCity?.name else state.placeName,
            query = text,
            results = results,
            isSearching = searching,
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(SUBSCRIPTION_TIMEOUT_MS), WeatherLocationUiState())

    fun onQueryChange(text: String) {
        query.value = text
    }

    fun refreshPermission() {
        permissionGranted.value = locationRepository.isPermissionGranted()
    }

    fun useCurrentLocation() {
        viewModelScope.launch { settingsRepository.setWeatherLocationMode(WeatherLocationMode.CURRENT) }
    }

    /** Saves [place] as the weather city and switches to it. */
    fun chooseCity(place: PlaceCandidate) {
        viewModelScope.launch {
            settingsRepository.setWeatherCity(place.toWeatherCity())
            settingsRepository.setWeatherLocationMode(WeatherLocationMode.CITY)
            query.value = ""
        }
    }

    private companion object {
        const val SEARCH_DEBOUNCE_MS = 400L
        const val SUBSCRIPTION_TIMEOUT_MS = 5_000L
    }
}
