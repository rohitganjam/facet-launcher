package com.facetlauncher.app.domain

import com.facetlauncher.app.data.AutomationPermissionRepository
import com.facetlauncher.app.data.AutomationRuleRepository
import com.facetlauncher.app.data.AutomationStateRepository
import com.facetlauncher.app.data.DeviceStateRepository
import com.facetlauncher.app.data.EntitlementRepository
import com.facetlauncher.app.data.FacetRepository
import com.facetlauncher.app.data.SettingsRepository
import com.facetlauncher.app.data.model.isMetBy
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
    private val deviceStateRepository: DeviceStateRepository,
    private val permissionRepository: AutomationPermissionRepository,
    private val entitlementRepository: EntitlementRepository,
    private val canUseTrigger: CanUseTriggerUseCase,
    private val clock: Clock,
) {
    /** Null while there is no valid active facet yet (a fresh install before `EnsureActiveFacetUseCase` has run). */
    suspend operator fun invoke(): AutomationDecision? {
        val currentFacetId = settingsRepository.settings.first().activeFacetId
        val existingFacetIds = facetRepository.observeFacets().first().map { it.id }.toSet()
        if (currentFacetId !in existingFacetIds) return null

        val rules = ruleRepository.getRules()
        val now = LocalDateTime.now(clock)
        val deviceState = deviceStateRepository.current()
        // A rule is met only when it is usable (entitled, permission granted) and its condition holds now.
        val entitled = canUseTrigger.entitledRuleIds(rules, entitlementRepository.isPro.value)
        val conditionsMet = rules
            .filter { it.id in entitled && permissionRepository.isUsable(it.trigger) && it.trigger.isMetBy(deviceState, now) }
            .map { it.id }
            .toSet()
        var desiredFacetId = currentFacetId
        stateRepository.update { state ->
            val result = evaluate(rules, conditionsMet, state, currentFacetId, existingFacetIds)
            desiredFacetId = result.desiredFacetId
            result.state
        }
        return AutomationDecision(desiredFacetId, currentFacetId)
    }
}
