package com.facetlauncher.app.data

import com.facetlauncher.app.data.local.FacetDockAppDao
import com.facetlauncher.app.data.local.FacetDockAppEntity
import com.facetlauncher.app.data.local.FolderEntity
import com.facetlauncher.app.data.model.AppInfo
import com.facetlauncher.app.data.model.AppProfile
import com.facetlauncher.app.data.model.PlacedItem
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test
import org.mockito.Mockito.mock
import org.mockito.Mockito.`when`

/** In-memory fake — mirrors `FavoriteAppRepositoryTest`'s `FakeFavoriteAppDao`. */
private class FakeFacetDockAppDao : FacetDockAppDao {
    private val state = MutableStateFlow<List<FacetDockAppEntity>>(emptyList())

    override fun observeForFacet(facetId: Long): Flow<List<FacetDockAppEntity>> =
        state.map { entries -> entries.filter { it.facetId == facetId } }

    override suspend fun upsert(dockApp: FacetDockAppEntity): Long {
        state.value = state.value.filterNot {
            it.facetId == dockApp.facetId && it.packageName == dockApp.packageName && it.activityName == dockApp.activityName && it.profile == dockApp.profile
        } + dockApp
        return 0
    }

    override suspend fun delete(dockApp: FacetDockAppEntity) {
        state.value = state.value.filterNot { it.id == dockApp.id }
    }

    override suspend fun deleteByComponent(facetId: Long, packageName: String, activityName: String, profile: AppProfile) {
        state.value = state.value.filterNot {
            it.facetId == facetId && it.packageName == packageName && it.activityName == activityName && it.profile == profile
        }
    }

    override suspend fun deleteByPackage(packageName: String, profile: AppProfile) {
        state.value = state.value.filterNot { it.packageName == packageName && it.profile == profile }
    }

    override suspend fun deleteByProfile(profile: AppProfile) {
        state.value = state.value.filterNot { it.profile == profile }
    }

    override suspend fun deleteAllForFacet(facetId: Long) {
        state.value = state.value.filterNot { it.facetId == facetId }
    }
}

class FacetDockAppRepositoryTest {

    private fun appInfo(letter: Char) =
        AppInfo(packageName = "com.example.$letter", activityName = ".Main", label = "$letter App", icon = null)

    private fun repository(appRepository: AppRepository, dao: FacetDockAppDao = FakeFacetDockAppDao()): FacetDockAppRepository {
        val folderRepository = FolderRepository(FakeFolderDao(), appRepository)
        return FacetDockAppRepository(dao, FakeFacetDockFolderPlacementDao(), folderRepository, appRepository)
    }

    @Test
    fun `dock list reflects stored entries hydrated against installed apps, ordered by position`() = runTest {
        // Given two installed apps and dock entries for one facet, out of position order
        val a = appInfo('a')
        val b = appInfo('b')
        val appRepository = mock(AppRepository::class.java)
        `when`(appRepository.observeInstalledApps()).thenReturn(flowOf(listOf(a, b)))
        val repository = repository(appRepository)
        repository.addDockApp(facetId = 1, app = b, position = 1)
        repository.addDockApp(facetId = 1, app = a, position = 0)

        // When observing that facet's dock
        val result = repository.observeDockAppsForFacet(1).first()

        // Then it returns the hydrated AppInfo list, ordered by position
        assertEquals(listOf(a, b), result)
    }

    @Test
    fun `an entry whose app is no longer installed is filtered out`() = runTest {
        val appRepository = mock(AppRepository::class.java)
        `when`(appRepository.observeInstalledApps()).thenReturn(flowOf(emptyList()))
        val repository = repository(appRepository)
        repository.addDockApp(facetId = 1, app = appInfo('a'), position = 0)

        // Then the unresolvable entry is collapsed out, not shown as a dead tile
        assertEquals(emptyList<AppInfo>(), repository.observeDockAppsForFacet(1).first())
    }

    @Test
    fun `dock apps are scoped per facet, not shared`() = runTest {
        val a = appInfo('a')
        val appRepository = mock(AppRepository::class.java)
        `when`(appRepository.observeInstalledApps()).thenReturn(flowOf(listOf(a)))
        val repository = repository(appRepository)
        repository.addDockApp(facetId = 1, app = a, position = 0)

        assertEquals(listOf(a), repository.observeDockAppsForFacet(1).first())
        assertEquals(emptyList<AppInfo>(), repository.observeDockAppsForFacet(2).first())
    }

    @Test
    fun `removeDockApp drops it from the facet's dock`() = runTest {
        val a = appInfo('a')
        val appRepository = mock(AppRepository::class.java)
        `when`(appRepository.observeInstalledApps()).thenReturn(flowOf(listOf(a)))
        val repository = repository(appRepository)
        repository.addDockApp(facetId = 1, app = a, position = 0)

        repository.removeDockApp(facetId = 1, app = a)

        assertEquals(emptyList<AppInfo>(), repository.observeDockAppsForFacet(1).first())
    }

    @Test
    fun `observeDockAppsForFacets returns each facet's own hydrated list, keyed by id`() = runTest {
        val a = appInfo('a')
        val b = appInfo('b')
        val appRepository = mock(AppRepository::class.java)
        `when`(appRepository.observeInstalledApps()).thenReturn(flowOf(listOf(a, b)))
        val repository = repository(appRepository)
        repository.addDockApp(facetId = 1, app = a, position = 0)
        repository.addDockApp(facetId = 2, app = b, position = 0)

        val result = repository.observeDockAppsForFacets(listOf(1, 2)).first()

        assertEquals(listOf(a), result[1])
        assertEquals(listOf(b), result[2])
    }

    @Test
    fun `observeDockAppsForFacets with an empty id list emits an empty map`() = runTest {
        val appRepository = mock(AppRepository::class.java)
        val repository = repository(appRepository)

        assertEquals(emptyMap<Long, List<AppInfo>>(), repository.observeDockAppsForFacets(emptyList()).first())
    }

    @Test
    fun `replaceItems wipes the facet's existing dock and inserts the new one in order`() = runTest {
        val a = appInfo('a')
        val b = appInfo('b')
        val c = appInfo('c')
        val appRepository = mock(AppRepository::class.java)
        `when`(appRepository.observeInstalledApps()).thenReturn(flowOf(listOf(a, b, c)))
        val repository = repository(appRepository)
        repository.addDockApp(facetId = 1, app = c, position = 0)

        repository.replaceItems(facetId = 1, items = listOf(PlacedItem.SingleApp(b), PlacedItem.SingleApp(a)))

        assertEquals(listOf(b, a), repository.observeDockAppsForFacet(1).first())
    }

    @Test
    fun `replaceItems carries a folder placement, not just apps`() = runTest {
        val a = appInfo('a')
        val appRepository = mock(AppRepository::class.java)
        `when`(appRepository.observeInstalledApps()).thenReturn(flowOf(listOf(a)))
        val folderDao = FakeFolderDao()
        val folderId = folderDao.insertFolder(FolderEntity(name = "Games"))
        val folderRepository = FolderRepository(folderDao, appRepository)
        val repository = FacetDockAppRepository(FakeFacetDockAppDao(), FakeFacetDockFolderPlacementDao(), folderRepository, appRepository)

        val folder = com.facetlauncher.app.data.model.Folder(folderId, "Games", emptyList())
        repository.replaceItems(facetId = 1, items = listOf(PlacedItem.SingleApp(a), PlacedItem.FolderItem(folder)))

        val items = repository.observeDockItems(1).first()
        assertEquals(listOf(PlacedItem.SingleApp(a), PlacedItem.FolderItem(folder)), items)
    }

    @Test
    fun `dock collapses live when AppRepository reports an uninstall, without resubscribing`() = runTest {
        val a = appInfo('a')
        val b = appInfo('b')
        val appRepository = mock(AppRepository::class.java)
        val installedApps = MutableStateFlow(listOf(a, b))
        `when`(appRepository.observeInstalledApps()).thenReturn(installedApps)
        val repository = repository(appRepository)
        repository.addDockApp(facetId = 1, app = a, position = 0)
        repository.addDockApp(facetId = 1, app = b, position = 1)

        val emissions = Channel<List<AppInfo>>(Channel.UNLIMITED)
        val collectJob = launch { repository.observeDockAppsForFacet(1).collect { emissions.send(it) } }
        assertEquals(listOf(a, b), emissions.receive())

        installedApps.value = listOf(a)

        assertEquals(listOf(a), emissions.receive())
        collectJob.cancel()
    }
}
