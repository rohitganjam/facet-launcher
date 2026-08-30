package com.lumenlauncher.app.ui.settings

import com.lumenlauncher.app.data.DefaultFavoriteAppRepository
import com.lumenlauncher.app.data.DefaultLauncherRepository
import com.lumenlauncher.app.data.DockAppRepository
import com.lumenlauncher.app.data.SettingsRepository
import com.lumenlauncher.app.data.model.AppInfo
import com.lumenlauncher.app.data.model.LauncherSettings
import com.lumenlauncher.app.domain.ObserveSettingsScreenStateUseCase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify
import org.mockito.Mockito.`when`

@OptIn(ExperimentalCoroutinesApi::class)
class SettingsViewModelTest {

    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun appInfo(n: Int) = AppInfo(packageName = "com.example.$n", activityName = ".Main", label = "App $n", icon = null)

    private fun createViewModel(
        dockApps: List<AppInfo> = emptyList(),
        settingsRepository: SettingsRepository = mock(SettingsRepository::class.java),
        dockAppRepository: DockAppRepository = mock(DockAppRepository::class.java),
    ): SettingsViewModel {
        `when`(settingsRepository.settings).thenReturn(flowOf(LauncherSettings()))
        `when`(dockAppRepository.observeDockApps()).thenReturn(flowOf(dockApps))
        val defaultFavoriteAppRepository = mock(DefaultFavoriteAppRepository::class.java)
        `when`(defaultFavoriteAppRepository.observeDefaultFavorites()).thenReturn(flowOf(emptyList()))
        val defaultLauncherRepository = mock(DefaultLauncherRepository::class.java)
        runBlocking { `when`(defaultLauncherRepository.isDefaultLauncher()).thenReturn(false) }
        val useCase = ObserveSettingsScreenStateUseCase(settingsRepository, dockAppRepository, defaultFavoriteAppRepository)
        return SettingsViewModel(useCase, settingsRepository, dockAppRepository, defaultFavoriteAppRepository, defaultLauncherRepository)
    }

    @Test
    fun `changing drawer presentation calls the repository setter`() = runTest {
        val settingsRepository = mock(SettingsRepository::class.java)
        val viewModel = createViewModel(settingsRepository = settingsRepository)

        viewModel.setDrawerPresentation(com.lumenlauncher.app.data.model.DrawerPresentation.GRID)
        testDispatcher.scheduler.advanceUntilIdle()

        verify(settingsRepository).setDrawerPresentation(com.lumenlauncher.app.data.model.DrawerPresentation.GRID)
    }

    @Test
    fun `changing drawer grid size calls the repository setter`() = runTest {
        val settingsRepository = mock(SettingsRepository::class.java)
        val viewModel = createViewModel(settingsRepository = settingsRepository)

        viewModel.setDrawerGridSize(com.lumenlauncher.app.data.model.DrawerGridSize.FOUR_BY_FOUR)
        testDispatcher.scheduler.advanceUntilIdle()

        verify(settingsRepository).setDrawerGridSize(com.lumenlauncher.app.data.model.DrawerGridSize.FOUR_BY_FOUR)
    }

    @Test
    fun `changing drawer list item size calls the repository setter`() = runTest {
        val settingsRepository = mock(SettingsRepository::class.java)
        val viewModel = createViewModel(settingsRepository = settingsRepository)

        viewModel.setDrawerListItemSize(com.lumenlauncher.app.data.model.DrawerListItemSize.SPACIOUS)
        testDispatcher.scheduler.advanceUntilIdle()

        verify(settingsRepository).setDrawerListItemSize(com.lumenlauncher.app.data.model.DrawerListItemSize.SPACIOUS)
    }

    @Test
    fun `changing icon render mode calls the repository setter`() = runTest {
        val settingsRepository = mock(SettingsRepository::class.java)
        val viewModel = createViewModel(settingsRepository = settingsRepository)

        viewModel.setIconRenderMode(com.lumenlauncher.app.data.model.IconRenderMode.MONOCHROME_BLACK_WHITE)
        testDispatcher.scheduler.advanceUntilIdle()

        verify(settingsRepository).setIconRenderMode(com.lumenlauncher.app.data.model.IconRenderMode.MONOCHROME_BLACK_WHITE)
    }

    @Test
    fun `toggling show drawer labels calls the repository setter`() = runTest {
        val settingsRepository = mock(SettingsRepository::class.java)
        val viewModel = createViewModel(settingsRepository = settingsRepository)

        viewModel.setShowDrawerLabels(false)
        testDispatcher.scheduler.advanceUntilIdle()

        verify(settingsRepository).setShowDrawerLabels(false)
    }

    @Test
    fun `dock cannot accept more apps once at the maximum`() = runTest {
        val fiveApps = (1..5).map(::appInfo)
        val viewModel = createViewModel(dockApps = fiveApps)
        backgroundScope.launch { viewModel.uiState.collect {} }
        testDispatcher.scheduler.advanceUntilIdle()

        assert(!viewModel.canAddMoreDockApps())
    }
}
