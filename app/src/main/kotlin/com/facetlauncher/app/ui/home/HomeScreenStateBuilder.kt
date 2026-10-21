package com.facetlauncher.app.ui.home

import androidx.compose.ui.unit.dp
import com.facetlauncher.app.data.model.CalendarEvent
import com.facetlauncher.app.data.model.Folder
import com.facetlauncher.app.data.model.LauncherSettings
import com.facetlauncher.app.data.model.MediaPlayerState
import com.facetlauncher.app.data.model.PlacedItem
import com.facetlauncher.app.domain.ClockAccessoryState
import com.facetlauncher.app.domain.ResolvedFacetStyle
import com.facetlauncher.app.ui.components.AppMenuNotifications

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
)

/** Where a hosted clock widget gets its size when its facet never resized it, and its smallest size. Both are `null` for an unknown provider. */
data class HomeWidgetSizing(
    val defaultSizeDp: (appWidgetId: Int) -> Pair<Int, Int>? = { null },
    val resizeFloorDp: (appWidgetId: Int) -> Pair<Int, Int>? = { null },
)

/** The parts of Home's state that only the live screen has: the adjust mode and which facet owns the clock position. */
data class HomeLiveState(
    val adjustMode: ClockAdjustMode = ClockAdjustMode.NONE,
    val draggingHandle: Boolean = false,
    val isPro: Boolean = true,
    val positionOwnerFacetId: Long? = null,
    val positionOwnerFacetName: String? = null,
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
            folderCandidates = content.folderCandidates,
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
        placement = clockPlacement.of(landscape),
        positionOwnerFacetId = live.positionOwnerFacetId,
        positionOwnerFacetName = live.positionOwnerFacetName,
        adjustMode = live.adjustMode,
        draggingHandle = live.draggingHandle,
        isPro = live.isPro,
    ),
    widget = clockWidget?.let { widget ->
        val size = widget.sizeDp.of(landscape) ?: widgetSizing.defaultSizeDp(widget.appWidgetId)
        val floor = widgetSizing.resizeFloorDp(widget.appWidgetId)
        HomeClockWidgetState(
            appWidgetId = widget.appWidgetId,
            widthDp = size?.first?.dp,
            heightDp = size?.second?.dp,
            minWidthDp = floor?.first?.dp,
            minHeightDp = floor?.second?.dp,
        )
    },
    calendar = HomeCalendarState(
        events = content.calendarEvents,
        colors = settings.calendarColors,
        display = calendarDisplay,
        nextAlarmMillis = content.clockAccessories?.nextAlarmMillis,
        batteryPercent = content.clockAccessories?.batteryPercent,
        isCharging = content.clockAccessories?.isCharging ?: false,
    ),
    media = HomeMediaState(player = content.mediaPlayer, showSkipButtons = settings.showMediaSkipButtons),
)
