package com.facetlauncher.app.domain

import com.facetlauncher.app.data.FacetRepository
import com.facetlauncher.app.data.FakeEntitlementRepository
import com.facetlauncher.app.data.local.FacetEntity
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.mockito.Mockito.mock
import org.mockito.Mockito.never
import org.mockito.Mockito.verify
import org.mockito.Mockito.`when`

class AddFacetUseCaseTest {

    private fun repository(count: Int): FacetRepository {
        val facets = MutableStateFlow(List(count) { FacetEntity(id = it + 1L, name = "F$it", position = it) })
        return mock(FacetRepository::class.java).also {
            `when`(it.observeFacets()).thenReturn(facets)
            runBlocking { `when`(it.addFacet()).thenReturn(FacetEntity(id = 99L, name = "New", position = count)) }
        }
    }

    @Test
    fun `a free user adds a facet below three`() = runTest {
        val result = AddFacetUseCase(repository(2), FakeEntitlementRepository(false))()

        assertTrue(result is AddFacetResult.Added)
    }

    @Test
    fun `a free user at three is refused and nothing is added`() = runTest {
        val repository = repository(3)

        val result = AddFacetUseCase(repository, FakeEntitlementRepository(false))()

        assertEquals(AddFacetResult.LimitReached, result)
        verify(repository, never()).addFacet()
    }

    @Test
    fun `a pro user can go past three`() = runTest {
        assertTrue(AddFacetUseCase(repository(3), FakeEntitlementRepository(true))() is AddFacetResult.Added)
    }

    @Test
    fun `a pro user at ten is refused`() = runTest {
        assertEquals(AddFacetResult.LimitReached, AddFacetUseCase(repository(10), FakeEntitlementRepository(true))())
    }

    @Test
    fun `a free user who still has more than three after a refund cannot add`() = runTest {
        assertEquals(AddFacetResult.LimitReached, AddFacetUseCase(repository(7), FakeEntitlementRepository(false))())
    }
}
