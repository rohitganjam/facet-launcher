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
class FavoriteAppDaoTest {

    private fun createDatabase(): FacetDatabase =
        Room.inMemoryDatabaseBuilder(ApplicationProvider.getApplicationContext(), FacetDatabase::class.java)
            .allowMainThreadQueries()
            .build()

    @Test
    fun `insert then observe returns the inserted favorite scoped to its facet`() = runTest {
        // Given a facet and an empty favorites table
        val database = createDatabase()
        val facetId = database.facetDao().insert(FacetEntity(name = "Facet 1", position = 0))

        // When a favorite is inserted for that facet
        database.favoriteAppDao().upsert(
            FavoriteAppEntity(facetId = facetId, packageName = "com.example.a", activityName = ".Main", position = 0),
        )

        // Then observing that facet returns exactly that favorite
        val result = database.favoriteAppDao().observeForFacet(facetId).first()
        assertEquals(1, result.size)
        assertEquals("com.example.a", result.first().packageName)
    }

    @Test
    fun `favorites are scoped per facet, not shared`() = runTest {
        // Given two facets, each with a favorite
        val database = createDatabase()
        val facetOneId = database.facetDao().insert(FacetEntity(name = "One", position = 0))
        val facetTwoId = database.facetDao().insert(FacetEntity(name = "Two", position = 1))
        database.favoriteAppDao().upsert(
            FavoriteAppEntity(facetId = facetOneId, packageName = "com.example.a", activityName = ".Main", position = 0),
        )
        database.favoriteAppDao().upsert(
            FavoriteAppEntity(facetId = facetTwoId, packageName = "com.example.b", activityName = ".Main", position = 0),
        )

        // Then observing one facet never returns the other's favorites
        val resultForFacetOne = database.favoriteAppDao().observeForFacet(facetOneId).first()
        assertEquals(listOf("com.example.a"), resultForFacetOne.map { it.packageName })
    }

    @Test
    fun `deleting a facet cascades to its favorites`() = runTest {
        // Given a facet with a favorite
        val database = createDatabase()
        val facetId = database.facetDao().insert(FacetEntity(name = "Facet 1", position = 0))
        database.favoriteAppDao().upsert(
            FavoriteAppEntity(facetId = facetId, packageName = "com.example.a", activityName = ".Main", position = 0),
        )

        // When the facet is deleted
        database.facetDao().delete(FacetEntity(id = facetId, name = "Facet 1", position = 0))

        // Then its favorites are gone too (foreign key cascade)
        assertTrue(database.favoriteAppDao().observeForFacet(facetId).first().isEmpty())
    }

    @Test
    fun `deleteByComponent removes only the matching facet's entry`() = runTest {
        // Given the same app favorited under two different facets
        val database = createDatabase()
        val facetOneId = database.facetDao().insert(FacetEntity(name = "One", position = 0))
        val facetTwoId = database.facetDao().insert(FacetEntity(name = "Two", position = 1))
        database.favoriteAppDao().upsert(
            FavoriteAppEntity(facetId = facetOneId, packageName = "com.example.a", activityName = ".Main", position = 0),
        )
        database.favoriteAppDao().upsert(
            FavoriteAppEntity(facetId = facetTwoId, packageName = "com.example.a", activityName = ".Main", position = 0),
        )

        // When deleting it by component for just facet one
        database.favoriteAppDao().deleteByComponent(facetOneId, "com.example.a", ".Main", -1)

        // Then only facet one's entry is gone
        assertTrue(database.favoriteAppDao().observeForFacet(facetOneId).first().isEmpty())
        assertEquals(1, database.favoriteAppDao().observeForFacet(facetTwoId).first().size)
    }
}
