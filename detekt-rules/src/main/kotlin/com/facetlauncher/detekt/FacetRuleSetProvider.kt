package com.facetlauncher.detekt

import dev.detekt.api.Config
import dev.detekt.api.Rule
import dev.detekt.api.RuleName
import dev.detekt.api.RuleSet
import dev.detekt.api.RuleSetId
import dev.detekt.api.RuleSetProvider

class FacetRuleSetProvider : RuleSetProvider {
    override val ruleSetId: RuleSetId = RuleSetId("facet")

    override fun instance(): RuleSet {
        val rules: Map<RuleName, (Config) -> Rule> = mapOf(
            RuleName("ComposeHardcodedText") to { config: Config -> ComposeHardcodedTextRule(config) }
        )
        return RuleSet(ruleSetId, rules)
    }
}
