package com.lumenlauncher.app.data

import com.lumenlauncher.app.data.local.DockAppDao
import com.lumenlauncher.app.data.local.DockAppEntity
import com.lumenlauncher.app.data.model.AppInfo
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

/** In-memory fake — simpler than mocking every [DockAppDao] method for this repository's needs. */
private class FakeDockAppDao : DockAppDao {
    private val state = MutableStateFlow<List<DockAppEntity>>(emptyList())

    override fun observeAll(): Flow<List<DockAppEntity>> = state

    override suspend fun upsert(dockApp: DockAppEntity): Long {
        state.value = state.value.filterNot { it.packageName == dockApp.packageName && it.activityName == dockApp.activityName } + dockApp
        return 0
    }

    override suspend fun delete(dockApp: DockAppEntity) {
        state.value = state.value.filterNot { it.id == dockApp.id }
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

class DockAppRepositoryTest {

    private fun appInfo(letter: Char) =
        AppInfo(packageName = "com.example.$letter", activityName = ".Main", label = "$letter App", icon = null)

    @Test
    fun `dock list reflects stored entries hydrated against installed apps, ordered by position`() = runTest {
        // Given two installed apps and dock entries referencing them out of position order
        val a = appInfo('a')
        val b = appInfo('b')
        val appRepository = mock(AppRepository::class.java)
        `when`(appRepository.observeInstalledApps()).thenReturn(flowOf(listOf(a, b)))
        val dao = FakeDockAppDao()
        dao.upsert(DockAppEntity(packageName = b.packageName, activityName = b.activityName, position = 1))
        dao.upsert(DockAppEntity(packageName = a.packageName, activityName = a.activityName, position = 0))

        // When observing the dock
        val result = DockAppRepository(dao, appRepository).observeDockApps().first()

        // Then it returns the hydrated AppInfo list, ordered by position
        assertEquals(listOf(a, b), result)
    }

    @Test
    fun `an entry whose app is no longer installed is filtered out`() = runTest {
        // Given a dock entry for an app that AppRepository no longer reports as installed
        val appRepository = mock(AppRepository::class.java)
        `when`(appRepository.observeInstalledApps()).thenReturn(flowOf(emptyList()))
        val dao = FakeDockAppDao()
        dao.upsert(DockAppEntity(packageName = "com.example.uninstalled", activityName = ".Main", position = 0))

        // When observing the dock
        val result = DockAppRepository(dao, appRepository).observeDockApps().first()

        // Then the unresolvable entry is collapsed out, not shown as a dead tile
        assertEquals(emptyList<AppInfo>(), result)
    }

    @Test
    fun `dock collapses live when AppRepository reports an uninstall, without resubscribing`() = runTest {
        // Given a dock app that's currently installed
        val a = appInfo('a')
        val appRepository = mock(AppRepository::class.java)
        val installedApps = MutableStateFlow(listOf(a))
        `when`(appRepository.observeInstalledApps()).thenReturn(installedApps)
        val dao = FakeDockAppDao()
        dao.upsert(DockAppEntity(packageName = a.packageName, activityName = a.activityName, position = 0))

        val emissions = Channel<List<AppInfo>>(Channel.UNLIMITED)
        val collectJob = launch { DockAppRepository(dao, appRepository).observeDockApps().collect { emissions.send(it) } }
        assertEquals(listOf(a), emissions.receive())

        // When AppRepository's live flow reports it uninstalled (F12's long-press menu, or any
        // other uninstall while Lumen is in the foreground)
        installedApps.value = emptyList()

        // Then it collapses out immediately, without the dock flow needing to resubscribe
        assertEquals(emptyList<AppInfo>(), emissions.receive())
        collectJob.cancel()
    }
}
