package com.facetlauncher.app.domain

import com.facetlauncher.app.data.model.AutomationLimits
import com.facetlauncher.app.data.model.AutomationRule
import com.facetlauncher.app.data.model.AutomationTrigger
import javax.inject.Inject

/**
 * The Pro gate for facet automation, in one place. Schedule rules are free (up to
 * [AutomationLimits.FREE_MAX_RULES]); every device trigger is Pro.
 */
class CanUseTriggerUseCase @Inject constructor() {

    operator fun invoke(trigger: AutomationTrigger, isPro: Boolean): Boolean = isPro || trigger is AutomationTrigger.Schedule

    /**
     * The rules that may run. Pro: all of them. Free: the first [AutomationLimits.FREE_MAX_RULES] schedule
     * rules in list order, so rules left over from a lapsed Pro are paused, never deleted.
     */
    fun entitledRuleIds(rules: List<AutomationRule>, isPro: Boolean): Set<Long> =
        if (isPro) {
            rules.map { it.id }.toSet()
        } else {
            rules.filter { invoke(it.trigger, isPro = false) }.take(AutomationLimits.FREE_MAX_RULES).map { it.id }.toSet()
        }
}
