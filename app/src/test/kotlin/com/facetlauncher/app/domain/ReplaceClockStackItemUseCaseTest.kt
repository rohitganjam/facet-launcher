package com.facetlauncher.app.domain

import com.facetlauncher.app.data.ClockStackRepository
import com.facetlauncher.app.data.ReplacedItem
import com.facetlauncher.app.data.widget.AppWidgetRepository
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify
import org.mockito.Mockito.verifyNoInteractions
import org.mockito.Mockito.`when`

class ReplaceClockStackItemUseCaseTest {

    private val clockStackRepository = mock(ClockStackRepository::class.java)
    private val appWidgetRepository = mock(AppWidgetRepository::class.java)
    private val useCase = ReplaceClockStackItemUseCase(clockStackRepository, appWidgetRepository)

    @Test
    fun `withWidget releases the id the replaced page held`() = runTest {
        // Given a page that held widget 10
        `when`(clockStackRepository.replaceWithWidget(5L, 99)).thenReturn(ReplacedItem(releasedAppWidgetId = 10))

        // When widget 99 replaces it
        val replaced = useCase.withWidget(5L, 99)

        // Then the old id is released
        assertTrue(replaced)
        verify(appWidgetRepository).deleteAppWidgetId(10)
    }

    @Test
    fun `withWidget on the clock page releases nothing`() = runTest {
        // Given a page that was the clock
        `when`(clockStackRepository.replaceWithWidget(5L, 99)).thenReturn(ReplacedItem(releasedAppWidgetId = null))

        // When a widget replaces it
        val replaced = useCase.withWidget(5L, 99)

        // Then there is no id to release
        assertTrue(replaced)
        verifyNoInteractions(appWidgetRepository)
    }

    @Test
    fun `withWidget reports a page that no longer exists`() = runTest {
        // Given a page that is gone
        `when`(clockStackRepository.replaceWithWidget(5L, 99)).thenReturn(null)

        // When a widget is meant to replace it
        val replaced = useCase.withWidget(5L, 99)

        // Then it reports failure so the caller can release the id it bound
        assertFalse(replaced)
        verifyNoInteractions(appWidgetRepository)
    }

    @Test
    fun `withClock releases the widget the page held`() = runTest {
        // Given a page that held widget 10
        `when`(clockStackRepository.replaceWithClock(5L)).thenReturn(ReplacedItem(releasedAppWidgetId = 10))

        // When the clock replaces it
        val replaced = useCase.withClock(5L)

        // Then widget 10 is released
        assertTrue(replaced)
        verify(appWidgetRepository).deleteAppWidgetId(10)
    }

    @Test
    fun `withClock is refused when the clock is already in the stack`() = runTest {
        // Given the repository refuses because there is another clock
        `when`(clockStackRepository.replaceWithClock(5L)).thenReturn(null)

        // When the clock would replace the page
        val replaced = useCase.withClock(5L)

        // Then nothing is released
        assertFalse(replaced)
        verifyNoInteractions(appWidgetRepository)
    }
}
