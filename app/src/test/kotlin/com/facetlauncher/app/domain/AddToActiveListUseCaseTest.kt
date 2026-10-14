package com.facetlauncher.app.domain

import com.facetlauncher.app.data.PlacementRepository
import com.facetlauncher.app.data.model.AppInfo
import com.facetlauncher.app.data.model.Folder
import com.facetlauncher.app.data.model.PlacedItem
import com.facetlauncher.app.data.model.PlacementList
import com.facetlauncher.app.data.model.PlacementTarget
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify
import org.mockito.Mockito.`when`
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class AddToActiveListUseCaseTest {

    private val app = AppInfo(packageName = "com.example.mail", activityName = ".Main", label = "Mail", icon = null)
    private val folder = Folder(id = 7L, name = "Games", apps = emptyList())

    private class Fixture(val target: PlacementTarget, existingItems: Int) {
        val resolveTarget = mock(ResolveActivePlacementTargetUseCase::class.java)
        val placementRepository = mock(PlacementRepository::class.java)
        val useCase = AddToActiveListUseCase(resolveTarget, placementRepository)

        init {
            kotlinx.coroutines.runBlocking { `when`(resolveTarget(target.list)).thenReturn(target) }
            `when`(placementRepository.observeItems(target)).thenReturn(flowOf(List(existingItems) { PlacedItem.FolderItem(Folder(it.toLong(), "f$it", emptyList())) }))
        }
    }

    @Test
    fun `an app is appended to the resolved list at the next position`() = runTest {
        listOf(PlacementTarget.GlobalDock, PlacementTarget(PlacementList.DOCK, 1L), PlacementTarget.GlobalFavorites, PlacementTarget(PlacementList.FAVORITES, 1L)).forEach { target ->
            // Given the resolved list already holds 3 items
            val fixture = Fixture(target, existingItems = 3)

            // When adding an app to that list
            fixture.useCase(target.list, PlacedItem.SingleApp(app))

            // Then it is written to the resolved target, after the existing items
            verify(fixture.placementRepository).addApp(target, app, 3)
        }
    }

    @Test
    fun `a folder is placed in the resolved list at the next position`() = runTest {
        listOf(PlacementTarget.GlobalDock, PlacementTarget(PlacementList.DOCK, 1L), PlacementTarget.GlobalFavorites, PlacementTarget(PlacementList.FAVORITES, 1L)).forEach { target ->
            // Given the resolved list is empty
            val fixture = Fixture(target, existingItems = 0)

            // When adding a folder
            fixture.useCase(target.list, PlacedItem.FolderItem(folder))

            // Then the folder is placed at position 0 of the resolved target
            verify(fixture.placementRepository).placeFolder(target, folder.id, 0)
        }
    }

    @Test
    fun `the position counts folders as well as apps`() = runTest {
        // Given a list holding only folders, which an app-only count would read as empty
        val fixture = Fixture(PlacementTarget.GlobalDock, existingItems = 2)

        // When adding an app
        fixture.useCase(PlacementList.DOCK, PlacedItem.SingleApp(app))

        // Then it lands after both folders
        verify(fixture.placementRepository).addApp(PlacementTarget.GlobalDock, app, 2)
    }
}
