package com.facetlauncher.app.ui.components

import android.content.res.Configuration
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.facetlauncher.app.R
import com.facetlauncher.app.ui.theme.Accent
import com.facetlauncher.app.ui.theme.FacetLauncherTheme
import com.facetlauncher.app.ui.theme.Muted

/**
 * A text-only action (Next, Skip, Turn on, Restore...) — M3's `TextButton`, so its press highlight
 * is a pill ([androidx.compose.foundation.shape.CircleShape]) like every other button. [color] is
 * [Accent] for an action and [Muted] for a dismissal; pass a different one only on a dark panel.
 */
@Composable
fun TextActionButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    color: Color = Accent,
    style: TextStyle = MaterialTheme.typography.bodyLarge,
    enabled: Boolean = true,
    /** Colour while [enabled] is false — [Muted] normally; [Accent] for a status that reads as a button (an "Active" mark). */
    disabledColor: Color = Muted,
    leadingIcon: ImageVector? = null,
) {
    TextButton(
        onClick = onClick,
        modifier = modifier,
        enabled = enabled,
        colors = ButtonDefaults.textButtonColors(contentColor = color, disabledContentColor = disabledColor),
    ) {
        if (leadingIcon != null) {
            Icon(imageVector = leadingIcon, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(4.dp))
        }
        Text(text = text, style = style)
    }
}

@Preview(showBackground = true)
@Preview(name = "Dark", showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun TextActionButtonPreview() {
    FacetLauncherTheme {
        TextActionButton(text = stringResource(R.string.action_next), onClick = {})
    }
}
