package com.facetlauncher.app.data.local

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.facetlauncher.app.data.model.ListContentMode
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

/**
 * Real Room-backed (not a fake in-memory [FacetDao]) — these actually exercise [Converters],
 * which a fake DAO never touches. That gap is exactly how a real bug shipped undetected: the
 * [ListContentMode]-to-[String] converters were declared non-null in/out for a *nullable* column,
 * which Room silently can't bind — every write landed as SQL NULL regardless of what was passed
 * in, so "Override for this facet" on the App list content card never actually stuck.
 * `FacetRepositoryTest`'s fake in-memory DAO round-tripped fine (it just stores the Kotlin
 * object directly) and so never caught it; this suite would have.
 */
@RunWith(RobolectricTestRunner::class)
class FacetDaoTest {

    private fun createDatabase(): FacetDatabase =
        Room.inMemoryDatabaseBuilder(ApplicationProvider.getApplicationContext(), FacetDatabase::class.java)
            .allowMainThreadQueries()
            .build()

    private fun createDao(): FacetDao = createDatabase().facetDao()

    @Test
    fun `a non-null listContentMode round-trips through Room, not just an in-memory copy`() = runTest {
        // Given a stored facet with no override
        val dao = createDao()
        val id = dao.insert(FacetEntity(name = "Facet 1", position = 0))

        // When it's updated again with a real (non-default) mode
        dao.update(FacetEntity(id = id, name = "Facet 1", position = 0, listContentMode = ListContentMode.MOST_USED))

        // Then a fresh read (forcing the value through Converters.toListContentMode) reflects it
        val result = dao.observeAll().first().single()
        assertEquals(ListContentMode.MOST_USED, result.listContentMode)
    }

    @Test
    fun `toggling overrideApps flag round-trips`() = runTest {
        // Given a facet inheriting global apps
        val dao = createDao()
        val id = dao.insert(FacetEntity(name = "Facet 1", position = 0, overrideApps = false))

        // When it's switched to override
        dao.update(FacetEntity(id = id, name = "Facet 1", position = 0, overrideApps = true))

        // Then it reflects the new flag
        val result = dao.observeAll().first().single()
        assertEquals(true, result.overrideApps)
    }

    @Test
    fun `every ListContentMode value round-trips`() = runTest {
        val dao = createDao()
        val id = dao.insert(FacetEntity(name = "Facet 1", position = 0))

        for (mode in ListContentMode.entries) {
            dao.update(FacetEntity(id = id, name = "Facet 1", position = 0, listContentMode = mode))
            assertEquals(mode, dao.observeAll().first().single().listContentMode)
        }
    }

    @Test
    fun `updating an unrelated facet field does not wipe its favorites or dock apps`() = runTest {
        // Given a facet with its own favorites and dock apps
        val database = createDatabase()
        val dao = database.facetDao()
        val favoriteAppDao = database.favoriteAppDao()
        val facetDockAppDao = database.facetDockAppDao()
        val id = dao.insert(FacetEntity(name = "Facet 1", position = 0))
        favoriteAppDao.upsert(FavoriteAppEntity(facetId = id, packageName = "com.example.a", activityName = ".Main", position = 0))
        facetDockAppDao.upsert(FacetDockAppEntity(facetId = id, packageName = "com.example.b", activityName = ".Main", position = 0))

        // When an unrelated field (e.g. toggling clock override) is updated on that same facet
        dao.update(FacetEntity(id = id, name = "Facet 1", position = 0, overrideClock = true))

        // Then the favorites and dock apps are untouched — a real UPDATE never cascades a delete
        assertEquals(1, favoriteAppDao.observeForFacet(id).first().size)
        assertEquals(1, facetDockAppDao.observeForFacet(id).first().size)
    }
}
