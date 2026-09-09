package com.lumenlauncher.app.ui.home

import android.content.res.Configuration
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.layout.layout
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.dp
import com.lumenlauncher.app.data.model.CalendarEvent
import com.lumenlauncher.app.data.model.ClockAlignment
import com.lumenlauncher.app.data.model.ClockColorOption
import com.lumenlauncher.app.data.model.ClockDateStyle
import com.lumenlauncher.app.data.model.ClockFontOption
import com.lumenlauncher.app.data.model.ClockTemplateId
import com.lumenlauncher.app.data.model.FontWeightOption
import com.lumenlauncher.app.data.model.LauncherFontOption
import com.lumenlauncher.app.ui.home.clock.CalendarEventsBlock
import com.lumenlauncher.app.ui.home.clock.ClockDisplay
import com.lumenlauncher.app.ui.theme.LumenLauncherTheme
import com.lumenlauncher.app.ui.theme.resolve
import com.lumenlauncher.app.ui.theme.resolveFontFamily
import java.time.Clock
import java.time.Instant
import java.time.ZoneId
import java.util.Locale

/**
 * Home clock — real, persisted [templateId]/[fontOption]/[colorOption]/[showMeridiem] (Settings'
 * Clock card → the clock style gallery) stacked above [CalendarEventsBlock] (its own persisted
 * [calendarFontOption]/[calendarColorOption], independent of the clock's). Every default here
 * matches the app's original shipped look (Light stack, system font, Ink) so nothing changes for
 * a fresh install before any setting is ever touched.
 */
@Composable
fun ClockBlock(
    modifier: Modifier = Modifier,
    /**
     * Applied to [ClockDisplay]'s own rendered content only — e.g. a tap/long-press hit box or
     * `testTag` that must hug just the time/date, not [CalendarEventsBlock] beneath it or the
     * empty space beside it. Kept separate from [modifier] (which sizes/positions the WHOLE
     * clock+calendar block, since this stays [androidx.compose.foundation.layout.fillMaxWidth] so
     * [CalendarEventsBlock]'s own independent [calendarAlignment] keeps resolving against the
     * true content width) — see [com.lumenlauncher.app.ui.home.HomeScreen]'s own call site for why.
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
    /** See [com.lumenlauncher.app.data.model.LauncherSettings.clockAccentColorOption] — only [com.lumenlauncher.app.data.model.usesAccentColor] templates read this. */
    accentColorOption: ClockColorOption = ClockColorOption.ACCENT_PRIMARY,
    showMeridiem: Boolean = false,
    dateStyle: ClockDateStyle = ClockDateStyle.FULL,
    events: List<CalendarEvent> = emptyList(),
    calendarColors: Map<String, String> = emptyMap(),
    calendarFontOption: ClockFontOption = ClockFontOption.SYSTEM,
    calendarColorOption: ClockColorOption = ClockColorOption.THEME,
    calendarFontWeight: FontWeightOption = FontWeightOption.REGULAR,
    onEventClick: (CalendarEvent) -> Unit = {},
    launcherFontOption: LauncherFontOption = LauncherFontOption.SYSTEM,
    /** Settings → Clock & Calendar Style → "Clock alignment" — aligns the time/date content itself, not just this block's own position within its container (that's the caller's job, e.g. [HomeScreen]/[com.lumenlauncher.app.ui.profiles.ProfileCarouselScreen]). */
    clockAlignment: ClockAlignment = ClockAlignment.LEFT,
    /** Settings → Clock & Calendar Style → "Calendar alignment" — entirely independent of [clockAlignment]; positions [CalendarEventsBlock] (and reverses its row order at [ClockAlignment.RIGHT]) without moving the clock. */
    calendarAlignment: ClockAlignment = ClockAlignment.LEFT,
    /** Home clock's scale factor — see [com.lumenlauncher.app.data.model.LauncherSettings.clockScale]. */
    clockScale: Float = 0.8f,
) {
    val now by rememberTickingNow(clock)
    val clockTextColor = colorOption.resolve()
    val clockAccentColor = accentColorOption.resolve()
    Column(modifier = modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Box(
            modifier = Modifier
                .align(clockAlignment.resolve())
                .then(clockBoxModifier),
        ) {
            Box(modifier = Modifier.uniformScale(clockScale, clockAlignment)) {
                ClockDisplay(
                    templateId = templateId,
                    fontOption = fontOption,
                    now = now,
                    use24HourTime = use24HourTime,
                    showMeridiem = showMeridiem,
                    textColor = clockTextColor,
                    mutedTextColor = clockTextColor.copy(alpha = 0.8f),
                    accentColor = clockAccentColor,
                    dateStyle = dateStyle,
                    modifier = clockContentModifier,
                    locale = locale,
                    launcherFontOption = launcherFontOption,
                    clockAlignment = clockAlignment,
                )
            }
        }
        CalendarEventsBlock(
            events = events,
            clock = clock,
            locale = locale,
            use24HourTime = use24HourTime,
            calendarColors = calendarColors,
            fontFamily = calendarFontOption.resolveFontFamily(launcherFontOption),
            textColor = calendarColorOption.resolve(),
            fontWeight = calendarFontWeight.resolve(),
            alignment = calendarAlignment,
            onEventClick = onEventClick,
        )
    }
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
    LumenLauncherTheme {
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
