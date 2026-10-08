package com.facetlauncher.app.ui.settings.automation

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.facetlauncher.app.R
import com.facetlauncher.app.domain.AutomationStatus
import com.facetlauncher.app.ui.components.SettingsCard
import com.facetlauncher.app.ui.components.dashedBorder
import com.facetlauncher.app.ui.theme.Accent
import com.facetlauncher.app.ui.theme.Ink
import com.facetlauncher.app.ui.theme.Muted

@Composable
internal fun StatusLine(status: AutomationStatus, modifier: Modifier = Modifier) {
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
internal fun FreePlanStrip(onSeePro: () -> Unit, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .padding(top = 10.dp)
            .fillMaxWidth()
            .dashedBorder(color = Ink.copy(alpha = 0.16f), cornerRadius = 12.dp)
            .padding(horizontal = 13.dp, vertical = 11.dp)
            .testTag("automation_free_strip"),
        verticalArrangement = Arrangement.spacedBy(4.dp),
        horizontalAlignment = Alignment.End,
    ) {
        Text(text = stringResource(R.string.automation_free_strip), modifier = Modifier.fillMaxWidth(), style = MaterialTheme.typography.bodyMedium, color = Muted)
        Text(
            text = stringResource(R.string.automation_see_pro),
            style = MaterialTheme.typography.bodyMedium,
            color = Accent,
            modifier = Modifier.clickable(onClick = onSeePro).padding(vertical = 4.dp).testTag("automation_see_pro"),
        )
    }
}

@Composable
internal fun EmptyStrip(modifier: Modifier = Modifier) {
    Text(
        text = stringResource(R.string.automation_empty),
        style = MaterialTheme.typography.bodyMedium,
        color = Muted,
        modifier = modifier
            .fillMaxWidth()
            .dashedBorder(color = Ink.copy(alpha = 0.16f), cornerRadius = 12.dp)
            .padding(horizontal = 13.dp, vertical = 11.dp)
            .testTag("automation_empty"),
    )
}

@Composable
internal fun OtherAppsCard() {
    SettingsCard {
        Text(
            text = stringResource(R.string.automation_other_apps_note),
            style = MaterialTheme.typography.bodyMedium,
            color = Muted,
            modifier = Modifier.padding(vertical = 12.dp).testTag("automation_other_apps_note"),
        )
    }
}
