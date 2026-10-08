package com.facetlauncher.app.data.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.DayOfWeek
import java.time.LocalDateTime

class DeviceStateTest {

    private val now = LocalDateTime.of(2026, 10, 5, 9, 30)

    private fun met(trigger: AutomationTrigger, state: DeviceState) = trigger.isMetBy(state, now)

    @Test
    fun `a schedule is decided by the clock alone`() {
        val monday = AutomationTrigger.Schedule(setOf(DayOfWeek.MONDAY), 9 * 60, 18 * 60)

        assertTrue(met(monday, DeviceState()))
        assertFalse(met(monday.copy(days = setOf(DayOfWeek.TUESDAY)), DeviceState()))
    }

    // --- Bluetooth ---

    @Test
    fun `a bluetooth rule is met while its device is connected`() {
        val state = DeviceState(connectedBluetoothAddresses = setOf("AA:BB", "CC:DD"))

        assertTrue(met(AutomationTrigger.Bluetooth("AA:BB", "Car"), state))
        assertFalse(met(AutomationTrigger.Bluetooth("EE:FF", "Watch"), state))
    }

    @Test
    fun `a negated bluetooth rule is met while its device is not connected`() {
        val state = DeviceState(connectedBluetoothAddresses = setOf("AA:BB"))

        assertFalse(met(AutomationTrigger.Bluetooth("AA:BB", "Car", negated = true), state))
        assertTrue(met(AutomationTrigger.Bluetooth("EE:FF", "Watch", negated = true), state))
    }

    @Test
    fun `a bluetooth rule is never met before the first query answers, negated or not`() {
        val unknown = DeviceState(connectedBluetoothAddresses = null)

        assertFalse(met(AutomationTrigger.Bluetooth("AA:BB", "Car"), unknown))
        assertFalse(met(AutomationTrigger.Bluetooth("AA:BB", "Car", negated = true), unknown))
    }

    @Test
    fun `an empty connected set is a real answer, so a negated rule is met`() {
        assertTrue(met(AutomationTrigger.Bluetooth("AA:BB", "Car", negated = true), DeviceState(connectedBluetoothAddresses = emptySet())))
    }

    // --- Wi-Fi ---

    @Test
    fun `any network is met while connected to any wifi`() {
        assertTrue(met(AutomationTrigger.Wifi(), DeviceState(wifi = WifiState(connected = true, ssid = "Home"))))
        assertTrue(met(AutomationTrigger.Wifi(), DeviceState(wifi = WifiState(connected = true, ssid = null))))
        assertFalse(met(AutomationTrigger.Wifi(), DeviceState(wifi = WifiState(connected = false))))
    }

    @Test
    fun `negated any network is met while not on wifi`() {
        assertTrue(met(AutomationTrigger.Wifi(negated = true), DeviceState(wifi = WifiState(connected = false))))
        assertFalse(met(AutomationTrigger.Wifi(negated = true), DeviceState(wifi = WifiState(connected = true, ssid = "Home"))))
    }

    @Test
    fun `a named network is met only on that network`() {
        val rule = AutomationTrigger.Wifi(ssid = "Home")

        assertTrue(met(rule, DeviceState(wifi = WifiState(true, "Home"))))
        assertFalse(met(rule, DeviceState(wifi = WifiState(true, "Office"))))
        assertFalse(met(rule, DeviceState(wifi = WifiState(false))))
    }

    @Test
    fun `a negated named network is met everywhere except that network`() {
        val rule = AutomationTrigger.Wifi(ssid = "Home", negated = true)

        assertFalse(met(rule, DeviceState(wifi = WifiState(true, "Home"))))
        assertTrue(met(rule, DeviceState(wifi = WifiState(true, "Office"))))
        assertTrue(met(rule, DeviceState(wifi = WifiState(false))))
    }

    @Test
    fun `a named network is never met while connected to an unreadable name, negated or not`() {
        val unreadable = DeviceState(wifi = WifiState(connected = true, ssid = null))

        assertFalse(met(AutomationTrigger.Wifi(ssid = "Home"), unreadable))
        assertFalse(met(AutomationTrigger.Wifi(ssid = "Home", negated = true), unreadable))
    }

    @Test
    fun `network names are matched exactly`() {
        assertFalse(met(AutomationTrigger.Wifi(ssid = "Home"), DeviceState(wifi = WifiState(true, "home"))))
    }

    // --- Headphones ---

    @Test
    fun `headphones are met while plugged in and negated while not`() {
        assertTrue(met(AutomationTrigger.Headphones(), DeviceState(headphonesPluggedIn = true)))
        assertFalse(met(AutomationTrigger.Headphones(), DeviceState(headphonesPluggedIn = false)))
        assertTrue(met(AutomationTrigger.Headphones(negated = true), DeviceState(headphonesPluggedIn = false)))
        assertFalse(met(AutomationTrigger.Headphones(negated = true), DeviceState(headphonesPluggedIn = true)))
    }

    // --- Battery ---

    @Test
    fun `a battery rule follows the charging state and the level together`() {
        val notChargingBelow20 = AutomationTrigger.Battery(false, BatteryLevelCondition(BatteryDirection.BELOW, 20))

        assertTrue(met(notChargingBelow20, DeviceState(battery = BatteryStatus(percent = 15, isCharging = false))))
        assertFalse(met(notChargingBelow20, DeviceState(battery = BatteryStatus(percent = 15, isCharging = true))))
        assertFalse(met(notChargingBelow20, DeviceState(battery = BatteryStatus(percent = 25, isCharging = false))))
    }

    @Test
    fun `a battery rule is never met without a reading`() {
        assertFalse(met(AutomationTrigger.Battery(false, BatteryLevelCondition(BatteryDirection.BELOW, 100)), DeviceState(battery = null)))
    }

    // --- Permissions ---

    @Test
    fun `bluetooth needs the bluetooth permission`() {
        assertEquals(AutomationPermission.BLUETOOTH_CONNECT, AutomationTrigger.Bluetooth("AA", "Car").requiredPermission())
    }

    @Test
    fun `only a named wifi network needs location`() {
        assertEquals(AutomationPermission.LOCATION, AutomationTrigger.Wifi(ssid = "Home").requiredPermission())
        assertNull(AutomationTrigger.Wifi(ssid = null).requiredPermission())
    }

    @Test
    fun `schedule headphones and battery need no permission`() {
        assertNull(AutomationTrigger.Schedule(setOf(DayOfWeek.MONDAY), 0, 1).requiredPermission())
        assertNull(AutomationTrigger.Headphones().requiredPermission())
        assertNull(AutomationTrigger.Battery(true, BatteryLevelCondition(BatteryDirection.ABOVE, 80)).requiredPermission())
    }
}
