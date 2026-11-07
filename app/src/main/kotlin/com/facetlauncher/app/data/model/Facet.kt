package com.facetlauncher.app.data.model

/** The scale a clock without a saved box draws at; the saved size is the box on its page. */
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

    /**
     * Whether the placement fields below ([clockAlignment], [clockZoneHeightDp] and their landscape twins)
     * are this facet's own. `false` until the user first adjusts it on Home: the facet then shows the launcher's built-in
     * fallback placement. Placement is never inherited by choice, so the clock override does not touch it.
     */
    val clockPlacementOwn: Boolean = false,
    val clockAlignment: ClockAlignment = ClockAlignment.LEFT,
    /** `null` until the facet drags the clock's grab handle for the first time. */
    val clockZoneHeightDp: Float? = null,
    /**
     * What the clock slot shows, in swipe order: the native clock and hosted widgets, each with its own size and
     * alignment. Empty draws nothing there. Held in its own table and loaded by `FacetRepository`; a facet built by
     * hand (a preview, a test) starts with the native clock, as a new facet does.
     */
    val clockStack: List<ClockStackItem> = listOf(nativeClockPage()),
    /** Landscape's own clock alignment and zone height; `null` uses the portrait value. */
    val landscapeClockAlignment: ClockAlignment? = null,
    val landscapeClockZoneHeightDp: Float? = null,

    // Apps section: the facet's own values, seeded from the launcher defaults when it is created (see ApplyFacetDefaultsUseCase).
    val appRowPosition: AppRowPosition = AppRowPosition.LEFT,
    /** Landscape's own [appRowPosition], [appListVerticalAlignment] and [appListColumnAlignment]; `null` is "same as portrait". */
    val landscapeAppRowPosition: AppRowPosition? = null,
    val landscapeAppListVerticalAlignment: AppListVerticalAlignment? = null,
    val landscapeAppListColumnAlignment: AppListColumnAlignment? = null,
    val appRowPresentation: AppRowPresentation = AppRowPresentation.ICON_AND_TEXT,
    val listContentMode: ListContentMode = ListContentMode.FAVORITES,
    val appsToShowCount: Int = AppListLimits.DEFAULT_APPS_TO_SHOW,
    val appListVerticalAlignment: AppListVerticalAlignment = AppListVerticalAlignment.BOTTOM,
    val appListLayout: AppListLayout = AppListLayout.SINGLE_COLUMN,
    val appListColumnAlignment: AppListColumnAlignment = AppListColumnAlignment.BOTH_LEFT,
    val appListGridColumns: AppListGridColumns = AppListGridColumns.FOUR,
    val appListGridDisplayMode: AppListGridDisplayMode = AppListGridDisplayMode.ICONS,
    val appListShrinkToFit: AppListShrinkToFit = AppListShrinkToFit.OFF,

    // Dock section
    val dockDisplayMode: DockDisplayMode = DockDisplayMode.ICONS,
)

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
        appRowPosition = landscapeAppRowPosition,
        appListVerticalAlignment = landscapeAppListVerticalAlignment,
        appListColumnAlignment = landscapeAppListColumnAlignment,
    )

/** This facet with [overrides] as its landscape twins. */
fun Facet.withLandscape(overrides: LandscapeOverrides): Facet = copy(
    landscapeClockAlignment = overrides.clockAlignment,
    landscapeClockZoneHeightDp = overrides.clockZoneHeightDp,
    landscapeAppRowPosition = overrides.appRowPosition,
    landscapeAppListVerticalAlignment = overrides.appListVerticalAlignment,
    landscapeAppListColumnAlignment = overrides.appListColumnAlignment,
)

/** Where [list] lives for `this` facet: its own rows (the launcher-wide list when there is no active facet). */
fun Facet?.placementTarget(list: PlacementList): PlacementTarget = PlacementTarget(list, this?.id)
