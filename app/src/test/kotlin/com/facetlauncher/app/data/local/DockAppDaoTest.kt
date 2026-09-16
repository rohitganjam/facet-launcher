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
class DockAppDaoTest {

    private fun createDao(): DockAppDao {
        val database = Room.inMemoryDatabaseBuilder(ApplicationProvider.getApplicationContext(), FacetDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        return database.dockAppDao()
    }

    @Test
    fun `insert then observe returns the inserted row`() = runTest {
        // Given an empty dock table
        val dao = createDao()

        // When a dock app is inserted
        dao.upsert(DockAppEntity(packageName = "com.example.a", activityName = ".Main", position = 0))

        // Then observing returns exactly that row
        val result = dao.observeAll().first()
        assertEquals(1, result.size)
        assertEquals("com.example.a", result.first().packageName)
    }

    @Test
    fun `observed rows are ordered by position`() = runTest {
        // Given rows inserted out of position order
        val dao = createDao()
        dao.upsert(DockAppEntity(packageName = "com.example.b", activityName = ".Main", position = 1))
        dao.upsert(DockAppEntity(packageName = "com.example.a", activityName = ".Main", position = 0))

        // Then they come back ordered by position, not insertion order
        val result = dao.observeAll().first()
        assertEquals(listOf("com.example.a", "com.example.b"), result.map { it.packageName })
    }

    @Test
    fun `deleteByComponent removes only the matching row`() = runTest {
        // Given two dock apps
        val dao = createDao()
        dao.upsert(DockAppEntity(packageName = "com.example.a", activityName = ".Main", position = 0))
        dao.upsert(DockAppEntity(packageName = "com.example.b", activityName = ".Main", position = 1))

        // When one is deleted by its component
        dao.deleteByComponent("com.example.a", ".Main", -1)

        // Then only the other remains
        val result = dao.observeAll().first()
        assertEquals(1, result.size)
        assertEquals("com.example.b", result.first().packageName)
    }

    @Test
    fun `upserting the same component twice updates it in place rather than duplicating`() = runTest {
        // Given a dock app already stored at position 0
        val dao = createDao()
        dao.upsert(DockAppEntity(packageName = "com.example.a", activityName = ".Main", position = 0))

        // When the same (packageName, activityName) is upserted again at a new position
        dao.upsert(DockAppEntity(packageName = "com.example.a", activityName = ".Main", position = 3))

        // Then there is still only one row, now at the new position
        val result = dao.observeAll().first()
        assertEquals(1, result.size)
        assertEquals(3, result.first().position)
    }

    @Test
    fun `delete removes the given entity`() = runTest {
        // Given a stored dock app
        val dao = createDao()
        val id = dao.upsert(DockAppEntity(packageName = "com.example.a", activityName = ".Main", position = 0))

        // When it is deleted by entity
        dao.delete(DockAppEntity(id = id, packageName = "com.example.a", activityName = ".Main", position = 0))

        // Then the table is empty
        assertTrue(dao.observeAll().first().isEmpty())
    }
}
