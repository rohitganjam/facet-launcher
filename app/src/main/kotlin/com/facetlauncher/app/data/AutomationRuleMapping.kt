package com.facetlauncher.app.data

import com.facetlauncher.app.data.local.AutomationRuleEntity
import com.facetlauncher.app.data.model.AutomationRule
import com.facetlauncher.app.data.model.AutomationTrigger
import com.facetlauncher.app.data.model.BatteryDirection
import com.facetlauncher.app.data.model.BatteryLevelCondition
import com.facetlauncher.app.data.model.RuleEndBehavior
import java.time.DayOfWeek

private const val END_RETURN = "RETURN_TO_BASELINE"
private const val END_SWITCH_TO = "SWITCH_TO"
private const val END_STAY = "STAY"

private const val TRIGGER_SCHEDULE = "SCHEDULE"
private const val TRIGGER_BLUETOOTH = "BLUETOOTH"
private const val TRIGGER_WIFI = "WIFI"
private const val TRIGGER_HEADPHONES = "HEADPHONES"
private const val TRIGGER_BATTERY = "BATTERY"

internal fun AutomationRule.toEntity(position: Int): AutomationRuleEntity {
    val endFacetId = (endBehavior as? RuleEndBehavior.SwitchTo)?.facetId
    val base = AutomationRuleEntity(
        id = id,
        position = position,
        enabled = enabled,
        targetFacetId = targetFacetId,
        endBehavior = when (endBehavior) {
            RuleEndBehavior.ReturnToBaseline -> END_RETURN
            is RuleEndBehavior.SwitchTo -> END_SWITCH_TO
            RuleEndBehavior.Stay -> END_STAY
        },
        endFacetId = endFacetId,
        triggerType = "",
    )
    return when (val t = trigger) {
        is AutomationTrigger.Schedule -> base.copy(
            triggerType = TRIGGER_SCHEDULE,
            scheduleDays = t.days.fold(0) { mask, day -> mask or (1 shl (day.value - 1)) },
            scheduleStartMinute = t.startMinute,
            scheduleEndMinute = t.endMinute,
        )
        is AutomationTrigger.Bluetooth -> base.copy(
            triggerType = TRIGGER_BLUETOOTH,
            negated = t.negated,
            deviceAddress = t.deviceAddress,
            deviceName = t.deviceName,
        )
        is AutomationTrigger.Wifi -> base.copy(triggerType = TRIGGER_WIFI, negated = t.negated, wifiSsid = t.ssid)
        is AutomationTrigger.Headphones -> base.copy(triggerType = TRIGGER_HEADPHONES, negated = t.negated)
        is AutomationTrigger.Battery -> base.copy(
            triggerType = TRIGGER_BATTERY,
            negated = !t.whileCharging,
            batteryThreshold = t.level.thresholdPercent,
            batteryDirection = t.level.direction.name,
        )
    }
}

/** Null for a row this build can't interpret (unknown trigger type, or a missing required parameter). */
internal fun AutomationRuleEntity.toRule(): AutomationRule? {
    val trigger = toTrigger() ?: return null
    val end = when (endBehavior) {
        // A deleted "switch to" facet nulls endFacetId, which degrades to the default ending.
        END_SWITCH_TO -> endFacetId?.let(RuleEndBehavior::SwitchTo) ?: RuleEndBehavior.ReturnToBaseline
        END_STAY -> RuleEndBehavior.Stay
        else -> RuleEndBehavior.ReturnToBaseline
    }
    return AutomationRule(id = id, targetFacetId = targetFacetId, trigger = trigger, endBehavior = end, enabled = enabled)
}

/** Both the threshold and a known direction are required; a row missing either can't be interpreted. */
private fun AutomationRuleEntity.toBattery(): AutomationTrigger? {
    val direction = batteryDirection?.let { runCatching { BatteryDirection.valueOf(it) }.getOrNull() }
    return if (batteryThreshold != null && direction != null) {
        AutomationTrigger.Battery(whileCharging = !negated, level = BatteryLevelCondition(direction, batteryThreshold))
    } else {
        null
    }
}

private fun AutomationRuleEntity.toTrigger(): AutomationTrigger? = when (triggerType) {
    TRIGGER_SCHEDULE -> AutomationTrigger.Schedule(
        days = DayOfWeek.entries.filter { scheduleDays and (1 shl (it.value - 1)) != 0 }.toSet(),
        startMinute = scheduleStartMinute,
        endMinute = scheduleEndMinute,
    )
    TRIGGER_BLUETOOTH -> deviceAddress?.let { AutomationTrigger.Bluetooth(it, deviceName.orEmpty(), negated) }
    TRIGGER_WIFI -> AutomationTrigger.Wifi(wifiSsid, negated)
    TRIGGER_HEADPHONES -> AutomationTrigger.Headphones(negated)
    TRIGGER_BATTERY -> toBattery()
    else -> null
}
