package com.facetlauncher.app.ui.settings.automation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.facetlauncher.app.R
import com.facetlauncher.app.ui.theme.Accent

/** Marks something that needs Pro. A small M3 shape (8dp), like a chip. */
@Composable
internal fun ProPill(modifier: Modifier = Modifier) {
    Text(
        text = stringResource(R.string.automation_pro_pill),
        style = MaterialTheme.typography.labelSmall,
        color = Accent,
        modifier = modifier
            .clip(MaterialTheme.shapes.small)
            .background(Accent.copy(alpha = 0.12f))
            .padding(horizontal = 8.dp, vertical = 2.dp),
    )
}
