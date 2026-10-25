package com.facetlauncher.app.data.places

import android.content.Context
import android.location.Address
import android.location.Geocoder
import android.os.Build
import com.facetlauncher.app.data.di.IoDispatcher
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.IOException
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.resume
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext

/** A place found by the device's geocoder. */
data class GeocodedPlace(
    val name: String,
    val admin: String?,
    val countryCode: String?,
    val latitude: Double,
    val longitude: Double,
)

/**
 * The device's geocoder, which turns a position into a place name and a name into positions in the user's language. Usually
 * backed by Google Play services, so it may be missing or fail without a network; every call then answers "nothing".
 */
interface PlaceGeocoder {
    suspend fun reverse(latitude: Double, longitude: Double, locale: Locale): GeocodedPlace?
    suspend fun search(query: String, locale: Locale, maxResults: Int): List<GeocodedPlace>
}

/** [PlaceGeocoder] over `android.location.Geocoder`: the listener API on Android 13 and up, the blocking one before. */
@Singleton
class AndroidPlaceGeocoder @Inject constructor(
    @ApplicationContext private val context: Context,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher,
) : PlaceGeocoder {

    override suspend fun reverse(latitude: Double, longitude: Double, locale: Locale): GeocodedPlace? =
        lookup(locale) { geocoder, deliver ->
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                geocoder.getFromLocation(latitude, longitude, 1, listener(deliver))
            } else {
                @Suppress("DEPRECATION")
                deliver(geocoder.getFromLocation(latitude, longitude, 1).orEmpty())
            }
        }.firstOrNull()

    override suspend fun search(query: String, locale: Locale, maxResults: Int): List<GeocodedPlace> =
        lookup(locale) { geocoder, deliver ->
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                geocoder.getFromLocationName(query, maxResults, listener(deliver))
            } else {
                @Suppress("DEPRECATION")
                deliver(geocoder.getFromLocationName(query, maxResults).orEmpty())
            }
        }

    private suspend fun lookup(locale: Locale, ask: (Geocoder, (List<Address>) -> Unit) -> Unit): List<GeocodedPlace> =
        if (!Geocoder.isPresent()) {
            emptyList()
        } else {
            withContext(ioDispatcher) {
                try {
                    suspendCancellableCoroutine { continuation ->
                        ask(Geocoder(context, locale)) { addresses -> if (continuation.isActive) continuation.resume(addresses.mapNotNull(::toPlace)) }
                    }
                } catch (_: IOException) {
                    emptyList()
                }
            }
        }

    private fun listener(deliver: (List<Address>) -> Unit) = object : Geocoder.GeocodeListener {
        override fun onGeocode(addresses: MutableList<Address>) = deliver(addresses)
        override fun onError(errorMessage: String?) = deliver(emptyList())
    }

    private fun toPlace(address: Address): GeocodedPlace? {
        val name = address.locality ?: address.subAdminArea ?: address.adminArea
        return if (name.isNullOrBlank() || !address.hasLatitude() || !address.hasLongitude()) null else GeocodedPlace(
            name = name,
            admin = address.adminArea,
            countryCode = address.countryCode,
            latitude = address.latitude,
            longitude = address.longitude,
        )
    }
}
