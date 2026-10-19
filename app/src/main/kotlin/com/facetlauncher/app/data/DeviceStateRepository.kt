package com.facetlauncher.app.data

import com.facetlauncher.app.data.di.ApplicationScope
import com.facetlauncher.app.data.model.AutomationRule
import com.facetlauncher.app.data.model.AutomationTrigger
import com.facetlauncher.app.data.model.DeviceState
import com.facetlauncher.app.data.model.WifiState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import javax.inject.Inject
import javax.inject.Singleton

private const val STOP_TIMEOUT_MS = 10_000L

/** How much Wi-Fi detail the enabled rules need: none, only whether Wi-Fi is connected, or the network name (which needs location). */
private enum class WifiNeed { NONE, CONNECTED, NAMED }

/** Which sources the enabled rules read — a source nobody reads is never subscribed. */
private data class SourceNeeds(
    val battery: Boolean = false,
    val headphones: Boolean = false,
    val wifi: WifiNeed = WifiNeed.NONE,
    val bluetooth: Boolean = false,
)

private fun List<AutomationRule>.toSourceNeeds(): SourceNeeds {
    val triggers = filter { it.enabled }.map { it.trigger }
    return SourceNeeds(
        battery = triggers.any { it is AutomationTrigger.Battery },
        headphones = triggers.any { it is AutomationTrigger.Headphones },
        wifi = when {
            triggers.any { it is AutomationTrigger.Wifi && it.ssid != null } -> WifiNeed.NAMED
            triggers.any { it is AutomationTrigger.Wifi } -> WifiNeed.CONNECTED
            else -> WifiNeed.NONE
        },
        bluetooth = triggers.any { it is AutomationTrigger.Bluetooth },
    )
}

/**
 * One shared, hot [DeviceState] for facet automation, combining the battery, headphones, Wi-Fi and
 * Bluetooth sources. It stays hot while anything collects it — [com.facetlauncher.app.domain.RunFacetAutomationUseCase]
 * does for the whole process — so [current] is a cheap read, not a fresh set of receivers (which would
 * also reset Bluetooth to "unknown" on every sample).
 *
 * Each source is subscribed only while an enabled rule reads it, so with no rules nothing is registered,
 * and the Wi-Fi source asks Android for the network name (which needs location) only for a "Named
 * network" rule. A source nobody reads reports its "nothing known" default. Each source reacts to its
 * own need only, so adding a rule never resets another source (Bluetooth would drop to "unknown").
 *
 * The Wi-Fi and Bluetooth sources check their permission when they subscribe, so [onPermissionsChanged]
 * re-subscribes them after the user grants one.
 */
@OptIn(ExperimentalCoroutinesApi::class)
@Singleton
class DeviceStateRepository @Inject constructor(
    batteryRepository: BatteryRepository,
    headphonesRepository: HeadphonesRepository,
    wifiRepository: WifiRepository,
    bluetoothRepository: BluetoothRepository,
    ruleRepository: AutomationRuleRepository,
    @ApplicationScope scope: CoroutineScope,
) {
    private val permissionEpoch = MutableStateFlow(0)

    private val needs: Flow<SourceNeeds> = ruleRepository.observeRules().map { it.toSourceNeeds() }

    /** [whenNeeded] while [isNeeded] holds, [otherwise] (the source's "nothing known" value) while it doesn't. */
    private fun <R> gated(isNeeded: (SourceNeeds) -> Boolean, otherwise: R, whenNeeded: () -> Flow<R>): Flow<R> =
        needs.map(isNeeded).distinctUntilChanged().flatMapLatest { if (it) whenNeeded() else flowOf(otherwise) }

    val deviceState: StateFlow<DeviceState> = combine(
        gated({ it.battery }, null) { batteryRepository.observeBatteryStatus() },
        gated({ it.headphones }, false) { headphonesRepository.observeHeadphonesPluggedIn() },
        needs.map { it.wifi }.distinctUntilChanged().flatMapLatest { need ->
            if (need == WifiNeed.NONE) {
                flowOf(WifiState())
            } else {
                permissionEpoch.flatMapLatest { wifiRepository.observeWifiState(includeSsid = need == WifiNeed.NAMED) }
            }
        },
        gated({ it.bluetooth }, null) { permissionEpoch.flatMapLatest { bluetoothRepository.observeConnectedAddresses() } },
    ) { battery, headphones, wifi, bluetooth ->
        DeviceState(battery = battery, headphonesPluggedIn = headphones, wifi = wifi, connectedBluetoothAddresses = bluetooth)
    }.stateIn(scope, SharingStarted.WhileSubscribed(STOP_TIMEOUT_MS), DeviceState())

    /** The latest known state; the "nothing known yet" default until the sources have first answered. */
    fun current(): DeviceState = deviceState.value

    /** Call after a permission is granted so the sources that were blocked by it start reading. */
    fun onPermissionsChanged() {
        permissionEpoch.update { it + 1 }
    }
}
