package com.facetlauncher.app.data.local

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.facetlauncher.app.data.model.AppProfile
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class FacetDockAppDaoTest {

    private fun createDatabase(): FacetDatabase =
        Room.inMemoryDatabaseBuilder(ApplicationProvider.getApplicationContext(), FacetDatabase::class.java)
            .allowMainThreadQueries()
            .build()

    @Test
    fun `insert then observe returns the inserted dock app scoped to its facet, ordered by position`() = runTest {
        // Given a facet and an empty per-facet dock table
        val database = createDatabase()
        val facetId = database.facetDao().insert(FacetEntity(name = "Facet 1", position = 0))

        // When two dock apps are inserted out of position order
        database.facetDockAppDao().upsert(
            FacetDockAppEntity(facetId = facetId, packageName = "com.example.b", activityName = ".Main", position = 1),
        )
        database.facetDockAppDao().upsert(
            FacetDockAppEntity(facetId = facetId, packageName = "com.example.a", activityName = ".Main", position = 0),
        )

        // Then observing that facet returns them ordered by position
        val result = database.facetDockAppDao().observeForFacet(facetId).first()
        assertEquals(listOf("com.example.a", "com.example.b"), result.map { it.packageName })
    }

    @Test
    fun `dock apps are scoped per facet, not shared`() = runTest {
        // Given two facets, each with a dock app
        val database = createDatabase()
        val facetOneId = database.facetDao().insert(FacetEntity(name = "One", position = 0))
        val facetTwoId = database.facetDao().insert(FacetEntity(name = "Two", position = 1))
        database.facetDockAppDao().upsert(
            FacetDockAppEntity(facetId = facetOneId, packageName = "com.example.a", activityName = ".Main", position = 0),
        )
        database.facetDockAppDao().upsert(
            FacetDockAppEntity(facetId = facetTwoId, packageName = "com.example.b", activityName = ".Main", position = 0),
        )

        // Then observing one facet never returns the other's dock apps
        val resultForFacetOne = database.facetDockAppDao().observeForFacet(facetOneId).first()
        assertEquals(listOf("com.example.a"), resultForFacetOne.map { it.packageName })
    }

    @Test
    fun `deleting a facet cascades to its dock apps`() = runTest {
        // Given a facet with a dock app
        val database = createDatabase()
        val facetId = database.facetDao().insert(FacetEntity(name = "Facet 1", position = 0))
        database.facetDockAppDao().upsert(
            FacetDockAppEntity(facetId = facetId, packageName = "com.example.a", activityName = ".Main", position = 0),
        )

        // When the facet is deleted
        database.facetDao().delete(FacetEntity(id = facetId, name = "Facet 1", position = 0))

        // Then its dock apps are gone too (foreign key cascade)
        assertTrue(database.facetDockAppDao().observeForFacet(facetId).first().isEmpty())
    }

    @Test
    fun `deleteByPackage removes every facet's entry for that package`() = runTest {
        // Given the same app in two facets' docks
        val database = createDatabase()
        val facetOneId = database.facetDao().insert(FacetEntity(name = "One", position = 0))
        val facetTwoId = database.facetDao().insert(FacetEntity(name = "Two", position = 1))
        database.facetDockAppDao().upsert(
            FacetDockAppEntity(facetId = facetOneId, packageName = "com.example.a", activityName = ".Main", position = 0),
        )
        database.facetDockAppDao().upsert(
            FacetDockAppEntity(facetId = facetTwoId, packageName = "com.example.a", activityName = ".Main", position = 0),
        )

        // When the package is cleaned up after an uninstall
        database.facetDockAppDao().deleteByPackage("com.example.a", AppProfile.PERSONAL)

        // Then it's gone from every facet's dock
        assertTrue(database.facetDockAppDao().observeForFacet(facetOneId).first().isEmpty())
        assertTrue(database.facetDockAppDao().observeForFacet(facetTwoId).first().isEmpty())
    }
}
