package com.facetlauncher.app.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.facetlauncher.app.ui.theme.Ink
import com.facetlauncher.app.ui.theme.Muted
import com.facetlauncher.app.ui.theme.Surface

/**
 * A **surface** secondary-action button — this app's opaque [Surface] fill with [Ink] text, in the same
 * fully-rounded ([CircleShape]) shape [TonalButton] uses. For an
 * acknowledgement/dismissal action that shouldn't read as accent-tinted — [TonalButton]'s accent tint
 * implies "the primary thing to do here," which doesn't fit e.g. [GestureHintOverlay]'s "Got it".
 * Its fill is nearly the same color as the backdrops it sits on (a 1.05–1.3:1 difference), so the
 * edge carries the contrast instead: a 1.5dp [Muted] border (≥3:1 against the overlay in both
 * themes) plus a soft elevation. Reads correctly over a translucent scrim, same as [TonalButton].
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
        shape = CircleShape,
        elevation = ButtonDefaults.buttonElevation(defaultElevation = 3.dp, pressedElevation = 1.dp),
        border = BorderStroke(1.5.dp, Muted),
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
