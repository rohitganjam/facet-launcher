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
)
