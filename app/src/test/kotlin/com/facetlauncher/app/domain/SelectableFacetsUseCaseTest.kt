package com.facetlauncher.app.domain

import com.facetlauncher.app.data.FacetRepository
import com.facetlauncher.app.data.FakeEntitlementRepository
import com.facetlauncher.app.data.local.FacetEntity
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test
import org.mockito.Mockito.`when`
import org.mockito.Mockito.mock

class SelectableFacetsUseCaseTest {

    private val five = List(5) { FacetEntity(id = it + 1L, name = "F$it", position = it) }
    private val facets = MutableStateFlow(five)
    private val facetRepository: FacetRepository = mock(FacetRepository::class.java).also { `when`(it.observeFacets()).thenReturn(facets) }

    @Test
    fun `pro can use every facet`() = runTest {
        assertEquals(setOf(1L, 2L, 3L, 4L, 5L), SelectableFacetsUseCase(facetRepository, FakeEntitlementRepository(true))())
    }

    @Test
    fun `free can use only the first three`() = runTest {
        assertEquals(setOf(1L, 2L, 3L), SelectableFacetsUseCase(facetRepository, FakeEntitlementRepository(false))())
    }

    @Test
    fun `observe follows the entitlement and the facet list`() = runTest {
        val entitlement = FakeEntitlementRepository(false)
        val useCase = SelectableFacetsUseCase(facetRepository, entitlement)
        assertEquals(five.take(3), useCase.observe().first())

        entitlement.proFlow.value = true
        assertEquals(five, useCase.observe().first())

        // Deleting one while free brings the next facet back in
        entitlement.proFlow.value = false
        facets.value = five.filterNot { it.id == 2L }
        assertEquals(listOf(1L, 3L, 4L), useCase.observe().first().map { it.id })
    }
}
