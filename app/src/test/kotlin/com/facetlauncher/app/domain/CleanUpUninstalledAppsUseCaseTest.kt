package com.facetlauncher.app.domain

import android.os.UserHandle
import com.facetlauncher.app.data.AppRepository
import com.facetlauncher.app.data.AppVaultRepository
import com.facetlauncher.app.data.FolderRepository
import com.facetlauncher.app.data.IconOverrideRepository
import com.facetlauncher.app.data.LaunchHistoryRepository
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.runTest
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify
import org.mockito.Mockito.verifyNoInteractions
import org.mockito.Mockito.`when`
import org.robolectric.RobolectricTestRunner
import com.facetlauncher.app.data.PlacementRepository

@RunWith(RobolectricTestRunner::class)
class CleanUpUninstalledAppsUseCaseTest {

    private class Fixture {
        val appRepository: AppRepository = mock(AppRepository::class.java)

        // replay = 1 so an emission is delivered even if `useCase()`'s collector hasn't
        // subscribed by the time it fires — avoids a race against the launched job below.
        val uninstalls = MutableSharedFlow<Pair<String, Int>>(replay = 1)
        val profileRemovals = MutableSharedFlow<UserHandle>(replay = 1)
        val placementRepository: PlacementRepository = mock(PlacementRepository::class.java)
        val folderRepository: FolderRepository = mock(FolderRepository::class.java)
        val launchHistoryRepository: LaunchHistoryRepository = mock(LaunchHistoryRepository::class.java)
        val iconOverrideRepository: IconOverrideRepository = mock(IconOverrideRepository::class.java)
        val appVaultRepository: AppVaultRepository = mock(AppVaultRepository::class.java)

        init {
            `when`(appRepository.observeUninstalledPackages()).thenReturn(uninstalls)
            `when`(appRepository.observeProfileRemoved()).thenReturn(profileRemovals)
        }

        fun useCase() = CleanUpUninstalledAppsUseCase(
            appRepository, placementRepository, folderRepository, launchHistoryRepository, iconOverrideRepository, appVaultRepository,
        )
    }

    @Test
    fun `deletes the uninstalled package's dock, favorite, folder-membership, launch-history and custom-icon rows outright, scoped to its real userId`() = runTest {
        // Given a live uninstall-event stream and mocked Dock/Favorite/Folder repositories
        val fixture = Fixture()
        val useCase = fixture.useCase()

        // When the use case is running and a Work Profile package is reported genuinely uninstalled
        val job = launch { useCase() }
        fixture.uninstalls.emit("com.example.removed" to 10)
        testScheduler.advanceUntilIdle()

        // Then every repository permanently deletes that package's row for that real userId, not
        // just filters it from view — including folder membership, regardless of whether any
        // folder holding it was ever placed in the Dock/Favorites. Scoped by the real userId, so
        // it can't collide with a personal favorite sharing the same package name, nor with a
        // second profile sharing the same display category (e.g. a clone profile also OTHER).
        verify(fixture.placementRepository).removeByPackage("com.example.removed", 10)
        verify(fixture.folderRepository).removeByPackage("com.example.removed", 10)
        verify(fixture.launchHistoryRepository).removeByPackage("com.example.removed", 10)
        verify(fixture.iconOverrideRepository).removeByPackage("com.example.removed", 10)
        job.cancel()
    }

    @Test
    fun `a profile removal bulk-clears every row for that exact handle's userId, without waiting on per-package events`() = runTest {
        // Given the use case running, with no per-package uninstall ever reported
        val fixture = Fixture()
        val useCase = fixture.useCase()
        val job = launch { useCase() }
        val removedHandle = mock(UserHandle::class.java)

        // When a Work Profile itself is unenrolled
        fixture.profileRemovals.emit(removedHandle)
        testScheduler.advanceUntilIdle()

        // Then every repository bulk-drops rows for exactly that handle's userId directly
        verify(fixture.placementRepository).removeByUserId(removedHandle.hashCode())
        verify(fixture.folderRepository).removeByUserId(removedHandle.hashCode())
        verify(fixture.launchHistoryRepository).removeByUserId(removedHandle.hashCode())
        verify(fixture.iconOverrideRepository).removeByUserId(removedHandle.hashCode())
        job.cancel()
    }

    @Test
    fun `an uninstalled app stays in the vault so a reinstall stays hidden`() = runTest {
        // Given the use case running
        val fixture = Fixture()
        val job = launch { fixture.useCase()() }

        // When a package is uninstalled
        fixture.uninstalls.emit("com.example.secret" to 0)
        testScheduler.advanceUntilIdle()

        // Then the vault is not touched
        verifyNoInteractions(fixture.appVaultRepository)
        job.cancel()
    }

    @Test
    fun `a removed profile drops its vaulted apps`() = runTest {
        val fixture = Fixture()
        val job = launch { fixture.useCase()() }
        val removedHandle = mock(UserHandle::class.java)

        fixture.profileRemovals.emit(removedHandle)
        testScheduler.advanceUntilIdle()

        verify(fixture.appVaultRepository).removeByUserId(removedHandle.hashCode())
        job.cancel()
    }
}
