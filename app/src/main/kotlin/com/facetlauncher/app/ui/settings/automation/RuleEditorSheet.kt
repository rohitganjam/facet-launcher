package com.facetlauncher.app.ui.settings.automation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.facetlauncher.app.R
import com.facetlauncher.app.data.model.AutomationTrigger
import com.facetlauncher.app.ui.components.ConfirmDialog
import com.facetlauncher.app.ui.components.LabeledDropdownRow
import com.facetlauncher.app.ui.components.ProUpgradeSheet
import com.facetlauncher.app.ui.components.ThemedModalBottomSheet
import com.facetlauncher.app.ui.components.TonalButton
import com.facetlauncher.app.ui.theme.Accent
import com.facetlauncher.app.ui.theme.ErrorColor
import com.facetlauncher.app.ui.theme.Ink
import com.facetlauncher.app.ui.theme.Muted

/** Create or edit one rule. Closing it any way (Cancel, swipe, Back, Home) discards the draft. */
@Composable
internal fun RuleEditorSheet(
    state: RuleEditorState,
    choices: DeviceChoices,
    isPro: Boolean,
    onChange: ((RuleEditorState) -> RuleEditorState) -> Unit,
    onChangeTrigger: (RuleEditorState) -> Unit,
    onSave: () -> Unit,
    onDelete: () -> Unit,
    onDismiss: () -> Unit,
) {
    var confirmingDelete by remember { mutableStateOf(false) }
    ThemedModalBottomSheet(onDismissRequest = onDismiss, skipPartiallyExpanded = true) {
        RuleEditorContent(
            state = state,
            choices = choices,
            isPro = isPro,
            onChange = onChange,
            onChangeTrigger = onChangeTrigger,
            onSave = onSave,
            onCancel = onDismiss,
            onDeleteClick = { confirmingDelete = true },
        )
    }
    state.proRequired?.let { reason -> ProUpgradeSheet(reason = reason, onDismiss = { onChange { it } }) }
    if (confirmingDelete) {
        ConfirmDialog(
            title = stringResource(R.string.automation_delete_title),
            message = stringResource(R.string.automation_delete_message),
            confirmLabel = stringResource(R.string.action_delete),
            onConfirm = { confirmingDelete = false; onDelete() },
            onDismiss = { confirmingDelete = false },
        )
    }
}

/** The sheet's body, separate from the sheet so previews can render it. */
@Composable
internal fun RuleEditorContent(
    state: RuleEditorState,
    choices: DeviceChoices,
    isPro: Boolean,
    onChange: ((RuleEditorState) -> RuleEditorState) -> Unit,
    onChangeTrigger: (RuleEditorState) -> Unit,
    onSave: () -> Unit,
    onCancel: () -> Unit,
    onDeleteClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp)
            .padding(bottom = 24.dp)
            .testTag("rule_editor"),
    ) {
        val title = if (state.isNew) R.string.automation_editor_new else R.string.automation_editor_edit
        Text(
            text = stringResource(title),
            style = MaterialTheme.typography.titleLarge,
            color = Ink,
            modifier = Modifier.padding(bottom = 4.dp).semantics { heading() },
        )
        LabeledDropdownRow(
            title = stringResource(R.string.automation_editor_switch_to),
            options = state.facets,
            selected = state.facets.firstOrNull { it.id == state.targetFacetId } ?: state.facets.first(),
            label = { it.name },
            onSelect = { facet -> onChange { it.copy(targetFacetId = facet.id) } },
            testTag = "rule_editor_target",
        )
        TriggerFields(state, choices, isPro, onChange, onChangeTrigger)
        EndBehaviorFields(state, onChange)
        if (state.facetMissing) ErrorText(stringResource(R.string.automation_error_facet_missing), "rule_editor_error_facet")
        EditorButtons(state.isNew, onSave = onSave, onCancel = onCancel, onDelete = onDeleteClick)
    }
}

@Composable
private fun EndBehaviorFields(state: RuleEditorState, onChange: ((RuleEditorState) -> RuleEditorState) -> Unit) {
    val facets = state.facets
    val targetName = facets.firstOrNull { it.id == state.targetFacetId }?.name.orEmpty()
    Column(modifier = Modifier.selectableGroup()) {
        Text(
            text = stringResource(R.string.automation_editor_when_it_ends),
            style = MaterialTheme.typography.labelLarge,
            color = Muted,
            modifier = Modifier.padding(top = 12.dp),
        )
        EndRadio(EndKind.RETURN, state.endKind, stringResource(R.string.automation_end_return), onChange)
        SwitchToRow(state, onChange)
        EndRadio(EndKind.STAY, state.endKind, stringResource(R.string.automation_end_stay, targetName), onChange)
    }
}

/** "Switch to <facet>" on one row: the radio and label on the left, the facet dropdown on the right. */
@Composable
private fun SwitchToRow(state: RuleEditorState, onChange: ((RuleEditorState) -> RuleEditorState) -> Unit) {
    val choices = state.facets.filter { it.id != state.targetFacetId }.ifEmpty { state.facets }
    val isSwitch = state.endKind == EndKind.SWITCH
    LabeledDropdownRow(
        title = stringResource(R.string.automation_end_switch_to),
        options = choices,
        selected = choices.firstOrNull { it.id == state.endFacetId } ?: choices.first(),
        label = { it.name },
        onSelect = { facet -> onChange { it.copy(endKind = EndKind.SWITCH, endFacetId = facet.id) } },
        leading = {
            RadioButton(
                selected = isSwitch,
                onClick = null,
                colors = RadioButtonDefaults.colors(selectedColor = Accent, unselectedColor = Muted),
                modifier = Modifier.padding(end = 12.dp),
            )
        },
        titleModifier = Modifier
            .selectable(selected = isSwitch, role = Role.RadioButton, onClick = { onChange { it.copy(endKind = EndKind.SWITCH) } })
            .testTag("rule_editor_end_SWITCH"),
        testTag = "rule_editor_end_facet",
    )
}

@Composable
private fun EndRadio(kind: EndKind, selected: EndKind, text: String, onChange: ((RuleEditorState) -> RuleEditorState) -> Unit) {
    RadioRow(selected = kind == selected, text = text, tag = "rule_editor_end_${kind.name}", onClick = { onChange { it.copy(endKind = kind) } })
}

@Composable
private fun EditorButtons(isNew: Boolean, onSave: () -> Unit, onCancel: () -> Unit, onDelete: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(top = 20.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (!isNew) {
            TextButton(onClick = onDelete, modifier = Modifier.testTag("rule_editor_delete")) {
                Icon(Icons.Default.Delete, contentDescription = null, tint = ErrorColor, modifier = Modifier.size(18.dp))
                Text(
                    text = stringResource(R.string.action_delete),
                    style = MaterialTheme.typography.labelLarge,
                    color = ErrorColor,
                    modifier = Modifier.padding(start = 6.dp),
                )
            }
        }
        Box(modifier = Modifier.weight(1f))
        TextButton(onClick = onCancel, modifier = Modifier.testTag("rule_editor_cancel")) {
            Text(text = stringResource(R.string.action_cancel), style = MaterialTheme.typography.labelLarge, color = Muted)
        }
        TonalButton(text = stringResource(R.string.action_save), onClick = onSave, modifier = Modifier.testTag("rule_editor_save"))
    }
}
