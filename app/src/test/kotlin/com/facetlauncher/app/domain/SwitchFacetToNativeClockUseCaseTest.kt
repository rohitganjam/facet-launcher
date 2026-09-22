package com.facetlauncher.app.domain

import com.facetlauncher.app.data.FacetRepository
import com.facetlauncher.app.data.local.FacetEntity
import com.facetlauncher.app.data.widget.AppWidgetRepository
import kotlinx.coroutines.test.runTest
import org.junit.Test
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify
import org.mockito.Mockito.verifyNoInteractions

class SwitchFacetToNativeClockUseCaseTest {

    @Test
    fun `invoke releases the bound widget id and clears the facet's own field`() = runTest {
        val facetRepository = mock(FacetRepository::class.java)
        val appWidgetRepository = mock(AppWidgetRepository::class.java)
        val useCase = SwitchFacetToNativeClockUseCase(facetRepository, appWidgetRepository)
        val facet = FacetEntity(id = 1, name = "Work", position = 0, clockWidgetAppWidgetId = 99)

        useCase(facet)

        verify(appWidgetRepository).deleteAppWidgetId(99)
        verify(facetRepository).setClockWidgetAppWidgetId(facet, null)
    }

    @Test
    fun `invoke is a no-op release when the facet already has no hosted clock widget`() = runTest {
        val facetRepository = mock(FacetRepository::class.java)
        val appWidgetRepository = mock(AppWidgetRepository::class.java)
        val useCase = SwitchFacetToNativeClockUseCase(facetRepository, appWidgetRepository)
        val facet = FacetEntity(id = 1, name = "Work", position = 0, clockWidgetAppWidgetId = null)

        useCase(facet)

        verifyNoInteractions(appWidgetRepository)
        verify(facetRepository).setClockWidgetAppWidgetId(facet, null)
    }
}
