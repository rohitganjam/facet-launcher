package com.facetlauncher.app.domain

import com.facetlauncher.app.data.FacetRepository
import com.facetlauncher.app.data.local.FacetEntity
import com.facetlauncher.app.data.widget.AppWidgetRepository
import kotlinx.coroutines.test.runTest
import org.junit.Test
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify
import org.mockito.Mockito.verifyNoInteractions

class DeleteFacetUseCaseTest {

    @Test
    fun `invoke releases a bound clock widget id before deleting the facet`() = runTest {
        val facetRepository = mock(FacetRepository::class.java)
        val appWidgetRepository = mock(AppWidgetRepository::class.java)
        val useCase = DeleteFacetUseCase(facetRepository, appWidgetRepository)
        val facet = FacetEntity(id = 1, name = "Work", position = 0, clockWidgetAppWidgetId = 42)

        useCase(facet)

        verify(appWidgetRepository).deleteAppWidgetId(42)
        verify(facetRepository).deleteFacet(facet)
    }

    @Test
    fun `invoke does not touch AppWidgetRepository when the facet has no hosted clock widget`() = runTest {
        val facetRepository = mock(FacetRepository::class.java)
        val appWidgetRepository = mock(AppWidgetRepository::class.java)
        val useCase = DeleteFacetUseCase(facetRepository, appWidgetRepository)
        val facet = FacetEntity(id = 1, name = "Work", position = 0, clockWidgetAppWidgetId = null)

        useCase(facet)

        verifyNoInteractions(appWidgetRepository)
        verify(facetRepository).deleteFacet(facet)
    }
}
