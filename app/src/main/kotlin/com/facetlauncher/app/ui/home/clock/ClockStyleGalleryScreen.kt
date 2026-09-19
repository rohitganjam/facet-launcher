package com.facetlauncher.app.ui.home.clock

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.facetlauncher.app.R
import com.facetlauncher.app.data.model.CalendarEvent
import com.facetlauncher.app.data.model.ClockAlignment
import com.facetlauncher.app.data.model.ClockColorOption
import com.facetlauncher.app.data.model.ClockDateStyle
import com.facetlauncher.app.data.model.ClockFontOption
import com.facetlauncher.app.data.model.ClockTemplateId
import com.facetlauncher.app.data.model.FontWeightOption
import com.facetlauncher.app.data.model.LauncherFontOption
import com.facetlauncher.app.ui.components.BackButton
import com.facetlauncher.app.ui.components.CardDivider
import com.facetlauncher.app.ui.components.FontWeightSlider
import com.facetlauncher.app.ui.components.LabeledDropdownRow
import com.facetlauncher.app.ui.components.SettingsCard
import com.facetlauncher.app.ui.components.StickyHeaderLayout
import com.facetlauncher.app.ui.theme.Accent
import com.facetlauncher.app.ui.theme.Hairline
import com.facetlauncher.app.ui.theme.Ink
import com.facetlauncher.app.ui.theme.FacetLauncherTheme
import com.facetlauncher.app.ui.theme.Muted
import com.facetlauncher.app.ui.theme.Surface
import com.facetlauncher.app.ui.theme.SurfaceContainer
import com.facetlauncher.app.ui.theme.resolve
import com.facetlauncher.app.ui.theme.resolveFontFamily
import java.time.Clock
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId

/**
 * Global entry point (Settings' "Default clock style" row) — real, persisted, actually applied
 * to Home via [com.facetlauncher.app.ui.home.ClockBlock].
 */
@Composable
fun ClockStyleGalleryRoute(onBack: () -> Unit, viewModel: ClockStyleGalleryViewModel = hiltViewModel()) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    ClockStyleGalleryScreen(
        onBack = onBack,
        templateId = uiState.templateId,
        fontOption = uiState.fontOption,
        colorOption = uiState.colorOption,
        accentColorOption = uiState.accentColorOption,
        use24HourTime = uiState.use24HourTime,
        showMeridiem = uiState.showMeridiem,
        dateStyle = uiState.dateStyle,
        calendarFontOption = uiState.calendarFontOption,
        calendarColorOption = uiState.calendarColorOption,
        calendarFontWeight = uiState.calendarFontWeight,
        launcherFontOption = uiState.launcherFontOption,
        onTemplateSelect = viewModel::setClockTemplateId,
        onFontOptionChange = viewModel::setClockFontOption,
        onColorOptionChange = viewModel::setClockColorOption,
        onAccentColorOptionChange = viewModel::setClockAccentColorOption,
        onUse24HourTimeChange = viewModel::setUse24HourTime,
        onShowMeridiemChange = viewModel::setClockShowMeridiem,
        onDateStyleChange = viewModel::setClockDateStyle,
        onCalendarFontOptionChange = viewModel::setCalendarFontOption,
        onCalendarColorOptionChange = viewModel::setCalendarColorOption,
        onCalendarFontWeightChange = viewModel::setCalendarFontWeight,
        clockAlignment = uiState.clockAlignment,
        calendarAlignment = uiState.calendarAlignment,
        onClockAlignmentChange = viewModel::setClockAlignment,
        onCalendarAlignmentChange = viewModel::setCalendarAlignment,
        onResetClockPosition = viewModel::resetClockPosition,
    )
}

/**
 * Facet-scoped entry point (a facet's Clock card) — now uses a ViewModel to persist changes
 * if the user chooses to override.
 */
@Composable
fun FacetClockStyleGalleryScreen(onBack: () -> Unit, viewModel: ClockStyleGalleryViewModel = hiltViewModel()) {
    ClockStyleGalleryRoute(onBack = onBack, viewModel = viewModel)
}

/**
 * Stateless clock-style picker. Order: Calendar font/color/weight controls plus its own "Calendar
 * alignment" row, then a live [CalendarEventsBlock] preview (which moves live with that alignment,
 * same as every other calendar control here), then Clock font/color/24h/meridiem controls plus its
 * own "Clock alignment" row, then every [ClockTemplateId] as its own selectable card — tapping one
 * calls [onTemplateSelect] and gets an [Accent] border to show it's the applied option. Each
 * alignment lives inside its own block's card (not a separate shared section) since it only ever
 * affects that one block; both alignments and the "Reset clock widget position" action are part of
 * the same Clock+Calendar design bundle as the font/color/template controls around them, so — like
 * those — they're facet-overridable too, not global-only. On [Surface] (the same theme-aware,
 * light/dark-following background every other screen uses).
 */
@Composable
private fun ClockStyleGalleryScreen(
    onBack: () -> Unit,
    templateId: ClockTemplateId,
    fontOption: ClockFontOption,
    colorOption: ClockColorOption,
    use24HourTime: Boolean,
    showMeridiem: Boolean,
    onTemplateSelect: (ClockTemplateId) -> Unit,
    onFontOptionChange: (ClockFontOption) -> Unit,
    onColorOptionChange: (ClockColorOption) -> Unit,
    onUse24HourTimeChange: (Boolean) -> Unit,
    onShowMeridiemChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    /** Always shown; only actually meaningful for templates where [com.facetlauncher.app.data.model.usesAccentColor] is true — inert for every other template's rendering. */
    accentColorOption: ClockColorOption = ClockColorOption.ACCENT_PRIMARY,
    onAccentColorOptionChange: (ClockColorOption) -> Unit = {},
    dateStyle: ClockDateStyle = ClockDateStyle.FULL,
    onDateStyleChange: (ClockDateStyle) -> Unit = {},
    calendarFontOption: ClockFontOption = ClockFontOption.LAUNCHER_DEFAULT,
    calendarColorOption: ClockColorOption = ClockColorOption.THEME,
    calendarFontWeight: FontWeightOption = FontWeightOption.REGULAR,
    onCalendarFontOptionChange: (ClockFontOption) -> Unit = {},
    onCalendarColorOptionChange: (ClockColorOption) -> Unit = {},
    onCalendarFontWeightChange: (FontWeightOption) -> Unit = {},
    launcherFontOption: LauncherFontOption = LauncherFontOption.SYSTEM,
    clockAlignment: ClockAlignment = ClockAlignment.LEFT,
    calendarAlignment: ClockAlignment = ClockAlignment.LEFT,
    onClockAlignmentChange: (ClockAlignment) -> Unit = {},
    onCalendarAlignmentChange: (ClockAlignment) -> Unit = {},
    onResetClockPosition: () -> Unit = {},
) {
    val textColor = colorOption.resolve()
    val accentColor = accentColorOption.resolve()
    val fixedClock = remember { Clock.fixed(Instant.parse("2026-08-27T21:05:00Z"), ZoneId.of("UTC")) }
    val now = remember { LocalDateTime.now(fixedClock) }
    val calendarPreviewClock = remember { Clock.fixed(Instant.parse("2026-08-27T21:05:00Z"), ZoneId.of("UTC")) }
    val calendarPreviewEvents = remember {
        listOf(
            CalendarEvent(id = 1, calendarId = "1", title = "Team standup", startTimeMillis = calendarPreviewClock.millis() + 3_600_000, endTimeMillis = calendarPreviewClock.millis() + 5_400_000, isAllDay = false),
            CalendarEvent(id = 2, calendarId = "1", title = "Design review", startTimeMillis = calendarPreviewClock.millis() + 7_200_000, endTimeMillis = calendarPreviewClock.millis() + 9_000_000, isAllDay = false),
        )
    }
    // Fixed sample accessory values (not the real device battery/alarm) so every card in the
    // gallery shows its "next alarm · battery" row the same way, letting the user compare
    // templates side by side — same reasoning as calendarPreviewEvents above. Computed against
    // the real system zone (not calendarPreviewClock's fixed UTC) since ClockDisplay itself
    // always formats nextAlarmMillis via ZoneId.systemDefault(), matching Home's own behavior.
    val samplePreviewNextAlarmMillis = remember { LocalDate.now().plusDays(1).atTime(7, 0).atZone(ZoneId.systemDefault()).toInstant().toEpochMilli() }
    val samplePreviewBatteryPercent = 64
    val samplePreviewIsCharging = false

    StickyHeaderLayout(
        modifier = modifier,
        header = { ClockStyleGalleryHeader(onBack = onBack) },
        content = { headerHeight ->
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(SurfaceContainer)
            .windowInsetsPadding(WindowInsets.systemBars)
            .testTag("clock_style_gallery_list"),
        contentPadding = PaddingValues(start = 24.dp, end = 24.dp, top = headerHeight + 16.dp, bottom = 16.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp),
    ) {
        item {
            Text(
                text = stringResource(R.string.clock_style_calendar_section),
                style = MaterialTheme.typography.labelSmall,
                color = Muted,
            )
        }
        item {
            SettingsCard {
                LabeledDropdownRow(
                    title = stringResource(R.string.clock_style_font_label),
                    options = ClockFontOption.entries,
                    selected = calendarFontOption,
                    label = { stringResource(it.displayNameRes) },
                    onSelect = onCalendarFontOptionChange,
                    testTag = "calendar_style_font_row",
                )
                CardDivider()
                LabeledDropdownRow(
                    title = stringResource(R.string.clock_style_color_label),
                    options = ClockColorOption.entries,
                    selected = calendarColorOption,
                    label = { stringResource(it.displayNameRes) },
                    onSelect = onCalendarColorOptionChange,
                    testTag = "calendar_style_color_row",
                )
                CardDivider()
                FontWeightSlider(
                    selected = calendarFontWeight,
                    onSelectedChange = onCalendarFontWeightChange,
                    showPreview = false,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 13.dp)
                        .testTag("calendar_style_weight_slider"),
                    sliderTestTag = "calendar_style_weight_slider_control",
                )
                CardDivider()
                LabeledDropdownRow(
                    title = stringResource(R.string.clock_style_calendar_alignment),
                    options = ClockAlignment.entries,
                    selected = calendarAlignment,
                    label = { stringResource(it.displayNameRes) },
                    onSelect = onCalendarAlignmentChange,
                    testTag = "calendar_alignment_row",
                )
            }
        }
        item {
            CalendarEventsBlock(
                events = calendarPreviewEvents,
                clock = calendarPreviewClock,
                fontFamily = calendarFontOption.resolveFontFamily(launcherFontOption),
                textColor = calendarColorOption.resolve(),
                fontWeight = calendarFontWeight.resolve(),
                alignment = calendarAlignment,
                modifier = Modifier.testTag("calendar_style_preview"),
            )
        }

        item {
            HorizontalDivider(color = Hairline, modifier = Modifier.testTag("calendar_clock_section_divider"))
        }

        item {
            Text(
                text = stringResource(R.string.clock_style_clock_section),
                style = MaterialTheme.typography.labelSmall,
                color = Muted,
            )
        }
        item {
            SettingsCard {
                LabeledDropdownRow(
                    title = stringResource(R.string.clock_style_font_label),
                    options = ClockFontOption.entries,
                    selected = fontOption,
                    label = { stringResource(it.displayNameRes) },
                    onSelect = onFontOptionChange,
                    testTag = "clock_font_row",
                )
                CardDivider()
                LabeledDropdownRow(
                    title = stringResource(R.string.clock_style_primary_font_color),
                    options = ClockColorOption.entries,
                    selected = colorOption,
                    label = { stringResource(it.displayNameRes) },
                    onSelect = onColorOptionChange,
                    testTag = "clock_color_row",
                )
                CardDivider()
                // Always visible, even for templates with no accent element in their design —
                // per direct request, so picking one isn't gated behind first scrolling down to
                // an accent-using template and back up (see chat history). It's simply inert
                // (unused) for a non-accent template's rendering, same as e.g. "Show meridiem"
                // stays visible (dimmed) rather than disappearing when 24-hour time is on.
                LabeledDropdownRow(
                    title = stringResource(R.string.appearance_accent_color),
                    options = ClockColorOption.entries,
                    selected = accentColorOption,
                    label = { stringResource(it.displayNameRes) },
                    onSelect = onAccentColorOptionChange,
                    testTag = "clock_accent_color_row",
                )
                CardDivider()
                Row(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 13.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(text = stringResource(R.string.settings_time_format_24h), style = MaterialTheme.typography.bodyLarge, color = Ink)
                    Switch(checked = use24HourTime, onCheckedChange = onUse24HourTimeChange, modifier = Modifier.testTag("clock_use_24_hour_time_toggle"))
                }
                CardDivider()
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 13.dp)
                        .alpha(if (use24HourTime) 0.4f else 1f),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(text = stringResource(R.string.clock_style_show_meridiem), style = MaterialTheme.typography.bodyLarge, color = Ink)
                    Switch(
                        checked = showMeridiem && !use24HourTime,
                        onCheckedChange = onShowMeridiemChange,
                        enabled = !use24HourTime,
                        modifier = Modifier.testTag("clock_show_meridiem_toggle"),
                    )
                }
                CardDivider()
                LabeledDropdownRow(
                    title = stringResource(R.string.clock_style_date_style),
                    options = ClockDateStyle.entries,
                    selected = dateStyle,
                    label = { stringResource(it.displayNameRes) },
                    onSelect = onDateStyleChange,
                    testTag = "clock_date_style_row",
                )
                CardDivider()
                LabeledDropdownRow(
                    title = stringResource(R.string.clock_style_clock_alignment),
                    options = ClockAlignment.entries,
                    selected = clockAlignment,
                    label = { stringResource(it.displayNameRes) },
                    onSelect = onClockAlignmentChange,
                    testTag = "clock_alignment_row",
                )
            }
        }

        item {
            Text(
                text = stringResource(R.string.clock_style_position_section),
                style = MaterialTheme.typography.labelSmall,
                color = Muted,
            )
        }
        item {
            SettingsCard {
                ClockPositionResetRow(onClick = onResetClockPosition)
            }
        }

        item {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = stringResource(R.string.clock_style_tap_to_select),
                    style = MaterialTheme.typography.labelSmall,
                    color = Muted,
                )
                Text(
                    text = stringResource(R.string.clock_style_long_press_hint),
                    style = MaterialTheme.typography.bodySmall,
                    color = Muted,
                )
            }
        }

        items(ClockTemplateId.entries.toList()) { id ->
            val selected = id == templateId
            val shape = MaterialTheme.shapes.medium
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(shape)
                    .background(Surface, shape)
                    .border(width = if (selected) 2.dp else 1.dp, color = if (selected) Accent else Hairline, shape = shape)
                    .clickable(onClick = { onTemplateSelect(id) })
                    .testTag("clock_template_card_${id.name}")
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp),
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(text = stringResource(id.displayNameRes), style = MaterialTheme.typography.titleSmall, color = Ink)
                    if (selected) Icon(Icons.Default.Check, contentDescription = stringResource(R.string.clock_style_applied), tint = Accent)
                }
                ClockDisplay(
                    templateId = id,
                    fontOption = fontOption,
                    now = now,
                    use24HourTime = use24HourTime,
                    showMeridiem = showMeridiem,
                    textColor = textColor,
                    mutedTextColor = textColor.copy(alpha = 0.8f),
                    accentColor = accentColor,
                    dateStyle = dateStyle,
                    // Every template's own root lost its `fillMaxWidth()` (see HomeScreen.kt's own
                    // doc on the clock hit-box fix), so this Column's shared `horizontalAlignment`
                    // no longer moves each card's preview — an explicit per-child align is needed
                    // here, same as ClockBlock's own real Home usage.
                    modifier = Modifier.align(clockAlignment.resolve()),
                    launcherFontOption = launcherFontOption,
                    clockAlignment = clockAlignment,
                    nextAlarmMillis = samplePreviewNextAlarmMillis,
                    batteryPercent = samplePreviewBatteryPercent,
                    isCharging = samplePreviewIsCharging,
                )
            }
        }
    }
        },
    )
}

/**
 * Direct one-tap action, no confirmation — restores the whole clock+calendar widget's position:
 * `clockZoneHeightDp` back to `null` (the grab handle) AND both `clockAlignment`/`calendarAlignment`
 * back to `LEFT`, not just the height alone. Consistent with this screen's other immediate-effect
 * controls.
 */
@Composable
private fun ClockPositionResetRow(onClick: () -> Unit, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .testTag("reset_clock_position_row")
            .padding(vertical = 13.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(text = stringResource(R.string.clock_style_reset_position), style = MaterialTheme.typography.bodyLarge, color = Ink)
    }
}

@Composable
private fun ClockStyleGalleryHeader(onBack: () -> Unit, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(SurfaceContainer)
            .windowInsetsPadding(WindowInsets.systemBars.only(WindowInsetsSides.Top))
            .padding(horizontal = 24.dp, vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        BackButton(onClick = onBack)
        Text(
            text = stringResource(R.string.settings_clock_calendar_title),
            style = MaterialTheme.typography.titleLarge,
            color = Ink,
            modifier = Modifier.padding(start = 12.dp),
        )
    }
}

@Preview(showBackground = true, heightDp = 3200)
@Composable
private fun ClockStyleGalleryScreenPreview() {
    FacetLauncherTheme {
        ClockStyleGalleryScreen(
            onBack = {},
            templateId = ClockTemplateId.LIGHT_STACK,
            fontOption = ClockFontOption.SYSTEM,
            colorOption = ClockColorOption.THEME,
            use24HourTime = false,
            showMeridiem = false,
            onTemplateSelect = {},
            onFontOptionChange = {},
            onColorOptionChange = {},
            onUse24HourTimeChange = {},
            onShowMeridiemChange = {},
        )
    }
}
