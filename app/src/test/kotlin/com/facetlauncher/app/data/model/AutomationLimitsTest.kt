package com.facetlauncher.app.data.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class AutomationLimitsTest {

    @Test
    fun `free users get two rules`() {
        assertEquals(2, AutomationLimits.FREE_MAX_RULES)
    }

    @Test
    fun `a free user can add rules until they hold two`() {
        assertTrue(AutomationLimits.canAddRule(savedRuleCount = 0, isPro = false))
        assertTrue(AutomationLimits.canAddRule(savedRuleCount = 1, isPro = false))
        assertFalse(AutomationLimits.canAddRule(savedRuleCount = 2, isPro = false))
        assertFalse(AutomationLimits.canAddRule(savedRuleCount = 5, isPro = false))
    }

    @Test
    fun `a pro user has no cap`() {
        assertTrue(AutomationLimits.canAddRule(savedRuleCount = 2, isPro = true))
        assertTrue(AutomationLimits.canAddRule(savedRuleCount = 50, isPro = true))
    }
}
