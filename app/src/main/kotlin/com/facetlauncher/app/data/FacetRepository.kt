package com.facetlauncher.app.data

import com.facetlauncher.app.data.local.ClockStackItemDao
import com.facetlauncher.app.data.local.ClockStackItemEntity
import com.facetlauncher.app.data.local.FacetDao
import com.facetlauncher.app.data.local.toModel
import com.facetlauncher.app.data.local.toEntity
import com.facetlauncher.app.data.local.toFacet
import com.facetlauncher.app.data.model.GlassStyle
import com.facetlauncher.app.data.model.AppListColumnAlignment
import com.facetlauncher.app.data.model.AppListGridColumns
import com.facetlauncher.app.data.model.AppListGridDisplayMode
import com.facetlauncher.app.data.model.AppListShrinkToFit
import com.facetlauncher.app.data.model.AppListLayout
import com.facetlauncher.app.data.model.AppListLimits
import com.facetlauncher.app.data.model.AppListVerticalAlignment
import com.facetlauncher.app.data.model.AppRowPosition
import com.facetlauncher.app.data.model.AppRowPresentation
import com.facetlauncher.app.data.model.CalendarDisplayOptions
import com.facetlauncher.app.data.model.ClockAlignment
import com.facetlauncher.app.data.model.ClockColorOption
import com.facetlauncher.app.data.model.ClockDateStyle
import com.facetlauncher.app.data.model.ClockFontOption
import com.facetlauncher.app.data.model.ClockStackItemKind
import com.facetlauncher.app.data.model.ClockTemplateId
import com.facetlauncher.app.data.model.DockDisplayMode
import com.facetlauncher.app.data.model.Facet
import com.facetlauncher.app.data.model.LandscapeOverrides
import com.facetlauncher.app.data.model.LauncherSettings
import com.facetlauncher.app.data.model.ListContentMode
import com.facetlauncher.app.data.model.WeatherIconStyle
import com.facetlauncher.app.data.model.WidgetWeatherLocation
import com.facetlauncher.app.data.model.landscape
import com.facetlauncher.app.data.model.nativeClockPage
import com.facetlauncher.app.data.model.withLandscape
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

/**
 * Wraps [FacetDao]. The launcher starts with exactly one facet (seeded by
 * [com.facetlauncher.app.domain.EnsureActiveFacetUseCase]); how many it may have is
 * [com.facetlauncher.app.data.model.FacetLimits], enforced by [com.facetlauncher.app.domain.AddFacetUseCase].
 * The last remaining facet can never be deleted ([MIN_FACETS]).
 */
@Singleton
class FacetRepository @Inject constructor(
    private val facetDao: FacetDao,
    private val clockStackItemDao: ClockStackItemDao,
) {

    companion object {
        const val MIN_FACETS = 1
        private const val DEFAULT_FACET_NAME_PREFIX = "Facet "
    }

    /** Each facet comes with its clock stack, so a change to either table re-emits. */
    fun observeFacets(): Flow<List<Facet>> = combine(facetDao.observeAll(), clockStackItemDao.observeAll()) { rows, items ->
        val stacks = items.groupBy({ it.facetId }, { it.toModel() })
        rows.map { it.toFacet(stacks[it.id].orEmpty()) }
    }

    suspend fun getById(id: Long): Facet? = facetDao.getById(id)?.toFacet(clockStackItemDao.getAll(id).map { it.toModel() })

    /** Reads the facet's current row and writes back [transform] of it, so a write never clobbers a newer change with a stale snapshot. */
    private suspend fun update(facetId: Long, transform: (Facet) -> Facet) {
        val current = facetDao.getById(facetId)?.toFacet() ?: return
        facetDao.update(transform(current).toEntity())
    }

    /**
     * Picks the next `Facet N` name and position. The row itself carries no seed values: callers follow with
     * [com.facetlauncher.app.domain.ApplyFacetDefaultsUseCase.applyAll] (backup restore does not).
     */
    suspend fun addFacet(): Facet {
        val existing = facetDao.observeAll().first()
        val nextPosition = (existing.maxOfOrNull { it.position } ?: -1) + 1
        val facet = Facet(name = "$DEFAULT_FACET_NAME_PREFIX${existing.size + 1}", position = nextPosition)
        val id = insertWithNativeClock(facet)
        return facet.copy(id = id, clockStack = listOf(nativeClockPage(facetId = id)))
    }

    suspend fun renameFacet(facetId: Long, newName: String) {
        update(facetId) { it.copy(name = newName) }
    }

    /**
     * Makes the clock placement the facet's own ([Facet.clockPlacementOwn]) with these values; [landscape]'s clock fields
     * are the landscape twins, `null` meaning "same as portrait". Other landscape fields are left alone.
     */
    suspend fun setClockPlacement(facetId: Long, alignment: ClockAlignment, zoneHeightDp: Float?, landscape: LandscapeOverrides) {
        update(facetId) {
            it.copy(clockPlacementOwn = true, clockAlignment = alignment, clockZoneHeightDp = zoneHeightDp)
                .withLandscape(
                    it.landscape.copy(
                        clockAlignment = landscape.clockAlignment,
                        clockZoneHeightDp = landscape.clockZoneHeightDp,
                    ),
                )
        }
    }

    /** Changes this facet's landscape values in one write; a `null` field goes back to "same as portrait". */
    suspend fun updateLandscape(facetId: Long, transform: (LandscapeOverrides) -> LandscapeOverrides) {
        update(facetId) { it.withLandscape(transform(it.landscape)) }
    }

    /**
     * "Reset clock position" for this facet: it stops owning its placement ([Facet.clockPlacementOwn]) and goes back to
     * the launcher's fallback, with its own values cleared, and its clock page loses its saved box (back to the default size).
     */
    suspend fun resetClockPosition(facetId: Long) {
        update(facetId) {
            it.copy(clockPlacementOwn = false, clockZoneHeightDp = null, clockAlignment = ClockAlignment.LEFT)
                .withLandscape(it.landscape.copy(clockZoneHeightDp = null, clockAlignment = null))
        }
        clockStackItemDao.clearClockBoxes(facetId)
    }

    /** Copies the launcher-wide apps-list content settings into this facet (creation or "Reset to default"). */
    suspend fun applyAppsContentDefaults(facetId: Long, settings: LauncherSettings) {
        update(facetId) {
            it.copy(
                listContentMode = settings.listContentMode,
                appsToShowCount = settings.appsToShowCount.coerceIn(AppListLimits.MIN_APPS_TO_SHOW, AppListLimits.MAX_APPS_TO_SHOW),
            )
        }
    }

    /** Copies the launcher-wide dock display style into this facet (creation or "Reset to default"). */
    suspend fun applyDockDefaults(facetId: Long, settings: LauncherSettings) {
        update(facetId) { it.copy(dockDisplayMode = settings.dockDisplayMode) }
    }

    /** Copies the launcher-wide app-list look, landscape twins included, into this facet (creation or "Reset to default"). */
    suspend fun applyLookDefaults(facetId: Long, settings: LauncherSettings) {
        update(facetId) {
            it.copy(
                appRowPosition = settings.appRowPosition,
                appRowPresentation = settings.appRowPresentation,
                appListVerticalAlignment = settings.appListVerticalAlignment,
                appListLayout = settings.appListLayout,
                appListColumnAlignment = settings.appListColumnAlignment,
                appListGridColumns = settings.appListGridColumns,
                appListGridDisplayMode = settings.appListGridDisplayMode,
                appListShrinkToFit = settings.appListShrinkToFit,
            ).withLandscape(
                it.landscape.copy(
                    appRowPosition = settings.landscape.appRowPosition,
                    appListVerticalAlignment = settings.landscape.appListVerticalAlignment,
                    appListColumnAlignment = settings.landscape.appListColumnAlignment,
                ),
            )
        }
    }

    /** Copies [sourceId]'s Dock display style onto [targetId]. */
    suspend fun copyDockSettings(sourceId: Long, targetId: Long) {
        val source = facetDao.getById(sourceId)?.toFacet() ?: return
        update(targetId) { it.copy(dockDisplayMode = source.dockDisplayMode, dockGlass = source.dockGlass) }
    }

    /** Copies [sourceId]'s Apps list settings (content, count, look, landscape twins, Allow landscape) onto [targetId]. */
    suspend fun copyAppsListSettings(sourceId: Long, targetId: Long) {
        val source = facetDao.getById(sourceId)?.toFacet() ?: return
        update(targetId) {
            it.copy(
                listContentMode = source.listContentMode,
                appsToShowCount = source.appsToShowCount,
                appRowPosition = source.appRowPosition,
                appRowPresentation = source.appRowPresentation,
                appListVerticalAlignment = source.appListVerticalAlignment,
                appListLayout = source.appListLayout,
                appListColumnAlignment = source.appListColumnAlignment,
                appListGridColumns = source.appListGridColumns,
                appListGridDisplayMode = source.appListGridDisplayMode,
                appListShrinkToFit = source.appListShrinkToFit,
                allowLandscape = source.allowLandscape,
            ).withLandscape(
                it.landscape.copy(
                    appRowPosition = source.landscape.appRowPosition,
                    appListVerticalAlignment = source.landscape.appListVerticalAlignment,
                    appListColumnAlignment = source.landscape.appListColumnAlignment,
                ),
            )
        }
    }

    /** Copies [sourceId]'s clock placement (alignment and zone height, landscape twins included) onto [targetId]. */
    suspend fun copyClockPlacement(sourceId: Long, targetId: Long) {
        val source = facetDao.getById(sourceId)?.toFacet() ?: return
        update(targetId) {
            it.copy(
                clockPlacementOwn = source.clockPlacementOwn,
                clockAlignment = source.clockAlignment,
                clockZoneHeightDp = source.clockZoneHeightDp,
            ).withLandscape(
                it.landscape.copy(
                    clockAlignment = source.landscape.clockAlignment,
                    clockZoneHeightDp = source.landscape.clockZoneHeightDp,
                ),
            )
        }
    }

    suspend fun setAppRowPosition(facetId: Long, position: AppRowPosition) {
        update(facetId) { it.copy(appRowPosition = position) }
    }

    suspend fun setAllowLandscape(facetId: Long, allow: Boolean) {
        update(facetId) { it.copy(allowLandscape = allow) }
    }

    suspend fun setAppRowPresentation(facetId: Long, presentation: AppRowPresentation) {
        update(facetId) { it.copy(appRowPresentation = presentation) }
    }

    suspend fun setListContentMode(facetId: Long, mode: ListContentMode) {
        update(facetId) { it.copy(listContentMode = mode) }
    }

    /** Coerced into [AppListLimits]' range (reduced from README's original `3d` 4…8 — see chat history). */
    suspend fun setAppsToShowCount(facetId: Long, count: Int) {
        update(facetId) { it.copy(appsToShowCount = count.coerceIn(AppListLimits.MIN_APPS_TO_SHOW, AppListLimits.MAX_APPS_TO_SHOW)) }
    }

    suspend fun setAppListVerticalAlignment(facetId: Long, alignment: AppListVerticalAlignment) {
        update(facetId) { it.copy(appListVerticalAlignment = alignment) }
    }

    suspend fun setAppListLayout(facetId: Long, layout: AppListLayout) {
        update(facetId) { it.copy(appListLayout = layout) }
    }

    suspend fun setAppListColumnAlignment(facetId: Long, alignment: AppListColumnAlignment) {
        update(facetId) { it.copy(appListColumnAlignment = alignment) }
    }

    suspend fun setAppListGridColumns(facetId: Long, columns: AppListGridColumns) {
        update(facetId) { it.copy(appListGridColumns = columns) }
    }

    suspend fun setAppListGridDisplayMode(facetId: Long, mode: AppListGridDisplayMode) {
        update(facetId) { it.copy(appListGridDisplayMode = mode) }
    }

    suspend fun setAppListShrinkToFit(facetId: Long, shrinkToFit: AppListShrinkToFit) {
        update(facetId) { it.copy(appListShrinkToFit = shrinkToFit) }
    }

    suspend fun setDockDisplayMode(facetId: Long, mode: DockDisplayMode) {
        update(facetId) { it.copy(dockDisplayMode = mode) }
    }

    suspend fun setDockGlass(facetId: Long, glass: GlassStyle) {
        update(facetId) { it.copy(dockGlass = glass) }
    }

    suspend fun setAppsListGlass(facetId: Long, glass: GlassStyle) {
        update(facetId) { it.copy(appsListGlass = glass) }
    }

    suspend fun deleteFacet(facetId: Long) {
        facetDao.getById(facetId)?.let { facetDao.delete(it) }
    }

    /** F14 Backup & Restore — wipes every facet (cascades to favorites) before restoring a backup's own list. */
    suspend fun deleteAllFacets() {
        facetDao.deleteAll()
    }

    /** F14 Backup & Restore — inserts [facet] as a brand-new row (its own `id` is ignored, a fresh one is autogenerated) and returns that new id. */
    suspend fun restoreFacet(facet: Facet): Long = insertWithNativeClock(facet.copy(id = 0))

    /** Every facet starts with its native clock as the stack's one page: an empty stack draws nothing. */
    private suspend fun insertWithNativeClock(facet: Facet): Long {
        val id = facetDao.insert(facet.toEntity())
        clockStackItemDao.insert(ClockStackItemEntity(facetId = id, position = 0, kind = ClockStackItemKind.CLOCK))
        return id
    }

    /** Positions the facets in [orderedFacetIds] order. */
    suspend fun reorderFacets(orderedFacetIds: List<Long>) {
        orderedFacetIds.forEachIndexed { index, id -> update(id) { it.copy(position = index) } }
    }
}
