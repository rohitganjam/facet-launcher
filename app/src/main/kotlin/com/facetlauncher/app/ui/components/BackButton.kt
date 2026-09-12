package com.facetlauncher.app.ui.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import com.facetlauncher.app.ui.theme.Ink

/**
 * Back button shown on every non-root screen (Settings, Dock picker, Profiles), wired to the
 * same nav-back action as the system back button/gesture — added per a design update
 * requesting an explicit on-screen back affordance everywhere, not just gesture/system-back.
 * Home/Drawer are excluded (swipe down already closes the drawer).
 *
 * Wrapped in [IconButton] (not a bare `Icon.clickable`) so the tap target meets Android's 48dp
 * minimum instead of shrinking to the 24dp glyph plus a few dp of padding.
 */
@Composable
fun BackButton(onClick: () -> Unit, modifier: Modifier = Modifier) {
    IconButton(
        onClick = onClick,
        modifier = modifier.testTag("back_button"),
        colors = IconButtonDefaults.iconButtonColors(contentColor = Ink),
    ) {
        Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
    }
}
