package com.facetlauncher.app.data.model

import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.DayOfWeek

class AutomationRuleValidationTest {

    private fun rule(trigger: AutomationTrigger) = AutomationRule(id = 0, targetFacetId = 1L, trigger = trigger)

    private fun schedule(startMinute: Int, endMinute: Int, days: Set<DayOfWeek> = setOf(DayOfWeek.MONDAY)) =
        rule(AutomationTrigger.Schedule(days, startMinute, endMinute))

    @Test
    fun `a normal schedule has no errors`() {
        assertEquals(emptyList<AutomationRuleError>(), schedule(540, 1080).validationErrors())
    }

    @Test
    fun `a schedule with no days cannot be saved`() {
        assertEquals(listOf(AutomationRuleError.NO_SCHEDULE_DAYS), schedule(540, 1080, days = emptySet()).validationErrors())
    }

    @Test
    fun `a start equal to the end is valid`() {
        assertEquals(emptyList<AutomationRuleError>(), schedule(540, 540).validationErrors())
    }

    @Test
    fun `an overnight window is valid`() {
        assertEquals(emptyList<AutomationRuleError>(), schedule(1320, 360).validationErrors())
    }

    @Test
    fun `the first and last minute of the day are valid`() {
        assertEquals(emptyList<AutomationRuleError>(), schedule(0, 1439).validationErrors())
    }

    @Test
    fun `minutes outside the day are rejected`() {
        assertEquals(listOf(AutomationRuleError.INVALID_SCHEDULE_MINUTE), schedule(-1, 1080).validationErrors())
        assertEquals(listOf(AutomationRuleError.INVALID_SCHEDULE_MINUTE), schedule(540, 1440).validationErrors())
    }

    @Test
    fun `every problem is reported together`() {
        val errors = schedule(-1, 1440, days = emptySet()).validationErrors()

        assertEquals(listOf(AutomationRuleError.NO_SCHEDULE_DAYS, AutomationRuleError.INVALID_SCHEDULE_MINUTE), errors)
    }

    @Test
    fun `any network is valid but a named network needs a name`() {
        assertEquals(emptyList<AutomationRuleError>(), rule(AutomationTrigger.Wifi(ssid = null)).validationErrors())
        assertEquals(emptyList<AutomationRuleError>(), rule(AutomationTrigger.Wifi(ssid = "HomeNet-5G")).validationErrors())
        assertEquals(listOf(AutomationRuleError.BLANK_WIFI_NETWORK), rule(AutomationTrigger.Wifi(ssid = "")).validationErrors())
        assertEquals(listOf(AutomationRuleError.BLANK_WIFI_NETWORK), rule(AutomationTrigger.Wifi(ssid = "  ")).validationErrors())
    }

    @Test
    fun `a bluetooth rule needs a selected device with an address and a name`() {
        assertEquals(emptyList<AutomationRuleError>(), rule(AutomationTrigger.Bluetooth("AA:BB:CC:DD:EE:FF", "Car")).validationErrors())
        assertEquals(listOf(AutomationRuleError.NO_BLUETOOTH_DEVICE), rule(AutomationTrigger.Bluetooth("", "")).validationErrors())
        assertEquals(listOf(AutomationRuleError.NO_BLUETOOTH_DEVICE), rule(AutomationTrigger.Bluetooth("AA:BB:CC:DD:EE:FF", " ")).validationErrors())
        assertEquals(listOf(AutomationRuleError.NO_BLUETOOTH_DEVICE), rule(AutomationTrigger.Bluetooth("", "Car")).validationErrors())
    }

    private fun battery(direction: BatteryDirection, percent: Int, whileCharging: Boolean = false) =
        rule(AutomationTrigger.Battery(whileCharging, BatteryLevelCondition(direction, percent)))

    @Test
    fun `a below threshold is one of the 5 percent stops from 5 to 100`() {
        listOf(5, 20, 55, 100).forEach {
            assertEquals("$it", emptyList<AutomationRuleError>(), battery(BatteryDirection.BELOW, it).validationErrors())
        }
        listOf(0, 1, 4, 22, 101, 105, -5).forEach {
            assertEquals("$it", listOf(AutomationRuleError.INVALID_BATTERY_THRESHOLD), battery(BatteryDirection.BELOW, it).validationErrors())
        }
    }

    @Test
    fun `an above threshold stops at 95 because nothing is above 100 percent`() {
        listOf(5, 50, 95).forEach {
            assertEquals("$it", emptyList<AutomationRuleError>(), battery(BatteryDirection.ABOVE, it).validationErrors())
        }
        listOf(100, 0, 22, 105).forEach {
            assertEquals("$it", listOf(AutomationRuleError.INVALID_BATTERY_THRESHOLD), battery(BatteryDirection.ABOVE, it).validationErrors())
        }
    }

    @Test
    fun `the same stops apply while charging`() {
        assertEquals(emptyList<AutomationRuleError>(), battery(BatteryDirection.ABOVE, 80, whileCharging = true).validationErrors())
        assertEquals(
            listOf(AutomationRuleError.INVALID_BATTERY_THRESHOLD),
            battery(BatteryDirection.ABOVE, 100, whileCharging = true).validationErrors(),
        )
    }

    @Test
    fun `the slider offers twenty stops below and nineteen above`() {
        assertEquals((5..100 step 5).toList(), BatteryLevelCondition.stopsFor(BatteryDirection.BELOW).toList())
        assertEquals(20, BatteryLevelCondition.stopsFor(BatteryDirection.BELOW).count())
        assertEquals((5..95 step 5).toList(), BatteryLevelCondition.stopsFor(BatteryDirection.ABOVE).toList())
        assertEquals(19, BatteryLevelCondition.stopsFor(BatteryDirection.ABOVE).count())
    }

    @Test
    fun `headphones have nothing to validate`() {
        assertEquals(emptyList<AutomationRuleError>(), rule(AutomationTrigger.Headphones()).validationErrors())
    }
}
