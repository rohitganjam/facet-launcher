package com.facetlauncher.app.ui.settings

import com.facetlauncher.app.data.ReleaseNotesRepository
import com.facetlauncher.app.data.model.ReleaseNotes
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
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
class WhatsNewViewModelTest {

    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setUp() { Dispatchers.setMain(testDispatcher) }

    @After
    fun tearDown() { Dispatchers.resetMain() }

    @Test
    fun `starts loading then exposes the releases from the repository`() = runTest {
        // Given a repository with two releases
        val releases = listOf(ReleaseNotes("0.1.2", emptyList(), listOf("b")), ReleaseNotes("0.1.1", listOf("a"), emptyList()))
        val repository = mock(ReleaseNotesRepository::class.java)
        `when`(repository.releases()).thenReturn(releases)
        val viewModel = WhatsNewViewModel(repository)

        // Then before anything is collected the state is the loading default
        assertTrue(viewModel.uiState.value.isLoading)

        // When collected
        backgroundScope.launch { viewModel.uiState.collect {} }
        testDispatcher.scheduler.advanceUntilIdle()

        // Then the releases arrive and loading ends
        assertEquals(WhatsNewUiState(releases = releases, isLoading = false), viewModel.uiState.value)
    }

    @Test
    fun `no releases ends loading with an empty list`() = runTest {
        val repository = mock(ReleaseNotesRepository::class.java)
        `when`(repository.releases()).thenReturn(emptyList())
        val viewModel = WhatsNewViewModel(repository)

        backgroundScope.launch { viewModel.uiState.collect {} }
        testDispatcher.scheduler.advanceUntilIdle()

        assertEquals(WhatsNewUiState(releases = emptyList(), isLoading = false), viewModel.uiState.value)
    }
}
