package com.facetlauncher.app.data

import com.facetlauncher.app.data.model.BatteryStatus
import com.facetlauncher.app.data.model.DeviceState
import com.facetlauncher.app.data.model.WifiState
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test
import org.mockito.Mockito.atLeast
import org.mockito.Mockito.mock
import org.mockito.Mockito.times
import org.mockito.Mockito.verify
import org.mockito.Mockito.`when`

@OptIn(ExperimentalCoroutinesApi::class)
class DeviceStateRepositoryTest {

    private class Fixture(scope: kotlinx.coroutines.CoroutineScope) {
        val battery = MutableStateFlow(BatteryStatus(percent = 80, isCharging = false))
        val headphones = MutableStateFlow(false)
        val wifi = MutableStateFlow(WifiState())
        val bluetooth = MutableStateFlow<Set<String>?>(null)

        val batteryRepository = mock(BatteryRepository::class.java).also { `when`(it.observeBatteryStatus()).thenReturn(battery) }
        val headphonesRepository = mock(HeadphonesRepository::class.java).also { `when`(it.observeHeadphonesPluggedIn()).thenReturn(headphones) }
        val wifiRepository = mock(WifiRepository::class.java).also { `when`(it.observeWifiState()).thenReturn(wifi) }
        val bluetoothRepository = mock(BluetoothRepository::class.java).also { `when`(it.observeConnectedAddresses()).thenReturn(bluetooth) }

        val repository = DeviceStateRepository(batteryRepository, headphonesRepository, wifiRepository, bluetoothRepository, scope)
    }

    private fun TestScope.start(fixture: Fixture) {
        backgroundScope.launch { fixture.repository.deviceState.collect { } }
        runCurrent()
    }

    @Test
    fun `before anything is collected the state is the nothing-known default`() = runTest {
        val fixture = Fixture(backgroundScope)

        assertEquals(DeviceState(), fixture.repository.current())
    }

    @Test
    fun `combines every source into one state`() = runTest {
        val fixture = Fixture(backgroundScope)
        start(fixture)
        fixture.headphones.value = true
        fixture.wifi.value = WifiState(connected = true, ssid = "Home")
        fixture.bluetooth.value = setOf("AA:BB")
        runCurrent()

        assertEquals(
            DeviceState(
                battery = BatteryStatus(80, false),
                headphonesPluggedIn = true,
                wifi = WifiState(true, "Home"),
                connectedBluetoothAddresses = setOf("AA:BB"),
            ),
            fixture.repository.current(),
        )
    }

    @Test
    fun `follows a source changing`() = runTest {
        val fixture = Fixture(backgroundScope)
        start(fixture)

        fixture.battery.value = BatteryStatus(percent = 19, isCharging = true)
        runCurrent()

        assertEquals(BatteryStatus(19, true), fixture.repository.current().battery)
    }

    @Test
    fun `bluetooth stays unknown until its source reports`() = runTest {
        val fixture = Fixture(backgroundScope)
        start(fixture)

        assertEquals(null, fixture.repository.current().connectedBluetoothAddresses)

        fixture.bluetooth.value = emptySet()
        runCurrent()

        assertEquals(emptySet<String>(), fixture.repository.current().connectedBluetoothAddresses)
    }

    @Test
    fun `sampling does not resubscribe the sources`() = runTest {
        val fixture = Fixture(backgroundScope)
        start(fixture)

        repeat(5) { fixture.repository.current() }

        verify(fixture.bluetoothRepository, times(1)).observeConnectedAddresses()
        verify(fixture.wifiRepository, times(1)).observeWifiState()
    }

    @Test
    fun `a permission change resubscribes wifi and bluetooth but not the others`() = runTest {
        val fixture = Fixture(backgroundScope)
        start(fixture)

        fixture.repository.onPermissionsChanged()
        runCurrent()

        verify(fixture.bluetoothRepository, times(2)).observeConnectedAddresses()
        verify(fixture.wifiRepository, times(2)).observeWifiState()
        verify(fixture.batteryRepository, times(1)).observeBatteryStatus()
        verify(fixture.headphonesRepository, atLeast(1)).observeHeadphonesPluggedIn()
    }
}
