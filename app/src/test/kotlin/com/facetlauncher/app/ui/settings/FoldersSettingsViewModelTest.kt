package com.facetlauncher.app.ui.settings

import com.facetlauncher.app.data.FolderRepository
import com.facetlauncher.app.data.model.Folder
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito.mock
import org.mockito.Mockito.never
import org.mockito.Mockito.verify
import org.mockito.Mockito.`when`

@OptIn(ExperimentalCoroutinesApi::class)
class FoldersSettingsViewModelTest {

    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setUp() { Dispatchers.setMain(testDispatcher) }

    @After
    fun tearDown() { Dispatchers.resetMain() }

    private fun createViewModel(
        repository: FolderRepository,
        folders: List<Folder> = emptyList(),
    ): FoldersSettingsViewModel {
        `when`(repository.observeFolders()).thenReturn(MutableStateFlow(folders))
        return FoldersSettingsViewModel(repository)
    }

    @Test
    fun `uiState exposes the folder library from the repository`() = runTest {
        val folders = listOf(Folder(1, "Work", emptyList()), Folder(2, "Games", emptyList()))
        val viewModel = createViewModel(mock(FolderRepository::class.java), folders)

        val job = viewModel.uiState.onEach { }.launchIn(backgroundScope)
        testDispatcher.scheduler.advanceUntilIdle()

        assertEquals(folders, viewModel.uiState.value.folders)
        job.cancel()
    }

    @Test
    fun `createFolder delegates to the repository`() = runTest {
        val repository = mock(FolderRepository::class.java)
        val viewModel = createViewModel(repository)

        viewModel.createFolder("Travel")
        testDispatcher.scheduler.advanceUntilIdle()

        verify(repository).createFolder("Travel")
    }

    @Test
    fun `deleteFolder delegates to the repository`() = runTest {
        val repository = mock(FolderRepository::class.java)
        val viewModel = createViewModel(repository)

        viewModel.deleteFolder(7L)
        testDispatcher.scheduler.advanceUntilIdle()

        verify(repository).deleteFolder(7L)
    }
}
