package com.facetlauncher.app.data

import com.facetlauncher.app.data.model.DEFAULT_CLOCK_SCALE
import com.facetlauncher.app.data.model.LandscapeOverrides
import com.facetlauncher.app.data.model.landscape
import com.facetlauncher.app.data.local.FacetDao
import com.facetlauncher.app.data.local.FacetEntity
import com.facetlauncher.app.data.model.Facet
import com.facetlauncher.app.data.model.AppListColumnAlignment
import com.facetlauncher.app.data.model.AppListGridColumns
import com.facetlauncher.app.data.model.AppListGridDisplayMode
import com.facetlauncher.app.data.model.AppListLayout
import com.facetlauncher.app.data.model.AppListVerticalAlignment
import com.facetlauncher.app.data.model.AppRowPosition
import com.facetlauncher.app.data.model.AppRowPresentation
import com.facetlauncher.app.data.local.ClockStackItemEntity
import com.facetlauncher.app.data.model.ClockAlignment
import com.facetlauncher.app.data.model.ClockStackItemKind
import com.facetlauncher.app.data.model.ListContentMode
import com.facetlauncher.app.data.model.ClockTemplateId
import com.facetlauncher.app.data.model.ClockFontOption
import com.facetlauncher.app.data.model.ClockColorOption
import com.facetlauncher.app.data.model.ClockDateStyle
import com.facetlauncher.app.data.model.FontWeightOption
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

/** In-memory fake — simpler than mocking every [FacetDao] method for this repository's needs. */
private class FakeFacetDao : FacetDao {
    private var nextId = 1L
    private val state = MutableStateFlow<List<FacetEntity>>(emptyList())

    override fun observeAll(): Flow<List<FacetEntity>> = state

    override suspend fun insert(facet: FacetEntity): Long {
        val id = if (facet.id != 0L) facet.id else nextId++
        val stored = facet.copy(id = id)
        state.value = state.value.filterNot { it.id == id } + stored
        return id
    }

    override suspend fun update(facet: FacetEntity) {
        state.value = state.value.map { if (it.id == facet.id) facet else it }
    }

    override suspend fun delete(facet: FacetEntity) {
        state.value = state.value.filterNot { it.id == facet.id }
    }

    override suspend fun getById(id: Long): FacetEntity? = state.value.find { it.id == id }

    override suspend fun deleteAll() {
        state.value = emptyList()
    }
}

class FacetRepositoryTest {

    @Test
    fun `addFacet names it Facet N and appends after the last position`() = runTest {
        val dao = FakeFacetDao()
        val repository = FacetRepository(dao, FakeClockStackItemDao())
        repository.addFacet()
        val second = repository.addFacet()
        assertEquals("Facet 2", second.name)
        assertEquals(1, second.position)
        assertEquals(2, repository.observeFacets().first().size)
    }

    @Test
    fun `renameFacet updates only the name`() = runTest {
        val dao = FakeFacetDao()
        val repository = FacetRepository(dao, FakeClockStackItemDao())
        val facet = repository.addFacet()
        repository.renameFacet(facet.id, "Work")
        val stored = repository.observeFacets().first().single()
        assertEquals("Work", stored.name)
        assertEquals(facet.position, stored.position)
    }

    @Test
    fun `reorderFacets re-assigns position by list order`() = runTest {
        val dao = FakeFacetDao()
        val repository = FacetRepository(dao, FakeClockStackItemDao())
        val first = repository.addFacet()
        val second = repository.addFacet()
        repository.reorderFacets(listOf(second.id, first.id))
        val stored = repository.observeFacets().first().associateBy { it.id }
        assertEquals(0, stored[second.id]?.position)
        assertEquals(1, stored[first.id]?.position)
    }

    @Test
    fun `deleteFacet removes it`() = runTest {
        val dao = FakeFacetDao()
        val repository = FacetRepository(dao, FakeClockStackItemDao())
        val facet = repository.addFacet()
        repository.deleteFacet(facet.id)
        assertTrue(repository.observeFacets().first().isEmpty())
    }

    @Test
    fun `setUse24HourTime round-trips true and false`() = runTest {
        val dao = FakeFacetDao()
        val repository = FacetRepository(dao, FakeClockStackItemDao())
        val facet = repository.addFacet()
        repository.setUse24HourTime(facet.id, true)
        assertEquals(true, repository.observeFacets().first().single().use24HourTime)
        repository.setUse24HourTime(facet.id, false)
        assertEquals(false, repository.observeFacets().first().single().use24HourTime)
    }

    @Test
    fun `updateOverridingClock sets all fields and flag together`() = runTest {
        val dao = FakeFacetDao()
        val repository = FacetRepository(dao, FakeClockStackItemDao())
        val facet = repository.addFacet()
        repository.updateOverridingClock(
            facetId = facet.id,
            overriding = true,
            templateId = ClockTemplateId.VERTICAL_STACK_BOLD_HOUR,
            fontOption = ClockFontOption.MANROPE,
            colorOption = ClockColorOption.THEME,
            use24HourTime = true,
            showMeridiem = true,
            accentColorOption = ClockColorOption.ACCENT_PRIMARY,
            dateStyle = ClockDateStyle.FULL,
        )
        val stored = repository.observeFacets().first().single()
        assertEquals(true, stored.overrideClock)
        assertEquals(ClockTemplateId.VERTICAL_STACK_BOLD_HOUR, stored.clockTemplateId)
        assertEquals(ClockFontOption.MANROPE, stored.clockFontOption)
        assertEquals(ClockColorOption.THEME, stored.clockColorOption)
        assertEquals(true, stored.use24HourTime)
        assertEquals(true, stored.clockShowMeridiem)
        // Placement is not part of the override: it stays as it was
        assertEquals(false, stored.clockPlacementOwn)
        assertEquals(ClockAlignment.LEFT, stored.clockAlignment)
    }

    @Test
    fun `setClockAccentColorOption and setClockDateStyle round-trip independently`() = runTest {
        val dao = FakeFacetDao()
        val repository = FacetRepository(dao, FakeClockStackItemDao())
        val facet = repository.addFacet()

        repository.setClockAccentColorOption(facet.id, ClockColorOption.ACCENT_SECONDARY)

        val stored = repository.observeFacets().first().single()
        assertEquals(ClockColorOption.ACCENT_SECONDARY, stored.clockAccentColorOption)
        assertEquals(ClockDateStyle.FULL, stored.clockDateStyle)

        repository.setClockDateStyle(stored.id, ClockDateStyle.CONDENSED)

        val restored = repository.observeFacets().first().single()
        assertEquals(ClockColorOption.ACCENT_SECONDARY, restored.clockAccentColorOption)
        assertEquals(ClockDateStyle.CONDENSED, restored.clockDateStyle)
    }

    @Test
    fun `setClockPlacement makes the placement the facet's own, with landscape twins kept apart`() = runTest {
        // Given a facet showing the launcher's fallback placement
        val repository = FacetRepository(FakeFacetDao(), FakeClockStackItemDao())
        val facet = repository.addFacet()
        assertEquals(false, repository.getById(facet.id)!!.clockPlacementOwn)

        // When a placement is saved with its own landscape alignment
        repository.setClockPlacement(
            facetId = facet.id, alignment = ClockAlignment.CENTER, zoneHeightDp = 220f, scale = 1.5f,
            landscape = LandscapeOverrides(clockAlignment = ClockAlignment.RIGHT, clockZoneHeightDp = 310f, clockScale = null),
        )

        // Then the facet owns it, portrait and landscape separately
        val stored = repository.getById(facet.id)!!
        assertEquals(true, stored.clockPlacementOwn)
        assertEquals(ClockAlignment.CENTER, stored.clockAlignment)
        assertEquals(220f, stored.clockZoneHeightDp)
        assertEquals(1.5f, stored.clockScale, 0.001f)
        assertEquals(ClockAlignment.RIGHT, stored.landscape.clockAlignment)
        assertEquals(310f, stored.landscape.clockZoneHeightDp)
        assertNull(stored.landscape.clockScale)
    }

    @Test
    fun `setClockPlacement leaves the other landscape fields alone`() = runTest {
        // Given a facet with a landscape app-row position
        val repository = FacetRepository(FakeFacetDao(), FakeClockStackItemDao())
        val facet = repository.addFacet()
        repository.updateLandscape(facet.id) { it.copy(appRowPosition = AppRowPosition.CENTER) }

        // When the clock placement is saved
        repository.setClockPlacement(facet.id, ClockAlignment.RIGHT, null, 1f, LandscapeOverrides(clockAlignment = ClockAlignment.CENTER))

        // Then the app row position is untouched
        assertEquals(AppRowPosition.CENTER, repository.getById(facet.id)!!.landscape.appRowPosition)
    }

    @Test
    fun `resetClockPosition gives the placement back to the launcher fallback`() = runTest {
        // Given a facet that owns a dragged placement in both orientations
        val repository = FacetRepository(FakeFacetDao(), FakeClockStackItemDao())
        val facet = repository.addFacet()
        repository.setClockPlacement(facet.id, ClockAlignment.CENTER, 220f, 1.5f, LandscapeOverrides(clockAlignment = ClockAlignment.RIGHT, clockZoneHeightDp = 310f, clockScale = 1.1f))

        // When it is reset
        repository.resetClockPosition(facet.id)

        // Then it stops owning placement and every value is cleared
        val stored = repository.getById(facet.id)!!
        assertEquals(false, stored.clockPlacementOwn)
        assertNull(stored.clockZoneHeightDp)
        assertEquals(ClockAlignment.LEFT, stored.clockAlignment)
        assertEquals(DEFAULT_CLOCK_SCALE, stored.clockScale, 0.001f)
        assertEquals(LandscapeOverrides(), stored.landscape)
    }

    @Test
    fun `updateOverridingApps sets content fields and flag together`() = runTest {
        // position/presentation are no longer part of this bundle — they're look fields, edited
        // from Settings -> Appearance independently now (see chat history)
        val dao = FakeFacetDao()
        val repository = FacetRepository(dao, FakeClockStackItemDao())
        val facet = repository.addFacet()
        repository.updateOverridingApps(
            facetId = facet.id,
            overriding = true,
            mode = ListContentMode.MOST_USED,
            count = 6,
            overridingFavorites = true
        )
        val stored = repository.observeFacets().first().single()
        assertEquals(true, stored.overrideApps)
        assertEquals(ListContentMode.MOST_USED, stored.listContentMode)
        assertEquals(6, stored.appsToShowCount)
        assertEquals(true, stored.overridingFavorites)
    }

    @Test
    fun `setAppRowPosition and setAppRowPresentation round-trip independently`() = runTest {
        val dao = FakeFacetDao()
        val repository = FacetRepository(dao, FakeClockStackItemDao())
        val facet = repository.addFacet()

        repository.setAppRowPosition(facet.id, AppRowPosition.RIGHT)
        val afterPosition = repository.observeFacets().first().single()
        assertEquals(AppRowPosition.RIGHT, afterPosition.appRowPosition)

        // Re-fetch before the second setter — each setter copies from the entity it's given and
        // fully replaces the stored row, so acting on the original (now-stale) `facet` here
        // would silently discard the position update just made above.
        repository.setAppRowPresentation(afterPosition.id, AppRowPresentation.ICON_ONLY)
        val stored = repository.observeFacets().first().single()
        assertEquals(AppRowPosition.RIGHT, stored.appRowPosition)
        assertEquals(AppRowPresentation.ICON_ONLY, stored.appRowPresentation)
    }

    @Test
    fun `setAppListVerticalAlignment round-trips`() = runTest {
        val dao = FakeFacetDao()
        val repository = FacetRepository(dao, FakeClockStackItemDao())
        val facet = repository.addFacet()

        repository.setAppListVerticalAlignment(facet.id, AppListVerticalAlignment.TOP)

        assertEquals(AppListVerticalAlignment.TOP, repository.observeFacets().first().single().appListVerticalAlignment)
    }

    @Test
    fun `a facet stores three grid columns and keeps its other grid settings`() = runTest {
        // Given a facet already on a grid with text display
        val repository = FacetRepository(FakeFacetDao(), FakeClockStackItemDao())
        val facet = repository.addFacet()
        repository.setAppListLayout(facet.id, AppListLayout.GRID)
        repository.setAppListGridDisplayMode(repository.observeFacets().first().single().id, AppListGridDisplayMode.TEXT)

        // When it is switched to three columns
        repository.setAppListGridColumns(repository.observeFacets().first().single().id, AppListGridColumns.THREE)

        // Then three columns persists with a column count of 3, and the rest is untouched
        val stored = repository.observeFacets().first().single()
        assertEquals(AppListGridColumns.THREE, stored.appListGridColumns)
        assertEquals(3, stored.appListGridColumns.columns)
        assertEquals(AppListLayout.GRID, stored.appListLayout)
        assertEquals(AppListGridDisplayMode.TEXT, stored.appListGridDisplayMode)
    }

    @Test
    fun `setAppListLayout, setAppListColumnAlignment, setAppListGridColumns and setAppListGridDisplayMode round-trip independently`() = runTest {
        val dao = FakeFacetDao()
        val repository = FacetRepository(dao, FakeClockStackItemDao())
        val facet = repository.addFacet()

        repository.setAppListLayout(facet.id, AppListLayout.GRID)
        val afterLayout = repository.observeFacets().first().single()
        assertEquals(AppListLayout.GRID, afterLayout.appListLayout)

        // Re-fetch before each next setter — each setter copies from the entity it's given and
        // fully replaces the stored row, so acting on a stale `facet` would discard prior updates.
        repository.setAppListColumnAlignment(afterLayout.id, AppListColumnAlignment.MIRRORED)
        val afterColumnAlignment = repository.observeFacets().first().single()
        assertEquals(AppListColumnAlignment.MIRRORED, afterColumnAlignment.appListColumnAlignment)

        repository.setAppListGridColumns(afterColumnAlignment.id, AppListGridColumns.SIX)
        val afterGridColumns = repository.observeFacets().first().single()
        assertEquals(AppListGridColumns.SIX, afterGridColumns.appListGridColumns)

        repository.setAppListGridDisplayMode(afterGridColumns.id, AppListGridDisplayMode.TEXT)
        val stored = repository.observeFacets().first().single()
        assertEquals(AppListLayout.GRID, stored.appListLayout)
        assertEquals(AppListColumnAlignment.MIRRORED, stored.appListColumnAlignment)
        assertEquals(AppListGridColumns.SIX, stored.appListGridColumns)
        assertEquals(AppListGridDisplayMode.TEXT, stored.appListGridDisplayMode)
    }

    @Test
    fun `setAppsToShowCount coerces into the 3 to 12 range`() = runTest {
        val dao = FakeFacetDao()
        val repository = FacetRepository(dao, FakeClockStackItemDao())
        val facet = repository.addFacet()
        repository.setAppsToShowCount(facet.id, 5)
        assertEquals(5, repository.observeFacets().first().single().appsToShowCount)
        repository.setAppsToShowCount(facet.id, 1)
        assertEquals(3, repository.observeFacets().first().single().appsToShowCount)
        repository.setAppsToShowCount(facet.id, 99)
        assertEquals(12, repository.observeFacets().first().single().appsToShowCount)
    }

    @Test
    fun `calendar display setters write the facet and clamp the item count`() = runTest {
        val repository = FacetRepository(FakeFacetDao(), FakeClockStackItemDao())
        val facet = repository.addFacet()

        repository.setShowCalendarIndicator(facet.id, false)
        repository.setCalendarItemsToShow(repository.observeFacets().first().single().id, 2)
        val stored = repository.observeFacets().first().single()
        assertEquals(false, stored.showCalendarIndicator)
        assertEquals(2, stored.calendarItemsToShow)

        repository.setCalendarItemsToShow(stored.id, 0)
        assertEquals(1, repository.observeFacets().first().single().calendarItemsToShow)
        repository.setCalendarItemsToShow(stored.id, 9)
        assertEquals(3, repository.observeFacets().first().single().calendarItemsToShow)
    }

    @Test
    fun `updateOverridingCalendar sets selection fields and flag together, design fields untouched`() = runTest {
        val dao = FakeFacetDao()
        val repository = FacetRepository(dao, FakeClockStackItemDao())
        val facet = repository.addFacet()
        repository.updateOverridingCalendar(
            facet.id,
            overriding = true,
            showAllDayEvents = false,
            selectedCalendarIds = setOf("1", "2"),
            showCalendarIndicator = true,
            calendarItemsToShow = 3,
        )
        val stored = repository.observeFacets().first().single()
        assertEquals(true, stored.overrideCalendar)
        assertEquals(false, stored.showAllDayEvents)
        assertEquals(setOf("1", "2"), stored.selectedCalendarIds)
    }

    @Test
    fun `setSelectedCalendarIds round-trips, null means every calendar selected`() = runTest {
        val dao = FakeFacetDao()
        val repository = FacetRepository(dao, FakeClockStackItemDao())
        val facet = repository.addFacet()

        repository.setSelectedCalendarIds(facet.id, setOf("10", "20", "30"))
        assertEquals(setOf("10", "20", "30"), repository.observeFacets().first().single().selectedCalendarIds)

        repository.setSelectedCalendarIds(repository.observeFacets().first().single().id, null)
        assertEquals(null, repository.observeFacets().first().single().selectedCalendarIds)
    }

    @Test
    fun `each facet comes with its own clock stack in swipe order`() = runTest {
        // Given two facets and a stack for each, stored out of order
        val dao = FakeFacetDao()
        val stackDao = FakeClockStackItemDao()
        val repository = FacetRepository(dao, stackDao)
        val first = dao.insert(FacetEntity(name = "A", position = 0))
        val second = dao.insert(FacetEntity(name = "B", position = 1))
        stackDao.insert(ClockStackItemEntity(facetId = first, position = 1, kind = ClockStackItemKind.WIDGET, appWidgetId = 11))
        stackDao.insert(ClockStackItemEntity(facetId = first, position = 0, kind = ClockStackItemKind.CLOCK))
        stackDao.insert(ClockStackItemEntity(facetId = second, position = 0, kind = ClockStackItemKind.WIDGET, appWidgetId = 22))

        // When the facets are observed
        val facets = repository.observeFacets().first()

        // Then each carries only its own items, in position order, and getById matches
        assertEquals(listOf(null, 11), facets.first { it.id == first }.clockStack.map { it.appWidgetId })
        assertEquals(listOf(22), facets.first { it.id == second }.clockStack.map { it.appWidgetId })
        assertEquals(listOf(null, 11), repository.getById(first)!!.clockStack.map { it.appWidgetId })
    }

    @Test
    fun `a facet with no stack rows has an empty clock stack`() = runTest {
        // Given a facet row with no stack rows (an empty stack draws nothing)
        val dao = FakeFacetDao()
        val repository = FacetRepository(dao, FakeClockStackItemDao())
        dao.insert(FacetEntity(name = "A", position = 0))

        // Then its clock stack is empty, not a default clock
        assertEquals(emptyList<Any>(), repository.observeFacets().first().single().clockStack)
    }

    @Test
    fun `a new facet starts with the native clock as its one page`() = runTest {
        // Given a repository
        val repository = FacetRepository(FakeFacetDao(), FakeClockStackItemDao())

        // When a facet is added
        val added = repository.addFacet()

        // Then its stack is the native clock, both on the returned facet and as stored
        assertEquals(listOf(ClockStackItemKind.CLOCK), added.clockStack.map { it.kind })
        assertEquals(listOf(ClockStackItemKind.CLOCK), repository.getById(added.id)!!.clockStack.map { it.kind })
        assertEquals(added.id, repository.getById(added.id)!!.clockStack.single().facetId)
    }

    @Test
    fun `a restored facet starts with the native clock too`() = runTest {
        // Given a repository
        val repository = FacetRepository(FakeFacetDao(), FakeClockStackItemDao())

        // When a facet is restored from a backup (which carries no stack)
        val id = repository.restoreFacet(Facet(name = "Restored", position = 0))

        // Then it has the native clock as its one page
        assertEquals(listOf(ClockStackItemKind.CLOCK), repository.getById(id)!!.clockStack.map { it.kind })
    }

    @Test
    fun `landscape values are saved apart from the portrait ones`() = runTest {
        val repository = FacetRepository(FakeFacetDao(), FakeClockStackItemDao())
        val facet = repository.addFacet()
        repository.setClockPlacement(facet.id, ClockAlignment.RIGHT, null, 0.8f, LandscapeOverrides())
        repository.setAppRowPosition(repository.observeFacets().first().single().id, AppRowPosition.RIGHT)

        // When landscape values are written
        val written = LandscapeOverrides(
            clockAlignment = ClockAlignment.CENTER,
            clockZoneHeightDp = 310f,
            clockScale = 1.1f,
            appRowPosition = AppRowPosition.CENTER,
            appListVerticalAlignment = AppListVerticalAlignment.TOP,
            appListColumnAlignment = AppListColumnAlignment.MIRRORED,
        )
        repository.updateLandscape(repository.observeFacets().first().single().id) { written }

        // Then both sets are kept
        val stored = repository.observeFacets().first().single()
        assertEquals(written, stored.landscape)
        assertEquals(ClockAlignment.RIGHT, stored.clockAlignment)
        assertEquals(AppRowPosition.RIGHT, stored.appRowPosition)
    }

    @Test
    fun `updateLandscape changes only the fields it names and a null clears one`() = runTest {
        val repository = FacetRepository(FakeFacetDao(), FakeClockStackItemDao())
        val facet = repository.addFacet()
        repository.updateLandscape(facet.id) { it.copy(clockScale = 1.1f, appRowPosition = AppRowPosition.CENTER) }

        repository.updateLandscape(repository.observeFacets().first().single().id) { it.copy(appRowPosition = null) }

        val landscape = repository.observeFacets().first().single().landscape
        assertEquals(1.1f, landscape.clockScale!!, 0.001f)
        assertNull(landscape.appRowPosition)
    }

    @Test
    fun `resetClockPosition clears the landscape clock props too`() = runTest {
        val repository = FacetRepository(FakeFacetDao(), FakeClockStackItemDao())
        val facet = repository.addFacet()
        repository.updateLandscape(facet.id) { it.copy(clockAlignment = ClockAlignment.CENTER, clockZoneHeightDp = 310f, clockScale = 1.1f, appRowPosition = AppRowPosition.CENTER) }

        repository.resetClockPosition(repository.observeFacets().first().single().id)

        val landscape = repository.observeFacets().first().single().landscape
        assertNull(landscape.clockAlignment)
        assertNull(landscape.clockZoneHeightDp)
        assertNull(landscape.clockScale)
        assertEquals(AppRowPosition.CENTER, landscape.appRowPosition)
    }
}
