package com.facetlauncher.app.domain

import com.facetlauncher.app.data.FacetRepository
import com.facetlauncher.app.data.FacetShortcutRepository
import com.facetlauncher.app.data.local.FacetEntity
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.runTest
import org.junit.Test
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify
import org.mockito.Mockito.`when`

class SyncFacetShortcutsUseCaseTest {

    private val facets = MutableStateFlow<List<FacetEntity>>(emptyList())
    private val facetRepository: FacetRepository = mock(FacetRepository::class.java).also {
        `when`(it.observeFacets()).thenReturn(facets)
    }
    private val facetShortcutRepository: FacetShortcutRepository = mock(FacetShortcutRepository::class.java)
    private val useCase = SyncFacetShortcutsUseCase(facetRepository, facetShortcutRepository)

    @Test
    fun `re-publishes the full shortcut set on every FacetRepository emission`() = runTest {
        // Given the use case is running and observing an initially-empty facet list
        val job = launch { useCase() }
        testScheduler.advanceUntilIdle()
        verify(facetShortcutRepository).syncShortcuts(emptyList())

        // When a facet is added
        val work = FacetEntity(id = 1, name = "Work", position = 0)
        facets.value = listOf(work)
        testScheduler.advanceUntilIdle()

        // Then the new full list is re-published
        verify(facetShortcutRepository).syncShortcuts(listOf(work))

        // When that facet is renamed (same id/position, new name — not a delete+recreate)
        val renamed = work.copy(name = "Deep Work")
        facets.value = listOf(renamed)
        testScheduler.advanceUntilIdle()

        // Then the resync carries the new name
        verify(facetShortcutRepository).syncShortcuts(listOf(renamed))

        // When it's deleted
        facets.value = emptyList()
        testScheduler.advanceUntilIdle()

        // Then the shortcut set is emptied too
        verify(facetShortcutRepository, org.mockito.Mockito.times(2)).syncShortcuts(emptyList())
        job.cancel()
    }
}
