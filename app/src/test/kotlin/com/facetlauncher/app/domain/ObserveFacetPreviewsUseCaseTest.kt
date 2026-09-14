package com.facetlauncher.app.domain

import com.facetlauncher.app.data.CalendarPermissionRepository
import com.facetlauncher.app.data.CalendarRepository
import com.facetlauncher.app.data.DefaultFavoriteAppRepository
import com.facetlauncher.app.data.DockAppRepository
import com.facetlauncher.app.data.FavoriteAppRepository
import com.facetlauncher.app.data.FacetDockAppRepository
import com.facetlauncher.app.data.FacetRepository
import com.facetlauncher.app.data.SettingsRepository
import com.facetlauncher.app.data.UsageAccessRepository
import com.facetlauncher.app.data.UsageStatsRepository
import com.facetlauncher.app.data.local.FacetEntity
import com.facetlauncher.app.data.model.AppInfo
import com.facetlauncher.app.data.model.CalendarEvent
import com.facetlauncher.app.data.model.LauncherSettings
import com.facetlauncher.app.data.model.ListContentMode
import com.facetlauncher.app.data.model.PlacedItem
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test
import org.mockito.ArgumentMatchers.anyList
import org.mockito.Mockito.mock
import org.mockito.Mockito.`when`

class ObserveFacetPreviewsUseCaseTest {

    private fun appInfo(letter: Char) =
        AppInfo(packageName = "com.example.$letter", activityName = ".Main", label = "$letter App", icon = null)

    private class Fixture {
        val settingsFlow = MutableStateFlow(LauncherSettings())
        val settingsRepository = mock(SettingsRepository::class.java).also { `when`(it.settings).thenReturn(settingsFlow) }
        val favoriteAppRepository = mock(FavoriteAppRepository::class.java)
            .also { `when`(it.observeFavoriteItemsForFacets(anyList())).thenReturn(flowOf(emptyMap())) }
        val defaultFavoriteAppRepository = mock(DefaultFavoriteAppRepository::class.java)
            .also { `when`(it.observeDefaultItems()).thenReturn(flowOf(emptyList())) }
        val facetDockAppRepository = mock(FacetDockAppRepository::class.java)
            .also { `when`(it.observeDockItemsForFacets(anyList())).thenReturn(flowOf(emptyMap())) }
        val dockAppRepository = mock(DockAppRepository::class.java)
            .also { `when`(it.observeDockItems()).thenReturn(flowOf(emptyList())) }
        val facetsFlow = MutableStateFlow<List<FacetEntity>>(emptyList())
        val facetRepository = mock(FacetRepository::class.java).also { `when`(it.observeFacets()).thenReturn(facetsFlow) }
        val usageStatsRepository = mock(UsageStatsRepository::class.java)
        val usageAccessRepository = mock(UsageAccessRepository::class.java).also { `when`(it.isGranted()).thenReturn(true) }
        val calendarPermissionRepository = mock(CalendarPermissionRepository::class.java).also { `when`(it.isGranted()).thenReturn(false) }
        val calendarRepository = mock(CalendarRepository::class.java)

        val useCase = ObserveFacetPreviewsUseCase(
            facetRepository,
            settingsRepository,
            favoriteAppRepository,
            defaultFavoriteAppRepository,
            facetDockAppRepository,
            dockAppRepository,
            usageStatsRepository,
            usageAccessRepository,
            calendarPermissionRepository,
            calendarRepository,
        )
    }

    @Test
    fun `resolves each facet's own effective favorites independently`() = runTest {
        // Given two facets, one overriding its own favorites, one inheriting the default list
        val fixture = Fixture()
        `when`(fixture.favoriteAppRepository.observeFavoriteItemsForFacets(listOf(1L, 2L)))
            .thenReturn(flowOf(mapOf(1L to listOf(PlacedItem.SingleApp(appInfo('a'))), 2L to emptyList())))
        `when`(fixture.defaultFavoriteAppRepository.observeDefaultItems()).thenReturn(flowOf(listOf(PlacedItem.SingleApp(appInfo('d')))))
        fixture.facetsFlow.value = listOf(
            FacetEntity(id = 1L, name = "P1", position = 0, overridingFavorites = true),
            FacetEntity(id = 2L, name = "P2", position = 1, overridingFavorites = false),
        )

        // When observing previews for every facet
        val result = fixture.useCase().first()

        // Then each resolves to its own effective list — facet 1's own override, facet 2's default
        assertEquals(listOf(PlacedItem.SingleApp(appInfo('a'))), result.getValue(1L).favorites)
        assertEquals(listOf(PlacedItem.SingleApp(appInfo('d'))), result.getValue(2L).favorites)
    }

    @Test
    fun `resolves each facet's own effective dock independently`() = runTest {
        // Given two facets, one overriding its dock, one inheriting the launcher-wide default
        val fixture = Fixture()
        `when`(fixture.facetDockAppRepository.observeDockItemsForFacets(listOf(1L, 2L)))
            .thenReturn(flowOf(mapOf(1L to listOf(PlacedItem.SingleApp(appInfo('a'))), 2L to emptyList())))
        `when`(fixture.dockAppRepository.observeDockItems()).thenReturn(flowOf(listOf(PlacedItem.SingleApp(appInfo('d')))))
        fixture.facetsFlow.value = listOf(
            FacetEntity(id = 1L, name = "P1", position = 0, overrideDock = true),
            FacetEntity(id = 2L, name = "P2", position = 1, overrideDock = false),
        )

        // When observing previews for every facet
        val result = fixture.useCase().first()

        // Then each resolves to its own effective dock — facet 1's own list, facet 2's default
        assertEquals(listOf(PlacedItem.SingleApp(appInfo('a'))), result.getValue(1L).dockApps)
        assertEquals(listOf(PlacedItem.SingleApp(appInfo('d'))), result.getValue(2L).dockApps)
    }

    @Test
    fun `recents mode without usage access yields an empty favorites list for that facet`() = runTest {
        // Given a facet set to Recents, usage access not granted
        val fixture = Fixture()
        `when`(fixture.usageAccessRepository.isGranted()).thenReturn(false)
        fixture.facetsFlow.value = listOf(
            FacetEntity(id = 1L, name = "P1", position = 0, overrideApps = true, listContentMode = ListContentMode.RECENTS),
        )

        // When observing previews
        val result = fixture.useCase().first()

        // Then the list is empty rather than a broken partial one
        assertEquals(emptyList<PlacedItem>(), result.getValue(1L).favorites)
    }

    @Test
    fun `calendar events are empty for every facet when calendar access isn't granted`() = runTest {
        // Given calendar access ungranted (the fixture default) and two facets
        val fixture = Fixture()
        fixture.facetsFlow.value = listOf(
            FacetEntity(id = 1L, name = "P1", position = 0),
            FacetEntity(id = 2L, name = "P2", position = 1),
        )

        // When observing previews
        val result = fixture.useCase().first()

        // Then neither facet exposes any events, and CalendarRepository is never queried
        assertEquals(emptyList<CalendarEvent>(), result.getValue(1L).calendarEvents)
        assertEquals(emptyList<CalendarEvent>(), result.getValue(2L).calendarEvents)
    }

    @Test
    fun `calendar events reflect each facet's own effective show-all-day setting when granted`() = runTest {
        // Given calendar access granted, and two facets with different effective all-day settings
        val fixture = Fixture()
        `when`(fixture.calendarPermissionRepository.isGranted()).thenReturn(true)
        val overriddenEvent = CalendarEvent(id = 1L, calendarId = "1", title = "Standup", startTimeMillis = 1_000L, endTimeMillis = 2_000L, isAllDay = false)
        val inheritedEvent = CalendarEvent(id = 2L, calendarId = "1", title = "All day", startTimeMillis = 3_000L, endTimeMillis = 4_000L, isAllDay = true)
        `when`(fixture.calendarRepository.getTodayEvents(null, false)).thenReturn(listOf(overriddenEvent))
        `when`(fixture.calendarRepository.getTodayEvents(null, true)).thenReturn(listOf(overriddenEvent, inheritedEvent))
        fixture.facetsFlow.value = listOf(
            // Overrides all-day events off — queried with includeAllDay=false.
            FacetEntity(id = 1L, name = "P1", position = 0, overrideCalendar = true, showAllDayEvents = false),
            // Inherits the global default (true) — queried with includeAllDay=true.
            FacetEntity(id = 2L, name = "P2", position = 1, overrideCalendar = false),
        )

        // When observing previews
        val result = fixture.useCase().first()

        // Then each facet's events reflect its own effective setting, not a shared/stale one
        assertEquals(listOf(overriddenEvent), result.getValue(1L).calendarEvents)
        assertEquals(listOf(overriddenEvent, inheritedEvent), result.getValue(2L).calendarEvents)
    }

    @Test
    fun `calendar events reflect a facet's own selected calendar ids when overriding`() = runTest {
        // Given calendar access granted, and a facet overriding with its own calendar selection
        val fixture = Fixture()
        `when`(fixture.calendarPermissionRepository.isGranted()).thenReturn(true)
        val event = CalendarEvent(id = 1L, calendarId = "9", title = "Standup", startTimeMillis = 1_000L, endTimeMillis = 2_000L, isAllDay = false)
        `when`(fixture.calendarRepository.getTodayEvents(setOf("9"), true)).thenReturn(listOf(event))
        fixture.settingsFlow.value = LauncherSettings(selectedCalendarIds = setOf("1", "2"))
        fixture.facetsFlow.value = listOf(
            FacetEntity(id = 1L, name = "P1", position = 0, overrideCalendar = true, selectedCalendarIdsCsv = "9"),
        )

        // When observing previews
        val result = fixture.useCase().first()

        // Then the facet's own selected calendar ids are used, not the global setting
        assertEquals(listOf(event), result.getValue(1L).calendarEvents)
    }
}
