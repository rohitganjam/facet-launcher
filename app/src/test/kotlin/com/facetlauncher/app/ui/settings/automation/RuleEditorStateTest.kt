package com.facetlauncher.app.ui.settings.automation

import com.facetlauncher.app.data.local.FacetEntity
import com.facetlauncher.app.data.model.AutomationRule
import com.facetlauncher.app.data.model.AutomationRuleError
import com.facetlauncher.app.data.model.AutomationTrigger
import com.facetlauncher.app.data.model.RuleEndBehavior
import com.facetlauncher.app.data.model.validationErrors
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.DayOfWeek

class RuleEditorStateTest {

    private val personal = FacetEntity(id = 1, name = "Personal", position = 0)
    private val work = FacetEntity(id = 2, name = "Work", position = 1)
    private val facets = listOf(personal, work)

    @Test
    fun `a new rule is a weekday nine to six schedule on a facet other than the active one`() {
        val state = RuleEditorState.forNew(facets, activeFacetId = personal.id)

        val schedule = state.trigger as AutomationTrigger.Schedule
        assertEquals(work.id, state.targetFacetId)
        assertEquals(5, schedule.days.size)
        assertEquals(540, schedule.startMinute)
        assertEquals(1080, schedule.endMinute)
        assertEquals(EndKind.RETURN, state.endKind)
        assertTrue(state.isNew)
    }

    @Test
    fun `a single facet is its own target`() {
        assertEquals(personal.id, RuleEditorState.forNew(listOf(personal), personal.id).targetFacetId)
    }

    @Test
    fun `a new rule saves as a valid rule`() {
        assertTrue(RuleEditorState.forNew(facets, personal.id).toRule().validationErrors().isEmpty())
    }

    @Test
    fun `an existing rule round-trips through the editor`() {
        val rule = AutomationRule(7L, work.id, AutomationTrigger.Headphones(), RuleEndBehavior.SwitchTo(personal.id), enabled = false)

        assertEquals(rule, RuleEditorState.from(rule, facets).toRule())
    }

    @Test
    fun `each ending maps to its behavior`() {
        val base = RuleEditorState.forNew(facets, personal.id).copy(endFacetId = personal.id)

        assertEquals(RuleEndBehavior.ReturnToBaseline, base.copy(endKind = EndKind.RETURN).toRule().endBehavior)
        assertEquals(RuleEndBehavior.SwitchTo(personal.id), base.copy(endKind = EndKind.SWITCH).toRule().endBehavior)
        assertEquals(RuleEndBehavior.Stay, base.copy(endKind = EndKind.STAY).toRule().endBehavior)
    }

    @Test
    fun `clearing every day makes the rule invalid`() {
        var state = RuleEditorState.forNew(facets, personal.id)
        (state.trigger as AutomationTrigger.Schedule).days.forEach { state = state.withScheduleDay(it, false) }

        assertEquals(listOf(AutomationRuleError.NO_SCHEDULE_DAYS), state.toRule().validationErrors())
    }

    @Test
    fun `days and times update the schedule`() {
        val state = RuleEditorState.forNew(facets, personal.id)
            .withScheduleDay(DayOfWeek.SUNDAY, true)
            .withStartMinute(60)
            .withEndMinute(120)

        val schedule = state.trigger as AutomationTrigger.Schedule
        assertTrue(DayOfWeek.SUNDAY in schedule.days)
        assertEquals(60, schedule.startMinute)
        assertEquals(120, schedule.endMinute)
    }

    @Test
    fun `schedule edits leave a device rule untouched`() {
        val rule = AutomationRule(7L, work.id, AutomationTrigger.Headphones())

        val state = RuleEditorState.from(rule, facets).withScheduleDay(DayOfWeek.MONDAY, true).withStartMinute(5)

        assertEquals(AutomationTrigger.Headphones(), state.trigger)
    }

    @Test
    fun `an edit clears the last save's errors`() {
        val state = RuleEditorState.forNew(facets, personal.id).copy(errors = listOf(AutomationRuleError.NO_SCHEDULE_DAYS), facetMissing = true)

        val edited = state.withStartMinute(100)

        assertTrue(edited.errors.isEmpty())
        assertEquals(false, edited.facetMissing)
    }

    @Test
    fun `switching type starts that type from its defaults`() {
        val base = RuleEditorState.forNew(facets, personal.id)

        assertEquals(TriggerKind.BLUETOOTH, base.withKind(TriggerKind.BLUETOOTH).trigger.kind())
        assertEquals(TriggerKind.WIFI, base.withKind(TriggerKind.WIFI).trigger.kind())
        assertEquals(AutomationTrigger.Headphones(), base.withKind(TriggerKind.HEADPHONES).trigger)
        val battery = base.withKind(TriggerKind.BATTERY).trigger as AutomationTrigger.Battery
        assertEquals(false, battery.whileCharging)
        assertEquals(20, battery.level.thresholdPercent)
    }

    @Test
    fun `picking the current type again changes nothing`() {
        val base = RuleEditorState.forNew(facets, personal.id).withScheduleDay(DayOfWeek.SUNDAY, true)

        assertEquals(base.trigger, base.withKind(TriggerKind.SCHEDULE).trigger)
    }

    @Test
    fun `a fresh bluetooth rule cannot be saved until a device is chosen`() {
        val state = RuleEditorState.forNew(facets, personal.id).withKind(TriggerKind.BLUETOOTH)

        assertEquals(listOf(AutomationRuleError.NO_BLUETOOTH_DEVICE), state.toRule().validationErrors())
    }

    @Test
    fun `any network is valid and a named network needs a name`() {
        val any = RuleEditorState.forNew(facets, personal.id).withKind(TriggerKind.WIFI)
        val named = any.withWifiScope(named = true)

        assertTrue(any.toRule().validationErrors().isEmpty())
        assertEquals(listOf(AutomationRuleError.BLANK_WIFI_NETWORK), named.toRule().validationErrors())
        assertEquals(null, (named.withWifiScope(named = false).trigger as AutomationTrigger.Wifi).ssid)
    }

    @Test
    fun `the default battery rule is valid`() {
        assertTrue(RuleEditorState.forNew(facets, personal.id).withKind(TriggerKind.BATTERY).toRule().validationErrors().isEmpty())
    }
}
