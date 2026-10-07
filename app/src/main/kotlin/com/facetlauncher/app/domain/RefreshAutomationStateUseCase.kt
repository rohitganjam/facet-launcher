package com.facetlauncher.app.domain

import com.facetlauncher.app.data.AutomationRuleRepository
import com.facetlauncher.app.data.AutomationStateRepository
import com.facetlauncher.app.data.FacetRepository
import com.facetlauncher.app.data.SettingsRepository
import com.facetlauncher.app.data.model.AutomationRule
import com.facetlauncher.app.data.model.AutomationTrigger
import kotlinx.coroutines.flow.first
import java.time.Clock
import java.time.LocalDateTime
import javax.inject.Inject

/** What the rules want shown right now, next to what is showing. */
data class AutomationDecision(val desiredFacetId: Long, val currentFacetId: Long)

/**
 * Samples every rule against the present moment and persists the evaluator's new state, **without
 * switching facets**. [ApplyFacetAutomationUseCase] calls it before switching, and
 * [ActivateFacetByIdUseCase] calls it before recording a manual switch so that a rule which became
 * true since the last evaluation is suppressed too — otherwise the user's choice would be overridden
 * the next time the rules are evaluated.
 */
class RefreshAutomationStateUseCase @Inject constructor(
    private val ruleRepository: AutomationRuleRepository,
    private val stateRepository: AutomationStateRepository,
    private val facetRepository: FacetRepository,
    private val settingsRepository: SettingsRepository,
    private val evaluate: EvaluateFacetAutomationUseCase,
    private val clock: Clock,
) {
    /** Null while there is no valid active facet yet (a fresh install before `EnsureActiveFacetUseCase` has run). */
    suspend operator fun invoke(): AutomationDecision? {
        val currentFacetId = settingsRepository.settings.first().activeFacetId
        val existingFacetIds = facetRepository.observeFacets().first().map { it.id }.toSet()
        if (currentFacetId !in existingFacetIds) return null

        val rules = ruleRepository.getRules()
        val now = LocalDateTime.now(clock)
        val conditionsMet = rules.filter { it.isConditionMet(now) }.map { it.id }.toSet()
        var desiredFacetId = currentFacetId
        stateRepository.update { state ->
            val result = evaluate(rules, conditionsMet, state, currentFacetId, existingFacetIds)
            desiredFacetId = result.desiredFacetId
            result.state
        }
        return AutomationDecision(desiredFacetId, currentFacetId)
    }
}

private fun AutomationRule.isConditionMet(now: LocalDateTime): Boolean = when (val t = trigger) {
    is AutomationTrigger.Schedule -> t.isActiveAt(now)
    // The device triggers arrive with their sources in phase 5; until then they are never met.
    else -> false
}
