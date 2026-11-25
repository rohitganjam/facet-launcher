package com.facetlauncher.app.domain

import com.facetlauncher.app.data.model.AutomationRule
import com.facetlauncher.app.data.model.AutomationTrigger
import com.facetlauncher.app.data.model.BackupAutomationRule
import com.facetlauncher.app.data.model.BatteryDirection
import com.facetlauncher.app.data.model.BatteryLevelCondition
import com.facetlauncher.app.data.model.RuleEndBehavior
import com.facetlauncher.app.data.model.locationTriggerOrNull
import com.facetlauncher.app.data.model.validationErrors
import java.time.DayOfWeek

private const val END_RETURN = "RETURN_TO_BASELINE"
private const val END_SWITCH_TO = "SWITCH_TO"
private const val END_STAY = "STAY"

private const val TRIGGER_SCHEDULE = "SCHEDULE"
private const val TRIGGER_BLUETOOTH = "BLUETOOTH"
private const val TRIGGER_WIFI = "WIFI"
private const val TRIGGER_HEADPHONES = "HEADPHONES"
private const val TRIGGER_BATTERY = "BATTERY"
private const val TRIGGER_LOCATION = "LOCATION"

/** Why a backed-up rule was left out of a restore. */
enum class SkippedRuleReason {
    /** Its target or "switch to" facet isn't in the backup. */
    FACET_MISSING,

    /** A trigger or ending this version doesn't know, from a newer app. */
    UNSUPPORTED,

    /** Missing or out-of-range fields; the rule editor would refuse it too. */
    INVALID,
}

sealed interface RestoredRule {
    /** [rule] has `id = 0`; its facets are this install's new facet ids. */
    data class Restored(val rule: AutomationRule) : RestoredRule

    /** [trigger] is null when it couldn't be read. */
    data class Skipped(val reason: SkippedRuleReason, val trigger: AutomationTrigger?) : RestoredRule
}

/** Null when a facet the rule points at has no UUID in [uuidByFacetId] (it isn't being exported). */
fun AutomationRule.toBackupRule(uuidByFacetId: Map<Long, String>): BackupAutomationRule? {
    val targetUuid = uuidByFacetId[targetFacetId] ?: return null
    val end = endBehavior
    val endUuid = if (end is RuleEndBehavior.SwitchTo) uuidByFacetId[end.facetId] ?: return null else null
    val base = BackupAutomationRule(
        enabled = enabled,
        targetFacetUuid = targetUuid,
        endBehavior = when (end) {
            RuleEndBehavior.ReturnToBaseline -> END_RETURN
            is RuleEndBehavior.SwitchTo -> END_SWITCH_TO
            RuleEndBehavior.Stay -> END_STAY
        },
        endFacetUuid = endUuid,
        triggerType = "",
    )
    return when (val t = trigger) {
        is AutomationTrigger.Schedule -> base.copy(
            triggerType = TRIGGER_SCHEDULE,
            scheduleDays = t.days.sorted().map { it.name },
            scheduleStartMinute = t.startMinute,
            scheduleEndMinute = t.endMinute,
        )
        is AutomationTrigger.Bluetooth -> base.copy(
            triggerType = TRIGGER_BLUETOOTH,
            negated = t.negated,
            bluetoothAddress = t.deviceAddress,
            bluetoothName = t.deviceName,
        )
        is AutomationTrigger.Wifi -> base.copy(triggerType = TRIGGER_WIFI, negated = t.negated, wifiSsid = t.ssid)
        is AutomationTrigger.Headphones -> base.copy(triggerType = TRIGGER_HEADPHONES, negated = t.negated)
        is AutomationTrigger.Location -> base.copy(
            triggerType = TRIGGER_LOCATION,
            negated = t.negated,
            locationName = t.name,
            locationLatitude = t.latitude,
            locationLongitude = t.longitude,
            locationRadiusM = t.radiusM,
        )
        is AutomationTrigger.Battery -> base.copy(
            triggerType = TRIGGER_BATTERY,
            batteryWhileCharging = t.whileCharging,
            batteryDirection = t.level.direction.name,
            batteryThreshold = t.level.thresholdPercent,
        )
    }
}

/** Resolves the rule's facets through [facetIdByUuid] (backup UUID to this install's new facet id). */
fun BackupAutomationRule.toRestoredRule(facetIdByUuid: Map<String, Long>): RestoredRule {
    if (triggerType !in KNOWN_TRIGGERS || endBehavior !in KNOWN_ENDINGS) return RestoredRule.Skipped(SkippedRuleReason.UNSUPPORTED, null)
    val trigger = toTrigger() ?: return RestoredRule.Skipped(SkippedRuleReason.INVALID, null)
    val targetId = facetIdByUuid[targetFacetUuid]
    val endId = endFacetUuid?.let(facetIdByUuid::get)
    if (targetId == null || (endBehavior == END_SWITCH_TO && endId == null)) {
        return RestoredRule.Skipped(SkippedRuleReason.FACET_MISSING, trigger)
    }
    val end = when (endBehavior) {
        END_SWITCH_TO -> RuleEndBehavior.SwitchTo(requireNotNull(endId))
        END_STAY -> RuleEndBehavior.Stay
        else -> RuleEndBehavior.ReturnToBaseline
    }
    val rule = AutomationRule(id = 0, targetFacetId = targetId, trigger = trigger, endBehavior = end, enabled = enabled)
    return if (rule.validationErrors().isEmpty()) RestoredRule.Restored(rule) else RestoredRule.Skipped(SkippedRuleReason.INVALID, trigger)
}

private val KNOWN_TRIGGERS = setOf(TRIGGER_SCHEDULE, TRIGGER_BLUETOOTH, TRIGGER_WIFI, TRIGGER_HEADPHONES, TRIGGER_BATTERY, TRIGGER_LOCATION)
private val KNOWN_ENDINGS = setOf(END_RETURN, END_SWITCH_TO, END_STAY)

/** Null when a required field is missing or unreadable. An unknown day name makes the whole schedule unreadable. */
private fun BackupAutomationRule.toTrigger(): AutomationTrigger? = when (triggerType) {
    TRIGGER_SCHEDULE -> scheduleDays.map { it.toEnumOrNull<DayOfWeek>() }
        .takeIf { days -> days.none { it == null } }
        ?.let { days -> AutomationTrigger.Schedule(days.filterNotNull().toSet(), scheduleStartMinute, scheduleEndMinute) }
    TRIGGER_BLUETOOTH -> bluetoothAddress?.let { AutomationTrigger.Bluetooth(it, bluetoothName ?: it, negated) }
    TRIGGER_WIFI -> AutomationTrigger.Wifi(wifiSsid, negated)
    TRIGGER_HEADPHONES -> AutomationTrigger.Headphones(negated)
    TRIGGER_LOCATION -> locationTriggerOrNull(locationName, locationLatitude, locationLongitude, locationRadiusM, negated)
    TRIGGER_BATTERY -> toBattery()
    else -> null
}

private fun BackupAutomationRule.toBattery(): AutomationTrigger? {
    val direction = batteryDirection?.toEnumOrNull<BatteryDirection>()
    val threshold = batteryThreshold
    return if (direction != null && threshold != null) {
        AutomationTrigger.Battery(batteryWhileCharging, BatteryLevelCondition(direction, threshold))
    } else {
        null
    }
}
