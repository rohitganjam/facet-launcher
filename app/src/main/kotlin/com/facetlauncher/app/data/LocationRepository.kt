package com.facetlauncher.app.data

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationManager
import android.os.CancellationSignal
import androidx.core.content.ContextCompat
import com.facetlauncher.app.data.di.IoDispatcher
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.resume
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeoutOrNull

/** One approximate position of the device. [altitudeM] is `null` when the fix has none. */
data class DeviceLocation(val latitude: Double, val longitude: Double, val altitudeM: Int?)

/**
 * The device's approximate position, for weather. One fix at a time and never continuous updates: the network provider's
 * current location, else the last known one. It needs only `ACCESS_COARSE_LOCATION` (a fine grant covers it too), and is
 * called only while Home is visible, so no background location is involved.
 */
@Singleton
class LocationRepository @Inject constructor(
    @ApplicationContext private val context: Context,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher,
) {
    private val locationManager: LocationManager? = context.getSystemService(LocationManager::class.java)

    fun isPermissionGranted(): Boolean =
        isGranted(Manifest.permission.ACCESS_COARSE_LOCATION) || isGranted(Manifest.permission.ACCESS_FINE_LOCATION)

    /** `null` without the permission, with location services off, or when no fix arrives in time. */
    suspend fun currentLocation(): DeviceLocation? {
        val manager = locationManager
        return if (manager == null || !isPermissionGranted()) null else withContext(ioDispatcher) {
            (fresh(manager) ?: lastKnown(manager))?.toDeviceLocation()
        }
    }

    /** The last position the system knows if it is at most [maxAgeMs] old, without waiting for a new fix; else `null`. */
    suspend fun recentLocation(maxAgeMs: Long): DeviceLocation? {
        val manager = locationManager
        return if (manager == null || !isPermissionGranted()) {
            null
        } else {
            withContext(ioDispatcher) {
                lastKnown(manager)?.takeIf { System.currentTimeMillis() - it.time <= maxAgeMs }?.toDeviceLocation()
            }
        }
    }

    @Suppress("MissingPermission") // checked by isPermissionGranted() before any call; a revoked grant is caught below
    private suspend fun fresh(manager: LocationManager): Location? =
        if (!manager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
            null
        } else {
            withTimeoutOrNull(FIX_TIMEOUT_MS) {
                suspendCancellableCoroutine { continuation ->
                    val signal = CancellationSignal()
                    continuation.invokeOnCancellation { signal.cancel() }
                    try {
                        manager.getCurrentLocation(LocationManager.NETWORK_PROVIDER, signal, Runnable::run) { location ->
                            if (continuation.isActive) continuation.resume(location)
                        }
                    } catch (_: SecurityException) {
                        // The permission was taken away between the check and the call: no fresh fix.
                        if (continuation.isActive) continuation.resume(null)
                    }
                }
            }
        }

    @Suppress("MissingPermission") // checked by isPermissionGranted() before any call
    private fun lastKnown(manager: LocationManager): Location? =
        listOf(LocationManager.NETWORK_PROVIDER, LocationManager.PASSIVE_PROVIDER)
            .mapNotNull { provider -> runCatching { manager.getLastKnownLocation(provider) }.getOrNull() }
            .maxByOrNull { it.time }

    private fun isGranted(permission: String) = ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED

    // A network fix often reports an altitude of exactly 0 for "unknown"; sending that would skew MET's temperature.
    private fun Location.toDeviceLocation() = DeviceLocation(latitude, longitude, if (hasAltitude() && altitude != 0.0) altitude.toInt() else null)

    private companion object {
        const val FIX_TIMEOUT_MS = 10_000L
    }
}
