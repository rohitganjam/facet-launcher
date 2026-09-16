package com.facetlauncher.app.data

import com.facetlauncher.app.data.local.DockAppDao
import com.facetlauncher.app.data.local.DockAppEntity
import com.facetlauncher.app.data.local.DockFolderPlacementDao
import com.facetlauncher.app.data.local.DockFolderPlacementEntity
import com.facetlauncher.app.data.local.FolderAppEntity
import com.facetlauncher.app.data.local.FolderDao
import com.facetlauncher.app.data.local.FolderEntity
import com.facetlauncher.app.data.model.AppInfo
import com.facetlauncher.app.data.model.AppProfile
import com.facetlauncher.app.data.model.PlacedItem
import android.os.UserHandle
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito.mock
import org.mockito.Mockito.`when`
import org.robolectric.RobolectricTestRunner

/** In-memory fake — simpler than mocking every [DockAppDao] method for this repository's needs. */
private class FakeDockAppDao : DockAppDao {
    private val state = MutableStateFlow<List<DockAppEntity>>(emptyList())

    override fun observeAll(): Flow<List<DockAppEntity>> = state

    override suspend fun upsert(dockApp: DockAppEntity): Long {
        state.value = state.value.filterNot { it.packageName == dockApp.packageName && it.activityName == dockApp.activityName && it.userId == dockApp.userId } + dockApp
        return 0
    }

    override suspend fun delete(dockApp: DockAppEntity) {
        state.value = state.value.filterNot { it.id == dockApp.id }
    }

    override suspend fun deleteByComponent(packageName: String, activityName: String, userId: Int) {
        state.value = state.value.filterNot { it.packageName == packageName && it.activityName == activityName && it.userId == userId }
    }

    override suspend fun deleteByPackage(packageName: String, userId: Int) {
        state.value = state.value.filterNot { it.packageName == packageName && it.userId == userId }
    }

    override suspend fun deleteByUserId(userId: Int) {
        state.value = state.value.filterNot { it.userId == userId }
    }

    override suspend fun deleteAll() {
        state.value = emptyList()
    }

    override suspend fun getOrphaned(): List<DockAppEntity> = state.value.filter { it.userId == -1 }
}

@RunWith(RobolectricTestRunner::class)
class DockAppRepositoryTest {

    /** Defaults to the primary user's handle — [UserHandle.hashCode] is always `0` for it (see `AppRepository`'s own doc). */
    private fun appInfo(letter: Char) =
        AppInfo(packageName = "com.example.$letter", activityName = ".Main", label = "$letter App", icon = null)

    private fun repository(
        appRepository: AppRepository,
        dao: DockAppDao = FakeDockAppDao(),
        placementDao: DockFolderPlacementDao = FakeDockFolderPlacementDao(),
        folderDao: FolderDao = FakeFolderDao(),
    ): DockAppRepository {
        val folderRepository = FolderRepository(folderDao, appRepository)
        return DockAppRepository(dao, placementDao, folderRepository, appRepository)
    }

    @Test
    fun `dock list reflects stored entries hydrated against installed apps, ordered by position`() = runTest {
        // Given two installed apps and dock entries referencing them out of position order
        val a = appInfo('a')
        val b = appInfo('b')
        val appRepository = mock(AppRepository::class.java)
        `when`(appRepository.observeInstalledApps()).thenReturn(flowOf(listOf(a, b)))
        val dao = FakeDockAppDao()
        dao.upsert(DockAppEntity(packageName = b.packageName, activityName = b.activityName, position = 1, userId = 0))
        dao.upsert(DockAppEntity(packageName = a.packageName, activityName = a.activityName, position = 0, userId = 0))

        // When observing the dock
        val result = repository(appRepository, dao).observeDockApps().first()

        // Then it returns the hydrated AppInfo list, ordered by position
        assertEquals(listOf(a, b), result)
    }

    @Test
    fun `an entry whose app is no longer installed is filtered out`() = runTest {
        // Given a dock entry for an app that AppRepository no longer reports as installed
        val appRepository = mock(AppRepository::class.java)
        `when`(appRepository.observeInstalledApps()).thenReturn(flowOf(emptyList()))
        val dao = FakeDockAppDao()
        dao.upsert(DockAppEntity(packageName = "com.example.uninstalled", activityName = ".Main", position = 0, userId = 0))

        // When observing the dock
        val result = repository(appRepository, dao).observeDockApps().first()

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
        dao.upsert(DockAppEntity(packageName = a.packageName, activityName = a.activityName, position = 0, userId = 0))

        val emissions = Channel<List<AppInfo>>(Channel.UNLIMITED)
        val collectJob = launch { repository(appRepository, dao).observeDockApps().collect { emissions.send(it) } }
        assertEquals(listOf(a), emissions.receive())

        // When AppRepository's live flow reports it uninstalled (F12's long-press menu, or any
        // other uninstall while Facet is in the foreground)
        installedApps.value = emptyList()

        // Then it collapses out immediately, without the dock flow needing to resubscribe
        assertEquals(emptyList<AppInfo>(), emissions.receive())
        collectJob.cancel()
    }

    @Test
    fun `removeByPackage deletes its own dock row`() = runTest {
        // Given an app in the Dock — folder-membership cleanup is CleanUpUninstalledAppsUseCase's
        // own direct responsibility (via FolderRepository), not this repository's, so this only
        // covers the Dock row itself. See CleanUpUninstalledAppsUseCaseTest for the folder case.
        val app = appInfo('a')
        val appRepository = mock(AppRepository::class.java)
        `when`(appRepository.observeInstalledApps()).thenReturn(flowOf(listOf(app)))
        val dao = FakeDockAppDao()
        dao.upsert(DockAppEntity(packageName = app.packageName, activityName = app.activityName, position = 0, userId = 0))

        // When the app is uninstalled
        repository(appRepository, dao = dao).removeByPackage(app.packageName, 0)

        // Then its dock row is gone
        assertEquals(emptyList<AppInfo>(), repository(appRepository, dao = dao).observeDockApps().first())
    }

    @Test
    fun `a personal and Work Profile app sharing the same package and activity are kept as distinct dock entries`() = runTest {
        // Given the same package+activity installed in both profiles (a duplicate-installed app) —
        // the collision this guards against is about the real UserHandle, not the display profile,
        // so the two AppInfos differ by a distinct mocked UserHandle, not just by `profile`.
        val personalHandle = mock(UserHandle::class.java)
        val workHandle = mock(UserHandle::class.java)
        val personalApp = AppInfo(packageName = "com.example.chat", activityName = ".Main", label = "Chat", icon = null, profile = AppProfile.PERSONAL, userHandle = personalHandle)
        val workApp = personalApp.copy(profile = AppProfile.WORK, userHandle = workHandle)
        val appRepository = mock(AppRepository::class.java)
        `when`(appRepository.observeInstalledApps()).thenReturn(flowOf(listOf(personalApp, workApp)))
        val repo = repository(appRepository)

        // When both are added to the dock
        repo.addDockApp(personalApp, position = 0)
        repo.addDockApp(workApp, position = 1)

        // Then both are present, not collapsed into one row
        assertEquals(listOf(personalApp, workApp), repo.observeDockApps().first())

        // And removing the Work Profile copy by uninstall-cleanup leaves the personal one intact
        repo.removeByPackage(workApp.packageName, workHandle.hashCode())
        assertEquals(listOf(personalApp), repo.observeDockApps().first())
    }

    @Test
    fun `removeByUserId only removes the matching user's rows, leaving a same-category colliding profile's rows intact`() = runTest {
        // Given two rows that share the same display profile (both OTHER — e.g. a clone-profile
        // collision below API 35) but come from two distinct real handles
        val appRepository = mock(AppRepository::class.java)
        `when`(appRepository.observeInstalledApps()).thenReturn(flowOf(emptyList()))
        val dao = FakeDockAppDao()
        dao.upsert(DockAppEntity(packageName = "com.example.a", activityName = ".Main", position = 0, profile = AppProfile.OTHER, userId = 10))
        dao.upsert(DockAppEntity(packageName = "com.example.b", activityName = ".Main", position = 1, profile = AppProfile.OTHER, userId = 20))
        val repo = repository(appRepository, dao = dao)

        // When removing only the first handle's rows
        repo.removeByUserId(10)

        // Then only that handle's row is gone — the other colliding-category row survives
        val remaining = dao.observeAll().first()
        assertEquals(listOf(20), remaining.map { it.userId })
    }

    @Test
    fun `observeDockItems merges apps and placed folders sorted by position`() = runTest {
        // Given a standalone app at position 1 and a folder placed at position 0
        val a = appInfo('a')
        val b = appInfo('b')
        val c = appInfo('c')
        val appRepository = mock(AppRepository::class.java)
        `when`(appRepository.observeInstalledApps()).thenReturn(flowOf(listOf(a, b, c)))
        val dao = FakeDockAppDao()
        val placementDao = FakeDockFolderPlacementDao()
        val folderDao = FakeFolderDao()
        dao.upsert(DockAppEntity(packageName = a.packageName, activityName = a.activityName, position = 1, userId = 0))
        val folderId = folderDao.insertFolder(FolderEntity(name = "Games"))
        folderDao.upsertFolderApp(FolderAppEntity(folderId = folderId, packageName = b.packageName, activityName = b.activityName, position = 0, userId = 0))
        folderDao.upsertFolderApp(FolderAppEntity(folderId = folderId, packageName = c.packageName, activityName = c.activityName, position = 1, userId = 0))
        placementDao.upsert(DockFolderPlacementEntity(folderId = folderId, position = 0))

        // When observing dock items
        val result = repository(appRepository, dao, placementDao, folderDao).observeDockItems().first()

        // Then the folder (position 0) comes before the standalone app (position 1)
        assertEquals(
            listOf(
                PlacedItem.FolderItem(com.facetlauncher.app.data.model.Folder(folderId, "Games", listOf(b, c))),
                PlacedItem.SingleApp(a),
            ),
            result,
        )
    }

    @Test
    fun `observeDockItems filters an uninstalled folder member but keeps the folder`() = runTest {
        // Given a placed folder with one installed and one uninstalled member
        val b = appInfo('b')
        val appRepository = mock(AppRepository::class.java)
        `when`(appRepository.observeInstalledApps()).thenReturn(flowOf(listOf(b)))
        val placementDao = FakeDockFolderPlacementDao()
        val folderDao = FakeFolderDao()
        val folderId = folderDao.insertFolder(FolderEntity(name = "Games"))
        folderDao.upsertFolderApp(FolderAppEntity(folderId = folderId, packageName = b.packageName, activityName = b.activityName, position = 0, userId = 0))
        folderDao.upsertFolderApp(FolderAppEntity(folderId = folderId, packageName = "com.example.gone", activityName = ".Main", position = 1, userId = 0))
        placementDao.upsert(DockFolderPlacementEntity(folderId = folderId, position = 0))

        // When observing dock items
        val result = repository(appRepository, placementDao = placementDao, folderDao = folderDao).observeDockItems().first()

        // Then the folder survives with only its installed member
        assertEquals(listOf(PlacedItem.FolderItem(com.facetlauncher.app.data.model.Folder(folderId, "Games", listOf(b)))), result)
    }

    @Test
    fun `a placed folder with zero apps is never dropped from observeDockItems`() = runTest {
        // Given a folder placed in the dock whose only member became uninstalled — a direct
        // regression test for the redesign's core requirement: a folder is never auto-hidden
        // (let alone auto-deleted) just because it has zero apps right now.
        val appRepository = mock(AppRepository::class.java)
        `when`(appRepository.observeInstalledApps()).thenReturn(flowOf(emptyList()))
        val placementDao = FakeDockFolderPlacementDao()
        val folderDao = FakeFolderDao()
        val folderId = folderDao.insertFolder(FolderEntity(name = "Games"))
        folderDao.upsertFolderApp(FolderAppEntity(folderId = folderId, packageName = "com.example.gone", activityName = ".Main", position = 0, userId = 0))
        placementDao.upsert(DockFolderPlacementEntity(folderId = folderId, position = 0))

        // When observing dock items
        val result = repository(appRepository, placementDao = placementDao, folderDao = folderDao).observeDockItems().first()

        // Then the folder is still emitted, now with zero apps
        assertEquals(listOf(PlacedItem.FolderItem(com.facetlauncher.app.data.model.Folder(folderId, "Games", emptyList()))), result)
    }

    @Test
    fun `placeFolderInDock adds a folder placement without touching its membership`() = runTest {
        // Given a folder that exists but isn't placed anywhere
        val a = appInfo('a')
        val appRepository = mock(AppRepository::class.java)
        `when`(appRepository.observeInstalledApps()).thenReturn(flowOf(listOf(a)))
        val folderDao = FakeFolderDao()
        val folderId = folderDao.insertFolder(FolderEntity(name = "Games"))
        folderDao.upsertFolderApp(FolderAppEntity(folderId = folderId, packageName = a.packageName, activityName = a.activityName, position = 0, userId = 0))
        val repo = repository(appRepository, folderDao = folderDao)
        assertTrue(repo.observeDockItems().first().isEmpty())

        // When placing it in the dock
        repo.placeFolderInDock(folderId, position = 0)

        // Then it appears in the dock with its membership untouched
        val items = repo.observeDockItems().first()
        assertEquals(listOf(PlacedItem.FolderItem(com.facetlauncher.app.data.model.Folder(folderId, "Games", listOf(a)))), items)
    }

    @Test
    fun `removeFolderFromDock un-places the folder without deleting it`() = runTest {
        // Given a folder placed in the dock
        val a = appInfo('a')
        val appRepository = mock(AppRepository::class.java)
        `when`(appRepository.observeInstalledApps()).thenReturn(flowOf(listOf(a)))
        val placementDao = FakeDockFolderPlacementDao()
        val folderDao = FakeFolderDao()
        val folderId = folderDao.insertFolder(FolderEntity(name = "Games"))
        folderDao.upsertFolderApp(FolderAppEntity(folderId = folderId, packageName = a.packageName, activityName = a.activityName, position = 0, userId = 0))
        placementDao.upsert(DockFolderPlacementEntity(folderId = folderId, position = 0))
        val repo = repository(appRepository, placementDao = placementDao, folderDao = folderDao)

        // When un-placing it from the dock
        repo.removeFolderFromDock(folderId)

        // Then the dock no longer shows it, but the folder library (via FolderDao) still has it
        assertTrue(repo.observeDockItems().first().isEmpty())
        assertEquals(1, folderDao.observeAllWithApps().first().size)
    }

    @Test
    fun `reorderDockItems rewrites position across both apps and folder placements`() = runTest {
        // Given an app at position 0 and a placed folder at position 1
        val a = appInfo('a')
        val b = appInfo('b')
        val appRepository = mock(AppRepository::class.java)
        `when`(appRepository.observeInstalledApps()).thenReturn(flowOf(listOf(a, b)))
        val dao = FakeDockAppDao()
        val placementDao = FakeDockFolderPlacementDao()
        val folderDao = FakeFolderDao()
        dao.upsert(DockAppEntity(packageName = a.packageName, activityName = a.activityName, position = 0, userId = 0))
        val folderId = folderDao.insertFolder(FolderEntity(name = "Games"))
        folderDao.upsertFolderApp(FolderAppEntity(folderId = folderId, packageName = b.packageName, activityName = b.activityName, position = 0, userId = 0))
        placementDao.upsert(DockFolderPlacementEntity(folderId = folderId, position = 1))
        val repo = repository(appRepository, dao, placementDao, folderDao)
        val folder = PlacedItem.FolderItem(com.facetlauncher.app.data.model.Folder(folderId, "Games", listOf(b)))
        val app = PlacedItem.SingleApp(a)

        // When reordering so the folder comes first
        repo.reorderDockItems(listOf(folder, app))

        // Then the new order is reflected, and the folder's membership survived the reposition
        assertEquals(listOf(folder, app), repo.observeDockItems().first())
    }
}
