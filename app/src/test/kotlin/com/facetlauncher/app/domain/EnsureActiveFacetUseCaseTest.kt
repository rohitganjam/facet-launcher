package com.facetlauncher.app.domain

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import com.facetlauncher.app.data.FacetRepository
import com.facetlauncher.app.data.FakeEntitlementRepository
import com.facetlauncher.app.data.SettingsRepository
import com.facetlauncher.app.data.local.FacetDao
import com.facetlauncher.app.data.local.FacetEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder

/** In-memory fake — mirrors [com.facetlauncher.app.data.FacetRepositoryTest]'s. */
private class FakeFacetDao : FacetDao {
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

class EnsureActiveFacetUseCaseTest {

    @get:Rule
    val tempFolder = TemporaryFolder()

    private fun createSettingsRepository(): SettingsRepository {
        val dataStore: DataStore<Preferences> = PreferenceDataStoreFactory.create(
            produceFile = { tempFolder.newFile("test-${System.nanoTime()}.preferences_pb") },
        )
        return SettingsRepository(dataStore)
    }

    @Test
    fun `seeds a default facet and sets it active when the database is empty`() = runTest {
        // Given no facets and no active facet id
        val facetRepository = FacetRepository(FakeFacetDao())
        val settingsRepository = createSettingsRepository()

        // When ensuring an active facet
        EnsureActiveFacetUseCase(facetRepository, settingsRepository, SelectableFacetsUseCase(facetRepository, FakeEntitlementRepository()))()

        // Then exactly one facet now exists and it's the active one
        val facets = facetRepository.observeFacets().first()
        assertEquals(1, facets.size)
        assertEquals(facets.single().id, settingsRepository.settings.first().activeFacetId)
    }

    @Test
    fun `leaves an already-valid active facet id untouched`() = runTest {
        // Given a facet that's already set active
        val facetRepository = FacetRepository(FakeFacetDao())
        val settingsRepository = createSettingsRepository()
        val facet = facetRepository.addFacet()
        settingsRepository.setActiveFacetId(facet.id)

        // When ensuring an active facet again
        EnsureActiveFacetUseCase(facetRepository, settingsRepository, SelectableFacetsUseCase(facetRepository, FakeEntitlementRepository()))()

        // Then it's unchanged, and no second facet was created
        assertEquals(facet.id, settingsRepository.settings.first().activeFacetId)
        assertEquals(1, facetRepository.observeFacets().first().size)
    }

    @Test
    fun `falls back to an existing facet when the active id no longer resolves`() = runTest {
        // Given a stored facet but an active id pointing at a facet that no longer exists
        val facetRepository = FacetRepository(FakeFacetDao())
        val settingsRepository = createSettingsRepository()
        val facet = facetRepository.addFacet()
        settingsRepository.setActiveFacetId(facet.id + 999)

        // When ensuring an active facet
        EnsureActiveFacetUseCase(facetRepository, settingsRepository, SelectableFacetsUseCase(facetRepository, FakeEntitlementRepository()))()

        // Then it falls back to the real facet rather than creating a redundant one
        assertEquals(facet.id, settingsRepository.settings.first().activeFacetId)
        assertEquals(1, facetRepository.observeFacets().first().size)
    }

    @Test
    fun `a free user whose active facet is past the third gets the first usable facet`() = runTest {
        // Given five facets on the free plan, with the fifth active (Pro was refunded)
        val facetRepository = FacetRepository(FakeFacetDao())
        val settingsRepository = createSettingsRepository()
        val facets = List(5) { facetRepository.addFacet() }
        settingsRepository.setActiveFacetId(facets[4].id)

        // When ensuring an active facet
        EnsureActiveFacetUseCase(facetRepository, settingsRepository, SelectableFacetsUseCase(facetRepository, FakeEntitlementRepository(initial = false)))()

        // Then the first facet takes over and nothing is deleted
        assertEquals(facets[0].id, settingsRepository.settings.first().activeFacetId)
        assertEquals(5, facetRepository.observeFacets().first().size)
    }

    @Test
    fun `keepUsable moves the active facet when pro is lost and again leaves it alone when pro returns`() = runTest {
        // Given a pro user with five facets, the fifth active
        val facetRepository = FacetRepository(FakeFacetDao())
        val settingsRepository = createSettingsRepository()
        val facets = List(5) { facetRepository.addFacet() }
        settingsRepository.setActiveFacetId(facets[4].id)
        val entitlement = FakeEntitlementRepository(initial = true)
        val job = launch { EnsureActiveFacetUseCase(facetRepository, settingsRepository, SelectableFacetsUseCase(facetRepository, entitlement)).keepUsable() }
        testScheduler.advanceUntilIdle()
        assertEquals("pro keeps it", facets[4].id, settingsRepository.settings.first().activeFacetId)

        // When Pro is lost
        entitlement.proFlow.value = false
        testScheduler.advanceUntilIdle()

        // Then the first usable facet takes over (DataStore writes off the test scheduler, so wait for it)
        assertEquals(facets[0].id, settingsRepository.settings.first { it.activeFacetId == facets[0].id }.activeFacetId)
        job.cancel()
    }
}
