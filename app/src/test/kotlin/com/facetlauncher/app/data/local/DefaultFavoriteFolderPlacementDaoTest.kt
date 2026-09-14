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
class DefaultFavoriteFolderPlacementDaoTest {

    private fun createDatabase(): FacetDatabase =
        Room.inMemoryDatabaseBuilder(ApplicationProvider.getApplicationContext(), FacetDatabase::class.java)
            .allowMainThreadQueries()
            .build()

    @Test
    fun `insert then observe returns the placement ordered by position`() = runTest {
        // Given two folders placed in the default favorites list out of position order
        val database = createDatabase()
        val folderA = database.folderDao().insertFolder(FolderEntity(name = "A"))
        val folderB = database.folderDao().insertFolder(FolderEntity(name = "B"))
        database.defaultFavoriteFolderPlacementDao().upsert(DefaultFavoriteFolderPlacementEntity(folderId = folderB, position = 1))
        database.defaultFavoriteFolderPlacementDao().upsert(DefaultFavoriteFolderPlacementEntity(folderId = folderA, position = 0))

        // Then observing returns them ordered by position
        val result = database.defaultFavoriteFolderPlacementDao().observeAll().first()
        assertEquals(listOf(folderA, folderB), result.map { it.folderId })
    }

    @Test
    fun `deleting the parent folder cascades and removes its default-favorites placement too`() = runTest {
        // Given a folder placed in the default favorites list
        val database = createDatabase()
        val folderId = database.folderDao().insertFolder(FolderEntity(name = "Games"))
        database.defaultFavoriteFolderPlacementDao().upsert(DefaultFavoriteFolderPlacementEntity(folderId = folderId, position = 0))

        // When the folder itself is deleted
        database.folderDao().deleteFolder(folderId)

        // Then its placement is gone too (foreign key cascade)
        assertTrue(database.defaultFavoriteFolderPlacementDao().observeAll().first().isEmpty())
    }

    @Test
    fun `upsert on the same folder replaces its position rather than duplicating`() = runTest {
        // Given a folder already placed at position 0
        val database = createDatabase()
        val folderId = database.folderDao().insertFolder(FolderEntity(name = "Games"))
        database.defaultFavoriteFolderPlacementDao().upsert(DefaultFavoriteFolderPlacementEntity(folderId = folderId, position = 0))

        // When it's placed again at a new position (a reorder)
        database.defaultFavoriteFolderPlacementDao().upsert(DefaultFavoriteFolderPlacementEntity(folderId = folderId, position = 3))

        // Then there is still only one placement row, now at the new position
        val result = database.defaultFavoriteFolderPlacementDao().observeAll().first()
        assertEquals(1, result.size)
        assertEquals(3, result.first().position)
    }

    @Test
    fun `deleteByFolderId un-places the folder without deleting it`() = runTest {
        // Given a folder placed in the default favorites list
        val database = createDatabase()
        val folderId = database.folderDao().insertFolder(FolderEntity(name = "Games"))
        database.defaultFavoriteFolderPlacementDao().upsert(DefaultFavoriteFolderPlacementEntity(folderId = folderId, position = 0))

        // When it's un-placed
        database.defaultFavoriteFolderPlacementDao().deleteByFolderId(folderId)

        // Then the placement is gone but the folder itself still exists
        assertTrue(database.defaultFavoriteFolderPlacementDao().observeAll().first().isEmpty())
        assertEquals(1, database.folderDao().observeAllWithApps().first().size)
    }
}
