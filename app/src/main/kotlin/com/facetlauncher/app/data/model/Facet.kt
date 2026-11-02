package com.facetlauncher.app.data.model

/** The clock scale a facet starts with, and that "Reset clock position" returns to. */
const val DEFAULT_CLOCK_SCALE = 0.8f

/**
 * One facet as the rest of the app sees it. `FacetRepository` maps it to and from the Room row, so a
 * schema change stops here. Landscape twins are flat `landscape*` fields; read them as a group
 * through [landscape].
 */
data class Facet(
    val id: Long = 0,
    val name: String,
    val position: Int,

    // Clock + Calendar design block — one override flag for the whole visual unit.
    val overrideClock: Boolean = false,
    val clockTemplateId: ClockTemplateId = ClockTemplateId.LIGHT_STACK,
    val clockFontOption: ClockFontOption = ClockFontOption.LAUNCHER_DEFAULT,
    val clockColorOption: ClockColorOption = ClockColorOption.THEME,
    val clockAccentColorOption: ClockColorOption = ClockColorOption.ACCENT_PRIMARY,
    val use24HourTime: Boolean = false,
    val clockShowMeridiem: Boolean = false,
    val clockDateStyle: ClockDateStyle = ClockDateStyle.FULL,
    /** The clock accessories this facet shows while it overrides the clock block; see `LauncherSettings.showNextAlarm`. */
    val clockShowAlarm: Boolean = true,
    val clockShowBattery: Boolean = true,
    val clockShowWeather: Boolean = false,
    /** The weather picture style this facet uses while it overrides the clock block; see `LauncherSettings.weatherIconStyle`. */
    val clockWeatherIconStyle: WeatherIconStyle = WeatherIconStyle.COLORED,
    /**
     * Whether the placement fields below ([clockAlignment], [clockZoneHeightDp], [clockScale] and their landscape twins)
     * are this facet's own. `false` until the user first adjusts it on Home: the facet then shows the launcher's built-in
     * fallback placement. Placement is never inherited by choice, so the clock override does not touch it.
     */
    val clockPlacementOwn: Boolean = false,
    val clockAlignment: ClockAlignment = ClockAlignment.LEFT,
    /** `null` until the facet drags the clock's grab handle for the first time. */
    val clockZoneHeightDp: Float? = null,
    val clockScale: Float = DEFAULT_CLOCK_SCALE,
    /**
     * What the clock slot shows, in swipe order: the native clock and hosted widgets, each with its own size and
     * alignment. Empty draws nothing there. Held in its own table and loaded by `FacetRepository`; a facet built by
     * hand (a preview, a test) starts with the native clock, as a new facet does.
     */
    val clockStack: List<ClockStackItem> = listOf(nativeClockPage()),
    /** Landscape's own clock alignment, zone height and scale; `null` uses the portrait value. */
    val landscapeClockAlignment: ClockAlignment? = null,
    val landscapeClockZoneHeightDp: Float? = null,
    val landscapeClockScale: Float? = null,

    // Apps section
    val overrideApps: Boolean = false,
    /** The look fields inherit per field through their `LAUNCHER_DEFAULT` sentinel, independent of [overrideApps]. */
    val appRowPosition: AppRowPosition = AppRowPosition.LAUNCHER_DEFAULT,
    /** Landscape's own [appRowPosition], [appListVerticalAlignment] and [appListColumnAlignment]; `null` is "same as portrait". */
    val landscapeAppRowPosition: AppRowPosition? = null,
    val landscapeAppListVerticalAlignment: AppListVerticalAlignment? = null,
    val landscapeAppListColumnAlignment: AppListColumnAlignment? = null,
    val appRowPresentation: AppRowPresentation = AppRowPresentation.LAUNCHER_DEFAULT,
    val listContentMode: ListContentMode = ListContentMode.FAVORITES,
    val appsToShowCount: Int = AppListLimits.DEFAULT_APPS_TO_SHOW,
    val appListVerticalAlignment: AppListVerticalAlignment = AppListVerticalAlignment.LAUNCHER_DEFAULT,
    val appListLayout: AppListLayout = AppListLayout.LAUNCHER_DEFAULT,
    val appListColumnAlignment: AppListColumnAlignment = AppListColumnAlignment.LAUNCHER_DEFAULT,
    val appListGridColumns: AppListGridColumns = AppListGridColumns.LAUNCHER_DEFAULT,
    val appListGridDisplayMode: AppListGridDisplayMode = AppListGridDisplayMode.LAUNCHER_DEFAULT,
    val appListShrinkToFit: AppListShrinkToFit = AppListShrinkToFit.LAUNCHER_DEFAULT,
    /** `true`: the facet has its own Favorites list; `false`: it uses the launcher-wide one. */
    val overridingFavorites: Boolean = false,

    // Dock section
    val overrideDock: Boolean = false,
    val dockDisplayMode: DockDisplayMode = DockDisplayMode.LAUNCHER_DEFAULT,

    // Calendar selection section
    val overrideCalendar: Boolean = false,
    val showAllDayEvents: Boolean = true,
    val showCalendarIndicator: Boolean = true,
    val calendarItemsToShow: Int = CalendarDisplayOptions.MAX_ITEMS,
    /** `null` means every calendar is implicitly selected. */
    val selectedCalendarIds: Set<String>? = null,
)

/**
 * The recurring shape behind every "this facet's X, falling back to the launcher default when it
 * isn't overriding" property: [overriding] picks which flag governs, [facetValue] reads the facet's
 * stored value, [globalValue] is used when there is no facet or it isn't overriding.
 */
inline fun <T> Facet?.resolveOverride(overriding: (Facet) -> Boolean, facetValue: (Facet) -> T, globalValue: T): T =
    if (this != null && overriding(this)) facetValue(this) else globalValue

/**
 * Per-field override via a `LAUNCHER_DEFAULT`-style sentinel, with no separate override flag:
 * [facetValue] is the stored value, [sentinel] its "inherit" marker, [globalValue] the launcher default.
 */
inline fun <T> Facet?.resolveSentinel(facetValue: (Facet) -> T, sentinel: T, globalValue: T): T {
    val value = this?.let(facetValue) ?: return globalValue
    return if (value == sentinel) globalValue else value
}

/**
 * Landscape resolution, one rule for every prop: the facet's landscape value (callers pass `null` when the
 * facet doesn't count, e.g. a clock override that is off), else the facet's portrait value if it applies,
 * else the launcher's landscape value, else its portrait value.
 */
inline fun <T> resolveLandscape(
    facetLandscape: T?,
    facetPortrait: T,
    facetPortraitApplies: Boolean,
    globalLandscape: T?,
    globalPortrait: T,
): T = facetLandscape ?: if (facetPortraitApplies) facetPortrait else (globalLandscape ?: globalPortrait)

/** The facet's landscape twins as one group. */
val Facet.landscape: LandscapeOverrides
    get() = LandscapeOverrides(
        clockAlignment = landscapeClockAlignment,
        clockZoneHeightDp = landscapeClockZoneHeightDp,
        clockScale = landscapeClockScale,
        appRowPosition = landscapeAppRowPosition,
        appListVerticalAlignment = landscapeAppListVerticalAlignment,
        appListColumnAlignment = landscapeAppListColumnAlignment,
    )

/** This facet with [overrides] as its landscape twins. */
fun Facet.withLandscape(overrides: LandscapeOverrides): Facet = copy(
    landscapeClockAlignment = overrides.clockAlignment,
    landscapeClockZoneHeightDp = overrides.clockZoneHeightDp,
    landscapeClockScale = overrides.clockScale,
    landscapeAppRowPosition = overrides.appRowPosition,
    landscapeAppListVerticalAlignment = overrides.appListVerticalAlignment,
    landscapeAppListColumnAlignment = overrides.appListColumnAlignment,
)

/** The calendar display options in force for `this` facet (or the launcher-wide ones when it's `null` or not overriding the calendar). */
fun Facet?.effectiveCalendarDisplay(settings: LauncherSettings): CalendarDisplayOptions =
    if (this?.overrideCalendar == true) {
        CalendarDisplayOptions(showCalendarIndicator, calendarItemsToShow)
    } else {
        CalendarDisplayOptions(settings.showCalendarIndicator, settings.calendarItemsToShow)
    }

/** Where [list] lives for `this` facet: its own rows when it overrides that list, else the launcher-wide default (also the answer when there is no active facet). */
fun Facet?.placementTarget(list: PlacementList): PlacementTarget = when {
    this == null -> PlacementTarget(list)
    list == PlacementList.DOCK && overrideDock -> PlacementTarget(list, id)
    list == PlacementList.FAVORITES && overridingFavorites -> PlacementTarget(list, id)
    else -> PlacementTarget(list)
}
