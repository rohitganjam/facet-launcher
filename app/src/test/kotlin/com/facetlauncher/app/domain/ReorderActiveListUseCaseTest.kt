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
class ReorderActiveListUseCaseTest {

    private val mail = PlacedItem.SingleApp(AppInfo(packageName = "com.example.mail", activityName = ".Main", label = "Mail", icon = null))
    private val games = PlacedItem.FolderItem(Folder(id = 7L, name = "Games", apps = emptyList()))

    @Test
    fun `the new order is written to the list the active facet resolves`() = runTest {
        listOf(PlacementTarget(PlacementList.DOCK, 1L), PlacementTarget(PlacementList.FAVORITES, 2L)).forEach { target ->
            // Given the active facet resolves the list to its own
            val resolveTarget = mock(ResolveActivePlacementTargetUseCase::class.java)
            val repository = mock(PlacementRepository::class.java)
            `when`(resolveTarget(target.list)).thenReturn(target)

            // When an app and a folder are reordered
            ReorderActiveListUseCase(resolveTarget, repository)(target.list, listOf(games, mail))

            // Then that order is rewritten on the resolved target
            verify(repository).reorderItems(target, listOf(games, mail))
        }
    }
}
