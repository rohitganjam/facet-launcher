package com.facetlauncher.app.domain

import com.facetlauncher.app.data.CalendarPermissionRepository
import com.facetlauncher.app.data.CalendarRepository
import com.facetlauncher.app.data.SettingsRepository
import com.facetlauncher.app.data.model.CalendarInfo
import com.facetlauncher.app.data.model.LauncherSettings
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.toList
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
    fun `every calendar change on the device triggers an assignment`() = runTest {
        // Given a calendar change signal and a calendar without a colour
        recordSaves()
        val useCase = useCase()
        `when`(permission.isGranted()).thenReturn(true)
        `when`(calendars.getSelectableCalendars()).thenReturn(listOf(CalendarInfo("7", "New", "me")))
        `when`(calendars.observeCalendarChanges()).thenReturn(flowOf(Unit, Unit))

        // When the keep-assigned flow runs
        useCase.keepAssigned().toList()

        // Then the new calendar has a colour
        assertNotNull(saved?.get("7"))
    }
}
