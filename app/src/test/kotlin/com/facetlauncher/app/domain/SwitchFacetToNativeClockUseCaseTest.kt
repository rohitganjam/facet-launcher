package com.facetlauncher.app.domain

import com.facetlauncher.app.data.ClockStackRepository
import com.facetlauncher.app.data.model.Facet
import com.facetlauncher.app.data.widget.AppWidgetRepository
import kotlinx.coroutines.test.runTest
import org.junit.Test
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify
import org.mockito.Mockito.verifyNoInteractions
import org.mockito.Mockito.`when`

class SwitchFacetToNativeClockUseCaseTest {

    @Test
    fun `invoke clears the stack and releases every widget id it held`() = runTest {
        // Given a facet whose stack held two widgets
        val clockStackRepository = mock(ClockStackRepository::class.java)
        val appWidgetRepository = mock(AppWidgetRepository::class.java)
        val facet = Facet(id = 1, name = "Work", position = 0)
        `when`(clockStackRepository.clear(facet.id)).thenReturn(listOf(99, 100))

        // When switching back to the native clock
        SwitchFacetToNativeClockUseCase(clockStackRepository, appWidgetRepository)(facet)

        // Then both ids are released
        verify(appWidgetRepository).deleteAppWidgetId(99)
        verify(appWidgetRepository).deleteAppWidgetId(100)
    }

    @Test
    fun `invoke releases nothing when the facet already has no hosted widget`() = runTest {
        // Given a facet on its native clock
        val clockStackRepository = mock(ClockStackRepository::class.java)
        val appWidgetRepository = mock(AppWidgetRepository::class.java)
        val facet = Facet(id = 1, name = "Work", position = 0)
        `when`(clockStackRepository.clear(facet.id)).thenReturn(emptyList())

        // When switching
        SwitchFacetToNativeClockUseCase(clockStackRepository, appWidgetRepository)(facet)

        // Then no widget id is released
        verifyNoInteractions(appWidgetRepository)
        verify(clockStackRepository).clear(facet.id)
    }
}
