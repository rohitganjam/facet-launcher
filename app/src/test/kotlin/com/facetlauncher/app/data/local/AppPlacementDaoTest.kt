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

/** Every behavior is checked for both lists (Dock and Favorites), since one table now backs all four former tables. */
@RunWith(RobolectricTestRunner::class)
class AppPlacementDaoTest {

    private fun createDatabase(): FacetDatabase =
        Room.inMemoryDatabaseBuilder(ApplicationProvider.getApplicationContext(), FacetDatabase::class.java)
            .allowMainThreadQueries()
            .build()

    private fun app(list: PlacementList, pkg: String, position: Int = 0, facetId: Long? = null, userId: Int = 0) =
        AppPlacementEntity(list = list, facetId = facetId, packageName = pkg, activityName = ".Main", position = position, userId = userId)

    @Test
    fun `launcher-wide rows come back ordered by position`() = runTest {
        PlacementList.entries.forEach { list ->
            // Given launcher-wide rows inserted out of position order
            val dao = createDatabase().appPlacementDao()
            dao.upsert(app(list, "b", position = 1))
            dao.upsert(app(list, "a", position = 0))

            // Then they come back ordered by position
            assertEquals(listOf("a", "b"), dao.observe(list, GLOBAL_OWNER_KEY).first().map { it.packageName })
        }
    }

    @Test
    fun `upserting the same launcher-wide component twice updates it in place rather than duplicating`() = runTest {
        PlacementList.entries.forEach { list ->
            // Given a launcher-wide row, which has a null facetId and so needs the ownerKey to be unique
            val dao = createDatabase().appPlacementDao()
            dao.upsert(app(list, "a", position = 0))

            // When the same component is upserted again at a new position
            dao.upsert(app(list, "a", position = 3))

            // Then there is still one row, at the new position
            val rows = dao.observe(list, GLOBAL_OWNER_KEY).first()
            assertEquals(1, rows.size)
            assertEquals(3, rows.single().position)
        }
    }

    @Test
    fun `lists and owners never see each other's rows`() = runTest {
        // Given the same component stored in every list and owner combination
        val database = createDatabase()
        val dao = database.appPlacementDao()
        val facetId = database.facetDao().insert(FacetEntity(name = "One", position = 0))
        PlacementList.entries.forEach { list ->
            dao.upsert(app(list, "same", position = 0))
            dao.upsert(app(list, "same", position = 1, facetId = facetId))
        }

        // Then each of the four views holds exactly its own row
        PlacementList.entries.forEach { list ->
            assertEquals(listOf(0), dao.observe(list, GLOBAL_OWNER_KEY).first().map { it.position })
            assertEquals(listOf(1), dao.observe(list, facetId).first().map { it.position })
        }
    }

    @Test
    fun `facet rows are scoped per facet`() = runTest {
        PlacementList.entries.forEach { list ->
            // Given one app in each of two facets' own lists
            val database = createDatabase()
            val dao = database.appPlacementDao()
            val one = database.facetDao().insert(FacetEntity(name = "One", position = 0))
            val two = database.facetDao().insert(FacetEntity(name = "Two", position = 1))
            dao.upsert(app(list, "x", facetId = one))
            dao.upsert(app(list, "y", facetId = two))

            // Then each facet sees only its own
            assertEquals(listOf("x"), dao.observe(list, one).first().map { it.packageName })
            assertEquals(listOf("y"), dao.observe(list, two).first().map { it.packageName })
        }
    }

    @Test
    fun `deleteByComponent removes only the matching owner's row in that list`() = runTest {
        // Given the same component in the Dock and Favorites, launcher-wide and in a facet
        val database = createDatabase()
        val dao = database.appPlacementDao()
        val facetId = database.facetDao().insert(FacetEntity(name = "One", position = 0))
        PlacementList.entries.forEach { list ->
            dao.upsert(app(list, "a"))
            dao.upsert(app(list, "a", facetId = facetId))
        }

        // When the launcher-wide Dock entry is deleted
        dao.deleteByComponent(PlacementList.DOCK, GLOBAL_OWNER_KEY, "a", ".Main", 0)

        // Then only that one row is gone
        assertTrue(dao.observe(PlacementList.DOCK, GLOBAL_OWNER_KEY).first().isEmpty())
        assertEquals(1, dao.observe(PlacementList.DOCK, facetId).first().size)
        assertEquals(1, dao.observe(PlacementList.FAVORITES, GLOBAL_OWNER_KEY).first().size)
        assertEquals(1, dao.observe(PlacementList.FAVORITES, facetId).first().size)
    }

    @Test
    fun `deleteByPackage with global true removes only launcher-wide rows of that list for that user`() = runTest {
        // Given a launcher-wide and a facet Dock row for the same package, plus a Favorites row and another user's row
        val database = createDatabase()
        val dao = database.appPlacementDao()
        val facetId = database.facetDao().insert(FacetEntity(name = "One", position = 0))
        dao.upsert(app(PlacementList.DOCK, "p", position = 0))
        dao.upsert(app(PlacementList.DOCK, "p", position = 1, userId = 10))
        dao.upsert(app(PlacementList.DOCK, "p", facetId = facetId))
        dao.upsert(app(PlacementList.FAVORITES, "p"))

        // When uninstall cleanup targets the launcher-wide Dock for user 0
        dao.deleteByPackage(PlacementList.DOCK, global = true, packageName = "p", userId = 0)

        // Then only that row is gone
        assertEquals(listOf(10), dao.observe(PlacementList.DOCK, GLOBAL_OWNER_KEY).first().map { it.userId })
        assertEquals(1, dao.observe(PlacementList.DOCK, facetId).first().size)
        assertEquals(1, dao.observe(PlacementList.FAVORITES, GLOBAL_OWNER_KEY).first().size)
    }

    @Test
    fun `deleteByPackage with global false removes every facet's rows of that list but not launcher-wide ones`() = runTest {
        PlacementList.entries.forEach { list ->
            // Given the same package in the launcher-wide list and in two facets
            val database = createDatabase()
            val dao = database.appPlacementDao()
            val one = database.facetDao().insert(FacetEntity(name = "One", position = 0))
            val two = database.facetDao().insert(FacetEntity(name = "Two", position = 1))
            dao.upsert(app(list, "p"))
            dao.upsert(app(list, "p", facetId = one))
            dao.upsert(app(list, "p", facetId = two))

            // When facet cleanup runs
            dao.deleteByPackage(list, global = false, packageName = "p", userId = 0)

            // Then both facets lose it and the launcher-wide row stays
            assertTrue(dao.observe(list, one).first().isEmpty())
            assertTrue(dao.observe(list, two).first().isEmpty())
            assertEquals(1, dao.observe(list, GLOBAL_OWNER_KEY).first().size)
        }
    }

    @Test
    fun `deleteByUserId removes only that user's rows in the chosen scope`() = runTest {
        PlacementList.entries.forEach { list ->
            // Given a personal and a work-profile row, launcher-wide and in a facet
            val database = createDatabase()
            val dao = database.appPlacementDao()
            val facetId = database.facetDao().insert(FacetEntity(name = "One", position = 0))
            dao.upsert(app(list, "a", userId = 0))
            dao.upsert(app(list, "w", userId = 10))
            dao.upsert(app(list, "fw", facetId = facetId, userId = 10))

            // When the work profile vanishes for the launcher-wide list
            dao.deleteByUserId(list, global = true, userId = 10)

            // Then only the launcher-wide work row is gone
            assertEquals(listOf("a"), dao.observe(list, GLOBAL_OWNER_KEY).first().map { it.packageName })
            assertEquals(listOf("fw"), dao.observe(list, facetId).first().map { it.packageName })
        }
    }

    @Test
    fun `deleteAllForOwner wipes one list for that owner only`() = runTest {
        // Given rows in both lists for the launcher-wide owner and a facet
        val database = createDatabase()
        val dao = database.appPlacementDao()
        val facetId = database.facetDao().insert(FacetEntity(name = "One", position = 0))
        PlacementList.entries.forEach { list ->
            dao.upsert(app(list, "a"))
            dao.upsert(app(list, "b", facetId = facetId))
        }

        // When the facet's Dock is wiped
        dao.deleteAllForOwner(PlacementList.DOCK, facetId)

        // Then nothing else changes
        assertTrue(dao.observe(PlacementList.DOCK, facetId).first().isEmpty())
        assertEquals(1, dao.observe(PlacementList.DOCK, GLOBAL_OWNER_KEY).first().size)
        assertEquals(1, dao.observe(PlacementList.FAVORITES, facetId).first().size)
        assertEquals(1, dao.observe(PlacementList.FAVORITES, GLOBAL_OWNER_KEY).first().size)
    }

    @Test
    fun `getOrphaned returns only rows still at the minus one user sentinel in the chosen scope`() = runTest {
        PlacementList.entries.forEach { list ->
            // Given an orphaned launcher-wide row, an orphaned facet row and a healthy row
            val database = createDatabase()
            val dao = database.appPlacementDao()
            val facetId = database.facetDao().insert(FacetEntity(name = "One", position = 0))
            dao.upsert(app(list, "g", userId = -1))
            dao.upsert(app(list, "f", facetId = facetId, userId = -1))
            dao.upsert(app(list, "ok", userId = 0))

            // Then each scope reports only its own orphan
            assertEquals(listOf("g"), dao.getOrphaned(list, global = true).map { it.packageName })
            assertEquals(listOf("f"), dao.getOrphaned(list, global = false).map { it.packageName })
        }
    }

    @Test
    fun `deleting a facet cascades to its rows in both lists but leaves launcher-wide ones`() = runTest {
        // Given rows in both lists, launcher-wide and in a facet
        val database = createDatabase()
        val dao = database.appPlacementDao()
        val facetId = database.facetDao().insert(FacetEntity(name = "One", position = 0))
        PlacementList.entries.forEach { list ->
            dao.upsert(app(list, "g"))
            dao.upsert(app(list, "f", facetId = facetId))
        }

        // When the facet is deleted
        database.facetDao().delete(FacetEntity(id = facetId, name = "One", position = 0))

        // Then its rows are gone and the launcher-wide ones remain
        PlacementList.entries.forEach { list ->
            assertTrue(dao.observe(list, facetId).first().isEmpty())
            assertEquals(1, dao.observe(list, GLOBAL_OWNER_KEY).first().size)
        }
    }

    @Test
    fun `wiping every facet cascades to all facet rows but not launcher-wide ones`() = runTest {
        // Given a facet row and a launcher-wide row
        val database = createDatabase()
        val dao = database.appPlacementDao()
        val facetId = database.facetDao().insert(FacetEntity(name = "One", position = 0))
        dao.upsert(app(PlacementList.FAVORITES, "g"))
        dao.upsert(app(PlacementList.FAVORITES, "f", facetId = facetId))

        // When all facets are deleted (as a backup restore does)
        database.facetDao().deleteAll()

        // Then only the launcher-wide row remains
        assertEquals(listOf("g"), dao.observe(PlacementList.FAVORITES, GLOBAL_OWNER_KEY).first().map { it.packageName })
        assertTrue(dao.observe(PlacementList.FAVORITES, facetId).first().isEmpty())
    }
}
