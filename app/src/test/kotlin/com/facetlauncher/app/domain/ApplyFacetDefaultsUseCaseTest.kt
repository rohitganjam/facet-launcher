package com.facetlauncher.app.domain

import com.facetlauncher.app.data.FacetRepository
import com.facetlauncher.app.data.FakeClockStackItemDao
import com.facetlauncher.app.data.SettingsRepository
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
import com.facetlauncher.app.data.model.landscape
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify
import org.mockito.Mockito.verifyNoInteractions
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
    private val seedFacetDefaults = mock(SeedFacetDefaultsUseCase::class.java)

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
        return ApplyFacetDefaultsUseCase(facetRepository, seedFacetDefaults, settingsRepository)
    }

    @Test
    fun `dock scope reseeds the facet dock from the device defaults and copies the display style`() = runTest {
        // Given a facet
        val facet = facetRepository.addFacet()

        // When the dock scope is applied
        useCase()(facet.id, FacetDefaultsScope.DOCK)

        // Then the dock is seeded afresh, and the dock display style is the launcher's
        verify(seedFacetDefaults).invoke(facet.id, true)
        assertEquals(DockDisplayMode.TEXT, facetRepository.getById(facet.id)!!.dockDisplayMode)
    }

    @Test
    fun `apps list scope copies content mode and count and seeds nothing`() = runTest {
        val facet = facetRepository.addFacet()

        useCase()(facet.id, FacetDefaultsScope.APPS_LIST)

        val stored = facetRepository.getById(facet.id)!!
        assertEquals(ListContentMode.RECENTS, stored.listContentMode)
        assertEquals(9, stored.appsToShowCount)
        verifyNoInteractions(seedFacetDefaults)
    }

    @Test
    fun `look scope copies the list look and landscape twins but leaves the clock twins`() = runTest {
        // Given a facet whose clock has its own landscape alignment
        val facet = facetRepository.addFacet()
        facetRepository.updateLandscape(facet.id) { it.copy(clockAlignment = ClockAlignment.RIGHT, appRowPosition = AppRowPosition.LEFT) }

        // When the look scope is applied
        useCase()(facet.id, FacetDefaultsScope.LOOK)

        // Then the look matches the launcher's, a null launcher twin stays null, and the clock twin is kept
        val stored = facetRepository.getById(facet.id)!!
        assertEquals(DockDisplayMode.ICONS, stored.dockDisplayMode)
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
    fun `applyAll seeds the dock and applies every setting`() = runTest {
        val facet = facetRepository.addFacet()

        useCase().applyAll(facet.id)

        val stored = facetRepository.getById(facet.id)!!
        verify(seedFacetDefaults).invoke(facet.id, true)
        assertEquals(9, stored.appsToShowCount)
        assertEquals(DockDisplayMode.TEXT, stored.dockDisplayMode)
    }
}
