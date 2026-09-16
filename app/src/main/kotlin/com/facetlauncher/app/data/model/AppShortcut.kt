package com.facetlauncher.app.data.model

import android.os.Process
import android.os.UserHandle
import androidx.compose.ui.graphics.ImageBitmap

/** A static/dynamic/pinned "quick action" an app publishes (`LauncherApps.getShortcuts`) — e.g. Gmail's "Compose". */
data class AppShortcut(
    val id: String,
    val packageName: String,
    val label: String,
    val icon: ImageBitmap? = null,
    /** The real profile this shortcut was fetched from — [AppShortcutRepository][com.facetlauncher.app.data.AppShortcutRepository]'s `launchShortcut` needs this to launch in the right one, since by then it's disconnected from the [AppInfo] that originally requested it. */
    val userHandle: UserHandle = Process.myUserHandle(),
)
