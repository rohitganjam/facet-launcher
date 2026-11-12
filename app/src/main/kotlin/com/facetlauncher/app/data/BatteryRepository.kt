package com.facetlauncher.app.data

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.BatteryManager
import com.facetlauncher.app.data.model.BatteryStatus
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow

/** `null` for a broadcast without a usable level, which is no reading, not a 0% one. */
private fun Intent.toBatteryStatus(): BatteryStatus? {
    val level = getIntExtra(BatteryManager.EXTRA_LEVEL, -1)
    val scale = getIntExtra(BatteryManager.EXTRA_SCALE, -1)
    if (level < 0 || scale <= 0) return null
    val percent = level * 100 / scale
    val status = getIntExtra(BatteryManager.EXTRA_STATUS, -1)
    val isCharging = status == BatteryManager.BATTERY_STATUS_CHARGING || status == BatteryManager.BATTERY_STATUS_FULL
    return BatteryStatus(percent = percent, isCharging = isCharging)
}

/**
 * Live battery percent/charging state — the clock's battery accessory item. `ACTION_BATTERY_CHANGED`
 * is a sticky broadcast, so registering for it both delivers the current state immediately and
 * every subsequent change; no permission is required to observe it. A broadcast without a usable level emits nothing.
 */
@Singleton
class BatteryRepository @Inject constructor(@ApplicationContext private val context: Context) {

    fun observeBatteryStatus(): Flow<BatteryStatus> = callbackFlow {
        val receiver = object : BroadcastReceiver() {
            override fun onReceive(receiverContext: Context, intent: Intent) {
                intent.toBatteryStatus()?.let { trySend(it) }
            }
        }
        val sticky = context.registerReceiver(receiver, IntentFilter(Intent.ACTION_BATTERY_CHANGED))
        sticky?.toBatteryStatus()?.let { trySend(it) }
        awaitClose { context.unregisterReceiver(receiver) }
    }
}
