package com.facetlauncher.app.data

import com.facetlauncher.app.data.local.FacetDao
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
import com.facetlauncher.app.data.model.ClockAlignment
import com.facetlauncher.app.data.model.ClockColorOption
import com.facetlauncher.app.data.model.ClockDateStyle
import com.facetlauncher.app.data.model.ClockFontOption
import com.facetlauncher.app.data.model.ClockTemplateId
import com.facetlauncher.app.data.model.DockDisplayMode
import com.facetlauncher.app.data.model.Facet
import com.facetlauncher.app.data.model.LandscapeOverrides
import com.facetlauncher.app.data.model.ListContentMode
import com.facetlauncher.app.data.model.landscape
import com.facetlauncher.app.data.model.withLandscape
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

/**
 * Wraps [FacetDao]. The launcher starts with exactly one facet (seeded by
 * [com.facetlauncher.app.domain.EnsureActiveFacetUseCase]); how many it may have is
 * [com.facetlauncher.app.data.model.FacetLimits], enforced by [com.facetlauncher.app.domain.AddFacetUseCase].
 * The last remaining facet can never be deleted ([MIN_FACETS]).
 */
@Singleton
class FacetRepository @Inject constructor(private val facetDao: FacetDao) {

    companion object {
        const val MIN_FACETS = 1
        private const val DEFAULT_FACET_NAME_PREFIX = "Facet "
    }

    fun observeFacets(): Flow<List<Facet>> = facetDao.observeAll().map { rows -> rows.map { it.toFacet() } }

    suspend fun getById(id: Long): Facet? = facetDao.getById(id)?.toFacet()

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
        val id = facetDao.insert(facet.toEntity())
        return facet.copy(id = id)
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

    suspend fun setClockAlignment(facetId: Long, alignment: ClockAlignment) {
        update(facetId) { it.copy(clockAlignment = alignment) }
    }

    suspend fun setClockZoneHeight(facetId: Long, heightDp: Float) {
        update(facetId) { it.copy(clockZoneHeightDp = heightDp) }
    }

    /** Changes this facet's landscape values in one write; a `null` field goes back to "same as portrait". */
    suspend fun updateLandscape(facetId: Long, transform: (LandscapeOverrides) -> LandscapeOverrides) {
        update(facetId) { it.withLandscape(transform(it.landscape)) }
    }

    /** Restores this facet's clock+calendar block to its natural, undragged position. */
    suspend fun resetClockZoneHeight(facetId: Long) {
        update(facetId) { it.copy(clockZoneHeightDp = null).withLandscape(it.landscape.copy(clockZoneHeightDp = null)) }
    }

    suspend fun setClockScale(facetId: Long, scale: Float) {
        update(facetId) { it.copy(clockScale = scale) }
    }

    /**
     * PRD F15 — binds [appWidgetId] as this facet's hosted clock widget, or (`null`) reverts it
     * to its native clock. The caller owns the real `AppWidgetHost` allocate/bind/configure
     * round trip (see `ClockWidgetPickerViewModel`) and releasing the old id via
     * `AppWidgetRepository.deleteAppWidgetId` when switching away from or replacing one — this
     * just persists which id (if any) is currently bound.
     */
    suspend fun setClockWidgetAppWidgetId(facetId: Long, appWidgetId: Int?) {
        // Also clears the persisted size (real bug, found on-device): a facet's clockWidgetWidthDp/
        // HeightDp is only ever a valid size for the widget it was resized against. Carrying it
        // over to a newly-bound widget (a switch, or reverting to null) can force the new widget
        // outside its own sane bounds and show "Can't show content" indefinitely, regardless of
        // which provider is picked — matches Facet.clockWidgetWidthDp's own doc ("meaningless
        // whenever clockWidgetAppWidgetId is null"), extended here to "meaningless for a *different*
        // non-null id too".
        update(facetId) {
            it.copy(clockWidgetAppWidgetId = appWidgetId, clockWidgetWidthDp = null, clockWidgetHeightDp = null)
                .withLandscape(it.landscape.copy(clockWidgetWidthDp = null, clockWidgetHeightDp = null))
        }
    }

    /** PRD F15's interactive resize commit — see [Facet.clockWidgetWidthDp]'s own doc. Landscape's own size goes through [updateLandscape]. */
    suspend fun setClockWidgetSize(facetId: Long, widthDp: Int, heightDp: Int) {
        update(facetId) { it.copy(clockWidgetWidthDp = widthDp, clockWidgetHeightDp = heightDp) }
    }

    /**
     * "Reset clock widget position" for this facet — the zone height back to `null`, alignment
     * back to `LEFT`, and scale back to `0.8f`, in one atomic upsert (avoids the clobbering risk
     * of sequential single-field upserts against the same stale snapshot).
     */
    suspend fun resetClockPosition(facetId: Long) {
        update(facetId) {
            it.copy(clockZoneHeightDp = null, clockAlignment = ClockAlignment.LEFT, clockScale = 0.8f)
                .withLandscape(it.landscape.copy(clockZoneHeightDp = null, clockAlignment = null, clockScale = null))
        }
    }

    /**
     * Seeds the facet's clock *design* settings from effective values and toggles the override
     * flag. One toggle for the whole Home clock/calendar block — it's a single visual unit, not
     * independent design decisions — which is why `clockAlignment`/`clockZoneHeightDp` (moved in
     * from being global-only) are seeded here too, not by [updateOverridingCalendar] (that one is
     * calendar *selection* only — which calendars, which events — see its own doc comment). The
     * calendar events strip no longer has its own font/color/weight to seed — it reads Appearance
     * (global-only) and this block's own `clockAlignment` (see chat history). Atomic upsert to
     * avoid clobbering. The landscape alignment is seeded the same way, so starting to override doesn't change a
     * facet's landscape look.
     */
    suspend fun updateOverridingClock(
        facetId: Long,
        overriding: Boolean,
        templateId: ClockTemplateId,
        fontOption: ClockFontOption,
        colorOption: ClockColorOption,
        use24HourTime: Boolean,
        showMeridiem: Boolean,
        clockAlignment: ClockAlignment,
        landscapeClockAlignment: ClockAlignment?,
        clockZoneHeightDp: Float?,
        clockScale: Float,
        accentColorOption: ClockColorOption,
        dateStyle: ClockDateStyle,
        showAlarm: Boolean = true,
        showBattery: Boolean = true,
        showWeather: Boolean = false,
    ) {
        update(facetId) {
            it.copy(
                overrideClock = overriding,
                clockShowAlarm = showAlarm,
                clockShowBattery = showBattery,
                clockShowWeather = showWeather,
                clockTemplateId = templateId,
                clockFontOption = fontOption,
                clockColorOption = colorOption,
                use24HourTime = use24HourTime,
                clockShowMeridiem = showMeridiem,
                clockAlignment = clockAlignment,
                clockZoneHeightDp = clockZoneHeightDp,
                clockScale = clockScale,
                clockAccentColorOption = accentColorOption,
                clockDateStyle = dateStyle,
            ).withLandscape(it.landscape.copy(clockAlignment = landscapeClockAlignment))
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
    suspend fun restoreFacet(facet: Facet): Long = facetDao.insert(facet.copy(id = 0).toEntity())

    /** Positions the facets in [orderedFacetIds] order. */
    suspend fun reorderFacets(orderedFacetIds: List<Long>) {
        orderedFacetIds.forEachIndexed { index, id -> update(id) { it.copy(position = index) } }
    }
}
