package com.facetlauncher.app.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
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
import com.facetlauncher.app.data.model.FontWeightOption
import com.facetlauncher.app.data.model.ListContentMode

@Entity(tableName = "facets")
data class FacetEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val position: Int,

    // Clock + Calendar design section — one override flag for the whole visual block (see
    // FacetRepository.updateOverridingClock's own doc comment): template, font, color, time
    // format, plus the calendar events strip's own font/color (calendarFontOption/calendarColorOption
    // moved in from what used to be a separate Calendar-design override — see chat history), plus
    // each block's own horizontal alignment and the shared zone height (see chat history — moved
    // in from being global-only settings).
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
    val calendarFontOption: ClockFontOption = ClockFontOption.LAUNCHER_DEFAULT,
    val calendarColorOption: ClockColorOption = ClockColorOption.THEME,
    val calendarFontWeight: FontWeightOption = FontWeightOption.REGULAR,
    /** The clock widget's own horizontal placement, independent of [calendarAlignment] — see [com.facetlauncher.app.data.model.LauncherSettings.clockAlignment]. */
    val clockAlignment: ClockAlignment = ClockAlignment.LEFT,
    /** The calendar strip's own horizontal placement, independent of [clockAlignment] — see [com.facetlauncher.app.data.model.LauncherSettings.calendarAlignment]. */
    val calendarAlignment: ClockAlignment = ClockAlignment.LEFT,
    /** `null` until this facet drags the clock's grab handle for the first time — see [com.facetlauncher.app.data.model.LauncherSettings.clockZoneHeightDp]. */
    val clockZoneHeightDp: Float? = null,
    /** Home clock's scale factor — see [com.facetlauncher.app.data.model.LauncherSettings.clockScale]. */
    val clockScale: Float = 0.8f,

    // Apps section
    val overrideApps: Boolean = false,
    val appRowPosition: AppRowPosition = AppRowPosition.LEFT,
    val appRowPresentation: AppRowPresentation = AppRowPresentation.ICON_AND_TEXT,
    val listContentMode: ListContentMode = ListContentMode.FAVORITES,
    val appsToShowCount: Int = AppListLimits.DEFAULT_APPS_TO_SHOW,
    /** Whether this facet's app list anchors to the bottom (above the dock) or the top (below the clock's grab handle) — see [com.facetlauncher.app.data.model.LauncherSettings.appListVerticalAlignment]. */
    val appListVerticalAlignment: AppListVerticalAlignment = AppListVerticalAlignment.BOTTOM,
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
    val dockDisplayMode: DockDisplayMode = DockDisplayMode.ICONS,

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
