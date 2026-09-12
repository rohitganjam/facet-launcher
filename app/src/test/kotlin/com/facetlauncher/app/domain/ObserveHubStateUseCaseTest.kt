package com.facetlauncher.app.domain

import android.appwidget.AppWidgetProviderInfo
import com.facetlauncher.app.data.WidgetPlacementRepository
import com.facetlauncher.app.data.local.WidgetPlacementEntity
import com.facetlauncher.app.data.widget.AppWidgetRepository
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.ArgumentMatchers.anyInt
import org.mockito.Mockito.mock
import org.mockito.Mockito.`when`
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class ObserveHubStateUseCaseTest {

    private fun placement(id: Int, row: Int = 0, col: Int = 0) = WidgetPlacementEntity(
        appWidgetId = id,
        providerPackageName = "com.example.widgets",
        providerClassName = ".Provider",
        row = row,
        col = col,
        colSpan = 1,
        rowSpan = 1,
    )

    @Test
    fun `a resolvable provider maps to a non-orphaned widget with its label`() = runTest {
        // Given one stored placement whose provider still resolves
        val widgetPlacementRepository = mock(WidgetPlacementRepository::class.java)
        `when`(widgetPlacementRepository.observeAll()).thenReturn(flowOf(listOf(placement(id = 1))))
        val appWidgetRepository = mock(AppWidgetRepository::class.java)
        `when`(appWidgetRepository.observeProviderChanges()).thenReturn(MutableSharedFlow())
        `when`(appWidgetRepository.getAppWidgetInfo(1)).thenReturn(AppWidgetProviderInfo())
        `when`(appWidgetRepository.getProviderLabel(1)).thenReturn("Weather")

        // When observing Hub state
        val state = ObserveHubStateUseCase(widgetPlacementRepository, appWidgetRepository)().first()

        // Then it's not orphaned and carries the resolved label
        val widget = state.widgets.single()
        assertEquals(false, widget.isOrphaned)
        assertEquals("Weather", widget.providerLabel)
    }

    @Test
    fun `an unresolvable provider (uninstalled app) maps to an orphaned widget`() = runTest {
        // Given a stored placement whose provider no longer resolves
        val widgetPlacementRepository = mock(WidgetPlacementRepository::class.java)
        `when`(widgetPlacementRepository.observeAll()).thenReturn(flowOf(listOf(placement(id = 1))))
        val appWidgetRepository = mock(AppWidgetRepository::class.java)
        `when`(appWidgetRepository.observeProviderChanges()).thenReturn(MutableSharedFlow())
        `when`(appWidgetRepository.getAppWidgetInfo(1)).thenReturn(null)

        // When observing Hub state
        val state = ObserveHubStateUseCase(widgetPlacementRepository, appWidgetRepository)().first()

        // Then it's flagged orphaned, with no label
        val widget = state.widgets.single()
        assertEquals(true, widget.isOrphaned)
        assertEquals(null, widget.providerLabel)
    }

    @Test
    fun `isAtCapacity flips true at twenty widgets`() = runTest {
        // Given exactly twenty stored placements
        val placements = (0 until HUB_MAX_WIDGETS).map { placement(id = it, row = it) }
        val widgetPlacementRepository = mock(WidgetPlacementRepository::class.java)
        `when`(widgetPlacementRepository.observeAll()).thenReturn(flowOf(placements))
        val appWidgetRepository = mock(AppWidgetRepository::class.java)
        `when`(appWidgetRepository.observeProviderChanges()).thenReturn(MutableSharedFlow())
        `when`(appWidgetRepository.getAppWidgetInfo(anyInt())).thenReturn(AppWidgetProviderInfo())

        // When observing Hub state
        val state = ObserveHubStateUseCase(widgetPlacementRepository, appWidgetRepository)().first()

        // Then it reports at-capacity
        assertTrue(state.isAtCapacity)
        assertEquals(HUB_MAX_WIDGETS, state.widgetCount)
    }
}
