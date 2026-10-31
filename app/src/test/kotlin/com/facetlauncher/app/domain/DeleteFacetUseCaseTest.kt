package com.facetlauncher.app.domain

import com.facetlauncher.app.data.FacetRepository
import com.facetlauncher.app.data.model.ClockStackItem
import com.facetlauncher.app.data.model.ClockStackItemKind
import com.facetlauncher.app.data.model.Facet
import com.facetlauncher.app.data.widget.AppWidgetRepository
import kotlinx.coroutines.test.runTest
import org.junit.Test
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify
import org.mockito.Mockito.verifyNoInteractions

class DeleteFacetUseCaseTest {

    @Test
    fun `invoke releases every widget id in the clock stack before deleting the facet`() = runTest {
        val facetRepository = mock(FacetRepository::class.java)
        val appWidgetRepository = mock(AppWidgetRepository::class.java)
        val useCase = DeleteFacetUseCase(facetRepository, appWidgetRepository)
        val facet = Facet(
            id = 1, name = "Work", position = 0,
            clockStack = listOf(
                ClockStackItem(id = 1, facetId = 1, position = 0, kind = ClockStackItemKind.CLOCK),
                ClockStackItem(id = 2, facetId = 1, position = 1, kind = ClockStackItemKind.WIDGET, appWidgetId = 42),
                ClockStackItem(id = 3, facetId = 1, position = 2, kind = ClockStackItemKind.WIDGET, appWidgetId = 43),
            ),
        )

        useCase(facet)

        verify(appWidgetRepository).deleteAppWidgetId(42)
        verify(appWidgetRepository).deleteAppWidgetId(43)
        verify(facetRepository).deleteFacet(facet.id)
    }

    @Test
    fun `invoke does not touch AppWidgetRepository when the facet has no hosted widget`() = runTest {
        val facetRepository = mock(FacetRepository::class.java)
        val appWidgetRepository = mock(AppWidgetRepository::class.java)
        val useCase = DeleteFacetUseCase(facetRepository, appWidgetRepository)
        val facet = Facet(id = 1, name = "Work", position = 0)

        useCase(facet)

        verifyNoInteractions(appWidgetRepository)
        verify(facetRepository).deleteFacet(facet.id)
    }
}
