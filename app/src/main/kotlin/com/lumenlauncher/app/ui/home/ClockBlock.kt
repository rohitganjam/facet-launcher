package com.lumenlauncher.app.ui.home

import android.content.res.Configuration
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.lumenlauncher.app.data.model.CalendarEvent
import com.lumenlauncher.app.data.model.ClockColorOption
import com.lumenlauncher.app.data.model.ClockFontOption
import com.lumenlauncher.app.data.model.ClockTemplateId
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
    clock: Clock = Clock.systemDefaultZone(),
    locale: Locale = Locale.getDefault(),
    use24HourTime: Boolean = false,
    templateId: ClockTemplateId = ClockTemplateId.LIGHT_STACK,
    fontOption: ClockFontOption = ClockFontOption.SYSTEM,
    colorOption: ClockColorOption = ClockColorOption.INK,
    showMeridiem: Boolean = false,
    events: List<CalendarEvent> = emptyList(),
    calendarColors: Map<String, String> = emptyMap(),
    calendarFontOption: ClockFontOption = ClockFontOption.SYSTEM,
    calendarColorOption: ClockColorOption = ClockColorOption.INK,
    onEventClick: (CalendarEvent) -> Unit = {},
    launcherFontOption: LauncherFontOption = LauncherFontOption.SYSTEM,
) {
    val now by rememberTickingNow(clock)
    val clockTextColor = colorOption.resolve()
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(10.dp)) {
        ClockDisplay(
            templateId = templateId,
            fontOption = fontOption,
            now = now,
            use24HourTime = use24HourTime,
            showMeridiem = showMeridiem,
            textColor = clockTextColor,
            mutedTextColor = clockTextColor.copy(alpha = 0.8f),
            locale = locale,
            launcherFontOption = launcherFontOption,
        )
        CalendarEventsBlock(
            events = events,
            clock = clock,
            locale = locale,
            use24HourTime = use24HourTime,
            calendarColors = calendarColors,
            fontFamily = calendarFontOption.resolveFontFamily(launcherFontOption),
            textColor = calendarColorOption.resolve(),
            onEventClick = onEventClick,
        )
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
