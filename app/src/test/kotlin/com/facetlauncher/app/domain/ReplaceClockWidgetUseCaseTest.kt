package com.facetlauncher.app.domain

import com.facetlauncher.app.data.ClockStackRepository
import com.facetlauncher.app.data.widget.AppWidgetRepository
import kotlinx.coroutines.test.runTest
import org.junit.Test
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify
import org.mockito.Mockito.verifyNoInteractions
import org.mockito.Mockito.`when`

class ReplaceClockWidgetUseCaseTest {

    @Test
    fun `invoke stores the new widget and releases every id the stack held before`() = runTest {
        // Given a facet whose stack held widgets 10 and 11
        val clockStackRepository = mock(ClockStackRepository::class.java)
        val appWidgetRepository = mock(AppWidgetRepository::class.java)
        `when`(clockStackRepository.replaceWithWidget(1L, 99)).thenReturn(listOf(10, 11))

        // When widget 99 replaces it
        ReplaceClockWidgetUseCase(clockStackRepository, appWidgetRepository)(1L, 99)

        // Then the old ids are released, so none leaks
        verify(appWidgetRepository).deleteAppWidgetId(10)
        verify(appWidgetRepository).deleteAppWidgetId(11)
    }

    @Test
    fun `invoke releases nothing when the facet had no widget`() = runTest {
        // Given a facet on its native clock
        val clockStackRepository = mock(ClockStackRepository::class.java)
        val appWidgetRepository = mock(AppWidgetRepository::class.java)
        `when`(clockStackRepository.replaceWithWidget(1L, 99)).thenReturn(emptyList())

        // When a widget is chosen
        ReplaceClockWidgetUseCase(clockStackRepository, appWidgetRepository)(1L, 99)

        // Then there is nothing to release
        verifyNoInteractions(appWidgetRepository)
    }
}
