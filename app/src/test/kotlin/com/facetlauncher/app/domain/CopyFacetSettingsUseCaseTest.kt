package com.facetlauncher.app.domain

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.facetlauncher.app.data.FacetRepository
import com.facetlauncher.app.data.PlacementRepository
import com.facetlauncher.app.data.local.AppPlacementEntity
import com.facetlauncher.app.data.local.FacetDatabase
import com.facetlauncher.app.data.local.FacetEntity
import com.facetlauncher.app.data.local.FolderEntity
import com.facetlauncher.app.data.local.FolderPlacementEntity
import com.facetlauncher.app.data.model.AppListLayout
import com.facetlauncher.app.data.model.AppRowPosition
import com.facetlauncher.app.data.model.DockDisplayMode
import com.facetlauncher.app.data.model.PlacementList
import com.facetlauncher.app.data.model.PlacementTarget
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class CopyFacetSettingsUseCaseTest {

    private lateinit var database: FacetDatabase
    private lateinit var facets: FacetRepository
    private lateinit var placements: PlacementRepository
    private lateinit var useCase: CopyFacetSettingsUseCase
    private var source = 0L
    private var target = 0L

    @Before
    fun setUp() = runTest {
        database = Room.inMemoryDatabaseBuilder(ApplicationProvider.getApplicationContext(), FacetDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        facets = FacetRepository(database.facetDao(), database.clockStackItemDao())
        placements = PlacementRepository(database.appPlacementDao(), database.folderPlacementDao())
        useCase = CopyFacetSettingsUseCase(placements, facets)
        source = database.facetDao().insert(FacetEntity(name = "Work", position = 0))
        target = database.facetDao().insert(FacetEntity(name = "Play", position = 1))
    }

    @After
    fun tearDown() = database.close()

    private fun app(list: PlacementList, facetId: Long, name: String, position: Int, row: Int? = null) = AppPlacementEntity(
        list = list, facetId = facetId, packageName = "com.example.$name", activityName = ".Main", position = position, userId = 0,
        portraitRow = row, portraitColumn = row,
    )

    @Test
    fun `copying the Dock replaces the target's apps and keeps the source's order and cells`() = runTest {
        // Given a source Dock of two apps (one on a saved cell) and a target Dock with an app of its own
        database.appPlacementDao().upsert(app(PlacementList.DOCK, source, "mail", 0))
        database.appPlacementDao().upsert(app(PlacementList.DOCK, source, "maps", 1, row = 2))
        database.appPlacementDao().upsert(app(PlacementList.DOCK, target, "old", 0))

        // When the target copies the source's Dock
        useCase.copyDock(source, target)

        // Then the target holds the source's apps in order with their cells, and its own app is gone
        val copied = placements.getRawApps(PlacementTarget(PlacementList.DOCK, target))
        assertEquals(listOf("com.example.mail", "com.example.maps"), copied.map { it.packageName })
        assertEquals(listOf(null, 2), copied.map { it.portraitRow })
        assertEquals(setOf(target), copied.map { it.facetId }.toSet())
    }

    @Test
    fun `copying the Dock leaves the source untouched and carries folders and the display style`() = runTest {
        // Given a source Dock with an app and a folder, shown as text
        val folderId = database.folderDao().insertFolder(FolderEntity(name = "Tools"))
        database.appPlacementDao().upsert(app(PlacementList.DOCK, source, "mail", 0))
        database.folderPlacementDao().upsert(FolderPlacementEntity(list = PlacementList.DOCK, facetId = source, folderId = folderId, position = 1))
        facets.setDockDisplayMode(source, DockDisplayMode.entries.last())

        // When copied
        useCase.copyDock(source, target)

        // Then the folder and style came over and the source still has its own rows
        assertEquals(listOf(folderId), placements.getRawFolderPlacements(PlacementTarget(PlacementList.DOCK, target)).map { it.folderId })
        assertEquals(DockDisplayMode.entries.last(), facets.getById(target)?.dockDisplayMode)
        assertEquals(1, placements.getRawApps(PlacementTarget(PlacementList.DOCK, source)).size)
        assertEquals(1, placements.getRawFolderPlacements(PlacementTarget(PlacementList.DOCK, source)).size)
    }

    @Test
    fun `copying the Apps list takes Favorites and the look but not the Dock`() = runTest {
        // Given a source with Favorites, its own look and a Dock app
        database.appPlacementDao().upsert(app(PlacementList.FAVORITES, source, "notes", 0))
        database.appPlacementDao().upsert(app(PlacementList.DOCK, source, "mail", 0))
        facets.setAppRowPosition(source, AppRowPosition.RIGHT)
        facets.setAppListLayout(source, AppListLayout.entries.last())
        facets.setAllowLandscape(source, false)

        // When the Apps list is copied
        useCase.copyAppsList(source, target)

        // Then Favorites and the look follow, and the target's Dock stays empty
        val copied = facets.getById(target)
        assertEquals(listOf("com.example.notes"), placements.getRawApps(PlacementTarget(PlacementList.FAVORITES, target)).map { it.packageName.substringAfterLast('.') }.map { "com.example.$it" })
        assertEquals(AppRowPosition.RIGHT, copied?.appRowPosition)
        assertEquals(AppListLayout.entries.last(), copied?.appListLayout)
        assertEquals(false, copied?.allowLandscape)
        assertEquals(0, placements.getRawApps(PlacementTarget(PlacementList.DOCK, target)).size)
    }

    @Test
    fun `copying a facet onto itself changes nothing`() = runTest {
        // Given a Dock app
        database.appPlacementDao().upsert(app(PlacementList.DOCK, source, "mail", 0))

        // When the facet copies from itself
        useCase.copyDock(source, source)

        // Then its Dock is intact
        assertNotNull(placements.getRawApps(PlacementTarget(PlacementList.DOCK, source)).singleOrNull())
    }
}
