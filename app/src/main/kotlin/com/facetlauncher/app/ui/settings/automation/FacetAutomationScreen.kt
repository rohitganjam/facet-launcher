package com.facetlauncher.app.ui.settings.automation

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.BatteryStd
import androidx.compose.material.icons.filled.Bluetooth
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.Headphones
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.facetlauncher.app.R
import com.facetlauncher.app.data.model.AutomationPermission
import com.facetlauncher.app.data.model.AutomationTrigger
import com.facetlauncher.app.data.model.ProReason
import com.facetlauncher.app.domain.AutomationRuleItem
import com.facetlauncher.app.domain.FacetAutomationScreenState
import com.facetlauncher.app.domain.RuleAvailability
import com.facetlauncher.app.ui.components.BackButton
import com.facetlauncher.app.ui.components.CardDivider
import com.facetlauncher.app.ui.components.LocalSettingsRowInset
import com.facetlauncher.app.ui.components.ProPill
import com.facetlauncher.app.ui.components.SettingsCard
import com.facetlauncher.app.ui.components.StickyHeaderLayout
import com.facetlauncher.app.ui.components.SettingsSectionHeader
import com.facetlauncher.app.ui.theme.Accent
import com.facetlauncher.app.ui.theme.Ink
import com.facetlauncher.app.ui.theme.Muted
import com.facetlauncher.app.ui.theme.SurfaceContainer

/**
 * Settings → Facets → Facet automation: the rules that switch the active facet, a line saying why
 * Home looks the way it does, and a pointer to the "Switch to" shortcuts for other automation apps.
 * See `docs/architecture/15-flow-facet-automation.md`.
 */
@Composable
fun FacetAutomationScreen(
    onBack: () -> Unit,
    onOpenFacetPro: (ProReason) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: FacetAutomationViewModel = hiltViewModel(),
    editorViewModel: RuleEditorViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val editor by editorViewModel.editor.collectAsStateWithLifecycle()
    val choices by editorViewModel.choices.collectAsStateWithLifecycle()
    val isPro by editorViewModel.isPro.collectAsStateWithLifecycle()
    val requestPermission = rememberPermissionRequest()
    val lifecycleOwner = LocalLifecycleOwner.current

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event -> if (event == Lifecycle.Event.ON_RESUME) viewModel.refresh() }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    FacetAutomationContent(
        state = uiState,
        onBack = onBack,
        onToggleRule = viewModel::setEnabled,
        onAddRule = { if (uiState?.canAddRule == false) onOpenFacetPro(ProReason.RULE_LIMIT) else editorViewModel.openNewRule() },
        onEditRule = { ruleId ->
            // A paused rule opens the upgrade sheet. A rule whose permission is missing asks for it first;
            // only a refusal opens the editor.
            val item = uiState?.items?.firstOrNull { it.rule.id == ruleId }
            val needed = item?.missingPermission()
            when {
                item?.availability == RuleAvailability.NEEDS_PRO -> onOpenFacetPro(item.upgradeReason())
                needed == null -> editorViewModel.openRule(ruleId)
                else -> requestPermission(needed) { granted ->
                    editorViewModel.onPermissionResult(needed, granted)
                    if (granted) viewModel.refresh() else editorViewModel.openRule(ruleId)
                }
            }
        },
        onSeePro = { onOpenFacetPro(ProReason.RULE_LIMIT) },
        modifier = modifier,
    )
    editor?.let { editing ->
        RuleEditorSheet(
            state = editing,
            choices = choices,
            isPro = isPro,
            onOpenFacetPro = onOpenFacetPro,
            onChange = { transform -> editorViewModel.editRule { transform(this) } },
            onChangeTrigger = { candidate -> editorViewModel.changeTrigger(candidate, requestPermission) },
            onSave = editorViewModel::saveRule,
            onDelete = editorViewModel::deleteRule,
            onDismiss = editorViewModel::closeEditor,
        )
    }
}

@Composable
internal fun FacetAutomationContent(
    state: FacetAutomationScreenState?,
    onBack: () -> Unit,
    onToggleRule: (ruleId: Long, enabled: Boolean) -> Unit,
    onAddRule: () -> Unit,
    onEditRule: (ruleId: Long) -> Unit,
    onSeePro: () -> Unit,
    modifier: Modifier = Modifier,
) {
    StickyHeaderLayout(
        modifier = modifier,
        header = { AutomationHeader(onBack = onBack) },
        content = { headerHeight ->
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .background(SurfaceContainer)
                    .testTag("facet_automation_screen")
                    .windowInsetsPadding(WindowInsets.systemBars)
                    .padding(horizontal = 24.dp),
                contentPadding = PaddingValues(top = headerHeight),
            ) {
                if (state != null) {
                    state.status?.let { status -> item { StatusLine(status) } }
                    item { SettingsSectionHeader(stringResource(R.string.automation_section_rules)) }
                    item { RulesCard(state, onToggleRule, onAddRule, onEditRule) }
                    if (!state.canAddRule) item { FreePlanStrip(onSeePro) }
                    item { SettingsSectionHeader(stringResource(R.string.automation_section_other_apps)) }
                    item { OtherAppsCard() }
                    item { Spacer(modifier = Modifier.height(24.dp)) }
                }
            }
        },
    )
}

@Composable
private fun AutomationHeader(onBack: () -> Unit, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(SurfaceContainer)
            .windowInsetsPadding(WindowInsets.systemBars.only(WindowInsetsSides.Top))
            .padding(horizontal = 24.dp)
            .padding(top = 24.dp, bottom = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        BackButton(onClick = onBack)
        Text(text = stringResource(R.string.automation_header_title), style = MaterialTheme.typography.headlineSmall, color = Ink)
    }
}

@Composable
private fun RulesCard(
    state: FacetAutomationScreenState,
    onToggleRule: (Long, Boolean) -> Unit,
    onAddRule: () -> Unit,
    onEditRule: (Long) -> Unit,
) {
    SettingsCard(fullBleedRows = true) {
        if (state.items.isEmpty()) {
            EmptyRulesMessage()
        }
        state.items.forEachIndexed { index, item ->
            if (index > 0) CardDivider()
            RuleRow(item = item, onToggle = { onToggleRule(item.rule.id, it) }, onClick = { onEditRule(item.rule.id) })
        }
        CardDivider()
        // The same "Add rule" row whether or not any rules exist yet.
        AddRuleRow(
            showProPill = !state.canAddRule,
            onClick = onAddRule,
            tag = if (state.items.isEmpty()) "automation_empty_add" else "automation_add_rule_row",
        )
    }
}

@Composable
private fun RuleRow(item: AutomationRuleItem, onToggle: (Boolean) -> Unit, onClick: () -> Unit, modifier: Modifier = Modifier) {
    val unavailable = item.availability != RuleAvailability.AVAILABLE
    val needsPro = item.availability == RuleAvailability.NEEDS_PRO
    val title = item.rule.trigger.titleText()
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .alpha(if (unavailable || !item.rule.enabled) 0.55f else 1f)
            .padding(horizontal = LocalSettingsRowInset.current, vertical = 13.dp)
            .testTag("automation_rule_row_${item.rule.id}"),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Icon(
            imageVector = when {
                needsPro -> Icons.Default.Lock
                unavailable -> Icons.Default.ErrorOutline
                else -> item.rule.trigger.icon()
            },
            contentDescription = null,
            tint = Muted,
            modifier = Modifier.size(22.dp),
        )
        Column(modifier = Modifier.weight(1f)) {
            Text(text = title, style = MaterialTheme.typography.bodyLarge, color = Ink)
            Text(text = item.subtitleText(), style = MaterialTheme.typography.bodyMedium, color = Muted)
        }
        Switch(
            checked = item.rule.enabled,
            onCheckedChange = onToggle,
            enabled = !unavailable,
            modifier = Modifier.semantics { contentDescription = title }.testTag("automation_rule_switch_${item.rule.id}"),
        )
    }
}

@Composable
private fun AddRuleRow(showProPill: Boolean, onClick: () -> Unit, modifier: Modifier = Modifier, tag: String = "automation_add_rule_row") {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = LocalSettingsRowInset.current, vertical = 14.dp)
            .testTag(tag),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center,
    ) {
        Icon(Icons.Default.Add, contentDescription = null, tint = Accent)
        Text(text = stringResource(R.string.automation_add_rule), style = MaterialTheme.typography.bodyLarge, color = Accent, modifier = Modifier.padding(start = 8.dp))
        if (showProPill) ProPill(modifier = Modifier.padding(start = 8.dp))
    }
}

private fun AutomationTrigger.icon(): ImageVector = when (this) {
    is AutomationTrigger.Schedule -> Icons.Default.Schedule
    is AutomationTrigger.Bluetooth -> Icons.Default.Bluetooth
    is AutomationTrigger.Wifi -> Icons.Default.Wifi
    is AutomationTrigger.Headphones -> Icons.Default.Headphones
    is AutomationTrigger.Battery -> Icons.Default.BatteryStd
}

/** A schedule rule is paused only by the free limit; anything else, by its Pro trigger. */
private fun AutomationRuleItem.upgradeReason(): ProReason =
    if (rule.trigger is AutomationTrigger.Schedule) ProReason.RULE_LIMIT else ProReason.TRIGGER

private fun AutomationRuleItem.missingPermission(): AutomationPermission? = when (availability) {
    RuleAvailability.AVAILABLE, RuleAvailability.NEEDS_PRO -> null
    RuleAvailability.NEEDS_BLUETOOTH -> AutomationPermission.BLUETOOTH_CONNECT
    RuleAvailability.NEEDS_LOCATION -> AutomationPermission.LOCATION
}
