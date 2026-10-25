package com.facetlauncher.app.ui.settings

import com.facetlauncher.app.data.LocationRepository
import com.facetlauncher.app.data.SettingsRepository
import com.facetlauncher.app.data.model.LauncherSettings
import com.facetlauncher.app.data.model.PressureUnit
import com.facetlauncher.app.data.model.RainUnit
import com.facetlauncher.app.data.model.TemperatureUnit
import com.facetlauncher.app.data.model.WindSpeedUnit
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
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify
import org.mockito.Mockito.`when`

@OptIn(ExperimentalCoroutinesApi::class)
class UnitsAndLocationViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private val settingsRepository = mock(SettingsRepository::class.java)
    private val locationRepository = mock(LocationRepository::class.java)

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        `when`(settingsRepository.settings).thenReturn(flowOf(LauncherSettings(temperatureUnit = TemperatureUnit.FAHRENHEIT)))
    }

    @After
    fun tearDown() { Dispatchers.resetMain() }

    private fun createViewModel(granted: Boolean = false): UnitsAndLocationViewModel {
        `when`(locationRepository.isPermissionGranted()).thenReturn(granted)
        return UnitsAndLocationViewModel(settingsRepository, locationRepository)
    }

    @Test
    fun `state carries the launcher settings and the permission`() = runTest {
        val viewModel = createViewModel(granted = true)
        backgroundScope.launch(testDispatcher) { viewModel.uiState.collect {} }
        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.uiState.value

        assertEquals(TemperatureUnit.FAHRENHEIT, state.settings.temperatureUnit)
        assertTrue(state.locationPermissionGranted)
    }

    @Test
    fun `each unit setter writes to the settings repository`() = runTest {
        val viewModel = createViewModel()

        viewModel.setTemperatureUnit(TemperatureUnit.CELSIUS)
        viewModel.setWindSpeedUnit(WindSpeedUnit.MPH)
        viewModel.setRainUnit(RainUnit.INCH)
        viewModel.setPressureUnit(PressureUnit.INHG)
        testDispatcher.scheduler.advanceUntilIdle()

        verify(settingsRepository).setTemperatureUnit(TemperatureUnit.CELSIUS)
        verify(settingsRepository).setWindSpeedUnit(WindSpeedUnit.MPH)
        verify(settingsRepository).setRainUnit(RainUnit.INCH)
        verify(settingsRepository).setPressureUnit(PressureUnit.INHG)
    }

    @Test
    fun `refreshing picks up a permission granted elsewhere`() = runTest {
        val viewModel = createViewModel(granted = false)
        backgroundScope.launch(testDispatcher) { viewModel.uiState.collect {} }
        testDispatcher.scheduler.advanceUntilIdle()
        assertFalse(viewModel.uiState.value.locationPermissionGranted)

        `when`(locationRepository.isPermissionGranted()).thenReturn(true)
        viewModel.refreshPermission()
        testDispatcher.scheduler.advanceUntilIdle()

        assertTrue(viewModel.uiState.value.locationPermissionGranted)
    }
}
