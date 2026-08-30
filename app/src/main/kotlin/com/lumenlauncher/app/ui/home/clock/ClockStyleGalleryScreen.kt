package com.lumenlauncher.app.ui.home.clock

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.lumenlauncher.app.data.model.ClockColorOption
import com.lumenlauncher.app.data.model.ClockFontOption
import com.lumenlauncher.app.data.model.ClockTemplateId
import com.lumenlauncher.app.ui.components.BackButton
import com.lumenlauncher.app.ui.components.CardDivider
import com.lumenlauncher.app.ui.components.LabeledDropdownRow
import com.lumenlauncher.app.ui.components.SettingsCard
import com.lumenlauncher.app.ui.theme.Accent
import com.lumenlauncher.app.ui.theme.Hairline
import com.lumenlauncher.app.ui.theme.Ink
import com.lumenlauncher.app.ui.theme.LumenLauncherTheme
import com.lumenlauncher.app.ui.theme.Muted
import com.lumenlauncher.app.ui.theme.Surface
import com.lumenlauncher.app.ui.theme.resolve
import java.time.Clock
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId

/**
 * Global entry point (Settings' "Default clock style" row) — real, persisted, actually applied
 * to Home via [com.lumenlauncher.app.ui.home.ClockBlock].
 */
@Composable
fun ClockStyleGalleryRoute(onBack: () -> Unit, viewModel: ClockStyleGalleryViewModel = hiltViewModel()) {
    val settings by viewModel.settings.collectAsStateWithLifecycle()
    ClockStyleGalleryScreen(
        onBack = onBack,
        templateId = settings.clockTemplateId,
        fontOption = settings.clockFontOption,
        colorOption = settings.clockColorOption,
        use24HourTime = settings.use24HourTime,
        showMeridiem = settings.clockShowMeridiem,
        onTemplateSelected = viewModel::setClockTemplateId,
        onFontOptionChanged = viewModel::setClockFontOption,
        onColorOptionChanged = viewModel::setClockColorOption,
        onUse24HourTimeChanged = viewModel::setUse24HourTime,
        onShowMeridiemChanged = viewModel::setClockShowMeridiem,
    )
}

/**
 * Profile-scoped entry point (a profile's Clock card) — evaluation only, per direct instruction:
 * local in-memory state, nothing persisted, resets every time the screen is reopened. Doesn't
 * affect Home or any real profile setting.
 */
@Composable
fun ProfileClockStyleGalleryScreen(onBack: () -> Unit) {
    var templateId by remember { mutableStateOf(ClockTemplateId.LIGHT_STACK) }
    var fontOption by remember { mutableStateOf(ClockFontOption.SYSTEM) }
    var colorOption by remember { mutableStateOf(ClockColorOption.INK) }
    var use24HourTime by remember { mutableStateOf(false) }
    var showMeridiem by remember { mutableStateOf(false) }
    ClockStyleGalleryScreen(
        onBack = onBack,
        templateId = templateId,
        fontOption = fontOption,
        colorOption = colorOption,
        use24HourTime = use24HourTime,
        showMeridiem = showMeridiem,
        onTemplateSelected = { templateId = it },
        onFontOptionChanged = { fontOption = it },
        onColorOptionChanged = { colorOption = it },
        onUse24HourTimeChanged = { use24HourTime = it },
        onShowMeridiemChanged = { showMeridiem = it },
    )
}

/**
 * Stateless clock-style picker: font/color/24h/meridiem controls up top (immediately affecting
 * every preview below), then every [ClockTemplateId] as its own selectable card — tapping one
 * calls [onTemplateSelected] and gets an [Accent] border to show it's the applied option. On
 * [Surface] (the same theme-aware, light/dark-following background every other screen uses).
 */
@Composable
private fun ClockStyleGalleryScreen(
    onBack: () -> Unit,
    templateId: ClockTemplateId,
    fontOption: ClockFontOption,
    colorOption: ClockColorOption,
    use24HourTime: Boolean,
    showMeridiem: Boolean,
    onTemplateSelected: (ClockTemplateId) -> Unit,
    onFontOptionChanged: (ClockFontOption) -> Unit,
    onColorOptionChanged: (ClockColorOption) -> Unit,
    onUse24HourTimeChanged: (Boolean) -> Unit,
    onShowMeridiemChanged: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
) {
    val textColor = colorOption.resolve()
    val fixedClock = remember { Clock.fixed(Instant.parse("2026-08-27T21:05:00Z"), ZoneId.of("UTC")) }
    val now = remember { LocalDateTime.now(fixedClock) }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(Surface)
            .windowInsetsPadding(WindowInsets.systemBars)
            .testTag("clock_style_gallery_list"),
        contentPadding = PaddingValues(horizontal = 24.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp),
    ) {
        item {
            Row(verticalAlignment = Alignment.CenterVertically) {
                BackButton(onClick = onBack)
                Text(
                    text = "Clock style",
                    style = MaterialTheme.typography.titleLarge,
                    color = Ink,
                    modifier = Modifier.padding(start = 12.dp),
                )
            }
        }

        item {
            SettingsCard {
                LabeledDropdownRow(
                    title = "Font",
                    options = ClockFontOption.entries,
                    selected = fontOption,
                    label = { it.displayName },
                    onSelect = onFontOptionChanged,
                    testTag = "clock_font_row",
                )
                CardDivider()
                LabeledDropdownRow(
                    title = "Color",
                    options = ClockColorOption.entries,
                    selected = colorOption,
                    label = { it.displayName },
                    onSelect = onColorOptionChanged,
                    testTag = "clock_color_row",
                )
                CardDivider()
                Row(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 13.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(text = "24-hour time", style = MaterialTheme.typography.bodyLarge, color = Ink)
                    Switch(checked = use24HourTime, onCheckedChange = onUse24HourTimeChanged, modifier = Modifier.testTag("clock_use_24_hour_time_toggle"))
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
                    Text(text = "Show meridiem (AM/PM)", style = MaterialTheme.typography.bodyLarge, color = Ink)
                    Switch(
                        checked = showMeridiem && !use24HourTime,
                        onCheckedChange = onShowMeridiemChanged,
                        enabled = !use24HourTime,
                        modifier = Modifier.testTag("clock_show_meridiem_toggle"),
                    )
                }
            }
        }

        item {
            Text(
                text = "TEMPLATES — TAP TO SELECT",
                style = MaterialTheme.typography.labelSmall,
                color = Muted,
            )
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
                    .clickable(onClick = { onTemplateSelected(id) })
                    .testTag("clock_template_card_${id.name}")
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp),
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(text = id.displayName, style = MaterialTheme.typography.titleSmall, color = Ink)
                    if (selected) Icon(Icons.Default.Check, contentDescription = "Applied", tint = Accent)
                }
                ClockDisplay(
                    templateId = id,
                    fontOption = fontOption,
                    now = now,
                    use24HourTime = use24HourTime,
                    showMeridiem = showMeridiem,
                    textColor = textColor,
                    mutedTextColor = textColor.copy(alpha = 0.8f),
                )
            }
        }
    }
}

@Preview(showBackground = true, heightDp = 3200)
@Composable
private fun ClockStyleGalleryScreenPreview() {
    LumenLauncherTheme {
        ClockStyleGalleryScreen(
            onBack = {},
            templateId = ClockTemplateId.LIGHT_STACK,
            fontOption = ClockFontOption.SYSTEM,
            colorOption = ClockColorOption.INK,
            use24HourTime = false,
            showMeridiem = false,
            onTemplateSelected = {},
            onFontOptionChanged = {},
            onColorOptionChanged = {},
            onUse24HourTimeChanged = {},
            onShowMeridiemChanged = {},
        )
    }
}
