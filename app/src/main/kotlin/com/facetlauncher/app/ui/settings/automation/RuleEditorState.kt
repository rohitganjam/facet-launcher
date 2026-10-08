package com.facetlauncher.app.ui.settings.automation

import com.facetlauncher.app.data.local.FacetEntity
import com.facetlauncher.app.data.model.AutomationPermission
import com.facetlauncher.app.data.model.AutomationRule
import com.facetlauncher.app.data.model.AutomationRuleError
import com.facetlauncher.app.data.model.AutomationTrigger
import com.facetlauncher.app.data.model.BatteryDirection
import com.facetlauncher.app.data.model.BatteryLevelCondition
import com.facetlauncher.app.data.model.ProReason
import com.facetlauncher.app.data.model.RuleEndBehavior
import java.time.DayOfWeek

enum class EndKind { RETURN, SWITCH, STAY }

enum class TriggerKind { SCHEDULE, BLUETOOTH, WIFI, HEADPHONES, BATTERY }

fun AutomationTrigger.kind(): TriggerKind = when (this) {
    is AutomationTrigger.Schedule -> TriggerKind.SCHEDULE
    is AutomationTrigger.Bluetooth -> TriggerKind.BLUETOOTH
    is AutomationTrigger.Wifi -> TriggerKind.WIFI
    is AutomationTrigger.Headphones -> TriggerKind.HEADPHONES
    is AutomationTrigger.Battery -> TriggerKind.BATTERY
}

private val DEFAULT_DAYS = setOf(DayOfWeek.MONDAY, DayOfWeek.TUESDAY, DayOfWeek.WEDNESDAY, DayOfWeek.THURSDAY, DayOfWeek.FRIDAY)
private const val DEFAULT_START_MINUTE = 9 * 60
private const val DEFAULT_END_MINUTE = 18 * 60
private const val DEFAULT_LOW_BATTERY_PERCENT = 20

private val DEFAULT_SCHEDULE = AutomationTrigger.Schedule(DEFAULT_DAYS, DEFAULT_START_MINUTE, DEFAULT_END_MINUTE)

/** A blank device or "named network" with no name is deliberately invalid until the user picks one. */
private fun TriggerKind.defaultTrigger(): AutomationTrigger = when (this) {
    TriggerKind.SCHEDULE -> DEFAULT_SCHEDULE
    TriggerKind.BLUETOOTH -> AutomationTrigger.Bluetooth(deviceAddress = "", deviceName = "")
    TriggerKind.WIFI -> AutomationTrigger.Wifi()
    TriggerKind.HEADPHONES -> AutomationTrigger.Headphones()
    TriggerKind.BATTERY -> AutomationTrigger.Battery(false, BatteryLevelCondition(BatteryDirection.BELOW, DEFAULT_LOW_BATTERY_PERCENT))
}

/** The rule being created or edited in the sheet. [ruleId] is 0 for a new rule. */
data class RuleEditorState(
    val ruleId: Long,
    val enabled: Boolean,
    val targetFacetId: Long,
    val trigger: AutomationTrigger,
    val endKind: EndKind,
    /** Only used when [endKind] is [EndKind.SWITCH]. */
    val endFacetId: Long,
    /** The facets the pickers offer, read when the editor opened. */
    val facets: List<FacetEntity>,
    val errors: List<AutomationRuleError> = emptyList(),
    val facetMissing: Boolean = false,
    /** The permission the user just refused for the trigger they tried to pick; the trigger itself stays as it was. */
    val permissionDenied: AutomationPermission? = null,
    /** Set when a free user tried something Pro (a locked trigger, or a save the free plan doesn't allow); the screen shows the upgrade sheet. */
    val proRequired: ProReason? = null,
) {
    val isNew: Boolean get() = ruleId == 0L

    fun toRule(): AutomationRule = AutomationRule(
        id = ruleId,
        targetFacetId = targetFacetId,
        trigger = trigger,
        endBehavior = when (endKind) {
            EndKind.RETURN -> RuleEndBehavior.ReturnToBaseline
            EndKind.SWITCH -> RuleEndBehavior.SwitchTo(endFacetId)
            EndKind.STAY -> RuleEndBehavior.Stay
        },
        enabled = enabled,
    )

    /** Edits clear the last save's complaints, so a message never outlives the field it was about. */
    fun edited(transform: RuleEditorState.() -> RuleEditorState): RuleEditorState =
        transform().copy(errors = emptyList(), facetMissing = false, permissionDenied = null, proRequired = null)

    private fun withSchedule(transform: (AutomationTrigger.Schedule) -> AutomationTrigger.Schedule): RuleEditorState = edited {
        (trigger as? AutomationTrigger.Schedule)?.let { copy(trigger = transform(it)) } ?: this
    }

    fun withTrigger(trigger: AutomationTrigger): RuleEditorState = edited { copy(trigger = trigger) }

    /** Switching type starts that type from its defaults; picking the current type again changes nothing. */
    fun withKind(kind: TriggerKind): RuleEditorState = if (trigger.kind() == kind) this else withTrigger(kind.defaultTrigger())

    /** "Named network" starts with no network chosen (a blank name), which is invalid until one is picked. */
    fun withWifiScope(named: Boolean): RuleEditorState {
        val wifi = trigger as? AutomationTrigger.Wifi ?: return this
        return withTrigger(wifi.copy(ssid = if (named) wifi.ssid ?: "" else null))
    }

    fun withScheduleDay(day: DayOfWeek, selected: Boolean) =
        withSchedule { it.copy(days = if (selected) it.days + day else it.days - day) }

    fun withStartMinute(minute: Int) = withSchedule { it.copy(startMinute = minute) }

    fun withEndMinute(minute: Int) = withSchedule { it.copy(endMinute = minute) }

    companion object {
        /** A weekday-hours schedule that targets the first facet other than the one showing. */
        fun forNew(facets: List<FacetEntity>, activeFacetId: Long): RuleEditorState {
            val target = facets.firstOrNull { it.id != activeFacetId } ?: facets.first()
            return RuleEditorState(
                ruleId = 0L,
                enabled = true,
                targetFacetId = target.id,
                trigger = DEFAULT_SCHEDULE,
                endKind = EndKind.RETURN,
                endFacetId = facets.firstOrNull { it.id != target.id }?.id ?: target.id,
                facets = facets,
            )
        }

        fun from(rule: AutomationRule, facets: List<FacetEntity>): RuleEditorState {
            val switchTo = rule.endBehavior as? RuleEndBehavior.SwitchTo
            return RuleEditorState(
                ruleId = rule.id,
                enabled = rule.enabled,
                targetFacetId = rule.targetFacetId,
                trigger = rule.trigger,
                endKind = when (rule.endBehavior) {
                    RuleEndBehavior.ReturnToBaseline -> EndKind.RETURN
                    is RuleEndBehavior.SwitchTo -> EndKind.SWITCH
                    RuleEndBehavior.Stay -> EndKind.STAY
                },
                endFacetId = switchTo?.facetId ?: facets.firstOrNull { it.id != rule.targetFacetId }?.id ?: rule.targetFacetId,
                facets = facets,
            )
        }
    }
}
