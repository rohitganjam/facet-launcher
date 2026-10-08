package com.facetlauncher.app.ui.settings.automation

import com.facetlauncher.app.data.AutomationPermissionRepository
import com.facetlauncher.app.data.AutomationRuleRepository
import com.facetlauncher.app.data.BluetoothRepository
import com.facetlauncher.app.data.DeviceStateRepository
import com.facetlauncher.app.data.FacetRepository
import com.facetlauncher.app.data.FakeEntitlementRepository
import com.facetlauncher.app.data.SettingsRepository
import com.facetlauncher.app.data.WifiRepository
import com.facetlauncher.app.data.local.FacetEntity
import com.facetlauncher.app.data.model.AutomationPermission
import com.facetlauncher.app.data.model.AutomationRule
import com.facetlauncher.app.data.model.AutomationRuleError
import com.facetlauncher.app.data.model.AutomationTrigger
import com.facetlauncher.app.data.model.LauncherSettings
import com.facetlauncher.app.data.model.PairedBluetoothDevice
import com.facetlauncher.app.data.model.ProReason
import com.facetlauncher.app.data.model.SaveRuleResult
import com.facetlauncher.app.domain.CanUseTriggerUseCase
import com.facetlauncher.app.domain.SaveAutomationRuleUseCase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito.mock
import org.mockito.Mockito.never
import org.mockito.Mockito.verify
import org.mockito.Mockito.`when`
import java.time.DayOfWeek

@OptIn(ExperimentalCoroutinesApi::class)
class RuleEditorViewModelTest {

    private val dispatcher = StandardTestDispatcher()

    @Before fun setUp() = Dispatchers.setMain(dispatcher)

    @After fun tearDown() = Dispatchers.resetMain()

    private val personal = FacetEntity(id = 1, name = "Personal", position = 0)
    private val work = FacetEntity(id = 2, name = "Work", position = 1)
    private val car = PairedBluetoothDevice("AA:BB", "Car")

    /** Matches any rule, returning a real one because Mockito's `any` hands back null. */
    private fun anyRule(): AutomationRule {
        org.mockito.ArgumentMatchers.any(AutomationRule::class.java).let { }
        return AutomationRule(0L, 1L, AutomationTrigger.Headphones())
    }

    private class Fixture(val personal: FacetEntity, val work: FacetEntity, car: PairedBluetoothDevice, var bluetoothGranted: Boolean = true) {
        val ruleRepository = mock(AutomationRuleRepository::class.java).also { runBlocking { `when`(it.getRules()).thenReturn(emptyList()) } }
        val settingsRepository = mock(SettingsRepository::class.java).also {
            `when`(it.settings).thenReturn(flowOf(LauncherSettings(activeFacetId = personal.id)))
        }
        val bluetoothRepository = mock(BluetoothRepository::class.java).also { runBlocking { `when`(it.pairedDevices()).thenReturn(listOf(car)) } }
        val wifiRepository = mock(WifiRepository::class.java).also { runBlocking { `when`(it.nearbyNetworkNames("Home")).thenReturn(listOf("Home", "Cafe")) } }
        val deviceStateRepository = mock(DeviceStateRepository::class.java)
        val entitlement = FakeEntitlementRepository()
        private val facetRepository = mock(FacetRepository::class.java).also { `when`(it.observeFacets()).thenReturn(flowOf(listOf(personal, work))) }
        private val permissionRepository = mock(AutomationPermissionRepository::class.java).also {
            `when`(it.isGranted(AutomationPermission.BLUETOOTH_CONNECT)).thenAnswer { bluetoothGranted }
            `when`(it.isGranted(AutomationPermission.LOCATION)).thenReturn(false)
        }
        val viewModel = RuleEditorViewModel(
            ruleRepository, facetRepository, settingsRepository, bluetoothRepository, wifiRepository, permissionRepository, deviceStateRepository,
            entitlement, CanUseTriggerUseCase(), SaveAutomationRuleUseCase(ruleRepository, CanUseTriggerUseCase()),
        )
    }

    private fun fixture(bluetoothGranted: Boolean = true) = Fixture(personal, work, car, bluetoothGranted)

    private fun Fixture.openNew() {
        viewModel.openNewRule()
        dispatcher.scheduler.advanceUntilIdle()
    }

    @Test
    fun `a new rule opens targeting a facet other than the active one`() = runTest {
        val fixture = fixture().apply { openNew() }

        val editor = fixture.viewModel.editor.value!!
        assertTrue(editor.isNew)
        assertEquals(work.id, editor.targetFacetId)
    }

    @Test
    fun `saving a valid rule closes the editor`() = runTest {
        val fixture = fixture().apply { openNew() }
        `when`(fixture.ruleRepository.save(anyRule())).thenReturn(SaveRuleResult.Saved(9L))

        fixture.viewModel.saveRule()
        dispatcher.scheduler.advanceUntilIdle()

        assertNull(fixture.viewModel.editor.value)
    }

    @Test
    fun `an invalid save keeps the editor open with the errors`() = runTest {
        val fixture = fixture().apply { openNew() }
        `when`(fixture.ruleRepository.save(anyRule())).thenReturn(SaveRuleResult.Invalid(listOf(AutomationRuleError.NO_SCHEDULE_DAYS)))

        fixture.viewModel.saveRule()
        dispatcher.scheduler.advanceUntilIdle()

        assertEquals(listOf(AutomationRuleError.NO_SCHEDULE_DAYS), fixture.viewModel.editor.value!!.errors)
    }

    @Test
    fun `a save that finds the facet gone says so`() = runTest {
        val fixture = fixture().apply { openNew() }
        `when`(fixture.ruleRepository.save(anyRule())).thenReturn(SaveRuleResult.FacetMissing)

        fixture.viewModel.saveRule()
        dispatcher.scheduler.advanceUntilIdle()

        assertTrue(fixture.viewModel.editor.value!!.facetMissing)
    }

    @Test
    fun `editing a field clears the previous errors`() = runTest {
        val fixture = fixture().apply { openNew() }
        `when`(fixture.ruleRepository.save(anyRule())).thenReturn(SaveRuleResult.Invalid(listOf(AutomationRuleError.NO_SCHEDULE_DAYS)))
        fixture.viewModel.saveRule()
        dispatcher.scheduler.advanceUntilIdle()

        fixture.viewModel.editRule { withScheduleDay(DayOfWeek.SUNDAY, true) }

        assertTrue(fixture.viewModel.editor.value!!.errors.isEmpty())
    }

    @Test
    fun `deleting removes the rule and closes the editor`() = runTest {
        val fixture = fixture()
        `when`(fixture.ruleRepository.getById(5L)).thenReturn(AutomationRule(5L, 1L, AutomationTrigger.Headphones()))
        fixture.viewModel.openRule(5L)
        dispatcher.scheduler.advanceUntilIdle()

        fixture.viewModel.deleteRule()
        dispatcher.scheduler.advanceUntilIdle()

        verify(fixture.ruleRepository).delete(5L)
        assertNull(fixture.viewModel.editor.value)
    }

    @Test
    fun `closing the editor discards the draft without saving`() = runTest {
        val fixture = fixture().apply { openNew() }

        fixture.viewModel.closeEditor()

        assertNull(fixture.viewModel.editor.value)
        verify(fixture.ruleRepository, never()).delete(0L)
    }

    @Test
    fun `a trigger that needs no permission applies without asking`() = runTest {
        val fixture = fixture().apply { openNew() }
        var asked = false

        fixture.viewModel.changeTrigger(fixture.viewModel.editor.value!!.withKind(TriggerKind.HEADPHONES)) { _, _ -> asked = true }

        assertEquals(false, asked)
        assertEquals(TriggerKind.HEADPHONES, fixture.viewModel.editor.value!!.trigger.kind())
    }

    @Test
    fun `bluetooth with its permission already granted applies and loads the paired devices`() = runTest {
        val fixture = fixture(bluetoothGranted = true).apply { openNew() }
        var asked = false

        fixture.viewModel.changeTrigger(fixture.viewModel.editor.value!!.withKind(TriggerKind.BLUETOOTH)) { _, _ -> asked = true }
        dispatcher.scheduler.advanceUntilIdle()

        assertEquals(false, asked)
        assertEquals(TriggerKind.BLUETOOTH, fixture.viewModel.editor.value!!.trigger.kind())
        assertEquals(listOf(car), fixture.viewModel.choices.value.bluetooth)
    }

    @Test
    fun `bluetooth without its permission asks and takes effect once granted`() = runTest {
        val fixture = fixture(bluetoothGranted = false).apply { openNew() }
        var asked: AutomationPermission? = null
        var answer: ((Boolean) -> Unit)? = null

        fixture.viewModel.changeTrigger(fixture.viewModel.editor.value!!.withKind(TriggerKind.BLUETOOTH)) { permission, onResult ->
            asked = permission
            answer = onResult
        }
        assertEquals(TriggerKind.SCHEDULE, fixture.viewModel.editor.value!!.trigger.kind())
        answer!!(true)
        dispatcher.scheduler.advanceUntilIdle()

        assertEquals(AutomationPermission.BLUETOOTH_CONNECT, asked)
        assertEquals(TriggerKind.BLUETOOTH, fixture.viewModel.editor.value!!.trigger.kind())
        verify(fixture.deviceStateRepository).onPermissionsChanged()
    }

    @Test
    fun `a refused permission keeps the old trigger and flags the refusal`() = runTest {
        val fixture = fixture(bluetoothGranted = false).apply { openNew() }

        fixture.viewModel.changeTrigger(fixture.viewModel.editor.value!!.withKind(TriggerKind.BLUETOOTH)) { _, onResult -> onResult(false) }
        dispatcher.scheduler.advanceUntilIdle()

        val editor = fixture.viewModel.editor.value!!
        assertEquals(TriggerKind.SCHEDULE, editor.trigger.kind())
        assertEquals(AutomationPermission.BLUETOOTH_CONNECT, editor.permissionDenied)
    }

    @Test
    fun `a refusal is cleared by the next edit`() = runTest {
        val fixture = fixture(bluetoothGranted = false).apply { openNew() }
        fixture.viewModel.changeTrigger(fixture.viewModel.editor.value!!.withKind(TriggerKind.BLUETOOTH)) { _, onResult -> onResult(false) }
        dispatcher.scheduler.advanceUntilIdle()

        fixture.viewModel.editRule { withStartMinute(100) }

        assertNull(fixture.viewModel.editor.value!!.permissionDenied)
    }

    @Test
    fun `a named wifi network needs location and any network does not`() = runTest {
        val fixture = fixture().apply { openNew() }
        fixture.viewModel.changeTrigger(fixture.viewModel.editor.value!!.withKind(TriggerKind.WIFI)) { _, _ -> error("any network needs no permission") }
        var asked: AutomationPermission? = null

        fixture.viewModel.changeTrigger(fixture.viewModel.editor.value!!.withWifiScope(named = true)) { permission, _ -> asked = permission }

        assertEquals(AutomationPermission.LOCATION, asked)
        assertNull((fixture.viewModel.editor.value!!.trigger as AutomationTrigger.Wifi).ssid)
    }

    @Test
    fun `an answer is recorded so the permissions screen knows it was asked`() = runTest {
        val fixture = fixture().apply { openNew() }

        fixture.viewModel.onPermissionResult(AutomationPermission.LOCATION, granted = false)
        dispatcher.scheduler.advanceUntilIdle()

        verify(fixture.settingsRepository).setLocationPermissionRequested(true)
        verify(fixture.deviceStateRepository).onPermissionsChanged()
    }

    @Test
    fun `a free user picking a Pro trigger gets the upgrade prompt without a permission request`() = runTest {
        val fixture = fixture(bluetoothGranted = false).apply { openNew() }
        fixture.entitlement.proFlow.value = false
        var asked = false

        fixture.viewModel.changeTrigger(fixture.viewModel.editor.value!!.withKind(TriggerKind.BLUETOOTH)) { _, _ -> asked = true }

        val editor = fixture.viewModel.editor.value!!
        assertEquals(false, asked)
        assertEquals(ProReason.TRIGGER, editor.proRequired)
        assertEquals(TriggerKind.SCHEDULE, editor.trigger.kind())
    }

    @Test
    fun `a free user can still pick the free trigger`() = runTest {
        val fixture = fixture().apply { openNew() }
        fixture.entitlement.proFlow.value = false

        fixture.viewModel.changeTrigger(fixture.viewModel.editor.value!!.withKind(TriggerKind.SCHEDULE)) { _, _ -> error("no permission needed") }

        assertNull(fixture.viewModel.editor.value!!.proRequired)
    }

    @Test
    fun `a save the free plan refuses opens the upgrade prompt and keeps the draft`() = runTest {
        val fixture = fixture().apply { openNew() }
        fixture.entitlement.proFlow.value = false
        fixture.viewModel.editRule { withTrigger(AutomationTrigger.Headphones()) }

        fixture.viewModel.saveRule()
        dispatcher.scheduler.advanceUntilIdle()

        assertEquals(ProReason.TRIGGER, fixture.viewModel.editor.value!!.proRequired)
        verify(fixture.ruleRepository, never()).save(anyRule())
    }

    @Test
    fun `the next edit dismisses the upgrade prompt`() = runTest {
        val fixture = fixture().apply { openNew() }
        fixture.entitlement.proFlow.value = false
        fixture.viewModel.changeTrigger(fixture.viewModel.editor.value!!.withKind(TriggerKind.HEADPHONES)) { _, _ -> }

        fixture.viewModel.editRule { this }

        assertNull(fixture.viewModel.editor.value!!.proRequired)
    }

    @Test
    fun `the viewmodel exposes the entitlement`() = runTest {
        val fixture = fixture()

        assertEquals(true, fixture.viewModel.isPro.value)
        fixture.entitlement.proFlow.value = false
        assertEquals(false, fixture.viewModel.isPro.value)
    }
}
