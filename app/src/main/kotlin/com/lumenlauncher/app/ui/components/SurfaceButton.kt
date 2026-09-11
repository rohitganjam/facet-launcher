package com.lumenlauncher.app.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.lumenlauncher.app.ui.theme.Hairline
import com.lumenlauncher.app.ui.theme.Ink
import com.lumenlauncher.app.ui.theme.Muted
import com.lumenlauncher.app.ui.theme.Surface

/**
 * A **surface** secondary-action button — this app's opaque [Surface] fill with [Ink] text and a
 * hairline border, in the same M3 Expressive square shape ([MaterialTheme.shapes.medium], 12dp)
 * [TonalButton] uses. For an acknowledgement/dismissal action that shouldn't read as accent-tinted
 * — [TonalButton]'s accent tint implies "the primary thing to do here," which doesn't fit e.g.
 * [GestureHintOverlay]'s "Got it". Reads correctly over a translucent scrim, same as [TonalButton].
 */
@Composable
fun SurfaceButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
) {
    Button(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier,
        shape = MaterialTheme.shapes.large,
        elevation = null,
        border = BorderStroke(1.dp, Hairline),
        colors = ButtonDefaults.buttonColors(
            containerColor = Surface,
            contentColor = Ink,
            disabledContainerColor = Surface,
            disabledContentColor = Muted,
        ),
        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 12.dp),
    ) {
        Text(text = text, style = MaterialTheme.typography.labelLarge)
    }
}
