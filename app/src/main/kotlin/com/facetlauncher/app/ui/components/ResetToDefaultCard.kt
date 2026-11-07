package com.facetlauncher.app.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.clickable
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.facetlauncher.app.R
import com.facetlauncher.app.ui.theme.Ink
import com.facetlauncher.app.ui.theme.Muted

/**
 * A facet screen's "Reset to default" action: copies the launcher-wide default over what this screen owns, after a confirmation that
 * names what is lost. [confirmMessage] says what the reset replaces.
 */
@Composable
fun ResetToDefaultCard(
    subtitle: String,
    confirmMessage: String,
    onReset: () -> Unit,
    testTagPrefix: String,
    modifier: Modifier = Modifier,
) {
    var confirming by rememberSaveable { mutableStateOf(false) }
    SettingsCard(modifier = modifier, fullBleedRows = true) {
        Row(
            modifier = Modifier
                .testTag("${testTagPrefix}_reset_row")
                .fillMaxWidth()
                .clickable { confirming = true }
                .padding(horizontal = LocalSettingsRowInset.current, vertical = 13.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column {
                Text(text = stringResource(R.string.reset_to_default_title), style = MaterialTheme.typography.bodyLarge, color = Ink)
                Text(text = subtitle, style = MaterialTheme.typography.bodyMedium, color = Muted)
            }
        }
    }
    if (confirming) {
        ConfirmDialog(
            title = stringResource(R.string.reset_to_default_title),
            message = confirmMessage,
            confirmLabel = stringResource(R.string.reset_to_default_confirm),
            onConfirm = {
                confirming = false
                onReset()
            },
            onDismiss = { confirming = false },
        )
    }
}

/** The line on a launcher-wide default screen saying what its values are for. */
@Composable
fun DefaultsNote(modifier: Modifier = Modifier) {
    Text(
        text = stringResource(R.string.defaults_screen_note),
        style = MaterialTheme.typography.bodyMedium,
        color = Muted,
        modifier = modifier.testTag("defaults_screen_note").padding(horizontal = LocalSettingsRowInset.current),
    )
}
