package com.facetlauncher.app.domain

import com.facetlauncher.app.data.DeviceLocation
import com.facetlauncher.app.data.LocationRepository
import com.facetlauncher.app.data.SettingsRepository
import com.facetlauncher.app.data.model.WeatherLocationMode
import com.facetlauncher.app.data.model.WeatherPlace
import com.facetlauncher.app.data.places.distanceKm
import com.facetlauncher.app.data.weather.PlaceNameSource
import com.facetlauncher.app.data.weather.RefreshOutcome
import com.facetlauncher.app.data.weather.WeatherRepository
import javax.inject.Inject
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
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

/**
 * One refresh of the weather for wherever the user is (or the city they chose), through the repository's gate. Callers
 * (Home on resume, the sheet's button) call it only while weather is shown, so with weather off nothing here runs: no
 * location read, no geocoder, no request.
 *
 * With current location and a stored place, the request for that place starts at once while the position is read, so the
 * weather does not wait for the fix; if the device has since moved [MOVED_KM] or more, the new place is fetched next. With
 * no stored place a recent last-known position is used rather than waiting for a fresh one. A new place is rounded to two
 * decimals, and once it is known its name is looked up when it has none (or only the offline one) and stored with it.
 */
class RefreshWeatherUseCase @Inject constructor(
    private val settingsRepository: SettingsRepository,
    private val locationRepository: LocationRepository,
    private val weatherRepository: WeatherRepository,
    private val resolvePlace: ResolvePlaceUseCase,
) {
    suspend operator fun invoke(): WeatherRefreshResult {
        val settings = settingsRepository.settings.first()
        return when (settings.weatherLocationMode) {
            WeatherLocationMode.CITY -> settings.weatherCity
                ?.let { WeatherRefreshResult.Done(weatherRepository.refresh(WeatherPlace.rounded(it.latitude, it.longitude))) }
                ?: WeatherRefreshResult.NoCity
            WeatherLocationMode.CURRENT -> when {
                !locationRepository.isPermissionGranted() -> WeatherRefreshResult.NeedsLocationPermission
                else -> refreshCurrent()
            }
        }
    }

    private suspend fun refreshCurrent(): WeatherRefreshResult {
        val stored = weatherRepository.observeState().first().place
        return if (stored == null) firstFetch() else refreshStored(stored)
    }

    private suspend fun firstFetch(): WeatherRefreshResult {
        val location = locationRepository.recentLocation(RECENT_FIX_MS) ?: locationRepository.currentLocation()
        return if (location == null) WeatherRefreshResult.NoLocation else refreshAndName(placeAt(location))
    }

    private suspend fun refreshStored(stored: WeatherPlace): WeatherRefreshResult = coroutineScope {
        val early = async { weatherRepository.refresh(stored) }
        val moved = locationRepository.currentLocation()?.takeUnless { hasNotMoved(stored, it) }?.let(::placeAt)
        val storedOutcome = early.await()
        val result = if (moved == null) WeatherRefreshResult.Done(storedOutcome) else WeatherRefreshResult.Done(weatherRepository.refresh(moved))
        nameIfMissing(moved ?: stored)
        result
    }

    private suspend fun refreshAndName(place: WeatherPlace): WeatherRefreshResult {
        val outcome = weatherRepository.refresh(place)
        nameIfMissing(place)
        return WeatherRefreshResult.Done(outcome)
    }

    private fun placeAt(location: DeviceLocation) = WeatherPlace.rounded(location.latitude, location.longitude, location.altitudeM)

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

        /** A last-known position this recent is used for a first fetch instead of waiting for a fresh fix. */
        const val RECENT_FIX_MS = 2 * 60 * 60 * 1000L
    }
}
