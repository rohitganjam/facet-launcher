package com.facetlauncher.app.ui.home

import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInRoot
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.onClick
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.facetlauncher.app.R
import com.facetlauncher.app.data.model.AppListLayout
import com.facetlauncher.app.data.model.AppRowPosition
import com.facetlauncher.app.data.model.ClockAlignment
import com.facetlauncher.app.data.model.DockDisplayMode
import com.facetlauncher.app.data.model.MediaPlayerState
import com.facetlauncher.app.ui.components.AppIconSize
import com.facetlauncher.app.ui.components.dashedBorder
import com.facetlauncher.app.ui.home.ClockAdjustMode
import com.facetlauncher.app.ui.home.DockIcon
import com.facetlauncher.app.ui.home.MediaPlayerRow
import com.facetlauncher.app.ui.theme.Accent
import com.facetlauncher.app.ui.theme.Ink
import com.facetlauncher.app.ui.theme.Muted

/**
 * The clock, calendar and accessories, or the hosted clock widget. A tap opens the default clock app and a
 * long-press opens the adjust menu; both live in one detector, since a nested child detector would claim the
 * down event and keep this ancestor's long-press from completing. The detector covers only the rendered clock,
 * so a long-press in the empty space beside it still reaches the facet carousel's own gesture.
 */
@Composable
internal fun HomeClockSlot(frame: HomeFrame, alignment: ClockAlignment, scaleOriginY: Float, modifier: Modifier = Modifier) {
    val state = frame.state
    val clock = state.clock
    val widget = state.widget
    val actions = frame.actions
    val layout = frame.layout
    val density = frame.density
    val widgetSize = frame.geometry.effectiveWidgetSizePx
    ClockBlock(
        modifier = modifier,
        clockContentModifier = Modifier
            .pointerInput(Unit) {
                detectTapGestures(
                    onTap = { actions.clock.onClockClick() },
                    onLongPress = { actions.clock.onAdjustModeChange(ClockAdjustMode.MENU) },
                )
            }
            .testTag(homeTag("home_clock_block")),
        clockBoxModifier = Modifier.onGloballyPositioned {
            layout.clockBoxSize = it.size
            layout.clockBoxOriginInRoot = it.positionInRoot()
        },
        use24HourTime = clock.use24HourTime,
        templateId = clock.templateId,
        fontOption = clock.fontOption,
        colorOption = clock.colorOption,
        accentColorOption = clock.accentColorOption,
        showMeridiem = clock.showMeridiem,
        dateStyle = clock.dateStyle,
        clockAlignment = alignment,
        clockScale = frame.geometry.effectiveScale,
        clockScaleOriginY = scaleOriginY,
        events = state.calendar.events,
        calendarColors = state.calendar.colors,
        calendarDisplay = state.calendar.display,
        homeAppsFontWeight = state.list.look.labelFontWeightOption,
        appLabelColorOption = state.list.look.labelColorOption,
        onEventClick = actions.clock.onEventClick,
        launcherFontOption = clock.launcherFontOption,
        nextAlarmMillis = state.calendar.nextAlarmMillis,
        batteryPercent = state.calendar.batteryPercent,
        isCharging = state.calendar.isCharging,
        clockWidgetAppWidgetId = widget?.appWidgetId,
        createClockWidgetHostView = actions.widget.createHostView,
        clockWidgetWidthDp = widgetSize?.let { with(density) { it.x.toDp() } } ?: widget?.widthDp,
        clockWidgetHeightDp = widgetSize?.let { with(density) { it.y.toDp() } } ?: widget?.heightDp,
        // The saved size, not the live drag: it only changes once a commit round-trips through Room, and the real provider push must not track every drag frame.
        clockWidgetCommittedWidthDp = widget?.widthDp,
        clockWidgetCommittedHeightDp = widget?.heightDp,
        onClockWidgetSizeChange = actions.widget.onSizeChange,
        onClockWidgetLongPress = { actions.clock.onAdjustModeChange(ClockAdjustMode.MENU) },
        onClockWidgetNestedScrollStart = actions.widget.onNestedScrollStart,
        onClockWidgetLeftoverScroll = actions.widget.onLeftoverScroll,
        onClockWidgetNestedScrollStop = actions.widget.onNestedScrollStop,
        onClockWidgetLeftoverFling = actions.widget.onLeftoverFling,
    )
}

@Composable
internal fun HomeMediaSlot(frame: HomeFrame, player: MediaPlayerState, modifier: Modifier = Modifier) {
    val media = frame.actions.media
    MediaPlayerRow(
        state = player,
        showSkipButtons = frame.state.media.showSkipButtons,
        onPrevious = media.onPrevious,
        onPlayPause = media.onPlayPause,
        onNext = media.onNext,
        onOpen = media.onOpen,
        labelColor = frame.itemStyle.labelColor,
        labelFontWeight = frame.itemStyle.labelFontWeight,
        modifier = modifier,
    )
}

/**
 * The app list's reserved region. Inert (never binds) whenever there's room below the handle, and only
 * clips and scrolls in the genuine overflow case. The grid scrolls itself; the other layouts scroll this Column.
 */
@Composable
internal fun HomeAppListRegion(frame: HomeFrame, maxHeight: Dp, modifier: Modifier = Modifier) {
    val layout = frame.layout
    Column(
        modifier = modifier
            .fillMaxWidth()
            .heightIn(max = maxHeight)
            .onGloballyPositioned {
                layout.appListSize = it.size
                layout.appListOriginInRoot = it.positionInRoot()
            }
            .let { if (frame.state.list.layout == AppListLayout.GRID) it else it.verticalScroll(rememberScrollState()) }
            .testTag(homeTag("home_app_list_scroll_region")),
    ) {
        // A scrollable Column measures its content at its natural height regardless of the clamp above, so this
        // inner Column lets the compact-spacing check read that height. The alpha is draw-phase only, so the
        // content stays measured while invisible before the reveal.
        Column(
            modifier = Modifier
                .onGloballyPositioned { layout.appListNaturalHeightPx = it.size.height.toFloat() }
                .graphicsLayer { alpha = frame.listAppearAlpha.value },
        ) {
            if (frame.state.list.showUsageAccessPrompt) {
                UsageAccessStrip(onClick = frame.actions.onUsageAccessPromptClick)
            } else {
                HomeAppListBody(frame)
            }
        }
    }
}

@Composable
private fun HomeAppListBody(frame: HomeFrame) {
    val list = frame.state.list
    val compact = frame.layout.useCompactAppSpacing
    val rowPadding = if (compact) HOME_APP_ROW_COMPACT_VERTICAL_PADDING else HOME_APP_ROW_REGULAR_VERTICAL_PADDING
    val rowIconSize = if (compact) AppIconSize.ROW_COMPACT else AppIconSize.ROW_REGULAR
    val position = list.alignment.rowPosition
    when (list.layout) {
        AppListLayout.TWO_COLUMN -> HomeAppTwoColumnList(
            content = frame.listContent,
            actions = frame.actions.item,
            columnAlignment = list.alignment.columnAlignment,
            appRowPresentation = list.rowPresentation,
            verticalPadding = rowPadding,
            iconSize = rowIconSize,
        )
        AppListLayout.GRID -> HomeAppGrid(
            content = frame.listContent,
            actions = frame.actions.item,
            columns = gridColumns(frame),
            displayMode = list.gridDisplayMode,
        )
        AppListLayout.SINGLE_COLUMN, AppListLayout.LAUNCHER_DEFAULT -> HomeAppListColumn(
            content = frame.listContent,
            actions = frame.actions.item,
            position = position,
            appRowPresentation = list.rowPresentation,
            verticalPadding = rowPadding,
            iconSize = rowIconSize,
            modifier = Modifier.fillMaxWidth(),
            rowWidthFraction = SINGLE_COLUMN_ROW_WIDTH_FRACTION,
            horizontalAlignment = when (position) {
                AppRowPosition.RIGHT -> Alignment.End
                AppRowPosition.CENTER -> Alignment.CenterHorizontally
                AppRowPosition.LEFT, AppRowPosition.LAUNCHER_DEFAULT -> Alignment.Start
            },
        )
    }
}

private fun gridColumns(frame: HomeFrame): Int {
    val requested = frame.state.list.gridColumns.columns
    if (!frame.isLandscape) return requested
    return landscapeGridColumns(requested, with(frame.density) { frame.layout.appsPaneWidthPx.toDp() }, frame.space)
}

@Composable
internal fun HomeDockItems(frame: HomeFrame, displayMode: DockDisplayMode) {
    frame.state.dock.items.forEach { item ->
        DockIcon(item = item, displayMode = displayMode, actions = frame.actions.item, style = frame.itemStyle)
    }
}

/** README `4p`'s shared permission-denied/empty-state strip styling, with a real tap target — shown when the active list content mode needs `PACKAGE_USAGE_STATS` and it isn't granted. */
@Composable
private fun UsageAccessStrip(onClick: () -> Unit, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .testTag(homeTag("home_usage_access_strip"))
            .dashedBorder(color = Ink.copy(alpha = 0.16f), cornerRadius = 10.dp)
            .padding(horizontal = 13.dp, vertical = 11.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = stringResource(R.string.home_usage_access_needed),
            style = MaterialTheme.typography.bodyMedium,
            color = Muted,
            modifier = Modifier.weight(1f),
        )
        Text(text = stringResource(R.string.open_settings), style = MaterialTheme.typography.bodyMedium, color = Accent)
    }
}

/** Single-column rows are only this fraction of the list's width (tap target + ripple), anchored by the row position. */
private const val SINGLE_COLUMN_ROW_WIDTH_FRACTION = 0.8f
