package com.facetlauncher.app.domain

import com.facetlauncher.app.data.DeviceLocation
import com.facetlauncher.app.data.LocationRepository
import com.facetlauncher.app.data.SettingsRepository
import com.facetlauncher.app.data.model.LauncherSettings
import com.facetlauncher.app.data.model.WeatherLocationMode
import com.facetlauncher.app.data.model.WeatherPlace
import com.facetlauncher.app.data.places.distanceKm
import com.facetlauncher.app.data.weather.PlaceNameSource
import com.facetlauncher.app.data.weather.RefreshOutcome
import com.facetlauncher.app.data.weather.WeatherRepository
import javax.inject.Inject
import kotlinx.coroutines.flow.first

sealed interface WeatherRefreshResult {
    /** "Use current location" is chosen but the location permission is not granted. */
    data object NeedsLocationPermission : WeatherRefreshResult

    /** There is a permission but no position, and no earlier place to fall back to. */
    data object NoLocation : WeatherRefreshResult

    /** "Choose a city" is chosen but no city was picked. */
    data object NoCity : WeatherRefreshResult
    data class Done(val outcome: RefreshOutcome) : WeatherRefreshResult
}

/** The place to ask MET about, or why there is none. */
private sealed interface PlaceChoice {
    data class Use(val place: WeatherPlace) : PlaceChoice
    data class Missing(val result: WeatherRefreshResult) : PlaceChoice
}

/**
 * One refresh of the weather for wherever the user is (or the city they chose), through the repository's gate. Callers
 * (Home on resume, the sheet's button) call it only while weather is shown, so with weather off nothing here runs: no
 * location read, no geocoder, no request.
 *
 * With current location: the device's position is read, and the stored place is kept while the device is within
 * [MOVED_KM] of it, so walking around town does not refetch. A new place is rounded to two decimals. Once the place is
 * known its name is looked up when it has none (or only the offline one), and stored with it.
 */
class RefreshWeatherUseCase @Inject constructor(
    private val settingsRepository: SettingsRepository,
    private val locationRepository: LocationRepository,
    private val weatherRepository: WeatherRepository,
    private val resolvePlace: ResolvePlaceUseCase,
) {
    suspend operator fun invoke(): WeatherRefreshResult {
        val settings = settingsRepository.settings.first()
        return when (val choice = choosePlace(settings)) {
            is PlaceChoice.Missing -> choice.result
            is PlaceChoice.Use -> {
                val outcome = weatherRepository.refresh(choice.place)
                if (settings.weatherLocationMode == WeatherLocationMode.CURRENT) nameIfMissing(choice.place)
                WeatherRefreshResult.Done(outcome)
            }
        }
    }

    private suspend fun choosePlace(settings: LauncherSettings): PlaceChoice = when (settings.weatherLocationMode) {
        WeatherLocationMode.CITY -> settings.weatherCity
            ?.let { PlaceChoice.Use(WeatherPlace.rounded(it.latitude, it.longitude)) }
            ?: PlaceChoice.Missing(WeatherRefreshResult.NoCity)
        WeatherLocationMode.CURRENT -> currentPlace()
    }

    private suspend fun currentPlace(): PlaceChoice {
        val stored = weatherRepository.observeState().first().place
        val location = if (locationRepository.isPermissionGranted()) locationRepository.currentLocation() else null
        return when {
            !locationRepository.isPermissionGranted() -> PlaceChoice.Missing(WeatherRefreshResult.NeedsLocationPermission)
            location == null -> stored?.let { PlaceChoice.Use(it) } ?: PlaceChoice.Missing(WeatherRefreshResult.NoLocation)
            stored != null && hasNotMoved(stored, location) -> PlaceChoice.Use(stored)
            else -> PlaceChoice.Use(WeatherPlace.rounded(location.latitude, location.longitude, location.altitudeM))
        }
    }

    private fun hasNotMoved(stored: WeatherPlace, location: DeviceLocation) =
        distanceKm(stored.latitude, stored.longitude, location.latitude, location.longitude) < MOVED_KM

    private suspend fun nameIfMissing(place: WeatherPlace) {
        val state = weatherRepository.observeState().first()
        val needsName = state.place == place && (state.placeName == null || state.placeNameSource == PlaceNameSource.OFFLINE)
        if (needsName) {
            val resolved = resolvePlace(place.latitude, place.longitude)
            if (resolved.name != null) weatherRepository.setPlaceName(place, resolved.name, resolved.source)
        }
    }

    companion object {
        const val MOVED_KM = 5.0
    }
}
