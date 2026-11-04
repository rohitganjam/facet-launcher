package com.facetlauncher.app.domain

import com.facetlauncher.app.data.WidgetPlacementRepository
import com.facetlauncher.app.data.widget.AppWidgetRepository
import kotlinx.coroutines.test.runTest
import org.junit.Test
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify

class DeleteWidgetUseCaseTest {

    @Test
    fun `invoke removes the placement row and releases the host id`() = runTest {
        // Given both repositories
        val widgetPlacementRepository = mock(WidgetPlacementRepository::class.java)
        val appWidgetRepository = mock(AppWidgetRepository::class.java)
        val useCase = DeleteWidgetUseCase(widgetPlacementRepository, appWidgetRepository)

        // When deleting a widget
        useCase(appWidgetId = 7)

        // Then both the Room row and the host id are cleaned up — neither alone is correct
        verify(widgetPlacementRepository).deleteById(7)
        verify(appWidgetRepository).deleteAppWidgetId(7)
    }

    @Test
    fun `a widget Facet draws itself is removed without releasing a host id`() = runTest {
        // Given both repositories
        val widgetPlacementRepository = mock(WidgetPlacementRepository::class.java)
        val appWidgetRepository = mock(AppWidgetRepository::class.java)
        val useCase = DeleteWidgetUseCase(widgetPlacementRepository, appWidgetRepository)

        // When deleting a native widget, whose id is negative
        useCase(appWidgetId = -3)

        // Then only the Room row goes
        verify(widgetPlacementRepository).deleteById(-3)
        verify(appWidgetRepository, org.mockito.Mockito.never()).deleteAppWidgetId(-3)
    }
}
