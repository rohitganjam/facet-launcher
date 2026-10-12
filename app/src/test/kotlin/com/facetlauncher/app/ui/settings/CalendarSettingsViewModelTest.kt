package com.facetlauncher.app.ui.settings

import androidx.lifecycle.SavedStateHandle
import com.facetlauncher.app.data.CalendarPermissionRepository
import com.facetlauncher.app.data.CalendarRepository
import com.facetlauncher.app.data.FacetRepository
import com.facetlauncher.app.data.SettingsRepository
import com.facetlauncher.app.data.WallpaperRepository
import com.facetlauncher.app.data.local.FacetEntity
import com.facetlauncher.app.data.model.CalendarDisplayOptions
import com.facetlauncher.app.data.model.CalendarInfo
import com.facetlauncher.app.data.model.LauncherSettings
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
import org.junit.Before
import org.junit.Test
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

    private fun createViewModel(
        facetId: Long? = null,
        globalShowAllDayEvents: Boolean = true,
        facet: FacetEntity? = null,
        settingsRepository: SettingsRepository = mock(SettingsRepository::class.java),
        facetRepository: FacetRepository = mock(FacetRepository::class.java),
        calendarPermissionRepository: CalendarPermissionRepository = mock(CalendarPermissionRepository::class.java),
        calendarRepository: CalendarRepository = mock(CalendarRepository::class.java),
        wallpaperRepository: WallpaperRepository = mock(WallpaperRepository::class.java),
    ): CalendarSettingsViewModel {
        `when`(settingsRepository.settings).thenReturn(flowOf(LauncherSettings(showAllDayEvents = globalShowAllDayEvents)))
        `when`(facetRepository.observeFacets()).thenReturn(flowOf(listOfNotNull(facet)))
        val savedStateHandle = if (facetId != null) SavedStateHandle(mapOf("facetId" to facetId)) else SavedStateHandle()
        return CalendarSettingsViewModel(savedStateHandle, settingsRepository, facetRepository, calendarPermissionRepository, calendarRepository, wallpaperRepository)
    }

    @Test
    fun `global mode toggling writes SettingsRepository directly`() = runTest {
        val settingsRepository = mock(SettingsRepository::class.java)
        val viewModel = createViewModel(settingsRepository = settingsRepository)
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.setShowAllDayEvents(false)
        testDispatcher.scheduler.advanceUntilIdle()

        verify(settingsRepository).setShowAllDayEvents(false)
    }

    @Test
    fun `global mode writes the indicator and item count to SettingsRepository`() = runTest {
        val settingsRepository = mock(SettingsRepository::class.java)
        val viewModel = createViewModel(settingsRepository = settingsRepository)
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.setShowCalendarIndicator(false)
        viewModel.setCalendarItemsToShow(2)
        testDispatcher.scheduler.advanceUntilIdle()

        verify(settingsRepository).setShowCalendarIndicator(false)
        verify(settingsRepository).setCalendarItemsToShow(2)
    }

    @Test
    fun `facet-scoped mode writes the indicator and item count to the facet`() = runTest {
        val facet = FacetEntity(id = 5L, name = "Work", position = 0, overrideCalendar = true)
        val facetRepository = mock(FacetRepository::class.java)
        val viewModel = createViewModel(facetId = 5L, facet = facet, facetRepository = facetRepository)
        backgroundScope.launch { viewModel.uiState.collect {} }
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.setShowCalendarIndicator(false)
        viewModel.setCalendarItemsToShow(1)
        testDispatcher.scheduler.advanceUntilIdle()

        verify(facetRepository).setShowCalendarIndicator(facet, false)
        verify(facetRepository).setCalendarItemsToShow(facet, 1)
    }

    @Test
    fun `calendar display follows the facet only while it overrides the calendar`() {
        val settings = LauncherSettings(showCalendarIndicator = true, calendarItemsToShow = 3)
        val facet = FacetEntity(id = 5L, name = "Work", position = 0, showCalendarIndicator = false, calendarItemsToShow = 1)

        val inheriting = CalendarSettingsUiState(facet = facet.copy(overrideCalendar = false), settings = settings)
        val overriding = CalendarSettingsUiState(facet = facet.copy(overrideCalendar = true), settings = settings)

        assertEquals(CalendarDisplayOptions(showIndicator = true, itemsToShow = 3), inheriting.calendarDisplay)
        assertEquals(CalendarDisplayOptions(showIndicator = false, itemsToShow = 1), overriding.calendarDisplay)
    }

    @Test
    fun `facet-scoped mode toggling writes the facet's own override`() = runTest {
        val facet = FacetEntity(id = 5L, name = "Work", position = 0, overrideCalendar = true)
        val facetRepository = mock(FacetRepository::class.java)
        val viewModel = createViewModel(facetId = 5L, facet = facet, facetRepository = facetRepository)
        backgroundScope.launch { viewModel.uiState.collect {} }
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.setShowAllDayEvents(false)
        testDispatcher.scheduler.advanceUntilIdle()

        verify(facetRepository).setShowAllDayEvents(facet, false)
    }

    @Test
    fun `switching to override seeds the facet's stored value with the current effective one`() = runTest {
        val facet = FacetEntity(id = 5L, name = "Work", position = 0, overrideCalendar = false)
        val facetRepository = mock(FacetRepository::class.java)
        val viewModel = createViewModel(
            facetId = 5L,
            globalShowAllDayEvents = true,
            facet = facet,
            facetRepository = facetRepository,
        )
        backgroundScope.launch { viewModel.uiState.collect {} }
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.setOverriding(true)
        testDispatcher.scheduler.advanceUntilIdle()

        verify(facetRepository).updateOverridingCalendar(
            facet,
            overriding = true,
            showAllDayEvents = true,
            selectedCalendarIds = null,
        )
    }

    @Test
    fun `switching back to inherit toggles the override flag`() = runTest {
        val facet = FacetEntity(id = 5L, name = "Work", position = 0, overrideCalendar = true, showAllDayEvents = false)
        val facetRepository = mock(FacetRepository::class.java)
        val viewModel = createViewModel(facetId = 5L, facet = facet, facetRepository = facetRepository)
        backgroundScope.launch { viewModel.uiState.collect {} }
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.setOverriding(false)
        testDispatcher.scheduler.advanceUntilIdle()

        verify(facetRepository).updateOverridingCalendar(
            facet,
            overriding = false,
            showAllDayEvents = false,
            selectedCalendarIds = null,
        )
    }

    @Test
    fun `switching to override seeds the facet's selectedCalendarIds with the current effective set`() = runTest {
        val facet = FacetEntity(id = 5L, name = "Work", position = 0, overrideCalendar = false)
        val facetRepository = mock(FacetRepository::class.java)
        val settingsRepository = mock(SettingsRepository::class.java)
        `when`(settingsRepository.settings).thenReturn(flowOf(LauncherSettings(selectedCalendarIds = setOf("7", "8"))))
        `when`(facetRepository.observeFacets()).thenReturn(flowOf(listOf(facet)))
        val savedStateHandle = SavedStateHandle(mapOf("facetId" to 5L))
        val viewModel = CalendarSettingsViewModel(
            savedStateHandle,
            settingsRepository,
            facetRepository,
            mock(CalendarPermissionRepository::class.java),
            mock(CalendarRepository::class.java),
        )
        backgroundScope.launch { viewModel.uiState.collect {} }
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.setOverriding(true)
        testDispatcher.scheduler.advanceUntilIdle()

        verify(facetRepository).updateOverridingCalendar(
            facet,
            overriding = true,
            showAllDayEvents = true,
            selectedCalendarIds = setOf("7", "8"),
        )
    }

    @Test
    fun `granted access fetches the real calendar list`() = runTest {
        val calendarPermissionRepository = mock(CalendarPermissionRepository::class.java)
        `when`(calendarPermissionRepository.isGranted()).thenReturn(true)
        val calendarRepository = mock(CalendarRepository::class.java)
        val calendars = listOf(CalendarInfo(id = "1", displayName = "Work", accountName = "work@example.com"))
        `when`(calendarRepository.getCalendars()).thenReturn(calendars)

        val viewModel = createViewModel(calendarPermissionRepository = calendarPermissionRepository, calendarRepository = calendarRepository)
        backgroundScope.launch { viewModel.uiState.collect {} }
        testDispatcher.scheduler.advanceUntilIdle()

        assertEquals(true, viewModel.uiState.value.isCalendarAccessGranted)
        assertEquals(calendars, viewModel.uiState.value.calendars)
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

    @Test
    fun `deselecting a calendar while implicitly all-selected persists the full set minus that one`() = runTest {
        val calendarPermissionRepository = mock(CalendarPermissionRepository::class.java)
        `when`(calendarPermissionRepository.isGranted()).thenReturn(true)
        val calendarRepository = mock(CalendarRepository::class.java)
        val calendars = listOf(
            CalendarInfo(id = "1", displayName = "Work", accountName = "work@example.com"),
            CalendarInfo(id = "2", displayName = "Personal", accountName = "personal@example.com"),
        )
        `when`(calendarRepository.getCalendars()).thenReturn(calendars)
        val settingsRepository = mock(SettingsRepository::class.java)

        val viewModel = createViewModel(
            settingsRepository = settingsRepository,
            calendarPermissionRepository = calendarPermissionRepository,
            calendarRepository = calendarRepository,
        )
        backgroundScope.launch { viewModel.uiState.collect {} }
        testDispatcher.scheduler.advanceUntilIdle()

        // When deselecting calendar "1" while selectedCalendarIds is still implicitly null (all selected)
        viewModel.setCalendarSelected("1", selected = false)
        testDispatcher.scheduler.advanceUntilIdle()

        // Then it persists the full set minus that one, not just "not 1"
        verify(settingsRepository).setSelectedCalendarIds(setOf("2"))
    }

    @Test
    fun `selecting all persists every currently-loaded calendar id`() = runTest {
        val calendarPermissionRepository = mock(CalendarPermissionRepository::class.java)
        `when`(calendarPermissionRepository.isGranted()).thenReturn(true)
        val calendarRepository = mock(CalendarRepository::class.java)
        val calendars = listOf(
            CalendarInfo(id = "1", displayName = "Work", accountName = "work@example.com"),
            CalendarInfo(id = "2", displayName = "Personal", accountName = "personal@example.com"),
        )
        `when`(calendarRepository.getCalendars()).thenReturn(calendars)
        val settingsRepository = mock(SettingsRepository::class.java)

        val viewModel = createViewModel(
            settingsRepository = settingsRepository,
            calendarPermissionRepository = calendarPermissionRepository,
            calendarRepository = calendarRepository,
        )
        backgroundScope.launch { viewModel.uiState.collect {} }
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.setAllCalendarsSelected(true)
        testDispatcher.scheduler.advanceUntilIdle()

        verify(settingsRepository).setSelectedCalendarIds(setOf("1", "2"))
    }

    @Test
    fun `deselecting all persists an empty set`() = runTest {
        val calendarPermissionRepository = mock(CalendarPermissionRepository::class.java)
        `when`(calendarPermissionRepository.isGranted()).thenReturn(true)
        val calendarRepository = mock(CalendarRepository::class.java)
        val calendars = listOf(CalendarInfo(id = "1", displayName = "Work", accountName = "work@example.com"))
        `when`(calendarRepository.getCalendars()).thenReturn(calendars)
        val settingsRepository = mock(SettingsRepository::class.java)

        val viewModel = createViewModel(
            settingsRepository = settingsRepository,
            calendarPermissionRepository = calendarPermissionRepository,
            calendarRepository = calendarRepository,
        )
        backgroundScope.launch { viewModel.uiState.collect {} }
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.setAllCalendarsSelected(false)
        testDispatcher.scheduler.advanceUntilIdle()

        verify(settingsRepository).setSelectedCalendarIds(emptySet())
    }
}
