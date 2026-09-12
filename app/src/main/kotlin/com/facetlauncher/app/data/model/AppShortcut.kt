package com.facetlauncher.app.data.model

import androidx.compose.ui.graphics.ImageBitmap

/** A static/dynamic/pinned "quick action" an app publishes (`LauncherApps.getShortcuts`) — e.g. Gmail's "Compose". */
data class AppShortcut(
    val id: String,
    val packageName: String,
    val label: String,
    val icon: ImageBitmap? = null,
)
