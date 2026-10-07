package com.facetlauncher.app.domain

import com.facetlauncher.app.data.model.AutomationRule
import com.facetlauncher.app.data.model.AutomationState
import com.facetlauncher.app.data.model.RuleEndBehavior
import javax.inject.Inject

/** What to show, plus the state to persist for the next evaluation. */
data class AutomationEvaluation(val desiredFacetId: Long, val state: AutomationState)

/**
 * Pure, level-based evaluation of facet automation rules (see `IMPLEMENTATION_PLAN.md`, "Facet
 * automation rules"). A rule is true when it is enabled and its id is in `conditionsMet`.
 *
 * - The most recently activated true, non-suppressed rule wins; with none, the baseline is shown.
 * - A rule that stops being true applies its end behavior — unless the user overrode it, in which
 *   case their choice stands.
 */
class EvaluateFacetAutomationUseCase @Inject constructor() {

    operator fun invoke(
        rules: List<AutomationRule>,
        conditionsMet: Set<Long>,
        state: AutomationState,
        currentFacetId: Long,
        existingFacetIds: Set<Long>,
    ): AutomationEvaluation {
        val rulesById = rules.associateBy { it.id }
        val trueNow = rules.filter { it.enabled && it.id in conditionsMet }.map { it.id }.toSet()

        val baseline = state.activeRuleIds
            .filter { it !in trueNow && it !in state.suppressedRuleIds }
            .fold(state.baselineFacetId ?: currentFacetId) { acc, endedId ->
                applyEndBehavior(rulesById[endedId]?.endBehavior, acc, currentFacetId, existingFacetIds)
            }
            .takeIf { it in existingFacetIds } ?: currentFacetId

        val stillActive = state.activeRuleIds.filter { it in trueNow }
        val started = rules.map { it.id }.filter { it in trueNow && it !in stillActive }
        val activeRuleIds = stillActive + started
        val suppressed = state.suppressedRuleIds.intersect(trueNow)

        val winner = activeRuleIds.lastOrNull { it !in suppressed }?.let { rulesById.getValue(it).targetFacetId }
        val desired = listOfNotNull(winner, baseline).firstOrNull { it in existingFacetIds } ?: currentFacetId

        return AutomationEvaluation(
            desiredFacetId = desired,
            state = AutomationState(baseline, activeRuleIds, suppressed),
        )
    }

    // A deleted rule has no end behavior and simply returns to the baseline.
    private fun applyEndBehavior(
        end: RuleEndBehavior?,
        baseline: Long,
        currentFacetId: Long,
        existingFacetIds: Set<Long>,
    ): Long = when (end) {
        is RuleEndBehavior.SwitchTo -> if (end.facetId in existingFacetIds) end.facetId else baseline
        RuleEndBehavior.Stay -> currentFacetId
        RuleEndBehavior.ReturnToBaseline, null -> baseline
    }
}
