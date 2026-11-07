package com.facetlauncher.app.domain

import com.facetlauncher.app.data.model.AppInfo
import javax.inject.Inject

/**
 * Apps installed or updated within the last [WINDOW_MILLIS], newest change first — the search sheet's
 * "Recently installed or updated" section. Unlike [RecentlyInstalledAppsUseCase], an update counts.
 */
class RecentlyChangedAppsUseCase @Inject constructor() {
    operator fun invoke(apps: List<AppInfo>, nowMillis: Long = System.currentTimeMillis()): List<AppInfo> =
        apps.filter { nowMillis - it.changedAt() <= WINDOW_MILLIS }
            .sortedByDescending { it.changedAt() }

    private fun AppInfo.changedAt() = maxOf(firstInstallTime, lastUpdateTime)

    private companion object {
        const val WINDOW_MILLIS = 72 * 60 * 60 * 1000L
    }
}
