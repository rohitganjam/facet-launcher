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
import com.facetlauncher.app.data.model.CalendarDisplayOptions
import com.facetlauncher.app.data.model.ClockAlignment
import com.facetlauncher.app.data.model.ClockColorOption
import com.facetlauncher.app.data.model.ClockDateStyle
import com.facetlauncher.app.data.model.ClockFontOption
import com.facetlauncher.app.data.model.ClockTemplateId
import com.facetlauncher.app.data.model.DockDisplayMode
import com.facetlauncher.app.data.model.ListContentMode
import com.facetlauncher.app.data.model.WeatherIconStyle

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
    /** See [com.facetlauncher.app.data.model.LauncherSettings.showNextAlarm]. */
    val clockShowAlarm: Boolean = true,
    val clockShowBattery: Boolean = true,
    val clockShowWeather: Boolean = false,
    /** The weather picture style this facet uses while it overrides the clock block; see `LauncherSettings.weatherIconStyle`. */
    val clockWeatherIconStyle: WeatherIconStyle = WeatherIconStyle.COLORED,
    /** The clock widget's own horizontal placement — also governs the calendar events strip, which shares it rather than having its own (see chat history: the strip's font/color/weight/alignment were consolidated into Appearance's/clock's shared settings). See [com.facetlauncher.app.data.model.LauncherSettings.clockAlignment]. */
    val clockPlacementOwn: Boolean = false,
    val clockAlignment: ClockAlignment = ClockAlignment.LEFT,
    /** `null` until this facet drags the clock's grab handle for the first time — see [com.facetlauncher.app.data.model.LauncherSettings.clockZoneHeightDp]. */
    val clockZoneHeightDp: Float? = null,
    /** Home clock's scale factor — see [com.facetlauncher.app.data.model.LauncherSettings.clockScale]. */
    val clockScale: Float = 0.8f,
    /**
     * Retired: a facet's hosted clock widget now lives in `clock_stack_items` (see `ClockStackItemEntity`), which
     * migration 34→35 filled from these columns. They are no longer read or written, and stay because SQLite on
     * minSdk 30 cannot drop a column.
     */
    val clockWidgetAppWidgetId: Int? = null,
    val clockWidgetWidthDp: Int? = null,
    val clockWidgetHeightDp: Int? = null,
    val landscapeClockWidgetWidthDp: Int? = null,
    val landscapeClockWidgetHeightDp: Int? = null,
    /** Landscape's own clock alignment (top, middle or bottom there), zone height (its divider position) and scale; `null` uses the portrait value above. */
    val landscapeClockAlignment: ClockAlignment? = null,
    val landscapeClockZoneHeightDp: Float? = null,
    val landscapeClockScale: Float? = null,

    // Apps section
    val overrideApps: Boolean = false,
    /** Look/placement, not content — editable from Settings → Appearance now, resolved independently of [overrideApps] via [resolveSentinel]. See [com.facetlauncher.app.data.model.DockDisplayMode]'s own doc for the sentinel model. */
    val appRowPosition: AppRowPosition = AppRowPosition.LAUNCHER_DEFAULT,
    /** Landscape's own value for [appRowPosition], [appListVerticalAlignment] and [appListColumnAlignment]; `null` is "same as portrait". */
    val landscapeAppRowPosition: AppRowPosition? = null,
    val landscapeAppListVerticalAlignment: AppListVerticalAlignment? = null,
    val landscapeAppListColumnAlignment: AppListColumnAlignment? = null,
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
    val showCalendarIndicator: Boolean = true,
    val calendarItemsToShow: Int = CalendarDisplayOptions.MAX_ITEMS,
    /**
     * Comma-joined [com.facetlauncher.app.data.model.CalendarInfo.id] values — Room has no
     * native `Set<String>` column type, and ids are always numeric so a comma delimiter is safe
     * (same manual-encoding style [com.facetlauncher.app.data.SettingsRepository.calendarColors]
     * already uses). `null` means every calendar is implicitly selected, mirroring
     * [com.facetlauncher.app.data.model.LauncherSettings.selectedCalendarIds]'s own null
     * semantics exactly. Never read/written directly outside [com.facetlauncher.app.data.FacetRepository] —
     * see [toFacet] for the decoded `Set<String>?` view.
     */
    val selectedCalendarIdsCsv: String? = null,
)
