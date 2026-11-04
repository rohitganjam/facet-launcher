package com.facetlauncher.app.ui.home.weather

import com.facetlauncher.app.data.SettingsRepository
import com.facetlauncher.app.data.model.LauncherSettings
import com.facetlauncher.app.data.model.PressureUnit
import com.facetlauncher.app.data.model.RainUnit
import com.facetlauncher.app.data.model.TemperatureUnit
import com.facetlauncher.app.data.model.WindSpeedUnit
import com.facetlauncher.app.data.weather.RefreshOutcome
import com.facetlauncher.app.domain.ClockWeather
import com.facetlauncher.app.domain.ObserveClockWeatherUseCase
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
    private val observeClockWeather = mock(ObserveClockWeatherUseCase::class.java)
    private val settingsRepository = mock(SettingsRepository::class.java)
    private val refreshWeather = mock(RefreshWeatherUseCase::class.java)

    @Before
    fun setUp() {
        Dispatchers.setMain(dispatcher)
        `when`(observeClockWeather.invoke()).thenReturn(flowOf(ClockWeather()))
        `when`(settingsRepository.settings).thenReturn(flowOf(LauncherSettings()))
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun viewModel() = WeatherViewModel(observeClockWeather, settingsRepository, refreshWeather)

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

    @Test
    fun `with a place of its own a refresh looks that place up and leaves the launcher's forecast alone`() = runTest {
        // Given the active facet shows a place of its own
        `when`(observeClockWeather.invoke()).thenReturn(flowOf(ClockWeather(own = true)))
        val viewModel = viewModel()
        collecting(viewModel)
        advanceUntilIdle()
        assertEquals(true, viewModel.uiState.value.ownLocation)

        // When Home resumes and the sheet's button is tapped
        viewModel.onHomeResumed(weatherShown = true)
        viewModel.refresh()
        advanceUntilIdle()

        // Then its own place is asked for again, and the launcher-wide refresh is not run for a forecast that is not on screen
        verify(observeClockWeather, times(2)).refreshOwn()
        verify(refreshWeather, never()).invoke()
    }

    @Test
    fun `a facet's own place without location access says so, and without a forecast says it failed`() {
        // Given a facet's own place that has no forecast
        val noPermission = ClockWeather(own = true, unavailable = true, locationMissing = true)
        val noForecast = ClockWeather(own = true, unavailable = true)

        // Then the sheet is told to ask for location in the first case, and that the update failed in the second
        assertEquals(WeatherNotice.NEEDS_LOCATION_PERMISSION, WeatherViewModel.ownNotice(noPermission))
        assertEquals(WeatherNotice.FAILED, WeatherViewModel.ownNotice(noForecast))

        // And nothing is said while there is a forecast, or when the launcher's own is on screen
        assertNull(WeatherViewModel.ownNotice(ClockWeather(own = true)))
        assertNull(WeatherViewModel.ownNotice(ClockWeather(unavailable = true)))
    }
}
