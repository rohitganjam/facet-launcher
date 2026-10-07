package com.facetlauncher.app.domain

import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.facetlauncher.app.data.AutomationPermissionRepository
import com.facetlauncher.app.data.AutomationRuleRepository
import com.facetlauncher.app.data.AutomationStateRepository
import com.facetlauncher.app.data.DeviceStateRepository
import com.facetlauncher.app.data.FacetRepository
import com.facetlauncher.app.data.SettingsRepository
import com.facetlauncher.app.data.local.FacetDatabase
import com.facetlauncher.app.data.model.AutomationPermission
import com.facetlauncher.app.data.model.AutomationRule
import com.facetlauncher.app.data.model.AutomationTrigger
import com.facetlauncher.app.data.model.BatteryDirection
import com.facetlauncher.app.data.model.BatteryLevelCondition
import com.facetlauncher.app.data.model.BatteryStatus
import com.facetlauncher.app.data.model.DeviceState
import com.facetlauncher.app.data.model.RuleEndBehavior
import com.facetlauncher.app.data.model.WifiState
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import org.junit.runner.RunWith
import org.mockito.Mockito.mock
import org.mockito.Mockito.never
import org.mockito.Mockito.verify
import org.mockito.Mockito.`when`
import org.robolectric.RobolectricTestRunner
import java.time.Clock
import java.time.DayOfWeek
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.ZoneOffset

private class MutableClock(var now: LocalDateTime) : Clock() {
    override fun getZone(): ZoneId = ZoneOffset.UTC
    override fun withZone(zone: ZoneId): Clock = this
    override fun instant(): Instant = now.toInstant(ZoneOffset.UTC)
}

/** The real chain — Room, DataStore, the evaluator and the switch use cases — driven by a controllable clock. */
@RunWith(RobolectricTestRunner::class)
class ApplyFacetAutomationUseCaseTest {

    @get:Rule
    val tempFolder = TemporaryFolder()

    private class Fixture(tempFolder: TemporaryFolder) {
        val clock = MutableClock(at(DayOfWeek.MONDAY, 8, 0))
        val database: FacetDatabase = Room.inMemoryDatabaseBuilder(ApplicationProvider.getApplicationContext(), FacetDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        val facets = FacetRepository(database.facetDao())
        val rules = AutomationRuleRepository(database.automationRuleDao(), database.facetDao())
        val settings = SettingsRepository(
            PreferenceDataStoreFactory.create(produceFile = { tempFolder.newFile("settings-${System.nanoTime()}.preferences_pb") }),
        )
        val state = AutomationStateRepository(
            PreferenceDataStoreFactory.create(produceFile = { tempFolder.newFile("automation-${System.nanoTime()}.preferences_pb") }),
        )
        /** What the device reports right now; tests assign it. */
        var deviceState = DeviceState()

        /** Permissions the fake permission check reports as not granted. */
        val denied = mutableSetOf<AutomationPermission>()

        private val devices = mock(DeviceStateRepository::class.java).also { `when`(it.current()).thenAnswer { deviceState } }
        private val permissions = object : AutomationPermissionRepository(ApplicationProvider.getApplicationContext()) {
            override fun isGranted(permission: AutomationPermission) = permission !in denied
        }
        private val refresh = RefreshAutomationStateUseCase(rules, state, facets, settings, EvaluateFacetAutomationUseCase(), devices, permissions, clock)
        val activate = ActivateFacetByIdUseCase(facets, settings, state, refresh)
        val apply = ApplyFacetAutomationUseCase(refresh, activate, settings)

        val personal = runBlocking { facets.addFacet().id }
        val work = runBlocking { facets.addFacet().id }
        val travel = runBlocking { facets.addFacet().id }

        init {
            runBlocking { settings.setActiveFacetId(personal) }
        }

        suspend fun activeFacet() = settings.settings.first().activeFacetId

        suspend fun saveRule(
            trigger: AutomationTrigger,
            target: Long = work,
            end: RuleEndBehavior = RuleEndBehavior.ReturnToBaseline,
            enabled: Boolean = true,
        ): Long = rules.save(AutomationRule(0, target, trigger, end, enabled)).let { (it as com.facetlauncher.app.data.model.SaveRuleResult.Saved).id }

        fun weekdaysNineToSix() = AutomationTrigger.Schedule(
            days = setOf(DayOfWeek.MONDAY, DayOfWeek.TUESDAY, DayOfWeek.WEDNESDAY, DayOfWeek.THURSDAY, DayOfWeek.FRIDAY),
            startMinute = 9 * 60,
            endMinute = 18 * 60,
        )
    }

    private companion object {
        /** A fixed week; 2026-10-05 is a Monday. */
        fun at(day: DayOfWeek, hour: Int, minute: Int, second: Int = 0): LocalDateTime =
            LocalDateTime.of(2026, 10, 5, hour, minute, second).plusDays((day.value - DayOfWeek.MONDAY.value).toLong())
    }

    @Test
    fun `with no rules the facet is left alone and the baseline is recorded`() = runTest {
        val f = Fixture(tempFolder)

        f.apply()

        assertEquals(f.personal, f.activeFacet())
        assertEquals(f.personal, f.state.get().baselineFacetId)
    }

    @Test
    fun `an active schedule switches to its target facet`() = runTest {
        val f = Fixture(tempFolder)
        val rule = f.saveRule(f.weekdaysNineToSix())
        f.clock.now = at(DayOfWeek.MONDAY, 9, 30)

        f.apply()

        assertEquals(f.work, f.activeFacet())
        assertEquals(listOf(rule), f.state.get().activeRuleIds)
    }

    @Test
    fun `the facet returns to the baseline once the window ends`() = runTest {
        val f = Fixture(tempFolder)
        f.saveRule(f.weekdaysNineToSix())
        f.clock.now = at(DayOfWeek.MONDAY, 9, 30)
        f.apply()

        f.clock.now = at(DayOfWeek.MONDAY, 18, 5)
        f.apply()

        assertEquals(f.personal, f.activeFacet())
    }

    @Test
    fun `the window starts at the first second of its start minute and ends after the last second of its end minute`() = runTest {
        val f = Fixture(tempFolder)
        f.saveRule(f.weekdaysNineToSix())

        f.clock.now = at(DayOfWeek.MONDAY, 8, 59, 59)
        f.apply()
        assertEquals("before the start minute", f.personal, f.activeFacet())

        f.clock.now = at(DayOfWeek.MONDAY, 9, 0, 0)
        f.apply()
        assertEquals("first second of the start minute", f.work, f.activeFacet())

        f.clock.now = at(DayOfWeek.MONDAY, 18, 0, 59)
        f.apply()
        assertEquals("last second of the end minute", f.work, f.activeFacet())

        f.clock.now = at(DayOfWeek.MONDAY, 18, 1, 0)
        f.apply()
        assertEquals("after the end minute", f.personal, f.activeFacet())
    }

    @Test
    fun `an overnight window carries across midnight and ends the next morning`() = runTest {
        val f = Fixture(tempFolder)
        f.saveRule(AutomationTrigger.Schedule(setOf(DayOfWeek.FRIDAY), 22 * 60, 6 * 60))

        f.clock.now = at(DayOfWeek.FRIDAY, 23, 0)
        f.apply()
        assertEquals(f.work, f.activeFacet())

        f.clock.now = at(DayOfWeek.SATURDAY, 3, 0)
        f.apply()
        assertEquals("still inside the window after midnight", f.work, f.activeFacet())

        f.clock.now = at(DayOfWeek.SATURDAY, 6, 1)
        f.apply()
        assertEquals(f.personal, f.activeFacet())
    }

    @Test
    fun `a manual switch during the window holds, including after the window ends`() = runTest {
        val f = Fixture(tempFolder)
        f.saveRule(f.weekdaysNineToSix())
        f.clock.now = at(DayOfWeek.MONDAY, 9, 30)
        f.apply()

        f.clock.now = at(DayOfWeek.MONDAY, 11, 0)
        f.activate(f.travel)
        f.clock.now = at(DayOfWeek.MONDAY, 11, 5)
        f.apply()
        assertEquals("rule still true but overridden", f.travel, f.activeFacet())

        f.clock.now = at(DayOfWeek.MONDAY, 18, 5)
        f.apply()
        assertEquals("rule ended, the user's choice stands", f.travel, f.activeFacet())
    }

    @Test
    fun `a manual switch also overrides a rule that became true but was never evaluated`() = runTest {
        // Given a pass just before the window opens, then time passing with no further pass
        // (Home stayed visible, so no wake event or Home press fired)
        val f = Fixture(tempFolder)
        f.saveRule(f.weekdaysNineToSix())
        f.clock.now = at(DayOfWeek.MONDAY, 8, 59)
        f.apply()
        f.clock.now = at(DayOfWeek.MONDAY, 9, 30)

        // When the user switches by hand, and a later pass runs
        f.activate(f.travel)
        f.clock.now = at(DayOfWeek.MONDAY, 9, 35)
        f.apply()

        // Then their choice wins: the rule was already true, so it must not override them afterwards
        assertEquals(f.travel, f.activeFacet())
    }

    @Test
    fun `a rule that starts again the next day applies again after being overridden`() = runTest {
        val f = Fixture(tempFolder)
        f.saveRule(f.weekdaysNineToSix())
        f.clock.now = at(DayOfWeek.MONDAY, 9, 30)
        f.apply()
        f.activate(f.travel)
        f.clock.now = at(DayOfWeek.MONDAY, 18, 5)
        f.apply()

        f.clock.now = at(DayOfWeek.TUESDAY, 9, 30)
        f.apply()

        assertEquals(f.work, f.activeFacet())
    }

    @Test
    fun `switch-to ending moves to that facet when the window ends`() = runTest {
        val f = Fixture(tempFolder)
        f.saveRule(f.weekdaysNineToSix(), end = RuleEndBehavior.SwitchTo(f.travel))
        f.clock.now = at(DayOfWeek.MONDAY, 9, 30)
        f.apply()

        f.clock.now = at(DayOfWeek.MONDAY, 18, 5)
        f.apply()

        assertEquals(f.travel, f.activeFacet())
    }

    @Test
    fun `a disabled rule never applies`() = runTest {
        val f = Fixture(tempFolder)
        f.saveRule(f.weekdaysNineToSix(), enabled = false)
        f.clock.now = at(DayOfWeek.MONDAY, 9, 30)

        f.apply()

        assertEquals(f.personal, f.activeFacet())
    }

    // --- device triggers ---

    private fun Fixture.bluetoothRule(negated: Boolean = false) =
        AutomationTrigger.Bluetooth(deviceAddress = "AA:BB", deviceName = "Car", negated = negated)

    @Test
    fun `a bluetooth rule applies while its device is connected and ends when it disconnects`() = runTest {
        val f = Fixture(tempFolder)
        f.saveRule(f.bluetoothRule())

        f.deviceState = DeviceState(connectedBluetoothAddresses = setOf("AA:BB"))
        f.apply()
        assertEquals(f.work, f.activeFacet())

        f.deviceState = DeviceState(connectedBluetoothAddresses = emptySet())
        f.apply()
        assertEquals(f.personal, f.activeFacet())
    }

    @Test
    fun `a bluetooth rule never fires while the connection state is still unknown, negated or not`() = runTest {
        val f = Fixture(tempFolder)
        f.saveRule(f.bluetoothRule(negated = true), target = f.travel)
        f.deviceState = DeviceState(connectedBluetoothAddresses = null)

        f.apply()

        assertEquals(f.personal, f.activeFacet())
    }

    @Test
    fun `a named wifi rule without its location permission is unusable and never applies`() = runTest {
        val f = Fixture(tempFolder)
        val trigger = AutomationTrigger.Wifi(ssid = "Home")
        f.saveRule(trigger)
        f.deviceState = DeviceState(wifi = WifiState(connected = true, ssid = "Home"))
        f.denied += AutomationPermission.LOCATION

        f.apply()
        assertEquals("permission missing", f.personal, f.activeFacet())

        f.denied -= AutomationPermission.LOCATION
        f.apply()
        assertEquals("permission granted", f.work, f.activeFacet())
    }

    @Test
    fun `a revoked permission ends a running rule like its condition stopping`() = runTest {
        val f = Fixture(tempFolder)
        val trigger = f.bluetoothRule()
        f.saveRule(trigger)
        f.deviceState = DeviceState(connectedBluetoothAddresses = setOf("AA:BB"))
        f.apply()
        assertEquals(f.work, f.activeFacet())

        f.denied += AutomationPermission.BLUETOOTH_CONNECT
        f.apply()

        assertEquals(f.personal, f.activeFacet())
    }

    @Test
    fun `a battery rule follows the level and the charging state`() = runTest {
        val f = Fixture(tempFolder)
        f.saveRule(AutomationTrigger.Battery(whileCharging = false, level = BatteryLevelCondition(BatteryDirection.BELOW, 20)))

        f.deviceState = DeviceState(battery = BatteryStatus(percent = 50, isCharging = false))
        f.apply()
        assertEquals("above the threshold", f.personal, f.activeFacet())

        f.deviceState = DeviceState(battery = BatteryStatus(percent = 19, isCharging = false))
        f.apply()
        assertEquals("below the threshold", f.work, f.activeFacet())

        f.deviceState = DeviceState(battery = BatteryStatus(percent = 19, isCharging = true))
        f.apply()
        assertEquals("plugged in", f.personal, f.activeFacet())
    }

    @Test
    fun `a headphones rule applies while they are plugged in`() = runTest {
        val f = Fixture(tempFolder)
        f.saveRule(AutomationTrigger.Headphones())

        f.deviceState = DeviceState(headphonesPluggedIn = true)
        f.apply()
        assertEquals(f.work, f.activeFacet())

        f.deviceState = DeviceState(headphonesPluggedIn = false)
        f.apply()
        assertEquals(f.personal, f.activeFacet())
    }

    @Test
    fun `a manual switch also overrides a device rule that became true but was never evaluated`() = runTest {
        // Given a pass while the device is idle, then the car connects with no pass in between
        val f = Fixture(tempFolder)
        f.saveRule(f.bluetoothRule())
        f.deviceState = DeviceState(connectedBluetoothAddresses = emptySet())
        f.apply()
        f.deviceState = DeviceState(connectedBluetoothAddresses = setOf("AA:BB"))

        // When the user switches by hand, and a later pass runs
        f.activate(f.travel)
        f.apply()

        // Then their choice wins, exactly as for a schedule
        assertEquals(f.travel, f.activeFacet())
    }

    @Test
    fun `running a pass twice with nothing changed changes nothing`() = runTest {
        val f = Fixture(tempFolder)
        f.saveRule(f.weekdaysNineToSix())
        f.clock.now = at(DayOfWeek.MONDAY, 9, 30)
        f.apply()
        val stateAfterFirst = f.state.get()

        f.apply()

        assertEquals(f.work, f.activeFacet())
        assertEquals(stateAfterFirst, f.state.get())
    }

    @Test
    fun `nothing happens while there is no valid active facet yet`() = runTest {
        // Given a launcher whose active facet id doesn't exist (a fresh install before the first facet is created)
        val f = Fixture(tempFolder)
        f.saveRule(f.weekdaysNineToSix())
        f.settings.setActiveFacetId(0L)
        f.clock.now = at(DayOfWeek.MONDAY, 9, 30)

        // When a pass runs
        f.apply()

        // Then it neither switches nor writes any state
        assertEquals(0L, f.activeFacet())
        assertNull(f.state.get().baselineFacetId)
    }

    @Test
    fun `a switch decided against a stale facet is not applied after the user switched by hand`() = runTest {
        // Given the rules want Work while Personal was showing, but the user has since moved to Travel
        val refresh = mock(RefreshAutomationStateUseCase::class.java)
        `when`(refresh.invoke()).thenReturn(AutomationDecision(desiredFacetId = 2L, currentFacetId = 1L))
        val activate = mock(ActivateFacetByIdUseCase::class.java)
        val settings = mock(SettingsRepository::class.java)
        `when`(settings.settings).thenReturn(kotlinx.coroutines.flow.flowOf(com.facetlauncher.app.data.model.LauncherSettings(activeFacetId = 3L)))

        // When the pass applies its decision
        ApplyFacetAutomationUseCase(refresh, activate, settings)()

        // Then it leaves the user's choice alone
        verify(activate, never()).invoke(2L, com.facetlauncher.app.data.model.FacetSwitchSource.AUTOMATION)
    }
}
