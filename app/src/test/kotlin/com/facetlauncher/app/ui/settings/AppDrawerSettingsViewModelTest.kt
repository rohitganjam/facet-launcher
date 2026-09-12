package com.facetlauncher.app.ui.settings

import com.facetlauncher.app.data.SettingsRepository
import com.facetlauncher.app.data.model.DrawerGridSize
import com.facetlauncher.app.data.model.DrawerListItemSize
import com.facetlauncher.app.data.model.DrawerPresentation
import com.facetlauncher.app.data.model.LauncherSettings
import com.facetlauncher.app.data.model.SearchBarPosition
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify
import org.mockito.Mockito.`when`

@OptIn(ExperimentalCoroutinesApi::class)
class AppDrawerSettingsViewModelTest {

    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setUp() { Dispatchers.setMain(testDispatcher) }

    @After
    fun tearDown() { Dispatchers.resetMain() }

    private fun createViewModel(settingsRepository: SettingsRepository = mock(SettingsRepository::class.java)): AppDrawerSettingsViewModel {
        `when`(settingsRepository.settings).thenReturn(flowOf(LauncherSettings()))
        return AppDrawerSettingsViewModel(settingsRepository)
    }

    @Test
    fun `changing drawer presentation calls the repository setter`() = runTest {
        val settingsRepository = mock(SettingsRepository::class.java)
        val viewModel = createViewModel(settingsRepository = settingsRepository)

        viewModel.setDrawerPresentation(DrawerPresentation.GRID)
        testDispatcher.scheduler.advanceUntilIdle()

        verify(settingsRepository).setDrawerPresentation(DrawerPresentation.GRID)
    }

    @Test
    fun `changing drawer grid size calls the repository setter`() = runTest {
        val settingsRepository = mock(SettingsRepository::class.java)
        val viewModel = createViewModel(settingsRepository = settingsRepository)

        viewModel.setDrawerGridSize(DrawerGridSize.FOUR_BY_FOUR)
        testDispatcher.scheduler.advanceUntilIdle()

        verify(settingsRepository).setDrawerGridSize(DrawerGridSize.FOUR_BY_FOUR)
    }

    @Test
    fun `changing drawer list item size calls the repository setter`() = runTest {
        val settingsRepository = mock(SettingsRepository::class.java)
        val viewModel = createViewModel(settingsRepository = settingsRepository)

        viewModel.setDrawerListItemSize(DrawerListItemSize.SPACIOUS)
        testDispatcher.scheduler.advanceUntilIdle()

        verify(settingsRepository).setDrawerListItemSize(DrawerListItemSize.SPACIOUS)
    }

    @Test
    fun `toggling show drawer icons calls the repository setter`() = runTest {
        val settingsRepository = mock(SettingsRepository::class.java)
        val viewModel = createViewModel(settingsRepository = settingsRepository)

        viewModel.setShowDrawerIcons(false)
        testDispatcher.scheduler.advanceUntilIdle()

        verify(settingsRepository).setShowDrawerIcons(false)
    }

    @Test
    fun `toggling show drawer labels calls the repository setter`() = runTest {
        val settingsRepository = mock(SettingsRepository::class.java)
        val viewModel = createViewModel(settingsRepository = settingsRepository)

        viewModel.setShowDrawerLabels(false)
        testDispatcher.scheduler.advanceUntilIdle()

        verify(settingsRepository).setShowDrawerLabels(false)
    }

    @Test
    fun `toggling search contacts calls the repository setter`() = runTest {
        val settingsRepository = mock(SettingsRepository::class.java)
        val viewModel = createViewModel(settingsRepository = settingsRepository)

        viewModel.setSearchContactsEnabled(true)
        testDispatcher.scheduler.advanceUntilIdle()

        verify(settingsRepository).setSearchContactsEnabled(true)
    }

    @Test
    fun `toggling search settings calls the repository setter`() = runTest {
        val settingsRepository = mock(SettingsRepository::class.java)
        val viewModel = createViewModel(settingsRepository = settingsRepository)

        viewModel.setSearchSettingsEnabled(true)
        testDispatcher.scheduler.advanceUntilIdle()

        verify(settingsRepository).setSearchSettingsEnabled(true)
    }

    @Test
    fun `changing search bar position calls the repository setter`() = runTest {
        val settingsRepository = mock(SettingsRepository::class.java)
        val viewModel = createViewModel(settingsRepository = settingsRepository)

        viewModel.setSearchBarPosition(SearchBarPosition.BOTTOM)
        testDispatcher.scheduler.advanceUntilIdle()

        verify(settingsRepository).setSearchBarPosition(SearchBarPosition.BOTTOM)
    }

    @Test
    fun `changing drawer opacity calls the repository setter`() = runTest {
        val settingsRepository = mock(SettingsRepository::class.java)
        val viewModel = createViewModel(settingsRepository = settingsRepository)

        viewModel.setDrawerOpacity(0.8f)
        testDispatcher.scheduler.advanceUntilIdle()

        verify(settingsRepository).setDrawerOpacity(0.8f)
    }
}
