package com.facetlauncher.app.data

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.facetlauncher.app.data.local.FacetDatabase
import com.facetlauncher.app.data.local.FacetEntity
import com.facetlauncher.app.data.model.ClockAlignment
import com.facetlauncher.app.data.model.ClockStackItemKind
import com.facetlauncher.app.data.model.MAX_CLOCK_STACK_ITEMS
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class ClockStackRepositoryTest {

    private lateinit var database: FacetDatabase
    private lateinit var repository: ClockStackRepository
    private var facetId = 0L

    @Before
    fun setUp() = runTest {
        database = Room.inMemoryDatabaseBuilder(ApplicationProvider.getApplicationContext(), FacetDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        repository = ClockStackRepository(database, database.clockStackItemDao())
        facetId = database.facetDao().insert(FacetEntity(name = "Home", position = 0))
    }

    @After
    fun tearDown() = database.close()

    private suspend fun widgetIds() = repository.getAll(facetId).map { it.appWidgetId }

    @Test
    fun `items come back in the order they were added`() = runTest {
        // Given three widgets added one after another
        repository.addWidget(facetId, 10)
        repository.addWidget(facetId, 11)
        repository.addWidget(facetId, 12)

        // Then they are observed in that order
        assertEquals(listOf(10, 11, 12), repository.observe(facetId).first().map { it.appWidgetId })
    }

    @Test
    fun `the stack holds at most five items and a sixth is refused`() = runTest {
        // Given a full stack
        repeat(MAX_CLOCK_STACK_ITEMS) { assertTrue(repository.addWidget(facetId, 100 + it)) }

        // When another widget and the clock are added
        val widgetAdded = repository.addWidget(facetId, 999)
        val clockAdded = repository.addClock(facetId)

        // Then both are refused and nothing is written
        assertFalse(widgetAdded)
        assertFalse(clockAdded)
        assertEquals(MAX_CLOCK_STACK_ITEMS, repository.getAll(facetId).size)
    }

    @Test
    fun `the native clock can be added once`() = runTest {
        // Given a stack that already has the clock
        assertTrue(repository.addClock(facetId))

        // When the clock is added again
        val again = repository.addClock(facetId)

        // Then it is refused and the stack still has one clock
        assertFalse(again)
        assertEquals(listOf(ClockStackItemKind.CLOCK), repository.getAll(facetId).map { it.kind })
    }

    @Test
    fun `removing an item closes the gap and returns its widget id`() = runTest {
        // Given widgets 10, 11, 12
        listOf(10, 11, 12).forEach { repository.addWidget(facetId, it) }
        val middle = repository.getAll(facetId)[1]

        // When the middle one is removed
        val released = repository.remove(middle.id)

        // Then its id is handed back for release and positions are 0 and 1 again
        assertEquals(11, released)
        assertEquals(listOf(10, 12), widgetIds())
        assertEquals(listOf(0, 1), repository.getAll(facetId).map { it.position })
    }

    @Test
    fun `removing the clock or an unknown item releases nothing`() = runTest {
        // Given a clock and a widget
        repository.addClock(facetId)
        repository.addWidget(facetId, 10)
        val clock = repository.getAll(facetId).first()

        // When the clock and a missing id are removed
        val releasedClock = repository.remove(clock.id)
        val releasedMissing = repository.remove(9999)

        // Then neither has a widget id to release
        assertNull(releasedClock)
        assertNull(releasedMissing)
        assertEquals(listOf(10), widgetIds())
    }

    @Test
    fun `replaceWithWidget leaves just that widget, unsized, and returns every earlier id`() = runTest {
        // Given a stack with a clock and two widgets, the first of them resized
        repository.addClock(facetId)
        repository.addWidget(facetId, 10)
        repository.addWidget(facetId, 11)
        repository.setSize(repository.getAll(facetId)[1].id, 400, 300)

        // When one widget replaces the stack
        val released = repository.replaceWithWidget(facetId, 99)

        // Then the earlier widget ids come back, and only the new widget remains with no inherited size
        assertEquals(listOf(10, 11), released)
        val only = repository.getAll(facetId).single()
        assertEquals(99, only.appWidgetId)
        assertEquals(0, only.position)
        assertNull(only.widthDp)
        assertNull(only.heightDp)
    }

    @Test
    fun `replaceWithWidget on a facet with no stack adds the widget and releases nothing`() = runTest {
        // When a widget replaces an empty stack
        val released = repository.replaceWithWidget(facetId, 5)

        // Then nothing is released and the widget is the whole stack
        assertTrue(released.isEmpty())
        assertEquals(listOf(5), widgetIds())
    }

    @Test
    fun `reorder applies the given order`() = runTest {
        // Given widgets 10, 11, 12
        listOf(10, 11, 12).forEach { repository.addWidget(facetId, it) }
        val ids = repository.getAll(facetId).map { it.id }

        // When they are reordered to 12, 10, 11
        repository.reorder(facetId, listOf(ids[2], ids[0], ids[1]))

        // Then the stack follows that order
        assertEquals(listOf(12, 10, 11), widgetIds())
    }

    @Test
    fun `reorder keeps items left out at the end and ignores unknown ids`() = runTest {
        // Given widgets 10, 11, 12 and an order naming only the last, plus an unknown id
        listOf(10, 11, 12).forEach { repository.addWidget(facetId, it) }
        val ids = repository.getAll(facetId).map { it.id }

        // When reordering
        repository.reorder(facetId, listOf(ids[2], 9999))

        // Then the named item leads and the rest keep their relative order
        assertEquals(listOf(12, 10, 11), widgetIds())
    }

    @Test
    fun `clear removes every item of that facet only and returns the widget ids`() = runTest {
        // Given two facets, each with widgets
        val other = database.facetDao().insert(FacetEntity(name = "Work", position = 1))
        repository.addClock(facetId)
        repository.addWidget(facetId, 10)
        repository.addWidget(other, 20)

        // When the first facet's stack is cleared
        val released = repository.clear(facetId)

        // Then its widget ids come back and the other facet is untouched
        assertEquals(listOf(10), released)
        assertTrue(repository.getAll(facetId).isEmpty())
        assertEquals(listOf(20), repository.getAll(other).map { it.appWidgetId })
    }

    @Test
    fun `size and alignment are stored per item and portrait and landscape are separate`() = runTest {
        // Given two widgets
        repository.addWidget(facetId, 10)
        repository.addWidget(facetId, 11)
        val (first, second) = repository.getAll(facetId)

        // When the first gets a portrait and a landscape size and alignment
        repository.setSize(first.id, 300, 150)
        repository.setLandscapeSize(first.id, 400, 120)
        repository.setAlignment(first.id, ClockAlignment.CENTER)
        repository.setLandscapeAlignment(first.id, ClockAlignment.RIGHT)

        // Then only the first changed, and each orientation holds its own values
        val rows = repository.getAll(facetId)
        assertEquals(listOf(300, 150, 400, 120), listOf(rows[0].widthDp, rows[0].heightDp, rows[0].landscapeWidthDp, rows[0].landscapeHeightDp))
        assertEquals(ClockAlignment.CENTER, rows[0].alignment)
        assertEquals(ClockAlignment.RIGHT, rows[0].landscapeAlignment)
        assertNull(rows[1].widthDp)
        assertNull(rows[1].alignment)
        assertEquals(second.id, rows[1].id)
    }

    @Test
    fun `deleting a facet deletes its stack`() = runTest {
        // Given a facet with a stack
        repository.addWidget(facetId, 10)

        // When the facet is deleted
        database.facetDao().delete(database.facetDao().getById(facetId)!!)

        // Then its items go with it
        assertTrue(repository.getAll(facetId).isEmpty())
    }
}
