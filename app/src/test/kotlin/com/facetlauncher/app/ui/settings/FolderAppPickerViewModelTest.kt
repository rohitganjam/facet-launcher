package com.facetlauncher.app.ui.settings

import androidx.lifecycle.SavedStateHandle
import com.facetlauncher.app.data.AppRepository
import com.facetlauncher.app.data.FolderRepository
import com.facetlauncher.app.data.FakeFolderDao
import com.facetlauncher.app.data.model.AppInfo
import com.facetlauncher.app.domain.GetInstalledAppsUseCase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito.mock
import org.mockito.Mockito.`when`

@OptIn(ExperimentalCoroutinesApi::class)
class FolderAppPickerViewModelTest {

    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setUp() { Dispatchers.setMain(testDispatcher) }

    @After
    fun tearDown() { Dispatchers.resetMain() }

    private fun appInfo(letter: Char) =
        AppInfo(packageName = "com.example.$letter", activityName = ".Main", label = "$letter App", icon = null)

    private suspend fun createViewModel(
        folderId: Long,
        installed: List<AppInfo>,
        repository: FolderRepository,
    ): FolderAppPickerViewModel {
        val getInstalledApps = GetInstalledAppsUseCase(fakeAppRepositoryReturning(installed))
        return FolderAppPickerViewModel(SavedStateHandle(mapOf("folderId" to folderId)), getInstalledApps, repository)
    }

    private suspend fun fakeAppRepositoryReturning(installed: List<AppInfo>): AppRepository {
        val appRepository = mock(AppRepository::class.java)
        `when`(appRepository.getInstalledApps()).thenReturn(installed)
        return appRepository
    }

    private fun folderRepository(installed: List<AppInfo>): FolderRepository {
        val appRepository = mock(AppRepository::class.java)
        `when`(appRepository.observeInstalledApps()).thenReturn(flowOf(installed))
        return FolderRepository(FakeFolderDao(), appRepository)
    }

    @Test
    fun `uiState splits installed apps into selectedResults for members and otherResults for the rest`() = runTest {
        val a = appInfo('a')
        val b = appInfo('b')
        val repository = folderRepository(listOf(a, b))
        val folderId = repository.createFolder("Games")
        repository.addAppToFolder(folderId, a)
        val viewModel = createViewModel(folderId, listOf(a, b), repository)

        backgroundScope.launch { viewModel.uiState.collect {} }
        testDispatcher.scheduler.advanceUntilIdle()

        assertEquals(listOf(a), viewModel.uiState.value.selectedResults)
        assertEquals(listOf(b), viewModel.uiState.value.otherResults)
        assertEquals("Games", viewModel.uiState.value.folderName)
    }

    @Test
    fun `toggleApp adds an app not yet in the folder, reflected live in folderAppComponents`() = runTest {
        // Note: selectedResults/otherResults sectioning is latched at load time (see uiState's
        // own doc comment) and deliberately does NOT reshuffle mid-visit — only the live
        // folderAppComponents set (what drives each row's checkbox) updates immediately.
        val a = appInfo('a')
        val repository = folderRepository(listOf(a))
        val folderId = repository.createFolder("Games")
        val viewModel = createViewModel(folderId, listOf(a), repository)
        backgroundScope.launch { viewModel.uiState.collect {} }
        testDispatcher.scheduler.advanceUntilIdle()
        assertTrue((a.packageName to a.activityName) !in viewModel.uiState.value.folderAppComponents)

        viewModel.toggleApp(a)
        testDispatcher.scheduler.advanceUntilIdle()

        assertTrue((a.packageName to a.activityName) in viewModel.uiState.value.folderAppComponents)
        assertEquals(listOf(a), repository.observeFolders().first().single().apps)
    }

    @Test
    fun `toggleApp removes an app already in the folder, reflected live in folderAppComponents`() = runTest {
        val a = appInfo('a')
        val repository = folderRepository(listOf(a))
        val folderId = repository.createFolder("Games")
        repository.addAppToFolder(folderId, a)
        val viewModel = createViewModel(folderId, listOf(a), repository)
        backgroundScope.launch { viewModel.uiState.collect {} }
        testDispatcher.scheduler.advanceUntilIdle()
        assertTrue((a.packageName to a.activityName) in viewModel.uiState.value.folderAppComponents)

        viewModel.toggleApp(a)
        testDispatcher.scheduler.advanceUntilIdle()

        assertTrue((a.packageName to a.activityName) !in viewModel.uiState.value.folderAppComponents)
        assertTrue(repository.observeFolders().first().single().apps.isEmpty())
    }

    @Test
    fun `onQueryChanged filters both result lists by label`() = runTest {
        val a = appInfo('a')
        val b = appInfo('b')
        val repository = folderRepository(listOf(a, b))
        val folderId = repository.createFolder("Games")
        val viewModel = createViewModel(folderId, listOf(a, b), repository)
        backgroundScope.launch { viewModel.uiState.collect {} }
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.onQueryChanged("a App")
        testDispatcher.scheduler.advanceUntilIdle()

        assertEquals(listOf(a), viewModel.uiState.value.otherResults)
    }

    @Test
    fun `selectedResults order stays latched to the folder's order at load time, not later re-sorts`() = runTest {
        val a = appInfo('a')
        val b = appInfo('b')
        val repository = folderRepository(listOf(a, b))
        val folderId = repository.createFolder("Games")
        repository.addAppToFolder(folderId, a)
        repository.addAppToFolder(folderId, b)
        val viewModel = createViewModel(folderId, listOf(a, b), repository)
        backgroundScope.launch { viewModel.uiState.collect {} }
        testDispatcher.scheduler.advanceUntilIdle()
        assertEquals(listOf(a, b), viewModel.uiState.value.selectedResults)

        // Reordering the folder's real membership after load doesn't reshuffle this visit's list
        repository.reorderFolderApps(folderId, listOf(b, a))
        testDispatcher.scheduler.advanceUntilIdle()

        assertEquals(listOf(a, b), viewModel.uiState.value.selectedResults)
    }
}
