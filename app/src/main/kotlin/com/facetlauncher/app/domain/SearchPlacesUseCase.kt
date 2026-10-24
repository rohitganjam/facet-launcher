package com.facetlauncher.app.domain

import com.facetlauncher.app.data.model.WeatherCity
import com.facetlauncher.app.data.places.City
import com.facetlauncher.app.data.places.CityRepository
import com.facetlauncher.app.data.places.GeocodedPlace
import com.facetlauncher.app.data.places.PlaceGeocoder
import com.facetlauncher.app.data.places.distanceKm
import java.time.ZoneId
import java.util.Locale
import javax.inject.Inject

/** A place the user can pick for weather. [timezone] is an IANA id. */
data class PlaceCandidate(
    val name: String,
    val admin: String?,
    val countryCode: String?,
    val latitude: Double,
    val longitude: Double,
    val timezone: String,
) {
    fun toWeatherCity() = WeatherCity(name, latitude, longitude, timezone)
}

/**
 * City search for the weather location picker: the device's geocoder first (any place, in the user's language), then the
 * offline list, which also answers when the geocoder is missing. A list match within [DUPLICATE_KM] of a geocoder result is
 * the same place and is left out. A geocoder result takes the timezone of the nearest listed city in its country, else the
 * device's zone.
 */
class SearchPlacesUseCase @Inject constructor(
    private val geocoder: PlaceGeocoder,
    private val cities: CityRepository,
) {
    suspend operator fun invoke(query: String, locale: Locale = Locale.getDefault(), limit: Int = DEFAULT_LIMIT): List<PlaceCandidate> {
        val geocoded = if (query.isBlank()) emptyList() else geocoder.search(query, locale, limit).map { toCandidate(it) }
        val listed = if (query.isBlank()) emptyList() else cities.search(query, limit).map { toCandidate(it) }
        return (geocoded + listed.filterNot { city -> geocoded.any { isSamePlace(it, city) } }).take(limit)
    }

    private suspend fun toCandidate(place: GeocodedPlace): PlaceCandidate {
        val nearest = cities.nearest(place.latitude, place.longitude, place.countryCode, ResolvePlaceUseCase.MAX_CITY_KM)
        return PlaceCandidate(
            name = place.name,
            admin = place.admin,
            countryCode = place.countryCode,
            latitude = place.latitude,
            longitude = place.longitude,
            timezone = nearest?.timezone ?: ZoneId.systemDefault().id,
        )
    }

    private fun toCandidate(city: City) = PlaceCandidate(
        name = city.name,
        admin = city.admin.ifBlank { null },
        countryCode = city.countryCode,
        latitude = city.latitude,
        longitude = city.longitude,
        timezone = city.timezone,
    )

    private fun isSamePlace(a: PlaceCandidate, b: PlaceCandidate) = distanceKm(a.latitude, a.longitude, b.latitude, b.longitude) <= DUPLICATE_KM

    companion object {
        const val DEFAULT_LIMIT = 10
        const val DUPLICATE_KM = 10.0
    }
}
