package com.lumenlauncher.app.ui.settings

import com.lumenlauncher.app.data.DefaultFavoriteAppRepository
import com.lumenlauncher.app.data.SettingsRepository
import com.lumenlauncher.app.data.model.AppInfo
import com.lumenlauncher.app.data.model.AppListVerticalAlignment
import com.lumenlauncher.app.data.model.AppRowPosition
import com.lumenlauncher.app.data.model.AppRowPresentation
import com.lumenlauncher.app.data.model.LauncherSettings
import com.lumenlauncher.app.data.model.ListContentMode
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
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
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify
import org.mockito.Mockito.`when`

@OptIn(ExperimentalCoroutinesApi::class)
class HomeAppsListSettingsViewModelTest {

    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setUp() { Dispatchers.setMain(testDispatcher) }

    @After
    fun tearDown() { Dispatchers.resetMain() }

    private fun appInfo(n: Int) = AppInfo(packageName = "com.example.$n", activityName = ".Main", label = "App $n", icon = null)

    private fun createViewModel(
        defaultFavorites: List<AppInfo> = emptyList(),
        settingsRepository: SettingsRepository = mock(SettingsRepository::class.java),
        defaultFavoriteAppRepository: DefaultFavoriteAppRepository = mock(DefaultFavoriteAppRepository::class.java),
    ): HomeAppsListSettingsViewModel {
        `when`(settingsRepository.settings).thenReturn(flowOf(LauncherSettings()))
        `when`(defaultFavoriteAppRepository.observeDefaultFavorites()).thenReturn(flowOf(defaultFavorites))
        return HomeAppsListSettingsViewModel(settingsRepository, defaultFavoriteAppRepository)
    }

    @Test
    fun `default favorites flow through to uiState`() = runTest {
        val twoFavorites = (1..2).map(::appInfo)
        val viewModel = createViewModel(defaultFavorites = twoFavorites)
        backgroundScope.launch { viewModel.uiState.collect {} }
        testDispatcher.scheduler.advanceUntilIdle()

        assertEquals(2, viewModel.uiState.value.defaultFavorites.size)
    }

    @Test
    fun `changing app row position calls the repository setter`() = runTest {
        val settingsRepository = mock(SettingsRepository::class.java)
        val viewModel = createViewModel(settingsRepository = settingsRepository)

        viewModel.setAppRowPosition(AppRowPosition.RIGHT)
        testDispatcher.scheduler.advanceUntilIdle()

        verify(settingsRepository).setAppRowPosition(AppRowPosition.RIGHT)
    }

    @Test
    fun `changing app row presentation calls the repository setter`() = runTest {
        val settingsRepository = mock(SettingsRepository::class.java)
        val viewModel = createViewModel(settingsRepository = settingsRepository)

        viewModel.setAppRowPresentation(AppRowPresentation.TEXT_ONLY)
        testDispatcher.scheduler.advanceUntilIdle()

        verify(settingsRepository).setAppRowPresentation(AppRowPresentation.TEXT_ONLY)
    }

    @Test
    fun `changing list content mode calls the repository setter`() = runTest {
        val settingsRepository = mock(SettingsRepository::class.java)
        val viewModel = createViewModel(settingsRepository = settingsRepository)

        viewModel.setListContentMode(ListContentMode.RECENTS)
        testDispatcher.scheduler.advanceUntilIdle()

        verify(settingsRepository).setListContentMode(ListContentMode.RECENTS)
    }

    @Test
    fun `changing apps to show count calls the repository setter`() = runTest {
        val settingsRepository = mock(SettingsRepository::class.java)
        val viewModel = createViewModel(settingsRepository = settingsRepository)

        viewModel.setAppsToShowCount(7)
        testDispatcher.scheduler.advanceUntilIdle()

        verify(settingsRepository).setAppsToShowCount(7)
    }

    @Test
    fun `changing app list vertical alignment calls the repository setter`() = runTest {
        val settingsRepository = mock(SettingsRepository::class.java)
        val viewModel = createViewModel(settingsRepository = settingsRepository)

        viewModel.setAppListVerticalAlignment(AppListVerticalAlignment.TOP)
        testDispatcher.scheduler.advanceUntilIdle()

        verify(settingsRepository).setAppListVerticalAlignment(AppListVerticalAlignment.TOP)
    }

    @Test
    fun `reordering default favorites calls the repository`() = runTest {
        val defaultFavoriteAppRepository = mock(DefaultFavoriteAppRepository::class.java)
        val viewModel = createViewModel(defaultFavoriteAppRepository = defaultFavoriteAppRepository)
        val reordered = listOf(appInfo(2), appInfo(1))

        viewModel.reorderDefaultFavorites(reordered)
        testDispatcher.scheduler.advanceUntilIdle()

        verify(defaultFavoriteAppRepository).reorderFavorites(reordered)
    }
}
