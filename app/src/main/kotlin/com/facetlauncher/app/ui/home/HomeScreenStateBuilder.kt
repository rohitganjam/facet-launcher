package com.facetlauncher.app.ui.home

import androidx.compose.ui.unit.dp
import com.facetlauncher.app.data.model.CalendarEvent
import com.facetlauncher.app.data.model.Folder
import com.facetlauncher.app.data.model.LauncherSettings
import com.facetlauncher.app.data.model.MediaPlayerState
import com.facetlauncher.app.data.model.PlacedItem
import com.facetlauncher.app.data.model.WeatherIconStyle
import com.facetlauncher.app.domain.ClockAccessoryState
import com.facetlauncher.app.data.model.ClockStackItemKind
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
    val adjustMode: ClockAdjustMode = ClockAdjustMode.NONE,
    val draggingHandle: Boolean = false,
    val placementFacetId: Long? = null,
    val styleOwnerFacetName: String? = null,
    /** The clock stack page showing; `null`, or one no longer in the stack, shows the first. */
    val visibleClockItemId: Long? = null,
    /** Without Pro only the first clock page shows. */
    val isPro: Boolean = true,
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
        look = HomeItemLook(
            badgeCounts = content.badgeCounts,
            badgeStyle = settings.notificationBadgeStyle,
            appMenuNotifications = content.appMenuNotifications,
            folderCandidates = content.folderCandidates?.toImmutableList(),
            labelColorOption = settings.appLabelColorOption,
            labelFontWeightOption = settings.homeAppsFontWeight,
        ),
        layout = appList.layout,
        alignment = listAlignment.of(landscape),
        rowPresentation = appList.rowPresentation,
        gridColumns = appList.gridColumns,
        gridDisplayMode = appList.gridDisplayMode,
        showUsageAccessPrompt = content.showUsageAccessPrompt,
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
        styleOwnerFacetName = live.styleOwnerFacetName,
        adjustMode = live.adjustMode,
        draggingHandle = live.draggingHandle,
    ),
    widget = shownClockPages(live).let { pages -> pages.getOrNull(pages.visibleIndex(live.visibleClockItemId)) }?.toWidgetState(landscape, widgetSizing),
    clockStack = shownClockPages(live).let { pages ->
        HomeClockStackState(
            pageCount = pages.size,
            pageIndex = pages.visibleIndex(live.visibleClockItemId),
            hasClock = pages.any { it.kind == ClockStackItemKind.CLOCK },
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
)

private fun WeatherState.toHomeWeather(settings: LauncherSettings, iconStyle: WeatherIconStyle) = HomeWeatherState(
    symbolCode = now.symbolCode,
    precipitationMm = now.precipitationMm,
    temperatureC = now.temperatureC,
    temperatureUnit = WeatherUnits.resolve(settings.temperatureUnit, Locale.getDefault()),
    iconStyle = iconStyle,
)

/** A widget page keeps its own alignment, the clock page the facet's: `null` or an unset alignment leaves [this] as resolved. */
private fun ClockPlacement.withPageAlignment(page: ResolvedClockItem?, landscape: Boolean): ClockPlacement =
    page?.takeIf { it.kind == ClockStackItemKind.WIDGET }?.alignment?.of(landscape)?.let { copy(alignment = it) } ?: this

/** The clock pages Home shows: all of them with Pro, else the first. */
private fun ResolvedFacetStyle.shownClockPages(live: HomeLiveState) = clockStack.limitedTo(live.isPro)

/** Where [itemId] sits in the stack, or the first page when it is `null` or gone. */
internal fun List<ResolvedClockItem>.visibleIndex(itemId: Long?): Int = indexOfFirst { it.id == itemId }.coerceAtLeast(0)

/** The placeholder's size for an orphaned widget that was never resized, since its provider can no longer say. */
private val ORPHANED_WIDGET_SIZE_DP = 160 to 110

/** The hosted widget a stack page draws; `null` for the native clock. */
private fun ResolvedClockItem.toWidgetState(landscape: Boolean, widgetSizing: HomeWidgetSizing): HomeClockWidgetState? {
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
