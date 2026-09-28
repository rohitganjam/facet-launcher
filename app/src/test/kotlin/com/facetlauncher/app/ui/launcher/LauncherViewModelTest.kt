package com.facetlauncher.app.ui.launcher

import com.facetlauncher.app.data.AppRepository
import com.facetlauncher.app.data.SettingsRepository
import com.facetlauncher.app.data.WorkProfileInfo
import com.facetlauncher.app.data.WorkProfileRepository
import com.facetlauncher.app.data.model.AppInfo
import com.facetlauncher.app.data.model.LauncherSettings
import com.facetlauncher.app.domain.ActivateFacetByIdUseCase
import com.facetlauncher.app.domain.CleanUpUninstalledAppsUseCase
import com.facetlauncher.app.domain.EnsureActiveFacetUseCase
import com.facetlauncher.app.domain.GetInstalledAppsUseCase
import com.facetlauncher.app.domain.RepairOrphanedProfileRowsUseCase
import com.facetlauncher.app.domain.SeedDefaultDockUseCase
import com.facetlauncher.app.domain.SyncFacetShortcutsUseCase
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
import org.junit.runner.RunWith
import org.mockito.Mockito.mock
import org.mockito.Mockito.`when`
import org.robolectric.RobolectricTestRunner

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(RobolectricTestRunner::class)
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

    private fun fakeWorkProfileRepository(workProfiles: List<WorkProfileInfo> = emptyList()): WorkProfileRepository {
        val repository = mock(WorkProfileRepository::class.java)
        `when`(repository.observeWorkProfiles()).thenReturn(flowOf(workProfiles))
        return repository
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
        val ensureActiveFacet = mock(EnsureActiveFacetUseCase::class.java)
        val cleanUpUninstalledApps = mock(CleanUpUninstalledAppsUseCase::class.java)
        val repairOrphanedProfileRows = mock(RepairOrphanedProfileRowsUseCase::class.java)
        val seedDefaultDock = mock(SeedDefaultDockUseCase::class.java)
        val syncFacetShortcuts = mock(SyncFacetShortcutsUseCase::class.java)
        val activateFacetById = mock(ActivateFacetByIdUseCase::class.java)
        val settingsRepository = mock(SettingsRepository::class.java)
        `when`(settingsRepository.settings).thenReturn(flowOf(LauncherSettings()))
        val viewModel = LauncherViewModel(
            GetInstalledAppsUseCase(repository),
            ensureActiveFacet,
            cleanUpUninstalledApps,
            repairOrphanedProfileRows,
            seedDefaultDock,
            syncFacetShortcuts,
            activateFacetById,
            settingsRepository,
            fakeWorkProfileRepository(),
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
        val ensureActiveFacet = mock(EnsureActiveFacetUseCase::class.java)
        val cleanUpUninstalledApps = mock(CleanUpUninstalledAppsUseCase::class.java)
        val repairOrphanedProfileRows = mock(RepairOrphanedProfileRowsUseCase::class.java)
        val seedDefaultDock = mock(SeedDefaultDockUseCase::class.java)
        val syncFacetShortcuts = mock(SyncFacetShortcutsUseCase::class.java)
        val activateFacetById = mock(ActivateFacetByIdUseCase::class.java)
        val settingsRepository = mock(SettingsRepository::class.java)
        `when`(settingsRepository.settings).thenReturn(kotlinx.coroutines.flow.MutableSharedFlow())
        val viewModel = LauncherViewModel(
            GetInstalledAppsUseCase(repository),
            ensureActiveFacet,
            cleanUpUninstalledApps,
            repairOrphanedProfileRows,
            seedDefaultDock,
            syncFacetShortcuts,
            activateFacetById,
            settingsRepository,
            fakeWorkProfileRepository(),
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
        val ensureActiveFacet = mock(EnsureActiveFacetUseCase::class.java)
        val cleanUpUninstalledApps = mock(CleanUpUninstalledAppsUseCase::class.java)
        val repairOrphanedProfileRows = mock(RepairOrphanedProfileRowsUseCase::class.java)
        val seedDefaultDock = mock(SeedDefaultDockUseCase::class.java)
        val syncFacetShortcuts = mock(SyncFacetShortcutsUseCase::class.java)
        val activateFacetById = mock(ActivateFacetByIdUseCase::class.java)
        val settingsRepository = mock(SettingsRepository::class.java)
        `when`(settingsRepository.settings).thenReturn(flowOf(LauncherSettings(onboardingCompleted = false)))
        val viewModel = LauncherViewModel(
            GetInstalledAppsUseCase(repository),
            ensureActiveFacet,
            cleanUpUninstalledApps,
            repairOrphanedProfileRows,
            seedDefaultDock,
            syncFacetShortcuts,
            activateFacetById,
            settingsRepository,
            fakeWorkProfileRepository(),
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

    @Test
    fun `reflects workProfiles from WorkProfileRepository`() = runTest {
        // Given a Work Profile that exists
        val repository = mock(AppRepository::class.java)
        `when`(repository.observeInstalledApps()).thenReturn(flowOf(emptyList()))
        val ensureActiveFacet = mock(EnsureActiveFacetUseCase::class.java)
        val cleanUpUninstalledApps = mock(CleanUpUninstalledAppsUseCase::class.java)
        val repairOrphanedProfileRows = mock(RepairOrphanedProfileRowsUseCase::class.java)
        val seedDefaultDock = mock(SeedDefaultDockUseCase::class.java)
        val syncFacetShortcuts = mock(SyncFacetShortcutsUseCase::class.java)
        val activateFacetById = mock(ActivateFacetByIdUseCase::class.java)
        val settingsRepository = mock(SettingsRepository::class.java)
        `when`(settingsRepository.settings).thenReturn(flowOf(LauncherSettings()))
        val workProfile = WorkProfileInfo(handle = mock(android.os.UserHandle::class.java), isPaused = false, label = "Work")
        val viewModel = LauncherViewModel(
            GetInstalledAppsUseCase(repository),
            ensureActiveFacet,
            cleanUpUninstalledApps,
            repairOrphanedProfileRows,
            seedDefaultDock,
            syncFacetShortcuts,
            activateFacetById,
            settingsRepository,
            fakeWorkProfileRepository(listOf(workProfile)),
        )
        testDispatcher.scheduler.advanceUntilIdle()

        // Then the ui state reflects it
        assertEquals(listOf(workProfile), viewModel.uiState.value.workProfiles)
    }

    @Test
    fun `runs repairOrphanedProfileRows once on init`() = runTest {
        // Given a fresh ViewModel
        val repository = mock(AppRepository::class.java)
        `when`(repository.observeInstalledApps()).thenReturn(flowOf(emptyList()))
        val ensureActiveFacet = mock(EnsureActiveFacetUseCase::class.java)
        val cleanUpUninstalledApps = mock(CleanUpUninstalledAppsUseCase::class.java)
        val repairOrphanedProfileRows = mock(RepairOrphanedProfileRowsUseCase::class.java)
        val seedDefaultDock = mock(SeedDefaultDockUseCase::class.java)
        val syncFacetShortcuts = mock(SyncFacetShortcutsUseCase::class.java)
        val activateFacetById = mock(ActivateFacetByIdUseCase::class.java)
        val settingsRepository = mock(SettingsRepository::class.java)
        `when`(settingsRepository.settings).thenReturn(flowOf(LauncherSettings()))

        // When it's constructed
        LauncherViewModel(
            GetInstalledAppsUseCase(repository),
            ensureActiveFacet,
            cleanUpUninstalledApps,
            repairOrphanedProfileRows,
            seedDefaultDock,
            syncFacetShortcuts,
            activateFacetById,
            settingsRepository,
            fakeWorkProfileRepository(),
        )
        testDispatcher.scheduler.advanceUntilIdle()

        // Then the one-time backfill pass ran
        org.mockito.Mockito.verify(repairOrphanedProfileRows).invoke()
    }

    @Test
    fun `runs syncFacetShortcuts once on init`() = runTest {
        // Given a fresh ViewModel
        val repository = mock(AppRepository::class.java)
        `when`(repository.observeInstalledApps()).thenReturn(flowOf(emptyList()))
        val ensureActiveFacet = mock(EnsureActiveFacetUseCase::class.java)
        val cleanUpUninstalledApps = mock(CleanUpUninstalledAppsUseCase::class.java)
        val repairOrphanedProfileRows = mock(RepairOrphanedProfileRowsUseCase::class.java)
        val seedDefaultDock = mock(SeedDefaultDockUseCase::class.java)
        val syncFacetShortcuts = mock(SyncFacetShortcutsUseCase::class.java)
        val activateFacetById = mock(ActivateFacetByIdUseCase::class.java)
        val settingsRepository = mock(SettingsRepository::class.java)
        `when`(settingsRepository.settings).thenReturn(flowOf(LauncherSettings()))

        // When it's constructed
        LauncherViewModel(
            GetInstalledAppsUseCase(repository),
            ensureActiveFacet,
            cleanUpUninstalledApps,
            repairOrphanedProfileRows,
            seedDefaultDock,
            syncFacetShortcuts,
            activateFacetById,
            settingsRepository,
            fakeWorkProfileRepository(),
        )
        testDispatcher.scheduler.advanceUntilIdle()

        // Then the live shortcut-sync collector was launched
        org.mockito.Mockito.verify(syncFacetShortcuts).invoke()
    }

    @Test
    fun `activateFacetFromDeepLink delegates the parsed id to ActivateFacetByIdUseCase`() = runTest {
        // Given a fresh ViewModel
        val repository = mock(AppRepository::class.java)
        `when`(repository.observeInstalledApps()).thenReturn(flowOf(emptyList()))
        val ensureActiveFacet = mock(EnsureActiveFacetUseCase::class.java)
        val cleanUpUninstalledApps = mock(CleanUpUninstalledAppsUseCase::class.java)
        val repairOrphanedProfileRows = mock(RepairOrphanedProfileRowsUseCase::class.java)
        val seedDefaultDock = mock(SeedDefaultDockUseCase::class.java)
        val syncFacetShortcuts = mock(SyncFacetShortcutsUseCase::class.java)
        val activateFacetById = mock(ActivateFacetByIdUseCase::class.java)
        val settingsRepository = mock(SettingsRepository::class.java)
        `when`(settingsRepository.settings).thenReturn(flowOf(LauncherSettings()))
        val viewModel = LauncherViewModel(
            GetInstalledAppsUseCase(repository),
            ensureActiveFacet,
            cleanUpUninstalledApps,
            repairOrphanedProfileRows,
            seedDefaultDock,
            syncFacetShortcuts,
            activateFacetById,
            settingsRepository,
            fakeWorkProfileRepository(),
        )

        // When a valid facet deep link arrives
        viewModel.activateFacetFromDeepLink(android.net.Uri.parse("facetlauncher://facet/7"))
        testDispatcher.scheduler.advanceUntilIdle()

        // Then its parsed id is forwarded to the use case
        org.mockito.Mockito.verify(activateFacetById).invoke(7L)
    }

    @Test
    fun `activateFacetFromDeepLink is a no-op for a uri that isn't a facet deep link`() = runTest {
        // Given a fresh ViewModel
        val repository = mock(AppRepository::class.java)
        `when`(repository.observeInstalledApps()).thenReturn(flowOf(emptyList()))
        val ensureActiveFacet = mock(EnsureActiveFacetUseCase::class.java)
        val cleanUpUninstalledApps = mock(CleanUpUninstalledAppsUseCase::class.java)
        val repairOrphanedProfileRows = mock(RepairOrphanedProfileRowsUseCase::class.java)
        val seedDefaultDock = mock(SeedDefaultDockUseCase::class.java)
        val syncFacetShortcuts = mock(SyncFacetShortcutsUseCase::class.java)
        val activateFacetById = mock(ActivateFacetByIdUseCase::class.java)
        val settingsRepository = mock(SettingsRepository::class.java)
        `when`(settingsRepository.settings).thenReturn(flowOf(LauncherSettings()))
        val viewModel = LauncherViewModel(
            GetInstalledAppsUseCase(repository),
            ensureActiveFacet,
            cleanUpUninstalledApps,
            repairOrphanedProfileRows,
            seedDefaultDock,
            syncFacetShortcuts,
            activateFacetById,
            settingsRepository,
            fakeWorkProfileRepository(),
        )

        // When an unrelated uri (e.g. Intent.ACTION_MAIN with no data) arrives
        viewModel.activateFacetFromDeepLink(android.net.Uri.parse("https://example.com"))
        testDispatcher.scheduler.advanceUntilIdle()

        // Then the use case is never invoked
        org.mockito.Mockito.verifyNoInteractions(activateFacetById)
    }
}
