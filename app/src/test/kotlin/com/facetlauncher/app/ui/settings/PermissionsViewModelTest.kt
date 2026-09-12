package com.facetlauncher.app.ui.settings

import com.facetlauncher.app.data.CalendarPermissionRepository
import com.facetlauncher.app.data.ContactPermissionRepository
import com.facetlauncher.app.data.NotificationAccessRepository
import com.facetlauncher.app.data.SettingsRepository
import com.facetlauncher.app.data.UsageAccessRepository
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
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify
import org.mockito.Mockito.`when`

@OptIn(ExperimentalCoroutinesApi::class)
class PermissionsViewModelTest {

    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun createViewModel(
        settings: LauncherSettings = LauncherSettings(),
        settingsRepository: SettingsRepository = mock(SettingsRepository::class.java),
        calendarPermissionRepository: CalendarPermissionRepository = mock(CalendarPermissionRepository::class.java),
        contactPermissionRepository: ContactPermissionRepository = mock(ContactPermissionRepository::class.java),
        usageAccessRepository: UsageAccessRepository = mock(UsageAccessRepository::class.java),
        notificationAccessRepository: NotificationAccessRepository = mock(NotificationAccessRepository::class.java),
    ): PermissionsViewModel {
        `when`(settingsRepository.settings).thenReturn(flowOf(settings))
        return PermissionsViewModel(
            settingsRepository,
            calendarPermissionRepository,
            contactPermissionRepository,
            usageAccessRepository,
            notificationAccessRepository,
        )
    }

    @Test
    fun `uiState reflects each repository's granted status and the persisted requested-before flags`() = runTest {
        val calendarPermissionRepository = mock(CalendarPermissionRepository::class.java)
        `when`(calendarPermissionRepository.isGranted()).thenReturn(true)
        val contactPermissionRepository = mock(ContactPermissionRepository::class.java)
        `when`(contactPermissionRepository.isGranted()).thenReturn(false)
        val usageAccessRepository = mock(UsageAccessRepository::class.java)
        `when`(usageAccessRepository.isGranted()).thenReturn(false)
        val notificationAccessRepository = mock(NotificationAccessRepository::class.java)
        `when`(notificationAccessRepository.isGranted()).thenReturn(true)
        val viewModel = createViewModel(
            settings = LauncherSettings(calendarPermissionRequested = true, contactsPermissionRequested = false),
            calendarPermissionRepository = calendarPermissionRepository,
            contactPermissionRepository = contactPermissionRepository,
            usageAccessRepository = usageAccessRepository,
            notificationAccessRepository = notificationAccessRepository,
        )
        backgroundScope.launch { viewModel.uiState.collect {} }
        testDispatcher.scheduler.advanceUntilIdle()

        val permissions = viewModel.uiState.value.permissions
        assertEquals(4, permissions.size)

        val calendar = permissions.single { it.kind == PermissionKind.CALENDAR }
        assertTrue(calendar.isGranted)
        assertTrue(calendar.hasRequestedBefore)

        val contacts = permissions.single { it.kind == PermissionKind.CONTACTS }
        assertFalse(contacts.isGranted)
        assertFalse(contacts.hasRequestedBefore)

        val usageAccess = permissions.single { it.kind == PermissionKind.USAGE_ACCESS }
        assertFalse(usageAccess.isGranted)

        val notificationAccess = permissions.single { it.kind == PermissionKind.NOTIFICATION_ACCESS }
        assertTrue(notificationAccess.isGranted)
    }

    @Test
    fun `markCalendarPermissionRequested and markContactsPermissionRequested write SettingsRepository directly`() = runTest {
        val settingsRepository = mock(SettingsRepository::class.java)
        val viewModel = createViewModel(settingsRepository = settingsRepository)
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.markCalendarPermissionRequested()
        viewModel.markContactsPermissionRequested()
        testDispatcher.scheduler.advanceUntilIdle()

        verify(settingsRepository).setCalendarPermissionRequested(true)
        verify(settingsRepository).setContactsPermissionRequested(true)
    }

    @Test
    fun `refresh re-checks every repository's granted status`() = runTest {
        val calendarPermissionRepository = mock(CalendarPermissionRepository::class.java)
        `when`(calendarPermissionRepository.isGranted()).thenReturn(false, true)
        val viewModel = createViewModel(calendarPermissionRepository = calendarPermissionRepository)
        backgroundScope.launch { viewModel.uiState.collect {} }
        testDispatcher.scheduler.advanceUntilIdle()
        assertFalse(viewModel.uiState.value.permissions.single { it.kind == PermissionKind.CALENDAR }.isGranted)

        // When refreshed (e.g. after returning from the system permission dialog or App Info)
        viewModel.refresh()
        testDispatcher.scheduler.advanceUntilIdle()

        // Then it re-checks and reflects the now-granted state
        assertTrue(viewModel.uiState.value.permissions.single { it.kind == PermissionKind.CALENDAR }.isGranted)
    }
}
