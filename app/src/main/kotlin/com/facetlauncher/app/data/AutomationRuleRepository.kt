package com.facetlauncher.app.data

import com.facetlauncher.app.data.local.AutomationRuleDao
import com.facetlauncher.app.data.local.FacetDao
import com.facetlauncher.app.data.model.AutomationRule
import com.facetlauncher.app.data.model.RuleEndBehavior
import com.facetlauncher.app.data.model.SaveRuleResult
import com.facetlauncher.app.data.model.validationErrors
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

/** Wraps [AutomationRuleDao]; rules come back in creation order, which is also the evaluator's tie-break order. */
@Singleton
class AutomationRuleRepository @Inject constructor(
    private val dao: AutomationRuleDao,
    private val facetDao: FacetDao,
) {

    /** Rows this build can't interpret are skipped, not surfaced. */
    fun observeRules(): Flow<List<AutomationRule>> = dao.observeAll().map { rows -> rows.mapNotNull { it.toRule() } }

    suspend fun getRules(): List<AutomationRule> = observeRules().first()

    suspend fun getById(id: Long): AutomationRule? = dao.getById(id)?.toRule()

    /**
     * Updates the rule in place when [rule] already exists, otherwise inserts it at the end of the
     * list. An invalid rule, or one pointing at a facet that has since been deleted, is rejected
     * and nothing is written.
     */
    suspend fun save(rule: AutomationRule): SaveRuleResult {
        val rejection = rule.rejection()
        if (rejection != null) return rejection
        val existing = if (rule.id != 0L) dao.getById(rule.id) else null
        val id = if (existing != null) {
            dao.update(rule.toEntity(existing.position))
            rule.id
        } else {
            dao.insert(rule.copy(id = 0).toEntity(dao.maxPosition() + 1))
        }
        return SaveRuleResult.Saved(id)
    }

    private suspend fun AutomationRule.rejection(): SaveRuleResult? {
        val errors = validationErrors()
        return when {
            errors.isNotEmpty() -> SaveRuleResult.Invalid(errors)
            !facetsExist() -> SaveRuleResult.FacetMissing
            else -> null
        }
    }

    private suspend fun AutomationRule.facetsExist(): Boolean {
        val switchTo = (endBehavior as? RuleEndBehavior.SwitchTo)?.facetId
        return facetDao.getById(targetFacetId) != null && (switchTo == null || facetDao.getById(switchTo) != null)
    }

    suspend fun setEnabled(ruleId: Long, enabled: Boolean) = dao.setEnabled(ruleId, enabled)

    suspend fun delete(ruleId: Long) = dao.deleteById(ruleId)
}
