package com.facetlauncher.app.domain

import com.facetlauncher.app.data.AutomationPermissionRepository
import com.facetlauncher.app.data.AutomationRuleRepository
import com.facetlauncher.app.data.AutomationStateRepository
import com.facetlauncher.app.data.EntitlementRepository
import com.facetlauncher.app.data.model.AutomationState
import com.facetlauncher.app.data.model.BackupAutomationRule
import javax.inject.Inject

/** What a restore did with the backup's automation rules. */
data class AutomationRestoreSummary(
    /** False for a backup from before rules were backed up: it has none to restore. */
    val inBackup: Boolean,
    val restoredCount: Int,
    val skipped: List<RestoredRule.Skipped>,
    /** Restored, but the free plan doesn't run them; they wait, never deleted. */
    val pausedCount: Int,
    /** Restored and allowed to run, but waiting on Bluetooth or location permission. */
    val needsPermissionCount: Int,
)

/**
 * Backup restore's automation step, run after the facets are back. Replaces every rule with the backup's,
 * skipping any whose facet isn't there, then resets the automation bookkeeping (it holds old rule ids) and
 * records [activeFacetId] as if picked by hand, so the user lands on the facet they backed up and a rule
 * that is already true waits for its next start.
 */
class RestoreAutomationRulesUseCase @Inject constructor(
    private val ruleRepository: AutomationRuleRepository,
    private val stateRepository: AutomationStateRepository,
    private val activateFacetById: ActivateFacetByIdUseCase,
    private val canUseTrigger: CanUseTriggerUseCase,
    private val entitlementRepository: EntitlementRepository,
    private val permissionRepository: AutomationPermissionRepository,
) {
    suspend operator fun invoke(
        backupRules: List<BackupAutomationRule>?,
        facetIdByUuid: Map<String, Long>,
        activeFacetId: Long?,
    ): AutomationRestoreSummary {
        val results = backupRules.orEmpty().map { it.toRestoredRule(facetIdByUuid) }
        ruleRepository.replaceAll(results.filterIsInstance<RestoredRule.Restored>().map { it.rule })
        stateRepository.update { AutomationState() }
        activeFacetId?.let { activateFacetById(it) }

        val rules = ruleRepository.getRules()
        val entitled = canUseTrigger.entitledRuleIds(rules, entitlementRepository.isPro.value)
        return AutomationRestoreSummary(
            inBackup = backupRules != null,
            restoredCount = rules.size,
            skipped = results.filterIsInstance<RestoredRule.Skipped>(),
            pausedCount = rules.count { it.id !in entitled },
            needsPermissionCount = rules.count { it.id in entitled && !permissionRepository.isUsable(it.trigger) },
        )
    }
}
