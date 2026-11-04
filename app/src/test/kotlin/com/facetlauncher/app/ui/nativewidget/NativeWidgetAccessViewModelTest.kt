package com.facetlauncher.app.ui.nativewidget

import com.facetlauncher.app.data.CalendarPermissionRepository
import com.facetlauncher.app.data.LocationRepository
import com.facetlauncher.app.data.SettingsRepository
import com.facetlauncher.app.data.model.LauncherSettings
import com.facetlauncher.app.data.model.WeatherLocationMode
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito.mock
import org.mockito.Mockito.`when`

@OptIn(ExperimentalCoroutinesApi::class)
class NativeWidgetAccessViewModelTest {

    private val calendar = mock(CalendarPermissionRepository::class.java)
    private val location = mock(LocationRepository::class.java)

    @Before
    fun setUp() = Dispatchers.setMain(UnconfinedTestDispatcher())

    @After
    fun tearDown() = Dispatchers.resetMain()

    private fun viewModel(mode: WeatherLocationMode): NativeWidgetAccessViewModel {
        val settings = mock(SettingsRepository::class.java)
        `when`(settings.settings).thenReturn(flowOf(LauncherSettings(weatherLocationMode = mode)))
        return NativeWidgetAccessViewModel(settings, calendar, location)
    }

    @Test
    fun `missing calendar access is reported`() = runTest {
        // Given no calendar permission
        `when`(calendar.isGranted()).thenReturn(false)
        `when`(location.isPermissionGranted()).thenReturn(true)

        // Then the widgets are told the calendar is missing
        assertEquals(NativeWidgetAccess(calendarMissing = true), viewModel(WeatherLocationMode.CURRENT).access.first())
    }

    @Test
    fun `missing location access matters only while the weather follows the position`() = runTest {
        // Given no location permission
        `when`(calendar.isGranted()).thenReturn(true)
        `when`(location.isPermissionGranted()).thenReturn(false)

        // Then it is missing for current location, and not for a chosen city
        assertEquals(NativeWidgetAccess(locationMissing = true), viewModel(WeatherLocationMode.CURRENT).access.first())
        assertEquals(NativeWidgetAccess(), viewModel(WeatherLocationMode.CITY).access.first())
    }
}
