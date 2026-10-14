package com.facetlauncher.app.data.local

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import com.facetlauncher.app.data.model.PlacementList
import com.facetlauncher.app.data.model.GLOBAL_OWNER_KEY

/** Every behavior is checked for both lists (Dock and Favorites), since one table now backs all four former tables. */
@RunWith(RobolectricTestRunner::class)
class FolderPlacementDaoTest {

    private fun createDatabase(): FacetDatabase =
        Room.inMemoryDatabaseBuilder(ApplicationProvider.getApplicationContext(), FacetDatabase::class.java)
            .allowMainThreadQueries()
            .build()

    private fun placement(list: PlacementList, folderId: Long, position: Int = 0, facetId: Long? = null) =
        FolderPlacementEntity(list = list, facetId = facetId, folderId = folderId, position = position)

    @Test
    fun `launcher-wide placements come back ordered by position`() = runTest {
        PlacementList.entries.forEach { list ->
            // Given two folders placed out of position order
            val database = createDatabase()
            val a = database.folderDao().insertFolder(FolderEntity(name = "A"))
            val b = database.folderDao().insertFolder(FolderEntity(name = "B"))
            database.folderPlacementDao().upsert(placement(list, b, position = 1))
            database.folderPlacementDao().upsert(placement(list, a, position = 0))

            // Then they come back ordered by position
            assertEquals(listOf(a, b), database.folderPlacementDao().observe(list, GLOBAL_OWNER_KEY).first().map { it.folderId })
        }
    }

    @Test
    fun `upsert on the same folder replaces its position rather than duplicating`() = runTest {
        PlacementList.entries.forEach { list ->
            // Given a folder placed launcher-wide, which has a null facetId and so needs the ownerKey to be unique
            val database = createDatabase()
            val folderId = database.folderDao().insertFolder(FolderEntity(name = "Games"))
            database.folderPlacementDao().upsert(placement(list, folderId, position = 0))

            // When it is placed again at a new position
            database.folderPlacementDao().upsert(placement(list, folderId, position = 4))

            // Then there is one row, at the new position
            val rows = database.folderPlacementDao().observe(list, GLOBAL_OWNER_KEY).first()
            assertEquals(1, rows.size)
            assertEquals(4, rows.single().position)
        }
    }

    @Test
    fun `the same folder can sit in every list and owner without collisions`() = runTest {
        // Given one folder placed in the Dock and Favorites, launcher-wide and in a facet
        val database = createDatabase()
        val dao = database.folderPlacementDao()
        val facetId = database.facetDao().insert(FacetEntity(name = "One", position = 0))
        val folderId = database.folderDao().insertFolder(FolderEntity(name = "Games"))
        PlacementList.entries.forEach { list ->
            dao.upsert(placement(list, folderId, position = 0))
            dao.upsert(placement(list, folderId, position = 1, facetId = facetId))
        }

        // Then each of the four views holds exactly its own placement
        PlacementList.entries.forEach { list ->
            assertEquals(listOf(0), dao.observe(list, GLOBAL_OWNER_KEY).first().map { it.position })
            assertEquals(listOf(1), dao.observe(list, facetId).first().map { it.position })
        }
    }

    @Test
    fun `deleteByFolderId un-places the folder from that owner and list only, without deleting it`() = runTest {
        // Given a folder placed in the Dock and Favorites, launcher-wide
        val database = createDatabase()
        val dao = database.folderPlacementDao()
        val folderId = database.folderDao().insertFolder(FolderEntity(name = "Games"))
        PlacementList.entries.forEach { dao.upsert(placement(it, folderId)) }

        // When it is un-placed from the Dock
        dao.deleteByFolderId(PlacementList.DOCK, GLOBAL_OWNER_KEY, folderId)

        // Then the Favorites placement and the folder itself remain
        assertTrue(dao.observe(PlacementList.DOCK, GLOBAL_OWNER_KEY).first().isEmpty())
        assertEquals(1, dao.observe(PlacementList.FAVORITES, GLOBAL_OWNER_KEY).first().size)
        assertEquals(1, database.folderDao().observeAllWithApps().first().size)
    }

    @Test
    fun `deleteAllForOwner wipes one list for that owner only`() = runTest {
        // Given a folder placed in both lists for the launcher-wide owner and a facet
        val database = createDatabase()
        val dao = database.folderPlacementDao()
        val facetId = database.facetDao().insert(FacetEntity(name = "One", position = 0))
        val folderId = database.folderDao().insertFolder(FolderEntity(name = "Games"))
        PlacementList.entries.forEach { list ->
            dao.upsert(placement(list, folderId))
            dao.upsert(placement(list, folderId, facetId = facetId))
        }

        // When the facet's Favorites folders are wiped
        dao.deleteAllForOwner(PlacementList.FAVORITES, facetId)

        // Then nothing else changes
        assertTrue(dao.observe(PlacementList.FAVORITES, facetId).first().isEmpty())
        assertEquals(1, dao.observe(PlacementList.FAVORITES, GLOBAL_OWNER_KEY).first().size)
        assertEquals(1, dao.observe(PlacementList.DOCK, facetId).first().size)
        assertEquals(1, dao.observe(PlacementList.DOCK, GLOBAL_OWNER_KEY).first().size)
    }

    @Test
    fun `deleting the parent folder cascades and removes all its placements`() = runTest {
        // Given a folder placed in every list and owner
        val database = createDatabase()
        val dao = database.folderPlacementDao()
        val facetId = database.facetDao().insert(FacetEntity(name = "One", position = 0))
        val folderId = database.folderDao().insertFolder(FolderEntity(name = "Games"))
        PlacementList.entries.forEach { list ->
            dao.upsert(placement(list, folderId))
            dao.upsert(placement(list, folderId, facetId = facetId))
        }

        // When the folder itself is deleted
        database.folderDao().deleteFolder(folderId)

        // Then every placement is gone
        PlacementList.entries.forEach { list ->
            assertTrue(dao.observe(list, GLOBAL_OWNER_KEY).first().isEmpty())
            assertTrue(dao.observe(list, facetId).first().isEmpty())
        }
    }

    @Test
    fun `deleting a facet cascades to its folder placements but not launcher-wide ones`() = runTest {
        // Given a folder placed launcher-wide and in a facet, in both lists
        val database = createDatabase()
        val dao = database.folderPlacementDao()
        val facetId = database.facetDao().insert(FacetEntity(name = "One", position = 0))
        val folderId = database.folderDao().insertFolder(FolderEntity(name = "Games"))
        PlacementList.entries.forEach { list ->
            dao.upsert(placement(list, folderId))
            dao.upsert(placement(list, folderId, facetId = facetId))
        }

        // When the facet is deleted
        database.facetDao().delete(FacetEntity(id = facetId, name = "One", position = 0))

        // Then its placements are gone and the launcher-wide ones remain
        PlacementList.entries.forEach { list ->
            assertTrue(dao.observe(list, facetId).first().isEmpty())
            assertEquals(1, dao.observe(list, GLOBAL_OWNER_KEY).first().size)
        }
    }
}
