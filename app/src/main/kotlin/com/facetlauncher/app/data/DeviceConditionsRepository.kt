package com.facetlauncher.app.data

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.PowerManager
import androidx.core.content.ContextCompat
import com.facetlauncher.app.data.model.NetworkConditions
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow

/** The phone's conditions at this moment, read on demand: the network, Battery Saver, and whether the screen is on. `open` for tests. */
@Singleton
open class DeviceConditionsRepository @Inject constructor(@ApplicationContext private val context: Context) {

    /** The active network: connected (validated), unmetered (Wi-Fi or another network the system says is not metered) and roaming. */
    open fun network(): NetworkConditions {
        val manager = context.getSystemService(ConnectivityManager::class.java)
        val capabilities = manager?.let { runCatching { it.getNetworkCapabilities(it.activeNetwork) }.getOrNull() }
            ?: return NetworkConditions(connected = false, unmetered = false, roaming = false)
        return NetworkConditions(
            connected = capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) &&
                capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED),
            unmetered = capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_NOT_METERED) ||
                capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_TEMPORARILY_NOT_METERED),
            roaming = !capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_NOT_ROAMING),
        )
    }

    open fun isPowerSaveMode(): Boolean = context.getSystemService(PowerManager::class.java)?.isPowerSaveMode ?: false

    open fun isScreenOn(): Boolean = context.getSystemService(PowerManager::class.java)?.isInteractive ?: true

    /** Emits each time the screen turns off, while collected (a runtime receiver, so only while the process is alive). */
    open fun screenOffEvents(): Flow<Unit> = callbackFlow {
        val receiver = object : BroadcastReceiver() {
            override fun onReceive(receiverContext: Context, intent: Intent) {
                trySend(Unit)
            }
        }
        ContextCompat.registerReceiver(context, receiver, IntentFilter(Intent.ACTION_SCREEN_OFF), ContextCompat.RECEIVER_NOT_EXPORTED)
        awaitClose { context.unregisterReceiver(receiver) }
    }
}
