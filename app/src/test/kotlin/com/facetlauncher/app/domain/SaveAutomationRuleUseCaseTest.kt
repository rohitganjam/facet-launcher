package com.facetlauncher.app.domain

import com.facetlauncher.app.data.AutomationRuleRepository
import com.facetlauncher.app.data.model.AutomationRule
import com.facetlauncher.app.data.model.AutomationTrigger
import com.facetlauncher.app.data.model.ProReason
import com.facetlauncher.app.data.model.SaveRuleResult
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test
import org.mockito.Mockito.mock
import org.mockito.Mockito.never
import org.mockito.Mockito.verify
import org.mockito.Mockito.`when`
import java.time.DayOfWeek

class SaveAutomationRuleUseCaseTest {

    private val schedule = AutomationTrigger.Schedule(setOf(DayOfWeek.MONDAY), 540, 1080)
    private val repository = mock(AutomationRuleRepository::class.java)
    private val useCase = SaveAutomationRuleUseCase(repository, CanUseTriggerUseCase())

    private fun saved(count: Int) = runBlockingStub(List(count) { AutomationRule(it + 1L, 1L, schedule) })

    private fun runBlockingStub(rules: List<AutomationRule>) = kotlinx.coroutines.runBlocking { `when`(repository.getRules()).thenReturn(rules) }

    /** Matches any rule, returning a real one because Mockito's `any` hands back null. */
    private fun anyRule(): AutomationRule {
        org.mockito.ArgumentMatchers.any(AutomationRule::class.java).let { }
        return AutomationRule(0L, 1L, schedule)
    }

    @Test
    fun `a free user saving a device trigger is sent to Pro and nothing is written`() = runTest {
        val result = useCase(AutomationRule(0L, 1L, AutomationTrigger.Headphones()), isPro = false)

        assertEquals(SaveRuleResult.ProRequired(ProReason.TRIGGER), result)
        verify(repository, never()).save(anyRule())
    }

    @Test
    fun `a free user with two rules cannot add a third`() = runTest {
        saved(2)

        val result = useCase(AutomationRule(0L, 1L, schedule), isPro = false)

        assertEquals(SaveRuleResult.ProRequired(ProReason.RULE_LIMIT), result)
    }

    @Test
    fun `a free user with one rule can add a second`() = runTest {
        saved(1)
        val rule = AutomationRule(0L, 1L, schedule)
        `when`(repository.save(rule)).thenReturn(SaveRuleResult.Saved(2L))

        assertEquals(SaveRuleResult.Saved(2L), useCase(rule, isPro = false))
    }

    @Test
    fun `editing an existing rule never counts as adding one`() = runTest {
        saved(2)
        val rule = AutomationRule(1L, 1L, schedule)
        `when`(repository.save(rule)).thenReturn(SaveRuleResult.Saved(1L))

        assertEquals(SaveRuleResult.Saved(1L), useCase(rule, isPro = false))
    }

    @Test
    fun `a pro user saves device rules past the free limit`() = runTest {
        saved(5)
        val rule = AutomationRule(0L, 1L, AutomationTrigger.Headphones())
        `when`(repository.save(rule)).thenReturn(SaveRuleResult.Saved(6L))

        assertEquals(SaveRuleResult.Saved(6L), useCase(rule, isPro = true))
    }
}
