package com.facetlauncher.app.ui.home

import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
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
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.key
import androidx.compose.runtime.derivedStateOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import kotlin.math.roundToInt
import kotlin.math.sign
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.constrainHeight
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.layout.Measurable
import androidx.compose.ui.layout.layoutId
import androidx.compose.ui.geometry.Offset
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
import com.facetlauncher.app.ui.home.clock.LocalBatteryClick
import com.facetlauncher.app.ui.home.weather.rememberClockWeatherAccessory
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
internal fun HomeClockSlot(
    frame: HomeFrame,
    alignment: ClockAlignment,
    scaleOriginY: Float,
    modifier: Modifier = Modifier,
    scaledFootprint: Boolean = false,
    /** Where the page coming in sits against the visible one while a swipe turns it: at the clock's own anchor. */
    anchor: Alignment = Alignment.BottomStart,
) {
    // An empty stack draws nothing, but the slot still takes its place so the divider and list stay put.
    if (frame.state.clockStack.isBlank) {
        Spacer(modifier = modifier)
    } else {
        ClockSlotBlock(frame, alignment, scaleOriginY, scaledFootprint, anchor, modifier)
    }
}

@Composable
private fun ClockSlotBlock(
    frame: HomeFrame,
    alignment: ClockAlignment,
    scaleOriginY: Float,
    scaledFootprint: Boolean,
    anchor: Alignment,
    modifier: Modifier = Modifier,
) {
    val stack = frame.state.clockStack
    val pageOffset = frame.actions.clock.pageOffset
    // Only the side is read here, so a swipe recomposes when it changes side, not on every frame.
    val side by remember(pageOffset) { derivedStateOf { sign(pageOffset()) } }
    // Once the page has changed under a swipe still at its edge, the old page is gone and nothing is beside the new one.
    val swiping = frame.actions.clock.pageAnchorIndex().let { it < 0 || it == stack.pageIndex }
    val incomingId = when {
        !swiping -> null
        side < 0f -> stack.next?.itemId
        side > 0f -> stack.previous?.itemId
        else -> null
    }
    CompositionLocalProvider(LocalBatteryClick provides frame.actions.clock.onBatteryClick) {
        // The pages either side stay composed (parked out of sight), so a widget is never detached and re-attached as a swipe reaches it.
        PageStack(anchor = anchor, landscapePaneHeightPx = if (frame.isLandscape) frame.layout.landscapeClockPaneHeightPx.toInt() else 0, modifier = modifier) {
            val beside = { neighbor: HomeClockNeighbor?, index: Int ->
                neighbor?.let {
                    val role = if (it.itemId == incomingId) PageRole.INCOMING else PageRole.PARKED
                    PageToDraw(it.itemId, role, index, if (frame.isLandscape) ClockAlignment.LEFT else it.alignment, it.widget, it.alignment.verticalOrigin())
                }
            }
            val pages = listOfNotNull(
                beside(stack.previous, stack.pageIndex - 1),
                PageToDraw(stack.visibleItemId, PageRole.VISIBLE, stack.pageIndex, alignment, frame.state.widget, frame.geometry.landscape.originY),
                beside(stack.next, stack.pageIndex + 1),
            )
            pages.forEach { page ->
                key(page.itemId) {
                    Box(Modifier.layoutId(PageSlot(page.role, page.originY))) {
                        ClockPage(frame, ClockPageSpec(page.role, page.alignment, page.widget, page.index), scaleOriginY, scaledFootprint)
                    }
                }
            }
        }
    }
}

private class PageToDraw(val itemId: Long?, val role: PageRole, val index: Int, val alignment: ClockAlignment, val widget: HomeClockWidgetState?, val originY: Float)

/** What [PageStack] reads from a page: its role, and in landscape where in the pane its own alignment rests it (0 top, 1 bottom). */
private data class PageSlot(val role: PageRole, val originY: Float)

private fun ClockAlignment.verticalOrigin() = when (this) {
    ClockAlignment.LEFT -> 0f
    ClockAlignment.CENTER -> 0.5f
    ClockAlignment.RIGHT -> 1f
}

/**
 * The visible page, with the page a swipe is bringing in drawn beside it (without adding to its size) while there is one, and the others parked far out of sight.
 * In landscape ([landscapePaneHeightPx] above 0) the stack fills the pane and each page sits where its own alignment puts it, so one that
 * is taller or lower than the visible page is not clipped as it slides in.
 */
@Composable
private fun PageStack(anchor: Alignment, landscapePaneHeightPx: Int, modifier: Modifier = Modifier, content: @Composable () -> Unit) {
    Layout(content = content, modifier = modifier) { measurables, constraints ->
        val slot = { m: Measurable -> m.layoutId as PageSlot }
        val visible = measurables.first { slot(it).role == PageRole.VISIBLE }
        val shown = visible.measure(constraints)
        val others = measurables.filter { slot(it).role != PageRole.VISIBLE }.map { it to it.measure(Constraints(maxWidth = constraints.maxWidth)) }
        val fillsPane = landscapePaneHeightPx > 0
        val height = if (fillsPane) constraints.constrainHeight(maxOf(landscapePaneHeightPx, shown.height)) else shown.height
        val restingTop = { originY: Float, pageHeight: Int -> (originY * (height - pageHeight)).roundToInt().coerceAtLeast(0) }
        layout(shown.width, height) {
            shown.place(0, if (fillsPane) restingTop(slot(visible).originY, shown.height) else 0)
            others.forEach { (measurable, placeable) ->
                val at = anchor.align(IntSize(placeable.width, placeable.height), IntSize(shown.width, shown.height), layoutDirection)
                val y = if (fillsPane) restingTop(slot(measurable).originY, placeable.height) else at.y
                val x = if (slot(measurable).role == PageRole.INCOMING) at.x else at.x + PARKED_OFFSET_PX
                placeable.place(x, y)
            }
        }
    }
}

/** Far enough off to the side that a parked page is never drawn or touched, yet still attached to the window. */
private const val PARKED_OFFSET_PX = 1 shl 20

/** What a composed page is doing: the visible one, the one a swipe is bringing in beside it, or one waiting out of sight. */
private enum class PageRole { VISIBLE, INCOMING, PARKED }

/** One drawn page: what it is (the clock when [widget] is `null`), where it sits and which role it plays in the swipe. */
private class ClockPageSpec(val role: PageRole, val alignment: ClockAlignment, val widget: HomeClockWidgetState?, val index: Int)

@Composable
private fun ClockPage(frame: HomeFrame, page: ClockPageSpec, scaleOriginY: Float, scaledFootprint: Boolean) {
    val state = frame.state
    val clock = state.clock
    val widget = page.widget
    val actions = frame.actions
    val layout = frame.layout
    val visible = page.role == PageRole.VISIBLE
    val widgetSize = if (visible) frame.geometry.effectiveWidgetSizePx else widget?.let(frame::neighborWidgetSizePx)
    val callbacks = widgetPageCallbacks(actions, visible)
    ClockBlock(
        modifier = Modifier.pageShift(frame, page.index),
        clockContentModifier = if (visible) clockTapModifier(actions.clock) else Modifier,
        clockBoxModifier = if (visible) {
            Modifier.onGloballyPositioned {
                layout.clockBoxSize = it.size
                // Without the page swipe's own shift, or the room left of the box would shrink a hosted widget as the page slides.
                layout.clockBoxOriginInRoot = it.positionInRoot() - Offset(pageShiftPx(frame, page.index), 0f)
            }
        } else {
            Modifier
        },
        use24HourTime = clock.use24HourTime,
        templateId = clock.templateId,
        fontOption = clock.fontOption,
        colorOption = clock.colorOption,
        accentColorOption = clock.accentColorOption,
        showMeridiem = clock.showMeridiem,
        dateStyle = clock.dateStyle,
        clockAlignment = page.alignment,
        clockScale = frame.geometry.effectiveScale,
        clockScaleOriginY = scaleOriginY,
        scaledFootprint = scaledFootprint,
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
        weather = rememberClockWeatherAccessory(state.calendar.weather, actions.clock.onWeatherClick),
        clockWidgetAppWidgetId = widget?.appWidgetId,
        clockWidgetOrphaned = widget?.isOrphaned == true,
        onClockWidgetRemove = callbacks.onRemove,
        createClockWidgetHostView = actions.widget.createHostView,
        clockWidgetWidthDp = widgetSize?.let { with(frame.density) { it.x.toDp() } } ?: widget?.widthDp,
        clockWidgetHeightDp = widgetSize?.let { with(frame.density) { it.y.toDp() } } ?: widget?.heightDp,
        // The saved size, not the live drag: it only changes once a commit round-trips through Room, and the real provider push must not track every drag frame.
        clockWidgetCommittedWidthDp = widget?.widthDp,
        clockWidgetCommittedHeightDp = widget?.heightDp,
        onClockWidgetSizeChange = actions.widget.onSizeChange,
        onClockWidgetLongPress = callbacks.onLongPress,
        onClockWidgetNestedScrollStart = callbacks.onNestedScrollStart,
        onClockWidgetLeftoverScroll = callbacks.onLeftoverScroll,
        onClockWidgetNestedScrollStop = callbacks.onNestedScrollStop,
        onClockWidgetLeftoverFling = callbacks.onLeftoverFling,
    )
}

/** What a widget page does with touches: only the visible page acts, the incoming one is for looking at. */
private class WidgetPageCallbacks(
    val onRemove: () -> Unit,
    val onLongPress: () -> Unit,
    val onNestedScrollStart: () -> Unit,
    val onLeftoverScroll: (Float, Float) -> Unit,
    val onNestedScrollStop: () -> Unit,
    val onLeftoverFling: (Float, Float) -> Unit,
)

private fun widgetPageCallbacks(actions: HomeScreenActions, visible: Boolean) = if (visible) {
    WidgetPageCallbacks(
        onRemove = actions.widget.onRemoveOrphan,
        onLongPress = { actions.clock.onAdjustModeChange(ClockAdjustMode.MENU) },
        onNestedScrollStart = actions.widget.onNestedScrollStart,
        onLeftoverScroll = actions.widget.onLeftoverScroll,
        onNestedScrollStop = actions.widget.onNestedScrollStop,
        onLeftoverFling = actions.widget.onLeftoverFling,
    )
} else {
    WidgetPageCallbacks({}, {}, {}, { _, _ -> }, {}, { _, _ -> })
}

/** A tap on the clock opens the clock app, a long press opens its adjust menu. */
@Composable
private fun clockTapModifier(clock: HomeClockActions): Modifier = Modifier
    .pointerInput(Unit) {
        detectTapGestures(
            onTap = { clock.onClockClick() },
            onLongPress = { clock.onAdjustModeChange(ClockAdjustMode.MENU) },
        )
    }
    .testTag(homeTag("home_clock_block"))

/** Slides the page with the swipe: the one it started from out, the one beside it in from the other side. Read in the draw phase, so a swipe does not recompose Home. */
private fun Modifier.pageShift(frame: HomeFrame, pageIndex: Int): Modifier = graphicsLayer { translationX = pageShiftPx(frame, pageIndex) }

/** How far page [pageIndex] is displaced right now: one zone per page from the one the swipe started on, plus the swipe's offset (a fraction of the zone's width). */
private fun pageShiftPx(frame: HomeFrame, pageIndex: Int): Float {
    val offset = frame.actions.clock.pageOffset()
    val zone = if (frame.isLandscape) frame.geometry.landscapePaneWidthPx else frame.layout.rootSize?.width?.toFloat() ?: 0f
    val anchor = frame.actions.clock.pageAnchorIndex().takeIf { it >= 0 } ?: frame.state.clockStack.pageIndex
    return (pageIndex - anchor + offset) * zone
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
    val rowIconSize = AppIconSize.scaled(if (compact) AppIconSize.COMPACT else AppIconSize.ICON)
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
