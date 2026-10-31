package com.facetlauncher.app.domain

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.facetlauncher.app.data.ClockStackRepository
import com.facetlauncher.app.data.FakeEntitlementRepository
import com.facetlauncher.app.data.local.FacetDatabase
import com.facetlauncher.app.data.local.FacetEntity
import com.facetlauncher.app.data.model.ClockStackItemKind
import com.facetlauncher.app.data.model.MAX_CLOCK_STACK_ITEMS
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class AddClockStackItemUseCaseTest {

    private lateinit var database: FacetDatabase
    private lateinit var repository: ClockStackRepository
    private val entitlement = FakeEntitlementRepository(initial = false)
    private var facetId = 0L

    private fun useCase() = AddClockStackItemUseCase(repository, entitlement)

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

    private suspend fun kinds() = repository.getAll(facetId).map { it.kind }

    @Test
    fun `a free facet on its one page has no room for another`() = runTest {
        // Given a free facet that starts as the native clock
        repository.addClock(facetId)

        // Then a widget and the clock are both turned away, and nothing is written
        assertFalse(useCase().hasRoom(facetId))
        assertEquals(AddClockStackItemResult.LIMIT_REACHED, useCase().addWidget(facetId, 10))
        assertEquals(listOf(ClockStackItemKind.CLOCK), kinds())
    }

    @Test
    fun `a free facet with an empty stack can add its one page`() = runTest {
        // Given a free facet whose stack was emptied
        assertTrue(useCase().hasRoom(facetId))

        // When a widget is added
        val result = useCase().addWidget(facetId, 10)

        // Then it is the one page, and a second is refused
        assertEquals(AddClockStackItemResult.ADDED, result)
        assertEquals(AddClockStackItemResult.LIMIT_REACHED, useCase().addWidget(facetId, 11))
    }

    @Test
    fun `a pro facet can fill the stack to the maximum and no further`() = runTest {
        // Given Pro
        entitlement.proFlow.value = true

        // When adding up to the maximum and then once more
        val results = (1..MAX_CLOCK_STACK_ITEMS + 1).map { useCase().addWidget(facetId, it) }

        // Then the first five are added and the sixth is turned away
        assertEquals(List(MAX_CLOCK_STACK_ITEMS) { AddClockStackItemResult.ADDED } + AddClockStackItemResult.LIMIT_REACHED, results)
        assertEquals(MAX_CLOCK_STACK_ITEMS, repository.getAll(facetId).size)
    }

    @Test
    fun `the clock can be added once`() = runTest {
        // Given Pro and a stack that already has the clock
        entitlement.proFlow.value = true
        assertEquals(AddClockStackItemResult.ADDED, useCase().addClock(facetId))

        // When the clock is added again
        val again = useCase().addClock(facetId)

        // Then it is refused as already there
        assertEquals(AddClockStackItemResult.CLOCK_ALREADY_IN_STACK, again)
        assertEquals(listOf(ClockStackItemKind.CLOCK), kinds())
    }

    @Test
    fun `the limit follows the plan as it changes`() = runTest {
        // Given a free facet on its one page
        repository.addClock(facetId)
        assertFalse(useCase().hasRoom(facetId))

        // When Pro is bought, and then lapses with two pages saved
        entitlement.proFlow.value = true
        assertTrue(useCase().hasRoom(facetId))
        useCase().addWidget(facetId, 10)
        entitlement.proFlow.value = false

        // Then nothing is removed, but there is no room to add more
        assertEquals(2, repository.getAll(facetId).size)
        assertFalse(useCase().hasRoom(facetId))
    }
}
