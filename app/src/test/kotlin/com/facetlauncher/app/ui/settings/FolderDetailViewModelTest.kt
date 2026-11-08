package com.facetlauncher.app.ui.settings

import androidx.lifecycle.SavedStateHandle
import com.facetlauncher.app.data.AppRepository
import com.facetlauncher.app.data.FolderRepository
import com.facetlauncher.app.data.FakeFolderDao
import com.facetlauncher.app.data.model.AppInfo
import com.facetlauncher.app.data.model.PlacedItem
import com.facetlauncher.app.data.model.PlacementList
import com.facetlauncher.app.domain.AddToActiveListUseCase
import com.facetlauncher.app.domain.FolderPlacementState
import com.facetlauncher.app.domain.ObserveFolderPlacementUseCase
import com.facetlauncher.app.domain.QuickPlacementAction
import com.facetlauncher.app.domain.RemoveFromActiveListUseCase
import org.mockito.Mockito.verify
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
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito.mock
import org.mockito.Mockito.`when`
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(RobolectricTestRunner::class)
class FolderDetailViewModelTest {

    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setUp() { Dispatchers.setMain(testDispatcher) }

    @After
    fun tearDown() { Dispatchers.resetMain() }

    private fun appInfo(letter: Char) =
        AppInfo(packageName = "com.example.$letter", activityName = ".Main", label = "$letter App", icon = null)

    private fun folderRepository(installed: List<AppInfo> = emptyList()): FolderRepository {
        val appRepository = mock(AppRepository::class.java)
        `when`(appRepository.observeInstalledApps()).thenReturn(flowOf(installed))
        return FolderRepository(FakeFolderDao(), appRepository)
    }


    private fun viewModel(
        folderId: Long,
        repository: FolderRepository,
        addToActiveList: AddToActiveListUseCase = mock(AddToActiveListUseCase::class.java),
        removeFromActiveList: RemoveFromActiveListUseCase = mock(RemoveFromActiveListUseCase::class.java),
        placement: FolderPlacementState = FolderPlacementState(),
    ): FolderDetailViewModel {
        val observePlacement = mock(ObserveFolderPlacementUseCase::class.java)
        `when`(observePlacement(folderId)).thenReturn(flowOf(placement))
        return FolderDetailViewModel(SavedStateHandle(mapOf("folderId" to folderId)), repository, observePlacement, addToActiveList, removeFromActiveList)
    }

    @Test
    fun `uiState surfaces the folder matching folderId from the live folder list`() = runTest {
        val app = appInfo('a')
        val repository = folderRepository(listOf(app))
        val folderId = repository.createFolder("Games")
        repository.addAppToFolder(folderId, app)
        // A second, unrelated folder proves the lookup filters by id, not just "the first one"
        repository.createFolder("Social")

        val viewModel = viewModel(folderId, repository)
        backgroundScope.launch { viewModel.uiState.collect {} }
        testDispatcher.scheduler.advanceUntilIdle()

        val folder = viewModel.uiState.value.folder
        assertEquals("Games", folder?.name)
        assertEquals(listOf(app), folder?.apps)
    }

    @Test
    fun `uiState folder is null when folderId matches no existing folder`() = runTest {
        val repository = folderRepository()

        val viewModel = viewModel(999L, repository)
        backgroundScope.launch { viewModel.uiState.collect {} }
        testDispatcher.scheduler.advanceUntilIdle()

        assertNull(viewModel.uiState.value.folder)
    }

    @Test
    fun `renameFolder delegates to the repository for this folderId`() = runTest {
        val repository = folderRepository()
        val folderId = repository.createFolder("Games")
        val viewModel = viewModel(folderId, repository)
        backgroundScope.launch { viewModel.uiState.collect {} }
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.renameFolder("Fun")
        testDispatcher.scheduler.advanceUntilIdle()

        assertEquals("Fun", viewModel.uiState.value.folder?.name)
    }

    @Test
    fun `reorderApps delegates to the repository for this folderId`() = runTest {
        val a = appInfo('a')
        val b = appInfo('b')
        val repository = folderRepository(listOf(a, b))
        val folderId = repository.createFolder("Games")
        repository.addAppToFolder(folderId, a)
        repository.addAppToFolder(folderId, b)
        val viewModel = viewModel(folderId, repository)
        backgroundScope.launch { viewModel.uiState.collect {} }
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.reorderApps(listOf(b, a))
        testDispatcher.scheduler.advanceUntilIdle()

        assertEquals(listOf(b, a), viewModel.uiState.value.folder?.apps)
    }

    @Test
    fun `removeApp delegates to the repository for this folderId, without deleting the folder`() = runTest {
        val a = appInfo('a')
        val b = appInfo('b')
        val repository = folderRepository(listOf(a, b))
        val folderId = repository.createFolder("Games")
        repository.addAppToFolder(folderId, a)
        repository.addAppToFolder(folderId, b)
        val viewModel = viewModel(folderId, repository)
        backgroundScope.launch { viewModel.uiState.collect {} }
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.removeApp(a)
        testDispatcher.scheduler.advanceUntilIdle()

        assertEquals(listOf(b), viewModel.uiState.value.folder?.apps)
    }

    @Test
    fun `the Dock and Favorites actions reach the active-list use cases with this folder`() = runTest {
        // Given a folder and mocked add and remove use cases
        val repository = folderRepository()
        val folderId = repository.createFolder("Games")
        val add = mock(AddToActiveListUseCase::class.java)
        val remove = mock(RemoveFromActiveListUseCase::class.java)
        val viewModel = viewModel(folderId, repository, addToActiveList = add, removeFromActiveList = remove)
        backgroundScope.launch { viewModel.uiState.collect {} }
        testDispatcher.scheduler.advanceUntilIdle()
        val folder = viewModel.uiState.value.folder!!

        // When the menu adds it to the Dock and removes it from Favorites
        viewModel.applyPlacement(PlacementList.DOCK, QuickPlacementAction.Add("Home"))
        viewModel.applyPlacement(PlacementList.FAVORITES, QuickPlacementAction.Remove("Home"))
        testDispatcher.scheduler.advanceUntilIdle()

        // Then each reaches its use case for this folder
        verify(add).invoke(PlacementList.DOCK, PlacedItem.FolderItem(folder))
        verify(remove).invoke(PlacementList.FAVORITES, PlacedItem.FolderItem(folder))
    }

    @Test
    fun `uiState carries the placement state for the menu`() = runTest {
        val repository = folderRepository()
        val folderId = repository.createFolder("Games")
        val placement = FolderPlacementState(facetName = "Home", showFacetBadge = true)
        val viewModel = viewModel(folderId, repository, placement = placement)
        backgroundScope.launch { viewModel.uiState.collect {} }
        testDispatcher.scheduler.advanceUntilIdle()

        assertEquals(placement, viewModel.uiState.value.placement)
    }

    @Test
    fun `deleteFolder removes this folder`() = runTest {
        val repository = folderRepository()
        val folderId = repository.createFolder("Games")
        val viewModel = viewModel(folderId, repository)
        backgroundScope.launch { viewModel.uiState.collect {} }
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.deleteFolder()
        testDispatcher.scheduler.advanceUntilIdle()

        assertNull(viewModel.uiState.value.folder)
    }
}
