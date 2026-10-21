package com.facetlauncher.app.ui.home

import com.facetlauncher.app.data.model.AppListColumnAlignment
import com.facetlauncher.app.data.model.AppListGridColumns
import com.facetlauncher.app.data.model.AppListGridDisplayMode
import com.facetlauncher.app.data.model.AppListLayout
import com.facetlauncher.app.data.model.AppListVerticalAlignment
import com.facetlauncher.app.data.model.AppRowPosition
import com.facetlauncher.app.data.model.AppRowPresentation
import com.facetlauncher.app.data.model.CalendarDisplayOptions
import com.facetlauncher.app.data.model.CalendarEvent
import com.facetlauncher.app.data.model.ClockAlignment
import com.facetlauncher.app.data.model.ClockColorOption
import com.facetlauncher.app.data.model.ClockDateStyle
import com.facetlauncher.app.data.model.ClockFontOption
import com.facetlauncher.app.data.model.ClockTemplateId
import com.facetlauncher.app.data.model.DockDisplayMode
import com.facetlauncher.app.data.model.DrawerPresentation
import com.facetlauncher.app.data.model.FontWeightOption
import com.facetlauncher.app.data.model.HomeWallpaper
import com.facetlauncher.app.data.model.LauncherFontOption
import com.facetlauncher.app.data.model.NotificationBadgeStyle
import com.facetlauncher.app.data.model.toColumnPositions
import com.facetlauncher.app.data.model.PlacedItem
import com.facetlauncher.app.domain.ResolvedFacetStyle

/** Everything a scaled Home preview reads — each value already resolved facet-or-global by the caller. */
internal data class HomePreviewState(
    val favorites: List<PlacedItem>,
    val dockItems: List<PlacedItem>,
    val use24HourTime: Boolean,
    val clockTemplateId: ClockTemplateId,
    val clockFontOption: ClockFontOption,
    val clockColorOption: ClockColorOption,
    val clockAccentColorOption: ClockColorOption,
    val clockShowMeridiem: Boolean,
    val clockDateStyle: ClockDateStyle,
    val clockAlignment: ClockAlignment,
    val clockScale: Float,
    val clockZoneHeightDp: Float?,
    val calendarEvents: List<CalendarEvent>,
    val calendarColors: Map<String, String>,
    val calendarDisplay: CalendarDisplayOptions,
    val launcherFontOption: LauncherFontOption,
    val appRowPosition: AppRowPosition,
    val appRowPresentation: AppRowPresentation,
    val appListLayout: AppListLayout,
    val appListVerticalAlignment: AppListVerticalAlignment,
    val appListColumnAlignment: AppListColumnAlignment,
    val appListGridColumns: AppListGridColumns,
    val appListGridDisplayMode: AppListGridDisplayMode,
    val appLabelColorOption: ClockColorOption,
    val homeAppsFontWeight: FontWeightOption,
    val dockDisplayMode: DockDisplayMode,
    /** The facet's own hosted clock widget, drawn in place of the native clock exactly as Home does; `null` means the native clock. */
    val clockWidget: PreviewClockWidget? = null,
)

/** [sizeDp] is the saved portrait size; `null` (never resized) falls back to the provider's own default, as on Home. */
internal data class PreviewClockWidget(val appWidgetId: Int, val sizeDp: Pair<Int, Int>?)

/**
 * The portrait preview of [this] style plus the content a card shows. Every look value comes from the
 * same [ResolvedFacetStyle] Home reads, so a preview cannot drift from the screen it stands for.
 */
internal fun ResolvedFacetStyle.toPreviewState(
    favorites: List<PlacedItem>,
    dockItems: List<PlacedItem>,
    calendarEvents: List<CalendarEvent>,
    calendarColors: Map<String, String>,
    launcherFontOption: LauncherFontOption,
    appLabelColorOption: ClockColorOption,
    homeAppsFontWeight: FontWeightOption,
): HomePreviewState {
    val clockPlacement = clockPlacement.portrait
    val listAlignment = listAlignment.portrait
    return HomePreviewState(
        favorites = favorites,
        dockItems = dockItems,
        use24HourTime = clock.use24HourTime,
        clockTemplateId = clock.templateId,
        clockFontOption = clock.fontOption,
        clockColorOption = clock.colorOption,
        clockAccentColorOption = clock.accentColorOption,
        clockShowMeridiem = clock.showMeridiem,
        clockDateStyle = clock.dateStyle,
        clockAlignment = clockPlacement.alignment,
        clockScale = clockPlacement.scale,
        clockZoneHeightDp = clockPlacement.zoneHeightDp,
        calendarEvents = calendarEvents,
        calendarColors = calendarColors,
        calendarDisplay = calendarDisplay,
        launcherFontOption = launcherFontOption,
        appRowPosition = listAlignment.rowPosition,
        appRowPresentation = appList.rowPresentation,
        appListLayout = appList.layout,
        appListVerticalAlignment = listAlignment.verticalAlignment,
        appListColumnAlignment = listAlignment.columnAlignment,
        appListGridColumns = appList.gridColumns,
        appListGridDisplayMode = appList.gridDisplayMode,
        appLabelColorOption = appLabelColorOption,
        homeAppsFontWeight = homeAppsFontWeight,
        dockDisplayMode = dockDisplayMode,
        clockWidget = clockWidget?.let { PreviewClockWidget(it.appWidgetId, it.sizeDp.portrait) },
    )
}
