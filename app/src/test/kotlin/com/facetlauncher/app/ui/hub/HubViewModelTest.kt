package com.facetlauncher.app.ui.hub

import com.facetlauncher.app.data.WidgetPlacementRepository
import com.facetlauncher.app.data.local.WidgetPlacementDao
import com.facetlauncher.app.data.local.WidgetPlacementEntity
import com.facetlauncher.app.data.widget.AppWidgetRepository
import com.facetlauncher.app.domain.DeleteWidgetUseCase
import com.facetlauncher.app.domain.HubDomainState
import com.facetlauncher.app.domain.HubWidgetState
import com.facetlauncher.app.domain.ObserveHubStateUseCase
import com.facetlauncher.app.domain.ResolveWidgetResizeUseCase
import com.facetlauncher.app.domain.CompactWidgetsUseCase
import com.facetlauncher.app.domain.FlowPackWidgetsUseCase
import com.facetlauncher.app.domain.LandscapeHubUseCase
import com.facetlauncher.app.domain.OrderWidgetsForLandscapeUseCase
import com.facetlauncher.app.domain.ReorderLandscapeWidgetUseCase
import com.facetlauncher.app.domain.ResolveWidgetDropUseCase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
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
            LandscapeHubUseCase(OrderWidgetsForLandscapeUseCase(), FlowPackWidgetsUseCase(), ReorderLandscapeWidgetUseCase()),
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
            LandscapeHubUseCase(OrderWidgetsForLandscapeUseCase(), FlowPackWidgetsUseCase(), ReorderLandscapeWidgetUseCase()),
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
            LandscapeHubUseCase(OrderWidgetsForLandscapeUseCase(), FlowPackWidgetsUseCase(), ReorderLandscapeWidgetUseCase()),
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
            LandscapeHubUseCase(OrderWidgetsForLandscapeUseCase(), FlowPackWidgetsUseCase(), ReorderLandscapeWidgetUseCase()),
        )

        viewModel.onHubVisible()
        verify(appWidgetRepository).startListening()

        viewModel.onHubHidden()
        verify(appWidgetRepository).stopListening()
    }

    // --- Landscape ---

    private class InMemoryWidgetPlacementDao(initial: List<WidgetPlacementEntity>) : WidgetPlacementDao {
        val state = MutableStateFlow(initial)
        override fun observeAll(): Flow<List<WidgetPlacementEntity>> = state
        override suspend fun getById(appWidgetId: Int): WidgetPlacementEntity? = state.value.find { it.appWidgetId == appWidgetId }
        override suspend fun upsert(placement: WidgetPlacementEntity) {
            state.value = state.value.filterNot { it.appWidgetId == placement.appWidgetId } + placement
        }
        override suspend fun deleteById(appWidgetId: Int) {
            state.value = state.value.filterNot { it.appWidgetId == appWidgetId }
        }
        override suspend fun delete(placement: WidgetPlacementEntity) = deleteById(placement.appWidgetId)
    }

    private fun entity(id: Int, row: Int, landscapeOrder: Int? = null) = WidgetPlacementEntity(
        appWidgetId = id, providerPackageName = "com.example", providerClassName = ".W$id", row = row, col = 0, colSpan = 1, rowSpan = 1, landscapeOrder = landscapeOrder,
    )

    private fun landscapeViewModel(dao: InMemoryWidgetPlacementDao, widgets: List<HubWidgetState>): HubViewModel {
        val observeHubState = mock(ObserveHubStateUseCase::class.java)
        `when`(observeHubState()).thenReturn(flowOf(HubDomainState(widgets = widgets)))
        return HubViewModel(
            observeHubState,
            mock(AppWidgetRepository::class.java),
            WidgetPlacementRepository(dao),
            mock(DeleteWidgetUseCase::class.java),
            ResolveWidgetDropUseCase(),
            ResolveWidgetResizeUseCase(),
            CompactWidgetsUseCase(),
            LandscapeHubUseCase(OrderWidgetsForLandscapeUseCase(), FlowPackWidgetsUseCase(), ReorderLandscapeWidgetUseCase()),
        )
    }

    private fun placed(id: Int, row: Int, landscapeOrder: Int? = null) =
        HubWidgetState(id, row = row, col = 0, colSpan = 1, rowSpan = 1, providerLabel = "W$id", isOrphaned = false, landscapeOrder = landscapeOrder)

    @Test
    fun `landscape widgets stay empty until the screen reports its rows`() = runTest {
        val viewModel = landscapeViewModel(InMemoryWidgetPlacementDao(emptyList()), listOf(placed(1, row = 0)))
        backgroundScope.launch { viewModel.uiState.collect {} }
        testDispatcher.scheduler.advanceUntilIdle()

        assertEquals(emptyList<HubLandscapeWidgetUi>(), viewModel.uiState.value.landscapeWidgets)
        assertEquals(1, viewModel.uiState.value.widgets.size)
    }

    @Test
    fun `reported rows pack the widgets down the columns in landscape order`() = runTest {
        // Given three widgets, with the third ordered first
        val viewModel = landscapeViewModel(
            InMemoryWidgetPlacementDao(emptyList()),
            listOf(placed(1, row = 0, landscapeOrder = 1), placed(2, row = 1, landscapeOrder = 2), placed(3, row = 2, landscapeOrder = 0)),
        )
        backgroundScope.launch { viewModel.uiState.collect {} }

        // When the screen reports two rows
        viewModel.onLandscapeRowsChange(2)
        testDispatcher.scheduler.advanceUntilIdle()

        // Then they fill column 0 then column 1, in order 3, 1, 2
        val packed = viewModel.uiState.value.landscapeWidgets
        assertEquals(listOf(Triple(3, 0, 0), Triple(1, 1, 0), Triple(2, 0, 1)), packed.map { Triple(it.appWidgetId, it.row, it.col) })
    }

    @Test
    fun `a landscape drop saves a new order for every widget that moved and leaves portrait alone`() = runTest {
        // Given three widgets that have never been ordered, stored with portrait rows 0, 1, 2
        val dao = InMemoryWidgetPlacementDao(listOf(entity(1, row = 0), entity(2, row = 1), entity(3, row = 2)))
        val viewModel = landscapeViewModel(dao, listOf(placed(1, row = 0), placed(2, row = 1), placed(3, row = 2)))
        backgroundScope.launch { viewModel.uiState.collect {} }
        viewModel.onLandscapeRowsChange(3)
        testDispatcher.scheduler.advanceUntilIdle()

        // When widget 3 is dropped on widget 1's cell
        viewModel.onLandscapeWidgetDrop(appWidgetId = 3, dropRow = 0, dropCol = 0)
        testDispatcher.scheduler.advanceUntilIdle()

        // Then the landscape order is 3, 1, 2 and every portrait row is unchanged
        val saved = dao.state.value.associateBy { it.appWidgetId }
        assertEquals(mapOf(3 to 0, 1 to 1, 2 to 2), saved.mapValues { it.value.landscapeOrder })
        assertEquals(mapOf(1 to 0, 2 to 1, 3 to 2), saved.mapValues { it.value.row })
    }

    @Test
    fun `a landscape drop before the screen has reported its rows does nothing`() = runTest {
        val dao = InMemoryWidgetPlacementDao(listOf(entity(1, row = 0), entity(2, row = 1)))
        val viewModel = landscapeViewModel(dao, listOf(placed(1, row = 0), placed(2, row = 1)))
        backgroundScope.launch { viewModel.uiState.collect {} }
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.onLandscapeWidgetDrop(appWidgetId = 2, dropRow = 0, dropCol = 0)
        testDispatcher.scheduler.advanceUntilIdle()

        assertEquals(listOf<Int?>(null, null), dao.state.value.sortedBy { it.appWidgetId }.map { it.landscapeOrder })
    }

    @Test
    fun `a landscape resize keeps the widget's portrait position and saves the new size`() = runTest {
        // Given widget 2 resting on widget 1, at portrait row 1, column 1
        val dao = InMemoryWidgetPlacementDao(listOf(entity(1, row = 0).copy(col = 1), entity(2, row = 1).copy(col = 1)))
        val viewModel = landscapeViewModel(dao, listOf(placed(1, row = 0).copy(col = 1), placed(2, row = 1).copy(col = 1)))
        backgroundScope.launch { viewModel.uiState.collect {} }
        testDispatcher.scheduler.advanceUntilIdle()

        // When widget 2 is resized in landscape to 2 by 2
        viewModel.onLandscapeWidgetResize(appWidgetId = 2, colSpan = 2, rowSpan = 2)
        testDispatcher.scheduler.advanceUntilIdle()

        // Then the size is saved and the portrait cell it was anchored on is unchanged
        val saved = dao.state.value.single { it.appWidgetId == 2 }
        assertEquals(listOf(2, 2, 1, 1), listOf(saved.colSpan, saved.rowSpan, saved.row, saved.col))
    }
}
