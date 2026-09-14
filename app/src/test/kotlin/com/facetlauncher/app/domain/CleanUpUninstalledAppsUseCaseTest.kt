package com.facetlauncher.app.domain

import com.facetlauncher.app.data.AppRepository
import com.facetlauncher.app.data.DefaultFavoriteAppRepository
import com.facetlauncher.app.data.DockAppRepository
import com.facetlauncher.app.data.FavoriteAppRepository
import com.facetlauncher.app.data.FacetDockAppRepository
import com.facetlauncher.app.data.FolderRepository
import com.facetlauncher.app.data.model.AppProfile
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.runTest
import org.junit.Test
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify
import org.mockito.Mockito.`when`

class CleanUpUninstalledAppsUseCaseTest {

    private class Fixture {
        val appRepository: AppRepository = mock(AppRepository::class.java)

        // replay = 1 so an emission is delivered even if `useCase()`'s collector hasn't
        // subscribed by the time it fires — avoids a race against the launched job below.
        val uninstalls = MutableSharedFlow<Pair<String, AppProfile>>(replay = 1)
        val profileRemovals = MutableSharedFlow<Unit>(replay = 1)
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
    fun `deletes the uninstalled package's dock, favorite, and folder-membership rows outright, scoped to its profile`() = runTest {
        // Given a live uninstall-event stream and mocked Dock/Favorite/Folder repositories
        val fixture = Fixture()
        val useCase = fixture.useCase()

        // When the use case is running and a Work Profile package is reported genuinely uninstalled
        val job = launch { useCase() }
        fixture.uninstalls.emit("com.example.removed" to AppProfile.WORK)
        testScheduler.advanceUntilIdle()

        // Then every repository permanently deletes that package's row for that profile, not just
        // filters it from view — including folder membership, regardless of whether any folder
        // holding it was ever placed in the Dock/Favorites. Scoped to WORK, so it can't collide
        // with a personal favorite sharing the same package name.
        verify(fixture.dockAppRepository).removeByPackage("com.example.removed", AppProfile.WORK)
        verify(fixture.facetDockAppRepository).removeByPackage("com.example.removed", AppProfile.WORK)
        verify(fixture.favoriteAppRepository).removeByPackage("com.example.removed", AppProfile.WORK)
        verify(fixture.defaultFavoriteAppRepository).removeByPackage("com.example.removed", AppProfile.WORK)
        verify(fixture.folderRepository).removeByPackage("com.example.removed", AppProfile.WORK)
        job.cancel()
    }

    @Test
    fun `a Work Profile removal bulk-clears every WORK row, without waiting on per-package events`() = runTest {
        // Given the use case running, with no per-package uninstall ever reported
        val fixture = Fixture()
        val useCase = fixture.useCase()
        val job = launch { useCase() }

        // When the Work Profile itself is unenrolled
        fixture.profileRemovals.emit(Unit)
        testScheduler.advanceUntilIdle()

        // Then every repository bulk-drops its WORK-tagged rows directly
        verify(fixture.dockAppRepository).removeByProfile(AppProfile.WORK)
        verify(fixture.facetDockAppRepository).removeByProfile(AppProfile.WORK)
        verify(fixture.favoriteAppRepository).removeByProfile(AppProfile.WORK)
        verify(fixture.defaultFavoriteAppRepository).removeByProfile(AppProfile.WORK)
        verify(fixture.folderRepository).removeByProfile(AppProfile.WORK)
        job.cancel()
    }
}
