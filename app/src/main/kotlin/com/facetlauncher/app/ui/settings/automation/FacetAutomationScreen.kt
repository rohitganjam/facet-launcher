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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.BatteryStd
import androidx.compose.material.icons.filled.Bluetooth
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.Headphones
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
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
import com.facetlauncher.app.data.local.FacetEntity
import com.facetlauncher.app.data.model.AutomationTrigger
import com.facetlauncher.app.domain.AutomationRuleItem
import com.facetlauncher.app.domain.AutomationStatus
import com.facetlauncher.app.domain.FacetAutomationScreenState
import com.facetlauncher.app.domain.RuleAvailability
import com.facetlauncher.app.ui.components.BackButton
import com.facetlauncher.app.ui.components.CardDivider
import com.facetlauncher.app.ui.components.SettingsCard
import com.facetlauncher.app.ui.components.StickyHeaderLayout
import com.facetlauncher.app.ui.components.dashedBorder
import com.facetlauncher.app.ui.facets.SectionHeader
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
    onAddRule: () -> Unit,
    onEditRule: (ruleId: Long) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: FacetAutomationViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
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
        onAddRule = onAddRule,
        onEditRule = onEditRule,
        modifier = modifier,
    )
}

@Composable
internal fun FacetAutomationContent(
    state: FacetAutomationScreenState?,
    onBack: () -> Unit,
    onToggleRule: (ruleId: Long, enabled: Boolean) -> Unit,
    onAddRule: () -> Unit,
    onEditRule: (ruleId: Long) -> Unit,
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
                    item { SectionHeader(stringResource(R.string.automation_section_rules)) }
                    item { RulesCard(state, onToggleRule, onAddRule, onEditRule) }
                    item { SectionHeader(stringResource(R.string.automation_section_other_apps), modifier = Modifier.padding(top = 20.dp)) }
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
private fun StatusLine(status: AutomationStatus, modifier: Modifier = Modifier) {
    val text = when (status) {
        is AutomationStatus.Driving -> stringResource(R.string.automation_status_driving, status.facetName, status.rule.rule.trigger.titleText())
        is AutomationStatus.ManualOverride ->
            stringResource(R.string.automation_status_override, status.facetName, status.pausedRule.rule.trigger.titleText())
    }
    Text(
        text = text,
        style = MaterialTheme.typography.bodyMedium,
        color = Ink,
        modifier = modifier
            .fillMaxWidth()
            .padding(bottom = 4.dp)
            .clip(MaterialTheme.shapes.medium)
            .background(Accent.copy(alpha = 0.10f))
            .padding(horizontal = 14.dp, vertical = 12.dp)
            .testTag("automation_status"),
    )
}

@Composable
private fun RulesCard(
    state: FacetAutomationScreenState,
    onToggleRule: (Long, Boolean) -> Unit,
    onAddRule: () -> Unit,
    onEditRule: (Long) -> Unit,
) {
    if (state.items.isEmpty()) {
        EmptyStrip(onAddRule = onAddRule)
        return
    }
    SettingsCard {
        state.items.forEachIndexed { index, item ->
            if (index > 0) CardDivider()
            RuleRow(item = item, onToggle = { onToggleRule(item.rule.id, it) }, onClick = { onEditRule(item.rule.id) })
        }
        if (state.canAddRule) {
            CardDivider()
            AddRuleRow(onClick = onAddRule)
        }
    }
}

@Composable
private fun RuleRow(item: AutomationRuleItem, onToggle: (Boolean) -> Unit, onClick: () -> Unit, modifier: Modifier = Modifier) {
    val unavailable = item.availability != RuleAvailability.AVAILABLE
    val title = item.rule.trigger.titleText()
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .alpha(if (unavailable || !item.rule.enabled) 0.55f else 1f)
            .padding(vertical = 13.dp)
            .testTag("automation_rule_row_${item.rule.id}"),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Icon(
            imageVector = if (unavailable) Icons.Default.ErrorOutline else item.rule.trigger.icon(),
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
private fun AddRuleRow(onClick: () -> Unit, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 14.dp)
            .testTag("automation_add_rule_row"),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center,
    ) {
        Icon(Icons.Default.Add, contentDescription = null, tint = Accent)
        Text(text = stringResource(R.string.automation_add_rule), style = MaterialTheme.typography.bodyLarge, color = Accent, modifier = Modifier.padding(start = 8.dp))
    }
}

@Composable
private fun EmptyStrip(onAddRule: () -> Unit, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .dashedBorder(color = Ink.copy(alpha = 0.16f), cornerRadius = 10.dp)
            .padding(horizontal = 13.dp, vertical = 11.dp)
            .testTag("automation_empty"),
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Text(text = stringResource(R.string.automation_empty), style = MaterialTheme.typography.bodyMedium, color = Muted)
        Text(
            text = stringResource(R.string.automation_add_rule),
            style = MaterialTheme.typography.bodyMedium,
            color = Accent,
            modifier = Modifier.clickable(onClick = onAddRule).padding(vertical = 4.dp).testTag("automation_empty_add"),
        )
    }
}

@Composable
private fun OtherAppsCard() {
    SettingsCard {
        Text(
            text = stringResource(R.string.automation_other_apps_note),
            style = MaterialTheme.typography.bodyMedium,
            color = Muted,
            modifier = Modifier.padding(vertical = 12.dp).testTag("automation_other_apps_note"),
        )
    }
}

private fun AutomationTrigger.icon(): ImageVector = when (this) {
    is AutomationTrigger.Schedule -> Icons.Default.Schedule
    is AutomationTrigger.Bluetooth -> Icons.Default.Bluetooth
    is AutomationTrigger.Wifi -> Icons.Default.Wifi
    is AutomationTrigger.Headphones -> Icons.Default.Headphones
    is AutomationTrigger.Battery -> Icons.Default.BatteryStd
}
