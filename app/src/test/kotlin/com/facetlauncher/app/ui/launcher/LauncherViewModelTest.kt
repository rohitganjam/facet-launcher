package com.facetlauncher.app.ui.launcher

import com.facetlauncher.app.data.AppRepository
import com.facetlauncher.app.data.SettingsRepository
import com.facetlauncher.app.data.model.AppInfo
import com.facetlauncher.app.data.model.LauncherSettings
import com.facetlauncher.app.domain.CleanUpUninstalledAppsUseCase
import com.facetlauncher.app.domain.EnsureActiveProfileUseCase
import com.facetlauncher.app.domain.GetInstalledAppsUseCase
import com.facetlauncher.app.domain.SeedDefaultDockUseCase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito.mock
import org.mockito.Mockito.`when`

@OptIn(ExperimentalCoroutinesApi::class)
class LauncherViewModelTest {

    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `exposes loading state until the use case resolves, then the fetched apps`() = runTest {
        // Given a repository that will return two apps
        val repository = mock(AppRepository::class.java)
        val apps = listOf(
            AppInfo("com.example.a", ".Main", "A", null),
            AppInfo("com.example.b", ".Main", "B", null),
        )
        `when`(repository.observeInstalledApps()).thenReturn(flowOf(apps))
        val ensureActiveProfile = mock(EnsureActiveProfileUseCase::class.java)
        val cleanUpUninstalledApps = mock(CleanUpUninstalledAppsUseCase::class.java)
        val seedDefaultDock = mock(SeedDefaultDockUseCase::class.java)
        val settingsRepository = mock(SettingsRepository::class.java)
        `when`(settingsRepository.settings).thenReturn(flowOf(LauncherSettings()))
        val viewModel = LauncherViewModel(
            GetInstalledAppsUseCase(repository),
            ensureActiveProfile,
            cleanUpUninstalledApps,
            seedDefaultDock,
            settingsRepository,
        )

        // Then immediately after construction it's still loading (the fetch coroutine hasn't run yet)
        assertTrue(viewModel.uiState.value.isLoading)
        assertEquals(emptyList<AppInfo>(), viewModel.uiState.value.apps)

        // When the pending coroutine work is allowed to complete
        testDispatcher.scheduler.advanceUntilIdle()

        // Then the ui state reflects the fetched apps and is no longer loading
        assertFalse(viewModel.uiState.value.isLoading)
        assertEquals(apps, viewModel.uiState.value.apps)
    }

    @Test
    fun `stops loading on its own after the timeout even if the underlying flows never emit`() = runTest {
        // Given a repository whose flow never emits (simulating a stuck/broken data source) —
        // this is the actual home screen, so it must never stay blank forever (see chat history).
        val repository = mock(AppRepository::class.java)
        `when`(repository.observeInstalledApps()).thenReturn(kotlinx.coroutines.flow.MutableSharedFlow())
        val ensureActiveProfile = mock(EnsureActiveProfileUseCase::class.java)
        val cleanUpUninstalledApps = mock(CleanUpUninstalledAppsUseCase::class.java)
        val seedDefaultDock = mock(SeedDefaultDockUseCase::class.java)
        val settingsRepository = mock(SettingsRepository::class.java)
        `when`(settingsRepository.settings).thenReturn(kotlinx.coroutines.flow.MutableSharedFlow())
        val viewModel = LauncherViewModel(
            GetInstalledAppsUseCase(repository),
            ensureActiveProfile,
            cleanUpUninstalledApps,
            seedDefaultDock,
            settingsRepository,
        )

        // Then it's still loading well before the timeout
        testDispatcher.scheduler.advanceTimeBy(2_000)
        assertTrue(viewModel.uiState.value.isLoading)

        // When the timeout elapses with no real data having arrived
        testDispatcher.scheduler.advanceTimeBy(1_500)

        // Then it stops loading anyway, still with the (empty/default) state it had
        assertFalse(viewModel.uiState.value.isLoading)
        assertEquals(emptyList<AppInfo>(), viewModel.uiState.value.apps)
    }

    @Test
    fun `reflects onboardingCompleted from settings, and completeOnboarding persists it`() = runTest {
        // Given settings with onboarding not yet completed
        val repository = mock(AppRepository::class.java)
        `when`(repository.observeInstalledApps()).thenReturn(flowOf(emptyList()))
        val ensureActiveProfile = mock(EnsureActiveProfileUseCase::class.java)
        val cleanUpUninstalledApps = mock(CleanUpUninstalledAppsUseCase::class.java)
        val seedDefaultDock = mock(SeedDefaultDockUseCase::class.java)
        val settingsRepository = mock(SettingsRepository::class.java)
        `when`(settingsRepository.settings).thenReturn(flowOf(LauncherSettings(onboardingCompleted = false)))
        val viewModel = LauncherViewModel(
            GetInstalledAppsUseCase(repository),
            ensureActiveProfile,
            cleanUpUninstalledApps,
            seedDefaultDock,
            settingsRepository,
        )
        testDispatcher.scheduler.advanceUntilIdle()

        // Then the ui state reflects it
        assertFalse(viewModel.uiState.value.onboardingCompleted)

        // When onboarding completes
        viewModel.completeOnboarding()
        testDispatcher.scheduler.advanceUntilIdle()

        // Then the repository is told to persist it
        org.mockito.Mockito.verify(settingsRepository).setOnboardingCompleted(true)
    }
}
