package com.facetlauncher.app.data.local

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class LaunchHistoryDaoTest {

    private val dao: LaunchHistoryDao = Room.inMemoryDatabaseBuilder(ApplicationProvider.getApplicationContext(), FacetDatabase::class.java)
        .allowMainThreadQueries()
        .build()
        .launchHistoryDao()

    private suspend fun launch(pkg: String, at: Long, day: Long, userId: Int = 0, keepFrom: Long = 0) =
        dao.recordLaunch(pkg, ".Main", userId, at, day, keepFrom)

    @Test
    fun `launches keep the latest time per app and count each day`() = runTest {
        // Given chat opened twice on day 10 and once on day 11, and maps once
        launch("chat", at = 1, day = 10)
        launch("chat", at = 2, day = 10)
        launch("maps", at = 3, day = 10)
        launch("chat", at = 4, day = 11)

        // Then the last launches are newest first, one row per app
        assertEquals(listOf("chat" to 4L, "maps" to 3L), dao.lastLaunches().map { it.packageName to it.lastLaunchedAt })
        // And counts sum the days in the window
        assertEquals(mapOf("chat" to 3, "maps" to 1), dao.launchCountsSince(10).associate { it.packageName to it.total })
        assertEquals(mapOf("chat" to 1), dao.launchCountsSince(11).associate { it.packageName to it.total })
        assertEquals(mapOf("chat" to 1), dao.observeLaunchCountsSince(11).first().associate { it.packageName to it.total })
    }

    @Test
    fun `days before the kept window are pruned on the next launch`() = runTest {
        // Given launches on day 1 and day 40
        launch("chat", at = 1, day = 1)
        launch("chat", at = 2, day = 40, keepFrom = 11)

        // Then only day 40 is still counted, even counting from the start
        assertEquals(1, dao.launchCountsSince(0).single().total)
    }

    @Test
    fun `picks count per query and app, and the oldest beyond the cap are dropped`() = runTest {
        // Given chat picked twice for "c", then maps and notes once for other queries, keeping 2
        dao.recordPick("c", "chat", ".Main", 0, at = 1, keep = 2)
        dao.recordPick("c", "chat", ".Main", 0, at = 2, keep = 2)
        dao.recordPick("m", "maps", ".Main", 0, at = 3, keep = 2)
        dao.recordPick("n", "notes", ".Main", 0, at = 4, keep = 2)

        // Then the least recently picked row (chat for "c") is gone
        val picks = dao.observeSearchPicks().first().associate { (it.query to it.packageName) to it.count }
        assertEquals(mapOf(("m" to "maps") to 1, ("n" to "notes") to 1), picks)
    }

    @Test
    fun `a repeated pick increments its count`() = runTest {
        dao.recordPick("c", "chat", ".Main", 0, at = 1, keep = 10)
        dao.recordPick("c", "chat", ".Main", 0, at = 5, keep = 10)

        val pick = dao.observeSearchPicks().first().single()
        assertEquals(2, pick.count)
        assertEquals(5L, pick.lastPickedAt)
    }

    @Test
    fun `removing a package clears only that package for that user`() = runTest {
        // Given chat in two profiles and maps in the personal one
        launch("chat", at = 1, day = 10, userId = 0)
        launch("chat", at = 2, day = 10, userId = 10)
        launch("maps", at = 3, day = 10, userId = 0)
        dao.recordPick("c", "chat", ".Main", 0, at = 1, keep = 10)
        dao.recordPick("c", "chat", ".Main", 10, at = 1, keep = 10)

        // When chat is uninstalled from the personal profile
        dao.removeByPackage("chat", 0)

        // Then the work profile's chat and personal maps remain
        assertEquals(setOf("chat" to 10, "maps" to 0), dao.lastLaunches().map { it.packageName to it.userId }.toSet())
        assertEquals(setOf("chat" to 10, "maps" to 0), dao.launchCountsSince(0).map { it.packageName to it.userId }.toSet())
        assertEquals(listOf(10), dao.observeSearchPicks().first().map { it.userId })
    }

    @Test
    fun `removing a user clears every row for that profile`() = runTest {
        launch("chat", at = 1, day = 10, userId = 10)
        launch("maps", at = 2, day = 10, userId = 0)
        dao.recordPick("c", "chat", ".Main", 10, at = 1, keep = 10)

        dao.removeByUserId(10)

        assertEquals(listOf("maps"), dao.lastLaunches().map { it.packageName })
        assertEquals(listOf("maps"), dao.launchCountsSince(0).map { it.packageName })
        assertEquals(emptyList<SearchPickEntity>(), dao.observeSearchPicks().first())
    }
}
