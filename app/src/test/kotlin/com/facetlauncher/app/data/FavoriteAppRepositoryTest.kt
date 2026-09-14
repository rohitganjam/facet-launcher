package com.facetlauncher.app.data

import com.facetlauncher.app.data.local.FavoriteAppDao
import com.facetlauncher.app.data.local.FavoriteAppEntity
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

/** In-memory fake — simpler than mocking every [FavoriteAppDao] method for this repository's needs. */
private class FakeFavoriteAppDao : FavoriteAppDao {
    private val state = MutableStateFlow<List<FavoriteAppEntity>>(emptyList())

    override fun observeForFacet(facetId: Long): Flow<List<FavoriteAppEntity>> =
        state.map { entries -> entries.filter { it.facetId == facetId } }

    override suspend fun upsert(favoriteApp: FavoriteAppEntity): Long {
        state.value = state.value.filterNot {
            it.facetId == favoriteApp.facetId && it.packageName == favoriteApp.packageName && it.activityName == favoriteApp.activityName && it.profile == favoriteApp.profile
        } + favoriteApp
        return 0
    }

    override suspend fun delete(favoriteApp: FavoriteAppEntity) {
        state.value = state.value.filterNot { it.id == favoriteApp.id }
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

class FavoriteAppRepositoryTest {

    private fun appInfo(letter: Char) =
        AppInfo(packageName = "com.example.$letter", activityName = ".Main", label = "$letter App", icon = null)

    private fun repository(dao: FavoriteAppDao, appRepository: AppRepository): FavoriteAppRepository {
        val folderRepository = FolderRepository(FakeFolderDao(), appRepository)
        return FavoriteAppRepository(dao, FakeFavoriteFolderPlacementDao(), folderRepository, appRepository)
    }

    @Test
    fun `favorites list reflects stored entries hydrated against installed apps, ordered by position`() = runTest {
        // Given two installed apps and favorite entries for one facet, out of position order
        val a = appInfo('a')
        val b = appInfo('b')
        val appRepository = mock(AppRepository::class.java)
        `when`(appRepository.observeInstalledApps()).thenReturn(flowOf(listOf(a, b)))
        val dao = FakeFavoriteAppDao()
        val repository = repository(dao, appRepository)
        repository.addFavorite(facetId = 1, app = b, position = 1)
        repository.addFavorite(facetId = 1, app = a, position = 0)

        // When observing that facet's favorites
        val result = repository.observeFavoritesForFacet(1).first()

        // Then it returns the hydrated AppInfo list, ordered by position
        assertEquals(listOf(a, b), result)
    }

    @Test
    fun `an entry whose app is no longer installed is filtered out`() = runTest {
        // Given a favorite entry for an app AppRepository no longer reports as installed
        val appRepository = mock(AppRepository::class.java)
        `when`(appRepository.observeInstalledApps()).thenReturn(flowOf(emptyList()))
        val dao = FakeFavoriteAppDao()
        val repository = repository(dao, appRepository)
        repository.addFavorite(facetId = 1, app = appInfo('a'), position = 0)

        // When observing that facet's favorites
        val result = repository.observeFavoritesForFacet(1).first()

        // Then the unresolvable entry is collapsed out, not shown as a dead tile
        assertEquals(emptyList<AppInfo>(), result)
    }

    @Test
    fun `favorites are scoped per facet, not shared`() = runTest {
        // Given the same app favorited under two different facets
        val a = appInfo('a')
        val appRepository = mock(AppRepository::class.java)
        `when`(appRepository.observeInstalledApps()).thenReturn(flowOf(listOf(a)))
        val dao = FakeFavoriteAppDao()
        val repository = repository(dao, appRepository)
        repository.addFavorite(facetId = 1, app = a, position = 0)

        // Then only facet 1 sees it — facet 2 sees none
        assertEquals(listOf(a), repository.observeFavoritesForFacet(1).first())
        assertEquals(emptyList<AppInfo>(), repository.observeFavoritesForFacet(2).first())
    }

    @Test
    fun `removeFavorite drops it from the facet's list`() = runTest {
        // Given a favorited app
        val a = appInfo('a')
        val appRepository = mock(AppRepository::class.java)
        `when`(appRepository.observeInstalledApps()).thenReturn(flowOf(listOf(a)))
        val dao = FakeFavoriteAppDao()
        val repository = repository(dao, appRepository)
        repository.addFavorite(facetId = 1, app = a, position = 0)

        // When it is removed
        repository.removeFavorite(facetId = 1, app = a)

        // Then it's gone
        assertEquals(emptyList<AppInfo>(), repository.observeFavoritesForFacet(1).first())
    }

    @Test
    fun `observeFavoritesForFacets returns each facet's own hydrated list, keyed by id`() = runTest {
        // Given two facets with different favorites
        val a = appInfo('a')
        val b = appInfo('b')
        val appRepository = mock(AppRepository::class.java)
        `when`(appRepository.observeInstalledApps()).thenReturn(flowOf(listOf(a, b)))
        val dao = FakeFavoriteAppDao()
        val repository = repository(dao, appRepository)
        repository.addFavorite(facetId = 1, app = a, position = 0)
        repository.addFavorite(facetId = 2, app = b, position = 0)

        // When observing both at once
        val result = repository.observeFavoritesForFacets(listOf(1, 2)).first()

        // Then each facet id maps to its own list, not the other's
        assertEquals(listOf(a), result[1])
        assertEquals(listOf(b), result[2])
    }

    @Test
    fun `favorites collapse live when AppRepository reports an uninstall, without resubscribing`() = runTest {
        // Given two favorited apps, both currently installed
        val a = appInfo('a')
        val b = appInfo('b')
        val appRepository = mock(AppRepository::class.java)
        val installedApps = MutableStateFlow(listOf(a, b))
        `when`(appRepository.observeInstalledApps()).thenReturn(installedApps)
        val dao = FakeFavoriteAppDao()
        val repository = repository(dao, appRepository)
        repository.addFavorite(facetId = 1, app = a, position = 0)
        repository.addFavorite(facetId = 1, app = b, position = 1)

        val emissions = Channel<List<AppInfo>>(Channel.UNLIMITED)
        val collectJob = launch { repository.observeFavoritesForFacet(1).collect { emissions.send(it) } }
        assertEquals(listOf(a, b), emissions.receive())

        // When AppRepository's live flow reports b uninstalled (F12's long-press menu, or any
        // other uninstall while Facet is in the foreground)
        installedApps.value = listOf(a)

        // Then it collapses out immediately, without the favorites flow needing to resubscribe
        assertEquals(listOf(a), emissions.receive())
        collectJob.cancel()
    }

    @Test
    fun `replaceItems wipes the facet's existing list and inserts the new one in order`() = runTest {
        // Given a facet with an existing favorite that isn't in the replacement list
        val a = appInfo('a')
        val b = appInfo('b')
        val c = appInfo('c')
        val appRepository = mock(AppRepository::class.java)
        `when`(appRepository.observeInstalledApps()).thenReturn(flowOf(listOf(a, b, c)))
        val dao = FakeFavoriteAppDao()
        val repository = repository(dao, appRepository)
        repository.addFavorite(facetId = 1, app = c, position = 0)

        // When replacing this facet's favorites with a different list
        repository.replaceItems(facetId = 1, items = listOf(PlacedItem.SingleApp(b), PlacedItem.SingleApp(a)))

        // Then only the new list remains, in the given order — the stale entry is gone
        assertEquals(listOf(b, a), repository.observeFavoritesForFacet(1).first())
    }

    @Test
    fun `replaceItems carries a folder placement, not just apps`() = runTest {
        // Given a folder that exists but isn't placed anywhere, and a facet's own dock/favorites Override-seed carrying it alongside an app
        val a = appInfo('a')
        val appRepository = mock(AppRepository::class.java)
        `when`(appRepository.observeInstalledApps()).thenReturn(flowOf(listOf(a)))
        val folderDao = FakeFolderDao()
        val folderId = folderDao.insertFolder(FolderEntity(name = "Games"))
        val folderRepository = FolderRepository(folderDao, appRepository)
        val dao = FakeFavoriteAppDao()
        val repository = FavoriteAppRepository(dao, FakeFavoriteFolderPlacementDao(), folderRepository, appRepository)

        // When replacing this facet's favorites with an app and the folder together
        val folder = com.facetlauncher.app.data.model.Folder(folderId, "Games", emptyList())
        repository.replaceItems(facetId = 1, items = listOf(PlacedItem.SingleApp(a), PlacedItem.FolderItem(folder)))

        // Then both are reflected — the folder placement wasn't silently dropped
        val items = repository.observeFavoriteItems(1).first()
        assertEquals(listOf(PlacedItem.SingleApp(a), PlacedItem.FolderItem(folder)), items)
    }

    @Test
    fun `observeFavoritesForFacets with an empty id list emits an empty map`() = runTest {
        // Given no facets to observe
        val appRepository = mock(AppRepository::class.java)
        val dao = FakeFavoriteAppDao()
        val repository = repository(dao, appRepository)

        // Then it emits immediately without needing AppRepository at all
        assertEquals(emptyMap<Long, List<AppInfo>>(), repository.observeFavoritesForFacets(emptyList()).first())
    }
}
