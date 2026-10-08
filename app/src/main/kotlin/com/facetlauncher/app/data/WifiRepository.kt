package com.facetlauncher.app.data

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import android.net.wifi.WifiInfo
import android.net.wifi.WifiManager
import androidx.core.content.ContextCompat
import com.facetlauncher.app.data.model.WifiState
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

private const val UNKNOWN_SSID = "<unknown ssid>"

/**
 * Wi-Fi connection state for facet automation, and the list of networks the "Named network" picker
 * offers. "Connected to any Wi-Fi" needs only the normal `ACCESS_NETWORK_STATE`; reading *which* network
 * needs `ACCESS_FINE_LOCATION` (and location turned on), and without it [WifiState.ssid] is null.
 */
@Singleton
class WifiRepository @Inject constructor(@ApplicationContext private val context: Context) {

    private val connectivityManager: ConnectivityManager? get() = context.getSystemService(ConnectivityManager::class.java)

    /** Emits the current state on subscribe (read synchronously, so there is no false "disconnected" at start), then on every change. */
    fun observeWifiState(): Flow<WifiState> = callbackFlow {
        val manager = connectivityManager
        if (manager == null) {
            trySend(WifiState())
            awaitClose { }
        } else {
            // Callbacks arrive serially on one thread, so a plain map is safe here.
            val networks = mutableMapOf<Network, String?>()
            @Suppress("DEPRECATION")
            manager.allNetworks.forEach { network ->
                manager.getNetworkCapabilities(network)?.takeIf { it.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) }
                    ?.let { networks[network] = it.ssidOrNull() }
            }
            trySend(networks.toWifiState())

            val callback = object : ConnectivityManager.NetworkCallback(FLAG_INCLUDE_LOCATION_INFO) {
                override fun onCapabilitiesChanged(network: Network, capabilities: NetworkCapabilities) {
                    networks[network] = capabilities.ssidOrNull()
                    trySend(networks.toWifiState())
                }

                override fun onLost(network: Network) {
                    networks.remove(network)
                    trySend(networks.toWifiState())
                }
            }
            manager.registerNetworkCallback(NetworkRequest.Builder().addTransportType(NetworkCapabilities.TRANSPORT_WIFI).build(), callback)
            awaitClose { manager.unregisterNetworkCallback(callback) }
        }
    }.distinctUntilChanged()

    /**
     * Names for the network picker: the current network first, then the networks in the latest scan,
     * strongest first, without duplicates. Empty without the location permission. Android hides a user's
     * saved networks from apps, so only networks in range can be listed.
     */
    suspend fun nearbyNetworkNames(current: String?): List<String> = withContext(Dispatchers.IO) {
        val granted = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
        val wifiManager = context.getSystemService(WifiManager::class.java)?.takeIf { granted }
        @Suppress("DEPRECATION")
        val scanned = wifiManager?.scanResults.orEmpty().sortedByDescending { it.level }.map { it.SSID }
        if (wifiManager == null) emptyList() else (listOfNotNull(current) + scanned).filter { it.isNotBlank() }.distinct()
    }
}

private fun Map<Network, String?>.toWifiState(): WifiState =
    if (isEmpty()) WifiState() else WifiState(connected = true, ssid = values.firstNotNullOfOrNull { it })

/** The SSID without the quotes Android wraps it in, or null when it is unreadable (no location permission, or location off). */
private fun NetworkCapabilities.ssidOrNull(): String? =
    (transportInfo as? WifiInfo)?.ssid?.removeSurrounding("\"")?.takeIf { it.isNotBlank() && it != UNKNOWN_SSID }
