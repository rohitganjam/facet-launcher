package com.facetlauncher.app.ui.facets

import com.facetlauncher.app.data.FacetRepository
import com.facetlauncher.app.data.SettingsRepository
import com.facetlauncher.app.data.local.FacetDao
import com.facetlauncher.app.data.local.FacetEntity
import com.facetlauncher.app.data.model.LauncherSettings
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify
import org.mockito.Mockito.`when`

/** In-memory fake — enough of [FacetDao] for [FacetRepository]'s needs. */
private class FakeFacetDao : FacetDao {
    private var nextId = 1L
    private val state = MutableStateFlow<List<FacetEntity>>(emptyList())

    override fun observeAll(): Flow<List<FacetEntity>> = state
    override suspend fun insert(facet: FacetEntity): Long {
        val id = if (facet.id != 0L) facet.id else nextId++
        state.value = (state.value.filterNot { it.id == id } + facet.copy(id = id)).sortedBy { it.position }
        return id
    }
    override suspend fun update(facet: FacetEntity) {
        state.value = state.value.map { if (it.id == facet.id) facet else it }.sortedBy { it.position }
    }
    override suspend fun delete(facet: FacetEntity) {
        state.value = state.value.filterNot { it.id == facet.id }
    }
    override suspend fun getById(id: Long): FacetEntity? = state.value.find { it.id == id }
    override suspend fun deleteAll() { state.value = emptyList() }
}

@OptIn(ExperimentalCoroutinesApi::class)
class ManageFacetsViewModelTest {

    private val dispatcher = StandardTestDispatcher()

    @Before fun setUp() = Dispatchers.setMain(dispatcher)

    @After fun tearDown() = Dispatchers.resetMain()

    private fun fakeSettings(activeFacetId: Long = 0L): SettingsRepository = mock(SettingsRepository::class.java).also {
        `when`(it.settings).thenReturn(MutableStateFlow(LauncherSettings(activeFacetId = activeFacetId)))
    }

    @Test
    fun `uiState reflects the observed facets and the add-max rule`() = runTest(dispatcher) {
        val facetRepository = FacetRepository(FakeFacetDao())
        facetRepository.addFacet()
        facetRepository.addFacet()
        val viewModel = ManageFacetsViewModel(facetRepository, fakeSettings())

        backgroundScope.launch { viewModel.uiState.collect {} }
        dispatcher.scheduler.advanceUntilIdle()

        assertEquals(listOf("Facet 1", "Facet 2"), viewModel.uiState.value.facets.map { it.name })
        assertTrue(viewModel.uiState.value.canAddFacet)
        assertTrue(viewModel.uiState.value.canDeleteFacet)
    }

    @Test
    fun `reorderFacets persists the new order`() = runTest(dispatcher) {
        val facetRepository = FacetRepository(FakeFacetDao())
        facetRepository.addFacet()
        facetRepository.addFacet()
        val viewModel = ManageFacetsViewModel(facetRepository, fakeSettings())
        backgroundScope.launch { viewModel.uiState.collect {} }
        dispatcher.scheduler.advanceUntilIdle()

        val reversed = viewModel.uiState.value.facets.reversed()
        viewModel.reorderFacets(reversed)
        dispatcher.scheduler.advanceUntilIdle()

        assertEquals(reversed.map { it.id }, facetRepository.observeFacets().first().map { it.id })
    }

    @Test
    fun `addFacet is a no-op once the maximum is reached`() = runTest(dispatcher) {
        val facetRepository = FacetRepository(FakeFacetDao())
        repeat(FacetRepository.MAX_FACETS) { facetRepository.addFacet() }
        val viewModel = ManageFacetsViewModel(facetRepository, fakeSettings())
        backgroundScope.launch { viewModel.uiState.collect {} }
        dispatcher.scheduler.advanceUntilIdle()
        assertFalse(viewModel.uiState.value.canAddFacet)

        viewModel.addFacet()
        dispatcher.scheduler.advanceUntilIdle()

        assertEquals(FacetRepository.MAX_FACETS, facetRepository.observeFacets().first().size)
    }

    @Test
    fun `deleting the active facet re-points active to a survivor`() = runTest(dispatcher) {
        val facetRepository = FacetRepository(FakeFacetDao())
        val first = facetRepository.addFacet()
        val second = facetRepository.addFacet()
        val settings = fakeSettings(activeFacetId = first.id)
        val viewModel = ManageFacetsViewModel(facetRepository, settings)
        backgroundScope.launch { viewModel.uiState.collect {} }
        dispatcher.scheduler.advanceUntilIdle()

        viewModel.deleteFacet(first)
        dispatcher.scheduler.advanceUntilIdle()

        assertEquals(listOf(second.id), facetRepository.observeFacets().first().map { it.id })
        verify(settings).setActiveFacetId(second.id)
    }
}
