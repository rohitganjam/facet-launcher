package com.lumenlauncher.app.ui.home.clock

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
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
import androidx.compose.ui.unit.sp
import com.lumenlauncher.app.data.model.CalendarEvent
import com.lumenlauncher.app.ui.home.rememberTickingNow
import com.lumenlauncher.app.ui.theme.Accent
import com.lumenlauncher.app.ui.theme.AccentSwatch
import com.lumenlauncher.app.ui.theme.HomeTextShadow
import com.lumenlauncher.app.ui.theme.Ink
import com.lumenlauncher.app.ui.theme.resolvedColor
import java.time.Clock
import java.time.Instant
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale

/**
 * Today's calendar events (F1), deliberately independent of [com.lumenlauncher.app.ui.home.ClockBlock]/
 * `ClockDisplay` — a font/color/layout change to one never has to touch the other, and either
 * can be dropped onto a screen alone. Own font/color configuration, kept separate from the rest
 * of the app's theme (per the clock/calendar template design brief) — [fontFamily]/[textColor]
 * default to values matching today's shipped look, not read from [com.lumenlauncher.app.ui.theme.LumenTypography]/
 * `Ink` directly, so a caller can override either without touching the app's global type scale.
 *
 * `[2×13px rule] [time] [title]` rows per row, the rule colored by which calendar the event
 * belongs to ([calendarColors], a calendar id -> an [AccentSwatch] enum name — see
 * [com.lumenlauncher.app.data.model.LauncherSettings.calendarColors]), falling back to [Accent]
 * for a calendar with no assigned color yet. [textColor] is applied at full opacity to both the
 * time and the title — deliberately not muted (see chat history): calendar events are all
 * equally relevant information, unlike a decorative label, so there's no "secondary" tier to
 * dim here the way the clock's date line is dimmed relative to its time. Of [events] (today's,
 * already filtered to selected
 * calendars/all-day setting by [com.lumenlauncher.app.data.CalendarRepository]), only the ones
 * still relevant are actually rendered: an all-day event always, a timed one only until its own
 * end time passes.
 *
 * Owns its own live "now" (via [rememberTickingNow]) so it's fully self-contained — it doesn't
 * need a clock composable anywhere on screen to know which events have ended.
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
    onEventClick: (CalendarEvent) -> Unit = {},
) {
    val now by rememberTickingNow(clock)
    val timeFormatter = remember(locale, use24HourTime) {
        DateTimeFormatter.ofPattern(if (use24HourTime) "HH:mm" else "h:mm", locale)
    }
    val nowMillis = now.atZone(clock.zone).toInstant().toEpochMilli()
    val visibleEvents = remember(events, nowMillis) {
        events.filter { it.isAllDay || it.endTimeMillis > nowMillis }
    }
    if (visibleEvents.isEmpty()) return

    Column(
        modifier = modifier.testTag("clock_event_rows").padding(top = 10.dp),
        verticalArrangement = Arrangement.spacedBy(9.dp),
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
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val time = if (event.isAllDay) {
        "All day"
    } else {
        LocalDateTime.ofInstant(Instant.ofEpochMilli(event.startTimeMillis), clock.zone).format(timeFormatter)
    }
    Row(
        modifier = modifier
            .testTag("clock_event_row_${event.id}")
            .clickable(onClick = onClick),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Column(
            modifier = Modifier
                .width(2.dp)
                .height(13.dp)
                .background(barColor),
        ) {}
        Text(
            text = time,
            style = TextStyle(fontFamily = fontFamily, fontWeight = fontWeight, fontSize = 14.5.sp, shadow = HomeTextShadow),
            color = textColor,
            modifier = Modifier.widthIn(min = 48.dp),
        )
        Text(
            text = event.title,
            style = TextStyle(fontFamily = fontFamily, fontWeight = fontWeight, fontSize = 14.5.sp, shadow = HomeTextShadow),
            color = textColor,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}
