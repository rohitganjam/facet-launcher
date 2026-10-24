package com.facetlauncher.app.domain

import com.facetlauncher.app.data.places.CityRepository
import com.facetlauncher.app.data.places.PlaceGeocoder
import com.facetlauncher.app.data.weather.PlaceNameSource
import java.util.Locale
import javax.inject.Inject

/** What is known about a position: a display name and where it came from, and the IANA timezone of the nearest listed city. */
data class ResolvedPlace(val name: String?, val source: PlaceNameSource?, val timezone: String?)

/**
 * Names a position. The device's geocoder answers first (in the user's language, exact); when it is missing or has no
 * answer, the nearest city of the offline list is used and the caller shows it as "Near X". Spans two data classes, so it is
 * a use case. The timezone comes from the nearest listed city (in the geocoder's country when it gave one), and is `null` when
 * none is within [MAX_CITY_KM], in which case the caller uses the device's zone.
 */
class ResolvePlaceUseCase @Inject constructor(
    private val geocoder: PlaceGeocoder,
    private val cities: CityRepository,
) {
    suspend operator fun invoke(latitude: Double, longitude: Double, locale: Locale = Locale.getDefault()): ResolvedPlace {
        val geocoded = geocoder.reverse(latitude, longitude, locale)
        val nearest = cities.nearest(latitude, longitude, geocoded?.countryCode, MAX_CITY_KM)
            ?: cities.nearest(latitude, longitude, null, MAX_CITY_KM)
        return when {
            geocoded != null -> ResolvedPlace(geocoded.name, PlaceNameSource.GEOCODER, nearest?.timezone)
            nearest != null -> ResolvedPlace(nearest.name, PlaceNameSource.OFFLINE, nearest.timezone)
            else -> ResolvedPlace(null, null, null)
        }
    }

    companion object {
        const val MAX_CITY_KM = 300.0
    }
}
