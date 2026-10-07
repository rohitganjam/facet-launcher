package com.facetlauncher.app.domain

import com.facetlauncher.app.data.AutomationRuleRepository
import com.facetlauncher.app.data.DeviceStateRepository
import com.facetlauncher.app.data.SettingsRepository
import com.facetlauncher.app.data.WakeEventsRepository
import com.facetlauncher.app.data.model.AutomationRule
import com.facetlauncher.app.data.model.AutomationTrigger
import com.facetlauncher.app.data.model.DeviceState
import com.facetlauncher.app.data.model.LauncherSettings
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.mockito.Mockito.mock
import org.mockito.Mockito.mockingDetails
import org.mockito.Mockito.`when`

@OptIn(ExperimentalCoroutinesApi::class)
class RunFacetAutomationUseCaseTest {

    private class Fixture {
        val apply: ApplyFacetAutomationUseCase = mock(ApplyFacetAutomationUseCase::class.java)
        val wake = MutableSharedFlow<Unit>(extraBufferCapacity = 8)
        val homePresses = MutableSharedFlow<Unit>(extraBufferCapacity = 8)
        val rules = MutableStateFlow<List<AutomationRule>>(emptyList())
        val settings = MutableStateFlow(LauncherSettings(activeFacetId = 1L))
        val deviceState = MutableStateFlow(DeviceState())

        private val ruleRepository = mock(AutomationRuleRepository::class.java).also { `when`(it.observeRules()).thenReturn(rules) }
        private val wakeEvents = mock(WakeEventsRepository::class.java).also { `when`(it.observeWakeEvents()).thenReturn(wake) }
        private val settingsRepository = mock(SettingsRepository::class.java).also { `when`(it.settings).thenReturn(settings) }
        private val deviceStateRepository = mock(DeviceStateRepository::class.java).also { `when`(it.deviceState).thenReturn(deviceState) }
        val useCase = RunFacetAutomationUseCase(apply, ruleRepository, wakeEvents, settingsRepository, deviceStateRepository)

        fun passes(): Int = mockingDetails(apply).invocations.count { it.method.name == "invoke" }
    }

    private val rule = AutomationRule(1L, 1L, AutomationTrigger.Headphones())

    // runCurrent, not advanceUntilIdle: the latter ignores work launched in backgroundScope.
    private fun TestScope.start(fixture: Fixture) {
        backgroundScope.launch { fixture.useCase(fixture.homePresses) }
        runCurrent()
    }

    @Test
    fun `a pass runs as soon as it starts`() = runTest {
        val fixture = Fixture()

        start(fixture)

        assertTrue("expected at least one pass at start, got ${fixture.passes()}", fixture.passes() >= 1)
    }

    @Test
    fun `a wake event triggers a pass`() = runTest {
        val fixture = Fixture()
        start(fixture)
        val before = fixture.passes()

        fixture.wake.tryEmit(Unit)
        runCurrent()

        assertEquals(before + 1, fixture.passes())
    }

    @Test
    fun `a Home press triggers a pass`() = runTest {
        val fixture = Fixture()
        start(fixture)
        val before = fixture.passes()

        fixture.homePresses.tryEmit(Unit)
        runCurrent()

        assertEquals(before + 1, fixture.passes())
    }

    @Test
    fun `a rule change triggers a pass`() = runTest {
        val fixture = Fixture()
        start(fixture)
        val before = fixture.passes()

        fixture.rules.value = listOf(rule)
        runCurrent()

        assertEquals(before + 1, fixture.passes())
    }

    @Test
    fun `a change of the active facet triggers a pass`() = runTest {
        val fixture = Fixture()
        start(fixture)
        val before = fixture.passes()

        fixture.settings.value = LauncherSettings(activeFacetId = 2L)
        runCurrent()

        assertEquals(before + 1, fixture.passes())
    }

    @Test
    fun `a change in the device state triggers a pass`() = runTest {
        val fixture = Fixture()
        start(fixture)
        val before = fixture.passes()

        fixture.deviceState.value = DeviceState(headphonesPluggedIn = true)
        runCurrent()

        assertEquals(before + 1, fixture.passes())
    }

    @Test
    fun `an unrelated settings change does not trigger a pass`() = runTest {
        val fixture = Fixture()
        start(fixture)
        val before = fixture.passes()

        fixture.settings.value = LauncherSettings(activeFacetId = 1L, use24HourTime = true)
        runCurrent()

        assertEquals(before, fixture.passes())
    }

    @Test
    fun `a burst of five events causes at least one and at most five passes`() = runTest {
        val fixture = Fixture()
        start(fixture)
        val before = fixture.passes()

        repeat(5) { fixture.wake.tryEmit(Unit) }
        runCurrent()

        val burst = fixture.passes() - before
        assertTrue("expected between one and five passes, but $burst ran", burst in 1..5)
    }
}
