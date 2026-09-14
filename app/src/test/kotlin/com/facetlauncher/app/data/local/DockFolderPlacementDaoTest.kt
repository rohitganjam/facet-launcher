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

@RunWith(RobolectricTestRunner::class)
class DockFolderPlacementDaoTest {

    private fun createDatabase(): FacetDatabase =
        Room.inMemoryDatabaseBuilder(ApplicationProvider.getApplicationContext(), FacetDatabase::class.java)
            .allowMainThreadQueries()
            .build()

    @Test
    fun `insert then observe returns the placement ordered by position`() = runTest {
        // Given two folders placed in the dock out of position order
        val database = createDatabase()
        val folderA = database.folderDao().insertFolder(FolderEntity(name = "A"))
        val folderB = database.folderDao().insertFolder(FolderEntity(name = "B"))
        database.dockFolderPlacementDao().upsert(DockFolderPlacementEntity(folderId = folderB, position = 1))
        database.dockFolderPlacementDao().upsert(DockFolderPlacementEntity(folderId = folderA, position = 0))

        // Then observing returns them ordered by position
        val result = database.dockFolderPlacementDao().observeAll().first()
        assertEquals(listOf(folderA, folderB), result.map { it.folderId })
    }

    @Test
    fun `deleting the parent folder cascades and removes its dock placement too`() = runTest {
        // Given a folder placed in the dock — the load-bearing new behavior this table exists
        // for: a folder's placements must disappear when the folder itself is deleted, without
        // any manual fan-out in the repository layer.
        val database = createDatabase()
        val folderId = database.folderDao().insertFolder(FolderEntity(name = "Games"))
        database.dockFolderPlacementDao().upsert(DockFolderPlacementEntity(folderId = folderId, position = 0))

        // When the folder itself is deleted
        database.folderDao().deleteFolder(folderId)

        // Then its dock placement is gone too (foreign key cascade)
        assertTrue(database.dockFolderPlacementDao().observeAll().first().isEmpty())
    }

    @Test
    fun `upsert on the same folder replaces its position rather than duplicating`() = runTest {
        // Given a folder already placed at position 0
        val database = createDatabase()
        val folderId = database.folderDao().insertFolder(FolderEntity(name = "Games"))
        database.dockFolderPlacementDao().upsert(DockFolderPlacementEntity(folderId = folderId, position = 0))

        // When it's placed again at a new position (a reorder)
        database.dockFolderPlacementDao().upsert(DockFolderPlacementEntity(folderId = folderId, position = 3))

        // Then there is still only one placement row, now at the new position
        val result = database.dockFolderPlacementDao().observeAll().first()
        assertEquals(1, result.size)
        assertEquals(3, result.first().position)
    }

    @Test
    fun `deleteByFolderId un-places the folder without deleting it`() = runTest {
        // Given a folder placed in the dock
        val database = createDatabase()
        val folderId = database.folderDao().insertFolder(FolderEntity(name = "Games"))
        database.dockFolderPlacementDao().upsert(DockFolderPlacementEntity(folderId = folderId, position = 0))

        // When it's un-placed
        database.dockFolderPlacementDao().deleteByFolderId(folderId)

        // Then the placement is gone but the folder itself still exists
        assertTrue(database.dockFolderPlacementDao().observeAll().first().isEmpty())
        assertEquals(1, database.folderDao().observeAllWithApps().first().size)
    }
}
