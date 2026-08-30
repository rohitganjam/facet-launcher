package com.lumenlauncher.app.ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.lumenlauncher.app.ui.components.CardDivider
import com.lumenlauncher.app.ui.theme.Faint
import com.lumenlauncher.app.ui.theme.Ink
import com.lumenlauncher.app.ui.theme.Muted
import com.lumenlauncher.app.ui.theme.Surface

/**
 * Long-press sheet (`2a`). Four rows per the corrected design screenshot (README's prose was
 * missing "Launcher settings") — all four are functional as of Phase 3.
 */
@Composable
fun LongPressSheet(
    activeProfileName: String,
    activeProfilePosition: Int,
    profileCount: Int,
    onSwitchProfileClick: () -> Unit,
    onEditProfileClick: () -> Unit,
    onLauncherSettingsClick: () -> Unit,
    onChangeWallpaperClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .testTag("long_press_sheet")
            .clickable(enabled = false, onClick = {})
            // M3's ModalBottomSheet default shape (SheetDefaults.ExpandedShape) is
            // extraLarge (28dp), applied top-only — see CLAUDE.md's Material 3 shape section.
            .background(Surface, shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp))
            .padding(bottom = 24.dp),
    ) {
        Box(
            modifier = Modifier
                .align(Alignment.CenterHorizontally)
                .padding(top = 12.dp, bottom = 8.dp)
                .size(width = 34.dp, height = 4.dp)
                .background(color = Faint, shape = RoundedCornerShape(2.dp)),
        )
        SheetRow(
            title = "Switch profile",
            subtitle = "Currently $activeProfileName — $activeProfilePosition of $profileCount",
            enabled = true,
            onClick = onSwitchProfileClick,
            testTag = "long_press_sheet_switch_profile",
        )
        CardDivider(modifier = Modifier.padding(horizontal = 24.dp))
        SheetRow(
            title = "Edit profile",
            subtitle = "Clock style, list content, favorites",
            enabled = true,
            onClick = onEditProfileClick,
            testTag = "long_press_sheet_edit_profile",
        )
        CardDivider(modifier = Modifier.padding(horizontal = 24.dp))
        SheetRow(
            title = "Launcher settings",
            subtitle = "Clock, favorites, drawer, badges",
            enabled = true,
            onClick = onLauncherSettingsClick,
            testTag = "long_press_sheet_launcher_settings",
        )
        CardDivider(modifier = Modifier.padding(horizontal = 24.dp))
        SheetRow(
            title = "Change wallpaper",
            subtitle = "Opens the system picker",
            enabled = true,
            onClick = onChangeWallpaperClick,
            testTag = "long_press_sheet_change_wallpaper",
        )
    }
}

@Composable
private fun SheetRow(
    title: String,
    subtitle: String,
    enabled: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    testTag: String? = null,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .then(if (testTag != null) Modifier.testTag(testTag) else Modifier)
            .alpha(if (enabled) 1f else 0.4f)
            .clickable(enabled = enabled, onClick = onClick)
            .padding(horizontal = 24.dp, vertical = 13.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(text = title, style = MaterialTheme.typography.bodyLarge, color = Ink)
            Text(text = subtitle, style = MaterialTheme.typography.bodyMedium, color = Muted)
        }
        // Every row here opens another screen (or the system wallpaper/settings picker) — a
        // right-side chevron is this app's general convention for any row that navigates away.
        Icon(imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = null, tint = Muted)
    }
}
