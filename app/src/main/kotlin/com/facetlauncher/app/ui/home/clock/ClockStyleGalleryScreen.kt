package com.facetlauncher.app.ui.home.clock

import androidx.annotation.StringRes
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
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
import com.facetlauncher.app.data.model.WeatherIconStyle
import com.facetlauncher.app.R
import com.facetlauncher.app.data.model.ClockAlignment
import com.facetlauncher.app.data.model.ClockColorOption
import com.facetlauncher.app.data.model.ClockDateStyle
import com.facetlauncher.app.data.model.ClockFontOption
import com.facetlauncher.app.data.model.ClockTemplateId
import com.facetlauncher.app.data.model.LauncherFontOption
import com.facetlauncher.app.ui.components.BackButton
import com.facetlauncher.app.ui.components.CardDivider
import com.facetlauncher.app.ui.components.InheritOverrideCard
import com.facetlauncher.app.ui.components.LabeledDropdownRow
import com.facetlauncher.app.ui.components.LocalSettingsRowInset
import com.facetlauncher.app.ui.components.SettingsCard
import com.facetlauncher.app.ui.components.StickyHeaderLayout
import com.facetlauncher.app.ui.settings.ClickableRow
import com.facetlauncher.app.ui.settings.NavigationChevron
import com.facetlauncher.app.ui.home.weather.WeatherIconKind
import com.facetlauncher.app.ui.theme.Accent
import com.facetlauncher.app.ui.theme.Hairline
import com.facetlauncher.app.ui.theme.Ink
import com.facetlauncher.app.ui.theme.FacetLauncherTheme
import com.facetlauncher.app.ui.theme.Muted
import com.facetlauncher.app.ui.theme.Surface
import com.facetlauncher.app.ui.theme.SurfaceContainer
import com.facetlauncher.app.ui.theme.fontFamily
import com.facetlauncher.app.ui.theme.resolve
import java.time.Clock
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId

/**
 * Entry point for the launcher default (Settings' "Default clock style" row) and, with a `facetId` in the ViewModel's
 * `SavedStateHandle`, for a facet's own clock — real, persisted, actually applied to Home via
 * [com.facetlauncher.app.ui.home.ClockBlock].
 */
@Composable
fun ClockStyleGalleryRoute(onBack: () -> Unit, onOpenClockAccessories: () -> Unit = {}, viewModel: ClockStyleGalleryViewModel = hiltViewModel()) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    ClockStyleGalleryScreen(
        uiState = uiState,
        actions = ClockStyleGalleryActions(
            onTemplateSelect = viewModel::setClockTemplateId,
            onFontOptionChange = viewModel::setClockFontOption,
            onColorOptionChange = viewModel::setClockColorOption,
            onAccentColorOptionChange = viewModel::setClockAccentColorOption,
            onUse24HourTimeChange = viewModel::setUse24HourTime,
            onShowMeridiemChange = viewModel::setClockShowMeridiem,
            onDateStyleChange = viewModel::setClockDateStyle,
            onClockAlignmentChange = viewModel::setClockAlignment,
            onResetClockPosition = viewModel::resetClockPosition,
            onOverridingChange = viewModel::setOverridingClock,
            onOpenClockAccessories = onOpenClockAccessories,
        ),
        onBack = onBack,
        inheritSubtitle = stringResource(
            R.string.dot_join_2,
            stringResource(uiState.settings.clockTemplateId.displayNameRes),
            stringResource(if (uiState.settings.use24HourTime) R.string.settings_time_format_24h else R.string.settings_time_format_12h),
        ),
    )
}

/** What the clock style screen's controls do. */
internal class ClockStyleGalleryActions(
    val onTemplateSelect: (ClockTemplateId) -> Unit = {},
    val onFontOptionChange: (ClockFontOption) -> Unit = {},
    val onColorOptionChange: (ClockColorOption) -> Unit = {},
    val onAccentColorOptionChange: (ClockColorOption) -> Unit = {},
    val onUse24HourTimeChange: (Boolean) -> Unit = {},
    val onShowMeridiemChange: (Boolean) -> Unit = {},
    val onDateStyleChange: (ClockDateStyle) -> Unit = {},
    val onClockAlignmentChange: (ClockAlignment) -> Unit = {},
    val onResetClockPosition: () -> Unit = {},
    val onOverridingChange: (Boolean) -> Unit = {},
    val onOpenClockAccessories: () -> Unit = {},
)

/**
 * Stateless clock-style picker. Order: an Inherit/Override switch when this instance is facet-scoped, then the clock's
 * font, color, time, date and alignment controls, then every [ClockTemplateId] as its own selectable card — tapping one
 * selects it and gives it an [Accent] border. Every control below the Inherit/Override switch is dimmed and disabled
 * while inheriting: editing then would write to the facet's row without taking effect until Override is picked.
 */
@Composable
private fun ClockStyleGalleryScreen(
    uiState: ClockStyleGalleryUiState,
    actions: ClockStyleGalleryActions,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    inheritSubtitle: String = "",
) {
    val controlsEnabled = !uiState.isFacetScoped || uiState.isOverriding
    val sample = rememberClockPreviewSample(uiState.showWeather, uiState.weatherIconStyle)

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
                if (uiState.isFacetScoped) {
                    item {
                        InheritOverrideCard(
                            overriding = uiState.isOverriding,
                            onOverridingChange = actions.onOverridingChange,
                            testTagPrefix = "clock_style",
                            inheritSubtitle = inheritSubtitle,
                        )
                    }
                }
                item { SectionLabel(R.string.clock_style_clock_section) }
                item { ClockStyleCard(uiState, controlsEnabled, actions) }
                item { ClockAccessoriesLinkCard(actions.onOpenClockAccessories) }
                item { SectionLabel(R.string.clock_style_position_section) }
                item {
                    SettingsCard(fullBleedRows = true) {
                        ClockPositionResetRow(onClick = actions.onResetClockPosition, enabled = controlsEnabled)
                    }
                }
                item { TemplateHints() }
                items(ClockTemplateId.entries.toList()) { id ->
                    ClockTemplateCard(id, uiState, controlsEnabled, sample, actions.onTemplateSelect)
                }
            }
        },
    )
}

@Composable
private fun SectionLabel(@StringRes textRes: Int) {
    Text(text = stringResource(textRes), style = MaterialTheme.typography.labelSmall, color = Muted)
}

@Composable
private fun TemplateHints() {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(text = stringResource(R.string.clock_style_tap_to_select), style = MaterialTheme.typography.labelSmall, color = Muted)
        Text(text = stringResource(R.string.clock_style_long_press_hint), style = MaterialTheme.typography.bodySmall, color = Muted)
    }
}

@Composable
private fun ClockAccessoriesLinkCard(onOpenClockAccessories: () -> Unit) {
    SettingsCard(fullBleedRows = true) {
        ClickableRow(
            title = stringResource(R.string.clock_accessories_title),
            subtitle = stringResource(R.string.clock_accessories_summary),
            onClick = onOpenClockAccessories,
            testTag = "clock_accessories_row",
            trailing = { NavigationChevron() },
        )
    }
}

/** The clock's font, colors, time format, date style and alignment, all dimmed and inert while a facet inherits. */
@Composable
private fun ClockStyleCard(uiState: ClockStyleGalleryUiState, controlsEnabled: Boolean, actions: ClockStyleGalleryActions) {
    SettingsCard {
        LabeledDropdownRow(
            title = stringResource(R.string.clock_style_font_label),
            options = ClockFontOption.entries,
            selected = uiState.fontOption,
            label = { stringResource(it.displayNameRes) },
            onSelect = actions.onFontOptionChange,
            enabled = controlsEnabled,
            testTag = "clock_font_row",
        )
        CardDivider()
        LabeledDropdownRow(
            title = stringResource(R.string.clock_style_primary_font_color),
            options = ClockColorOption.entries,
            selected = uiState.colorOption,
            label = { stringResource(it.displayNameRes) },
            onSelect = actions.onColorOptionChange,
            enabled = controlsEnabled,
            testTag = "clock_color_row",
        )
        CardDivider()
        // Always visible, even for templates with no accent element in their design — per direct request, so picking
        // one isn't gated behind first scrolling down to an accent-using template and back up. It's simply inert for
        // a non-accent template, the way "Show meridiem" stays visible (dimmed) while 24-hour time is on.
        LabeledDropdownRow(
            title = stringResource(R.string.appearance_accent_color),
            options = ClockColorOption.entries,
            selected = uiState.accentColorOption,
            label = { stringResource(it.displayNameRes) },
            onSelect = actions.onAccentColorOptionChange,
            enabled = controlsEnabled,
            testTag = "clock_accent_color_row",
        )
        CardDivider()
        TimeFormatRows(uiState, controlsEnabled, actions)
        CardDivider()
        LabeledDropdownRow(
            title = stringResource(R.string.clock_style_date_style),
            options = ClockDateStyle.entries,
            selected = uiState.dateStyle,
            label = { stringResource(it.displayNameRes) },
            onSelect = actions.onDateStyleChange,
            enabled = controlsEnabled,
            testTag = "clock_date_style_row",
        )
        CardDivider()
        LabeledDropdownRow(
            title = stringResource(R.string.clock_style_clock_alignment),
            options = ClockAlignment.entries,
            selected = uiState.clockAlignment,
            label = { stringResource(it.displayNameRes) },
            onSelect = actions.onClockAlignmentChange,
            enabled = controlsEnabled,
            testTag = "clock_alignment_row",
        )
    }
}

/** The 24-hour switch and, under it, the meridiem switch, which is dimmed and inert while 24-hour time is on. */
@Composable
private fun ColumnScope.TimeFormatRows(uiState: ClockStyleGalleryUiState, controlsEnabled: Boolean, actions: ClockStyleGalleryActions) {
    val use24HourTime = uiState.use24HourTime
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 13.dp).alpha(if (controlsEnabled) 1f else 0.4f),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(text = stringResource(R.string.settings_time_format_24h), style = MaterialTheme.typography.bodyLarge, color = Ink)
        Switch(
            checked = use24HourTime,
            onCheckedChange = actions.onUse24HourTimeChange,
            enabled = controlsEnabled,
            modifier = Modifier.testTag("clock_use_24_hour_time_toggle"),
        )
    }
    CardDivider()
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 13.dp)
            .alpha(if (use24HourTime || !controlsEnabled) 0.4f else 1f),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(text = stringResource(R.string.clock_style_show_meridiem), style = MaterialTheme.typography.bodyLarge, color = Ink)
        Switch(
            checked = uiState.showMeridiem && !use24HourTime,
            onCheckedChange = actions.onShowMeridiemChange,
            enabled = !use24HourTime && controlsEnabled,
            modifier = Modifier.testTag("clock_show_meridiem_toggle"),
        )
    }
}

/** Fixed sample values, so every card in the gallery shows its accessory row the same way and templates compare side by side. */
private class ClockPreviewSample(val now: LocalDateTime, val nextAlarmMillis: Long, val weather: ClockWeatherAccessory?)

private const val SAMPLE_BATTERY_PERCENT = 64

@Composable
private fun rememberClockPreviewSample(showWeather: Boolean, weatherIconStyle: WeatherIconStyle): ClockPreviewSample {
    val fixedClock = remember { Clock.fixed(Instant.parse("2026-08-27T21:05:00Z"), ZoneId.of("UTC")) }
    val now = remember { LocalDateTime.now(fixedClock) }
    // Against the real system zone, not the fixed UTC one: ClockDisplay formats nextAlarmMillis via ZoneId.systemDefault(),
    // the same as Home does.
    val nextAlarmMillis = remember { LocalDate.now().plusDays(1).atTime(7, 0).atZone(ZoneId.systemDefault()).toInstant().toEpochMilli() }
    val weather = if (showWeather) {
        remember(weatherIconStyle) { ClockWeatherAccessory(WeatherIconKind.PARTLY_SUN, "21°C", "21°C, Partly cloudy", onClick = {}, iconStyle = weatherIconStyle) }
    } else {
        null
    }
    return ClockPreviewSample(now, nextAlarmMillis, weather)
}

/** One selectable template: its name, a check while applied, and a live preview in the current style. */
@Composable
private fun ClockTemplateCard(
    id: ClockTemplateId,
    uiState: ClockStyleGalleryUiState,
    controlsEnabled: Boolean,
    sample: ClockPreviewSample,
    onTemplateSelect: (ClockTemplateId) -> Unit,
) {
    val selected = id == uiState.templateId
    val shape = MaterialTheme.shapes.medium
    val textColor = uiState.colorOption.resolve()
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(shape)
            .background(Surface, shape)
            .border(width = if (selected) 2.dp else 1.dp, color = if (selected) Accent else Hairline, shape = shape)
            .clickable(enabled = controlsEnabled, onClick = { onTemplateSelect(id) })
            .alpha(if (controlsEnabled) 1f else 0.4f)
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
            fontOption = uiState.fontOption,
            now = sample.now,
            use24HourTime = uiState.use24HourTime,
            showMeridiem = uiState.showMeridiem,
            textColor = textColor,
            mutedTextColor = textColor,
            accentColor = uiState.accentColorOption.resolve(),
            dateStyle = uiState.dateStyle,
            // Every template's own root lost its `fillMaxWidth()` (see HomeScreen.kt's own doc on the clock hit-box fix),
            // so this Column's shared `horizontalAlignment` no longer moves each card's preview — an explicit per-child
            // align is needed here, same as ClockBlock's own real Home usage.
            modifier = Modifier.align(uiState.clockAlignment.resolve()),
            launcherFontOption = uiState.launcherFontOption,
            clockAlignment = uiState.clockAlignment,
            nextAlarmMillis = sample.nextAlarmMillis,
            batteryPercent = SAMPLE_BATTERY_PERCENT,
            isCharging = false,
            weather = sample.weather,
        )
    }
}

/**
 * Direct one-tap action, no confirmation — restores the whole clock+calendar widget's position:
 * `clockZoneHeightDp` back to `null` (the grab handle) AND `clockAlignment` back to `LEFT`, not
 * just the height alone. Consistent with this screen's other immediate-effect controls.
 */
@Composable
private fun ClockPositionResetRow(onClick: () -> Unit, modifier: Modifier = Modifier, enabled: Boolean = true) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clickable(enabled = enabled, onClick = onClick)
            .testTag("reset_clock_position_row")
            .padding(horizontal = LocalSettingsRowInset.current, vertical = 13.dp)
            .alpha(if (enabled) 1f else 0.4f),
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
        ClockStyleGalleryScreen(uiState = ClockStyleGalleryUiState(), actions = ClockStyleGalleryActions(), onBack = {})
    }
}
