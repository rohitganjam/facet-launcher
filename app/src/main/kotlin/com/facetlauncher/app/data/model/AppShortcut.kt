package com.facetlauncher.app.data.model

import androidx.compose.ui.graphics.ImageBitmap

/** A static/dynamic/pinned "quick action" an app publishes (`LauncherApps.getShortcuts`) — e.g. Gmail's "Compose". */
data class AppShortcut(
    val id: String,
    val packageName: String,
    val label: String,
    val icon: ImageBitmap? = null,
    /** Which profile this shortcut was fetched from — [AppShortcutRepository][com.facetlauncher.app.data.AppShortcutRepository]'s `launchShortcut` needs this to resolve the right `UserHandle`, since by then it's disconnected from the [AppInfo] that originally requested it. */
    val profile: AppProfile = AppProfile.PERSONAL,
)
