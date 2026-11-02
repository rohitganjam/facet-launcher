package com.facetlauncher.app.ui.home

import com.facetlauncher.app.data.model.WeatherIconStyle
import com.facetlauncher.app.domain.ListAlignment
import com.facetlauncher.app.domain.ClockPlacement
import android.appwidget.AppWidgetHostView
import android.content.Context
import androidx.compose.runtime.Immutable
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.layout.layout
import androidx.compose.ui.unit.Dp
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
import com.facetlauncher.app.data.model.Folder
import com.facetlauncher.app.data.model.FontWeightOption
import com.facetlauncher.app.data.model.TemperatureUnit
import com.facetlauncher.app.data.model.LauncherFontOption
import com.facetlauncher.app.data.model.MediaPlayerState
import com.facetlauncher.app.data.model.NotificationBadgeStyle
import com.facetlauncher.app.data.model.PlacedItem
import com.facetlauncher.app.ui.components.AppMenuNotifications
import com.facetlauncher.app.ui.home.ClockAdjustMode
import com.facetlauncher.app.ui.home.HomeItemActions
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.ImmutableMap
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.persistentMapOf

/*
 * What `HomeScreen` shows (the `*State` classes) and what it does about it (the `*Actions` classes), grouped by
 * the part of Home they belong to. All are data classes of stable fields, so an unchanged group lets its
 * composables skip recomposition. `HomeDrawerRoute` builds them from the view models.
 */

/** The clock block: its look, where it sits in the current orientation, and the adjust-mode state. */
@Immutable
data class HomeClockState(
    val use24HourTime: Boolean = false,
    val templateId: ClockTemplateId = ClockTemplateId.LIGHT_STACK,
    val fontOption: ClockFontOption = ClockFontOption.SYSTEM,
    val colorOption: ClockColorOption = ClockColorOption.THEME,
    val accentColorOption: ClockColorOption = ClockColorOption.ACCENT_PRIMARY,
    val showMeridiem: Boolean = false,
    val dateStyle: ClockDateStyle = ClockDateStyle.FULL,
    val launcherFontOption: LauncherFontOption = LauncherFontOption.SYSTEM,
    val placement: ClockPlacement = ClockPlacement(ClockAlignment.LEFT, zoneHeightDp = null, scale = 0.8f),
    /** The facet whose clock override governs the placement now (`null`: the launcher-wide default), and its name for the menu badge. */
    /** The facet whose placement Home shows (always the active one), so a change of facet re-fits the clock. */
    val placementFacetId: Long? = null,
    /** Names the facet whose own clock style is in force, for the sheet's scope badge; `null` is the launcher-wide style. */
    val styleOwnerFacetName: String? = null,
    val adjustMode: ClockAdjustMode = ClockAdjustMode.NONE,
    /** True while a handle is being dragged, which suppresses the screen's own gestures. */
    val draggingHandle: Boolean = false,
)

/** A hosted clock widget in place of the native clock. Absent when the facet uses the native clock. */
@Immutable
data class HomeClockWidgetState(
    /** The stack item this widget is, so a resize is saved against it. */
    val itemId: Long,
    val appWidgetId: Int,
    val widthDp: Dp?,
    val heightDp: Dp?,
    val minWidthDp: Dp?,
    val minHeightDp: Dp?,
    /** The provider's app was uninstalled: Home draws a placeholder with a Remove action instead of the widget. */
    val isOrphaned: Boolean = false,
)

/** A clock stack page next to the visible one: its own widget (`null` for the native clock) and alignment. */
@Immutable
data class HomeClockNeighbor(val itemId: Long, val widget: HomeClockWidgetState?, val alignment: ClockAlignment)

/** The clock stack as Home draws it: how many pages, which one shows, and whether there are none (the clock area stays blank). */
@Immutable
data class HomeClockStackState(
    val pageCount: Int = 1,
    val pageIndex: Int = 0,
    /** Whether the native clock is on some page: there is only one, so a widget page cannot be replaced with it then. */
    val hasClock: Boolean = true,
    /** The visible page's stack item, which keys its drawing so it keeps its widget view when a swipe makes it the visible one. */
    val visibleItemId: Long? = null,
    /** The pages beside the visible one, drawn next to it while a swipe turns the page. */
    val previous: HomeClockNeighbor? = null,
    val next: HomeClockNeighbor? = null,
) {
    val isBlank: Boolean get() = pageCount == 0
}

/** What sits with the clock: the calendar strip and the alarm, battery, charging and weather accessories. */
@Immutable
data class HomeCalendarState(
    val events: ImmutableList<CalendarEvent> = persistentListOf(),
    val colors: ImmutableMap<String, String> = persistentMapOf(),
    val display: CalendarDisplayOptions = CalendarDisplayOptions(),
    val nextAlarmMillis: Long? = null,
    val batteryPercent: Int? = null,
    val isCharging: Boolean = false,
    val weather: HomeWeatherState? = null,
)

/** What the weather accessory shows: the current hour's symbol and temperature, with the unit already resolved. `null` hides it. */
@Immutable
data class HomeWeatherState(
    val symbolCode: String?,
    val precipitationMm: Double?,
    val temperatureC: Double,
    val temperatureUnit: TemperatureUnit,
    val iconStyle: WeatherIconStyle = WeatherIconStyle.COLORED,
)

/** The data behind a [HomeItemStyle]: its label colour and weight are options here, resolved to themed values where they are drawn. */
@Immutable
data class HomeItemLook(
    val badgeCounts: Map<String, Int> = emptyMap(),
    val badgeStyle: NotificationBadgeStyle = NotificationBadgeStyle.DOT,
    val appMenuNotifications: AppMenuNotifications = AppMenuNotifications.None,
    val folderCandidates: ImmutableList<Folder>? = null,
    /** How Home's folder sheets lay out: a grid like Home's own when Home uses the grid layout, else a list. */
    val drawerPresentation: DrawerPresentation = DrawerPresentation.LIST,
    val folderGridColumns: Int = AppListGridColumns.FOUR.columns,
    val folderGridDisplayMode: AppListGridDisplayMode = AppListGridDisplayMode.ICONS,
    val labelColorOption: ClockColorOption = ClockColorOption.THEME,
    val labelFontWeightOption: FontWeightOption = FontWeightOption.REGULAR,
)

/** The apps list: its items, layout and alignment in the current orientation. */
@Immutable
data class HomeListState(
    val items: List<PlacedItem>,
    val look: HomeItemLook = HomeItemLook(),
    val layout: AppListLayout = AppListLayout.SINGLE_COLUMN,
    val alignment: ListAlignment = ListAlignment(AppRowPosition.LEFT, AppListVerticalAlignment.BOTTOM, AppListColumnAlignment.BOTH_LEFT),
    val rowPresentation: AppRowPresentation = AppRowPresentation.ICON_AND_TEXT,
    val gridColumns: AppListGridColumns = AppListGridColumns.FOUR,
    val gridDisplayMode: AppListGridDisplayMode = AppListGridDisplayMode.ICONS,
    val showUsageAccessPrompt: Boolean = false,
    /** "Shrink list to fit": drop to Compact rows when Regular ones wouldn't fit; off, the list scrolls instead. */
    val shrinkToFit: Boolean = false,
)

@Immutable
data class HomeDockState(
    val items: List<PlacedItem>,
    val displayMode: DockDisplayMode = DockDisplayMode.ICONS,
)

/** The media row, shown while [player] is non-null. */
@Immutable
data class HomeMediaState(
    val player: MediaPlayerState? = null,
    val showSkipButtons: Boolean = true,
)

/** Everything `HomeScreen` shows. */
@Immutable
data class HomeScreenState(
    val list: HomeListState,
    val dock: HomeDockState,
    val clock: HomeClockState = HomeClockState(),
    val widget: HomeClockWidgetState? = null,
    val clockStack: HomeClockStackState = HomeClockStackState(),
    val calendar: HomeCalendarState = HomeCalendarState(),
    val media: HomeMediaState = HomeMediaState(),
)

/** What the clock block and its adjust mode report or ask for. */
@Immutable
data class HomeClockActions(
    val onClockClick: () -> Unit = {},
    val onWeatherClick: () -> Unit = {},
    val onBatteryClick: () -> Unit = {},
    val onClockSettingsClick: () -> Unit = {},
    val onNavigateToSettings: () -> Unit = {},
    val onNavigateToFacetSettings: () -> Unit = {},
    val onReplaceWithWidgetClick: () -> Unit = {},
    val onReplaceWithClockClick: () -> Unit = {},
    val onWidgetStackClick: () -> Unit = {},
    val onEventClick: (CalendarEvent) -> Unit = {},
    val onZoneHeightCommit: (Float) -> Unit = {},
    val onScaleCommit: (Float) -> Unit = {},
    val onAlignmentChange: (ClockAlignment) -> Unit = {},
    val onAdjustModeChange: (ClockAdjustMode) -> Unit = {},
    val onDraggingHandleChange: (Boolean) -> Unit = {},
    /** How far a swipe has displaced the visible page, as a fraction of the zone's width; read while drawing so a swipe does not recompose Home. */
    val pageOffset: () -> Float = { 0f },
    /** The page index the swipe started from, `-1` when none is under way. */
    val pageAnchorIndex: () -> Int = { -1 },
    /** True while a swipe between pages is in progress, so the dots show. */
    val pageDragging: () -> Boolean = { false },
)

/** What a hosted clock widget needs: its host view, its size changes, and the nested-scroll handoff to Home's swipes. */
@Immutable
data class HomeClockWidgetActions(
    val createHostView: (Context, Int) -> AppWidgetHostView? = { _, _ -> null },
    val onSizeChange: (appWidgetId: Int, widthDp: Int, heightDp: Int) -> Unit = { _, _, _ -> },
    val onSizeCommit: (widthDp: Int, heightDp: Int) -> Unit = { _, _ -> },
    /** Removes the visible orphaned widget page from the stack. */
    val onRemoveOrphan: () -> Unit = {},
    val onBoundsChange: (Rect?) -> Unit = {},
    val onNestedScrollStart: () -> Unit = {},
    val onLeftoverScroll: (dxPx: Float, dyPx: Float) -> Unit = { _, _ -> },
    val onNestedScrollStop: () -> Unit = {},
    val onLeftoverFling: (velocityXPx: Float, velocityYPx: Float) -> Unit = { _, _ -> },
)

@Immutable
data class HomeMediaActions(
    val onPrevious: () -> Unit = {},
    val onPlayPause: () -> Unit = {},
    val onNext: () -> Unit = {},
    val onOpen: () -> Unit = {},
)

/** Everything `HomeScreen` can do. */
@Immutable
data class HomeScreenActions(
    val item: HomeItemActions,
    val clock: HomeClockActions = HomeClockActions(),
    val widget: HomeClockWidgetActions = HomeClockWidgetActions(),
    val media: HomeMediaActions = HomeMediaActions(),
    val onUsageAccessPromptClick: () -> Unit = {},
    val onAppListBoundsChange: (Rect?) -> Unit = {},
    /** Landscape's clock pane, `null` in portrait: its vertical drags scroll the pane instead of opening the drawer. */
    val onClockPaneBoundsChange: (Rect?) -> Unit = {},
    /** Portrait's clock zone, everything above the divider: horizontal drags there turn the clock stack's pages. */
    val onClockZoneBoundsChange: (Rect?) -> Unit = {},
)
