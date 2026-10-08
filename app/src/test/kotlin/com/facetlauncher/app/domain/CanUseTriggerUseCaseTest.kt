package com.facetlauncher.app.domain

import com.facetlauncher.app.data.model.AutomationRule
import com.facetlauncher.app.data.model.AutomationTrigger
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.DayOfWeek

class CanUseTriggerUseCaseTest {

    private val canUse = CanUseTriggerUseCase()
    private val schedule = AutomationTrigger.Schedule(setOf(DayOfWeek.MONDAY), 540, 1080)

    private fun rule(id: Long, trigger: AutomationTrigger) = AutomationRule(id, 1L, trigger)

    @Test
    fun `a free user can use a schedule and nothing else`() {
        assertTrue(canUse(schedule, isPro = false))
        assertFalse(canUse(AutomationTrigger.Bluetooth("AA", "Car"), isPro = false))
        assertFalse(canUse(AutomationTrigger.Wifi(), isPro = false))
        assertFalse(canUse(AutomationTrigger.Headphones(), isPro = false))
    }

    @Test
    fun `a pro user can use every trigger`() {
        assertTrue(canUse(AutomationTrigger.Bluetooth("AA", "Car"), isPro = true))
        assertTrue(canUse(AutomationTrigger.Wifi(), isPro = true))
    }

    @Test
    fun `pro keeps every rule running`() {
        val rules = listOf(rule(1, schedule), rule(2, AutomationTrigger.Headphones()), rule(3, schedule), rule(4, schedule))

        assertEquals(setOf(1L, 2L, 3L, 4L), canUse.entitledRuleIds(rules, isPro = true))
    }

    @Test
    fun `free keeps the first two schedule rules in list order and pauses the rest`() {
        val rules = listOf(rule(1, schedule), rule(2, AutomationTrigger.Headphones()), rule(3, schedule), rule(4, schedule))

        assertEquals(setOf(1L, 3L), canUse.entitledRuleIds(rules, isPro = false))
    }

    @Test
    fun `free with only device rules runs none of them`() {
        assertEquals(emptySet<Long>(), canUse.entitledRuleIds(listOf(rule(1, AutomationTrigger.Headphones())), isPro = false))
    }
}
