package com.facetlauncher.app.ui.settings

import com.facetlauncher.app.data.NotificationAccessRepository
import com.facetlauncher.app.data.SettingsRepository
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
class NotificationAccessExplanationViewModelTest {

    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setUp() { Dispatchers.setMain(testDispatcher) }

    @After
    fun tearDown() { Dispatchers.resetMain() }

    @Test
    fun `initial state reflects the current grant`() = runTest {
        val access = mock(NotificationAccessRepository::class.java)
        `when`(access.isGranted()).thenReturn(true)

        val viewModel = NotificationAccessExplanationViewModel(access, mock(SettingsRepository::class.java))

        assertEquals(true, viewModel.isGranted.value)
    }

    @Test
    fun `refresh after a grant turns notification badges on`() = runTest {
        val access = mock(NotificationAccessRepository::class.java)
        val settings = mock(SettingsRepository::class.java)
        `when`(access.isGranted()).thenReturn(false)
        val viewModel = NotificationAccessExplanationViewModel(access, settings)

        `when`(access.isGranted()).thenReturn(true)
        viewModel.refresh()
        testDispatcher.scheduler.advanceUntilIdle()

        assertEquals(true, viewModel.isGranted.value)
        verify(settings).setNotificationDotsEnabled(true)
    }

    @Test
    fun `refresh while still not granted leaves notification badges untouched`() = runTest {
        val access = mock(NotificationAccessRepository::class.java)
        val settings = mock(SettingsRepository::class.java)
        `when`(access.isGranted()).thenReturn(false)
        val viewModel = NotificationAccessExplanationViewModel(access, settings)

        viewModel.refresh()
        testDispatcher.scheduler.advanceUntilIdle()

        assertEquals(false, viewModel.isGranted.value)
        verify(settings, never()).setNotificationDotsEnabled(true)
    }
}
