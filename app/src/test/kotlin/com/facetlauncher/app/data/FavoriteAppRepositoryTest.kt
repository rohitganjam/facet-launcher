package com.facetlauncher.app.data

import com.facetlauncher.app.data.local.FavoriteAppDao
import com.facetlauncher.app.data.local.FavoriteAppEntity
import com.facetlauncher.app.data.model.AppInfo
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

    override fun observeForProfile(profileId: Long): Flow<List<FavoriteAppEntity>> =
        state.map { entries -> entries.filter { it.profileId == profileId } }

    override suspend fun upsert(favoriteApp: FavoriteAppEntity): Long {
        state.value = state.value.filterNot {
            it.profileId == favoriteApp.profileId && it.packageName == favoriteApp.packageName && it.activityName == favoriteApp.activityName
        } + favoriteApp
        return 0
    }

    override suspend fun delete(favoriteApp: FavoriteAppEntity) {
        state.value = state.value.filterNot { it.id == favoriteApp.id }
    }

    override suspend fun deleteByComponent(profileId: Long, packageName: String, activityName: String) {
        state.value = state.value.filterNot {
            it.profileId == profileId && it.packageName == packageName && it.activityName == activityName
        }
    }

    override suspend fun deleteByPackage(packageName: String) {
        state.value = state.value.filterNot { it.packageName == packageName }
    }

    override suspend fun deleteAllForProfile(profileId: Long) {
        state.value = state.value.filterNot { it.profileId == profileId }
    }
}

class FavoriteAppRepositoryTest {

    private fun appInfo(letter: Char) =
        AppInfo(packageName = "com.example.$letter", activityName = ".Main", label = "$letter App", icon = null)

    @Test
    fun `favorites list reflects stored entries hydrated against installed apps, ordered by position`() = runTest {
        // Given two installed apps and favorite entries for one profile, out of position order
        val a = appInfo('a')
        val b = appInfo('b')
        val appRepository = mock(AppRepository::class.java)
        `when`(appRepository.observeInstalledApps()).thenReturn(flowOf(listOf(a, b)))
        val dao = FakeFavoriteAppDao()
        val repository = FavoriteAppRepository(dao, appRepository)
        repository.addFavorite(profileId = 1, app = b, position = 1)
        repository.addFavorite(profileId = 1, app = a, position = 0)

        // When observing that profile's favorites
        val result = repository.observeFavoritesForProfile(1).first()

        // Then it returns the hydrated AppInfo list, ordered by position
        assertEquals(listOf(a, b), result)
    }

    @Test
    fun `an entry whose app is no longer installed is filtered out`() = runTest {
        // Given a favorite entry for an app AppRepository no longer reports as installed
        val appRepository = mock(AppRepository::class.java)
        `when`(appRepository.observeInstalledApps()).thenReturn(flowOf(emptyList()))
        val dao = FakeFavoriteAppDao()
        val repository = FavoriteAppRepository(dao, appRepository)
        repository.addFavorite(profileId = 1, app = appInfo('a'), position = 0)

        // When observing that profile's favorites
        val result = repository.observeFavoritesForProfile(1).first()

        // Then the unresolvable entry is collapsed out, not shown as a dead tile
        assertEquals(emptyList<AppInfo>(), result)
    }

    @Test
    fun `favorites are scoped per profile, not shared`() = runTest {
        // Given the same app favorited under two different profiles
        val a = appInfo('a')
        val appRepository = mock(AppRepository::class.java)
        `when`(appRepository.observeInstalledApps()).thenReturn(flowOf(listOf(a)))
        val dao = FakeFavoriteAppDao()
        val repository = FavoriteAppRepository(dao, appRepository)
        repository.addFavorite(profileId = 1, app = a, position = 0)

        // Then only profile 1 sees it — profile 2 sees none
        assertEquals(listOf(a), repository.observeFavoritesForProfile(1).first())
        assertEquals(emptyList<AppInfo>(), repository.observeFavoritesForProfile(2).first())
    }

    @Test
    fun `removeFavorite drops it from the profile's list`() = runTest {
        // Given a favorited app
        val a = appInfo('a')
        val appRepository = mock(AppRepository::class.java)
        `when`(appRepository.observeInstalledApps()).thenReturn(flowOf(listOf(a)))
        val dao = FakeFavoriteAppDao()
        val repository = FavoriteAppRepository(dao, appRepository)
        repository.addFavorite(profileId = 1, app = a, position = 0)

        // When it is removed
        repository.removeFavorite(profileId = 1, app = a)

        // Then it's gone
        assertEquals(emptyList<AppInfo>(), repository.observeFavoritesForProfile(1).first())
    }

    @Test
    fun `observeFavoritesForProfiles returns each profile's own hydrated list, keyed by id`() = runTest {
        // Given two profiles with different favorites
        val a = appInfo('a')
        val b = appInfo('b')
        val appRepository = mock(AppRepository::class.java)
        `when`(appRepository.observeInstalledApps()).thenReturn(flowOf(listOf(a, b)))
        val dao = FakeFavoriteAppDao()
        val repository = FavoriteAppRepository(dao, appRepository)
        repository.addFavorite(profileId = 1, app = a, position = 0)
        repository.addFavorite(profileId = 2, app = b, position = 0)

        // When observing both at once
        val result = repository.observeFavoritesForProfiles(listOf(1, 2)).first()

        // Then each profile id maps to its own list, not the other's
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
        val repository = FavoriteAppRepository(dao, appRepository)
        repository.addFavorite(profileId = 1, app = a, position = 0)
        repository.addFavorite(profileId = 1, app = b, position = 1)

        val emissions = Channel<List<AppInfo>>(Channel.UNLIMITED)
        val collectJob = launch { repository.observeFavoritesForProfile(1).collect { emissions.send(it) } }
        assertEquals(listOf(a, b), emissions.receive())

        // When AppRepository's live flow reports b uninstalled (F12's long-press menu, or any
        // other uninstall while Facet is in the foreground)
        installedApps.value = listOf(a)

        // Then it collapses out immediately, without the favorites flow needing to resubscribe
        assertEquals(listOf(a), emissions.receive())
        collectJob.cancel()
    }

    @Test
    fun `replaceFavorites wipes the profile's existing list and inserts the new one in order`() = runTest {
        // Given a profile with an existing favorite that isn't in the replacement list
        val a = appInfo('a')
        val b = appInfo('b')
        val c = appInfo('c')
        val appRepository = mock(AppRepository::class.java)
        `when`(appRepository.observeInstalledApps()).thenReturn(flowOf(listOf(a, b, c)))
        val dao = FakeFavoriteAppDao()
        val repository = FavoriteAppRepository(dao, appRepository)
        repository.addFavorite(profileId = 1, app = c, position = 0)

        // When replacing this profile's favorites with a different list
        repository.replaceFavorites(profileId = 1, apps = listOf(b, a))

        // Then only the new list remains, in the given order — the stale entry is gone
        assertEquals(listOf(b, a), repository.observeFavoritesForProfile(1).first())
    }

    @Test
    fun `observeFavoritesForProfiles with an empty id list emits an empty map`() = runTest {
        // Given no profiles to observe
        val appRepository = mock(AppRepository::class.java)
        val dao = FakeFavoriteAppDao()
        val repository = FavoriteAppRepository(dao, appRepository)

        // Then it emits immediately without needing AppRepository at all
        assertEquals(emptyMap<Long, List<AppInfo>>(), repository.observeFavoritesForProfiles(emptyList()).first())
    }
}
