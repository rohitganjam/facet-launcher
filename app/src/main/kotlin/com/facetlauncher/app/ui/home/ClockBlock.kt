package com.facetlauncher.app.ui.home

import android.appwidget.AppWidgetHostView
import android.content.Context
import android.content.res.Configuration
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.layout.layout
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
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
import com.facetlauncher.app.ui.theme.FacetLauncherTheme
import com.facetlauncher.app.ui.theme.fontFamily
import com.facetlauncher.app.ui.theme.resolve
import java.time.Clock
import java.time.Instant
import java.time.ZoneId
import java.util.Locale

/**
 * Home clock — real, persisted [templateId]/[fontOption]/[colorOption]/[showMeridiem] (Settings'
 * Clock card → the clock style gallery) stacked above [CalendarEventsBlock], which reads
 * Appearance's home-text settings ([launcherFontOption]/[homeAppsFontWeight]/[appLabelColorOption])
 * and this block's own [clockAlignment] rather than having independent styling of its own (see
 * chat history: calendar/appearance styling consolidation). Every default here matches the app's
 * original shipped look (Light stack, system font, Ink) so nothing changes for a fresh install
 * before any setting is ever touched.
 */
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
    events: List<CalendarEvent> = emptyList(),
    calendarColors: Map<String, String> = emptyMap(),
    /** Settings → Appearance → "Text weight" — see [com.facetlauncher.app.data.model.LauncherSettings.homeAppsFontWeight]; also styles [CalendarEventsBlock] now. */
    homeAppsFontWeight: FontWeightOption = FontWeightOption.REGULAR,
    /** Settings → Appearance → "Font Color" — see [com.facetlauncher.app.data.model.LauncherSettings.appLabelColorOption]; also styles [CalendarEventsBlock] now. */
    appLabelColorOption: ClockColorOption = ClockColorOption.THEME,
    onEventClick: (CalendarEvent) -> Unit = {},
    launcherFontOption: LauncherFontOption = LauncherFontOption.SYSTEM,
    /** Settings → Clock & Calendar Style → "Clock alignment" — aligns the time/date content itself, not just this block's own position within its container (that's the caller's job, e.g. [HomeScreen]/[com.facetlauncher.app.ui.facets.FacetCarouselScreen]). Also positions [CalendarEventsBlock] now, which no longer has an alignment of its own (see chat history). */
    clockAlignment: ClockAlignment = ClockAlignment.LEFT,
    /** Home clock's scale factor — see [com.facetlauncher.app.data.model.LauncherSettings.clockScale]. */
    clockScale: Float = 0.8f,
    /** Millis since epoch of the system's next alarm, or `null` when none is set — see [com.facetlauncher.app.domain.ObserveClockAccessoriesUseCase]. */
    nextAlarmMillis: Long? = null,
    /** `null` until the first real battery reading arrives. */
    batteryPercent: Int? = null,
    isCharging: Boolean = false,
    /**
     * PRD F15 — non-null when the active facet has a hosted third-party `AppWidget` bound as its
     * clock, replacing every param above (`templateId`, `fontOption`, `clockScale`, ...) with that
     * widget's own opaque rendering. [createClockWidgetHostView]/[clockWidgetWidthDp]/
     * [clockWidgetHeightDp] are only read when this is non-null.
     */
    clockWidgetAppWidgetId: Int? = null,
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
    onClockWidgetSizeChange: (appWidgetId: Int, widthDp: Int, heightDp: Int) -> Unit = { _, _, _ -> },
    /** Fired on a real long-press on the hosted widget's own surface — see [ClockWidgetTouchGate]'s own doc for why this can't be [clockContentModifier]'s Compose-level gesture, unlike the native clock. */
    onClockWidgetLongPress: () -> Unit = {},
    /** PRD F15's nested-scroll handoff — see [ClockWidgetTile]'s own doc; a swipe starting on the widget hands off to Home's own swipe axis only once the widget's own scrollable content genuinely has nothing left to do with it. */
    onClockWidgetNestedScrollStart: () -> Unit = {},
    onClockWidgetLeftoverScroll: (dxPx: Float, dyPx: Float) -> Unit = { _, _ -> },
    onClockWidgetNestedScrollStop: () -> Unit = {},
    onClockWidgetLeftoverFling: (velocityXPx: Float, velocityYPx: Float) -> Unit = { _, _ -> },
) {
    Column(modifier = modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Box(
            modifier = Modifier
                .align(clockAlignment.resolve())
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
                nextAlarmMillis = nextAlarmMillis,
                batteryPercent = batteryPercent,
                isCharging = isCharging,
                clockWidgetAppWidgetId = clockWidgetAppWidgetId,
                createClockWidgetHostView = createClockWidgetHostView,
                clockWidgetWidthDp = clockWidgetWidthDp,
                clockWidgetHeightDp = clockWidgetHeightDp,
                clockWidgetCommittedWidthDp = clockWidgetCommittedWidthDp,
                clockWidgetCommittedHeightDp = clockWidgetCommittedHeightDp,
                onClockWidgetSizeChange = onClockWidgetSizeChange,
                onClockWidgetLongPress = onClockWidgetLongPress,
                onClockWidgetNestedScrollStart = onClockWidgetNestedScrollStart,
                onClockWidgetLeftoverScroll = onClockWidgetLeftoverScroll,
                onClockWidgetNestedScrollStop = onClockWidgetNestedScrollStop,
                onClockWidgetLeftoverFling = onClockWidgetLeftoverFling,
                clockContentModifier = clockContentModifier,
            )
        }
        CalendarEventsBlock(
            events = events,
            clock = clock,
            locale = locale,
            use24HourTime = use24HourTime,
            calendarColors = calendarColors,
            fontFamily = launcherFontOption.fontFamily,
            textColor = appLabelColorOption.resolve(),
            fontWeight = homeAppsFontWeight.resolve(),
            alignment = clockAlignment,
            onEventClick = onEventClick,
        )
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
    nextAlarmMillis: Long?,
    batteryPercent: Int?,
    isCharging: Boolean,
    clockWidgetAppWidgetId: Int?,
    createClockWidgetHostView: (Context, Int) -> AppWidgetHostView?,
    clockWidgetWidthDp: Dp?,
    clockWidgetHeightDp: Dp?,
    clockWidgetCommittedWidthDp: Dp?,
    clockWidgetCommittedHeightDp: Dp?,
    onClockWidgetSizeChange: (appWidgetId: Int, widthDp: Int, heightDp: Int) -> Unit,
    onClockWidgetLongPress: () -> Unit,
    onClockWidgetNestedScrollStart: () -> Unit,
    onClockWidgetLeftoverScroll: (dxPx: Float, dyPx: Float) -> Unit,
    onClockWidgetNestedScrollStop: () -> Unit,
    onClockWidgetLeftoverFling: (velocityXPx: Float, velocityYPx: Float) -> Unit,
    clockContentModifier: Modifier,
) {
    if (clockWidgetAppWidgetId != null && clockWidgetWidthDp != null && clockWidgetHeightDp != null) {
        ClockWidgetContent(
            appWidgetId = clockWidgetAppWidgetId,
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
        Box(modifier = Modifier.uniformScale(clockScale, clockAlignment)) {
            ClockDisplay(
                templateId = templateId,
                fontOption = fontOption,
                now = now,
                use24HourTime = use24HourTime,
                showMeridiem = showMeridiem,
                textColor = colorOption.resolve(),
                mutedTextColor = colorOption.resolve().copy(alpha = 0.8f),
                accentColor = accentColorOption.resolve(),
                dateStyle = dateStyle,
                modifier = clockContentModifier,
                locale = locale,
                launcherFontOption = launcherFontOption,
                clockAlignment = clockAlignment,
                nextAlarmMillis = nextAlarmMillis,
                batteryPercent = batteryPercent,
                isCharging = isCharging,
            )
        }
    }
}

/** [ClockBlock]'s hosted-widget branch, factored out purely to stay under this codebase's max-composable-length lint rule — see [ClockBlock]'s own doc for the params' real meaning. */
@Composable
private fun ClockWidgetContent(
    appWidgetId: Int,
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
) = this.layout { measurable, _ ->
    val placeable = measurable.measure(Constraints())
    layout(placeable.width, placeable.height) {
        placeable.placeWithLayer(0, 0) {
            scaleX = scale
            scaleY = scale
            transformOrigin = when (alignment) {
                ClockAlignment.LEFT -> TransformOrigin(0f, 1f)
                ClockAlignment.RIGHT -> TransformOrigin(1f, 1f)
                ClockAlignment.CENTER -> TransformOrigin(0.5f, 1f)
            }
        }
    }
}

@Preview(showBackground = true)
@Preview(name = "Dark", showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun ClockBlockPreview() {
    FacetLauncherTheme {
        val fixedClock = Clock.fixed(Instant.parse("2026-08-27T09:05:00Z"), ZoneId.of("UTC"))
        ClockBlock(
            clock = fixedClock,
            events = listOf(
                CalendarEvent(id = 1, calendarId = "1", title = "Team standup", startTimeMillis = fixedClock.millis() + 3_600_000, endTimeMillis = fixedClock.millis() + 5_400_000, isAllDay = false),
                CalendarEvent(id = 2, calendarId = "1", title = "Design review", startTimeMillis = fixedClock.millis() + 7_200_000, endTimeMillis = fixedClock.millis() + 9_000_000, isAllDay = false),
            ),
        )
    }
}
