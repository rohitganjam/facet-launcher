package com.facetlauncher.app.domain

import com.facetlauncher.app.data.AppRepository
import com.facetlauncher.app.data.DefaultFavoriteAppRepository
import com.facetlauncher.app.data.DockAppRepository
import com.facetlauncher.app.data.FavoriteAppRepository
import com.facetlauncher.app.data.FacetDockAppRepository
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.runTest
import org.junit.Test
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify
import org.mockito.Mockito.`when`

class CleanUpUninstalledAppsUseCaseTest {

    @Test
    fun `deletes the uninstalled package's dock and favorite rows outright`() = runTest {
        // Given a live uninstall-event stream and mocked Dock/Favorite repositories
        val appRepository = mock(AppRepository::class.java)
        // replay = 1 so the emission below is delivered even if `useCase()`'s collector hasn't
        // subscribed by the time it fires — avoids a race against the launched job below.
        val uninstalls = MutableSharedFlow<String>(replay = 1)
        `when`(appRepository.observeUninstalledPackages()).thenReturn(uninstalls)
        val dockAppRepository = mock(DockAppRepository::class.java)
        val facetDockAppRepository = mock(FacetDockAppRepository::class.java)
        val favoriteAppRepository = mock(FavoriteAppRepository::class.java)
        val defaultFavoriteAppRepository = mock(DefaultFavoriteAppRepository::class.java)
        val useCase = CleanUpUninstalledAppsUseCase(
            appRepository, dockAppRepository, facetDockAppRepository, favoriteAppRepository, defaultFavoriteAppRepository,
        )

        // When the use case is running and a package is reported genuinely uninstalled
        val job = launch { useCase() }
        uninstalls.emit("com.example.removed")
        testScheduler.advanceUntilIdle()

        // Then every repository permanently deletes that package's row, not just filters it from view
        verify(dockAppRepository).removeByPackage("com.example.removed")
        verify(facetDockAppRepository).removeByPackage("com.example.removed")
        verify(favoriteAppRepository).removeByPackage("com.example.removed")
        verify(defaultFavoriteAppRepository).removeByPackage("com.example.removed")
        job.cancel()
    }
}
