package com.facetlauncher.app.data

import com.facetlauncher.app.data.local.DefaultFavoriteAppDao
import com.facetlauncher.app.data.local.DefaultFavoriteAppEntity
import com.facetlauncher.app.data.local.FolderEntity
import com.facetlauncher.app.data.model.AppInfo
import com.facetlauncher.app.data.model.PlacedItem
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test
import org.mockito.Mockito.mock
import org.mockito.Mockito.`when`

/** In-memory fake — simpler than mocking every [DefaultFavoriteAppDao] method for this repository's needs. */
private class FakeDefaultFavoriteAppDao : DefaultFavoriteAppDao {
    private val state = MutableStateFlow<List<DefaultFavoriteAppEntity>>(emptyList())

    override fun observeAll(): Flow<List<DefaultFavoriteAppEntity>> = state

    override suspend fun upsert(defaultFavoriteApp: DefaultFavoriteAppEntity): Long {
        state.value = state.value.filterNot {
            it.packageName == defaultFavoriteApp.packageName && it.activityName == defaultFavoriteApp.activityName
        } + defaultFavoriteApp
        return 0
    }

    override suspend fun delete(defaultFavoriteApp: DefaultFavoriteAppEntity) {
        state.value = state.value.filterNot { it.id == defaultFavoriteApp.id }
    }

    override suspend fun deleteByComponent(packageName: String, activityName: String) {
        state.value = state.value.filterNot { it.packageName == packageName && it.activityName == activityName }
    }

    override suspend fun deleteByPackage(packageName: String) {
        state.value = state.value.filterNot { it.packageName == packageName }
    }

    override suspend fun deleteAll() {
        state.value = emptyList()
    }
}

class DefaultFavoriteAppRepositoryTest {

    private fun appInfo(letter: Char) =
        AppInfo(packageName = "com.example.$letter", activityName = ".Main", label = "$letter App", icon = null)

    private fun repository(dao: DefaultFavoriteAppDao, appRepository: AppRepository): DefaultFavoriteAppRepository {
        val folderRepository = FolderRepository(FakeFolderDao(), appRepository)
        return DefaultFavoriteAppRepository(dao, FakeDefaultFavoriteFolderPlacementDao(), folderRepository, appRepository)
    }

    @Test
    fun `default favorites list reflects stored entries hydrated against installed apps, ordered by position`() = runTest {
        // Given two installed apps and default-favorite entries referencing them out of position order
        val a = appInfo('a')
        val b = appInfo('b')
        val appRepository = mock(AppRepository::class.java)
        `when`(appRepository.observeInstalledApps()).thenReturn(flowOf(listOf(a, b)))
        val dao = FakeDefaultFavoriteAppDao()
        dao.upsert(DefaultFavoriteAppEntity(packageName = b.packageName, activityName = b.activityName, position = 1))
        dao.upsert(DefaultFavoriteAppEntity(packageName = a.packageName, activityName = a.activityName, position = 0))

        // When observing the default favorites list
        val result = repository(dao, appRepository).observeDefaultFavorites().first()

        // Then it returns the hydrated AppInfo list, ordered by position
        assertEquals(listOf(a, b), result)
    }

    @Test
    fun `an entry whose app is no longer installed is filtered out`() = runTest {
        // Given a default-favorite entry for an app that AppRepository no longer reports as installed
        val appRepository = mock(AppRepository::class.java)
        `when`(appRepository.observeInstalledApps()).thenReturn(flowOf(emptyList()))
        val dao = FakeDefaultFavoriteAppDao()
        dao.upsert(DefaultFavoriteAppEntity(packageName = "com.example.uninstalled", activityName = ".Main", position = 0))

        // When observing the default favorites list
        val result = repository(dao, appRepository).observeDefaultFavorites().first()

        // Then the unresolvable entry is collapsed out, not shown as a dead tile
        assertEquals(emptyList<AppInfo>(), result)
    }

    @Test
    fun `default favorites collapse live when AppRepository reports an uninstall, without resubscribing`() = runTest {
        // Given a default-favorite app that's currently installed
        val a = appInfo('a')
        val appRepository = mock(AppRepository::class.java)
        val installedApps = MutableStateFlow(listOf(a))
        `when`(appRepository.observeInstalledApps()).thenReturn(installedApps)
        val dao = FakeDefaultFavoriteAppDao()
        dao.upsert(DefaultFavoriteAppEntity(packageName = a.packageName, activityName = a.activityName, position = 0))

        val emissions = Channel<List<AppInfo>>(Channel.UNLIMITED)
        val collectJob = launch { repository(dao, appRepository).observeDefaultFavorites().collect { emissions.send(it) } }
        assertEquals(listOf(a), emissions.receive())

        // When AppRepository's live flow reports it uninstalled (F12's long-press menu, or any
        // other uninstall while Facet is in the foreground)
        installedApps.value = emptyList()

        // Then it collapses out immediately, without the flow needing to resubscribe
        assertEquals(emptyList<AppInfo>(), emissions.receive())
        collectJob.cancel()
    }

    @Test
    fun `reorderItems persists the new order`() = runTest {
        // Given two default favorites in one order
        val a = appInfo('a')
        val b = appInfo('b')
        val appRepository = mock(AppRepository::class.java)
        `when`(appRepository.observeInstalledApps()).thenReturn(flowOf(listOf(a, b)))
        val dao = FakeDefaultFavoriteAppDao()
        val repository = repository(dao, appRepository)
        repository.addFavorite(a, position = 0)
        repository.addFavorite(b, position = 1)

        // When reordered
        repository.reorderItems(listOf(PlacedItem.SingleApp(b), PlacedItem.SingleApp(a)))

        // Then the new order is reflected
        assertEquals(listOf(b, a), repository.observeDefaultFavorites().first())
    }

    @Test
    fun `a folder placed in the default favorites list is never dropped from observeDefaultItems even with zero apps`() = runTest {
        // Given a folder with no members, placed in the default favorites list
        val appRepository = mock(AppRepository::class.java)
        `when`(appRepository.observeInstalledApps()).thenReturn(flowOf(emptyList()))
        val dao = FakeDefaultFavoriteAppDao()
        val folderDao = FakeFolderDao()
        val folderId = folderDao.insertFolder(FolderEntity(name = "Games"))
        val folderRepository = FolderRepository(folderDao, appRepository)
        val repository = DefaultFavoriteAppRepository(dao, FakeDefaultFavoriteFolderPlacementDao(), folderRepository, appRepository)

        // When placing it
        repository.placeFolder(folderId, position = 0)

        // Then it's still emitted, not hidden for having zero apps
        val folder = com.facetlauncher.app.data.model.Folder(folderId, "Games", emptyList())
        assertEquals(listOf(PlacedItem.FolderItem(folder)), repository.observeDefaultItems().first())
    }

    @Test
    fun `removeFavorite drops it from the list`() = runTest {
        // Given a default-favorite app
        val a = appInfo('a')
        val appRepository = mock(AppRepository::class.java)
        `when`(appRepository.observeInstalledApps()).thenReturn(flowOf(listOf(a)))
        val dao = FakeDefaultFavoriteAppDao()
        val repository = repository(dao, appRepository)
        repository.addFavorite(a, position = 0)

        // When it is removed
        repository.removeFavorite(a)

        // Then it's gone
        assertEquals(emptyList<AppInfo>(), repository.observeDefaultFavorites().first())
    }
}
