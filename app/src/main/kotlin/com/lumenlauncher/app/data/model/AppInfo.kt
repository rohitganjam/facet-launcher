package com.lumenlauncher.app.data.model

import androidx.compose.ui.graphics.ImageBitmap

/** A single launchable app activity, as surfaced by `LauncherApps`. */
data class AppInfo(
    val packageName: String,
    val activityName: String,
    val label: String,
    val icon: ImageBitmap?,
)
