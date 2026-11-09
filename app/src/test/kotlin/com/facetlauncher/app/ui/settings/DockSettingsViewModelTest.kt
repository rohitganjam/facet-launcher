package com.facetlauncher.app.ui.settings

import androidx.lifecycle.SavedStateHandle
import com.facetlauncher.app.data.FacetRepository
import com.facetlauncher.app.data.SettingsRepository
import com.facetlauncher.app.data.WallpaperRepository
import com.facetlauncher.app.data.model.Facet
import com.facetlauncher.app.data.model.AppInfo
import com.facetlauncher.app.data.model.DockDisplayMode
import com.facetlauncher.app.data.model.HomeWallpaper
import com.facetlauncher.app.data.model.LauncherSettings
import com.facetlauncher.app.data.model.PlacedItem
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collect
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
import org.mockito.ArgumentMatchers.anyLong
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify
import org.mockito.Mockito.`when`
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import com.facetlauncher.app.data.model.PlacementTarget
import com.facetlauncher.app.data.model.PlacementList
import com.facetlauncher.app.data.PlacementRepository
import com.facetlauncher.app.domain.ApplyFacetDefaultsUseCase
import com.facetlauncher.app.domain.FacetDefaultsScope
import com.facetlauncher.app.domain.ObservePlacedItemsUseCase

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(RobolectricTestRunner::class)
class DockSettingsViewModelTest {

    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setUp() { Dispatchers.setMain(testDispatcher) }

    @After
    fun tearDown() { Dispatchers.resetMain() }

    private fun appInfo(n: Int) = AppInfo(packageName = "com.example.$n", activityName = ".Main", label = "App $n", icon = null)

    /** A real (not Mockito) stub — plain Mockito can't reliably stub a `suspend` return. */
    private fun fakeWallpaperRepository() =
        object : WallpaperRepository(mock(android.app.WallpaperManager::class.java)) {
            override suspend fun currentHomeWallpaper(): HomeWallpaper = HomeWallpaper.Unavailable
        }

    private fun createViewModel(
        facetId: Long = 1L,
        dockItems: List<PlacedItem> = emptyList(),
        facets: List<Facet> = emptyList(),
        settingsRepository: SettingsRepository = mock(SettingsRepository::class.java),
        facetRepository: FacetRepository = mock(FacetRepository::class.java),
        placementRepository: PlacementRepository = mock(PlacementRepository::class.java),
        observePlacedItems: ObservePlacedItemsUseCase = mock(ObservePlacedItemsUseCase::class.java),
        applyFacetDefaults: ApplyFacetDefaultsUseCase = mock(ApplyFacetDefaultsUseCase::class.java),
    ): DockSettingsViewModel {
        `when`(settingsRepository.settings).thenReturn(flowOf(LauncherSettings()))
        `when`(facetRepository.observeFacets()).thenReturn(MutableStateFlow(facets))
        `when`(observePlacedItems.items(PlacementTarget(PlacementList.DOCK, facetId))).thenReturn(flowOf(dockItems))
        return DockSettingsViewModel(
            SavedStateHandle(mapOf("facetId" to facetId)),
            settingsRepository,
            facetRepository,
            placementRepository,
            observePlacedItems,
            fakeWallpaperRepository(),
            applyFacetDefaults,
        )
    }

    @Test
    fun `dock items flow through to uiState`() = runTest {
        val fiveItems = (1..5).map { PlacedItem.SingleApp(appInfo(it)) }
        val viewModel = createViewModel(dockItems = fiveItems)
        backgroundScope.launch { viewModel.uiState.collect {} }
        testDispatcher.scheduler.advanceUntilIdle()

        assertEquals(5, viewModel.uiState.value.dockItems.size)
    }

    @Test
    fun `display style writes to that facet, not SettingsRepository`() = runTest {
        val settingsRepository = mock(SettingsRepository::class.java)
        val facetRepository = mock(FacetRepository::class.java)
        val facet = Facet(id = 7L, name = "Work", position = 0)
        `when`(facetRepository.getById(7L)).thenReturn(facet)
        val viewModel = createViewModel(facetId = 7L, facets = listOf(facet), settingsRepository = settingsRepository, facetRepository = facetRepository)

        viewModel.setDockDisplayMode(DockDisplayMode.TEXT)
        testDispatcher.scheduler.advanceUntilIdle()

        verify(facetRepository).setDockDisplayMode(facet.id, DockDisplayMode.TEXT)
        verify(settingsRepository, org.mockito.Mockito.never()).setDockDisplayMode(DockDisplayMode.TEXT)
    }

    @Test
    fun `reorder writes to that facet's own dock`() = runTest {
        val placementRepository = mock(PlacementRepository::class.java)
        val facet = Facet(id = 7L, name = "Work", position = 0)
        val viewModel = createViewModel(
            facetId = 7L,
            facets = listOf(facet),
            placementRepository = placementRepository,
        )
        val reordered = listOf(PlacedItem.SingleApp(appInfo(2)), PlacedItem.SingleApp(appInfo(1)))

        viewModel.reorderDockItems(reordered)
        testDispatcher.scheduler.advanceUntilIdle()

        verify(placementRepository).reorderItems(PlacementTarget(PlacementList.DOCK, 7L), reordered)
    }

    @Test
    fun `dockDisplayMode is the facet's own value`() = runTest {
        val facet = Facet(id = 3L, name = "Work", position = 0, dockDisplayMode = DockDisplayMode.TEXT)
        val viewModel = createViewModel(facetId = 3L, facets = listOf(facet))
        backgroundScope.launch { viewModel.uiState.collect {} }
        testDispatcher.scheduler.advanceUntilIdle()

        assertEquals(DockDisplayMode.TEXT, viewModel.uiState.value.dockDisplayMode)
    }

    @Test
    fun `the screen lists that facet's own dock`() = runTest {
        val facet = Facet(id = 3L, name = "Work", position = 0)
        val viewModel = createViewModel(facetId = 3L, facets = listOf(facet), dockItems = listOf(PlacedItem.SingleApp(appInfo(1))))
        backgroundScope.launch { viewModel.uiState.collect {} }
        testDispatcher.scheduler.advanceUntilIdle()

        assertEquals(1, viewModel.uiState.value.dockItems.size)
    }

    @Test
    fun `reset to default applies the dock defaults to the facet`() = runTest {
        val applyDefaults = mock(ApplyFacetDefaultsUseCase::class.java)
        val viewModel = createViewModel(facetId = 3L, facets = listOf(Facet(id = 3L, name = "Work", position = 0)), applyFacetDefaults = applyDefaults)

        viewModel.resetToDefault()
        testDispatcher.scheduler.advanceUntilIdle()

        verify(applyDefaults).invoke(3L, FacetDefaultsScope.DOCK)
    }

}
