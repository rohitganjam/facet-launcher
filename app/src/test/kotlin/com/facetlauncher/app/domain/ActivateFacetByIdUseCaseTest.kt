package com.facetlauncher.app.domain

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import com.facetlauncher.app.data.FacetRepository
import com.facetlauncher.app.data.SettingsRepository
import com.facetlauncher.app.data.local.FacetDao
import com.facetlauncher.app.data.local.FacetEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder

/** In-memory fake — mirrors [com.facetlauncher.app.data.FacetRepositoryTest]'s. Named distinctly from
 *  [EnsureActiveFacetUseCaseTest]'s own private `FakeFacetDao` — top-level private classes are
 *  file-scoped for access but still occupy the package's class-file namespace, so two files in the
 *  same package can't both be named `FakeFacetDao`. */
private class ActivateFacetByIdFakeFacetDao : FacetDao {
    private var nextId = 1L
    private val state = MutableStateFlow<List<FacetEntity>>(emptyList())

    override fun observeAll(): Flow<List<FacetEntity>> = state

    override suspend fun insert(facet: FacetEntity): Long {
        val id = if (facet.id != 0L) facet.id else nextId++
        state.value = state.value.filterNot { it.id == id } + facet.copy(id = id)
        return id
    }

    override suspend fun update(facet: FacetEntity) {
        state.value = state.value.map { if (it.id == facet.id) facet else it }
    }

    override suspend fun delete(facet: FacetEntity) {
        state.value = state.value.filterNot { it.id == facet.id }
    }

    override suspend fun getById(id: Long): FacetEntity? = state.value.find { it.id == id }

    override suspend fun deleteAll() {
        state.value = emptyList()
    }
}

class ActivateFacetByIdUseCaseTest {

    @get:Rule
    val tempFolder = TemporaryFolder()

    private fun createSettingsRepository(): SettingsRepository {
        val dataStore: DataStore<Preferences> = PreferenceDataStoreFactory.create(
            produceFile = { tempFolder.newFile("test-${System.nanoTime()}.preferences_pb") },
        )
        return SettingsRepository(dataStore)
    }

    @Test
    fun `invoke activates a facet that exists`() = runTest {
        // Given a second facet that isn't currently active
        val facetRepository = FacetRepository(ActivateFacetByIdFakeFacetDao())
        val settingsRepository = createSettingsRepository()
        val first = facetRepository.addFacet()
        val second = facetRepository.addFacet()
        settingsRepository.setActiveFacetId(first.id)

        // When activating it by id (e.g. via a deep link or shortcut)
        ActivateFacetByIdUseCase(facetRepository, settingsRepository)(second.id)

        // Then it becomes the active facet
        assertEquals(second.id, settingsRepository.settings.first().activeFacetId)
    }

    @Test
    fun `invoke is a no-op for an id that doesn't resolve to a real facet`() = runTest {
        // Given a facet that's currently active
        val facetRepository = FacetRepository(ActivateFacetByIdFakeFacetDao())
        val settingsRepository = createSettingsRepository()
        val facet = facetRepository.addFacet()
        settingsRepository.setActiveFacetId(facet.id)

        // When activating an id that doesn't exist (stale shortcut, hand-typed bad id, deleted facet)
        ActivateFacetByIdUseCase(facetRepository, settingsRepository)(facet.id + 999)

        // Then the currently-active facet is left untouched
        assertEquals(facet.id, settingsRepository.settings.first().activeFacetId)
        assertNotEquals(facet.id + 999, settingsRepository.settings.first().activeFacetId)
    }
}
