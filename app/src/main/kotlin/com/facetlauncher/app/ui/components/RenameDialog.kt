package com.facetlauncher.app.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import com.facetlauncher.app.ui.theme.Accent
import com.facetlauncher.app.ui.theme.Hairline
import com.facetlauncher.app.ui.theme.Ink
import com.facetlauncher.app.ui.theme.Muted
import com.facetlauncher.app.ui.theme.Surface

/**
 * Rename dialog (`4m`) — generic so it's reusable for app-label rename later (F12/Phase 4)
 * without change: title/explanation, the value on an editable field with a Reset-to-original
 * affordance, Cancel/Save.
 */
@Composable
fun RenameDialog(
    title: String,
    explanation: String,
    initialValue: String,
    onSave: (String) -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var value by remember(initialValue) { mutableStateOf(initialValue) }

    AlertDialog(
        onDismissRequest = onDismiss,
        modifier = modifier.testTag("rename_dialog"),
        shape = MaterialTheme.shapes.extraLarge,
        // See ConfirmDialog — AlertDialog's default containerColor doesn't match this app's
        // Surface token unless forced explicitly.
        containerColor = Surface,
        title = { Text(text = title, style = MaterialTheme.typography.titleMedium, color = Ink) },
        text = {
            Column {
                Text(text = explanation, style = MaterialTheme.typography.bodyMedium, color = Muted)
                OutlinedTextField(
                    value = value,
                    onValueChange = { value = it },
                    singleLine = true,
                    // OutlinedTextField's own defaults resolve most colors from color-scheme
                    // slots FacetLightColorScheme never sets (e.g. primaryContainer-derived
                    // tones) — spelled out explicitly so it reads as this app's field, not a
                    // stock Material one.
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Ink,
                        unfocusedTextColor = Ink,
                        focusedBorderColor = Accent,
                        unfocusedBorderColor = Hairline,
                        cursorColor = Accent,
                    ),
                    modifier = Modifier.fillMaxWidth().testTag("rename_dialog_field"),
                )
                if (value != initialValue) {
                    TextButton(onClick = { value = initialValue }, modifier = Modifier.testTag("rename_dialog_reset")) {
                        Text(text = "Reset", style = MaterialTheme.typography.bodyMedium, color = Muted)
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = { onSave(value.trim()) },
                enabled = value.isNotBlank(),
                modifier = Modifier.testTag("rename_dialog_save"),
            ) {
                Text(text = "Save", style = MaterialTheme.typography.bodyLarge, color = Accent)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss, modifier = Modifier.testTag("rename_dialog_cancel")) {
                Text(text = "Cancel", style = MaterialTheme.typography.bodyLarge, color = Muted)
            }
        },
    )
}
