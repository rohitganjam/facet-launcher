package com.facetlauncher.app.domain

import com.facetlauncher.app.data.PlacementRepository
import com.facetlauncher.app.data.model.AppInfo
import com.facetlauncher.app.data.model.Folder
import com.facetlauncher.app.data.model.PlacedItem
import com.facetlauncher.app.data.model.PlacementList
import com.facetlauncher.app.data.model.PlacementTarget
import kotlinx.coroutines.test.runTest
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify
import org.mockito.Mockito.`when`
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class RemoveFromActiveListUseCaseTest {

    private val app = AppInfo(packageName = "com.example.mail", activityName = ".Main", label = "Mail", icon = null)
    private val folder = Folder(id = 7L, name = "Games", apps = emptyList())
    private val targets = listOf(PlacementTarget.GlobalDock, PlacementTarget(PlacementList.DOCK, 1L), PlacementTarget.GlobalFavorites, PlacementTarget(PlacementList.FAVORITES, 1L))

    private fun fixture(target: PlacementTarget): Pair<RemoveFromActiveListUseCase, PlacementRepository> {
        val resolveTarget = mock(ResolveActivePlacementTargetUseCase::class.java)
        kotlinx.coroutines.runBlocking { `when`(resolveTarget(target.list)).thenReturn(target) }
        val placementRepository = mock(PlacementRepository::class.java)
        return RemoveFromActiveListUseCase(resolveTarget, placementRepository) to placementRepository
    }

    @Test
    fun `an app is removed from the resolved list`() = runTest {
        targets.forEach { target ->
            // Given the list resolves to this target
            val (useCase, repository) = fixture(target)

            // When removing an app from that list
            useCase(target.list, PlacedItem.SingleApp(app))

            // Then it is removed from the resolved target
            verify(repository).removeApp(target, app)
        }
    }

    @Test
    fun `a folder is un-placed from the resolved list`() = runTest {
        targets.forEach { target ->
            // Given the list resolves to this target
            val (useCase, repository) = fixture(target)

            // When removing a folder from that list
            useCase(target.list, PlacedItem.FolderItem(folder))

            // Then its placement is removed from the resolved target
            verify(repository).removeFolderPlacement(target, folder.id)
        }
    }
}
