package com.facetlauncher.app.domain

import com.facetlauncher.app.data.CalendarPermissionRepository
import com.facetlauncher.app.data.CalendarRepository
import com.facetlauncher.app.data.LocationRepository
import com.facetlauncher.app.data.SettingsRepository
import com.facetlauncher.app.data.WidgetPlacementRepository
import com.facetlauncher.app.data.local.WidgetPlacementEntity
import com.facetlauncher.app.data.model.CalendarEvent
import com.facetlauncher.app.data.model.LauncherSettings
import com.facetlauncher.app.data.model.NativeWidgetConfig
import com.facetlauncher.app.data.model.NativeWidgetType
import com.facetlauncher.app.data.model.WeatherCity
import com.facetlauncher.app.data.model.WeatherHour
import com.facetlauncher.app.data.model.WeatherLocationMode
import com.facetlauncher.app.data.model.WeatherPlace
import com.facetlauncher.app.data.model.WidgetWeatherLocation
import com.facetlauncher.app.data.model.encode
import com.facetlauncher.app.data.weather.WeatherFetchState
import com.facetlauncher.app.data.weather.WeatherRepository
import com.facetlauncher.app.data.weather.WidgetForecast
import com.facetlauncher.app.data.weather.WidgetWeatherRepository
import java.time.Clock
import java.time.Instant
import java.time.ZoneOffset
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import org.mockito.Mockito.mock
import org.mockito.Mockito.never
import org.mockito.Mockito.times
import org.mockito.Mockito.verify
import org.mockito.Mockito.`when`

class ObserveNativeWidgetOverridesUseCaseTest {

    private val now = Instant.parse("2026-11-04T08:00:00Z")
    private val oslo = WeatherCity("Oslo", 59.91, 10.75, "Europe/Oslo")
    private val osloPlace = WeatherPlace.rounded(oslo.latitude, oslo.longitude)
    private val hours = listOf(WeatherHour(now, 4.0, null, null, null, null, null, 0.0, "clearsky_day"))

    private val placements = mock(WidgetPlacementRepository::class.java)
    private val settings = mock(SettingsRepository::class.java)
    private val calendars = mock(CalendarRepository::class.java)
    private val calendarPermission = mock(CalendarPermissionRepository::class.java)
    private val widgetWeather = mock(WidgetWeatherRepository::class.java)
    private val launcherWeather = mock(WeatherRepository::class.java)
    private val location = mock(LocationRepository::class.java)
    private val resolvePlace = mock(ResolvePlaceUseCase::class.java)

    private fun useCase() = ObserveNativeWidgetOverridesUseCase(
        placements, settings, calendars, calendarPermission, widgetWeather, launcherWeather, location, resolvePlace,
        object : Clock() {
            override fun instant(): Instant = now
            override fun getZone() = ZoneOffset.UTC
            override fun withZone(zone: java.time.ZoneId?): Clock = this
        },
    )

    private fun native(id: Int, type: NativeWidgetType, config: NativeWidgetConfig) = WidgetPlacementEntity(
        appWidgetId = id, providerPackageName = "", providerClassName = "", row = 0, col = 0, colSpan = 4, rowSpan = 2,
        nativeWidgetType = type, nativeWidgetConfig = config.encode(),
    )

    private fun given(vararg widgets: WidgetPlacementEntity, launcher: LauncherSettings = LauncherSettings()) {
        `when`(placements.observeAll()).thenReturn(flowOf(widgets.toList()))
        `when`(settings.settings).thenReturn(flowOf(launcher))
        `when`(launcherWeather.observeState()).thenReturn(flowOf(WeatherFetchState()))
        `when`(launcherWeather.observeHours()).thenReturn(flowOf(emptyList()))
    }

    @Test
    fun `an agenda widget with its own calendars reads only those`() = runTest {
        // Given an agenda widget set to calendar 5 and another that follows the launcher
        val event = CalendarEvent(1, "5", "Standup", 0, 1, false)
        given(native(-1, NativeWidgetType.AGENDA, NativeWidgetConfig(calendarIds = setOf("5"))), native(-2, NativeWidgetType.AGENDA, NativeWidgetConfig()))
        `when`(calendarPermission.isGranted()).thenReturn(true)
        `when`(calendars.getUpcomingEvents(setOf("5"), true)).thenReturn(listOf(event))

        // When observing
        val overrides = useCase()().first()

        // Then only the first has an override, with its own events
        assertEquals(listOf(event), overrides[-1]?.events)
        assertNull(overrides[-2])
    }

    @Test
    fun `an agenda widget without calendar access shows no events`() = runTest {
        // Given a widget with its own calendars but no permission
        given(native(-1, NativeWidgetType.AGENDA, NativeWidgetConfig(calendarIds = setOf("5"))))
        `when`(calendarPermission.isGranted()).thenReturn(false)

        // Then it shows an empty list rather than the launcher's
        assertEquals(emptyList<CalendarEvent>(), useCase()().first()[-1]?.events)
    }

    @Test
    fun `a weather widget set to the launcher's own city makes no request`() = runTest {
        // Given the launcher already holds Oslo's forecast
        given(native(-1, NativeWidgetType.WEATHER, NativeWidgetConfig(weatherLocation = WidgetWeatherLocation.City(oslo))))
        `when`(launcherWeather.observeState()).thenReturn(flowOf(WeatherFetchState(place = osloPlace, fetchedAt = now)))
        `when`(launcherWeather.observeHours()).thenReturn(flowOf(hours))

        // When observing
        val override = useCase()().first()[-1]

        // Then the widget gets Oslo's weather without a request of its own
        assertEquals("Oslo", override?.weather?.placeName)
        verify(widgetWeather, never()).refresh(osloPlace)
    }

    @Test
    fun `weather widgets on one other city share one forecast`() = runTest {
        // Given two widgets set to Oslo while the launcher is elsewhere
        val config = NativeWidgetConfig(weatherLocation = WidgetWeatherLocation.City(oslo))
        given(native(-1, NativeWidgetType.WEATHER, config), native(-2, NativeWidgetType.WEATHER, config))
        `when`(widgetWeather.refresh(osloPlace)).thenReturn(WidgetForecast(hours, WeatherFetchState(place = osloPlace, fetchedAt = now)))

        // When observing
        val overrides = useCase()().first()

        // Then both show Oslo, asking the one shared holder for the same place
        assertEquals("Oslo", overrides[-1]?.weather?.placeName)
        assertEquals("Oslo", overrides[-2]?.weather?.placeName)
        verify(widgetWeather, times(2)).refresh(osloPlace)
    }

    @Test
    fun `a widget set to current location follows the launcher when the launcher already does`() = runTest {
        // Given the launcher follows the position and a widget is set to current location
        given(
            native(-1, NativeWidgetType.WEATHER, NativeWidgetConfig(weatherLocation = WidgetWeatherLocation.Current)),
            launcher = LauncherSettings(weatherLocationMode = WeatherLocationMode.CURRENT),
        )

        // Then it has no override of its own
        assertNull(useCase()().first()[-1])
    }

    @Test
    fun `a widget set to current location without permission is marked unavailable`() = runTest {
        // Given the launcher follows a city, a widget wants the position, and there is no permission
        given(
            native(-1, NativeWidgetType.WEATHER, NativeWidgetConfig(weatherLocation = WidgetWeatherLocation.Current)),
            launcher = LauncherSettings(weatherLocationMode = WeatherLocationMode.CITY, weatherCity = oslo),
        )
        `when`(location.isPermissionGranted()).thenReturn(false)

        // Then it shows no forecast instead of the launcher's city
        val override = useCase()().first()[-1]
        assertEquals(true, override?.weatherUnavailable)
        assertEquals(true, override?.locationMissing)
        assertNull(override?.weather)
    }
}
