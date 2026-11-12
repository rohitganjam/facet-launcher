package com.facetlauncher.app.domain

import androidx.compose.runtime.Immutable
import com.facetlauncher.app.data.model.GlassStyle
import com.facetlauncher.app.data.model.AppListColumnAlignment
import com.facetlauncher.app.data.model.AppListGridColumns
import com.facetlauncher.app.data.model.AppListGridDisplayMode
import com.facetlauncher.app.data.model.AppListShrinkToFit
import com.facetlauncher.app.data.model.AppListLayout
import com.facetlauncher.app.data.model.AppListVerticalAlignment
import com.facetlauncher.app.data.model.AppRowPosition
import com.facetlauncher.app.data.model.AppRowPresentation
import com.facetlauncher.app.data.model.CalendarDisplayOptions
import com.facetlauncher.app.data.model.ClockAlignment
import com.facetlauncher.app.data.model.ClockWidgetConfig
import com.facetlauncher.app.data.model.ClockColorOption
import com.facetlauncher.app.data.model.ClockDateStyle
import com.facetlauncher.app.data.model.ClockFontOption
import com.facetlauncher.app.data.model.ClockStackItem
import com.facetlauncher.app.data.model.ClockStackItemKind
import com.facetlauncher.app.data.model.NativeWidgetType
import com.facetlauncher.app.data.model.clockStackLimit
import com.facetlauncher.app.data.model.nativeClockPage
import com.facetlauncher.app.data.model.ClockTemplateId
import com.facetlauncher.app.data.model.DockDisplayMode
import com.facetlauncher.app.data.model.DEFAULT_CLOCK_SCALE
import com.facetlauncher.app.data.model.Facet
import com.facetlauncher.app.data.model.LandscapeOverrides
import com.facetlauncher.app.data.model.LauncherSettings
import com.facetlauncher.app.data.model.ListContentMode
import com.facetlauncher.app.data.model.landscape
import com.facetlauncher.app.data.model.resolveLandscape
import com.facetlauncher.app.data.model.WeatherIconStyle
import com.facetlauncher.app.data.model.WidgetWeatherLocation
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList

/** The apps list in force: the facet's own values. */
@Immutable
data class ResolvedAppListStyle(
    val contentMode: ListContentMode,
    val rowPosition: AppRowPosition,
    val rowPresentation: AppRowPresentation,
    val verticalAlignment: AppListVerticalAlignment,
    val layout: AppListLayout,
    val columnAlignment: AppListColumnAlignment,
    val gridColumns: AppListGridColumns,
    val gridDisplayMode: AppListGridDisplayMode,
    val shrinkToFit: AppListShrinkToFit = AppListShrinkToFit.OFF,
)

/** A value for each orientation; [of] picks one. */
@Immutable
data class Oriented<T>(val portrait: T, val landscape: T) {
    fun of(landscape: Boolean): T = if (landscape) this.landscape else portrait
}

/**
 * One page of the facet's clock stack. Facet-scoped only, so it is not gated by `overrideClock`. A `null` size means
 * "not resized yet" and a `null` alignment "use the facet's own clock alignment". The native clock has neither.
 */
@Immutable
data class ResolvedClockItem(
    val id: Long,
    val kind: ClockStackItemKind,
    val appWidgetId: Int?,
    val nativeWidgetType: NativeWidgetType? = null,
    val sizeDp: Oriented<Pair<Int, Int>?>,
    val alignment: Oriented<ClockAlignment?>,
)

/**
 * Everything a facet owns, with the launcher-wide settings only filling in the clock placement fallback: the one place Home,
 * the facet carousel and every settings preview read it from.
 */
@Immutable
data class ResolvedFacetStyle(
    /** The clock's settings in force, its calendar included. */
    val clock: ClockWidgetConfig,
    val appList: ResolvedAppListStyle,
    val dockDisplayMode: DockDisplayMode,
    /** Frosted-glass panels behind the dock and the apps list items; off without a facet. */
    val dockGlass: GlassStyle = GlassStyle.OFF,
    val appsListGlass: GlassStyle = GlassStyle.OFF,
    val calendarDisplay: CalendarDisplayOptions,
    /** The clock's alignment, divider position and scale in each orientation. */
    val clockPlacement: Oriented<ClockPlacement>,
    /** The apps list's row position, vertical anchor and two-column alignment in each orientation. */
    val listAlignment: Oriented<ListAlignment>,
    /** The clock slot's pages in swipe order; empty is the plain native clock. */
    val clockStack: ImmutableList<ResolvedClockItem>,
)

/** [facet] is `null` when there is none, which yields the launcher-wide values. */
fun resolveFacetStyle(facet: Facet?, settings: LauncherSettings): ResolvedFacetStyle = ResolvedFacetStyle(
    clock = effectiveClockConfig(facet, settings),
    appList = resolveAppListStyle(facet, settings),
    dockDisplayMode = facet?.dockDisplayMode ?: settings.dockDisplayMode,
    dockGlass = facet?.dockGlass ?: GlassStyle.OFF,
    appsListGlass = facet?.appsListGlass ?: GlassStyle.OFF,
    calendarDisplay = effectiveClockConfig(facet, settings).calendar.let { CalendarDisplayOptions(it.showIndicator, it.itemsToShow) },
    clockPlacement = resolveClockPlacement(facet, settings),
    listAlignment = resolveListAlignment(facet, settings),
    // No facet (a launcher-wide preview) draws the launcher clock alone.
    clockStack = (facet?.clockStack ?: listOf(nativeClockPage())).map { it.resolve() }.toImmutableList(),
)

/** A facet's own values; with no facet the launcher-wide ones, which are what a new facet is seeded from. */
private fun resolveAppListStyle(facet: Facet?, settings: LauncherSettings) = if (facet != null) {
    ResolvedAppListStyle(
        contentMode = facet.listContentMode,
        rowPosition = facet.appRowPosition,
        rowPresentation = facet.appRowPresentation,
        verticalAlignment = facet.appListVerticalAlignment,
        layout = facet.appListLayout,
        columnAlignment = facet.appListColumnAlignment,
        gridColumns = facet.appListGridColumns,
        gridDisplayMode = facet.appListGridDisplayMode,
        shrinkToFit = facet.appListShrinkToFit,
    )
} else {
    ResolvedAppListStyle(
        contentMode = settings.listContentMode,
        rowPosition = settings.appRowPosition,
        rowPresentation = settings.appRowPresentation,
        verticalAlignment = settings.appListVerticalAlignment,
        layout = settings.appListLayout,
        columnAlignment = settings.appListColumnAlignment,
        gridColumns = settings.appListGridColumns,
        gridDisplayMode = settings.appListGridDisplayMode,
        shrinkToFit = settings.appListShrinkToFit,
    )
}

/** Portrait follows the facet while it overrides the clock; landscape follows [resolveLandscape] over that. */
private fun resolveClockPlacement(facet: Facet?, settings: LauncherSettings): Oriented<ClockPlacement> {
    val owner = facet?.takeIf { it.clockPlacementOwn }
    val global = settings.landscape
    fun <T> prop(portrait: (Facet) -> T, landscape: (LandscapeOverrides) -> T?, globalPortrait: T, globalLandscape: T?): Oriented<T> {
        val own = if (owner != null) portrait(owner) else globalPortrait
        return Oriented(own, resolveLandscape(owner?.landscape?.let(landscape), own, owner != null, globalLandscape, globalPortrait))
    }
    val alignment = prop({ it.clockAlignment }, { it.clockAlignment }, settings.clockAlignment, global.clockAlignment)
    val zoneHeight = prop({ it.clockZoneHeightDp }, { it.clockZoneHeightDp }, settings.clockZoneHeightDp, global.clockZoneHeightDp)
    return Oriented(
        ClockPlacement(alignment.portrait, zoneHeight.portrait, DEFAULT_CLOCK_SCALE),
        ClockPlacement(alignment.landscape, zoneHeight.landscape, DEFAULT_CLOCK_SCALE),
    )
}

/** Portrait is the facet's own value (the launcher-wide one with no facet); landscape is its own twin, else portrait. */
private fun resolveListAlignment(facet: Facet?, settings: LauncherSettings): Oriented<ListAlignment> {
    val portrait = if (facet != null) {
        ListAlignment(facet.appRowPosition, facet.appListVerticalAlignment, facet.appListColumnAlignment)
    } else {
        ListAlignment(settings.appRowPosition, settings.appListVerticalAlignment, settings.appListColumnAlignment)
    }
    val twins = facet?.landscape ?: settings.landscape
    return Oriented(
        portrait,
        ListAlignment(
            rowPosition = twins.appRowPosition ?: portrait.rowPosition,
            verticalAlignment = twins.appListVerticalAlignment ?: portrait.verticalAlignment,
            columnAlignment = twins.appListColumnAlignment ?: portrait.columnAlignment,
        ),
    )
}

/** Without Pro only the first [FREE_CLOCK_STACK_ITEMS] pages show: the rest stay saved and come back with Pro. */
fun ImmutableList<ResolvedClockItem>.limitedTo(isPro: Boolean): ImmutableList<ResolvedClockItem> =
    if (isPro) this else take(clockStackLimit(isPro)).toImmutableList()

/** Landscape's own size and alignment where saved (size per dimension), else the portrait ones. */
private fun ClockStackItem.resolve(): ResolvedClockItem {
    fun size(width: Int?, height: Int?) = if (width != null && height != null) width to height else null
    return ResolvedClockItem(
        id = id,
        kind = kind,
        appWidgetId = appWidgetId,
        nativeWidgetType = nativeWidgetType,
        sizeDp = Oriented(
            portrait = size(widthDp, heightDp),
            landscape = size(landscapeWidthDp ?: widthDp, landscapeHeightDp ?: heightDp),
        ),
        alignment = Oriented(portrait = alignment, landscape = landscapeAlignment ?: alignment),
    )
}
