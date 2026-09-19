package com.facetlauncher.app.ui.facets

import androidx.lifecycle.SavedStateHandle
import com.facetlauncher.app.data.AppRepository
import com.facetlauncher.app.data.DefaultFavoriteAppRepository
import com.facetlauncher.app.data.FavoriteAppRepository
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
 * Focused on the sort control + Usage Access gating added on top of the Favorites picker —
 * capacity/placement behavior itself is exercised end-to-end by [FavoriteAppRepositoryTest] and
 * the instrumented `FavoritesPickerScreenTest`.
 */
@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(RobolectricTestRunner::class)
class FavoritesPickerViewModelTest {

    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setUp() { Dispatchers.setMain(testDispatcher) }

    @After
    fun tearDown() { Dispatchers.resetMain() }

    private fun appInfo(letter: Char) =
        AppInfo(packageName = "com.example.$letter", activityName = ".Main", label = "$letter App", icon = null)

    private suspend fun createViewModel(
        facetId: Long,
        installed: List<AppInfo>,
        favorites: List<AppInfo>,
        usageAccessGranted: Boolean = false,
    ): FavoritesPickerViewModel {
        val appRepository = mock(AppRepository::class.java)
        `when`(appRepository.getInstalledApps()).thenReturn(installed)
        val getInstalledApps = GetInstalledAppsUseCase(appRepository)

        val favoriteAppRepository = mock(FavoriteAppRepository::class.java)
        `when`(favoriteAppRepository.observeFavoriteItems(facetId))
            .thenReturn(flowOf(favorites.map { PlacedItem.SingleApp(it) }))
        val defaultFavoriteAppRepository = mock(DefaultFavoriteAppRepository::class.java)
        `when`(defaultFavoriteAppRepository.observeDefaultItems()).thenReturn(flowOf(emptyList()))

        val folderRepository = mock(FolderRepository::class.java)
        `when`(folderRepository.observeFolders()).thenReturn(flowOf(emptyList()))

        val usageAccessRepository = mock(UsageAccessRepository::class.java)
        `when`(usageAccessRepository.isGranted()).thenReturn(usageAccessGranted)
        val usageStatsRepository = mock(UsageStatsRepository::class.java)
        `when`(usageStatsRepository.getLastUsedTimestamps()).thenReturn(emptyMap())
        val sortAppsForPicker = SortAppsForPickerUseCase(usageStatsRepository)

        return FavoritesPickerViewModel(
            SavedStateHandle(mapOf("facetId" to facetId)),
            getInstalledApps,
            favoriteAppRepository,
            defaultFavoriteAppRepository,
            folderRepository,
            sortAppsForPicker,
            usageAccessRepository,
        )
    }

    @Test
    fun `onSortOptionChange reorders otherResults but never selectedResults`() = runTest {
        val a = appInfo('a')
        val b = appInfo('b')
        val c = appInfo('c')
        val viewModel = createViewModel(facetId = 1L, installed = listOf(a, b, c), favorites = listOf(b, a))
        backgroundScope.launch { viewModel.uiState.collect {} }
        testDispatcher.scheduler.advanceUntilIdle()
        assertEquals(listOf(b, a), viewModel.uiState.value.selectedResults)
        assertEquals(listOf(c), viewModel.uiState.value.otherResults)

        viewModel.onSortDirectionToggle()
        testDispatcher.scheduler.advanceUntilIdle()

        assertEquals(SortDirection.DESCENDING, viewModel.uiState.value.sortDirection)
        assertEquals(listOf(b, a), viewModel.uiState.value.selectedResults)
    }

    @Test
    fun `onSortOptionChange to LAST_USED is a no-op without Usage Access granted`() = runTest {
        val a = appInfo('a')
        val viewModel = createViewModel(facetId = 1L, installed = listOf(a), favorites = emptyList(), usageAccessGranted = false)
        backgroundScope.launch { viewModel.uiState.collect {} }
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.onSortOptionChange(AppSortOption.LAST_USED)
        testDispatcher.scheduler.advanceUntilIdle()

        assertEquals(AppSortOption.ALPHABETICAL, viewModel.uiState.value.sortOption)
    }

    @Test
    fun `onSortOptionChange to LAST_USED applies once Usage Access is granted`() = runTest {
        val a = appInfo('a')
        val viewModel = createViewModel(facetId = 1L, installed = listOf(a), favorites = emptyList(), usageAccessGranted = true)
        backgroundScope.launch { viewModel.uiState.collect {} }
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.onSortOptionChange(AppSortOption.LAST_USED)
        testDispatcher.scheduler.advanceUntilIdle()

        assertEquals(AppSortOption.LAST_USED, viewModel.uiState.value.sortOption)
    }

    @Test
    fun `refreshUsageAccessGranted re-checks the permission, since it has no grant-change callback`() = runTest {
        val a = appInfo('a')
        val appRepository = mock(AppRepository::class.java)
        `when`(appRepository.getInstalledApps()).thenReturn(listOf(a))
        val favoriteAppRepository = mock(FavoriteAppRepository::class.java)
        `when`(favoriteAppRepository.observeFavoriteItems(1L)).thenReturn(flowOf(emptyList()))
        val folderRepository = mock(FolderRepository::class.java)
        `when`(folderRepository.observeFolders()).thenReturn(flowOf(emptyList()))
        // False when the ViewModel is constructed (before the user leaves for system Settings),
        // true on every check after — simulates granting access and returning to this screen.
        val usageAccessRepository = mock(UsageAccessRepository::class.java)
        `when`(usageAccessRepository.isGranted()).thenReturn(false, true)

        val viewModel = FavoritesPickerViewModel(
            SavedStateHandle(mapOf("facetId" to 1L)),
            GetInstalledAppsUseCase(appRepository),
            favoriteAppRepository,
            mock(DefaultFavoriteAppRepository::class.java),
            folderRepository,
            SortAppsForPickerUseCase(mock(UsageStatsRepository::class.java)),
            usageAccessRepository,
        )
        assertEquals(false, viewModel.usageAccessGranted.value)

        viewModel.refreshUsageAccessGranted()

        assertEquals(true, viewModel.usageAccessGranted.value)
    }
}
