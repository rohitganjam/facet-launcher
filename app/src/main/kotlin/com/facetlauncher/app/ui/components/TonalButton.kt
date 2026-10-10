package com.facetlauncher.app.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.compositeOver
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.facetlauncher.app.ui.theme.Accent
import com.facetlauncher.app.ui.theme.Muted
import com.facetlauncher.app.ui.theme.Surface

/**
 * A **tonal accent** button — M3's filled-tonal role (an accent-tinted container with accent
 * text) in M3's default fully-rounded (Full, [CircleShape]) button shape. Used for secondary
 * actions that should still read as a real button: the Switch Facets carousel's *Reorder*, the
 * Hub header's *Add*, the Hub empty-state's *Add widget*.
 *
 * This app doesn't map M3's `secondaryContainer` slot, so the tonal fill is an accent tint
 * **composited over the opaque [Surface]** — an opaque colour, so the button reads the same on a
 * translucent scrim (the carousel) or straight on the wallpaper (the Hub) as it does on an opaque
 * card. A hairline accent border keeps the edge defined regardless of what's behind it.
 */
@Composable
fun TonalButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    leadingIcon: ImageVector? = null,
) {
    val container = Accent.copy(alpha = 0.28f).compositeOver(Surface)
    val disabledContainer = Accent.copy(alpha = 0.10f).compositeOver(Surface)
    Button(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier,
        shape = CircleShape,
        elevation = null, // tonal buttons are flat
        border = BorderStroke(1.dp, Accent.copy(alpha = if (enabled) 0.32f else 0.12f)),
        colors = ButtonDefaults.buttonColors(
            containerColor = container,
            contentColor = Accent,
            disabledContainerColor = disabledContainer,
            disabledContentColor = Muted,
        ),
        contentPadding = PaddingValues(
            horizontal = if (leadingIcon != null) 16.dp else 20.dp,
            vertical = 8.dp,
        ),
    ) {
        if (leadingIcon != null) {
            Icon(leadingIcon, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(Modifier.width(8.dp))
        }
        Text(text = text, style = MaterialTheme.typography.labelLarge)
    }
}
