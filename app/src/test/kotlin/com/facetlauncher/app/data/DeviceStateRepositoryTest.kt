package com.facetlauncher.app.data

import com.facetlauncher.app.data.model.AutomationRule
import com.facetlauncher.app.data.model.AutomationTrigger
import com.facetlauncher.app.data.model.BatteryDirection
import com.facetlauncher.app.data.model.BatteryLevelCondition
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
import org.mockito.Mockito.never
import org.mockito.Mockito.times
import org.mockito.Mockito.verify
import org.mockito.Mockito.`when`

private fun rule(id: Long, trigger: AutomationTrigger, enabled: Boolean = true) =
    AutomationRule(id = id, targetFacetId = 1, trigger = trigger, enabled = enabled)

private val batteryRule = rule(1, AutomationTrigger.Battery(false, BatteryLevelCondition(BatteryDirection.BELOW, 20)))
private val headphonesRule = rule(2, AutomationTrigger.Headphones())
private val bluetoothRule = rule(4, AutomationTrigger.Bluetooth("AA:BB", "Buds"))
private fun wifiRule(ssid: String?, enabled: Boolean = true) = rule(3, AutomationTrigger.Wifi(ssid = ssid), enabled)

@OptIn(ExperimentalCoroutinesApi::class)
class DeviceStateRepositoryTest {

    private class Fixture(scope: kotlinx.coroutines.CoroutineScope) {
        val battery = MutableStateFlow(BatteryStatus(percent = 80, isCharging = false))
        val headphones = MutableStateFlow(false)
        val wifi = MutableStateFlow(WifiState())
        val bluetooth = MutableStateFlow<Set<String>?>(null)

        val batteryRepository = mock(BatteryRepository::class.java).also { `when`(it.observeBatteryStatus()).thenReturn(battery) }
        val headphonesRepository = mock(HeadphonesRepository::class.java).also { `when`(it.observeHeadphonesPluggedIn()).thenReturn(headphones) }
        val wifiRepository = mock(WifiRepository::class.java).also {
            `when`(it.observeWifiState(true)).thenReturn(wifi)
            `when`(it.observeWifiState(false)).thenReturn(wifi)
        }
        val bluetoothRepository = mock(BluetoothRepository::class.java).also { `when`(it.observeConnectedAddresses()).thenReturn(bluetooth) }

        /** Every source in use by default, as if a rule read each one; tests narrow it. */
        val rules = MutableStateFlow(listOf(batteryRule, headphonesRule, wifiRule(ssid = "Home"), bluetoothRule))
        val ruleRepository = mock(AutomationRuleRepository::class.java).also { `when`(it.observeRules()).thenReturn(rules) }

        val repository = DeviceStateRepository(batteryRepository, headphonesRepository, wifiRepository, bluetoothRepository, ruleRepository, scope)
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
        verify(fixture.wifiRepository, times(1)).observeWifiState(true)
    }

    @Test
    fun `a permission change resubscribes wifi and bluetooth but not the others`() = runTest {
        val fixture = Fixture(backgroundScope)
        start(fixture)

        fixture.repository.onPermissionsChanged()
        runCurrent()

        verify(fixture.bluetoothRepository, times(2)).observeConnectedAddresses()
        verify(fixture.wifiRepository, times(2)).observeWifiState(true)
        verify(fixture.batteryRepository, times(1)).observeBatteryStatus()
        verify(fixture.headphonesRepository, atLeast(1)).observeHeadphonesPluggedIn()
    }

    @Test
    fun `with no rules no source is subscribed`() = runTest {
        val fixture = Fixture(backgroundScope)
        fixture.rules.value = emptyList()
        start(fixture)

        verify(fixture.batteryRepository, never()).observeBatteryStatus()
        verify(fixture.headphonesRepository, never()).observeHeadphonesPluggedIn()
        verify(fixture.bluetoothRepository, never()).observeConnectedAddresses()
        verify(fixture.wifiRepository, never()).observeWifiState(true)
        verify(fixture.wifiRepository, never()).observeWifiState(false)
        assertEquals(DeviceState(), fixture.repository.current())
    }

    @Test
    fun `a disabled rule does not subscribe its source`() = runTest {
        val fixture = Fixture(backgroundScope)
        fixture.rules.value = listOf(wifiRule(ssid = null, enabled = false))
        start(fixture)

        verify(fixture.wifiRepository, never()).observeWifiState(true)
        verify(fixture.wifiRepository, never()).observeWifiState(false)
    }

    @Test
    fun `an any network rule subscribes wifi without the ssid`() = runTest {
        val fixture = Fixture(backgroundScope)
        fixture.rules.value = listOf(wifiRule(ssid = null))
        start(fixture)

        verify(fixture.wifiRepository, times(1)).observeWifiState(false)
        verify(fixture.wifiRepository, never()).observeWifiState(true)
    }

    @Test
    fun `a named network rule subscribes wifi with the ssid`() = runTest {
        val fixture = Fixture(backgroundScope)
        fixture.rules.value = listOf(wifiRule(ssid = "Home"))
        start(fixture)

        verify(fixture.wifiRepository, times(1)).observeWifiState(true)
    }

    @Test
    fun `adding a rule starts only its source and leaves the others running`() = runTest {
        val fixture = Fixture(backgroundScope)
        fixture.rules.value = listOf(bluetoothRule)
        start(fixture)
        fixture.bluetooth.value = setOf("AA:BB")
        runCurrent()

        fixture.rules.value = listOf(bluetoothRule, headphonesRule)
        runCurrent()

        verify(fixture.bluetoothRepository, times(1)).observeConnectedAddresses()
        verify(fixture.headphonesRepository, times(1)).observeHeadphonesPluggedIn()
        assertEquals(setOf("AA:BB"), fixture.repository.current().connectedBluetoothAddresses)
    }
}
