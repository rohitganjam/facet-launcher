package com.facetlauncher.app.ui.settings.automation

import com.facetlauncher.app.data.AutomationPermissionRepository
import com.facetlauncher.app.data.AutomationRuleRepository
import com.facetlauncher.app.data.AutomationStateRepository
import com.facetlauncher.app.data.FacetRepository
import com.facetlauncher.app.data.SettingsRepository
import com.facetlauncher.app.data.local.FacetEntity
import com.facetlauncher.app.data.model.AutomationPermission
import com.facetlauncher.app.data.model.AutomationRule
import com.facetlauncher.app.data.model.AutomationState
import com.facetlauncher.app.data.model.AutomationTrigger
import com.facetlauncher.app.data.model.LauncherSettings
import com.facetlauncher.app.domain.ObserveFacetAutomationUseCase
import com.facetlauncher.app.domain.RuleAvailability
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify
import org.mockito.Mockito.`when`

@OptIn(ExperimentalCoroutinesApi::class)
class FacetAutomationViewModelTest {

    private val dispatcher = StandardTestDispatcher()

    @Before fun setUp() = Dispatchers.setMain(dispatcher)

    @After fun tearDown() = Dispatchers.resetMain()

    private val bluetoothRule = AutomationRule(5L, 1L, AutomationTrigger.Bluetooth("AA:BB", "Car"))

    private class Fixture(rules: List<AutomationRule>, var bluetoothGranted: Boolean = false) {
        val ruleRepository = mock(AutomationRuleRepository::class.java).also { `when`(it.observeRules()).thenReturn(flowOf(rules)) }
        private val stateRepository = mock(AutomationStateRepository::class.java).also { `when`(it.state).thenReturn(flowOf(AutomationState())) }
        private val facetRepository = mock(FacetRepository::class.java).also {
            `when`(it.observeFacets()).thenReturn(flowOf(listOf(FacetEntity(id = 1, name = "Work", position = 0))))
        }
        private val settingsRepository = mock(SettingsRepository::class.java).also {
            `when`(it.settings).thenReturn(flowOf(LauncherSettings(activeFacetId = 1L)))
        }
        private val permissionRepository = mock(AutomationPermissionRepository::class.java).also {
            `when`(it.isGranted(AutomationPermission.BLUETOOTH_CONNECT)).thenAnswer { bluetoothGranted }
        }
        val viewModel = FacetAutomationViewModel(
            ObserveFacetAutomationUseCase(ruleRepository, stateRepository, facetRepository, settingsRepository, permissionRepository),
            ruleRepository,
        )
    }

    @Test
    fun `the state is null until the first emission`() = runTest {
        val fixture = Fixture(listOf(bluetoothRule))

        assertNull(fixture.viewModel.uiState.value)
    }

    @Test
    fun `the state carries the rows and the facet names`() = runTest {
        val fixture = Fixture(listOf(bluetoothRule))
        backgroundScope.launch { fixture.viewModel.uiState.collect {} }
        dispatcher.scheduler.advanceUntilIdle()

        val item = fixture.viewModel.uiState.value!!.items.single()
        assertEquals(5L, item.rule.id)
        assertEquals("Work", item.targetFacetName)
    }

    @Test
    fun `a refresh re-reads the permission grants`() = runTest {
        val fixture = Fixture(listOf(bluetoothRule), bluetoothGranted = false)
        backgroundScope.launch { fixture.viewModel.uiState.collect {} }
        dispatcher.scheduler.advanceUntilIdle()
        assertEquals(RuleAvailability.NEEDS_BLUETOOTH, fixture.viewModel.uiState.value!!.items.single().availability)

        // When the user grants the permission elsewhere and the screen resumes
        fixture.bluetoothGranted = true
        fixture.viewModel.refresh()
        dispatcher.scheduler.advanceUntilIdle()

        // Then the row is no longer flagged
        assertEquals(RuleAvailability.AVAILABLE, fixture.viewModel.uiState.value!!.items.single().availability)
    }

    @Test
    fun `toggling a rule writes the enabled flag`() = runTest {
        val fixture = Fixture(listOf(bluetoothRule))

        fixture.viewModel.setEnabled(5L, false)
        dispatcher.scheduler.advanceUntilIdle()

        verify(fixture.ruleRepository).setEnabled(5L, false)
    }
}
