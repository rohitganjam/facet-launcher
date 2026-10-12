package com.facetlauncher.app.ui.settings

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.facetlauncher.app.R
import com.facetlauncher.app.data.model.CalendarEvent
import com.facetlauncher.app.ui.components.WallpaperBackground
import com.facetlauncher.app.ui.home.clock.CalendarEventsBlock
import com.facetlauncher.app.ui.theme.Hairline
import com.facetlauncher.app.ui.theme.fontFamily
import com.facetlauncher.app.ui.theme.resolve
import java.time.Clock

private const val HOUR_MILLIS = 3_600_000L

/**
 * A short window onto the wallpaper (same crop as Home's top) with the calendar block drawn on it
 * using the scope's real font/colour/alignment and the display options being edited. Events are
 * samples, so the item count and indicator are always demonstrable; the bar colours follow the
 * device's actual calendars when they're loaded.
 */
@Composable
internal fun CalendarPreviewStrip(uiState: CalendarSettingsUiState, modifier: Modifier = Modifier) {
    val settings = uiState.settings
    val titles = listOf(
        stringResource(R.string.calendar_settings_preview_event_1),
        stringResource(R.string.calendar_settings_preview_event_2),
        stringResource(R.string.calendar_settings_preview_event_3),
    )
    val calendarIds = uiState.calendars.map { it.id }
    val events = remember(titles, calendarIds) {
        val now = System.currentTimeMillis()
        titles.mapIndexed { index, title ->
            CalendarEvent(
                id = index + 1L,
                calendarId = calendarIds.getOrNull(index) ?: "sample-$index",
                title = title,
                startTimeMillis = now + (index + 1) * HOUR_MILLIS,
                endTimeMillis = now + (index + 2) * HOUR_MILLIS,
                isAllDay = false,
            )
        }
    }
    val shape = MaterialTheme.shapes.extraLarge
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(140.dp)
            .clip(shape)
            .border(width = 1.dp, color = Hairline, shape = shape)
            .testTag("calendar_preview_strip"),
    ) {
        WallpaperBackground(uiState.homeWallpaper, Modifier.matchParentSize(), alignment = Alignment.TopCenter)
        CalendarEventsBlock(
            events = events,
            modifier = Modifier.padding(horizontal = 18.dp),
            clock = Clock.systemDefaultZone(),
            use24HourTime = uiState.previewUse24HourTime,
            calendarColors = settings.calendarColors,
            fontFamily = settings.launcherFontOption.fontFamily,
            fontWeight = settings.homeAppsFontWeight.resolve(),
            textColor = settings.appLabelColorOption.resolve(),
            alignment = uiState.previewClockAlignment,
            display = uiState.calendarDisplay,
        )
    }
}
