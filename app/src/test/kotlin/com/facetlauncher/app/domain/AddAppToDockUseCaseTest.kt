package com.facetlauncher.app.domain

import com.facetlauncher.app.data.DockAppRepository
import com.facetlauncher.app.data.FacetDockAppRepository
import com.facetlauncher.app.data.FacetRepository
import com.facetlauncher.app.data.SettingsRepository
import com.facetlauncher.app.data.local.FacetEntity
import com.facetlauncher.app.data.model.AppInfo
import com.facetlauncher.app.data.model.LauncherSettings
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Test
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify
import org.mockito.Mockito.`when`

class AddAppToDockUseCaseTest {

    private val app = AppInfo(packageName = "com.example.mail", activityName = ".Main", label = "Mail", icon = null)

    private class Fixture {
        val settingsFlow = MutableStateFlow(LauncherSettings(activeFacetId = 1L))
        val settingsRepository = mock(SettingsRepository::class.java).also { `when`(it.settings).thenReturn(settingsFlow) }
        val facetRepository = mock(FacetRepository::class.java)
        val dockAppRepository = mock(DockAppRepository::class.java)
        val facetDockAppRepository = mock(FacetDockAppRepository::class.java)

        val useCase = AddAppToDockUseCase(settingsRepository, facetRepository, dockAppRepository, facetDockAppRepository)
    }

    @Test
    fun `writes to the launcher-wide default when the active facet isn't overriding the dock`() = runTest {
        // Given facet 1 active, not overriding its own dock, with 3 already in the default dock
        val fixture = Fixture()
        `when`(fixture.facetRepository.getById(1L)).thenReturn(FacetEntity(id = 1L, name = "P1", position = 0, overrideDock = false))
        `when`(fixture.dockAppRepository.observeDockApps()).thenReturn(flowOf(listOf(app, app, app)))

        // When adding an app
        fixture.useCase(app)

        // Then it's appended to the default dock, at the next position
        verify(fixture.dockAppRepository).addDockApp(app, 3)
    }

    @Test
    fun `writes to the active facet's own dock when it's overriding`() = runTest {
        // Given facet 1 active, overriding its own dock, empty so far
        val fixture = Fixture()
        `when`(fixture.facetRepository.getById(1L)).thenReturn(FacetEntity(id = 1L, name = "P1", position = 0, overrideDock = true))
        `when`(fixture.facetDockAppRepository.observeDockAppsForFacet(1L)).thenReturn(flowOf(emptyList()))

        // When adding an app
        fixture.useCase(app)

        // Then it's added to facet 1's own dock, not the default
        verify(fixture.facetDockAppRepository).addDockApp(1L, app, 0)
    }
}
