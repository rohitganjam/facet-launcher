package com.facetlauncher.app.data

import com.facetlauncher.app.data.di.ApplicationScope
import com.facetlauncher.app.data.model.DeviceState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import javax.inject.Inject
import javax.inject.Singleton

private const val STOP_TIMEOUT_MS = 10_000L

/**
 * One shared, hot [DeviceState] for facet automation, combining the battery, headphones, Wi-Fi and
 * Bluetooth sources. It stays hot while anything collects it — [com.facetlauncher.app.domain.RunFacetAutomationUseCase]
 * does for the whole process — so [current] is a cheap read, not a fresh set of receivers (which would
 * also reset Bluetooth to "unknown" on every sample).
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
    @ApplicationScope scope: CoroutineScope,
) {
    private val permissionEpoch = MutableStateFlow(0)

    val deviceState: StateFlow<DeviceState> = combine(
        batteryRepository.observeBatteryStatus(),
        headphonesRepository.observeHeadphonesPluggedIn(),
        permissionEpoch.flatMapLatest { wifiRepository.observeWifiState() },
        permissionEpoch.flatMapLatest { bluetoothRepository.observeConnectedAddresses() },
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
