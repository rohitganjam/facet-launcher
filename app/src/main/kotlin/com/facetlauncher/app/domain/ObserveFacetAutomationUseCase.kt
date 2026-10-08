package com.facetlauncher.app.domain

import com.facetlauncher.app.data.AutomationPermissionRepository
import com.facetlauncher.app.data.AutomationRuleRepository
import com.facetlauncher.app.data.AutomationStateRepository
import com.facetlauncher.app.data.EntitlementRepository
import com.facetlauncher.app.data.FacetRepository
import com.facetlauncher.app.data.SettingsRepository
import com.facetlauncher.app.data.local.FacetEntity
import com.facetlauncher.app.data.model.AutomationLimits
import com.facetlauncher.app.data.model.AutomationPermission
import com.facetlauncher.app.data.model.AutomationRule
import com.facetlauncher.app.data.model.AutomationState
import com.facetlauncher.app.data.model.RuleEndBehavior
import com.facetlauncher.app.data.model.requiredPermission
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import javax.inject.Inject

/** Why a rule can't run right now. Shown on its row so the user can fix it. [NEEDS_PRO] wins over a missing permission. */
enum class RuleAvailability { AVAILABLE, NEEDS_PRO, NEEDS_BLUETOOTH, NEEDS_LOCATION }

/** One row of the automation screen: a rule plus the facet names it refers to. */
data class AutomationRuleItem(
    val rule: AutomationRule,
    val targetFacetName: String,
    /** Set only for a "switch to" ending. */
    val endFacetName: String?,
    val availability: RuleAvailability,
)

/** Why Home looks the way it does; null on the screen when no rule is involved. */
sealed interface AutomationStatus {
    /** A rule is showing [facetName]. */
    data class Driving(val facetName: String, val rule: AutomationRuleItem) : AutomationStatus

    /** The user chose [facetName] by hand, so [pausedRule], which is running, is paused until it ends. */
    data class ManualOverride(val facetName: String, val pausedRule: AutomationRuleItem) : AutomationStatus
}

data class FacetAutomationScreenState(
    val items: List<AutomationRuleItem> = emptyList(),
    val status: AutomationStatus? = null,
    val canAddRule: Boolean = true,
    /** False shows the free-plan strip and the Pro pills. */
    val isPro: Boolean = true,
)

/**
 * Everything the facet automation screen shows, from the rules, the evaluator's persisted state, the
 * facets, the active facet and the live permission grants. [refresh] re-reads the grants, which have no
 * change callback (the screen bumps it on resume).
 */
class ObserveFacetAutomationUseCase @Inject constructor(
    private val ruleRepository: AutomationRuleRepository,
    private val stateRepository: AutomationStateRepository,
    private val facetRepository: FacetRepository,
    private val settingsRepository: SettingsRepository,
    private val permissionRepository: AutomationPermissionRepository,
    private val entitlementRepository: EntitlementRepository,
    private val canUseTrigger: CanUseTriggerUseCase,
) {
    operator fun invoke(refresh: Flow<Int>): Flow<FacetAutomationScreenState> = combine(
        ruleRepository.observeRules(),
        stateRepository.state,
        facetRepository.observeFacets(),
        settingsRepository.settings,
        entitlementRepository.isPro,
        refresh,
    ) { rules, state, facets, settings, isPro, _ ->
        buildFacetAutomationScreenState(
            rules = rules,
            state = state,
            facets = facets,
            activeFacetId = settings.activeFacetId,
            isGranted = permissionRepository::isGranted,
            entitledRuleIds = canUseTrigger.entitledRuleIds(rules, isPro),
            isPro = isPro,
        )
    }
}

internal fun buildFacetAutomationScreenState(
    rules: List<AutomationRule>,
    state: AutomationState,
    facets: List<FacetEntity>,
    activeFacetId: Long,
    isGranted: (AutomationPermission) -> Boolean,
    entitledRuleIds: Set<Long>,
    isPro: Boolean,
): FacetAutomationScreenState {
    val facetNames = facets.associate { it.id to it.name }
    val items = rules.map { rule ->
        AutomationRuleItem(
            rule = rule,
            targetFacetName = facetNames[rule.targetFacetId].orEmpty(),
            endFacetName = (rule.endBehavior as? RuleEndBehavior.SwitchTo)?.let { facetNames[it.facetId] },
            availability = rule.availability(isGranted, entitled = rule.id in entitledRuleIds),
        )
    }
    return FacetAutomationScreenState(
        items = items,
        status = items.status(state, facetNames[activeFacetId].orEmpty(), activeFacetId),
        canAddRule = AutomationLimits.canAddRule(rules.size, isPro),
        isPro = isPro,
    )
}

private fun AutomationRule.availability(isGranted: (AutomationPermission) -> Boolean, entitled: Boolean): RuleAvailability {
    val needed = trigger.requiredPermission()
    return when {
        !entitled -> RuleAvailability.NEEDS_PRO
        needed == null || isGranted(needed) -> RuleAvailability.AVAILABLE
        needed == AutomationPermission.BLUETOOTH_CONNECT -> RuleAvailability.NEEDS_BLUETOOTH
        else -> RuleAvailability.NEEDS_LOCATION
    }
}

/** Mirrors the evaluator: the winner is the latest active rule the user hasn't overridden. */
private fun List<AutomationRuleItem>.status(state: AutomationState, activeFacetName: String, activeFacetId: Long): AutomationStatus? {
    val byId = associateBy { it.rule.id }
    val winner = state.activeRuleIds.lastOrNull { it !in state.suppressedRuleIds }?.let(byId::get)
    val paused = state.activeRuleIds.firstOrNull { it in state.suppressedRuleIds }?.let(byId::get)
    return when {
        winner != null && winner.rule.targetFacetId == activeFacetId -> AutomationStatus.Driving(activeFacetName, winner)
        paused != null -> AutomationStatus.ManualOverride(activeFacetName, paused)
        else -> null
    }
}
