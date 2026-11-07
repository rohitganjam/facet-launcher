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

    private fun facet() = Facet(id = 1L, name = "P1", position = 0)

    @Test
    fun `the active facet's dock resolves to its own list`() = runTest {
        assertEquals(PlacementTarget(PlacementList.DOCK, 1L), useCase(facet())(PlacementList.DOCK))
    }

    @Test
    fun `the active facet's favorites resolve to its own list`() = runTest {
        assertEquals(PlacementTarget(PlacementList.FAVORITES, 1L), useCase(facet())(PlacementList.FAVORITES))
    }

    @Test
    fun `no active facet resolves both lists to the launcher-wide ones`() = runTest {
        val useCase = useCase(null)
        assertEquals(PlacementTarget.GlobalDock, useCase(PlacementList.DOCK))
        assertEquals(PlacementTarget.GlobalFavorites, useCase(PlacementList.FAVORITES))
    }
}
