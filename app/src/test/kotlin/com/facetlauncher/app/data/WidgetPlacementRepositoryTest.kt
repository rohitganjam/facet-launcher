package com.facetlauncher.app.data

import com.facetlauncher.app.data.model.NativeWidgetType
import com.facetlauncher.app.data.local.WidgetPlacementDao
import com.facetlauncher.app.data.local.WidgetPlacementEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

/** In-memory fake — simpler than mocking every [WidgetPlacementDao] method for this repository's needs. */
private class FakeWidgetPlacementDao : WidgetPlacementDao {
    private val state = MutableStateFlow<List<WidgetPlacementEntity>>(emptyList())

    override fun observeAll(): Flow<List<WidgetPlacementEntity>> = state

    override suspend fun getById(appWidgetId: Int): WidgetPlacementEntity? = state.value.find { it.appWidgetId == appWidgetId }

    override suspend fun deleteAllNative() {
        state.value = state.value.filterNot { it.nativeWidgetType != null }
    }

    override suspend fun minAppWidgetId(): Int? = state.value.minOfOrNull { it.appWidgetId }

    override suspend fun upsert(placement: WidgetPlacementEntity) {
        state.value = state.value.filterNot { it.appWidgetId == placement.appWidgetId } + placement
    }

    override suspend fun deleteById(appWidgetId: Int) {
        state.value = state.value.filterNot { it.appWidgetId == appWidgetId }
    }

    override suspend fun delete(placement: WidgetPlacementEntity) {
        state.value = state.value.filterNot { it.appWidgetId == placement.appWidgetId }
    }
}

class WidgetPlacementRepositoryTest {

    private fun placement(appWidgetId: Int, row: Int = 0, col: Int = 0) = WidgetPlacementEntity(
        appWidgetId = appWidgetId,
        providerPackageName = "com.example.widgets",
        providerClassName = ".MyWidgetProvider",
        row = row,
        col = col,
        colSpan = 2,
        rowSpan = 1,
    )

    @Test
    fun `upsert then observeAll returns the stored placement`() = runTest {
        // Given an empty repository
        val repository = WidgetPlacementRepository(FakeWidgetPlacementDao())
        assertTrue(repository.observeAll().first().isEmpty())

        // When a placement is upserted
        repository.upsert(placement(appWidgetId = 1))

        // Then it's observable
        val stored = repository.observeAll().first().single()
        assertEquals(1, stored.appWidgetId)
    }

    @Test
    fun `upsert with an existing id replaces rather than duplicates`() = runTest {
        // Given a stored placement
        val repository = WidgetPlacementRepository(FakeWidgetPlacementDao())
        repository.upsert(placement(appWidgetId = 1, row = 0, col = 0))

        // When upserted again with a moved position, same id
        repository.upsert(placement(appWidgetId = 1, row = 2, col = 3))

        // Then there's still exactly one row, at the new position
        val stored = repository.observeAll().first().single()
        assertEquals(2, stored.row)
        assertEquals(3, stored.col)
    }

    @Test
    fun `getById returns null for an id that doesn't exist`() = runTest {
        // Given an empty repository
        val repository = WidgetPlacementRepository(FakeWidgetPlacementDao())

        // Then a lookup for a nonexistent id returns null
        assertNull(repository.getById(999))
    }

    @Test
    fun `deleteById removes only the matching placement`() = runTest {
        // Given two stored placements
        val repository = WidgetPlacementRepository(FakeWidgetPlacementDao())
        repository.upsert(placement(appWidgetId = 1))
        repository.upsert(placement(appWidgetId = 2))

        // When one is deleted
        repository.deleteById(1)

        // Then only the other remains
        val remaining = repository.observeAll().first()
        assertEquals(1, remaining.size)
        assertEquals(2, remaining.single().appWidgetId)
    }

    @Test
    fun `nextNativeId is negative and below every stored id`() = runTest {
        // Given hosted widgets and one native widget already stored
        val repository = WidgetPlacementRepository(FakeWidgetPlacementDao())
        assertEquals(-1, repository.nextNativeId())
        repository.upsert(placement(appWidgetId = 40))
        assertEquals(-1, repository.nextNativeId())
        repository.upsert(placement(appWidgetId = -1))
        repository.upsert(placement(appWidgetId = -2))

        // When asking for the next id, then it is one below the lowest
        assertEquals(-3, repository.nextNativeId())
    }

    @Test
    fun `deleteAllNative removes the widgets Facet draws itself and keeps hosted ones`() = runTest {
        // Given a hosted widget and a native one
        val repository = WidgetPlacementRepository(FakeWidgetPlacementDao())
        repository.upsert(placement(appWidgetId = 4))
        repository.upsert(placement(appWidgetId = -1).copy(nativeWidgetType = NativeWidgetType.WEATHER))

        // When the native ones are cleared
        repository.deleteAllNative()

        // Then only the hosted one is left
        assertEquals(listOf(4), repository.observeAll().first().map { it.appWidgetId })
    }
}
