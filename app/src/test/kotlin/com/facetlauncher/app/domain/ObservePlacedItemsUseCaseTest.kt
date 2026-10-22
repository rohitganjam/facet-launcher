package com.facetlauncher.app.domain

import com.facetlauncher.app.data.AppRepository
import com.facetlauncher.app.data.FolderRepository
import com.facetlauncher.app.data.PlacementRepository
import com.facetlauncher.app.data.local.AppPlacementEntity
import com.facetlauncher.app.data.local.FolderPlacementEntity
import com.facetlauncher.app.data.model.AppInfo
import com.facetlauncher.app.data.model.Folder
import com.facetlauncher.app.data.model.PlacedItem
import com.facetlauncher.app.data.model.PlacementList
import com.facetlauncher.app.data.model.PlacementTarget
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito.mock
import org.mockito.Mockito.`when`
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class ObservePlacedItemsUseCaseTest {

    private val target = PlacementTarget.GlobalDock

    private fun appInfo(letter: Char) =
        AppInfo(packageName = "com.example.$letter", activityName = ".Main", label = "$letter App", icon = null)

    private fun appEntity(app: AppInfo, position: Int) =
        AppPlacementEntity(list = target.list, packageName = app.packageName, activityName = app.activityName, position = position, userId = 0)

    private class Fixture(
        installed: Flow<List<AppInfo>>,
        folders: List<Folder> = emptyList(),
    ) {
        val placementRepository = mock(PlacementRepository::class.java)
        val useCase = ObservePlacedItemsUseCase(
            placementRepository,
            mock(FolderRepository::class.java).also { `when`(it.observeFolders()).thenReturn(flowOf(folders)) },
            mock(AppRepository::class.java).also { `when`(it.observeInstalledApps()).thenReturn(installed) },
        )
    }

    @Test
    fun `apps are hydrated against installed apps and ordered by position`() = runTest {
        // Given two installed apps stored out of position order
        val a = appInfo('a')
        val b = appInfo('b')
        val f = Fixture(flowOf(listOf(a, b)))
        `when`(f.placementRepository.observeAppEntities(target)).thenReturn(flowOf(listOf(appEntity(b, 1), appEntity(a, 0))))

        // Then the hydrated list comes back ordered by position
        assertEquals(listOf(a, b), f.useCase.apps(target).first())
    }

    @Test
    fun `an entry whose app is no longer installed is filtered out`() = runTest {
        // Given a stored entry for an app that is not installed
        val f = Fixture(flowOf(emptyList()))
        `when`(f.placementRepository.observeAppEntities(target)).thenReturn(flowOf(listOf(appEntity(appInfo('x'), 0))))

        // Then it is collapsed out, not shown as a dead tile
        assertEquals(emptyList<AppInfo>(), f.useCase.apps(target).first())
    }

    @Test
    fun `the list collapses live when an app is uninstalled, without resubscribing`() = runTest {
        val a = appInfo('a')
        val installed = MutableStateFlow(listOf(a))
        val f = Fixture(installed)
        `when`(f.placementRepository.observeAppEntities(target)).thenReturn(flowOf(listOf(appEntity(a, 0))))

        val emissions = Channel<List<AppInfo>>(Channel.UNLIMITED)
        val collectJob = launch { f.useCase.apps(target).collect { emissions.send(it) } }
        assertEquals(listOf(a), emissions.receive())

        // When the live installed list reports it gone
        installed.value = emptyList()

        // Then it collapses out immediately
        assertEquals(emptyList<AppInfo>(), emissions.receive())
        collectJob.cancel()
    }

    @Test
    fun `items merge apps and placed folders sorted by position and drop a placement whose folder is gone`() = runTest {
        // Given an app at position 1, a folder at position 0 and a placement for a deleted folder
        val a = appInfo('a')
        val folder = Folder(7L, "Games", listOf(appInfo('b')))
        val f = Fixture(flowOf(listOf(a)), folders = listOf(folder))
        `when`(f.placementRepository.observeAppEntities(target)).thenReturn(flowOf(listOf(appEntity(a, 1))))
        `when`(f.placementRepository.observeFolderPlacements(target)).thenReturn(
            flowOf(
                listOf(
                    FolderPlacementEntity(list = target.list, folderId = 7L, position = 0),
                    FolderPlacementEntity(list = target.list, folderId = 99L, position = 2),
                ),
            ),
        )

        // Then the folder precedes the app and the orphaned placement is omitted
        assertEquals(listOf(PlacedItem.FolderItem(folder), PlacedItem.SingleApp(a)), f.useCase.items(target).first())
    }

    @Test
    fun `the batched reads return each facet's own list keyed by id`() = runTest {
        // Given facet 1 and facet 2 holding different apps
        val a = appInfo('a')
        val b = appInfo('b')
        val f = Fixture(flowOf(listOf(a, b)))
        `when`(f.placementRepository.observeAppEntitiesForFacets(PlacementList.DOCK, listOf(1L, 2L)))
            .thenReturn(flowOf(listOf(listOf(appEntity(a, 0)), listOf(appEntity(b, 0)))))
        `when`(f.placementRepository.observeFolderPlacementsForFacets(PlacementList.DOCK, listOf(1L, 2L)))
            .thenReturn(flowOf(listOf(emptyList(), emptyList())))

        // Then each facet id maps to its own hydrated list
        assertEquals(mapOf(1L to listOf(a), 2L to listOf(b)), f.useCase.appsForFacets(PlacementList.DOCK, listOf(1L, 2L)).first())
        assertEquals(
            mapOf(1L to listOf<PlacedItem>(PlacedItem.SingleApp(a)), 2L to listOf<PlacedItem>(PlacedItem.SingleApp(b))),
            f.useCase.itemsForFacets(PlacementList.DOCK, listOf(1L, 2L)).first(),
        )
    }

    @Test
    fun `the batched reads with an empty id list emit an empty map`() = runTest {
        val f = Fixture(flowOf(emptyList()))

        assertEquals(emptyMap<Long, List<AppInfo>>(), f.useCase.appsForFacets(PlacementList.DOCK, emptyList()).first())
        assertEquals(emptyMap<Long, List<PlacedItem>>(), f.useCase.itemsForFacets(PlacementList.DOCK, emptyList()).first())
    }
}
