package com.facetlauncher.app.ui.settings

import com.facetlauncher.app.data.AccessibilityAccessRepository
import com.facetlauncher.app.data.SettingsRepository
import com.facetlauncher.app.data.model.LauncherSettings
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify
import org.mockito.Mockito.`when`

@OptIn(ExperimentalCoroutinesApi::class)
class GesturesViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private val settings = MutableStateFlow(LauncherSettings())

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun createViewModel(
        accessGranted: Boolean,
        settingsRepository: SettingsRepository = mock(SettingsRepository::class.java),
        accessRepository: AccessibilityAccessRepository = mock(AccessibilityAccessRepository::class.java),
    ): GesturesViewModel {
        `when`(settingsRepository.settings).thenReturn(settings)
        `when`(accessRepository.isGranted()).thenReturn(accessGranted)
        return GesturesViewModel(settingsRepository, accessRepository)
    }

    @Test
    fun `both optional gestures are off by default`() = runTest {
        // Given no gesture has been turned on
        val viewModel = createViewModel(accessGranted = false)
        backgroundScope.launch { viewModel.uiState.collect {} }
        testDispatcher.scheduler.advanceUntilIdle()

        // Then neither switch reads on
        val state = viewModel.uiState.value
        assertFalse(state.homeOpensSearch)
        assertFalse(state.doubleTapToLock)
    }

    @Test
    fun `double-tap to lock reads off without the accessibility grant even when stored on`() = runTest {
        // Given the preference is stored on but the accessibility service is not enabled
        settings.value = LauncherSettings(doubleTapToLock = true)
        val viewModel = createViewModel(accessGranted = false)
        backgroundScope.launch { viewModel.uiState.collect {} }
        testDispatcher.scheduler.advanceUntilIdle()

        // Then the switch reads off and access is reported missing
        assertFalse(viewModel.uiState.value.doubleTapToLock)
        assertFalse(viewModel.uiState.value.isAccessibilityGranted)
    }

    @Test
    fun `double-tap to lock reads on when stored on and the service is enabled`() = runTest {
        // Given the preference is on and the accessibility service is enabled
        settings.value = LauncherSettings(doubleTapToLock = true, homeOpensSearch = true)
        val viewModel = createViewModel(accessGranted = true)
        backgroundScope.launch { viewModel.uiState.collect {} }
        testDispatcher.scheduler.advanceUntilIdle()

        // Then both switches read on
        assertTrue(viewModel.uiState.value.doubleTapToLock)
        assertTrue(viewModel.uiState.value.homeOpensSearch)
    }

    @Test
    fun `refreshAccess picks up a grant made in system settings`() = runTest {
        // Given the preference is on and access starts out missing
        settings.value = LauncherSettings(doubleTapToLock = true)
        val accessRepository = mock(AccessibilityAccessRepository::class.java)
        val viewModel = createViewModel(accessGranted = false, accessRepository = accessRepository)
        backgroundScope.launch { viewModel.uiState.collect {} }
        testDispatcher.scheduler.advanceUntilIdle()
        assertFalse(viewModel.uiState.value.doubleTapToLock)

        // When the user enables the service and returns, and the screen refreshes
        `when`(accessRepository.isGranted()).thenReturn(true)
        viewModel.refreshAccess()
        testDispatcher.scheduler.advanceUntilIdle()

        // Then the switch now reads on
        assertTrue(viewModel.uiState.value.doubleTapToLock)
    }

    @Test
    fun `the switches write their settings`() = runTest {
        // Given a view model over a mocked settings repository
        val settingsRepository = mock(SettingsRepository::class.java)
        val viewModel = createViewModel(accessGranted = true, settingsRepository = settingsRepository)

        // When both switches are turned on
        viewModel.setHomeOpensSearch(true)
        viewModel.setDoubleTapToLock(true)
        testDispatcher.scheduler.advanceUntilIdle()

        // Then each is persisted
        verify(settingsRepository).setHomeOpensSearch(true)
        verify(settingsRepository).setDoubleTapToLock(true)
    }
}
