package com.facetlauncher.app.domain

import com.facetlauncher.app.data.model.AutomationRule
import com.facetlauncher.app.data.model.AutomationTrigger
import com.facetlauncher.app.data.model.BackupAutomationRule
import com.facetlauncher.app.data.model.BatteryDirection
import com.facetlauncher.app.data.model.BatteryLevelCondition
import com.facetlauncher.app.data.model.RuleEndBehavior
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import java.time.DayOfWeek

class AutomationRuleBackupMappingTest {

    private val uuidByFacetId = mapOf(1L to "work-uuid", 2L to "home-uuid")

    /** The restoring install gave the same facets different ids. */
    private val newIdByUuid = mapOf("work-uuid" to 41L, "home-uuid" to 42L)

    private fun backupRule(triggerType: String = "HEADPHONES", target: String = "work-uuid", end: String = "RETURN_TO_BASELINE", endUuid: String? = null) =
        BackupAutomationRule(targetFacetUuid = target, endBehavior = end, endFacetUuid = endUuid, triggerType = triggerType)

    @Test
    fun `every trigger and ending survives a backup round trip with the facets mapped to their new ids`() {
        // Given one rule per trigger type and per ending, some negated or turned off
        val triggers = listOf(
            AutomationTrigger.Schedule(setOf(DayOfWeek.MONDAY, DayOfWeek.SUNDAY), 22 * 60, 6 * 60),
            AutomationTrigger.Bluetooth("AA:BB:CC:DD:EE:FF", "Car", negated = true),
            AutomationTrigger.Wifi(ssid = null),
            AutomationTrigger.Wifi(ssid = "Office", negated = true),
            AutomationTrigger.Headphones(negated = true),
            AutomationTrigger.Location("Office", 12.9716, 77.5946, 800),
            AutomationTrigger.Location("Home", -33.8688, 151.2093, 300, negated = true),
            AutomationTrigger.Battery(whileCharging = true, level = BatteryLevelCondition(BatteryDirection.ABOVE, 80)),
            AutomationTrigger.Battery(whileCharging = false, level = BatteryLevelCondition(BatteryDirection.BELOW, 20)),
        )
        val endings = listOf(RuleEndBehavior.ReturnToBaseline, RuleEndBehavior.SwitchTo(2), RuleEndBehavior.Stay)
        val rules = triggers.flatMap { trigger ->
            endings.map { end -> AutomationRule(id = 9, targetFacetId = 1, trigger = trigger, endBehavior = end, enabled = end != RuleEndBehavior.Stay) }
        }

        // When each goes out and comes back
        val restored = rules.map { requireNotNull(it.toBackupRule(uuidByFacetId)).toRestoredRule(newIdByUuid) }

        // Then each is the same rule, as a new rule, on the new facet ids
        val expected = rules.map { rule ->
            val end = (rule.endBehavior as? RuleEndBehavior.SwitchTo)?.let { RuleEndBehavior.SwitchTo(42) } ?: rule.endBehavior
            RestoredRule.Restored(rule.copy(id = 0, targetFacetId = 41, endBehavior = end))
        }
        assertEquals(expected, restored)
    }

    @Test
    fun `a rule whose facet is not being exported is left out`() {
        val rule = AutomationRule(id = 1, targetFacetId = 3, trigger = AutomationTrigger.Headphones())
        val switchingToMissing = AutomationRule(id = 2, targetFacetId = 1, trigger = AutomationTrigger.Headphones(), endBehavior = RuleEndBehavior.SwitchTo(3))

        assertNull(rule.toBackupRule(uuidByFacetId))
        assertNull(switchingToMissing.toBackupRule(uuidByFacetId))
    }

    @Test
    fun `a rule whose target facet is not in the backup is skipped and keeps its trigger for the summary`() {
        val restored = backupRule(target = "gone-uuid").toRestoredRule(newIdByUuid)

        assertEquals(RestoredRule.Skipped(SkippedRuleReason.FACET_MISSING, AutomationTrigger.Headphones()), restored)
    }

    @Test
    fun `a rule whose switch-to facet is not in the backup is skipped`() {
        val missing = backupRule(end = "SWITCH_TO", endUuid = "gone-uuid").toRestoredRule(newIdByUuid)
        val noFacetAtAll = backupRule(end = "SWITCH_TO", endUuid = null).toRestoredRule(newIdByUuid)

        assertEquals(SkippedRuleReason.FACET_MISSING, (missing as RestoredRule.Skipped).reason)
        assertEquals(SkippedRuleReason.FACET_MISSING, (noFacetAtAll as RestoredRule.Skipped).reason)
    }

    @Test
    fun `a trigger or ending from a newer app is skipped as unsupported`() {
        assertEquals(RestoredRule.Skipped(SkippedRuleReason.UNSUPPORTED, null), backupRule(triggerType = "GEOFENCE").toRestoredRule(newIdByUuid))
        assertEquals(RestoredRule.Skipped(SkippedRuleReason.UNSUPPORTED, null), backupRule(end = "SWITCH_BACK_LATER").toRestoredRule(newIdByUuid))
    }

    @Test
    fun `a rule the editor would refuse or that is missing fields is skipped as invalid`() {
        // Given a schedule with no days, one with an unknown day, a Bluetooth rule with no device, and a battery rule with no direction
        val noDays = backupRule(triggerType = "SCHEDULE").copy(scheduleDays = emptyList(), scheduleEndMinute = 60)
        val unknownDay = backupRule(triggerType = "SCHEDULE").copy(scheduleDays = listOf("MONDAY", "FUNDAY"))
        val noDevice = backupRule(triggerType = "BLUETOOTH")
        val noDirection = backupRule(triggerType = "BATTERY").copy(batteryThreshold = 20)
        val offStepThreshold = backupRule(triggerType = "BATTERY").copy(batteryDirection = "BELOW", batteryThreshold = 23)

        // When restored
        val reasons = listOf(noDays, unknownDay, noDevice, noDirection, offStepThreshold)
            .map { (it.toRestoredRule(newIdByUuid) as RestoredRule.Skipped).reason }

        // Then every one is skipped as invalid
        assertEquals(List(5) { SkippedRuleReason.INVALID }, reasons)
    }

    @Test
    fun `a Bluetooth rule saved without a device name shows its address instead`() {
        val restored = backupRule(triggerType = "BLUETOOTH").copy(bluetoothAddress = "AA:BB").toRestoredRule(newIdByUuid)

        assertEquals(AutomationTrigger.Bluetooth("AA:BB", "AA:BB"), (restored as RestoredRule.Restored).rule.trigger)
    }
}
