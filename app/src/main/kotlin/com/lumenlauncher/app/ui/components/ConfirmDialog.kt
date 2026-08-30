package com.lumenlauncher.app.ui.components

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import com.lumenlauncher.app.ui.theme.Accent
import com.lumenlauncher.app.ui.theme.ErrorColor
import com.lumenlauncher.app.ui.theme.Ink
import com.lumenlauncher.app.ui.theme.Muted
import com.lumenlauncher.app.ui.theme.Surface

/** Shared confirm/cancel dialog — used for profile deletion (F4 requires confirmation before it completes). */
@Composable
fun ConfirmDialog(
    title: String,
    message: String,
    confirmLabel: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
    destructive: Boolean = true,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        modifier = modifier.testTag("confirm_dialog"),
        shape = MaterialTheme.shapes.extraLarge,
        // AlertDialog's default containerColor resolves to Material3's own
        // surfaceContainerHigh tone, which LumenLightColorScheme never maps — left at the
        // default it renders a faintly different off-white than every other Surface-colored
        // card in the app. Force it explicitly instead of relying on M3's fallback.
        containerColor = Surface,
        title = { Text(text = title, style = MaterialTheme.typography.titleMedium, color = Ink) },
        text = { Text(text = message, style = MaterialTheme.typography.bodyMedium, color = Muted) },
        confirmButton = {
            TextButton(onClick = onConfirm, modifier = Modifier.testTag("confirm_dialog_confirm")) {
                Text(text = confirmLabel, style = MaterialTheme.typography.bodyLarge, color = if (destructive) ErrorColor else Accent)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss, modifier = Modifier.testTag("confirm_dialog_cancel")) {
                Text(text = "Cancel", style = MaterialTheme.typography.bodyLarge, color = Muted)
            }
        },
    )
}
