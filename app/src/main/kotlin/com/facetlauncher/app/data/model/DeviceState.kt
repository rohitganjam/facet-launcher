package com.facetlauncher.app.data.model

import java.time.LocalDateTime

/** The connected Wi-Fi network, if any. [ssid] is null while connected but unreadable (no location permission, or location off). */
data class WifiState(
    val connected: Boolean = false,
    val ssid: String? = null,
)

/**
 * Everything the device triggers read, sampled at one moment. Defaults are the "nothing known"
 * values: no battery reading, nothing plugged in, no Wi-Fi.
 */
data class DeviceState(
    val battery: BatteryStatus? = null,
    val headphonesPluggedIn: Boolean = false,
    val wifi: WifiState = WifiState(),
    /** Null until the first Bluetooth query answers, so a "not connected" rule never fires on a guess. */
    val connectedBluetoothAddresses: Set<String>? = null,
)

/** A runtime permission a trigger needs before it can work. */
enum class AutomationPermission { BLUETOOTH_CONNECT, LOCATION }

/**
 * Null when the trigger needs none. Wi-Fi only needs location to read a *named* network; "any
 * network" runs on the normal `ACCESS_NETWORK_STATE`.
 */
fun AutomationTrigger.requiredPermission(): AutomationPermission? = when (this) {
    is AutomationTrigger.Bluetooth -> AutomationPermission.BLUETOOTH_CONNECT
    is AutomationTrigger.Wifi -> AutomationPermission.LOCATION.takeIf { ssid != null }
    is AutomationTrigger.Schedule, is AutomationTrigger.Headphones, is AutomationTrigger.Battery -> null
}

/**
 * Whether this trigger's condition holds now. Anything that can't be known yet — Bluetooth before its
 * first query, a battery with no reading, a named network whose name is unreadable — is "not met",
 * for negated rules too, so a missing reading never fires a rule.
 */
fun AutomationTrigger.isMetBy(state: DeviceState, now: LocalDateTime): Boolean = when (this) {
    is AutomationTrigger.Schedule -> isActiveAt(now)
    is AutomationTrigger.Bluetooth -> state.connectedBluetoothAddresses?.let { (deviceAddress in it) != negated } ?: false
    is AutomationTrigger.Wifi -> isWifiMet(state.wifi)
    is AutomationTrigger.Headphones -> state.headphonesPluggedIn != negated
    is AutomationTrigger.Battery -> state.battery?.let { isMetBy(charging = it.isCharging, levelPercent = it.percent) } ?: false
}

private fun AutomationTrigger.Wifi.isWifiMet(wifi: WifiState): Boolean {
    val matches = when {
        ssid == null -> wifi.connected
        !wifi.connected -> false
        wifi.ssid == null -> return false
        else -> wifi.ssid == ssid
    }
    return matches != negated
}
