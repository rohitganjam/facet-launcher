package com.facetlauncher.app.domain

import com.facetlauncher.app.data.DefaultFavoriteAppRepository
import com.facetlauncher.app.data.DockAppRepository
import com.facetlauncher.app.data.FavoriteAppRepository
import com.facetlauncher.app.data.FacetDockAppRepository
import com.facetlauncher.app.data.FacetRepository
import com.facetlauncher.app.data.SettingsRepository
import com.facetlauncher.app.data.local.FacetEntity
import com.facetlauncher.app.data.model.AppInfo
import com.facetlauncher.app.data.model.Folder
import com.facetlauncher.app.data.model.LauncherSettings
import com.facetlauncher.app.data.model.PlacedItem
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test
import org.mockito.Mockito.mock
import org.mockito.Mockito.`when`

class ObserveQuickAddStateUseCaseTest {

    private fun appInfo(letter: Char) =
        AppInfo(packageName = "com.example.$letter", activityName = ".Main", label = "$letter App", icon = null)

    private val app = appInfo('z')

    private class Fixture {
        val settingsFlow = MutableStateFlow(LauncherSettings(activeFacetId = 1L))
        val settingsRepository = mock(SettingsRepository::class.java).also { `when`(it.settings).thenReturn(settingsFlow) }
        val facet = FacetEntity(id = 1L, name = "P1", position = 0)
        val facetRepository = mock(FacetRepository::class.java).also {
            kotlinx.coroutines.runBlocking { `when`(it.getById(1L)).thenReturn(facet) }
        }
        val favoriteAppRepository = mock(FavoriteAppRepository::class.java)
        val defaultFavoriteAppRepository = mock(DefaultFavoriteAppRepository::class.java)
            .also { `when`(it.observeDefaultItems()).thenReturn(flowOf(emptyList())) }
        val dockAppRepository = mock(DockAppRepository::class.java).also { `when`(it.observeDockItems()).thenReturn(flowOf(emptyList())) }
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
    fun `forApp shows Add against the launcher-wide default when the active facet isn't overriding`() = runTest {
        val fixture = Fixture()

        val result = fixture.useCase.forApp(app)

        assertEquals(QuickPlacementAction.Add(facetName = null), result.favoritesAction)
    }

    @Test
    fun `forApp shows Add to facet favorites when the active facet overrides its own`() = runTest {
        val fixture = Fixture()
        val overridingFacet = fixture.facet.copy(overridingFavorites = true)
        `when`(fixture.facetRepository.getById(1L)).thenReturn(overridingFacet)
        `when`(fixture.favoriteAppRepository.observeFavoriteItems(1L)).thenReturn(flowOf(emptyList()))

        val result = fixture.useCase.forApp(app)

        assertEquals(QuickPlacementAction.Add(facetName = "P1"), result.favoritesAction)
    }

    @Test
    fun `forApp hides Favorites row once the effective list is already at the cap and app isn't a member`() = runTest {
        val fixture = Fixture()
        val fullFavorites = (1..6).map { PlacedItem.SingleApp(appInfo('a' + it)) }
        `when`(fixture.defaultFavoriteAppRepository.observeDefaultItems()).thenReturn(flowOf(fullFavorites))

        val result = fixture.useCase.forApp(app)

        assertEquals(null, result.favoritesAction)
    }

    @Test
    fun `forApp shows Remove from Favorites when the app is already a member, even at the cap`() = runTest {
        val fixture = Fixture()
        val fullFavoritesWithApp = listOf(PlacedItem.SingleApp(app)) + (1..5).map { PlacedItem.SingleApp(appInfo('a' + it)) }
        `when`(fixture.defaultFavoriteAppRepository.observeDefaultItems()).thenReturn(flowOf(fullFavoritesWithApp))

        val result = fixture.useCase.forApp(app)

        assertEquals(QuickPlacementAction.Remove(facetName = null), result.favoritesAction)
    }

    @Test
    fun `forApp shows Add to Dock against the launcher-wide default when the active facet isn't overriding`() = runTest {
        val fixture = Fixture()

        val result = fixture.useCase.forApp(app)

        assertEquals(QuickPlacementAction.Add(facetName = null), result.dockAction)
    }

    @Test
    fun `forApp shows Add to facet dock when the active facet overrides its own`() = runTest {
        val fixture = Fixture()
        val overridingFacet = fixture.facet.copy(overrideDock = true)
        `when`(fixture.facetRepository.getById(1L)).thenReturn(overridingFacet)
        `when`(fixture.facetDockAppRepository.observeDockItems(1L)).thenReturn(flowOf(emptyList()))

        val result = fixture.useCase.forApp(app)

        assertEquals(QuickPlacementAction.Add(facetName = "P1"), result.dockAction)
    }

    @Test
    fun `forApp hides Dock row once the effective dock is already at the cap and app isn't a member`() = runTest {
        val fixture = Fixture()
        val fullDock = (1..5).map { PlacedItem.SingleApp(appInfo('a' + it)) }
        `when`(fixture.dockAppRepository.observeDockItems()).thenReturn(flowOf(fullDock))

        val result = fixture.useCase.forApp(app)

        assertEquals(null, result.dockAction)
    }

    @Test
    fun `forApp shows Remove from Dock when the app is already a member, even at the cap`() = runTest {
        val fixture = Fixture()
        val fullDockWithApp = listOf(PlacedItem.SingleApp(app)) + (1..4).map { PlacedItem.SingleApp(appInfo('a' + it)) }
        `when`(fixture.dockAppRepository.observeDockItems()).thenReturn(flowOf(fullDockWithApp))

        val result = fixture.useCase.forApp(app)

        assertEquals(QuickPlacementAction.Remove(facetName = null), result.dockAction)
    }

    @Test
    fun `forFolder shows Add when the folder isn't yet placed`() = runTest {
        val fixture = Fixture()
        val folder = Folder(id = 9L, name = "Games", apps = emptyList())

        val result = fixture.useCase.forFolder(folder)

        assertEquals(QuickPlacementAction.Add(facetName = null), result.favoritesAction)
        assertEquals(QuickPlacementAction.Add(facetName = null), result.dockAction)
    }

    @Test
    fun `forFolder shows Remove when the folder is already placed in Favorites and Dock`() = runTest {
        val fixture = Fixture()
        val folder = Folder(id = 9L, name = "Games", apps = emptyList())
        `when`(fixture.defaultFavoriteAppRepository.observeDefaultItems()).thenReturn(flowOf(listOf(PlacedItem.FolderItem(folder))))
        `when`(fixture.dockAppRepository.observeDockItems()).thenReturn(flowOf(listOf(PlacedItem.FolderItem(folder))))

        val result = fixture.useCase.forFolder(folder)

        assertEquals(QuickPlacementAction.Remove(facetName = null), result.favoritesAction)
        assertEquals(QuickPlacementAction.Remove(facetName = null), result.dockAction)
    }
}
