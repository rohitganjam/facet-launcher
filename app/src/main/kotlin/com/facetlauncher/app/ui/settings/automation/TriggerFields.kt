package com.facetlauncher.app.ui.settings.automation

import android.content.Intent
import android.net.Uri
import android.provider.Settings
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.facetlauncher.app.R
import com.facetlauncher.app.data.model.AutomationPermission
import com.facetlauncher.app.data.model.AutomationRuleError
import com.facetlauncher.app.data.model.AutomationTrigger
import com.facetlauncher.app.data.model.BatteryDirection
import com.facetlauncher.app.data.model.BatteryLevelCondition
import com.facetlauncher.app.data.model.PairedBluetoothDevice
import com.facetlauncher.app.ui.components.LabeledDropdownRow
import com.facetlauncher.app.ui.theme.Accent
import com.facetlauncher.app.ui.theme.Hairline
import com.facetlauncher.app.ui.theme.Muted

private const val BATTERY_STEP_PERCENT = 5

@Composable
private fun TriggerKind.label(): String = stringResource(
    when (this) {
        TriggerKind.SCHEDULE -> R.string.automation_trigger_schedule
        TriggerKind.BLUETOOTH -> R.string.automation_trigger_bluetooth
        TriggerKind.WIFI -> R.string.automation_trigger_wifi
        TriggerKind.HEADPHONES -> R.string.automation_trigger_headphones
        TriggerKind.BATTERY -> R.string.automation_trigger_battery
    },
)

/**
 * The "When" row and the fields of the chosen trigger type. A type change goes through [onChangeTrigger]
 * (the permission gate); everything inside one type is an ordinary [onChange] edit.
 */
@Composable
internal fun TriggerFields(
    state: RuleEditorState,
    choices: DeviceChoices,
    isPro: Boolean,
    onChange: ((RuleEditorState) -> RuleEditorState) -> Unit,
    onChangeTrigger: (RuleEditorState) -> Unit,
) {
    Column {
        LabeledDropdownRow(
            title = stringResource(R.string.automation_editor_when),
            options = TriggerKind.entries,
            selected = state.trigger.kind(),
            label = { it.label() },
            optionTrailing = if (isPro) null else { kind -> if (kind != TriggerKind.SCHEDULE) ProPill() },
            onSelect = { onChangeTrigger(state.withKind(it)) },
            testTag = "rule_editor_when",
        )
        state.permissionDenied?.let { PermissionNote(it) }
        when (val trigger = state.trigger) {
            is AutomationTrigger.Schedule -> ScheduleFields(state, onChange)
            is AutomationTrigger.Bluetooth -> BluetoothFields(state, trigger, choices, onChange)
            is AutomationTrigger.Wifi -> WifiFields(state, trigger, choices, onChange, onChangeTrigger)
            is AutomationTrigger.Headphones -> PolarityRadios(
                negated = trigger.negated,
                positive = stringResource(R.string.automation_while_plugged_in),
                negative = stringResource(R.string.automation_while_not_plugged_in),
                tag = "rule_editor_polarity",
                onSelect = { onChange { s -> s.withTrigger(trigger.copy(negated = it)) } },
            )
            is AutomationTrigger.Battery -> BatteryFields(trigger, onChange)
        }
    }
}

@Composable
private fun PermissionNote(permission: AutomationPermission) {
    val context = LocalContext.current
    val message = when (permission) {
        AutomationPermission.BLUETOOTH_CONNECT -> R.string.automation_permission_bluetooth_off
        AutomationPermission.LOCATION -> R.string.automation_permission_location_off
    }
    Column(modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp).testTag("rule_editor_permission_note")) {
        Text(text = stringResource(message), style = MaterialTheme.typography.bodyMedium, color = Muted)
        Text(
            text = stringResource(R.string.automation_open_settings),
            style = MaterialTheme.typography.bodyMedium,
            color = Accent,
            modifier = Modifier
                .clickable {
                    val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.fromParts("package", context.packageName, null))
                    runCatching { context.startActivity(intent) }
                }
                .padding(vertical = 6.dp)
                .testTag("rule_editor_open_settings"),
        )
    }
}

/** "While X" / "While not X" for a device condition. */
@Composable
private fun PolarityRadios(negated: Boolean, positive: String, negative: String, tag: String, onSelect: (negated: Boolean) -> Unit) {
    Column(modifier = Modifier.selectableGroup()) {
        RadioRow(selected = !negated, text = positive, tag = "${tag}_on", onClick = { onSelect(false) })
        RadioRow(selected = negated, text = negative, tag = "${tag}_off", onClick = { onSelect(true) })
    }
}

@Composable
private fun BluetoothFields(
    state: RuleEditorState,
    trigger: AutomationTrigger.Bluetooth,
    choices: DeviceChoices,
    onChange: ((RuleEditorState) -> RuleEditorState) -> Unit,
) {
    val paired = choices.bluetooth
    val current = PairedBluetoothDevice(trigger.deviceAddress, trigger.deviceName)
    // A device that was unpaired since the rule was saved stays selectable so the row still reads as it was.
    val options = if (current.address.isBlank() || paired.any { it.address == current.address }) paired else listOf(current) + paired
    Column {
        PolarityRadios(
            negated = trigger.negated,
            positive = stringResource(R.string.automation_while_connected),
            negative = stringResource(R.string.automation_while_not_connected),
            tag = "rule_editor_polarity",
            onSelect = { onChange { s -> s.withTrigger(trigger.copy(negated = it)) } },
        )
        LabeledDropdownRow(
            title = stringResource(R.string.automation_editor_device),
            options = options,
            selected = current,
            label = { if (it.address.isBlank()) stringResource(R.string.automation_choose_device) else it.name },
            onSelect = { device -> onChange { s -> s.withTrigger(trigger.copy(deviceAddress = device.address, deviceName = device.name)) } },
            testTag = "rule_editor_device",
        )
        if (paired.isEmpty()) {
            Text(text = stringResource(R.string.automation_no_paired_devices), style = MaterialTheme.typography.bodyMedium, color = Muted)
        }
        if (AutomationRuleError.NO_BLUETOOTH_DEVICE in state.errors) ErrorText(stringResource(R.string.automation_error_no_device), "rule_editor_error_device")
    }
}

@Composable
private fun WifiFields(
    state: RuleEditorState,
    trigger: AutomationTrigger.Wifi,
    choices: DeviceChoices,
    onChange: ((RuleEditorState) -> RuleEditorState) -> Unit,
    onChangeTrigger: (RuleEditorState) -> Unit,
) {
    val networks = choices.networks
    val named = trigger.ssid != null
    Column {
        PolarityRadios(
            negated = trigger.negated,
            positive = stringResource(R.string.automation_while_connected),
            negative = stringResource(R.string.automation_while_not_connected),
            tag = "rule_editor_polarity",
            onSelect = { onChange { s -> s.withTrigger(trigger.copy(negated = it)) } },
        )
        RadioRow(!named, stringResource(R.string.automation_wifi_scope_any), "rule_editor_wifi_any") { onChangeTrigger(state.withWifiScope(false)) }
        RadioRow(named, stringResource(R.string.automation_wifi_scope_named), "rule_editor_wifi_named") { onChangeTrigger(state.withWifiScope(true)) }
        if (named) {
            val ssid = trigger.ssid.orEmpty()
            LabeledDropdownRow(
                title = stringResource(R.string.automation_editor_network),
                options = if (ssid.isBlank() || ssid in networks) networks else listOf(ssid) + networks,
                selected = ssid,
                label = { it.ifBlank { stringResource(R.string.automation_choose_network) } },
                onSelect = { name -> onChange { s -> s.withTrigger(trigger.copy(ssid = name)) } },
                testTag = "rule_editor_network",
            )
            if (AutomationRuleError.BLANK_WIFI_NETWORK in state.errors) ErrorText(stringResource(R.string.automation_error_no_network), "rule_editor_error_network")
        }
    }
}

@Composable
private fun BatteryFields(trigger: AutomationTrigger.Battery, onChange: ((RuleEditorState) -> RuleEditorState) -> Unit) {
    val level = trigger.level
    val stops = BatteryLevelCondition.stopsFor(level.direction)
    Column {
        PolarityRadios(
            negated = !trigger.whileCharging,
            positive = stringResource(R.string.automation_battery_charging),
            negative = stringResource(R.string.automation_battery_not_charging),
            tag = "rule_editor_charging",
            onSelect = { notCharging -> onChange { s -> s.withTrigger(trigger.copy(whileCharging = !notCharging)) } },
        )
        PolarityRadios(
            negated = level.direction == BatteryDirection.ABOVE,
            positive = stringResource(R.string.automation_battery_option_below),
            negative = stringResource(R.string.automation_battery_option_above),
            tag = "rule_editor_direction",
            onSelect = { above ->
                val direction = if (above) BatteryDirection.ABOVE else BatteryDirection.BELOW
                val threshold = level.thresholdPercent.coerceIn(BatteryLevelCondition.stopsFor(direction).first, BatteryLevelCondition.stopsFor(direction).last)
                onChange { s -> s.withTrigger(trigger.copy(level = BatteryLevelCondition(direction, threshold))) }
            },
        )
        Text(
            text = stringResource(R.string.automation_battery_percent, level.thresholdPercent),
            style = MaterialTheme.typography.bodyLarge,
            color = Accent,
            modifier = Modifier.padding(top = 8.dp).testTag("rule_editor_battery_value"),
        )
        Slider(
            value = level.thresholdPercent.toFloat(),
            onValueChange = { raw ->
                val snapped = (Math.round(raw / BATTERY_STEP_PERCENT) * BATTERY_STEP_PERCENT).coerceIn(stops.first, stops.last)
                onChange { s -> s.withTrigger(trigger.copy(level = level.copy(thresholdPercent = snapped))) }
            },
            valueRange = stops.first.toFloat()..stops.last.toFloat(),
            steps = (stops.last - stops.first) / BATTERY_STEP_PERCENT - 1,
            colors = SliderDefaults.colors(thumbColor = Accent, activeTrackColor = Accent, inactiveTrackColor = Hairline),
            modifier = Modifier.testTag("rule_editor_battery_slider"),
        )
    }
}
