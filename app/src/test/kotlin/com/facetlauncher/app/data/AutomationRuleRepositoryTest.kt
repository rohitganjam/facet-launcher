package com.facetlauncher.app.data

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.facetlauncher.app.data.local.AutomationRuleEntity
import com.facetlauncher.app.data.local.FacetDatabase
import com.facetlauncher.app.data.local.FacetEntity
import com.facetlauncher.app.data.model.AutomationRule
import com.facetlauncher.app.data.model.AutomationRuleError
import com.facetlauncher.app.data.model.AutomationTrigger
import com.facetlauncher.app.data.model.BatteryDirection
import com.facetlauncher.app.data.model.BatteryLevelCondition
import com.facetlauncher.app.data.model.RuleEndBehavior
import com.facetlauncher.app.data.model.SaveRuleResult
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import java.time.DayOfWeek

private val BATTERY_BELOW_20 = AutomationTrigger.Battery(whileCharging = false, level = BatteryLevelCondition(BatteryDirection.BELOW, 20))

@RunWith(RobolectricTestRunner::class)
class AutomationRuleRepositoryTest {

    private class Fixture(val database: FacetDatabase) {
        val repository = AutomationRuleRepository(database.automationRuleDao(), database.facetDao())
        val work = runBlocking { database.facetDao().insert(FacetEntity(name = "Work", position = 0)) }
        val home = runBlocking { database.facetDao().insert(FacetEntity(name = "Home", position = 1)) }

        /** Saves a rule the test expects to be valid, returning its id. */
        suspend fun saveValid(rule: AutomationRule): Long = (repository.save(rule) as SaveRuleResult.Saved).id
    }

    private fun createFixture() = Fixture(
        Room.inMemoryDatabaseBuilder(ApplicationProvider.getApplicationContext(), FacetDatabase::class.java)
            .allowMainThreadQueries()
            .build(),
    )

    private fun rule(
        fixture: Fixture,
        trigger: AutomationTrigger,
        end: RuleEndBehavior = RuleEndBehavior.ReturnToBaseline,
        enabled: Boolean = true,
    ) = AutomationRule(id = 0, targetFacetId = fixture.work, trigger = trigger, endBehavior = end, enabled = enabled)

    @Test
    fun `every trigger type round-trips through storage unchanged`() = runTest {
        // Given one rule per trigger type, including negation, an any-network Wi-Fi and a named one
        val fixture = createFixture()
        val triggers = listOf(
            AutomationTrigger.Schedule(setOf(DayOfWeek.MONDAY, DayOfWeek.WEDNESDAY, DayOfWeek.SUNDAY), 540, 1080),
            AutomationTrigger.Schedule(setOf(DayOfWeek.FRIDAY), 0, 1439),
            AutomationTrigger.Schedule(setOf(DayOfWeek.SATURDAY), 540, 540),
            AutomationTrigger.Bluetooth("AA:BB:CC:DD:EE:FF", "Car", negated = true),
            AutomationTrigger.Wifi(ssid = null),
            AutomationTrigger.Wifi(ssid = "HomeNet-5G", negated = true),
            AutomationTrigger.Headphones(),
            AutomationTrigger.Battery(whileCharging = false, level = BatteryLevelCondition(BatteryDirection.BELOW, 20)),
            AutomationTrigger.Battery(whileCharging = false, level = BatteryLevelCondition(BatteryDirection.ABOVE, 80)),
            AutomationTrigger.Battery(whileCharging = true, level = BatteryLevelCondition(BatteryDirection.BELOW, 100)),
            AutomationTrigger.Battery(whileCharging = true, level = BatteryLevelCondition(BatteryDirection.ABOVE, 95)),
        )

        // When saving them and reading them back
        triggers.forEach { fixture.saveValid(rule(fixture, it)) }
        val stored = fixture.repository.getRules()

        // Then the triggers come back identical, in the order saved
        assertEquals(triggers, stored.map { it.trigger })
    }

    @Test
    fun `every end behavior round-trips and switch-to keeps its target facet`() = runTest {
        val fixture = createFixture()
        val endings = listOf(RuleEndBehavior.ReturnToBaseline, RuleEndBehavior.SwitchTo(fixture.home), RuleEndBehavior.Stay)

        endings.forEach { fixture.saveValid(rule(fixture, BATTERY_BELOW_20, end = it)) }

        assertEquals(endings, fixture.repository.getRules().map { it.endBehavior })
    }

    @Test
    fun `save inserts new rules at the end and returns their ids`() = runTest {
        val fixture = createFixture()

        val first = fixture.saveValid(rule(fixture, BATTERY_BELOW_20))
        val second = fixture.saveValid(rule(fixture, AutomationTrigger.Headphones()))

        assertEquals(listOf(first, second), fixture.repository.getRules().map { it.id })
    }

    @Test
    fun `save with an existing id updates the rule in place and keeps its position`() = runTest {
        // Given two rules
        val fixture = createFixture()
        val first = fixture.saveValid(rule(fixture, BATTERY_BELOW_20))
        val second = fixture.saveValid(rule(fixture, AutomationTrigger.Headphones()))

        // When editing the first one
        val edited = fixture.repository.getById(first)!!.copy(trigger = AutomationTrigger.Headphones(), enabled = false)
        fixture.saveValid(edited)

        // Then it changed in place and still sorts first
        val rules = fixture.repository.getRules()
        assertEquals(listOf(first, second), rules.map { it.id })
        assertEquals(AutomationTrigger.Headphones(), rules.first().trigger)
        assertEquals(false, rules.first().enabled)
    }

    @Test
    fun `setEnabled and delete change only the targeted rule`() = runTest {
        val fixture = createFixture()
        val keep = fixture.saveValid(rule(fixture, BATTERY_BELOW_20))
        val drop = fixture.saveValid(rule(fixture, AutomationTrigger.Headphones()))

        fixture.repository.setEnabled(keep, false)
        fixture.repository.delete(drop)

        val rules = fixture.repository.getRules()
        assertEquals(listOf(keep), rules.map { it.id })
        assertEquals(false, rules.single().enabled)
    }

    @Test
    fun `a switch-to rule whose end facet was deleted degrades to return-to-baseline`() = runTest {
        // Given a rule that switches to Home when it ends
        val fixture = createFixture()
        val id = fixture.saveValid(rule(fixture, BATTERY_BELOW_20, end = RuleEndBehavior.SwitchTo(fixture.home)))

        // When Home is deleted
        fixture.database.facetDao().delete(fixture.database.facetDao().getById(fixture.home)!!)

        // Then the rule is still there, ending with the default behavior
        assertEquals(RuleEndBehavior.ReturnToBaseline, fixture.repository.getById(id)!!.endBehavior)
    }

    @Test
    fun `rows this build cannot interpret are skipped`() = runTest {
        // Given a stored row with a trigger type from some newer build, and one valid rule
        val fixture = createFixture()
        val unknown = fixture.database.automationRuleDao().insert(
            AutomationRuleEntity(position = 0, targetFacetId = fixture.work, endBehavior = "RETURN_TO_BASELINE", triggerType = "GEOFENCE"),
        )
        val valid = fixture.saveValid(rule(fixture, BATTERY_BELOW_20))

        // Then only the interpretable rule is returned, and the unknown one can't be fetched by id either
        assertEquals(listOf(valid), fixture.repository.getRules().map { it.id })
        assertNull(fixture.repository.getById(unknown))
    }

    @Test
    fun `saving a schedule with no days is rejected and writes nothing`() = runTest {
        // Given an empty rule list
        val fixture = createFixture()

        // When saving a schedule with no days selected
        val result = fixture.repository.save(rule(fixture, AutomationTrigger.Schedule(emptySet(), 540, 1080)))

        // Then it is rejected with the reason, and nothing is stored
        assertEquals(SaveRuleResult.Invalid(listOf(AutomationRuleError.NO_SCHEDULE_DAYS)), result)
        assertEquals(emptyList<AutomationRule>(), fixture.repository.getRules())
    }

    @Test
    fun `editing a rule into an invalid one is rejected and leaves the stored rule unchanged`() = runTest {
        // Given a valid stored schedule
        val fixture = createFixture()
        val valid = AutomationTrigger.Schedule(setOf(DayOfWeek.MONDAY), 540, 1080)
        val id = fixture.saveValid(rule(fixture, valid))

        // When saving it back with every day removed
        val result = fixture.repository.save(fixture.repository.getById(id)!!.copy(trigger = valid.copy(days = emptySet())))

        // Then the edit is rejected and the stored rule is untouched
        assertEquals(SaveRuleResult.Invalid(listOf(AutomationRuleError.NO_SCHEDULE_DAYS)), result)
        assertEquals(valid, fixture.repository.getById(id)!!.trigger)
    }

    @Test
    fun `saving a rule for a facet that no longer exists is a no-op`() = runTest {
        // Given a rule aimed at a facet that has since been deleted
        val fixture = createFixture()
        fixture.database.facetDao().delete(fixture.database.facetDao().getById(fixture.work)!!)

        // When saving it
        val result = fixture.repository.save(rule(fixture, BATTERY_BELOW_20))

        // Then nothing is written and nothing throws
        assertEquals(SaveRuleResult.FacetMissing, result)
        assertEquals(emptyList<AutomationRule>(), fixture.repository.getRules())
    }

    @Test
    fun `saving a switch-to ending for a facet that no longer exists is a no-op`() = runTest {
        val fixture = createFixture()
        fixture.database.facetDao().delete(fixture.database.facetDao().getById(fixture.home)!!)

        val result = fixture.repository.save(rule(fixture, BATTERY_BELOW_20, end = RuleEndBehavior.SwitchTo(fixture.home)))

        assertEquals(SaveRuleResult.FacetMissing, result)
        assertEquals(emptyList<AutomationRule>(), fixture.repository.getRules())
    }

    @Test
    fun `a schedule whose start equals its end is saved`() = runTest {
        val fixture = createFixture()

        val result = fixture.repository.save(rule(fixture, AutomationTrigger.Schedule(setOf(DayOfWeek.MONDAY), 540, 540)))

        assertTrue(result is SaveRuleResult.Saved)
    }

    @Test
    fun `a battery row with an unknown direction is skipped`() = runTest {
        val fixture = createFixture()
        fixture.database.automationRuleDao().insert(
            AutomationRuleEntity(
                position = 0,
                targetFacetId = fixture.work,
                endBehavior = "RETURN_TO_BASELINE",
                triggerType = "BATTERY",
                batteryThreshold = 20,
                batteryDirection = "SIDEWAYS",
            ),
        )

        assertEquals(emptyList<AutomationRule>(), fixture.repository.getRules())
    }

    @Test
    fun `a battery row without a complete level condition is skipped`() = runTest {
        val fixture = createFixture()
        val dao = fixture.database.automationRuleDao()
        dao.insert(AutomationRuleEntity(position = 0, targetFacetId = fixture.work, endBehavior = "RETURN_TO_BASELINE", triggerType = "BATTERY", batteryThreshold = 20))
        dao.insert(AutomationRuleEntity(position = 1, targetFacetId = fixture.work, endBehavior = "RETURN_TO_BASELINE", triggerType = "BATTERY", batteryDirection = "BELOW"))
        dao.insert(AutomationRuleEntity(position = 2, targetFacetId = fixture.work, endBehavior = "RETURN_TO_BASELINE", triggerType = "BATTERY"))

        assertEquals(emptyList<AutomationRule>(), fixture.repository.getRules())
    }

    @Test
    fun `a bluetooth row without a device address is skipped`() = runTest {
        val fixture = createFixture()
        fixture.database.automationRuleDao().insert(
            AutomationRuleEntity(position = 0, targetFacetId = fixture.work, endBehavior = "RETURN_TO_BASELINE", triggerType = "BLUETOOTH"),
        )

        assertEquals(emptyList<AutomationRule>(), fixture.repository.getRules())
    }
}
