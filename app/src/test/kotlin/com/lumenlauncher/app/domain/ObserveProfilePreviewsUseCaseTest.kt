package com.lumenlauncher.app.domain

import com.lumenlauncher.app.data.CalendarPermissionRepository
import com.lumenlauncher.app.data.CalendarRepository
import com.lumenlauncher.app.data.DefaultFavoriteAppRepository
import com.lumenlauncher.app.data.DockAppRepository
import com.lumenlauncher.app.data.FavoriteAppRepository
import com.lumenlauncher.app.data.ProfileDockAppRepository
import com.lumenlauncher.app.data.ProfileRepository
import com.lumenlauncher.app.data.SettingsRepository
import com.lumenlauncher.app.data.UsageAccessRepository
import com.lumenlauncher.app.data.UsageStatsRepository
import com.lumenlauncher.app.data.local.ProfileEntity
import com.lumenlauncher.app.data.model.AppInfo
import com.lumenlauncher.app.data.model.CalendarEvent
import com.lumenlauncher.app.data.model.LauncherSettings
import com.lumenlauncher.app.data.model.ListContentMode
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test
import org.mockito.ArgumentMatchers.anyList
import org.mockito.Mockito.mock
import org.mockito.Mockito.`when`

class ObserveProfilePreviewsUseCaseTest {

    private fun appInfo(letter: Char) =
        AppInfo(packageName = "com.example.$letter", activityName = ".Main", label = "$letter App", icon = null)

    private class Fixture {
        val settingsFlow = MutableStateFlow(LauncherSettings())
        val settingsRepository = mock(SettingsRepository::class.java).also { `when`(it.settings).thenReturn(settingsFlow) }
        val favoriteAppRepository = mock(FavoriteAppRepository::class.java)
            .also { `when`(it.observeFavoritesForProfiles(anyList())).thenReturn(flowOf(emptyMap())) }
        val defaultFavoriteAppRepository = mock(DefaultFavoriteAppRepository::class.java)
            .also { `when`(it.observeDefaultFavorites()).thenReturn(flowOf(emptyList())) }
        val profileDockAppRepository = mock(ProfileDockAppRepository::class.java)
            .also { `when`(it.observeDockAppsForProfiles(anyList())).thenReturn(flowOf(emptyMap())) }
        val dockAppRepository = mock(DockAppRepository::class.java)
            .also { `when`(it.observeDockApps()).thenReturn(flowOf(emptyList())) }
        val profilesFlow = MutableStateFlow<List<ProfileEntity>>(emptyList())
        val profileRepository = mock(ProfileRepository::class.java).also { `when`(it.observeProfiles()).thenReturn(profilesFlow) }
        val usageStatsRepository = mock(UsageStatsRepository::class.java)
        val usageAccessRepository = mock(UsageAccessRepository::class.java).also { `when`(it.isGranted()).thenReturn(true) }
        val calendarPermissionRepository = mock(CalendarPermissionRepository::class.java).also { `when`(it.isGranted()).thenReturn(false) }
        val calendarRepository = mock(CalendarRepository::class.java)

        val useCase = ObserveProfilePreviewsUseCase(
            profileRepository,
            settingsRepository,
            favoriteAppRepository,
            defaultFavoriteAppRepository,
            profileDockAppRepository,
            dockAppRepository,
            usageStatsRepository,
            usageAccessRepository,
            calendarPermissionRepository,
            calendarRepository,
        )
    }

    @Test
    fun `resolves each profile's own effective favorites independently`() = runTest {
        // Given two profiles, one overriding its own favorites, one inheriting the default list
        val fixture = Fixture()
        `when`(fixture.favoriteAppRepository.observeFavoritesForProfiles(listOf(1L, 2L)))
            .thenReturn(flowOf(mapOf(1L to listOf(appInfo('a')), 2L to emptyList())))
        `when`(fixture.defaultFavoriteAppRepository.observeDefaultFavorites()).thenReturn(flowOf(listOf(appInfo('d'))))
        fixture.profilesFlow.value = listOf(
            ProfileEntity(id = 1L, name = "P1", position = 0, overridingFavorites = true),
            ProfileEntity(id = 2L, name = "P2", position = 1, overridingFavorites = false),
        )

        // When observing previews for every profile
        val result = fixture.useCase().first()

        // Then each resolves to its own effective list — profile 1's own override, profile 2's default
        assertEquals(listOf(appInfo('a')), result.getValue(1L).favorites)
        assertEquals(listOf(appInfo('d')), result.getValue(2L).favorites)
    }

    @Test
    fun `resolves each profile's own effective dock independently`() = runTest {
        // Given two profiles, one overriding its dock, one inheriting the launcher-wide default
        val fixture = Fixture()
        `when`(fixture.profileDockAppRepository.observeDockAppsForProfiles(listOf(1L, 2L)))
            .thenReturn(flowOf(mapOf(1L to listOf(appInfo('a')), 2L to emptyList())))
        `when`(fixture.dockAppRepository.observeDockApps()).thenReturn(flowOf(listOf(appInfo('d'))))
        fixture.profilesFlow.value = listOf(
            ProfileEntity(id = 1L, name = "P1", position = 0, overrideDock = true),
            ProfileEntity(id = 2L, name = "P2", position = 1, overrideDock = false),
        )

        // When observing previews for every profile
        val result = fixture.useCase().first()

        // Then each resolves to its own effective dock — profile 1's own list, profile 2's default
        assertEquals(listOf(appInfo('a')), result.getValue(1L).dockApps)
        assertEquals(listOf(appInfo('d')), result.getValue(2L).dockApps)
    }

    @Test
    fun `recents mode without usage access yields an empty favorites list for that profile`() = runTest {
        // Given a profile set to Recents, usage access not granted
        val fixture = Fixture()
        `when`(fixture.usageAccessRepository.isGranted()).thenReturn(false)
        fixture.profilesFlow.value = listOf(
            ProfileEntity(id = 1L, name = "P1", position = 0, overrideApps = true, listContentMode = ListContentMode.RECENTS),
        )

        // When observing previews
        val result = fixture.useCase().first()

        // Then the list is empty rather than a broken partial one
        assertEquals(emptyList<AppInfo>(), result.getValue(1L).favorites)
    }

    @Test
    fun `calendar events are empty for every profile when calendar access isn't granted`() = runTest {
        // Given calendar access ungranted (the fixture default) and two profiles
        val fixture = Fixture()
        fixture.profilesFlow.value = listOf(
            ProfileEntity(id = 1L, name = "P1", position = 0),
            ProfileEntity(id = 2L, name = "P2", position = 1),
        )

        // When observing previews
        val result = fixture.useCase().first()

        // Then neither profile exposes any events, and CalendarRepository is never queried
        assertEquals(emptyList<CalendarEvent>(), result.getValue(1L).calendarEvents)
        assertEquals(emptyList<CalendarEvent>(), result.getValue(2L).calendarEvents)
    }

    @Test
    fun `calendar events reflect each profile's own effective show-all-day setting when granted`() = runTest {
        // Given calendar access granted, and two profiles with different effective all-day settings
        val fixture = Fixture()
        `when`(fixture.calendarPermissionRepository.isGranted()).thenReturn(true)
        val overriddenEvent = CalendarEvent(id = 1L, calendarId = "1", title = "Standup", startTimeMillis = 1_000L, endTimeMillis = 2_000L, isAllDay = false)
        val inheritedEvent = CalendarEvent(id = 2L, calendarId = "1", title = "All day", startTimeMillis = 3_000L, endTimeMillis = 4_000L, isAllDay = true)
        `when`(fixture.calendarRepository.getTodayEvents(null, false)).thenReturn(listOf(overriddenEvent))
        `when`(fixture.calendarRepository.getTodayEvents(null, true)).thenReturn(listOf(overriddenEvent, inheritedEvent))
        fixture.profilesFlow.value = listOf(
            // Overrides all-day events off — queried with includeAllDay=false.
            ProfileEntity(id = 1L, name = "P1", position = 0, overrideCalendar = true, showAllDayEvents = false),
            // Inherits the global default (true) — queried with includeAllDay=true.
            ProfileEntity(id = 2L, name = "P2", position = 1, overrideCalendar = false),
        )

        // When observing previews
        val result = fixture.useCase().first()

        // Then each profile's events reflect its own effective setting, not a shared/stale one
        assertEquals(listOf(overriddenEvent), result.getValue(1L).calendarEvents)
        assertEquals(listOf(overriddenEvent, inheritedEvent), result.getValue(2L).calendarEvents)
    }

    @Test
    fun `calendar events reflect a profile's own selected calendar ids when overriding`() = runTest {
        // Given calendar access granted, and a profile overriding with its own calendar selection
        val fixture = Fixture()
        `when`(fixture.calendarPermissionRepository.isGranted()).thenReturn(true)
        val event = CalendarEvent(id = 1L, calendarId = "9", title = "Standup", startTimeMillis = 1_000L, endTimeMillis = 2_000L, isAllDay = false)
        `when`(fixture.calendarRepository.getTodayEvents(setOf("9"), true)).thenReturn(listOf(event))
        fixture.settingsFlow.value = LauncherSettings(selectedCalendarIds = setOf("1", "2"))
        fixture.profilesFlow.value = listOf(
            ProfileEntity(id = 1L, name = "P1", position = 0, overrideCalendar = true, selectedCalendarIdsCsv = "9"),
        )

        // When observing previews
        val result = fixture.useCase().first()

        // Then the profile's own selected calendar ids are used, not the global setting
        assertEquals(listOf(event), result.getValue(1L).calendarEvents)
    }
}
