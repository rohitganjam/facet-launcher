package com.facetlauncher.app.ui.settings.automation

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.facetlauncher.app.R
import com.facetlauncher.app.data.model.ProReason
import com.facetlauncher.app.ui.components.SurfaceButton
import com.facetlauncher.app.ui.components.ThemedModalBottomSheet
import com.facetlauncher.app.ui.theme.Ink
import com.facetlauncher.app.ui.theme.Muted

/**
 * What a free user sees on reaching a Pro feature of facet automation. Only explains and dismisses:
 * the purchase action arrives with the billing plan, which owns the real sheet.
 */
@Composable
internal fun UpgradeSheet(reason: ProReason, onDismiss: () -> Unit) {
    ThemedModalBottomSheet(onDismissRequest = onDismiss, skipPartiallyExpanded = true) {
        UpgradeContent(reason = reason, onDismiss = onDismiss)
    }
}

@Composable
internal fun UpgradeContent(reason: ProReason, onDismiss: () -> Unit, modifier: Modifier = Modifier) {
    Column(modifier = modifier.fillMaxWidth().padding(horizontal = 24.dp).padding(bottom = 24.dp).testTag("automation_upgrade_sheet")) {
        Text(
            text = stringResource(R.string.automation_upgrade_title),
            style = MaterialTheme.typography.titleLarge,
            color = Ink,
            modifier = Modifier.semantics { heading() },
        )
        val body = when (reason) {
            ProReason.TRIGGER -> R.string.automation_upgrade_trigger
            ProReason.RULE_LIMIT -> R.string.automation_upgrade_limit
        }
        Text(text = stringResource(body), style = MaterialTheme.typography.bodyLarge, color = Muted, modifier = Modifier.padding(top = 8.dp, bottom = 20.dp))
        SurfaceButton(text = stringResource(R.string.automation_upgrade_dismiss), onClick = onDismiss, modifier = Modifier.testTag("automation_upgrade_dismiss"))
    }
}
