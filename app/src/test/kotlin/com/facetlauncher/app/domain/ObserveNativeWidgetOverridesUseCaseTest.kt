package com.facetlauncher.app.domain

import com.facetlauncher.app.data.CalendarPermissionRepository
import com.facetlauncher.app.data.ClockStackRepository
import com.facetlauncher.app.data.CalendarRepository
import com.facetlauncher.app.data.DeviceLocation
import com.facetlauncher.app.data.LocationRepository
import com.facetlauncher.app.data.SettingsRepository
import com.facetlauncher.app.data.WidgetPlacementRepository
import com.facetlauncher.app.data.local.WidgetPlacementEntity
import com.facetlauncher.app.data.model.CalendarEvent
import com.facetlauncher.app.data.model.GlassStyle
import com.facetlauncher.app.data.model.LauncherSettings
import com.facetlauncher.app.data.model.ClockStackItem
import com.facetlauncher.app.data.model.ClockStackItemKind
import com.facetlauncher.app.data.model.NativeWidgetConfig
import com.facetlauncher.app.data.model.NativeWidgetRef
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
    private val clockStack = mock(ClockStackRepository::class.java)
    private val calendars = mock(CalendarRepository::class.java)
    private val calendarPermission = mock(CalendarPermissionRepository::class.java)
    private val widgetWeather = mock(WidgetWeatherRepository::class.java)
    private val launcherWeather = mock(WeatherRepository::class.java)
    private val location = mock(LocationRepository::class.java)
    private val resolvePlace = mock(ResolvePlaceUseCase::class.java)

    private fun useCase() = ObserveNativeWidgetOverridesUseCase(
        placements, clockStack, settings,
        NativeWidgetOverrideResolver(
            calendars, calendarPermission, widgetWeather, launcherWeather, location, resolvePlace,
            object : Clock() {
                override fun instant(): Instant = now
                override fun getZone() = ZoneOffset.UTC
                override fun withZone(zone: java.time.ZoneId?): Clock = this
            },
        ),
    )

    private fun native(id: Int, type: NativeWidgetType, config: NativeWidgetConfig) = WidgetPlacementEntity(
        appWidgetId = id, providerPackageName = "", providerClassName = "", row = 0, col = 0, colSpan = 4, rowSpan = 2,
        nativeWidgetType = type, nativeWidgetConfig = config.encode(),
    )

    private fun given(vararg widgets: WidgetPlacementEntity, launcher: LauncherSettings = LauncherSettings()) {
        `when`(placements.observeAll()).thenReturn(flowOf(widgets.toList()))
        `when`(settings.settings).thenReturn(flowOf(launcher))
        `when`(clockStack.observe(launcher.activeFacetId)).thenReturn(flowOf(emptyList()))
        `when`(launcherWeather.observeState()).thenReturn(flowOf(WeatherFetchState()))
        `when`(launcherWeather.observeHours()).thenReturn(flowOf(emptyList()))
    }

    @Test
    fun `an agenda widget with its own calendars reads only those, and a new one reads every calendar`() = runTest {
        // Given an agenda widget set to calendar 5 and a new one with nothing chosen
        val event = CalendarEvent(1, "5", "Standup", 0, 1, false)
        val everything = CalendarEvent(2, "9", "Lunch", 0, 1, false)
        given(native(-1, NativeWidgetType.AGENDA, NativeWidgetConfig(calendarIds = setOf("5"))), native(-2, NativeWidgetType.AGENDA, NativeWidgetConfig()))
        `when`(calendarPermission.isGranted()).thenReturn(true)
        `when`(calendars.getAgendaEvents(setOf("5"), true)).thenReturn(listOf(event))
        `when`(calendars.getAgendaEvents(null, true)).thenReturn(listOf(event, everything))

        // When observing
        val overrides = useCase()().first()

        // Then the first reads its own calendar, and the new one every calendar
        assertEquals(listOf(event), overrides[NativeWidgetRef.Hub(-1)]?.events)
        assertEquals(listOf(event, everything), overrides[NativeWidgetRef.Hub(-2)]?.events)
    }

    @Test
    fun `a widget with only a background gets an override carrying it`() = runTest {
        // Given a Now playing widget and an agenda that each chose a background, and a Now playing that did not
        given(
            native(-1, NativeWidgetType.NOW_PLAYING, NativeWidgetConfig(glass = GlassStyle.BRIGHT)),
            native(-2, NativeWidgetType.AGENDA, NativeWidgetConfig(glass = GlassStyle.OFF)),
            native(-3, NativeWidgetType.NOW_PLAYING, NativeWidgetConfig()),
        )
        `when`(calendarPermission.isGranted()).thenReturn(true)

        // When observing
        val overrides = useCase()().first()

        // Then the choices are carried, and the widget that chose nothing has no entry
        assertEquals(GlassStyle.BRIGHT, overrides[NativeWidgetRef.Hub(-1)]?.glass)
        assertEquals(GlassStyle.OFF, overrides[NativeWidgetRef.Hub(-2)]?.glass)
        assertNull(overrides[NativeWidgetRef.Hub(-3)])
    }

    @Test
    fun `an agenda with only its own all-day choice still reads every calendar`() = runTest {
        // Given an agenda that hides all-day events and picks no calendars
        val event = CalendarEvent(1, "2", "Standup", 0, 1, false)
        given(native(-1, NativeWidgetType.AGENDA, NativeWidgetConfig(showAllDayEvents = false)))
        `when`(calendarPermission.isGranted()).thenReturn(true)
        `when`(calendars.getAgendaEvents(null, false)).thenReturn(listOf(event))

        // Then it reads every calendar with its own all-day choice
        assertEquals(listOf(event), useCase()().first()[NativeWidgetRef.Hub(-1)]?.events)
    }

    @Test
    fun `a Home page with its own calendars is resolved like a Hub widget`() = runTest {
        // Given an agenda page on the active facet's clock stack set to calendar 5
        val event = CalendarEvent(1, "5", "Standup", 0, 1, false)
        given()
        val page = ClockStackItem(
            id = 7, facetId = LauncherSettings().activeFacetId, position = 0, kind = ClockStackItemKind.NATIVE_WIDGET,
            nativeWidgetType = NativeWidgetType.AGENDA, nativeWidgetConfig = NativeWidgetConfig(calendarIds = setOf("5")).encode(),
        )
        `when`(clockStack.observe(LauncherSettings().activeFacetId)).thenReturn(flowOf(listOf(page)))
        `when`(calendarPermission.isGranted()).thenReturn(true)
        `when`(calendars.getAgendaEvents(setOf("5"), true)).thenReturn(listOf(event))

        // When observing
        val overrides = useCase()().first()

        // Then the page gets its own events, keyed as a Home widget
        assertEquals(listOf(event), overrides[NativeWidgetRef.Home(7)]?.events)
    }

    @Test
    fun `an agenda widget without calendar access shows no events`() = runTest {
        // Given a widget with its own calendars but no permission
        given(native(-1, NativeWidgetType.AGENDA, NativeWidgetConfig(calendarIds = setOf("5"))))
        `when`(calendarPermission.isGranted()).thenReturn(false)

        // Then it shows an empty list rather than the launcher's
        assertEquals(emptyList<CalendarEvent>(), useCase()().first()[NativeWidgetRef.Hub(-1)]?.events)
    }

    @Test
    fun `a weather widget set to the launcher's own city makes no request`() = runTest {
        // Given the launcher already holds Oslo's forecast
        given(native(-1, NativeWidgetType.WEATHER, NativeWidgetConfig(weatherLocation = WidgetWeatherLocation.City(oslo))))
        `when`(launcherWeather.observeState()).thenReturn(flowOf(WeatherFetchState(place = osloPlace, fetchedAt = now)))
        `when`(launcherWeather.observeHours()).thenReturn(flowOf(hours))

        // When observing
        val override = useCase()().first()[NativeWidgetRef.Hub(-1)]

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
        assertEquals("Oslo", overrides[NativeWidgetRef.Hub(-1)]?.weather?.placeName)
        assertEquals("Oslo", overrides[NativeWidgetRef.Hub(-2)]?.weather?.placeName)
        verify(widgetWeather, times(2)).refresh(osloPlace)
    }

    @Test
    fun `a widget set to current location follows the launcher when its weather is on and already follows the position`() = runTest {
        // Given the launcher follows the position and a widget is set to current location
        given(
            native(-1, NativeWidgetType.WEATHER, NativeWidgetConfig(weatherLocation = WidgetWeatherLocation.Current)),
            launcher = LauncherSettings(showWeather = true, weatherLocationMode = WeatherLocationMode.CURRENT),
        )

        // Then it has no override of its own
        assertNull(useCase()().first()[NativeWidgetRef.Hub(-1)])
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
        val override = useCase()().first()[NativeWidgetRef.Hub(-1)]
        assertEquals(true, override?.weatherUnavailable)
        assertEquals(true, override?.locationMissing)
        assertNull(override?.weather)
    }

    private val fix = DeviceLocation(oslo.latitude, oslo.longitude, null)

    @Test
    fun `a weather widget fetches for the position when the launcher's weather is off and access is granted`() = runTest {
        // Given the launcher's Weather off, a widget following the launcher, and location access
        given(native(-1, NativeWidgetType.WEATHER, NativeWidgetConfig()), launcher = LauncherSettings(showWeather = false, weatherLocationMode = WeatherLocationMode.CURRENT))
        `when`(location.isPermissionGranted()).thenReturn(true)
        `when`(location.recentLocation(RefreshWeatherUseCase.RECENT_FIX_MS)).thenReturn(fix)
        `when`(widgetWeather.refresh(osloPlace)).thenReturn(WidgetForecast(hours, WeatherFetchState(place = osloPlace, fetchedAt = now, placeName = "Oslo")))

        // When observing
        val override = useCase()().first()[NativeWidgetRef.Hub(-1)]

        // Then it shows its own forecast anyway
        assertEquals("Oslo", override?.weather?.placeName)
        verify(widgetWeather).refresh(osloPlace)
    }

    @Test
    fun `a weather widget set to current location also fetches when the launcher's weather is off`() = runTest {
        // Given the launcher's Weather off but pointing at the position, and a widget set to current location
        given(
            native(-1, NativeWidgetType.WEATHER, NativeWidgetConfig(weatherLocation = WidgetWeatherLocation.Current)),
            launcher = LauncherSettings(showWeather = false, weatherLocationMode = WeatherLocationMode.CURRENT),
        )
        `when`(location.isPermissionGranted()).thenReturn(true)
        `when`(location.recentLocation(RefreshWeatherUseCase.RECENT_FIX_MS)).thenReturn(fix)
        `when`(widgetWeather.refresh(osloPlace)).thenReturn(WidgetForecast(hours, WeatherFetchState(place = osloPlace, fetchedAt = now, placeName = "Oslo")))

        // Then it has its own forecast rather than waiting for the launcher's
        assertEquals("Oslo", useCase()().first()[NativeWidgetRef.Hub(-1)]?.weather?.placeName)
    }

    @Test
    fun `a weather widget needing the position without access says the location is missing`() = runTest {
        // Given the launcher's Weather off, a widget following it, and no location access
        given(native(-1, NativeWidgetType.WEATHER, NativeWidgetConfig()), launcher = LauncherSettings(showWeather = false, weatherLocationMode = WeatherLocationMode.CURRENT))
        `when`(location.isPermissionGranted()).thenReturn(false)

        // Then it is unavailable, flagged so the widget offers to allow location, and nothing is fetched
        val override = useCase()().first()[NativeWidgetRef.Hub(-1)]
        assertEquals(true, override?.weatherUnavailable)
        assertEquals(true, override?.locationMissing)
        verify(widgetWeather, never()).refresh(osloPlace)
    }

    @Test
    fun `a weather widget following the launcher's city fetches it while the launcher's weather is off`() = runTest {
        // Given the launcher's Weather off but set to Oslo, and a widget following it
        given(native(-1, NativeWidgetType.WEATHER, NativeWidgetConfig()), launcher = LauncherSettings(showWeather = false, weatherLocationMode = WeatherLocationMode.CITY, weatherCity = oslo))
        `when`(widgetWeather.refresh(osloPlace)).thenReturn(WidgetForecast(hours, WeatherFetchState(place = osloPlace, fetchedAt = now)))

        // Then it shows Oslo without needing any permission
        assertEquals("Oslo", useCase()().first()[NativeWidgetRef.Hub(-1)]?.weather?.placeName)
    }

    @Test
    fun `a weather widget with the launcher on a city that was never chosen is unavailable without asking for location`() = runTest {
        // Given the launcher's Weather off, set to a city that was never chosen
        given(native(-1, NativeWidgetType.WEATHER, NativeWidgetConfig()), launcher = LauncherSettings(showWeather = false, weatherLocationMode = WeatherLocationMode.CITY, weatherCity = null))

        // Then it is unavailable, and it is not a location-permission problem
        val override = useCase()().first()[NativeWidgetRef.Hub(-1)]
        assertEquals(true, override?.weatherUnavailable)
        assertEquals(false, override?.locationMissing)
    }
}
