package com.facetlauncher.app.domain

import com.facetlauncher.app.data.AutomationRuleRepository
import com.facetlauncher.app.data.model.AutomationLimits
import com.facetlauncher.app.data.model.AutomationRule
import com.facetlauncher.app.data.model.ProReason
import com.facetlauncher.app.data.model.SaveRuleResult
import javax.inject.Inject

/** Applies the Pro gate (trigger type, then the free rule limit) before [AutomationRuleRepository.save] validates and writes. */
class SaveAutomationRuleUseCase @Inject constructor(
    private val ruleRepository: AutomationRuleRepository,
    private val canUseTrigger: CanUseTriggerUseCase,
) {
    suspend operator fun invoke(rule: AutomationRule, isPro: Boolean): SaveRuleResult = when {
        !canUseTrigger(rule.trigger, isPro) -> SaveRuleResult.ProRequired(ProReason.TRIGGER)
        rule.id == 0L && !AutomationLimits.canAddRule(ruleRepository.getRules().size, isPro) -> SaveRuleResult.ProRequired(ProReason.RULE_LIMIT)
        else -> ruleRepository.save(rule)
    }
}
