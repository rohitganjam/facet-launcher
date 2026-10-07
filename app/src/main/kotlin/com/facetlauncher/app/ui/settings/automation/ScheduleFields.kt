package com.facetlauncher.app.ui.settings.automation

import android.text.format.DateFormat
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.TimePickerDefaults
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.compositeOver
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.facetlauncher.app.R
import com.facetlauncher.app.data.model.AutomationRuleError
import com.facetlauncher.app.data.model.AutomationTrigger
import com.facetlauncher.app.ui.theme.Accent
import com.facetlauncher.app.ui.theme.Hairline
import com.facetlauncher.app.ui.theme.Ink
import com.facetlauncher.app.ui.theme.Muted
import com.facetlauncher.app.ui.theme.Surface
import java.time.DayOfWeek
import java.time.format.TextStyle
import java.util.Locale

private const val MINUTES_PER_HOUR = 60

@Composable
internal fun ScheduleFields(state: RuleEditorState, onChange: ((RuleEditorState) -> RuleEditorState) -> Unit) {
    val schedule = state.trigger as AutomationTrigger.Schedule
    Column {
        DayChipRow(state, onChange)
        if (AutomationRuleError.NO_SCHEDULE_DAYS in state.errors) {
            ErrorText(stringResource(R.string.automation_error_no_days), "rule_editor_error_days")
        }
        TimeRow(stringResource(R.string.automation_editor_from), schedule.startMinute, "rule_editor_from") { m -> onChange { it.withStartMinute(m) } }
        TimeRow(stringResource(R.string.automation_editor_until), schedule.endMinute, "rule_editor_until") { m -> onChange { it.withEndMinute(m) } }
        if (schedule.endMinute < schedule.startMinute) {
            Text(
                text = stringResource(R.string.automation_ends_next_day),
                style = MaterialTheme.typography.bodyMedium,
                color = Muted,
                modifier = Modifier.testTag("rule_editor_ends_next_day"),
            )
        }
    }
}

@Composable
private fun DayChipRow(state: RuleEditorState, onChange: ((RuleEditorState) -> RuleEditorState) -> Unit) {
    val selectedDays = (state.trigger as AutomationTrigger.Schedule).days
    Row(modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
        DayOfWeek.entries.forEach { day ->
            DayChip(
                day = day,
                selected = day in selectedDays,
                onToggle = { on -> onChange { it.withScheduleDay(day, on) } },
                modifier = Modifier.weight(1f),
            )
        }
    }
}

/** An M3 filter chip (small shape, 8dp), themed explicitly. The visible letter is narrow; the full day name is announced. */
@Composable
private fun DayChip(day: DayOfWeek, selected: Boolean, onToggle: (Boolean) -> Unit, modifier: Modifier = Modifier) {
    val fullName = day.getDisplayName(TextStyle.FULL, Locale.getDefault())
    FilterChip(
        selected = selected,
        onClick = { onToggle(!selected) },
        label = {
            Text(
                text = day.getDisplayName(TextStyle.NARROW, Locale.getDefault()),
                style = MaterialTheme.typography.labelLarge,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth(),
            )
        },
        modifier = modifier.semantics { contentDescription = fullName }.testTag("rule_editor_day_${day.name}"),
        colors = FilterChipDefaults.filterChipColors(
            containerColor = Surface,
            labelColor = Muted,
            selectedContainerColor = Accent.copy(alpha = 0.18f).compositeOver(Surface),
            selectedLabelColor = Ink,
        ),
        border = FilterChipDefaults.filterChipBorder(
            enabled = true,
            selected = selected,
            borderColor = Hairline,
            selectedBorderColor = Accent,
        ),
    )
}

@Composable
private fun TimeRow(title: String, minute: Int, tag: String, onPick: (Int) -> Unit) {
    var picking by remember { mutableStateOf(false) }
    Row(
        modifier = Modifier.fillMaxWidth().clickable { picking = true }.padding(vertical = 13.dp).testTag(tag),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(text = title, style = MaterialTheme.typography.bodyLarge, color = Ink)
        Text(text = minuteText(minute), style = MaterialTheme.typography.bodyLarge, color = Accent)
    }
    if (picking) TimePickerDialog(title, minute, onPick = { picking = false; onPick(it) }, onDismiss = { picking = false })
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TimePickerDialog(title: String, minute: Int, onPick: (Int) -> Unit, onDismiss: () -> Unit) {
    val picker = rememberTimePickerState(
        initialHour = minute / MINUTES_PER_HOUR,
        initialMinute = minute % MINUTES_PER_HOUR,
        is24Hour = DateFormat.is24HourFormat(LocalContext.current),
    )
    AlertDialog(
        onDismissRequest = onDismiss,
        modifier = Modifier.testTag("rule_editor_time_dialog"),
        shape = MaterialTheme.shapes.extraLarge,
        containerColor = Surface,
        title = { Text(text = title, style = MaterialTheme.typography.titleMedium, color = Ink) },
        text = {
            TimePicker(
                state = picker,
                colors = TimePickerDefaults.colors(
                    clockDialColor = Hairline,
                    clockDialSelectedContentColor = Surface,
                    clockDialUnselectedContentColor = Ink,
                    selectorColor = Accent,
                    containerColor = Surface,
                    periodSelectorBorderColor = Hairline,
                    periodSelectorSelectedContainerColor = Accent.copy(alpha = 0.2f),
                    periodSelectorUnselectedContainerColor = Surface,
                    periodSelectorSelectedContentColor = Ink,
                    periodSelectorUnselectedContentColor = Muted,
                    timeSelectorSelectedContainerColor = Accent.copy(alpha = 0.2f),
                    timeSelectorUnselectedContainerColor = Hairline,
                    timeSelectorSelectedContentColor = Ink,
                    timeSelectorUnselectedContentColor = Ink,
                ),
            )
        },
        confirmButton = {
            TextButton(onClick = { onPick(picker.hour * MINUTES_PER_HOUR + picker.minute) }, modifier = Modifier.testTag("rule_editor_time_ok")) {
                Text(text = stringResource(R.string.action_done), style = MaterialTheme.typography.bodyLarge, color = Accent)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(text = stringResource(R.string.action_cancel), style = MaterialTheme.typography.bodyLarge, color = Muted)
            }
        },
    )
}
