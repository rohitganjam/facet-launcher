package com.facetlauncher.app.domain

import com.facetlauncher.app.data.local.FacetEntity
import com.facetlauncher.app.data.local.effectiveCalendarDisplay
import com.facetlauncher.app.data.local.resolveOverride
import com.facetlauncher.app.data.local.resolveSentinel
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
import com.facetlauncher.app.data.model.LauncherSettings
import com.facetlauncher.app.data.model.ListContentMode

/** The clock block in force: the facet's own while it overrides the clock, the launcher's otherwise. Portrait values. */
data class ResolvedClockStyle(
    val templateId: ClockTemplateId,
    val fontOption: ClockFontOption,
    val colorOption: ClockColorOption,
    val accentColorOption: ClockColorOption,
    val use24HourTime: Boolean,
    val showMeridiem: Boolean,
    val dateStyle: ClockDateStyle,
    val alignment: ClockAlignment,
    val zoneHeightDp: Float?,
    val scale: Float,
)

/** The apps list in force. The look fields inherit per field via their `LAUNCHER_DEFAULT` sentinel; [contentMode] follows `overrideApps`. */
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

/** Everything a facet can override, resolved against the launcher-wide settings. */
data class ResolvedFacetStyle(
    val clock: ResolvedClockStyle,
    val appList: ResolvedAppListStyle,
    val dockDisplayMode: DockDisplayMode,
    val calendarDisplay: CalendarDisplayOptions,
)

/** [facet] is `null` when there is none, which yields the launcher-wide values. */
fun resolveFacetStyle(facet: FacetEntity?, settings: LauncherSettings): ResolvedFacetStyle = ResolvedFacetStyle(
    clock = ResolvedClockStyle(
        templateId = facet.resolveOverride({ it.overrideClock }, { it.clockTemplateId }, settings.clockTemplateId),
        fontOption = facet.resolveOverride({ it.overrideClock }, { it.clockFontOption }, settings.clockFontOption),
        colorOption = facet.resolveOverride({ it.overrideClock }, { it.clockColorOption }, settings.clockColorOption),
        accentColorOption = facet.resolveOverride({ it.overrideClock }, { it.clockAccentColorOption }, settings.clockAccentColorOption),
        use24HourTime = facet.resolveOverride({ it.overrideClock }, { it.use24HourTime }, settings.use24HourTime),
        showMeridiem = facet.resolveOverride({ it.overrideClock }, { it.clockShowMeridiem }, settings.clockShowMeridiem),
        dateStyle = facet.resolveOverride({ it.overrideClock }, { it.clockDateStyle }, settings.clockDateStyle),
        alignment = facet.resolveOverride({ it.overrideClock }, { it.clockAlignment }, settings.clockAlignment),
        zoneHeightDp = facet.resolveOverride({ it.overrideClock }, { it.clockZoneHeightDp }, settings.clockZoneHeightDp),
        scale = facet.resolveOverride({ it.overrideClock }, { it.clockScale }, settings.clockScale),
    ),
    appList = ResolvedAppListStyle(
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
    ),
    dockDisplayMode = facet.resolveSentinel({ it.dockDisplayMode }, DockDisplayMode.LAUNCHER_DEFAULT, settings.dockDisplayMode),
    calendarDisplay = facet.effectiveCalendarDisplay(settings),
)
