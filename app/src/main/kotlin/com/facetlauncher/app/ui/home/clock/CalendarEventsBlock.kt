package com.facetlauncher.app.ui.home.clock

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.facetlauncher.app.data.model.CalendarEvent
import com.facetlauncher.app.data.model.ClockAlignment
import com.facetlauncher.app.ui.home.rememberTickingNow
import com.facetlauncher.app.ui.theme.Accent
import com.facetlauncher.app.ui.theme.AccentSwatch
import com.facetlauncher.app.ui.theme.homeTextShadow
import com.facetlauncher.app.ui.theme.Ink
import com.facetlauncher.app.ui.theme.SMALL_TEXT_SHADOW_BLUR_RADIUS
import com.facetlauncher.app.ui.theme.resolve
import com.facetlauncher.app.ui.theme.resolvedColor
import java.time.Clock
import java.time.Instant
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale

/** The clock block shows only the next few events, not the whole day's agenda. */
private const val MAX_VISIBLE_EVENTS = 3

/**
 * Today's calendar events (F1), deliberately independent of [com.facetlauncher.app.ui.home.ClockBlock]/
 * `ClockDisplay` — a font/color/layout change to one never has to touch the other, and either
 * can be dropped onto a screen alone. Own font/color configuration, kept separate from the rest
 * of the app's theme (per the clock/calendar template design brief) — [fontFamily]/[textColor]
 * default to values matching today's shipped look, not read from [com.facetlauncher.app.ui.theme.facetTypography]/
 * `Ink` directly, so a caller can override either without touching the app's global type scale.
 *
 * `[2×13px rule] [time] [title]` rows per row, the rule colored by which calendar the event
 * belongs to ([calendarColors], a calendar id -> an [AccentSwatch] enum name — see
 * [com.facetlauncher.app.data.model.LauncherSettings.calendarColors]), falling back to [Accent]
 * for a calendar with no assigned color yet. [textColor] is applied at full opacity to both the
 * time and the title — deliberately not muted (see chat history): calendar events are all
 * equally relevant information, unlike a decorative label, so there's no "secondary" tier to
 * dim here the way the clock's date line is dimmed relative to its time. Of [events] (today's,
 * already filtered to selected
 * calendars/all-day setting by [com.facetlauncher.app.data.CalendarRepository]), only the ones
 * still relevant are actually rendered: an all-day event always, a timed one only until its own
 * end time passes — capped at [MAX_VISIBLE_EVENTS], since this is a glanceable clock-block strip,
 * not a full day agenda.
 *
 * Owns its own live "now" (via [rememberTickingNow]) so it's fully self-contained — it doesn't
 * need a clock composable anywhere on screen to know which events have ended.
 *
 * [alignment] is entirely independent of [com.facetlauncher.app.ui.home.ClockBlock]'s own clock
 * alignment — the clock and this strip are positioned separately (see chat history). `RIGHT`
 * both packs each row against the block's trailing edge and reverses that row's own item order
 * (event name, then time, then the calendar-color indicator, mirroring [AppRowPosition]'s own
 * icon/label reversal for `RIGHT`); `LEFT`/`CENTER` keep today's indicator/time/name order.
 */
@Composable
fun CalendarEventsBlock(
    events: List<CalendarEvent>,
    modifier: Modifier = Modifier,
    clock: Clock = Clock.systemDefaultZone(),
    locale: Locale = Locale.getDefault(),
    use24HourTime: Boolean = false,
    calendarColors: Map<String, String> = emptyMap(),
    fontFamily: FontFamily = FontFamily.SansSerif,
    fontWeight: FontWeight = FontWeight.SemiBold,
    textColor: Color = Ink,
    alignment: ClockAlignment = ClockAlignment.LEFT,
    onEventClick: (CalendarEvent) -> Unit = {},
) {
    val now by rememberTickingNow(clock)
    val timeFormatter = remember(locale, use24HourTime) {
        DateTimeFormatter.ofPattern(if (use24HourTime) "HH:mm" else "h:mm", locale)
    }
    val nowMillis = now.atZone(clock.zone).toInstant().toEpochMilli()
    val visibleEvents = remember(events, nowMillis) {
        events.filter { it.isAllDay || it.endTimeMillis > nowMillis }.take(MAX_VISIBLE_EVENTS)
    }
    if (visibleEvents.isEmpty()) return

    Column(
        modifier = modifier.testTag("clock_event_rows").padding(top = 10.dp).fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(9.dp),
        horizontalAlignment = alignment.resolve(),
    ) {
        visibleEvents.forEach { event ->
            EventRow(
                event = event,
                barColor = calendarColors[event.calendarId]
                    ?.let { runCatching { AccentSwatch.valueOf(it) }.getOrNull() }
                    ?.resolvedColor()
                    ?: Accent,
                timeFormatter = timeFormatter,
                clock = clock,
                fontFamily = fontFamily,
                fontWeight = fontWeight,
                textColor = textColor,
                reversed = alignment == ClockAlignment.RIGHT,
                onClick = { onEventClick(event) },
            )
        }
    }
}

@Composable
private fun EventRow(
    event: CalendarEvent,
    barColor: Color,
    timeFormatter: DateTimeFormatter,
    clock: Clock,
    fontFamily: FontFamily,
    fontWeight: FontWeight,
    textColor: Color,
    reversed: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val time = if (event.isAllDay) {
        "All day"
    } else {
        LocalDateTime.ofInstant(Instant.ofEpochMilli(event.startTimeMillis), clock.zone).format(timeFormatter)
    }
    val indicator: @Composable () -> Unit = {
        Column(
            modifier = Modifier
                .testTag("clock_event_indicator_${event.id}")
                .width(2.dp)
                .height(13.dp)
                .background(barColor),
        ) {}
    }
    val timeText: @Composable () -> Unit = {
        Text(
            text = time,
            style = TextStyle(fontFamily = fontFamily, fontWeight = fontWeight, fontSize = MaterialTheme.typography.bodyLarge.fontSize, shadow = homeTextShadow(textColor, blurRadius = SMALL_TEXT_SHADOW_BLUR_RADIUS)),
            color = textColor,
            modifier = Modifier.widthIn(min = 48.dp),
        )
    }
    val titleText: @Composable () -> Unit = {
        Text(
            text = event.title,
            style = TextStyle(fontFamily = fontFamily, fontWeight = fontWeight, fontSize = MaterialTheme.typography.bodyLarge.fontSize, shadow = homeTextShadow(textColor, blurRadius = SMALL_TEXT_SHADOW_BLUR_RADIUS)),
            color = textColor,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
    Row(
        modifier = modifier
            .testTag("clock_event_row_${event.id}")
            .clickable(onClick = onClick),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        if (reversed) {
            titleText()
            timeText()
            indicator()
        } else {
            indicator()
            timeText()
            titleText()
        }
    }
}
