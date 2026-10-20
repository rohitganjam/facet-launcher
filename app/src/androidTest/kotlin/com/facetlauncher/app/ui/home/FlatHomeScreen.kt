package com.facetlauncher.app.ui.home

import android.appwidget.AppWidgetHostView
import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import com.facetlauncher.app.data.model.AppInfo
import com.facetlauncher.app.data.model.AppListColumnAlignment
import com.facetlauncher.app.data.model.AppListGridColumns
import com.facetlauncher.app.data.model.AppListGridDisplayMode
import com.facetlauncher.app.data.model.AppListLayout
import com.facetlauncher.app.data.model.AppListVerticalAlignment
import com.facetlauncher.app.data.model.AppRowPosition
import com.facetlauncher.app.data.model.AppRowPresentation
import com.facetlauncher.app.data.model.CalendarEvent
import com.facetlauncher.app.data.model.ClockAlignment
import com.facetlauncher.app.data.model.DockDisplayMode
import com.facetlauncher.app.data.model.MediaPlayerState
import com.facetlauncher.app.data.model.PlacedItem

/**
 * `HomeScreen` with the flat parameters the tests set, so a test names only what it cares about instead of
 * building the state and action groups by hand. Anything not listed keeps the groups' own defaults.
 */
@Composable
internal fun FlatHomeScreen(
    appListItems: List<PlacedItem>,
    dockApps: List<PlacedItem>,
    onAppClick: (AppInfo) -> Unit,
    modifier: Modifier = Modifier,
    appRowPosition: AppRowPosition = AppRowPosition.LEFT,
    appRowPresentation: AppRowPresentation = AppRowPresentation.ICON_AND_TEXT,
    appListLayout: AppListLayout = AppListLayout.SINGLE_COLUMN,
    appListColumnAlignment: AppListColumnAlignment = AppListColumnAlignment.BOTH_LEFT,
    appListGridColumns: AppListGridColumns = AppListGridColumns.FOUR,
    appListGridDisplayMode: AppListGridDisplayMode = AppListGridDisplayMode.ICONS,
    appListVerticalAlignment: AppListVerticalAlignment = AppListVerticalAlignment.BOTTOM,
    showUsageAccessPrompt: Boolean = false,
    onUsageAccessPromptClick: () -> Unit = {},
    badgeCounts: Map<String, Int> = emptyMap(),
    dockDisplayMode: DockDisplayMode = DockDisplayMode.ICONS,
    mediaPlayer: MediaPlayerState? = null,
    calendarEvents: List<CalendarEvent> = emptyList(),
    onEventClick: (CalendarEvent) -> Unit = {},
    clockAlignment: ClockAlignment = ClockAlignment.LEFT,
    clockScale: Float = 0.8f,
    clockZoneHeightDp: Float? = null,
    clockAdjustMode: ClockAdjustMode = ClockAdjustMode.NONE,
    draggingHandle: Boolean = false,
    onClockZoneHeightCommit: (Float) -> Unit = {},
    onClockScaleCommit: (Float) -> Unit = {},
    onClockAlignmentChange: (ClockAlignment) -> Unit = {},
    onAdjustModeChange: (ClockAdjustMode) -> Unit = {},
    onDraggingHandleChange: (Boolean) -> Unit = {},
    onClockClick: () -> Unit = {},
    onNavigateToSettings: () -> Unit = {},
    onNavigateToFacetSettings: () -> Unit = {},
    /** Ignored: a widget is present exactly when [clockWidgetAppWidgetId] is set. */
    @Suppress("UNUSED_PARAMETER") hasCustomClockWidget: Boolean = false,
    clockWidgetAppWidgetId: Int? = null,
    createClockWidgetHostView: (Context, Int) -> AppWidgetHostView? = { _, _ -> null },
    clockWidgetWidthDp: Dp? = null,
    clockWidgetHeightDp: Dp? = null,
    clockWidgetMinWidthDp: Dp? = null,
    clockWidgetMinHeightDp: Dp? = null,
    onClockWidgetSizeCommit: (widthDp: Int, heightDp: Int) -> Unit = { _, _ -> },
    layoutMode: HomeLayoutMode = rememberHomeLayoutMode(),
    landscapeSpace: HomeLandscapeSpace = rememberHomeLandscapeSpace(),
) {
    HomeScreen(
        state = HomeScreenState(
            list = HomeListState(
                items = appListItems,
                look = HomeItemLook(badgeCounts = badgeCounts),
                layout = appListLayout,
                alignment = ListAlignment(appRowPosition, appListVerticalAlignment, appListColumnAlignment),
                rowPresentation = appRowPresentation,
                gridColumns = appListGridColumns,
                gridDisplayMode = appListGridDisplayMode,
                showUsageAccessPrompt = showUsageAccessPrompt,
            ),
            dock = HomeDockState(items = dockApps, displayMode = dockDisplayMode),
            clock = HomeClockState(
                placement = ClockPlacement(clockAlignment, clockZoneHeightDp, clockScale),
                adjustMode = clockAdjustMode,
                draggingHandle = draggingHandle,
            ),
            widget = clockWidgetAppWidgetId?.let {
                HomeClockWidgetState(it, clockWidgetWidthDp, clockWidgetHeightDp, clockWidgetMinWidthDp, clockWidgetMinHeightDp)
            },
            calendar = HomeCalendarState(events = calendarEvents),
            media = HomeMediaState(player = mediaPlayer),
        ),
        actions = HomeScreenActions(
            item = HomeItemActions(onAppClick = onAppClick),
            clock = HomeClockActions(
                onClockClick = onClockClick,
                onNavigateToSettings = onNavigateToSettings,
                onNavigateToFacetSettings = onNavigateToFacetSettings,
                onEventClick = onEventClick,
                onZoneHeightCommit = onClockZoneHeightCommit,
                onScaleCommit = onClockScaleCommit,
                onAlignmentChange = onClockAlignmentChange,
                onAdjustModeChange = onAdjustModeChange,
                onDraggingHandleChange = onDraggingHandleChange,
            ),
            widget = HomeClockWidgetActions(createHostView = createClockWidgetHostView, onSizeCommit = onClockWidgetSizeCommit),
            onUsageAccessPromptClick = onUsageAccessPromptClick,
        ),
        modifier = modifier,
        layoutMode = layoutMode,
        landscapeSpace = landscapeSpace,
    )
}
