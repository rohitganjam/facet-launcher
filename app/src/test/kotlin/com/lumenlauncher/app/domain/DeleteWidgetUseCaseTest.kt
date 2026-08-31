package com.lumenlauncher.app.domain

import com.lumenlauncher.app.data.WidgetPlacementRepository
import com.lumenlauncher.app.data.widget.AppWidgetRepository
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
}
