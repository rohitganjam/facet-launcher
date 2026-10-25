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
    val positionOwnerFacetId: Long? = null,
    val positionOwnerFacetName: String? = null,
    val adjustMode: ClockAdjustMode = ClockAdjustMode.NONE,
    /** True while a handle is being dragged, which suppresses the screen's own gestures. */
    val draggingHandle: Boolean = false,
    /** Whether "Use custom widget" is a Pro feature the user has. */
    val isPro: Boolean = true,
)

/** A hosted clock widget in place of the native clock. Absent when the facet uses the native clock. */
@Immutable
data class HomeClockWidgetState(
    val appWidgetId: Int,
    val widthDp: Dp?,
    val heightDp: Dp?,
    val minWidthDp: Dp?,
    val minHeightDp: Dp?,
)

/** What sits with the clock: the calendar strip and the alarm, battery, charging and weather accessories. */
@Immutable
data class HomeCalendarState(
    val events: List<CalendarEvent> = emptyList(),
    val colors: Map<String, String> = emptyMap(),
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
    val folderCandidates: List<Folder>? = null,
    val drawerPresentation: DrawerPresentation = DrawerPresentation.LIST,
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
    val calendar: HomeCalendarState = HomeCalendarState(),
    val media: HomeMediaState = HomeMediaState(),
)

/** What the clock block and its adjust mode report or ask for. */
@Immutable
data class HomeClockActions(
    val onClockClick: () -> Unit = {},
    val onWeatherClick: () -> Unit = {},
    val onEditClockStyles: () -> Unit = {},
    val onNavigateToSettings: () -> Unit = {},
    val onNavigateToFacetSettings: () -> Unit = {},
    val onUseCustomWidgetClick: () -> Unit = {},
    val onSwitchToLauncherClockClick: () -> Unit = {},
    val onEventClick: (CalendarEvent) -> Unit = {},
    val onZoneHeightCommit: (Float) -> Unit = {},
    val onScaleCommit: (Float) -> Unit = {},
    val onAlignmentChange: (ClockAlignment) -> Unit = {},
    val onAdjustModeChange: (ClockAdjustMode) -> Unit = {},
    val onDraggingHandleChange: (Boolean) -> Unit = {},
)

/** What a hosted clock widget needs: its host view, its size changes, and the nested-scroll handoff to Home's swipes. */
@Immutable
data class HomeClockWidgetActions(
    val createHostView: (Context, Int) -> AppWidgetHostView? = { _, _ -> null },
    val onSizeChange: (appWidgetId: Int, widthDp: Int, heightDp: Int) -> Unit = { _, _, _ -> },
    val onSizeCommit: (widthDp: Int, heightDp: Int) -> Unit = { _, _ -> },
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
)
