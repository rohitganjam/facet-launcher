package com.facetlauncher.app.data.model

/** What a rule does with the active facet when its condition stops being true. */
sealed interface RuleEndBehavior {
    /** Go back to the facet the user last chose by hand (the baseline). */
    data object ReturnToBaseline : RuleEndBehavior

    data class SwitchTo(val facetId: Long) : RuleEndBehavior

    /** Keep whatever facet is showing and make it the new baseline. */
    data object Stay : RuleEndBehavior
}

/** A new rule has `id = 0` until it is saved. */
data class AutomationRule(
    val id: Long,
    val targetFacetId: Long,
    val trigger: AutomationTrigger,
    val endBehavior: RuleEndBehavior = RuleEndBehavior.ReturnToBaseline,
    val enabled: Boolean = true,
)
