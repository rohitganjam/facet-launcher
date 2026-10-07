package com.facetlauncher.app.data.model

/** Why a rule can't be saved. The editor maps each to an inline message. */
enum class AutomationRuleError {
    /** A schedule with no days selected would never be active. */
    NO_SCHEDULE_DAYS,

    /** A schedule start or end outside 0..1439 minutes since midnight. */
    INVALID_SCHEDULE_MINUTE,

    /** "Named network" with no network chosen. A null name means any network and is fine. */
    BLANK_WIFI_NETWORK,

    /** A Bluetooth rule with no device chosen. The picker fills the name with the address for a device that has none. */
    NO_BLUETOOTH_DEVICE,

    /** A battery level condition whose threshold isn't one of [BatteryLevelCondition.stopsFor] its direction. */
    INVALID_BATTERY_THRESHOLD,
}

private const val MINUTES_PER_DAY = 24 * 60

/** Empty when the rule is fine to save. Note `start == end` is valid: it is a one-minute window. */
fun AutomationRule.validationErrors(): List<AutomationRuleError> = when (val t = trigger) {
    is AutomationTrigger.Schedule -> buildList {
        if (t.days.isEmpty()) add(AutomationRuleError.NO_SCHEDULE_DAYS)
        if (t.startMinute !in 0 until MINUTES_PER_DAY || t.endMinute !in 0 until MINUTES_PER_DAY) {
            add(AutomationRuleError.INVALID_SCHEDULE_MINUTE)
        }
    }
    is AutomationTrigger.Wifi -> listOfNotNull(AutomationRuleError.BLANK_WIFI_NETWORK.takeIf { t.ssid?.isBlank() == true })
    is AutomationTrigger.Bluetooth ->
        listOfNotNull(AutomationRuleError.NO_BLUETOOTH_DEVICE.takeIf { t.deviceAddress.isBlank() || t.deviceName.isBlank() })
    is AutomationTrigger.Battery -> listOfNotNull(
        AutomationRuleError.INVALID_BATTERY_THRESHOLD.takeIf { t.level.thresholdPercent !in BatteryLevelCondition.stopsFor(t.level.direction) },
    )
    is AutomationTrigger.Headphones -> emptyList()
}

sealed interface SaveRuleResult {
    data class Saved(val id: Long) : SaveRuleResult

    /** Nothing was written. */
    data class Invalid(val errors: List<AutomationRuleError>) : SaveRuleResult

    /** The rule's target facet, or its "switch to" facet, no longer exists. Nothing was written. */
    data object FacetMissing : SaveRuleResult
}

/** What free users get; Pro has no cap yet. */
object AutomationLimits {
    const val FREE_MAX_RULES = 2

    /** Counts every saved rule, enabled or not. Editing an existing rule never counts as adding one. */
    fun canAddRule(savedRuleCount: Int, isPro: Boolean): Boolean = isPro || savedRuleCount < FREE_MAX_RULES
}
