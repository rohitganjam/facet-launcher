package com.facetlauncher.app.domain

import com.facetlauncher.app.data.local.FacetEntity
import com.facetlauncher.app.data.model.AutomationPermission
import com.facetlauncher.app.data.model.AutomationRule
import com.facetlauncher.app.data.model.AutomationState
import com.facetlauncher.app.data.model.AutomationTrigger
import com.facetlauncher.app.data.model.RuleEndBehavior
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.DayOfWeek

class ObserveFacetAutomationUseCaseTest {

    private val personal = FacetEntity(id = 1, name = "Personal", position = 0)
    private val work = FacetEntity(id = 2, name = "Work", position = 1)
    private val travel = FacetEntity(id = 3, name = "Travel", position = 2)
    private val facets = listOf(personal, work, travel)

    private val schedule = AutomationTrigger.Schedule(setOf(DayOfWeek.MONDAY), 540, 1080)
    private val workRule = AutomationRule(10L, work.id, schedule)
    private val carRule = AutomationRule(11L, travel.id, AutomationTrigger.Bluetooth("AA:BB", "Car"))

    private fun build(
        rules: List<AutomationRule> = listOf(workRule),
        state: AutomationState = AutomationState(),
        activeFacetId: Long = personal.id,
        granted: Set<AutomationPermission> = emptySet(),
        isPro: Boolean = true,
    ) = buildFacetAutomationScreenState(rules, state, facets, activeFacetId, { it in granted }, isPro)

    @Test
    fun `rows carry the target facet name and a switch-to ending's facet name`() {
        val rule = workRule.copy(endBehavior = RuleEndBehavior.SwitchTo(travel.id))

        val item = build(rules = listOf(rule)).items.single()

        assertEquals("Work", item.targetFacetName)
        assertEquals("Travel", item.endFacetName)
    }

    @Test
    fun `other endings have no end facet name`() {
        assertNull(build().items.single().endFacetName)
    }

    @Test
    fun `no rules means no rows and no status`() {
        val state = build(rules = emptyList())

        assertTrue(state.items.isEmpty())
        assertNull(state.status)
    }

    @Test
    fun `a running rule that is showing its facet is the status`() {
        val state = build(state = AutomationState(activeRuleIds = listOf(10L)), activeFacetId = work.id)

        val status = state.status as AutomationStatus.Driving
        assertEquals("Work", status.facetName)
        assertEquals(10L, status.rule.rule.id)
    }

    @Test
    fun `with two running rules the latest one is the status`() {
        val state = build(
            rules = listOf(workRule, carRule),
            state = AutomationState(activeRuleIds = listOf(10L, 11L)),
            activeFacetId = travel.id,
        )

        assertEquals(11L, (state.status as AutomationStatus.Driving).rule.rule.id)
    }

    @Test
    fun `a running rule the user overrode is reported as paused`() {
        val state = build(
            state = AutomationState(baselineFacetId = travel.id, activeRuleIds = listOf(10L), suppressedRuleIds = setOf(10L)),
            activeFacetId = travel.id,
        )

        val status = state.status as AutomationStatus.ManualOverride
        assertEquals("Travel", status.facetName)
        assertEquals(10L, status.pausedRule.rule.id)
    }

    @Test
    fun `an overridden rule does not hide a newer rule that is driving`() {
        val state = build(
            rules = listOf(workRule, carRule),
            state = AutomationState(activeRuleIds = listOf(10L, 11L), suppressedRuleIds = setOf(10L)),
            activeFacetId = travel.id,
        )

        assertEquals(11L, (state.status as AutomationStatus.Driving).rule.rule.id)
    }

    @Test
    fun `no status when nothing is running`() {
        assertNull(build(state = AutomationState(baselineFacetId = personal.id)).status)
    }

    @Test
    fun `no status when the running rule's facet is not the one showing`() {
        assertNull(build(state = AutomationState(activeRuleIds = listOf(10L)), activeFacetId = personal.id).status)
    }

    @Test
    fun `a bluetooth rule without its permission is unavailable and says what it needs`() {
        val item = build(rules = listOf(carRule), granted = emptySet()).items.single()

        assertEquals(RuleAvailability.NEEDS_BLUETOOTH, item.availability)
    }

    @Test
    fun `a named wifi rule without location is unavailable and says what it needs`() {
        val rule = AutomationRule(12L, work.id, AutomationTrigger.Wifi(ssid = "Home"))

        assertEquals(RuleAvailability.NEEDS_LOCATION, build(rules = listOf(rule), granted = emptySet()).items.single().availability)
    }

    @Test
    fun `rules are available once their permission is granted or none is needed`() {
        val named = AutomationRule(12L, work.id, AutomationTrigger.Wifi(ssid = "Home"))
        val state = build(rules = listOf(workRule, carRule, named), granted = setOf(AutomationPermission.BLUETOOTH_CONNECT, AutomationPermission.LOCATION))

        assertTrue(state.items.all { it.availability == RuleAvailability.AVAILABLE })
    }

    @Test
    fun `a free user can add rules up to the limit, a pro user always can`() {
        val two = listOf(workRule, carRule)

        assertTrue(build(rules = listOf(workRule), isPro = false).canAddRule)
        assertFalse(build(rules = two, isPro = false).canAddRule)
        assertTrue(build(rules = two, isPro = true).canAddRule)
    }

    @Test
    fun `a facet that no longer exists shows an empty name rather than failing`() {
        val orphan = AutomationRule(13L, 99L, schedule)

        assertEquals("", build(rules = listOf(orphan)).items.single().targetFacetName)
    }
}
