package com.facetlauncher.app.domain

import com.facetlauncher.app.data.CalendarPermissionRepository
import com.facetlauncher.app.data.CalendarRepository
import com.facetlauncher.app.data.DefaultFavoriteAppRepository
import com.facetlauncher.app.data.DockAppRepository
import com.facetlauncher.app.data.FavoriteAppRepository
import com.facetlauncher.app.data.NotificationAccessRepository
import com.facetlauncher.app.data.NotificationBadgeRepository
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
import org.mockito.Mockito.mock
import org.mockito.Mockito.`when`
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class ObserveHomeScreenStateUseCaseTest {

    private fun appInfo(letter: Char) =
        AppInfo(packageName = "com.example.$letter", activityName = ".Main", label = "$letter App", icon = null)

    private class Fixture {
        val settingsFlow = MutableStateFlow(LauncherSettings(activeFacetId = 1L))
        val settingsRepository = mock(SettingsRepository::class.java).also { `when`(it.settings).thenReturn(settingsFlow) }
        val dockAppRepository = mock(DockAppRepository::class.java).also { `when`(it.observeDockItems()).thenReturn(flowOf(emptyList())) }
        val facetDockAppRepository = mock(FacetDockAppRepository::class.java)
        val favoriteAppRepository = mock(FavoriteAppRepository::class.java)
        val defaultFavoriteAppRepository = mock(DefaultFavoriteAppRepository::class.java)
            .also { `when`(it.observeDefaultItems()).thenReturn(flowOf(emptyList())) }
        val facetsFlow = MutableStateFlow<List<FacetEntity>>(emptyList())
        val facetRepository = mock(FacetRepository::class.java).also { `when`(it.observeFacets()).thenReturn(facetsFlow) }
        val usageStatsRepository = mock(UsageStatsRepository::class.java)
        val usageAccessRepository = mock(UsageAccessRepository::class.java).also { `when`(it.isGranted()).thenReturn(true) }
        val calendarPermissionRepository = mock(CalendarPermissionRepository::class.java).also { `when`(it.isGranted()).thenReturn(false) }
        val calendarRepository = mock(CalendarRepository::class.java)
        val notificationBadgeRepository = mock(NotificationBadgeRepository::class.java)
            .also { `when`(it.badgeCounts).thenReturn(MutableStateFlow(emptyMap())) }
        val notificationAccessRepository = mock(NotificationAccessRepository::class.java)
            .also { `when`(it.isGranted()).thenReturn(false) }
        val observeClockAccessories = mock(ObserveClockAccessoriesUseCase::class.java)
            .also { `when`(it.invoke()).thenReturn(flowOf(ClockAccessoryState(nextAlarmMillis = null, batteryPercent = 0, isCharging = false))) }

        val useCase = ObserveHomeScreenStateUseCase(
            settingsRepository,
            dockAppRepository,
            facetDockAppRepository,
            favoriteAppRepository,
            defaultFavoriteAppRepository,
            facetRepository,
            usageStatsRepository,
            usageAccessRepository,
            calendarPermissionRepository,
            calendarRepository,
            notificationBadgeRepository,
            notificationAccessRepository,
            observeClockAccessories,
        )
    }

    @Test
    fun `emits the active facet's own favorites when it overrides them`() = runTest {
        // Given settings pointing at facet 1, overriding its own favorites, resolving to one app
        val fixture = Fixture()
        `when`(fixture.favoriteAppRepository.observeFavoriteItems(1L)).thenReturn(flowOf(listOf(PlacedItem.SingleApp(appInfo('a')))))
        fixture.facetsFlow.value = listOf(FacetEntity(id = 1L, name = "P1", position = 0, overridingFavorites = true))

        // When observing home screen state
        val result = fixture.useCase().first()

        // Then it exposes facet 1's own favorites
        assertEquals(listOf(PlacedItem.SingleApp(appInfo('a'))), result.appListItems)
    }

    @Test
    fun `emits the launcher-wide default favorites when the active facet isn't overriding`() = runTest {
        // Given settings pointing at facet 1, which hasn't overridden its own favorites
        val fixture = Fixture()
        `when`(fixture.defaultFavoriteAppRepository.observeDefaultItems()).thenReturn(flowOf(listOf(PlacedItem.SingleApp(appInfo('d')))))
        fixture.facetsFlow.value = listOf(FacetEntity(id = 1L, name = "P1", position = 0, overridingFavorites = false))

        // When observing home screen state
        val result = fixture.useCase().first()

        // Then it exposes the default list, not a per-facet one
        assertEquals(listOf(PlacedItem.SingleApp(appInfo('d'))), result.appListItems)
    }

    @Test
    fun `re-subscribes to the new facet's favorites when the active facet changes`() = runTest {
        // Given facet 1 active initially, each facet overriding with its own distinct favorite
        val fixture = Fixture()
        `when`(fixture.favoriteAppRepository.observeFavoriteItems(1L)).thenReturn(flowOf(listOf(PlacedItem.SingleApp(appInfo('a')))))
        `when`(fixture.favoriteAppRepository.observeFavoriteItems(2L)).thenReturn(flowOf(listOf(PlacedItem.SingleApp(appInfo('b')))))
        fixture.facetsFlow.value = listOf(
            FacetEntity(id = 1L, name = "P1", position = 0, overridingFavorites = true),
            FacetEntity(id = 2L, name = "P2", position = 1, overridingFavorites = true),
        )

        // When switching the active facet to 2
        fixture.settingsFlow.value = LauncherSettings(activeFacetId = 2L)
        val result = fixture.useCase().first()

        // Then favorites reflect facet 2, not the stale facet-1 value
        assertEquals(listOf(PlacedItem.SingleApp(appInfo('b'))), result.appListItems)
    }

    @Test
    fun `favoriteItems resolves the real favorites list even in Recents mode`() = runTest {
        // Given a facet set to Recents (so appListItems won't be Favorites), but with real
        // favorites of its own on the launcher-wide default list
        val fixture = Fixture()
        fixture.facetsFlow.value = listOf(
            FacetEntity(id = 1L, name = "P1", position = 0, overrideApps = true, listContentMode = ListContentMode.RECENTS, appsToShowCount = 6),
        )
        `when`(fixture.usageStatsRepository.getRecentApps(6)).thenReturn(emptyList())
        `when`(fixture.defaultFavoriteAppRepository.observeDefaultItems()).thenReturn(flowOf(listOf(PlacedItem.SingleApp(appInfo('f')))))

        // When observing home screen state
        val result = fixture.useCase().first()

        // Then favoriteItems still exposes the real Favorites list, independent of appListItems
        // (which is Recents-shaped here) — this is what lets HomeViewModel.quickAddStateForApp
        // resolve the Favorites row without a fresh repository read regardless of Home's display mode.
        assertEquals(emptyList<PlacedItem>(), result.appListItems)
        assertEquals(listOf(PlacedItem.SingleApp(appInfo('f'))), result.favoriteItems)
    }

    @Test
    fun `recents mode reads from UsageStatsRepository when usage access is granted`() = runTest {
        // Given a facet set to Recents, usage access granted
        val fixture = Fixture()
        fixture.facetsFlow.value = listOf(
            FacetEntity(id = 1L, name = "P1", position = 0, overrideApps = true, listContentMode = ListContentMode.RECENTS, appsToShowCount = 6),
        )
        `when`(fixture.usageStatsRepository.getRecentApps(6)).thenReturn(listOf(appInfo('r')))

        // When observing home screen state
        val result = fixture.useCase().first()

        // Then it exposes the ranked recents, not favorites
        assertEquals(listOf(PlacedItem.SingleApp(appInfo('r'))), result.appListItems)
        assertEquals(true, result.usageAccessGranted)
    }

    @Test
    fun `most used mode reads from UsageStatsRepository when usage access is granted`() = runTest {
        // Given a facet set to Most Used, usage access granted
        val fixture = Fixture()
        fixture.facetsFlow.value = listOf(
            FacetEntity(id = 1L, name = "P1", position = 0, overrideApps = true, listContentMode = ListContentMode.MOST_USED, appsToShowCount = 4),
        )
        `when`(fixture.usageStatsRepository.getMostUsedApps(4)).thenReturn(listOf(appInfo('m')))

        // When observing home screen state
        val result = fixture.useCase().first()

        // Then it exposes the ranked most-used apps
        assertEquals(listOf(PlacedItem.SingleApp(appInfo('m'))), result.appListItems)
    }

    @Test
    fun `recents mode without usage access yields an empty list and reports ungranted`() = runTest {
        // Given a facet set to Recents, usage access NOT granted
        val fixture = Fixture()
        `when`(fixture.usageAccessRepository.isGranted()).thenReturn(false)
        fixture.facetsFlow.value = listOf(
            FacetEntity(id = 1L, name = "P1", position = 0, overrideApps = true, listContentMode = ListContentMode.RECENTS),
        )

        // When observing home screen state
        val result = fixture.useCase().first()

        // Then the list is empty (never a broken partial list) and the ungranted state is surfaced
        assertEquals(emptyList<PlacedItem>(), result.appListItems)
        assertEquals(false, result.usageAccessGranted)
    }

    @Test
    fun `emits the active facet's own dock when it overrides the dock`() = runTest {
        // Given facet 1 active and overriding its dock, resolving to one app
        val fixture = Fixture()
        `when`(fixture.facetDockAppRepository.observeDockItems(1L)).thenReturn(flowOf(listOf(PlacedItem.SingleApp(appInfo('p')))))
        fixture.facetsFlow.value = listOf(FacetEntity(id = 1L, name = "P1", position = 0, overrideDock = true))

        // When observing home screen state
        val result = fixture.useCase().first()

        // Then the dock is the facet's own list, not the launcher-wide default
        assertEquals(listOf(PlacedItem.SingleApp(appInfo('p'))), result.dockApps)
    }

    @Test
    fun `emits the launcher-wide dock when the active facet isn't overriding the dock`() = runTest {
        // Given facet 1 active, NOT overriding its dock, and a non-empty default dock
        val fixture = Fixture()
        `when`(fixture.dockAppRepository.observeDockItems()).thenReturn(flowOf(listOf(PlacedItem.SingleApp(appInfo('g')))))
        fixture.facetsFlow.value = listOf(FacetEntity(id = 1L, name = "P1", position = 0, overrideDock = false))

        // When observing home screen state
        val result = fixture.useCase().first()

        // Then the dock is the launcher-wide default, and the per-facet dock is never consulted
        assertEquals(listOf(PlacedItem.SingleApp(appInfo('g'))), result.dockApps)
    }

    @Test
    fun `calendar events are empty when calendar access isn't granted`() = runTest {
        // Given calendar access ungranted (the fixture default)
        val fixture = Fixture()
        fixture.facetsFlow.value = listOf(FacetEntity(id = 1L, name = "P1", position = 0))

        // When observing home screen state
        val result = fixture.useCase().first()

        // Then no calendar events are exposed, and CalendarRepository is never queried
        assertEquals(emptyList<CalendarEvent>(), result.calendarEvents)
    }

    @Test
    fun `calendar events reflect the active facet's effective show-all-day setting when granted`() = runTest {
        // Given calendar access granted, and a facet that overrides all-day events off
        val fixture = Fixture()
        `when`(fixture.calendarPermissionRepository.isGranted()).thenReturn(true)
        val event = CalendarEvent(id = 1L, calendarId = "1", title = "Standup", startTimeMillis = 1_000L, endTimeMillis = 2_000L, isAllDay = false)
        `when`(fixture.calendarRepository.getTodayEvents(null, false)).thenReturn(listOf(event))
        fixture.facetsFlow.value = listOf(
            FacetEntity(id = 1L, name = "P1", position = 0, overrideCalendar = true, showAllDayEvents = false),
        )

        // When observing home screen state
        val result = fixture.useCase().first()

        // Then it queries with the facet's effective (overridden) setting and exposes the result
        assertEquals(listOf(event), result.calendarEvents)
    }

    @Test
    fun `calendar events reflect the active facet's own selected calendar ids when overriding`() = runTest {
        // Given calendar access granted, and a facet overriding with its own calendar selection
        val fixture = Fixture()
        `when`(fixture.calendarPermissionRepository.isGranted()).thenReturn(true)
        val event = CalendarEvent(id = 1L, calendarId = "9", title = "Standup", startTimeMillis = 1_000L, endTimeMillis = 2_000L, isAllDay = false)
        `when`(fixture.calendarRepository.getTodayEvents(setOf("9"), true)).thenReturn(listOf(event))
        fixture.settingsFlow.value = LauncherSettings(activeFacetId = 1L, selectedCalendarIds = setOf("1", "2"))
        fixture.facetsFlow.value = listOf(
            FacetEntity(id = 1L, name = "P1", position = 0, overrideCalendar = true, selectedCalendarIdsCsv = "9"),
        )

        // When observing home screen state
        val result = fixture.useCase().first()

        // Then it queries with the facet's own selected calendar ids, not the global setting
        assertEquals(listOf(event), result.calendarEvents)
    }
}
