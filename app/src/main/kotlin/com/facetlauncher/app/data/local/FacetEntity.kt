package com.facetlauncher.app.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
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

@Entity(tableName = "facets")
data class FacetEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val position: Int,

    // Clock + Calendar design section — one override flag for the whole visual block (see
    // FacetRepository.updateOverridingClock's own doc comment): template, font, color, time
    // format, plus the shared zone height (see chat history — moved in from being global-only
    // settings). The calendar events strip no longer has its own font/color/weight/alignment —
    // it reads Appearance's home-text settings and this block's own clockAlignment instead (see
    // chat history: calendar/appearance styling consolidation).
    val overrideClock: Boolean = false,
    val clockTemplateId: ClockTemplateId = ClockTemplateId.LIGHT_STACK,
    val clockFontOption: ClockFontOption = ClockFontOption.LAUNCHER_DEFAULT,
    val clockColorOption: ClockColorOption = ClockColorOption.THEME,
    /** See [com.facetlauncher.app.data.model.LauncherSettings.clockAccentColorOption]. */
    val clockAccentColorOption: ClockColorOption = ClockColorOption.ACCENT_PRIMARY,
    val use24HourTime: Boolean = false,
    val clockShowMeridiem: Boolean = false,
    /** See [com.facetlauncher.app.data.model.LauncherSettings.clockDateStyle]. */
    val clockDateStyle: ClockDateStyle = ClockDateStyle.FULL,
    /** The clock widget's own horizontal placement — also governs the calendar events strip, which shares it rather than having its own (see chat history: the strip's font/color/weight/alignment were consolidated into Appearance's/clock's shared settings). See [com.facetlauncher.app.data.model.LauncherSettings.clockAlignment]. */
    val clockAlignment: ClockAlignment = ClockAlignment.LEFT,
    /** `null` until this facet drags the clock's grab handle for the first time — see [com.facetlauncher.app.data.model.LauncherSettings.clockZoneHeightDp]. */
    val clockZoneHeightDp: Float? = null,
    /** Home clock's scale factor — see [com.facetlauncher.app.data.model.LauncherSettings.clockScale]. */
    val clockScale: Float = 0.8f,
    /**
     * PRD F15 — a hosted third-party `AppWidget` bound as this facet's clock, replacing the
     * native [clockTemplateId] rendering. Facet-scoped only (no global equivalent — see F15's
     * Scope) — `null` (the default) means this facet uses its own native clock; non-null is this
     * facet's own [android.appwidget.AppWidgetHost] id, unique to it, never shared with another
     * facet or the Hub's own widget ids. Deleting a facet with a non-null id here must release it
     * via `AppWidgetHost.deleteAppWidgetId` first (see `CleanUpUninstalledAppsUseCase`-style
     * cleanup principle) — never just drop the row.
     */
    val clockWidgetAppWidgetId: Int? = null,
    /**
     * PRD F15 — this facet's hosted clock widget's own persisted on-screen size, real dp (not a
     * scale factor — see `ClockBlock`'s own doc for why a hosted widget needs its actual box size,
     * not [clockScale]'s continuous transform). `null` until the user drags a resize handle for
     * the first time, mirroring [clockZoneHeightDp]'s own null-until-touched convention — the
     * effective size before that first drag is the provider's own declared minimum (see
     * `ClockWidgetHostController.defaultSizeDp`). Meaningless (and unset) whenever
     * [clockWidgetAppWidgetId] is null.
     */
    val clockWidgetWidthDp: Int? = null,
    val clockWidgetHeightDp: Int? = null,

    // Apps section
    val overrideApps: Boolean = false,
    /** Look/placement, not content — editable from Settings → Appearance now, resolved independently of [overrideApps] via [resolveSentinel]. See [com.facetlauncher.app.data.model.DockDisplayMode]'s own doc for the sentinel model. */
    val appRowPosition: AppRowPosition = AppRowPosition.LAUNCHER_DEFAULT,
    val appRowPresentation: AppRowPresentation = AppRowPresentation.LAUNCHER_DEFAULT,
    val listContentMode: ListContentMode = ListContentMode.FAVORITES,
    val appsToShowCount: Int = AppListLimits.DEFAULT_APPS_TO_SHOW,
    /** Look/placement, not content — see [appRowPosition]'s own doc for why this is a `LAUNCHER_DEFAULT`-sentinel field rather than [overrideApps]-gated. */
    val appListVerticalAlignment: AppListVerticalAlignment = AppListVerticalAlignment.LAUNCHER_DEFAULT,
    /** Look, not content — see [appRowPosition]'s own doc for the sentinel model. */
    val appListLayout: AppListLayout = AppListLayout.LAUNCHER_DEFAULT,
    /** Look, not content — see [appRowPosition]'s own doc for the sentinel model. */
    val appListColumnAlignment: AppListColumnAlignment = AppListColumnAlignment.LAUNCHER_DEFAULT,
    /** Look, not content — see [appRowPosition]'s own doc for the sentinel model. */
    val appListGridColumns: AppListGridColumns = AppListGridColumns.LAUNCHER_DEFAULT,
    /** Look, not content — see [appRowPosition]'s own doc for the sentinel model. */
    val appListGridDisplayMode: AppListGridDisplayMode = AppListGridDisplayMode.LAUNCHER_DEFAULT,
    /**
     * Independent of [overrideApps] flag for the mode/count, but typically switched 
     * together in the UI (see [com.facetlauncher.app.ui.facets.FacetSettingsViewModel.setOverridingApps]).
     * [false] = use global default favorites; [true] = use this facet's own list.
     */
    val overridingFavorites: Boolean = false,

    // Dock section — one override flag for the whole Home dock: which apps it holds (this
    // facet's own [com.facetlauncher.app.data.local.FacetDockAppEntity] list rather than the
    // launcher-wide default) plus its Icons/Text display style. Mirrors the Apps section's single
    // `overrideApps` flag exactly.
    val overrideDock: Boolean = false,
    /** Look, not content — editable from Settings → Appearance now, resolved independently of [overrideDock] via [resolveSentinel] (that flag still gates only the dock's own app list). See [com.facetlauncher.app.data.model.DockDisplayMode]'s own doc for the sentinel model. */
    val dockDisplayMode: DockDisplayMode = DockDisplayMode.LAUNCHER_DEFAULT,

    // Calendar selection section — content/productivity concerns (which calendars, which kinds
    // of events), deliberately separate from the Clock+Calendar *design* section above (see
    // FacetRepository.updateOverridingCalendar's own doc comment).
    val overrideCalendar: Boolean = false,
    val showAllDayEvents: Boolean = true,
    /**
     * Comma-joined [com.facetlauncher.app.data.model.CalendarInfo.id] values — Room has no
     * native `Set<String>` column type, and ids are always numeric so a comma delimiter is safe
     * (same manual-encoding style [com.facetlauncher.app.data.SettingsRepository.calendarColors]
     * already uses). `null` means every calendar is implicitly selected, mirroring
     * [com.facetlauncher.app.data.model.LauncherSettings.selectedCalendarIds]'s own null
     * semantics exactly. Never read/written directly outside [com.facetlauncher.app.data.FacetRepository] —
     * see its `selectedCalendarIds` extension property for the decoded `Set<String>?` view.
     */
    val selectedCalendarIdsCsv: String? = null,
)

/**
 * The one recurring shape behind every "active facet's X, falling back to the global default
 * when not overriding" property in [com.facetlauncher.app.ui.home.HomeUiState]/
 * [com.facetlauncher.app.ui.facets.FacetCarouselViewModel]/
 * [com.facetlauncher.app.ui.facets.FacetSettingsViewModel] — `this` is the active/looked-up
 * facet (or `null` if there isn't one), [overriding] picks which of that facet's flags governs
 * (`overrideClock`/`overrideApps`/`overrideCalendar`), [facetValue] reads the facet's own
 * stored value, and [globalValue] is what's used both when there's no active facet and when
 * that facet isn't overriding. New for the clock/calendar-alignment and zone-height properties —
 * existing call sites elsewhere still hand-write the equivalent `?.let { if (...) ... else ... }
 * ?: ...` and aren't migrated to this by this change.
 */
inline fun <T> FacetEntity?.resolveOverride(overriding: (FacetEntity) -> Boolean, facetValue: (FacetEntity) -> T, globalValue: T): T =
    this?.let { if (overriding(it)) facetValue(it) else globalValue } ?: globalValue

/**
 * Per-field facet override via a `LAUNCHER_DEFAULT`-style sentinel — no separate override flag;
 * [facetValue] reads this field's own stored value, [sentinel] is its "inherit the global value"
 * marker, [globalValue] is the launcher-wide default. Used for Appearance's dock/app-list "look"
 * fields ([com.facetlauncher.app.data.model.DockDisplayMode]/[com.facetlauncher.app.data.model.AppRowPosition]/
 * [com.facetlauncher.app.data.model.AppRowPresentation]/[com.facetlauncher.app.data.model.AppListVerticalAlignment]) —
 * moved out of the whole-block `overrideDock`/`overrideApps` gate that [resolveOverride] models,
 * since these are edited from Appearance now, independently of Dock's/Apps-list's own content
 * (see chat history).
 */
inline fun <T> FacetEntity?.resolveSentinel(facetValue: (FacetEntity) -> T, sentinel: T, globalValue: T): T {
    val value = this?.let(facetValue) ?: return globalValue
    return if (value == sentinel) globalValue else value
}
