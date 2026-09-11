package com.lumenlauncher.app.ui.home

import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.lumenlauncher.app.R
import com.lumenlauncher.app.ui.components.CardDivider
import com.lumenlauncher.app.ui.theme.Faint
import com.lumenlauncher.app.ui.theme.IconTile
import com.lumenlauncher.app.ui.theme.Ink
import com.lumenlauncher.app.ui.theme.InkInverted
import com.lumenlauncher.app.ui.theme.LumenLauncherTheme
import com.lumenlauncher.app.ui.theme.Muted
import com.lumenlauncher.app.ui.theme.Surface

/**
 * Long-press bottom sheet — opened both by long-pressing the clock and by long-pressing any other
 * empty space on Home (see [com.lumenlauncher.app.ui.home.HomeScreen]'s own root long-press
 * detector). Lets the user enter the combined clock adjust mode (move handle + resize handles
 * together), jump to the clock/calendar style gallery, or jump to the active profile's own settings
 * or launcher-wide settings — each row carries a subheading, same pattern as
 * [com.lumenlauncher.app.ui.profiles.ProfileCarouselScreen]'s own "Launcher settings" row and its
 * per-card "Profile settings" gear.
 */
@Composable
fun ClockAdjustSheet(
    onAdjustClick: () -> Unit,
    onEditStylesClick: () -> Unit,
    onProfileSettingsClick: () -> Unit,
    onLauncherSettingsClick: () -> Unit,
    isOverridden: Boolean,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .testTag("clock_adjust_sheet")
            .background(Surface, shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp))
            .padding(bottom = 24.dp),
    ) {
        // Drag affordance / handle
        Box(
            modifier = Modifier
                .align(Alignment.CenterHorizontally)
                .padding(top = 12.dp, bottom = 8.dp)
                .size(width = 34.dp, height = 4.dp)
                .background(color = Faint, shape = RoundedCornerShape(2.dp)),
        )

        AdjustRow(
            icon = { Icon(painter = painterResource(R.drawable.open_in_full), contentDescription = null, tint = InkInverted, modifier = Modifier.size(18.dp)) },
            label = "Adjust size & position",
            subtitle = "Resize or reposition the clock",
            onClick = onAdjustClick,
            testTag = "clock_adjust_open",
        )
        CardDivider(modifier = Modifier.padding(horizontal = 24.dp))
        AdjustRow(
            icon = { Icon(painter = painterResource(R.drawable.ic_palette_24), contentDescription = null, tint = InkInverted, modifier = Modifier.size(18.dp)) },
            label = if (isOverridden) "Edit Profile clock & calendar styles" else "Edit Default clock & calendar styles",
            subtitle = "Templates, fonts, colors, and alignment",
            onClick = onEditStylesClick,
            testTag = "clock_adjust_edit_styles",
        )
        CardDivider(modifier = Modifier.padding(horizontal = 24.dp))
        AdjustRow(
            icon = { Icon(imageVector = Icons.Default.Settings, contentDescription = null, tint = InkInverted, modifier = Modifier.size(18.dp)) },
            label = "Profile settings",
            subtitle = "Profile specific apps list, dock, clock & calendar settings",
            onClick = onProfileSettingsClick,
            testTag = "clock_adjust_profile_settings",
        )
        CardDivider(modifier = Modifier.padding(horizontal = 24.dp))
        AdjustRow(
            icon = { Icon(imageVector = Icons.Default.Tune, contentDescription = null, tint = InkInverted, modifier = Modifier.size(18.dp)) },
            label = "Launcher settings",
            subtitle = "Appearance, default clock, favorites, drawer and more",
            onClick = onLauncherSettingsClick,
            testTag = "clock_adjust_launcher_settings",
        )
    }
}

@Composable
private fun AdjustRow(
    icon: @Composable () -> Unit,
    label: String,
    subtitle: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    testTag: String? = null,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .then(if (testTag != null) Modifier.testTag(testTag) else Modifier)
            .clickable(onClick = onClick)
            .padding(horizontal = 24.dp, vertical = 13.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        Box(
            modifier = Modifier.size(32.dp).clip(CircleShape).background(IconTile),
            contentAlignment = Alignment.Center,
        ) {
            icon()
        }
        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(text = label, style = MaterialTheme.typography.bodyLarge, color = Ink)
            Text(text = subtitle, style = MaterialTheme.typography.bodyMedium, color = Muted)
        }
    }
}

@Preview(showBackground = true)
@Preview(name = "Dark", showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun ClockAdjustSheetPreview() {
    LumenLauncherTheme {
        ClockAdjustSheet(
            onAdjustClick = {},
            onEditStylesClick = {},
            onProfileSettingsClick = {},
            onLauncherSettingsClick = {},
            isOverridden = false,
        )
    }
}
