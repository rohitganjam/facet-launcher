package com.lumenlauncher.app.ui.settings

import androidx.lifecycle.SavedStateHandle
import com.lumenlauncher.app.data.CalendarPermissionRepository
import com.lumenlauncher.app.data.CalendarRepository
import com.lumenlauncher.app.data.ProfileRepository
import com.lumenlauncher.app.data.SettingsRepository
import com.lumenlauncher.app.data.local.ProfileEntity
import com.lumenlauncher.app.data.model.CalendarInfo
import com.lumenlauncher.app.data.model.LauncherSettings
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
        profileId: Long? = null,
        globalShowAllDayEvents: Boolean = true,
        profile: ProfileEntity? = null,
        settingsRepository: SettingsRepository = mock(SettingsRepository::class.java),
        profileRepository: ProfileRepository = mock(ProfileRepository::class.java),
        calendarPermissionRepository: CalendarPermissionRepository = mock(CalendarPermissionRepository::class.java),
        calendarRepository: CalendarRepository = mock(CalendarRepository::class.java),
    ): CalendarSettingsViewModel {
        `when`(settingsRepository.settings).thenReturn(flowOf(LauncherSettings(showAllDayEvents = globalShowAllDayEvents)))
        `when`(profileRepository.observeProfiles()).thenReturn(flowOf(listOfNotNull(profile)))
        val savedStateHandle = if (profileId != null) SavedStateHandle(mapOf("profileId" to profileId)) else SavedStateHandle()
        return CalendarSettingsViewModel(savedStateHandle, settingsRepository, profileRepository, calendarPermissionRepository, calendarRepository)
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
    fun `profile-scoped mode toggling writes the profile's own override`() = runTest {
        val profile = ProfileEntity(id = 5L, name = "Work", position = 0)
        val profileRepository = mock(ProfileRepository::class.java)
        val viewModel = createViewModel(profileId = 5L, profile = profile, profileRepository = profileRepository)
        backgroundScope.launch { viewModel.uiState.collect {} }
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.setShowAllDayEvents(false)
        testDispatcher.scheduler.advanceUntilIdle()

        verify(profileRepository).setShowAllDayEventsOverride(profile, false)
    }

    @Test
    fun `switching to override seeds the profile's stored value with the current effective one`() = runTest {
        val profile = ProfileEntity(id = 5L, name = "Work", position = 0, showAllDayEventsOverride = null)
        val profileRepository = mock(ProfileRepository::class.java)
        val viewModel = createViewModel(
            profileId = 5L,
            globalShowAllDayEvents = true,
            profile = profile,
            profileRepository = profileRepository,
        )
        backgroundScope.launch { viewModel.uiState.collect {} }
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.setOverriding(true)
        testDispatcher.scheduler.advanceUntilIdle()

        verify(profileRepository).setShowAllDayEventsOverride(profile, true)
    }

    @Test
    fun `switching back to inherit clears the profile's override`() = runTest {
        val profile = ProfileEntity(id = 5L, name = "Work", position = 0, showAllDayEventsOverride = false)
        val profileRepository = mock(ProfileRepository::class.java)
        val viewModel = createViewModel(profileId = 5L, profile = profile, profileRepository = profileRepository)
        backgroundScope.launch { viewModel.uiState.collect {} }
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.setOverriding(false)
        testDispatcher.scheduler.advanceUntilIdle()

        verify(profileRepository).setShowAllDayEventsOverride(profile, null)
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
