package com.facetlauncher.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.facetlauncher.app.ui.theme.LocalIsDarkTheme
import com.facetlauncher.app.ui.theme.SettingsSectionHue

val SettingsIconBadgeSize = 40.dp

/** Space a divider skips to clear a [SettingsIconBadge] plus its 16dp gap to the row text. */
val SettingsIconBadgeInset = SettingsIconBadgeSize + 16.dp

/** Leading icon of a Settings row: the section's hue as a glyph on a low-alpha circle of the same hue. */
@Composable
fun SettingsIconBadge(icon: ImageVector, hue: SettingsSectionHue, modifier: Modifier = Modifier) {
    val tint = hue.color
    val circleAlpha = if (LocalIsDarkTheme.current) 0.24f else 0.14f
    Box(
        modifier = modifier
            .testTag("settings_icon_badge")
            .size(SettingsIconBadgeSize)
            .clip(CircleShape)
            .background(tint.copy(alpha = circleAlpha)),
        contentAlignment = Alignment.Center,
    ) {
        Icon(imageVector = icon, contentDescription = null, tint = tint, modifier = Modifier.size(22.dp))
    }
}
