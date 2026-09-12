package com.facetlauncher.app.domain

import com.facetlauncher.app.data.DefaultFavoriteAppRepository
import com.facetlauncher.app.data.DockAppRepository
import com.facetlauncher.app.data.FavoriteAppRepository
import com.facetlauncher.app.data.FacetDockAppRepository
import com.facetlauncher.app.data.FacetRepository
import com.facetlauncher.app.data.SettingsRepository
import com.facetlauncher.app.data.local.FacetEntity
import com.facetlauncher.app.data.model.AppInfo
import com.facetlauncher.app.data.model.LauncherSettings
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test
import org.mockito.Mockito.mock
import org.mockito.Mockito.`when`

class ObserveQuickAddStateUseCaseTest {

    private fun appInfo(letter: Char) =
        AppInfo(packageName = "com.example.$letter", activityName = ".Main", label = "$letter App", icon = null)

    private class Fixture {
        val settingsFlow = MutableStateFlow(LauncherSettings(activeFacetId = 1L))
        val settingsRepository = mock(SettingsRepository::class.java).also { `when`(it.settings).thenReturn(settingsFlow) }
        val facetsFlow = MutableStateFlow<List<FacetEntity>>(listOf(FacetEntity(id = 1L, name = "P1", position = 0)))
        val facetRepository = mock(FacetRepository::class.java).also { `when`(it.observeFacets()).thenReturn(facetsFlow) }
        val favoriteAppRepository = mock(FavoriteAppRepository::class.java)
        val defaultFavoriteAppRepository = mock(DefaultFavoriteAppRepository::class.java)
            .also { `when`(it.observeDefaultFavorites()).thenReturn(flowOf(emptyList())) }
        val dockAppRepository = mock(DockAppRepository::class.java).also { `when`(it.observeDockApps()).thenReturn(flowOf(emptyList())) }
        val facetDockAppRepository = mock(FacetDockAppRepository::class.java)

        val useCase = ObserveQuickAddStateUseCase(
            settingsRepository,
            facetRepository,
            favoriteAppRepository,
            defaultFavoriteAppRepository,
            dockAppRepository,
            facetDockAppRepository,
        )
    }

    @Test
    fun `shows Add to Favorites against the launcher-wide default when the active facet isn't overriding`() = runTest {
        val fixture = Fixture()

        val result = fixture.useCase().first()

        assertEquals(false, result.favoritesOverride)
    }

    @Test
    fun `shows Add to facet favorites when the active facet overrides its own`() = runTest {
        val fixture = Fixture()
        fixture.facetsFlow.value = listOf(FacetEntity(id = 1L, name = "P1", position = 0, overridingFavorites = true))
        `when`(fixture.favoriteAppRepository.observeFavoritesForFacet(1L)).thenReturn(flowOf(emptyList()))

        val result = fixture.useCase().first()

        assertEquals(true, result.favoritesOverride)
    }

    @Test
    fun `hides Add to Favorites once the effective list is already at the cap`() = runTest {
        val fixture = Fixture()
        val fullFavorites = (1..6).map { appInfo('a' + it) }
        `when`(fixture.defaultFavoriteAppRepository.observeDefaultFavorites()).thenReturn(flowOf(fullFavorites))

        val result = fixture.useCase().first()

        assertEquals(null, result.favoritesOverride)
    }

    @Test
    fun `shows Add to Dock against the launcher-wide default when the active facet isn't overriding`() = runTest {
        val fixture = Fixture()

        val result = fixture.useCase().first()

        assertEquals(false, result.dockOverride)
    }

    @Test
    fun `shows Add to facet dock when the active facet overrides its own`() = runTest {
        val fixture = Fixture()
        fixture.facetsFlow.value = listOf(FacetEntity(id = 1L, name = "P1", position = 0, overrideDock = true))
        `when`(fixture.facetDockAppRepository.observeDockAppsForFacet(1L)).thenReturn(flowOf(emptyList()))

        val result = fixture.useCase().first()

        assertEquals(true, result.dockOverride)
    }

    @Test
    fun `hides Add to Dock once the effective dock is already at the cap`() = runTest {
        val fixture = Fixture()
        val fullDock = (1..5).map { appInfo('a' + it) }
        `when`(fixture.dockAppRepository.observeDockApps()).thenReturn(flowOf(fullDock))

        val result = fixture.useCase().first()

        assertEquals(null, result.dockOverride)
    }
}
