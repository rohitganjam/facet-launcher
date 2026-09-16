package com.facetlauncher.app.domain

import android.os.UserHandle
import com.facetlauncher.app.data.AppRepository
import com.facetlauncher.app.data.DefaultFavoriteAppRepository
import com.facetlauncher.app.data.DockAppRepository
import com.facetlauncher.app.data.FavoriteAppRepository
import com.facetlauncher.app.data.FacetDockAppRepository
import com.facetlauncher.app.data.FolderRepository
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.runTest
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify
import org.mockito.Mockito.`when`
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class CleanUpUninstalledAppsUseCaseTest {

    private class Fixture {
        val appRepository: AppRepository = mock(AppRepository::class.java)

        // replay = 1 so an emission is delivered even if `useCase()`'s collector hasn't
        // subscribed by the time it fires — avoids a race against the launched job below.
        val uninstalls = MutableSharedFlow<Pair<String, Int>>(replay = 1)
        val profileRemovals = MutableSharedFlow<UserHandle>(replay = 1)
        val dockAppRepository: DockAppRepository = mock(DockAppRepository::class.java)
        val facetDockAppRepository: FacetDockAppRepository = mock(FacetDockAppRepository::class.java)
        val favoriteAppRepository: FavoriteAppRepository = mock(FavoriteAppRepository::class.java)
        val defaultFavoriteAppRepository: DefaultFavoriteAppRepository = mock(DefaultFavoriteAppRepository::class.java)
        val folderRepository: FolderRepository = mock(FolderRepository::class.java)

        init {
            `when`(appRepository.observeUninstalledPackages()).thenReturn(uninstalls)
            `when`(appRepository.observeProfileRemoved()).thenReturn(profileRemovals)
        }

        fun useCase() = CleanUpUninstalledAppsUseCase(
            appRepository, dockAppRepository, facetDockAppRepository, favoriteAppRepository, defaultFavoriteAppRepository, folderRepository,
        )
    }

    @Test
    fun `deletes the uninstalled package's dock, favorite, and folder-membership rows outright, scoped to its real userId`() = runTest {
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
        verify(fixture.dockAppRepository).removeByPackage("com.example.removed", 10)
        verify(fixture.facetDockAppRepository).removeByPackage("com.example.removed", 10)
        verify(fixture.favoriteAppRepository).removeByPackage("com.example.removed", 10)
        verify(fixture.defaultFavoriteAppRepository).removeByPackage("com.example.removed", 10)
        verify(fixture.folderRepository).removeByPackage("com.example.removed", 10)
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
        verify(fixture.dockAppRepository).removeByUserId(removedHandle.hashCode())
        verify(fixture.facetDockAppRepository).removeByUserId(removedHandle.hashCode())
        verify(fixture.favoriteAppRepository).removeByUserId(removedHandle.hashCode())
        verify(fixture.defaultFavoriteAppRepository).removeByUserId(removedHandle.hashCode())
        verify(fixture.folderRepository).removeByUserId(removedHandle.hashCode())
        job.cancel()
    }
}
