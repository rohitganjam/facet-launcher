package com.facetlauncher.app.ui.home

import androidx.compose.material3.MaterialTheme
import androidx.compose.foundation.layout.padding
import com.facetlauncher.app.ui.components.GlassPanel
import com.facetlauncher.app.data.model.GlassStyle
import android.appwidget.AppWidgetHostView
import android.content.Context
import android.content.res.Configuration
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import com.facetlauncher.app.ui.hub.OrphanedWidgetTile
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.ui.unit.DpSize
import com.facetlauncher.app.ui.nativewidget.NativeWidget
import com.facetlauncher.app.ui.nativewidget.longPressFallback
import com.facetlauncher.app.ui.nativewidget.NativeWidgetPage
import androidx.compose.runtime.Composable
import androidx.compose.ui.layout.Layout
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.layout.layout
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.facetlauncher.app.data.model.CalendarDisplayOptions
import com.facetlauncher.app.data.model.CalendarEvent
import com.facetlauncher.app.data.model.ClockAlignment
import com.facetlauncher.app.data.model.ClockColorOption
import com.facetlauncher.app.data.model.ClockDateStyle
import com.facetlauncher.app.data.model.ClockFontOption
import com.facetlauncher.app.data.model.ClockTemplateId
import com.facetlauncher.app.data.model.FontWeightOption
import com.facetlauncher.app.data.model.LauncherFontOption
import com.facetlauncher.app.ui.home.clock.CalendarEventsBlock
import com.facetlauncher.app.ui.home.clock.ClockDisplay
import com.facetlauncher.app.ui.home.clock.ClockWeatherAccessory
import com.facetlauncher.app.ui.theme.FacetLauncherTheme
import com.facetlauncher.app.ui.theme.fontFamily
import com.facetlauncher.app.ui.theme.resolve
import java.time.Clock
import java.time.Instant
import java.time.ZoneId
import java.util.Locale
import kotlin.math.roundToInt
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.ImmutableMap
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.persistentMapOf

/**
 * Home clock — real, persisted [templateId]/[fontOption]/[colorOption]/[showMeridiem] (Settings'
 * Clock card → the clock style gallery) stacked above [CalendarEventsBlock], which reads
 * Appearance's home-text settings ([launcherFontOption]/[homeAppsFontWeight]/[appLabelColorOption])
 * and this block's own [clockAlignment] rather than having independent styling of its own (see
 * chat history: calendar/appearance styling consolidation). Every default here matches the app's
 * original shipped look (Light stack, system font, Ink) so nothing changes for a fresh install
 * before any setting is ever touched.
 */
// The clock and its events, with or without the shared panel: the parameter list is most of it.
@Suppress("LongMethod")
@Composable
fun ClockBlock(
    modifier: Modifier = Modifier,
    /**
     * Applied to [ClockDisplay]'s own rendered content only — e.g. a tap/long-press hit box or
     * `testTag` that must hug just the time/date, not [CalendarEventsBlock] beneath it or the
     * empty space beside it. Kept separate from [modifier] (which sizes/positions the WHOLE
     * clock+calendar block, since this stays [androidx.compose.foundation.layout.fillMaxWidth] so
     * [CalendarEventsBlock]'s own [clockAlignment] keeps resolving against the true content
     * width) — see [com.facetlauncher.app.ui.home.HomeScreen]'s own call site for why.
     */
    clockContentModifier: Modifier = Modifier,
    /**
     * Applied to the wrapper whose measured bounds are the clock's *scaled* on-screen box — sits
     * outside [uniformScale] so `onGloballyPositioned`/`border` here see the real post-scale size
     * and position, not the pre-scale layout the draw-only scale layer would otherwise expose.
     */
    clockBoxModifier: Modifier = Modifier,
    clock: Clock = Clock.systemDefaultZone(),
    locale: Locale = Locale.getDefault(),
    use24HourTime: Boolean = false,
    templateId: ClockTemplateId = ClockTemplateId.LIGHT_STACK,
    fontOption: ClockFontOption = ClockFontOption.SYSTEM,
    colorOption: ClockColorOption = ClockColorOption.THEME,
    /** See [com.facetlauncher.app.data.model.LauncherSettings.clockAccentColorOption] — only [com.facetlauncher.app.data.model.usesAccentColor] templates read this. */
    accentColorOption: ClockColorOption = ClockColorOption.ACCENT_PRIMARY,
    showMeridiem: Boolean = false,
    dateStyle: ClockDateStyle = ClockDateStyle.FULL,
    /** One panel behind the clock and its events together; the clock then sizes to what it draws, as in landscape. */
    glass: GlassStyle = GlassStyle.OFF,
    events: ImmutableList<CalendarEvent> = persistentListOf(),
    calendarColors: ImmutableMap<String, String> = persistentMapOf(),
    calendarDisplay: CalendarDisplayOptions = CalendarDisplayOptions(),
    /** Settings → Appearance → "Text weight" — see [com.facetlauncher.app.data.model.LauncherSettings.homeAppsFontWeight]; also styles [CalendarEventsBlock] now. */
    homeAppsFontWeight: FontWeightOption = FontWeightOption.REGULAR,
    /** Settings → Appearance → "Font Color" — see [com.facetlauncher.app.data.model.LauncherSettings.appLabelColorOption]; also styles [CalendarEventsBlock] now. */
    appLabelColorOption: ClockColorOption = ClockColorOption.THEME,
    onEventClick: (CalendarEvent) -> Unit = {},
    /** A long-press on the events under the clock; it belongs to the clock, so it opens the clock's own sheet. */
    onCalendarLongPress: () -> Unit = {},
    launcherFontOption: LauncherFontOption = LauncherFontOption.SYSTEM,
    /** Settings → Clock & Calendar Style → "Clock alignment" — aligns the time/date content itself, not just this block's own position within its container (that's the caller's job, e.g. [HomeScreen]/[com.facetlauncher.app.ui.facets.FacetCarouselScreen]). Also positions [CalendarEventsBlock] now, which no longer has an alignment of its own (see chat history). */
    clockAlignment: ClockAlignment = ClockAlignment.LEFT,
    /** Home clock's scale factor — see [com.facetlauncher.app.data.model.LauncherSettings.clockScale]. */
    clockScale: Float = 0.8f,
    /** Vertical anchor of the scale: 1 (the default) grows the clock up from its bottom edge, 0 grows it down from its top, 0.5 from its middle. */
    clockScaleOriginY: Float = 1f,
    /** When true the block's height follows the clock's drawn size, so the calendar sits right under it instead of under the unscaled footprint. Landscape sets this; its scale is clamped to fit. */
    scaledFootprint: Boolean = false,
    /** Millis since epoch of the system's next alarm, or `null` when none is set — see [com.facetlauncher.app.domain.ObserveClockAccessoriesUseCase]. */
    nextAlarmMillis: Long? = null,
    /** `null` until the first real battery reading arrives. */
    batteryPercent: Int? = null,
    isCharging: Boolean = false,
    /** The weather accessory, or `null` for none. */
    weather: ClockWeatherAccessory? = null,
    /**
     * PRD F15 — non-null when the active facet has a hosted third-party `AppWidget` bound as its
     * clock, replacing every param above (`templateId`, `fontOption`, `clockScale`, ...) with that
     * widget's own opaque rendering. [createClockWidgetHostView]/[clockWidgetWidthDp]/
     * [clockWidgetHeightDp] are only read when this is non-null.
     */
    clockWidgetAppWidgetId: Int? = null,
    /** The widget's app was uninstalled: a placeholder with a Remove action is drawn instead of the host. */
    clockWidgetOrphaned: Boolean = false,
    onClockWidgetRemove: () -> Unit = {},
    /** `null` when [clockWidgetAppWidgetId] has no resolvable provider (orphaned) — see [com.facetlauncher.app.ui.home.HomeViewModel.createClockWidgetHostView]. */
    createClockWidgetHostView: (Context, Int) -> AppWidgetHostView? = { _, _ -> null },
    /**
     * The hosted widget's live on-screen box size, real dp — deliberately **not** [clockScale]'s
     * continuous `graphicsLayer` transform (see [uniformScale]'s own doc): a hosted `AppWidget`
     * needs its actual box size, not a visual stretch of an already-rendered layer. Updates every
     * frame while the user drags a resize handle, purely visual (cheap Compose layout) — see
     * [ClockWidgetTile]'s own doc for why the real system-level push is driven by
     * [clockWidgetCommittedWidthDp]/[clockWidgetCommittedHeightDp] instead, not this.
     */
    clockWidgetWidthDp: Dp? = null,
    clockWidgetHeightDp: Dp? = null,
    /** The size actually pushed to the widget's provider — only changes once, on drag-end commit. Falls back to [clockWidgetWidthDp]/[clockWidgetHeightDp] when not supplied (e.g. previews). */
    clockWidgetCommittedWidthDp: Dp? = null,
    clockWidgetCommittedHeightDp: Dp? = null,
    /** Set when the widget page is one Facet draws itself; the hosted-widget params above are then unused except the size and long-press. */
    nativeWidget: NativeWidgetPage? = null,
    onClockWidgetSizeChange: (appWidgetId: Int, widthDp: Int, heightDp: Int) -> Unit = { _, _, _ -> },
    /** Fired on a real long-press on the hosted widget's own surface — see [ClockWidgetTouchGate]'s own doc for why this can't be [clockContentModifier]'s Compose-level gesture, unlike the native clock. */
    onClockWidgetLongPress: () -> Unit = {},
    /** PRD F15's nested-scroll handoff — see [ClockWidgetTile]'s own doc; a swipe starting on the widget hands off to Home's own swipe axis only once the widget's own scrollable content genuinely has nothing left to do with it. */
    onClockWidgetNestedScrollStart: () -> Unit = {},
    onClockWidgetLeftoverScroll: (dxPx: Float, dyPx: Float) -> Unit = { _, _ -> },
    onClockWidgetNestedScrollStop: () -> Unit = {},
    onClockWidgetLeftoverFling: (velocityXPx: Float, velocityYPx: Float) -> Unit = { _, _ -> },
) {
    // The panel is for the built-in clock only: a hosted or native widget page draws its own look.
    val panelOn = glass != GlassStyle.OFF && clockWidgetAppWidgetId == null && nativeWidget == null
    val clockBox: @Composable (Modifier) -> Unit = { alignModifier ->
        Box(
            modifier = alignModifier
                // Measure the clock at its natural width even when the column is narrower (a narrow
                // landscape pane): otherwise the box size, which the resize maths relies on, is clamped.
                .wrapContentWidth(align = clockAlignment.resolve(), unbounded = true)
                .scaledFootprint((scaledFootprint || panelOn) && clockWidgetAppWidgetId == null, clockScale, clockAlignment, clockScaleOriginY)
                .then(clockBoxModifier),
        ) {
            ClockOrWidgetContent(
                clock = clock,
                locale = locale,
                use24HourTime = use24HourTime,
                templateId = templateId,
                fontOption = fontOption,
                colorOption = colorOption,
                accentColorOption = accentColorOption,
                showMeridiem = showMeridiem,
                dateStyle = dateStyle,
                launcherFontOption = launcherFontOption,
                clockAlignment = clockAlignment,
                clockScale = clockScale,
                clockScaleOriginY = clockScaleOriginY,
                nextAlarmMillis = nextAlarmMillis,
                batteryPercent = batteryPercent,
                isCharging = isCharging,
                weather = weather,
                clockWidgetAppWidgetId = clockWidgetAppWidgetId,
                clockWidgetOrphaned = clockWidgetOrphaned,
                onClockWidgetRemove = onClockWidgetRemove,
                createClockWidgetHostView = createClockWidgetHostView,
                clockWidgetWidthDp = clockWidgetWidthDp,
                clockWidgetHeightDp = clockWidgetHeightDp,
                clockWidgetCommittedWidthDp = clockWidgetCommittedWidthDp,
                clockWidgetCommittedHeightDp = clockWidgetCommittedHeightDp,
                nativeWidget = nativeWidget,
                onClockWidgetSizeChange = onClockWidgetSizeChange,
                onClockWidgetLongPress = onClockWidgetLongPress,
                onClockWidgetNestedScrollStart = onClockWidgetNestedScrollStart,
                onClockWidgetLeftoverScroll = onClockWidgetLeftoverScroll,
                onClockWidgetNestedScrollStop = onClockWidgetNestedScrollStop,
                onClockWidgetLeftoverFling = onClockWidgetLeftoverFling,
                clockContentModifier = clockContentModifier,
            )
        }
    }
    val eventsBlock: @Composable (Modifier) -> Unit = { alignModifier ->
        if (clockWidgetAppWidgetId == null) { // only the built-in clock carries the events list
            CalendarEventsBlock(
                events = events, clock = clock, locale = locale,
                use24HourTime = use24HourTime, calendarColors = calendarColors,
                fontFamily = launcherFontOption.fontFamily,
                textColor = appLabelColorOption.resolve(),
                fontWeight = homeAppsFontWeight.resolve(),
                alignment = clockAlignment,
                display = calendarDisplay,
                onEventClick = onEventClick,
                modifier = alignModifier.longPressFallback(onCalendarLongPress),
            )
        }
    }
    if (panelOn) {
        // The outer column keeps the block's full width; the panel hugs the wider of the clock and its events.
        Column(modifier = modifier.fillMaxWidth()) {
            GlassPanel(style = glass, shape = MaterialTheme.shapes.large, modifier = Modifier.align(clockAlignment.resolve()).testTag("clock_glass_panel")) {
                HuggingColumn(alignment = clockAlignment, spacing = CLOCK_PANEL_GAP, modifier = Modifier.padding(CLOCK_PANEL_PADDING)) {
                    clockBox(Modifier)
                    eventsBlock(Modifier)
                }
            }
        }
    } else {
        Column(modifier = modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            clockBox(Modifier.align(clockAlignment.resolve()))
            eventsBlock(Modifier.align(clockAlignment.resolve()))
        }
    }
}

private val CLOCK_PANEL_PADDING = 12.dp
private val CLOCK_PANEL_GAP = 10.dp

/**
 * A column as wide as its widest child, each child placed by [alignment] inside that width: what a panel behind the clock and its events needs,
 * since the events are aligned against the clock's width and not the screen's. Measured without intrinsics, which the clock's own layouts do not offer.
 */
@Composable
private fun HuggingColumn(alignment: ClockAlignment, spacing: Dp, modifier: Modifier = Modifier, content: @Composable () -> Unit) {
    Layout(content = content, modifier = modifier) { measurables, constraints ->
        val gap = spacing.roundToPx()
        val placeables = measurables.map { it.measure(constraints.copy(minWidth = 0, minHeight = 0)) }.filter { it.width > 0 || it.height > 0 }
        val width = (placeables.maxOfOrNull { it.width } ?: 0).coerceIn(constraints.minWidth, constraints.maxWidth)
        val height = placeables.sumOf { it.height } + gap * (placeables.size - 1).coerceAtLeast(0)
        layout(width, height.coerceIn(constraints.minHeight, constraints.maxHeight)) {
            var y = 0
            placeables.forEach { placeable ->
                val x = when (alignment) {
                    ClockAlignment.LEFT -> 0
                    ClockAlignment.CENTER -> (width - placeable.width) / 2
                    ClockAlignment.RIGHT -> width - placeable.width
                }
                placeable.placeRelative(x, y)
                y += placeable.height + gap
            }
        }
    }
}

/**
 * [ClockBlock]'s own clock-or-hosted-widget branch, factored out purely to stay under this
 * codebase's max-composable-length lint rule — every param here is exactly its identically-named
 * counterpart on [ClockBlock] itself; see that composable's own doc for what each one means.
 */
@Composable
private fun ClockOrWidgetContent(
    clock: Clock,
    locale: Locale,
    use24HourTime: Boolean,
    templateId: ClockTemplateId,
    fontOption: ClockFontOption,
    colorOption: ClockColorOption,
    accentColorOption: ClockColorOption,
    showMeridiem: Boolean,
    dateStyle: ClockDateStyle,
    launcherFontOption: LauncherFontOption,
    clockAlignment: ClockAlignment,
    clockScale: Float,
    clockScaleOriginY: Float,
    nextAlarmMillis: Long?,
    batteryPercent: Int?,
    isCharging: Boolean,
    weather: ClockWeatherAccessory?,
    clockWidgetAppWidgetId: Int?,
    clockWidgetOrphaned: Boolean,
    onClockWidgetRemove: () -> Unit,
    createClockWidgetHostView: (Context, Int) -> AppWidgetHostView?,
    clockWidgetWidthDp: Dp?,
    clockWidgetHeightDp: Dp?,
    clockWidgetCommittedWidthDp: Dp?,
    clockWidgetCommittedHeightDp: Dp?,
    nativeWidget: NativeWidgetPage?,
    onClockWidgetSizeChange: (appWidgetId: Int, widthDp: Int, heightDp: Int) -> Unit,
    onClockWidgetLongPress: () -> Unit,
    onClockWidgetNestedScrollStart: () -> Unit,
    onClockWidgetLeftoverScroll: (dxPx: Float, dyPx: Float) -> Unit,
    onClockWidgetNestedScrollStop: () -> Unit,
    onClockWidgetLeftoverFling: (velocityXPx: Float, velocityYPx: Float) -> Unit,
    clockContentModifier: Modifier,
) {
    if (nativeWidget != null && clockWidgetWidthDp != null && clockWidgetHeightDp != null) {
        NativeWidget(
            type = nativeWidget.type,
            size = DpSize(clockWidgetWidthDp, clockWidgetHeightDp),
            state = nativeWidget.state,
            actions = nativeWidget.actions,
            ref = nativeWidget.ref,
            modifier = Modifier.longPressFallback(onClockWidgetLongPress),
        )
    } else if (clockWidgetAppWidgetId != null && clockWidgetWidthDp != null && clockWidgetHeightDp != null) {
        ClockWidgetContent(
            appWidgetId = clockWidgetAppWidgetId,
            orphaned = clockWidgetOrphaned,
            onRemove = onClockWidgetRemove,
            widthDp = clockWidgetWidthDp,
            heightDp = clockWidgetHeightDp,
            committedWidthDp = clockWidgetCommittedWidthDp ?: clockWidgetWidthDp,
            committedHeightDp = clockWidgetCommittedHeightDp ?: clockWidgetHeightDp,
            createHostView = createClockWidgetHostView,
            onSizeChange = onClockWidgetSizeChange,
            onLongPress = onClockWidgetLongPress,
            onNestedScrollStart = onClockWidgetNestedScrollStart,
            onLeftoverScroll = onClockWidgetLeftoverScroll,
            onNestedScrollStop = onClockWidgetNestedScrollStop,
            onLeftoverFling = onClockWidgetLeftoverFling,
        )
    } else {
        val now by rememberTickingNow(clock)
        Box(modifier = Modifier.uniformScale(clockScale, clockAlignment, clockScaleOriginY)) {
            ClockDisplay(
                templateId = templateId,
                fontOption = fontOption,
                now = now,
                use24HourTime = use24HourTime,
                showMeridiem = showMeridiem,
                textColor = colorOption.resolve(),
                // The date and accessory rows use the clock's own text color at full strength, not a faded tone.
                mutedTextColor = colorOption.resolve(),
                accentColor = accentColorOption.resolve(),
                dateStyle = dateStyle,
                modifier = clockContentModifier,
                locale = locale,
                launcherFontOption = launcherFontOption,
                clockAlignment = clockAlignment,
                nextAlarmMillis = nextAlarmMillis,
                batteryPercent = batteryPercent,
                isCharging = isCharging,
                weather = weather,
            )
        }
    }
}

/** [ClockBlock]'s hosted-widget branch, factored out purely to stay under this codebase's max-composable-length lint rule — see [ClockBlock]'s own doc for the params' real meaning. */
@Composable
private fun ClockWidgetContent(
    appWidgetId: Int,
    orphaned: Boolean,
    onRemove: () -> Unit,
    widthDp: Dp,
    heightDp: Dp,
    committedWidthDp: Dp,
    committedHeightDp: Dp,
    createHostView: (Context, Int) -> AppWidgetHostView?,
    onSizeChange: (appWidgetId: Int, widthDp: Int, heightDp: Int) -> Unit,
    onLongPress: () -> Unit,
    onNestedScrollStart: () -> Unit,
    onLeftoverScroll: (dxPx: Float, dyPx: Float) -> Unit,
    onNestedScrollStop: () -> Unit,
    onLeftoverFling: (velocityXPx: Float, velocityYPx: Float) -> Unit,
) {
    if (orphaned) {
        Box(modifier = Modifier.size(widthDp, heightDp)) {
            OrphanedWidgetTile(providerLabel = null, onRemove = onRemove, onKeepSpace = null)
        }
        return
    }
    ClockWidgetTile(
        appWidgetId = appWidgetId,
        committedWidthDp = committedWidthDp,
        committedHeightDp = committedHeightDp,
        createHostView = createHostView,
        onSizeChange = onSizeChange,
        onLongPress = onLongPress,
        onNestedScrollStart = onNestedScrollStart,
        onLeftoverScroll = onLeftoverScroll,
        onNestedScrollStop = onNestedScrollStop,
        onLeftoverFling = onLeftoverFling,
        modifier = Modifier.size(widthDp, heightDp),
    )
}

/**
 * Draw-only scaling: the clock keeps its *natural* layout footprint (so nothing positioned
 * relative to it reflows when the scale changes), and the glyph is scaled via a graphics layer
 * anchored to the alignment-facing bottom corner — so it grows up and away from its anchored
 * edge, into the empty space above, never pushing its own bottom down.
 */
private fun Modifier.uniformScale(
    scale: Float,
    alignment: ClockAlignment,
    originY: Float,
) = this.layout { measurable, _ ->
    val placeable = measurable.measure(Constraints())
    layout(placeable.width, placeable.height) {
        placeable.placeWithLayer(0, 0) {
            scaleX = scale
            scaleY = scale
            transformOrigin = TransformOrigin(scaleOriginX(alignment), originY)
        }
    }
}

/** How far below the top of its natural box of [naturalHeight] a clock is drawn once [uniformScale] has scaled it by [scale] about [originY]. Negative when it is scaled up past the top. */
internal fun scaledClockTop(naturalHeight: Int, scale: Float, originY: Float): Int =
    (naturalHeight * originY * (1f - scale)).roundToInt()

/** The height of the drawn clock, once [uniformScale] has scaled its natural box of [naturalHeight] by [scale]. */
internal fun scaledClockHeight(naturalHeight: Int, scale: Float): Int =
    (naturalHeight * scale).roundToInt().coerceAtLeast(0)

/** [uniformScale]'s horizontal origin for [alignment]: the clock grows away from the edge it is aligned to. */
internal fun scaleOriginX(alignment: ClockAlignment): Float = when (alignment) {
    ClockAlignment.LEFT -> 0f
    ClockAlignment.RIGHT -> 1f
    ClockAlignment.CENTER -> 0.5f
}

/**
 * Makes the wrapped box occupy exactly the rectangle the scaled clock is drawn in, both ways, so what sits below follows the clock
 * and a glass panel around it hugs what is drawn. Measurements taken inside this modifier still see the natural box.
 */
private fun Modifier.scaledFootprint(enabled: Boolean, scale: Float, alignment: ClockAlignment, originY: Float) =
    if (!enabled) this else this.layout { measurable, constraints ->
        val placeable = measurable.measure(constraints)
        // The same arithmetic as for the height: the drawn edge's offset into the natural box along that axis.
        val left = scaledClockTop(placeable.width, scale, scaleOriginX(alignment))
        val top = scaledClockTop(placeable.height, scale, originY)
        layout(scaledClockHeight(placeable.width, scale), scaledClockHeight(placeable.height, scale)) { placeable.place(-left, -top) }
    }

@Preview(showBackground = true)
@Preview(name = "Dark", showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun ClockBlockPreview() {
    FacetLauncherTheme {
        val fixedClock = Clock.fixed(Instant.parse("2026-08-27T09:05:00Z"), ZoneId.of("UTC"))
        ClockBlock(
            clock = fixedClock,
            events = persistentListOf(
                CalendarEvent(id = 1, calendarId = "1", title = "Team standup", startTimeMillis = fixedClock.millis() + 3_600_000, endTimeMillis = fixedClock.millis() + 5_400_000, isAllDay = false),
                CalendarEvent(id = 2, calendarId = "1", title = "Design review", startTimeMillis = fixedClock.millis() + 7_200_000, endTimeMillis = fixedClock.millis() + 9_000_000, isAllDay = false),
            ),
        )
    }
}
