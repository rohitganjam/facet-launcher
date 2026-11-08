package com.facetlauncher.app.domain

import com.facetlauncher.app.data.FacetRepository
import com.facetlauncher.app.data.SettingsRepository
import com.facetlauncher.app.data.model.AppListLimits
import com.facetlauncher.app.data.model.Facet
import com.facetlauncher.app.data.model.Folder
import com.facetlauncher.app.data.model.LauncherSettings
import com.facetlauncher.app.data.model.PlacedItem
import com.facetlauncher.app.data.model.PlacementList
import com.facetlauncher.app.data.model.PlacementTarget
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito.mock
import org.mockito.Mockito.`when`
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class ObserveFolderPlacementUseCaseTest {

    private val folder = PlacedItem.FolderItem(Folder(id = 5L, name = "Games", apps = emptyList()))

    private fun useCase(
        facets: List<Facet>,
        activeFacetId: Long,
        dockByFacet: Map<Long, List<PlacedItem>> = emptyMap(),
        favoritesByFacet: Map<Long, List<PlacedItem>> = emptyMap(),
    ): ObserveFolderPlacementUseCase {
        val settingsRepository = mock(SettingsRepository::class.java)
        `when`(settingsRepository.settings).thenReturn(MutableStateFlow(LauncherSettings(activeFacetId = activeFacetId)))
        val facetRepository = mock(FacetRepository::class.java)
        `when`(facetRepository.observeFacets()).thenReturn(flowOf(facets))
        val observePlacedItems = mock(ObservePlacedItemsUseCase::class.java)
        facets.forEach { facet ->
            `when`(observePlacedItems.items(PlacementTarget(PlacementList.DOCK, facet.id))).thenReturn(flowOf(dockByFacet[facet.id].orEmpty()))
            `when`(observePlacedItems.items(PlacementTarget(PlacementList.FAVORITES, facet.id))).thenReturn(flowOf(favoritesByFacet[facet.id].orEmpty()))
        }
        return ObserveFolderPlacementUseCase(settingsRepository, facetRepository, observePlacedItems, ObserveQuickAddStateUseCase())
    }

    @Test
    fun `a folder in the active facet's Dock offers Remove and one outside it offers Add`() = runTest {
        // Given two facets, the folder in Home's Dock but not its Favorites
        val home = Facet(id = 1L, name = "Home", position = 0)
        val work = Facet(id = 2L, name = "Work", position = 1)
        val useCase = useCase(listOf(home, work), activeFacetId = 1L, dockByFacet = mapOf(1L to listOf(folder)))

        // When the state is read
        val state = useCase(folderId = 5L).first()

        // Then Dock offers Remove, Favorites offers Add, and the menu names the facet
        assertTrue(state.quickAdd.dockAction is QuickPlacementAction.Remove)
        assertTrue(state.quickAdd.favoritesAction is QuickPlacementAction.Add)
        assertEquals("Home", state.facetName)
        assertTrue(state.showFacetBadge)
    }

    @Test
    fun `only the active facet's lists are read`() = runTest {
        // Given the folder placed in Work's Dock while Home is active
        val home = Facet(id = 1L, name = "Home", position = 0)
        val work = Facet(id = 2L, name = "Work", position = 1)
        val useCase = useCase(listOf(home, work), activeFacetId = 1L, dockByFacet = mapOf(2L to listOf(folder)))

        // When the state is read
        val state = useCase(folderId = 5L).first()

        // Then Home's Dock still offers Add
        assertTrue(state.quickAdd.dockAction is QuickPlacementAction.Add)
    }

    @Test
    fun `a full Dock offers nothing for a folder not in it`() = runTest {
        // Given the Dock already at its cap with other folders
        val home = Facet(id = 1L, name = "Home", position = 0)
        val others = (1..AppListLimits.MAX_DOCK_APPS).map { PlacedItem.FolderItem(Folder(id = 100L + it, name = "F$it", apps = emptyList())) }
        val useCase = useCase(listOf(home), activeFacetId = 1L, dockByFacet = mapOf(1L to others))

        // When the state is read
        val state = useCase(folderId = 5L).first()

        // Then the Dock row is hidden
        assertNull(state.quickAdd.dockAction)
    }

    @Test
    fun `a single facet hides the badge`() = runTest {
        // Given one facet only
        val home = Facet(id = 1L, name = "Home", position = 0)
        val useCase = useCase(listOf(home), activeFacetId = 1L)

        // When the state is read
        val state = useCase(folderId = 5L).first()

        // Then the facet name is known but the badge is off
        assertEquals("Home", state.facetName)
        assertFalse(state.showFacetBadge)
    }
}
