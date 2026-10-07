package com.facetlauncher.app.data

import android.Manifest
import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothManager
import android.bluetooth.BluetoothProfile
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat
import androidx.core.content.IntentCompat
import com.facetlauncher.app.data.model.PairedBluetoothDevice
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

private const val PROFILE_QUERY_TIMEOUT_MS = 1_500L

/** The profiles that carry a typical phone-to-car, headset or watch link; GATT (BLE) is read directly. */
private val QUERIED_PROFILES = listOf(BluetoothProfile.A2DP, BluetoothProfile.HEADSET)

/**
 * Which paired Bluetooth devices are connected, for facet automation, and the paired list the device
 * picker offers. Both need `BLUETOOTH_CONNECT`; without it, connections are unknown and the list is empty.
 *
 * Android has no public "is this device connected" call, so it combines three things: connect and
 * disconnect broadcasts, a one-time query of the A2DP and headset profiles plus GATT at start, and an
 * "unknown" state until that query answers (or times out), so a "not connected" rule never fires on a guess.
 */
@Singleton
class BluetoothRepository @Inject constructor(@ApplicationContext private val context: Context) {

    private val bluetoothManager: BluetoothManager? get() = context.getSystemService(BluetoothManager::class.java)

    private fun permissionGranted() =
        ContextCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH_CONNECT) == PackageManager.PERMISSION_GRANTED

    /** Null while unknown (no permission, or the first query hasn't answered), then the connected addresses on every change. */
    fun observeConnectedAddresses(): Flow<Set<String>?> = callbackFlow {
        val adapter = bluetoothManager?.adapter
        if (adapter == null || !permissionGranted()) {
            trySend(null)
            awaitClose { }
        } else {
            val tracker = BluetoothConnectionTracker(context, adapter, bluetoothManager) { trySend(it) }
            tracker.start()
            launch {
                delay(PROFILE_QUERY_TIMEOUT_MS)
                tracker.markKnown()
            }
            awaitClose { tracker.stop() }
        }
    }.distinctUntilChanged()

    /** Paired devices, by name. Empty without the permission or without Bluetooth. */
    @SuppressLint("MissingPermission") // guarded by permissionGranted()
    fun pairedDevices(): List<PairedBluetoothDevice> {
        val adapter = bluetoothManager?.adapter
        if (adapter == null || !permissionGranted()) return emptyList()
        return adapter.bondedDevices.orEmpty()
            .map { PairedBluetoothDevice(address = it.address, name = it.name?.takeIf(String::isNotBlank) ?: it.address) }
            .sortedBy { it.name.lowercase() }
    }
}

/** Mutable bookkeeping for one subscription. Everything is called from the main thread (broadcasts and profile callbacks). */
@SuppressLint("MissingPermission") // only constructed after the permission check
private class BluetoothConnectionTracker(
    private val context: Context,
    private val adapter: BluetoothAdapter,
    private val manager: BluetoothManager?,
    private val onChange: (Set<String>?) -> Unit,
) {
    private val connected = mutableSetOf<String>()
    private val proxies = mutableMapOf<Int, BluetoothProfile>()
    private var pendingProfiles = QUERIED_PROFILES.size
    private var known = false

    private val receiver = object : BroadcastReceiver() {
        override fun onReceive(receiverContext: Context, intent: Intent) {
            val address = IntentCompat.getParcelableExtra(intent, BluetoothDevice.EXTRA_DEVICE, BluetoothDevice::class.java)?.address
            when (intent.action) {
                BluetoothDevice.ACTION_ACL_CONNECTED -> address?.let { connected += it }
                BluetoothDevice.ACTION_ACL_DISCONNECTED -> address?.let { connected -= it }
                BluetoothAdapter.ACTION_STATE_CHANGED ->
                    if (intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, -1) == BluetoothAdapter.STATE_OFF) connected.clear()
            }
            publish()
        }
    }

    fun start() {
        val filter = IntentFilter().apply {
            addAction(BluetoothDevice.ACTION_ACL_CONNECTED)
            addAction(BluetoothDevice.ACTION_ACL_DISCONNECTED)
            addAction(BluetoothAdapter.ACTION_STATE_CHANGED)
        }
        ContextCompat.registerReceiver(context, receiver, filter, ContextCompat.RECEIVER_NOT_EXPORTED)
        onChange(null)
        if (!adapter.isEnabled) {
            markKnown()
            return
        }
        manager?.getConnectedDevices(BluetoothProfile.GATT)?.forEach { connected += it.address }
        QUERIED_PROFILES.forEach { profile ->
            val listener = object : BluetoothProfile.ServiceListener {
                override fun onServiceConnected(profileId: Int, proxy: BluetoothProfile) {
                    proxies[profileId] = proxy
                    proxy.connectedDevices.forEach { connected += it.address }
                    profileAnswered()
                }

                override fun onServiceDisconnected(profileId: Int) = Unit
            }
            if (!adapter.getProfileProxy(context, listener, profile)) profileAnswered()
        }
    }

    /** The first query has answered (or timed out): from here on the set is a real answer. */
    fun markKnown() {
        known = true
        publish()
    }

    fun stop() {
        context.unregisterReceiver(receiver)
        proxies.forEach { (profile, proxy) -> adapter.closeProfileProxy(profile, proxy) }
        proxies.clear()
    }

    private fun profileAnswered() {
        pendingProfiles--
        if (pendingProfiles <= 0) markKnown()
    }

    private fun publish() = onChange(if (known) connected.toSet() else null)
}
