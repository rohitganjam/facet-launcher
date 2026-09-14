package com.facetlauncher.app.ui.settings

import androidx.lifecycle.SavedStateHandle
import com.facetlauncher.app.data.AppRepository
import com.facetlauncher.app.data.FolderRepository
import com.facetlauncher.app.data.FakeFolderDao
import com.facetlauncher.app.data.model.AppInfo
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

@OptIn(ExperimentalCoroutinesApi::class)
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

    @Test
    fun `uiState surfaces the folder matching folderId from the live folder list`() = runTest {
        val app = appInfo('a')
        val repository = folderRepository(listOf(app))
        val folderId = repository.createFolder("Games")
        repository.addAppToFolder(folderId, app)
        // A second, unrelated folder proves the lookup filters by id, not just "the first one"
        repository.createFolder("Social")

        val viewModel = FolderDetailViewModel(SavedStateHandle(mapOf("folderId" to folderId)), repository)
        backgroundScope.launch { viewModel.uiState.collect {} }
        testDispatcher.scheduler.advanceUntilIdle()

        val folder = viewModel.uiState.value.folder
        assertEquals("Games", folder?.name)
        assertEquals(listOf(app), folder?.apps)
    }

    @Test
    fun `uiState folder is null when folderId matches no existing folder`() = runTest {
        val repository = folderRepository()

        val viewModel = FolderDetailViewModel(SavedStateHandle(mapOf("folderId" to 999L)), repository)
        backgroundScope.launch { viewModel.uiState.collect {} }
        testDispatcher.scheduler.advanceUntilIdle()

        assertNull(viewModel.uiState.value.folder)
    }

    @Test
    fun `renameFolder delegates to the repository for this folderId`() = runTest {
        val repository = folderRepository()
        val folderId = repository.createFolder("Games")
        val viewModel = FolderDetailViewModel(SavedStateHandle(mapOf("folderId" to folderId)), repository)
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
        val viewModel = FolderDetailViewModel(SavedStateHandle(mapOf("folderId" to folderId)), repository)
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
        val viewModel = FolderDetailViewModel(SavedStateHandle(mapOf("folderId" to folderId)), repository)
        backgroundScope.launch { viewModel.uiState.collect {} }
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.removeApp(a)
        testDispatcher.scheduler.advanceUntilIdle()

        assertEquals(listOf(b), viewModel.uiState.value.folder?.apps)
    }
}
