package com.facetlauncher.app.data.model

import android.os.Process
import android.os.UserHandle
import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.ImageBitmap

/**
 * A single launchable app activity, as surfaced by `LauncherApps`. [userHandle] is the real
 * identity — the actual Android user this activity was enumerated from, used for launch routing,
 * Favorites/Dock/Folder dedup, and uninstall cleanup; [profile] is a display-only category
 * derived from it (see [AppProfile]'s own doc) and must never be used in place of [userHandle]
 * for any of those. `@Immutable`, not just a plain `data class` — [UserHandle] is a framework
 * type the Compose compiler can't infer stability for on its own, and this class is passed
 * through Compose pervasively (Home/Drawer/Dock rows).
 */
@Immutable
data class AppInfo(
    val packageName: String,
    val activityName: String,
    val label: String,
    val icon: ImageBitmap?,
    val profile: AppProfile = AppProfile.PERSONAL,
    val userHandle: UserHandle = Process.myUserHandle(),
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
