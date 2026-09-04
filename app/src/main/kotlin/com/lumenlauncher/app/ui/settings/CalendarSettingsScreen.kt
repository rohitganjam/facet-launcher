package com.lumenlauncher.app.ui.settings

import android.Manifest
import android.content.res.Configuration
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Checkbox
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TriStateCheckbox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.state.ToggleableState
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.lumenlauncher.app.data.model.CalendarEvent
import com.lumenlauncher.app.data.model.CalendarInfo
import com.lumenlauncher.app.data.model.ClockColorOption
import com.lumenlauncher.app.data.model.ClockFontOption
import com.lumenlauncher.app.data.model.LauncherFontOption
import com.lumenlauncher.app.ui.components.BackButton
import com.lumenlauncher.app.ui.components.CardDivider
import com.lumenlauncher.app.ui.components.InheritOverrideCard
import com.lumenlauncher.app.ui.components.dashedBorder
import com.lumenlauncher.app.ui.components.LabeledDropdownRow
import com.lumenlauncher.app.ui.components.SettingsCard
import com.lumenlauncher.app.ui.components.StickyHeaderLayout
import com.lumenlauncher.app.ui.home.clock.CalendarEventsBlock
import com.lumenlauncher.app.ui.theme.Accent
import com.lumenlauncher.app.ui.theme.AccentSwatch
import com.lumenlauncher.app.ui.theme.Ink
import com.lumenlauncher.app.ui.theme.LumenLauncherTheme
import com.lumenlauncher.app.ui.theme.Muted
import com.lumenlauncher.app.ui.theme.Surface
import com.lumenlauncher.app.ui.theme.resolve
import com.lumenlauncher.app.ui.theme.resolveFontFamily
import com.lumenlauncher.app.ui.theme.resolvedColor
import java.time.Clock
import java.time.Instant
import java.time.ZoneId

/**
 * Calendar settings (`4l`). Reached from the global Settings Clock card (no profile scope) or a
 * profile's Clock card (scoped, with an inherit/override header per `3f`'s pattern). Real
 * `READ_CALENDAR` runtime request + a live `CalendarContract` query back the "Calendars to
 * display" card once granted.
 */
@Composable
fun CalendarSettingsScreen(
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: CalendarSettingsViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    // READ_CALENDAR has no grant-change callback — re-check whenever the user returns to this
    // screen (e.g. from the system permission dialog, or having granted it via App info).
    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) viewModel.refreshCalendarAccess()
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    val requestPermission = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) {
        viewModel.refreshCalendarAccess()
    }

    CalendarSettingsContent(
        uiState = uiState,
        onBack = onBack,
        onShowAllDayEventsChanged = viewModel::setShowAllDayEvents,
        onOverridingChanged = viewModel::setOverriding,
        onTurnOnClick = { requestPermission.launch(Manifest.permission.READ_CALENDAR) },
        onCalendarToggled = viewModel::setCalendarSelected,
        onSelectAllCalendarsChanged = viewModel::setAllCalendarsSelected,
        onCalendarFontOptionChanged = viewModel::setCalendarFontOption,
        onCalendarColorOptionChanged = viewModel::setCalendarColorOption,
        modifier = modifier,
    )
}

@Composable
private fun CalendarSettingsContent(
    uiState: CalendarSettingsUiState,
    onBack: () -> Unit,
    onShowAllDayEventsChanged: (Boolean) -> Unit,
    onOverridingChanged: (Boolean) -> Unit,
    onTurnOnClick: () -> Unit,
    onCalendarToggled: (String, Boolean) -> Unit,
    onSelectAllCalendarsChanged: (Boolean) -> Unit,
    onCalendarFontOptionChanged: (ClockFontOption) -> Unit,
    onCalendarColorOptionChanged: (ClockColorOption) -> Unit,
    modifier: Modifier = Modifier,
) {
    val calendarFontOption = uiState.effectiveCalendarFontOption
    val calendarColorOption = uiState.effectiveCalendarColorOption

    // Every control below (all-day events, font, color) is read-only under Inherit and live under Override.
    val controlsEnabled = !uiState.isProfileScoped || uiState.isOverriding

    StickyHeaderLayout(
        modifier = modifier,
        header = { CalendarSettingsHeader(onBack = onBack) },
        content = { headerHeight ->
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Surface)
                    .testTag("calendar_settings_screen")
                    .windowInsetsPadding(WindowInsets.systemBars)
                    .padding(horizontal = 24.dp),
                contentPadding = PaddingValues(top = headerHeight),
            ) {
                if (uiState.isProfileScoped) {
                    item {
                        InheritOverrideCard(
                            overriding = uiState.isOverriding,
                            onOverridingChanged = onOverridingChanged,
                            testTagPrefix = "calendar",
                            inheritSubtitle = "Follows the launcher-wide Calendar setting",
                        )
                    }
                    item { Spacer(modifier = Modifier.height(16.dp)) }
                }

                item {
                    SettingsCard {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 13.dp)
                                .alpha(if (controlsEnabled) 1f else 0.4f),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Text(text = "Show all-day events", style = MaterialTheme.typography.bodyLarge, color = Ink)
                            Switch(
                                checked = uiState.effectiveShowAllDayEvents,
                                onCheckedChange = onShowAllDayEventsChanged,
                                enabled = controlsEnabled,
                                modifier = Modifier.testTag("show_all_day_events_toggle"),
                            )
                        }
                    }
                }

                item { Spacer(modifier = Modifier.height(16.dp)) }

                item {
                    SettingsCard {
                        Column(modifier = Modifier.padding(vertical = 13.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(bottom = 10.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Text(text = "Calendars to display", style = MaterialTheme.typography.bodyLarge, color = Ink)
                                // Selects/deselects every calendar in one tap instead of one-by-one — real
                                // pain with a lot of calendars (see chat history). Indeterminate when only
                                // some are selected, matching the standard "select all" checkbox convention.
                                if (uiState.isCalendarAccessGranted && uiState.calendars.isNotEmpty()) {
                                    val selectedCount = uiState.calendars.count { uiState.isCalendarSelected(it.id) }
                                    val allState = when (selectedCount) {
                                        uiState.calendars.size -> ToggleableState.On
                                        0 -> ToggleableState.Off
                                        else -> ToggleableState.Indeterminate
                                    }
                                    TriStateCheckbox(
                                        state = allState,
                                        onClick = { onSelectAllCalendarsChanged(allState != ToggleableState.On) },
                                        modifier = Modifier.testTag("calendar_select_all_checkbox"),
                                    )
                                }
                            }
                            if (uiState.isCalendarAccessGranted) {
                                if (uiState.calendars.isEmpty()) {
                                    Text(text = "No calendars found on this device.", style = MaterialTheme.typography.bodyMedium, color = Muted)
                                } else {
                                    uiState.calendars.forEach { calendar ->
                                        CalendarPickerRow(
                                            calendar = calendar,
                                            swatch = uiState.calendarColors[calendar.id]?.let { runCatching { AccentSwatch.valueOf(it) }.getOrNull() },
                                            checked = uiState.isCalendarSelected(calendar.id),
                                            onToggle = { onCalendarToggled(calendar.id, !uiState.isCalendarSelected(calendar.id)) },
                                        )
                                    }
                                }
                            } else {
                                PermissionDeniedStrip(
                                    message = "Calendar access off — events hidden.",
                                    actionLabel = "Turn on",
                                    testTag = "calendar_permission_strip",
                                    onClick = onTurnOnClick,
                                )
                            }
                        }
                    }
                }

                item { Spacer(modifier = Modifier.height(16.dp)) }

                item {
                    SettingsCard {
                        LabeledDropdownRow(
                            title = "Font",
                            options = ClockFontOption.entries,
                            selected = calendarFontOption,
                            label = { it.displayName },
                            onSelect = onCalendarFontOptionChanged,
                            enabled = controlsEnabled,
                            testTag = "calendar_style_font_row",
                        )
                        CardDivider()
                        LabeledDropdownRow(
                            title = "Color",
                            options = ClockColorOption.entries,
                            selected = calendarColorOption,
                            label = { it.displayName },
                            onSelect = onCalendarColorOptionChanged,
                            enabled = controlsEnabled,
                            testTag = "calendar_style_color_row",
                        )
                    }
                }

                item { Spacer(modifier = Modifier.height(12.dp)) }

                item {
                    CalendarEventsBlock(
                        events = calendarPreviewEvents,
                        clock = calendarPreviewClock,
                        fontFamily = calendarFontOption.resolveFontFamily(uiState.launcherFontOption),
                        textColor = calendarColorOption.resolve(),
                        modifier = Modifier.padding(bottom = 16.dp),
                    )
                }
            }
        },
    )
}

@Composable
private fun CalendarSettingsHeader(onBack: () -> Unit, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(Surface)
            .windowInsetsPadding(WindowInsets.systemBars.only(WindowInsetsSides.Top))
            .padding(horizontal = 24.dp)
            .padding(top = 24.dp, bottom = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        BackButton(onClick = onBack)
        Text(text = "Calendar settings", style = MaterialTheme.typography.headlineSmall, color = Ink)
    }
}

private val calendarPreviewClock: Clock = Clock.fixed(Instant.parse("2026-08-27T21:05:00Z"), ZoneId.of("UTC"))
private val calendarPreviewEvents: List<CalendarEvent> = listOf(
    CalendarEvent(id = 1, calendarId = "1", title = "Team standup", startTimeMillis = calendarPreviewClock.millis() + 3_600_000, endTimeMillis = calendarPreviewClock.millis() + 5_400_000, isAllDay = false),
    CalendarEvent(id = 2, calendarId = "1", title = "Design review", startTimeMillis = calendarPreviewClock.millis() + 7_200_000, endTimeMillis = calendarPreviewClock.millis() + 9_000_000, isAllDay = false),
)

@Composable
private fun CalendarPickerRow(calendar: CalendarInfo, swatch: AccentSwatch?, checked: Boolean, onToggle: () -> Unit, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onToggle)
            .padding(vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        if (swatch != null) {
            Box(
                modifier = Modifier
                    .size(12.dp)
                    .clip(CircleShape)
                    .background(swatch.resolvedColor())
                    .semantics { contentDescription = swatch.label },
            )
        }
        Column(modifier = Modifier.weight(1f)) {
            Text(text = calendar.displayName, style = MaterialTheme.typography.bodyLarge, color = Ink)
            Text(text = calendar.accountName, style = MaterialTheme.typography.bodyMedium, color = Muted)
        }
        Checkbox(
            checked = checked,
            onCheckedChange = { onToggle() },
            modifier = Modifier.testTag("calendar_picker_row_${calendar.id}"),
        )
    }
}

/** README `4p`'s shared permission-denied/empty-state pattern — reused verbatim styling, with a real tap target. */
@Composable
private fun PermissionDeniedStrip(message: String, actionLabel: String, testTag: String, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .testTag(testTag)
            .dashedBorder(color = Ink.copy(alpha = 0.16f), cornerRadius = 10.dp)
            .clickable(onClick = onClick)
            .padding(horizontal = 13.dp, vertical = 11.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(text = message, style = MaterialTheme.typography.bodyMedium, color = Muted, modifier = Modifier.weight(1f))
        Text(text = actionLabel, style = MaterialTheme.typography.bodyMedium, color = Accent)
    }
}

@Preview(showBackground = true, widthDp = 390, heightDp = 844)
@Preview(name = "Dark", showBackground = true, widthDp = 390, heightDp = 844, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun CalendarSettingsScreenPreview() {
    LumenLauncherTheme {
        CalendarSettingsContent(
            uiState = CalendarSettingsUiState(),
            onBack = {},
            onShowAllDayEventsChanged = {},
            onOverridingChanged = {},
            onTurnOnClick = {},
            onCalendarToggled = { _, _ -> },
            onSelectAllCalendarsChanged = {},
            onCalendarFontOptionChanged = {},
            onCalendarColorOptionChanged = {},
        )
    }
}
