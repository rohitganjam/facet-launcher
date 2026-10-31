package com.facetlauncher.app.domain

import com.facetlauncher.app.data.ClockStackRepository
import com.facetlauncher.app.data.widget.AppWidgetRepository
import kotlinx.coroutines.test.runTest
import org.junit.Test
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify
import org.mockito.Mockito.verifyNoInteractions
import org.mockito.Mockito.`when`

class RemoveClockStackItemUseCaseTest {

    private val clockStackRepository = mock(ClockStackRepository::class.java)
    private val appWidgetRepository = mock(AppWidgetRepository::class.java)
    private val useCase = RemoveClockStackItemUseCase(clockStackRepository, appWidgetRepository)

    @Test
    fun `removing a widget page releases its widget id`() = runTest {
        // Given a page that held widget 42
        `when`(clockStackRepository.remove(5L)).thenReturn(42)

        // When it is removed
        useCase(5L)

        // Then the id is released, so none leaks
        verify(appWidgetRepository).deleteAppWidgetId(42)
    }

    @Test
    fun `removing the clock page releases nothing`() = runTest {
        // Given the clock page, which holds no widget id
        `when`(clockStackRepository.remove(5L)).thenReturn(null)

        // When it is removed
        useCase(5L)

        // Then there is nothing to release
        verifyNoInteractions(appWidgetRepository)
    }
}
