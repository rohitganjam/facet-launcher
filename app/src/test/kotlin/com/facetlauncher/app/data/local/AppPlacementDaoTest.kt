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
    fun `deleteByPackage removes the package for that user from every list and owner`() = runTest {
        // Given the same package in both lists, launcher-wide and in a facet, plus another user's copy and another package
        val database = createDatabase()
        val dao = database.appPlacementDao()
        val facetId = database.facetDao().insert(FacetEntity(name = "One", position = 0))
        PlacementList.entries.forEach { list ->
            dao.upsert(app(list, "p"))
            dao.upsert(app(list, "p", facetId = facetId))
            dao.upsert(app(list, "p", position = 1, userId = 10))
            dao.upsert(app(list, "keep", position = 2))
        }

        // When uninstall cleanup targets the package for user 0
        dao.deleteByPackage("p", 0)

        // Then every user-0 copy is gone, in every list and owner; user 10's copy and the other package stay
        PlacementList.entries.forEach { list ->
            assertEquals(listOf("p", "keep"), dao.observe(list, GLOBAL_OWNER_KEY).first().map { it.packageName })
            assertEquals(listOf(10, 0), dao.observe(list, GLOBAL_OWNER_KEY).first().map { it.userId })
            assertTrue(dao.observe(list, facetId).first().isEmpty())
        }
    }

    @Test
    fun `deleteByUserId removes only that user's rows from every list and owner`() = runTest {
        // Given a personal and a work-profile row, launcher-wide and in a facet, in both lists
        val database = createDatabase()
        val dao = database.appPlacementDao()
        val facetId = database.facetDao().insert(FacetEntity(name = "One", position = 0))
        PlacementList.entries.forEach { list ->
            dao.upsert(app(list, "a", userId = 0))
            dao.upsert(app(list, "w", userId = 10))
            dao.upsert(app(list, "fw", facetId = facetId, userId = 10))
            dao.upsert(app(list, "fa", facetId = facetId, userId = 0))
        }

        // When the work profile vanishes
        dao.deleteByUserId(10)

        // Then only the user-0 rows remain, everywhere
        PlacementList.entries.forEach { list ->
            assertEquals(listOf("a"), dao.observe(list, GLOBAL_OWNER_KEY).first().map { it.packageName })
            assertEquals(listOf("fa"), dao.observe(list, facetId).first().map { it.packageName })
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
    fun `getOrphaned returns every row still at the minus one user sentinel, in any list or owner`() = runTest {
        // Given orphaned rows in both lists, launcher-wide and in a facet, and a healthy row
        val database = createDatabase()
        val dao = database.appPlacementDao()
        val facetId = database.facetDao().insert(FacetEntity(name = "One", position = 0))
        PlacementList.entries.forEach { list ->
            dao.upsert(app(list, "g-$list", userId = -1))
            dao.upsert(app(list, "f-$list", facetId = facetId, userId = -1))
            dao.upsert(app(list, "ok-$list", userId = 0))
        }

        // Then all four orphans are reported and no healthy row is
        assertEquals(
            setOf("g-DOCK", "f-DOCK", "g-FAVORITES", "f-FAVORITES"),
            dao.getOrphaned().map { it.packageName }.toSet(),
        )
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
