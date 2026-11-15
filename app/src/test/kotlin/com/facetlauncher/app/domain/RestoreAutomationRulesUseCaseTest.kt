package com.facetlauncher.app.domain

import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.facetlauncher.app.data.AutomationPermissionRepository
import com.facetlauncher.app.data.AutomationRuleRepository
import com.facetlauncher.app.data.AutomationStateRepository
import com.facetlauncher.app.data.DeviceStateRepository
import com.facetlauncher.app.data.FacetRepository
import com.facetlauncher.app.data.FakeEntitlementRepository
import com.facetlauncher.app.data.SettingsRepository
import com.facetlauncher.app.data.local.FacetDatabase
import com.facetlauncher.app.data.model.AutomationPermission
import com.facetlauncher.app.data.model.AutomationRule
import com.facetlauncher.app.data.model.AutomationState
import com.facetlauncher.app.data.model.AutomationTrigger
import com.facetlauncher.app.data.model.BackupAutomationRule
import com.facetlauncher.app.data.model.DeviceState
import com.facetlauncher.app.data.model.RuleEndBehavior
import java.time.Clock
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import org.junit.runner.RunWith
import org.mockito.Mockito.`when`
import org.mockito.Mockito.mock
import org.robolectric.RobolectricTestRunner

/** The restore's automation step over a real database, settings and automation state. */
@RunWith(RobolectricTestRunner::class)
class RestoreAutomationRulesUseCaseTest {

    @get:Rule
    val tempFolder = TemporaryFolder()

    private class Fixture(tempFolder: TemporaryFolder, isPro: Boolean) {
        val database: FacetDatabase = Room.inMemoryDatabaseBuilder(ApplicationProvider.getApplicationContext(), FacetDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        val facets = FacetRepository(database.facetDao(), database.clockStackItemDao())
        val rules = AutomationRuleRepository(database.automationRuleDao(), database.facetDao())
        val settings = SettingsRepository(
            PreferenceDataStoreFactory.create(produceFile = { tempFolder.newFile("settings-${System.nanoTime()}.preferences_pb") }),
        )
        val state = AutomationStateRepository(
            PreferenceDataStoreFactory.create(produceFile = { tempFolder.newFile("automation-${System.nanoTime()}.preferences_pb") }),
        )

        /** What the device reports; headphones are in, so a headphones rule is true right away. */
        var deviceState = DeviceState(headphonesPluggedIn = true)
        val denied = mutableSetOf<AutomationPermission>()
        private val devices = mock(DeviceStateRepository::class.java).also { `when`(it.current()).thenAnswer { deviceState } }
        private val permissions = object : AutomationPermissionRepository(ApplicationProvider.getApplicationContext()) {
            override fun isGranted(permission: AutomationPermission) = permission !in denied
        }
        private val entitlement = FakeEntitlementRepository(isPro)
        private val selectable = SelectableFacetsUseCase(facets, entitlement)
        private val refresh = RefreshAutomationStateUseCase(
            rules, state, selectable, settings, EvaluateFacetAutomationUseCase(), devices, permissions, entitlement, CanUseTriggerUseCase(), Clock.systemUTC(),
        )
        val restore = RestoreAutomationRulesUseCase(
            rules, state, ActivateFacetByIdUseCase(selectable, settings, state, refresh), CanUseTriggerUseCase(), entitlement, permissions,
        )

        val work = runBlocking { facets.addFacet() }
        val home = runBlocking { facets.addFacet() }
        val facetIdByUuid = mapOf(work.uuid to work.id, home.uuid to home.id)

        fun rule(triggerType: String, target: String = work.uuid, end: String = "RETURN_TO_BASELINE", endUuid: String? = null, enabled: Boolean = true) =
            BackupAutomationRule(enabled = enabled, targetFacetUuid = target, endBehavior = end, endFacetUuid = endUuid, triggerType = triggerType)

        fun schedule() = rule("SCHEDULE").copy(scheduleDays = listOf("SATURDAY"), scheduleStartMinute = 0, scheduleEndMinute = 1)
    }

    @Test
    fun `restores the backup's rules in order, replacing the current ones and keeping which are off`() = runTest {
        // Given a rule already on this phone, and a backup with two rules
        val fixture = Fixture(tempFolder, isPro = true)
        fixture.rules.save(AutomationRule(0, fixture.home.id, AutomationTrigger.Headphones()))
        val backup = listOf(
            fixture.rule("WIFI", target = fixture.home.uuid, end = "SWITCH_TO", endUuid = fixture.work.uuid, enabled = false).copy(wifiSsid = "Office"),
            fixture.rule("HEADPHONES", end = "STAY"),
        )

        // When restoring
        val summary = fixture.restore(backup, fixture.facetIdByUuid, fixture.home.id)

        // Then exactly the backup's rules are there, in its order
        val restored = fixture.rules.getRules().map { it.copy(id = 0) }
        assertEquals(
            listOf(
                AutomationRule(0, fixture.home.id, AutomationTrigger.Wifi("Office"), RuleEndBehavior.SwitchTo(fixture.work.id), enabled = false),
                AutomationRule(0, fixture.work.id, AutomationTrigger.Headphones(), RuleEndBehavior.Stay),
            ),
            restored,
        )
        assertEquals(2, summary.restoredCount)
        assertTrue(summary.inBackup)
        assertTrue(summary.skipped.isEmpty())
    }

    @Test
    fun `a rule whose facet is missing is skipped, reported, and not written`() = runTest {
        // Given a backup rule aimed at a facet that isn't in it, and one that switches to such a facet
        val fixture = Fixture(tempFolder, isPro = true)
        val backup = listOf(
            fixture.rule("HEADPHONES", target = "gone"),
            fixture.rule("HEADPHONES", end = "SWITCH_TO", endUuid = "gone"),
            fixture.schedule(),
        )

        // When restoring
        val summary = fixture.restore(backup, fixture.facetIdByUuid, fixture.work.id)

        // Then only the schedule rule is written, and both others are reported with their trigger
        assertEquals(listOf<AutomationTrigger>(AutomationTrigger.Schedule(setOf(java.time.DayOfWeek.SATURDAY), 0, 1)), fixture.rules.getRules().map { it.trigger })
        assertEquals(
            List(2) { RestoredRule.Skipped(SkippedRuleReason.FACET_MISSING, AutomationTrigger.Headphones()) },
            summary.skipped,
        )
        assertEquals(1, summary.restoredCount)
    }

    @Test
    fun `on the free plan every rule is restored and the ones it doesn't run are counted as paused`() = runTest {
        // Given a free user restoring three schedule rules and a Wi-Fi rule
        val fixture = Fixture(tempFolder, isPro = false)
        val backup = List(3) { fixture.schedule() } + fixture.rule("WIFI")

        // When restoring
        val summary = fixture.restore(backup, fixture.facetIdByUuid, fixture.work.id)

        // Then all four are kept; the third schedule and the Wi-Fi rule wait for Pro
        assertEquals(4, fixture.rules.getRules().size)
        assertEquals(2, summary.pausedCount)
    }

    @Test
    fun `a restored rule waiting on a permission is counted`() = runTest {
        // Given Bluetooth and location not granted
        val fixture = Fixture(tempFolder, isPro = true)
        fixture.denied += setOf(AutomationPermission.BLUETOOTH_CONNECT, AutomationPermission.LOCATION)
        val backup = listOf(
            fixture.rule("BLUETOOTH").copy(bluetoothAddress = "AA:BB", bluetoothName = "Car"),
            fixture.rule("WIFI").copy(wifiSsid = "Office"),
            fixture.rule("WIFI"),
        )

        // When restoring
        val summary = fixture.restore(backup, fixture.facetIdByUuid, fixture.work.id)

        // Then the Bluetooth and named-network rules need a permission; "any network" doesn't
        assertEquals(2, summary.needsPermissionCount)
    }

    @Test
    fun `the restored active facet counts as picked by hand, so a rule already true waits for its next start`() = runTest {
        // Given stale automation state from before the restore, and a backup rule that is true right now (headphones in)
        val fixture = Fixture(tempFolder, isPro = true)
        fixture.state.update { AutomationState(baselineFacetId = 999, activeRuleIds = listOf(500), suppressedRuleIds = setOf(501)) }
        fixture.settings.setActiveFacetId(fixture.home.id)

        // When restoring with Home as the backed-up active facet
        fixture.restore(listOf(fixture.rule("HEADPHONES")), fixture.facetIdByUuid, fixture.home.id)

        // Then Home is the baseline and the true rule is suppressed instead of switching to Work
        val ruleId = fixture.rules.getRules().single().id
        assertEquals(AutomationState(baselineFacetId = fixture.home.id, activeRuleIds = listOf(ruleId), suppressedRuleIds = setOf(ruleId)), fixture.state.get())
        assertEquals(fixture.home.id, fixture.settings.settings.first().activeFacetId)
    }

    @Test
    fun `a backup from before rules were backed up clears the rules and says it had none`() = runTest {
        // Given a rule on this phone
        val fixture = Fixture(tempFolder, isPro = true)
        fixture.rules.save(AutomationRule(0, fixture.work.id, AutomationTrigger.Headphones()))

        // When restoring a backup with no rule list
        val summary = fixture.restore(null, fixture.facetIdByUuid, fixture.work.id)

        // Then no rules are left, and the summary says the backup had none
        assertTrue(fixture.rules.getRules().isEmpty())
        assertFalse(summary.inBackup)
        assertEquals(0, summary.restoredCount)
    }
}
