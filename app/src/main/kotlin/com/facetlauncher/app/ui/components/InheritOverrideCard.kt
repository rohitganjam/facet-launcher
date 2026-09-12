package com.facetlauncher.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.facetlauncher.app.ui.theme.Accent
import com.facetlauncher.app.ui.theme.Hairline
import com.facetlauncher.app.ui.theme.Ink
import com.facetlauncher.app.ui.theme.Muted
import com.facetlauncher.app.ui.theme.Surface

/**
 * The two-radio-row "Inherit default" / "Override for this profile" switch README specs at `3f`
 * for per-profile overrides — reused wherever a profile can diverge from a global default
 * (Clock card's 24-hour time, Calendar settings' show-all-day-events).
 */
@Composable
fun InheritOverrideCard(
    overriding: Boolean,
    onOverridingChanged: (Boolean) -> Unit,
    testTagPrefix: String,
    modifier: Modifier = Modifier,
    inheritSubtitle: String = "Follows the launcher-wide default",
) {
    SettingsCard(modifier = modifier) {
        RadioOptionRow(
            title = "Inherit default",
            subtitle = inheritSubtitle,
            selected = !overriding,
            onClick = { onOverridingChanged(false) },
            testTag = "${testTagPrefix}_inherit_row",
        )
        CardDivider()
        RadioOptionRow(
            title = "Override for this profile",
            subtitle = "Changes here don't affect other profiles",
            selected = overriding,
            onClick = { onOverridingChanged(true) },
            testTag = "${testTagPrefix}_override_row",
        )
    }
}

@Composable
private fun RadioOptionRow(
    title: String,
    subtitle: String,
    selected: Boolean,
    onClick: () -> Unit,
    testTag: String,
    modifier: Modifier = Modifier,
    tinted: Boolean = false,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(if (tinted) Accent.copy(alpha = 0.04f) else Color.Transparent)
            .testTag(testTag)
            .clickable(onClick = onClick)
            .padding(vertical = 13.dp),
        verticalAlignment = androidx.compose.ui.Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        RadioDot(selected = selected)
        Column {
            Text(text = title, style = MaterialTheme.typography.bodyLarge, color = Ink)
            Text(text = subtitle, style = MaterialTheme.typography.bodyMedium, color = Muted)
        }
    }
}

@Composable
private fun RadioDot(selected: Boolean, modifier: Modifier = Modifier) {
    val ringColor = if (selected) Accent else Hairline
    Column(
        modifier = modifier
            .size(18.dp)
            .clip(CircleShape)
            .background(Surface)
            .drawBehind {
                drawRoundRect(color = ringColor, style = Stroke(width = 1.5.dp.toPx()), cornerRadius = CornerRadius(size.minDimension / 2f))
            },
    ) {
        if (selected) {
            Column(modifier = Modifier.padding(4.dp).fillMaxSize().clip(CircleShape).background(Accent)) {}
        }
    }
}
