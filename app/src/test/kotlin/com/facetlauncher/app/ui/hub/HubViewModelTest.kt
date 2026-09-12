package com.facetlauncher.app.ui.hub

import com.facetlauncher.app.data.WidgetPlacementRepository
import com.facetlauncher.app.data.widget.AppWidgetRepository
import com.facetlauncher.app.domain.DeleteWidgetUseCase
import com.facetlauncher.app.domain.HubDomainState
import com.facetlauncher.app.domain.HubWidgetState
import com.facetlauncher.app.domain.ObserveHubStateUseCase
import com.facetlauncher.app.domain.ResolveWidgetResizeUseCase
import com.facetlauncher.app.domain.CompactWidgetsUseCase
import com.facetlauncher.app.domain.ResolveWidgetDropUseCase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify
import org.mockito.Mockito.`when`

@OptIn(ExperimentalCoroutinesApi::class)
class HubViewModelTest {

    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setUp() { Dispatchers.setMain(testDispatcher) }

    @After
    fun tearDown() { Dispatchers.resetMain() }

    private fun widget(id: Int, orphaned: Boolean = false, label: String? = "Weather") = HubWidgetState(
        appWidgetId = id,
        row = 0,
        col = 0,
        colSpan = 1,
        rowSpan = 1,
        providerLabel = if (orphaned) null else label,
        isOrphaned = orphaned,
    )

    @Test
    fun `maps domain widgets into stable UI widgets`() = runTest {
        val observeHubState = mock(ObserveHubStateUseCase::class.java)
        `when`(observeHubState()).thenReturn(flowOf(HubDomainState(widgets = listOf(widget(id = 1)))))
        val viewModel = HubViewModel(
            observeHubState,
            mock(AppWidgetRepository::class.java),
            mock(WidgetPlacementRepository::class.java),
            mock(DeleteWidgetUseCase::class.java),
            ResolveWidgetDropUseCase(),
            ResolveWidgetResizeUseCase(),
            CompactWidgetsUseCase(),
        )
        backgroundScope.launch { viewModel.uiState.collect {} }
        testDispatcher.scheduler.advanceUntilIdle()

        val widget = viewModel.uiState.value.widgets.single()
        assertEquals(1, widget.appWidgetId)
        assertEquals("Weather", widget.providerLabel)
        assertEquals(false, widget.isOrphaned)
        assertEquals(false, viewModel.uiState.value.isLoading)
    }

    @Test
    fun `an orphaned domain widget maps through with no label`() = runTest {
        val observeHubState = mock(ObserveHubStateUseCase::class.java)
        `when`(observeHubState()).thenReturn(flowOf(HubDomainState(widgets = listOf(widget(id = 1, orphaned = true)))))
        val viewModel = HubViewModel(
            observeHubState,
            mock(AppWidgetRepository::class.java),
            mock(WidgetPlacementRepository::class.java),
            mock(DeleteWidgetUseCase::class.java),
            ResolveWidgetDropUseCase(),
            ResolveWidgetResizeUseCase(),
            CompactWidgetsUseCase(),
        )
        backgroundScope.launch { viewModel.uiState.collect {} }
        testDispatcher.scheduler.advanceUntilIdle()

        val widget = viewModel.uiState.value.widgets.single()
        assertEquals(true, widget.isOrphaned)
        assertEquals(null, widget.providerLabel)
    }

    @Test
    fun `isEmpty and isAtCapacity reflect the widget count`() = runTest {
        val observeHubState = mock(ObserveHubStateUseCase::class.java)
        `when`(observeHubState()).thenReturn(flowOf(HubDomainState(widgets = emptyList())))
        val viewModel = HubViewModel(
            observeHubState,
            mock(AppWidgetRepository::class.java),
            mock(WidgetPlacementRepository::class.java),
            mock(DeleteWidgetUseCase::class.java),
            ResolveWidgetDropUseCase(),
            ResolveWidgetResizeUseCase(),
            CompactWidgetsUseCase(),
        )
        backgroundScope.launch { viewModel.uiState.collect {} }
        testDispatcher.scheduler.advanceUntilIdle()

        assertEquals(true, viewModel.uiState.value.isEmpty)
        assertEquals(false, viewModel.uiState.value.isAtCapacity)
    }

    @Test
    fun `onHubVisible and onHubHidden delegate to AppWidgetRepository`() {
        val observeHubState = mock(ObserveHubStateUseCase::class.java)
        `when`(observeHubState()).thenReturn(flowOf(HubDomainState(widgets = emptyList())))
        val appWidgetRepository = mock(AppWidgetRepository::class.java)
        val viewModel = HubViewModel(
            observeHubState,
            appWidgetRepository,
            mock(WidgetPlacementRepository::class.java),
            mock(DeleteWidgetUseCase::class.java),
            ResolveWidgetDropUseCase(),
            ResolveWidgetResizeUseCase(),
            CompactWidgetsUseCase(),
        )

        viewModel.onHubVisible()
        verify(appWidgetRepository).startListening()

        viewModel.onHubHidden()
        verify(appWidgetRepository).stopListening()
    }
}
