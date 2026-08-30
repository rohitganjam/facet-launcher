package com.lumenlauncher.app.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.lumenlauncher.app.ui.theme.Ink

/**
 * Back button shown on every non-root screen (Settings, Dock picker, Profiles), wired to the
 * same nav-back action as the system back button/gesture — added per a design update
 * requesting an explicit on-screen back affordance everywhere, not just gesture/system-back.
 * Home/Drawer are excluded (swipe down already closes the drawer).
 */
@Composable
fun BackButton(onClick: () -> Unit, modifier: Modifier = Modifier) {
    Icon(
        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
        contentDescription = "Back",
        tint = Ink,
        modifier = modifier
            .testTag("back_button")
            .clickable(onClick = onClick)
            .padding(horizontal = 4.dp, vertical = 2.dp),
    )
}
