package com.lumenlauncher.app.data

import com.lumenlauncher.app.data.local.ProfileDockAppDao
import com.lumenlauncher.app.data.local.ProfileDockAppEntity
import com.lumenlauncher.app.data.model.AppInfo
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
private class FakeProfileDockAppDao : ProfileDockAppDao {
    private val state = MutableStateFlow<List<ProfileDockAppEntity>>(emptyList())

    override fun observeForProfile(profileId: Long): Flow<List<ProfileDockAppEntity>> =
        state.map { entries -> entries.filter { it.profileId == profileId } }

    override suspend fun upsert(dockApp: ProfileDockAppEntity): Long {
        state.value = state.value.filterNot {
            it.profileId == dockApp.profileId && it.packageName == dockApp.packageName && it.activityName == dockApp.activityName
        } + dockApp
        return 0
    }

    override suspend fun delete(dockApp: ProfileDockAppEntity) {
        state.value = state.value.filterNot { it.id == dockApp.id }
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

class ProfileDockAppRepositoryTest {

    private fun appInfo(letter: Char) =
        AppInfo(packageName = "com.example.$letter", activityName = ".Main", label = "$letter App", icon = null)

    @Test
    fun `dock list reflects stored entries hydrated against installed apps, ordered by position`() = runTest {
        // Given two installed apps and dock entries for one profile, out of position order
        val a = appInfo('a')
        val b = appInfo('b')
        val appRepository = mock(AppRepository::class.java)
        `when`(appRepository.observeInstalledApps()).thenReturn(flowOf(listOf(a, b)))
        val repository = ProfileDockAppRepository(FakeProfileDockAppDao(), appRepository)
        repository.addDockApp(profileId = 1, app = b, position = 1)
        repository.addDockApp(profileId = 1, app = a, position = 0)

        // When observing that profile's dock
        val result = repository.observeDockAppsForProfile(1).first()

        // Then it returns the hydrated AppInfo list, ordered by position
        assertEquals(listOf(a, b), result)
    }

    @Test
    fun `an entry whose app is no longer installed is filtered out`() = runTest {
        val appRepository = mock(AppRepository::class.java)
        `when`(appRepository.observeInstalledApps()).thenReturn(flowOf(emptyList()))
        val repository = ProfileDockAppRepository(FakeProfileDockAppDao(), appRepository)
        repository.addDockApp(profileId = 1, app = appInfo('a'), position = 0)

        // Then the unresolvable entry is collapsed out, not shown as a dead tile
        assertEquals(emptyList<AppInfo>(), repository.observeDockAppsForProfile(1).first())
    }

    @Test
    fun `dock apps are scoped per profile, not shared`() = runTest {
        val a = appInfo('a')
        val appRepository = mock(AppRepository::class.java)
        `when`(appRepository.observeInstalledApps()).thenReturn(flowOf(listOf(a)))
        val repository = ProfileDockAppRepository(FakeProfileDockAppDao(), appRepository)
        repository.addDockApp(profileId = 1, app = a, position = 0)

        assertEquals(listOf(a), repository.observeDockAppsForProfile(1).first())
        assertEquals(emptyList<AppInfo>(), repository.observeDockAppsForProfile(2).first())
    }

    @Test
    fun `removeDockApp drops it from the profile's dock`() = runTest {
        val a = appInfo('a')
        val appRepository = mock(AppRepository::class.java)
        `when`(appRepository.observeInstalledApps()).thenReturn(flowOf(listOf(a)))
        val repository = ProfileDockAppRepository(FakeProfileDockAppDao(), appRepository)
        repository.addDockApp(profileId = 1, app = a, position = 0)

        repository.removeDockApp(profileId = 1, app = a)

        assertEquals(emptyList<AppInfo>(), repository.observeDockAppsForProfile(1).first())
    }

    @Test
    fun `observeDockAppsForProfiles returns each profile's own hydrated list, keyed by id`() = runTest {
        val a = appInfo('a')
        val b = appInfo('b')
        val appRepository = mock(AppRepository::class.java)
        `when`(appRepository.observeInstalledApps()).thenReturn(flowOf(listOf(a, b)))
        val repository = ProfileDockAppRepository(FakeProfileDockAppDao(), appRepository)
        repository.addDockApp(profileId = 1, app = a, position = 0)
        repository.addDockApp(profileId = 2, app = b, position = 0)

        val result = repository.observeDockAppsForProfiles(listOf(1, 2)).first()

        assertEquals(listOf(a), result[1])
        assertEquals(listOf(b), result[2])
    }

    @Test
    fun `observeDockAppsForProfiles with an empty id list emits an empty map`() = runTest {
        val appRepository = mock(AppRepository::class.java)
        val repository = ProfileDockAppRepository(FakeProfileDockAppDao(), appRepository)

        assertEquals(emptyMap<Long, List<AppInfo>>(), repository.observeDockAppsForProfiles(emptyList()).first())
    }

    @Test
    fun `replaceDockApps wipes the profile's existing dock and inserts the new one in order`() = runTest {
        val a = appInfo('a')
        val b = appInfo('b')
        val c = appInfo('c')
        val appRepository = mock(AppRepository::class.java)
        `when`(appRepository.observeInstalledApps()).thenReturn(flowOf(listOf(a, b, c)))
        val repository = ProfileDockAppRepository(FakeProfileDockAppDao(), appRepository)
        repository.addDockApp(profileId = 1, app = c, position = 0)

        repository.replaceDockApps(profileId = 1, apps = listOf(b, a))

        assertEquals(listOf(b, a), repository.observeDockAppsForProfile(1).first())
    }

    @Test
    fun `dock collapses live when AppRepository reports an uninstall, without resubscribing`() = runTest {
        val a = appInfo('a')
        val b = appInfo('b')
        val appRepository = mock(AppRepository::class.java)
        val installedApps = MutableStateFlow(listOf(a, b))
        `when`(appRepository.observeInstalledApps()).thenReturn(installedApps)
        val repository = ProfileDockAppRepository(FakeProfileDockAppDao(), appRepository)
        repository.addDockApp(profileId = 1, app = a, position = 0)
        repository.addDockApp(profileId = 1, app = b, position = 1)

        val emissions = Channel<List<AppInfo>>(Channel.UNLIMITED)
        val collectJob = launch { repository.observeDockAppsForProfile(1).collect { emissions.send(it) } }
        assertEquals(listOf(a, b), emissions.receive())

        installedApps.value = listOf(a)

        assertEquals(listOf(a), emissions.receive())
        collectJob.cancel()
    }
}
