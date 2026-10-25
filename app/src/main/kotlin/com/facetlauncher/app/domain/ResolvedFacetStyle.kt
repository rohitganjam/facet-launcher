package com.facetlauncher.app.domain

import androidx.compose.runtime.Immutable
import com.facetlauncher.app.data.model.AppListColumnAlignment
import com.facetlauncher.app.data.model.AppListGridColumns
import com.facetlauncher.app.data.model.AppListGridDisplayMode
import com.facetlauncher.app.data.model.AppListLayout
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
import com.facetlauncher.app.data.model.LauncherSettings
import com.facetlauncher.app.data.model.ListContentMode
import com.facetlauncher.app.data.model.effectiveCalendarDisplay
import com.facetlauncher.app.data.model.landscape
import com.facetlauncher.app.data.model.resolveLandscape
import com.facetlauncher.app.data.model.WeatherIconStyle
import com.facetlauncher.app.data.model.resolveOverride
import com.facetlauncher.app.data.model.resolveSentinel

/** The clock block in force: the facet's own while it overrides the clock, the launcher's otherwise. Portrait values. */
@Immutable
data class ResolvedClockStyle(
    val templateId: ClockTemplateId,
    val fontOption: ClockFontOption,
    val colorOption: ClockColorOption,
    val accentColorOption: ClockColorOption,
    val use24HourTime: Boolean,
    val showMeridiem: Boolean,
    val dateStyle: ClockDateStyle,
    val showAlarm: Boolean,
    val showBattery: Boolean,
    val showWeather: Boolean,
    val weatherIconStyle: WeatherIconStyle,
    val alignment: ClockAlignment,
    val zoneHeightDp: Float?,
    val scale: Float,
)

/** The apps list in force. The look fields inherit per field via their `LAUNCHER_DEFAULT` sentinel; [contentMode] follows `overrideApps`. */
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
)

/** A value for each orientation; [of] picks one. */
@Immutable
data class Oriented<T>(val portrait: T, val landscape: T) {
    fun of(landscape: Boolean): T = if (landscape) this.landscape else portrait
}

/** The facet's hosted third-party clock widget. Facet-scoped only, so it is not gated by `overrideClock`. A `null` size means "not resized yet". */
@Immutable
data class ResolvedClockWidget(val appWidgetId: Int, val sizeDp: Oriented<Pair<Int, Int>?>)

/**
 * Everything a facet can override, resolved against the launcher-wide settings: the one place Home,
 * the facet carousel and every settings preview read it from.
 */
@Immutable
data class ResolvedFacetStyle(
    val clock: ResolvedClockStyle,
    val appList: ResolvedAppListStyle,
    val dockDisplayMode: DockDisplayMode,
    val calendarDisplay: CalendarDisplayOptions,
    /** The clock's alignment, divider position and scale in each orientation. */
    val clockPlacement: Oriented<ClockPlacement>,
    /** The apps list's row position, vertical anchor and two-column alignment in each orientation. */
    val listAlignment: Oriented<ListAlignment>,
    val clockWidget: ResolvedClockWidget?,
)

/** [facet] is `null` when there is none, which yields the launcher-wide values. */
fun resolveFacetStyle(facet: Facet?, settings: LauncherSettings): ResolvedFacetStyle = ResolvedFacetStyle(
    clock = resolveClockStyle(facet, settings),
    appList = resolveAppListStyle(facet, settings),
    dockDisplayMode = facet.resolveSentinel({ it.dockDisplayMode }, DockDisplayMode.LAUNCHER_DEFAULT, settings.dockDisplayMode),
    calendarDisplay = facet.effectiveCalendarDisplay(settings),
    clockPlacement = resolveClockPlacement(facet, settings),
    listAlignment = resolveListAlignment(facet, settings),
    clockWidget = facet?.clockWidgetAppWidgetId?.let { ResolvedClockWidget(it, resolveClockWidgetSize(facet)) },
)

private fun resolveClockStyle(facet: Facet?, settings: LauncherSettings) = ResolvedClockStyle(
    templateId = facet.resolveOverride({ it.overrideClock }, { it.clockTemplateId }, settings.clockTemplateId),
    fontOption = facet.resolveOverride({ it.overrideClock }, { it.clockFontOption }, settings.clockFontOption),
    colorOption = facet.resolveOverride({ it.overrideClock }, { it.clockColorOption }, settings.clockColorOption),
    accentColorOption = facet.resolveOverride({ it.overrideClock }, { it.clockAccentColorOption }, settings.clockAccentColorOption),
    use24HourTime = facet.resolveOverride({ it.overrideClock }, { it.use24HourTime }, settings.use24HourTime),
    showMeridiem = facet.resolveOverride({ it.overrideClock }, { it.clockShowMeridiem }, settings.clockShowMeridiem),
    dateStyle = facet.resolveOverride({ it.overrideClock }, { it.clockDateStyle }, settings.clockDateStyle),
    showAlarm = facet.resolveOverride({ it.overrideClock }, { it.clockShowAlarm }, settings.showNextAlarm),
    showBattery = facet.resolveOverride({ it.overrideClock }, { it.clockShowBattery }, settings.showBatteryPercentage),
    showWeather = facet.resolveOverride({ it.overrideClock }, { it.clockShowWeather }, settings.showWeather),
    weatherIconStyle = facet.resolveOverride({ it.overrideClock }, { it.clockWeatherIconStyle }, settings.weatherIconStyle),
    alignment = facet.resolveOverride({ it.overrideClock }, { it.clockAlignment }, settings.clockAlignment),
    zoneHeightDp = facet.resolveOverride({ it.overrideClock }, { it.clockZoneHeightDp }, settings.clockZoneHeightDp),
    scale = facet.resolveOverride({ it.overrideClock }, { it.clockScale }, settings.clockScale),
)

private fun resolveAppListStyle(facet: Facet?, settings: LauncherSettings) = ResolvedAppListStyle(
    contentMode = facet.resolveOverride({ it.overrideApps }, { it.listContentMode }, settings.listContentMode),
    rowPosition = facet.resolveSentinel({ it.appRowPosition }, AppRowPosition.LAUNCHER_DEFAULT, settings.appRowPosition),
    rowPresentation = facet.resolveSentinel({ it.appRowPresentation }, AppRowPresentation.LAUNCHER_DEFAULT, settings.appRowPresentation),
    verticalAlignment = facet.resolveSentinel(
        { it.appListVerticalAlignment }, AppListVerticalAlignment.LAUNCHER_DEFAULT, settings.appListVerticalAlignment,
    ),
    layout = facet.resolveSentinel({ it.appListLayout }, AppListLayout.LAUNCHER_DEFAULT, settings.appListLayout),
    columnAlignment = facet.resolveSentinel(
        { it.appListColumnAlignment }, AppListColumnAlignment.LAUNCHER_DEFAULT, settings.appListColumnAlignment,
    ),
    gridColumns = facet.resolveSentinel({ it.appListGridColumns }, AppListGridColumns.LAUNCHER_DEFAULT, settings.appListGridColumns),
    gridDisplayMode = facet.resolveSentinel(
        { it.appListGridDisplayMode }, AppListGridDisplayMode.LAUNCHER_DEFAULT, settings.appListGridDisplayMode,
    ),
)

/** Portrait follows the facet while it overrides the clock; landscape follows [resolveLandscape] over that. */
private fun resolveClockPlacement(facet: Facet?, settings: LauncherSettings): Oriented<ClockPlacement> {
    val owner = facet?.takeIf { it.overrideClock }
    val global = settings.landscape
    fun <T> prop(portrait: (Facet) -> T, landscape: (LandscapeOverrides) -> T?, globalPortrait: T, globalLandscape: T?): Oriented<T> {
        val own = if (owner != null) portrait(owner) else globalPortrait
        return Oriented(own, resolveLandscape(owner?.landscape?.let(landscape), own, owner != null, globalLandscape, globalPortrait))
    }
    val alignment = prop({ it.clockAlignment }, { it.clockAlignment }, settings.clockAlignment, global.clockAlignment)
    val zoneHeight = prop({ it.clockZoneHeightDp }, { it.clockZoneHeightDp }, settings.clockZoneHeightDp, global.clockZoneHeightDp)
    val scale = prop({ it.clockScale }, { it.clockScale }, settings.clockScale, global.clockScale)
    return Oriented(
        ClockPlacement(alignment.portrait, zoneHeight.portrait, scale.portrait),
        ClockPlacement(alignment.landscape, zoneHeight.landscape, scale.landscape),
    )
}

/** Each field goes through its own `LAUNCHER_DEFAULT` sentinel, then [resolveLandscape] for landscape. */
private fun resolveListAlignment(facet: Facet?, settings: LauncherSettings): Oriented<ListAlignment> {
    val global = settings.landscape
    fun <T : Any> prop(
        portrait: (Facet) -> T,
        landscape: (LandscapeOverrides) -> T?,
        sentinel: T,
        globalPortrait: T,
        globalLandscape: T?,
    ): Oriented<T> {
        val facetValue = facet?.let(portrait)?.takeIf { it != sentinel }
        val own = facetValue ?: globalPortrait
        return Oriented(own, resolveLandscape(facet?.landscape?.let(landscape), own, facetValue != null, globalLandscape, globalPortrait))
    }
    val row = prop(
        { it.appRowPosition }, { it.appRowPosition }, AppRowPosition.LAUNCHER_DEFAULT, settings.appRowPosition, global.appRowPosition,
    )
    val vertical = prop(
        { it.appListVerticalAlignment }, { it.appListVerticalAlignment }, AppListVerticalAlignment.LAUNCHER_DEFAULT,
        settings.appListVerticalAlignment, global.appListVerticalAlignment,
    )
    val column = prop(
        { it.appListColumnAlignment }, { it.appListColumnAlignment }, AppListColumnAlignment.LAUNCHER_DEFAULT,
        settings.appListColumnAlignment, global.appListColumnAlignment,
    )
    return Oriented(
        ListAlignment(row.portrait, vertical.portrait, column.portrait),
        ListAlignment(row.landscape, vertical.landscape, column.landscape),
    )
}

/** Landscape's own size where saved (per dimension), else the portrait one; `null` before any resize. */
private fun resolveClockWidgetSize(facet: Facet): Oriented<Pair<Int, Int>?> {
    fun size(width: Int?, height: Int?) = if (width != null && height != null) width to height else null
    val landscape = facet.landscape
    return Oriented(
        portrait = size(facet.clockWidgetWidthDp, facet.clockWidgetHeightDp),
        landscape = size(landscape.clockWidgetWidthDp ?: facet.clockWidgetWidthDp, landscape.clockWidgetHeightDp ?: facet.clockWidgetHeightDp),
    )
}
