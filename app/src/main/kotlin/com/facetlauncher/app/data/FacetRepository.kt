package com.facetlauncher.app.data

import com.facetlauncher.app.data.local.ClockStackItemDao
import com.facetlauncher.app.data.local.ClockStackItemEntity
import com.facetlauncher.app.data.local.FacetDao
import com.facetlauncher.app.data.local.toModel
import com.facetlauncher.app.data.local.toEntity
import com.facetlauncher.app.data.local.toFacet
import com.facetlauncher.app.data.model.AppListColumnAlignment
import com.facetlauncher.app.data.model.AppListGridColumns
import com.facetlauncher.app.data.model.AppListGridDisplayMode
import com.facetlauncher.app.data.model.AppListLayout
import com.facetlauncher.app.data.model.AppListLimits
import com.facetlauncher.app.data.model.AppListVerticalAlignment
import com.facetlauncher.app.data.model.AppRowPosition
import com.facetlauncher.app.data.model.AppRowPresentation
import com.facetlauncher.app.data.model.CalendarDisplayOptions
import com.facetlauncher.app.data.model.DEFAULT_CLOCK_SCALE
import com.facetlauncher.app.data.model.ClockAlignment
import com.facetlauncher.app.data.model.ClockColorOption
import com.facetlauncher.app.data.model.ClockDateStyle
import com.facetlauncher.app.data.model.ClockFontOption
import com.facetlauncher.app.data.model.ClockStackItemKind
import com.facetlauncher.app.data.model.ClockTemplateId
import com.facetlauncher.app.data.model.DockDisplayMode
import com.facetlauncher.app.data.model.Facet
import com.facetlauncher.app.data.model.LandscapeOverrides
import com.facetlauncher.app.data.model.ListContentMode
import com.facetlauncher.app.data.model.WeatherIconStyle
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
     * A new facet has no per-facet settings to copy yet — it inherits the launcher-wide
     * default Favorites list (see [PlacementRepository]) until it overrides them, and
     * this just picks the next `Facet N` name and position.
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

    /** Reverts this facet to inheriting the global default or switches to overriding. */
    suspend fun setOverrideClock(facetId: Long, overriding: Boolean) {
        update(facetId) { it.copy(overrideClock = overriding) }
    }

    suspend fun setClockTemplateId(facetId: Long, id: ClockTemplateId) {
        update(facetId) { it.copy(clockTemplateId = id) }
    }

    suspend fun setClockFontOption(facetId: Long, option: ClockFontOption) {
        update(facetId) { it.copy(clockFontOption = option) }
    }

    suspend fun setClockColorOption(facetId: Long, option: ClockColorOption) {
        update(facetId) { it.copy(clockColorOption = option) }
    }

    /** See [Facet.clockAccentColorOption]. */
    suspend fun setClockAccentColorOption(facetId: Long, option: ClockColorOption) {
        update(facetId) { it.copy(clockAccentColorOption = option) }
    }

    suspend fun setUse24HourTime(facetId: Long, enabled: Boolean) {
        update(facetId) { it.copy(use24HourTime = enabled) }
    }

    /** See [Facet.clockDateStyle]. */
    suspend fun setClockDateStyle(facetId: Long, dateStyle: ClockDateStyle) {
        update(facetId) { it.copy(clockDateStyle = dateStyle) }
    }

    suspend fun setClockShowMeridiem(facetId: Long, enabled: Boolean) {
        update(facetId) { it.copy(clockShowMeridiem = enabled) }
    }

    /** See [Facet.clockShowAlarm]. */
    suspend fun setClockShowAlarm(facetId: Long, enabled: Boolean) {
        update(facetId) { it.copy(clockShowAlarm = enabled) }
    }

    suspend fun setClockShowBattery(facetId: Long, enabled: Boolean) {
        update(facetId) { it.copy(clockShowBattery = enabled) }
    }

    suspend fun setClockShowWeather(facetId: Long, enabled: Boolean) {
        update(facetId) { it.copy(clockShowWeather = enabled) }
    }

    /** See [Facet.clockWeatherIconStyle]. */
    suspend fun setClockWeatherIconStyle(facetId: Long, style: WeatherIconStyle) {
        update(facetId) { it.copy(clockWeatherIconStyle = style) }
    }

    /**
     * Makes the clock placement the facet's own ([Facet.clockPlacementOwn]) with these values; [landscape]'s clock fields
     * are the landscape twins, `null` meaning "same as portrait". Other landscape fields are left alone.
     */
    suspend fun setClockPlacement(facetId: Long, alignment: ClockAlignment, zoneHeightDp: Float?, scale: Float, landscape: LandscapeOverrides) {
        update(facetId) {
            it.copy(clockPlacementOwn = true, clockAlignment = alignment, clockZoneHeightDp = zoneHeightDp, clockScale = scale)
                .withLandscape(
                    it.landscape.copy(
                        clockAlignment = landscape.clockAlignment,
                        clockZoneHeightDp = landscape.clockZoneHeightDp,
                        clockScale = landscape.clockScale,
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
     * the launcher's fallback, with its own values cleared, in one atomic write.
     */
    suspend fun resetClockPosition(facetId: Long) {
        update(facetId) {
            it.copy(clockPlacementOwn = false, clockZoneHeightDp = null, clockAlignment = ClockAlignment.LEFT, clockScale = DEFAULT_CLOCK_SCALE)
                .withLandscape(it.landscape.copy(clockZoneHeightDp = null, clockAlignment = null, clockScale = null))
        }
    }

    /**
     * Seeds the facet's clock *style* from the effective values and toggles the override flag. One toggle for the
     * whole style and accessories block, which is one visual unit, not independent decisions: template, font, colours,
     * date, time format and the alarm, battery and weather accessories. Placement (alignment, scale, divider) is not
     * part of it: it is always the facet's own ([Facet.clockPlacementOwn]). The calendar *selection* is separate, see
     * [updateOverridingCalendar]. One atomic write, to avoid clobbering.
     */
    suspend fun updateOverridingClock(
        facetId: Long,
        overriding: Boolean,
        templateId: ClockTemplateId,
        fontOption: ClockFontOption,
        colorOption: ClockColorOption,
        use24HourTime: Boolean,
        showMeridiem: Boolean,
        accentColorOption: ClockColorOption,
        dateStyle: ClockDateStyle,
        showAlarm: Boolean = true,
        showBattery: Boolean = true,
        showWeather: Boolean = false,
        weatherIconStyle: WeatherIconStyle = WeatherIconStyle.COLORED,
    ) {
        update(facetId) {
            it.copy(
                overrideClock = overriding,
                clockShowAlarm = showAlarm,
                clockShowBattery = showBattery,
                clockShowWeather = showWeather,
                clockWeatherIconStyle = weatherIconStyle,
                clockTemplateId = templateId,
                clockFontOption = fontOption,
                clockColorOption = colorOption,
                use24HourTime = use24HourTime,
                clockShowMeridiem = showMeridiem,
                clockAccentColorOption = accentColorOption,
                clockDateStyle = dateStyle,
            )
        }
    }

    /** Reverts this facet to inheriting the global default or switches to overriding. */
    suspend fun setOverrideApps(facetId: Long, overriding: Boolean) {
        update(facetId) { it.copy(overrideApps = overriding) }
    }

    suspend fun setAppRowPosition(facetId: Long, position: AppRowPosition) {
        update(facetId) { it.copy(appRowPosition = position) }
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

    suspend fun setOverridingFavorites(facetId: Long, overriding: Boolean) {
        update(facetId) { it.copy(overridingFavorites = overriding) }
    }

    /**
     * Seeds the facet's app *content* settings from effective values and toggles the override
     * flag. Atomic upsert to avoid clobbering. `appRowPosition`/`appRowPresentation`/
     * `appListVerticalAlignment`/`appListLayout`/`appListColumnAlignment`/`appListGridColumns`/
     * `appListGridDisplayMode` are no longer part of this bundle — they're "look" fields, edited
     * from Settings → Appearance independently now, resolved via their own `LAUNCHER_DEFAULT`
     * sentinel regardless of [overriding] (see [setAppRowPosition]/[setAppRowPresentation]/
     * [setAppListVerticalAlignment] and [com.facetlauncher.app.data.local.resolveSentinel]).
     */
    suspend fun updateOverridingApps(
        facetId: Long,
        overriding: Boolean,
        mode: ListContentMode,
        count: Int,
        overridingFavorites: Boolean,
    ) {
        update(facetId) {
            it.copy(
                overrideApps = overriding,
                listContentMode = mode,
                appsToShowCount = count.coerceIn(AppListLimits.MIN_APPS_TO_SHOW, AppListLimits.MAX_APPS_TO_SHOW),
                overridingFavorites = overridingFavorites,
            )
        }
    }

    suspend fun setDockDisplayMode(facetId: Long, mode: DockDisplayMode) {
        update(facetId) { it.copy(dockDisplayMode = mode) }
    }

    /**
     * The Dock card's single Inherit/Override switch — governs only the dock's own app list (this
     * facet's own [com.facetlauncher.app.data.local.FacetDockAppEntity] rows) now; its Icons/Text
     * display style is edited from Settings → Appearance independently, resolved via its own
     * `LAUNCHER_DEFAULT` sentinel regardless of [overriding] (see [setDockDisplayMode] and
     * [com.facetlauncher.app.data.local.resolveSentinel]).
     */
    suspend fun updateOverridingDock(facetId: Long, overriding: Boolean) {
        update(facetId) { it.copy(overrideDock = overriding) }
    }

    /** Reverts this facet to inheriting the global default or switches to overriding. */
    suspend fun setOverrideCalendar(facetId: Long, overriding: Boolean) {
        update(facetId) { it.copy(overrideCalendar = overriding) }
    }

    suspend fun setShowAllDayEvents(facetId: Long, enabled: Boolean) {
        update(facetId) { it.copy(showAllDayEvents = enabled) }
    }

    suspend fun setShowCalendarIndicator(facetId: Long, enabled: Boolean) {
        update(facetId) { it.copy(showCalendarIndicator = enabled) }
    }

    suspend fun setCalendarItemsToShow(facetId: Long, count: Int) {
        update(facetId) { it.copy(calendarItemsToShow = count.coerceIn(CalendarDisplayOptions.MIN_ITEMS, CalendarDisplayOptions.MAX_ITEMS)) }
    }

    suspend fun setSelectedCalendarIds(facetId: Long, ids: Set<String>?) {
        update(facetId) { it.copy(selectedCalendarIds = ids) }
    }

    /**
     * Seeds the facet's calendar *selection* (which calendars, which kinds of events — content,
     * not design) from effective values and toggles the override flag. Atomic upsert to avoid
     * clobbering. See [updateOverridingClock] for the separate, merged Clock+Calendar *design*
     * override this deliberately excludes (font/color moved there — see chat history).
     */
    suspend fun updateOverridingCalendar(
        facetId: Long,
        overriding: Boolean,
        showAllDayEvents: Boolean,
        selectedCalendarIds: Set<String>?,
        showCalendarIndicator: Boolean,
        calendarItemsToShow: Int,
    ) {
        update(facetId) {
            it.copy(
                overrideCalendar = overriding,
                showAllDayEvents = showAllDayEvents,
                showCalendarIndicator = showCalendarIndicator,
                calendarItemsToShow = calendarItemsToShow,
                selectedCalendarIds = selectedCalendarIds,
            )
        }
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
