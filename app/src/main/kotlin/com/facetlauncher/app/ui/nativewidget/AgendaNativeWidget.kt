package com.facetlauncher.app.ui.nativewidget

import android.content.res.Configuration
import android.text.format.DateUtils
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import com.facetlauncher.app.R
import com.facetlauncher.app.data.model.CalendarEvent
import com.facetlauncher.app.data.model.NativeWidgetType
import com.facetlauncher.app.domain.WidgetTier
import com.facetlauncher.app.ui.home.HomeCalendarState
import com.facetlauncher.app.ui.home.clock.eventTimeLabel
import com.facetlauncher.app.ui.home.rememberTickingNow
import com.facetlauncher.app.ui.theme.Accent
import com.facetlauncher.app.ui.theme.AccentSwatch
import com.facetlauncher.app.ui.theme.FacetLauncherTheme
import com.facetlauncher.app.ui.theme.Ink
import com.facetlauncher.app.ui.theme.Muted
import com.facetlauncher.app.ui.theme.resolvedColor
import java.time.Clock
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.util.Locale
import kotlinx.collections.immutable.persistentListOf

private val ROW_HEIGHT = 40.dp
private const val MAX_EVENTS = 20
private val HEADER_HEIGHT = 22.dp
private val DAY_HEADER_HEIGHT = 22.dp
private val SMALL_ROW_HEIGHT = 28.dp
private val SMALL_VERTICAL_PADDING = 4.dp

/** One line of the list: a day's heading, or an event under it. */
private sealed interface AgendaLine {
    val key: String

    data class Day(val date: LocalDate, val millis: Long) : AgendaLine {
        override val key get() = "day_$date"
    }

    data class Item(val event: CalendarEvent) : AgendaLine {
        override val key get() = "event_${event.id}_${event.startTimeMillis}" // a repeating event shares its id
    }
}

/** Upcoming events: as many one-line rows as fit when Small, otherwise a scrolling list of up to 20 under the widget heading. */
@Composable
internal fun AgendaNativeWidget(tier: WidgetTier, calendar: HomeCalendarState, onOpenEvent: (CalendarEvent) -> Unit, modifier: Modifier = Modifier) {
    val clock = remember { Clock.systemDefaultZone() }
    val now by rememberTickingNow(clock)
    val nowMillis = now.atZone(clock.zone).toInstant().toEpochMilli()
    val upcoming = remember(calendar.events, nowMillis) { calendar.events.filter { it.isAllDay || it.endTimeMillis > nowMillis } }
    val locale = Locale.getDefault()
    val formatter = remember(locale) { DateTimeFormatter.ofPattern("h:mm", locale) }
    val showRoom = tier != WidgetTier.SMALL
    val zone = clock.zone

    BoxWithConstraints(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.CenterStart) {
        val smallFit = ((maxHeight - SMALL_VERTICAL_PADDING * 2) / SMALL_ROW_HEIGHT).toInt().coerceAtLeast(1)
        val lines = remember(upcoming, tier, zone, smallFit) { upcoming.toLines(zone, showHeadings = showRoom, limit = if (showRoom) MAX_EVENTS else smallFit) }
        if (upcoming.isEmpty()) {
            Text(stringResource(R.string.native_widget_agenda_empty), style = MaterialTheme.typography.bodyMedium, color = Muted, maxLines = 2, overflow = TextOverflow.Ellipsis)
        } else {
            Column(modifier = Modifier.fillMaxSize()) {
                if (showRoom) {
                    Text(
                        stringResource(R.string.native_widget_agenda), style = MaterialTheme.typography.labelLarge, color = Accent, maxLines = 1,
                        modifier = Modifier.height(HEADER_HEIGHT).testTag("agenda_header"),
                    )
                }
                val row: @Composable (AgendaLine) -> Unit = { line ->
                    when (line) {
                        is AgendaLine.Day -> Text(
                            dayLabel(line.millis, nowMillis), style = MaterialTheme.typography.labelMedium, color = Muted, maxLines = 1,
                            modifier = Modifier.height(DAY_HEADER_HEIGHT).testTag("agenda_day_${line.date}"),
                        )
                        is AgendaLine.Item -> {
                            val event = line.event
                            val color = calendar.colors[event.calendarId]?.let { runCatching { AccentSwatch.valueOf(it) }.getOrNull() }?.resolvedColor() ?: Accent
                            EventRow(event, color, eventTimeLabel(event, formatter, clock, locale), room = event.roomLabel.takeIf { showRoom }.orEmpty(), large = tier == WidgetTier.LARGE, timeFirst = !showRoom, onClick = { onOpenEvent(event) })
                        }
                    }
                }
                if (showRoom) {
                    LazyColumn(modifier = Modifier.weight(1f).fillMaxWidth().testTag("agenda_list"), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                        items(lines, key = { it.key }) { row(it) }
                    }
                } else {
                    Column(modifier = Modifier.weight(1f).fillMaxWidth().padding(vertical = SMALL_VERTICAL_PADDING)) { lines.forEach { row(it) } }
                }
            }
        }
    }
}

@Composable
private fun EventRow(event: CalendarEvent, barColor: Color, time: String, room: String, large: Boolean, timeFirst: Boolean, onClick: () -> Unit) {
    Row(
        modifier = Modifier.heightIn(min = if (timeFirst) SMALL_ROW_HEIGHT else ROW_HEIGHT - 6.dp).clickable(onClick = onClick),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Box(modifier = Modifier.width(3.dp).height(if (large) 28.dp else 22.dp).background(barColor, MaterialTheme.shapes.extraSmall))
        if (timeFirst) {
            // Small: one line, time then title, like the clock's own events list
            Text(time, style = MaterialTheme.typography.bodyMedium, color = Muted, maxLines = 1, modifier = Modifier.widthIn(min = 40.dp).testTag("agenda_time"))
            Text(event.title, style = MaterialTheme.typography.bodyMedium, color = Ink, maxLines = 1, overflow = TextOverflow.Ellipsis, modifier = Modifier.weight(1f))
            return@Row
        }
        Column(modifier = Modifier.weight(1f)) {
            Text(event.title, style = if (large) MaterialTheme.typography.titleSmall else MaterialTheme.typography.bodyMedium, color = Ink, maxLines = 1, overflow = TextOverflow.Ellipsis)
            if (room.isNotEmpty()) Text(room, style = MaterialTheme.typography.labelMedium, color = Muted, maxLines = 1, overflow = TextOverflow.Ellipsis)
            Text(time, style = MaterialTheme.typography.labelMedium, color = Muted, maxLines = 1)
        }
    }
}

/** "Today", "Tomorrow", or the date: the system's own wording for the day, so it is localised. */
private fun dayLabel(eventMillis: Long, nowMillis: Long): String =
    DateUtils.getRelativeTimeSpanString(eventMillis, nowMillis, DateUtils.DAY_IN_MILLIS, DateUtils.FORMAT_ABBREV_RELATIVE or DateUtils.FORMAT_SHOW_DATE).toString()

/** The first [limit] events as lines: with a heading per day, or without headings (Small). */
private fun List<CalendarEvent>.toLines(zone: ZoneId, showHeadings: Boolean, limit: Int): List<AgendaLine> {
    val lines = mutableListOf<AgendaLine>()
    var day: LocalDate? = null
    for (event in take(limit)) {
        val date = if (event.isAllDay) Instant.ofEpochMilli(event.startTimeMillis).atZone(ZoneOffset.UTC).toLocalDate() else Instant.ofEpochMilli(event.startTimeMillis).atZone(zone).toLocalDate()
        if (showHeadings && date != day) lines += AgendaLine.Day(date, event.startTimeMillis)
        day = date
        lines += AgendaLine.Item(event)
    }
    return lines
}

internal fun previewCalendarState(): HomeCalendarState {
    val base = 4_000_000_000_000L // far ahead, so the sample events never count as ended
    fun event(id: Long, title: String, offsetMin: Long, location: String = "") =
        CalendarEvent(id, "1", title, base + offsetMin * 60_000, base + (offsetMin + 45) * 60_000, isAllDay = false, location = location)
    return HomeCalendarState(
        events = persistentListOf(
            event(1, "Design review", 30, "BLR-Innovate-2-Marie Curie (4)"), event(2, "Lunch with Sam", 150), event(3, "Dentist", 300),
            event(4, "Team sync", 420, "Room 4B"), event(5, "Gym", 600), event(6, "Dinner", 1500),
        ),
    )
}

@Preview(name = "Small", showBackground = true, widthDp = 148, heightDp = 72)
@Preview(name = "Small dark", showBackground = true, widthDp = 148, heightDp = 72, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun AgendaSmallPreview() = FacetLauncherTheme { NativeWidgetPreviewBox(NativeWidgetType.AGENDA, 148, 72) }

@Preview(name = "Medium", showBackground = true, widthDp = 224, heightDp = 148)
@Preview(name = "Medium dark", showBackground = true, widthDp = 224, heightDp = 148, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun AgendaMediumPreview() = FacetLauncherTheme { NativeWidgetPreviewBox(NativeWidgetType.AGENDA, 224, 148) }

@Preview(name = "Large", showBackground = true, widthDp = 300, heightDp = 300)
@Preview(name = "Large dark", showBackground = true, widthDp = 300, heightDp = 300, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun AgendaLargePreview() = FacetLauncherTheme { NativeWidgetPreviewBox(NativeWidgetType.AGENDA, 300, 300) }
