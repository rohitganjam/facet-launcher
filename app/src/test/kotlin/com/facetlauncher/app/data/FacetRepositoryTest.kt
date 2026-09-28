package com.facetlauncher.app.data

import com.facetlauncher.app.data.local.FacetDao
import com.facetlauncher.app.data.local.FacetEntity
import com.facetlauncher.app.data.model.AppListColumnAlignment
import com.facetlauncher.app.data.model.AppListGridColumns
import com.facetlauncher.app.data.model.AppListGridDisplayMode
import com.facetlauncher.app.data.model.AppListLayout
import com.facetlauncher.app.data.model.AppListVerticalAlignment
import com.facetlauncher.app.data.model.AppRowPosition
import com.facetlauncher.app.data.model.AppRowPresentation
import com.facetlauncher.app.data.model.ClockAlignment
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
        val repository = FacetRepository(dao)
        repository.addFacet()
        val second = repository.addFacet()
        assertEquals("Facet 2", second.name)
        assertEquals(1, second.position)
        assertEquals(2, repository.observeFacets().first().size)
    }

    @Test
    fun `renameFacet updates only the name`() = runTest {
        val dao = FakeFacetDao()
        val repository = FacetRepository(dao)
        val facet = repository.addFacet()
        repository.renameFacet(facet, "Work")
        val stored = repository.observeFacets().first().single()
        assertEquals("Work", stored.name)
        assertEquals(facet.position, stored.position)
    }

    @Test
    fun `reorderFacets re-assigns position by list order`() = runTest {
        val dao = FakeFacetDao()
        val repository = FacetRepository(dao)
        val first = repository.addFacet()
        val second = repository.addFacet()
        repository.reorderFacets(listOf(second, first))
        val stored = repository.observeFacets().first().associateBy { it.id }
        assertEquals(0, stored[second.id]?.position)
        assertEquals(1, stored[first.id]?.position)
    }

    @Test
    fun `deleteFacet removes it`() = runTest {
        val dao = FakeFacetDao()
        val repository = FacetRepository(dao)
        val facet = repository.addFacet()
        repository.deleteFacet(facet)
        assertTrue(repository.observeFacets().first().isEmpty())
    }

    @Test
    fun `setUse24HourTime round-trips true and false`() = runTest {
        val dao = FakeFacetDao()
        val repository = FacetRepository(dao)
        val facet = repository.addFacet()
        repository.setUse24HourTime(facet, true)
        assertEquals(true, repository.observeFacets().first().single().use24HourTime)
        repository.setUse24HourTime(facet, false)
        assertEquals(false, repository.observeFacets().first().single().use24HourTime)
    }

    @Test
    fun `updateOverridingClock sets all fields and flag together`() = runTest {
        val dao = FakeFacetDao()
        val repository = FacetRepository(dao)
        val facet = repository.addFacet()
        repository.updateOverridingClock(
            facet = facet,
            overriding = true,
            templateId = ClockTemplateId.VERTICAL_STACK_BOLD_HOUR,
            fontOption = ClockFontOption.MANROPE,
            colorOption = ClockColorOption.THEME,
            use24HourTime = true,
            showMeridiem = true,
            clockAlignment = ClockAlignment.CENTER,
            clockZoneHeightDp = 180f,
            clockScale = 1.2f,
        )
        val stored = repository.observeFacets().first().single()
        assertEquals(true, stored.overrideClock)
        assertEquals(ClockTemplateId.VERTICAL_STACK_BOLD_HOUR, stored.clockTemplateId)
        assertEquals(ClockFontOption.MANROPE, stored.clockFontOption)
        assertEquals(ClockColorOption.THEME, stored.clockColorOption)
        assertEquals(true, stored.use24HourTime)
        assertEquals(true, stored.clockShowMeridiem)
        assertEquals(ClockAlignment.CENTER, stored.clockAlignment)
        assertEquals(180f, stored.clockZoneHeightDp)
        assertEquals(1.2f, stored.clockScale, 0.001f)
    }

    @Test
    fun `setClockAccentColorOption and setClockDateStyle round-trip independently`() = runTest {
        val dao = FakeFacetDao()
        val repository = FacetRepository(dao)
        val facet = repository.addFacet()

        repository.setClockAccentColorOption(facet, ClockColorOption.ACCENT_SECONDARY)

        val stored = repository.observeFacets().first().single()
        assertEquals(ClockColorOption.ACCENT_SECONDARY, stored.clockAccentColorOption)
        assertEquals(ClockDateStyle.FULL, stored.clockDateStyle)

        repository.setClockDateStyle(stored, ClockDateStyle.CONDENSED)

        val restored = repository.observeFacets().first().single()
        assertEquals(ClockColorOption.ACCENT_SECONDARY, restored.clockAccentColorOption)
        assertEquals(ClockDateStyle.CONDENSED, restored.clockDateStyle)
    }

    @Test
    fun `setClockAlignment persists and round-trips`() = runTest {
        val dao = FakeFacetDao()
        val repository = FacetRepository(dao)
        val facet = repository.addFacet()

        repository.setClockAlignment(facet, ClockAlignment.CENTER)

        val stored = repository.observeFacets().first().single()
        assertEquals(ClockAlignment.CENTER, stored.clockAlignment)
    }

    @Test
    fun `setClockZoneHeight and resetClockZoneHeight round-trip`() = runTest {
        val dao = FakeFacetDao()
        val repository = FacetRepository(dao)
        val facet = repository.addFacet()

        repository.setClockZoneHeight(facet, 220f)
        assertEquals(220f, repository.observeFacets().first().single().clockZoneHeightDp)

        repository.resetClockZoneHeight(repository.observeFacets().first().single())
        assertNull(repository.observeFacets().first().single().clockZoneHeightDp)
    }

    @Test
    fun `setClockScale and resetClockPosition round-trip`() = runTest {
        val dao = FakeFacetDao()
        val repository = FacetRepository(dao)
        val facet = repository.addFacet()

        repository.setClockScale(facet, 1.5f)
        assertEquals(1.5f, repository.observeFacets().first().single().clockScale, 0.001f)

        repository.resetClockPosition(repository.observeFacets().first().single())
        assertEquals(0.8f, repository.observeFacets().first().single().clockScale, 0.001f)
    }

    @Test
    fun `resetClockPosition clears the zone height, alignment, and scale together`() = runTest {
        val dao = FakeFacetDao()
        val repository = FacetRepository(dao)
        val facet = repository.addFacet()
        repository.setClockZoneHeight(facet, 220f)
        repository.setClockAlignment(facet, ClockAlignment.CENTER)
        repository.setClockScale(facet, 1.5f)
        val dragged = repository.observeFacets().first().single()

        repository.resetClockPosition(dragged)

        val stored = repository.observeFacets().first().single()
        assertNull(stored.clockZoneHeightDp)
        assertEquals(ClockAlignment.LEFT, stored.clockAlignment)
        assertEquals(0.8f, stored.clockScale, 0.001f)
    }

    @Test
    fun `updateOverridingApps sets content fields and flag together`() = runTest {
        // position/presentation are no longer part of this bundle — they're look fields, edited
        // from Settings -> Appearance independently now (see chat history)
        val dao = FakeFacetDao()
        val repository = FacetRepository(dao)
        val facet = repository.addFacet()
        repository.updateOverridingApps(
            facet = facet,
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
        val repository = FacetRepository(dao)
        val facet = repository.addFacet()

        repository.setAppRowPosition(facet, AppRowPosition.RIGHT)
        val afterPosition = repository.observeFacets().first().single()
        assertEquals(AppRowPosition.RIGHT, afterPosition.appRowPosition)

        // Re-fetch before the second setter — each setter copies from the entity it's given and
        // fully replaces the stored row, so acting on the original (now-stale) `facet` here
        // would silently discard the position update just made above.
        repository.setAppRowPresentation(afterPosition, AppRowPresentation.ICON_ONLY)
        val stored = repository.observeFacets().first().single()
        assertEquals(AppRowPosition.RIGHT, stored.appRowPosition)
        assertEquals(AppRowPresentation.ICON_ONLY, stored.appRowPresentation)
    }

    @Test
    fun `setAppListVerticalAlignment round-trips`() = runTest {
        val dao = FakeFacetDao()
        val repository = FacetRepository(dao)
        val facet = repository.addFacet()

        repository.setAppListVerticalAlignment(facet, AppListVerticalAlignment.TOP)

        assertEquals(AppListVerticalAlignment.TOP, repository.observeFacets().first().single().appListVerticalAlignment)
    }

    @Test
    fun `setAppListLayout, setAppListColumnAlignment, setAppListGridColumns and setAppListGridDisplayMode round-trip independently`() = runTest {
        val dao = FakeFacetDao()
        val repository = FacetRepository(dao)
        val facet = repository.addFacet()

        repository.setAppListLayout(facet, AppListLayout.GRID)
        val afterLayout = repository.observeFacets().first().single()
        assertEquals(AppListLayout.GRID, afterLayout.appListLayout)

        // Re-fetch before each next setter — each setter copies from the entity it's given and
        // fully replaces the stored row, so acting on a stale `facet` would discard prior updates.
        repository.setAppListColumnAlignment(afterLayout, AppListColumnAlignment.MIRRORED)
        val afterColumnAlignment = repository.observeFacets().first().single()
        assertEquals(AppListColumnAlignment.MIRRORED, afterColumnAlignment.appListColumnAlignment)

        repository.setAppListGridColumns(afterColumnAlignment, AppListGridColumns.SIX)
        val afterGridColumns = repository.observeFacets().first().single()
        assertEquals(AppListGridColumns.SIX, afterGridColumns.appListGridColumns)

        repository.setAppListGridDisplayMode(afterGridColumns, AppListGridDisplayMode.TEXT)
        val stored = repository.observeFacets().first().single()
        assertEquals(AppListLayout.GRID, stored.appListLayout)
        assertEquals(AppListColumnAlignment.MIRRORED, stored.appListColumnAlignment)
        assertEquals(AppListGridColumns.SIX, stored.appListGridColumns)
        assertEquals(AppListGridDisplayMode.TEXT, stored.appListGridDisplayMode)
    }

    @Test
    fun `setAppsToShowCount coerces into the 3 to 12 range`() = runTest {
        val dao = FakeFacetDao()
        val repository = FacetRepository(dao)
        val facet = repository.addFacet()
        repository.setAppsToShowCount(facet, 5)
        assertEquals(5, repository.observeFacets().first().single().appsToShowCount)
        repository.setAppsToShowCount(facet, 1)
        assertEquals(3, repository.observeFacets().first().single().appsToShowCount)
        repository.setAppsToShowCount(facet, 99)
        assertEquals(12, repository.observeFacets().first().single().appsToShowCount)
    }

    @Test
    fun `updateOverridingCalendar sets selection fields and flag together, design fields untouched`() = runTest {
        val dao = FakeFacetDao()
        val repository = FacetRepository(dao)
        val facet = repository.addFacet()
        repository.updateOverridingCalendar(
            facet,
            overriding = true,
            showAllDayEvents = false,
            selectedCalendarIds = setOf("1", "2"),
        )
        val stored = repository.observeFacets().first().single()
        assertEquals(true, stored.overrideCalendar)
        assertEquals(false, stored.showAllDayEvents)
        assertEquals(setOf("1", "2"), stored.selectedCalendarIds)
    }

    @Test
    fun `setSelectedCalendarIds round-trips, null means every calendar selected`() = runTest {
        val dao = FakeFacetDao()
        val repository = FacetRepository(dao)
        val facet = repository.addFacet()

        repository.setSelectedCalendarIds(facet, setOf("10", "20", "30"))
        assertEquals(setOf("10", "20", "30"), repository.observeFacets().first().single().selectedCalendarIds)

        repository.setSelectedCalendarIds(repository.observeFacets().first().single(), null)
        assertEquals(null, repository.observeFacets().first().single().selectedCalendarIds)
    }

    @Test
    fun `setClockWidgetAppWidgetId round-trips, null reverts to the native clock`() = runTest {
        val dao = FakeFacetDao()
        val repository = FacetRepository(dao)
        val facet = repository.addFacet()
        assertNull(facet.clockWidgetAppWidgetId)

        repository.setClockWidgetAppWidgetId(facet, 42)
        assertEquals(42, repository.observeFacets().first().single().clockWidgetAppWidgetId)

        repository.setClockWidgetAppWidgetId(repository.observeFacets().first().single(), null)
        assertNull(repository.observeFacets().first().single().clockWidgetAppWidgetId)
    }

    @Test
    fun `setClockWidgetAppWidgetId clears the persisted size — a size is only ever valid for the widget it was resized against`() = runTest {
        val dao = FakeFacetDao()
        val repository = FacetRepository(dao)
        val facet = repository.addFacet()
        repository.setClockWidgetAppWidgetId(facet, 42)
        repository.setClockWidgetSize(repository.observeFacets().first().single(), 400, 300)
        val resized = repository.observeFacets().first().single()
        assertEquals(400, resized.clockWidgetWidthDp)

        // Switching to a different widget must not carry the old widget's size over — a real bug
        // found on-device: a size sane for widget A could be outside widget B's own sane bounds,
        // showing "Can't show content" no matter which provider is picked.
        repository.setClockWidgetAppWidgetId(resized, 99)
        val switched = repository.observeFacets().first().single()
        assertEquals(99, switched.clockWidgetAppWidgetId)
        assertNull(switched.clockWidgetWidthDp)
        assertNull(switched.clockWidgetHeightDp)
    }
}
