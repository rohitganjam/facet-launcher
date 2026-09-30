package com.facetlauncher.app.domain

import com.facetlauncher.app.data.model.AppInfo
import javax.inject.Inject

/**
 * Apps installed within the last [WINDOW_MILLIS] — the App Drawer's and Private Space's own
 * "Recently installed" star item. Filters on [AppInfo.firstInstallTime] only, never
 * [AppInfo.lastUpdateTime] — an ordinary update to an app installed long ago must not qualify.
 * [nowMillis] is a parameter (not read internally) so the 72-hour boundary is directly testable.
 */
class RecentlyInstalledAppsUseCase @Inject constructor() {
    operator fun invoke(apps: List<AppInfo>, nowMillis: Long = System.currentTimeMillis()): List<AppInfo> =
        apps.filter { nowMillis - it.firstInstallTime <= WINDOW_MILLIS }
            .sortedByDescending { it.firstInstallTime }

    private companion object {
        const val WINDOW_MILLIS = 72 * 60 * 60 * 1000L
    }
}
