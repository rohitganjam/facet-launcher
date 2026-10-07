package com.facetlauncher.app.data.model

/** What a rule does with the active facet when its condition stops being true. */
sealed interface RuleEndBehavior {
    /** Go back to the facet the user last chose by hand (the baseline). */
    data object ReturnToBaseline : RuleEndBehavior

    data class SwitchTo(val facetId: Long) : RuleEndBehavior

    /** Keep whatever facet is showing and make it the new baseline. */
    data object Stay : RuleEndBehavior
}

/** A rule's trigger is evaluated elsewhere; the evaluator only needs to know whether it holds. */
data class AutomationRule(
    val id: Long,
    val targetFacetId: Long,
    val endBehavior: RuleEndBehavior = RuleEndBehavior.ReturnToBaseline,
    val enabled: Boolean = true,
)
