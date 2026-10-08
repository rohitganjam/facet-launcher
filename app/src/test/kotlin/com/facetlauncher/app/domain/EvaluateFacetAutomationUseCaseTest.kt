package com.facetlauncher.app.domain

import com.facetlauncher.app.data.model.AutomationRule
import com.facetlauncher.app.data.model.AutomationState
import com.facetlauncher.app.data.model.AutomationTrigger
import com.facetlauncher.app.data.model.BatteryDirection
import com.facetlauncher.app.data.model.BatteryLevelCondition
import com.facetlauncher.app.data.model.RuleEndBehavior
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

private const val PERSONAL = 1L
private const val WORK = 2L
private const val TRAVEL = 3L
private const val DRIVE = 4L
private const val HOME = 5L

private const val WORK_RULE = 10L
private const val CAR_RULE = 11L

/** Plays a timeline: each [tick] is one evaluation, each [manual] a switch made by the user. */
private class Sim(
    var rules: List<AutomationRule>,
    var facets: Set<Long> = setOf(PERSONAL, WORK, TRAVEL, DRIVE, HOME),
    var shown: Long = PERSONAL,
) {
    private val evaluate = EvaluateFacetAutomationUseCase()
    var state = AutomationState()

    fun tick(vararg conditionsMet: Long): Long {
        val result = evaluate(rules, conditionsMet.toSet(), state, shown, facets)
        state = result.state
        shown = result.desiredFacetId
        return shown
    }

    fun manual(facetId: Long) {
        state = state.afterManualSwitch(facetId)
        shown = facetId
    }
}

// The evaluator never reads the trigger; it only sees which rule ids have their condition met.
private val ANY_TRIGGER = AutomationTrigger.Battery(whileCharging = false, level = BatteryLevelCondition(BatteryDirection.BELOW, 20))

private fun workRule(end: RuleEndBehavior = RuleEndBehavior.ReturnToBaseline, enabled: Boolean = true) =
    AutomationRule(WORK_RULE, WORK, ANY_TRIGGER, end, enabled)

private fun carRule() = AutomationRule(CAR_RULE, DRIVE, ANY_TRIGGER)

class EvaluateFacetAutomationUseCaseTest {

    @Test
    fun `no rules keeps the current facet and records it as baseline`() {
        val sim = Sim(emptyList())

        val shown = sim.tick()

        assertEquals(PERSONAL, shown)
        assertEquals(PERSONAL, sim.state.baselineFacetId)
    }

    @Test
    fun `a rule starting switches to its target facet`() {
        val sim = Sim(listOf(workRule()))
        sim.tick()

        val shown = sim.tick(WORK_RULE)

        assertEquals(WORK, shown)
    }

    @Test
    fun `a rule ending returns to the baseline`() {
        val sim = Sim(listOf(workRule()))
        sim.tick()
        sim.tick(WORK_RULE)

        val shown = sim.tick()

        assertEquals(PERSONAL, shown)
    }

    @Test
    fun `overlapping rules resolve to the most recently activated`() {
        val sim = Sim(listOf(workRule(), carRule()))
        sim.tick()
        sim.tick(WORK_RULE)

        val shown = sim.tick(WORK_RULE, CAR_RULE)

        assertEquals(DRIVE, shown)
    }

    @Test
    fun `when the newer overlapping rule ends the older one is shown again`() {
        val sim = Sim(listOf(workRule(), carRule()))
        sim.tick()
        sim.tick(WORK_RULE)
        sim.tick(WORK_RULE, CAR_RULE)

        val afterCarEnds = sim.tick(WORK_RULE)
        val afterWorkEnds = sim.tick()

        assertEquals(WORK, afterCarEnds)
        assertEquals(PERSONAL, afterWorkEnds)
    }

    @Test
    fun `a manual switch during a rule holds while the rule stays true`() {
        val sim = Sim(listOf(workRule()))
        sim.tick()
        sim.tick(WORK_RULE)
        sim.manual(TRAVEL)

        val shown = sim.tick(WORK_RULE)

        assertEquals(TRAVEL, shown)
    }

    @Test
    fun `ending a rule the user overrode leaves their facet in place`() {
        val sim = Sim(listOf(workRule()))
        sim.tick()
        sim.tick(WORK_RULE)
        sim.manual(TRAVEL)
        sim.tick(WORK_RULE)

        val shown = sim.tick()

        assertEquals(TRAVEL, shown)
        assertTrue(sim.state.suppressedRuleIds.isEmpty())
    }

    @Test
    fun `an overridden rule applies again the next time it starts`() {
        val sim = Sim(listOf(workRule()))
        sim.tick()
        sim.tick(WORK_RULE)
        sim.manual(TRAVEL)
        sim.tick()

        val shown = sim.tick(WORK_RULE)

        assertEquals(WORK, shown)
    }

    @Test
    fun `a rule overridden by hand skips its switch-to end behavior`() {
        val sim = Sim(listOf(workRule(end = RuleEndBehavior.SwitchTo(HOME))))
        sim.tick()
        sim.tick(WORK_RULE)
        sim.manual(TRAVEL)

        val shown = sim.tick()

        assertEquals(TRAVEL, shown)
    }

    @Test
    fun `switch-to end behavior moves to that facet and makes it the baseline`() {
        val sim = Sim(listOf(workRule(end = RuleEndBehavior.SwitchTo(HOME))))
        sim.tick()
        sim.tick(WORK_RULE)

        val shown = sim.tick()

        assertEquals(HOME, shown)
        assertEquals(HOME, sim.state.baselineFacetId)
    }

    @Test
    fun `stay end behavior keeps the target facet as the new baseline`() {
        val sim = Sim(listOf(workRule(end = RuleEndBehavior.Stay)))
        sim.tick()
        sim.tick(WORK_RULE)

        val shown = sim.tick()

        assertEquals(WORK, shown)
        assertEquals(WORK, sim.state.baselineFacetId)
    }

    @Test
    fun `a different rule starting after a manual switch still applies`() {
        val sim = Sim(listOf(workRule(), carRule()))
        sim.tick()
        sim.tick(WORK_RULE)
        sim.manual(TRAVEL)

        val shown = sim.tick(WORK_RULE, CAR_RULE)

        assertEquals(DRIVE, shown)
    }

    @Test
    fun `a disabled rule never fires`() {
        val sim = Sim(listOf(workRule(enabled = false)))
        sim.tick()

        val shown = sim.tick(WORK_RULE)

        assertEquals(PERSONAL, shown)
    }

    @Test
    fun `disabling an active rule ends it with its end behavior`() {
        val sim = Sim(listOf(workRule(end = RuleEndBehavior.SwitchTo(HOME))))
        sim.tick()
        sim.tick(WORK_RULE)
        sim.rules = listOf(workRule(end = RuleEndBehavior.SwitchTo(HOME), enabled = false))

        val shown = sim.tick(WORK_RULE)

        assertEquals(HOME, shown)
    }

    @Test
    fun `deleting an active rule returns to the baseline`() {
        val sim = Sim(listOf(workRule()))
        sim.tick()
        sim.tick(WORK_RULE)
        sim.rules = emptyList()

        val shown = sim.tick(WORK_RULE)

        assertEquals(PERSONAL, shown)
    }

    @Test
    fun `a switch-to target that no longer exists falls back to the baseline`() {
        val sim = Sim(listOf(workRule(end = RuleEndBehavior.SwitchTo(HOME))))
        sim.tick()
        sim.tick(WORK_RULE)
        sim.facets = sim.facets - HOME

        val shown = sim.tick()

        assertEquals(PERSONAL, shown)
        assertEquals(PERSONAL, sim.state.baselineFacetId)
    }

    @Test
    fun `first evaluation with a rule already true records the current facet as baseline`() {
        val sim = Sim(listOf(workRule()))

        val whileTrue = sim.tick(WORK_RULE)
        val afterEnd = sim.tick()

        assertEquals(WORK, whileTrue)
        assertEquals(PERSONAL, afterEnd)
    }

    @Test
    fun `a manual switch with no active rules only moves the baseline`() {
        val sim = Sim(listOf(workRule()))
        sim.tick()
        sim.manual(TRAVEL)

        val shown = sim.tick()

        assertEquals(TRAVEL, shown)
        assertTrue(sim.state.suppressedRuleIds.isEmpty())
    }

    @Test
    fun `rules that start together resolve to the later one in the list`() {
        val sim = Sim(listOf(workRule(), carRule()))
        sim.tick()

        val shown = sim.tick(WORK_RULE, CAR_RULE)

        assertEquals(DRIVE, shown)
    }

    @Test
    fun `a deleted baseline facet falls back to the current facet`() {
        val sim = Sim(listOf(workRule()))
        sim.tick()
        sim.facets = sim.facets - PERSONAL
        sim.shown = TRAVEL

        val shown = sim.tick()

        assertEquals(TRAVEL, shown)
        assertEquals(TRAVEL, sim.state.baselineFacetId)
    }
}
