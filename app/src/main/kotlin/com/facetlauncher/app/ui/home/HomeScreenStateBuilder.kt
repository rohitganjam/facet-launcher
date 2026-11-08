package com.facetlauncher.app.ui.home

import androidx.compose.ui.unit.dp
import com.facetlauncher.app.data.model.AppListLayout
import com.facetlauncher.app.data.model.ListContentMode
import com.facetlauncher.app.data.model.AppListShrinkToFit
import com.facetlauncher.app.data.model.DrawerPresentation
import com.facetlauncher.app.data.model.CalendarEvent
import com.facetlauncher.app.data.model.Folder
import com.facetlauncher.app.data.model.LauncherSettings
import com.facetlauncher.app.data.model.MediaPlayerState
import com.facetlauncher.app.data.model.PlacedItem
import com.facetlauncher.app.data.model.WeatherIconStyle
import com.facetlauncher.app.domain.ClockAccessoryState
import com.facetlauncher.app.data.model.ClockAlignment
import com.facetlauncher.app.data.model.ClockStackItemKind
import com.facetlauncher.app.data.model.NativeWidgetType
import com.facetlauncher.app.data.model.ClockWidgetConfig
import com.facetlauncher.app.ui.home.weather.ResolvedWeatherUnits
import com.facetlauncher.app.ui.home.weather.WeatherUiState
import com.facetlauncher.app.ui.nativewidget.NativeWidgetState
import com.facetlauncher.app.data.model.isWidget
import com.facetlauncher.app.domain.defaultSizeDp
import com.facetlauncher.app.domain.minSizeDp
import com.facetlauncher.app.domain.ClockPlacement
import com.facetlauncher.app.domain.ResolvedClockItem
import com.facetlauncher.app.domain.ResolvedFacetStyle
import com.facetlauncher.app.domain.limitedTo
import com.facetlauncher.app.domain.WeatherState
import com.facetlauncher.app.domain.WeatherUnits
import com.facetlauncher.app.ui.components.AppMenuNotifications
import java.util.Locale
import kotlinx.collections.immutable.toImmutableList
import kotlinx.collections.immutable.toImmutableMap

/** What a Home screen shows besides the facet's look. */
data class HomeScreenContent(
    val listItems: List<PlacedItem>,
    val dockItems: List<PlacedItem>,
    val calendarEvents: List<CalendarEvent>,
    val badgeCounts: Map<String, Int> = emptyMap(),
    val appMenuNotifications: AppMenuNotifications = AppMenuNotifications.None,
    val folderCandidates: List<Folder>? = null,
    val showUsageAccessPrompt: Boolean = false,
    val clockAccessories: ClockAccessoryState? = null,
    val mediaPlayer: MediaPlayerState? = null,
    val weather: WeatherState? = null,
    /** A weather refresh is running, so a Weather widget with no forecast yet shows a loader rather than its empty or permission text. */
    val weatherRefreshing: Boolean = false,
)

/** Where a hosted clock widget gets its size when its facet never resized it, and its smallest size. Both are `null` for an unknown provider. */
data class HomeWidgetSizing(
    val defaultSizeDp: (appWidgetId: Int) -> Pair<Int, Int>? = { null },
    val resizeFloorDp: (appWidgetId: Int) -> Pair<Int, Int>? = { null },
    /** Whether the widget's provider is gone (its app was uninstalled). */
    val isOrphaned: (appWidgetId: Int) -> Boolean = { false },
)

/** The parts of Home's state that only the live screen has: the adjust mode and which facet owns the clock position. */
data class HomeLiveState(
    val adjustMode: HomeMenuMode = HomeMenuMode.NONE,
    val draggingHandle: Boolean = false,
    val placementFacetId: Long? = null,
    /** The clock stack page showing; `null`, or one no longer in the stack, shows the first. */
    val visibleClockItemId: Long? = null,
    /** Without Pro only the first clock page shows. */
    val isPro: Boolean = true,
)

/** How Home's tiles look; its folders open the way Home's list is laid out, a grid only when Home's is. */
private fun ResolvedFacetStyle.itemLook(settings: LauncherSettings, content: HomeScreenContent) = HomeItemLook(
    badgeCounts = content.badgeCounts,
    badgeStyle = settings.notificationBadgeStyle,
    appMenuNotifications = content.appMenuNotifications,
    folderCandidates = content.folderCandidates?.toImmutableList(),
    drawerPresentation = if (appList.layout == AppListLayout.GRID) DrawerPresentation.GRID else DrawerPresentation.LIST,
    folderGridColumns = appList.gridColumns.columns,
    folderGridDisplayMode = appList.gridDisplayMode,
    labelColorOption = settings.appLabelColorOption,
    labelFontWeightOption = settings.homeAppsFontWeight,
)

/**
 * The state [HomeScreen] draws, built from the resolved style for the given orientation. Home and the
 * facet previews both build theirs here, so a preview shows exactly the placement Home would.
 */
fun ResolvedFacetStyle.toHomeScreenState(
    landscape: Boolean,
    settings: LauncherSettings,
    content: HomeScreenContent,
    widgetSizing: HomeWidgetSizing = HomeWidgetSizing(),
    live: HomeLiveState = HomeLiveState(),
): HomeScreenState = HomeScreenState(
    list = HomeListState(
        items = content.listItems,
        look = itemLook(settings, content),
        layout = appList.layout,
        alignment = listAlignment.of(landscape),
        rowPresentation = appList.rowPresentation,
        gridColumns = appList.gridColumns,
        gridDisplayMode = appList.gridDisplayMode,
        showUsageAccessPrompt = content.showUsageAccessPrompt,
        reorderable = appList.contentMode == ListContentMode.FAVORITES,
        shrinkToFit = appList.shrinkToFit == AppListShrinkToFit.ON,
    ),
    dock = HomeDockState(items = content.dockItems, displayMode = dockDisplayMode),
    clock = HomeClockState(
        use24HourTime = clock.use24HourTime,
        templateId = clock.templateId,
        fontOption = clock.fontOption,
        colorOption = clock.colorOption,
        accentColorOption = clock.accentColorOption,
        showMeridiem = clock.showMeridiem,
        dateStyle = clock.dateStyle,
        launcherFontOption = settings.launcherFontOption,
        placement = clockPlacement.of(landscape).withPageAlignment(shownClockPages(live).let { it.getOrNull(it.visibleIndex(live.visibleClockItemId)) }, landscape),
        placementFacetId = live.placementFacetId,
        boxWidthDp = shownClockPages(live).let { it.getOrNull(it.visibleIndex(live.visibleClockItemId)) }
            ?.takeIf { it.kind == ClockStackItemKind.CLOCK }?.sizeDp?.of(landscape)?.first,
        adjustMode = live.adjustMode,
        draggingHandle = live.draggingHandle,
    ),
    widget = shownClockPages(live).let { pages -> pages.getOrNull(pages.visibleIndex(live.visibleClockItemId)) }?.toWidgetState(landscape, widgetSizing),
    clockStack = shownClockPages(live).let { pages ->
        val index = pages.visibleIndex(live.visibleClockItemId)
        val neighbor = { page: ResolvedClockItem? -> page?.toNeighbor(clockPlacement.of(landscape).alignment, landscape, widgetSizing) }
        HomeClockStackState(
            pageCount = pages.size,
            pageIndex = index,
            hasClock = pages.any { it.kind == ClockStackItemKind.CLOCK },
            visibleItemId = pages.getOrNull(index)?.id,
            previous = neighbor(pages.getOrNull(index - 1)),
            next = neighbor(pages.getOrNull(index + 1)),
        )
    },
    calendar = HomeCalendarState(
        events = content.calendarEvents.toImmutableList(),
        colors = settings.calendarColors.toImmutableMap(),
        display = calendarDisplay,
        nextAlarmMillis = content.clockAccessories?.nextAlarmMillis?.takeIf { clock.showAlarm },
        batteryPercent = content.clockAccessories?.batteryPercent?.takeIf { clock.showBattery },
        isCharging = content.clockAccessories?.isCharging ?: false,
        weather = content.weather?.takeIf { clock.showWeather && !it.isOutdated }?.toHomeWeather(settings, clock.weatherIconStyle),
    ),
    media = HomeMediaState(player = content.mediaPlayer, showSkipButtons = settings.showMediaSkipButtons),
).withNativeWidgets(settings, content.weather, content.weatherRefreshing, clock)

private fun WeatherState.toHomeWeather(settings: LauncherSettings, iconStyle: WeatherIconStyle) = HomeWeatherState(
    symbolCode = now.symbolCode,
    precipitationMm = now.precipitationMm,
    temperatureC = now.temperatureC,
    temperatureUnit = WeatherUnits.resolve(settings.temperatureUnit, Locale.getDefault()),
    iconStyle = iconStyle,
)

/** A widget page keeps its own alignment, the clock page the facet's: `null` or an unset alignment leaves [this] as resolved. */
private fun ClockPlacement.withPageAlignment(page: ResolvedClockItem?, landscape: Boolean): ClockPlacement =
    page?.takeIf { it.kind.isWidget }?.alignment?.of(landscape)?.let { copy(alignment = it) } ?: this

/** A page as drawn beside the visible one: a widget page keeps its own alignment, the clock page uses the facet's [clockAlignment]. */
private fun ResolvedClockItem.toNeighbor(clockAlignment: ClockAlignment, landscape: Boolean, widgetSizing: HomeWidgetSizing) = HomeClockNeighbor(
    itemId = id,
    widget = toWidgetState(landscape, widgetSizing),
    alignment = if (kind.isWidget) alignment.of(landscape) ?: clockAlignment else clockAlignment,
)

/** The clock pages Home shows: all of them with Pro, else the first. */
private fun ResolvedFacetStyle.shownClockPages(live: HomeLiveState) = clockStack.limitedTo(live.isPro)

/** Where [itemId] sits in the stack, or the first page when it is `null` or gone. */
internal fun List<ResolvedClockItem>.visibleIndex(itemId: Long?): Int = indexOfFirst { it.id == itemId }.coerceAtLeast(0)

/** The placeholder's size for an orphaned widget that was never resized, since its provider can no longer say. */
private val ORPHANED_WIDGET_SIZE_DP = 160 to 110

/** The hosted widget a stack page draws; `null` for the native clock. */
private fun ResolvedClockItem.toWidgetState(landscape: Boolean, widgetSizing: HomeWidgetSizing): HomeClockWidgetState? =
    nativeWidgetType?.let { toNativeWidgetState(it, landscape) } ?: toHostedWidgetState(landscape, widgetSizing)

private fun ResolvedClockItem.toHostedWidgetState(landscape: Boolean, widgetSizing: HomeWidgetSizing): HomeClockWidgetState? {
    val widgetId = appWidgetId ?: return null
    val orphaned = widgetSizing.isOrphaned(widgetId)
    val size = sizeDp.of(landscape) ?: widgetSizing.defaultSizeDp(widgetId) ?: ORPHANED_WIDGET_SIZE_DP.takeIf { orphaned }
    val floor = widgetSizing.resizeFloorDp(widgetId)
    return HomeClockWidgetState(
        itemId = id,
        appWidgetId = widgetId,
        isOrphaned = orphaned,
        widthDp = size?.first?.dp,
        heightDp = size?.second?.dp,
        minWidthDp = floor?.first?.dp,
        minHeightDp = floor?.second?.dp,
    )
}

/** A native page has no provider to ask, so its size, floor and id come from its type; the id is negative so it never meets a host-issued one. */
private fun ResolvedClockItem.toNativeWidgetState(type: NativeWidgetType, landscape: Boolean): HomeClockWidgetState {
    val size = sizeDp.of(landscape) ?: type.defaultSizeDp()
    val floor = type.minSizeDp()
    return HomeClockWidgetState(
        itemId = id,
        appWidgetId = -id.toInt() - 1,
        widthDp = size.first.dp,
        heightDp = size.second.dp,
        minWidthDp = floor.first.dp,
        minHeightDp = floor.second.dp,
        nativeType = type,
    )
}

/** What the widgets Facet draws itself read: the full weather with its units, and the calendar and media Home already holds. */
private fun HomeScreenState.withNativeWidgets(settings: LauncherSettings, weather: WeatherState?, weatherRefreshing: Boolean, clockStyle: ClockWidgetConfig): HomeScreenState {
    val locale = Locale.getDefault()
    val ui = WeatherUiState(
        weather = weather,
        isRefreshing = weatherRefreshing,
        units = ResolvedWeatherUnits(
            temperature = WeatherUnits.resolve(settings.temperatureUnit, locale),
            wind = WeatherUnits.resolve(settings.windSpeedUnit, locale),
            rain = WeatherUnits.resolve(settings.rainUnit, locale),
            pressure = WeatherUnits.resolve(settings.pressureUnit, locale),
        ),
        use24HourTime = clockStyle.use24HourTime,
        iconStyle = clockStyle.weatherIconStyle,
    )
    return copy(nativeWidgets = NativeWidgetState(weather = ui, calendar = calendar, media = media))
}
