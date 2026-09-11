package com.lumenlauncher.app.ui.onboarding

import com.lumenlauncher.app.data.DefaultFavoriteAppRepository
import com.lumenlauncher.app.data.DefaultLauncherRepository
import com.lumenlauncher.app.data.DockAppRepository
import com.lumenlauncher.app.data.SettingsRepository
import com.lumenlauncher.app.data.WallpaperRepository
import com.lumenlauncher.app.data.model.AppInfo
import com.lumenlauncher.app.data.model.HomeWallpaper
import com.lumenlauncher.app.data.model.LauncherSettings
import com.lumenlauncher.app.data.model.ListContentMode
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runCurrent
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
class OnboardingViewModelTest {

    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setUp() { Dispatchers.setMain(testDispatcher) }

    @After
    fun tearDown() { Dispatchers.resetMain() }

    private fun appInfo(name: String) = AppInfo("com.example.$name", ".Main", name, icon = null)

    /** A real (not Mockito) stub — plain Mockito can't reliably stub a `suspend` return, same reasoning as `DockSettingsViewModelTest`. */
    private fun fakeWallpaperRepository() =
        object : WallpaperRepository(mock(android.app.WallpaperManager::class.java)) {
            override suspend fun currentHomeWallpaper(): HomeWallpaper = HomeWallpaper.Unavailable
        }

    private class Fixture(
        val favorites: MutableStateFlow<List<AppInfo>>,
        val dockApps: MutableStateFlow<List<AppInfo>>,
        val defaultFavoriteAppRepository: DefaultFavoriteAppRepository,
        val dockAppRepository: DockAppRepository,
        val settingsRepository: SettingsRepository,
        val viewModel: OnboardingViewModel,
    )

    private suspend fun fixture(
        favorites: List<AppInfo> = emptyList(),
        dockApps: List<AppInfo> = emptyList(),
        settings: LauncherSettings = LauncherSettings(),
    ): Fixture {
        val favoritesFlow = MutableStateFlow(favorites)
        val dockFlow = MutableStateFlow(dockApps)
        val defaultFavoriteAppRepository = mock(DefaultFavoriteAppRepository::class.java)
        `when`(defaultFavoriteAppRepository.observeDefaultFavorites()).thenReturn(favoritesFlow)
        val dockAppRepository = mock(DockAppRepository::class.java)
        `when`(dockAppRepository.observeDockApps()).thenReturn(dockFlow)
        val settingsRepository = mock(SettingsRepository::class.java)
        `when`(settingsRepository.settings).thenReturn(MutableStateFlow(settings))
        val defaultLauncherRepository = mock(DefaultLauncherRepository::class.java)
        `when`(defaultLauncherRepository.isDefaultLauncher()).thenReturn(false)
        val viewModel = OnboardingViewModel(
            defaultFavoriteAppRepository,
            dockAppRepository,
            settingsRepository,
            fakeWallpaperRepository(),
            defaultLauncherRepository,
        )
        testDispatcher.scheduler.advanceUntilIdle()
        return Fixture(favoritesFlow, dockFlow, defaultFavoriteAppRepository, dockAppRepository, settingsRepository, viewModel)
    }

    @Test
    fun `starts from the live favorites and dock`() = runTest {
        // Given an already-seeded dock and one existing favorite
        val browser = appInfo("browser")
        val messages = appInfo("messages")
        val f = fixture(favorites = listOf(messages), dockApps = listOf(browser))
        backgroundScope.launch { f.viewModel.uiState.collect {} }
        runCurrent()
        testDispatcher.scheduler.advanceUntilIdle()

        // Then the ui state reflects both live lists
        assertEquals(listOf(messages), f.viewModel.uiState.value.favoriteApps)
        assertEquals(listOf(browser), f.viewModel.uiState.value.dockApps)
    }

    @Test
    fun `uiState reflects the current list content mode`() = runTest {
        // Given settings already set to Most used
        val f = fixture(settings = LauncherSettings(listContentMode = ListContentMode.MOST_USED))
        backgroundScope.launch { f.viewModel.uiState.collect {} }
        runCurrent()
        testDispatcher.scheduler.advanceUntilIdle()

        assertEquals(ListContentMode.MOST_USED, f.viewModel.uiState.value.listContentMode)
    }

    @Test
    fun `setListContentMode delegates to the settings repository`() = runTest {
        val f = fixture()
        backgroundScope.launch { f.viewModel.uiState.collect {} }
        runCurrent()

        f.viewModel.setListContentMode(ListContentMode.RECENTS)
        testDispatcher.scheduler.advanceUntilIdle()

        verify(f.settingsRepository).setListContentMode(ListContentMode.RECENTS)
    }

    @Test
    fun `uiState reflects the current apps-to-show count`() = runTest {
        // Given settings already set to a non-default count
        val f = fixture(settings = LauncherSettings(appsToShowCount = 4))
        backgroundScope.launch { f.viewModel.uiState.collect {} }
        runCurrent()
        testDispatcher.scheduler.advanceUntilIdle()

        assertEquals(4, f.viewModel.uiState.value.appsToShowCount)
    }

    @Test
    fun `setAppsToShowCount delegates to the settings repository`() = runTest {
        val f = fixture()
        backgroundScope.launch { f.viewModel.uiState.collect {} }
        runCurrent()

        f.viewModel.setAppsToShowCount(4)
        testDispatcher.scheduler.advanceUntilIdle()

        verify(f.settingsRepository).setAppsToShowCount(4)
    }

    @Test
    fun `reorderDockApps delegates to the dock repository`() = runTest {
        val apps = listOf(appInfo("a"), appInfo("b"))
        val f = fixture(dockApps = apps)
        backgroundScope.launch { f.viewModel.uiState.collect {} }
        runCurrent()

        val reordered = apps.reversed()
        f.viewModel.reorderDockApps(reordered)
        testDispatcher.scheduler.advanceUntilIdle()

        verify(f.dockAppRepository).reorderDockApps(reordered)
    }

    @Test
    fun `reorderFavorites delegates to the default favorite repository`() = runTest {
        val apps = listOf(appInfo("a"), appInfo("b"))
        val f = fixture(favorites = apps)
        backgroundScope.launch { f.viewModel.uiState.collect {} }
        runCurrent()

        val reordered = apps.reversed()
        f.viewModel.reorderFavorites(reordered)
        testDispatcher.scheduler.advanceUntilIdle()

        verify(f.defaultFavoriteAppRepository).reorderFavorites(reordered)
    }
}
