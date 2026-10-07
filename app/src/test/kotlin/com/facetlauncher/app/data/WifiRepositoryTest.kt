package com.facetlauncher.app.data

import android.Manifest
import android.app.Application
import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkInfo
import android.net.wifi.WifiInfo
import android.net.wifi.WifiManager
import android.os.Looper
import androidx.test.core.app.ApplicationProvider
import com.facetlauncher.app.data.model.WifiState
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.Shadows.shadowOf
import org.robolectric.shadow.api.Shadow
import org.robolectric.shadows.ShadowNetwork
import org.robolectric.shadows.ShadowNetworkCapabilities
import org.robolectric.shadows.ShadowScanResult

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(RobolectricTestRunner::class)
class WifiRepositoryTest {

    private val context = ApplicationProvider.getApplicationContext<Context>()
    private val connectivity = shadowOf(context.getSystemService(ConnectivityManager::class.java))
    private val repository = WifiRepository(context)

    init {
        // Robolectric registers a default Wi-Fi network; these tests start from none.
        connectivity.clearAllNetworks()
    }

    private fun TestScope.idle() {
        advanceUntilIdle()
        shadowOf(Looper.getMainLooper()).idle()
        advanceUntilIdle()
    }

    private fun wifiCapabilities(ssid: String?): NetworkCapabilities {
        val capabilities = ShadowNetworkCapabilities.newInstance()
        shadowOf(capabilities).addTransportType(NetworkCapabilities.TRANSPORT_WIFI)
        ssid?.let { shadowOf(capabilities).setTransportInfo(WifiInfo.Builder().setSsid(it.toByteArray()).build()) }
        return capabilities
    }

    private fun TestScope.collectInto(results: MutableList<WifiState>) =
        launch { repository.observeWifiState().collect { results.add(it) } }

    private fun deliver(network: Network, capabilities: NetworkCapabilities) =
        connectivity.networkCallbacks.toList().forEach { it.onCapabilitiesChanged(network, capabilities) }

    private fun lose(network: Network) = connectivity.networkCallbacks.toList().forEach { it.onLost(network) }

    @Test
    fun `starts disconnected when there is no wifi`() = runTest {
        val results = mutableListOf<WifiState>()
        val job = collectInto(results)
        idle()
        job.cancel()

        assertEquals(listOf(WifiState(connected = false, ssid = null)), results)
    }

    @Test
    fun `reports an already connected network at once, without a false disconnected first`() = runTest {
        val network = ShadowNetwork.newInstance(1)
        connectivity.addNetwork(network, Shadow.newInstanceOf(NetworkInfo::class.java))
        connectivity.setNetworkCapabilities(network, wifiCapabilities("Home"))

        val results = mutableListOf<WifiState>()
        val job = collectInto(results)
        idle()
        job.cancel()

        assertEquals(listOf(WifiState(connected = true, ssid = "Home")), results)
    }

    @Test
    fun `follows a network connecting and being lost`() = runTest {
        val results = mutableListOf<WifiState>()
        val job = collectInto(results)
        idle()
        val network = ShadowNetwork.newInstance(2)

        deliver(network, wifiCapabilities("Office"))
        idle()
        lose(network)
        idle()
        job.cancel()

        assertEquals(
            listOf(WifiState(false, null), WifiState(true, "Office"), WifiState(false, null)),
            results,
        )
    }

    @Test
    fun `connected but unreadable keeps the name null`() = runTest {
        val results = mutableListOf<WifiState>()
        val job = collectInto(results)
        idle()

        deliver(ShadowNetwork.newInstance(3), wifiCapabilities(ssid = null))
        idle()
        job.cancel()

        assertEquals(WifiState(connected = true, ssid = null), results.last())
    }

    @Test
    fun `android's unknown ssid placeholder is treated as unreadable`() = runTest {
        val results = mutableListOf<WifiState>()
        val job = collectInto(results)
        idle()

        deliver(ShadowNetwork.newInstance(4), wifiCapabilities("<unknown ssid>"))
        idle()
        job.cancel()

        assertEquals(WifiState(connected = true, ssid = null), results.last())
    }

    @Test
    fun `moving to another network updates the name`() = runTest {
        val results = mutableListOf<WifiState>()
        val job = collectInto(results)
        idle()
        val first = ShadowNetwork.newInstance(5)
        val second = ShadowNetwork.newInstance(6)

        deliver(first, wifiCapabilities("Home"))
        idle()
        lose(first)
        deliver(second, wifiCapabilities("Cafe"))
        idle()
        job.cancel()

        assertEquals(WifiState(true, "Cafe"), results.last())
    }

    @Test
    fun `an unchanged state is not re-emitted`() = runTest {
        val results = mutableListOf<WifiState>()
        val job = collectInto(results)
        idle()
        val network = ShadowNetwork.newInstance(7)

        deliver(network, wifiCapabilities("Home"))
        deliver(network, wifiCapabilities("Home"))
        idle()
        job.cancel()

        assertEquals(listOf(WifiState(false, null), WifiState(true, "Home")), results)
    }

    // --- nearby networks for the picker ---

    private fun scan(ssid: String, level: Int) = ShadowScanResult.newInstance(ssid, "00:11:22:33:44:$level", "[WPA2]", level, 2412)

    private fun setScanResults(vararg results: android.net.wifi.ScanResult) =
        shadowOf(context.getSystemService(WifiManager::class.java)).setScanResults(results.toList())

    @Test
    fun `nearby networks list the current one first then the strongest scan results without duplicates`() = runTest {
        shadowOf(context as Application).grantPermissions(Manifest.permission.ACCESS_FINE_LOCATION)
        setScanResults(scan("Weak", -80), scan("Home", -40), scan("Strong", -30), scan("Home", -45))

        assertEquals(listOf("Home", "Strong", "Weak"), repository.nearbyNetworkNames(current = "Home"))
    }

    @Test
    fun `nearby networks skip hidden networks with a blank name`() = runTest {
        shadowOf(context as Application).grantPermissions(Manifest.permission.ACCESS_FINE_LOCATION)
        setScanResults(scan("", -30), scan("Cafe", -50))

        assertEquals(listOf("Cafe"), repository.nearbyNetworkNames(current = null))
    }

    @Test
    fun `nearby networks are empty without the location permission`() = runTest {
        shadowOf(context as Application).denyPermissions(Manifest.permission.ACCESS_FINE_LOCATION)
        setScanResults(scan("Home", -40))

        assertEquals(emptyList<String>(), repository.nearbyNetworkNames(current = "Home"))
    }
}
