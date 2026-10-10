package com.facetlauncher.app.ui.settings

import com.facetlauncher.app.data.UsageAccessRepository
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
class UsageAccessExplanationViewModelTest {

    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setUp() { Dispatchers.setMain(testDispatcher) }

    @After
    fun tearDown() { Dispatchers.resetMain() }

    @Test
    fun `initial state reflects the current grant`() = runTest {
        val repository = mock(UsageAccessRepository::class.java)
        `when`(repository.isGranted()).thenReturn(true)

        val viewModel = UsageAccessExplanationViewModel(repository)

        assertEquals(true, viewModel.isGranted.value)
    }

    @Test
    fun `refresh picks up a grant made in system Settings`() = runTest {
        val repository = mock(UsageAccessRepository::class.java)
        `when`(repository.isGranted()).thenReturn(false)
        val viewModel = UsageAccessExplanationViewModel(repository)

        `when`(repository.isGranted()).thenReturn(true)
        viewModel.refresh()
        testDispatcher.scheduler.advanceUntilIdle()

        assertEquals(true, viewModel.isGranted.value)
    }

    @Test
    fun `refresh picks up a revoked grant`() = runTest {
        val repository = mock(UsageAccessRepository::class.java)
        `when`(repository.isGranted()).thenReturn(true)
        val viewModel = UsageAccessExplanationViewModel(repository)

        `when`(repository.isGranted()).thenReturn(false)
        viewModel.refresh()
        testDispatcher.scheduler.advanceUntilIdle()

        assertEquals(false, viewModel.isGranted.value)
    }
}
