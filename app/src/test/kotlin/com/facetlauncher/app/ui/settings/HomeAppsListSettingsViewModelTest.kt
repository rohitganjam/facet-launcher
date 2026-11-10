package com.facetlauncher.app.ui.settings

import androidx.lifecycle.SavedStateHandle
import com.facetlauncher.app.data.DefaultAppRepository
import com.facetlauncher.app.data.FacetRepository
import com.facetlauncher.app.data.SettingsRepository
import com.facetlauncher.app.data.WallpaperRepository
import com.facetlauncher.app.data.model.Facet
import com.facetlauncher.app.data.model.AppInfo
import com.facetlauncher.app.data.model.AppListVerticalAlignment
import com.facetlauncher.app.data.model.AppRowPosition
import com.facetlauncher.app.data.model.AppRowPresentation
import com.facetlauncher.app.data.model.HomeWallpaper
import com.facetlauncher.app.data.model.LauncherSettings
import com.facetlauncher.app.data.model.ListContentMode
import com.facetlauncher.app.data.model.PlacedItem
import com.facetlauncher.app.domain.FacetDefaultsScope
import com.facetlauncher.app.domain.CopyFacetSettingsUseCase
import com.facetlauncher.app.domain.GetInstalledAppsUseCase
import com.facetlauncher.app.domain.SelectPreviewAppsUseCase
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
import com.facetlauncher.app.eqOf
import com.facetlauncher.app.LandscapeTransformCaptor
import com.facetlauncher.app.data.model.AppListColumnAlignment
import com.facetlauncher.app.data.model.LandscapeOverrides
import org.robolectric.RobolectricTestRunner
import com.facetlauncher.app.data.model.PlacementTarget
import com.facetlauncher.app.data.model.PlacementList
import com.facetlauncher.app.data.PlacementRepository
import com.facetlauncher.app.domain.ObservePlacedItemsUseCase
import com.facetlauncher.app.data.model.AppListLimits

/** A real (not Mockito) stub — plain Mockito can't reliably stub a `suspend` return. */
private fun fakeWallpaperRepository() =
    object : WallpaperRepository(mock(android.app.WallpaperManager::class.java)) {
        override suspend fun currentHomeWallpaper(): HomeWallpaper = HomeWallpaper.Unavailable
    }

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(RobolectricTestRunner::class)
class HomeAppsListSettingsViewModelTest {

    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setUp() { Dispatchers.setMain(testDispatcher) }

    @After
    fun tearDown() { Dispatchers.resetMain() }

    private fun appInfo(n: Int) = AppInfo(packageName = "com.example.$n", activityName = ".Main", label = "App $n", icon = null)

    private suspend fun createViewModel(
        facetId: Long? = null,
        favorites: List<PlacedItem> = emptyList(),
        facets: List<Facet> = emptyList(),
        settings: LauncherSettings = LauncherSettings(),
        settingsRepository: SettingsRepository = mock(SettingsRepository::class.java),
        facetRepository: FacetRepository = mock(FacetRepository::class.java),
        placementRepository: PlacementRepository = mock(PlacementRepository::class.java),
        observePlacedItems: ObservePlacedItemsUseCase = mock(ObservePlacedItemsUseCase::class.java),
    ): HomeAppsListSettingsViewModel {
        val getInstalledApps = mock(GetInstalledAppsUseCase::class.java)
        val defaultAppRepository = mock(DefaultAppRepository::class.java)
        `when`(settingsRepository.settings).thenReturn(flowOf(settings))
        `when`(facetRepository.observeFacets()).thenReturn(MutableStateFlow(facets))
        facetId?.let { `when`(observePlacedItems.items(PlacementTarget(PlacementList.FAVORITES, it))).thenReturn(flowOf(favorites)) }
        `when`(getInstalledApps.observe()).thenReturn(flowOf(emptyList()))
        `when`(defaultAppRepository.getDefaultAppPackages()).thenReturn(emptyList())
        return HomeAppsListSettingsViewModel(
            SavedStateHandle(facetId?.let { mapOf("facetId" to it) } ?: emptyMap()),
            settingsRepository,
            facetRepository,
            placementRepository,
            observePlacedItems,
            fakeWallpaperRepository(),
            defaultAppRepository,
            getInstalledApps,
            SelectPreviewAppsUseCase(),
            CopyFacetSettingsUseCase(placementRepository, facetRepository),
        )
    }

    @Test
    fun `favorites flow through to uiState`() = runTest {
        val twoFavorites = (1..2).map(::appInfo).map { PlacedItem.SingleApp(it) }
        val viewModel = createViewModel(facetId = 5L, facets = listOf(Facet(id = 5L, name = "Work", position = 0)), favorites = twoFavorites)
        backgroundScope.launch { viewModel.uiState.collect {} }
        testDispatcher.scheduler.advanceUntilIdle()

        assertEquals(2, viewModel.uiState.value.favorites.size)
    }

    @Test
    fun `a favorited folder is counted and shows up in uiState, not just apps`() = runTest {
        val folder = com.facetlauncher.app.data.model.Folder(id = 1L, name = "Games", apps = emptyList())
        val favorites = listOf(PlacedItem.SingleApp(appInfo(1)), PlacedItem.FolderItem(folder))
        val viewModel = createViewModel(facetId = 5L, facets = listOf(Facet(id = 5L, name = "Work", position = 0)), favorites = favorites)
        backgroundScope.launch { viewModel.uiState.collect {} }
        testDispatcher.scheduler.advanceUntilIdle()

        assertEquals(2, viewModel.uiState.value.favorites.size)
        assertEquals("2 of ${AppListLimits.MAX_FAVORITES}", viewModel.uiState.value.favoritesLabel)
        assertEquals(favorites, viewModel.uiState.value.previewApps)
    }

    @Test
    fun `appRowPosition and appRowPresentation are the facet's own values`() = runTest {
        // A facet with explicit look values (set from Dock & Apps layout) shows them here —
        // the preview needs them, even though this screen does not edit them.
        val facet = Facet(
            id = 7L, name = "Work", position = 0,
            appRowPosition = AppRowPosition.RIGHT, appRowPresentation = AppRowPresentation.TEXT_ONLY,
            appListVerticalAlignment = AppListVerticalAlignment.TOP,
        )
        val viewModel = createViewModel(facetId = 7L, facets = listOf(facet))
        backgroundScope.launch { viewModel.uiState.collect {} }
        testDispatcher.scheduler.advanceUntilIdle()

        assertEquals(AppRowPosition.RIGHT, viewModel.uiState.value.appRowPosition)
        assertEquals(AppRowPresentation.TEXT_ONLY, viewModel.uiState.value.appRowPresentation)
        assertEquals(AppListVerticalAlignment.TOP, viewModel.uiState.value.appListVerticalAlignment)
    }

    @Test
    fun `launcher scope has no favorites list to reorder`() = runTest {
        val placementRepository = mock(PlacementRepository::class.java)
        val viewModel = createViewModel(placementRepository = placementRepository)

        viewModel.reorderFavorites(listOf(PlacedItem.SingleApp(appInfo(1))))
        testDispatcher.scheduler.advanceUntilIdle()

        org.mockito.Mockito.verifyNoInteractions(placementRepository)
    }

    @Test
    fun `facet-scoped reorderFavorites hits that facet's list, folders included`() = runTest {
        val placementRepository = mock(PlacementRepository::class.java)
        val viewModel = createViewModel(facetId = 7L, placementRepository = placementRepository)
        val folder = com.facetlauncher.app.data.model.Folder(id = 1L, name = "Games", apps = emptyList())
        val reordered = listOf(PlacedItem.FolderItem(folder), PlacedItem.SingleApp(appInfo(1)))

        viewModel.reorderFavorites(reordered)
        testDispatcher.scheduler.advanceUntilIdle()

        verify(placementRepository).reorderItems(PlacementTarget(PlacementList.FAVORITES, 7L), reordered)
    }

    @Test
    fun `facet-scoped look setters write to that facet's row, not SettingsRepository`() = runTest {
        val settingsRepository = mock(SettingsRepository::class.java)
        val facetRepository = mock(FacetRepository::class.java)
        val facet = Facet(id = 7L, name = "Work", position = 0)
        `when`(facetRepository.getById(7L)).thenReturn(facet)
        val viewModel = createViewModel(
            facetId = 7L,
            facets = listOf(facet),
            settingsRepository = settingsRepository,
            facetRepository = facetRepository,
        )

        viewModel.setAppRowPosition(AppRowPosition.RIGHT)
        testDispatcher.scheduler.advanceUntilIdle()

        verify(facetRepository).setAppRowPosition(facet.id, AppRowPosition.RIGHT)
        verify(settingsRepository, org.mockito.Mockito.never()).setAppRowPosition(AppRowPosition.RIGHT)
    }

    @Test
    fun `a null landscape choice clears the field, back to same as portrait`() = runTest {
        val facetRepository = mock(FacetRepository::class.java)
        val facet = Facet(id = 7L, name = "Work", position = 0)
        `when`(facetRepository.getById(7L)).thenReturn(facet)
        val viewModel = createViewModel(facetId = 7L, facets = listOf(facet), facetRepository = facetRepository)

        viewModel.setLandscapeAppRowPosition(null)
        testDispatcher.scheduler.advanceUntilIdle()

        val captor = LandscapeTransformCaptor()
        verify(facetRepository).updateLandscape(eqOf(facet.id), captor.capture())
        assertEquals(LandscapeOverrides(), captor.last(LandscapeOverrides(appRowPosition = AppRowPosition.RIGHT)))
    }

    @Test
    fun `facet-scoped landscape alignment setters write that facet's landscape group`() = runTest {
        val settingsRepository = mock(SettingsRepository::class.java)
        val facetRepository = mock(FacetRepository::class.java)
        val facet = Facet(id = 7L, name = "Work", position = 0)
        `when`(facetRepository.getById(7L)).thenReturn(facet)
        val viewModel = createViewModel(facetId = 7L, facets = listOf(facet), settingsRepository = settingsRepository, facetRepository = facetRepository)

        viewModel.setLandscapeAppRowPosition(AppRowPosition.RIGHT)
        testDispatcher.scheduler.advanceUntilIdle()

        val captor = LandscapeTransformCaptor()
        verify(facetRepository).updateLandscape(eqOf(facet.id), captor.capture())
        assertEquals(AppRowPosition.RIGHT, captor.last(LandscapeOverrides()).appRowPosition)
        verify(settingsRepository, org.mockito.Mockito.never()).updateLandscape(LandscapeTransformCaptor().capture())
    }

    @Test
    fun `the landscape rows show this scope's own landscape values, null meaning same as portrait`() = runTest {
        val facet = Facet(id = 9L, name = "Work", position = 0, landscapeAppRowPosition = AppRowPosition.CENTER)
        val viewModel = createViewModel(
            facetId = 9L,
            facets = listOf(facet),
            settings = LauncherSettings(landscape = LandscapeOverrides(appListVerticalAlignment = AppListVerticalAlignment.TOP)),
        )
        backgroundScope.launch { viewModel.uiState.collect {} }
        testDispatcher.scheduler.advanceUntilIdle()

        // Then a facet shows its own landscape values, not the launcher's
        assertEquals(AppRowPosition.CENTER, viewModel.uiState.value.landscape.appRowPosition)
        assertEquals(null, viewModel.uiState.value.landscape.appListVerticalAlignment)
    }

    @Test
    fun `copy from facet copies the source favorites onto this facet and lists the other facets`() = runTest {
        val placementRepository = mock(PlacementRepository::class.java)
        val facets = listOf(Facet(id = 5L, name = "Work", position = 0), Facet(id = 6L, name = "Play", position = 1))
        val viewModel = createViewModel(facetId = 5L, facets = facets, placementRepository = placementRepository)
        backgroundScope.launch { viewModel.uiState.collect {} }
        testDispatcher.scheduler.advanceUntilIdle()
        assertEquals(listOf(6L), viewModel.uiState.value.otherFacets.map { it.id })

        viewModel.copyFromFacet(6L)
        testDispatcher.scheduler.advanceUntilIdle()

        verify(placementRepository).copyList(PlacementTarget(PlacementList.FAVORITES, 6L), PlacementTarget(PlacementList.FAVORITES, 5L))
    }
}
