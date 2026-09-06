package com.lumenlauncher.app.ui.settings

import com.lumenlauncher.app.data.DockAppRepository
import com.lumenlauncher.app.data.SettingsRepository
import com.lumenlauncher.app.data.model.AppInfo
import com.lumenlauncher.app.data.model.DockDisplayMode
import com.lumenlauncher.app.data.model.LauncherSettings
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
class DockSettingsViewModelTest {

    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setUp() { Dispatchers.setMain(testDispatcher) }

    @After
    fun tearDown() { Dispatchers.resetMain() }

    private fun appInfo(n: Int) = AppInfo(packageName = "com.example.$n", activityName = ".Main", label = "App $n", icon = null)

    private fun createViewModel(
        dockApps: List<AppInfo> = emptyList(),
        settingsRepository: SettingsRepository = mock(SettingsRepository::class.java),
        dockAppRepository: DockAppRepository = mock(DockAppRepository::class.java),
    ): DockSettingsViewModel {
        `when`(settingsRepository.settings).thenReturn(flowOf(LauncherSettings()))
        `when`(dockAppRepository.observeDockApps()).thenReturn(flowOf(dockApps))
        return DockSettingsViewModel(settingsRepository, dockAppRepository)
    }

    @Test
    fun `dock apps flow through to uiState`() = runTest {
        val fiveApps = (1..5).map(::appInfo)
        val viewModel = createViewModel(dockApps = fiveApps)
        backgroundScope.launch { viewModel.uiState.collect {} }
        testDispatcher.scheduler.advanceUntilIdle()

        assertEquals(5, viewModel.uiState.value.dockApps.size)
    }

    @Test
    fun `changing dock display mode calls the repository setter`() = runTest {
        val settingsRepository = mock(SettingsRepository::class.java)
        val viewModel = createViewModel(settingsRepository = settingsRepository)

        viewModel.setDockDisplayMode(DockDisplayMode.TEXT)
        testDispatcher.scheduler.advanceUntilIdle()

        verify(settingsRepository).setDockDisplayMode(DockDisplayMode.TEXT)
    }

    @Test
    fun `reordering dock apps calls the repository`() = runTest {
        val dockAppRepository = mock(DockAppRepository::class.java)
        val viewModel = createViewModel(dockAppRepository = dockAppRepository)
        val reordered = listOf(appInfo(2), appInfo(1))

        viewModel.reorderDockApps(reordered)
        testDispatcher.scheduler.advanceUntilIdle()

        verify(dockAppRepository).reorderDockApps(reordered)
    }
}
