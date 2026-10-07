package com.facetlauncher.app.data

import com.facetlauncher.app.data.local.FacetDao
import com.facetlauncher.app.data.local.FacetEntity
import com.facetlauncher.app.data.model.AppListColumnAlignment
import com.facetlauncher.app.data.model.AppListGridColumns
import com.facetlauncher.app.data.model.AppListGridDisplayMode
import com.facetlauncher.app.data.model.AppListLayout
import com.facetlauncher.app.data.model.AppListLimits
import com.facetlauncher.app.data.model.AppListVerticalAlignment
import com.facetlauncher.app.data.model.AppRowPosition
import com.facetlauncher.app.data.model.AppRowPresentation
import com.facetlauncher.app.data.model.ClockAlignment
import com.facetlauncher.app.data.model.ClockColorOption
import com.facetlauncher.app.data.model.ClockDateStyle
import com.facetlauncher.app.data.model.ClockFontOption
import com.facetlauncher.app.data.model.ClockTemplateId
import com.facetlauncher.app.data.model.DockDisplayMode
import com.facetlauncher.app.data.model.ListContentMode
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import javax.inject.Inject
import javax.inject.Singleton

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

    fun observeFacets(): Flow<List<FacetEntity>> = facetDao.observeAll()

    suspend fun getById(id: Long): FacetEntity? = facetDao.getById(id)

    /**
     * A new facet has no per-facet settings to copy yet — it inherits the launcher-wide
     * default Favorites list (see [DefaultFavoriteAppRepository]) until it overrides them, and
     * this just picks the next `Facet N` name and position.
     */
    suspend fun addFacet(): FacetEntity {
        val existing = facetDao.observeAll().first()
        val nextPosition = (existing.maxOfOrNull { it.position } ?: -1) + 1
        val facet = FacetEntity(name = "$DEFAULT_FACET_NAME_PREFIX${existing.size + 1}", position = nextPosition)
        val id = facetDao.insert(facet)
        return facet.copy(id = id)
    }

    suspend fun renameFacet(facet: FacetEntity, newName: String) {
        facetDao.update(facet.copy(name = newName))
    }

    /** Reverts this facet to inheriting the global default or switches to overriding. */
    suspend fun setOverrideClock(facet: FacetEntity, overriding: Boolean) {
        facetDao.update(facet.copy(overrideClock = overriding))
    }

    suspend fun setClockTemplateId(facet: FacetEntity, id: ClockTemplateId) {
        facetDao.update(facet.copy(clockTemplateId = id))
    }

    suspend fun setClockFontOption(facet: FacetEntity, option: ClockFontOption) {
        facetDao.update(facet.copy(clockFontOption = option))
    }

    suspend fun setClockColorOption(facet: FacetEntity, option: ClockColorOption) {
        facetDao.update(facet.copy(clockColorOption = option))
    }

    /** See [FacetEntity.clockAccentColorOption]. */
    suspend fun setClockAccentColorOption(facet: FacetEntity, option: ClockColorOption) {
        facetDao.update(facet.copy(clockAccentColorOption = option))
    }

    suspend fun setUse24HourTime(facet: FacetEntity, enabled: Boolean) {
        facetDao.update(facet.copy(use24HourTime = enabled))
    }

    /** See [FacetEntity.clockDateStyle]. */
    suspend fun setClockDateStyle(facet: FacetEntity, dateStyle: ClockDateStyle) {
        facetDao.update(facet.copy(clockDateStyle = dateStyle))
    }

    suspend fun setClockShowMeridiem(facet: FacetEntity, enabled: Boolean) {
        facetDao.update(facet.copy(clockShowMeridiem = enabled))
    }

    suspend fun setClockAlignment(facet: FacetEntity, alignment: ClockAlignment) {
        facetDao.update(facet.copy(clockAlignment = alignment))
    }

    suspend fun setClockZoneHeight(facet: FacetEntity, heightDp: Float) {
        facetDao.update(facet.copy(clockZoneHeightDp = heightDp))
    }

    /** Restores this facet's clock+calendar block to its natural, undragged position. */
    suspend fun resetClockZoneHeight(facet: FacetEntity) {
        facetDao.update(facet.copy(clockZoneHeightDp = null))
    }

    suspend fun setClockScale(facet: FacetEntity, scale: Float) {
        facetDao.update(facet.copy(clockScale = scale))
    }

    /**
     * PRD F15 — binds [appWidgetId] as this facet's hosted clock widget, or (`null`) reverts it
     * to its native clock. The caller owns the real `AppWidgetHost` allocate/bind/configure
     * round trip (see `ClockWidgetPickerViewModel`) and releasing the old id via
     * `AppWidgetRepository.deleteAppWidgetId` when switching away from or replacing one — this
     * just persists which id (if any) is currently bound.
     */
    suspend fun setClockWidgetAppWidgetId(facet: FacetEntity, appWidgetId: Int?) {
        // Also clears the persisted size (real bug, found on-device): a facet's clockWidgetWidthDp/
        // HeightDp is only ever a valid size for the widget it was resized against. Carrying it
        // over to a newly-bound widget (a switch, or reverting to null) can force the new widget
        // outside its own sane bounds and show "Can't show content" indefinitely, regardless of
        // which provider is picked — matches FacetEntity.clockWidgetWidthDp's own doc ("meaningless
        // whenever clockWidgetAppWidgetId is null"), extended here to "meaningless for a *different*
        // non-null id too".
        facetDao.update(facet.copy(clockWidgetAppWidgetId = appWidgetId, clockWidgetWidthDp = null, clockWidgetHeightDp = null))
    }

    /** PRD F15's interactive resize commit — see [FacetEntity.clockWidgetWidthDp]'s own doc. */
    suspend fun setClockWidgetSize(facet: FacetEntity, widthDp: Int, heightDp: Int) {
        facetDao.update(facet.copy(clockWidgetWidthDp = widthDp, clockWidgetHeightDp = heightDp))
    }

    /**
     * "Reset clock widget position" for this facet — the zone height back to `null`, alignment
     * back to `LEFT`, and scale back to `0.8f`, in one atomic upsert (avoids the clobbering risk
     * of sequential single-field upserts against the same stale snapshot).
     */
    suspend fun resetClockPosition(facet: FacetEntity) {
        facetDao.update(
            facet.copy(
                clockZoneHeightDp = null,
                clockAlignment = ClockAlignment.LEFT,
                clockScale = 0.8f,
            ),
        )
    }

    /**
     * Seeds the facet's clock *design* settings from effective values and toggles the override
     * flag. One toggle for the whole Home clock/calendar block — it's a single visual unit, not
     * independent design decisions — which is why `clockAlignment`/`clockZoneHeightDp` (moved in
     * from being global-only) are seeded here too, not by [updateOverridingCalendar] (that one is
     * calendar *selection* only — which calendars, which events — see its own doc comment). The
     * calendar events strip no longer has its own font/color/weight to seed — it reads Appearance
     * (global-only) and this block's own `clockAlignment` (see chat history). Atomic upsert to
     * avoid clobbering.
     */
    suspend fun updateOverridingClock(
        facet: FacetEntity,
        overriding: Boolean,
        templateId: ClockTemplateId,
        fontOption: ClockFontOption,
        colorOption: ClockColorOption,
        use24HourTime: Boolean,
        showMeridiem: Boolean,
        clockAlignment: ClockAlignment = facet.clockAlignment,
        clockZoneHeightDp: Float? = facet.clockZoneHeightDp,
        clockScale: Float = facet.clockScale,
        accentColorOption: ClockColorOption = facet.clockAccentColorOption,
        dateStyle: ClockDateStyle = facet.clockDateStyle,
    ) {
        facetDao.update(
            facet.copy(
                overrideClock = overriding,
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
            ),
        )
    }

    /** Reverts this facet to inheriting the global default or switches to overriding. */
    suspend fun setOverrideApps(facet: FacetEntity, overriding: Boolean) {
        facetDao.update(facet.copy(overrideApps = overriding))
    }

    suspend fun setAppRowPosition(facet: FacetEntity, position: AppRowPosition) {
        facetDao.update(facet.copy(appRowPosition = position))
    }

    suspend fun setAppRowPresentation(facet: FacetEntity, presentation: AppRowPresentation) {
        facetDao.update(facet.copy(appRowPresentation = presentation))
    }

    suspend fun setListContentMode(facet: FacetEntity, mode: ListContentMode) {
        facetDao.update(facet.copy(listContentMode = mode))
    }

    /** Coerced into [AppListLimits]' range (reduced from README's original `3d` 4…8 — see chat history). */
    suspend fun setAppsToShowCount(facet: FacetEntity, count: Int) {
        facetDao.update(facet.copy(appsToShowCount = count.coerceIn(AppListLimits.MIN_APPS_TO_SHOW, AppListLimits.MAX_APPS_TO_SHOW)))
    }

    suspend fun setAppListVerticalAlignment(facet: FacetEntity, alignment: AppListVerticalAlignment) {
        facetDao.update(facet.copy(appListVerticalAlignment = alignment))
    }

    suspend fun setAppListLayout(facet: FacetEntity, layout: AppListLayout) {
        facetDao.update(facet.copy(appListLayout = layout))
    }

    suspend fun setAppListColumnAlignment(facet: FacetEntity, alignment: AppListColumnAlignment) {
        facetDao.update(facet.copy(appListColumnAlignment = alignment))
    }

    suspend fun setAppListGridColumns(facet: FacetEntity, columns: AppListGridColumns) {
        facetDao.update(facet.copy(appListGridColumns = columns))
    }

    suspend fun setAppListGridDisplayMode(facet: FacetEntity, mode: AppListGridDisplayMode) {
        facetDao.update(facet.copy(appListGridDisplayMode = mode))
    }

    suspend fun setOverridingFavorites(facet: FacetEntity, overriding: Boolean) {
        facetDao.update(facet.copy(overridingFavorites = overriding))
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
        facet: FacetEntity,
        overriding: Boolean,
        mode: ListContentMode,
        count: Int,
        overridingFavorites: Boolean,
    ) {
        facetDao.update(
            facet.copy(
                overrideApps = overriding,
                listContentMode = mode,
                appsToShowCount = count.coerceIn(AppListLimits.MIN_APPS_TO_SHOW, AppListLimits.MAX_APPS_TO_SHOW),
                overridingFavorites = overridingFavorites,
            ),
        )
    }

    suspend fun setDockDisplayMode(facet: FacetEntity, mode: DockDisplayMode) {
        facetDao.update(facet.copy(dockDisplayMode = mode))
    }

    /**
     * The Dock card's single Inherit/Override switch — governs only the dock's own app list (this
     * facet's own [com.facetlauncher.app.data.local.FacetDockAppEntity] rows) now; its Icons/Text
     * display style is edited from Settings → Appearance independently, resolved via its own
     * `LAUNCHER_DEFAULT` sentinel regardless of [overriding] (see [setDockDisplayMode] and
     * [com.facetlauncher.app.data.local.resolveSentinel]).
     */
    suspend fun updateOverridingDock(facet: FacetEntity, overriding: Boolean) {
        facetDao.update(facet.copy(overrideDock = overriding))
    }

    /** Reverts this facet to inheriting the global default or switches to overriding. */
    suspend fun setOverrideCalendar(facet: FacetEntity, overriding: Boolean) {
        facetDao.update(facet.copy(overrideCalendar = overriding))
    }

    suspend fun setShowAllDayEvents(facet: FacetEntity, enabled: Boolean) {
        facetDao.update(facet.copy(showAllDayEvents = enabled))
    }

    suspend fun setSelectedCalendarIds(facet: FacetEntity, ids: Set<String>?) {
        facetDao.update(facet.copy(selectedCalendarIdsCsv = ids.toCsv()))
    }

    /**
     * Seeds the facet's calendar *selection* (which calendars, which kinds of events — content,
     * not design) from effective values and toggles the override flag. Atomic upsert to avoid
     * clobbering. See [updateOverridingClock] for the separate, merged Clock+Calendar *design*
     * override this deliberately excludes (font/color moved there — see chat history).
     */
    suspend fun updateOverridingCalendar(
        facet: FacetEntity,
        overriding: Boolean,
        showAllDayEvents: Boolean,
        selectedCalendarIds: Set<String>?,
    ) {
        facetDao.update(
            facet.copy(
                overrideCalendar = overriding,
                showAllDayEvents = showAllDayEvents,
                selectedCalendarIdsCsv = selectedCalendarIds.toCsv(),
            ),
        )
    }

    suspend fun deleteFacet(facet: FacetEntity) {
        facetDao.delete(facet)
    }

    /** F14 Backup & Restore — wipes every facet (cascades to favorites) before restoring a backup's own list. */
    suspend fun deleteAllFacets() {
        facetDao.deleteAll()
    }

    /** F14 Backup & Restore — inserts [facet] as a brand-new row (its own `id` is ignored, a fresh one is autogenerated) and returns that new id. */
    suspend fun restoreFacet(facet: FacetEntity): Long = facetDao.insert(facet.copy(id = 0))

    suspend fun reorderFacets(orderedFacets: List<FacetEntity>) {
        orderedFacets.forEachIndexed { index, facet ->
            facetDao.update(facet.copy(position = index))
        }
    }
}

/** Decoded view of [FacetEntity.selectedCalendarIdsCsv] — `null` means every calendar is implicitly selected. */
val FacetEntity.selectedCalendarIds: Set<String>?
    get() = selectedCalendarIdsCsv?.split(",")?.filter { it.isNotEmpty() }?.toSet()

private fun Set<String>?.toCsv(): String? = this?.joinToString(",")
