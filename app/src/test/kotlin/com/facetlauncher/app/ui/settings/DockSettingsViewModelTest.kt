package com.facetlauncher.app.ui.settings

import androidx.lifecycle.SavedStateHandle
import com.facetlauncher.app.data.DockAppRepository
import com.facetlauncher.app.data.FacetDockAppRepository
import com.facetlauncher.app.data.FacetRepository
import com.facetlauncher.app.data.SettingsRepository
import com.facetlauncher.app.data.WallpaperRepository
import com.facetlauncher.app.data.local.FacetEntity
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
import org.mockito.Mockito.never
import org.mockito.Mockito.verify
import org.mockito.Mockito.`when`

@OptIn(ExperimentalCoroutinesApi::class)
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
        facetId: Long? = null,
        dockItems: List<PlacedItem> = emptyList(),
        facets: List<FacetEntity> = emptyList(),
        settingsRepository: SettingsRepository = mock(SettingsRepository::class.java),
        facetRepository: FacetRepository = mock(FacetRepository::class.java),
        dockAppRepository: DockAppRepository = mock(DockAppRepository::class.java),
        facetDockAppRepository: FacetDockAppRepository = mock(FacetDockAppRepository::class.java),
    ): DockSettingsViewModel {
        `when`(settingsRepository.settings).thenReturn(flowOf(LauncherSettings()))
        `when`(facetRepository.observeFacets()).thenReturn(MutableStateFlow(facets))
        `when`(dockAppRepository.observeDockItems()).thenReturn(flowOf(dockItems))
        `when`(facetDockAppRepository.observeDockItems(anyLong())).thenReturn(flowOf(dockItems))
        return DockSettingsViewModel(
            SavedStateHandle(facetId?.let { mapOf("facetId" to it) } ?: emptyMap()),
            settingsRepository,
            facetRepository,
            dockAppRepository,
            facetDockAppRepository,
            fakeWallpaperRepository(),
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
    fun `global-scoped setters write to SettingsRepository and DockAppRepository`() = runTest {
        val settingsRepository = mock(SettingsRepository::class.java)
        val dockAppRepository = mock(DockAppRepository::class.java)
        val viewModel = createViewModel(settingsRepository = settingsRepository, dockAppRepository = dockAppRepository)
        val reordered = listOf(PlacedItem.SingleApp(appInfo(2)), PlacedItem.SingleApp(appInfo(1)))

        viewModel.setDockDisplayMode(DockDisplayMode.TEXT)
        viewModel.reorderDockItems(reordered)
        testDispatcher.scheduler.advanceUntilIdle()

        verify(settingsRepository).setDockDisplayMode(DockDisplayMode.TEXT)
        verify(dockAppRepository).reorderDockItems(reordered)
    }

    @Test
    fun `facet-scoped setters write to that facet, not the launcher-wide dock`() = runTest {
        val settingsRepository = mock(SettingsRepository::class.java)
        val facetRepository = mock(FacetRepository::class.java)
        val facetDockAppRepository = mock(FacetDockAppRepository::class.java)
        `when`(facetDockAppRepository.observeDockItems(anyLong())).thenReturn(flowOf(emptyList()))
        val facet = FacetEntity(id = 7L, name = "Work", position = 0)
        `when`(facetRepository.getById(7L)).thenReturn(facet)
        val viewModel = createViewModel(
            facetId = 7L,
            facets = listOf(facet),
            settingsRepository = settingsRepository,
            facetRepository = facetRepository,
            facetDockAppRepository = facetDockAppRepository,
        )
        val reordered = listOf(PlacedItem.SingleApp(appInfo(2)), PlacedItem.SingleApp(appInfo(1)))

        viewModel.setDockDisplayMode(DockDisplayMode.TEXT)
        viewModel.reorderDockItems(reordered)
        testDispatcher.scheduler.advanceUntilIdle()

        verify(facetRepository).setDockDisplayMode(facet, DockDisplayMode.TEXT)
        verify(facetDockAppRepository).reorderDockItems(7L, reordered)
        verify(settingsRepository, never()).setDockDisplayMode(DockDisplayMode.TEXT)
    }
}
