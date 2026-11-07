package com.facetlauncher.app.domain

import com.facetlauncher.app.data.FacetRepository
import com.facetlauncher.app.data.FakeAppPlacementDao
import com.facetlauncher.app.data.FakeClockStackItemDao
import com.facetlauncher.app.data.FakeFolderPlacementDao
import com.facetlauncher.app.data.PlacementRepository
import com.facetlauncher.app.data.SettingsRepository
import com.facetlauncher.app.data.local.AppPlacementEntity
import com.facetlauncher.app.data.local.FacetDao
import com.facetlauncher.app.data.local.FacetEntity
import com.facetlauncher.app.data.model.AppListColumnAlignment
import com.facetlauncher.app.data.model.AppListLayout
import com.facetlauncher.app.data.model.AppListVerticalAlignment
import com.facetlauncher.app.data.model.AppRowPosition
import com.facetlauncher.app.data.model.AppRowPresentation
import com.facetlauncher.app.data.model.ClockAlignment
import com.facetlauncher.app.data.model.DockDisplayMode
import com.facetlauncher.app.data.model.LandscapeOverrides
import com.facetlauncher.app.data.model.LauncherSettings
import com.facetlauncher.app.data.model.ListContentMode
import com.facetlauncher.app.data.model.PlacementList
import com.facetlauncher.app.data.model.PlacementTarget
import com.facetlauncher.app.data.model.landscape
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import org.mockito.Mockito.mock
import org.mockito.Mockito.`when`

private class InMemoryFacetDao : FacetDao {
    private var nextId = 1L
    private val state = MutableStateFlow<List<FacetEntity>>(emptyList())
    override fun observeAll(): Flow<List<FacetEntity>> = state
    override suspend fun insert(facet: FacetEntity): Long {
        val id = if (facet.id != 0L) facet.id else nextId++
        state.value = state.value.filterNot { it.id == id } + facet.copy(id = id)
        return id
    }
    override suspend fun update(facet: FacetEntity) {
        state.value = state.value.map { if (it.id == facet.id) facet else it }
    }
    override suspend fun delete(facet: FacetEntity) {
        state.value = state.value.filterNot { it.id == facet.id }
    }
    override suspend fun getById(id: Long): FacetEntity? = state.value.find { it.id == id }
    override suspend fun deleteAll() { state.value = emptyList() }
}

class ApplyFacetDefaultsUseCaseTest {

    private val facetRepository = FacetRepository(InMemoryFacetDao(), FakeClockStackItemDao())
    private val placementRepository = PlacementRepository(FakeAppPlacementDao(), FakeFolderPlacementDao())

    private val launcherSettings = LauncherSettings(
        dockDisplayMode = DockDisplayMode.TEXT,
        appRowPosition = AppRowPosition.RIGHT,
        appRowPresentation = AppRowPresentation.TEXT_ONLY,
        listContentMode = ListContentMode.RECENTS,
        appsToShowCount = 9,
        appListVerticalAlignment = AppListVerticalAlignment.TOP,
        appListLayout = AppListLayout.TWO_COLUMN,
        appListColumnAlignment = AppListColumnAlignment.MIRRORED,
        landscape = LandscapeOverrides(appRowPosition = AppRowPosition.CENTER),
    )

    private fun useCase(settings: LauncherSettings = launcherSettings): ApplyFacetDefaultsUseCase {
        val settingsRepository = mock(SettingsRepository::class.java)
        `when`(settingsRepository.settings).thenReturn(MutableStateFlow(settings))
        return ApplyFacetDefaultsUseCase(facetRepository, placementRepository, settingsRepository)
    }

    private fun app(letter: Char) = "com.example.$letter"

    // Rows are written directly: an AppInfo would need Robolectric for its UserHandle.
    private suspend fun PlacementRepository.addApp(target: PlacementTarget, packageName: String, position: Int) =
        restoreApp(AppPlacementEntity(list = target.list, facetId = target.facetId, packageName = packageName, activityName = ".Main", position = position))

    private suspend fun packages(list: PlacementList, facetId: Long?) =
        placementRepository.getRawApps(PlacementTarget(list, facetId)).map { it.packageName }

    @Test
    fun `dock scope copies the launcher dock into the facet and nothing else`() = runTest {
        // Given a launcher dock and favorites, and a facet with its own stale dock
        val facet = facetRepository.addFacet()
        placementRepository.addApp(PlacementTarget.GlobalDock, app('a'), 0)
        placementRepository.addApp(PlacementTarget.GlobalDock, app('b'), 1)
        placementRepository.addApp(PlacementTarget.GlobalFavorites, app('f'), 0)
        placementRepository.addApp(PlacementTarget(PlacementList.DOCK, facet.id), app('z'), 0)

        // When the dock scope is applied
        useCase()(facet.id, FacetDefaultsScope.DOCK)

        // Then the facet's dock is a copy of the launcher dock, and favorites and look are untouched
        assertEquals(listOf("com.example.a", "com.example.b"), packages(PlacementList.DOCK, facet.id))
        assertEquals(emptyList<String>(), packages(PlacementList.FAVORITES, facet.id))
        assertEquals(DockDisplayMode.LAUNCHER_DEFAULT, facetRepository.getById(facet.id)!!.dockDisplayMode)
    }

    @Test
    fun `an empty launcher dock empties the facet dock`() = runTest {
        val facet = facetRepository.addFacet()
        placementRepository.addApp(PlacementTarget(PlacementList.DOCK, facet.id), app('z'), 0)

        useCase()(facet.id, FacetDefaultsScope.DOCK)

        assertEquals(emptyList<String>(), packages(PlacementList.DOCK, facet.id))
    }

    @Test
    fun `a copied list is independent of the launcher list afterwards`() = runTest {
        val facet = facetRepository.addFacet()
        placementRepository.addApp(PlacementTarget.GlobalDock, app('a'), 0)
        useCase()(facet.id, FacetDefaultsScope.DOCK)

        placementRepository.addApp(PlacementTarget.GlobalDock, app('b'), 1)

        assertEquals(listOf("com.example.a"), packages(PlacementList.DOCK, facet.id))
        assertEquals(listOf("com.example.a", "com.example.b"), packages(PlacementList.DOCK, null))
    }

    @Test
    fun `apps list scope copies favorites with content mode and count`() = runTest {
        val facet = facetRepository.addFacet()
        placementRepository.addApp(PlacementTarget.GlobalFavorites, app('f'), 0)
        placementRepository.addApp(PlacementTarget.GlobalDock, app('d'), 0)

        useCase()(facet.id, FacetDefaultsScope.APPS_LIST)

        val stored = facetRepository.getById(facet.id)!!
        assertEquals(listOf("com.example.f"), packages(PlacementList.FAVORITES, facet.id))
        assertEquals(emptyList<String>(), packages(PlacementList.DOCK, facet.id))
        assertEquals(ListContentMode.RECENTS, stored.listContentMode)
        assertEquals(9, stored.appsToShowCount)
    }

    @Test
    fun `look scope copies the dock mode, list look and landscape twins but leaves the clock twins`() = runTest {
        // Given a facet whose clock has its own landscape alignment
        val facet = facetRepository.addFacet()
        facetRepository.updateLandscape(facet.id) { it.copy(clockAlignment = ClockAlignment.RIGHT, appRowPosition = AppRowPosition.LEFT) }

        // When the look scope is applied
        useCase()(facet.id, FacetDefaultsScope.LOOK)

        // Then the look matches the launcher's, a null launcher twin stays null, and the clock twin is kept
        val stored = facetRepository.getById(facet.id)!!
        assertEquals(DockDisplayMode.TEXT, stored.dockDisplayMode)
        assertEquals(AppRowPosition.RIGHT, stored.appRowPosition)
        assertEquals(AppRowPresentation.TEXT_ONLY, stored.appRowPresentation)
        assertEquals(AppListVerticalAlignment.TOP, stored.appListVerticalAlignment)
        assertEquals(AppListLayout.TWO_COLUMN, stored.appListLayout)
        assertEquals(AppListColumnAlignment.MIRRORED, stored.appListColumnAlignment)
        assertEquals(AppRowPosition.CENTER, stored.landscape.appRowPosition)
        assertNull(stored.landscape.appListVerticalAlignment)
        assertEquals(ClockAlignment.RIGHT, stored.landscape.clockAlignment)
    }

    @Test
    fun `applyAll seeds every scope`() = runTest {
        val facet = facetRepository.addFacet()
        placementRepository.addApp(PlacementTarget.GlobalDock, app('d'), 0)
        placementRepository.addApp(PlacementTarget.GlobalFavorites, app('f'), 0)

        useCase().applyAll(facet.id)

        val stored = facetRepository.getById(facet.id)!!
        assertEquals(listOf("com.example.d"), packages(PlacementList.DOCK, facet.id))
        assertEquals(listOf("com.example.f"), packages(PlacementList.FAVORITES, facet.id))
        assertEquals(9, stored.appsToShowCount)
        assertEquals(DockDisplayMode.TEXT, stored.dockDisplayMode)
    }
}
