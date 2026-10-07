package com.facetlauncher.app.ui.components

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
import com.facetlauncher.app.ui.theme.Ink
import com.facetlauncher.app.ui.theme.Muted

/**
 * What a free user sees on reaching a Pro feature (more facets, or a Pro automation trigger or rule). Only
 * explains and dismisses until billing lands, which adds the price and the purchase action here.
 */
@Composable
fun ProUpgradeSheet(reason: ProReason, onDismiss: () -> Unit) {
    ThemedModalBottomSheet(onDismissRequest = onDismiss, skipPartiallyExpanded = true) {
        ProUpgradeContent(reason = reason, onDismiss = onDismiss)
    }
}

@Composable
fun ProUpgradeContent(reason: ProReason, onDismiss: () -> Unit, modifier: Modifier = Modifier) {
    Column(modifier = modifier.fillMaxWidth().padding(horizontal = 24.dp).padding(bottom = 24.dp).testTag("pro_upgrade_sheet")) {
        Text(
            text = stringResource(R.string.pro_upgrade_title),
            style = MaterialTheme.typography.titleLarge,
            color = Ink,
            modifier = Modifier.semantics { heading() },
        )
        val body = when (reason) {
            ProReason.TRIGGER -> R.string.pro_upgrade_trigger
            ProReason.RULE_LIMIT -> R.string.pro_upgrade_rule_limit
            ProReason.FACET_LIMIT -> R.string.pro_upgrade_facet_limit
            ProReason.FACET_LOCKED -> R.string.pro_upgrade_facet_locked
        }
        Text(text = stringResource(body), style = MaterialTheme.typography.bodyLarge, color = Muted, modifier = Modifier.padding(top = 8.dp, bottom = 20.dp))
        SurfaceButton(text = stringResource(R.string.pro_upgrade_dismiss), onClick = onDismiss, modifier = Modifier.testTag("pro_upgrade_dismiss"))
    }
}
