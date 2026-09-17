package com.facetlauncher.app.ui.settings

import androidx.lifecycle.SavedStateHandle
import com.facetlauncher.app.data.DefaultAppRepository
import com.facetlauncher.app.data.DefaultFavoriteAppRepository
import com.facetlauncher.app.data.FavoriteAppRepository
import com.facetlauncher.app.data.FacetRepository
import com.facetlauncher.app.data.SettingsRepository
import com.facetlauncher.app.data.WallpaperRepository
import com.facetlauncher.app.data.local.FacetEntity
import com.facetlauncher.app.data.model.AppInfo
import com.facetlauncher.app.data.model.AppListVerticalAlignment
import com.facetlauncher.app.data.model.AppRowPosition
import com.facetlauncher.app.data.model.AppRowPresentation
import com.facetlauncher.app.data.model.HomeWallpaper
import com.facetlauncher.app.data.model.LauncherSettings
import com.facetlauncher.app.data.model.ListContentMode
import com.facetlauncher.app.data.model.PlacedItem
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
import org.mockito.Mockito.never
import org.mockito.Mockito.verify
import org.mockito.Mockito.`when`
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

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
        facets: List<FacetEntity> = emptyList(),
        settingsRepository: SettingsRepository = mock(SettingsRepository::class.java),
        facetRepository: FacetRepository = mock(FacetRepository::class.java),
        favoriteAppRepository: FavoriteAppRepository = mock(FavoriteAppRepository::class.java),
        defaultFavoriteAppRepository: DefaultFavoriteAppRepository = mock(DefaultFavoriteAppRepository::class.java),
    ): HomeAppsListSettingsViewModel {
        val getInstalledApps = mock(GetInstalledAppsUseCase::class.java)
        val defaultAppRepository = mock(DefaultAppRepository::class.java)
        `when`(settingsRepository.settings).thenReturn(flowOf(LauncherSettings()))
        `when`(facetRepository.observeFacets()).thenReturn(MutableStateFlow(facets))
        `when`(favoriteAppRepository.observeFavoriteItems(anyLong())).thenReturn(flowOf(favorites))
        `when`(defaultFavoriteAppRepository.observeDefaultItems()).thenReturn(flowOf(favorites))
        `when`(getInstalledApps.observe()).thenReturn(flowOf(emptyList()))
        `when`(defaultAppRepository.getDefaultAppPackages()).thenReturn(emptyList())
        return HomeAppsListSettingsViewModel(
            SavedStateHandle(facetId?.let { mapOf("facetId" to it) } ?: emptyMap()),
            settingsRepository,
            facetRepository,
            favoriteAppRepository,
            defaultFavoriteAppRepository,
            fakeWallpaperRepository(),
            defaultAppRepository,
            getInstalledApps,
            SelectPreviewAppsUseCase(),
        )
    }

    @Test
    fun `favorites flow through to uiState`() = runTest {
        val twoFavorites = (1..2).map(::appInfo).map { PlacedItem.SingleApp(it) }
        val viewModel = createViewModel(favorites = twoFavorites)
        backgroundScope.launch { viewModel.uiState.collect {} }
        testDispatcher.scheduler.advanceUntilIdle()

        assertEquals(2, viewModel.uiState.value.favorites.size)
    }

    @Test
    fun `a favorited folder is counted and shows up in uiState, not just apps`() = runTest {
        val folder = com.facetlauncher.app.data.model.Folder(id = 1L, name = "Games", apps = emptyList())
        val favorites = listOf(PlacedItem.SingleApp(appInfo(1)), PlacedItem.FolderItem(folder))
        val viewModel = createViewModel(favorites = favorites)
        backgroundScope.launch { viewModel.uiState.collect {} }
        testDispatcher.scheduler.advanceUntilIdle()

        assertEquals(2, viewModel.uiState.value.favorites.size)
        assertEquals("2 of ${DefaultFavoriteAppRepository.MAX_FAVORITES}", viewModel.uiState.value.favoritesLabel)
        assertEquals(favorites, viewModel.uiState.value.previewApps)
    }

    @Test
    fun `global-scoped setter writes to SettingsRepository`() = runTest {
        val settingsRepository = mock(SettingsRepository::class.java)
        val viewModel = createViewModel(settingsRepository = settingsRepository)

        viewModel.setAppRowPosition(AppRowPosition.RIGHT)
        viewModel.setAppRowPresentation(AppRowPresentation.TEXT_ONLY)
        viewModel.setListContentMode(ListContentMode.RECENTS)
        viewModel.setAppsToShowCount(7)
        viewModel.setAppListVerticalAlignment(AppListVerticalAlignment.TOP)
        testDispatcher.scheduler.advanceUntilIdle()

        verify(settingsRepository).setAppRowPosition(AppRowPosition.RIGHT)
        verify(settingsRepository).setAppRowPresentation(AppRowPresentation.TEXT_ONLY)
        verify(settingsRepository).setListContentMode(ListContentMode.RECENTS)
        verify(settingsRepository).setAppsToShowCount(7)
        verify(settingsRepository).setAppListVerticalAlignment(AppListVerticalAlignment.TOP)
    }

    @Test
    fun `facet-scoped setter writes to that facet's row, not SettingsRepository`() = runTest {
        val settingsRepository = mock(SettingsRepository::class.java)
        val facetRepository = mock(FacetRepository::class.java)
        val facet = FacetEntity(id = 7L, name = "Work", position = 0)
        `when`(facetRepository.getById(7L)).thenReturn(facet)
        val viewModel = createViewModel(
            facetId = 7L,
            facets = listOf(facet),
            settingsRepository = settingsRepository,
            facetRepository = facetRepository,
        )

        viewModel.setAppRowPosition(AppRowPosition.RIGHT)
        testDispatcher.scheduler.advanceUntilIdle()

        verify(facetRepository).setAppRowPosition(facet, AppRowPosition.RIGHT)
        verify(settingsRepository, never()).setAppRowPosition(AppRowPosition.RIGHT)
    }

    @Test
    fun `global reorderFavorites hits the default list, folders included`() = runTest {
        val defaultFavoriteAppRepository = mock(DefaultFavoriteAppRepository::class.java)
        val viewModel = createViewModel(defaultFavoriteAppRepository = defaultFavoriteAppRepository)
        val folder = com.facetlauncher.app.data.model.Folder(id = 1L, name = "Games", apps = emptyList())
        val reordered = listOf(PlacedItem.FolderItem(folder), PlacedItem.SingleApp(appInfo(1)))

        viewModel.reorderFavorites(reordered)
        testDispatcher.scheduler.advanceUntilIdle()

        verify(defaultFavoriteAppRepository).reorderItems(reordered)
    }

    @Test
    fun `facet-scoped reorderFavorites hits that facet's list, folders included`() = runTest {
        val favoriteAppRepository = mock(FavoriteAppRepository::class.java)
        val viewModel = createViewModel(facetId = 7L, favoriteAppRepository = favoriteAppRepository)
        val folder = com.facetlauncher.app.data.model.Folder(id = 1L, name = "Games", apps = emptyList())
        val reordered = listOf(PlacedItem.FolderItem(folder), PlacedItem.SingleApp(appInfo(1)))

        viewModel.reorderFavorites(reordered)
        testDispatcher.scheduler.advanceUntilIdle()

        verify(favoriteAppRepository).reorderFavoriteItems(7L, reordered)
    }
}
