package com.facetlauncher.app.ui.dock

import androidx.lifecycle.SavedStateHandle
import com.facetlauncher.app.data.AppRepository
import com.facetlauncher.app.data.DockAppRepository
import com.facetlauncher.app.data.FacetDockAppRepository
import com.facetlauncher.app.data.FolderRepository
import com.facetlauncher.app.data.UsageAccessRepository
import com.facetlauncher.app.data.UsageStatsRepository
import com.facetlauncher.app.data.model.AppInfo
import com.facetlauncher.app.data.model.AppSortOption
import com.facetlauncher.app.data.model.PlacedItem
import com.facetlauncher.app.data.model.SortDirection
import com.facetlauncher.app.domain.GetInstalledAppsUseCase
import com.facetlauncher.app.domain.SortAppsForPickerUseCase
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
import org.junit.runner.RunWith
import org.mockito.Mockito.mock
import org.mockito.Mockito.`when`
import org.robolectric.RobolectricTestRunner

/**
 * Focused on the sort control + Usage Access gating added on top of the Dock picker —
 * capacity/placement behavior itself is exercised end-to-end by [DockAppRepositoryTest] and the
 * instrumented `DockAppPickerScreenTest`.
 */
@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(RobolectricTestRunner::class)
class DockAppPickerViewModelTest {

    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setUp() { Dispatchers.setMain(testDispatcher) }

    @After
    fun tearDown() { Dispatchers.resetMain() }

    private fun appInfo(letter: Char) =
        AppInfo(packageName = "com.example.$letter", activityName = ".Main", label = "$letter App", icon = null)

    private suspend fun createViewModel(
        installed: List<AppInfo>,
        dockApps: List<AppInfo>,
        usageAccessGranted: Boolean = false,
    ): DockAppPickerViewModel {
        val appRepository = mock(AppRepository::class.java)
        `when`(appRepository.getInstalledApps()).thenReturn(installed)
        val getInstalledApps = GetInstalledAppsUseCase(appRepository)

        val dockAppRepository = mock(DockAppRepository::class.java)
        `when`(dockAppRepository.observeDockItems()).thenReturn(flowOf(dockApps.map { PlacedItem.SingleApp(it) }))
        val facetDockAppRepository = mock(FacetDockAppRepository::class.java)

        val folderRepository = mock(FolderRepository::class.java)
        `when`(folderRepository.observeFolders()).thenReturn(flowOf(emptyList()))

        val usageAccessRepository = mock(UsageAccessRepository::class.java)
        `when`(usageAccessRepository.isGranted()).thenReturn(usageAccessGranted)
        val usageStatsRepository = mock(UsageStatsRepository::class.java)
        `when`(usageStatsRepository.getLastUsedTimestamps()).thenReturn(emptyMap())
        val sortAppsForPicker = SortAppsForPickerUseCase(usageStatsRepository)

        return DockAppPickerViewModel(
            SavedStateHandle(),
            getInstalledApps,
            dockAppRepository,
            facetDockAppRepository,
            folderRepository,
            sortAppsForPicker,
            usageAccessRepository,
        )
    }

    @Test
    fun `onSortOptionChanged reorders otherResults but never selectedResults`() = runTest {
        val a = appInfo('a')
        val b = appInfo('b')
        val c = appInfo('c')
        val viewModel = createViewModel(installed = listOf(a, b, c), dockApps = listOf(b, a))
        backgroundScope.launch { viewModel.uiState.collect {} }
        testDispatcher.scheduler.advanceUntilIdle()
        assertEquals(listOf(b, a), viewModel.uiState.value.selectedResults)
        assertEquals(listOf(c), viewModel.uiState.value.otherResults)

        viewModel.onSortDirectionToggled()
        testDispatcher.scheduler.advanceUntilIdle()

        assertEquals(SortDirection.DESCENDING, viewModel.uiState.value.sortDirection)
        assertEquals(listOf(b, a), viewModel.uiState.value.selectedResults)
    }

    @Test
    fun `onSortOptionChanged to LAST_USED is a no-op without Usage Access granted`() = runTest {
        val a = appInfo('a')
        val viewModel = createViewModel(installed = listOf(a), dockApps = emptyList(), usageAccessGranted = false)
        backgroundScope.launch { viewModel.uiState.collect {} }
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.onSortOptionChanged(AppSortOption.LAST_USED)
        testDispatcher.scheduler.advanceUntilIdle()

        assertEquals(AppSortOption.ALPHABETICAL, viewModel.uiState.value.sortOption)
    }

    @Test
    fun `onSortOptionChanged to LAST_USED applies once Usage Access is granted`() = runTest {
        val a = appInfo('a')
        val viewModel = createViewModel(installed = listOf(a), dockApps = emptyList(), usageAccessGranted = true)
        backgroundScope.launch { viewModel.uiState.collect {} }
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.onSortOptionChanged(AppSortOption.LAST_USED)
        testDispatcher.scheduler.advanceUntilIdle()

        assertEquals(AppSortOption.LAST_USED, viewModel.uiState.value.sortOption)
    }
}
