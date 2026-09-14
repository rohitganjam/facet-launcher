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
class FavoriteFolderPlacementDaoTest {

    private fun createDatabase(): FacetDatabase =
        Room.inMemoryDatabaseBuilder(ApplicationProvider.getApplicationContext(), FacetDatabase::class.java)
            .allowMainThreadQueries()
            .build()

    @Test
    fun `insert then observe returns the placement scoped to its facet, ordered by position`() = runTest {
        // Given a facet and two folders placed in its own favorites out of position order
        val database = createDatabase()
        val facetId = database.facetDao().insert(FacetEntity(name = "Facet 1", position = 0))
        val folderA = database.folderDao().insertFolder(FolderEntity(name = "A"))
        val folderB = database.folderDao().insertFolder(FolderEntity(name = "B"))
        database.favoriteFolderPlacementDao().upsert(FavoriteFolderPlacementEntity(facetId = facetId, folderId = folderB, position = 1))
        database.favoriteFolderPlacementDao().upsert(FavoriteFolderPlacementEntity(facetId = facetId, folderId = folderA, position = 0))

        // Then observing that facet returns them ordered by position
        val result = database.favoriteFolderPlacementDao().observeForFacet(facetId).first()
        assertEquals(listOf(folderA, folderB), result.map { it.folderId })
    }

    @Test
    fun `placements are scoped per facet, not shared`() = runTest {
        // Given the same folder placed in two different facets' own favorites
        val database = createDatabase()
        val facetOneId = database.facetDao().insert(FacetEntity(name = "One", position = 0))
        val facetTwoId = database.facetDao().insert(FacetEntity(name = "Two", position = 1))
        val folderId = database.folderDao().insertFolder(FolderEntity(name = "Games"))
        database.favoriteFolderPlacementDao().upsert(FavoriteFolderPlacementEntity(facetId = facetOneId, folderId = folderId, position = 0))

        // Then only facet one sees it — facet two sees none
        assertEquals(1, database.favoriteFolderPlacementDao().observeForFacet(facetOneId).first().size)
        assertTrue(database.favoriteFolderPlacementDao().observeForFacet(facetTwoId).first().isEmpty())
    }

    @Test
    fun `deleting the parent folder cascades and removes its facet favorites placement too`() = runTest {
        // Given a folder placed in a facet's own favorites
        val database = createDatabase()
        val facetId = database.facetDao().insert(FacetEntity(name = "Facet 1", position = 0))
        val folderId = database.folderDao().insertFolder(FolderEntity(name = "Games"))
        database.favoriteFolderPlacementDao().upsert(FavoriteFolderPlacementEntity(facetId = facetId, folderId = folderId, position = 0))

        // When the folder itself is deleted
        database.folderDao().deleteFolder(folderId)

        // Then its placement is gone too (foreign key cascade)
        assertTrue(database.favoriteFolderPlacementDao().observeForFacet(facetId).first().isEmpty())
    }

    @Test
    fun `deleting the facet cascades and removes its own favorites folder placements too`() = runTest {
        // Given a facet with a folder placed in its own favorites
        val database = createDatabase()
        val facet = FacetEntity(name = "Facet 1", position = 0)
        val facetId = database.facetDao().insert(facet)
        val folderId = database.folderDao().insertFolder(FolderEntity(name = "Games"))
        database.favoriteFolderPlacementDao().upsert(FavoriteFolderPlacementEntity(facetId = facetId, folderId = folderId, position = 0))

        // When the facet is deleted
        database.facetDao().delete(facet.copy(id = facetId))

        // Then its favorites folder placements are gone too (foreign key cascade), the folder itself survives
        assertTrue(database.favoriteFolderPlacementDao().observeForFacet(facetId).first().isEmpty())
        assertEquals(1, database.folderDao().observeAllWithApps().first().size)
    }

    @Test
    fun `deleteByFolderId un-places the folder from that facet without deleting it`() = runTest {
        // Given a folder placed in a facet's own favorites
        val database = createDatabase()
        val facetId = database.facetDao().insert(FacetEntity(name = "Facet 1", position = 0))
        val folderId = database.folderDao().insertFolder(FolderEntity(name = "Games"))
        database.favoriteFolderPlacementDao().upsert(FavoriteFolderPlacementEntity(facetId = facetId, folderId = folderId, position = 0))

        // When it's un-placed
        database.favoriteFolderPlacementDao().deleteByFolderId(facetId, folderId)

        // Then the placement is gone but the folder itself still exists
        assertTrue(database.favoriteFolderPlacementDao().observeForFacet(facetId).first().isEmpty())
        assertEquals(1, database.folderDao().observeAllWithApps().first().size)
    }
}
