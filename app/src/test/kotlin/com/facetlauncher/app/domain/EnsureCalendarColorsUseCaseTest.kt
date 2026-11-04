package com.facetlauncher.app.domain

import com.facetlauncher.app.data.CalendarPermissionRepository
import com.facetlauncher.app.data.CalendarRepository
import com.facetlauncher.app.data.SettingsRepository
import com.facetlauncher.app.data.model.CalendarInfo
import com.facetlauncher.app.data.model.LauncherSettings
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertNotNull
import org.junit.Test
import org.mockito.ArgumentMatchers.anyMap
import org.mockito.Mockito.mock
import org.mockito.Mockito.never
import org.mockito.Mockito.verify
import org.mockito.Mockito.`when`

class EnsureCalendarColorsUseCaseTest {

    private val permission = mock(CalendarPermissionRepository::class.java)
    private val calendars = mock(CalendarRepository::class.java)
    private val settings = mock(SettingsRepository::class.java)
    private var saved: Map<String, String>? = null

    private fun useCase(existing: Map<String, String> = emptyMap()): EnsureCalendarColorsUseCase {
        `when`(settings.settings).thenReturn(flowOf(LauncherSettings(calendarColors = existing)))
        return EnsureCalendarColorsUseCase(permission, calendars, settings, AssignCalendarColorsUseCase())
    }

    private suspend fun recordSaves() {
        `when`(settings.setCalendarColors(anyMap())).thenAnswer {
            @Suppress("UNCHECKED_CAST")
            saved = it.arguments[0] as Map<String, String>
            Unit
        }
    }

    @Test
    fun `a new calendar gets a colour and an existing one keeps its own`() = runTest {
        // Given a coloured calendar and a new work calendar
        recordSaves()
        val useCase = useCase(mapOf("1" to "TEAL"))
        `when`(permission.isGranted()).thenReturn(true)
        `when`(calendars.getSelectableCalendars()).thenReturn(listOf(CalendarInfo("1", "Home", "me"), CalendarInfo("work:1", "", "", isWork = true)))

        // When ensuring
        useCase()

        // Then the first is untouched and the second is added, in the shared settings
        assertEquals("TEAL", saved?.get("1"))
        assertNotNull(saved?.get("work:1"))
        assertNotEquals("TEAL", saved?.get("work:1"))
    }

    @Test
    fun `nothing is read or written without calendar access`() = runTest {
        // Given no calendar permission
        val useCase = useCase()
        `when`(permission.isGranted()).thenReturn(false)

        // When ensuring
        useCase()

        // Then the calendar provider is not queried and nothing is saved
        verify(calendars, never()).getSelectableCalendars()
        verify(settings, never()).setCalendarColors(anyMap())
    }

    @Test
    fun `while access is granted every calendar change triggers an assignment`() = runTest {
        // Given access, a change signal we control, and a calendar without a colour
        recordSaves()
        val useCase = useCase()
        val changes = MutableSharedFlow<Unit>()
        `when`(permission.isGranted()).thenReturn(true)
        `when`(calendars.getSelectableCalendars()).thenReturn(listOf(CalendarInfo("7", "New", "me")))
        `when`(calendars.observeCalendarChanges()).thenReturn(changes)
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { useCase.keepAssigned().collect {} }

        // When the device reports a calendar change
        changes.emit(Unit)

        // Then the new calendar has a colour
        assertNotNull(saved?.get("7"))
    }

    @Test
    fun `one failed assignment does not end the observation`() = runTest {
        // Given the provider failing on the first change and working on the second
        recordSaves()
        val useCase = useCase()
        val changes = MutableSharedFlow<Unit>()
        `when`(permission.isGranted()).thenReturn(true)
        `when`(calendars.getSelectableCalendars())
            .thenThrow(SecurityException("revoked mid-query"))
            .thenReturn(listOf(CalendarInfo("7", "New", "me")))
        `when`(calendars.observeCalendarChanges()).thenReturn(changes)
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { useCase.keepAssigned().collect {} }

        // When the first change fails and a second arrives
        changes.emit(Unit)
        changes.emit(Unit)

        // Then the second is still handled
        assertNotNull(saved?.get("7"))
    }

    @Test
    fun `without access nothing is observed and the stored colours are cleared`() = runTest {
        // Given colours stored earlier and no calendar access now
        recordSaves()
        val useCase = useCase(mapOf("1" to "BLUE", "work:1" to "PURPLE"))
        `when`(permission.isGranted()).thenReturn(false)

        // When the app starts keeping colours assigned
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { useCase.keepAssigned().collect {} }

        // Then the provider is never observed and the colours are gone
        verify(calendars, never()).observeCalendarChanges()
        assertEquals(emptyMap<String, String>(), saved)
    }

    @Test
    fun `revoking access stops observing and clears, and granting it again resumes`() = runTest {
        // Given access that is granted, then revoked, then granted again, each noticed when the app is rechecked
        recordSaves()
        val useCase = useCase(mapOf("1" to "BLUE"))
        val access = java.util.concurrent.atomic.AtomicBoolean(true)
        `when`(permission.isGranted()).thenAnswer { access.get() }
        `when`(calendars.getSelectableCalendars()).thenReturn(listOf(CalendarInfo("1", "Home", "me")))
        var observers = 0
        var stopped = 0
        `when`(calendars.observeCalendarChanges()).thenAnswer {
            callbackFlow<Unit> {
                observers++
                trySend(Unit)
                awaitClose { stopped++ }
            }
        }
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { useCase.keepAssigned().collect {} }
        assertEquals(1, observers)

        // When access is revoked
        access.set(false)
        useCase.recheckAccess()
        runCurrent()

        // Then observation stops and the colours are cleared
        assertEquals(1, stopped)
        assertEquals(emptyMap<String, String>(), saved)

        // When it is granted again
        access.set(true)
        useCase.recheckAccess()
        runCurrent()

        // Then it observes again
        assertEquals(2, observers)
    }

    @Test
    fun `after the process was killed a restart colours calendars added while it was dead`() = runTest {
        // Given calendars that appeared while the app was not running (no colours stored) and a fresh process
        recordSaves()
        val restarted = useCase(mapOf("1" to "BLUE"))
        `when`(permission.isGranted()).thenReturn(true)
        `when`(calendars.getSelectableCalendars()).thenReturn(listOf(CalendarInfo("1", "Home", "me"), CalendarInfo("5", "Added meanwhile", "me")))
        `when`(calendars.observeCalendarChanges()).thenReturn(flowOf(Unit)) // the repository emits once as soon as it is observed

        // When the new process starts keeping colours assigned, with no change event of its own
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { restarted.keepAssigned().collect {} }

        // Then it catches up at once, leaving the existing colour alone
        assertEquals("BLUE", saved?.get("1"))
        assertNotNull(saved?.get("5"))
    }

    @Test
    fun `rechecking when access has not changed does not register the observer again`() = runTest {
        // Given access that stays granted
        recordSaves()
        val useCase = useCase()
        `when`(permission.isGranted()).thenReturn(true)
        `when`(calendars.getSelectableCalendars()).thenReturn(emptyList())
        var observers = 0
        `when`(calendars.observeCalendarChanges()).thenAnswer { callbackFlow<Unit> { observers++; awaitClose { } } }
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { useCase.keepAssigned().collect {} }

        // When the app resumes several times
        repeat(3) { useCase.recheckAccess(); runCurrent() }

        // Then the observer was registered once
        assertEquals(1, observers)
    }

    @Test
    fun `an observation that fails is retried after a back-off`() = runTest {
        // Given the provider refusing the first registration and accepting the second
        recordSaves()
        val useCase = useCase()
        `when`(permission.isGranted()).thenReturn(true)
        `when`(calendars.getSelectableCalendars()).thenReturn(listOf(CalendarInfo("7", "New", "me")))
        var attempts = 0
        `when`(calendars.observeCalendarChanges()).thenAnswer {
            callbackFlow<Unit> {
                attempts++
                if (attempts == 1) throw SecurityException("provider not ready")
                trySend(Unit)
                awaitClose { }
            }
        }
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { useCase.keepAssigned().collect {} }
        assertEquals(1, attempts)
        assertEquals(null, saved)

        // When the back-off has passed
        advanceTimeBy(1_001)
        runCurrent()

        // Then it observes again and assigns
        assertEquals(2, attempts)
        assertNotNull(saved?.get("7"))
    }

    @Test
    fun `stopping the collection unregisters the observer`() = runTest {
        // Given an observation in progress
        val useCase = useCase()
        `when`(permission.isGranted()).thenReturn(true)
        `when`(calendars.getSelectableCalendars()).thenReturn(emptyList())
        var stopped = 0
        `when`(calendars.observeCalendarChanges()).thenAnswer { callbackFlow<Unit> { awaitClose { stopped++ } } }
        val job = backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { useCase.keepAssigned().collect {} }

        // When the collection is cancelled
        job.cancel()
        runCurrent()

        // Then the observer was released
        assertEquals(1, stopped)
    }
}
