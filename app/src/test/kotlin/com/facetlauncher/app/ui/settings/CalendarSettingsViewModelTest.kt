package com.facetlauncher.app.ui.settings

import androidx.lifecycle.SavedStateHandle
import com.facetlauncher.app.domain.AssignCalendarColorsUseCase
import com.facetlauncher.app.data.CalendarPermissionRepository
import com.facetlauncher.app.data.CalendarRepository
import com.facetlauncher.app.data.FacetRepository
import com.facetlauncher.app.data.SettingsRepository
import com.facetlauncher.app.data.WallpaperRepository
import com.facetlauncher.app.data.model.Facet
import com.facetlauncher.app.domain.UpdateClockConfigUseCase
import com.facetlauncher.app.data.model.encode
import com.facetlauncher.app.data.model.NativeWidgetConfig
import com.facetlauncher.app.data.model.ClockWidgetConfig
import com.facetlauncher.app.data.model.ClockStackItemKind
import com.facetlauncher.app.data.model.ClockStackItem
import com.facetlauncher.app.data.model.ClockCalendarConfig
import com.facetlauncher.app.data.ClockStackRepository
import com.facetlauncher.app.data.model.CalendarDisplayOptions
import com.facetlauncher.app.data.model.CalendarEvent
import com.facetlauncher.app.data.model.CalendarInfo
import com.facetlauncher.app.data.model.HomeWallpaper
import com.facetlauncher.app.data.model.LauncherSettings
import com.facetlauncher.app.ui.theme.AccentSwatch
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
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
import org.mockito.ArgumentMatchers.any
import org.mockito.ArgumentMatchers.anyBoolean
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify
import org.mockito.Mockito.`when`

@OptIn(ExperimentalCoroutinesApi::class)
class CalendarSettingsViewModelTest {

    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setUp() { Dispatchers.setMain(testDispatcher) }

    @After
    fun tearDown() { Dispatchers.resetMain() }

    private val clockStackRepository = mock(ClockStackRepository::class.java)

    /** A facet with a clock page (id 9) holding [config]. */
    private fun facetWith(config: ClockWidgetConfig) = Facet(
        id = 5L, name = "Work", position = 0,
        clockStack = listOf(ClockStackItem(id = 9, facetId = 5, position = 0, kind = ClockStackItemKind.CLOCK, nativeWidgetConfig = NativeWidgetConfig(clock = config).encode())),
    )

    private fun stored(calendar: ClockCalendarConfig) = NativeWidgetConfig(clock = ClockWidgetConfig(calendar = calendar))

    private fun unavailableWallpaperRepository(): WallpaperRepository =
        mock(WallpaperRepository::class.java).also { repository ->
            runBlocking { `when`(repository.currentHomeWallpaper()).thenReturn(HomeWallpaper.Unavailable) }
        }

    private fun createViewModel(
        facet: Facet? = facetWith(ClockWidgetConfig()),
        settingsRepository: SettingsRepository = mock(SettingsRepository::class.java),
        facetRepository: FacetRepository = mock(FacetRepository::class.java),
        calendarPermissionRepository: CalendarPermissionRepository = mock(CalendarPermissionRepository::class.java),
        calendarRepository: CalendarRepository = mock(CalendarRepository::class.java),
        clockStackRepository: ClockStackRepository = this.clockStackRepository,
        wallpaperRepository: WallpaperRepository = unavailableWallpaperRepository(),
        todayEvents: List<CalendarEvent> = emptyList(),
    ): CalendarSettingsViewModel {
        `when`(settingsRepository.settings).thenReturn(flowOf(LauncherSettings()))
        `when`(facetRepository.observeFacets()).thenReturn(flowOf(listOfNotNull(facet)))
        facet?.let { runBlocking { `when`(facetRepository.getById(it.id)).thenReturn(it) } }
        runBlocking { `when`(calendarRepository.getUpcomingEvents(any(), anyBoolean())).thenReturn(todayEvents) }
        val savedStateHandle = if (facet != null) SavedStateHandle(mapOf("facetId" to facet.id)) else SavedStateHandle()
        return CalendarSettingsViewModel(
            savedStateHandle, settingsRepository, facetRepository, calendarPermissionRepository, calendarRepository, wallpaperRepository,
            AssignCalendarColorsUseCase(), UpdateClockConfigUseCase(settingsRepository, facetRepository, clockStackRepository),
        )
    }






    @Test
    fun `calendar swatches are hidden when the indicator is off`() {
        val colors = mapOf("1" to AccentSwatch.entries.first().name)
        val on = CalendarSettingsUiState(settings = LauncherSettings(showCalendarIndicator = true), calendarColors = colors)
        val off = CalendarSettingsUiState(settings = LauncherSettings(showCalendarIndicator = false), calendarColors = colors)

        assertEquals(AccentSwatch.entries.first(), on.calendarSwatch("1"))
        assertEquals(null, off.calendarSwatch("1"))
        assertEquals(null, on.calendarSwatch("unknown"))
    }





    @Test
    fun `granted access fetches the real calendar list`() = runTest {
        val calendarPermissionRepository = mock(CalendarPermissionRepository::class.java)
        `when`(calendarPermissionRepository.isGranted()).thenReturn(true)
        val calendarRepository = mock(CalendarRepository::class.java)
        val calendars = listOf(CalendarInfo(id = "1", displayName = "Work", accountName = "work@example.com"))
        `when`(calendarRepository.getSelectableCalendars()).thenReturn(calendars)

        val viewModel = createViewModel(calendarPermissionRepository = calendarPermissionRepository, calendarRepository = calendarRepository)
        backgroundScope.launch { viewModel.uiState.collect {} }
        testDispatcher.scheduler.advanceUntilIdle()

        assertEquals(true, viewModel.uiState.value.isCalendarAccessGranted)
        assertEquals(calendars, viewModel.uiState.value.calendars)
    }

    @Test
    fun `revoked access drops the calendar list that was loaded`() = runTest {
        // Given a list loaded while access was granted
        val calendarPermissionRepository = mock(CalendarPermissionRepository::class.java)
        `when`(calendarPermissionRepository.isGranted()).thenReturn(true)
        val calendarRepository = mock(CalendarRepository::class.java)
        `when`(calendarRepository.getSelectableCalendars()).thenReturn(listOf(CalendarInfo(id = "1", displayName = "Work", accountName = "work@example.com")))
        val viewModel = createViewModel(calendarPermissionRepository = calendarPermissionRepository, calendarRepository = calendarRepository)
        backgroundScope.launch { viewModel.uiState.collect {} }
        testDispatcher.scheduler.advanceUntilIdle()
        assertEquals(1, viewModel.uiState.value.calendars.size)

        // When access is revoked and the screen resumes
        `when`(calendarPermissionRepository.isGranted()).thenReturn(false)
        viewModel.refreshCalendarAccess()
        testDispatcher.scheduler.advanceUntilIdle()

        // Then no calendars are kept
        assertEquals(emptyList<CalendarInfo>(), viewModel.uiState.value.calendars)
    }

    @Test
    fun `granted access loads today's real events for the preview`() = runTest {
        val calendarPermissionRepository = mock(CalendarPermissionRepository::class.java)
        `when`(calendarPermissionRepository.isGranted()).thenReturn(true)
        val calendarRepository = mock(CalendarRepository::class.java)
        `when`(calendarRepository.getSelectableCalendars()).thenReturn(emptyList())
        val events = listOf(CalendarEvent(id = 1, calendarId = "1", title = "Standup", startTimeMillis = 0, endTimeMillis = 1, isAllDay = false))
        val viewModel = createViewModel(
            calendarPermissionRepository = calendarPermissionRepository,
            calendarRepository = calendarRepository,
            todayEvents = events,
        )
        backgroundScope.launch { viewModel.uiState.collect {} }
        testDispatcher.scheduler.advanceUntilIdle()

        assertEquals(events, viewModel.uiState.value.previewEvents)
    }

    @Test
    fun `ungranted access never queries events for the preview`() = runTest {
        val calendarPermissionRepository = mock(CalendarPermissionRepository::class.java)
        `when`(calendarPermissionRepository.isGranted()).thenReturn(false)
        val calendarRepository = mock(CalendarRepository::class.java)
        val viewModel = createViewModel(calendarPermissionRepository = calendarPermissionRepository, calendarRepository = calendarRepository)
        backgroundScope.launch { viewModel.uiState.collect {} }
        testDispatcher.scheduler.advanceUntilIdle()

        assertEquals(emptyList<CalendarEvent>(), viewModel.uiState.value.previewEvents)
        runBlocking { verify(calendarRepository, org.mockito.Mockito.never()).getUpcomingEvents(any(), anyBoolean()) }
    }

    @Test
    fun `ungranted access never queries the calendar list`() = runTest {
        val calendarPermissionRepository = mock(CalendarPermissionRepository::class.java)
        `when`(calendarPermissionRepository.isGranted()).thenReturn(false)
        val calendarRepository = mock(CalendarRepository::class.java)

        val viewModel = createViewModel(calendarPermissionRepository = calendarPermissionRepository, calendarRepository = calendarRepository)
        backgroundScope.launch { viewModel.uiState.collect {} }
        testDispatcher.scheduler.advanceUntilIdle()

        assertEquals(false, viewModel.uiState.value.isCalendarAccessGranted)
        assertEquals(emptyList<CalendarInfo>(), viewModel.uiState.value.calendars)
        verify(calendarRepository, org.mockito.Mockito.never()).getCalendars()
    }

    private fun granted(vararg calendars: CalendarInfo): Pair<CalendarPermissionRepository, CalendarRepository> {
        val permission = mock(CalendarPermissionRepository::class.java)
        `when`(permission.isGranted()).thenReturn(true)
        val calendarRepository = mock(CalendarRepository::class.java)
        runBlocking { `when`(calendarRepository.getSelectableCalendars()).thenReturn(calendars.toList()) }
        return permission to calendarRepository
    }

    private val twoPersonalOneWork = arrayOf(CalendarInfo("1", "A", "a"), CalendarInfo("2", "B", "b"), CalendarInfo("work:1", "", "", isWork = true))

    @Test
    fun `a work calendar is off until chosen and joins the personal ones when ticked`() = runTest {
        // Given two personal calendars and a work one, on a clock that follows every calendar
        val (permission, calendarRepository) = granted(*twoPersonalOneWork)
        val viewModel = createViewModel(calendarPermissionRepository = permission, calendarRepository = calendarRepository)
        backgroundScope.launch { viewModel.uiState.collect {} }
        testDispatcher.scheduler.advanceUntilIdle()

        // Then with nothing chosen the personal ones show and the work one does not
        assertTrue(viewModel.uiState.value.isCalendarSelected("1"))
        assertFalse(viewModel.uiState.value.isCalendarSelected("work:1"))

        // When ticking the work calendar
        viewModel.setCalendarSelected("work:1", true)
        testDispatcher.scheduler.advanceUntilIdle()

        // Then the clock stores the personal calendars already showing plus the work one
        verify(clockStackRepository).setConfig(9, stored(ClockCalendarConfig(calendarIds = setOf("1", "2", "work:1"))))
    }

    @Test
    fun `deselecting a calendar while on every calendar stores the full set minus that one`() = runTest {
        val (permission, calendarRepository) = granted(CalendarInfo("1", "A", "a"), CalendarInfo("2", "B", "b"))
        val viewModel = createViewModel(calendarPermissionRepository = permission, calendarRepository = calendarRepository)
        backgroundScope.launch { viewModel.uiState.collect {} }
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.setCalendarSelected("1", false)
        testDispatcher.scheduler.advanceUntilIdle()

        verify(clockStackRepository).setConfig(9, stored(ClockCalendarConfig(calendarIds = setOf("2"))))
    }

    @Test
    fun `selecting all stores every loaded calendar and deselecting all stores none`() = runTest {
        val (permission, calendarRepository) = granted(CalendarInfo("1", "A", "a"), CalendarInfo("2", "B", "b"))
        val viewModel = createViewModel(facet = facetWith(ClockWidgetConfig(calendar = ClockCalendarConfig(calendarIds = setOf("1")))), calendarPermissionRepository = permission, calendarRepository = calendarRepository)
        backgroundScope.launch { viewModel.uiState.collect {} }
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.setAllCalendarsSelected(true)
        viewModel.setAllCalendarsSelected(false)
        testDispatcher.scheduler.advanceUntilIdle()

        verify(clockStackRepository).setConfig(9, stored(ClockCalendarConfig(calendarIds = setOf("1", "2"))))
        verify(clockStackRepository).setConfig(9, stored(ClockCalendarConfig(calendarIds = emptySet())))
    }

    @Test
    fun `the all-day switch, the indicator and the item count are stored on the clock`() = runTest {
        val viewModel = createViewModel()

        viewModel.setShowAllDayEvents(false)
        viewModel.setShowCalendarIndicator(false)
        viewModel.setCalendarItemsToShow(2)
        testDispatcher.scheduler.advanceUntilIdle()

        verify(clockStackRepository).setConfig(9, stored(ClockCalendarConfig(showAllDay = false)))
        verify(clockStackRepository).setConfig(9, stored(ClockCalendarConfig(showIndicator = false)))
        verify(clockStackRepository).setConfig(9, stored(ClockCalendarConfig(itemsToShow = 2)))
    }

    @Test
    fun `the display options are the clock's own`() = runTest {
        val own = ClockWidgetConfig(calendar = ClockCalendarConfig(showIndicator = false, itemsToShow = 1, showAllDay = false))
        val viewModel = createViewModel(facet = facetWith(own))
        backgroundScope.launch { viewModel.uiState.collect {} }
        testDispatcher.scheduler.advanceUntilIdle()

        assertEquals(CalendarDisplayOptions(showIndicator = false, itemsToShow = 1), viewModel.uiState.value.calendarDisplay)
        assertEquals(false, viewModel.uiState.value.effectiveShowAllDayEvents)
    }

    @Test
    fun `with no facet nothing is written`() = runTest {
        val viewModel = createViewModel(facet = null)

        viewModel.setShowAllDayEvents(false)
        testDispatcher.scheduler.advanceUntilIdle()

        verify(clockStackRepository, org.mockito.Mockito.never()).setConfig(9, stored(ClockCalendarConfig(showAllDay = false)))
    }
}
