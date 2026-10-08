package com.facetlauncher.app.data.model

import org.junit.Assert.assertEquals
import org.junit.Test

class AutomationStateTest {

    @Test
    fun `afterManualSwitch sets the baseline and suppresses every active rule`() {
        val state = AutomationState(baselineFacetId = 1L, activeRuleIds = listOf(10L, 11L))

        val result = state.afterManualSwitch(3L)

        assertEquals(3L, result.baselineFacetId)
        assertEquals(setOf(10L, 11L), result.suppressedRuleIds)
        assertEquals(listOf(10L, 11L), result.activeRuleIds)
    }

    @Test
    fun `afterManualSwitch with no active rules suppresses nothing`() {
        val result = AutomationState(baselineFacetId = 1L).afterManualSwitch(2L)

        assertEquals(2L, result.baselineFacetId)
        assertEquals(emptySet<Long>(), result.suppressedRuleIds)
    }
}
