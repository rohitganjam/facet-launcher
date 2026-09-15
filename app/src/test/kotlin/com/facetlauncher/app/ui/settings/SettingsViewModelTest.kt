package com.facetlauncher.app.ui.settings

import com.facetlauncher.app.data.DefaultFavoriteAppRepository
import com.facetlauncher.app.data.DefaultLauncherRepository
import com.facetlauncher.app.data.DockAppRepository
import com.facetlauncher.app.data.FolderRepository
import com.facetlauncher.app.data.SettingsRepository
import com.facetlauncher.app.data.WorkProfileRepository
import com.facetlauncher.app.data.model.AppInfo
import com.facetlauncher.app.data.model.LauncherSettings
import com.facetlauncher.app.data.model.PlacedItem
import com.facetlauncher.app.domain.ObserveSettingsScreenStateUseCase
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
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito.mock
import org.mockito.Mockito.`when`

/** [SettingsViewModel] is purely observational now — every mutable section lives in its own screen/ViewModel — so this only exercises the summary state it composes for the main list's rows. */
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
        defaultFavorites: List<AppInfo> = emptyList(),
        folderCount: Int = 0,
        isDefaultLauncher: Boolean = false,
        settings: LauncherSettings = LauncherSettings(),
        hasWorkProfile: Boolean = false,
        isWorkProfilePaused: Boolean = false,
    ): SettingsViewModel {
        val settingsRepository = mock(SettingsRepository::class.java)
        `when`(settingsRepository.settings).thenReturn(flowOf(settings))
        val dockAppRepository = mock(DockAppRepository::class.java)
        `when`(dockAppRepository.observeDockItems()).thenReturn(flowOf(dockApps.map { PlacedItem.SingleApp(it) }))
        val defaultFavoriteAppRepository = mock(DefaultFavoriteAppRepository::class.java)
        `when`(defaultFavoriteAppRepository.observeDefaultItems()).thenReturn(flowOf(defaultFavorites.map { PlacedItem.SingleApp(it) }))
        val folderRepository = mock(FolderRepository::class.java)
        `when`(folderRepository.observeFolders()).thenReturn(flowOf((1..folderCount).map { mock(com.facetlauncher.app.data.model.Folder::class.java) }))
        val defaultLauncherRepository = mock(DefaultLauncherRepository::class.java)
        runBlocking { `when`(defaultLauncherRepository.isDefaultLauncher()).thenReturn(isDefaultLauncher) }
        val workProfileRepository = mock(WorkProfileRepository::class.java)
        `when`(workProfileRepository.hasWorkProfile()).thenReturn(flowOf(hasWorkProfile))
        `when`(workProfileRepository.isWorkProfilePaused()).thenReturn(flowOf(isWorkProfilePaused))
        val useCase = ObserveSettingsScreenStateUseCase(settingsRepository, dockAppRepository, defaultFavoriteAppRepository, folderRepository)
        return SettingsViewModel(useCase, defaultLauncherRepository, workProfileRepository)
    }

    @Test
    fun `uiState composes dock apps, default favorites, settings, and default-launcher status`() = runTest {
        val dockApps = (1..3).map(::appInfo)
        val defaultFavorites = (1..2).map(::appInfo)
        val viewModel = createViewModel(dockApps = dockApps, defaultFavorites = defaultFavorites, folderCount = 4, isDefaultLauncher = true)
        backgroundScope.launch { viewModel.uiState.collect {} }
        testDispatcher.scheduler.advanceUntilIdle()

        assertEquals(3, viewModel.uiState.value.dockItems.size)
        assertEquals(2, viewModel.uiState.value.defaultFavorites.size)
        // The folder count reflects the whole library, independent of the dock — see
        // ObserveSettingsScreenStateUseCase's own doc for why this isn't derived from dockItems.
        assertEquals(4, viewModel.uiState.value.folderCount)
        assertEquals(true, viewModel.uiState.value.isDefaultLauncher)
    }

    @Test
    fun `uiState reflects Facet not being the default launcher`() = runTest {
        val viewModel = createViewModel(isDefaultLauncher = false)
        backgroundScope.launch { viewModel.uiState.collect {} }
        testDispatcher.scheduler.advanceUntilIdle()

        assertEquals(false, viewModel.uiState.value.isDefaultLauncher)
    }

    @Test
    fun `selectedCalendarCount is zero when nothing has been explicitly selected yet`() = runTest {
        // `selectedCalendarIds == null` means "never touched" — that's "none decided", not "every calendar"
        val viewModel = createViewModel(settings = LauncherSettings(selectedCalendarIds = null))
        backgroundScope.launch { viewModel.uiState.collect {} }
        testDispatcher.scheduler.advanceUntilIdle()

        assertEquals(0, viewModel.uiState.value.selectedCalendarCount)
    }

    @Test
    fun `selectedCalendarCount reflects an explicit selection`() = runTest {
        val viewModel = createViewModel(settings = LauncherSettings(selectedCalendarIds = setOf("1", "2")))
        backgroundScope.launch { viewModel.uiState.collect {} }
        testDispatcher.scheduler.advanceUntilIdle()

        assertEquals(2, viewModel.uiState.value.selectedCalendarCount)
    }

    @Test
    fun `selectedCalendarCount is zero when every calendar has been explicitly deselected`() = runTest {
        val viewModel = createViewModel(settings = LauncherSettings(selectedCalendarIds = emptySet()))
        backgroundScope.launch { viewModel.uiState.collect {} }
        testDispatcher.scheduler.advanceUntilIdle()

        assertEquals(0, viewModel.uiState.value.selectedCalendarCount)
    }

    @Test
    fun `uiState reflects no Work Profile by default`() = runTest {
        val viewModel = createViewModel()
        backgroundScope.launch { viewModel.uiState.collect {} }
        testDispatcher.scheduler.advanceUntilIdle()

        assertEquals(false, viewModel.uiState.value.hasWorkProfile)
    }

    @Test
    fun `uiState reflects a Work Profile that exists and is paused`() = runTest {
        val viewModel = createViewModel(hasWorkProfile = true, isWorkProfilePaused = true)
        backgroundScope.launch { viewModel.uiState.collect {} }
        testDispatcher.scheduler.advanceUntilIdle()

        assertEquals(true, viewModel.uiState.value.hasWorkProfile)
        assertEquals(true, viewModel.uiState.value.isWorkProfilePaused)
    }
}
