package com.facetlauncher.app.data.local

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class AutomationRuleDaoTest {

    private fun createDatabase(): FacetDatabase =
        Room.inMemoryDatabaseBuilder(ApplicationProvider.getApplicationContext(), FacetDatabase::class.java)
            .allowMainThreadQueries()
            .build()

    private suspend fun FacetDatabase.addFacet(name: String): Long = facetDao().insert(FacetEntity(name = name, position = 0))

    private fun rule(targetFacetId: Long, position: Int, endFacetId: Long? = null) = AutomationRuleEntity(
        position = position,
        targetFacetId = targetFacetId,
        endBehavior = if (endFacetId != null) "SWITCH_TO" else "RETURN_TO_BASELINE",
        endFacetId = endFacetId,
        triggerType = "BATTERY",
    )

    @Test
    fun `observeAll returns rules ordered by position then id`() = runTest {
        // Given three rules inserted out of position order
        val database = createDatabase()
        val facet = database.addFacet("Work")
        val dao = database.automationRuleDao()
        val last = dao.insert(rule(facet, position = 2))
        val first = dao.insert(rule(facet, position = 0))
        val tiedA = dao.insert(rule(facet, position = 1))
        val tiedB = dao.insert(rule(facet, position = 1))

        // When observing
        val ids = dao.observeAll().first().map { it.id }

        // Then position decides, and id breaks ties
        assertEquals(listOf(first, tiedA, tiedB, last), ids)
    }

    @Test
    fun `maxPosition is minus one on an empty table`() = runTest {
        val database = createDatabase()

        assertEquals(-1, database.automationRuleDao().maxPosition())
    }

    @Test
    fun `setEnabled flips only the enabled flag`() = runTest {
        // Given an enabled rule
        val database = createDatabase()
        val facet = database.addFacet("Work")
        val dao = database.automationRuleDao()
        val id = dao.insert(rule(facet, position = 0))

        // When disabling it
        dao.setEnabled(id, false)

        // Then only the flag changed
        val stored = dao.getById(id)!!
        assertEquals(false, stored.enabled)
        assertEquals(facet, stored.targetFacetId)
        assertEquals("BATTERY", stored.triggerType)
    }

    @Test
    fun `deleteById removes just that rule`() = runTest {
        val database = createDatabase()
        val facet = database.addFacet("Work")
        val dao = database.automationRuleDao()
        val keep = dao.insert(rule(facet, position = 0))
        val drop = dao.insert(rule(facet, position = 1))

        dao.deleteById(drop)

        assertEquals(listOf(keep), dao.observeAll().first().map { it.id })
    }

    @Test
    fun `deleting the target facet cascades and removes its rules`() = runTest {
        // Given a rule targeting a facet, and another rule targeting a different facet
        val database = createDatabase()
        val work = database.addFacet("Work")
        val home = database.addFacet("Home")
        val dao = database.automationRuleDao()
        dao.insert(rule(work, position = 0))
        val survivor = dao.insert(rule(home, position = 1))

        // When the target facet is deleted
        database.facetDao().delete(database.facetDao().getById(work)!!)

        // Then only the rule targeting it is gone
        assertEquals(listOf(survivor), dao.observeAll().first().map { it.id })
    }

    @Test
    fun `deleting the switch-to facet clears the end facet but keeps the rule`() = runTest {
        // Given a rule that ends by switching to another facet
        val database = createDatabase()
        val work = database.addFacet("Work")
        val home = database.addFacet("Home")
        val dao = database.automationRuleDao()
        val id = dao.insert(rule(work, position = 0, endFacetId = home))

        // When that end facet is deleted
        database.facetDao().delete(database.facetDao().getById(home)!!)

        // Then the rule survives with its end facet cleared
        val stored = dao.getById(id)
        assertTrue(stored != null)
        assertNull(stored!!.endFacetId)
    }
}
