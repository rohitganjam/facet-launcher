package com.facetlauncher.app.domain

import com.facetlauncher.app.data.FacetRepository
import com.facetlauncher.app.data.SettingsRepository
import com.facetlauncher.app.data.model.Facet
import com.facetlauncher.app.data.model.LauncherSettings
import com.facetlauncher.app.data.model.PlacementList
import com.facetlauncher.app.data.model.PlacementTarget
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test
import org.mockito.Mockito.mock
import org.mockito.Mockito.`when`

class ResolveActivePlacementTargetUseCaseTest {

    private fun useCase(facet: Facet?): ResolveActivePlacementTargetUseCase {
        val settingsRepository = mock(SettingsRepository::class.java)
        `when`(settingsRepository.settings).thenReturn(MutableStateFlow(LauncherSettings(activeFacetId = 1L)))
        val facetRepository = mock(FacetRepository::class.java)
        kotlinx.coroutines.runBlocking { `when`(facetRepository.getById(1L)).thenReturn(facet) }
        return ResolveActivePlacementTargetUseCase(settingsRepository, facetRepository)
    }

    private fun facet(overrideDock: Boolean = false, overridingFavorites: Boolean = false) =
        Facet(id = 1L, name = "P1", position = 0, overrideDock = overrideDock, overridingFavorites = overridingFavorites)

    @Test
    fun `a facet overriding its dock resolves the dock to its own list`() = runTest {
        assertEquals(PlacementTarget(PlacementList.DOCK, 1L), useCase(facet(overrideDock = true))(PlacementList.DOCK))
    }

    @Test
    fun `a facet not overriding its dock resolves the dock to the launcher-wide list`() = runTest {
        assertEquals(PlacementTarget.GlobalDock, useCase(facet(overrideDock = false, overridingFavorites = true))(PlacementList.DOCK))
    }

    @Test
    fun `a facet overriding its favorites resolves favorites to its own list`() = runTest {
        assertEquals(PlacementTarget(PlacementList.FAVORITES, 1L), useCase(facet(overridingFavorites = true))(PlacementList.FAVORITES))
    }

    @Test
    fun `a facet not overriding its favorites resolves favorites to the launcher-wide list`() = runTest {
        assertEquals(PlacementTarget.GlobalFavorites, useCase(facet(overrideDock = true, overridingFavorites = false))(PlacementList.FAVORITES))
    }

    @Test
    fun `no active facet resolves both lists to the launcher-wide ones`() = runTest {
        val useCase = useCase(null)
        assertEquals(PlacementTarget.GlobalDock, useCase(PlacementList.DOCK))
        assertEquals(PlacementTarget.GlobalFavorites, useCase(PlacementList.FAVORITES))
    }
}
