package com.facetlauncher.app.data.model

/**
 * Persisted bookkeeping for facet automation.
 *
 * [activeRuleIds] is the last evaluated set of true rules, oldest activation first — it both
 * detects start/end transitions and decides which rule wins (the last one). [suppressedRuleIds]
 * are active rules the user overrode by hand; they stay ignored until they stop being true.
 */
data class AutomationState(
    val baselineFacetId: Long? = null,
    val activeRuleIds: List<Long> = emptyList(),
    val suppressedRuleIds: Set<Long> = emptySet(),
) {
    /** Any switch the user makes (in-app, shortcut or deep link) wins over every rule active right now. */
    fun afterManualSwitch(facetId: Long): AutomationState = copy(
        baselineFacetId = facetId,
        suppressedRuleIds = activeRuleIds.toSet(),
    )
}
