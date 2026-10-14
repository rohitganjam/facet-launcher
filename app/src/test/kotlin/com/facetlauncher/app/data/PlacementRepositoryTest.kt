package com.facetlauncher.app.data

import android.os.UserHandle
import com.facetlauncher.app.data.local.AppPlacementDao
import com.facetlauncher.app.data.local.AppPlacementEntity
import com.facetlauncher.app.data.local.FolderAppEntity
import com.facetlauncher.app.data.local.FolderDao
import com.facetlauncher.app.data.local.FolderEntity
import com.facetlauncher.app.data.local.FolderPlacementDao
import com.facetlauncher.app.data.local.FolderPlacementEntity
import com.facetlauncher.app.data.model.AppInfo
import com.facetlauncher.app.data.model.AppProfile
import com.facetlauncher.app.data.model.Folder
import com.facetlauncher.app.data.model.PlacedItem
import com.facetlauncher.app.data.model.PlacementList
import com.facetlauncher.app.data.model.PlacementTarget
import kotlinx.coroutines.channels.Channel
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

/**
 * One repository now backs all four lists, so every shared behavior is checked against each of
 * them: the launcher-wide Dock and Favorites, and a facet's own Dock and Favorites.
 */
@RunWith(RobolectricTestRunner::class)
class PlacementRepositoryTest {

    private val facetOne = 1L
    private val facetTwo = 2L

    private val allTargets = listOf(
        PlacementTarget.GlobalDock,
        PlacementTarget.GlobalFavorites,
        PlacementTarget(PlacementList.DOCK, facetOne),
        PlacementTarget(PlacementList.FAVORITES, facetOne),
    )

    /** Defaults to the primary user's handle — [UserHandle.hashCode] is always `0` for it (see `AppRepository`'s own doc). */
    private fun appInfo(letter: Char) =
        AppInfo(packageName = "com.example.$letter", activityName = ".Main", label = "$letter App", icon = null)

    private fun installed(vararg apps: AppInfo): AppRepository =
        mock(AppRepository::class.java).also { `when`(it.observeInstalledApps()).thenReturn(flowOf(apps.toList())) }

    private fun repository(
        appRepository: AppRepository,
        dao: AppPlacementDao = FakeAppPlacementDao(),
        placementDao: FolderPlacementDao = FakeFolderPlacementDao(),
        folderDao: FolderDao = FakeFolderDao(),
    ) = PlacementRepository(dao, placementDao, FolderRepository(folderDao, appRepository), appRepository)

    private fun entity(target: PlacementTarget, app: AppInfo, position: Int, userId: Int = 0) = AppPlacementEntity(
        list = target.list,
        facetId = target.facetId,
        packageName = app.packageName,
        activityName = app.activityName,
        position = position,
        userId = userId,
    )

    @Test
    fun `apps are hydrated against installed apps and ordered by position`() = runTest {
        allTargets.forEach { target ->
            // Given two installed apps stored out of position order
            val a = appInfo('a')
            val b = appInfo('b')
            val dao = FakeAppPlacementDao()
            dao.upsert(entity(target, b, position = 1))
            dao.upsert(entity(target, a, position = 0))

            // When observing the target
            val result = repository(installed(a, b), dao).observeApps(target).first()

            // Then the hydrated AppInfo list comes back ordered by position
            assertEquals(listOf(a, b), result)
        }
    }

    @Test
    fun `an entry whose app is no longer installed is filtered out`() = runTest {
        allTargets.forEach { target ->
            // Given a stored entry for an app AppRepository no longer reports as installed
            val dao = FakeAppPlacementDao()
            dao.upsert(entity(target, appInfo('x'), position = 0))

            // Then the unresolvable entry is collapsed out, not shown as a dead tile
            assertEquals(emptyList<AppInfo>(), repository(installed(), dao).observeApps(target).first())
        }
    }

    @Test
    fun `the list collapses live when AppRepository reports an uninstall, without resubscribing`() = runTest {
        val target = PlacementTarget.GlobalDock
        val a = appInfo('a')
        val installedApps = MutableStateFlow(listOf(a))
        val appRepository = mock(AppRepository::class.java)
        `when`(appRepository.observeInstalledApps()).thenReturn(installedApps)
        val dao = FakeAppPlacementDao()
        dao.upsert(entity(target, a, position = 0))

        val emissions = Channel<List<AppInfo>>(Channel.UNLIMITED)
        val collectJob = launch { repository(appRepository, dao).observeApps(target).collect { emissions.send(it) } }
        assertEquals(listOf(a), emissions.receive())

        // When AppRepository's live flow reports it uninstalled
        installedApps.value = emptyList()

        // Then it collapses out immediately
        assertEquals(emptyList<AppInfo>(), emissions.receive())
        collectJob.cancel()
    }

    @Test
    fun `the same app in all four lists stays separate`() = runTest {
        // Given one app added to the launcher-wide Dock, launcher-wide Favorites and facet 1's Dock and Favorites
        val a = appInfo('a')
        val repo = repository(installed(a))
        allTargets.forEach { repo.addApp(it, a, position = 0) }

        // When it is removed from only the launcher-wide Dock
        repo.removeApp(PlacementTarget.GlobalDock, a)

        // Then the other three lists still hold it
        assertEquals(emptyList<AppInfo>(), repo.observeApps(PlacementTarget.GlobalDock).first())
        allTargets.drop(1).forEach { assertEquals(listOf(a), repo.observeApps(it).first()) }
    }

    @Test
    fun `facets are scoped, not shared`() = runTest {
        PlacementList.entries.forEach { list ->
            // Given an app in facet 1's list
            val a = appInfo('a')
            val repo = repository(installed(a))
            repo.addApp(PlacementTarget(list, facetOne), a, position = 0)

            // Then facet 2 and the launcher-wide list see nothing
            assertEquals(listOf(a), repo.observeApps(PlacementTarget(list, facetOne)).first())
            assertEquals(emptyList<AppInfo>(), repo.observeApps(PlacementTarget(list, facetTwo)).first())
            assertEquals(emptyList<AppInfo>(), repo.observeApps(PlacementTarget(list)).first())
        }
    }

    @Test
    fun `a personal and Work Profile app sharing the same package and activity are kept as distinct entries`() = runTest {
        allTargets.forEach { target ->
            // Given the same package+activity installed in both profiles; the collision is about the
            // real UserHandle, so the two AppInfos differ by a distinct mocked handle
            val personalHandle = mock(UserHandle::class.java)
            val workHandle = mock(UserHandle::class.java)
            val personalApp = AppInfo(packageName = "com.example.chat", activityName = ".Main", label = "Chat", icon = null, profile = AppProfile.PERSONAL, userHandle = personalHandle)
            val workApp = personalApp.copy(profile = AppProfile.WORK, userHandle = workHandle)
            val repo = repository(installed(personalApp, workApp))

            // When both are added
            repo.addApp(target, personalApp, position = 0)
            repo.addApp(target, workApp, position = 1)

            // Then both are present, not collapsed into one row
            assertEquals(listOf(personalApp, workApp), repo.observeApps(target).first())

            // And uninstall-cleanup for the Work Profile copy leaves the personal one intact
            repo.removeByPackage(workApp.packageName, workHandle.hashCode())
            assertEquals(listOf(personalApp), repo.observeApps(target).first())
        }
    }

    @Test
    fun `removeByPackage deletes the app's rows from every list and facet`() = runTest {
        // Given the same app in all four lists, plus another app that must survive
        val a = appInfo('a')
        val b = appInfo('b')
        val repo = repository(installed(a, b))
        allTargets.forEach {
            repo.addApp(it, a, position = 0)
            repo.addApp(it, b, position = 1)
        }

        // When the first app is uninstalled
        repo.removeByPackage(a.packageName, 0)

        // Then it is gone everywhere and the other app is untouched
        allTargets.forEach { assertEquals(listOf(b), repo.observeApps(it).first()) }
    }

    @Test
    fun `removeByUserId only removes the matching user's rows, leaving a same-category colliding profile's rows intact`() = runTest {
        // Given two rows in different lists that share the same display profile (both OTHER, e.g. a
        // clone-profile collision below API 35) but come from two distinct real handles
        val dao = FakeAppPlacementDao()
        dao.upsert(AppPlacementEntity(list = PlacementList.DOCK, packageName = "com.example.a", activityName = ".Main", position = 0, profile = AppProfile.OTHER, userId = 10))
        dao.upsert(AppPlacementEntity(list = PlacementList.FAVORITES, facetId = facetTwo, packageName = "com.example.b", activityName = ".Main", position = 0, profile = AppProfile.OTHER, userId = 20))
        val repo = repository(installed(), dao)

        // When removing only the first handle's rows
        repo.removeByUserId(10)

        // Then only that handle's row is gone
        assertEquals(emptyList<AppPlacementEntity>(), repo.getRawApps(PlacementTarget.GlobalDock))
        assertEquals(listOf(20), repo.getRawApps(PlacementTarget(PlacementList.FAVORITES, facetTwo)).map { it.userId })
    }

    @Test
    fun `items merge apps and placed folders sorted by position`() = runTest {
        allTargets.forEach { target ->
            // Given a standalone app at position 1 and a folder placed at position 0
            val a = appInfo('a')
            val b = appInfo('b')
            val c = appInfo('c')
            val folderDao = FakeFolderDao()
            val folderId = folderDao.insertFolder(FolderEntity(name = "Games"))
            folderDao.upsertFolderApp(FolderAppEntity(folderId = folderId, packageName = b.packageName, activityName = b.activityName, position = 0, userId = 0))
            folderDao.upsertFolderApp(FolderAppEntity(folderId = folderId, packageName = c.packageName, activityName = c.activityName, position = 1, userId = 0))
            val repo = repository(installed(a, b, c), folderDao = folderDao)
            repo.addApp(target, a, position = 1)
            repo.placeFolder(target, folderId, position = 0)

            // Then the folder (position 0) comes before the standalone app (position 1)
            assertEquals(
                listOf(PlacedItem.FolderItem(Folder(folderId, "Games", listOf(b, c))), PlacedItem.SingleApp(a)),
                repo.observeItems(target).first(),
            )
        }
    }

    @Test
    fun `a placed folder keeps only its installed members and is never dropped for having none`() = runTest {
        allTargets.forEach { target ->
            // Given a folder with one installed and one uninstalled member, and a second folder whose only member is gone
            val b = appInfo('b')
            val folderDao = FakeFolderDao()
            val partial = folderDao.insertFolder(FolderEntity(name = "Partial"))
            folderDao.upsertFolderApp(FolderAppEntity(folderId = partial, packageName = b.packageName, activityName = b.activityName, position = 0, userId = 0))
            folderDao.upsertFolderApp(FolderAppEntity(folderId = partial, packageName = "com.example.gone", activityName = ".Main", position = 1, userId = 0))
            val empty = folderDao.insertFolder(FolderEntity(name = "Empty"))
            folderDao.upsertFolderApp(FolderAppEntity(folderId = empty, packageName = "com.example.gone2", activityName = ".Main", position = 0, userId = 0))
            val repo = repository(installed(b), folderDao = folderDao)
            repo.placeFolder(target, partial, position = 0)
            repo.placeFolder(target, empty, position = 1)

            // Then both folders are emitted, the second with zero apps
            assertEquals(
                listOf(
                    PlacedItem.FolderItem(Folder(partial, "Partial", listOf(b))),
                    PlacedItem.FolderItem(Folder(empty, "Empty", emptyList())),
                ),
                repo.observeItems(target).first(),
            )
        }
    }

    @Test
    fun `removeFolderPlacement un-places the folder from that list only, without deleting it`() = runTest {
        // Given a folder placed in every list
        val a = appInfo('a')
        val folderDao = FakeFolderDao()
        val folderId = folderDao.insertFolder(FolderEntity(name = "Games"))
        folderDao.upsertFolderApp(FolderAppEntity(folderId = folderId, packageName = a.packageName, activityName = a.activityName, position = 0, userId = 0))
        val repo = repository(installed(a), folderDao = folderDao)
        allTargets.forEach { repo.placeFolder(it, folderId, position = 0) }

        // When un-placing it from the launcher-wide Dock
        repo.removeFolderPlacement(PlacementTarget.GlobalDock, folderId)

        // Then that list no longer shows it, the others do, and the folder library still has it
        assertTrue(repo.observeItems(PlacementTarget.GlobalDock).first().isEmpty())
        allTargets.drop(1).forEach { assertEquals(1, repo.observeItems(it).first().size) }
        assertEquals(1, folderDao.observeAllWithApps().first().size)
    }

    @Test
    fun `reorderItems rewrites position across both apps and folder placements`() = runTest {
        allTargets.forEach { target ->
            // Given an app at position 0 and a placed folder at position 1
            val a = appInfo('a')
            val b = appInfo('b')
            val folderDao = FakeFolderDao()
            val folderId = folderDao.insertFolder(FolderEntity(name = "Games"))
            folderDao.upsertFolderApp(FolderAppEntity(folderId = folderId, packageName = b.packageName, activityName = b.activityName, position = 0, userId = 0))
            val repo = repository(installed(a, b), folderDao = folderDao)
            repo.addApp(target, a, position = 0)
            repo.placeFolder(target, folderId, position = 1)
            val folder = PlacedItem.FolderItem(Folder(folderId, "Games", listOf(b)))
            val app = PlacedItem.SingleApp(a)

            // When reordering so the folder comes first
            repo.reorderItems(target, listOf(folder, app))

            // Then the new order is reflected, and the folder's membership survived the reposition
            assertEquals(listOf(folder, app), repo.observeItems(target).first())
        }
    }

    @Test
    fun `replaceItems wipes the existing list and inserts the new one in order, folders included`() = runTest {
        allTargets.forEach { target ->
            // Given a list holding one app, and a folder that exists
            val a = appInfo('a')
            val b = appInfo('b')
            val folderDao = FakeFolderDao()
            val folderId = folderDao.insertFolder(FolderEntity(name = "Games"))
            val repo = repository(installed(a, b), folderDao = folderDao)
            repo.addApp(target, b, position = 0)
            val folder = Folder(folderId, "Games", emptyList())

            // When it is replaced with an app followed by the folder
            repo.replaceItems(target, listOf(PlacedItem.SingleApp(a), PlacedItem.FolderItem(folder)))

            // Then the old app is gone and the new items are in order
            assertEquals(listOf(PlacedItem.SingleApp(a), PlacedItem.FolderItem(folder)), repo.observeItems(target).first())
        }
    }

    @Test
    fun `replaceItems leaves every other list alone`() = runTest {
        // Given an app in all four lists
        val a = appInfo('a')
        val b = appInfo('b')
        val repo = repository(installed(a, b))
        allTargets.forEach { repo.addApp(it, a, position = 0) }

        // When facet 1's Favorites is replaced
        repo.replaceItems(PlacementTarget(PlacementList.FAVORITES, facetOne), listOf(PlacedItem.SingleApp(b)))

        // Then the other three lists are unchanged
        assertEquals(listOf(b), repo.observeApps(PlacementTarget(PlacementList.FAVORITES, facetOne)).first())
        allTargets.filter { it != PlacementTarget(PlacementList.FAVORITES, facetOne) }.forEach {
            assertEquals(listOf(a), repo.observeApps(it).first())
        }
    }

    @Test
    fun `deleteAll wipes one list's apps and folder placements only`() = runTest {
        // Given an app and a folder placement in every list
        val a = appInfo('a')
        val folderDao = FakeFolderDao()
        val folderId = folderDao.insertFolder(FolderEntity(name = "Games"))
        val repo = repository(installed(a), folderDao = folderDao)
        allTargets.forEach {
            repo.addApp(it, a, position = 0)
            repo.placeFolder(it, folderId, position = 1)
        }

        // When the launcher-wide Favorites is wiped (as a backup restore does)
        repo.deleteAll(PlacementTarget.GlobalFavorites)

        // Then it is empty and the other lists keep both rows
        assertTrue(repo.observeItems(PlacementTarget.GlobalFavorites).first().isEmpty())
        allTargets.filter { it != PlacementTarget.GlobalFavorites }.forEach { assertEquals(2, repo.observeItems(it).first().size) }
    }

    @Test
    fun `observeAppsForFacets returns each facet's own hydrated list, keyed by id`() = runTest {
        PlacementList.entries.forEach { list ->
            // Given two facets with different apps in this list
            val a = appInfo('a')
            val b = appInfo('b')
            val repo = repository(installed(a, b))
            repo.addApp(PlacementTarget(list, facetOne), a, position = 0)
            repo.addApp(PlacementTarget(list, facetTwo), b, position = 0)

            // Then the batched read keys each list by facet id
            assertEquals(mapOf(facetOne to listOf(a), facetTwo to listOf(b)), repo.observeAppsForFacets(list, listOf(facetOne, facetTwo)).first())
        }
    }

    @Test
    fun `observeItemsForFacets returns each facet's apps and folders, keyed by id`() = runTest {
        PlacementList.entries.forEach { list ->
            // Given facet 1 with an app and a folder, facet 2 empty
            val a = appInfo('a')
            val folderDao = FakeFolderDao()
            val folderId = folderDao.insertFolder(FolderEntity(name = "Games"))
            val repo = repository(installed(a), folderDao = folderDao)
            repo.addApp(PlacementTarget(list, facetOne), a, position = 0)
            repo.placeFolder(PlacementTarget(list, facetOne), folderId, position = 1)

            // Then the batched read returns the merged items per facet
            assertEquals(
                mapOf(
                    facetOne to listOf(PlacedItem.SingleApp(a), PlacedItem.FolderItem(Folder(folderId, "Games", emptyList()))),
                    facetTwo to emptyList(),
                ),
                repo.observeItemsForFacets(list, listOf(facetOne, facetTwo)).first(),
            )
        }
    }

    @Test
    fun `the batched reads with an empty id list emit an empty map`() = runTest {
        PlacementList.entries.forEach { list ->
            val repo = repository(installed())
            assertEquals(emptyMap<Long, List<AppInfo>>(), repo.observeAppsForFacets(list, emptyList()).first())
            assertEquals(emptyMap<Long, List<PlacedItem>>(), repo.observeItemsForFacets(list, emptyList()).first())
        }
    }

    @Test
    fun `raw reads include apps that are not installed, for backup export`() = runTest {
        allTargets.forEach { target ->
            // Given a stored app and folder placement for an app that is not installed
            val folderDao = FakeFolderDao()
            val folderId = folderDao.insertFolder(FolderEntity(name = "Games"))
            val repo = repository(installed(), folderDao = folderDao)
            repo.addApp(target, appInfo('x'), position = 0)
            repo.placeFolder(target, folderId, position = 1)

            // Then the raw reads still return them, though hydrated reads would not
            assertEquals(listOf("com.example.x"), repo.getRawApps(target).map { it.packageName })
            assertEquals(listOf(folderId), repo.getRawFolderPlacements(target).map { it.folderId })
            assertEquals(emptyList<AppInfo>(), repo.observeApps(target).first())
        }
    }

    @Test
    fun `restoreApp inserts a brand-new row and ignores the entity's own id`() = runTest {
        // Given a restored entity carrying a stale id
        val dao = FakeAppPlacementDao()
        val repo = repository(installed(), dao)
        val restored = AppPlacementEntity(id = 99, list = PlacementList.DOCK, facetId = facetOne, packageName = "com.example.a", activityName = ".Main", position = 0, userId = 0)

        // When restoring it
        repo.restoreApp(restored)

        // Then it is stored under a fresh id
        val stored = repo.getRawApps(PlacementTarget(PlacementList.DOCK, facetOne)).single()
        assertTrue(stored.id != 99L)
        assertEquals("com.example.a", stored.packageName)
    }

    @Test
    fun `restoreFolderPlacement places a restored folder into the given list`() = runTest {
        allTargets.forEach { target ->
            val folderDao = FakeFolderDao()
            val folderId = folderDao.insertFolder(FolderEntity(name = "Games"))
            val repo = repository(installed(), folderDao = folderDao)

            repo.restoreFolderPlacement(target, folderId, position = 3)

            assertEquals(listOf(FolderPlacementEntity(id = 1, list = target.list, facetId = target.facetId, folderId = folderId, position = 3)), repo.getRawFolderPlacements(target))
        }
    }

    @Test
    fun `orphaned rows are found across every list and backfilled in place`() = runTest {
        // Given an orphaned (userId -1) row in the launcher-wide Dock and one in facet 1's Favorites, plus a healthy row
        val dao = FakeAppPlacementDao()
        dao.upsert(AppPlacementEntity(list = PlacementList.DOCK, packageName = "com.example.a", activityName = ".Main", position = 0, userId = -1))
        dao.upsert(AppPlacementEntity(list = PlacementList.FAVORITES, facetId = facetOne, packageName = "com.example.b", activityName = ".Main", position = 0, userId = -1))
        dao.upsert(AppPlacementEntity(list = PlacementList.DOCK, packageName = "com.example.c", activityName = ".Main", position = 1, userId = 0))
        val repo = repository(installed(), dao)

        // Then both orphans are reported
        val orphans = repo.getOrphanedRows()
        assertEquals(setOf("com.example.a", "com.example.b"), orphans.map { it.packageName }.toSet())

        // When one is backfilled
        repo.backfillUserId(orphans.first { it.packageName == "com.example.a" }, userId = 10)

        // Then it is updated in place, not duplicated, and no longer reported
        assertEquals(listOf(10, 0), repo.getRawApps(PlacementTarget.GlobalDock).map { it.userId })
        assertEquals(listOf("com.example.b"), repo.getOrphanedRows().map { it.packageName })
    }
}
