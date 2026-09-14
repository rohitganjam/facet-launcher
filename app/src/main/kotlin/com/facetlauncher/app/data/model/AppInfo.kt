package com.facetlauncher.app.data.model

import androidx.compose.ui.graphics.ImageBitmap

/** A single launchable app activity, as surfaced by `LauncherApps`. */
data class AppInfo(
    val packageName: String,
    val activityName: String,
    val label: String,
    val icon: ImageBitmap?,
    val profile: AppProfile = AppProfile.PERSONAL,
)

/**
 * This launcher's own `applicationId` (`app/build.gradle.kts`) — a real, installed, launchable
 * app like any other, but one that would never make sense to show in its own app lists (Home's
 * Recents/Most Used, the App Drawer): launching yourself from within yourself is meaningless.
 * A plain compile-time constant rather than reading it from `Context.packageName` — the
 * `data/`/`domain/` classes that need it ([UsageStatsRepository], [GetInstalledAppsUseCase])
 * would otherwise need `Context` injected purely to exclude one known, fixed string, and
 * [GetInstalledAppsUseCase] specifically isn't supposed to touch Android framework APIs at all.
 */
const val FACET_LAUNCHER_PACKAGE_NAME = "com.facetlauncher.app"
