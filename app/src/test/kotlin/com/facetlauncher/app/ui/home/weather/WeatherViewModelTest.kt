package com.facetlauncher.app.ui.home.weather

import com.facetlauncher.app.data.SettingsRepository
import com.facetlauncher.app.data.model.LauncherSettings
import com.facetlauncher.app.data.model.PressureUnit
import com.facetlauncher.app.data.model.RainUnit
import com.facetlauncher.app.data.model.TemperatureUnit
import com.facetlauncher.app.data.model.WindSpeedUnit
import com.facetlauncher.app.data.weather.RefreshOutcome
import com.facetlauncher.app.domain.ObserveWeatherUseCase
import com.facetlauncher.app.domain.RefreshWeatherUseCase
import com.facetlauncher.app.domain.WeatherRefreshResult
import java.time.Instant
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito.mock
import org.mockito.Mockito.never
import org.mockito.Mockito.times
import org.mockito.Mockito.verify
import org.mockito.Mockito.`when`

@OptIn(ExperimentalCoroutinesApi::class)
class WeatherViewModelTest {

    private val dispatcher = StandardTestDispatcher()
    private val observeWeather = mock(ObserveWeatherUseCase::class.java)
    private val settingsRepository = mock(SettingsRepository::class.java)
    private val refreshWeather = mock(RefreshWeatherUseCase::class.java)

    @Before
    fun setUp() {
        Dispatchers.setMain(dispatcher)
        `when`(observeWeather.invoke()).thenReturn(flowOf(null))
        `when`(settingsRepository.settings).thenReturn(flowOf(LauncherSettings()))
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun viewModel() = WeatherViewModel(observeWeather, settingsRepository, refreshWeather)

    /** The state is combined and shared only while someone collects it, as the Home route does. */
    private fun TestScope.collecting(viewModel: WeatherViewModel) {
        backgroundScope.launch { viewModel.uiState.collect { } }
    }

    @Test
    fun `resuming Home with weather off refreshes nothing`() = runTest {
        val viewModel = viewModel()

        viewModel.onHomeResumed(weatherShown = false)
        advanceUntilIdle()

        verify(refreshWeather, never()).invoke()
    }

    @Test
    fun `resuming Home with weather shown refreshes quietly`() = runTest {
        `when`(refreshWeather.invoke()).thenReturn(WeatherRefreshResult.Done(RefreshOutcome.UpToDate))
        val viewModel = viewModel()
        collecting(viewModel)

        viewModel.onHomeResumed(weatherShown = true)
        advanceUntilIdle()

        verify(refreshWeather).invoke()
        assertNull(viewModel.uiState.value.notice)
    }

    @Test
    fun `a tap on refresh reports what happened and stops refreshing`() = runTest {
        `when`(refreshWeather.invoke()).thenReturn(WeatherRefreshResult.Done(RefreshOutcome.UpToDate))
        val viewModel = viewModel()
        collecting(viewModel)

        viewModel.refresh()
        advanceUntilIdle()

        assertEquals(WeatherNotice.UP_TO_DATE, viewModel.uiState.value.notice)
        assertFalse(viewModel.uiState.value.isRefreshing)
    }

    @Test
    fun `a second tap before the first refresh has finished is ignored`() = runTest {
        `when`(refreshWeather.invoke()).thenReturn(WeatherRefreshResult.Done(RefreshOutcome.Updated))
        val viewModel = viewModel()

        viewModel.refresh()
        viewModel.refresh()
        advanceUntilIdle()

        verify(refreshWeather, times(1)).invoke()
    }

    @Test
    fun `units resolve against the locale and an explicit choice wins`() = runTest {
        `when`(settingsRepository.settings).thenReturn(
            flowOf(LauncherSettings(temperatureUnit = TemperatureUnit.FAHRENHEIT, windSpeedUnit = WindSpeedUnit.MS, rainUnit = RainUnit.INCH, pressureUnit = PressureUnit.MMHG)),
        )
        val viewModel = viewModel()
        collecting(viewModel)
        advanceUntilIdle()

        val units = viewModel.uiState.value.units

        assertEquals(
            listOf(TemperatureUnit.FAHRENHEIT, WindSpeedUnit.MS, RainUnit.INCH, PressureUnit.MMHG),
            listOf(units.temperature, units.wind, units.rain, units.pressure),
        )
    }

    @Test
    fun `a user tap hears every result`() {
        assertEquals(WeatherNotice.UP_TO_DATE, WeatherViewModel.noticeFor(WeatherRefreshResult.Done(RefreshOutcome.UpToDate), userInitiated = true))
        assertEquals(WeatherNotice.UPDATED, WeatherViewModel.noticeFor(WeatherRefreshResult.Done(RefreshOutcome.Updated), userInitiated = true))
        assertEquals(WeatherNotice.TOO_SOON, WeatherViewModel.noticeFor(WeatherRefreshResult.Done(RefreshOutcome.TooSoon(Instant.EPOCH)), userInitiated = true))
    }

    @Test
    fun `a quiet refresh stays silent unless something is wrong or needs the user`() {
        assertNull(WeatherViewModel.noticeFor(WeatherRefreshResult.Done(RefreshOutcome.UpToDate), userInitiated = false))
        assertNull(WeatherViewModel.noticeFor(WeatherRefreshResult.Done(RefreshOutcome.Updated), userInitiated = false))
        assertNull(WeatherViewModel.noticeFor(WeatherRefreshResult.Done(RefreshOutcome.TooSoon(Instant.EPOCH)), userInitiated = false))
        assertEquals(WeatherNotice.BUSY, WeatherViewModel.noticeFor(WeatherRefreshResult.Done(RefreshOutcome.Busy(Instant.EPOCH)), userInitiated = false))
        assertEquals(WeatherNotice.OFFLINE, WeatherViewModel.noticeFor(WeatherRefreshResult.Done(RefreshOutcome.Offline), userInitiated = false))
        assertEquals(WeatherNotice.FAILED, WeatherViewModel.noticeFor(WeatherRefreshResult.Done(RefreshOutcome.Failed), userInitiated = false))
        assertEquals(WeatherNotice.NEEDS_LOCATION_PERMISSION, WeatherViewModel.noticeFor(WeatherRefreshResult.NeedsLocationPermission, userInitiated = false))
        assertEquals(WeatherNotice.NO_LOCATION, WeatherViewModel.noticeFor(WeatherRefreshResult.NoLocation, userInitiated = false))
        assertEquals(WeatherNotice.NO_CITY, WeatherViewModel.noticeFor(WeatherRefreshResult.NoCity, userInitiated = false))
    }
}
